// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: HttpException.java,v 1.1.1.1 2001/06/17 19:00:40 noelbu Exp $
// ========================================================================

package org.alicebot.server.net.http;
import java.io.IOException;
import java.util.HashMap;


/* ------------------------------------------------------------ */
/** Exception for known HTTP error status. 
 *
 * @version $Revision: 1.1.1.1 $
 * @author Greg Wilkins (gregw)
 */
public class HttpException extends IOException
{
    private int _code;

    public int getCode()
    {
        return _code;
    }
    
    public String getReason()
    {
        return (String)HttpResponse.__statusMsg.get(new Integer(_code));
    }
    
    public HttpException()
    {
        _code=HttpResponse.__400_Bad_Request ;
    }
    
    public HttpException(int code)
    {
        _code=code;
    }
    
    public HttpException(int code, String message)
    {
        super(message);
        _code=code;
    }

    public String toString()
    {
        String message=getMessage();
        String reason=getReason();
        return "HttpException("+_code+","+reason+","+message+")";
    }
}

