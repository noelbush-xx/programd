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

package org.alicebot.server.net.listener;

import org.alicebot.server.core.Graphmaster;
import org.alicebot.server.core.util.ClassRegistry;


/**
 *  Registers {@link AliceChatListener}s.
 *
 *  @since  4.1.3
 *  @author Noel Bush
 */
public class AliceChatListenerRegistry extends ClassRegistry
{
    /** The version of Program D in which these listeners are included. */
    private static final String VERSION = Graphmaster.VERSION;

    /** The list of processors (fully-qualified class names). */
    private static final String[] PROCESSOR_LIST = {"org.alicebot.server.net.listener.AliceAIM",
                                                    "org.alicebot.server.net.listener.AliceICQ",
                                                    "org.alicebot.server.net.listener.AliceIRC"};

    /** The fully-qualified name of {@link AliceChatListener}. */
    private static final String PROCESSOR_BASE_CLASS_NAME = "org.alicebot.server.net.listener.AliceChatListener";

    /** The private member that initializes this class. */
    private static final AliceChatListenerRegistry self = new AliceChatListenerRegistry();


    private AliceChatListenerRegistry()
    {
        super(VERSION, PROCESSOR_LIST, PROCESSOR_BASE_CLASS_NAME);
    }
    
    
    public static AliceChatListenerRegistry getSelf()
    {
        return self;
    }
}
