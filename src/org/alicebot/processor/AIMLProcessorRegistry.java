/*
    Alicebot Program D
    Copyright (C) 1995-2001, A.L.I.C.E. AI Foundation
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
    USA.
*/

package org.alicebot.processor;

import org.alicebot.util.ClassRegistry;

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
            "org.alicebot.processor.BotProcessor",
            "org.alicebot.processor.ConditionProcessor",
            "org.alicebot.processor.DateProcessor",
            "org.alicebot.processor.FormalProcessor",
            "org.alicebot.processor.GenderProcessor",
            "org.alicebot.processor.GetProcessor",
            "org.alicebot.processor.GossipProcessor",
            "org.alicebot.processor.IDProcessor",
            "org.alicebot.processor.InputProcessor",
            "org.alicebot.processor.JavaScriptProcessor",
            "org.alicebot.processor.LearnProcessor",
            "org.alicebot.processor.LowerCaseProcessor",
            "org.alicebot.processor.Person2Processor",
            "org.alicebot.processor.PersonProcessor",
            "org.alicebot.processor.RandomProcessor",
            "org.alicebot.processor.SentenceProcessor",
            "org.alicebot.processor.SetProcessor",
            "org.alicebot.processor.SizeProcessor",
            "org.alicebot.processor.SRAIProcessor",
            "org.alicebot.processor.SRProcessor",
            "org.alicebot.processor.StarProcessor",
            "org.alicebot.processor.SystemProcessor",
            "org.alicebot.processor.ThatProcessor",
            "org.alicebot.processor.ThatStarProcessor",
            "org.alicebot.processor.ThinkProcessor",
            "org.alicebot.processor.TopicStarProcessor",
            "org.alicebot.processor.UpperCaseProcessor",
            "org.alicebot.processor.VersionProcessor" };

    /** The fully-qualified name of {@link AIMLProcessor}. */
    private static final String PROCESSOR_BASE_CLASS_NAME =
        "org.alicebot.processor.AIMLProcessor";

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
