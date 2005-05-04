/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor.botconfiguration;

import java.util.List;
import java.util.logging.Level;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.w3c.dom.Element;

import org.aitools.programd.Core;
import org.aitools.programd.bot.Bot;
import org.aitools.programd.parser.BotsConfigurationFileParser;
import org.aitools.programd.util.UserError;
import org.aitools.programd.util.XMLKit;

/**
 * @version 4.5
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
abstract public class SubstitutionElementProcessor extends BotConfigurationElementProcessor
{
    /** Package-visibility enum for specifying substitution type. */
    static enum SubstitutionType
    {
        /** an input substitution */
        INPUT,
        
        /** a gender substitution */
        GENDER,
        
        /** a person substitution */
        PERSON,
        
        /** a person2 substitution */
        PERSON2
    }

    // Convenience constants.

    /** The string &quot;{@value}&quot;. */
    private static final String FIND = "find";

    /** The string &quot;{@value}&quot;. */
    private static final String REPLACE = "replace";

    /**
     * Creates a new SubstitutionElementProcessor using the given Core.
     * 
     * @param coreToUse the Core object to use
     */
    public SubstitutionElementProcessor(Core coreToUse)
    {
        super(coreToUse);
    }

    /**
     * Adds substitutions of the specified type.
     * 
     * @param type the type of substitution to add
     * @param element the container of the &lt;substitute/&gt; elements
     * @param parser the parser handling this
     */
    protected void addSubstitutions(SubstitutionType type, Element element, BotsConfigurationFileParser parser)
    {
        Bot bot = parser.getCurrentBot();
        List<Element> substitutions = XMLKit.getElementChildrenOf(element);

        for (Element substitution : substitutions)
        {
            String find = substitution.getAttribute(FIND);
            
            // Compile the find pattern.
            Pattern pattern;
            try
            {
                pattern = Pattern.compile(find, Pattern.CANON_EQ | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            }
            catch (PatternSyntaxException e)
            {
                throw new UserError("Invalid substitution pattern \"" + find + "\".", e);
            }
            String replace = substitution.getAttribute(REPLACE);
            switch (type)
            {
                case INPUT:
                    bot.addInputSubstitution(pattern, replace);
                    break;
                case GENDER:
                    bot.addSubstitution(org.aitools.programd.processor.aiml.GenderProcessor.class, pattern, replace);
                    break;
                case PERSON:
                    bot.addSubstitution(org.aitools.programd.processor.aiml.PersonProcessor.class, pattern, replace);
                    break;
                case PERSON2:
                    bot.addSubstitution(org.aitools.programd.processor.aiml.Person2Processor.class, pattern, replace);
                    break;
            }
        }
        logger.log(Level.INFO, "Loaded " + substitutions.size() + " " + element.getNodeName() + " substitutions.");
    }
}