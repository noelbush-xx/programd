// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: Dispatcher.java,v 1.1.1.1 2001/06/17 19:02:34 noelbu Exp $
// ---------------------------------------------------------------------------

package org.alicebot.server.net.http.handler.servlet;

import org.alicebot.server.net.http.HttpFields;
import org.alicebot.server.net.http.HandlerContext;
import org.alicebot.server.net.http.handler.ResourceHandler;
import org.alicebot.server.net.http.HttpRequest;
import org.alicebot.server.net.http.HttpResponse;
import org.alicebot.server.net.http.PathMap;
import org.alicebot.server.net.http.util.Code;
import org.alicebot.server.net.http.util.MultiMap;
import org.alicebot.server.net.http.util.Resource;
import org.alicebot.server.net.http.util.UrlEncoded;
import java.io.InputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;


/* ------------------------------------------------------------ */
/** Servlet RequestDispatcher.
 * 
 * @version $Id: Dispatcher.java,v 1.1.1.1 2001/06/17 19:02:34 noelbu Exp $
 * @author Greg Wilkins (gregw)
 */
public class Dispatcher implements RequestDispatcher
{
    Context _context;
    HandlerContext _handlerContext;
    ServletHolder _holder=null;
    String _pathSpec;
    String _path;
    String _query;
    Resource _resource;
    ResourceHandler _resourceHandler;
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param server 
     * @param URL 
     */
    Dispatcher(Context context, String pathInContext, String query)
        throws IllegalStateException
    {
        Code.debug("Dispatcher for ",context,",",pathInContext,",",query);
        
        _path = Resource.canonicalPath(pathInContext);
        _query=query;
        
        _context = context;
        _handlerContext = _context.getHandler().getHandlerContext();

        for(int i=_handlerContext.getHandlerSize();i-->0;)
        {
            if (_handlerContext.getHandler(i) instanceof ServletHandler)
            {
                // Look for path in servlet handlers
                ServletHandler handler=(ServletHandler)
                    _handlerContext.getHandler(i);
                if (!handler.isStarted())
                    continue;

                Map.Entry entry=handler.getHolderEntry(_path);
                if(entry!=null)
                {
                    _pathSpec=(String)entry.getKey();
                    _holder = (ServletHolder)entry.getValue();
                    break;
                }
            }
            else if (_handlerContext.getHandler(i) instanceof ResourceHandler &&
                     _resourceHandler==null)
            {
                // remember resourceHandler as we may need it for a
                // resource forward.
                _resourceHandler=(ResourceHandler)_handlerContext.getHandler(i);
            }
        }

        // If no servlet found
        if (_holder==null && _resourceHandler!=null)
        {
            // Look for a static resource
            try{
                Resource resource= context.getServletHandler()
                    .getHandlerContext().getResourceBase();
                if (resource!=null)
                    resource = resource.addPath(_path);
                if (resource.exists() && !resource.isDirectory())
                {
                    _resource=resource;
                    Code.debug("Dispatcher for resource ",_resource);
                }
            }
            catch(IOException e){Code.ignore(e);}
        }

        // if no servlet and no resource
        if (_holder==null && _resource==null)
            throw new IllegalStateException("No servlet handlers in context");
    }

    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param server 
     * @param URL 
     */
    Dispatcher(Context context, String name)
        throws IllegalStateException
    {
        _context = context;
        _handlerContext = _context.getHandler().getHandlerContext();

        for(int i=_handlerContext.getHandlerSize();i-->0;)
        {
            if (_handlerContext.getHandler(i) instanceof ServletHandler)
            {
                ServletHandler handler=(ServletHandler)
                    _handlerContext.getHandler(i);
                if (!handler.isStarted())
                    continue;
                _holder=handler.getServletHolder(name);
                break;
            }
        }
        
        if (_holder==null)
            throw new IllegalStateException("No named servlet handler in context");
    }

    
    /* ------------------------------------------------------------ */
    /** 
     * @param request 
     * @param response 
     * @exception ServletException 
     * @exception IOException 
     */
    public void forward(javax.servlet.ServletRequest request,
                        javax.servlet.ServletResponse response)
        throws ServletException,IOException
    {
        ServletRequest servletRequest=(ServletRequest)request;
        HttpRequest httpRequest=servletRequest.getHttpRequest();
        ServletResponse servletResponse=(ServletResponse)response;
        HttpResponse httpResponse=servletResponse.getHttpResponse();
            
        if (servletRequest.getHttpRequest().isCommitted())
            throw new IllegalStateException("Request is committed");
        servletResponse.resetBuffer();
        servletResponse.setOutputState(-1);
        
        // Remove any evidence of previous include
        httpRequest.removeAttribute( "javax.servlet.include.request_uri");
        httpRequest.removeAttribute( "javax.servlet.include.servlet_path");
        httpRequest.removeAttribute( "javax.servlet.include.context_path");
        httpRequest.removeAttribute( "javax.servlet.include.query_string");
        httpRequest.removeAttribute( "javax.servlet.include.path_info");

        // Handler resource forward.
        if (_resource!=null)
        {
            Code.debug("Forward request to resource ",_resource);
            _resourceHandler.handleGet(httpRequest,httpResponse,
                                       _path,_resource,false);
            return;
        }
            
        // handle named servlet
        if (_pathSpec==null )
        {
            Code.debug("Forward request to named ",_holder);
            // just call it with existing request/response
            _holder.handle(servletRequest,servletResponse);
            return;
        }
        
        // merge query string
        if (_query!=null && _query.length()>0)
        {
            MultiMap parameters=new MultiMap();
            UrlEncoded.decodeTo(_query,parameters);
            servletRequest.pushParameters(parameters);

            String oldQ=servletRequest.getQueryString();
            if (oldQ!=null && oldQ.length()>0)
            {
                UrlEncoded encoded = new UrlEncoded(oldQ);
                Iterator iter = parameters.entrySet().iterator();
                while(iter.hasNext())
                {
                    Map.Entry entry = (Map.Entry)iter.next();
                    encoded.put(entry.getKey(),entry.getValue());
                }
                _query=encoded.encode(false);
            }
        }
        
        // The path of the new request is the forward path
        // context must be the same, info is recalculate.
        Code.debug("Forward request to ",_holder,
                   " at ",_pathSpec);
        servletRequest.setForwardPaths(_context,
                                       PathMap.pathMatch(_pathSpec,_path),
                                       PathMap.pathInfo(_pathSpec,_path),
                                       _query);
            
        // try service request
        _holder.handle(servletRequest,servletResponse);
    }
        
        
    /* ------------------------------------------------------------ */
    /** 
     * @param request 
     * @param response 
     * @exception ServletException 
     * @exception IOException 
     */
    public void include(javax.servlet.ServletRequest request,
                        javax.servlet.ServletResponse response)
        throws ServletException, IOException     
    {
        ServletRequest servletRequest=(ServletRequest)request;
        HttpRequest httpRequest=servletRequest.getHttpRequest();
        ServletResponse servletResponse=(ServletResponse)response;
        HttpResponse httpResponse=servletResponse.getHttpResponse();
            
        // Need to ensure that there is no change to the
        // response other than write
        boolean old_locked = servletResponse.getLocked();
        servletResponse.setLocked(true);
        int old_output_state = servletResponse.getOutputState();
        servletResponse.setOutputState(0);

        // handle static resource
        if (_resource!=null)
        {
            Code.debug("Include resource ",_resource);
            // just call it with existing request/response
            InputStream in = _resource.getInputStream();
            try
            {
                int len = (int)_resource.length();
                httpResponse.getOutputStream().write(in,len);
                return;
            }
            finally
            {
                try{in.close();}catch(IOException e){Code.ignore(e);}
                servletResponse.setLocked(old_locked);
                servletResponse.setOutputState(old_output_state);
            }
        }
        
        // handle named servlet
        if (_pathSpec==null)
        {
            Code.debug("Include named ",_holder);
            // just call it with existing request/response
            try
            {
                _holder.handle(servletRequest,servletResponse);
                return;
            }
            finally
            {
                servletResponse.setLocked(old_locked);
                servletResponse.setOutputState(old_output_state);
            }
        }
        
        // merge query string
        if (_query!=null && _query.length()>0)
        {
            MultiMap parameters=new MultiMap();
            UrlEncoded.decodeTo(_query,parameters);
            servletRequest.pushParameters(parameters);
        }
        
        // Request has all original path and info etc.
        // New path is in attributes - whose values are
        // saved to handle chains of includes.
        
        // javax.servlet.include.request_uri
        Object old_request_uri =
            request.getAttribute("javax.servlet.include.request_uri");
        httpRequest.setAttribute("javax.servlet.include.request_uri",
                                    servletRequest.getRequestURI());
        
        // javax.servlet.include.context_path
        Object old_context_path =
            request.getAttribute("javax.servlet.include.context_path");
        httpRequest.setAttribute("javax.servlet.include.context_path",
                                    servletRequest.getContextPath());
        
        // javax.servlet.include.query_string
        Object old_query_string =
            request.getAttribute("javax.servlet.include.query_string");
        httpRequest.setAttribute("javax.servlet.include.query_string",
                                    _query);
        
        // javax.servlet.include.servlet_path
        Object old_servlet_path =
            request.getAttribute("javax.servlet.include.servlet_path");
        
        // javax.servlet.include.path_info
        Object old_path_info =
            request.getAttribute("javax.servlet.include.path_info");

        // Try each holder until handled.
        try
        {
            // The path of the new request is the forward path
            // context must be the same, info is recalculate.
            Code.debug("Include request to ",_holder,
                       " at ",_pathSpec);
            httpRequest.setAttribute("javax.servlet.include.servlet_path",
                                 PathMap.pathMatch(_pathSpec,_path));
            httpRequest.setAttribute("javax.servlet.include.path_info",
                                 PathMap.pathInfo(_pathSpec,_path));
                
            // try service request
            _holder.handle(servletRequest,servletResponse);
        }
        finally
        {
            // revert request back to it's old self.
            servletResponse.setLocked(old_locked);
            servletResponse.setOutputState(old_output_state);
            if (_query!=null && _query.length()>0)
                servletRequest.popParameters();
            httpRequest.setAttribute("javax.servlet.include.request_uri",
                                 old_request_uri);
            httpRequest.setAttribute("javax.servlet.include.context_path",
                                 old_context_path);
            httpRequest.setAttribute("javax.servlet.include.query_string",
                                 old_query_string);
            httpRequest.setAttribute("javax.servlet.include.servlet_path",
                                 old_servlet_path);
            httpRequest.setAttribute("javax.servlet.include.path_info",
                                 old_path_info);
        }
    }
};



