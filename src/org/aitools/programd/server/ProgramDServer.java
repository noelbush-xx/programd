/*    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
    USA.
*/

package org.aitools.programd.server;

import java.io.IOException;
import java.util.Date;

import org.aitools.programd.bot.BotProcesses;
import org.aitools.programd.graph.Graphmaster;
import org.aitools.programd.loader.AIMLWatcher;
import org.aitools.programd.multiplexor.ActiveMultiplexor;
import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.Globals;
import org.aitools.programd.util.Heart;
import org.aitools.programd.util.Shell;
import org.aitools.programd.util.Trace;
import org.aitools.programd.util.UserError;
import org.aitools.programd.util.logging.Log;

/**
 *  A server-based Alicebot.
 *
 *  @author Jon Baer
 *  @author Noel Bush
 */
public class ProgramDServer
{
    private Shell shell;
    private String propertiesPath;

    private ProgramDServer(String propertiesPath)
    {
        this.propertiesPath = propertiesPath;
    }

    public ProgramDServer(String propertiesPath, Shell shell)
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
        String configParameter =
            Globals.getProperty("programd.httpserver.config");

        // Fail if http server class name is not specified.
        if (className == null)
        {
            throw new UserError("You must specify an http server to run ProgramDServer. Failing.");
        }

        try
        {
            if (Globals.showConsole())
            {
                Log.userinfo(
                    "Starting Alicebot Program D version "
                        + Globals.getVersion(),
                    Log.STARTUP);
                Log.userinfo(
                    "Using Java VM "
                        + System.getProperty("java.vm.version")
                        + " from "
                        + System.getProperty("java.vendor"),
                    Log.STARTUP);
                Log.userinfo(
                    "On "
                        + System.getProperty("os.name")
                        + " version "
                        + System.getProperty("os.version")
                        + " ("
                        + System.getProperty("os.arch")
                        + ")",
                    Log.STARTUP);

                Log.userinfo(
                    "Predicates with no values defined will return: \""
                        + Globals.getPredicateEmptyDefault()
                        + "\".",
                    Log.STARTUP);
            }

            // Start the http server.
            startHttpServer(className, configParameter);

            // Initialize the ActiveMultiplexor.getInstance().
            if (Globals.showConsole())
            {
                Log.userinfo("Initializing Multiplexor.", Log.STARTUP);
            }
            ActiveMultiplexor.getInstance().initialize();

            String serverAddress =
                serverAddress =
                    "http://"
                        + Globals.getHostName()
                        + ":"
                        + Globals.getHttpPort();

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
                Log.userinfo(
                    Graphmaster.getTotalCategories()
                        + " categories loaded in "
                        + (float) time / 1000.00
                        + " seconds.",
                    Log.STARTUP);

                // Describe whether AIMLWatcher is running.
                if (Globals.isWatcherActive())
                {
                    AIMLWatcher.start();
                    Log.userinfo("The AIML Watcher is active.", Log.STARTUP);
                }
                else
                {
                    Log.userinfo(
                        "The AIML Watcher is not active.",
                        Log.STARTUP);
                }

                // Give server info.
                Log.userinfo(
                    "HTTP server listening at " + serverAddress,
                    Log.STARTUP);
            }

            // If configured, start up a browser with the address.
            String browserCommand =
                Globals.getProperty("programd.browser-launch");
            if (browserCommand != null
                && !serverAddress.equals("unknown address"))
            {
                try
                {
                    Runtime.getRuntime().exec(
                        browserCommand + " " + serverAddress);
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
                    Log.userinfo(
                        "Interactive shell disabled.  Awaiting interrupt to shut down.",
                        Log.STARTUP);
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
            Log.userfail(
                "Exiting abnormally due to developer error.",
                Log.ERROR);
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
            Log.userfail(
                "Exiting abnormally due to unforeseen runtime exception.",
                e,
                Log.ERROR);
            System.exit(1);
        }
        catch (Exception e)
        {
            Log.userfail(
                "Exiting abnormally due to unforeseen exception.",
                e,
                Log.ERROR);
            System.exit(1);
        }
    }

    /**
     *  Tries to instantiate
     *  an http server of unpredetermined type.
     *  We wish to be compatible with any server, not
     *  just the choix du jour.
     */
    private static void startHttpServer(
        String className,
        String configParameter)
    {
        // First, see if the http server class can be found.
        Class serverClass;
        try
        {
            serverClass = Class.forName(className);
        }
        catch (ClassNotFoundException e)
        {
            throw new UserError(
                "Could not find http server \"" + className + "\".");
        }

        // Now, try to get an instance of the server.
        ProgramDCompatibleHttpServer server;

        /*
            Any http server must implement ProgramDCompatibleHttpServer.
            The interface itself is very trivial, and is just a way
            for us to isolate dependencies on particular http servers
            (non-GPL) to a single wrapper class.
        */
        try
        {
            server = (ProgramDCompatibleHttpServer) serverClass.newInstance();
        }
        catch (InstantiationException e)
        {
            throw new UserError(
                "Couldn't instantiate http server \"" + className + "\".");
        }
        catch (IllegalAccessException e)
        {
            throw new DeveloperError(
                "The constructor for \""
                    + className
                    + "\" or the class itself is not available.");
        }
        catch (ClassCastException e)
        {
            throw new DeveloperError(
                "\""
                    + className
                    + "\" is not an implementation ofProgramDCompatibleHttpServerr.");
        }

        /*
            If the server config parameter was defined, and if
            the http server is an implementation of ProgramDCompatibleHttpServer,
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
                throw new UserError(
                    "Could not find \"" + configParameter + "\".");
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

        ProgramDServer server = new ProgramDServer(serverPropertiesPath);

        Runtime.getRuntime().addShutdownHook(new Thread("Shutdown Thread")
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
        Trace.userinfo("ProgramDServer is shutting down.");
        BotProcesses.shutdownAll();
        Graphmaster.shutdown();
        Trace.userinfo("Shutdown complete.");
    }
}
