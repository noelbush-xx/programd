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
    More fixes {4.1.3 [02] - November 2001, Noel Bush)
    - added patternFitIgnoreCase
    - added Toolkit.removeMarkup to patternFit and patternFitIgnoreCase
    - changed sentenceSplit to return ArrayList instead of StringTokenizer
      (StringTokenizer is not very useful because one cannot iterate repeatedly over it)
    - added fix to sentenceSplit so that empty inputs still return an ArrayList
      (containing one member with an empty string)
*/

/*
    4.1.4 [00] - December 2001, Noel Bush
    - important fix that avoids generating spurious sentences for extra punctuation
*/

package org.alicebot.server.core.util;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;


/**
 *  <code>InputNormalizer</code> replaces <code>Substituter</code>
 *  as the utility class for performing various stages of
 *  <a href="http://www.alicebot.org/TR/2001/WD-aiml/#section-input-normalization">input normalization</a>.
 *  Substitutions of other types are now handled independently
 *  by their respective processors.
 *
 *  @since  4.1.3
 */
public class InputNormalizer
{
    /** The map of substitutions to be performed on an input. */
    private static HashMap substitutionMap = new HashMap();

    /** The list of strings on which to split sentences. */
    private static ArrayList sentenceSplitters = new ArrayList();


    // Convenience constants.

    /** An empty string. */
    private static final String EMPTY_STRING = "";


    /**
     *  Applies substitutions as defined in the {@link #substitutionMap}.
     *  Comparisons are case-insensitive.
     *
     *  @param  input   the input on which to perform substitutions
     *
     *  @return the input with substitutions performed
     */
    public static String applySubstitutions(String input)
    {
        return Substituter.applySubstitutions(substitutionMap, input);
    }


    /**
     *  Splits an input into sentences, as defined by the
     *  {@link #sentenceSplitterList}.
     *
     *  @param input    the input to split
     *
     *  @return the input split into sentences
     */
    public static ArrayList sentenceSplit(String input)
    {
        ArrayList result = new ArrayList();
        if (input.length() == 0)
        {
            result.add(EMPTY_STRING);
            return result;
        }

        Iterator splitters = sentenceSplitters.iterator();

        int start = 0;

        while (splitters.hasNext())
        {
            String splitter = (String)splitters.next();
            int splitterIndex = input.indexOf(splitter, start);
            while (splitterIndex != -1)
            {
                if (splitterIndex > start)
                {
                    result.add(input.substring(start, splitterIndex + 1).trim());
                }
                start = splitterIndex + 1;
                splitterIndex = input.indexOf(splitter, start);
            }
        }

        // Add whatever remains.
        if (start < input.length())
        {
            result.add(input.substring(start));
        }
        return result;
    }


    /**
     *  <p>
     *  Performs
     *  <a href="http://www.alicebot.org/TR/2001/WD-aiml/#section-pattern-fitting-normalizations">pattern-fitting</a>
     *  normalization on an input.
     *  </p>
     *  <p>
     *  This is best used to produce a caseless representation of the
     *  effective match string when presenting match results to a
     *  user; however, if used when actually performing the match it
     *  will result in the case of wildcard-captured values being lost.
     *  Some amendment of the specification is probably in order.
     *  </p>
     *
     *  @param input    the string to pattern-fit
     *
     *  @return the pattern-fitted input
     */
    public static String patternFit(String input)
    {
        // Remove all tags.
        input = Toolkit.removeMarkup(input);

        StringCharacterIterator iterator = new StringCharacterIterator(input);
        StringBuffer result = new StringBuffer(input.length());

        // Iterate over the input.
        for (char aChar = iterator.first();
             aChar != StringCharacterIterator.DONE;
             aChar = iterator.next())
        {
            // Replace non-letters/digits with a space.
            if (!Character.isLetterOrDigit(aChar) && aChar != '*' && aChar != '_')
            {
                result.append(' ');
            }
            else
            {
                result.append(Character.toUpperCase(aChar));
            }
        }
        return Toolkit.filterWhitespace(result.toString());

    }


    /**
     *  <p>
     *  Performs a partial
     *  <a href="http://www.alicebot.org/TR/2001/WD-aiml/#section-pattern-fitting-normalizations">pattern-fitting</a>
     *  normalization on an input -- partial because it
     *  does <i>not</i> convert letters to uppercase.
     *  </p>
     *  <p>
     *  This is used when sending patterns to the Graphmaster
     *  so that wildcard contents can be captured and case retained.
     *  Some amendment of the specification is probably in order.
     *  </p>
     *
     *  @param input    the string to pattern-fit
     *
     *  @return the pattern-fitted input
     */
    public static String patternFitIgnoreCase(String input)
    {
        // Remove all tags.
        input = Toolkit.removeMarkup(input);

        StringCharacterIterator iterator = new StringCharacterIterator(input);
        StringBuffer result = new StringBuffer(input.length());

        // Iterate over the input.
        for (char aChar = iterator.first();
             aChar != StringCharacterIterator.DONE;
             aChar = iterator.next())
        {
            // Replace non-letters/digits with a space.
            if (!Character.isLetterOrDigit(aChar))
            {
                result.append(' ');
            }
            else
            {
                result.append(aChar);
            }
        }
        return Toolkit.filterWhitespace(result.toString());

    }


    /**
     *  Adds a substitution to the substitutions map.  The
     *  <code>find</code> parameter is stored in uppercase,
     *  to do case-insensitive comparisons.  The <code>replace</code>
     *  parameter is stored as is.
     *
     *  @param find     the string to find in the input
     *  @param replace  the string with which to replace the found string
     */
    public static void addSubstitution(String find, String replace)
    {
        if (find != null && replace != null)
        {
            substitutionMap.put(find.toUpperCase(), replace);
        }
    }


    /**
     *  Adds a sentence splitter to the sentence splitters list.
     *
     *  @param splitter the string on which to divide sentences
     */
    public static void addSentenceSplitter(String splitter)
    {
        if (splitter != null)
        {
            sentenceSplitters.add(splitter);
        }
    }
}
