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

package org.aitools.programd.processor;

import org.aitools.programd.interpreter.ActiveJavaScriptInterpreter;
import org.aitools.programd.parser.TemplateParser;
import org.aitools.programd.parser.XMLNode;
import org.aitools.programd.util.Globals;
import org.aitools.programd.util.logging.Log;

/**
 *  Handles a
 *  <code><a href="http://www.alicebot.org/TR/2001/WD-aiml/#section-javascript">javascript</a></code>
 *  element.
 *
 *  @version    4.1.3
 *  @author     Jon Baer
 *  @author     Thomas Ringate, Pedro Colla
 */
public class JavaScriptProcessor extends AIMLProcessor
{
    public static final String label = "javascript";

    /**
     *  Returns the result of processing the contents of the <code>javascript</code>
     *  element by the JavaScript interpreter.
     */
    public String process(int level, XMLNode tag, TemplateParser parser)
        throws AIMLProcessorException
    {
        // Don't use the system tag if not permitted.
        if (!Globals.jsAccessAllowed())
        {
            Log.userinfo("Use of <javascript> prohibited!", Log.INTERPRETER);
            return EMPTY_STRING;
        }
        if (tag.XMLType == XMLNode.TAG)
        {
            Log.devinfo(
                "Calling JavaScript interpreter "
                    + Globals.javaScriptInterpreter(),
                Log.INTERPRETER);
            return ActiveJavaScriptInterpreter.getInstance().evaluate(
                parser.evaluate(level++, tag.XMLChild));
        }
        else
        {
            throw new AIMLProcessorException("<javascript></javascript> must have content!");
        }
    }
}
