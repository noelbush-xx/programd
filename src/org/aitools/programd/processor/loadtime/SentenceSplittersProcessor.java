/*    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
    USA.
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

    public String process(int level, XMLNode tag, StartupFileParser parser)
        throws InvalidStartupElementException
    {
        // Does it have an href attribute?
        String href = getHref(tag);

        if (href.length() > 0)
        {
            try
            {
                return parser.processResponse(
                    FileManager.getFileContents(href));
            }
            catch (ProcessorException e)
            {
                throw new UserError(e.getMessage());
            }
        }
        // (otherwise...)
        Bot bot = parser.getCurrentBot();

        int splitterCount = parser.nodeCount(SPLITTER, tag.XMLChild, true);
        for (int index = splitterCount; --index > 0;)
        {
            XMLNode node = parser.getNode(SPLITTER, tag.XMLChild, index);
            if (node.XMLType == XMLNode.EMPTY)
            {
                String splitter =
                    XMLKit.getAttributeValue(VALUE, node.XMLAttr);
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
            Log.userinfo(
                "Loaded " + splitterCount + " " + tag.XMLData + ".",
                Log.STARTUP);
        }
        return EMPTY_STRING;
    }
}
