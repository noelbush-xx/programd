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

package org.alicebot.server.core.processor.loadtime;

import org.alicebot.server.core.Graphmaster;
import org.alicebot.server.core.util.ClassRegistry;


/**
 *  Registers {@link StartupElementProcessor}s for Program D.
 *
 *  @since  4.1.3
 *  @author Noel Bush
 */
public class StartupElementProcessorRegistry extends ClassRegistry
{
    /** The version of Program D for which this registry is intended. */
    private static final String VERSION = Graphmaster.VERSION;

    /** The list of processors (fully-qualified class names). */
    private static final String[] PROCESSOR_LIST = {"org.alicebot.server.core.processor.loadtime.BotProcessor",
                                                    "org.alicebot.server.core.processor.loadtime.BotsProcessor",
                                                    "org.alicebot.server.core.processor.loadtime.GenderProcessor",
                                                    "org.alicebot.server.core.processor.loadtime.InputProcessor",
                                                    "org.alicebot.server.core.processor.loadtime.LearnProcessor",
                                                    "org.alicebot.server.core.processor.loadtime.ListenerProcessor",
                                                    "org.alicebot.server.core.processor.loadtime.ListenersProcessor",
                                                    "org.alicebot.server.core.processor.loadtime.PersonProcessor",
                                                    "org.alicebot.server.core.processor.loadtime.Person2Processor",
                                                    "org.alicebot.server.core.processor.loadtime.PredicateProcessor",
                                                    "org.alicebot.server.core.processor.loadtime.PredicatesProcessor",
                                                    "org.alicebot.server.core.processor.loadtime.PropertiesProcessor",
                                                    "org.alicebot.server.core.processor.loadtime.PropertyProcessor",
                                                    "org.alicebot.server.core.processor.loadtime.SentenceSplittersProcessor",
                                                    "org.alicebot.server.core.processor.loadtime.SubstitutionsProcessor"};

    /** The fully-qualified name of {@link StartupElementProcessor}. */
    private static final String PROCESSOR_BASE_CLASS_NAME = "org.alicebot.server.core.processor.loadtime.StartupElementProcessor";

    /** The private member that initializes this class. */
    private static final StartupElementProcessorRegistry self = new StartupElementProcessorRegistry();


    private StartupElementProcessorRegistry()
    {
        super(VERSION, PROCESSOR_LIST, PROCESSOR_BASE_CLASS_NAME);
    }
    
    
    public static StartupElementProcessorRegistry getSelf()
    {
        return self;
    }
}
