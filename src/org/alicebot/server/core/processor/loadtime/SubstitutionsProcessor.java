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

/*
    Further optimizations {4.1.3 [0]1 - November 2001, Noel Bush)
    - changed to extend StartupTagProcessor
    - moved to loadtime subpackage
*/

package org.alicebot.server.core.processor.loadtime;

import java.io.File;

import org.alicebot.server.core.Bot;
import org.alicebot.server.core.Globals;
import org.alicebot.server.core.Graphmaster;
import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.parser.StartupFileParser;
import org.alicebot.server.core.parser.XMLNode;
import org.alicebot.server.core.processor.ProcessorException;
import org.alicebot.server.core.util.InputNormalizer;
import org.alicebot.server.core.util.Toolkit;
import org.alicebot.server.core.util.UserError;


/**
 *  The <code>substitutions</code> element is a container
 *  for definitions of various substitutions performed by
 *  the engine.
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
    static final int INPUT   = 0;

    /** A gender substitution. */
    static final int GENDER  = 1;

    /** A person substitution. */
    static final int PERSON  = 2;

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
                return parser.
                		processResponse(
                			Toolkit.getFileContents(
                				Graphmaster.getWorkingDirectory() + File.separator + href));
            }
            catch (ProcessorException e)
            {
                throw new UserError(e);
            }
        }
        else
        {
            return parser.evaluate(level++, tag.XMLChild);
        }
    }


    /**
     *  Adds substitutions of the specified type.
     *
     *  @param type     the type of substitution to add
     *  @param tag      the container of the &lt;substitute/&gt; elements
     *  @param parser   the parser handling this
     *
     *  @throws InvalidStartupElementException if the element content is bad
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
                String find = Toolkit.getAttributeValue(FIND, node.XMLAttr);
                String replace = Toolkit.getAttributeValue(REPLACE, node.XMLAttr);
                if (find != null && replace != null)
                {
                    switch (type)
                    {
                        case INPUT :
                            bot.addInputSubstitution(find, replace);
                            break;
                        case GENDER :
                            bot.addGenderSubstitution(find, replace);
                            break;
                        case PERSON :
                            bot.addPersonSubstitution(find, replace);
                            break;
                        case PERSON2 :
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

