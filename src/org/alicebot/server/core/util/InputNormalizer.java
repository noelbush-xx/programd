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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;


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
    // Convenience constants.

    /** An empty string. */
    private static final String EMPTY_STRING = "";


    /**
     *  Splits an input into sentences, as defined by the
     *  {@link #sentenceSplitterList}.
     *
     *  @param input    the input to split
     *
     *  @return the input split into sentences
     */
    public static ArrayList sentenceSplit(ArrayList sentenceSplitters, String input)
    {
        ArrayList result = new ArrayList();

        int inputLength = input.length();
        if (inputLength == 0)
        {
            result.add(EMPTY_STRING);
            return result;
        }

        // This will hold the indices of all splitters in the input.
        ArrayList splitterIndices = new ArrayList();

        // This will iterate over the splitters.
        Iterator splitters = sentenceSplitters.iterator();

        // Iterate over all the splitters.
        while (splitters.hasNext())
        {
            // Get the next splitter.
            String splitter = (String)splitters.next();

            // Look for it in the input.
            int index = input.indexOf(splitter);

            // As long as it exists,
            while (index != -1)
            {
                // add its index to the list of indices.
                splitterIndices.add(new Integer(index));

                // and look for it again starting just after the discovered index.
                index = input.indexOf(splitter, index + 1);
            }
        }
        if (splitterIndices.size() == 0)
        {
            result.add(input);
            return result;
        }

        // Sort the list of indices.
        Collections.sort(splitterIndices);

        // Iterate through the indices and remove (all previous of) consecutive values.
        ListIterator indices = splitterIndices.listIterator();
        int previousIndex = ((Integer)indices.next()).intValue();
        while (indices.hasNext())
        {
            int nextIndex = ((Integer)indices.next()).intValue();
            if (nextIndex == previousIndex + 1)
            {
                indices.previous();
                indices.previous();
                indices.remove();
            }
            previousIndex = nextIndex;
        }

        // Now iterate through the remaining indices and split sentences.
        indices = splitterIndices.listIterator();
        int startIndex = 0;
        int endIndex = inputLength - 1;
        while (indices.hasNext())
        {
            endIndex = ((Integer)indices.next()).intValue();
            result.add(input.substring(startIndex, endIndex + 1).trim());
            startIndex = endIndex + 1;
        }

        // Add whatever remains.
        if (startIndex < inputLength - 1)
        {
            result.add(input.substring(startIndex).trim());
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
}
