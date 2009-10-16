/*
 * aitools utilities
 * Copyright (C) 2006 Noel Bush
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.aitools.util.xml;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jdom.Text;

/**
 * Utilities specific to (X)HTML handling.
 *
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class XHTML
{
    private static final Pattern XHTML_BREAK_LINE_REGEX = Pattern.compile("[\r\n]*(<(.+?:)?br( .*?)?/>|<(.+?:)?p( .+?)?>|</(.+?:)?p>)[\r\n]*");
    
    private static final Pattern PRE_REGEX = Pattern.compile("<(?:.+?:)?pre(?: .+?)?>(.+?)</(?:.+?:)?pre>", Pattern.DOTALL);
    
    private static final Pattern LINEFEED_REGEX = Pattern.compile("[\r\n]+");
    
    private static final String[] EMPTY_STRING_ARRAY = {("")};

    /**
     * Breaks a message into multiple lines at an (X)HTML &lt;br/&gt;, except if it
     * comes at the beginning of the message, or ending (X)HTML &lt;/p&gt;.
     * 
     * @param input the string to break
     * @return one line per array item
     */
    public static String[] breakLines(String input)
    {
        // Null inputs return an empty string array.
        if (input == null)
        {
            return EMPTY_STRING_ARRAY;
        }
        // Trim all whitespace at beginning and end.
        String _input = input.trim();
    
        // Empty trimmed inputs return an empty string array.
        if (_input.equals(""))
        {
            return EMPTY_STRING_ARRAY;
        }
        
        // Split into lines.
        List<String> rawLines = Arrays.asList(XHTML_BREAK_LINE_REGEX.split(_input));
        
        // Preserve actual line breaks within <pre/> sections.
        List<String> preservedLines = new ArrayList<String>(rawLines.size());
        Matcher matcher;
        String normalizedLine;
        for (String line : rawLines)
        {
            matcher = PRE_REGEX.matcher(line);
            if (matcher.matches())
            {
                for (String preLine : LINEFEED_REGEX.split(matcher.group(1)))
                {
                    if (preLine.length() > 0)
                    {
                        preservedLines.add(preLine);
                    }
                }
            }
            else
            {
                // Remove blank lines.
                normalizedLine = Text.normalizeString(line);
                if (normalizedLine.length() > 0)
                {
                    preservedLines.add(normalizedLine);
                }
            }
        }
        
        return preservedLines.toArray(EMPTY_STRING_ARRAY);
    }

}
