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

import org.aitools.programd.util.ClassRegistry;

/**
 *  Registers {@link AIMLProcessor}s for a given version of AIML.
 *
 *  @since  4.1.3
 *  @author Noel Bush
 */
public class AIMLProcessorRegistry extends ClassRegistry
{
    /** The version of AIML for which this registry is intended. */
    private static final String VERSION = "1.0.1";

    /** The list of processors (fully-qualified class names). */
    private static final String[] PROCESSOR_LIST =
        {
            "org.aitools.programd.processor.BotProcessor",
            "org.aitools.programd.processor.ConditionProcessor",
            "org.aitools.programd.processor.DateProcessor",
            "org.aitools.programd.processor.FormalProcessor",
            "org.aitools.programd.processor.GenderProcessor",
            "org.aitools.programd.processor.GetProcessor",
            "org.aitools.programd.processor.GossipProcessor",
            "org.aitools.programd.processor.IDProcessor",
            "org.aitools.programd.processor.InputProcessor",
            "org.aitools.programd.processor.JavaScriptProcessor",
            "org.aitools.programd.processor.LearnProcessor",
            "org.aitools.programd.processor.LowerCaseProcessor",
            "org.aitools.programd.processor.Person2Processor",
            "org.aitools.programd.processor.PersonProcessor",
            "org.aitools.programd.processor.RandomProcessor",
            "org.aitools.programd.processor.SentenceProcessor",
            "org.aitools.programd.processor.SetProcessor",
            "org.aitools.programd.processor.SizeProcessor",
            "org.aitools.programd.processor.SRAIProcessor",
            "org.aitools.programd.processor.SRProcessor",
            "org.aitools.programd.processor.StarProcessor",
            "org.aitools.programd.processor.SystemProcessor",
            "org.aitools.programd.processor.ThatProcessor",
            "org.aitools.programd.processor.ThatStarProcessor",
            "org.aitools.programd.processor.ThinkProcessor",
            "org.aitools.programd.processor.TopicStarProcessor",
            "org.aitools.programd.processor.UpperCaseProcessor",
            "org.aitools.programd.processor.VersionProcessor" };

    /** The fully-qualified name of {@link AIMLProcessor}. */
    private static final String PROCESSOR_BASE_CLASS_NAME =
        "org.aitools.programd.processor.AIMLProcessor";

    /** The private member that initializes this class. */
    private static final AIMLProcessorRegistry self =
        new AIMLProcessorRegistry();

    public AIMLProcessorRegistry()
    {
        super(VERSION, PROCESSOR_LIST, PROCESSOR_BASE_CLASS_NAME);
    }

    public static AIMLProcessorRegistry getSelf()
    {
        return self;
    }
}
