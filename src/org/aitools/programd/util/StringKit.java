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
     * Returns a tab of the specified length.
     * 
     * @param level
     *            the level of the tab
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
     * @param input
     *            the input to split
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

}