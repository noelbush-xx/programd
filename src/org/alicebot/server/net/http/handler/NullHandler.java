// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: NullHandler.java,v 1.1.1.1 2001/06/17 19:02:26 noelbu Exp $
// ========================================================================

package org.alicebot.server.net.http.handler;

import org.alicebot.server.net.http.HandlerContext;
import org.alicebot.server.net.http.HttpException;
import org.alicebot.server.net.http.HttpHandler;
import org.alicebot.server.net.http.HttpRequest;
import org.alicebot.server.net.http.HttpResponse;
import org.alicebot.server.net.http.util.Code;
import org.alicebot.server.net.http.util.Log;
import java.io.IOException;

/* ------------------------------------------------------------ */
/** Base HTTP Handler.
 * This No-op handler is a good base for other handlers
 *
 * @version $Id: NullHandler.java,v 1.1.1.1 2001/06/17 19:02:26 noelbu Exp $
 * @author Greg Wilkins (gregw)
 */
abstract public class NullHandler implements HttpHandler
{
    /* ----------------------------------------------------------------- */
    private boolean _started=false;
    private boolean _destroyed=true;
    private HandlerContext _context;
    private String _name;

    /* ------------------------------------------------------------ */
    public void setName(String name)
    {
        _name=name;
    }
    
    /* ------------------------------------------------------------ */
    public String getName()
    {
        if (_name==null)
        {
            _name=this.getClass().getName();
            if (!Code.debug())
                _name=_name.substring(_name.lastIndexOf(".")+1);
        }
        return _name;
    }
    
    
    /* ------------------------------------------------------------ */
    public HandlerContext getHandlerContext()
    {
        return _context;
    }
    
    /* ------------------------------------------------------------ */
    /** Initialize with a HandlerContext.
     * @param configuration Must be the HandlerContext of the handler
     */
    public void initialize(HandlerContext context)
    {
        if (_context==null)
            _context=context;
        else if (_context!=context)
            throw new IllegalStateException("Can't initialize handler for different context");
    }
    
    /* ----------------------------------------------------------------- */
    public void start()
    {
        if (_context==null)
            throw new IllegalStateException("No context for "+this);        
        if (!_context.isStarted())
            Code.warning("Handler Context not started for "+this);
        
        _started=true;
        _destroyed=false;
        Log.event("Started "+this);
    }
    
    /* ----------------------------------------------------------------- */
    public void stop()
    {
        _started=false;
        Log.event("Stopped "+this);
    }
    
    /* ----------------------------------------------------------------- */
    public void destroy()
    {
        _started=false;
        _destroyed=true;
        _context=null;
        Log.event("Destroyed "+this);
    }

    /* ----------------------------------------------------------------- */
    public boolean isStarted()
    {
        return _started;
    }
    
    /* ----------------------------------------------------------------- */
    public boolean isDestroyed()
    {
        return _destroyed;
    }    
    
    /* ------------------------------------------------------------ */
    public String toString()
    {
        return getName()+" in "+_context;
    }    

}




