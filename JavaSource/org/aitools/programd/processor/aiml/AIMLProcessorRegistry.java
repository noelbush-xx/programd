/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor.aiml;

import org.aitools.programd.processor.ProcessorRegistry;

/**
 * Registers {@link AIMLProcessor}s for a given version of AIML.
 * 
 * @since 4.1.3
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class AIMLProcessorRegistry extends ProcessorRegistry<AIMLProcessor>
{
    /** The XML namespace URI for AIML. */
    private static final String XMLNS = "http://alicebot.org/2001/AIML-1.0.1";

    /** The list of processors (fully-qualified class names). */
    private static final String[] PROCESSOR_LIST = { "org.aitools.programd.processor.aiml.BotProcessor",
                                                     "org.aitools.programd.processor.aiml.ConditionProcessor",
                                                     "org.aitools.programd.processor.aiml.DateProcessor",
                                                     "org.aitools.programd.processor.aiml.FormalProcessor", 
                                                     "org.aitools.programd.processor.aiml.GenderProcessor",
                                                     "org.aitools.programd.processor.aiml.GetProcessor",
                                                     "org.aitools.programd.processor.aiml.GossipProcessor",
                                                     "org.aitools.programd.processor.aiml.IDProcessor",
                                                     "org.aitools.programd.processor.aiml.InputProcessor",
                                                     "org.aitools.programd.processor.aiml.JavaScriptProcessor",
                                                     "org.aitools.programd.processor.aiml.LearnProcessor",
                                                     "org.aitools.programd.processor.aiml.LowerCaseProcessor",
                                                     "org.aitools.programd.processor.aiml.Person2Processor",
                                                     "org.aitools.programd.processor.aiml.PersonProcessor",
                                                     "org.aitools.programd.processor.aiml.RandomProcessor",
                                                     "org.aitools.programd.processor.aiml.SentenceProcessor",
                                                     "org.aitools.programd.processor.aiml.SetProcessor",
                                                     "org.aitools.programd.processor.aiml.SizeProcessor",
                                                     "org.aitools.programd.processor.aiml.SRAIProcessor",
                                                     "org.aitools.programd.processor.aiml.SRProcessor",
                                                     "org.aitools.programd.processor.aiml.StarProcessor",
                                                     "org.aitools.programd.processor.aiml.SystemProcessor",
                                                     "org.aitools.programd.processor.aiml.TemplateProcessor",
                                                     "org.aitools.programd.processor.aiml.ThatProcessor",
                                                     "org.aitools.programd.processor.aiml.ThatStarProcessor",
                                                     "org.aitools.programd.processor.aiml.ThinkProcessor",
                                                     "org.aitools.programd.processor.aiml.TopicStarProcessor",
                                                     "org.aitools.programd.processor.aiml.UpperCaseProcessor",
                                                     "org.aitools.programd.processor.aiml.VersionProcessor" };

    /**
     * Creates a new <code>AIMLProcessorRegistry</code>.
     */
    public AIMLProcessorRegistry()
    {
        super(XMLNS, PROCESSOR_LIST);
    }
}