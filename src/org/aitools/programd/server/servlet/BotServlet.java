package org.aitools.programd.server.servlet;

import java.security.Principal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.aitools.programd.Bot;
import org.aitools.programd.Bots;
import org.aitools.programd.Core;

/**
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class BotServlet extends HttpServlet
{
    /** The current session. */
    protected HttpSession session;
    
    /** The Core object that will be used throughout. */
    protected Core core;
    
    /** The current botid. */
    protected String botid;
    
    /** The userid. */
    protected String userid;
    
    /** The bot object in use. */
    protected Bot bot;

    /**
     * Gets the Core (should have already been initialized by a listener,
     * or whatever) and keeps a reference to it.
     *  
     * @see javax.servlet.GenericServlet#init()
     */
    @Override
    public void init()
    {
        this.core = (Core) getServletContext().getAttribute("core");
    }

    /**
     * Removes the reference to the Core, and removes it from the context
     * attributes.
     * 
     * @see javax.servlet.GenericServlet#destroy()
     */
    @Override
    public void destroy()
    {
        getServletContext().removeAttribute("core");
        this.core = null;
    }

    /**
     * Just passes to {@link #setupBot}.
     * @throws ServletException 
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException
    {
        setupBot(req, resp);
    }

    /**
     * Just passes to {@link #setupBot}.
     * @throws ServletException 
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException
    {
        setupBot(req, resp);
    }

    /**
     * @param req the request
     * @param resp the response
     * @throws ServletException 
     */
    protected void setupBot(HttpServletRequest req, HttpServletResponse resp) throws ServletException
    {
        // Get/create the session.
        this.session = req.getSession(true);

        // Look for a userid.
        this.userid = null;
        Principal principal = req.getUserPrincipal();
        if (principal == null)
        {
            // Use the session id.
            this.userid = this.session.getId();
        }
        else
        {
            this.userid = principal.getName();
        }
        if (this.userid == null)
        {
            throw new ServletException("Missing userid parameter.");
        }

        // Put the userid into a session attribute (so it can be accessed by other things; but we always recompute it!)
        this.session.setAttribute("userid", this.userid);

        // Get the bot parameter, if there is one.
        this.botid = req.getParameter("botid");

        // It might be in a request attribute.
        if (this.botid == null)
        {
            this.botid = (String) req.getAttribute("botid");
        }
        
        this.bot = null;

        // If a bot parameter is not specified, try to get any bot.
        if (this.botid == null || this.botid.length() == 0)
        {
            boolean noBot = true;
            Bots bots = this.core.getBots();
            if (bots != null && bots.size() > 0)
            {
                this.bot = this.core.getBots().getABot();
                if (this.bot != null)
                {
                    this.botid = this.bot.getID();
                    noBot = false;
                }
            }
            if (noBot)
            {
                throw new ServletException("No bots are available from the Core.  Cannot continue.");
            }
        }
        else if (!this.core.getBots().containsKey(this.botid))
        {
            throw new ServletException("Requested botid not found.");
        }
        // otherwise...
        this.bot = this.core.getBot(this.botid);
    }
}
