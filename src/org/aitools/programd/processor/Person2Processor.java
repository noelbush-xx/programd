/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor;

import java.util.HashMap;

import org.aitools.programd.bot.Bots;
import org.aitools.programd.parser.TemplateParser;
import org.aitools.programd.parser.XMLNode;
import org.aitools.programd.util.Substituter;

/**
 * <p>
 * Handles a
 * <code><a href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-person2">person2</a></code>
 * element.
 * </p>
 * 
 * @version 4.1.3
 * @author Jon Baer
 * @author Thomas Ringate, Pedro Colla
 * @author Noel Bush
 */
public class Person2Processor extends AIMLProcessor
{
    public static final String label = "person2";

    /** The map of substitutions to be performed on an input. */
    private static HashMap substitutionMap = new HashMap();

    public String process(int level, XMLNode tag, TemplateParser parser) throws AIMLProcessorException
    {
        if (tag.XMLType == XMLNode.TAG)
        {
            try
            {
                // Return the processed contents of the element, properly
                // substituted.
                return parser.processResponse(applySubstitutions(parser.evaluate(level++, tag.XMLChild), parser
                        .getBotID()));
            } 
            catch (ProcessorException e)
            {
                throw (AIMLProcessorException) e;
            } 
        } 
        // (otherwise...)
        return parser.shortcutTag(level, label, XMLNode.TAG, EMPTY_STRING, StarProcessor.label, XMLNode.EMPTY);
    } 

    /**
     * Applies substitutions as defined in the {@link #substitutionMap} .
     * Comparisons are case-insensitive.
     * 
     * @param input
     *            the input on which to perform substitutions
     * @return the input with substitutions performed
     */
    public static String applySubstitutions(String input, String botid)
    {
        return Substituter.applySubstitutions(Bots.getBot(botid).getPerson2SubstitutionsMap(), input);
    } 

    /**
     * Adds a substitution to the substitutions map. The <code>find</code>
     * parameter is stored in uppercase, to do case-insensitive comparisons. The
     * <code>replace</code> parameter is stored as is.
     * 
     * @param find
     *            the string to find in the input
     * @param replace
     *            the string with which to replace the found string
     */
    public static void addSubstitution(String find, String replace)
    {
        if (find != null && replace != null)
        {
            substitutionMap.put(find.toUpperCase(), replace);
        } 
    } 
}