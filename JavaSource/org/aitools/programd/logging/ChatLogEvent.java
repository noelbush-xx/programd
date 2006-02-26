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
 * Note that a ChatLogEvent must be logged using {@link Logger#callAppenders(LoggingEvent)},
 * not one of the wrapper methods like "info" nor the basic "log" method.  This is due to a
 * limitation with log4j (the provided logging methods will re-cast the event as a LoggingEvent
 * and it won't be recognized by the chatlog-specific filters and layouts).
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.6
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
    
    /** The fully qualified class name of the Logger class. */
    private static final String LOGGER_FQCN = Logger.class.getName();
    
    /**
     * Creates a new ChatLogEvent.
     * 
     * @param bot the botid with whom this exchange occurred
     * @param user the userid with whom this exchange occurred
     * @param in the input from the user
     * @param out the reply from the bot
     */
    public ChatLogEvent(String bot, String user, String in, String out)
    {
        super(LOGGER_FQCN,
                Logger.getLogger("programd." + bot),
                Level.INFO,
                String.format("%s -> %s: \"%s\"; %s -> %s: \"%s\"",
                        user, bot, in, bot, user, XMLKit.filterWhitespace(XMLKit.removeMarkup(out))),
                null);
        this.botid = bot;
        this.userid = user;
        this.input = in;
        this.reply = out;
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
