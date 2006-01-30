/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.interfaces;

import org.apache.log4j.PatternLayout;

// TODO: Elaborate this to be configurable.
/**
 * A ConsoleLayout formats output for display on a console.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class ConsoleLayout extends PatternLayout
{
    /** The system line separator. */
    private static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

    /**
     * Creates a new ConsoleLayout using the specified console settings.
     * 
     * @param consoleSettings
     */
    public ConsoleLayout(ConsoleSettings consoleSettings)
    {
        super();
        
        StringBuilder pattern = new StringBuilder();
        String timestampFormatString = consoleSettings.getTimestampFormat();
        if (timestampFormatString.length() > 0)
        {
            pattern.append("[%d{" + timestampFormatString + "}] ");
        }
        pattern.append("%P - ");
        if (consoleSettings.showMethodNamesAlways())
        {
            pattern.append("%I: ");
        }
        pattern.append("%m");
        pattern.append(LINE_SEPARATOR);
        
        setConversionPattern(pattern.toString());
    }
}
