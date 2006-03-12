/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor.aiml;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


import org.aitools.programd.Core;
import org.aitools.programd.parser.TemplateParser;
import org.aitools.programd.parser.TemplateParserException;
import org.aitools.programd.processor.ProcessorException;
import org.apache.log4j.Logger;

/**
 * Implements the &lt;srai/&gt; element.
 * 
 * @version 4.5
 * @author Jon Baer
 * @author Thomas Ringate, Pedro Colla
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class SRAIProcessor extends AIMLProcessor
{
    /** The label (as required by the registration scheme). */
    public static final String label = "srai";

    private static Logger matchLogger = Logger.getLogger("programd.matching");

    /**
     * Creates a new SRAIProcessor using the given Core.
     * 
     * @param coreToUse the Core object to use
     */
    public SRAIProcessor(Core coreToUse)
    {
        super(coreToUse);
    }

    /**
     * Processes a &lt;srai/&gt; element. First, all elements contained within a
     * given &lt;srai/&gt; are evaluated, and the result is recursively fed as
     * input to the pattern matching process. The result of such evaluation
     * (which itself might be recursive) is returned as the result.
     * 
     * @param element the <code>srai</code> element
     * @param parser the parser that is at work
     * @return the result of processing the element
     * @see AIMLProcessor#process(Element, TemplateParser)
     */
    @Override
    public String process(Element element, TemplateParser parser) throws ProcessorException
    {
        // Check for some simple kinds of infinite loops.
        if (element.getChildNodes().getLength() == 1)
        {
            Node sraiChild = element.getChildNodes().item(0);

            if (sraiChild.getNodeType() == Node.TEXT_NODE)
            {
                String sraiContent = sraiChild.getTextContent();
                for (String input : parser.getInputs())
                {
                    if (sraiContent.equalsIgnoreCase(input))
                    {
                        String infiniteLoopInput = parser.getCore().getSettings().getInfiniteLoopInput();
                        if (!input.equalsIgnoreCase(infiniteLoopInput))
                        {
                            sraiChild.setTextContent(infiniteLoopInput);
                            aimlLogger.warn("Infinite loop detected; substituting \"" + infiniteLoopInput + "\".");
                        }
                        else
                        {
                            aimlLogger.error("Unrecoverable infinite loop.");
                            return EMPTY_STRING;
                        }
                    }
                }
                parser.addInput(sraiContent);
            }
        }

        matchLogger.debug("[SYMBOLIC REDUCTION]");

        String input = parser.evaluate(element.getChildNodes());
        String userid = parser.getUserID();
        String botid = parser.getBotID();
        try
        {
            return this.core.getMultiplexor()
                    .getInternalResponse(input, userid, botid,
                            new TemplateParser(input, userid, botid, this.core));
        }
        catch (TemplateParserException e)
        {
            throw new ProcessorException("Could not create new TemplateParser for <srai/>.", e);
        }
    }
}