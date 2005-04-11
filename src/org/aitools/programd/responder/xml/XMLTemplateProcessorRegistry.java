/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.responder.xml;

import java.util.logging.Logger;

import org.aitools.programd.processor.ProcessorRegistry;
import org.aitools.programd.processor.aiml.AIMLProcessor;

/**
 * Registers {@link XMLTemplateProcessor}s.
 * 
 * @since 4.5
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class XMLTemplateProcessorRegistry extends ProcessorRegistry<AIMLProcessor>
{
    /** The XML namespace URI for AIML. */
    private static final String XMLNS = "http://aitools.org/programd/html-responder-template";

    /** The list of processors (fully-qualified class names). */
    private static final String[] PROCESSOR_LIST = { "org.aitools.programd.responder.xml.BotProcessor",
                                                     "org.aitools.programd.responder.xml.UserInputProcessor",
                                                     "org.aitools.programd.responder.xml.ReplyProcessor",
                                                     "org.aitools.programd.responder.xml.ResponseProcessor" };

    private static final Logger logger = Logger.getLogger("programd");

    /**
     * Creates a new <code>XMLTemplateProcessorRegistry</code>.
     */
    public XMLTemplateProcessorRegistry()
    {
        super(XMLNS, PROCESSOR_LIST);
    }
}