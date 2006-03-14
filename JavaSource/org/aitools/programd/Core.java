/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.SAXParser;

import org.aitools.programd.bot.Bot;
import org.aitools.programd.bot.Bots;
import org.aitools.programd.graph.Graphmaster;
import org.aitools.programd.graph.Nodemapper;
import org.aitools.programd.interfaces.ConsoleStreamAppender;
import org.aitools.programd.interpreter.Interpreter;
import org.aitools.programd.multiplexor.Multiplexor;
import org.aitools.programd.multiplexor.PredicateMaster;
import org.aitools.programd.parser.AIMLReader;
import org.aitools.programd.parser.BotsConfigurationFileParser;
import org.aitools.programd.processor.ProcessorException;
import org.aitools.programd.processor.aiml.AIMLProcessorRegistry;
import org.aitools.programd.processor.botconfiguration.BotConfigurationElementProcessorRegistry;
import org.aitools.programd.util.AIMLWatcher;
import org.aitools.programd.util.ClassUtils;
import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.FileManager;
import org.aitools.programd.util.Heart;
import org.aitools.programd.util.JDKLogHandler;
import org.aitools.programd.util.ManagedProcesses;
import org.aitools.programd.util.UnspecifiedParameterError;
import org.aitools.programd.util.URLTools;
import org.aitools.programd.util.UserError;
import org.aitools.programd.util.UserSystem;
import org.aitools.programd.util.XMLKit;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;
import org.w3c.dom.Document;

/**
 * The "core" of Program D, independent of any interfaces.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class Core
{
    // Public access informational constants.

    /** Copyright notice. */
    public static final String[] COPYLEFT = { "Program D",
            "This program is free software; you can redistribute it and/or",
            "modify it under the terms of the GNU General Public License",
            "as published by the Free Software Foundation; either version 2",
            "of the License, or (at your option) any later version." };

    /** Version of this package. */
    public static final String VERSION = "4.6";

    /** Build identifier. */
    public static final String BUILD = "";
    
    /** The location of the AIML schema. */
    private static final String AIML_SCHEMA_LOCATION = "resources/schema/AIML.xsd";

    /** The namespace URI of the bot configuration. */
    public static final String BOT_CONFIG_SCHEMA_URI = "http://aitools.org/programd/4.6/bot-configuration";
    
    /** The namespace URI of the plugin configuration. */
    public static final String PLUGIN_CONFIG_SCHEMA_URI = "http://aitools.org/programd/4.6/plugins";
    
    /** The location of the plugins schema. */
    private static final String PLUGINS_SCHEMA_LOCATION = "resources/schema/plugins.xsd";
        
    /** The base URL. */
    private URL baseURL;

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
    private Logger logger = LogManager.getLogger("programd");

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

    // Convenience constants.
    private static final String EMPTY_STRING = "";

    /** The <code>*</code> wildcard. */
    public static final String ASTERISK = "*";

    /**
     * Initializes a new Core object with default property values
     * and the given base URL.
     * 
     * @param base the base URL to use
     */
    public Core(URL base)
    {
        this.settings = new CoreSettings();
        this.baseURL = base;
        FileManager.setRootPath(FileManager.getWorkingDirectory());
        start();
    }

    /**
     * Initializes a new Core object with the properties from the given file
     * and the given base URL.
     * 
     * @param base the base URL to use
     * @param propertiesPath
     */
    public Core(URL base, URL propertiesPath)
    {
        this.baseURL = base;
        this.settings = new CoreSettings(propertiesPath);
        FileManager.setRootPath(URLTools.getParent(this.baseURL));
        start();
    }

    /**
     * Initializes a new Core object with the given CoreSettings object
     * and the given base URL.
     * 
     * @param base the base URL to use
     * @param settingsToUse the settings to use
     */
    public Core(URL base, CoreSettings settingsToUse)
    {
        this.settings = settingsToUse;
        this.baseURL = base;
        FileManager.setRootPath(URLTools.getParent(this.baseURL));
        start();
    }

    /**
     * Initializes and starts up the Core.
     */
    protected void start()
    {
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
        
        // Use the stdout and stderr appenders in a special way, if they are defined.
        ConsoleStreamAppender stdOutAppender = ((ConsoleStreamAppender)Logger.getLogger("programd").getAppender("stdout"));
        if (stdOutAppender != null)
        {
            if (!stdOutAppender.isWriterSet())
            {
                stdOutAppender.setWriter(new OutputStreamWriter(System.out));
            }
        }
        
        ConsoleStreamAppender stdErrAppender = ((ConsoleStreamAppender)Logger.getLogger("programd").getAppender("stderr"));
        if (stdErrAppender != null)
        {
            if (!stdErrAppender.isWriterSet())
            {
                stdErrAppender.setWriter(new OutputStreamWriter(System.err));
            }
        }
        
        // Set up an interception of calls to the JDK logging system and re-route to log4j.
        JDKLogHandler.setupInterception();
        
        this.aimlProcessorRegistry = new AIMLProcessorRegistry();
        this.botConfigurationElementProcessorRegistry = new BotConfigurationElementProcessorRegistry();
        
        this.parser = XMLKit.getSAXParser(URLTools.contextualize(FileManager.getWorkingDirectory(), AIML_SCHEMA_LOCATION), "AIML");
        
        this.graphmaster = new Graphmaster(this);
        this.bots = new Bots();
        this.processes = new ManagedProcesses(this);

        // Get an instance of the settings-specified Multiplexor.
        this.multiplexor = ClassUtils.getSubclassInstance(Multiplexor.class, this.settings.getMultiplexorImplementation(),
                "Multiplexor", this);

        // Initialize the PredicateMaster and attach it to the Multiplexor.
        this.predicateMaster = new PredicateMaster(this);
        this.multiplexor.attach(this.predicateMaster);

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
            this.pluginConfig = XMLKit.getDocumentBuilder(URLTools.contextualize(FileManager.getWorkingDirectory(), PLUGINS_SCHEMA_LOCATION),
                    "plugin configuration").parse(
                    URLTools.contextualize(this.baseURL, this.settings.getConfLocationPlugins())
                            .toString());
        }
        catch (IOException e)
        {
            this.logger.error("IO error trying to read plugin configuration.", e);
        }
        catch (SAXException e)
        {
            this.logger.error("Error trying to parse plugin configuration.", e);
        }

        this.logger.info("Starting Program D version " + VERSION + BUILD + '.');
        this.logger.info(UserSystem.jvmDescription());
        this.logger.info(UserSystem.osDescription());
        this.logger.info(UserSystem.memoryReport());
        this.logger.info("Predicates with no values defined will return: \""
                + this.settings.getPredicateEmptyDefault() + "\".");

        try
        {
            this.logger.info("Initializing "
                    + this.multiplexor.getClass().getSimpleName() + ".");

            // Initialize the Multiplexor.
            this.multiplexor.initialize();

            // Create the AIMLWatcher if configured to do so.
            if (this.settings.useWatcher())
            {
                this.aimlWatcher = new AIMLWatcher(this);
            }

            // Setup a JavaScript interpreter if supposed to.
            setupInterpreter();

            // Start the AIMLWatcher if configured to do so.
            startWatcher();

            this.logger.info("Starting up the Graphmaster.");

            // Start loading bots.
            loadBots(URLTools.contextualize(this.baseURL, this.settings.getStartupFilePath()));
            
            // Request garbage collection.
            System.gc();

            this.logger.info(UserSystem.memoryReport());

            // Start the heart, if enabled.
            startHeart();
        }
        catch (DeveloperError e)
        {
            alert("developer error", e);
            //return;
        }
        catch (UserError e)
        {
            alert("user error", e);
            //return;
        }
        catch (RuntimeException e)
        {
            alert("unforeseen runtime exception", e);
            //return;
        }
        catch (Throwable e)
        {
            alert("unforeseen problem", e);
            //return;
        }

        // Set the status indicator.
        this.status = Status.READY;
        
        // Exit immediately if configured to do so (for timing purposes).
        if (this.settings.exitImmediatelyOnStartup())
        {
            shutdown();
        }
    }

    private void startWatcher()
    {
        if (this.settings.useWatcher())
        {
            this.aimlWatcher.start();
            this.logger.info("The AIML Watcher is active.");
        }
        else
        {
            this.logger.info("The AIML Watcher is not active.");
        }
    }

    private void startHeart()
    {
        if (this.settings.heartEnabled())
        {
            this.heart = new Heart(this.settings.getHeartPulserate());
            // Add a simple IAmAlive Pulse (this should be more
            // configurable).
            this.heart.addPulse(new org.aitools.programd.util.IAmAlivePulse());
            this.heart.start();
            this.logger.info("Heart started.");
        }
    }

    private void setupInterpreter() throws UserError, DeveloperError
    {
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

            this.logger.info("Initializing " + javascriptInterpreterClassname + ".");

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
            this.logger.info("JavaScript interpreter not started.");
        }
    }

    /**
     * Loads the <code>Graphmaster</code> with the contents of a given path.
     * 
     * @param path path to the file(s) to load
     * @param botid
     */
    public void load(URL path, String botid)
    {
        // Handle paths with wildcards that need to be expanded.
        if (path.getProtocol().equals(FileManager.FILE))
        {
            String spec = path.getFile();
            if (spec.indexOf(ASTERISK) != -1)
            {
                List<File> files = null;
    
                try
                {
                    files = FileManager.glob(spec);
                }
                catch (FileNotFoundException e)
                {
                    this.logger.warn(e.getMessage());
                }
                if (files != null)
                {
                    for (File file : files)
                    {
                        load(URLTools.contextualize(URLTools.getParent(path), file.getAbsolutePath()), botid);
                    }
                }
                return;
            }
        }

        Bot bot = this.bots.getBot(botid);

        if (!shouldLoad(path, bot))
        {
            return;
        }

        //FileManager.pushWorkingDirectory(URLTools.getParent(path));
        
        // Let the Graphmaster use a shortcut if possible.
        if (this.graphmaster.hasAlreadyLoaded(path))
        {
        	if (this.graphmaster.hasAlreadyLoadedForBot(path, botid))
        	{
        		this.graphmaster.unload(path, bot);
        		doLoad(path, botid);
        	}
            if (this.logger.isDebugEnabled())
            {
                this.logger.debug(String.format("Graphaster has already loaded \"%s\" for some other bot.", path));
            }
            this.graphmaster.addForBot(path, botid);
        }
        else
        {
            if (this.settings.loadNotifyEachFile())
            {
                this.logger.info("Loading " + URLTools.unescape(path) + "....");
            }
            doLoad(path, botid);
            // Add it to the AIMLWatcher, if active.
            if (this.settings.useWatcher())
            {
                this.aimlWatcher.addWatchFile(path);
            }
        }
        //FileManager.popWorkingDirectory();
    }
    
    /**
     * Reloads a file&mdash;it is not necessary to specify a particular
     * botid here, because a reload of a file for one botid suffices
     * for all bots associated with that file.
     * 
     * @param path
     * @throws IllegalArgumentException if the given path is not actually loaded for <em>any</em> bot
     */
    public void reload(URL path)
    {
        Set<String> botids = this.graphmaster.getURLCatalog().get(path);
        if (botids == null || botids.size() == 0)
        {
            throw new IllegalArgumentException("Called Core.reload() with a path that is not loaded by any bot.");
        }
        // Get any botid -- we don't care.
        doLoad(path, botids.iterator().next());
    }
    
    /**
     * An internal method used by {@link #load(URL, String)}.
     * @param path
     * @param botid
     */
    private void doLoad(URL path, String botid)
    {
    	try
    	{
	        AIMLReader reader = new AIMLReader(this.graphmaster, path, botid, this.bots
	                .getBot(botid), this.settings.getAimlSchemaNamespaceUri().toString());
	        try
	        {
	            this.parser.getXMLReader().setProperty("http://xml.org/sax/properties/lexical-handler", reader);
	        }
	        catch (SAXNotRecognizedException e)
	        {
	            this.logger.warn("The XML reader in use does not support lexical handlers -- CDATA will not be handled.", e);
	        }
	        catch (SAXNotSupportedException e)
	        {
	            this.logger.warn("The XML reader in use cannot enable the lexical handler feature -- CDATA will not be handled.", e);
	        }
	        catch (SAXException e)
	        {
	            this.logger.warn("An exception occurred when trying to enable the lexical handler feature on the XML reader -- CDATA will not be handled.", e);
	        }
	        this.parser.parse(path.toString(), reader);
	        //System.gc();
	        this.graphmaster.addURL(path, botid);
        }
        catch (IOException e)
        {
            this.logger.warn(String.format("Error reading \"%s\": %s", URLTools.unescape(path), e.getMessage()), e);
        }
        catch (SAXException e)
        {
            this.logger.warn(String.format("Error parsing \"%s\": %s", URLTools.unescape(path), e.getMessage()), e);
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
    private boolean shouldLoad(URL path, Bot bot)
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
                return;
            }
            this.logger.warn("No bot available to process response!");
            return;
        }
        //throw new DeveloperError("Check that the Core is ready before sending it messages.", new CoreNotReadyException());
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
            return this.multiplexor.getResponse(input, userid, botid);
        }
        // otherwise...
        //throw new DeveloperError("Check that the Core is running before sending it messages.", new CoreNotReadyException());
        return null;
    }

    /**
     * Performs all necessary shutdown tasks. Shuts down the Graphmaster and all
     * ManagedProcesses.
     */
    public void shutdown()
    {
        this.logger.info("Program D is shutting down.");
        this.processes.shutdownAll();
        this.predicateMaster.saveAll();
        this.logger.info("Shutdown complete.");
        this.status = Status.SHUT_DOWN;
    }

    /**
     * Notes the given Throwable and advises that the Core
     * may no longer be stable.
     * 
     * @param e the Throwable to log
     */
    public void alert(Throwable e)
    {
        alert(e.getClass().getSimpleName(), Thread.currentThread(), e);
    }

    /**
     * Notes the given Throwable and advises that the Core
     * may no longer be stable.
     * 
     * @param t the thread in which the Throwable was thrown
     * @param e the Throwable to log
     */
    public void alert(Thread t, Throwable e)
    {
        alert(e.getClass().getSimpleName(), t, e);
    }

    /**
     * Notes the given Throwable and advises that the Core
     * may no longer be stable.
     * 
     * @param description the description of the Throwable
     * @param e the Throwable to log
     */
    public void alert(String description, Throwable e)
    {
        alert(description, Thread.currentThread(), e);
    }

    /**
     * Notes the given Throwable and advises that the Core
     * may no longer be stable.
     * 
     * @param description the description of the Throwable
     * @param t the thread in which the Throwable was thrown
     * @param e the Throwable to log
     */
    public void alert(String description, Thread t, Throwable e)
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
        this.logger.error("Core may no longer be stable due to " + description + ":\n"
                + throwableDescription);

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
        //shutdown();
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
     * Loads bots from the indicated config file path.
     * 
     * @param path the config file path
     */
    public void loadBots(URL path)
    {
        if (this.settings.useWatcher())
        {
            this.logger.debug("Suspending AIMLWatcher.");
            this.aimlWatcher.stop();
        }
        if (path.getProtocol().equals(FileManager.FILE))
        {
            FileManager.pushWorkingDirectory(URLTools.getParent(path));
        }
        try
        {
            new BotsConfigurationFileParser(this).process(path);
        }
        catch (ProcessorException e)
        {
            this.logger.error("Processor exception during startup: " + e.getExplanatoryMessage(), e);
        }
        if (path.getProtocol().equals(FileManager.FILE))
        {
            FileManager.popWorkingDirectory();
        }
        if (this.settings.useWatcher())
        {
            this.logger.debug("Restarting AIMLWatcher.");
            this.aimlWatcher.start();
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
    public String loadBot(URL path)
    {
        this.logger.info("Loading bot from \"" + path + "\".");
        /*if (path.getProtocol().equals(FileManager.FILE))
        {
            FileManager.pushWorkingDirectory(URLTools.getParent(path));
        }*/

        String id = null;

        try
        {
            id = new BotsConfigurationFileParser(this).processResponse(path);
        }
        catch (ProcessorException e)
        {
            this.logger.error(e.getExplanatoryMessage());
        }
        this.logger.info(String.format("Bot \"%s\" has been loaded.", id));
        /*if (path.getProtocol().equals(FileManager.FILE))
        {
            FileManager.popWorkingDirectory();
        }*/

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
            this.logger.warn("Bot \"" + id + "\" is not loaded; cannot unload.");
            return;
        }
        Bot bot = this.bots.getBot(id);
        for (URL path : bot.getLoadedFilesMap().keySet())
        {
            this.graphmaster.unload(path, bot);
        }
        this.bots.removeBot(id);
        this.logger.info("Bot \"" + id + "\" has been unloaded.");
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
     * @param id the id of the bot desired
     * @return the requested bot
     */
    public Bot getBot(String id)
    {
        return this.bots.getBot(id);
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
    
    /**
     * @return the base URL
     */
    public URL getBaseURL()
    {
        return this.baseURL;
    }
    
    /**
     * @return the logger
     */
    public Logger getLogger()
    {
        return this.logger;
    }
}