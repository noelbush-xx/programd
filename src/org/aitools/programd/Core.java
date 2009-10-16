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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.aitools.programd.graph.Graphmapper;
import org.aitools.programd.graph.Match;
import org.aitools.programd.interfaces.ConsoleStreamAppender;
import org.aitools.programd.interpreter.Interpreter;
import org.aitools.programd.logging.ChatLogEvent;
import org.aitools.programd.parser.BotsConfigurationFileParser;
import org.aitools.programd.parser.TemplateParser;
import org.aitools.programd.predicates.PredicateManager;
import org.aitools.programd.processor.aiml.AIMLProcessorRegistry;
import org.aitools.programd.util.AIMLWatcher;
import org.aitools.programd.util.Heart;
import org.aitools.programd.util.InputNormalizer;
import org.aitools.programd.util.ManagedProcesses;
import org.aitools.programd.util.NoMatchException;
import org.aitools.programd.util.Pulse;
import org.aitools.util.Classes;
import org.aitools.util.runtime.DeveloperError;
import org.aitools.util.resource.Filesystem;
import org.aitools.util.JDKLogHandler;
import org.aitools.util.resource.URLTools;
import org.aitools.util.UnspecifiedParameterError;
import org.aitools.util.runtime.Errors;
import org.aitools.util.runtime.UserError;
import org.aitools.util.runtime.UserSystem;
import org.aitools.util.xml.JDOM;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDriver;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.jdom.Document;

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
    
    /** The location of the XMLResolver config file, as a string. */
    private static String XMLRESOLVER_CONFIG_URL = "conf/XMLResolver.properties";

    /** The URL of the XML catalog. */
    private URL _xmlCatalog;
    
    /** The URL of the XMLResolver config file. */
    private URL _xmlresolverConfig;
    
    /** XML Parser feature settings. */
    private Map<String, Boolean> _xmlParserFeatureSettings;

    /** The Settings. */
    protected CoreSettings _settings;

    /** The base URL. */
    private URL _baseURL;

    /** The Graphmapper. */
    private Graphmapper _graphmapper;

    /** The PredicateMaster. */
    private PredicateManager _predicateManager;

    /** The bots. */
    private Bots _bots;

    /** The processes that are managed by the core. */
    private ManagedProcesses _processes;

    /** The AIML processor registry. */
    private AIMLProcessorRegistry _aimlProcessorRegistry;

    /** An AIMLWatcher. */
    private AIMLWatcher _aimlWatcher;

    /** An interpreter. */
    private Interpreter _interpreter;

    /** The database connection pool. */
    private GenericObjectPool _connectionPool;

    /** The logger for the Core. */
    private Logger _logger = LogManager.getLogger("programd");

    /** The log where match info will be recorded. */
    protected Logger _matchLogger = Logger.getLogger("programd.matching");

    /** Name of the local host. */
    private String _hostname;

    /** A heart. */
    private Heart _heart;

    /** The plugin config. */
    private Document _pluginConfig;
    
    /** The value to return when a predicate is empty. */
    private String _predicateEmptyDefault;

    /** The time that the Multiplexor started operation. */
    protected long _startTime = System.currentTimeMillis();

    /** A counter for tracking the number of responses produced. */
    protected long _responseCount = 0;

    /** The total response time. */
    protected long _totalTime = 0;

    /** A counter for tracking average response time. */
    protected float _avgResponseTime = 0;

    /** The status of the Core. */
    private Status _status = Status.NOT_STARTED;

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
        this._status = Status.INITIALIZED;
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
        Filesystem.setRootPath(URLTools.getParent(this._baseURL));
        this._settings = new XMLCoreSettings(settings, this._logger);
        this._status = Status.INITIALIZED;
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
        Filesystem.setRootPath(this._baseURL);
        this._status = Status.INITIALIZED;
        start();
    }

    private void setup(URL base)
    {
        this._baseURL = base;
        this._xmlCatalog = URLTools.contextualize(this._baseURL, XML_CATALOG_URL);
        this._xmlresolverConfig = URLTools.contextualize(this._baseURL, XMLRESOLVER_CONFIG_URL);
    }

    /**
     * Initializes and starts up the Core.
     */
    @SuppressWarnings("boxing")
    protected void start()
    {
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
        
        // Set up the XML parsing feature settings.
        this._xmlParserFeatureSettings = new HashMap<String, Boolean>(6);
        this._xmlParserFeatureSettings.put("http://xml.org/sax/features/use-entity-resolver2", this._settings.xmlParserUseEntityResolver2());
        this._xmlParserFeatureSettings.put("http://xml.org/sax/features/validation", this._settings.xmlParserUseValidation());
        this._xmlParserFeatureSettings.put("http://apache.org/xml/features/validation/schema", this._settings.xmlParserUseSchemaValidation());
        this._xmlParserFeatureSettings.put("http://apache.org/xml/features/honour-all-schemaLocations", this._settings.xmlParserHonourAllSchemaLocations());
        this._xmlParserFeatureSettings.put("http://apache.org/xml/features/xinclude", this._settings.xmlParserUseXInclude());
        this._xmlParserFeatureSettings.put("http://apache.org/xml/features/validate-annotations", this._settings.xmlParserValidateAnnotations());

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

        this._logger.info(String.format("Base URL for Program D Core: \"%s\".", this._baseURL));

        this._aimlProcessorRegistry = new AIMLProcessorRegistry();

        this._graphmapper = Classes.getSubclassInstance(Graphmapper.class, this._settings
                .getGraphmapperImplementation(), "Graphmapper implementation", this);
        this._bots = new Bots();
        this._processes = new ManagedProcesses(this);

        // Get an instance of the settings-specified PredicateManager.
        this._predicateManager = Classes.getSubclassInstance(PredicateManager.class, this._settings
                .getPredicateManagerImplementation(), "PredicateManager", this);

        // Get the hostname (used occasionally).
        try
        {
            this._hostname = InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e)
        {
            this._hostname = "unknown hostname";
        }

        // Load the plugin config.
        URL pluginConfigURL = this._settings.getPluginConfigURL();
        if (pluginConfigURL != null)
        {
            pluginConfigURL = URLTools.contextualize(this._baseURL, pluginConfigURL);
            try
            {
                if (pluginConfigURL.openStream() != null)
                {
                    this._pluginConfig = JDOM.getDocument(pluginConfigURL, this._logger);
                }
            }
            catch (IOException e)
            {
                // Don't load plugin config.
            }
        }

        // TODO: Make this work even if the classes aren't in a jar.
        Package pkg = Package.getPackage("org.aitools.programd");
        this._logger.info(String.format("Starting %s version %s [%s].", pkg.getSpecificationTitle(), pkg
                .getSpecificationVersion(), pkg.getImplementationVersion()));
        this._logger.info(UserSystem.jvmDescription());
        this._logger.info(UserSystem.osDescription());
        this._logger.info(UserSystem.memoryReport());
        this._logger.info("Predicates with no values defined will return: \""
                + this._settings.getPredicateEmptyDefault() + "\".");

        try
        {
            // Create the AIMLWatcher if configured to do so.
            if (this._settings.useAIMLWatcher())
            {
                this._aimlWatcher = new AIMLWatcher(this);
            }

            // Setup a JavaScript interpreter if supposed to.
            setupInterpreter();

            // Start the AIMLWatcher if configured to do so.
            startWatcher();

            this._logger.info("Starting up the Graphmaster.");

            // Start loading bots.
            URL botConfigURL = this._settings.getBotConfigURL();
            if (botConfigURL != null)
            {
                loadBotConfig(botConfigURL);
            }
            else
            {
                this._logger.warn("No bot config URL specified; no bots will be loaded.");
            }

            // Request garbage collection.
            System.gc();

            this._logger.info(UserSystem.memoryReport());

            // Start the heart, if enabled.
            startHeart();
        }
        catch (DeveloperError e)
        {
            alert("developer error", e);
        }
        catch (UserError e)
        {
            alert("user error", e);
        }
        catch (RuntimeException e)
        {
            alert("unforeseen runtime exception", e);
        }
        catch (Throwable e)
        {
            alert("unforeseen problem", e);
        }

        // Set the status indicator.
        this._status = Status.READY;

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
            this._aimlWatcher.start();
            this._logger.info("The AIML Watcher is active.");
        }
        else
        {
            this._logger.info("The AIML Watcher is not active.");
        }
    }

    protected void startHeart()
    {
        if (this._settings.heartEnabled())
        {
            this._heart = new Heart(this._settings.getHeartPulseRate());
            String pulseImplementation = this._settings.getPulseImplementation();
            if ("".equals(pulseImplementation))
            {
                this._logger.error(new UnspecifiedParameterError("pulse.implementation"));
            }
            else
            {
                this._heart.addPulse(Classes.getSubclassInstance(Pulse.class, pulseImplementation, "Pulse", this));
                this._heart.start();
                this._logger.info("Heart started.");
            }
        }
    }

    protected void setupInterpreter()
    {
        if (this._settings.allowJavaScript())
        {
            if (this._settings.getJavascriptInterpreterClassname() == null)
            {
                this._logger.error(new UnspecifiedParameterError("javascript-interpreter.classname"));
            }

            String javascriptInterpreterClassname = this._settings.getJavascriptInterpreterClassname();

            if ("".equals(javascriptInterpreterClassname))
            {
                this._logger.error(new UnspecifiedParameterError("javascript-interpreter.classname"));
            }

            this._logger.info("Initializing " + javascriptInterpreterClassname + ".");

            try
            {
                this._interpreter = (Interpreter) Class.forName(javascriptInterpreterClassname).newInstance();
            }
            catch (Exception e)
            {
                this._logger.error("Error while creating new instance of JavaScript interpreter.", e);
            }
        }
        else
        {
            this._logger.info("JavaScript interpreter not started.");
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
        if (this._status == Status.READY)
        {
            Bot bot = this._bots.getABot();
            if (bot != null)
            {
                getResponse(input, this._hostname, bot.getID());
                return;
            }
            this._logger.warn("No bot available to process response!");
            return;
        }
        // throw new DeveloperError("Check that the Core is ready before sending it messages.", new
        // CoreNotReadyException());
    }

    /**
     * Returns the response to an input.
     * 
     * @param input the &quot;non-internal&quot; (possibly multi-sentence, non-substituted) input
     * @param userid the userid for whom the response will be generated
     * @param botid the botid from which to get the response
     * @return the response
     */
    public synchronized String getResponse(String input, String userid, String botid)
    {
        if (this._status == Status.READY)
        {
            // Get the specified bot object.
            Bot bot = this._bots.get(botid);

            // Split sentences (after performing substitutions).
            List<String> sentenceList = bot.sentenceSplit(bot.applyInputSubstitutions(input));

            // Get the replies.
            List<String> replies = getReplies(sentenceList, userid, botid);

            if (replies == null)
            {
                return null;
            }

            // Start by assuming an empty response.
            StringBuilder responseBuffer = new StringBuilder("");

            // Append each reply to the response.
            for (String reply : replies)
            {
                responseBuffer.append(reply);
            }

            String response = responseBuffer.toString();

            // Log the response.
            logResponse(input, response, userid, botid);

            // Return the response (may be just ""!)
            return response;
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
        this._logger.info("Program D is shutting down.");
        this._processes.shutdownAll();
        this._predicateManager.saveAll();
        this._logger.info("Shutdown complete.");
        this._status = Status.SHUT_DOWN;
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
        this._logger.error("Core may no longer be stable due to " + description + ":\n" + throwableDescription);

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
            System.err.println(String.format("Uncaught exception \"%s\" in thread \"%s\".", Errors.describe(e), t.getName()));
            if (Core.this._settings.printStackTraceOnUncaughtExceptions())
            {
                e.printStackTrace(System.err);
            }
        }
    }

    /**
     * Loads bot(s) from the indicated config file path.
     * 
     * @param path the config file path
     */
    public void loadBotConfig(URL path)
    {
        if (this._settings.useAIMLWatcher())
        {
            this._logger.debug("Suspending AIMLWatcher.");
            this._aimlWatcher.stop();
        }
        new BotsConfigurationFileParser(this).parse(path);
        if (this._settings.useAIMLWatcher())
        {
            this._logger.debug("Restarting AIMLWatcher.");
            this._aimlWatcher.start();
        }
    }

    /**
     * Unloads a bot with the given id.
     * 
     * @param id the bot to unload
     */
    public void unload(String id)
    {
        if (!this._bots.containsKey(id))
        {
            this._logger.warn("Bot \"" + id + "\" is not loaded; cannot unload.");
            return;
        }
        Bot bot = this._bots.get(id);
        for (URL path : bot.getLoadedFilesMap().keySet())
        {
            this._graphmapper.unload(path, bot);
        }
        this._bots.remove(id);
        this._logger.info("Bot \"" + id + "\" has been unloaded.");
    }
    
    /**
     * Adds the given bot to the core.
     * 
     * @param bot
     */
    public void addBot(Bot bot)
    {
        this._bots.put(bot.getID(), bot);
    }
    
    /**
     * Removes the given bot from the core, if it exists.
     * 
     * @param bot
     * @return the bot if it was there, null if not
     */
    public Bot removeBot(Bot bot)
    {
        return this._bots.remove(bot);
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
     * @return the PredicateMaster
     */
    public PredicateManager getPredicateMaster()
    {
        if (this._predicateManager != null)
        {
            return this._predicateManager;
        }
        throw new NullPointerException("The Core's PredicateMaster object has not yet been initialized!");
    }

    /**
     * @return the AIML processor registry.
     */
    public AIMLProcessorRegistry getAIMLProcessorRegistry()
    {
        return this._aimlProcessorRegistry;
    }

    /**
     * @return the AIMLWatcher
     */
    public AIMLWatcher getAIMLWatcher()
    {
        if (this._aimlWatcher != null)
        {
            return this._aimlWatcher;
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
        if (this._interpreter != null)
        {
            return this._interpreter;
        }
        throw new NullPointerException("The Core's Interpreter object has not yet been initialized!");
    }

    /**
     * @return the local hostname
     */
    public String getHostname()
    {
        return this._hostname;
    }

    /**
     * @return the managed processes
     */
    public ManagedProcesses getManagedProcesses()
    {
        return this._processes;
    }

    /**
     * @return the status of the Core
     */
    public Status getStatus()
    {
        return this._status;
    }

    /**
     * @return the plugin config
     */
    public Document getPluginConfig()
    {
        return this._pluginConfig;
    }

    /**
     * @return the base URL
     */
    public URL getBaseURL()
    {
        return this._baseURL;
    }

    /**
     * @return the logger
     */
    public Logger getLogger()
    {
        return this._logger;
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
            return (T) object;
        }
        storageMap.put(key, defaultObject);
        return defaultObject;
    }

    /**
     * @return the URL of the XML catalog
     */
    public URL getXMLCatalog()
    {
        return this._xmlCatalog;
    }
    
    /**
     * @return the URL of the XMLResolver configuration file
     */
    public URL getXMLResolverConfig()
    {
        return this._xmlresolverConfig;
    }
    
    /**
     * @return the XML parser feature settings
     */
    public Map<String, Boolean> getXMLParserFeatureSettings()
    {
        return this._xmlParserFeatureSettings;
    }

    /**
     * Returns a database connection backed by a pooling driver.
     * This is initialized lazily, since some people may not be
     * using any database-based features.
     * 
     * @return a dbmanager
     */
    public Connection getDBConnection()
    {
        if (this._connectionPool == null)
        {
            try
            {
                Class.forName(this._settings.getDatabaseDriver());
            }
            catch (ClassNotFoundException e)
            {
                throw new UserError("Could not find your database driver.", e);
            }
            this._connectionPool = new GenericObjectPool();
            ConnectionFactory connectionFactory = new DriverManagerConnectionFactory(this._settings.getDatabaseURL(),
                    this._settings.getDatabaseUsername(), this._settings.getDatabasePassword());
            new PoolableConnectionFactory(connectionFactory, this._connectionPool, null, null, false, true);
            PoolingDriver driver = new PoolingDriver();
            driver.registerPool("programd", this._connectionPool);
        }
        try
        {
            return DriverManager.getConnection("jdbc:apache:commons:dbcp:programd");
        }
        catch (SQLException e)
        {
            this._logger.error("SQL exception when getting a db connection.", e);
            return null;
        }
    }

    /**
     * <p>
     * Produces a response to an &quot;internal&quot; input sentence -- i.e., an
     * input that has been produced by a <code>srai</code>.
     * </p>
     * <p>
     * This method
     * takes an already-existing <code>TemplateParser</code>, <i>doesn't </i>
     * take a <code>Responder</code>, and assumes that the inputs have
     * already been normalized.
     * </p>
     * 
     * @param input the input sentence
     * @param userid the userid requesting the response
     * @param botid the botid from which to get the response
     * @param parser the parser object to update when generating the response
     * @return the response
     */
    @SuppressWarnings("boxing")
    public String getInternalResponse(String input, String userid, String botid, TemplateParser parser)
    {
        // Get the requested bot.
        Bot bot = this._bots.get(botid);

        String _input = input;
        parser.addInput(_input);

        // Ready the that and topic predicates for constructing the match path.
        List<String> thatSentences = bot.sentenceSplit(this._predicateManager.get("that", 1, userid, botid));
        String that = InputNormalizer.patternFitIgnoreCase(thatSentences.get(thatSentences.size() - 1));

        if ("".equals(that) || that.equals(this._predicateEmptyDefault))
        {
            that = "*";
        }
        parser.addThat(that);

        String topic = this._predicateManager.get("topic", userid, botid);
        if ("".equals(topic) || topic.equals(this._predicateEmptyDefault))
        {
            topic = "*";
        }
        parser.addTopic(topic);

        // Verify we've been tracking thats and topics correctly.
        List<String> inputs = parser.getInputs();
        List<String> thats = parser.getThats();
        List<String> topics = parser.getTopics();
        int stackSize = inputs.size();
        assert stackSize == thats.size() && thats.size() == topics.size() : String.format(
                "%d inputs, %d thats, %d topics", stackSize, thats.size(), topics.size());

        // Check for some simple kinds of infinite loops.
        if (stackSize > 1)
        {
            for (int lookback = stackSize - 2; lookback > -1; lookback--)
            {
                String comparisonInput = inputs.get(lookback);
                String comparisonThat = thats.get(lookback);
                String comparisonTopic = topics.get(lookback);
                String infiniteLoopInput = parser.getCore().getSettings().getInfiniteLoopInput();
                if (that.equalsIgnoreCase(comparisonThat) && topic.equalsIgnoreCase(comparisonTopic))
                {
                    if (_input.equalsIgnoreCase(infiniteLoopInput))
                    {
                        this._matchLogger.error("Unrecoverable infinite loop.");
                        return "";
                    }
                    if (_input.equalsIgnoreCase(comparisonInput))
                    {
                        _input = infiniteLoopInput;
                        inputs.set(stackSize - 1, infiniteLoopInput);
                        this._matchLogger.warn(String.format("Infinite loop detected; substituting \"%s\".",
                                infiniteLoopInput));
                    }
                }
            }
        }

        return getMatchResult(_input, that, topic, userid, botid, parser);
    }

    /**
     * Gets the list of replies to some input sentences. Assumes that the
     * sentences have already had all necessary pre-processing and substitutions
     * performed.
     * 
     * @param sentenceList the input sentences
     * @param userid the userid requesting the replies
     * @param botid
     * @return the list of replies to the input sentences
     */
    @SuppressWarnings("boxing")
    protected List<String> getReplies(List<String> sentenceList, String userid, String botid)
    {
        if (sentenceList == null)
        {
            return null;
        }

        // All replies will be assembled in this ArrayList.
        List<String> replies = Collections.checkedList(new ArrayList<String>(sentenceList.size()), String.class);

        // Get the requested bot.
        Bot bot = this._bots.get(botid);

        // Ready the that and topic predicates for constructing the match path.
        List<String> thatSentences = bot.sentenceSplit(this._predicateManager.get("that", 1, userid, botid));
        String that = null;
        if (thatSentences.size() > 0)
        {
            that = InputNormalizer.patternFitIgnoreCase(thatSentences.get(thatSentences.size() - 1));
        }

        if (that == null || "".equals(that) || that.equals(this._predicateEmptyDefault))
        {
            that = "*";
        }

        String topic = InputNormalizer.patternFitIgnoreCase(this._predicateManager.get("topic", userid, botid));
        if ("".equals(topic) || topic.equals(this._predicateEmptyDefault))
        {
            topic = "*";
        }

        // We might use this to track matching statistics.
        long time = 0;

        // Mark the time just before matching starts.
        time = System.currentTimeMillis();

        // Get a reply for each sentence.
        for (String sentence : sentenceList)
        {
            replies.add(getReply(sentence, that, topic, userid, botid));
        }

        // Increment the (static) response count.
        this._responseCount++;

        // Produce statistics about the response time.
        // Mark the time that processing is finished.
        time = System.currentTimeMillis() - time;

        // Calculate the average response time.
        this._totalTime += time;
        this._avgResponseTime = (float) this._totalTime / (float) this._responseCount;
        if (this._matchLogger.isDebugEnabled())
        {
            this._matchLogger.debug(String.format("Response %d in %dms. (Average: %.2fms)", this._responseCount, time,
                    this._avgResponseTime));
        }

        // Invoke targeting if appropriate.
        /*
         * if (responseCount % TARGET_SKIP == 0) { if (USE_TARGETING) {
         * Graphmaster.checkpoint(); } }
         */

        // If no replies, return an empty string.
        if (replies.size() == 0)
        {
            replies.add("");
        }
        return replies;
    }

    /**
     * Gets a reply to an input. Assumes that the input has already had all
     * necessary substitutions and pre-processing performed, and that the input
     * is a single sentence.
     * 
     * @param input the input sentence
     * @param that the input that value
     * @param topic the input topic value
     * @param userid the userid requesting the reply
     * @param botid
     * @return the reply to the input sentence
     */
    protected String getReply(String input, String that, String topic, String userid, String botid)
    {
        // Push the input onto the <input/> stack.
        this._predicateManager.push("input", input, userid, botid);

        // Create a new TemplateParser.
        TemplateParser parser = new TemplateParser(input, that, topic, userid, botid, this);

        String reply = getMatchResult(input, that, topic, userid, botid, parser);
        if (reply == null)
        {
            this._logger.error("getMatchReply generated a null reply!", new NullPointerException("Null reply."));
            return "";
        }

        // Push the reply onto the <that/> stack.
        this._predicateManager.push("that", reply, userid, botid);

        return reply;
    }

    /**
     * Gets the match result from the Graphmaster.
     * 
     * @param input the input to match
     * @param that the current that value
     * @param topic the current topic value
     * @param userid the userid for whom to perform the match
     * @param botid the botid for whom to perform the match
     * @param parser the parser to use
     * @return the match result
     */
    protected String getMatchResult(String input, String that, String topic, String userid, String botid,
            TemplateParser parser)
    {
        // Show the input path.
        if (this._matchLogger.isDebugEnabled())
        {
            this._matchLogger.debug(String.format("[INPUT (%s)] %s:%s:%s:%s", userid, input, that, topic, botid));
        }

        Match match = null;

        try
        {
            match = this._graphmapper.match(InputNormalizer.patternFitIgnoreCase(input), that, topic, botid);
        }
        catch (NoMatchException e)
        {
            this._logger.warn(e.getMessage());
            return "";
        }

        if (match == null)
        {
            this._logger.warn(String.format("No match found for input \"%s\".", input));
            return "";
        }

        if (this._matchLogger.isDebugEnabled())
        {
            this._matchLogger.debug(String.format("[MATCH (%s)] %s (\"%s\")", userid, match.getPath(), match.getFileNames()));
        }

        parser.addMatch(match);

        String template = match.getTemplate();
        String reply = null;

        try
        {
            reply = parser.processResponse(template, match.getFileNames().get(0));
        }
        catch (Throwable e)
        {
            // Log the error message.
            this._logger.error(String.format("Error while processing response: \"%s\"", Errors.describe(e)), e);

            // Set response to empty string.
            return "";
        }
        return reply;
    }

    /**
     * Logs a response to the chat log.
     * 
     * @param input the input that produced the response
     * @param response the response
     * @param userid the userid for whom the response was produced
     * @param botid the botid that produced the response
     */
    protected void logResponse(String input, String response, String userid, String botid)
    {
        /*
         * NOTA BENE: This is a very specific workaround for a problem with the log4j
         * JDBCAppender.  It appears that the appender fails to maintain the database
         * connection after some period of time, and thus stops working.  Here, we
         * catch the exception thrown in this case, and force log4j to reinitialize
         * the appender.  This is horribly specific and should go away as soon as possible.
         * - 2006-03-29, NB
         */
        try
        {
            this._logger.callAppenders(new ChatLogEvent(botid, userid, input, response));
        }
        catch (Exception e)
        {
            this._logger
                    .error(
                            "A known bug with log4j has been encountered.  Attempting to reset logging configuration. This may or may not work.",
                            e);
            LogManager.resetConfiguration();
        }
    }

    /**
     * Returns the average response time.
     * 
     * @return the average response time
     */
    public float averageResponseTime()
    {
        return this._avgResponseTime;
    }

    /**
     * Returns the number of queries per hour.
     * 
     * @return the number of queries per hour
     */
    public float queriesPerHour()
    {
        return this._responseCount / ((System.currentTimeMillis() - this._startTime) / 3600000.00f);
    }
}
