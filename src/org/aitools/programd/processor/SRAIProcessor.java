/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.aitools.programd.Core;
import org.aitools.programd.parser.GenericParser;
import org.aitools.programd.parser.TemplateParser;

/**
 * Implements the &lt;srai/&gt; element.
 * 
 * @version 4.2
 * @author Jon Baer
 * @author Thomas Ringate, Pedro Colla
 * @author Noel Bush
 */
public class SRAIProcessor extends AIMLProcessor
{
    public static final String label = "srai";
    
    public SRAIProcessor(Core coreToUse)
    {
        super(coreToUse);
    }
    
    private static final Logger errorLogger = Logger.getLogger("programd.error");

    /**
     * Processes a &lt;srai/&gt; element. First, all elements contained within a
     * given &lt;srai/&gt; are evaluated, and the result is recursively fed as
     * input to the pattern matching process. The result of such evaluation
     * (which itself might be recursive) is returned as the result.
     * 
     * @see AIMLProcessor#process(int, XMLNode, GenericParser)
     */
    public String process(Element element, TemplateParser parser)
    {
        // Check for some simple kinds of infinite loops.
        if (element.getChildNodes().getLength() == 1)
        {
            Node sraiChild = element.getChildNodes().item(0);

            if (sraiChild.getNodeType() == Node.TEXT_NODE)
            {
                String sraiContent = sraiChild.getTextContent();
                Iterator inputsIterator = parser.getInputs().iterator();

                while (inputsIterator.hasNext())
                {
                    String input = (String) inputsIterator.next();

                    if (sraiContent.equalsIgnoreCase(input))
                    {
                        String infiniteLoopInput = parser.getCore().getSettings().getInfiniteLoopInput();
                        if (!sraiContent.equalsIgnoreCase(infiniteLoopInput))
                        {
                            sraiChild.setTextContent(infiniteLoopInput);
                            errorLogger.log(Level.WARNING, "Infinite loop detected; substituting \"" + infiniteLoopInput
                                    + "\".");
                        } 
                        else
                        {
                            errorLogger.log(Level.SEVERE, "Unrecoverable infinite loop.");
                            return EMPTY_STRING;
                        } 
                    } 
                } 
                parser.addInput(sraiContent);
            } 
        }
        /*
        if (Settings.showMatchTrace())
        {
            Trace.userinfo("Symbolic Reduction:");
        }
        */ 
        return this.core.getMultiplexor().getInternalResponse(parser.evaluate(element.getChildNodes()), parser.getUserID(), parser
                .getBotID(), parser);
    } 
}