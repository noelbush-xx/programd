/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.listener;

import org.aitools.programd.Core;
import org.aitools.programd.util.ClassRegistry;

/**
 * Registers {@link Listener} s.
 * 
 * @since 4.1.3
 * @author Noel Bush
 */
public class ListenerRegistry extends ClassRegistry
{
    /** The list of processors (fully-qualified class names). */
    private static final String[] PROCESSOR_LIST =
        { "org.aitools.programd.listener.AIMListener", "org.aitools.programd.listener.ICQListener",
                "org.aitools.programd.listener.IRCListener" } ;

    /** The fully-qualified name of {@link Listener} . */
    private static final String PROCESSOR_BASE_CLASS_NAME = "org.aitools.programd.listener.Listener";

    /** The private member that initializes this class. */
    private static final ListenerRegistry self = new ListenerRegistry();

    private ListenerRegistry()
    {
        super(Core.VERSION, PROCESSOR_LIST, PROCESSOR_BASE_CLASS_NAME);
    } 

    /**
     * Returns the ListenerRegistry's singelton self.
     * 
     * @return ListenerRegistry
     */
    public static ListenerRegistry getSelf()
    {
        return self;
    } 
}