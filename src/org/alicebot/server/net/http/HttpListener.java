// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: HttpListener.java,v 1.1.1.1 2001/06/17 19:00:43 noelbu Exp $
// ========================================================================

package org.alicebot.server.net.http;
import org.alicebot.server.net.http.util.LifeCycle;
import java.net.UnknownHostException;


/* ------------------------------------------------------------ */
/** HTTP Listener.
 *
 * @see HttpConnection
 * @version $Id: HttpListener.java,v 1.1.1.1 2001/06/17 19:00:43 noelbu Exp $
 * @author Greg Wilkins (gregw)
 */
public interface HttpListener extends LifeCycle
{
    public abstract void setHttpServer(HttpServer server);
    public abstract HttpServer getHttpServer();
    public abstract void setHost(String host)
        throws UnknownHostException;
    public abstract String getHost();
    public abstract void setPort(int port);
    public abstract int getPort();

    public abstract void customizeRequest(HttpConnection connection,
                                          HttpRequest request);
    public abstract String getDefaultScheme();
}











