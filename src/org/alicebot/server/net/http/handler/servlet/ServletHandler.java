// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: ServletHandler.java,v 1.1.1.1 2001/06/17 19:02:35 noelbu Exp $
// ---------------------------------------------------------------------------

package org.alicebot.server.net.http.handler.servlet;

import org.alicebot.server.net.http.ContextLoader;
import org.alicebot.server.net.http.handler.NullHandler;
import org.alicebot.server.net.http.HandlerContext;
import org.alicebot.server.net.http.HttpException;
import org.alicebot.server.net.http.HttpRequest;
import org.alicebot.server.net.http.HttpResponse;
import org.alicebot.server.net.http.PathMap;
import org.alicebot.server.net.http.util.Code;
import org.alicebot.server.net.http.util.IO;
import org.alicebot.server.net.http.util.Log;
import org.alicebot.server.net.http.util.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.TreeSet;
import java.util.Iterator;
import java.util.StringTokenizer;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpSession;
import javax.servlet.ServletException;
import javax.servlet.UnavailableException;


/* --------------------------------------------------------------------- */
/** Servlet HttpHandler.
 * This handler maps requests to servlets that implement the
 * javax.servlet.http.HttpServlet API.
 *
 * @version $Id: ServletHandler.java,v 1.1.1.1 2001/06/17 19:02:35 noelbu Exp $
 * @author Greg Wilkins
 */
public class ServletHandler extends NullHandler 
{
    /* ------------------------------------------------------------ */
    private final String __JSP_SERVLET="org.apache.jasper.servlet.JspServlet";
    
    /* ------------------------------------------------------------ */
    private PathMap _servletMap=new PathMap();
    
    private Map _nameMap=new HashMap();
    private Context _context;
    private ClassLoader _loader;
    private String _dynamicServletPathSpec;
    private Map _dynamicInitParams ;

    /* ------------------------------------------------------------ */
    /** Constructor. 
     */
    public ServletHandler()
    {
        _context = new Context(this);
    }
    
    /* ------------------------------------------------------------ */
    public Context getContext()
    {
        return _context;
    }

    /* ------------------------------------------------------------ */
    public PathMap getServletMap()
    {
        return _servletMap;
    }
    
    /* ------------------------------------------------------------ */
    public boolean isAutoReload()
    {
        return false;
    }
    
    /* ------------------------------------------------------------ */
    public String getDynamicServletPathSpec()
    {
        return _dynamicServletPathSpec;
    }

    /* ------------------------------------------------------------ */
    /** Set the dynamic servlet path.
     * If set, the ServletHandler will dynamically load servlet
     * classes that have their class names as the path info after the
     * set path sepcification.
     * @param dynamicServletPathSpec The path within the context at which
     * dynamic servlets are launched. eg /servlet/*
     */
    public void setDynamicServletPathSpec(String dynamicServletPathSpec)
    {
        if (dynamicServletPathSpec!=null &&
            !dynamicServletPathSpec.equals("/") &&
            !dynamicServletPathSpec.endsWith("/*"))
            throw new IllegalArgumentException("dynamicServletPathSpec must end with /*");
            
        _dynamicServletPathSpec=dynamicServletPathSpec;
    }
    
    /* ------------------------------------------------------------ */
    public Map getDynamicInitParams()
    {
        return _dynamicInitParams;
    }
    
    /* ------------------------------------------------------------ */
    /** Set dynamic servlet initial parameters.
     * @param initParams Map passed as initParams to newly created
     * dynamic servlets.
     */
    public void setDynamicInitParams(Map initParams)
    {
        _dynamicInitParams = initParams;
    }
    
    /* ------------------------------------------------------------ */
    /** Not Supported. 
     * @param autoReload 
     */
    public void setAutoReload(boolean autoReload)
    {
        if (autoReload==true)
            Code.warning("AutoReload is no longer supported!\n"+
                         "It may be resurrected once the URL libraries fully support lastModified");
    }
    
    /* ------------------------------------------------------------ */
    public ClassLoader getClassLoader()
    {
        return _loader;
    }
    
    /* ----------------------------------------------------------------- */
    public synchronized void start()
    {        
        _context.setHandlerContext(getHandlerContext());
        
        // Initialize classloader
        _loader=getHandlerContext().getClassLoader();
        
        // Sort and Initialize servlets
        TreeSet sorted=new TreeSet(_servletMap.values());
        
        Iterator i = sorted.iterator();
        while (i.hasNext())
        {
            ServletHolder holder = (ServletHolder)i.next();
            
            if (holder.isInitOnStartup())
                holder.initialize();
            else
            {
                try
                {
                    holder.initializeClass();
                }
                catch(UnavailableException e)
                {
                    Code.warning(e);
                }
            }
        }
        
        super.start();
    }   
    
    /* ----------------------------------------------------------------- */
    public synchronized void stop()
    {
        _loader=null;
        // Stop servlets
        Iterator i = _servletMap.values().iterator();
        while (i.hasNext())
        {
            ServletHolder holder = (ServletHolder)i.next();
            holder.destroy();
        }
        super.stop();
    }
    
    
    /* ------------------------------------------------------------ */
    /** 
     * @param path 
     * @param servletClass 
     */
    public ServletHolder addServlet(String name,
                                    String pathSpec,
                                    String servletClass)
    {
        ServletHolder holder = addServlet(pathSpec,servletClass);
        holder.setServletName(name);
        return holder;
    }
    
    /* ------------------------------------------------------------ */
    /** 
     * @param path 
     * @param servletClass 
     */
    public ServletHolder addServlet(String pathSpec,
                                    String servletClass)
    {
        try
        {
            ServletHolder holder =
                newServletHolder(servletClass);
            addHolder(pathSpec,holder);
            
            return holder;
        }
        catch(Exception e)
        {
            Code.warning(e);
            throw new IllegalArgumentException(e.toString());
        }
    }

    /* ------------------------------------------------------------ */
    /** Get servlet by name.
     * @param name 
     * @return 
     */
    public ServletHolder getServletHolder(String name)
    {
        return (ServletHolder)_nameMap.get(name);
    }
    
    /* ------------------------------------------------------------ */
    public void addHolder(String pathSpec, ServletHolder holder)
    {
        try
        {
            if (isStarted())
                holder.initializeClass();
            _servletMap.put(pathSpec,holder);
        }
        catch(UnavailableException e)
        {
            Code.warning(e);
        }
    }
    
    
    /* ----------------------------------------------------------------- */
    /**
     * @param contextPath 
     * @param pathInContext 
     * @param httpRequest 
     * @param httpResponse 
     * @exception IOException 
     */
    public void handle(String pathInContext,
                       HttpRequest httpRequest,
                       HttpResponse httpResponse)
         throws IOException
    {
        try
        {            
            // Build servlet request and response
            ServletRequest request =
                new ServletRequest(_context,httpRequest);
            ServletResponse response =
                new ServletResponse(request,httpResponse);

            // Check session stuff
            pathInContext=request.setSessionId(pathInContext);
            HttpSession session=null;
            if ((session=request.getSession(false))!=null)
                Context.access(session);

            // handle
            handle(pathInContext,request,response);
        }
        catch(Exception e)
        {
            Throwable th=e;
            if (e instanceof ServletException)
            {
                if (((ServletException)e).getRootCause()!=null)
                {
                    Code.debug("Extracting root cause from ",e);
                    th=((ServletException)e).getRootCause();
                }
            }
            
            if (Code.debug())
            {
                Code.warning(th);
                Code.debug(httpRequest);
            }
            
            if (th instanceof HttpException)
                throw (HttpException)th;
            if (th instanceof IOException)
                throw (IOException)th;
            if (!(th instanceof UnavailableException) && !Code.debug())
                Code.warning(th.toString());
            httpResponse.sendError(503,th);
        }
        catch(Error e)
        {
            if (Code.debug())
            {
                Code.warning(e);
                Code.debug(httpRequest);
            }
            httpResponse.sendError(503,e);
        }
    }

    
    /* ------------------------------------------------------------ */
    /** ServletHolder matching path.
     * In a separate method so that dynamic servlet loading can be
     * implemented by derived handlers.
     * @param pathInContext Path within context.
     * @return PathMap Entries pathspec to ServletHolder
     */
    public Map.Entry getHolderEntry(String pathInContext)
    {
        Map.Entry entry =_servletMap.getMatch(pathInContext);

        String servletClass=null;
        if (_dynamicServletPathSpec!=null)
            servletClass=PathMap.pathInfo(_dynamicServletPathSpec,pathInContext);
        
        // Do we have a match and no chance of a new
        // dynamci servlet
        if (entry!=null && servletClass==null)
            return entry;
        
        // If it could be a dynamic servlet
        if (servletClass!=null && servletClass.length()>2 &&
            (entry==null||!PathMap.match(_dynamicServletPathSpec,(String)entry.getKey())))
        {
            try
            {
                // OK lets look for a dynamic servlet.
                String path=pathInContext;
                Code.debug("looking for ",servletClass," in ",
                           getHandlerContext().getClassPath());
                
                // remove prefix
                servletClass=servletClass.substring(1);
                
                // remove suffix
                int slash=servletClass.indexOf("/");
                if (slash>=0)
                    servletClass=servletClass.substring(0,slash);            
                if (servletClass.endsWith(".class"))
                    servletClass=servletClass.substring(0,servletClass.length()-6);
                
                // work out the actual servlet path
                if ("/".equals(_dynamicServletPathSpec))
                    path="/"+servletClass;
                else
                    path=PathMap.pathMatch(_dynamicServletPathSpec,path)+"/"+servletClass;
                
                Code.debug("Dynamic path=",path);
                
                // make a holder
                ServletHolder holder=newServletHolder(servletClass);
                Map params=getDynamicInitParams();
                if (params!=null)
                    holder.putAll(params);
                holder.getServlet();
                
                Log.event("Dynamic load '"+servletClass+"' at "+path);
                addHolder(path+"/*",holder);
                addHolder(path+".class/*",holder);

                entry=_servletMap.getMatch(pathInContext);
            }
            catch(Exception e)
            {
                Code.ignore(e);
            }
        }
        
        return entry;
    
    }
    

    /* ------------------------------------------------------------ */
    /** 
     * @param pathInContext 
     * @param request 
     * @param response 
     * @exception IOException 
     * @exception ServletException 
     * @exception UnavailableException 
     */
    void handle(String pathInContext,
                ServletRequest request,
                ServletResponse response)
        throws IOException, ServletException, UnavailableException
    {
        Code.debug("Looking for servlet at ",pathInContext);
        HttpResponse httpResponse=response.getHttpResponse();

        Map.Entry entry=getHolderEntry(pathInContext);
        
        if (entry!=null)
        {
            String servletPathSpec=(String)entry.getKey();
            ServletHolder holder = (ServletHolder)entry.getValue();
        
            Code.debug("Pass request to servlet at ",entry);
            request.setPaths(PathMap.pathMatch(servletPathSpec,
                                               pathInContext),
                             PathMap.pathInfo(servletPathSpec,
                                              pathInContext));
            
            // service request
            holder.handle(request,response);
            response.setOutputState(0);
            Code.debug("Handled by ",entry);
            if (!httpResponse.isCommitted())
                httpResponse.commit();  
        }
    }
        
    /* ------------------------------------------------------------ */
    public ServletHolder newServletHolder(String servletClass)
        throws javax.servlet.UnavailableException,
               ClassNotFoundException
    {
        return new ServletHolder(this,servletClass);
    }

    /* ------------------------------------------------------------ */
    public ServletHolder newServletHolder(String servletClass, String path)
        throws javax.servlet.UnavailableException,
               ClassNotFoundException
    {
        return new ServletHolder(this,servletClass,path);
    }
    
    /* ------------------------------------------------------------ */
    /** Destroy Handler.
     * Destroy all servlets
     */
    public synchronized void destroy()
    {
        Iterator i = _servletMap.values().iterator();
        while (i.hasNext())
        {
            ServletHolder holder = (ServletHolder)i.next();
            holder.destroy();
        }
    }


    /* ------------------------------------------------------------ */
    void mapHolder(String name,ServletHolder holder, String oldName)
    {
        synchronized(_nameMap)
        {
            if (oldName!=null)
                _nameMap.remove(oldName);
            _nameMap.put(name,holder);
        }
    }

    /* ------------------------------------------------------------ */
    public String getJSPClassName()
    {
        return __JSP_SERVLET;
    }
    
}
