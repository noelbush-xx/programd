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

package org.alicebot.server.net;

import java.io.IOException;

import org.alicebot.server.core.BotProcess;


/**
 *  Specifies the methods that an http server must implement
 *  in order to be compatible with Program D.
 *
 *  @author Noel Bush
 */
public interface AliceCompatibleHttpServer extends BotProcess
{
    /**
     *  Configures the http server given
     *  the path or URL to a configuration file.
     *
     *  @param configFilePath   path or URL for a configuration file
     *
     *  @throws IOException if the configuration file cannot be read
     */
    public void configure(String configFilePath) throws IOException;


    /**
     *  Starts the http server.
     */
    public void run();
}