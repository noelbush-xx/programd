/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor.loadtime;

import org.aitools.programd.graph.Graphmaster;
import org.aitools.programd.util.ClassRegistry;

/**
 * Registers {@link StartupElementProcessor} s for Program D.
 * 
 * @since 4.1.3
 * @author Noel Bush
 */
public class StartupElementProcessorRegistry extends ClassRegistry
{
    /** The version of Program D for which this registry is intended. */
    private static final String VERSION = Graphmaster.VERSION;

    /** The list of processors (fully-qualified class names). */
    private static final String[] PROCESSOR_LIST =
        { "org.aitools.programd.processor.loadtime.BotProcessor",
                "org.aitools.programd.processor.loadtime.BotsProcessor",
                "org.aitools.programd.processor.loadtime.GenderProcessor",
                "org.aitools.programd.processor.loadtime.InputProcessor",
                "org.aitools.programd.processor.loadtime.LearnProcessor",
                "org.aitools.programd.processor.loadtime.ListenerProcessor",
                "org.aitools.programd.processor.loadtime.ListenersProcessor",
                "org.aitools.programd.processor.loadtime.PersonProcessor",
                "org.aitools.programd.processor.loadtime.Person2Processor",
                "org.aitools.programd.processor.loadtime.PredicateProcessor",
                "org.aitools.programd.processor.loadtime.PredicatesProcessor",
                "org.aitools.programd.processor.loadtime.PropertiesProcessor",
                "org.aitools.programd.processor.loadtime.PropertyProcessor",
                "org.aitools.programd.processor.loadtime.SentenceSplittersProcessor",
                "org.aitools.programd.processor.loadtime.SubstitutionsProcessor" } ;

    /** The fully-qualified name of {@link StartupElementProcessor} . */
    private static final String PROCESSOR_BASE_CLASS_NAME = "org.aitools.programd.processor.loadtime.StartupElementProcessor";

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