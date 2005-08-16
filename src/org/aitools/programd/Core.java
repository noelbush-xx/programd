/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.SAXParser;

import org.aitools.programd.bot.Bot;
import org.aitools.programd.bot.Bots;
import org.aitools.programd.graph.Graphmaster;
import org.aitools.programd.graph.Nodemapper;
import org.aitools.programd.interpreter.Interpreter;
import org.aitools.programd.logging.LogUtils;
import org.aitools.programd.multiplexor.Multiplexor;
import org.aitools.programd.multiplexor.PredicateMaster;
import org.aitools.programd.parser.AIMLReader;
import org.aitools.programd.parser.BotsConfigurationFileParser;
import org.aitools.programd.processor.ProcessorException;
import org.aitools.programd.processor.aiml.AIMLProcessorRegistry;
import org.aitools.programd.processor.botconfiguration.BotConfigurationElementProcessorRegistry;
import org.aitools.programd.responder.Responder;
import org.aitools.programd.responder.TextResponder;
import org.aitools.programd.util.AIMLWatcher;
import org.aitools.programd.util.ClassUtils;
import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.FileManager;
import org.aitools.programd.util.Heart;
import org.aitools.programd.util.ManagedProcesses;
import org.aitools.programd.util.UnspecifiedParameterError;
import org.aitools.programd.util.URITools;
import org.aitools.programd.util.UserError;
import org.aitools.programd.util.XMLKit;
import org.xml.sax.SAXException;
import org.w3c.dom.Document;

/**
 * The "core" of Program D, independent of any interfaces.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class Core extends Thread
{
    // Public access informational constants.

    /** Copyright notice. */
    public static final String[] COPYLEFT = { "Program D",
            "This program is free software; you can redistribute it and/or",
            "modify it under the terms of the GNU General Public License",
            "as published by the Free Software Foundation; either version 2",
            "of the License, or (at your option) any later version." };

    /** Version of this package. */
    public static final String VERSION = "4.5.1";

    /** Build identifier. */
    public static final String BUILD = "";

    /** The namespace URI of the bot configuration. */
    public static final String BOT_CONFIG_SCHEMA_URI = "http://aitools.org/programd/4.5/bot-configuration";

    /** The namespace URI of the plugin configuration. */
    public static final String PLUGIN_CONFIG_SCHEMA_URI = "http://aitools.org/programd/4.5/plugins";

    /** The location of the plugin configuration schema. */
    private static final String PLUGIN_CONFIG_SCHEMA_LOCATION = "resources/schema/plugins.xsd";

    /** The Settings. */
    protected CoreSettings settings;

    /** The Graphmaster. */
    private Graphmaster graphmaster;

    /** The Multiplexor. */
    private Multiplexor multiplexor;

    /** The PredicateMaster. */
    private PredicateMaster predicateMaster;

    /** The bots. */
    private Bots bots;

    /** The processes. */
    private ManagedProcesses processes;

    /** The bot configuration element processor registry. */
    private BotConfigurationElementProcessorRegistry botConfigurationElementProcessorRegistry;

    /** The SAXParser used in loading AIML. */
    private SAXParser parser;

    /** The AIML processor registry. */
    private AIMLProcessorRegistry aimlProcessorRegistry;

    /** An AIMLWatcher. */
    private AIMLWatcher aimlWatcher;

    /** An interpreter. */
    private Interpreter interpreter;

    /** The logger for the Core. */
    private Logger logger;

    /** Load time marker. */
    private boolean loadtime;

    /** Name of the local host. */
    private String hostname;

    /** A heart. */
    private Heart heart;

    /** The plugin config. */
    private Document pluginConfig;

    /** The status of the Core. */
    protected Status status = Status.NOT_STARTED;

    /** Possible values for status. */
    public static enum Status
    {
        /** The Core has not yet started. */
        NOT_STARTED,

        /** The Core has been properly intialized (internal, by constructor). */
        INITIALIZED,

        /** The Core has been properly set up (external, by user). */
        READY,

        /** The Core has shut down. */
        SHUT_DOWN,

        /** The Core has crashed. */
        CRASHED
    }
    
    /** The AIML schema location. */
    private static final String AIML_SCHEMA_LOCATION = "resources/schema/AIML.xsd";

    // Convenience constants.
    private static final String EMPTY_STRING = "";

    /** The <code>*</code> wildcard. */
    public static final String ASTERISK = "*";

    /**
     * Initializes a new Core object with the properties from the given file.
     * 
     * @param propertiesPath
     */
    public Core(String propertiesPath)
    {
        super("Core");
        this.settings = new CoreSettings(propertiesPath);
        FileManager.setRootPath(URITools.contextualize(URITools.createValidURL(propertiesPath),
                this.settings.getRootDirectory()));
        initialize();
    }

    /**
     * Initializes a new Core object with the given CoreSettings object.
     * 
     * @param settingsToUse the settings to use
     */
    public Core(CoreSettings settingsToUse)
    {
        super("Core");
        this.settings = settingsToUse;
        FileManager.setRootPath(URITools.contextualize(FileManager.getWorkingDirectory(),
                this.settings.getRootDirectory()));
        initialize();
    }

    /**
     * Initializes a new Core object with default property values.
     */
    public Core()
    {
        super("Core");
        this.settings = new CoreSettings();
        FileManager.setRootPath(URITools.contextualize(FileManager.getWorkingDirectory(),
                this.settings.getRootDirectory()));
        initialize();
    }

    /**
     * Initialization common to all constructors.
     */
    private void initialize()
    {
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
        this.aimlProcessorRegistry = new AIMLProcessorRegistry();
        this.botConfigurationElementProcessorRegistry = new BotConfigurationElementProcessorRegistry();
        this.parser = XMLKit.getSAXParser(URITools.getResource(AIML_SCHEMA_LOCATION), "AIML");
        this.graphmaster = new Graphmaster(this.settings);
        this.bots = new Bots();
        this.processes = new ManagedProcesses(this);

        // Get an instance of the settings-specified Multiplexor.
        this.multiplexor = ClassUtils.getSubclassInstance(Multiplexor.class, this.settings.getMultiplexorClassname(),
                "Multiplexor", this);

        // Initialize the PredicateMaster and attach it to the Multiplexor.
        this.predicateMaster = new PredicateMaster(this);
        this.multiplexor.attach(this.predicateMaster);

        // Set up loggers based on the settings.
        this.logger = setupLogger("programd", this.settings.getActivityLogPattern());
        this.logger.setLevel(Level.ALL);

        // Get the hostname (used occasionally).
        try
        {
            this.hostname = InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e)
        {
            this.hostname = "unknown-host";
        }

        // Load the plugin config.
        try
        {
            this.pluginConfig = XMLKit.getDocumentBuilder(URITools.getResource(PLUGIN_CONFIG_SCHEMA_LOCATION),
                    "plugin configuration").parse(
                    URITools.createValidURL(this.settings.getConfLocationPlugins())
                            .toExternalForm());
        }
        catch (IOException e)
        {
            throw new DeveloperError("IO error trying to read plugin configuration.", e);
        }
        catch (SAXException e)
        {
            throw new DeveloperError("Error trying to parse plugin configuration.", e);
        }

        // Set the status indicator.
        this.status = Status.INITIALIZED;
    }

    /**
     * Sets up the Core and prepares it to work. This should be called by every
     * Core user after creating the Core but before starting it.
     */
    public void setup()
    {
        if (this.status != Status.INITIALIZED)
        {
            throw new DeveloperError(new IllegalStateException(
                    "Core has not been initialized; cannot set up."));
        }
        this.logger.log(Level.INFO, "Starting Program D version " + VERSION + BUILD + '.');
        this.logger.log(Level.INFO, "Using Java VM " + System.getProperty("java.vm.version")
                + " from " + System.getProperty("java.vendor"));
        Runtime runtime = Runtime.getRuntime();
        this.logger.log(Level.INFO, "On " + System.getProperty("os.name") + " version "
                + System.getProperty("os.version") + " (" + System.getProperty("os.arch")
                + ") with " + runtime.availableProcessors() + " processor(s) available.");
        this.logger
                .log(
                        Level.INFO,
                        String
                                .format(
                                        "%.1f MB of memory free out of %.1f MB total in JVM.  Configured maximum: %.1f MB.",
                                        (runtime.freeMemory() / 1048576.0),
                                        (runtime.totalMemory() / 1048576.0),
                                        (runtime.maxMemory() / 1048576.0)));

        this.logger.log(Level.INFO, "Predicates with no values defined will return: \""
                + this.settings.getPredicateEmptyDefault() + "\".");

        try
        {
            this.logger.log(Level.INFO, "Initializing "
                    + this.multiplexor.getClass().getSimpleName() + ".");

            // Initialize the Multiplexor.
            this.multiplexor.initialize();

            // Create the AIMLWatcher if configured to do so.
            if (this.settings.useWatcher())
            {
                this.aimlWatcher = new AIMLWatcher(this);
            }

            this.logger.log(Level.INFO, "Starting up the Graphmaster.");

            // Start loading bots.
            loadBots(this.settings.getStartupFilePath());
            
            // Start the AIMLWatcher if configured to do so.
            if (this.settings.useWatcher())
            {
                this.aimlWatcher.start();
                this.logger.log(Level.INFO, "The AIML Watcher is active.");
            }
            else
            {
                this.logger.log(Level.INFO, "The AIML Watcher is not active.");
            }

            // Setup a JavaScript interpreter if supposed to.
            if (this.settings.javascriptAllowed())
            {
                if (this.settings.getJavascriptInterpreterClassname() == null)
                {
                    throw new UserError(new UnspecifiedParameterError(
                            "javascript-interpreter.classname"));
                }

                String javascriptInterpreterClassname = this.settings
                        .getJavascriptInterpreterClassname();

                if (javascriptInterpreterClassname.equals(EMPTY_STRING))
                {
                    throw new UserError(new UnspecifiedParameterError(
                            "javascript-interpreter.classname"));
                }

                this.logger.log(Level.INFO, "Initializing " + javascriptInterpreterClassname + ".");

                try
                {
                    this.interpreter = (Interpreter) Class.forName(javascriptInterpreterClassname)
                            .newInstance();
                }
                catch (Exception e)
                {
                    throw new DeveloperError(
                            "Error while creating new instance of JavaScript interpreter.", e);
                }
            }
            else
            {
                this.logger.log(Level.INFO, "JavaScript interpreter not started.");
            }

            // Request garbage collection.
            System.gc();

            this.logger
            .log(
                    Level.INFO,
                    String
                            .format(
                                    "%.1f MB of memory free out of %.1f MB total in JVM.  (Configured maximum: %.1f MB.)",
                                    (runtime.freeMemory() / 1048576.0),
                                    (runtime.totalMemory() / 1048576.0),
                                    (runtime.maxMemory() / 1048576.0)));

            // Start the heart, if enabled.
            if (this.settings.heartEnabled())
            {
                this.heart = new Heart(this.settings.getHeartPulserate());
                // Add a simple IAmAlive Pulse (this should be more
                // configurable).
                this.heart.addPulse(new org.aitools.programd.util.IAmAlivePulse());
                this.heart.start();
                this.logger.log(Level.INFO, "Heart started.");
            }
        }
        catch (DeveloperError e)
        {
            fail("developer error", e);
            return;
        }
        catch (UserError e)
        {
            fail("user error", e);
            return;
        }
        catch (RuntimeException e)
        {
            fail("unforeseen runtime exception", e);
            return;
        }
        catch (Throwable e)
        {
            fail("unforeseen problem", e);
            return;
        }

        // Set the status indicator.
        this.status = Status.READY;
    }

    /**
     * Runs the Core -- this just means keeping it alive until the status flag
     * is changed to <code>SHUT_DOWN</code>.
     */
    public void run()
    {
        if (this.status != Status.READY)
        {
            throw new DeveloperError(new IllegalStateException(
                    "Core has not been set up; cannot run."));
        }

        synchronized (this)
        {
            notifyAll();
        }

        // Now just run as long as the status flag stays at READY.
        while (this.status == Status.READY)
        {
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                break;
            }
        }
    }

    /**
     * Loads the <code>Graphmaster</code> with the contents of a given path.
     * 
     * @param path path to the file(s) to load
     * @param botid
     */
    public void load(String path, String botid)
    {
        final String ENC_UTF8 = "UTF-8";

        boolean localFile;
        
        // Check for obviously invalid paths of zero length.
        if (path.length() < 1)
        {
            this.logger.log(Level.WARNING, "Cannot open a file whose name has zero length.");
        }

        // Handle paths with wildcards that need to be expanded.
        if (path.indexOf(ASTERISK) != -1)
        {
            String[] files = null;

            try
            {
                files = FileManager.glob(path);
            }
            catch (FileNotFoundException e)
            {
                this.logger.log(Level.WARNING, e.getMessage());
            }
            if (files != null)
            {
                for (int index = files.length; --index >= 0;)
                {
                    load(files[index], botid);
                }
            }
            return;
        }

        Bot bot = this.bots.getBot(botid);
        URL url = URITools.createValidURL(path);

        if (!loadCheck(url, bot))
        {
            return;
        }

        if (!url.getProtocol().equals(FileManager.FILE))
        {
            localFile = false;
        }
        else
        {
            localFile = true;
            // Add it to the AIMLWatcher, if active.
            if (this.settings.useWatcher())
            {
                try
                {
                    this.aimlWatcher.addWatchFile(URLDecoder.decode(url.getPath(), ENC_UTF8), botid);
                }
                catch (UnsupportedEncodingException e)
                {
                    assert false;
                }
            }
            FileManager.pushFileParentAsWorkingDirectory(path);
        }

        try
        {
            if (this.settings.loadNotifyEachFile())
            {
                this.logger.log(Level.INFO, "Loading " + url.toString() + "....");
            }
            this.parser.parse(url.toString(), new AIMLReader(this.graphmaster, url, botid, this.bots
                    .getBot(botid), this.settings.getAimlSchemaNamespaceUri()));
            System.gc();
            // this.parser.reset();
        }
        catch (IOException e)
        {
            this.logger.log(Level.WARNING, "Error reading \"" + url + "\".");
        }
        catch (SAXException e)
        {
            this.logger.log(Level.WARNING, "Error parsing \"" + url + "\": " + e.getMessage());
        }

        if (localFile)
        {
            FileManager.popWorkingDirectory();
        }
    }

    /**
     * Tracks/checks whether a given path should be loaded, depending on whether
     * or not it's currently &quot;loadtime&quot;; if the file has already been
     * loaded and is allowed to be reloaded, unloads the file first.
     * 
     * @param path the path to check
     * @param bot the bot for whom to check
     * @return whether or not the given path should be loaded
     */
    private boolean loadCheck(URL path, Bot bot)
    {
        if (bot == null)
        {
            throw new NullPointerException("Null bot passed to loadCheck().");
        }

        Map<URL, Set<Nodemapper>> loadedFiles = bot.getLoadedFilesMap();

        if (loadedFiles.keySet().contains(path))
        {
            // At load time, don't load an already-loaded file.
            if (this.loadtime)
            {
                return false;
            }
            // At other times, unload the file before loading it again.
            this.graphmaster.unload(path, bot);
        }
        else
        {
            loadedFiles.put(path, new HashSet<Nodemapper>());
        }
        return true;
    }
    
    /**
     * Sets "loadtime" mode
     * (so accidentally duplicated paths in a load config
     * won't be loaded multiple times).
     */
    public void setLoadtime()
    {
        this.loadtime = true;
    }

    /**
     * Unsets "loadtime" mode.
     */
    public void unsetLoadtime()
    {
        this.loadtime = false;
    }

    /**
     * Processes the given input using default values for userid (the hostname),
     * botid (the first available bot), and no responder. The result is not
     * returned. This method is mostly useful for a simple test of the Core.
     * 
     * @param input the input to send
     */
    public synchronized void processResponse(String input)
    {
        if (this.status == Status.READY)
        {
            Bot bot = this.bots.getABot();
            if (bot != null)
            {
                this.multiplexor.getResponse(input, this.hostname, bot.getID());
            }
            else
            {
                this.logger.log(Level.WARNING, "No bot available to process response!");
            }
        }
        else
        {
            throw new DeveloperError("Check that the Core is ready before sending it messages.", new CoreNotReadyException());
        }
    }

    /**
     * Returns the response to an input, using a default TextResponder.
     * 
     * @param input the &quot;non-internal&quot; (possibly multi-sentence,
     *            non-substituted) input
     * @param userid the userid for whom the response will be generated
     * @param botid the botid from which to get the response
     * @return the response
     */
    public synchronized String getResponse(String input, String userid, String botid)
    {
        if (this.status == Status.READY)
        {
            String response = this.multiplexor.getResponse(input, userid, botid, new TextResponder());
            return response;
        }
        // otherwise...
        throw new DeveloperError("Check that the Core is running before sending it messages.", new CoreNotReadyException());
    }

    /**
     * Returns the response to an input, using the given Responder.
     * 
     * @param input the &quot;non-internal&quot; (possibly multi-sentence,
     *            non-substituted) input
     * @param userid the userid for whom the response will be generated
     * @param botid the botid from which to get the response
     * @param responder the Responder who cares about this response
     * @return the response
     */
    public synchronized String getResponse(String input, String userid, String botid,
            Responder responder)
    {
        if (this.status == Status.READY)
        {
            String response = this.multiplexor.getResponse(input, userid, botid, responder);
            return response;
        }
        // otherwise...
        throw new DeveloperError("Check that the Core is running before sending it messages.", new CoreNotReadyException());
    }

    /**
     * Performs all necessary shutdown tasks. Shuts down the Graphmaster and all
     * ManagedProcesses.
     */
    public void shutdown()
    {
        this.logger.log(Level.INFO, "Program D is shutting down.");
        this.processes.shutdownAll();
        this.predicateMaster.saveAll();
        this.logger.log(Level.INFO, "Shutdown complete.");
        this.status = Status.SHUT_DOWN;
    }

    /**
     * Logs the given Throwable and shuts down.
     * 
     * @param e the Throwable to log
     */
    public void fail(Throwable e)
    {
        fail(e.getClass().getSimpleName(), Thread.currentThread(), e);
    }

    /**
     * Logs the given Throwable and shuts down.
     * 
     * @param t the thread in which the Throwable was thrown
     * @param e the Throwable to log
     */
    public void fail(Thread t, Throwable e)
    {
        fail(e.getClass().getSimpleName(), t, e);
    }

    /**
     * Logs the given Throwable and shuts down.
     * 
     * @param description the description of the Throwable
     * @param e the Throwable to log
     */
    public void fail(String description, Throwable e)
    {
        fail(description, Thread.currentThread(), e);
    }

    /**
     * Logs the given Throwable and shuts down.
     * 
     * @param description the description of the Throwable
     * @param t the thread in which the Throwable was thrown
     * @param e the Throwable to log
     */
    public void fail(String description, Thread t, Throwable e)
    {
        String throwableDescription = e.getClass().getSimpleName() + " in thread \"" + t.getName()
                + "\"";
        if (e.getMessage() != null)
        {
            throwableDescription += ": " + e.getMessage();
        }
        else
        {
            throwableDescription += ".";
        }
        this.logger.log(Level.SEVERE, "Core is exiting abnormally due to " + description + ":\n"
                + throwableDescription);

        System.err.println();

        if (this.settings.onUncaughtExceptionsPrintStackTrace())
        {
            if (e instanceof UserError || e instanceof DeveloperError)
            {
                e.getCause().printStackTrace(System.err);
            }
            else
            {
                e.printStackTrace(System.err);
            }
        }
        shutdown();
    }

    class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler
    {
        /**
         * Causes the Core to fail, with information about the exception.
         * 
         * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang.Thread,
         *      java.lang.Throwable)
         */
        public void uncaughtException(Thread t, Throwable e)
        {
            System.err.println("Uncaught exception " + e.getClass().getSimpleName()
                    + " in thread \"" + t.getName() + "\".");
            if (Core.this.settings.onUncaughtExceptionsPrintStackTrace())
            {
                e.printStackTrace(System.err);
            }
            Core.this.status = Core.Status.CRASHED;
            System.err.println("Core has crashed.  Shutdown may not have completed properly.");
        }
    }

    /**
     * Sets up a Logger in a standard way. (A FileHandler is attached with some
     * generic settings.)
     * 
     * @param name the name of the logger
     * @param pattern the pattern for the determining the logger's file output
     *            file
     * @return the Logger that was set up.
     */
    public Logger setupLogger(String name, String pattern)
    {
        return LogUtils.setupLogger(name, pattern, this.settings.getLogTimestampFormat());
    }
    
    /**
     * Loads bots from the indicated config file path.
     * 
     * @param path the config file path
     */
    public void loadBots(String path)
    {
        URL url = URITools.createValidURL(path);
        if (url.getProtocol().equals(FileManager.FILE))
        {
            FileManager.pushFileParentAsWorkingDirectory(url.getPath());
        }
        try
        {
            new BotsConfigurationFileParser(this).process(url);
        }
        catch (ProcessorException e)
        {
            this.logger.log(Level.SEVERE, e.getExplanatoryMessage());
            fail("processor exception during startup", e);
        }
    }
    
    /**
     * Loads a bot from the given path.  Will only
     * work right if the file at the path actually
     * has a &gt;bot&lt; element as its root.
     * 
     * @param path the bot config file
     * @return the id of the bot loaded
     */
    public String loadBot(String path)
    {
        this.logger.log(Level.INFO, "Loading bot from \"" + path + "\".");
        URL url = URITools.createValidURL(path);
        if (url.getProtocol().equals(FileManager.FILE))
        {
            FileManager.pushFileParentAsWorkingDirectory(url.getPath());
        }

        String id = null;

        try
        {
            id = new BotsConfigurationFileParser(this).processResponse(url);
        }
        catch (ProcessorException e)
        {
            this.logger.log(Level.SEVERE, e.getExplanatoryMessage());
        }
        this.logger.log(Level.INFO, "Bot \"" + id + "\" has been loaded.");

        return id;
    }
    
    /**
     * Unloads a bot with the given id.
     * 
     * @param id the bot to unload
     */
    public void unloadBot(String id)
    {
        if (!this.bots.include(id))
        {
            this.logger.log(Level.WARNING, "Bot \"" + id + "\" is not loaded; cannot unload.");
            return;
        }
        Bot bot = this.bots.getBot(id);
        for (URL path : bot.getLoadedFilesMap().keySet())
        {
            this.graphmaster.unload(path, bot);
        }
        this.bots.removeBot(id);
        this.logger.log(Level.INFO, "Bot \"" + id + "\" has been unloaded.");
    }

    /*
     * All of these "get" methods throw a NullPointerException if the item has
     * not yet been initialized, to avoid accidents.
     */

    /**
     * @return the object that manages information about all bots
     */
    public Bots getBots()
    {
        if (this.bots != null)
        {
            return this.bots;
        }
        throw new NullPointerException("The Core's Bots object has not yet been initialized!");
    }

    /**
     * @return the Graphmaster
     */
    public Graphmaster getGraphmaster()
    {
        if (this.graphmaster != null)
        {
            return this.graphmaster;
        }
        throw new NullPointerException(
                "The Core's Graphmaster object has not yet been initialized!");
    }

    /**
     * @return the Multiplexor
     */
    public Multiplexor getMultiplexor()
    {
        if (this.multiplexor != null)
        {
            return this.multiplexor;
        }
        throw new NullPointerException(
                "The Core's Multiplexor object has not yet been initialized!");
    }

    /**
     * @return the PredicateMaster
     */
    public PredicateMaster getPredicateMaster()
    {
        if (this.predicateMaster != null)
        {
            return this.predicateMaster;
        }
        throw new NullPointerException(
                "The Core's PredicateMaster object has not yet been initialized!");
    }

    /**
     * @return the BotConfigurationElementProcessorRegistry
     */
    public BotConfigurationElementProcessorRegistry getBotConfigurationElementProcessorRegistry()
    {
        return this.botConfigurationElementProcessorRegistry;
    }

    /**
     * @return the AIML processor registry.
     */
    public AIMLProcessorRegistry getAIMLProcessorRegistry()
    {
        return this.aimlProcessorRegistry;
    }

    /**
     * @return the AIMLWatcher
     */
    public AIMLWatcher getAIMLWatcher()
    {
        if (this.aimlWatcher != null)
        {
            return this.aimlWatcher;
        }
        throw new NullPointerException(
                "The Core's AIMLWatcher object has not yet been initialized!");
    }

    /**
     * @return the settings for this core
     */
    public CoreSettings getSettings()
    {
        if (this.settings != null)
        {
            return this.settings;
        }
        throw new NullPointerException(
                "The Core's CoreSettings object has not yet been initialized!");
    }

    /**
     * @return the active JavaScript interpreter
     */
    public Interpreter getInterpreter()
    {
        if (this.interpreter != null)
        {
            return this.interpreter;
        }
        throw new NullPointerException(
                "The Core's Interpreter object has not yet been initialized!");
    }

    /**
     * @return the local hostname
     */
    public String getHostname()
    {
        return this.hostname;
    }

    /**
     * @return the managed processes
     */
    public ManagedProcesses getManagedProcesses()
    {
        return this.processes;
    }

    /**
     * @return the status of the Core
     */
    public Status getStatus()
    {
        return this.status;
    }

    /**
     * @return the plugin config
     */
    public Document getPluginConfig()
    {
        return this.pluginConfig;
    }
}