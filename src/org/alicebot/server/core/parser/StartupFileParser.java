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

package org.alicebot.server.core.parser;

import org.alicebot.server.core.Bot;
import org.alicebot.server.core.Globals;
import org.alicebot.server.core.processor.loadtime.StartupElementProcessorRegistry;
import org.alicebot.server.core.util.Trace;


/**
 *  <code>StartupFileParser</code> processes a Program D startup file.
 */
public class StartupFileParser extends GenericParser
{
    private Bot currentBot;


    /**
     *  Initializes a <code>StartupFileParser</code>.
     */
    public StartupFileParser()
    {
        super.processorRegistry = StartupElementProcessorRegistry.getSelf();
    }


    public void setCurrentBot(Bot bot)
    {
        currentBot = bot;
    }


    public Bot getCurrentBot()
    {
        return currentBot;
    }
}
