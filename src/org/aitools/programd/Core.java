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
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.aitools.programd.bot.Bots;
import org.aitools.programd.bot.BotProcesses;
import org.aitools.programd.graph.Graphmaster;
import org.aitools.programd.interpreter.Interpreter;
import org.aitools.programd.loader.AIMLWatcher;
import org.aitools.programd.logging.ChatLogRecord;
import org.aitools.programd.multiplexor.Multiplexor;
import org.aitools.programd.multiplexor.PredicateMaster;
import org.aitools.programd.parser.TemplateParser;
import org.aitools.programd.responder.Responder;
import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.Heart;
import org.aitools.programd.util.UserError;

/**
 * A "core" version of Program D, independent of any interfaces.
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
    public static final String VERSION = "4.2";

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
        initialize();
    }

    /**
     * Initializes a new Core object with default property values.
     */
    public Core()
    {
        this.settings = new CoreSettings();
        initialize();
    }
    
    /**
     * Initialization that is common to both constructors.
     */
    private void initialize()
    {
        this.graphmaster = new Graphmaster(this);
        this.bots = new Bots();
        this.predicateMaster = new PredicateMaster(this);
        // Set up loggers based on the settings.

        try
        {
            this.logger = Logger.getLogger("programd.core");
            this.logger.addHandler(new FileHandler(this.settings.getLoggingErrorPath(), 1024, 10));
            
            Logger databaseLogger = Logger.getLogger("programd.database");
            databaseLogger.addHandler(new FileHandler(this.settings.getLoggingDatabasePath(), 1024, 10));
            
            Logger errorLogger = Logger.getLogger("programd.error");
            errorLogger.addHandler(new FileHandler(this.settings.getLoggingErrorPath(), 1024, 10));

            Logger gossipLogger = Logger.getLogger("programd.gossip");
            gossipLogger.addHandler(new FileHandler(this.settings.getLoggingInterpreterPath(), 1024, 10));

            Logger interpreterLogger = Logger.getLogger("programd.interpreter");
            interpreterLogger.addHandler(new FileHandler(this.settings.getLoggingInterpreterPath(), 1024, 10));
            
            Logger learnLogger = Logger.getLogger("programd.learn");
            learnLogger.addHandler(new FileHandler(this.settings.getLoggingLearnPath(), 1024, 10));
            
            Logger listenerLogger = Logger.getLogger("programd.listener");
            listenerLogger.addHandler(new FileHandler(this.settings.getLoggingListenerPath(), 1024, 10));
            
            Logger matchingLogger = Logger.getLogger("programd.matching");
            matchingLogger.addHandler(new FileHandler(this.settings.getLoggingMatchingPath(), 1024, 10));
            
            Logger mergeLogger = Logger.getLogger("programd.merge");
            mergeLogger.addHandler(new FileHandler(this.settings.getLoggingMergePath(), 1024, 10));
            
            Logger responderLogger = Logger.getLogger("programd.responder");
            responderLogger.addHandler(new FileHandler(this.settings.getLoggingResponderPath(), 1024, 10));
            
            Logger startupLogger = Logger.getLogger("programd.startup");
            startupLogger.addHandler(new FileHandler(this.settings.getLoggingStartupPath(), 1024, 10));
            
            Logger systemLogger = Logger.getLogger("programd.system");
            systemLogger.addHandler(new FileHandler(this.settings.getLoggingSystemPath(), 1024, 10));
        }
        catch (IOException e)
        {
            throw new UserError("I/O Error setting up a logger: " + e.getMessage());
        }
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
        
        // Get the class for the settings-specified Multiplexor.
        Class forName = null;
        try
        {
            forName = Class.forName(this.settings.multiplexorClassname);
        } 
        catch (ClassNotFoundException e)
        {
            throw new UserError("Specified multiplexor (\"" + this.settings.multiplexorClassname + "\") could not be found.");
        }
        
        // Get the Multiplexor constructor that takes a Core as an argument.
        Constructor<Multiplexor> constructor = null;
        try
        {
            constructor = forName.getDeclaredConstructor(new Class[] {Core.class});
        }
        catch (NoSuchMethodException e)
        {
            throw new DeveloperError("Developed specified an invalid constructor for Multiplexor: " + e.getMessage());
        }
        catch (SecurityException e)
        {
            throw new DeveloperError("Permission denied to create new Multiplexor with specified constructor: " + e.getMessage());
        }
        
        // Get a new instance of the Multiplexor.
        try
        {
            this.multiplexor = constructor.newInstance(new Object[] {this});
        } 
        catch (IllegalAccessException e)
        {
            throw new DeveloperError("Underlying constructor for Multiplexor is inaccessible: " + e.getMessage());
        } 
        catch (InstantiationException e)
        {
            throw new DeveloperError("Could not instantiate Multiplexor: " + e.getMessage());
        } 
        catch (IllegalArgumentException e)
        {
            throw new DeveloperError("Illegal argument exception when creating Multiplexor: " + e.getMessage());
        } 
        catch (InvocationTargetException e)
        {
            throw new DeveloperError("Constructor threw an exception when getting a Multiplexor instance from it: " + e.getMessage());
        } 

        try
        {
            this.logger.log(Level.INFO, "Initializing " + this.settings.multiplexorClassname + ".");
            
            // Initialize the Multiplexor.
            this.multiplexor.initialize();

            this.logger.log(Level.INFO, "Starting up the Graphmaster.");
            
            // Index the start time before loading.
            long time = new Date().getTime();

            // Start up the Graphmaster.
            this.graphmaster.startup(this.settings.startupFilePath);

            // Calculate the time used to load all categories.
            time = new Date().getTime() - time;
            
            this.logger.log(Level.INFO, this.graphmaster.getTotalCategories() + " categories loaded in " + time / 1000.00 + " seconds.");

            // Run AIMLWatcher if configured to do so.
            if (this.settings.useWatcher)
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
            if (this.settings.javascriptAllowed)
            {
                if (this.settings.javascriptInterpreterClassname == null)
                {
                    throw new UserError("No JavaScript interpreter defined!");
                }
                
                String javascriptInterpreterClassname = this.settings.javascriptInterpreterClassname;
                
                if (javascriptInterpreterClassname.equals(EMPTY_STRING))
                {
                    throw new UserError("No JavaScript interpreter defined!");
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
            if (this.settings.heartEnabled)
            {
                this.heart = new Heart(this.settings.getHeartPulserate());
                // Add a simple IAmAlive Pulse (this should be more configurable).
                this.heart.addPulse(new org.aitools.programd.util.IAmAlivePulse());
                this.heart.start();
                this.logger.log(Level.INFO, "Heart started.");
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
     * Returns the response to a non-internal input, using a Responder.
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
     */
    public synchronized String getResponse(String input, String userid, String botid, Responder responder)
    {
        String response = this.multiplexor.getResponse(input, userid, botid, responder);
        logResponse(input, response, userid, botid);
        return response;
    }

    /**
     * <p>
     * Produces a response to an &quot;internal&quot; input sentence -- i.e., an
     * input that has been produced by a <code>srai</code>.
     * </p>
     * <p>
     * The main differences between this and
     * {@link #getResponse(String,String,String,Responder)} are that this method
     * takes an already-existing <code>TemplateParser</code>, <i>doesn't </i>
     * take a <code>Responder</code>, and assumes that the inputs have
     * already been normalized.
     * </p>
     * 
     * @param input
     *            the input sentence
     * @param userid
     *            the userid requesting the response
     * @param botid
     *            the botid from which to get the response
     * @param parser
     *            the parser object to update when generating the response
     */
    public String getInternalResponse(String input, String userid, String botid, TemplateParser parser)
    {
        String response = this.multiplexor.getInternalResponse(input, userid, botid, parser);
        logResponse(input, response, userid, botid);
        return response;
    }
    
    /**
     * Logs a response to the chat log.
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
     * Logs the given exception and shuts down.
     * 
     * @param exception
     */
    public void fail(Throwable e)
    {
        this.logger.log(Level.SEVERE, "Exiting abnormally due to " + e.getClass().getName() + ": " + e.getMessage());
        System.exit(1);
    }    
    
    /**
     * Logs the given exception and shuts down.
     * 
     * @param exception
     */
    public void fail(String description, Throwable e)
    {
        this.logger.log(Level.SEVERE, "Exiting abnormally due to " + description + ": " + e.getMessage());
        System.exit(1);
    }    
    
    /**
     * @return the object that manages information about all bots
     */
    public Bots getBots()
    {
        return this.bots;
    }

    /**
     * @return the Graphmaster
     */
    public Graphmaster getGraphmaster()
    {
        return this.graphmaster;
    }
    
    /*
     * @return the Multiplexor
     */
    public Multiplexor getMultiplexor()
    {
        return this.multiplexor;
    }
    
    /**
     * @return the PredicateMaster
     */
    public PredicateMaster getPredicateMaster()
    {
        return this.predicateMaster;
    }
    
    /**
     * @return the AIMLWatcher
     */
    public AIMLWatcher getAIMLWatcher()
    {
        return this.aimlWatcher;
    }
    
    /**
     * @return the settings for this core
     */
    public CoreSettings getSettings()
    {
        return this.settings;
    }
    
    /**
     * @return the active JavaScript interpreter
     */
    public Interpreter getInterpreter()
    {
        return this.interpreter;
    }
}