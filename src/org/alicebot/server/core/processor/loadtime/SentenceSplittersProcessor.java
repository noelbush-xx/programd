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
 *  The <code>sentence-splitters</code> element is a container
 *  for defining strings that should cause the input to be
 *  split into sentences.
 */
public class SentenceSplittersProcessor extends StartupElementProcessor
{
    public static final String label = "sentence-splitters";


    // Convenience constants.

    /** The string &quot;splitter&quot;. */
    private static final String SPLITTER = "splitter";


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
            Bot bot = parser.getCurrentBot();

            int splitterCount = parser.nodeCount(SPLITTER, tag.XMLChild, true);
            for (int index = splitterCount; --index > 0; )
            {
                XMLNode node = parser.getNode(SPLITTER, tag.XMLChild, index);
                if (node.XMLType == XMLNode.EMPTY)
                {
                    String splitter = Toolkit.getAttributeValue(VALUE, node.XMLAttr);
                    if (splitter != null)
                    {
                        bot.addSentenceSplitter(splitter);
                    }
                }
                else
                {
                    throw new InvalidStartupElementException("<splitter/> cannot have content!");
                }
            }
            if (Globals.showConsole())
            {
                Log.userinfo("Loaded " + splitterCount + " " + tag.XMLData + ".", Log.STARTUP);
            }
            return EMPTY_STRING;
        }
    }
}

