package org.aitools.programd.server.servlet;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.aitools.programd.Core;
import org.aitools.programd.bot.Bot;
import org.aitools.programd.bot.Bots;
import org.aitools.programd.server.BotAccess;

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
    protected String defaultBotServletPage = "/pages/TalkToBot.jspx";
    
    /** A page to use for displaying errors. */
    protected String errorPage = "/pages/Error.jspx";

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
        String botPage = getInitParameter("default-bot-page");
        if (botPage != null)
        {
            this.defaultBotServletPage = botPage;
        }
        String otherErrorPage = getInitParameter("default-error-page");
        if (otherErrorPage != null)
        {
            this.errorPage = otherErrorPage;
        }
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
            userid = principal.getName();
        }
        if (userid == null)
        {
            req.setAttribute("error", "Could not figure out a usable userid.");
            forward(this.errorPage, req, resp);
            return;
        }
        
        // Put the userid into a session attribute (so it can be accessed by other things; but we always recompute it!)
        session.setAttribute("userid", userid);
        
        // Get the bot parameter, if there is one.
        String botid = req.getParameter("botid");
        
        // It might be in a request attribute.
        if (botid == null)
        {
            botid = (String)req.getAttribute("botid");
        }
        
        // We intend to wind up with some sort of Program D Bot object, one way or another.
        Bot programDBot = null;
        
        // See if there is already a bot access object in the session.
        Object botAccess = session.getAttribute("bot");
        
        // If it's null, or if it is a bot but there's a bot id specified that doesn't match it, try to replace it.
        if (botAccess == null ||
                !(botAccess instanceof BotAccess &&
                (botid == null || ((BotAccess)botAccess).getBotId().equals(botid))))
        {
            // If a bot parameter is not specified, try to get any bot.
            if (botid == null || botid.length() == 0)
            {
                boolean noBot = true;
                if (this.core != null)
                {
                    Bots bots = this.core.getBots();
                    if (bots != null && bots.getCount() > 0)
                    {
                        programDBot = this.core.getBots().getABot();
                        if (programDBot != null)
                        {
                            botid = programDBot.getID();
                            noBot = false;
                        }
                    }
                }
                if (noBot)
                {
                    req.setAttribute("error", "No bots are available from the Core.  Cannot continue.");
                    forward(this.errorPage, req, resp);
                    return;
                }
            }
            else
            {
                // Forward to error page if invalid botid requested.
                if (!this.core.getBots().include(botid))
                {
                    req.setAttribute("errortext", "Requested botid not found.");
                    forward(this.errorPage, req, resp);
                    return;
                }
                // otherwise...
                programDBot = this.core.getBot(botid);
            }
            BotAccess bot = new BotAccess(this.core, botid, userid);
            session.setAttribute("bot", bot);
        }
        // Otherwise (should have a valid bot access object here), just get the program D bot
        else
        {
            programDBot = ((BotAccess)botAccess).getBot();
        }
        String botPage = programDBot.getServletPage();
        if (botPage == null || botPage.length() == 0)
        {
            botPage = this.defaultBotServletPage;
        }
        forward(botPage, req, resp);
    }
    
    protected void forward(String page, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
    {
        req.getRequestDispatcher(page).forward(req, resp);
    }
}
