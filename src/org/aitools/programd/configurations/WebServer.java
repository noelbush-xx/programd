/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.configurations;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.aitools.programd.Core;
import org.aitools.programd.bot.BotProcesses;
import org.aitools.programd.interfaces.Console;
import org.aitools.programd.server.ProgramDCompatibleHttpServer;
import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.UnspecifiedParameterError;
import org.aitools.programd.util.UserError;

/**
 * An implementation of Program D combined with a web server.
 * 
 * @author Noel Bush
 */
public class WebServer
{
    /** The Core to which this web server will be attached. */
    private Core core;

    /** The Console that will (may) be created for this web server. */
    private Console console;

    /** The logger for web server activity. */
    private Logger logger;

    /** Whatever HTTP server is going to be used. */
    private ProgramDCompatibleHttpServer server;

    /** The WebServer settings. */
    private WebServerSettings settings;

    /**
     * A WebServer without a console.
     * 
     * @param corePropertiesPath        the path to the console properties file
     * @param webServerPropertiesPath   the path to the web server properties file
     */
    public WebServer(String corePropertiesPath, String webServerPropertiesPath)
    {
        launch(corePropertiesPath, webServerPropertiesPath);
    }

    /**
     * A WebServer with a console.
     * 
     * @param corePropertiesPath        the path to the console properties file
     * @param webServerPropertiesPath   the path to the web server properties file
     * @param consolePropertiesPath     the path to the console properties file
     */
    public WebServer(String corePropertiesPath, String webServerPropertiesPath, String consolePropertiesPath)
    {
        this.console = new Console(consolePropertiesPath);
        launch(corePropertiesPath, webServerPropertiesPath);
    }

    private void launch(String corePropertiesPath, String webServerPropertiesPath)
    {
        this.core = new Core(corePropertiesPath);
        if (this.console != null)
        {
            this.console.attachTo(this.core);
        }
        this.core.startup();
        this.settings = new WebServerSettings(webServerPropertiesPath);

        this.logger = Logger.getLogger("programd");
        try
        {
            this.logger.addHandler(new FileHandler(this.settings.getLogPath(), 1024, 10));
        }
        catch (IOException e)
        {
            throw new UserError("Could not open web server log path \"" + this.settings.getLogPath() + "\".", e);
        }

        String className = this.settings.getHttpserverClassname();

        // Fail if http server class name is not specified.
        if (className == null)
        {
            throw new UserError(new UnspecifiedParameterError("httpserver-classname"));
        }

        this.logger.log(Level.INFO, "Starting web server " + className + ".");

        // Start the http server.
        startHttpServer(className, this.settings.getHttpserverConfig());

        // Figure out what the full server address is.
        InetAddress localhost;
        try
        {
            localhost = InetAddress.getLocalHost();
        }
        catch (UnknownHostException e)
        {
            throw new DeveloperError("Unbelievable -- localhost is an 'unknown host'!", e);
        }

        String serverAddress = "http://" + localhost.getHostName() + ":" + this.server.getHttpPort();

        this.logger.log(Level.INFO, "Web server is listening at " + serverAddress);
    }

    /**
     * Tries to instantiate an http server of unpredetermined type. We wish to
     * be compatible with any server, not just the choix du jour.
     * @param className the classname of the http server to instantiate
     * @param configParameters the parameters need to configure the http server
     */
    private void startHttpServer(String className, Object... configParameters)
    {
        // First, see if the http server class can be found.
        Class< ? extends ProgramDCompatibleHttpServer> serverClass;
        try
        {
            serverClass = (Class< ? extends ProgramDCompatibleHttpServer>) Class.forName(className);
        }
        catch (ClassNotFoundException e)
        {
            throw new UserError("Could not find http server \"" + className + "\".", e);
        }
        catch (ClassCastException e)
        {
            throw new UserError("\"" + className + "\" is not a subclass of ProgramDCompatibleHttpServer.", e);
        }

        // Get the constructor that takes a Core as an argument.
        Constructor<? extends ProgramDCompatibleHttpServer> constructor;
        try
        {
            constructor = serverClass.getDeclaredConstructor(Core.class);
        }
        catch (NoSuchMethodException e)
        {
            throw new DeveloperError("Requested constructor not found for web server class.", e);
        }

        /*
         * Any http server must implement ProgramDCompatibleHttpServer. The
         * interface itself is very trivial, and is just a way for us to isolate
         * dependencies on particular http servers (non-GPL) to a single wrapper
         * class.
         */
        try
        {
            this.server = constructor.newInstance(this.core);
        }
        catch (InstantiationException e)
        {
            throw new UserError("Couldn't instantiate http server \"" + className + "\".", e);
        }
        catch (IllegalAccessException e)
        {
            throw new DeveloperError("The constructor for \"" + className + "\" or the class itself is not available.", e);
        }
        catch (ClassCastException e)
        {
            throw new DeveloperError("\"" + className + "\" is not an implementation ofProgramDCompatibleHttpServer.", e);
        }
        catch (InvocationTargetException e)
        {
            throw new DeveloperError("The web server constructor threw an exception.", e);
        }

        /*
         * If the server config parameter was defined, and if the http server is
         * an implementation of ProgramDCompatibleHttpServer, configure it.
         */
        if (configParameters != null)
        {
            this.server.configure(configParameters);
        }

        // Start the server as one of the BotProcesses.
        BotProcesses.start(this.server, "http server");
    }

    private static void usage()
    {
        System.out.println("Usage: web-server -c <CORE_CONFIG> -w <WEBSERVER_CONFIG> [-n <CONSOLE_CONFIG>]");
        System.out.println("Start up a web server version of Program D using the specified config files.");
        System.out.println();
        System.out.println("  -c, --core-properties        the path to the core configuration (XML properties) file");
        System.out.println("  -w, --web-server-properties  the path to the web server configuration (XML properties) file");
        System.out.println("  -n, --console-properties     the path to the console configuration (XML properties) file");
        System.out.println();
        System.out.println("Report bugs to <programd@aitools.org>");
    }

    /**
     * Starts up the WebServer configuration.  Required arguments are:
     * <ul>
     * <li><code>-c, --core-properties     the path to the core configuration (XML properties) file</code></li>
     * <li><code>-w, --web-server-properties  the path to the web server configuration (XML properties) file</code></li>
     * <li><code>-n, --console-properties  the path to the console configuration (XML properties) file</code></li>
     * </ul>
     * @param argv
     */
    public static void main(String[] argv)
    {
        String corePropertiesPath = null;
        String webServerPropertiesPath = null;
        String consolePropertiesPath = null;

        int opt;
        LongOpt[] longopts = new LongOpt[3];
        longopts[0] = new LongOpt("core-properties", LongOpt.REQUIRED_ARGUMENT, null, 'c');
        longopts[1] = new LongOpt("web-server-properties", LongOpt.REQUIRED_ARGUMENT, null, 'w');
        longopts[2] = new LongOpt("console-properties", LongOpt.REQUIRED_ARGUMENT, null, 'n');

        Getopt getopt = new Getopt("web-server", argv, ":c:n:w:", longopts);

        while ((opt = getopt.getopt()) != -1)
        {
            switch (opt)
            {
                case 'c':
                    corePropertiesPath = getopt.getOptarg();
                    break;

                case 'n':
                    consolePropertiesPath = getopt.getOptarg();
                    break;

                case 'w':
                    webServerPropertiesPath = getopt.getOptarg();
                    break;
            }
        }

        if (corePropertiesPath == null)
        {
            System.err.println("You must specify a core properties path.");
            usage();
            System.exit(1);
        }

        if (webServerPropertiesPath == null)
        {
            System.err.println("You must specify a web server properties path.");
            usage();
            System.exit(1);
        }

        if (consolePropertiesPath == null)
        {
            new WebServer(corePropertiesPath, webServerPropertiesPath);
        }
        else
        {
            new WebServer(corePropertiesPath, webServerPropertiesPath, consolePropertiesPath);
        }
    }
}