package org.aitools.programd.server.jettyinterface;

import java.io.IOException;

import org.aitools.programd.Core;
import org.aitools.programd.server.ProgramDCompatibleHttpServer;
import org.aitools.programd.server.jettyinterface.ProgramDAwareJettyServer;
import org.aitools.programd.util.DeveloperError;
import org.mortbay.http.HttpListener;
import org.mortbay.http.SocketListener;
import org.mortbay.log.LogImpl;
import org.mortbay.log.OutputStreamLogSink;

/**
 *  <p>
 *  Implements a &quot;wrapper&quot; for Jetty
 *  so it implements the {@link org.aitools.programd.server.ProgramDCompatibleHttpServer ProgramDCompatibleHttpServer} interface.
 *  </p>
 *
 *  @author Noel Bush
 */
public class JettyWrapper implements ProgramDCompatibleHttpServer
{

    /** A private reference to the Jetty server. */
    private ProgramDAwareJettyServer jetty;
    
    private Core core;

    /**
     * Creates a new JettyWrapper using the given Core.
     * @param coreToUse the Core to use
     */
    public JettyWrapper(Core coreToUse)
    {
        this.core = coreToUse;
    } 

    /**
     *  Configures the server.
     *
     *  @param configParameters   one String: the config file path to pass to Jetty
     */
    public void configure(Object ... configParameters)
    {
        if (configParameters.length != 1)
        {
            throw new DeveloperError("JettyWrapper requires exactly one config parameter!", new IllegalArgumentException());
        }
        String configFilePath = configParameters[0].toString();

        this.jetty = new ProgramDAwareJettyServer(this.core);
        
        // Add a LogSink to Jetty so it will shut up and not send messages to System.err.
        OutputStreamLogSink quietJetty = new OutputStreamLogSink("./logs/jetty.log");
        try
        {
            (new LogImpl()).add(quietJetty);
        }
        catch (Exception e)
        {
            throw new DeveloperError(e);
        }

        try
        {
            this.jetty.configure(configFilePath);
        }
        catch (IOException e)
        {
            throw new DeveloperError(e);
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
            throw new DeveloperError(e);
        } 
    } 

    /**
     * @see org.aitools.programd.bot.BotProcess#shutdown()
     */
    public void shutdown()
    {
        try
        {
            this.jetty.stop(true);
        }
        catch (InterruptedException e)
        {
            throw new DeveloperError(e);
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