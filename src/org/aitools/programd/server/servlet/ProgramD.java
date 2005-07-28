/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.server.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aitools.programd.Core;
import org.aitools.programd.server.ServletRequestTransactionEnvelope;
import org.aitools.programd.server.ServletRequestResponderManagerRegistry;

/**
 * <p>
 * This is the chat servlet used to broker a conversation from a client. It does
 * not really do much except pass information to the
 * ServletRequestTransactionEnvelope, which is responsible for:
 * </p>
 * <ol>
 * <li>Determining the type of client requesting a bot response (via
 * User-Agent)</li>
 * <li>Obtaining a bot response from the Graphmaster</li>
 * <li>Forwarding the bot response to the appropriate Responder</li>
 * </ol>
 * 
 * @author Jon Baer
 * @author Kris Drent
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class ProgramD extends HttpServlet
{
    /** The string &quot;{@value}&quot;. */
    private static final String CORE = "core";
    
    /** The string &quot;{@value}&quot;. */
    private static final String RESPONDER_REGISTRY = "responder-registry";

    /** The Core to use. */
    private Core core;

    /** The ServletRequestResponderManagerRegistry to use. */
    private ServletRequestResponderManagerRegistry responderRegistry;

    /**
     * @see javax.servlet.GenericServlet#init()
     */
    public void init()
    {
        this.core = (Core) this.getServletContext().getAttribute(CORE);
        this.responderRegistry = (ServletRequestResponderManagerRegistry) this.getServletContext().getAttribute(RESPONDER_REGISTRY);
    }

    /**
     * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
     */
    public void init(ServletConfig config)
    {
        this.core = (Core) config.getServletContext().getAttribute(CORE);
        this.responderRegistry = (ServletRequestResponderManagerRegistry) config.getServletContext().getAttribute(RESPONDER_REGISTRY);
    }

    /**
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    public void doGet(HttpServletRequest request, HttpServletResponse response)
    {
        if (!request.getServletPath().substring(1).equals(request.getContextPath()))
        {
            return;
        }
        try
        {
            new ServletRequestTransactionEnvelope(request, response, this.core, this.responderRegistry).process();
        }
        catch (Throwable e)
        {
            this.core.fail(e);
        }
    }

    /**
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    public void doPost(HttpServletRequest request, HttpServletResponse response)
    {
        doGet(request, response);
    }
}