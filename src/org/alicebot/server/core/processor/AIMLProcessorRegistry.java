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

package org.alicebot.server.core.processor;

import org.alicebot.server.core.util.ClassRegistry;


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
    private static final String[] PROCESSOR_LIST = {"org.alicebot.server.core.processor.BotProcessor",
                                                    "org.alicebot.server.core.processor.ConditionProcessor",
                                                    "org.alicebot.server.core.processor.DateProcessor",
                                                    "org.alicebot.server.core.processor.FormalProcessor",
                                                    "org.alicebot.server.core.processor.GenderProcessor",
                                                    "org.alicebot.server.core.processor.GetProcessor",
                                                    "org.alicebot.server.core.processor.GossipProcessor",
                                                    "org.alicebot.server.core.processor.IDProcessor",
                                                    "org.alicebot.server.core.processor.InputProcessor",
                                                    "org.alicebot.server.core.processor.JavaScriptProcessor",
                                                    "org.alicebot.server.core.processor.LearnProcessor",
                                                    "org.alicebot.server.core.processor.LowerCaseProcessor",
                                                    "org.alicebot.server.core.processor.Person2Processor",
                                                    "org.alicebot.server.core.processor.PersonProcessor",
                                                    "org.alicebot.server.core.processor.RandomProcessor",
                                                    "org.alicebot.server.core.processor.SentenceProcessor",
                                                    "org.alicebot.server.core.processor.SetProcessor",
                                                    "org.alicebot.server.core.processor.SizeProcessor",
                                                    "org.alicebot.server.core.processor.SRAIProcessor",
                                                    "org.alicebot.server.core.processor.SRProcessor",
                                                    "org.alicebot.server.core.processor.StarProcessor",
                                                    "org.alicebot.server.core.processor.SystemProcessor",
                                                    "org.alicebot.server.core.processor.ThatProcessor",
                                                    "org.alicebot.server.core.processor.ThatStarProcessor",
                                                    "org.alicebot.server.core.processor.ThinkProcessor",
                                                    "org.alicebot.server.core.processor.TopicStarProcessor",
                                                    "org.alicebot.server.core.processor.UpperCaseProcessor",
                                                    "org.alicebot.server.core.processor.VersionProcessor"};

    /** The fully-qualified name of {@link AIMLProcessor}. */
    private static final String PROCESSOR_BASE_CLASS_NAME = "org.alicebot.server.core.processor.AIMLProcessor";

    /** The private member that initializes this class. */
    private static final AIMLProcessorRegistry self = new AIMLProcessorRegistry();


    public AIMLProcessorRegistry()
    {
        super(VERSION, PROCESSOR_LIST, PROCESSOR_BASE_CLASS_NAME);
    }
    
    
    public static AIMLProcessorRegistry getSelf()
    {
        return self;
    }
}
