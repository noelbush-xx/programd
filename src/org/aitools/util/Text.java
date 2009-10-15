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

package org.aitools.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Contains various utilities for manipulating text.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class Text
{
    /** The regex for splitting words at blank spaces. */
    public static final String WORD_SPLIT = "\\p{Blank}+";

    /** The system line separator. */
    private static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

    /**
     * Returns a string of the specified number of repetitions of the given char.
     * 
     * @param character the character to repeat
     * @param count the number of repetitions
     * @return the requested string
     */
    public static String repeat(char character, int count)
    {
        char[] result = new char[count];
        Arrays.fill(result, character);
        return new String(result).intern();
    }

    /**
     * Returns a tab of the specified length.
     * 
     * @param level the level of the tab
     * @return the requested tab
     */
    public static String tab(int level)
    {
        return repeat('\t', level);
    }

    /**
     * Splits an input into words, breaking at word boundaries.
     * 
     * @param input the input to split
     * @return the input split into sentences
     */
    public static ArrayList<String> wordSplit(String input)
    {
        return new ArrayList<String>(Arrays.asList(input.split(WORD_SPLIT)));
    }

    /**
     * Turns an array of strings into a single string with line separators between each of the original strings.
     * 
     * @param strings the strings to render
     * @return the rendered strings
     */
    public static String renderAsLines(String[] strings)
    {
        int stringCount = strings.length;
        if (stringCount == 0)
        {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (int index = 0; index < stringCount - 1; index++)
        {
            result.append(strings[index]);
            result.append(LINE_SEPARATOR);
        }
        result.append(strings[stringCount - 1]);
        return result.toString();
    }
    
    /**
     * Returns the index of the last space block
     * (one or more consecutive space characters)
     * in the given StringBuilder.
     * 
     * @param builder
     * @return the index of the last whitespace block
     */
    public static int lastSpace(StringBuilder builder)
    {
        int index = builder.lastIndexOf(" ");
        if (index > -1)
        {
            while (builder.substring(index - 1, index).equals(" "))
            {
                index--;
            }
        }
        return index;
    }
    
    /**
     * Merges the given list of char arrays into a single string.
     * 
     * @param list
     * @return the merged result
     */
    public static String merge(List<char[]> list)
    {
        return merge(list, 0, list.size());
    }
    
    /**
     * Merges the sublist between the given indices of given list
     * of char arrays into a single string.  (As in {@link List#subList(int, int)},
     * <code>fromIndex</code> is inclusive and <code>toIndex</code>
     * is exclusive).
     * 
     * @param list
     * @param fromIndex 
     * @param toIndex 
     * @return the merged result
     */
    public static String merge(List<char[]> list, int fromIndex, int toIndex)
    {
        StringBuilder result = new StringBuilder();
        for (char[] charlist : list.subList(fromIndex, toIndex))
        {
            result.append(charlist);
        }
        return result.toString();
    }
}
