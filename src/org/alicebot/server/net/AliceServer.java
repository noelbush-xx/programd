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
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Date;
import java.util.Properties;

import org.alicebot.server.core.ActiveMultiplexor;
import org.alicebot.server.core.Globals;
import org.alicebot.server.core.Graphmaster;
import org.alicebot.server.core.loader.AIMLWatcher;
import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.logging.Trace;
import org.alicebot.server.core.util.Toolkit;
import org.alicebot.server.net.listener.AliceChatListener;

public class AliceServer implements Runnable
{
    /** The Graphmaster used by the server. */
    private static Graphmaster graphmaster;

    /** The Thread that controls the Graphmaster. */
    private static Thread gmThread;
    
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
        serverProperties = new Properties();
        try
        {
            serverProperties.load(new FileInputStream(serverPropertiesPath));
        }
        catch (IOException e)
        {
            // Error loading properties
            Log.userfail("Could not find \"" + serverPropertiesPath + "\"!", new String[] {Log.ERROR, Log.STARTUP});
        }

        HTTP_SERVER_CLASS_NAME =
            serverProperties.getProperty("programd.httpserver.classname");
        HTTP_SERVER_CONFIG_PARAMETER =
            serverProperties.getProperty("programd.httpserver.config");

        // Fail if http server class name is not specified.
        if (HTTP_SERVER_CLASS_NAME == null)
        {
            Log.userfail("You must specify an http server to run AliceServer. Failing.",
                new String[] {Log.STARTUP, Log.ERROR});
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
            Log.userinfo("Bot predicates with no values defined will return: \"" +
                         Globals.getBotPredicateEmptyDefault() + "\".", Log.STARTUP);
        }
        
        if (serverProperties.getProperty("programd.listeners.scan", "false").equals("true"))
        {
            initializeChatListeners();
        }

        // Start the http server.
        startHttpServer();

        // Initialize the ActiveMultiplexor.
        Log.userinfo("Initializing Multiplexor.", Log.STARTUP);
        ActiveMultiplexor.StaticSelf.initialize();

        Log.userinfo("Starting Graphmaster.", Log.STARTUP);

        String serverAddress = serverAddress = "http://" + Globals.getHostName() + ":" + Globals.getHttpPort();

        // Create a new Graphmaster with the message to display after loading.
        graphmaster = new Graphmaster("HTTP Server listening at " + serverAddress);

        // Index the start time before loading.
        long time = new Date().getTime();

        // Start the Graphmaster loading.
        graphmaster.load(Globals.getStartupFilePath());

        // Also load the targets file.
        graphmaster.load(Globals.getTargetsAIMLPath());

        // Calculate the time needed to load all categories.
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

        // Now start the main Graphmaster thread (just runs an interactive shell).
        gmThread = new Thread(graphmaster);
        gmThread.setPriority(Thread.MAX_PRIORITY-1);
        gmThread.start();
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
                AliceChatListener listenerInstance = null;
                try
                {
                    listenerInstance = (AliceChatListener)listeners[index].newInstance();
                }
                catch (InstantiationException e)
                {
                    // Couldn't instantiate listener.
                    Log.devfail("Could not instantiate listener \"" + listeners[index].getName() + "\"",
                                new String[] {Log.STARTUP, Log.ERROR});
                }
                catch (IllegalAccessException e)
                {
                    // Couldn't instantiate listener.
                    Log.devfail("Cannot access AliceChatListener base class.",
                                new String[] {Log.STARTUP, Log.ERROR});
                }
                // Just double-checking the Toolkit method.
                catch (ClassCastException e)
                {
                    Log.devfail("\"" + listeners[index] + "\" is not an AliceChatListener.",
                                new String[] {Log.STARTUP, Log.ERROR});
                }

                // Initialize the listener with the server properties.
                listenerInstance.initialize(serverProperties);
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
        Class serverClass = null;
        try
        {
            serverClass = Class.forName(HTTP_SERVER_CLASS_NAME);
        }
        catch (ClassNotFoundException e)
        {
            Log.userfail("Could not find http server \"" +
                           HTTP_SERVER_CLASS_NAME + "\".", new String[] {Log.STARTUP, Log.ERROR});
        }

        // Now, try to get an instance of the server.
        AliceCompatibleHttpServer server = null;

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
            Log.userfail("Couldn't instantiate http server \"" +
                           HTTP_SERVER_CLASS_NAME + "\".",
                         new String[] {Log.STARTUP, Log.ERROR});
        }
        catch (IllegalAccessException e)
        {
            Log.devfail("The constructor for \"" +
                           HTTP_SERVER_CLASS_NAME +
                           "\" or the class itself is not available.",
                        new String[] {Log.STARTUP, Log.ERROR});
        }
        catch (ClassCastException e)
        {
            Log.devfail("\"" + HTTP_SERVER_CLASS_NAME +
                           "\" is not an implementation of AliceCompatibleHttpServer.",
                        new String[] {Log.STARTUP, Log.ERROR});
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
                Log.userfail("Could not find \"" + HTTP_SERVER_CONFIG_PARAMETER + "\".",
                             new String[] {Log.STARTUP, Log.ERROR});
            }
        }
        
        // Start the server.
        server.start();
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
        new Thread(new AliceServer()).start();
    }
    
}
