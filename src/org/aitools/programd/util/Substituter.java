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

/**
 * Provides substitution utilities for all classes.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class Substituter
{
    /**
     * Performs replacements specified by the <code>substitutionMap</code>
     * in the given <code>input</code>.
     * 
     * @param substitutionMap the map of substitutions to be performed
     * @param input the string on which to perform the replacement
     * @return the input with substitutions applied
     */
    public static String applySubstitutions(Map<Pattern, String> substitutionMap, String input)
    {
        if (substitutionMap == null)
        {
            return input;
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
                // Is the find string in the untouched input?
                String untouchedTest = untouchedIterator.next();
                if (matcher != null)
                {
                    matcher.reset(untouchedTest);
                }
                else
                {
                    matcher = find.matcher(untouchedTest);
                }

                // We only look at the first match -- we'll get others later
                if (matcher.find())
                {
                    // If there is a match, replace the current untouched input with the
                    // substring up to startIndex,
                    int startIndex = matcher.start();
                    untouchedIterator.set(untouchedTest.substring(0, startIndex));
                    
                    // put the replacement text into the replacements list,
                    String replacement = substitutionMap.get(find);
                    replacements.add(untouchedIterator.nextIndex() - 1, replacement);

                    // and put the remainder of the untouched input into the
                    // untouched list.
                    untouchedIterator.add(untouchedTest.substring(matcher.end()));
                }
            }
        }

        // Now construct the result.
        StringBuffer result = new StringBuffer();

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