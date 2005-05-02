/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.interfaces;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.aitools.programd.Core;
import org.aitools.programd.server.ProgramDCompatibleHttpServer;
import org.aitools.programd.server.ServletRequestResponderManagerRegistry;
import org.aitools.programd.util.ClassUtils;
import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.ManagedProcess;
import org.aitools.programd.util.UnspecifiedParameterError;
import org.aitools.programd.util.UserError;

/**
 * An HTTP server interface to Program D. This class manages the creation and
 * use of an actual HTTP server (it is not itself the server).
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class HTTPServer implements ManagedProcess
{
    /** The Core to which this is attached. */
    private Core core;

    /** The HTTPServer settings. */
    private HTTPServerSettings settings;

    /** The registry of Responders that can handle ServletRequests. */
    private ServletRequestResponderManagerRegistry servletRequestResponderRegistry;

    /** Whatever HTTP server is going to be used. */
    private ProgramDCompatibleHttpServer server;

    /** The logger for web server activity. */
    private Logger logger;

    /**
     * Creates a new HTTPServer.
     * 
     * @param coreToUse the core to use
     * @param webServerPropertiesPath the path to the config file
     */
    public HTTPServer(Core coreToUse, String webServerPropertiesPath)
    {
        this.core = coreToUse;
        this.servletRequestResponderRegistry = new ServletRequestResponderManagerRegistry(this.core);
        this.settings = new HTTPServerSettings(webServerPropertiesPath);
        this.logger = this.core.setupLogger("programd.web-server", this.settings.getLogPathPattern());
    }

    /**
     * Runs the HTTPServer interface.
     */
    public void run()
    {
        String classname = this.settings.getClassname();

        // Fail if http server class name is not specified.
        if (classname == null)
        {
            throw new UserError(new UnspecifiedParameterError("httpserver-classname"));
        }

        this.logger.log(Level.INFO, "Starting web server " + classname + ".");

        // Start the http server.
        startHttpServer(classname, this.settings.getConfig());

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
     * Tries to instantiate an http server of unpredetermined type (although it
     * must extend the ProgramDCompatibleHttpServer class).
     * 
     * @param classname the classname of the http server to instantiate
     * @param configParameters the parameters need to configure the http server
     */
    private void startHttpServer(String classname, Object... configParameters)
    {
        /*
         * Any http server must implement ProgramDCompatibleHttpServer. The
         * interface itself is very trivial, and is just a way for us to isolate
         * dependencies on particular http servers (non-GPL) to a single wrapper
         * class.
         */
        this.server = ClassUtils.getSubclassInstance(ProgramDCompatibleHttpServer.class, classname, "http server", this.core, this.servletRequestResponderRegistry, this.settings);

        /*
         * If the server config parameter was defined, and if the http server is
         * an implementation of ProgramDCompatibleHttpServer, configure it.
         */
        if (configParameters != null)
        {
            this.server.configure(configParameters);
        }

        // Start the server.
        this.server.run();
    }

    /**
     * @see ManagedProcess#shutdown
     */
    public void shutdown()
    {
        this.server.shutdown();
    }
}
