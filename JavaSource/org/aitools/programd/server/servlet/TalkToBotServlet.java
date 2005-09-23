package org.aitools.programd.server.servlet;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.aitools.programd.Core;
import org.aitools.programd.server.Bot;
import org.aitools.programd.util.DeveloperError;

/**
 * Puts a Bot object into the session context (if it isn't there already)
 * so that the user can conduct a conversation with the bot.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class TalkToBotServlet extends HttpServlet
{
    /** The Core object that will be used throughout. */
    protected Core core;
    
    /** A default bot page if none other is provided. */
    protected static final String DEFAULT_BOT_PAGE = "/pages/TalkToBot.jspx";

    /**
     * Gets the Core (should have already been initialized by a listener,
     * or whatever) and keeps a reference to it.
     *  
     * @see javax.servlet.GenericServlet#init()
     */
    @Override
    public void init()
    {
        this.core = (Core)getServletContext().getAttribute("core");
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
     * @throws IOException 
     * @throws ServletException 
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        setupBot(req, resp);
    }

    /**
     * Just passes to {@link #setupBot}.
     * @throws IOException 
     * @throws ServletException 
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        setupBot(req, resp);
    }

    /**
     * Creates a {@link org.aitools.programd.bot.Bot Bot} object if necessary,
     * puts it in a session attribute, and forwards to a JSP page.
     * 
     * @param req the request
     * @param resp the response
     * @throws ServletException 
     * @throws IOException 
     */
    protected void setupBot(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        // Get/create the session.
        HttpSession session = req.getSession(true);
        
        // Get the bot parameter, if there is one.
        String botid = req.getParameter("bot");
        
        // Look for a userid.
        String userid = null;
        Principal principal = req.getUserPrincipal();
        if (principal == null)
        {
            // Use the session id.
            userid = session.getId();
        }
        else
        {
            principal.getName();
        }
        if (userid == null)
        {
            throw new DeveloperError("Could not figure out a usable userid!", new NullPointerException());
        }
        
        // Put the userid into a session attribute (so it can be accessed by other things; but we always recompute it!)
        session.setAttribute("userid", userid);
        
        // We intend to wind up with some sort of Program D Bot object, one way or another.
        org.aitools.programd.bot.Bot programDBot = null;
        
        /*
         * If the session already contains a bot, and its ID matches the specified bot ID,
         * or no bot ID is specified, just forward the request.
         */
        Object botObject = session.getAttribute("bot");
        if (botObject == null ||
                !(botObject instanceof Bot &&
                (botid == null || ((Bot)botObject).getBotId().equals(botid))))
        {
            // If a bot parameter is not specified, try to get any bot.
            if (botid == null || botid.length() == 0)
            {
                programDBot = this.core.getBots().getABot();
                if (programDBot == null)
                {
                    this.log("No bots are available from the Core.  Cannot continue.", new NullPointerException());
                    return;
                }
                botid = programDBot.getID();
            }
            else
            {
                programDBot = this.core.getBot(botid);
            }
            Bot bot = new Bot(this.core, botid, userid);
            session.setAttribute("bot", bot);
        }
        if (programDBot == null)
        {
            this.log("Could not find a bot.  Cannot continue.", new NullPointerException());
        }
        forward(programDBot.getServletPage(), req, resp);
    }
    
    protected void forward(String page, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        req.getRequestDispatcher(page).forward(req, resp);
    }
}
