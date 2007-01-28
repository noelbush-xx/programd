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

/**
 * Handles a <code><a href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-think">think</a></code> element.
 * 
 * @author Jon Baer
 * @author Thomas Ringate, Pedro Colla
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class ThinkProcessor extends AIMLProcessor
{
    /** The label (as required by the registration scheme). */
    public static final String label = "think";

    /**
     * Creates a new ThinkProcessor using the given Core.
     * 
     * @param core the Core object to use
     */
    public ThinkProcessor(Core core)
    {
        super(core);
    }

    /**
     * @see AIMLProcessor#process(Element, TemplateParser)
     */
    @SuppressWarnings("unchecked")
    @Override
    public String process(Element element, TemplateParser parser) throws ProcessorException
    {
        parser.evaluate(element.getContent());
        return "";
    }
}
