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
import org.aitools.programd.bot.Bot;
import org.aitools.programd.parser.BotsConfigurationFileParser;

/**
 * The <code>substitutions</code> element is a container for definitions of
 * various substitutions performed by the engine.
 */
public class SubstitutionsProcessor extends BotConfigurationElementProcessor
{
    /** The label (as required by the registration scheme). */
    public static final String label = "substitutions";

    // Convenience constants.

    /** The string &quot;substitute&quot;. */
    private static final String SUBSTITUTE = "substitute";

    /** The string &quot;find&quot;. */
    private static final String FIND = "find";

    /** The string &quot;replace&quot;. */
    private static final String REPLACE = "replace";

    // Package-visibility enum for specifying substitution type.
    static enum SubstitutionType {INPUT, GENDER, PERSON, PERSON2 }

    /**
     * Creates a new SubstitutionsProcessor using the given Core.
     * @param coreToUse the Core object to use
     */
    public SubstitutionsProcessor(Core coreToUse)
    {
        super(coreToUse);
    }
    
    /**
     * @see org.aitools.programd.processor.botconfiguration.BotConfigurationElementProcessor#process(org.w3c.dom.Element, org.aitools.programd.parser.BotsConfigurationFileParser)
     */
    public void process(Element element, BotsConfigurationFileParser parser)
    {
        // Does it have an href attribute?
        if (element.hasAttribute(HREF))
        {
            parser.verifyAndParse(element.getAttribute(HREF));
        }
        else
        {
            parser.evaluate(element.getChildNodes());
        }
    } 

    /**
     * Adds substitutions of the specified type.
     * 
     * @param type
     *            the type of substitution to add
     * @param element
     *            the container of the &lt;substitute/&gt; elements
     * @param parser
     *            the parser handling this
     */
    static void addSubstitutions(SubstitutionType type, Element element, BotsConfigurationFileParser parser)
    {
        int substituteCount = element.getElementsByTagName(SUBSTITUTE).getLength();

        Bot bot = parser.getCurrentBot();

        for (int index = substituteCount; index > 0; index--)
        {
            Element substitution = (Element)parser.getNode(SUBSTITUTE, element.getChildNodes(), index);
            String find = substitution.getAttribute(FIND);
            String replace = substitution.getAttribute(REPLACE);
            switch (type)
            {
                case INPUT:
                    bot.addInputSubstitution(find, replace);
                    break;
                case GENDER:
                    bot.addGenderSubstitution(find, replace);
                    break;
                case PERSON:
                    bot.addPersonSubstitution(find, replace);
                    break;
                case PERSON2:
                    bot.addPerson2Substitution(find, replace);
                    break;
            } 
        }
        /*
        if (Settings.showConsole())
        {
            Log.userinfo("Loaded " + substituteCount + " " + element.getNodeName() + " substitutions.", Log.STARTUP);
        }
        */
    } 
}