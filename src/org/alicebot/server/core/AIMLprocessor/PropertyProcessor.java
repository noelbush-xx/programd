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
  Property processor is the responsible to set bot property values to be
  retrieved with <bot name="property"/>, since it is a non-standard AIML
  tag the parser will allow it to be executed only during configuration.
  @version 4.1.1
  @author  Thomas Ringate/Pedro Colla
*/
public class PropertyProcessor implements AIMLProcessor, Serializable {
        public String processAIML(int level, String ip, XMLNode tag, AIMLParser p) {
         String property = p.getArg("name",tag.XMLAttr);
         if (property.equals("")) {
            return "";
         }
         String value    = p.getArg("value",tag.XMLAttr);
         Globals.setValue(property,value);
         return "";
	}
}

