package org.aitools.programd.server.jettyinterface;

import java.io.IOException;

import org.aitools.programd.Core;
import org.aitools.programd.interfaces.HTTPServerSettings;
import org.aitools.programd.server.ProgramDCompatibleHttpServer;
import org.aitools.programd.server.ServletRequestResponderManagerRegistry;
import org.aitools.programd.server.jettyinterface.ProgramDAwareJettyServer;
import org.aitools.programd.util.DeveloperError;
import org.mortbay.http.HttpListener;
import org.mortbay.http.SocketListener;
import org.mortbay.log.LogImpl;
import org.mortbay.log.OutputStreamLogSink;

/**
 * <p>
 * Implements a &quot;wrapper&quot; for Jetty so it implements the
 * {@link org.aitools.programd.server.ProgramDCompatibleHttpServer ProgramDCompatibleHttpServer}
 * interface.
 * </p>
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class JettyWrapper implements ProgramDCompatibleHttpServer
{

    /** A private reference to the Jetty server. */
    private ProgramDAwareJettyServer jetty;

    private Core core;

    private ServletRequestResponderManagerRegistry responderRegistry;

    private HTTPServerSettings serverSettings;

    /**
     * Creates a new JettyWrapper using the given Core.
     * 
     * @param coreToUse the Core to use
     * @param registry the responder registry to use
     * @param serverSettingsToUse the web server settings
     */
    public JettyWrapper(Core coreToUse, ServletRequestResponderManagerRegistry registry, HTTPServerSettings serverSettingsToUse)
    {
        this.core = coreToUse;
        this.responderRegistry = registry;
        this.serverSettings = serverSettingsToUse;
    }

    /**
     * Configures the server.
     * 
     * @param configParameters one String: the config file path to pass to Jetty
     */
    public void configure(Object... configParameters)
    {
        if (configParameters.length != 1)
        {
            throw new DeveloperError("JettyWrapper requires exactly one config parameter!", new IllegalArgumentException());
        }
        String configFilePath = configParameters[0].toString();

        this.jetty = new ProgramDAwareJettyServer(this.core, this.responderRegistry);

        // Add a LogSink to Jetty so it will send its errors to the web server
        // log.
        OutputStreamLogSink logsink = new OutputStreamLogSink(this.serverSettings.getLogPathPattern());
        try
        {
            (new LogImpl()).add(logsink);
        }
        catch (Exception e)
        {
            throw new DeveloperError("Error occurred while setting up logger for jetty.", e);
        }

        try
        {
            this.jetty.configure(configFilePath);
        }
        catch (IOException e)
        {
            throw new DeveloperError("Error occurred while configuring jetty.  Check that the config file is valid.", e);
        }

        this.jetty.setStatsOn(true);
    }

    /**
     * @see org.aitools.programd.server.ProgramDCompatibleHttpServer#getHttpPort()
     */
    public int getHttpPort()
    {
        // Isn't there an easier way to do this?
        int port = 0;
        for (HttpListener listener : this.jetty.getListeners())
        {
            if (listener instanceof SocketListener)
            {
                port = ((SocketListener) listener).getPort();
            }
        }
        return port;
    }

    /**
     * @see org.aitools.programd.server.ProgramDCompatibleHttpServer#run()
     */
    public void run()
    {
        try
        {
            this.jetty.start();
        }
        catch (Exception e)
        {
            throw new DeveloperError("Exception occurred while starting jetty.", e);
        }
    }

    /**
     * @see ProgramDCompatibleHttpServer#shutdown()
     */
    public void shutdown()
    {
        try
        {
            this.jetty.stop(true);
        }
        catch (InterruptedException e)
        {
            throw new DeveloperError("Exception occurred while stopping jetty.", e);
        }
    }

    /**
     * @return the Jetty server itself
     */
    public ProgramDAwareJettyServer getServer()
    {
        return this.jetty;
    }
}