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

/**
 PersonProcessor is the responsible to produce 1st to 3rd shift person
 transformations when the PERSON tag is used. The tags beneath it are
 evaluated and the result is shifted using the person method in the
 Substituter class.
 @version 4.1.1
 @author  Thomas Ringate/Pedro Colla
*/
public class PersonProcessor implements AIMLProcessor, Serializable {
        public String processAIML(int level, String ip, XMLNode tag, AIMLParser p) {

         /*
           Evaluate the underlying template structure first
         */
         String response = p.evaluate(level++,ip,tag.XMLChild);

         /*
           Now perform the person substitutions
         */
         response = Substituter.person(response);

         /*
           This might well be regarded as an overkill, but we want to
           keep the capability to resolve AIML tags on the substitutions
           so handle it just like a template on itself.
           Most of the time it won't imply a serious performance
           degradation.
         */
         response = p.processResponse(ip,response);
         return response;

	}
}

