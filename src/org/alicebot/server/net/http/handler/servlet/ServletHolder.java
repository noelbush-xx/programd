// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: ServletHolder.java,v 1.1.1.1 2001/06/17 19:02:37 noelbu Exp $
// ---------------------------------------------------------------------------

package org.alicebot.server.net.http.handler.servlet;

import org.alicebot.server.net.http.util.Code;
import org.alicebot.server.net.http.util.ThreadPool;
import org.alicebot.server.net.http.ContextLoader;
import java.io.IOException;
import java.util.AbstractMap;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import javax.servlet.Servlet;
import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.SingleThreadModel;
import javax.servlet.UnavailableException;


/* --------------------------------------------------------------------- */
/** Servlet Instance and Context Holder.
 * Holds the name, params and some state of a javax.servlet.Servlet
 * instance. It implements the ServletConfig interface.
 * This class will organise the loading of the servlet when needed or
 * requested.
 *
 * @see org.alicebot.server.net.http.handler.servletHandler
 * @version $Id: ServletHolder.java,v 1.1.1.1 2001/06/17 19:02:37 noelbu Exp $
 * @author Greg Wilkins
 */
public class ServletHolder
    extends AbstractMap
    implements Comparable
{
    /* ---------------------------------------------------------------- */
    private ServletHandler _handler;
    private Context _context;
    private boolean _singleThreadModel;

    private Class _servletClass=null;
    private Stack _servlets=new Stack();
    private Servlet _servlet=null;
    private String _name=null;
    private String _className ;
    private Map _initParams ;
    private int _initOrder;
    private boolean _initOnStartup=false;
    private Config _config;
    private Map _roleMap;
    private int _checks;
    private String _path;

    
    /* ---------------------------------------------------------------- */
    /** Construct a Servlet property mostly from the servers config.
     * file.
     * @param handler ServletHandler
     * @param className Servlet class name (fully qualified)
     */
    public ServletHolder(ServletHandler handler,
                         String className)
    {
        _handler=handler;
        _context=_handler.getContext();
        setServletName(className);
        _className=className;
        _name=className;
        _config=new Config();
    }

    /* ---------------------------------------------------------------- */
    /** Constructor. 
     * @param handler 
     * @param className 
     * @param pathName 
     */
    public ServletHolder(ServletHandler handler,
                         String className,
                         String path)
    {
        this(handler,className);
        _path=path;
    }
    
    
    /* ------------------------------------------------------------ */
    public String getServletName()
    {
        return _name;
    }

    /* ------------------------------------------------------------ */
    public void setServletName(String name)
    {
        synchronized(_handler)
        {
            _handler.mapHolder(name,this,_name);
            _name=name;
        }
    }

    /* ------------------------------------------------------------ */
    public String getClassName()
    {
        return _className;
    }

    /* ------------------------------------------------------------ */
    public void setClassName(String className)
    {
        _className = className;
    }

    /* ------------------------------------------------------------ */
    /**
     * @deprecated Use getInitORder()
     */
    public boolean isInitOnStartup()
    {
        return _initOrder!=0 || _initOnStartup;
    }

    /* ------------------------------------------------------------ */
    /** 
     * @deprecated Use setInitOrder(int)
     */
    public void setInitOnStartup(boolean b)
    {
        _initOrder=b?0:-1;
    }
    
    /* ------------------------------------------------------------ */
    public int getInitOrder()
    {
        return _initOrder;
    }

    /* ------------------------------------------------------------ */
    /** Set the initialize order.
     * Holders with order<0, are initialized on use. Those with
     * order>=0 are initialized in increasing order when the handler
     * is started.
     */
    public void setInitOrder(int order)
    {
        _initOnStartup=true;
        _initOrder = order;
    }

    /* ------------------------------------------------------------ */
    /** Comparitor by init order.
     */
    public int compareTo(Object o)
    {
        if (o instanceof ServletHolder)
        {
            ServletHolder sh= (ServletHolder)o;
            if (sh==this)
                return 0;
            if (sh._initOrder<_initOrder)
                return 1;
            if (sh._initOrder>_initOrder)
                return -1;
            return _name.compareTo(sh._name);
        }
        return 1;
    }
    
    
    /* ------------------------------------------------------------ */
    public void initialize()
    {
        try
        {
            getServlet();
        }
        catch(javax.servlet.UnavailableException e)
        {
            Code.warning(e);
            throw new IllegalStateException(e.toString());
        }
    }

    /* ------------------------------------------------------------ */
    synchronized void initializeClass()
        throws UnavailableException
    {
        try
        {
            // XXX - This is horrible - got to find a better way.
            if (getClassName().equals(_handler.getJSPClassName()))
            {                
                ClassLoader jettyLoader=_handler.getHandlerContext().getClassLoader();
                ClassLoader jasperLoader=(ClassLoader)
                    _context.getAttribute("org.apache.tomcat.classloader");
                if (jettyLoader!=null && jasperLoader==null)
                {
                    Code.debug("Fiddle classloader for Jasper: "+jettyLoader);
                    _context.setAttribute("org.apache.tomcat.classloader",
                                          jettyLoader);
                }

                String classpath = getInitParameter("classpath");
                String ctxClasspath =(jettyLoader instanceof ContextLoader)
                    ?((ContextLoader)jettyLoader).getFileClassPath()
                    :_handler.getHandlerContext().getClassPath();
                String tomcatpath=(String)
                    _context.getAttribute("org.apache.tomcat.jsp_classpath");

                if (classpath==null && tomcatpath!=null)
                {
                    classpath=tomcatpath;
                    Code.debug("Fiddle classpath for Jasper: "+classpath);
                    setInitParameter("classpath",classpath);
                }
                
                if ((classpath==null || classpath.length()==0) &&
                    ctxClasspath!=null && ctxClasspath.length()>0)
                {
                    classpath=ctxClasspath;
                    Code.debug("Fiddle classpath for Jasper: "+classpath);
                    setInitParameter("classpath",classpath);
                }            
            }
            
            ClassLoader loader=_context.getHandler().getClassLoader();
            Code.debug("Servlet loader ",loader);
            if (loader==null)
                _servletClass=Class.forName(getClassName());
            else
                _servletClass=loader.loadClass(getClassName());
            Code.debug("Servlet Class ",_servletClass);
            if (!javax.servlet.Servlet.class
                .isAssignableFrom(_servletClass))
                Code.fail("Servlet class "+getClassName()+
                          " is not a javax.servlet.Servlet");
        }
        catch(ClassNotFoundException e)
        {
            Code.debug(e);
            throw new UnavailableException(e.toString());
        }
    }

    /* ---------------------------------------------------------------- */
    /** Destroy.
     */
    public synchronized void destroy()
    {
        // Destroy singleton servlet
        if (_servlet!=null)
            _servlet.destroy();
        _servlet=null;
        _checks=0;
        
        // Destroy stack of servlets
        while (_servlets!=null && _servlets.size()>0)
        {
            Servlet s = (Servlet)_servlets.pop();
            s.destroy();
        }
        _servlets=new Stack();
        _servletClass=null;
    }


    /* ------------------------------------------------------------ */
    /** Get the servlet.
     * The state of the servlet is unknown, specially if using
     * SingleThreadModel
     * @return The servlet
     */
    public synchronized Servlet getServlet()
        throws UnavailableException
    {
        try{
            if (_servletClass==null)
                initializeClass();

            if (_servlet==null)
            {
                Servlet newServlet =
                    newServlet = (Servlet)_servletClass.newInstance();
                newServlet.init(_config);
                _singleThreadModel =
                    newServlet instanceof
                    javax.servlet.SingleThreadModel;
                
                if (_servlet==null && !_singleThreadModel)
                        _servlet=newServlet;
            }
            return _servlet;
        }
        catch(UnavailableException e)
        {
            throw e;
        }
        catch(Exception e)
        {
            Code.warning(e);
            throw new UnavailableException(e.toString());
        }    
    }


    /* ---------------------------------------------------------------- */
    public javax.servlet.ServletContext getServletContext()
    {
        return (javax.servlet.ServletContext)_context;
    }

    /* ------------------------------------------------------------ */
    public void setInitParameter(String param,String value)
    {
        put(param,value);
    }

    /* ---------------------------------------------------------------- */
    /**
     * Gets an initialization parameter of the servlet.
     * @param name the parameter name
     */
    public String getInitParameter(String param)
    {
        if (_initParams==null)
            return null;
        return (String)_initParams.get(param);
    }

    /* ------------------------------------------------------------ */
    public Enumeration getInitParameterNames()
    {
        if (_initParams==null)
            return Collections.enumeration(Collections.EMPTY_LIST);
        return Collections.enumeration(_initParams.keySet());
    }

    /* ------------------------------------------------------------ */
    /** Link a user role.
     * Translate the role name used by a servlet, to the link name
     * used by the container.
     * @param name The role name as used by the servlet
     * @param link The role name as used by the container.
     */
    public synchronized void setUserRoleLink(String name,String link)
    {
        if (_roleMap==null)
            _roleMap=new HashMap();
    }
    
    /* ------------------------------------------------------------ */
    /** get a user role link.
     * @param name The name of the role
     * @return The name as translated by the link. If no link exists,
     * the name is returned.
     */
    public String getUserRoleLink(String name)
    {
        if (_roleMap==null)
            return name;
        String link=(String)_roleMap.get(name);
        return (link==null)?name:link;
    }
    
    /* --------------------------------------------------------------- */
    /** Service a request with this servlet.
     */
    public void handle(ServletRequest request,
                       ServletResponse response)
        throws ServletException,
               UnavailableException,
               IOException
    {
        if (_servletClass==null)
            throw new UnavailableException("Servlet class not initialized");
        
        Servlet useServlet=null;

        // reference pool to protect from reloads
        Stack pool=_servlets;

        if (_singleThreadModel)
        {
            // try getting a servlet from the pool of servlets
            try{useServlet = (Servlet)pool.pop();}
            catch(EmptyStackException e)
            {
                // Create a new one for the pool
                try
                {
                    useServlet = (Servlet) _servletClass.newInstance();
                    useServlet.init(_config);
                }
                catch(Exception e2)
                {
                    Code.warning(e2);
                    useServlet = null;
                }
            }
        }
        else
        {
            // Is the singleton instance ready?
            // Double null check sync problem is reduced by hit
            // counter. First 10 hits always  
            if (_servlet == null || _checks<2)
            {
                // no so get a lock on the class
                synchronized(this)
                {
                    // check if still not ready
                    if (_servlet == null)
                    {
                        // no so build it
                        try
                        {
                            useServlet =
                                (Servlet) _servletClass.newInstance();
                            useServlet.init(_config);
                            _servlet = useServlet;

                            _singleThreadModel =
                                _servlet instanceof
                                javax.servlet.SingleThreadModel;
                            if (_singleThreadModel)
                                _servlet=null;
                        }
                        catch(UnavailableException e)
                        {
                            throw e;
                        }
                        catch(Exception e)
                        {
                            Code.warning(e);
                            useServlet = _servlet = null;
                        }
                    }
                    else
                        // yes so use it.
                        useServlet = _servlet;
                    _checks++;
                }
            }
            else
                // yes so use it.
                useServlet = _servlet;
        }

        // Check that we got one in the end
        if (useServlet==null)
            throw new UnavailableException("Could not construct servlet");

        // Service the request
        try
        {
            // Handle aliased path
            if (_path!=null)
            {
                request.setAttribute("javax.servlet.include.request_uri",
                                     request.getContextPath()+
                                     (_path.startsWith("/")?"":"/")+
                                     _path);
                request.setAttribute("javax.servlet.include.servlet_path",_path);
            }
            
            useServlet.service(request,response);
            response.flushBuffer();
        }
        catch(UnavailableException e)
        {
            if (_singleThreadModel && useServlet!=null)
                useServlet.destroy();
            else
                destroy();
            useServlet=null;
            throw e;
        }
        finally
        {
            // Return to singleThreaded pool
            if (_singleThreadModel && useServlet!=null)
                pool.push(useServlet);
        }
    }

    /* ------------------------------------------------------------ */
    /** Map method.
     * ServletHolder implements the Map interface as a
     * configuration conveniance. The methods are mapped to the
     * servlet properties.
     * @return The entrySet of the initParameter map
     */
    public synchronized Set entrySet()
    {
        if (_initParams==null)
            _initParams=new HashMap(3);
        return _initParams.entrySet();
    }

    /* ------------------------------------------------------------ */
    /** Map method.
     * ServletHolder implements the Map interface as a
     * configuration conveniance. The methods are mapped to the
     * servlet properties.
     */
    public synchronized Object put(Object name,Object value)
    {
        if (_initParams==null)
            _initParams=new HashMap(3);
        return _initParams.put(name,value);
    }

    /* ------------------------------------------------------------ */
    /** Get the name of the Servlet.
     * @return Servlet name
     */
    public String toString()
    {
        return _name;
    }
    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    class Config implements ServletConfig
    {
        /* -------------------------------------------------------- */
        public String getServletName()
        {
            return ServletHolder.this.getServletName();
        }
        
        /* -------------------------------------------------------- */
        public javax.servlet.ServletContext getServletContext()
        {
            return (javax.servlet.ServletContext)_context;
        }

        /* -------------------------------------------------------- */
        /**
         * Gets an initialization parameter of the servlet.
         * @param name the parameter name
         */
        public String getInitParameter(String param)
        {
            return ServletHolder.this.getInitParameter(param);
        }
    
        /* -------------------------------------------------------- */
        public Enumeration getInitParameterNames()
        {
            return ServletHolder.this.getInitParameterNames();
        }
    }
}





