/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor;

import java.util.Iterator;

import org.aitools.programd.multiplexor.Multiplexor;
import org.aitools.programd.parser.GenericParser;
import org.aitools.programd.parser.TemplateParser;
import org.aitools.programd.parser.XMLNode;
import org.aitools.programd.util.Globals;
import org.aitools.programd.util.Trace;
import org.aitools.programd.util.logging.Log;

/**
 * Implements the &lt;srai/&gt; element.
 * 
 * @version 4.1.3
 * @author Jon Baer
 * @author Thomas Ringate, Pedro Colla
 */
public class SRAIProcessor extends AIMLProcessor
{
    public static final String label = "srai";

    /**
     * Processes a &lt;srai/&gt; element. First, all elements contained within a
     * given &lt;srai/&gt; are evaluated, and the result is recursively fed as
     * input to the pattern matching process. The result of such evaluation
     * (which itself might be recursive) is returned as the result.
     * 
     * @see AIMLProcessor#process(int, XMLNode, GenericParser)
     */
    public String process(int level, XMLNode tag, TemplateParser parser) throws AIMLProcessorException
    {
        if (tag.XMLType == XMLNode.TAG)
        {
            // Check for infinite loops.
            if (tag.XMLChild.size() == 1)
            {
                XMLNode sraiChild = tag.XMLChild.get(0);

                if (sraiChild.XMLType == XMLNode.DATA)
                {
                    String sraiContent = sraiChild.XMLData;
                    Iterator inputsIterator = parser.getInputs().iterator();

                    while (inputsIterator.hasNext())
                    {
                        String input = (String) inputsIterator.next();

                        if (sraiContent.equalsIgnoreCase(input))
                        {
                            if (!sraiContent.equalsIgnoreCase(Globals.getInfiniteLoopInput()))
                            {
                                sraiChild.XMLData = Globals.getInfiniteLoopInput();
                                Log.userinfo("Infinite loop detected; substituting \"" + Globals.getInfiniteLoopInput()
                                        + "\".", Log.RUNTIME);
                            } 
                            else
                            {
                                Log.userinfo("Unrecoverable infinite loop.", Log.RUNTIME);
                                return EMPTY_STRING;
                            } 
                        } 
                    } 
                    parser.addInput(sraiContent);
                } 
            } 
            if (Globals.showMatchTrace())
            {
                Trace.userinfo("Symbolic Reduction:");
            } 
            return Multiplexor.getInternalResponse(parser.evaluate(level++, tag.XMLChild), parser.getUserID(), parser
                    .getBotID(), parser);
        } 
        // (otherwise...)
        throw new AIMLProcessorException("<srai></srai> must have content!");
    } 
}