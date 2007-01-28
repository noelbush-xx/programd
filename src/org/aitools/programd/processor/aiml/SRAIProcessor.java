/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor.aiml;

import org.jdom.Element;

import org.aitools.programd.Core;
import org.aitools.programd.parser.TemplateParser;
import org.aitools.programd.processor.ProcessorException;
import org.apache.log4j.Logger;

/**
 * Implements the <code><a href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-srai">srai</a></code> element.
 * 
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
     * @param core the Core object to use
     */
    public SRAIProcessor(Core core)
    {
        super(core);
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
    @SuppressWarnings("unchecked")
    @Override
    public String process(Element element, TemplateParser parser) throws ProcessorException
    {
        String input = parser.evaluate(element.getContent());
        matchLogger.debug("[SYMBOLIC REDUCTION]");
        String userid = parser.getUserID();
        String botid = parser.getBotID();
        TemplateParser recursiveParser =
            new TemplateParser(parser.getInputs(), parser.getThats(), parser.getTopics(), userid, botid, this._core);
        return this._core.getInternalResponse(input, userid, botid, recursiveParser);
    }
}
