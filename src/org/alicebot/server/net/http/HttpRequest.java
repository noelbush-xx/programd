// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: HttpRequest.java,v 1.1.1.1 2001/06/17 19:00:48 noelbu Exp $
// ---------------------------------------------------------------------------

package org.alicebot.server.net.http;

import org.alicebot.server.net.http.util.Code;
import org.alicebot.server.net.http.util.LineInput;
import org.alicebot.server.net.http.util.MultiMap;
import org.alicebot.server.net.http.util.QuotedStringTokenizer;
import org.alicebot.server.net.http.util.StringUtil;
import org.alicebot.server.net.http.util.URI;
import org.alicebot.server.net.http.util.UrlEncoded;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.Principal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.Cookie;


/* ------------------------------------------------------------ */
/** HTTP Request.
 * This class manages the headers, trailers and content streams
 * of a HTTP request. It can be used for receiving or generating
 * requests.
 *
 * This class is not synchronized. It should be explicitly
 * synchronized if it is used by multiple threads.
 *
 * @see HttpResponse
 * @version $Id: HttpRequest.java,v 1.1.1.1 2001/06/17 19:00:48 noelbu Exp $
 * @author Greg Wilkins (gregw)
 */
public class HttpRequest extends HttpMessage
{
    /* ------------------------------------------------------------ */
    /** Request METHODS.
     */
    public static final String
        __OPTIONS="OPTIONS",
        __GET="GET",
        __HEAD="HEAD",
        __POST="POST",
        __PUT="PUT",
        __DELETE="DELETE",
        __TRACE="TRACE",
        __CONNECT="CONNECT",
        __MOVE="MOVE";

    public static final String
        __AuthType = "org.alicebot.server.net.http.HttpRequest.AuthType",
        __AuthUser = "org.alicebot.server.net.http.HttpRequest.AuthUser";
    
    /* ------------------------------------------------------------ */
    private String _method=null;
    private URI _uri=null;
    private String _host;
    private int _port;
    private List _te;
    private MultiMap _parameters;
    private boolean _paramsExtracted;
    private boolean _handled;
    private Cookie[] _cookies;
    private Map _attributes;

    /* ------------------------------------------------------------ */
    /** Constructor. 
     */
    public HttpRequest()
    {}
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param connection 
     */
    public HttpRequest(HttpConnection connection)
    {
        super(connection);
    }

    /* ------------------------------------------------------------ */
    /** Get the HTTP Response.
     * Get the HTTP Response associated with this request.
     * @return associated response
     */
    public HttpResponse getResponse()
    {
        if (_connection==null)
            return null;
        return _connection.getResponse();
    }

    /* ------------------------------------------------------------ */
    /** Is the request handled.
     * @return True if the request has been set to handled or the
     * associated response is not editable.
     */
    public boolean isHandled()
    {
        if (_handled)
            return true;

        HttpResponse response= getResponse();
        return (response!=null && response.getState()!=response.__MSG_EDITABLE);
    }

    /* ------------------------------------------------------------ */
    /** Set the handled status.
     * @param handled true or false
     */
    public void setHandled(boolean handled)
    {
        _handled=handled;
    }
    
    /* ------------------------------------------------------------ */
    /** Read the request line and header.
     * @param in 
     * @exception IOException 
     */
    public void readHeader(ChunkableInputStream in)
        throws IOException
    {
        _state=__MSG_BAD;

        LineInput line_input = (LineInput)in.getRawStream();
        
        // Get start line
        org.alicebot.server.net.http.util.LineInput.LineBuffer line_buffer;

        do
        {
            line_buffer=line_input.readLineBuffer();
        }
        while(line_buffer!=null && line_buffer.size==0);
        
        
        if (line_buffer==null)
            throw new InterruptedIOException("EOF");
        if (line_buffer.size==in.__maxLineLength)
            throw new HttpException(HttpResponse.__414_Request_URI_Too_Large);
        decodeRequestLine(line_buffer.buffer,line_buffer.size);
        
        // Handle version - replace with fast compare
        if (__HTTP_1_1.equals(_version))
        {
            _dotVersion=1;
            _version=__HTTP_1_1;
            _header.read(line_input);
            setMimeAndEncoding(_header.get(HttpFields.__ContentType));
        }
        else if (__HTTP_0_9.equals(_version))
        {
            _dotVersion=-1;
            _version=__HTTP_0_9;
        }
        else
        {
            _dotVersion=0;
            _version=__HTTP_1_0;
            _header.read(line_input);
            setMimeAndEncoding(_header.get(HttpFields.__ContentType));
        }

        _handled=false;
        _state=__MSG_RECEIVED;
    }
    
    /* -------------------------------------------------------------- */
    /** Write the request header.
     * Places the message in __MSG_SENDING state.
     * @param out Chunkable output stream
     * @exception IOException IO problem
     */
    public void writeHeader(Writer writer)
        throws IOException
    {
        if (_state!=__MSG_EDITABLE)
            throw new IllegalStateException("Not MSG_EDITABLE");
        
        _state=__MSG_BAD;
        synchronized(writer)
        {
            writer.write(_method);
            writer.write(' ');
            writer.write(_uri!=null?_uri.toString():"null");
            writer.write(' ');
            writer.write(_version);
            writer.write(HttpFields.__CRLF);
            _header.write(writer);
        }
        _state=__MSG_SENDING;
    }

    /* -------------------------------------------------------------- */
    /** Return the HTTP request line as it was received.
     */
    public String getRequestLine()
    {
        return _method+" "+_uri+" "+_version;
    }
    
    /* -------------------------------------------------------------- */
    /** Get the HTTP method for this request.
     * Returns the method with which the request was made. The returned
     * value can be "GET", "HEAD", "POST", or an extension method. Same
     * as the CGI variable REQUEST_METHOD.
     * @return The method
     */
    public String getMethod()
    {
        return _method;
    }
    
    /* -------------------------------------------------------------- */
    /** Set the HTTP method for this request.
     * @param method the method
     * @exception IllegalStateException Request is not EDITABLE
     */
    public void setMethod(String method)
        throws IllegalStateException
    {
        if (_state!=__MSG_EDITABLE)
            throw new IllegalStateException("Not EDITABLE");
        _method=method;
    }


    /* ------------------------------------------------------------ */
    /**
     * Reconstructs the URL the client used to make the request.
     * The returned URL contains a protocol, server name, port
     * number, and server path, but it does not include query
     * string parameters.
     * 
     * <p>Because this method returns a <code>StringBuffer</code>,
     * not a string, you can modify the URL easily, for example,
     * to append query parameters.
     *
     * <p>This method is useful for creating redirect messages
     * and for reporting errors.
     *
     * @return		a <code>StringBuffer</code> object containing
     *			the reconstructed URL
     *
     */
    public StringBuffer getRequestURL()
    {
        StringBuffer url = new StringBuffer ();
        synchronized(url)
        {
            String scheme = getScheme();
            int port = getPort();

            url.append (scheme);
            url.append ("://");
            url.append (getHost());
            if (port>0 && ((scheme.equals ("http") && port != 80)||
                           (scheme.equals ("https") && port != 443)))
            {
                url.append (':');
                url.append (port);
            }
            url.append(getPath());
            return url;
        }
    }

    
    /* -------------------------------------------------------------- */
    /** Get the full URI.
     * @return A cloned copy of the request URI
     */
    public URI getURI()
    {
        return (URI)_uri.clone();
    }
    
    /* -------------------------------------------------------------- */
    /** Set the HTTP URI.
     * @param uri the uri
     * @exception IllegalStateException Request is not EDITABLE
     */
    public void setURI(URI uri)
    {
        if (_state!=__MSG_EDITABLE)
            throw new IllegalStateException("Not EDITABLE");
        _uri=uri;
    }
    
    /* ------------------------------------------------------------ */
    /** Get the request Scheme.
     * The scheme is obtained from an absolute URI.  If the URI in
     * the request is not absolute, then the connections default
     * scheme is returned.  If there is no connection "http" is returned.
     * @return The request scheme (eg. "http", "https", etc.)
     */
    public String getScheme()
    {
        String scheme=_uri.getScheme();
        if (scheme==null && _connection!=null)
            scheme=_connection.getDefaultScheme();
        return scheme==null?"http":scheme;
    }
    
    /* ------------------------------------------------------------ */
    /** Set the request scheme.
     * If the URI was not previously an absolute URI, the URI host and
     * port are also set from the HTTP Host Header field.
     * @param scheme The scheme
     * @exception IllegalStateException Request is not EDITABLE
     */
    public void setScheme(String scheme)
    {
        if (_state!=__MSG_EDITABLE)
            throw new IllegalStateException("Not EDITABLE");
        
        if (!_uri.isAbsolute())
        {
            _uri.setHost(getHost());
            _uri.setPort(getPort());
        }
        _uri.setScheme(scheme);
    }
    
    /* ------------------------------------------------------------ */
    /** Get the request host.
     * The host is obtained from an absolute URI, the HTTP header field,
     * the requests connection or the local host name.
     * @return 
     */
    public String getHost()
    {
        // Return already determined host
        if (_host!=null)
            return _host;

        // Return host from absolute URI
        _host=_uri.getHost();
        _port=_uri.getPort();
        if (_host!=null)
            return _host;

        // Return host from header field
        _host=_header.get(HttpFields.__Host);
        _port=0;
        if (_host!=null)
        {
            int colon=_host.indexOf(':');
            if (colon>=0)
            {
                if (colon<_host.length())
                {
                    try{
                        _port=Integer
                            .parseInt(_host.substring(colon+1));
                    }
                    catch(Exception e)
                    {Code.ignore(e);}
                }
                _host=_host.substring(0,colon);
            }

            return _host;
        }

        // Return host from connection
        if (_connection!=null)
        {
            _host=_connection.getHost();
            _port=_connection.getPort();
            return _host;
        }

        // Return the local host
        try {_host=InetAddress.getLocalHost().getHostName();}
        catch(java.net.UnknownHostException e){Code.ignore(e);}
        return _host;
    }
    
    /* ------------------------------------------------------------ */
    /** Set the request host.
     * If the current uri is absolute, then the URI is updated.
     * The HTTP Host header field is always updated.
     * @param host The host
     * @exception IllegalStateException Request is not EDITABLE
     */
    public void setHost(String host)
        throws IllegalStateException
    {
        if (_state!=__MSG_EDITABLE)
            throw new IllegalStateException("Not EDITABLE");
        
        _host=host;
        if (_uri.isAbsolute())
            _uri.setHost(host);
        
        getPort();
        if (_port>0)
            _header.put(HttpFields.__Host,host+":"+_port);
        else
            _header.put(HttpFields.__Host,host);
    }
    
    /* ------------------------------------------------------------ */
    /** Get the request port.
     * The port is obtained either from an absolute URI, the HTTP
     * Host header field, the connection or the default.
     * @return The port.  0 should be interpreted as the default port.
     */
    public int getPort()
    {
        if (_port>0)
            return _port;
        if (_uri.isAbsolute())
            _port=_uri.getPort();
        else if (_connection!=null)
            _port=_connection.getPort();
        return _port;    
    }
    
    /* ------------------------------------------------------------ */
    /** Set the request port.
     * If the current uri is absolute, then the URI is updated.
     * The HTTP Host header field is always updated.
     * @param port The port
     * @exception IllegalStateException Request is not EDITABLE
     */
    public void setPort(int port)
        throws IllegalStateException
    {
        if (_state!=__MSG_EDITABLE)
            throw new IllegalStateException("Not EDITABLE");
        
        _port=port;
        if (_uri.isAbsolute())
            _uri.setPort(port);
        
        getHost();
        if (_port>0)
            _header.put(HttpFields.__Host,_host+":"+_port);
        else
            _header.put(HttpFields.__Host,_host);
    }
    
    /* ------------------------------------------------------------ */
    /** Get the request path.
     * @return 
     */
    public String getPath()
    {
        return _uri.getPath();
    }
    
    /* ------------------------------------------------------------ */
    /** Set the request path.
     * @param path The path
     * @exception IllegalStateException Request is not EDITABLE
     */
    public void setPath(String path)
    {
        if (_state!=__MSG_EDITABLE)
            throw new IllegalStateException("Not EDITABLE");
        
        _uri.setPath(path);
    }
    
    /* ------------------------------------------------------------ */
    /** Get the request query.
     * @return 
     */
    public String getQuery()
    {
        return _uri.getQuery();
    }
    
    /* ------------------------------------------------------------ */
    /** Set the request query.
     * @param query The query
     * @exception IllegalStateException Request is not EDITABLE
     */
    public void setQuery(String query)
    {
        if (_state!=__MSG_EDITABLE)
            throw new IllegalStateException("Not EDITABLE");
        
        _uri.setQuery(query);
    }

    /* ------------------------------------------------------------ */
    public String getRemoteAddr()
    {
        HttpConnection connection = getConnection();
        if (connection!=null)
        {
            InetAddress addr = connection.getRemoteAddr();
            if (addr!=null)
                return addr.getHostAddress();
        }
        return "127.0.0.1";
    }
    
    /* ------------------------------------------------------------ */
    /** Decode HTTP request line.
     * @param buf Character buffer
     * @param len Length of line in buffer.
     * @exception IOException 
     */
    void decodeRequestLine(char[] buf,int len)
        throws IOException
    {        
        // Search for first space separated chunk
        int s1=-1,s2=-1,s3=-1;
        int state=0;
    startloop:
        for (int i=0;i<len;i++)
        {
            char c=buf[i];
            switch(state)
            {
              case 0: // leading white
                  if (c==' ')
                      continue;
                  state=1;
                  s1=i;
                  
              case 1: // reading method
                  if (c==' ')
                      state=2;
                  else
                  {
                      s2=i;
                      if (c>='a'&&c<='z')
                          buf[i]=(char)(c-'a'+'A');
                  }
                  continue;
                  
              case 2: // skip whitespace after method
                  s3=i;
                  if (c!=' ')
                      break startloop;
            }
        }

        // Search for first space separated chunk
        int e1=-1,e2=-1,e3=-1;
        state=0;
    endloop:
        for (int i=len;i-->0;)
        {
            char c=buf[i];
            switch(state)
            {
              case 0: // trailing white
                  if (c==' ')
                      continue;
                  state=1;
                  e1=i;
                  
              case 1: // reading Version
                  if (c==' ')
                      state=2;
                  else
                      e2=i;
                  continue;
                  
              case 2: // skip whitespace after method
                  e3=i;
                  if (c!=' ')
                      break endloop;
            }
        }
        
        // Check sufficient params
        if (s3<0 || e1<0 || e3<s2 )
            throw new IOException("Bad Request: "+new String(buf,0,len));

        // get method
        _method=new String(buf,s1,s2-s1+1);
        
        // get version as uppercase
        if (s2!=e3 || s3!=e2)
        {
            for (int i=e2;i<=e1;i++)
                if (buf[i]>='a'&&buf[i]<='z')
                    buf[i]=(char)(buf[i]-'a'+'A');
            _version=new String(buf,e2,e1-e2+1);
        }
        else
        {
            // missing version
            _version=__HTTP_0_9;
            e3=e1;
        }

        // handle URI
        _uri= new URI(new String(buf,s3,e3-s3+1));
        
    }
    
    /* ------------------------------------------------------------ */
    /** Force a removeField.
     * This call ignores the message state and forces a field
     * to be removed from the request.  It is required for the
     * handling of the Connection field.
     * @param name The field name
     * @return The old value or null.
     */
    public Object forceRemoveField(String name)
    {
        if (Code.verbose(99))
            Code.debug("force remove ",name);
        int saved_state=_state;
        try{
            _state=__MSG_EDITABLE;
            return removeField(name);
        }
        finally
        {
            _state=saved_state;
        }
    }


    /* ------------------------------------------------------------ */
    /** Get the acceptable transfer encodings.
     * The TE field is used to construct a list of acceptable
     * extension transfer codings in quality order.
     * An empty list implies that only "chunked" is acceptable.
     * A null list implies that no transfer coding can be applied.
     *
     * If the "trailer" coding is found in the TE field, then
     * message trailers are enabled in any linked response.
     * @return List of codings.
     */
    public List getAcceptableTransferCodings()
    {
        if (_dotVersion<1)
            return null;
        if (_te!=null)
            return _te;
        
        // Decode any TE field
        List te = getFieldValues(HttpFields.__TE);
        if (te!=null && te.size()>0)
        {
            // Sort the list
            te=HttpFields.qualityList(te);

            // remove trailer and chunked items.
            ListIterator iter = te.listIterator();
            while(iter.hasNext())
            {
                String coding= StringUtil.asciiToLowerCase
                    (HttpFields.valueParameters(iter.next().toString(),null));
                
                iter.set(coding);
                if ("trailer".equals(coding))
                {
                    // Allow trailers in the response
                    HttpResponse response=getResponse();
                    if (response!=null)
                        response.setAcceptTrailer(true);
                    iter.remove();
                }
                else if (HttpFields.__Chunked.equals(coding))
                    iter.remove();
            }
            _te=te;
        }
        else
            _te=Collections.EMPTY_LIST;

        return _te;
    }


    /* ------------------------------------------------------------ */
    /* Extract Paramters from query string and/or form content.
     */
    private void extractParameters()
    {
        if (_paramsExtracted)
            return;
        _paramsExtracted=true;
        if (_parameters==null)
            _parameters=new MultiMap(_uri.getUnmodifiableParameters());
        else
            _parameters.putAll(_uri.getUnmodifiableParameters());
        
        if (_state==__MSG_RECEIVED)
        {
            String content_type=getField(HttpFields.__ContentType);
            if (content_type!=null && content_type.length()>0)
            {
                content_type=StringUtil.asciiToLowerCase(content_type);
                content_type=HttpFields.valueParameters(content_type,null);

                if (HttpFields.__WwwFormUrlEncode.equals(content_type)&&
                    HttpRequest.__POST.equals(getMethod()))
                {
                    int content_length = getIntField(HttpFields.__ContentLength);
                    if (content_length<0)
                        Code.warning("No contentLength for "+
                                     HttpFields.__WwwFormUrlEncode);
                    else
                    {
                        try
                        {
                            // Read the content
                            byte[] content=new byte[content_length];
                            InputStream in = getInputStream();
                            int offset=0;
                            int len=0;
                            while ((content_length - offset) > 0)
                            {
                                 len=in.read(content,offset,content_length-offset);
                                 if (len <= 0)
                                     throw new IOException("Premature EOF reading params");
                                 offset+=len;
                            }

                            // Add form params to query params
                            String contentStr = new String(content,
                                                           0,
                                                           content_length,
                                                           StringUtil.__ISO_8859_1);
                            Code.debug("Form content='",contentStr,"'");
                            UrlEncoded.decodeTo(contentStr,_parameters);
                        }
                        catch (IOException e)
                        {
                            if (Code.debug())
                                Code.warning(e);
                            else
                                Code.warning(e.toString());
                        }
                    }
                }
            }
        }
    }

    /* ------------------------------------------------------------ */
    /** 
     * @return 
     */
    public MultiMap getParameters()
    {
        if (!_paramsExtracted)
            extractParameters();
        return _parameters;
    }
    
    
    /* ------------------------------------------------------------ */
    /** Get the set of parameter names.
     * @return Set of parameter names.
     */
    public Set getParameterNames()
    {
        if (!_paramsExtracted)
            extractParameters();
        return _parameters.keySet();
    }
    
    /* ------------------------------------------------------------ */
    /** Get a parameter value.
     * @param name Parameter name
     * @return Parameter value
     */
    public String getParameter(String name)
    {
        if (!_paramsExtracted)
            extractParameters();
        return _parameters.getString(name);
    }
    
    /* ------------------------------------------------------------ */
    /** Get multi valued paramater.
     * @param name Parameter name
     * @return Parameter values
     */
    public List getParameterValues(String name)
    {
        if (!_paramsExtracted)
            extractParameters();
        return _parameters.getValues(name);
    }
    

    /* -------------------------------------------------------------- */
    /** Extract received cookies from a header.
     * @param buffer Contains encoded cookies
     * @return Array of Cookies.
     */
    public Cookie[] getCookies()
    {
        if (_cookies!=null)
            return _cookies;

        try
        {
            List cookieStrs = _header.getValues(HttpFields.__Cookie);
            
            if (cookieStrs==null || cookieStrs.size()==0)
            {
                _cookies=new Cookie[0];
                return _cookies;
            }
            
            ArrayList cookies=new ArrayList(4);
            for (int i=0;cookieStrs!=null && i<cookieStrs.size();i++)
            {
                String cookieStr = (String)cookieStrs.get(i);
                int version=0;
                Cookie cookie=null;
                Code.debug("Cookies=",cookieStr);
                QuotedStringTokenizer tok = new QuotedStringTokenizer(cookieStr,";");
                while (tok.hasMoreTokens())
                {
                    String c = tok.nextToken();
                    int e = c.indexOf("=");
                    String n;
                    String v;
                    if (e>0)
                    {
                        n=c.substring(0,e).trim();
                        v=c.substring(e+1).trim();
                    }
                    else
                    {
                        n=c.trim();
                        v="";
                    }
                    
                    // Handle quoted values
                    if (version>0)
                        v=StringUtil.unquote(v);
                    
                    // Ignore $ names
                    if (n.startsWith("$"))
                    {
                        if ("$version".equalsIgnoreCase(n))
                        {
                            int coma=v.indexOf(",");
                            if (coma>=0)
                            {   
                                version=Integer.parseInt
                                    (StringUtil.unquote(v.substring(0,coma)));
                                v=v.substring(coma+1);
                                e=v.indexOf("=");
                                if (e>0)
                                {
                                    n=v.substring(0,e).trim();
                                    v=v.substring(e+1).trim();
                                    v=StringUtil.unquote(v);
                                }
                                else
                                {
                                    n=v.trim();
                                    v="";
                                }
                            }
                            else
                                continue;
                        }
                        else
                        {
                            if ("$path".equalsIgnoreCase(n) && cookie!=null)
                                cookie.setPath(v);
                            else if ("$domain".equalsIgnoreCase(n)&&cookie!=null)
                                cookie.setDomain(v);
                            continue;
                        }
                    }
                    
                    v=UrlEncoded.decodeString(v);
                    cookie=new Cookie(n,v);
                    if (version>0)
                        cookie.setVersion(version);
                    cookies.add(cookie);
                }
            }
            
            _cookies=new Cookie[cookies.size()];
            if (cookies.size()>0)
                cookies.toArray(_cookies);
        }
        catch(Exception e)
        {
            Code.warning(e);
        }
        
        return _cookies;
    }


    /* ------------------------------------------------------------ */
    /** Get a request attribute.
     * @param name 
     * @return 
     */
    public Object getAttribute(String name)
    {
        if (_attributes==null)
            return null;
        return _attributes.get(name);
    }

    /* ------------------------------------------------------------ */
    /** Set a request attribute.
     * @param name 
     * @param attribute 
     * @return 
     */
    public Object setAttribute(String name, Object attribute)
    {
        if (_attributes==null)
            _attributes=new HashMap(11);
        return _attributes.put(name,attribute);
    }

    /* ------------------------------------------------------------ */
    /** Get Attribute names.
     * @return 
     */
    public Collection getAttributeNames()
    {
        if (_attributes==null)
            return Collections.EMPTY_LIST;
        return _attributes.keySet();
    }


    /* ------------------------------------------------------------ */
    /** Remove a request attribute.
     * @param name 
     * @param attribute 
     * @return 
     */
    public void removeAttribute(String name)
    {
        if (_attributes!=null)
            _attributes.remove(name);
    }
    
    /* ------------------------------------------------------------ */
    public boolean isUserInRole(String role)
    {
        Principal principal=getUserPrincipal();
        if (principal!=null && principal instanceof UserPrincipal)
            return ((UserPrincipal)principal).isUserInRole(role);
        return false;
    }
    
    /* ------------------------------------------------------------ */
    public Principal getUserPrincipal()
    {
        return (Principal)getAttribute(UserPrincipal.__ATTR);
    }
    
    /* ------------------------------------------------------------ */
    /** Recycle the request.
     */
    public void recycle(HttpConnection connection)
    {
        super.recycle(connection);
        _method=null;
        _uri=null;
        _host=null;
        _port=0;
        _te=null;
        if (_parameters!=null)
            _parameters.clear();
        _paramsExtracted=false;
        _handled=false;
        _cookies=null;
        if (_attributes!=null)
            _attributes.clear();
    }
    
    /* ------------------------------------------------------------ */
    /** Destroy the request.
     * Help the garbage collector by null everything that we can.
     */
    public void destroy()
    {
        _method=null;
        _uri=null;
        _version=null;
        if (_attributes!=null)
            _attributes.clear();
        super.destroy();
    }  
}
