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
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import org.aitools.programd.CoreSettings;
import org.aitools.programd.logging.ChatLogRecord;
import org.aitools.programd.util.XMLKit;

/**
 * Formats a ChatLogRecord by printing a number of extra fields as we like them.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.5
 */
public class SimpleChatLogFormatter extends SimpleFormatter
{
    private SimpleDateFormat timestampFormat;

    private boolean showTimestamp;

    private static final String RBRACKET_SPACE = "] ";

    private static final String RANGLE_BRACKET_SPACE = "> ";

    private static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

    /**
     * Creates a new SimpleFormatter with the given Core settings.
     * 
     * @param coreSettings
     *            the settings of the Core to consult
     */
    public SimpleChatLogFormatter(CoreSettings coreSettings)
    {
        super();
        String timestampFormatString = coreSettings.getChatLogTimestampFormat();
        if (timestampFormatString.length() > 0)
        {
            this.timestampFormat = new SimpleDateFormat(timestampFormatString);
            this.showTimestamp = true;
        }
        else
        {
            this.showTimestamp = false;
        }
    }

    /**
     * We insist that the record be a ChatLogRecord.
     * 
     * @see java.util.logging.SimpleFormatter#format
     * @param record
     *            the ChatLogRecord to format
     * @return the result of formatting the given ChatLogRecord
     * @throws IllegalArgumentException
     *             if the record is not a ChatLogRecord
     */
    public String format(LogRecord record)
    {
        if (!(record instanceof ChatLogRecord))
        {
            throw new IllegalArgumentException(
                    "XMLChatLogFormatter is intended to handle ChatRecords only.");
        }
        return format((ChatLogRecord) record);
    }

    /**
     * @see java.util.logging.XMLFormatter#format
     * @param record
     *            the ChatLogRecord to format
     * @return the result of formatting the given ChatLogRecord
     */
    public String format(ChatLogRecord record)
    {
        String[] responseLines = XMLKit.filterViaHTMLTags(record.getReply());
        StringBuffer result = new StringBuffer();
        int responseLineCount = responseLines.length;
        String datetime = null;
        if (this.showTimestamp)
        {
            datetime = this.timestampFormat.format(new Date(record.getMillis()));
            result.append('[' + datetime + RBRACKET_SPACE + record.getUserID()
                    + RANGLE_BRACKET_SPACE + record.getInput() + LINE_SEPARATOR);
        }
        else
        {
            result.append(record.getUserID() + RANGLE_BRACKET_SPACE + record.getInput()
                    + LINE_SEPARATOR);
        }
        for (int index = 0; index < responseLineCount; index++)
        {
            if (this.showTimestamp)
            {
                result.append('[' + datetime + RBRACKET_SPACE + record.getBotID()
                        + RANGLE_BRACKET_SPACE + responseLines[index] + LINE_SEPARATOR);
            }
            else
            {
                result.append(record.getBotID() + RANGLE_BRACKET_SPACE + responseLines[index]
                        + LINE_SEPARATOR);
            }
        }
        return result.toString();
    }
}
