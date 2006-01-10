package org.aitools.programd.server.tags;

import java.io.IOException;

import javax.servlet.jsp.PageContext;
import javax.servlet.jsp.tagext.SimpleTagSupport;

import org.aitools.programd.server.BotAccess;

/**
 * Provides the version of the engine via JSP.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class Bot extends SimpleTagSupport
{
    /** The name of the desired predicate. */
    protected String name;
    
    /** The Bot from which values will be gotten. */
    protected org.aitools.programd.bot.Bot bot;
    
    /**
     * Sets the name attribute.
     * 
     * @param value
     */
    public void setName(String value)
    {
        this.name = value;
        BotAccess botAccess = ((BotAccess)getJspContext().getAttribute("bot", PageContext.SESSION_SCOPE));
        if (botAccess != null)
        {
            this.bot = botAccess.getBot();
        }
    }
    
    /**
     * @see javax.servlet.jsp.tagext.SimpleTagSupport#doTag()
     */
    @Override
    public void doTag() throws IOException
    {
        if (this.bot != null)
        {
            getJspContext().getOut().write(this.bot.getPropertyValue(this.name));
        }
    }
}
