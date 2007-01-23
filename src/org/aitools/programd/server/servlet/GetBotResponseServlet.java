package org.aitools.programd.server.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jdom.Text;

/**
 * A simple HTTP GET based servlet which accepts a user input and botid as
 * request parameters and returns the reply as a single string (not an HTML page!).
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class GetBotResponseServlet extends BotServlet
{
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException
    {
        super.doGet(req, resp);
        resp.setContentType(req.getContentType());
        resp.setCharacterEncoding(req.getCharacterEncoding());
        PrintWriter writer = null;
        try
        {
            writer = resp.getWriter();
        }
        catch (IOException e)
        {
            throw new ServletException("IO exception when trying to get response printwriter.", e);
        }
        String input = req.getParameter("input");
        if (input == null)
        {
            return;
        }
        writer.write(Text.normalizeString(this.core.getResponse(input, this.userid, this.botid)));
        writer.flush();
        writer.close();
    }
}
