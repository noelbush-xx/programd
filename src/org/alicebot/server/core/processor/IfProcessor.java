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
 IfProcessor manages the IF tag and the variances on the inner THEN/ELSE
 tags.
 The condition is first validated and the THEN or ELSE clause is evaluated
 as appropriate. A special provision is made for the THEN clause not to
 be required if the condition is true and there are no ELSE structures
 present.
 @version 4.1.1
 @author  Thomas Ringate/Pedro Colla
*/
public class IfProcessor implements AIMLProcessor, Serializable {
      public String processAIML(int level, String ip, XMLNode tag, AIMLParser p) {

        /*
          Somehow this is an empty tag that got so far as here...
        */

        if (tag.XMLChild == null) {
           return "";
        }

        /*
          No attributes --> don't waste time
        */

        if (tag.XMLAttr.trim().equals("")) {
           return "";
        }

        /*
          Both the name= and value= tokens must be present
        */

        if ( (tag.XMLAttr.toLowerCase().indexOf("name=",0) < 0) ||
             (tag.XMLAttr.toLowerCase().indexOf("value=",0) < 0) ) {
           return "";
        }

        String response= "";

        /*
          Get the values for the arguments of the tag
        */

        String nameval = p.getArg("name",tag.XMLAttr);
        String valueval= p.getArg("value",tag.XMLAttr);
        //System.out.println("*** IF: name("+nameval+") value("+valueval+") ***");

        /*
          Now get the current value for the variable pointed by the tag
        */

        String varvalue= Classifier.getValue(nameval,ip);


        if (varvalue.toLowerCase().equals(valueval.toLowerCase())) { //4.1.1 b12 case insensitive comparisson

           /*
             variable value matches tag value, now get a pointer to
             the first THEN
           */
           //System.out.println("*** IF: MATCH (THEN) ***");
           XMLNode n = p.getnode(AIML10Tag.THEN,tag.XMLChild,1);

           /*
             the THEN is optional if there is no else, so it might
             not be there.
           */

           if (n == null) {

              //System.out.println("*** IF: THEN TAG NOT FOUND ***");

              /*
                Verify there are tags beneath and there is no ELSE
                tag in sight.
              */

              if ( (tag.XMLChild != null) &&
                   (p.getnode(AIML10Tag.ELSE,tag.XMLChild,1) == null) ) {

                   //System.out.println("*** IF: NO THEN && NO ELSE ***");
                   n = tag;

              } else {

                //System.out.println("*** IF: INVALID TAG FORMAT ***");
                return "";
              }
           }

           /*
             the pointer n held the root of the structure to evaluate,
             so evaluate the childs of it
           */

           response = response + p.evaluate(level++,ip,n.XMLChild);

        } else {

           /*
             variable value does not matches tag value, so get a pointer
             to the ELSE tag
           */

           XMLNode n = p.getnode(AIML10Tag.ELSE,tag.XMLChild,1);

           /*
             the ELSE tag is mandatory, so if there is none just leave
           */

           if (n == null) {
              return "";
           }

           /*
             Otherwise evaluate the child tags
           */

           response = response + p.evaluate(level++,ip,n.XMLChild);
        }
        return response;
      }
}
