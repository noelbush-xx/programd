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
 RandomProcessor is the responsible to manage the RANDOM tag, the number
 of ListItem structures  bellow (at the level inmediately beneath) are
 counted and based on a random pick one of them is choosen. The
 selected ListItem structure is then evaluated, recursion on evaluation
 allows other conditional structures to be processed there as needed.
 @version 4.1.1
 @author  Thomas Ringate/Pedro Colla
*/
public class RandomProcessor implements AIMLProcessor, Serializable {
      public String processAIML(int level, String ip, XMLNode tag, AIMLParser p) {

        /*
          Somehow this is an empty tag that got so far as here...
        */

        if (tag.XMLChild == null) {
           return "";
        }

        String response= "";
        int numbernodes = p.countnode(AIML10Tag.LI,tag.XMLChild,false);

        if (numbernodes == 0) {
           return "";
        }

        XMLNode n = null;
        if (numbernodes == 1) {
           n = p.getnode(AIML10Tag.LI,tag.XMLChild,1);
           response = response + p.evaluate(level++,ip,n.XMLChild);
           return response;
        }

        /*
          select a random elemento of the listitem
        */
        double r          = 0.0;
        int    random_amt = 0;

        while ( (random_amt < 1) || (random_amt > numbernodes) ) {
          r = Classifier.RNG.nextDouble();
          r = r + 0.05;
          random_amt = (int)( (double)(numbernodes) * r );
        }
        n = p.getnode(AIML10Tag.LI,tag.XMLChild,random_amt);
        response = response + p.evaluate(level++,ip,n.XMLChild);

        return response;
      }
}
