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
 *  so it can be created with a parameterless constructor
 *  and then initialized with a configuration file..
 *  </p>
 *
 *  @author Noel Bush
 */
public class JettyWrapper implements ProgramDCompatibleHttpServer
{

    /** A private reference to the Jetty server. */
    private ProgramDAwareJettyServer jetty;
    
    private Core core;

    public JettyWrapper(Core coreToUse)
    {
        this.core = coreToUse;
    } 

    /**
     *  Configures the server and sets the address and port number in
     *  {@link org.aitools.programd.server.Settings Settings} 
     *  (for reference).
     *
     *  @param configFilePath   the config file path to pass to Jetty
     */
    public void configure(String configFilePath) throws IOException
    {
        this.jetty = new ProgramDAwareJettyServer(this.core);

        // Add a LogSink to Jetty so it will shut up and not send messages to System.err.
        OutputStreamLogSink quietJetty = new OutputStreamLogSink("./logs/jetty.log");
        try
        {
            (new LogImpl()).add(quietJetty);
        }
        catch (Exception e)
        {
            throw new DeveloperError(e.getMessage());
        }

        this.jetty.configure(configFilePath);

        this.jetty.setStatsOn(true);
    }
    
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

    public void run()
    {
        try
        {
            this.jetty.start();
        } 
        catch (Exception e)
        {
            throw new DeveloperError(e.getMessage());
        } 
    } 

    public void shutdown()
    {
        try
        {
            this.jetty.stop(true);
        }
        catch (InterruptedException e)
        {
            throw new DeveloperError(e.getMessage());
        }
    } 

    public ProgramDAwareJettyServer getServer()
    {
        return this.jetty;
    } 
}