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

import java.util.Vector;
import java.lang.*;
import java.net.*;
import java.io.*;


import org.alicebot.server.core.*;
import org.alicebot.server.core.util.*;
import org.alicebot.server.core.parser.*;


/**
 The Star processor is the responsible to handle the STAR tag,
 (NOTE): Currently still only one star is supported
 @version 4.1.1
 @author  Thomas Ringate/Pedro Colla
*/
public class StarProcessor implements AIMLProcessor, Serializable {
        public String processAIML(int level, String ip, XMLNode tag, AIMLParser p) {

         if (p.INPUT_STAR == null) {
            return "";
         }

         if (tag.XMLAttr.toLowerCase().indexOf("index=",0) < 0) {
            tag.XMLAttr = tag.XMLAttr + " index=\"1\"";
         }

         String indexname  = p.getArg("index",tag.XMLAttr.toLowerCase());
         int index = 1;
         if (indexname.equals("")) {
         } else {
           index = Integer.parseInt(indexname,10);
           if (index < 1) {
              index = 1;
           }
         }
         /*
           Vectors are 0 based so shift [1..N] --> [0..N-1]
         */
         index--;

         if (p.INPUT_STAR.isEmpty()) {
            return "";
         }

         int vsize= p.INPUT_STAR.size();
         if (index >= vsize) {
            return "";
         }

         /*
          Recover and pretty-fy the result
         */
         String response = (String)p.INPUT_STAR.get(index);
         response = Substituter.pretty(response);
         return response;
	}
}

