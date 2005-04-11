package org.aitools.programd.server.jettyinterface;

import org.aitools.programd.Core;
import org.aitools.programd.server.ServletRequestResponderManagerRegistry;

import org.mortbay.http.HttpContext;
import org.mortbay.jetty.servlet.ServletHttpContext;
import org.mortbay.jetty.Server;

/**
 * This subclass of the Jetty server includes a reference to a Core object as
 * one of its attributes, so that servlets can get access to the Core. The Core
 * object reference taken by the constructor is saved so that each time a
 * context is added, the Core object is inserted as an attribute of the context.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.5
 */
public class ProgramDAwareJettyServer extends Server
{
    private Core core;

    private ServletRequestResponderManagerRegistry responderRegistry;

    /**
     * Creates a new ProgramDAwareJettyServer and keeps a reference the given
     * Core and the given responder registry.
     * 
     * @param coreToUse the Core to use.
     * @param registry the responder registry to use
     */
    public ProgramDAwareJettyServer(Core coreToUse, ServletRequestResponderManagerRegistry registry)
    {
        super();
        this.core = coreToUse;
        this.responderRegistry = registry;
    }

    /**
     * @see org.mortbay.http.HttpServer#addContext(org.mortbay.http.HttpContext)
     */
    public HttpContext addContext(HttpContext context)
    {
        ServletHttpContext result = (ServletHttpContext) super.addContext(context);
        result.getServletContext().setAttribute("core", this.core);
        result.getServletContext().setAttribute("responder-registry", this.responderRegistry);
        return result;
    }
}
