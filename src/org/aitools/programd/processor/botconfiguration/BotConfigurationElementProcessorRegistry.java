/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor.botconfiguration;

import org.aitools.programd.util.ClassRegistry;

/**
 * Registers {@link BotConfigurationElementProcessor} s for Program D.
 * 
 * @version 4.2
 * @since 4.1.3
 * @author Noel Bush
 */
public class BotConfigurationElementProcessorRegistry extends ClassRegistry
{
    /** The XML namespace URI for the Program D bot configuration file. */
    private static final String XMLNS = "http://aitools.org/programd/startup/2005/02/06";

    /** The list of processors (fully-qualified class names). */
    private static final String[] PROCESSOR_LIST =
        { "org.aitools.programd.processor.botconfiguration.BotProcessor",
          "org.aitools.programd.processor.botconfiguration.BotsProcessor",
          "org.aitools.programd.processor.botconfiguration.GenderProcessor",
          "org.aitools.programd.processor.botconfiguration.InputProcessor",
          "org.aitools.programd.processor.botconfiguration.LearnProcessor",
          "org.aitools.programd.processor.botconfiguration.ListenerProcessor",
          "org.aitools.programd.processor.botconfiguration.ListenersProcessor",
          "org.aitools.programd.processor.botconfiguration.PersonProcessor",
          "org.aitools.programd.processor.botconfiguration.Person2Processor",
          "org.aitools.programd.processor.botconfiguration.PredicateProcessor",
          "org.aitools.programd.processor.botconfiguration.PredicatesProcessor",
          "org.aitools.programd.processor.botconfiguration.PropertiesProcessor",
          "org.aitools.programd.processor.botconfiguration.PropertyProcessor",
          "org.aitools.programd.processor.botconfiguration.SentenceSplittersProcessor",
          "org.aitools.programd.processor.botconfiguration.SubstitutionsProcessor" } ;

    /** The fully-qualified name of {@link BotConfigurationElementProcessor} . */
    private static final String PROCESSOR_BASE_CLASS_NAME = "org.aitools.programd.processor.botconfiguration.BotConfigurationElementProcessor";

    /** The private member that initializes this class. */
    private static final BotConfigurationElementProcessorRegistry self = new BotConfigurationElementProcessorRegistry();

    private BotConfigurationElementProcessorRegistry()
    {
        super(XMLNS, PROCESSOR_LIST, PROCESSOR_BASE_CLASS_NAME);
    } 

    /**
     * @return the BotConfigurationElementProcessorRegistry itself
     */
    public static BotConfigurationElementProcessorRegistry getSelf()
    {
        return self;
    } 
}