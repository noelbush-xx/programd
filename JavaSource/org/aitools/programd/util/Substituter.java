/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.util;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

/**
 * Provides substitution utilities for all classes.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class Substituter
{
    private static final Logger aimlLogger = Logger.getLogger("programd.aiml-processing");
    
    /**
     * Performs replacements specified by the <code>substitutionMap</code>
     * in the given <code>input</code>.
     * 
     * @param substitutionMap the map of substitutions to be performed
     * @param input the string on which to perform the replacement
     * @return the input with substitutions applied
     */
    @SuppressWarnings("boxing")
    public static String applySubstitutions(Map<Pattern, String> substitutionMap, String input)
    {
        if (substitutionMap == null || input == null)
        {
            return input;
        }
        
        if (aimlLogger.isDebugEnabled())
        {
            aimlLogger.debug(String.format("Applying %,d-element substituion map to input \"%s\".", substitutionMap.size(), input));
        }
        
        // This will contain all pieces of the input untouched by substitution.
        List<String> untouchedPieces = Collections.checkedList(new LinkedList<String>(), String.class);
        untouchedPieces.add(input);

        // This will contain all replacements to be inserted in the result.
        LinkedList<String> replacements = new LinkedList<String>();

        // Iterate over all substitutions.
        for (Pattern find : substitutionMap.keySet())
        {
            Matcher matcher = null;
            // Iterate through all untouched pieces of the inputs.
            ListIterator<String> untouchedIterator = untouchedPieces.listIterator(0);
            while (untouchedIterator.hasNext())
            {
                // Get the next untouched piece, and set up the matcher.
                String untouchedTest = untouchedIterator.next();
                if (matcher != null)
                {
                    matcher.reset(untouchedTest);
                }
                else
                {
                    matcher = find.matcher(untouchedTest);
                }

                // Is the find string in the untouched input? We only look at the first match -- we'll get others later
                if (matcher.find())
                {
                    if (aimlLogger.isDebugEnabled())
                    {
                        aimlLogger.debug(String.format("Matched \"%s\" in \"%s\".", find, untouchedTest));
                    }
                    
                    // If there is a match, replace the current untouched input with the
                    // substring up to startIndex,
                    int startIndex = matcher.start();
                    String newUntouched = untouchedTest.substring(0, startIndex);
                    untouchedIterator.set(newUntouched);
                    if (aimlLogger.isDebugEnabled())
                    {

                        aimlLogger.debug(String.format("From \"%s\" leaving untouched \"%s\".", untouchedTest, newUntouched));
                    }
                    
                    // put the replacement text into the replacements list,
                    String replacement = substitutionMap.get(find);
                    replacements.add(untouchedIterator.nextIndex() - 1, replacement);
                    if (aimlLogger.isDebugEnabled())
                    {
                        aimlLogger.debug(String.format("Added \"%s\" to replacements list.", replacement));
                    }
                    
                    // and put the remainder of the untouched input into the
                    // untouched list.
                    String remainingUntouched = untouchedTest.substring(matcher.end());
                    untouchedIterator.add(remainingUntouched);
                    untouchedIterator.previous();
                    if (aimlLogger.isDebugEnabled())
                    {
                        aimlLogger.debug(String.format("Stored remaining untouched: \"%s\".", remainingUntouched));
                    }
                }
            }
        }

        // Now construct the result.
        StringBuilder result = new StringBuilder();
        if (aimlLogger.isDebugEnabled())
        {
            aimlLogger.debug(String.format("Constructing result using %d untouched piece(s) and %d replacement(s).",
                    untouchedPieces.size(), replacements.size()));
        }

        // Iterate through the untouched pieces and the replacements.
        ListIterator<String> untouchedIterator = untouchedPieces.listIterator(0);
        ListIterator<String> replaceIterator = replacements.listIterator(0);
        while (untouchedIterator.hasNext())
        {
            result.append(untouchedIterator.next());
            // It can be that there is one less replacement than untouched
            // pieces.
            if (replaceIterator.hasNext())
            {
                result.append(replaceIterator.next());
            }
        }
        return result.toString();
    }
}