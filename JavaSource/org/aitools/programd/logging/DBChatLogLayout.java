/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.logging;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import org.aitools.programd.logging.ChatLogEvent;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.spi.LoggingEvent;

/**
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class DBChatLogLayout extends SimpleLayout
{
    /**
     * Creates a new SimpleFormatter with the given Core settings.
     */
    public DBChatLogLayout()
    {
        super();
    }

    /**
     * We insist that the event be a ChatLogEvent.
     * 
     * @param event
     *            the ChatLogEvent to format
     * @return the result of formatting the given ChatLogEvent
     * @throws IllegalArgumentException
     *             if the record is not a ChatLogEvent
     */
    @Override
    public String format(LoggingEvent event)
    {
        if (!(event instanceof ChatLogEvent))
        {
            throw new IllegalArgumentException("DBChatLogLayout is intended to handle ChatLogEvents only.");
        }
        return format((ChatLogEvent) event);
    }

    /**
     * @param event
     *            the ChatLogEvent to format
     * @return the result of formatting the given ChatLogEvent
     */
    public static String format(ChatLogEvent event)
    {
        try
        {
            return String.format(
                    "insert into chatlog (userid, botid, input, response) values ('%s', '%s', '%s', '%s')", URLEncoder
                            .encode(event.getUserID(), "utf-8"), URLEncoder.encode(event.getBotID(), "utf-8"),
                    URLEncoder.encode(event.getInput(), "utf-8"), URLEncoder.encode(event.getReply(), "utf-8"));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new RuntimeException("UTF encoding is not supported on this platform!", e);
        }
    }
}
