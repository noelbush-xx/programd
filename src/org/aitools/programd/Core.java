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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.HashSet;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.aitools.programd.bot.Bots;
import org.aitools.programd.bot.BotProcesses;
import org.aitools.programd.graph.Graphmaster;
import org.aitools.programd.interpreter.Interpreter;
import org.aitools.programd.loader.AIMLWatcher;
import org.aitools.programd.logging.ChatLogRecord;
import org.aitools.programd.logging.SimpleFormatter;
import org.aitools.programd.multiplexor.Multiplexor;
import org.aitools.programd.multiplexor.PredicateMaster;
import org.aitools.programd.responder.Responder;
import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.FileManager;
import org.aitools.programd.util.Heart;
import org.aitools.programd.util.UnspecifiedParameterError;
import org.aitools.programd.util.URITools;
import org.aitools.programd.util.UserError;

/**
 * The "core" of Program D, independent of any interfaces.
 * 
 * @author Noel Bush
 */
public class Core
{
    // Public access informational constants.

    /** Copyright notice. */
    public static final String[] COPYLEFT =
        { "Program D", "This program is free software; you can redistribute it and/or",
                "modify it under the terms of the GNU General Public License",
                "as published by the Free Software Foundation; either version 2",
                "of the License, or (at your option) any later version." } ;

    /** Version of this package. */
    public static final String VERSION = "4.5";

    /** Build Number of this package (internal regression test control). */
    public static final String BUILD = "00";

    /** The Settings. */
    private CoreSettings settings;
    
    /** The Graphmaster. */
    private Graphmaster graphmaster;
    
    /** The Multiplexor. */
    private Multiplexor multiplexor;
    
    /** The PredicateMaster. */
    private PredicateMaster predicateMaster;
    
    /** The bots. */
    private Bots bots;
    
    /** An AIMLWatcher. */
    private AIMLWatcher aimlWatcher;
    
    /** An interpreter. */
    private Interpreter interpreter;
    
    /** The logger for the Core. */
    private Logger logger;
    
    /** Name of the local host. */
    private String hostname;

    /** The {@link org.aitools.programd.CoreListener CoreListener}s, who get notices of certain events. */
    private HashSet<CoreListener> listeners;
    
    /** A heart. */
    private Heart heart;

    // Convenience constants.
    private static final String EMPTY_STRING = "";

    /**
     * Initializes a new Core object with the properties from the given file.
     * 
     * @param propertiesPath
     */
    public Core(String propertiesPath)
    {
        this.settings = new CoreSettings(propertiesPath);
        FileManager.setRootPath(URITools.contextualize(URITools.createValidURL(propertiesPath), this.settings.getRootDirectory()));
        initialize();
    }

    /**
     * Initializes a new Core object with default property values.
     */
    public Core()
    {
        this.settings = new CoreSettings();
        FileManager.setRootPath(URITools.contextualize(FileManager.getWorkingDirectory(), this.settings.getRootDirectory()));
        initialize();
    }
    
    /**
     * Initialization that is common to both constructors.
     */
    private void initialize()
    {
        this.graphmaster = new Graphmaster(this);
        this.bots = new Bots();
        // Get the class for the settings-specified Multiplexor.
        Class<? extends Multiplexor> multiplexorClass = null;
        try
        {
            multiplexorClass = (Class<? extends Multiplexor>)Class.forName(this.settings.getMultiplexorClassname());
        }
        catch (ClassNotFoundException e)
        {
            throw new UserError("Specified multiplexor (\"" + this.settings.getMultiplexorClassname() + "\") could not be found.", e);
        }
        catch (ClassCastException e)
        {
            throw new UserError("\"" + this.settings.getMultiplexorClassname() + "\" is not a subclass of Multiplexor.", e);
        }
        
        // Get the Multiplexor constructor that takes a Core as an argument.
        Constructor<? extends Multiplexor> constructor = null;
        try
        {
            constructor = multiplexorClass.getDeclaredConstructor(Core.class);
        }
        catch (NoSuchMethodException e)
        {
            throw new DeveloperError("Developed specified an invalid constructor for Multiplexor.", e);
        }
        catch (SecurityException e)
        {
            throw new DeveloperError("Permission denied to create new Multiplexor with specified constructor.", e);
        }
        
        // Get a new instance of the Multiplexor.
        try
        {
            this.multiplexor = constructor.newInstance(this);
        } 
        catch (IllegalAccessException e)
        {
            throw new DeveloperError("Underlying constructor for Multiplexor is inaccessible", e);
        } 
        catch (InstantiationException e)
        {
            throw new DeveloperError("Could not instantiate Multiplexor", e);
        } 
        catch (IllegalArgumentException e)
        {
            throw new DeveloperError("Illegal argument exception when creating Multiplexor", e);
        } 
        catch (InvocationTargetException e)
        {
            throw new DeveloperError("Constructor threw an exception when getting a Multiplexor instance from it", e);
        } 
        
        this.predicateMaster = new PredicateMaster(this);
        this.multiplexor.attach(this.predicateMaster);
        
        this.listeners = new HashSet<CoreListener>();
        
        // Remove all Handlers from the root logger.
        Logger rootLogger = Logger.getLogger("");
        Handler[] handlers = rootLogger.getHandlers();
        for (int index = 0; index < handlers.length; index++)
        {
            rootLogger.removeHandler(handlers[index]);
        }
        
        // Set up loggers based on the settings.
        this.logger = setupLogger("programd", this.settings.getActivityLogPath());
        this.logger.setLevel(Level.ALL);
        
        Logger matchingLogger = setupLogger("programd.matching", this.settings.getMatchingLogPath());
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
    }
    
    /**
     * Attaches the given {@link org.aitools.programd.CoreListener CoreListener}
     * so that it can be notified of certain events by the <code>Core</core>.
     * 
     * @param listener  the {@link org.aitools.programd.CoreListener CoreListener} to attach
     */
    public void attach(CoreListener listener)
    {
        this.listeners.add(listener);
    }
    
    /**
     */
    public void startup()
    {
        this.logger.log(Level.INFO, "Starting Program D version " + VERSION);
        this.logger.log(Level.INFO, "Using Java VM " + System.getProperty("java.vm.version") + " from "
                + System.getProperty("java.vendor"));
        this.logger.log(Level.INFO, "On " + System.getProperty("os.name") + " version " + System.getProperty("os.version")
                + " (" + System.getProperty("os.arch") + ")");

        this.logger.log(Level.INFO, "Predicates with no values defined will return: \"" + this.settings.getPredicateEmptyDefault()
                + "\".");
        

        try
        {
            this.logger.log(Level.INFO, "Initializing " + this.multiplexor.getClass().getSimpleName() + ".");
            
            // Initialize the Multiplexor.
            this.multiplexor.initialize();

            this.logger.log(Level.INFO, "Starting up the Graphmaster.");
            
            // Index the start time before loading.
            long time = new Date().getTime();

            // Start up the Graphmaster.
            this.graphmaster.startup(this.settings.getStartupFilePath());

            // Calculate the time used to load all categories.
            time = new Date().getTime() - time;
            
            this.logger.log(Level.INFO, this.graphmaster.getTotalCategories() + " categories loaded in " + time / 1000.00 + " seconds.");

            // Run AIMLWatcher if configured to do so.
            if (this.settings.useWatcher())
            {
                this.aimlWatcher = new AIMLWatcher(this.graphmaster);
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
                    throw new UserError(new UnspecifiedParameterError("javascript-interpreter.classname"));
                }
                
                String javascriptInterpreterClassname = this.settings.getJavascriptInterpreterClassname();
                
                if (javascriptInterpreterClassname.equals(EMPTY_STRING))
                {
                    throw new UserError(new UnspecifiedParameterError("javascript-interpreter.classname"));
                }
                
                this.logger.log(Level.INFO, "Initializing " + javascriptInterpreterClassname + ".");
                
                try
                {
                    this.interpreter = (Interpreter) Class.forName(javascriptInterpreterClassname).newInstance();
                } 
                catch (Exception e)
                {
                    throw new DeveloperError(e);
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
                // Add a simple IAmAlive Pulse (this should be more configurable).
                this.heart.addPulse(new org.aitools.programd.util.IAmAlivePulse());
                this.heart.start();
                this.logger.log(Level.INFO, "Heart started.");
            }
            
            // Notify the listeners that the Core is ready.
            for (CoreListener listener : this.listeners)
            {
                listener.coreReady();
            }
        }
        catch (DeveloperError e)
        {
            fail("developer error", e);
        }
        catch (UserError e)
        {
            fail("user error", e);
        }
        catch (RuntimeException e)
        {
            fail("unforeseen runtime exception", e);
        }
        catch (Exception e)
        {
            fail("unforeseen exception", e);
        }

        Runtime.getRuntime().addShutdownHook(new Thread("Shutdown Thread")
                {
                    public void run()
                    {
                        shutdown();
                    } 
                } );
    }
    
    /**
     * Processes the given input using default values for userid (the hostname), botid (the first available bot), and no responder.
     * The result is not returned.  This method is mostly useful for a simple test of the Core.
     * @param input the input to send
     */
    public synchronized void processResponse(String input)
    {
        String botid = this.bots.getABot().getID();
        String response = this.multiplexor.getResponse(input, this.hostname, botid);
        logResponse(input, response, this.hostname, botid);
    }
    
    /**
     * Returns the response to an input, using a Responder.
     * 
     * @param input
     *            the &quot;non-internal&quot; (possibly multi-sentence,
     *            non-substituted) input
     * @param userid
     *            the userid for whom the response will be generated
     * @param botid
     *            the botid from which to get the response
     * @param responder
     *            the Responder who cares about this response
     * @return the response
     */
    public synchronized String getResponse(String input, String userid, String botid, Responder responder)
    {
        String response = this.multiplexor.getResponse(input, userid, botid, responder);
        logResponse(input, response, userid, botid);
        return response;
    }

    /**
     * Logs a response to the chat log.
     * @param input the input that produced the response
     * @param response the response
     * @param userid the userid for whom the response was produced
     * @param botid the botid that produced the response
     */
    private void logResponse(String input, String response, String userid, String botid)
    {
        this.bots.getBot(botid).getLogger().log(new ChatLogRecord(botid, userid, input, response));
    }
    
    /**
     * Performs all necessary shutdown tasks. Shuts down the Graphmaster and all
     * BotProcesses.
     */
    public void shutdown()
    {
        this.logger.log(Level.INFO, "Program D is shutting down.");
        BotProcesses.shutdownAll();
        this.graphmaster.shutdown();
        this.logger.log(Level.INFO, "Shutdown complete.");
    }
    
    /**
     * Logs the given Throwable and shuts down.
     * @param e the Throwable to log
     */
    public void fail(Throwable e)
    {
        fail(e.getClass().getName(), e);
    }    
    
    /**
     * Logs the given Throwable and shuts down.
     * @param description the description of the Throwable
     * @param e the Throwable to log
     */
    public void fail(String description, Throwable e)
    {
        String throwableDescription = e.getClass().getSimpleName();
        if (e.getMessage() != null)
        {
            throwableDescription += ": " + e.getMessage();
        }
        else
        {
            throwableDescription += ".";
        }
        this.logger.log(Level.SEVERE, "Exiting abnormally due to " + description + " " + throwableDescription);
        
        // Notify the listeners of the failure.
        for (CoreListener listener : this.listeners)
        {
            listener.failure(e);
        }
        System.exit(1);
    }    
    
    /**
     * Sets up a Logger in a standard way.  (A FileHandler is attached with some generic settings.)
     * @param name the name of the logger
     * @param path the path for the logger's file output
     * @return the Logger that was set up.
     */
    private Logger setupLogger(String name, String path)
    {
        Logger newLogger = Logger.getLogger(name);
        FileHandler newHandler = null;
        try
        {
            newHandler = new FileHandler(path, 1024, 10, true);
        }
        catch (IOException e)
        {
            throw new UserError("I/O Error setting up a logger: ", e);            
        }
        newHandler.setFormatter(new SimpleFormatter(this.settings));
        newLogger.addHandler(newHandler);
        return newLogger;
    }
    
    /* All of these "get" methods throw a NullPointerException if the item
     * has not yet been initialized, to avoid accidents.
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
        throw new NullPointerException("The Core's Graphmaster object has not yet been initialized!");
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
        if (this.settings != null)
        {
            return this.settings;
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
}