// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: ForwardHandler.java,v 1.1.1.1 2001/06/17 19:02:25 noelbu Exp $
// ========================================================================

package org.alicebot.server.net.http.handler;

import org.alicebot.server.net.http.HandlerContext;
import org.alicebot.server.net.http.HttpException;
import org.alicebot.server.net.http.HttpHandler;
import org.alicebot.server.net.http.HttpMessage;
import org.alicebot.server.net.http.HttpRequest;
import org.alicebot.server.net.http.HttpResponse;
import org.alicebot.server.net.http.PathMap;
import org.alicebot.server.net.http.util.Code;
import org.alicebot.server.net.http.util.Log;
import java.io.IOException;
import java.util.Map;


/* ------------------------------------------------------------ */
/** Forward Request Handler.
 *
 * @version $Revision: 1.1.1.1 $
 * @author Greg Wilkins (gregw)
 */
public class ForwardHandler extends NullHandler
{
    PathMap _forward = new PathMap();
    String _root;

    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     */
    public ForwardHandler()
    {}
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param rootForward 
     */
    public ForwardHandler(String rootForward)
    {
        _root=rootForward;
    }
    
    /* ------------------------------------------------------------ */
    /** Add a forward mapping.
     * @param pathSpecInContext The path to forward from 
     * @param newPath The path to forward to.
     */
    public void addForward(String pathSpecInContext,
                           String newPath)
    {
        _forward.put(pathSpecInContext,newPath);
    }
    
    /* ------------------------------------------------------------ */
    /** Add a forward mapping for root path.
     * @param newPath The path to forward to.
     */
    public void setRootForward(String newPath)
    {
        _root=newPath;
    }
    
    /* ------------------------------------------------------------ */
    /** 
     * @param pathInContext 
     * @param request 
     * @param response 
     * @exception HttpException 
     * @exception IOException 
     */
    public void handle(String pathInContext,
                       HttpRequest request,
                       HttpResponse response)
        throws HttpException, IOException
    {
        String newPath=null;
        if (_root!=null && "/".equals(pathInContext))
            newPath=_root;
        else
        {
            Map.Entry entry = _forward.getMatch(pathInContext);
            if (entry!=null)
            {
                String info=PathMap.pathInfo((String)entry.getKey(),pathInContext);
                newPath=(String)entry.getValue()+(info==null?"":info);
            }
        }
        
        if (newPath!=null)
        {
            Code.debug("Forward from ",pathInContext," to ",newPath);
            
            int last=request.setState(HttpMessage.__MSG_EDITABLE);
            String context=getHandlerContext().getContextPath();
            if (context.length()==1)
                request.setPath(newPath);
            else
                request.setPath(getHandlerContext().getContextPath()+newPath);
            request.setState(last);
            getHandlerContext().getHttpServer().service(request,response);
            return;
        }
    }
}
