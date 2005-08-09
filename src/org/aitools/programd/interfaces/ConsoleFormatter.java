/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.interfaces;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

// TODO: Elaborate this to be configurable.
/**
 * A ConsoleFormatter formats output for display on a console.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class ConsoleFormatter extends Formatter
{
    private static final String RBRACKET_SPACE = "] ";

    private static final String COLON_SPACE = ": ";

    private static int WARNING_VALUE = Level.WARNING.intValue();

    private SimpleDateFormat timestampFormat;

    private boolean showTimestamp;

    private boolean showMethodNamesForErrors;

    private boolean showMethodNamesAlways;

    /** The system line separator. */
    private static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

    /**
     * Creates a new ConsoleFormatter using the specified console settings.
     * 
     * @param consoleSettings
     */
    public ConsoleFormatter(ConsoleSettings consoleSettings)
    {
        super();
        this.showMethodNamesAlways = consoleSettings.showMethodNamesAlways();
        this.showMethodNamesForErrors = this.showMethodNamesAlways || consoleSettings.showMethodNamesForErrors();
        String timestampFormatString = consoleSettings.getTimestampFormat();
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
     * Formats the given record.
     * 
     * @see java.util.logging.Formatter#format(java.util.logging.LogRecord)
     */
    public String format(LogRecord record)
    {
        StringBuffer result = new StringBuffer();
        if (this.showTimestamp)
        {
            result.append('[' + this.timestampFormat.format(new Date(record.getMillis())) + RBRACKET_SPACE);
        }
        if (this.showMethodNamesAlways || (record.getLevel().intValue() >= WARNING_VALUE && this.showMethodNamesForErrors))
        {
            result.append(record.getSourceClassName() + '.' + record.getSourceMethodName() + COLON_SPACE);
        }
        result.append(record.getMessage() + LINE_SEPARATOR);
        return result.toString();
    }
}
