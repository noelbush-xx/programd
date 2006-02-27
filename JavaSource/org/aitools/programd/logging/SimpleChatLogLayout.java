/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.logging;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.aitools.programd.logging.ChatLogEvent;
import org.aitools.programd.util.XMLKit;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Formats a ChatLogEvent by printing a number of extra fields as we like them.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.6
 */
public class SimpleChatLogLayout extends SimpleLayout
{
    private SimpleDateFormat timestampFormat;
    
    private boolean showTimestamp;
    
    private static final String RBRACKET_SPACE = "] ";

    private static final String RANGLE_BRACKET_SPACE = "> ";

    private static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

    /**
     * Creates a new SimpleFormatter with the given Core settings.
     */
    public SimpleChatLogLayout()
    {
        super();
    }
    
   /**
    * Sets the timestamp format to the given format.
    * 
    * @param format the timestamp format to use
    */
    public void setTimestampFormat(String format)
    {
        if (format.length() > 0)
        {
            this.timestampFormat = new SimpleDateFormat(format);
            this.showTimestamp = true;
        }
        else
        {
            this.showTimestamp = false;
        }
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
            throw new IllegalArgumentException(
                    "XMLChatLogLayout is intended to handle ChatLogEvents only.");
        }
        return format((ChatLogEvent) event);
    }

    /**
     * @param event
     *            the ChatLogEvent to format
     * @return the result of formatting the given ChatLogEvent
     */
    public String format(ChatLogEvent event)
    {
        String[] responseLines = XMLKit.filterViaHTMLTags(event.getReply());
        StringBuilder result = new StringBuilder();
        int responseLineCount = responseLines.length;
        String datetime = null;
        if (this.showTimestamp)
        {
            datetime = this.timestampFormat.format(new Date(event.timeStamp));
            result.append('[' + datetime + RBRACKET_SPACE + event.getUserID()
                    + RANGLE_BRACKET_SPACE + event.getInput() + LINE_SEPARATOR);
        }
        else
        {
            result.append(event.getUserID() + RANGLE_BRACKET_SPACE + event.getInput()
                    + LINE_SEPARATOR);
        }
        for (int index = 0; index < responseLineCount; index++)
        {
            if (this.showTimestamp)
            {
                result.append('[' + datetime + RBRACKET_SPACE + event.getBotID()
                        + RANGLE_BRACKET_SPACE + responseLines[index] + LINE_SEPARATOR);
            }
            else
            {
                result.append(event.getBotID() + RANGLE_BRACKET_SPACE + responseLines[index]
                        + LINE_SEPARATOR);
            }
        }
        return result.toString();
    }
}
