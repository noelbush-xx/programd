// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: HttpResponse.java,v 1.1.1.1 2001/06/17 19:00:51 noelbu Exp $
// ========================================================================

package org.alicebot.server.net.http;

import org.alicebot.server.net.http.util.Code;
import org.alicebot.server.net.http.util.IO;
import org.alicebot.server.net.http.util.UrlEncoded;
import org.alicebot.server.net.http.util.StringUtil;
import org.alicebot.server.net.http.util.Resource;
import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import javax.servlet.http.Cookie;


/* ------------------------------------------------------------ */
/** HTTP Request.
 * This class manages the headers, trailers and content streams
 * of a HTTP response. It can be used for receiving or generating
 * requests.
 *
 * This class is not synchronized. It should be explicitly
 * synchronized if it is used by multiple threads.
 *
 * @see HttpRequest
 * @version $Id: HttpResponse.java,v 1.1.1.1 2001/06/17 19:00:51 noelbu Exp $
 * @author Greg Wilkins (gregw)
 */
public class HttpResponse extends HttpMessage 
{ 
      public final static int
          __100_Continue = 100,
          __101_Switching_Protocols = 101,
          __200_OK = 200,
          __201_Created = 201,
          __202_Accepted = 202,
          __203_Non_Authoritative_Information = 203,
          __204_No_Content = 204,
          __205_Reset_Content = 205,
          __206_Partial_Content = 206,
          __300_Multiple_Choices = 300,
          __301_Moved_Permanently = 301,
          __302_Moved_Temporarily = 302,
          __303_See_Other = 303,
          __304_Not_Modified = 304,
          __305_Use_Proxy = 305,
          __400_Bad_Request = 400,
          __401_Unauthorized = 401, 
          __402_Payment_Required = 402,
          __403_Forbidden = 403,
          __404_Not_Found = 404,
          __405_Method_Not_Allowed = 405,
          __406_Not_Acceptable = 406,
          __407_Proxy_Authentication_Required = 407,
          __408_Request_Timeout = 408,
          __409_Conflict = 409,
          __410_Gone = 410,
          __411_Length_Required = 411,
          __412_Precondition_Failed = 412,
          __413_Request_Entity_Too_Large = 413,
          __414_Request_URI_Too_Large = 414,
          __415_Unsupported_Media_Type = 415,
          __416_Requested_Range_Not_Satisfiable = 416,
          __417_Expectation_Failed = 417,
          __500_Internal_Server_Error = 500,
          __501_Not_Implemented = 501,
          __502_Bad_Gateway = 502,
          __503_Service_Unavailable = 503,
          __504_Gateway_Timeout = 504,
          __505_HTTP_Version_Not_Supported = 505;

          
    /* -------------------------------------------------------------- */
    public final static HashMap __statusMsg = new HashMap();
    static
    {
        // Build error code map using reflection
        try
        {
            Field[] fields = org.alicebot.server.net.http.HttpResponse.class
                .getDeclaredFields();
            for (int f=fields.length; f-->0 ;)
            {
                int m = fields[f].getModifiers();
                String name=fields[f].getName();
                if (Modifier.isFinal(m) &&
                    Modifier.isStatic(m) &&
                    fields[f].getType().equals(Integer.TYPE) &&
                    name.startsWith("__") &&
                    Character.isDigit(name.charAt(2)))
                {
                    String message = name.substring(6);
                    message = message.replace('_',' ');
                    __statusMsg.put(fields[f].get(null),message);
                }
            }
        }
        catch (Exception e)
        {
            Code.warning(e);
        }
    }
    
    /* ------------------------------------------------------------ */
    public static final byte[] __Continue=
        "HTTP/1.1 100 Continue\015\012\015\012".getBytes();
    
    /* -------------------------------------------------------------- */
    private int _status= __200_OK;
    private String _reason;
    private HandlerContext _handlerContext;
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     */
    public HttpResponse()
    {
        _version=__HTTP_1_0;
        _state=__MSG_EDITABLE;
    }
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param connection 
     */
    public HttpResponse(HttpConnection connection)
    {
        super(connection);
        _version=__HTTP_1_0;
        _state=__MSG_EDITABLE;
    }

    /* ------------------------------------------------------------ */
    /** Get the HandlerContext handling this reponse. 
     * @return 
     */
    public HandlerContext getHandlerContext()
    {
        return _handlerContext;
    }
    
    /* ------------------------------------------------------------ */
    /** Set the HandlerContext handling this reponse. 
     * @return 
     */
    void setHandlerContext(HandlerContext context)
    {
        _handlerContext=context;
    }
    
    /* ------------------------------------------------------------ */
    /** 
     * @return true if the message has been modified. 
     */
    public boolean isDirty()
    {
        return _status!=__200_OK || super.isDirty();
    }

    /* ------------------------------------------------------------ */
    /** Reset the response.
     * Clears any data that exists in the buffer as well as the status code and
     * headers.  If the response has been committed, this method throws an 
     * <code>IllegalStateException</code>.
     *
     * @exception IllegalStateException  if the response has already been
     *                                   committed
     */
    public void reset()
    {
        if (isCommitted())
            throw new IllegalStateException("Already committed");

        try
        {
            getOutputStream().resetBuffer();
            _status= __200_OK;
            _reason=null;
            super.reset();
        }
        catch(Exception e)
        {
            Code.warning(e);
            throw new IllegalStateException(e.toString());
        }
    }
    
    
    /* ------------------------------------------------------------ */
    /** Get the HTTP Request.
     * Get the HTTP Request associated with this response.
     * @return associated request
     */
    public HttpRequest getRequest()
    {
        if (_connection==null)
            return null;
        return _connection.getRequest();
    }
    
    /* ------------------------------------------------------------ */
    /** Not Implemented.
     * @param in 
     * @exception IOException 
     */
    public void readHeader(ChunkableInputStream in)
        throws IOException
    {
        _state=__MSG_BAD;
        Code.notImplemented();
    }
    
    
    /* -------------------------------------------------------------- */
    public void writeHeader(Writer writer) 
        throws IOException
    {
        if (_state!=__MSG_EDITABLE)
            throw new IllegalStateException(__state[_state]+
                                            " is not EDITABLE");
        if (_header==null)
            throw new IllegalStateException("Response is destroyed");

        if (_dotVersion>=0)
        {
            String status=_status+" ";
            if (Code.verbose())
                Code.debug("writeHeaders: ",status);
            _state=__MSG_BAD;
            synchronized(writer)
            {
                writer.write(_version);
                writer.write(' ');
                writer.write(status);
                writer.write(getReason());
                writer.write(HttpFields.__CRLF);
                _header.write(writer);
            }
        }
        _state=__MSG_SENDING;
    }
    
    /* -------------------------------------------------------------- */
    public int getStatus()
    {
        return _status;
    }
    
    /* -------------------------------------------------------------- */
    public void setStatus(int status)
    {
        _status=status;
    }
    
    /* -------------------------------------------------------------- */
    public String getReason()
    {
        if (_reason!=null)
            return _reason;
        _reason=(String)__statusMsg.get(new Integer(_status));
        if (_reason==null)
            _reason="unknown";
        return _reason;
    }
    
    /* -------------------------------------------------------------- */
    public void setReason(String reason)
    {
        _reason=reason;
    }
      
    /* ------------------------------------------------------------ */
    /* Which fields to set?
     * Specialized HttpMessage.setFields to consult request TE field
     * for a "trailer" token if state is SENDING.
     * @return Header or Trailer fields
     * @exception IllegalStateException Not editable or sending 1.1
     *                                  with trailers
     */
    protected HttpFields setFields()
        throws IllegalStateException
    {
        if (!_acceptTrailer &&
            _state==__MSG_SENDING &&
            _version.equals(__HTTP_1_1))
        {
            HttpRequest request=_connection.getRequest();
            if (request!=null)
                request.getAcceptableTransferCodings();
        }

        return super.setFields();
    }
    
    /* ------------------------------------------------------------- */
    /** Send Error Response.
     */
    private void sendError(int code,String error,String message) 
        throws IOException
    {        
        setStatus(code);
        String reason = (String)__statusMsg.get(new Integer(code));
        setReason(reason);

        if (code!=204 && code!=304 && code!=206 && code>=200)
        {
            _header.put(HttpFields.__ContentType,HttpFields.__TextHtml);
            _mimeType=HttpFields.__TextHtml;
            _characterEncoding=null;
            ChunkableOutputStream out=getOutputStream();
            
            Resource errorPage=null;
            if (_handlerContext!=null && error!=null)
                errorPage=_handlerContext.getErrorPageResource(error);

            if (errorPage!=null)
            {
                _header.putIntField(HttpFields.__ContentLength,
                                    (int)errorPage.length());
                IO.copy(errorPage.getInputStream(),out);
            }
            else
            {
                String body=
                    "<HTML>\n<HEAD>\n<TITLE>Error "+code+
                    " "+reason+
                    "</TITLE>\n<BODY>\n<H2>HTTP ERROR: "+code+
                    " "+reason+
                    "</H2>\n"+(message==null?"":message);
                for (int i=0;i<10;i++)
                    body+="<!-- Padding for IE                                                 -->";
                body+="\n</BODY>\n</HTML>\n";
                
                byte[] buf=body.getBytes(StringUtil.__ISO_8859_1);
                _header.putIntField(HttpFields.__ContentLength,buf.length);
                out.write(buf);
            }
        }
        else if (code!=206) 
        {
            _header.remove(HttpFields.__ContentType);
            _header.remove(HttpFields.__ContentLength);
            _characterEncoding=null;
            _mimeType=null;
        }
        commitHeader();
    }
    
    /* ------------------------------------------------------------- */
    /**
     * Sends an error response to the client using the specified status
     * code and no default message.
     * @param code the status code
     * @param message the detail message
     * @exception IOException If an I/O error has occurred.
     */public void sendError(int code,String message) 
        throws IOException
    {
        sendError(code,""+code,message);
    }
    
    /* ------------------------------------------------------------- */
    /**
     * Sends an error response to the client using the specified status
     * code and no default message.
     * @param code the status code
     * @exception IOException If an I/O error has occurred.
     */
    public void sendError(int code) 
        throws IOException
    {
        sendError(code,""+code,null);
    }
    
    /* ------------------------------------------------------------- */
    /** Send Error Response.
     * Sends an error response to the client using the specified status
     * code and detail message.
     * @param exception 
     * @exception IOException If an I/O error has occurred.
     */
    public void sendError(int code,Throwable exception) 
        throws IOException
    {
        if (exception instanceof HttpException)
        {
            HttpException he = (HttpException)exception;
            code=he.getCode();
            sendError(code,""+code,he.getMessage());
        }
        else
            sendError(code,exception.getClass().getName(),exception.toString());
    }
    
    /* ------------------------------------------------------------- */
    /**
     * Sends a redirect response to the client using the specified redirect
     * location URL.
     * @param location the redirect location URL
     * @exception IOException If an I/O error has occurred.
     */
    public void sendRedirect(String location)
        throws IOException
    {
        _header.put(HttpFields.__Location,location);
        setStatus(__302_Moved_Temporarily);
        commitHeader();
    }

    /* -------------------------------------------------------------- */
    /** Add a Set-Cookie field.
     */
    public void addSetCookie(String name,
                             String value)
    {
        addSetCookie(new Cookie(name,value),false);
    }
    
    /* -------------------------------------------------------------- */
    /** Add a Set-Cookie field.
     */
    public void addSetCookie(Cookie cookie)
    {
        addSetCookie(cookie,false);
    }
    
    /* -------------------------------------------------------------- */
    /** Add a Set-Cookie field.
     * @param cookie The cookie.
     * @param cookie2 If true, use the alternate cookie 2 header
     */
    public void addSetCookie(Cookie cookie, boolean cookie2)
    {
        String name=cookie.getName();
        String value=cookie.getValue();
        int version=cookie.getVersion();
        
        // Check arguments
        if (name==null || name.length()==0)
            throw new IllegalArgumentException("Bad cookie name");
        
        // Format value and params
        StringBuffer buf = new StringBuffer(128);
        String name_value_params=null;
        synchronized(buf)
        {
            buf.append(name);
            if (value!=null && value.length()>0)
            {
                buf.append('=');
                buf.append(UrlEncoded.encodeString(value));
            }
            
            if (version>0)
            {
                buf.append(";Version=");
                buf.append(version);
                String comment=cookie.getComment();
                if (comment!=null && comment.length()>0)
                {
                    buf.append(";Comment=\"");
                    buf.append(comment);
                    buf.append('"');
                }
            }
            String path=cookie.getPath();
            if (path!=null && path.length()>0)
            {
                buf.append(";Path=");
                buf.append(path);
            }
            String domain=cookie.getDomain();
            if (domain!=null && domain.length()>0)
            {
                buf.append(";Domain=");
                buf.append(domain.toLowerCase());// lowercase for IE
            }
            long maxAge = cookie.getMaxAge();
            if (maxAge>=0)
            {
                if (version==0)
                {
                    buf.append(";Expires=");
                    buf.append(HttpFields.__dateSend.format(new Date(System.currentTimeMillis()+1000L*maxAge)));
                }
                else
                {
                    buf.append (";Max-Age=");
                    buf.append (cookie.getMaxAge());
                }
            }
            else if (version>0)
            {
                buf.append (";Discard");
            }
            if (cookie.getSecure())
            {
                buf.append(";secure");
            }
            name_value_params=buf.toString();
        }
        
        _header.add(cookie2?HttpFields.__SetCookie2:HttpFields.__SetCookie,
                    name_value_params);        
    }
    
    /* ------------------------------------------------------------ */
    /** Recycle the response.
     */
    public void recycle(HttpConnection connection)
    {
        super.recycle(connection);
        _status=__200_OK;
        _version=__HTTP_1_0;
        _state=__MSG_EDITABLE;
        _reason=null;
    }
    
    /* ------------------------------------------------------------ */
    /** Destroy the response.
     * Help the garbage collector by null everything that we can.
     */
    public void destroy()
    {
        _reason=null;
        super.destroy();
    }

    /* ------------------------------------------------------------ */
    public synchronized void commitHeader()
        throws IOException
    {
        _connection.commitResponse();
        super.commitHeader();
    }
    
    /* ------------------------------------------------------------ */
    /** 
     * @exception IOException 
     */
    public void commit()
        throws IOException
    {
        if (isCommitted())
            return;
        
        super.commit();
        HttpRequest request=getRequest();
        if (request!=null)
            request.setHandled(true);
    }
}



