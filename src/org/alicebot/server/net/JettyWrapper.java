package org.alicebot.server.net;

import java.io.IOException;
import java.util.Iterator;

import org.alicebot.server.core.Globals;
import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.util.DeveloperError;
import org.alicebot.server.core.util.Trace;

import org.mortbay.http.HttpServer;
import org.mortbay.http.SocketListener;
import org.mortbay.jetty.Server;
import org.mortbay.util.LogSink;
import org.mortbay.util.MultiException;
import org.mortbay.util.OutputStreamLogSink;

/**
 *  <p>
 *  Implements a &quot;wrapper&quot; for Jetty 3.1.4
 *  so it can be created with a parameterless constructor
 *  and then initialized with a configuration file.
 *  </p>
 *
 *  @author Noel Bush
 */
public class JettyWrapper implements AliceCompatibleHttpServer
{

    /** A private reference to the Jetty server. */
    private static Server jetty;


    public JettyWrapper()
    {
    }


    /**
     *  Configures the server and sets the address and port number in
     *  {@link org.alicebot.server.core.Globals Globals}
     *  (for reference).
     *
     *  @param configFilePath   the config file path to pass to Jetty
     *
     *  @return the Jetty server
     */
    public void configure(String configFilePath) throws IOException
    {
        jetty = new Server();

        // Add a LogSink to Jetty so it will shut up and not send messages to System.err.
        OutputStreamLogSink quietJetty = new OutputStreamLogSink("./logs/jetty.log");
        org.mortbay.util.Log.instance().add(quietJetty);

        jetty.configure(configFilePath);

        jetty.setStatsOn(true);

        // Get the port number and set it in Globals for future reference.
        int port = 0;
        Iterator listeners = jetty.getListeners().iterator();
        while (listeners.hasNext())
        {
            Object listener = listeners.next();
            if (listener.getClass().getName().equals("org.mortbay.http.SocketListener"))
            {
                port = ((SocketListener)listener).getPort();
            }
        }
        Globals.setHttpPort(port);
    }


    public void run()
    {
        try
        {
            jetty.start();
        }
        catch (MultiException e)
        {
            throw new DeveloperError(e.getMessage());
        }
    }


    public void shutdown()
    {
        try
        {
            jetty.stop();
        }
        catch (InterruptedException e)
        {
            Log.devinfo("Jetty was interrupted while stopping.", Log.ERROR);
        }
    }


    public Server getServer()
    {
        return jetty;
    }
}
