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
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

//
/**
 * Our own version of the SimpleFormatter, which has the appearance we like for
 * textual logs. TODO: Elaborate this to be configurable in the information it
 * logs.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.5
 */
public class SimpleFormatter extends Formatter
{
    private static final String RBRACKET_SPACE = "] ";

    private SimpleDateFormat timestampFormat;

    private boolean showTimestamp;

    /** The system line separator. */
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /**
     * Creates a new SimpleFormatter.
     * 
     * @param timestampFormatString the timestamp format
     */
    public SimpleFormatter(String timestampFormatString)
    {
        super();
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
     * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
     */
    public String format(LogRecord record)
    {
        if (this.showTimestamp)
        {
            return '[' + this.timestampFormat.format(new Date(record.getMillis())) + RBRACKET_SPACE + record.getMessage() + LINE_SEPARATOR;
        }
        // else...
        return record.getMessage();
    }
}
