package org.alicebot.server.core.AIMLprocessor;

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
import org.alicebot.server.core.AIMLparser.*;

/**
 ThatStarProcessor evaluates the THATSTAR tag, currently it's a very
 simple processor that operates only for index=1.
 @version 4.1.1
 @author  Thomas Ringate/Pedro Colla
*/
public class ThatStarProcessor implements AIMLProcessor, Serializable {
        public String processAIML(int level, String ip, XMLNode tag, AIMLParser p) {

         if (tag.XMLAttr.toLowerCase().indexOf("index=",0) < 0) {
            tag.XMLAttr = tag.XMLAttr + " index=\"1\"";
         }

         String indexname  = p.getArg("index",tag.XMLAttr.toLowerCase());
         int index = 1;
         if (indexname.equals("")) {
         } else {
           index = Integer.parseInt(indexname,10);
           if ( (index < 1) || (index > 1) ) { //Only index="1" supported at this time
              index = 1;
           }
         }
         return Substituter.pretty(Graphmaster.THAT_STAR);
	}
}

