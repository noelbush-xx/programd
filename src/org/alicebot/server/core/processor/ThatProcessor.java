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
import java.util.StringTokenizer;

import org.alicebot.server.core.*;
import org.alicebot.server.core.util.*;
import org.alicebot.server.core.parser.*;


/**
 ThatProcessor process the THAT tag (template side) by retrieving the
 proper level of the stack of variables.
 Currently this tag ignores the 2nd index and operates the previous
 bot answer as one dimensional.
 @version 4.1.1
 @author  Thomas Ringate/Pedro Colla
*/

public class ThatProcessor implements AIMLProcessor, Serializable {
        public String processAIML(int level, String ip, XMLNode tag, AIMLParser p) {

         /**
           Recover the "index" attribute, ensure it's there or load
           a default
         */

         if (tag.XMLAttr.toLowerCase().indexOf("index=",0) < 0) {
            tag.XMLAttr = tag.XMLAttr + " index=\"1,1\"";
         }

         //System.out.println("*** THAT: Attr("+tag.XMLAttr+") ***");


         String indexname  = p.getArg("index",tag.XMLAttr.toLowerCase());
         if (indexname.equals("")) {
            indexname = "1,1";
         }

         //System.out.println("*** THAT: indexname("+indexname+") ***");

         /**
          Ensure the index is formed by two dimensions, manipulate
          defaults if not  
         */
         int x = indexname.indexOf(",");
         if (x < 0) {
            indexname = indexname + ",1";
            x = indexname.indexOf(",");
         }

         String ivar = indexname.substring(0,x);
         //System.out.println("*** THAT: x("+x+") ivar("+ivar+") ***");

         String jvar = "";
         if (indexname.length() > 1) {
            jvar = indexname.substring(x+1,indexname.length());
         } else {
            jvar = "1";
         }

         //System.out.println("*** THAT: jvar("+jvar+") ***");

         if (ivar.equals("")) {
           ivar = "1";
         }

         int index = Integer.parseInt(ivar,10);
         int jndex = Integer.parseInt(jvar,10);

         //System.out.println("*** THAT: index("+index+") jndex("+jndex+") ***");

         if ( (index <= 0) || (index > Globals.MAX_INDEX_DEPTH) ) {
            return "";
         }
         //System.out.println("*** THAT: i("+index+") j("+jndex+") ***");

         /**
          Get the THAT (previous bot answer block) based on the first
          index, it will be a composite answer potentially with many
          statements.
         */

         String response = Classifier.getValueIndex("that",index,ip);

         /**
          Segment each individual answer and the the one pointed
          by the second index
         */

         StringTokenizer st = new StringTokenizer(response,".");

         /**
          No token --> Just one answer
         */
         int ntoken = st.countTokens();
         if (ntoken == 0) {
            //System.out.println("*** THAT: ntoken == 0 ***");
            return response;
         }

         /**
          Requested an answer beyond the actual answer of the bot,
          return empty.
         */

         if (jndex > ntoken) {
            //System.out.println("*** THAT: jndex > ntoken("+ntoken+") ***");
            return "";
         }

         /**
          Lookup the nth answer, being 1 the last, 2 the previous,...
         */

         int nt = ntoken;
         while (nt != 0) {

           String token = st.nextToken();
           token = token.trim();
           //System.out.println("*** THAT: nt("+nt+") token("+token+") ***");

           if (nt == jndex) {
              //System.out.println("*** THAT: nt==jndex token("+token+") ***");
              return token;
           }
           nt--;
         }
         //System.out.println("*** THAT: return() ***");
         return "";
	}
}

