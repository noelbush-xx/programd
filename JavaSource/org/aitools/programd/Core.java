/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.aitools.programd.graph.Graphmapper;
import org.aitools.programd.interfaces.ConsoleStreamAppender;
import org.aitools.programd.interpreter.Interpreter;
import org.aitools.programd.multiplexor.Multiplexor;
import org.aitools.programd.multiplexor.PredicateMaster;
import org.aitools.programd.parser.BotsConfigurationFileParser;
import org.aitools.programd.processor.ProcessorException;
import org.aitools.programd.processor.aiml.AIMLProcessorRegistry;
import org.aitools.programd.processor.botconfiguration.BotConfigurationElementProcessorRegistry;
import org.aitools.programd.util.AIMLWatcher;
import org.aitools.programd.util.Heart;
import org.aitools.programd.util.ManagedProcesses;
import org.aitools.util.ClassUtils;
import org.aitools.util.runtime.DeveloperError;
import org.aitools.util.resource.Filesystem;
import org.aitools.util.JDKLogHandler;
import org.aitools.util.resource.URLTools;
import org.aitools.util.UnspecifiedParameterError;
import org.aitools.util.runtime.UserError;
import org.aitools.util.runtime.UserSystem;
import org.aitools.util.sql.DbAccessRefsPoolMgr;
import org.aitools.util.xml.Loader;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;

/**
 * The "core" of Program D, independent of any user interfaces.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class Core
{
    /** The namespace URI of the plugin configuration. */
    public static final String PLUGIN_CONFIG_NS_URI = "http://aitools.org/programd/4.7/plugins";
    
    /** The location of the XML catalog, as a string. */
    private static String XML_CATALOG_URL = "resources/catalog.xml";
    
    /** The URL location of the XML catalog. */
    private URL xmlCatalog;

    /** The Settings. */
    protected CoreSettings _settings;

    /** The base URL. */
    private URL baseURL;

    /** The Graphmapper. */
    private Graphmapper _graphmapper;

    /** The Multiplexor. */
    private Multiplexor<?> multiplexor;

    /** The PredicateMaster. */
    private PredicateMaster predicateMaster;

    /** The bots. */
    private Bots _bots;

    /** The processes. */
    private ManagedProcesses processes;

    /** The bot configuration element processor registry. */
    private BotConfigurationElementProcessorRegistry botConfigurationElementProcessorRegistry;
    
    /** A manager for database access. */
    private DbAccessRefsPoolMgr dbManager;

    /** The AIML processor registry. */
    private AIMLProcessorRegistry aimlProcessorRegistry;

    /** An AIMLWatcher. */
    private AIMLWatcher aimlWatcher;

    /** An interpreter. */
    private Interpreter interpreter;

    /** The logger for the Core. */
    private Logger logger = LogManager.getLogger("programd");

    /** Name of the local host. */
    private String hostname;

    /** A heart. */
    private Heart heart;

    /** The plugin config. */
    private Document pluginConfig;

    /** The status of the Core. */
    private Status status = Status.NOT_STARTED;

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
    
    /** A general-purpose map for storing all manner of objects (by AIML processors and the like). */
    private Map<String, Map<String, Object>> classStorage = new HashMap<String, Map<String, Object>>();

    /**
     * Initializes a new Core object with default settings and the given base URL.
     * 
     * @param base the base URL to use
     */
    public Core(URL base)
    {
        setup(base);
        Filesystem.setRootPath(Filesystem.getWorkingDirectory());
        this._settings = new ProgrammaticCoreSettings();
        start();
    }

    /**
     * Initializes a new Core object with the settings from the given file and the given base URL.
     * 
     * @param base the base URL to use
     * @param settings the path to the file with settings
     */
    public Core(URL base, URL settings)
    {
        setup(base);
        Filesystem.setRootPath(URLTools.getParent(this.baseURL));
        this._settings = new XMLCoreSettings(settings, base, this.xmlCatalog, this.logger);
        start();
    }

    /**
     * Initializes a new Core object with the given CoreSettings object and the given base URL.
     * 
     * @param base the base URL to use
     * @param settings the settings to use
     */
    public Core(URL base, CoreSettings settings)
    {
        setup(base);
        this._settings = settings;
        Filesystem.setRootPath(this.baseURL);
    }
    
    private void setup(URL base)
    {
        this.baseURL = base;
        this.xmlCatalog = URLTools.contextualize(this.baseURL, XML_CATALOG_URL);
    }
    
    /**
     * Initializes and starts up the Core.
     */
    protected void start()
    {
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());

        // Use the stdout and stderr appenders in a special way, if they are defined.
        ConsoleStreamAppender stdOutAppender = ((ConsoleStreamAppender) Logger.getLogger("programd").getAppender(
                "stdout"));
        if (stdOutAppender != null)
        {
            if (!stdOutAppender.isWriterSet())
            {
                stdOutAppender.setWriter(new OutputStreamWriter(System.out));
            }
        }

        ConsoleStreamAppender stdErrAppender = ((ConsoleStreamAppender) Logger.getLogger("programd").getAppender(
                "stderr"));
        if (stdErrAppender != null)
        {
            if (!stdErrAppender.isWriterSet())
            {
                stdErrAppender.setWriter(new OutputStreamWriter(System.err));
            }
        }

        // Set up an interception of calls to the JDK logging system and re-route to log4j.
        JDKLogHandler.setupInterception();
        
        this.logger.info(String.format("Base URL for Program D Core: \"%s\".", this.baseURL));

        this.aimlProcessorRegistry = new AIMLProcessorRegistry();
        this.botConfigurationElementProcessorRegistry = new BotConfigurationElementProcessorRegistry();

        this._graphmapper = ClassUtils.getSubclassInstance(Graphmapper.class, this._settings.
                getGraphmapperImplementation(), "Graphmapper implementation", this);
        this._bots = new Bots();
        this.processes = new ManagedProcesses(this);

        // Get an instance of the settings-specified Multiplexor.
        this.multiplexor = ClassUtils.getSubclassInstance(Multiplexor.class, this._settings
                .getMultiplexorImplementation(), "Multiplexor", this);

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
        URL pluginConfigURL = this._settings.getPluginConfigURL();
        if (pluginConfigURL != null)
        {
            pluginConfigURL = URLTools.contextualize(this.baseURL, pluginConfigURL);
            try
            {
                if (pluginConfigURL.openStream() != null)
                {
                    Loader pluginConfigLoader = new Loader(this.baseURL, PLUGIN_CONFIG_NS_URI, this.xmlCatalog, this.logger);
                    this.pluginConfig = pluginConfigLoader.parse(pluginConfigURL);
                }
            }
            catch (IOException e)
            {
                // Don't load plugin config.
            }
        }

        Package pkg = Package.getPackage("org.aitools.programd");
        this.logger.info(String.format("Starting %s version %s [%s].", pkg.getSpecificationTitle(), pkg.getSpecificationVersion(), pkg.getImplementationVersion()));
        this.logger.info(UserSystem.jvmDescription());
        this.logger.info(UserSystem.osDescription());
        this.logger.info(UserSystem.memoryReport());
        this.logger.info("Predicates with no values defined will return: \""
                + this._settings.getPredicateEmptyDefault() + "\".");

        try
        {
            this.logger.info("Initializing " + this.multiplexor.getClass().getSimpleName() + ".");

            // Initialize the Multiplexor.
            this.multiplexor.initialize();

            // Create the AIMLWatcher if configured to do so.
            if (this._settings.useAIMLWatcher())
            {
                this.aimlWatcher = new AIMLWatcher(this);
            }

            // Setup a JavaScript interpreter if supposed to.
            setupInterpreter();

            // Start the AIMLWatcher if configured to do so.
            startWatcher();

            this.logger.info("Starting up the Graphmaster.");

            // Start loading bots.
            URL botConfigURL = this._settings.getBotConfigURL();
            if (botConfigURL != null)
            {
                loadBots(botConfigURL);
            }
            else
            {
                this.logger.warn("No bot config URL specified; no bots will be loaded.");
            }

            // Request garbage collection.
            System.gc();

            this.logger.info(UserSystem.memoryReport());

            // Start the heart, if enabled.
            startHeart();
        }
        catch (DeveloperError e)
        {
            alert("developer error", e);
            // return;
        }
        catch (UserError e)
        {
            alert("user error", e);
            // return;
        }
        catch (RuntimeException e)
        {
            alert("unforeseen runtime exception", e);
            // return;
        }
        catch (Throwable e)
        {
            alert("unforeseen problem", e);
            // return;
        }

        // Set the status indicator.
        this.status = Status.READY;

        // Exit immediately if configured to do so (for timing purposes).
        if (this._settings.exitImmediatelyOnStartup())
        {
            shutdown();
        }
    }

    protected void startWatcher()
    {
        if (this._settings.useAIMLWatcher())
        {
            this.aimlWatcher.start();
            this.logger.info("The AIML Watcher is active.");
        }
        else
        {
            this.logger.info("The AIML Watcher is not active.");
        }
    }

    protected void startHeart()
    {
        if (this._settings.heartEnabled())
        {
            this.heart = new Heart(this._settings.getHeartPulseRate());
            // Add a simple IAmAlive Pulse (this should be more
            // configurable).
            this.heart.addPulse(new org.aitools.programd.util.IAmAlivePulse());
            this.heart.start();
            this.logger.info("Heart started.");
        }
    }

    protected void setupInterpreter()
    {
        if (this._settings.allowJavaScript())
        {
            if (this._settings.getJavascriptInterpreterClassname() == null)
            {
                this.logger.error(new UnspecifiedParameterError("javascript-interpreter.classname"));
            }

            String javascriptInterpreterClassname = this._settings.getJavascriptInterpreterClassname();

            if ("".equals(javascriptInterpreterClassname))
            {
                this.logger.error(new UnspecifiedParameterError("javascript-interpreter.classname"));
            }

            this.logger.info("Initializing " + javascriptInterpreterClassname + ".");

            try
            {
                this.interpreter = (Interpreter) Class.forName(javascriptInterpreterClassname).newInstance();
            }
            catch (Exception e)
            {
                this.logger.error("Error while creating new instance of JavaScript interpreter.", e);
            }
        }
        else
        {
            this.logger.info("JavaScript interpreter not started.");
        }
    }
    
    /**
     * Loads the given path for the given botid.
     * 
     * @param path
     * @param botid
     */
    public void load(URL path, String botid)
    {
        this._graphmapper.load(path, botid);
    }
    
    /**
     * Reloads the given path for the given botid.
     * 
     * @param path
     */
    public void reload(URL path)
    {
        Set<Bot> bots = new HashSet<Bot>();
        for (Bot bot : this._bots.values())
        {
            if (bot.getLoadedFilesMap().containsKey(path))
            {
                bots.add(bot);
            }
        }
        // First unload all,
        for (Bot bot : bots)
        {
            this._graphmapper.unload(path, bot);
        }
        // then reload all.
        for (Bot bot : bots)
        {
            this._graphmapper.load(path, bot.getID());
        }
    }
    
    /**
     * Unloads the given path for the given botid.
     * 
     * @param path
     * @param botid
     */
    public void unload(URL path, String botid)
    {
        this._graphmapper.unload(path, getBot(botid));
    }

    /**
     * Processes the given input using default values for userid (the hostname), botid (the first available bot), and no
     * responder. The result is not returned. This method is mostly useful for a simple test of the Core.
     * 
     * @param input the input to send
     */
    public synchronized void processResponse(String input)
    {
        if (this.status == Status.READY)
        {
            Bot bot = this._bots.getABot();
            if (bot != null)
            {
                this.multiplexor.getResponse(input, this.hostname, bot.getID());
                return;
            }
            this.logger.warn("No bot available to process response!");
            return;
        }
        // throw new DeveloperError("Check that the Core is ready before sending it messages.", new
        // CoreNotReadyException());
    }

    /**
     * Returns the response to an input, using a default TextResponder.
     * 
     * @param input the &quot;non-internal&quot; (possibly multi-sentence, non-substituted) input
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
        // throw new DeveloperError("Check that the Core is running before sending it messages.", new
        // CoreNotReadyException());
        return null;
    }

    /**
     * Performs all necessary shutdown tasks. Shuts down the Graphmaster and all ManagedProcesses.
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
     * Notes the given Throwable and advises that the Core may no longer be stable.
     * 
     * @param e the Throwable to log
     */
    public void alert(Throwable e)
    {
        alert(e.getClass().getSimpleName(), Thread.currentThread(), e);
    }

    /**
     * Notes the given Throwable and advises that the Core may no longer be stable.
     * 
     * @param t the thread in which the Throwable was thrown
     * @param e the Throwable to log
     */
    public void alert(Thread t, Throwable e)
    {
        alert(e.getClass().getSimpleName(), t, e);
    }

    /**
     * Notes the given Throwable and advises that the Core may no longer be stable.
     * 
     * @param description the description of the Throwable
     * @param e the Throwable to log
     */
    public void alert(String description, Throwable e)
    {
        alert(description, Thread.currentThread(), e);
    }

    /**
     * Notes the given Throwable and advises that the Core may no longer be stable.
     * 
     * @param description the description of the Throwable
     * @param t the thread in which the Throwable was thrown
     * @param e the Throwable to log
     */
    public void alert(String description, Thread t, Throwable e)
    {
        String throwableDescription = e.getClass().getSimpleName() + " in thread \"" + t.getName() + "\"";
        if (e.getMessage() != null)
        {
            throwableDescription += ": " + e.getMessage();
        }
        else
        {
            throwableDescription += ".";
        }
        this.logger.error("Core may no longer be stable due to " + description + ":\n" + throwableDescription);

        if (this._settings.printStackTraceOnUncaughtExceptions())
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
        // shutdown();
    }

    class UncaughtExceptionHandler implements Thread.UncaughtExceptionHandler
    {
        /**
         * Causes the Core to fail, with information about the exception.
         * 
         * @see java.lang.Thread.UncaughtExceptionHandler#uncaughtException(java.lang.Thread, java.lang.Throwable)
         */
        public void uncaughtException(Thread t, Throwable e)
        {
            System.err.println("Uncaught exception " + e.getClass().getSimpleName() + " in thread \"" + t.getName()
                    + "\".");
            if (Core.this._settings.printStackTraceOnUncaughtExceptions())
            {
                e.printStackTrace(System.err);
            }
            /*
             * Core.this.status = Core.Status.CRASHED; System.err.println("Core has crashed. Shutdown may not have
             * completed properly.");
             */
        }
    }

    /**
     * Loads bots from the indicated config file path.
     * 
     * @param path the config file path
     */
    public void loadBots(URL path)
    {
        if (this._settings.useAIMLWatcher())
        {
            this.logger.debug("Suspending AIMLWatcher.");
            this.aimlWatcher.stop();
        }
        /*
        if (path.getProtocol().equals(Filesystem.FILE))
        {
            Filesystem.pushWorkingDirectory(URLTools.getParent(path));
        }
        */
        try
        {
            new BotsConfigurationFileParser(this).process(path);
        }
        catch (ProcessorException e)
        {
            this.logger.error("Processor exception during startup: " + e.getExplanatoryMessage(), e);
        }
        /*
        if (path.getProtocol().equals(Filesystem.FILE))
        {
            Filesystem.popWorkingDirectory();
        }
        */
        if (this._settings.useAIMLWatcher())
        {
            this.logger.debug("Restarting AIMLWatcher.");
            this.aimlWatcher.start();
        }
    }

    /**
     * Loads a bot from the given path. Will only work right if the file at the path actually has a &gt;bot&lt; element
     * as its root.
     * 
     * @param path the bot config file
     * @return the id of the bot loaded
     */
    public String loadBot(URL path)
    {
        this.logger.info("Loading bot from \"" + path + "\".");
        /*
         * if (path.getProtocol().equals(Filesystem.FILE)) { Filesystem.pushWorkingDirectory(URLTools.getParent(path)); }
         */

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
        /*
         * if (path.getProtocol().equals(Filesystem.FILE)) { Filesystem.popWorkingDirectory(); }
         */

        return id;
    }

    /**
     * Unloads a bot with the given id.
     * 
     * @param id the bot to unload
     */
    public void unloadBot(String id)
    {
        if (!this._bots.containsKey(id))
        {
            this.logger.warn("Bot \"" + id + "\" is not loaded; cannot unload.");
            return;
        }
        Bot bot = this._bots.get(id);
        for (URL path : bot.getLoadedFilesMap().keySet())
        {
            this._graphmapper.unload(path, bot);
        }
        this._bots.remove(id);
        this.logger.info("Bot \"" + id + "\" has been unloaded.");
    }

    /*
     * All of these "get" methods throw a NullPointerException if the item has not yet been initialized, to avoid
     * accidents.
     */

    /**
     * @return the object that manages information about all bots
     */
    public Bots getBots()
    {
        if (this._bots != null)
        {
            return this._bots;
        }
        throw new NullPointerException("The Core's Bots object has not yet been initialized!");
    }

    /**
     * @param id the id of the bot desired
     * @return the requested bot
     */
    public Bot getBot(String id)
    {
        return this._bots.get(id);
    }

    /**
     * @return the Graphmapper
     */
    public Graphmapper getGraphmapper()
    {
        if (this._graphmapper != null)
        {
            return this._graphmapper;
        }
        throw new NullPointerException("The Core's Graphmapper object has not yet been initialized!");
    }

    /**
     * @return the Multiplexor
     */
    public Multiplexor<?> getMultiplexor()
    {
        if (this.multiplexor != null)
        {
            return this.multiplexor;
        }
        throw new NullPointerException("The Core's Multiplexor object has not yet been initialized!");
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
        throw new NullPointerException("The Core's PredicateMaster object has not yet been initialized!");
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
        throw new NullPointerException("The Core's AIMLWatcher object has not yet been initialized!");
    }

    /**
     * @return the settings for this core
     */
    public CoreSettings getSettings()
    {
        if (this._settings != null)
        {
            return this._settings;
        }
        throw new NullPointerException("The Core's CoreSettings object has not yet been initialized!");
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
        throw new NullPointerException("The Core's Interpreter object has not yet been initialized!");
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
    
    /**
     * Gets an object from the class storage, using the given classname
     * to look up the map for the class, then the given key to retrieve
     * the object.  If the object is not found, then the given defaultObject
     * is stored with the appropriate key, so it will be there next time.
     * 
     * @param <T> the type of object to retrieve
     * @param classname the classname from whose map to retrieve
     * @param key the key to retrieve
     * @param defaultObject default object to use if none is found
     * @return the object associated with this key
     */
    @SuppressWarnings("unchecked")
    public <T> T getStoredObject(String classname, String key, T defaultObject)
    {
        Map<String, Object> storageMap = this.classStorage.get(classname);
        if (storageMap == null)
        {
            storageMap = new HashMap<String, Object>();
            this.classStorage.put(classname, storageMap);
        }
        Object object = storageMap.get(key);
        if (object != null)
        {
            return (T)object;
        }
        storageMap.put(key, defaultObject);
        return defaultObject;
    }
    
    /**
     * @return the URL of the XML catalog
     */
    public URL getCatalog()
    {
        return this.xmlCatalog;
    }
    
    /**
     * Returns a dbmanager object, first creating it if necessary.
     * 
     * @return a dbmanager
     */
    public DbAccessRefsPoolMgr getDBManager()
    {
        if (this.dbManager == null)
        {
            this.logger.debug("Opening database pool.");
    
            this.dbManager = new DbAccessRefsPoolMgr(this._settings.getDatabaseDriver(), this._settings.getDatabaseURL(),
                    this._settings.getDatabaseUsername(), this._settings.getDatabasePassword());
    
            this.logger.debug("Populating database pool.");
    
            this.dbManager.populate(this._settings.getDatabaseMaximumConnections());
        }
        return this.dbManager;
    }
}
