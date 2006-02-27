/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.logging;

import java.util.Date;

import org.aitools.programd.logging.ChatLogEvent;
import org.aitools.programd.util.XMLKit;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.ISO8601DateFormat;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Formats a ChatLogEvent by printing a number of extra fields as we like them.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.6
 */
public class XMLChatLogLayout extends Layout
{
    private ISO8601DateFormat timestampFormatter = new ISO8601DateFormat();

    private static final String EVENT_START = "<record>\n";

    private static final String EVENT_END = "</record>\n";

    private static final String TIME_START = "  <time>";

    private static final String TIME_END = "</time>\n";

    private static final String BOTID_START = "  <botid>";

    private static final String BOTID_END = "</botid>\n";

    private static final String USERID_START = "  <userid>";

    private static final String USERID_END = "</userid>\n";

    private static final String INPUT_START = "  <input>";

    private static final String INPUT_END = "</input>\n";

    private static final String REPLY_START = "  <reply>";

    private static final String REPLY_END = "</reply>\n";
    
    /**
     * We insist that the record be a ChatLogEvent.
     * 
     * @param event the ChatLogEvent to format
     * @return the result of formatting the given ChatLogEvent
     * @throws IllegalArgumentException if the record is not a ChatLogEvent
     */
    @Override
    public String format(LoggingEvent event)
    {
        if (!(event instanceof ChatLogEvent))
        {
            throw new IllegalArgumentException("XMLChatLogLayout is intended to handle ChatRecords only.");
        }
        return format((ChatLogEvent) event);
    }

    /**
     * @param event the ChatLogEvent to format
     * @return the result of formatting the given ChatLogEvent
     */
    public String format(ChatLogEvent event)
    {
        StringBuilder result = new StringBuilder(1000);
        result.append(EVENT_START);

        result.append(TIME_START);
        result.append(this.timestampFormatter.format(new Date(event.timeStamp)));
        result.append(TIME_END);

        result.append(BOTID_START);
        result.append(event.getBotID());
        result.append(BOTID_END);

        result.append(USERID_START);
        result.append(event.getUserID());
        result.append(USERID_END);

        result.append(INPUT_START);
        result.append(XMLKit.escapeXMLChars(event.getInput()));
        result.append(INPUT_END);

        result.append(REPLY_START);
        result.append(XMLKit.escapeXMLChars(event.getReply()));
        result.append(REPLY_END);

        result.append(EVENT_END);
        return result.toString();
    }

    @Override
    public String getHeader()
    {
        return "<?xml version=\"1.0\"?>\n<log xmlns=\"http://aitools.org/programd/4.6/chatlog\">\n";
    }
    
    @Override
    public String getFooter()
    {
        return "</log>\n";
    }

    /**
     * @see org.apache.log4j.Layout#ignoresThrowable()
     */
    @Override
    public boolean ignoresThrowable()
    {
        return true;
    }

    /**
     * @see org.apache.log4j.spi.OptionHandler#activateOptions()
     */
    @Override
    public void activateOptions()
    {
        // Do nothing.
    }
}
