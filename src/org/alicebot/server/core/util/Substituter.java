/*
    Alicebot Program D
    Copyright (C) 1995-2001, A.L.I.C.E. AI Foundation
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
    USA.
*/

/*
    (4.1.3 [00] - October 2001, Noel Bush)
    - removed "implements Serializable"
*/

/*
    Code cleanup (4.1.3 [01] - November 2001, Noel Bush)
    - formatting cleanup
    - complete javadoc
    - made all imports explicit
    - inlined method calls to avoid unnecessary temporary variables
    - removed the following methods (replaced by individual processors & external configuration)
      - normalize
      - deperiodize
      - person
      - person2
      - gender
      - capitalize
      - formal
    - also removed (not used or not needed):
      - pretty
      - cleanup_http
      - format_http
      - suppress_html
      - wrapText
      - stripHTML
      - capitalizeWords
    - rewrote replace and added replaceIgnoreCase
    - added applySubstitutions
    - full javadoc
    - cleaned up imports
*/

/*
    More fixes (4.1.3 [02] - November 2001, Noel Bush
    - fixed applySubstitutions so that replaced values are not
      rescanned for further substitution, and added note that
      the replace() methods do *not* do this
    - fixed applySubstitutions so that correct part of original
      string is retained
*/

/*
    4.1.4 [00] - December 2001, Noel Bush
    Fixed a problem with applySubstitutions in which some
    results were getting inappropriately trimmed.
*/

/*
	4.1.5
	Jonathan Roewen supplied a fix to applySubstitutions
*/

package org.alicebot.server.core.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Set;


/**
 *  Provides generic substitution utilities for classes
 *  that don't have their own.
 *
 *  @author Noel Bush
 */
public class Substituter
{
    // Convenience constants.

    /** A space. */
    private static final String SPACE = " ";

    /** An empty string. */
    private static final String EMPTY_STRING = "";


    /**
     *  <p>
     *  Replaces all instances of <code>find</code>
     *  with <code>replace</code>.
     *  </p>
     *  <p>
     *  <b>NB:</b> This method does <i>not</i> take care that
     *  further substitutions are not performed on replaced text.
     *  For this extra caution, use {@link #applySubstitutions}.
     *  </p>
     *
     *  @param input    the string on which to perform the replacement
     *  @param find     the string to find
     *  @param replace  the string with which to replace it
     *
     *  @return the str
     */
    public static String replace(String find, String replace, String input)
    {
        // Result will be constructed in this StringBuffer.
        StringBuffer result = new StringBuffer(SPACE + input + SPACE);

        // Is the find string in the input?
        for (int startIndex = result.toString().indexOf(find);
             startIndex != -1;
             startIndex = result.toString().indexOf(find))
        {
            // If so, replace it in the result.
            result.replace(startIndex, startIndex + find.length(), replace);
        }
        return result.toString().trim();
    }


    /**
     *  <p>
     *  Same as {@link #replace} except case is ignored.
     *  </p>
     *  <p>
     *  <b>NB:</b> This method does <i>not</i> take care that
     *  further substitutions are not performed on replaced text.
     *  For this extra caution, use {@link #applySubstitutions}.
     *  </p>
     *
     *  @param input    the string on which to perform the replacement
     *  @param find     the string to find
     *  @param replace  the string with which to replace it
     *
     *  @return the str
     */
    public static String replaceIgnoreCase(String find, String replace, String input)
    {
        // Result will be constructed in this StringBuffer.
        StringBuffer result = new StringBuffer(SPACE + input + SPACE);

        find = find.toUpperCase();

        // Is the find string in the input?
        for (int startIndex = result.toString().toUpperCase().indexOf(find);
             startIndex != -1;
             startIndex = result.toString().toUpperCase().indexOf(find))
        {
            // If so, replace it in the result.
            result.replace(startIndex, startIndex + find.length(), replace);
        }
        return result.toString().trim();
    }


    /**
     *  Same as {@link #replaceIgnoreCase} except a substitution map is given,
     *  and extra care is taken to ensure that a replaced value does not
     *  have further substitutions performed on it.
     *
     *  @param substitutionMap  the map of substitutions to be performed
     *  @param input            the string on which to perform the replacement
     *
     *  @return the input with substitutions applied
     */
    public static String applySubstitutions(HashMap substitutionMap, String input)
    {
        // This will contain all pieces of the input untouched by substitution.
        LinkedList untouchedPieces = new LinkedList();

        // Pad the input with spaces.
        untouchedPieces.add(SPACE + input + SPACE);

        // This will contain all replacements to be inserted in the result.
        LinkedList replacements = new LinkedList();

        // Iterate over all substitutions.
        Iterator substitutions = substitutionMap.keySet().iterator();

        while (substitutions.hasNext())
        {
            String find = (String)substitutions.next();

            // Iterate through all untouched pieces of the inputs.
            ListIterator untouchedIterator = untouchedPieces.listIterator(0);
            while (untouchedIterator.hasNext())
            {
                // Is the find string in the untouched input?
                String untouchedTest = (String)untouchedIterator.next();
                int startIndex = untouchedTest.toUpperCase().indexOf(find.toUpperCase());

                if (startIndex >= 0 && startIndex < untouchedTest.length())
                {
                    // If so, replace the current untouched input with the substring up to startIndex,
                    untouchedIterator.set(untouchedTest.substring(0, startIndex));

                    // put the replacement text into the replacements list,
                    String replacement = (String)substitutionMap.get(find);
                    replacements.add(untouchedIterator.nextIndex() - 1, replacement);

                    // and put the remainder of the untouched input into the untouched list.
                    if (startIndex + replacement.length() < untouchedTest.length())
                    {
                        untouchedIterator.add(untouchedTest.substring(startIndex + find.length()));
                    }
                    else
                    {
                        untouchedIterator.add(EMPTY_STRING);
                    }
                }
            }
        }

        // Now construct the result.
        StringBuffer result = new StringBuffer();

        // Iterate through the untouched pieces and the replacements.
        ListIterator untouchedIterator = untouchedPieces.listIterator(0);
        ListIterator replaceIterator = replacements.listIterator(0);
        while (untouchedIterator.hasNext())
        {
            result.append(untouchedIterator.next());
            // It can be that there is one less replacement than untouched pieces.
            if (replaceIterator.hasNext())
            {
                result.append(replaceIterator.next());
            }
        }

        // Remove the padding spaces before returning!
        int resultLength = result.length();
        if (resultLength >= 2)
        {
            int resultStart = 0;
            if (result.charAt(0) == ' ')
            {
                resultStart = 1;
            }
            if (result.charAt(resultLength - 1) == ' ')
            {
                resultLength--;
            }
            if (resultStart == resultLength)
            {
                return EMPTY_STRING;
            }
            else
            {
                return result.substring(resultStart, resultLength);
            }
        }
        else
        {
            return result.toString();
        }
    }
}

