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
 ConditionProcessor is the one handling the many mutations of the CONDITION
 tag. First the variant at hand is detected and then the evaluation
 of the listitems below is triggered with that information. The way
 it's recursed allows for unlimited nesting to occur and to be executed
 in the proper order.
 @version 4.1.1
 @author  Thomas Ringate/Pedro Colla
*/
public class ConditionProcessor implements AIMLProcessor, Serializable {
        public String processAIML(int level, String ip, XMLNode tag, AIMLParser p) {

        if (tag.XMLChild == null) {
           return "";
        }

        String response= "";

        String nameval = p.getArg("name",tag.XMLAttr);
        String valueval= p.getArg("value",tag.XMLAttr);
        //System.out.println("*** CONDITION: name("+nameval+") value("+valueval+") ***");

        /*
          First form of condition <condition>
                                    <li name="xxx" value="xxx"></li>
                                    <li></li>
                                  </condition>
        */

        if ( (tag.XMLAttr.toLowerCase().indexOf("name=",0)  < 0) &&
             (tag.XMLAttr.toLowerCase().indexOf("value=",0) < 0) ) {

           //System.out.println("*** CONDITION: name==() value==() ***");
           response = p.ProcessListItem(level,ip,tag.XMLChild,1,nameval,valueval);
           return response;
        }


        /*
          Second form of condition <condition name="xxx" value="yyy">
                                   </condition>

        */

        if ( (tag.XMLAttr.toLowerCase().indexOf("name=",0)  >= 0) &&
             (tag.XMLAttr.toLowerCase().indexOf("value=",0) >= 0) ) {

           String varname = Classifier.getValue(nameval,ip);
           //System.out.println("*** CONDITION: name("+nameval+") value("+varname+") ***");
           if (varname.toLowerCase().equals(valueval.toLowerCase())) { //4.1.1 b12 case insensitive comparisson
              response = p.ProcessListItem(level,ip,tag.XMLChild,2,"","");
           }

           return response;
        }

        /*
          Third  form of condition <condition name="xxx">
                                     <li value="yyy"></li>
                                     <li></li>
                                   </condition>
        */
        if ( (tag.XMLAttr.toLowerCase().indexOf("name=",0)  >= 0) &&
             (tag.XMLAttr.toLowerCase().indexOf("value=",0) <  0) ) {

           //System.out.println("*** CONDITION: name!=() value==() ***");
           response = p.ProcessListItem(level,ip,tag.XMLChild,3,nameval,"");
           return response;
        }


         return "";
	}
}
