/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.util;

import java.text.StringCharacterIterator;
import java.util.ArrayList;

/**
 * Contains various utilities.
 * 
 * @author Richard Wallace
 * @author Jon Baer
 * @author Thomas Ringate, Pedro Colla
 * @author Noel Bush
 */
public class StringKit
{
    /** An empty string. */
    private static final String EMPTY_STRING = "";

    /** A space, for convenience. */
    private static final String SPACE = " ";

    /** A tab, for convenience. */
    private static final String TAB = new Character('\u0009').toString();

    /** The system line separator. */
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    /**
     * Filters out multiple consecutive instances of a given string.
     * 
     * @param input
     *            the string to filter
     * @param filter
     *            the string to filter out
     * @return the string without multiple consecutive instances of
     *         <code>filterChar</code>
     */
    public static String filterMultipleConsecutive(String input, String filter)
    {
        // Null inputs return an empty string.
        if (input == null)
        {
            return EMPTY_STRING;
        }

        // trim() removes all whitespace, not only spaces.
        input = input.trim();

        // Empty inputs return an empty string.
        if (input.equals((EMPTY_STRING)))
        {
            return EMPTY_STRING;
        }

        // Index the input length.
        int inputLength = input.length();

        // Index the filter length.
        int filterLength = filter.length();

        // Calculate maximum index.
        int maxIndex = inputLength - filterLength + 1;

        // The result will be constructed in this StringBuffer.
        StringBuffer result = new StringBuffer(inputLength);

        if (maxIndex > -1)
        {
            // Previous strings will be compared using this String.
            String previous = input.substring(0, filterLength);

            // Append the first character no matter what.
            result.append(previous);

            for (int index = filterLength; index < maxIndex; index++)
            {
                String current = input.substring(index, index + filterLength);
                if (!(current.equals(previous) && current.equals(filter)))
                {
                    result.append(current);
                }
                previous = current;
            }
            return result.toString();
        }
        // (otherwise...)
        return input;
    }

    /**
     * Removes all instances of a given string from an input.
     * 
     * @param input
     *            the string to filter
     * @param filter
     *            the string to remove
     * @return the input with all instances of <code>filter</code> removed
     * @throws StringIndexOutOfBoundsException
     *             if there is malformed text in the input.
     */
    private static String removeAll(String input, String filter)
    {
        // Null inputs return an empty string.
        if (input == null)
        {
            return EMPTY_STRING;
        }

        // trim() removes all whitespace, not only spaces.
        input = input.trim();

        // Empty inputs return an empty string.
        if (input.equals((EMPTY_STRING)))
        {
            return EMPTY_STRING;
        }

        // If the filter is null, return the input as-is.
        if (filter == null)
        {
            return input;
        }

        // If the filter is an empty string, return the input as-is.
        if (filter.equals(EMPTY_STRING))
        {
            return input;
        }

        // Index the input length.
        int inputLength = input.length();

        // filterAddend is the amount to add to the index when comparing a
        // substring of the input to the filter.
        int filterAddend = filter.length() - 1;

        // If the filter does not exist in the input, return the input as-is.
        if (input.indexOf(filter) == -1)
        {
            return input;
        }

        // Never look for the filter at an offset greater than inputLength -
        // filterAddend.
        int maxIndex = inputLength - filterAddend;

        if (maxIndex > -1)
        {
            // The result will be constructed in this StringBuffer.
            StringBuffer result = new StringBuffer(inputLength);

            // Look through the string character by character.
            for (int index = 0; index <= maxIndex; index++)
            {
                // If the input at offset index doesn't start with the filter,
                if (!input.startsWith(filter, index))
                {
                    // append the present character.
                    result.append(input.substring(index, index + 1));
                }
                // Otherwise, we have found an instance of the filter,
                else
                {
                    // and so do not append anything, but increment index to
                    // skip ahead of the filter.
                    index += filterAddend;
                }
            }
            return result.toString();
        }
        // (otherwise...)
        return input;
    }

    /**
     * Returns a tab of the specified length.
     * 
     * @param level
     *            the level of the tab
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
     * @param input
     *            the input to split
     * @return the input split into sentences
     */
    public static ArrayList wordSplit(String input)
    {
        ArrayList result = new ArrayList();

        int inputLength = input.length();
        if (inputLength == 0)
        {
            result.add(EMPTY_STRING);
            return result;
        }

        int wordStart = 0;

        StringCharacterIterator iterator = new StringCharacterIterator(input);

        for (char aChar = iterator.first(); aChar != StringCharacterIterator.DONE; aChar = iterator.next())
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

}