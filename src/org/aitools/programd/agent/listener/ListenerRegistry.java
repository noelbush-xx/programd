/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.agent.listener;

import org.aitools.programd.graph.Graphmaster;
import org.aitools.programd.util.ClassRegistry;

/**
 * Registers {@link Listener}s.
 * 
 * @since 4.1.3
 * @author Noel Bush
 */
public class ListenerRegistry extends ClassRegistry
{
    /** The version of Program D in which these listeners are included. */
    private static final String VERSION = Graphmaster.VERSION;

    /** The list of processors (fully-qualified class names). */
    private static final String[] PROCESSOR_LIST =
        { "org.aitools.programd.agent.listener.AIMListener", "org.aitools.programd.agent.listener.ICQListener",
                "org.aitools.programd.agent.listener.IRCListener" };

    /** The fully-qualified name of {@link Listener}. */
    private static final String PROCESSOR_BASE_CLASS_NAME = "org.aitools.programd.agent.listener.Listener";

    /** The private member that initializes this class. */
    private static final ListenerRegistry self = new ListenerRegistry();

    private ListenerRegistry()
    {
        super(VERSION, PROCESSOR_LIST, PROCESSOR_BASE_CLASS_NAME);
    }

    public static ListenerRegistry getSelf()
    {
        return self;
    }
}