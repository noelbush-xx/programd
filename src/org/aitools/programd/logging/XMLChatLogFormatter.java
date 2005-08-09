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
import java.util.logging.XMLFormatter;

import org.aitools.programd.logging.ChatLogRecord;
import org.aitools.programd.util.XMLKit;

/**
 * Formats a ChatLogRecord by printing a number of extra fields as we like them.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.5
 */
public class XMLChatLogFormatter extends XMLFormatter
{
    private static final SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

    private static final String RECORD_START = "<record>\n";

    private static final String RECORD_END = "</record>\n";

    private static final String DATE_START = "  <date>";

    private static final String DATE_END = "</date>\n";

    private static final String MILLIS_START = "  <millis>";

    private static final String MILLIS_END = "</millis>\n";

    private static final String SEQUENCE_START = "  <sequence>";

    private static final String SEQUENCE_END = "</sequence>\n";

    private static final String BOTID_START = "  <botid>";

    private static final String BOTID_END = "</botid>\n";

    private static final String USERID_START = "  <userid>";

    private static final String USERID_END = "</userid>\n";

    private static final String INPUT_START = "  <input>";

    private static final String INPUT_END = "</input>\n";

    private static final String REPLY_START = "  <reply>";

    private static final String REPLY_END = "</reply>\n";

    /**
     * We insist that the record be a ChatLogRecord.
     * 
     * @see java.util.logging.XMLFormatter#format
     * @param record the ChatLogRecord to format
     * @return the result of formatting the given ChatLogRecord
     * @throws IllegalArgumentException if the record is not a ChatLogRecord
     */
    public String format(LogRecord record)
    {
        if (!(record instanceof ChatLogRecord))
        {
            throw new IllegalArgumentException("XMLChatLogFormatter is intended to handle ChatRecords only.");
        }
        return format((ChatLogRecord) record);
    }

    /**
     * @see java.util.logging.XMLFormatter#format
     * @param record the ChatLogRecord to format
     * @return the result of formatting the given ChatLogRecord
     */
    public String format(ChatLogRecord record)
    {
        StringBuffer result = new StringBuffer(1000);
        result.append(RECORD_START);

        result.append(DATE_START);
        result.append(XMLChatLogFormatter.dateFormatter.format(new Date(record.getMillis())));
        result.append(DATE_END);

        result.append(MILLIS_START);
        result.append(record.getMillis());
        result.append(MILLIS_END);

        result.append(SEQUENCE_START);
        result.append(record.getSequenceNumber());
        result.append(SEQUENCE_END);

        result.append(BOTID_START);
        result.append(record.getBotID());
        result.append(BOTID_END);

        result.append(USERID_START);
        result.append(record.getUserID());
        result.append(USERID_END);

        result.append(INPUT_START);
        result.append(XMLKit.escapeXMLChars(record.getInput()));
        result.append(INPUT_END);

        result.append(REPLY_START);
        result.append(XMLKit.escapeXMLChars(record.getReply()));
        result.append(REPLY_END);

        result.append(RECORD_END);
        return result.toString();
    }

}
