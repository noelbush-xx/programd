/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor.loadtime;

import java.net.URI;
import java.net.URISyntaxException;

import org.w3c.dom.Element;

import org.aitools.programd.Core;
import org.aitools.programd.bot.Bot;
import org.aitools.programd.parser.StartupFileParser;
import org.aitools.programd.processor.ProcessorException;
import org.aitools.programd.util.UserError;

/**
 * The <code>substitutions</code> element is a container for definitions of
 * various substitutions performed by the engine.
 */
public class SubstitutionsProcessor extends StartupElementProcessor
{
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

    public SubstitutionsProcessor(Core coreToUse)
    {
        super(coreToUse);
    }
    
    public void process(Element element, StartupFileParser parser)
    {
        // Does it have an href attribute?
        if (element.hasAttribute(HREF))
        {
            String href = element.getAttribute(HREF);
            try
            {
                parser.processResponse(new URI(href));
            } 
            catch (ProcessorException e)
            {
                throw new UserError(e.getMessage());
            }
			catch (URISyntaxException e)
			{
                throw new UserError(e.getMessage());
			}
        }
        // (otherwise...)
        parser.evaluate(element.getChildNodes());
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
     * @throws InvalidStartupElementException
     *             if the element content is bad
     */
    static void addSubstitutions(SubstitutionType type, Element element, StartupFileParser parser)
    {
        int substituteCount = parser.elementCount(SUBSTITUTE, element.getChildNodes());

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