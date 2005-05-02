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
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.aitools.programd.bot.Bots;
import org.aitools.programd.graph.Graphmaster;
import org.aitools.programd.interpreter.Interpreter;
import org.aitools.programd.loader.AIMLWatcher;
import org.aitools.programd.logging.LogUtils;
import org.aitools.programd.multiplexor.Multiplexor;
import org.aitools.programd.multiplexor.PredicateMaster;
import org.aitools.programd.processor.aiml.AIMLProcessorRegistry;
import org.aitools.programd.processor.botconfiguration.BotConfigurationElementProcessorRegistry;
import org.aitools.programd.responder.Responder;
import org.aitools.programd.responder.TextResponder;
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
    public static final String VERSION = "4.5";

    /** Build identifier. */
    public static final String BUILD = "rc2";

    /** The namespace URI of the bot configuration. */
    public static final String BOT_CONFIG_SCHEMA_URI = "http://aitools.org/programd/4.5/bot-configuration";

    /** The namespace URI of the plugin configuration. */
    public static final String PLUGIN_CONFIG_SCHEMA_URI = "http://aitools.org/programd/4.5/plugins";

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

    /** The AIML processor registry. */
    private AIMLProcessorRegistry aimlProcessorRegistry;

    /** An AIMLWatcher. */
    private AIMLWatcher aimlWatcher;

    /** An interpreter. */
    private Interpreter interpreter;

    /** The logger for the Core. */
    private Logger logger;

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
        SET_UP,

        /** The Core is running. */
        RUNNING,

        /** The Core has shut down. */
        SHUT_DOWN,

        /** The Core has crashed. */
        CRASHED
    }

    // Convenience constants.
    private static final String EMPTY_STRING = "";

    /** The location of the plugin configuration schema. */
    private static final String PLUGIN_CONFIG_SCHEMA = "./resources/schema/plugins.xsd";

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
        this.graphmaster = new Graphmaster(this);
        this.bots = new Bots();
        this.processes = new ManagedProcesses(this);

        // Get an instance of the settings-specified Multiplexor.
        this.multiplexor = ClassUtils.getSubclassInstance(this.settings.getMultiplexorClassname(),
                "Multiplexor", this);

        // Initialize the PredicateMaster and attach it to the Multiplexor.
        this.predicateMaster = new PredicateMaster(this);
        this.multiplexor.attach(this.predicateMaster);

        // Remove all Handlers from the root logger.
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        for (int index = 0; index < handlers.length; index++)
        {
            rootLogger.removeHandler(handlers[index]);
        }

        // Set up loggers based on the settings.
        this.logger = setupLogger("programd", this.settings.getActivityLogPattern());
        this.logger.setLevel(Level.ALL);

        Logger matchingLogger = setupLogger("programd.matching", this.settings
                .getMatchingLogPattern());
        if (this.settings.recordMatchTrace())
        {
            matchingLogger.setLevel(Level.FINE);
        }

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
            this.pluginConfig = XMLKit.getDocumentBuilder(PLUGIN_CONFIG_SCHEMA,
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
                this.aimlWatcher = new AIMLWatcher(this.graphmaster);
            }

            this.logger.log(Level.INFO, "Starting up the Graphmaster.");

            // Index the start time before loading.
            long time = new Date().getTime();

            // Start up the Graphmaster.
            this.graphmaster.startup(this.settings.getStartupFilePath());

            // Calculate the time used to load all categories.
            time = new Date().getTime() - time;

            this.logger.log(Level.INFO, this.graphmaster.getTotalCategories()
                    + " categories loaded in " + time / 1000.00 + " seconds.");

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
        this.status = Status.SET_UP;
    }

    /**
     * Runs the Core -- this just means keeping it alive until the status flag
     * is changed to <code>SHUT_DOWN</code>.
     */
    public void run()
    {
        if (this.status != Status.SET_UP)
        {
            throw new DeveloperError(new IllegalStateException(
                    "Core has not been set up; cannot run."));
        }

        this.status = Status.RUNNING;

        // Now just run as long as the status flag stays at RUNNING.
        while (this.status == Status.RUNNING)
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
     * Processes the given input using default values for userid (the hostname),
     * botid (the first available bot), and no responder. The result is not
     * returned. This method is mostly useful for a simple test of the Core.
     * 
     * @param input the input to send
     */
    public synchronized void processResponse(String input)
    {
        this.multiplexor.getResponse(input, this.hostname, this.bots.getABot().getID());
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
        String response = this.multiplexor.getResponse(input, userid, botid, new TextResponder());
        return response;
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
        String response = this.multiplexor.getResponse(input, userid, botid, responder);
        return response;
    }

    /**
     * Performs all necessary shutdown tasks. Shuts down the Graphmaster and all
     * ManagedProcesses.
     */
    public void shutdown()
    {
        this.logger.log(Level.INFO, "Program D is shutting down.");
        this.processes.shutdownAll();
        this.graphmaster.shutdown();
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