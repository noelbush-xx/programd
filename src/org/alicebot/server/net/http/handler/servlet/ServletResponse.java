// ========================================================================
// Copyright (c) 2000 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: ServletResponse.java,v 1.1.1.1 2001/06/17 19:02:41 noelbu Exp $
// ========================================================================

package org.alicebot.server.net.http.handler.servlet;

import org.alicebot.server.net.http.ChunkableOutputStream;
import org.alicebot.server.net.http.HttpFields;
import org.alicebot.server.net.http.HttpRequest;
import org.alicebot.server.net.http.HttpResponse;
import org.alicebot.server.net.http.util.Code;
import org.alicebot.server.net.http.util.IO;
import org.alicebot.server.net.http.util.StringUtil;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/* ------------------------------------------------------------ */
/** Servlet Response Wrapper.
 * This class wraps a Jetty HTTP response as a 2.2 Servlet
 * response.
 *
 * Note that this wrapper is not synchronized and if a response is to
 * be operated on by multiple threads, then higher level
 * synchronizations may be required.
 *
 * @version $Id: ServletResponse.java,v 1.1.1.1 2001/06/17 19:02:41 noelbu Exp $
 * @author Greg Wilkins (gregw)
 */
public class ServletResponse implements HttpServletResponse
{
    private HttpResponse _httpResponse;
    private ServletRequest _servletRequest;
    private int _outputState=0;
    private ServletOut _out =null;
    private ServletWriter _writer=null;
    private HttpSession _session=null;
    private boolean _noSession=false;
    private boolean _locked=false;
    private Locale _locale=null;

    private static Map __charSetMap = new HashMap();
    static
    {
        // list borrowed from tomcat 3.2B6 - thanks guys.
        __charSetMap.put("ar", "ISO-8859-6");
        __charSetMap.put("be", "ISO-8859-5");
        __charSetMap.put("bg", "ISO-8859-5");
        __charSetMap.put("ca", StringUtil.__ISO_8859_1);
        __charSetMap.put("cs", "ISO-8859-2");
        __charSetMap.put("da", StringUtil.__ISO_8859_1);
        __charSetMap.put("de", StringUtil.__ISO_8859_1);
        __charSetMap.put("el", "ISO-8859-7");
        __charSetMap.put("en", StringUtil.__ISO_8859_1);
        __charSetMap.put("es", StringUtil.__ISO_8859_1);
        __charSetMap.put("et", StringUtil.__ISO_8859_1);
        __charSetMap.put("fi", StringUtil.__ISO_8859_1);
        __charSetMap.put("fr", StringUtil.__ISO_8859_1);
        __charSetMap.put("hr", "ISO-8859-2");
        __charSetMap.put("hu", "ISO-8859-2");
        __charSetMap.put("is", StringUtil.__ISO_8859_1);
        __charSetMap.put("it", StringUtil.__ISO_8859_1);
        __charSetMap.put("iw", "ISO-8859-8");
        __charSetMap.put("ja", "Shift__JIS");
        __charSetMap.put("ko", "EUC-KR");     
        __charSetMap.put("lt", "ISO-8859-2");
        __charSetMap.put("lv", "ISO-8859-2");
        __charSetMap.put("mk", "ISO-8859-5");
        __charSetMap.put("nl", StringUtil.__ISO_8859_1);
        __charSetMap.put("no", StringUtil.__ISO_8859_1);
        __charSetMap.put("pl", "ISO-8859-2");
        __charSetMap.put("pt", StringUtil.__ISO_8859_1);
        __charSetMap.put("ro", "ISO-8859-2");
        __charSetMap.put("ru", "ISO-8859-5");
        __charSetMap.put("sh", "ISO-8859-5");
        __charSetMap.put("sk", "ISO-8859-2");
        __charSetMap.put("sl", "ISO-8859-2");
        __charSetMap.put("sq", "ISO-8859-2");
        __charSetMap.put("sr", "ISO-8859-5");
        __charSetMap.put("sv", StringUtil.__ISO_8859_1);
        __charSetMap.put("tr", "ISO-8859-9");
        __charSetMap.put("uk", "ISO-8859-5");
        __charSetMap.put("zh", "GB2312");
        __charSetMap.put("zh_TW", "Big5");    
    }
    
    
    /* ------------------------------------------------------------ */
    ServletResponse(ServletRequest request,HttpResponse response)
    {
        _servletRequest=request;
        _servletRequest.setServletResponse(this);
        _httpResponse=response;
    }

    /* ------------------------------------------------------------ */
    boolean getLocked()
    {
        return _locked;
    }
    
    /* ------------------------------------------------------------ */
    void setLocked(boolean locked)
    {
        _locked=locked;
    }

    /* ------------------------------------------------------------ */
    int getOutputState()
    {
        return _outputState;
    }
    
    /* ------------------------------------------------------------ */
    void setOutputState(int s)
        throws IOException
    {
        if (s<0)
        {
            _outputState=0;
            if (_writer!=null)
                _writer.disable();
            _writer=null;
            if (_out!=null)
                _out.disable();
            _out=null;
        }
        else
        {
            _outputState=s;
            if (_writer!=null)
                _writer.flush();
            if (_out!=null)
                _out.flush();
        }
    }
    
    
    /* ------------------------------------------------------------ */
    HttpResponse getHttpResponse()
    {
        return _httpResponse;
    }
    
    /* ------------------------------------------------------------ */
    void commit()
        throws IOException
    {
        _httpResponse.commit();
    }

    /* ------------------------------------------------------------ */
    public boolean isCommitted()
    {
        return _httpResponse.isCommitted();
    }
    
    /* ------------------------------------------------------------ */
    boolean isDirty()
    {
        return _httpResponse.isDirty();
    }

    /* ------------------------------------------------------------ */
    public void setBufferSize(int size)
    {
        ChunkableOutputStream out = _httpResponse.getOutputStream();
        if (out.isWritten())
            throw new IllegalStateException("Output written");
        
        out.setBufferCapacity(size);
    }
    
    /* ------------------------------------------------------------ */
    public int getBufferSize()
    {
        return _httpResponse.getOutputStream().getBufferCapacity();
    }
    
    
    /* ------------------------------------------------------------ */
    public void flushBuffer()
        throws IOException
    {
        if (_writer!=null)
            _writer.flush();
        if (_out!=null)
            _out.flush();
        if (_writer==null && _out==null)
            _httpResponse.getOutputStream().flush();
        _httpResponse.commit();
    }
    
    /* ------------------------------------------------------------ */
    void resetBuffer()
    {
        _httpResponse.getOutputStream().resetBuffer();
    }
    
    /* ------------------------------------------------------------ */
    public void reset()
    {
        _httpResponse.reset();
    }
    
    /* ------------------------------------------------------------ */
    /**
     * Sets the locale of the response, setting the headers (including the
     * Content-Type's charset) as appropriate.  This method should be called
     * before a call to {@link #getWriter}.  By default, the response locale
     * is the default locale for the server.
     * 
     * @see   #getLocale
     * @param loc the locale of the response
     */
    public void setLocale(Locale locale)
    {
        if (locale == null)
            return; 

        _locale = locale;
        String lang = locale.getLanguage();
        setHeader(HttpFields.__ContentLanguage, lang);
                          
        /* get current MIME type from Content-Type header */                  
        String type=_httpResponse.getField(HttpFields.__ContentType);
        if (type==null)
        {
            // servlet did not set Content-Type yet
            // so lets assume default one
            type="application/octet-stream";
        }
        else if (type.startsWith("text/") && _httpResponse.getCharacterEncoding()==null)
            /* If there is already charset parameter in content-type,
               we will leave it alone as it is already correct.
               This allows for both setContentType() and setLocale() to be called.
               It makes some sense for text/ MIME types to try to guess output encoding 
               based on language code from locale when charset parameter is not present.
               Guessing is not a problem because when encoding matters, setContentType()
               should be called with charset parameter in MIME type.
              
               JH: I think guessing should be exterminated as it makes no sense.
            */
        {
            /* pick up encoding from map based on languge code */
            String charset = (String)__charSetMap.get(lang);
            if (charset != null)
            {
                type = ((type.indexOf(";")==-1)?"; charset=":" charset=")+
                    charset;
            }
        }        
        /* lets put updated MIME type back */
        setHeader(HttpFields.__ContentType,type);
    }
    
    /* ------------------------------------------------------------ */
    public Locale getLocale()
    {
        return _locale;
    }
    
    /* ------------------------------------------------------------ */
    public void addCookie(Cookie cookie) 
    {
        _httpResponse.addSetCookie(cookie);
    }

    /* ------------------------------------------------------------ */
    public boolean containsHeader(String name) 
    {
        return _httpResponse.containsField(name);
    }

    /* ------------------------------------------------------------ */
    public String encodeURL(String url) 
    {
        // should not encode if cookies in evidence
        if (_servletRequest==null || _servletRequest.isRequestedSessionIdFromCookie())
            return url;        
        
        // get session;
        if (_session==null && !_noSession)
        {
            _session=_servletRequest.getSession(false);
            _noSession=(_session==null);
        }
        
        // no session or no url
        if (_session == null || url==null)
            return url;
        
        // invalid session
        String id = _session.getId();
        if (id == null)
            return url;
        
        // Check host and port are for this server
        // XXX not implemented
        
        // Already encoded
        int prefix=url.indexOf(Context.__SessionUrlPrefix);
        if (prefix!=-1)
        {
            int suffix=url.indexOf("?",prefix);
            if (suffix<0)
                suffix=url.indexOf("#",prefix);
            
            return url.substring(0,prefix+Context.__SessionUrlPrefix.length())+
                (suffix<=prefix?"":url.substring(suffix));
        }        
        
        // edit the session
        int end=url.indexOf('?');
        if (end<0)
            end=url.indexOf('#');
        if (end<0)
            return url+Context.__SessionUrlPrefix+id;
        return url.substring(0,end)+
            Context.__SessionUrlPrefix+id+url.substring(end);
    }

    /* ------------------------------------------------------------ */
    public String encodeRedirectURL(String url) 
    {
        return encodeURL(url);
    }

    /* ------------------------------------------------------------ */
    /**
     * @deprecated	As of version 2.1, use encodeURL(String url) instead
     */
    public String encodeUrl(String url) 
    {
        return encodeURL(url);
    }

    /* ------------------------------------------------------------ */
    /**
     * @deprecated	As of version 2.1, use 
     *			encodeRedirectURL(String url) instead
     */
    public String encodeRedirectUrl(String url) 
    {
        return encodeRedirectURL(url);
    }

    /* ------------------------------------------------------------ */
    public void sendError(int status, String message)
        throws IOException
    {
        _httpResponse.sendError(status,message);
    }

    /* ------------------------------------------------------------ */
    public void sendError(int status) 
        throws IOException
    {
        _httpResponse.sendError(status);
    }

    /* ------------------------------------------------------------ */
    public void sendRedirect(String url) 
        throws IOException
    {
        _httpResponse.sendRedirect(url);
    }

    /* ------------------------------------------------------------ */
    public void setDateHeader(String name, long value) 
    {
        if (_locked)
            return;
        _httpResponse.setDateField(name,value);
    }

    /* ------------------------------------------------------------ */
    public void setHeader(String name, String value) 
    {
        if (_locked)
            return;
        _httpResponse.setField(name,value);
    }

    /* ------------------------------------------------------------ */
    public void setIntHeader(String name, int value) 
    {
        if (_locked)
            return;
        _httpResponse.setIntField(name,value);
    }
    
    /* ------------------------------------------------------------ */
    public void addDateHeader(String name, long value) 
    {
        if (_locked)
            return;
        
        _httpResponse.addDateField(name,new Date(value));
    }

    /* ------------------------------------------------------------ */
    public void addHeader(String name, String value) 
    {
        if (_locked)
            return;
        
        _httpResponse.addField(name,value);
    }

    /* ------------------------------------------------------------ */
    public void addIntHeader(String name, int value) 
    {
        if (_locked)
            return;
        
        _httpResponse.addIntField(name,value);
    }

    /* ------------------------------------------------------------ */
    public void setStatus(int status) 
    {
        _httpResponse.setStatus(status);
    }

    /* ------------------------------------------------------------ */
    public void setStatus(int status, String message) 
    {
        setStatus(status);
        _httpResponse.setReason(message);
    }

    /* ------------------------------------------------------------ */
    public String getCharacterEncoding() 
    {
        String encoding=_httpResponse.getCharacterEncoding();
        return (encoding==null)?StringUtil.__ISO_8859_1:encoding;
    }

    /* ------------------------------------------------------------ */
    public ServletOutputStream getOutputStream() 
    {
        if (_outputState!=0 && _outputState!=1)
            throw new IllegalStateException();
        
        if (_writer!=null)
        {
            _writer.flush();
            _writer.disable();
            _writer=null;
        }
        
        if (_out==null)
            _out = new ServletOut(_servletRequest.getHttpRequest()
                                  .getOutputStream());  
        _outputState=1;
        return _out;
    }

    /* ------------------------------------------------------------ */
    public PrintWriter getWriter()
        throws java.io.IOException 
    {
        if (_outputState!=0 && _outputState!=2)
            throw new IllegalStateException();
        /* if there is no writer yet */
        if (_writer==null)
        {
            /* get encoding from Content-Type header */
            String encoding = getCharacterEncoding();
            if (encoding==null && _servletRequest!=null)
            {
                /* implementation of educated defaults */
                String mimeType = _httpResponse.getMimeType();                
                encoding = _servletRequest.getContext().getServletHandler()
                    .getHandlerContext().getEncodingByMimeType(mimeType);
            }
            if (encoding==null)
                // get last resort hardcoded default
                encoding = StringUtil.__ISO_8859_1; 
            
            /* construct Writer using correct encoding */
            _writer = new ServletWriter(_httpResponse.getOutputStream(), encoding);
        }                    
        _outputState=2;
        return _writer;
    }
    
    /* ------------------------------------------------------------ */
    public void setContentLength(int len) 
    {
        // Protect from setting after committed as default handling
        // of a servlet HEAD request ALWAYS sets content length, even
        // if the getHandling committed the response!
        if (!isCommitted())
            setIntHeader(HttpFields.__ContentLength,len);
    }
    
    /* ------------------------------------------------------------ */
    public void setContentType(String contentType) 
    {
        setHeader(HttpFields.__ContentType,contentType);
        if (_locale!=null)
            setLocale(_locale);
    }

}





