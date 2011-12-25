package org.aitools.programd.server.servlet;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A base class for bot interaction scenarios that use JSP.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
abstract public class JSPOrientedBotServlet extends BotServlet {

  /**
   * 
   */
  private static final long serialVersionUID = 1L;

  /** A default bot page if none other is provided. */
  protected String defaultBotServletPage = "/pages/TalkToBot.jspx";

  /** A page to use for displaying errors. */
  protected String errorPage = "/pages/Error.jspx";

  protected static void forward(String page, HttpServletRequest req, HttpServletResponse resp) throws ServletException,
      IOException {
    req.getRequestDispatcher(page).forward(req, resp);
  }

  /**
   * @see org.aitools.programd.server.servlet.BotServlet#init()
   */
  @Override
  public void init() {
    super.init();
    String botPage = this.getInitParameter("default-bot-page");
    if (botPage != null) {
      this.defaultBotServletPage = botPage;
    }
    String otherErrorPage = this.getInitParameter("default-error-page");
    if (otherErrorPage != null) {
      this.errorPage = otherErrorPage;
    }
  }
}
