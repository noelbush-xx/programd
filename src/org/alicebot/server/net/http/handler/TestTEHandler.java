// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: TestTEHandler.java,v 1.1.1.1 2001/06/17 19:02:27 noelbu Exp $
// ========================================================================

package org.alicebot.server.net.http.handler;

import org.alicebot.server.net.http.HttpException;
import org.alicebot.server.net.http.HttpFields;
import org.alicebot.server.net.http.HttpRequest;
import org.alicebot.server.net.http.HttpResponse;
import java.io.IOException;

/* ------------------------------------------------------------ */
/** Handler to test TE transfer encoding.
 * If 'gzip' or 'deflate' is in the query string, the
 * response is given that transfer encoding
 *
 * @version $Id: TestTEHandler.java,v 1.1.1.1 2001/06/17 19:02:27 noelbu Exp $
 * @author Greg Wilkins (gregw)
 */
public class TestTEHandler extends NullHandler
{
    /* ------------------------------------------------------------ */
    public void handle(String pathInContext,
                       HttpRequest request,
                       HttpResponse response)
        throws HttpException, IOException
    {
        // For testing set transfer encodings
        if (request.getQuery()!=null)
        {
            if (request.getQuery().indexOf("gzip")>=0)
            {
                response.setField(HttpFields.__TransferEncoding,"gzip");
                response.addField(HttpFields.__TransferEncoding,"chunked");
            }
            if (request.getQuery().indexOf("deflate")>=0)
            {
                response.setField(HttpFields.__TransferEncoding,"deflate");
                response.addField(HttpFields.__TransferEncoding,"chunked");
            }
        }
    }
}
