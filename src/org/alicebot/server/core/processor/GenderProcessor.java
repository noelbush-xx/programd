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
    Code cleanup (4.1.3 [00] - October 2001, Noel Bush)
    - formatting cleanup
    - complete javadoc
    - made all imports explicit
*/

package org.alicebot.server.core.processor;

import java.util.HashMap;

import org.alicebot.server.core.Bots;
import org.alicebot.server.core.Globals;
import org.alicebot.server.core.parser.TemplateParser;
import org.alicebot.server.core.parser.XMLNode;
import org.alicebot.server.core.util.Substituter;


/**
 *  <p>
 *  Handles a
 *  <code><a href="http://www.alicebot.org/TR/2001/WD-aiml/#section-gender">gender</a></code>
 *  element.
 *  </p>
 *  <p>
 *  Currently does not permit definition of gender substitutions
 *  in an external file (they are hard-coded in {@link Substituter#gender}.
 *  </p>
 *
 *  @version    4.1.3
 *  @author     Jon Baer
 *  @author     Thomas Ringate, Pedro Colla
 */
public class GenderProcessor extends AIMLProcessor
{
    public static final String label = "gender";

    /** The map of substitutions to be performed on an input. */
    private static HashMap substitutionMap = new HashMap();


    public String process(int level, XMLNode tag, TemplateParser parser) throws AIMLProcessorException
    {
        if (tag.XMLType == XMLNode.TAG)
        {
            // This looks ugly, but completely avoids a temporary variable.
            try
            {
                return parser.processResponse(applySubstitutions(parser.evaluate(level++, tag.XMLChild), parser.getBotID()));
            }
            catch (ProcessorException e)
            {
                throw (AIMLProcessorException)e;
            }
        }
        else if (tag.XMLType == XMLNode.EMPTY)
        {
            if (!Globals.supportDeprecatedTags())
            {
                return parser.shortcutTag(level, label,
                                          tag.TAG, EMPTY_STRING, StarProcessor.label, tag.EMPTY);
            }
            else
            {
                return parser.shortcutTag(level, BotProcessor.label,
                                               tag.EMPTY, "name=\"gender\"", EMPTY_STRING, tag.EMPTY);
            }
        }
        else
        {
            throw new AIMLProcessorException("Invalid gender element!");
        }
    }


    /**
     *  Applies substitutions as defined in the {@link #substitutionMap}.
     *  Comparisons are case-insensitive.
     *
     *  @param  input   the input on which to perform substitutions
     *
     *  @return the input with substitutions performed
     */
    public static String applySubstitutions(String input, String botid)
    {
        return Substituter.applySubstitutions(Bots.getBot(botid).getGenderSubstitutionsMap(), input);
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
}

