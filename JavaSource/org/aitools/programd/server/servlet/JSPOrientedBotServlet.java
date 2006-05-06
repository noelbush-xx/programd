package org.aitools.programd.server.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class JSPOrientedBotServlet extends BotServlet
{
    /** A default bot page if none other is provided. */
    protected String defaultBotServletPage = "/pages/TalkToBot.jspx";

    /** A page to use for displaying errors. */
    protected String errorPage = "/pages/Error.jspx";

    @Override
    public void init()
    {
        super.init();
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

    protected void forward(String page, HttpServletRequest req, HttpServletResponse resp) throws ServletException,
            IOException
    {
        req.getRequestDispatcher(page).forward(req, resp);
    }
}
