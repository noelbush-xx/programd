package org.aitools.programd.server;

import java.io.IOException;
import java.util.Iterator;

import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.Globals;
import org.mortbay.http.SocketListener;
import org.mortbay.jetty.Server;
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
public class JettyWrapper implements ProgramDCompatibleHttpServer
{

    /** A private reference to the Jetty server. */
    private static Server jetty;

    public JettyWrapper()
    {
        // Do nothing.
    }

    /**
     *  Configures the server and sets the address and port number in
     *  {@link org.aitools.programd.util.Globals Globals}
     *  (for reference).
     *
     *  @param configFilePath   the config file path to pass to Jetty
     */
    public void configure(String configFilePath) throws IOException
    {
        jetty = new Server();

        // Add a LogSink to Jetty so it will shut up and not send messages to System.err.
        OutputStreamLogSink quietJetty = new OutputStreamLogSink("./logs/jetty.log");
        org.mortbay.util.Log.instance().add(quietJetty);

        jetty.configure(Globals.getRootPath() + configFilePath);

        jetty.setStatsOn(true);

        // Get the port number and set it in Globals for future reference.
        int port = 0;
        Iterator listeners = jetty.getListeners().iterator();
        while (listeners.hasNext())
        {
            Object listener = listeners.next();
            if (listener.getClass().getName().equals("org.mortbay.http.SocketListener"))
            {
                port = ((SocketListener) listener).getPort();
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
        // This is crude, but jetty.stop() isn't reliable.
        jetty = null;
    }

    public Server getServer()
    {
        return jetty;
    }
}