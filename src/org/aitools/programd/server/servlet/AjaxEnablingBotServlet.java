package org.aitools.programd.server.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aitools.programd.server.BotAccess;
import org.aitools.util.runtime.Errors;

/**
 * Puts a Bot object into the session context (if it isn't there already) so that the user can conduct a conversation
 * with the bot.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class AjaxEnablingBotServlet extends JSPOrientedBotServlet
{
    /**
     * Just passes to {@link #setupBot}.
     * 
     * @throws ServletException
     * @see javax.servlet.http.HttpServlet#doGet(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException
    {
        setupBot(req, resp);
    }

    /**
     * Just passes to {@link #setupBot}.
     * 
     * @throws ServletException
     * @see javax.servlet.http.HttpServlet#doPost(javax.servlet.http.HttpServletRequest,
     *      javax.servlet.http.HttpServletResponse)
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException
    {
        setupBot(req, resp);
    }

    /**
     * Creates a {@link org.aitools.programd.Bot Bot} object if necessary, puts it in a session attribute, and
     * forwards to a JSP page.
     * 
     * @param req the request
     * @param resp the response
     * @throws ServletException
     */
    @Override
    protected void setupBot(HttpServletRequest req, HttpServletResponse resp) throws ServletException
    {
        try
        {
            super.setupBot(req, resp);
        }
        catch (ServletException e)
        {
            req.setAttribute("error", Errors.describe(e));
            try
            {
                forward(this.errorPage, req, resp);
            }
            catch (IOException ee)
            {
                throw new ServletException(ee);
            }
            return;
        }

        // See if there is already a bot access object in the session.
        Object botAccessObject = this.session.getAttribute("bot");

        // If it's null, or if it is a bot but there's a bot id specified that doesn't match it, replace it.
        if (botAccessObject == null
                || !(botAccessObject instanceof BotAccess && ((BotAccess) botAccessObject).getBotId()
                        .equals(this.botid)))
        {
            BotAccess botAccess = new BotAccess(this.core, this.botid, this.userid);
            this.session.setAttribute("bot", botAccess);
        }
        else
        {
            this.bot = ((BotAccess) botAccessObject).getBot();
        }

        String botPage = this.bot.getServletPage();
        if (botPage == null || botPage.length() == 0)
        {
            botPage = this.defaultBotServletPage;
        }
        try
        {
            forward(botPage, req, resp);
        }
        catch (IOException e)
        {
            throw new ServletException(e);
        }
    }
}
