package org.aitools.programd.server;

import org.aitools.programd.Core;

/**
 * This is an object that provides an interface to a Program D bot to
 * be used from a server context such as a JSP page.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class BotAccess
{
    protected Core core;
    
    protected String botid;
    
    protected String userid;
    
    /**
     * Creates a new <code>Bot</code> with the given
     * core, bot id and user id.
     * 
     * @param coreToUse
     * @param bot the id of the bot
     * @param user the id of the user
     */
    public BotAccess(Core coreToUse, String bot, String user)
    {
        this.core = coreToUse;
        this.botid = bot;
        this.userid = user;
    }
    
    /**
     * Returns a response to the given input, for the assigned
     * botid and userid.
     * 
     * @param input the user input
     * @return the response to the input
     */
    public String getResponse(String input)
    {
        return this.core.getResponse(input, this.userid, this.botid);
    }
    
    /**
     * @return the botid
     */
    public String getBotId()
    {
        return this.botid;
    }
    
    /**
     * @return the underlying bot object
     */
    public org.aitools.programd.bot.Bot getBot()
    {
        return this.core.getBot(this.botid);
    }
}
