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

package org.alicebot.processor.loadtime;

import org.alicebot.graph.Graphmaster;
import org.alicebot.util.ClassRegistry;

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
    private static final String[] PROCESSOR_LIST =
        {
            "org.alicebot.processor.loadtime.BotProcessor",
            "org.alicebot.processor.loadtime.BotsProcessor",
            "org.alicebot.processor.loadtime.GenderProcessor",
            "org.alicebot.processor.loadtime.InputProcessor",
            "org.alicebot.processor.loadtime.LearnProcessor",
            "org.alicebot.processor.loadtime.ListenerProcessor",
            "org.alicebot.processor.loadtime.ListenersProcessor",
            "org.alicebot.processor.loadtime.PersonProcessor",
            "org.alicebot.processor.loadtime.Person2Processor",
            "org.alicebot.processor.loadtime.PredicateProcessor",
            "org.alicebot.processor.loadtime.PredicatesProcessor",
            "org.alicebot.processor.loadtime.PropertiesProcessor",
            "org.alicebot.processor.loadtime.PropertyProcessor",
            "org.alicebot.processor.loadtime.SentenceSplittersProcessor",
            "org.alicebot.processor.loadtime.SubstitutionsProcessor" };

    /** The fully-qualified name of {@link StartupElementProcessor}. */
    private static final String PROCESSOR_BASE_CLASS_NAME =
        "org.alicebot.processor.loadtime.StartupElementProcessor";

    /** The private member that initializes this class. */
    private static final StartupElementProcessorRegistry self =
        new StartupElementProcessorRegistry();

    private StartupElementProcessorRegistry()
    {
        super(VERSION, PROCESSOR_LIST, PROCESSOR_BASE_CLASS_NAME);
    }

    public static StartupElementProcessorRegistry getSelf()
    {
        return self;
    }
}
