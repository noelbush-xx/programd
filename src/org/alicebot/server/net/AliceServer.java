/*
    Alicebot Program D
    Copyright (C) 1995-2001, A.L.I.C.E. AI Foundation
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
    USA.
*/

/*
    Code cleanup (4.1.3 [00] - October 2001, Noel Bush)
    - formatting cleanup
    - general grammar fixes
    - complete javadoc
    - made all imports explicit
    - removed silly "extends Object" ;-)
    - added kldugy initializeClassListeners()
      to scan classpath for known listeners and initialize them
    - implemented less lazy exception handling
    - added ability to specify different http server
    - moved note about http server config to here (out of Graphmaster)
    - changed this to implement Runnable (start() becomes run())
*/

/*
    Further cleanup (4.1.3 [01] - November 2001, Noel Bush)
    - added ability to specify server properties file from command line
*/

/*
    More fixes (4.1.3 [02] - November 2001, Noel Bush)
    - changed some server property names
    - added notification about AIMLWatcher from here
    - added (perhaps silly) feature that can execute a command on
      startup, for launching a browser
*/

package org.alicebot.server.net;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Properties;

import org.alicebot.server.core.ActiveMultiplexor;
import org.alicebot.server.core.BotProcesses;
import org.alicebot.server.core.Globals;
import org.alicebot.server.core.Graphmaster;
import org.alicebot.server.core.Multiplexor;
import org.alicebot.server.core.loader.AIMLWatcher;
import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.util.DeveloperErrorException;
import org.alicebot.server.core.util.Shell;
import org.alicebot.server.core.util.Trace;
import org.alicebot.server.core.util.Toolkit;
import org.alicebot.server.core.util.UserErrorException;
import org.alicebot.server.net.listener.AliceChatListener;

/**
 *  A server-based Alicebot.
 *
 *  @author Jon Baer
 */
public class AliceServer implements Runnable
{
    /** The path to the server properties file. */
    private static String serverPropertiesPath;
    
    /** The server properties. */
    private static Properties serverProperties;

    /** The current bot. */
    private static String bot;

    /** The fully-qualified class name of the http server to use. */
    private static String HTTP_SERVER_CLASS_NAME;

    /** The parameter for configuring the http server. Presently only one is supported. Optional. */
    private static String HTTP_SERVER_CONFIG_PARAMETER;


    /**
     *  Loads the bot configuration and initializes the
     *  {@link org.alicebot.server.core.Globals Globals}.
     */
    public AliceServer()
    {
        HTTP_SERVER_CLASS_NAME =
            serverProperties.getProperty("programd.httpserver.classname");
        HTTP_SERVER_CONFIG_PARAMETER =
            serverProperties.getProperty("programd.httpserver.config");

        // Fail if http server class name is not specified.
        if (HTTP_SERVER_CLASS_NAME == null)
        {
            throw new UserErrorException("You must specify an http server to run AliceServer. Failing.");
        }

        Globals.load(serverProperties);
    }
    

    /**
     *  Tries to register any listeners in the classpath,
     *  starts the http server, and then starts a Graphmaster.
     */
    public void run()
    {
        Log.userinfo("Starting Alicebot Program D version " + Globals.getVersion(),
                     Log.STARTUP);
        Log.userinfo("Using Java VM " + System.getProperty("java.vm.version") +
                     " from " + System.getProperty("java.vendor"), Log.STARTUP);
        Log.userinfo("On " + System.getProperty("os.name") + " version " +
                     System.getProperty("os.version") + " (" +
                     System.getProperty("os.arch") + ")", Log.STARTUP);

         // Set & Print EmptyDefault if console enabled
        if (Globals.showConsole())
        {
            Log.userinfo("Predicates with no values defined will return: \"" +
                         Globals.getPredicateEmptyDefault() + "\".", Log.STARTUP);
        }
        
        // Start the http server.
        startHttpServer();

        // Initialize the ActiveMultiplexor.getInstance().
        Log.userinfo("Initializing Multiplexor.", Log.STARTUP);
        ActiveMultiplexor.getInstance().initialize();

        Log.userinfo("Starting Graphmaster.", Log.STARTUP);

        String serverAddress = serverAddress = "http://" + Globals.getHostName() + ":" + Globals.getHttpPort();

        // Index the start time before loading.
        long time = new Date().getTime();

        // Load the startup file (and whatever it specifies).
        Graphmaster.load(Globals.getStartupFilePath());

        // Also load the targets file.
        Graphmaster.load(Globals.getTargetsAIMLPath());

        // Tell the Graphmaster that it is ready; let it report.
        Graphmaster.ready();

        // Calculate the time used to load all categories.
        time = new Date().getTime() - time;

        if (Globals.showConsole())
        {
            // Give load time statistics.
            Log.userinfo(Graphmaster.getTotalCategories() + " categories loaded in " + (float)time / 1000.00 + " seconds.", Log.STARTUP);

            // Describe whether AIMLWatcher is running.
            if (Globals.isWatcherActive())
            {
                AIMLWatcher.start();
                Log.userinfo("The AIML Watcher is active.", Log.STARTUP);
            }
            else
            {
                Log.userinfo("The AIML Watcher is not active." , Log.STARTUP);
            }

            // Give server info.
            Log.userinfo("HTTP server listening at " + serverAddress, Log.STARTUP);
        }

        // If configured, start up a browser with the address.
        String browserCommand = Globals.getProperty("programd.browser-launch");
        if (browserCommand != null && !serverAddress.equals("unknown address"))
        {
            try
            {
                Runtime.getRuntime().exec(browserCommand + " " + serverAddress);
            }
            catch (IOException e)
            {
                Trace.userinfo("Could not launch your web browser. Sorry.");
            }
        }

        // Start the chat listeners (if enabled).
        if (serverProperties.getProperty("programd.listeners.scan", "false").equals("true"))
        {
            initializeChatListeners();
        }

        // If shell is enabled, start it.
        if (Globals.useShell())
        {
            Shell.run();
            Trace.devinfo("Shell exited.");
        }
        else
        {
            Log.userinfo("Interactive shell disabled.  Awaiting interrupt to shut down.", Log.STARTUP);
            while (true)
            {
                try
                {
                    Thread.sleep(86400000);
                }
                catch (InterruptedException e)
                {
                    // That's it!
                }
            }
        }
    }


    /**
     *  This kludgy method would be better replaced by
     *  a registration scheme, since it just searches the
     *  classpath for implementations of the
     *  {@link org.alicebot.server.listener.AliceChatListener AliceChatListener}
     *  interface, which is a rather slow method.
    */
    private static void initializeChatListeners()
    {
        // Scan the classpath for implementations of AliceChatListener.
        Log.userinfo("Scanning for AliceChatListeners.", Log.STARTUP);

        Class[] listeners =
            Toolkit.getImplementorsOf("org.alicebot.server.net.listener.AliceChatListener", true);

        // If some listeners were found, try to initialize them.
        if (listeners != null)
        {
            for (int index = 0; index < listeners.length; index++)
            {
                AliceChatListener listenerInstance;
                String listenerName;
                try
                {
                    listenerInstance = (AliceChatListener)listeners[index].newInstance();
                    listenerName = listeners[index].getName();
                    listenerName = listenerName.substring(listenerName.lastIndexOf('.') + 1);
                }
                catch (InstantiationException e)
                {
                    // Couldn't instantiate listener.
                    throw new DeveloperErrorException("Could not instantiate listener \"" + listeners[index].getName() + "\"");
                }
                catch (IllegalAccessException e)
                {
                    // Couldn't instantiate listener.
                    throw new DeveloperErrorException("Cannot access AliceChatListener base class.");
                }
                // Just double-checking the Toolkit method.
                catch (ClassCastException e)
                {
                    throw new DeveloperErrorException("\"" + listeners[index] + "\" is not an AliceChatListener.");
                }

                // Try to initialize the listener with the server properties.
                if (listenerInstance.initialize(serverProperties))
                {
                    // Add it to the BotProcesses and start it.
                    Trace.userinfo("Starting " + listenerName + "...");
                    BotProcesses.start(listenerInstance, listenerName);
                }
            }
        }
    }
    

    /**
     *  Tries to instantiate
     *  an http server of unpredetermined type.
     *  We wish to be compatible with any server, not
     *  just the choix du jour.
     */
    private static void startHttpServer()
    {
        // First, see if the http server class can be found.
        Class serverClass;
        try
        {
            serverClass = Class.forName(HTTP_SERVER_CLASS_NAME);
        }
        catch (ClassNotFoundException e)
        {
            throw new UserErrorException("Could not find http server \"" +
                           HTTP_SERVER_CLASS_NAME + "\".");
        }

        // Now, try to get an instance of the server.
        AliceCompatibleHttpServer server;

        /*
            Any http server must implement AliceCompatibleHttpServer.
            The interface itself is very trivial, and is just a way
            for us to isolate dependencies on particular http servers
            (non-GPL) to a single wrapper class.
        */
        try
        {
            server = (AliceCompatibleHttpServer)serverClass.newInstance();
        }
        catch (InstantiationException e)
        {
            throw new UserErrorException("Couldn't instantiate http server \"" +
                           HTTP_SERVER_CLASS_NAME + "\".");
        }
        catch (IllegalAccessException e)
        {
            throw new DeveloperErrorException("The constructor for \"" +
                           HTTP_SERVER_CLASS_NAME +
                           "\" or the class itself is not available.");
        }
        catch (ClassCastException e)
        {
            throw new DeveloperErrorException("\"" + HTTP_SERVER_CLASS_NAME +
                           "\" is not an implementation of AliceCompatibleHttpServer.");
        }

        /*
            If the server config parameter was defined, and if
            the http server is an implementation of AliceCompatibleHttpServer,
            configure it.
        */
        if (HTTP_SERVER_CONFIG_PARAMETER != null)
        {
            try
            {
                server.configure(HTTP_SERVER_CONFIG_PARAMETER);
            }
            catch (IOException e)
            {
                throw new UserErrorException("Could not find \"" + HTTP_SERVER_CONFIG_PARAMETER + "\".");
            }
        }

        // Start the server as one of the BotProcesses.
        BotProcesses.start(server, "http server");
    }

    
    public static void main(String[] args)
    {
        if (args.length > 0)
        {
            serverPropertiesPath = args[0];
        }
        else
        {
            serverPropertiesPath = "server.properties";
        }

        serverProperties = new Properties();
        try
        {
            serverProperties.load(new FileInputStream(serverPropertiesPath));
        }
        catch (IOException e)
        {
            // Error loading properties
            System.err.println("Could not find \"" + serverPropertiesPath + "\"!");
            System.exit(1);
        }

        Runtime.getRuntime().addShutdownHook(
            new Thread("Shutdown Thread")
            {
                public void run()
                {
                    shutdown();
                }
            });
        try
        {
            new Thread(new AliceServer(), "AliceServer").start();
        }
        catch (DeveloperErrorException e)
        {
            Log.devfail(e);
            Log.userfail("Exiting abnormally due to developer error.", Log.ERROR);
            System.exit(1);
        }
        catch (UserErrorException e)
        {
            Log.userfail(e);
            Log.userfail("Exiting abnormally due to user error.", Log.ERROR);
            System.exit(1);
        }
        catch (RuntimeException e)
        {
            Log.userfail("Exiting abnormally due to unforeseen runtime exception.", e, Log.ERROR);
            System.exit(1);
        }
        catch (Exception e)
        {
            Log.userfail("Exiting abnormally due to unforeseen exception.", e, Log.ERROR);
            System.exit(1);
        }
    }
    

    /**
     *  Performs all necessary shutdown tasks.
     *  Shuts down the Graphmaster and all BotProcesses.
     */
    private static void shutdown()
    {
        Trace.userinfo("AliceServer is shutting down.");
        Trace.devinfo("Shutting down bot processes.");
        BotProcesses.shutdownAll();
        Trace.devinfo("Shutting down Graphmaster.");
        Graphmaster.shutdown();
        Trace.devinfo("Shutdown complete.");
    }
}
