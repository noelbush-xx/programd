/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor.loadtime;

import org.aitools.programd.bot.Bot;
import org.aitools.programd.parser.StartupFileParser;
import org.aitools.programd.parser.XMLNode;
import org.aitools.programd.processor.ProcessorException;
import org.aitools.programd.util.FileManager;
import org.aitools.programd.util.Globals;
import org.aitools.programd.util.UserError;
import org.aitools.programd.util.XMLKit;
import org.aitools.programd.util.logging.Log;

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

    // Package-visibility constants for specifying substitution type.

    /** An input substitution. */
    static final int INPUT = 0;

    /** A gender substitution. */
    static final int GENDER = 1;

    /** A person substitution. */
    static final int PERSON = 2;

    /** A person2 substitution. */
    static final int PERSON2 = 3;

    public String process(int level, XMLNode tag, StartupFileParser parser) throws InvalidStartupElementException
    {
        // Does it have an href attribute?
        String href = getHref(tag);

        if (href.length() > 0)
        {
            try
            {
                return parser.processResponse(FileManager.getFileContents(href));
            } 
            catch (ProcessorException e)
            {
                throw new UserError(e.getMessage());
            } 
        } 
        // (otherwise...)
        return parser.evaluate(level++, tag.XMLChild);
    } 

    /**
     * Adds substitutions of the specified type.
     * 
     * @param type
     *            the type of substitution to add
     * @param tag
     *            the container of the &lt;substitute/&gt; elements
     * @param parser
     *            the parser handling this
     * @throws InvalidStartupElementException
     *             if the element content is bad
     */
    static void addSubstitutions(int type, XMLNode tag, StartupFileParser parser) throws InvalidStartupElementException
    {
        int substituteCount = parser.nodeCount(SUBSTITUTE, tag.XMLChild, true);

        Bot bot = parser.getCurrentBot();

        for (int index = substituteCount; index > 0; index--)
        {
            XMLNode node = parser.getNode(SUBSTITUTE, tag.XMLChild, index);
            if (node.XMLType == XMLNode.EMPTY)
            {
                String find = XMLKit.getAttributeValue(FIND, node.XMLAttr);
                String replace = XMLKit.getAttributeValue(REPLACE, node.XMLAttr);
                if (find != null && replace != null)
                {
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
            } 
            else
            {
                throw new InvalidStartupElementException("<" + tag.XMLData + "/> cannot have content!");
            } 
        } 
        if (Globals.showConsole())
        {
            Log.userinfo("Loaded " + substituteCount + " " + tag.XMLData + " substitutions.", Log.STARTUP);
        } 
    } 
}