/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor.botconfiguration;

import org.w3c.dom.Element;

import org.aitools.programd.Core;
import org.aitools.programd.parser.BotsConfigurationFileParser;

/**
 * The <code>predicate</code> element specifies for specifying characteristics
 * of a <a
 * href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-aiml-predicates">predicate
 * </a>.
 * 
 * @version 4.5
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class PredicateProcessor extends BotConfigurationElementProcessor
{
    /** The label (as required by the registration scheme). */
    public static final String label = "predicate";

    private static final String DEFAULT = "default";

    private static final String SET_RETURN = "set-return";

    /**
     * Creates a new PredicateProcessor using the given Core.
     * 
     * @param coreToUse the Core object to use
     */
    public PredicateProcessor(Core coreToUse)
    {
        super(coreToUse);
    }

    /**
     * @see BotConfigurationElementProcessor#process(Element,
     *      BotsConfigurationFileParser)
     */
    public void process(Element element, BotsConfigurationFileParser parser)
    {
        String name = element.getAttribute(NAME);
        String defaultValue = element.getAttribute(DEFAULT);
        if (defaultValue.equals(EMPTY_STRING))
        {
            defaultValue = null;
        }
        String setReturn = element.getAttribute(SET_RETURN);
        boolean returnNameWhenSet = false;
        if (setReturn.equals(NAME))
        {
            returnNameWhenSet = true;
        }
        parser.getCurrentBot().addPredicateInfo(name, defaultValue, returnNameWhenSet);
    }
}