/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.logging;

import org.aitools.programd.util.XMLKit;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;

/**
 * A ChatLogEvent contains additional information about an exchange in a chat.
 * Note that the "message" member of a ChatLogEvent will be null.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.2
 */
public class ChatLogEvent extends LoggingEvent
{
    /** The client's input. */
    private String input;

    /** The bot's response. */
    private String reply;

    /** The id of the user. */
    private String userid;

    /** The id of the bot. */
    private String botid;
    
    /**
     * Creates a new ChatLogEvent.
     * 
     * @param botidToUse the botid with whom this exchange occurred
     * @param useridToUse the userid with whom this exchange occurred
     * @param inputToUse the input from the user
     * @param replyToUse the reply from the bot
     */
    public ChatLogEvent(Logger logger,
            String botidToUse, String useridToUse, String inputToUse, String replyToUse)
    {
        super(logger.getClass().getName(), logger, Level.INFO, "chat log event", null);
        this.botid = botidToUse;
        this.userid = useridToUse;
        this.input = inputToUse;
        this.reply = replyToUse;
    }

    /**
     * @return the botid.
     */
    public String getBotID()
    {
        return this.botid;
    }

    /**
     * @return the userid.
     */
    public String getUserID()
    {
        return this.userid;
    }

    /**
     * @return the input.
     */
    public String getInput()
    {
        return this.input;
    }

    /**
     * @return the reply.
     */
    public String getReply()
    {
        return this.reply;
    }

    @Override
    public String toString()
    {
        return String.format("%s -> %s: \"%s\"; %s -> %s: \"%s\"",
                this.userid, this.botid, this.input, this.botid, this.userid, XMLKit.removeMarkup(this.reply));
    }
}
