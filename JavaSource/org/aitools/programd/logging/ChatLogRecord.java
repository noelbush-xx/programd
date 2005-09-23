/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.logging;

import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 * A ChatLogRecord contains additional information about an exchange in a chat.
 * Note that the "message" member of a ChatLogRecord will be null.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.2
 */
public class ChatLogRecord extends LogRecord
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
     * Creates a new ChatLogRecord.
     * 
     * @param botidToUse the botid with whom this exchange occurred
     * @param useridToUse the userid with whom this exchange occurred
     * @param inputToUse the input from the user
     * @param replyToUse the reply from the bot
     */
    public ChatLogRecord(String botidToUse, String useridToUse, String inputToUse, String replyToUse)
    {
        super(Level.INFO, null);
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

}
