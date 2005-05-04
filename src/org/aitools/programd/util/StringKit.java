/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.util;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;

/**
 * Contains various utilities.
 * 
 * @author Richard Wallace
 * @author Jon Baer
 * @author Thomas Ringate, Pedro Colla
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class StringKit
{
    /** An empty string. */
    private static final String EMPTY_STRING = "";

    /** The system line separator. */
    private static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

    /**
     * Returns a tab of the specified length.
     * 
     * @param level the level of the tab
     * @return the requested tab
     */
    public static String tab(int level)
    {
        char[] result = new char[level];
        for (int index = level; --index >= 0;)
        {
            result[index] = '\t';
        }
        return new String(result);
    }

    /**
     * Splits an input into words, breaking at spaces. This method is obviously
     * limited in that it is not aware of other word boundaries.
     * 
     * @param input the input to split
     * @return the input split into sentences
     */
    public static ArrayList<String> wordSplit(String input)
    {
        ArrayList<String> result = new ArrayList<String>();

        int inputLength = input.length();
        if (inputLength == 0)
        {
            result.add(EMPTY_STRING);
            return result;
        }

        int wordStart = 0;

        StringCharacterIterator iterator = new StringCharacterIterator(input);

        for (char aChar = iterator.first(); aChar != CharacterIterator.DONE; aChar = iterator.next())
        {
            if (aChar == ' ')
            {
                int index = iterator.getIndex();
                result.add(input.substring(wordStart, index));
                wordStart = index + 1;
            }
        }
        if (wordStart < input.length())
        {
            result.add(input.substring(wordStart));
        }
        return result;
    }

    /**
     * Turns an array of strings into a single string with
     * line separators between each of the original strings.
     * 
     * @param strings the strings to render
     * @return the rendered strings
     */
    public static String renderAsLines(String[] strings)
    {
        int stringCount = strings.length;
        if (stringCount == 0)
        {
            return strings[0];
        }
        StringBuffer result = new StringBuffer();
        for (int index = 0; index < stringCount - 1; index++)
        {
            result.append(strings[index]);
            result.append(LINE_SEPARATOR);
        }
        result.append(strings[stringCount - 1]);
        return result.toString();
    }
}