// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: Context.java,v 1.1.1.1 2001/06/17 19:02:32 noelbu Exp $
// ---------------------------------------------------------------------------

package org.alicebot.server.net.http.handler.servlet;

import org.alicebot.server.net.http.HandlerContext;
import org.alicebot.server.net.http.Version;
import org.alicebot.server.net.http.util.Code;
import org.alicebot.server.net.http.util.Frame;
import org.alicebot.server.net.http.util.Log;
import org.alicebot.server.net.http.util.LogSink;
import org.alicebot.server.net.http.util.Resource;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.ConcurrentModificationException;
import javax.servlet.RequestDispatcher;
import javax.servlet.Servlet;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;


/* --------------------------------------------------------------------- */
/** Jetty Servlet Context.
 *
 * @version $Id: Context.java,v 1.1.1.1 2001/06/17 19:02:32 noelbu Exp $
 * @author Greg Wilkins
 */
public class Context implements ServletContext, HttpSessionContext
{
    /* ------------------------------------------------------------ */
    private static final String
        CONTEXT_LOG="org.alicebot.server.net.http.handler.servlet.Context.LogSink";
    
    /* ------------------------------------------------------------ */
    private ServletHandler _handler;
    private HandlerContext _handlerContext;
    private LogSink _logSink;

    /* ------------------------------------------------------------ */
    ServletHandler getHandler(){return _handler;}
    HandlerContext getHandlerContext(){return _handlerContext;}
    void setHandlerContext(HandlerContext hc)
    {
        _handlerContext=hc;
        if(_handlerContext!=null)
            _logSink=(LogSink)_handlerContext.getAttribute(CONTEXT_LOG);
    }

    /* ------------------------------------------------------------ */
    /** Constructor.
     * @param handler
     */
    Context(ServletHandler handler)
    {
        _handler=handler;
        _handlerContext=_handler.getHandlerContext();
        if(_handlerContext!=null)
            _logSink=(LogSink)_handlerContext.getAttribute(CONTEXT_LOG);
    }

    /* ------------------------------------------------------------ */
    ServletHandler getServletHandler()
    {
        return _handler;
    }

    /* ------------------------------------------------------------ */
    public String getContextPath()
    {
        return _handlerContext.getContextPath();
    }

    /* ------------------------------------------------------------ */
    public ServletContext getContext(String uri)
    {        
        HandlerContext context=
            _handlerContext;

        ServletHandler handler=context.getHttpServer()
            .findServletHandler(uri,context.getHosts());

        if (handler!=null)
            return handler.getContext();
        return null;
    }

    /* ------------------------------------------------------------ */
    public int getMajorVersion()
    {
        return 2;
    }

    /* ------------------------------------------------------------ */
    public int getMinorVersion()
    {
        return 2;
    }

    /* ------------------------------------------------------------ */
    public String getMimeType(String file)
    {
        return _handlerContext.getMimeByExtension(file);
    }

    /* ------------------------------------------------------------ */
    /** Get a Resource.
     * If no resource is found, resource aliases are tried.
     * @param uriInContext 
     * @return 
     * @exception MalformedURLException 
     */
    public URL getResource(String uriInContext)
        throws MalformedURLException
    {
        Resource resourceBase=_handlerContext.getResourceBase();
        uriInContext=Resource.canonicalPath(uriInContext);
        if (resourceBase==null || uriInContext==null)
            return null;

        try{
            Resource resource = resourceBase.addPath(uriInContext);
            if (resource.exists())
                return resource.getURL();

            String aliasedUri=_handlerContext.getResourceAlias(uriInContext);
            if (aliasedUri!=null)
                return getResource(aliasedUri);
        }
        catch(MalformedURLException e)
        {
            throw e;
        }
        catch(IOException e)
        {
            Code.warning(e);
        }
        return null;
    }

    /* ------------------------------------------------------------ */
    public InputStream getResourceAsStream(String uriInContext)
    {
        try
        {
            URL url = getResource(uriInContext);
            if (url!=null)
                return url.openStream();
        }
        catch(MalformedURLException e)
        {
            Code.ignore(e);
        }
        catch(IOException e)
        {
            Code.ignore(e);
        }
        return null;
    }

    /* ------------------------------------------------------------ */
    public RequestDispatcher getRequestDispatcher(String uriInContext)
    {
        
        if (uriInContext == null || !uriInContext.startsWith("/"))
            return null;

        try
        {
            String pathInContext=uriInContext;
            String query=null;
            int q=0;
            if ((q=pathInContext.indexOf("?"))>0)
            {
                pathInContext=uriInContext.substring(0,q);
                query=uriInContext.substring(q+1);
            }

            return new Dispatcher(this,pathInContext,query);
        }
        catch(Exception e)
        {
            Code.ignore(e);
            return null;
        }
    }

    /* ------------------------------------------------------------ */
    public RequestDispatcher getNamedDispatcher(String name)
    {
        if (name == null || name.length()==0)
            return null;

        try
        {
            return new Dispatcher(this,name);
        }
        catch(Exception e)
        {
            Code.ignore(e);
            return null;
        }
    }

    /* ------------------------------------------------------------ */
    /**
     * @deprecated 
     */
    public Servlet getServlet(String name)
    {
        return null;
    }

    /* ------------------------------------------------------------ */
    /**
     * @deprecated 
     */
    public Enumeration getServlets()
    {
        return Collections.enumeration(Collections.EMPTY_LIST);
    }

    /* ------------------------------------------------------------ */
    /**
     * @deprecated 
     */
    public Enumeration getServletNames()
    {
        return Collections.enumeration(Collections.EMPTY_LIST);
    }

    /* ------------------------------------------------------------ */
    /** Servlet Log.
     * Log message to servlet log. Use either the system log or a
     * LogSinkset via the context attribute
     * org.alicebot.server.net.http.handler.servlet.Context.LogSink
     * @param msg 
     */
    public void log(String msg)
    {
        if (_logSink!=null)
            _logSink.log(Log.EVENT,msg,new
                Frame(2),System.currentTimeMillis());
        else
            Log.message(Log.EVENT,msg,new Frame(2));
    }

    /* ------------------------------------------------------------ */
    public void log(Exception e, String msg)
    {
        Code.warning(msg,e);
    }

    /* ------------------------------------------------------------ */
    public void log(String msg, Throwable th)
    {
        Code.warning(msg,th);
    }

    /* ------------------------------------------------------------ */
    public String getRealPath(String path)
    {
        if(Code.debug())
            Code.debug("getRealPath of ",path," in ",this);

        Resource resourceBase=_handlerContext.getResourceBase();
        if (resourceBase==null )
            return null;

        try{
            Resource resource = resourceBase.addPath(path);
            File file = resource.getFile();

            return (file==null)
                ?"null"
                :(file.getAbsolutePath());
        }
        catch(IOException e)
        {
            Code.warning(e);
            return null;
        }
    }

    /* ------------------------------------------------------------ */
    public String getServerInfo()
    {
        return Version.__Version;
    }

    /* ------------------------------------------------------------ */
    /** Get context init parameter.
     * Delegated to HandlerContext.
     * Init Parameters differ from attributes as they can only
     * have string values, servlets cannot set them and they do
     * not have a package scoped name space.
     * @param param param name
     * @return param value or null
     */
    public String getInitParameter(String param)
    {
        return _handlerContext.getInitParameter(param);
    }

    /* ------------------------------------------------------------ */
    /** Get context init parameter names.
     * Delegated to HandlerContext.
     * @return Enumeration of names
     */
    public Enumeration getInitParameterNames()
    {
        return _handlerContext.getInitParameterNames();
    }

    /* ------------------------------------------------------------ */
    /** Get context attribute.
     * Delegated to HandlerContext.
     * @param name attribute name.
     * @return attribute
     */
    public Object getAttribute(String name)
    {
        if ("javax.servlet.context.tempdir".equals(name))
        {
            // Initialize temporary directory
            File tempDir=(File)_handlerContext
                .getAttribute("javax.servlet.context.tempdir");
            if (tempDir==null)
            {
                try{
                    tempDir=File.createTempFile("JettyContext",null);
                    if (tempDir.exists())
                        tempDir.delete();
                    tempDir.mkdir();
                    tempDir.deleteOnExit();
                    _handlerContext
                        .setAttribute("javax.servlet.context.tempdir",
                                      tempDir);
                }
                catch(Exception e)
                {
                    Code.warning(e);
                }
            }
            Code.debug("TempDir=",tempDir);
        }

        return _handlerContext.getAttribute(name);
    }

    /* ------------------------------------------------------------ */
    /** Get context attribute names.
     * Delegated to HandlerContext.
     */
    public Enumeration getAttributeNames()
    {
        return _handlerContext.getAttributeNames();
    }

    /* ------------------------------------------------------------ */
    /** Set context attribute names.
     * Delegated to HandlerContext.
     * @param name attribute name.
     * @param value attribute value
     */
    public void setAttribute(String name, Object value)
    {
        if (name.startsWith("org.alicebot.server.net.http"))
        {
            Code.warning("Servlet attempted update of "+name);
            return;
        }
        _handlerContext.setAttribute(name,value);
    }

    /* ------------------------------------------------------------ */
    /** Remove context attribute.
     * Delegated to HandlerContext.
     * @param name attribute name.
     */
    public void removeAttribute(String name)
    {
        if (name.startsWith("org.alicebot.server.net.http"))
        {
            Code.warning("Servlet attempted update of "+name);
            return;
        }
        _handlerContext.removeAttribute(name);
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    public final static String __SessionId  = "JSESSIONID";
    public final static String __SessionUrlPrefix = ";"+__SessionId+"=";
    public final static int __distantFuture = 60*60*24*7*52*20;
    private static long __nextSessionId = System.currentTimeMillis();

    // Setting of max inactive interval for new sessions
    // -1 means no timeout
    private int _defaultMaxIdleTime = -1;
    private SessionScavenger _scavenger = null;
    private Map _sessions = new HashMap();

    /* ------------------------------------------------------------ */
    /**
     * @deprecated From HttpSessionContext
     */
    public Enumeration getIds()
    {
        return Collections.enumeration(Collections.EMPTY_LIST);
    }

    /* ------------------------------------------------------------ */
    /**
     * @deprecated From HttpSessionContext
     */
    public HttpSession getSession(String id)
    {
        return null;
    }

    /* ------------------------------------------------------------ */
    /**
     * @param id
     * @return
     */
    HttpSession getHttpSession(String id)
    {
        HttpSession s = (HttpSession)_sessions.get(id);
        return s;
    }

    /* ------------------------------------------------------------ */
    public synchronized HttpSession newSession()
    {
        HttpSession session = new Session();
        session.setMaxInactiveInterval(_defaultMaxIdleTime);
        _sessions.put(session.getId(),session);
        return session;
    }

    /* ------------------------------------------------------------ */
    public static void access(HttpSession session)
    {
        ((Session)session).accessed();
    }

    /* ------------------------------------------------------------ */
    public static boolean isValid(HttpSession session)
    {
        return !(((Session)session).invalid);
    }

    /* -------------------------------------------------------------- */
    /** Set the default session timeout.
     *  @param  default The timeout in minutes
     */
    public synchronized void setSessionTimeout(int timeoutMinutes)
    {
        _defaultMaxIdleTime = timeoutMinutes*60;;

        // Start the session scavenger if we haven't already
        if (_scavenger == null)
            _scavenger = new SessionScavenger();
    }

    /* -------------------------------------------------------------- */
    /** Find sessions that have timed out and invalidate them.
     *  This runs in the SessionScavenger thread.
     */
    private void scavenge()
    {
        long now = System.currentTimeMillis();

        // Since Hashtable enumeration is not safe over deletes,
        // we build a list of stale sessions, then go back and invalidate them
        ArrayList staleSessions = null;

        // For each session
        try
        {
            for (Iterator i = _sessions.values().iterator(); i.hasNext(); )
            {
                Session session = (Session)i.next();
                long idleTime = session.maxIdleTime;
                if (idleTime > 0 && session.accessed + idleTime < now) {
                    // Found a stale session, add it to the list
                    if (staleSessions == null)
                        staleSessions = new ArrayList(5);
                    staleSessions.add(session);
                }
            }
        }
        catch(ConcurrentModificationException e)
        {
            Code.ignore(e);
            // Oops something changed while we were looking.
            // Lock the context and try again.
            // Set our priority high while we have the sessions locked
            int oldPriority = Thread.currentThread().getPriority();
            Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
            try
            {
                synchronized(this)
                {
                    staleSessions=null;
                    scavenge();
                }
            }
            finally {Thread.currentThread().setPriority(oldPriority);}
        }

        // Remove the stale sessions
        if (staleSessions != null)
        {
            for (int i = staleSessions.size() - 1; i >= 0; --i) {
                ((Session)staleSessions.get(i)).invalidate();
            }
        }
    }

    /* ------------------------------------------------------------ */
    public String toString()
    {
        return "Servlet"+_handlerContext.toString();
    }
    
    /* ------------------------------------------------------------ */
    // how often to check - XXX - make this configurable
    final static int scavengeDelay = 30000;

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* -------------------------------------------------------------- */
    /** SessionScavenger is a background thread that kills off old sessions */
    class SessionScavenger extends Thread
    {
        public void run() {
            while (true) {
                try {
                    sleep(scavengeDelay);
                } catch (InterruptedException ex) {}
                Context.this.scavenge();
            }
        }

        SessionScavenger() {
            super("SessionScavenger");
            setDaemon(true);
            this.start();
        }

    }   // SessionScavenger


    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    class Session implements HttpSession
    {
        HashMap _values = new HashMap(11);
        boolean invalid=false;
        boolean newSession=true;
        long created=System.currentTimeMillis();
        long accessed=created;
        long maxIdleTime = -1;
        String id=null;

        /* ------------------------------------------------------------- */
        Session()
        {
            synchronized(org.alicebot.server.net.http.handler.servlet.Context.class)
            {
                do
                {
                    // XXX This needs to be much better!
                    long idtmp = __nextSessionId;
                    __nextSessionId+=created%4096;
                    long newId=idtmp ^(created<<8);
                    if (newId<0)newId=-newId;
                    this.id=Long.toString(newId,30+(int)(created%7));
                }
                while (_sessions.containsKey(this.id));
            }
            if (_defaultMaxIdleTime>=0)
                maxIdleTime=_defaultMaxIdleTime*1000;
        }

        /* ------------------------------------------------------------- */
        void accessed()
        {
            newSession=false;
            accessed=System.currentTimeMillis();
        }

        /* ------------------------------------------------------------- */
        public String getId()
            throws IllegalStateException
        {
            if (invalid) throw new IllegalStateException();
            return id;
        }

        /* ------------------------------------------------------------- */
        public long getCreationTime()
            throws IllegalStateException
        {
            if (invalid) throw new IllegalStateException();
            return created;
        }

        /* ------------------------------------------------------------- */
        public long getLastAccessedTime()
            throws IllegalStateException
        {
            if (invalid) throw new IllegalStateException();
            return accessed;
        }

        /* ------------------------------------------------------------- */
        public int getMaxInactiveInterval()
        {
            if (invalid) throw new IllegalStateException();
            return (int)(maxIdleTime / 1000);
        }

        /* ------------------------------------------------------------- */
        /**
         * @deprecated
         */
        public HttpSessionContext getSessionContext()
            throws IllegalStateException
        {
            if (invalid) throw new IllegalStateException();
            return Context.this;
        }

        /* ------------------------------------------------------------- */
        public void setMaxInactiveInterval(int i)
        {
            maxIdleTime = (long)i * 1000;
        }

        /* ------------------------------------------------------------- */
        public synchronized void invalidate()
            throws IllegalStateException
        {
            if (invalid) throw new IllegalStateException();

            Iterator iter = _values.keySet().iterator();
            while (iter.hasNext())
            {
                String key = (String)iter.next();
                Object value = _values.get(key);
                iter.remove();
                unbindValue(key, value);
            }
            synchronized (Context.this)
            {
                Context.this._sessions.remove(id);
            }
            invalid=true;
        }

        /* ------------------------------------------------------------- */
        public boolean isNew()
            throws IllegalStateException
        {
            if (invalid) throw new IllegalStateException();
            return newSession;
        }


        /* ------------------------------------------------------------ */
        public Object getAttribute(String name)
        {
            if (invalid) throw new IllegalStateException();
            return _values.get(name);
        }

        /* ------------------------------------------------------------ */
        public Enumeration getAttributeNames()
        {
            if (invalid) throw new IllegalStateException();
            return Collections.enumeration(_values.keySet());
        }

        /* ------------------------------------------------------------ */
        public void setAttribute(String name, Object value)
        {
            if (invalid) throw new IllegalStateException();
            Object oldValue = _values.put(name,value);

            if (value != oldValue)
            {
                unbindValue(name, oldValue);
                bindValue(name, value);
            }
        }

        /* ------------------------------------------------------------ */
        public void removeAttribute(String name)
        {
            if (invalid) throw new IllegalStateException();
            Object value=_values.remove(name);
            unbindValue(name, value);
        }

        /* ------------------------------------------------------------- */
        /**
         * @deprecated 	As of Version 2.2, this method is
         * 		replaced by {@link #getAttribute}
         */
        public Object getValue(String name)
            throws IllegalStateException
        {
            return getAttribute(name);
        }

        /* ------------------------------------------------------------- */
        /**
         * @deprecated 	As of Version 2.2, this method is
         * 		replaced by {@link #getAttributeNames}
         */
        public synchronized String[] getValueNames()
            throws IllegalStateException
        {
            if (invalid) throw new IllegalStateException();
            String[] a = new String[_values.size()];
            return (String[])_values.keySet().toArray(a);
        }

        /* ------------------------------------------------------------- */
        /**
         * @deprecated 	As of Version 2.2, this method is
         * 		replaced by {@link #setAttribute}
         */
        public void putValue(java.lang.String name,
                             java.lang.Object value)
            throws IllegalStateException
        {
            setAttribute(name,value);
        }

        /* ------------------------------------------------------------- */
        /**
         * @deprecated 	As of Version 2.2, this method is
         * 		replaced by {@link #removeAttribute}
         */
        public void removeValue(java.lang.String name)
            throws IllegalStateException
        {
            removeAttribute(name);
        }

        /* ------------------------------------------------------------- */
        /** If value implements HttpSessionBindingListener, call valueBound() */
        private void bindValue(java.lang.String name, Object value)
        {
            if (value!=null && value instanceof HttpSessionBindingListener)
                ((HttpSessionBindingListener)value)
                    .valueBound(new HttpSessionBindingEvent(this,name));
        }

        /* ------------------------------------------------------------- */
        /** If value implements HttpSessionBindingListener, call valueUnbound() */
        private void unbindValue(java.lang.String name, Object value)
        {
            if (value!=null && value instanceof HttpSessionBindingListener)
                ((HttpSessionBindingListener)value)
                    .valueUnbound(new HttpSessionBindingEvent(this,name));
        }
    }
}
