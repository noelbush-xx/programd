/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.aitools.util.Lists;
import org.aitools.util.xml.Characters;
import org.jdom.Text;

/**
 * <code>InputNormalizer</code> replaces <code>Substituter</code> as the
 * utility class for performing various stages of <a
 * href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-input-normalization">input
 * normalization </a>. Substitutions of other types are now handled
 * independently by their respective processors.
 * 
 */
public class InputNormalizer
{
    /** A regex to match characters that are not legal in AIML patterns. */
    private static final java.util.regex.Pattern ILLEGAL_CHARACTERS = java.util.regex.Pattern
            .compile("[^\\p{javaUpperCase}\\p{Digit} \\*_]+");

    /** A regex to match characters that are not legal in AIML patterns, ignoring case. */
    private static final java.util.regex.Pattern ILLEGAL_CHARACTERS_IGNORE_CASE = java.util.regex.Pattern
            .compile("[^\\p{javaLetter}\\p{Digit} \\*_]+");

    /**
     * Splits an input into sentences, as defined by the
     * <code>sentenceSplitters</code>.
     * 
     * @param sentenceSplitters the sentence splitters to use
     * @param input the input to split
     * @return the input split into sentences
     */
    public static List<String> sentenceSplit(Pattern sentenceSplitters, String input)
    {
        List<String> result = new ArrayList<String>();
        Matcher matcher = sentenceSplitters.matcher(input);
        if (matcher.find())
        {
            do
            {
                result.add(input.substring(matcher.start(), matcher.end()));
            } while (matcher.find());
            if (!matcher.hitEnd())
            {
                result.add(input.substring(matcher.end()));
            }
            return result;
        }
        // else
        return Lists.singleItem(input);
    }

    /**
     * <p>
     * Performs <a
     * href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-pattern-fitting-normalizations">pattern-fitting
     * </a> normalization on an input.
     * </p>
     * <p>
     * This is best used to produce a caseless representation of the effective
     * match string when presenting match results to a user; however, if used
     * when actually performing the match it will result in the case of
     * wildcard-captured values being lost. Some amendment of the specification
     * is probably in order.
     * </p>
     * 
     * @param input the string to pattern-fit
     * @return the pattern-fitted input
     */
    public static String patternFit(String input)
    {
        return Text.normalizeString(ILLEGAL_CHARACTERS.matcher(Characters.removeMarkup(input)).replaceAll(" ")).trim();
    }

    /**
     * <p>
     * Performs a partial <a
     * href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-pattern-fitting-normalizations">pattern-fitting
     * </a> normalization on an input -- partial because it does <i>not </i>
     * convert letters to uppercase.
     * </p>
     * <p>
     * This is used when sending patterns to the Graphmaster so that wildcard
     * contents can be captured and case retained. Some amendment of the
     * specification is probably in order.
     * </p>
     * 
     * @param input the string to pattern-fit
     * @return the pattern-fitted input
     */
    public static String patternFitIgnoreCase(String input)
    {
        return Text.normalizeString(ILLEGAL_CHARACTERS_IGNORE_CASE.matcher(Characters.removeMarkup(input)).replaceAll(" ")).trim();
    }
}
