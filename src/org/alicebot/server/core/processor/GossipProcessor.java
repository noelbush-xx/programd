package org.alicebot.server.core.processor;

/**
Alice Program D
Copyright (C) 1995-2001, A.L.I.C.E. AI Foundation

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
USA.

@author  Richard Wallace
@author  Jon Baer
@author  Thomas Ringate/Pedro Colla
@version 4.1.1
*/

//import java.util.*;
import java.lang.*;
import java.net.*;
import java.io.*;

import org.alicebot.server.core.*;
import org.alicebot.server.core.util.*;
import org.alicebot.server.core.parser.*;
import org.alicebot.server.core.logging.*;

/**
 GossipProcessor is the responsible to handle the GOSSIP tag, the
 tags beneath it are processed into a response and the result is placed
 in either the choosen gossip file or the default one.
 @version 4.1.1
 @author  Thomas Ringate/Pedro Colla
*/
public class GossipProcessor implements AIMLProcessor, Serializable {
        public String processAIML(int level, String ip, XMLNode tag, AIMLParser p) {

         /*
           The default GOSSIP file is .\logs\gossip.log
         */
         String LogName = Log.GOSSIP;

         /*
           Get the actual Gossip
         */
         String response = p.evaluate(level++,ip,tag.XMLChild);

         /*
           Post the actual entry on the log
         */

         Log.log(response, LogName);
         return "";

	}
}

