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
import org.aitools.util.xml.Characters;
import org.apache.log4j.Layout;
import org.apache.log4j.helpers.ISO8601DateFormat;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Formats a ChatLogEvent by printing a number of extra fields as we like them.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class XMLChatLogLayout extends Layout
{
    private ISO8601DateFormat timestampFormatter = new ISO8601DateFormat();

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
        result.append(String.format("<record>%n"));
        result.append(String.format("  <time>%s</time>%n", this.timestampFormatter.format(new Date(event.timeStamp))));
        result.append(String.format("  <botid>%s</botid>%n", event.getBotID()));
        result.append(String.format("  <userid>%s</userid>%n", event.getUserID()));
        result.append(String.format("  <input>%s</input>%n", Characters.escapeXMLChars(event.getInput())));
        result.append(String.format("  <reply>%s</reply>", Characters.escapeXMLChars(event.getReply())));
        result.append(String.format("</record>%n"));
        return result.toString();
    }

    /**
     * @see org.apache.log4j.Layout#getHeader()
     */
    @Override
    public String getHeader()
    {
        return String.format("<?xml version=\"1.0\"?>%n<log xmlns=\"http://aitools.org/programd/4.6/chatlog\">%n");
    }

    /**
     * @see org.apache.log4j.Layout#getFooter()
     */
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
