// ========================================================================
// Copyright (c) 2000 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: ServletRequest.java,v 1.1.1.1 2001/06/17 19:02:39 noelbu Exp $
// ========================================================================


package org.alicebot.server.net.http.handler.servlet;

import org.alicebot.server.net.http.HandlerContext;
import org.alicebot.server.net.http.HttpConnection;
import org.alicebot.server.net.http.HttpFields;
import org.alicebot.server.net.http.HttpRequest;
import org.alicebot.server.net.http.util.Code;
import org.alicebot.server.net.http.util.MultiMap;
import org.alicebot.server.net.http.util.Resource;
import org.alicebot.server.net.http.util.StringUtil;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.security.Principal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletInputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;


/* ------------------------------------------------------------ */
/** Servlet Request Wrapper.
 * This class wraps a Jetty HTTP request as a 2.2 Servlet
 * request.
 *
 * Note that this wrapper is not synchronized and if a request is to
 * be operated on by multiple threads, then higher level
 * synchronizations may be required.
 * 
 * @version $Id: ServletRequest.java,v 1.1.1.1 2001/06/17 19:02:39 noelbu Exp $
 * @author Greg Wilkins (gregw)
 */
public class ServletRequest
    implements HttpServletRequest
{
    /* -------------------------------------------------------------- */
    public static final String
        __SESSIONID_NOT_CHECKED = "not checked",
        __SESSIONID_URL = "url",
        __SESSIONID_COOKIE = "cookie",
        __SESSIONID_NONE = "none";

    private HttpRequest _httpRequest;
    private ServletResponse _servletResponse;

    private String _uri=null;
    private String _contextPath=null;
    private String _servletPath=null;
    private String _pathInfo=null;
    private String _query=null;
    private String _pathTranslated=null;
    private String _sessionId=null;
    private HttpSession _session=null;
    private String _sessionIdState=__SESSIONID_NOT_CHECKED;
    private ServletIn _in =null;
    private BufferedReader _reader=null;
    private int _inputState=0;
    private Context _context;
    private ArrayList _mergedParameters;

    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param request 
     */
    ServletRequest(Context context,HttpRequest request)
    {
        _context=context;
        _contextPath=context.getContextPath();
        if (_contextPath.length()==1)
            _contextPath="";
        _httpRequest=request;
    }

    /* ------------------------------------------------------------ */
    /** Set servletpath and pathInfo.
     * Called by the Handler before passing a request to a particular
     * holder to split the context path into a servlet path and path info.
     * @param servletPath 
     * @param pathInfo 
     */
    void setPaths(String servletPath,String pathInfo)
    {
        _servletPath=servletPath;
        _pathInfo=pathInfo;
    }
    
    /* ------------------------------------------------------------ */
    /** Set the paths for a RequestDispatcher.forward().
     *
     * @param context 
     * @param servletPath 
     * @param pathInfo 
     * @param query 
     */
    void setForwardPaths(Context context,
                         String servletPath,
                         String pathInfo,
                         String query)
    {
        _context=context;
        _contextPath=context.getContextPath();
        if (_contextPath.length()==1)
            _contextPath="";
        _servletPath=servletPath;
        _pathInfo=pathInfo;
        _query=query;

        if (getContextPath()==null)
            _uri=_servletPath+_pathInfo;
        else
            _uri=getContextPath()+_servletPath+_pathInfo;
    }
    
    /* ------------------------------------------------------------ */
    HttpRequest getHttpRequest()
    {
        return _httpRequest;
    }
    
    /* ------------------------------------------------------------ */
    ServletResponse getServletResponse()
    {
        return _servletResponse;
    }
    
    /* ------------------------------------------------------------ */
    void setServletResponse(ServletResponse response)
    {
        _servletResponse = response;
    }

    /* ------------------------------------------------------------ */
    Context getContext()
    {
        return _context;
    }
    
    /* ------------------------------------------------------------ */
    public Locale getLocale()
    {
        return (Locale)getLocales().nextElement();
    }
    
    /* ------------------------------------------------------------ */
    public Enumeration getLocales()
    {
        List acceptLanguage =
            _httpRequest.getHeader().getValues("Accept-Language");

        // handle no locale
        if (acceptLanguage == null || acceptLanguage.size()==0)
            return
                Collections.enumeration(Collections.singleton(Locale.getDefault()));
        
        
        // sort the list in quality order
        acceptLanguage = HttpFields.qualityList(acceptLanguage);
        
        if (acceptLanguage.size()==0)
            return
                Collections.enumeration(Collections.singleton(Locale.getDefault()));

        // convert to locals
        for (int i=0; i<acceptLanguage.size(); i++)
        {
            String language = (String)acceptLanguage.get(i);
            language=HttpFields.valueParameters(language,null);
            String country = "";
            int dash = language.indexOf("-");
            if (dash > -1)
            {
                country = language.substring(dash + 1).trim();
                language = language.substring(0,dash).trim();
            }
            
            acceptLanguage.set(i,new Locale(language, country));
        }
        
        return Collections.enumeration(acceptLanguage);
    }

    
    /* ------------------------------------------------------------ */
    public String getAuthType()
    {
        Object o=_httpRequest.getAttribute(HttpRequest.__AuthType);
        if (o!=null)
            return o.toString();
        return null;
    }

    /* ------------------------------------------------------------ */
    public boolean isSecure()
    {
        return "https".equals(_httpRequest.getScheme());
    }
    
    /* ------------------------------------------------------------ */
    public Cookie[] getCookies()
    {
        return _httpRequest.getCookies();
    }
    
    /* ------------------------------------------------------------ */
    public long getDateHeader(String name)
    {
        return _httpRequest.getDateField(name);
    }
    
    /* ------------------------------------------------------------ */
    public Enumeration getHeaderNames()
    {
        return Collections.enumeration(_httpRequest.getFieldNames());
    }
    
    /* ------------------------------------------------------------ */
    public String getHeader(String name)
    {
        return _httpRequest.getField(name);
    }
    
    /* ------------------------------------------------------------ */
    public Enumeration getHeaders(String s)
    {
        List list=_httpRequest.getFieldValues(s);
        if (list==null)
            return Collections.enumeration(Collections.EMPTY_LIST);
        return Collections.enumeration(list);
    }
    
    /* ------------------------------------------------------------ */
    public int getIntHeader(String name)
        throws NumberFormatException
    {
        return _httpRequest.getIntField(name);
    }
    
    /* ------------------------------------------------------------ */
    public String getMethod()
    {
        return _httpRequest.getMethod();
    }
    
    /* ------------------------------------------------------------ */
    public String getContextPath()
    {
        return _contextPath;
    }
    
    /* ------------------------------------------------------------ */
    public String getPathInfo()
    {
        return _pathInfo;
    }
    
    /* ------------------------------------------------------------ */
    public String getPathTranslated()
    {
        if (_pathInfo==null || _pathInfo.length()==0)
            return null;
        if (_pathTranslated==null)
        {
            Resource resource =
                _context.getHandler()
                .getHandlerContext()
                .getResourceBase();

            if (resource==null)
                return null;

            try
            {
                resource=resource.addPath(_pathInfo);
                File file = resource.getFile();
                if (file==null)
                    return null;
                _pathTranslated=file.getAbsolutePath();
            }
            catch(Exception e)
            {
                Code.debug(e);
            }
        }
        
        return _pathTranslated;
    }
    
    /* ------------------------------------------------------------ */
    public String getQueryString()
    {
        if (_query==null)
            _query =_httpRequest.getQuery();
        return _query;
    }
    
    /* ------------------------------------------------------------ */
    public String getRemoteUser()
    {
        Object o=_httpRequest.getAttribute(HttpRequest.__AuthUser);
        if (o!=null)
            return o.toString();
        return null;
    }

    /* ------------------------------------------------------------ */
    public boolean isUserInRole(String role)
    {
        return _httpRequest.isUserInRole(role);
    }

    /* ------------------------------------------------------------ */
    public Principal getUserPrincipal()
    {
        return _httpRequest.getUserPrincipal();
    }
    
    /* ------------------------------------------------------------ */
    String setSessionId(String pathInContext)
    {
        _sessionId=null;
        
        // try cookies first
        Cookie[] cookies=_httpRequest.getCookies();
        if (cookies!=null && cookies.length>0)
        {
            for (int i=0;i<cookies.length;i++)
            {
                if (Context.__SessionId.equals(cookies[i].getName()))
                {
                    _sessionId=cookies[i].getValue();
                    _sessionIdState = __SESSIONID_COOKIE;
                    Code.debug("Got Session ",_sessionId," from cookie");
                    break;
                }
            }
        }
            
        // check if there is a url encoded session param.
        int prefix=pathInContext.indexOf(Context.__SessionUrlPrefix);
        if (prefix!=-1)
        {
            String id =
                pathInContext.substring(prefix+Context.__SessionUrlPrefix.length());
            pathInContext=pathInContext.substring(0,prefix);
            Code.debug("Got Session ",id," from URL");
            
            try
            {
                Long.parseLong(id,36);
                if (_sessionId==null)
                {
                    _sessionId=id;
                    _sessionIdState = __SESSIONID_URL;
                }
                else if (!id.equals(_sessionId))
                    Code.warning("Mismatched session IDs");
                
            }
            catch(NumberFormatException e)
            {
                Code.ignore(e);
            }
        }
        
        if (_sessionId == null)
            _sessionIdState = __SESSIONID_NONE;
        
        return pathInContext;
    }
    
    /* ------------------------------------------------------------ */
    public String getRequestedSessionId()
    {
        return _sessionId;
    }
    
    /* ------------------------------------------------------------ */
    public String getRequestURI()
    {
        if (_uri!=null)
            return _uri;
        
        String path=_httpRequest.getPath();

        int prefix=path.indexOf(Context.__SessionUrlPrefix);
        if (prefix!=-1)
            path = path.substring(0,prefix);
        
        return path;
    }
    
    /* ------------------------------------------------------------ */
    public String getServletPath()
    {
        return _servletPath;
    }
    
    /* ------------------------------------------------------------ */
    public HttpSession getSession(boolean create)
    {        
        if (_session != null && _context.isValid(_session))
            return _session;
        
        String id = getRequestedSessionId();
        
        if (id != null)
        {
            _session=_context.getHttpSession(id);
            if (_session == null && !create)
                return null;
        }

        if (_session == null && create)
        {
            _session = _context.newSession();
            Cookie cookie =
                new Cookie(_context.__SessionId,_session.getId());
            String path=getContextPath();
            if (path==null || path.length()==0)
                path="/";
            cookie.setPath(path);
            _servletResponse.getHttpResponse().addSetCookie(cookie,false);
            cookie.setVersion(1);
            _servletResponse.getHttpResponse().addSetCookie(cookie,true); 
        }

        return _session;
    }
    
    /* ------------------------------------------------------------ */
    public HttpSession getSession()
    {
        HttpSession session = getSession(false);
        return (session == null) ? getSession(true) : session;
    }
    
    /* ------------------------------------------------------------ */
    public boolean isRequestedSessionIdValid()
    {
        return _sessionId != null && getSession(false) != null;
    }
    
    /* -------------------------------------------------------------- */
    public boolean isRequestedSessionIdFromCookie()
    {
        return _sessionIdState == __SESSIONID_COOKIE;
    }
    
    /* -------------------------------------------------------------- */
    public boolean isRequestedSessionIdFromURL()
    {
        return _sessionIdState == __SESSIONID_URL;
    }
    
    /* -------------------------------------------------------------- */
    /**
     * @deprecated
     */
    public boolean isRequestedSessionIdFromUrl()
    {
        return isRequestedSessionIdFromURL();
    }
    
    /* -------------------------------------------------------------- */
    public Enumeration getAttributeNames()
    {
        return Collections.enumeration(_httpRequest.getAttributeNames());
    }
    
    /* -------------------------------------------------------------- */
    public Object getAttribute(String name)
    {
        return _httpRequest.getAttribute(name);
    }
    
    /* -------------------------------------------------------------- */
    public void setAttribute(String name, Object value)
    {
        if (name.startsWith("org.alicebot.server.net.http"))
        {
            Code.warning("Servlet attempted update of "+name);
            return;
        }
        _httpRequest.setAttribute(name,value);
    }
    
    /* -------------------------------------------------------------- */
    public void removeAttribute(String name)
    {
        if (name.startsWith("org.alicebot.server.net.http"))
        {
            Code.warning("Servlet attempted update of "+name);
            return;
        }
        _httpRequest.removeAttribute(name);
    }
    
    /* -------------------------------------------------------------- */
    public String getCharacterEncoding()
    {
        return _httpRequest.getCharacterEncoding();
    }
    
    /* -------------------------------------------------------------- */
    public int getContentLength()
    {
        return _httpRequest.getIntField(HttpFields.__ContentLength);
    }
    
    /* -------------------------------------------------------------- */
    public String getContentType()
    {
        return _httpRequest.getField(HttpFields.__ContentType);
    }
    
    /* -------------------------------------------------------------- */
    public ServletInputStream getInputStream()
    {
        if (_inputState!=0 && _inputState!=1)
            throw new IllegalStateException();
        if (_in==null)
            _in = new ServletIn(_httpRequest.getInputStream());  
        _inputState=1;
        return _in;
    }
    
    /* -------------------------------------------------------------- */
    void popParameters()
    {
        _mergedParameters.remove(_mergedParameters.size()-1);
    }
    
    /* -------------------------------------------------------------- */
    void pushParameters(MultiMap parameters)
    {
        if (_mergedParameters==null)
            _mergedParameters=new ArrayList(2);
        _mergedParameters.add(parameters);
    }
    
    /* -------------------------------------------------------------- */
    public String getParameter(String name)
    {
        if (_mergedParameters!=null)
        {
            for (int p=_mergedParameters.size();p-->0;)
            {
                MultiMap params=(MultiMap)_mergedParameters.get(p);
                String param=params.getString(name);
                if (param!=null)
                    return param;
            }
        }
        return _httpRequest.getParameter(name);
    }
    
    /* -------------------------------------------------------------- */
    public Enumeration getParameterNames()
    {
        if (_mergedParameters!=null)
        {
            HashSet set = new HashSet(_httpRequest.getParameterNames());
            
            for (int p=_mergedParameters.size();p-->0;)
            {
                MultiMap params=(MultiMap)_mergedParameters.get(p);
                set.addAll(params.keySet());
            }
            return Collections.enumeration(set);
        }
        
        return Collections.enumeration(_httpRequest.getParameterNames());
    }
    
    /* -------------------------------------------------------------- */
    public String[] getParameterValues(String name)
    {
        List v=null;
        
        if (_mergedParameters!=null)
        {
            for (int p=_mergedParameters.size();v==null && p-->0;)
            {
                MultiMap params=(MultiMap)_mergedParameters.get(p);
                v=params.getValues(name);
            }
        }
        
        if (v==null)
            v=_httpRequest.getParameterValues(name);
        
        if (v==null)
            return null;
        
        String[]a=new String[v.size()];
        return (String[])v.toArray(a);
    }
    
    /* -------------------------------------------------------------- */
    public String getProtocol()
    {
        return _httpRequest.getVersion();
    }
    
    /* -------------------------------------------------------------- */
    public String getScheme()
    {
        return _httpRequest.getScheme();
    }
    
    /* -------------------------------------------------------------- */
    public String getServerName()
    {
        return _httpRequest.getHost();
    }
    
    /* -------------------------------------------------------------- */
    public int getServerPort()
    {
        int port = _httpRequest.getPort();
        return port==0?80:port;
    }
    
    /* -------------------------------------------------------------- */
    public BufferedReader getReader()
    {
        if (_inputState!=0 && _inputState!=2)
            throw new IllegalStateException();
        if (_reader==null)
        {
            try
            {
                _reader=new BufferedReader(new InputStreamReader(getInputStream(),StringUtil.__ISO_8859_1));
            }
            catch(UnsupportedEncodingException e)
            {
                Code.ignore(e);
                _reader=new BufferedReader(new InputStreamReader(getInputStream()));
            }
            _inputState=2;
        }
        return _reader;
    }
    
    /* -------------------------------------------------------------- */
    public String getRemoteAddr()
    {
        return _httpRequest.getRemoteAddr();
    }
    
    /* -------------------------------------------------------------- */
    public String getRemoteHost()
    {
        String remoteHost=null;
        HttpConnection connection = _httpRequest.getConnection();
        if (connection!=null)
        {
            InetAddress addr = connection.getRemoteAddr();
            if (addr!=null)
                remoteHost = addr.getHostName();
        }
        return remoteHost;
    }

    /* -------------------------------------------------------------- */
    public String getRealPath(String path)
    {
        return _context.getRealPath(path);
    }

    /* ------------------------------------------------------------ */
    public RequestDispatcher getRequestDispatcher(String url)
    {
        if (url == null)
            return null;

        if (!url.startsWith("/"))
        {
            String relTo=_servletPath+_pathInfo;
            
            int slash=relTo.lastIndexOf("/");
            relTo=relTo.substring(0,slash);
            
            while(url.startsWith("../"))
            {
                if (relTo.length()==0)
                    return null;
                url=url.substring(3);
                slash=relTo.lastIndexOf("/");
                relTo=relTo.substring(0,slash);
            }

            url=relTo+url;
            
        }
    
        return _context.getRequestDispatcher(url);
    }

    /* ------------------------------------------------------------ */
    public String toString()
    {
        return _httpRequest.toString();
    }
}






