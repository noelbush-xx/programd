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
import java.util.Date;
import java.util.Properties;

import org.alicebot.server.core.ActiveMultiplexor;
import org.alicebot.server.core.BotProcesses;
import org.alicebot.server.core.Globals;
import org.alicebot.server.core.Graphmaster;
import org.alicebot.server.core.Multiplexor;
import org.alicebot.server.core.loader.AIMLWatcher;
import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.util.DeveloperError;
import org.alicebot.server.core.util.Heart;
import org.alicebot.server.core.util.Shell;
import org.alicebot.server.core.util.Trace;
import org.alicebot.server.core.util.Toolkit;
import org.alicebot.server.core.util.UserError;
import org.alicebot.server.net.listener.AliceChatListener;

/**
 *  A server-based Alicebot.
 *
 *  @author Jon Baer
 *  @author Noel Bush
 */
public class AliceServer
{
    private Shell shell;
    private String propertiesPath;
    
    
    private AliceServer(String propertiesPath)
    {
        this.propertiesPath = propertiesPath;
     }
    
    
    public AliceServer(String propertiesPath, Shell shell)
    {
        this.propertiesPath = propertiesPath;
        this.shell = shell;
    }


    /**
     *  Tries to register any listeners in the classpath,
     *  starts the http server, and then starts a Graphmaster.
     */
    public void startup()
    {
        if (propertiesPath == null)
        {
            throw new DeveloperError("Did not specify a server properties path.");
        }
        
        if (!Globals.isLoaded())
        {
            Globals.load(propertiesPath);
            this.shell = new Shell();
        }
        String className = Globals.getProperty("programd.httpserver.classname");
        String configParameter = Globals.getProperty("programd.httpserver.config");

        // Fail if http server class name is not specified.
        if (className == null)
        {
            throw new UserError("You must specify an http server to run AliceServer. Failing.");
        }
        
        try
        {
            if (Globals.showConsole())
            {
                Log.userinfo("Starting Alicebot Program D version " + Globals.getVersion(),
                             Log.STARTUP);
                Log.userinfo("Using Java VM " + System.getProperty("java.vm.version") +
                             " from " + System.getProperty("java.vendor"), Log.STARTUP);
                Log.userinfo("On " + System.getProperty("os.name") + " version " +
                             System.getProperty("os.version") + " (" +
                             System.getProperty("os.arch") + ")", Log.STARTUP);

                Log.userinfo("Predicates with no values defined will return: \"" +
                             Globals.getPredicateEmptyDefault() + "\".", Log.STARTUP);
            }
            
            // Start the http server.
            startHttpServer(className, configParameter);

            // Initialize the ActiveMultiplexor.getInstance().
            if (Globals.showConsole())
            {
                Log.userinfo("Initializing Multiplexor.", Log.STARTUP);
            }
            ActiveMultiplexor.getInstance().initialize();

            String serverAddress = serverAddress = "http://" + Globals.getHostName() + ":" + Globals.getHttpPort();

            // Index the start time before loading.
            long time = new Date().getTime();


            if (Globals.showConsole())
            {
                Log.userinfo("Loading Graphmaster.", Log.STARTUP);
            }

            // Load the startup file (and whatever it specifies).
            Graphmaster.load(Globals.getStartupFilePath(), null);

            // Calculate the time used to load all categories.
            time = new Date().getTime() - time;

            // Tell the Graphmaster that it is ready; let it report.
            Graphmaster.markReady();

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

            // Request garbage collection.
            System.gc();
            
            // Start the heart, if enabled.
            if (Globals.useHeart())
            {
                Heart.start();
                Trace.userinfo("Heart started.");
            }

            // If shell is enabled, start it.
            if (Globals.useShell())
            {
                shell.run();
                Trace.devinfo("Shell exited.");
            }
            else
            {
                if (Globals.showConsole())
                {
                    Log.userinfo("Interactive shell disabled.  Awaiting interrupt to shut down.", Log.STARTUP);
                }
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
        catch (DeveloperError e)
        {
            Log.devfail(e);
            Log.userfail("Exiting abnormally due to developer error.", Log.ERROR);
            System.exit(1);
        }
        catch (UserError e)
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
     *  Tries to instantiate
     *  an http server of unpredetermined type.
     *  We wish to be compatible with any server, not
     *  just the choix du jour.
     */
    private static void startHttpServer(String className, String configParameter)
    {
        // First, see if the http server class can be found.
        Class serverClass;
        try
        {
            serverClass = Class.forName(className);
        }
        catch (ClassNotFoundException e)
        {
            throw new UserError("Could not find http server \"" + className + "\".");
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
            throw new UserError("Couldn't instantiate http server \"" + className + "\".");
        }
        catch (IllegalAccessException e)
        {
            throw new DeveloperError("The constructor for \"" + className +
                           "\" or the class itself is not available.");
        }
        catch (ClassCastException e)
        {
            throw new DeveloperError("\"" + className +
                           "\" is not an implementation of AliceCompatibleHttpServer.");
        }

        /*
            If the server config parameter was defined, and if
            the http server is an implementation of AliceCompatibleHttpServer,
            configure it.
        */
        if (configParameter != null)
        {
            try
            {
                server.configure(configParameter);
            }
            catch (IOException e)
            {
                throw new UserError("Could not find \"" + configParameter + "\".");
            }
        }

        // Start the server as one of the BotProcesses.
        BotProcesses.start(server, "http server");
    }

    
    public static void main(String[] args)
    {
        String serverPropertiesPath;

        if (args.length > 0)
        {
            serverPropertiesPath = args[0];
        }
        else
        {
            serverPropertiesPath = "server.properties";
        }
        
        AliceServer server = new AliceServer(serverPropertiesPath);

        Runtime.getRuntime().addShutdownHook(
            new Thread("Shutdown Thread")
            {
                public void run()
                {
                    shutdown();
                }
            });

        server.startup();
    }
    

    /**
     *  Performs all necessary shutdown tasks.
     *  Shuts down the Graphmaster and all BotProcesses.
     */
    public static void shutdown()
    {
        Trace.userinfo("AliceServer is shutting down.");
        BotProcesses.shutdownAll();
        Graphmaster.shutdown();
        Trace.userinfo("Shutdown complete.");
    }
}
