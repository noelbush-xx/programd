// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: SocketListener.java,v 1.1.1.1 2001/06/17 19:00:58 noelbu Exp $
// ========================================================================

package org.alicebot.server.net.http;
import org.alicebot.server.net.http.util.Code;
import org.alicebot.server.net.http.util.InetAddrPort;
import org.alicebot.server.net.http.util.Log;
import org.alicebot.server.net.http.util.ThreadedServer;
import java.io.IOException;
import java.net.Socket;


/* ------------------------------------------------------------ */
/** Socket HTTP Listener.
 * The behaviour of the listener can be controlled with the
 * attributues of the ThreadedServer and ThreadPool from which it is
 * derived. Specifically: <PRE>
 * MinThreads    - Minumum threads waiting to service requests.
 * MaxThread     - Maximum thread that will service requests.
 * MaxIdleTimeMs - Time for an idle thread to wait for a request.
 * MaxReadTimeMs - Time that a read on a request can block.
 * </PRE>
 * @version $Id: SocketListener.java,v 1.1.1.1 2001/06/17 19:00:58 noelbu Exp $
 * @author Greg Wilkins (gregw)
 */
public class SocketListener 
    extends ThreadedServer
    implements HttpListener
{
    /* ------------------------------------------------------------------- */
    private HttpServer _server;
    
    /* ------------------------------------------------------------------- */
    public SocketListener()
        throws IOException
    {}
    
    /* ------------------------------------------------------------------- */
    public SocketListener(InetAddrPort address)
        throws IOException
    {
        super(address);
    }

    /* ------------------------------------------------------------ */
    public void setHttpServer(HttpServer server)
    {
        Code.assert(_server==null || _server==server,
                    "Cannot share listeners");
        _server=server;
    }
    
    /* ------------------------------------------------------------ */
    public HttpServer getHttpServer()
    {
        return _server;
    }
    
    /* --------------------------------------------------------------- */
    public String getDefaultScheme()
    {
        return "http";
    }

    /* --------------------------------------------------------------- */
    public void start()
    {
        super.start();
        Log.event("Started SocketListener on "+getInetAddrPort());
    }
    
    /* --------------------------------------------------------------- */
    public void stop()
        throws InterruptedException
    {
        super.stop();
        Log.event("Stopped SocketListener on "+getInetAddrPort());
    }
    
    /* --------------------------------------------------------------- */
    public void destroy()
    {
        Log.event("Destroy SocketListener on "+getInetAddrPort());
        super.destroy();
    }
    
    /* ------------------------------------------------------------ */
    /** Handle Job.
     * Implementation of ThreadPool.handle(), calls handleConnection.
     * @param job A Connection.
     */
    public final void handleConnection(Socket socket)
        throws IOException
    {
        HttpConnection connection =
            new SocketConnection(socket);
        connection.handle();
    }
    
    /* ------------------------------------------------------------ */
    /** 
     * @param request 
     */
    public final void customizeRequest(HttpConnection connection,
                                       HttpRequest request)
    {
        customizeRequest(((SocketConnection)connection).getSocket(),
                         request);
    }
    
    /* ------------------------------------------------------------ */
    /** 
     * @param request 
     */
    protected void customizeRequest(Socket socket,
                                    HttpRequest request)
    {
        // Do nothing
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private class SocketConnection extends HttpConnection
    {
        private Socket _socket;
        public Socket getSocket()
        {
            return _socket;
        }

        /* -------------------------------------------------------- */
        SocketConnection(Socket socket)
            throws IOException
        {
            super(SocketListener.this,
                  socket.getInetAddress(),
                  socket.getInputStream(),
                  socket.getOutputStream());
            _socket=socket;
        }
    }
}






