package org.alicebot.server.core.parser;

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
@version 4.1.2
*/

import org.alicebot.server.core.*;
import org.alicebot.server.core.util.*;
import org.alicebot.server.core.parser.*;
import org.alicebot.server.core.processor.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
//import java.util.*;

import javax.swing.*;

/**
  This class implements a node of the XML trie created by XMLParser
  @version 4.1.1
  @author  Thomas Ringate/Pedro Colla
*/
public class XMLNode extends Object {

        /*
         XML Types
        */

        /** TAG = XML Tag <tag>*/
        public static final int TAG     = 0;

        /** EMPTY = XML Empty <tag/> */
        public static final int EMPTY   = 1;

        /** DATA = Text */
        public static final int DATA    = 2;

        /** CDATA = Block of Data */
        public static final int CDATA   = 3;

        /** COMMENT = XML Comment */
        public static final int COMMENT = 4;

        /** ENDTAG </tag> */
        public static final int ENDTAG  = 5;

        /**
         XMLNode
         Record structure of an XML node
         */

         /** Type of the node (tag,empty,text,endtag,etc.) */
         public    int     XMLType;

         /** Actual data on the node or tag content  */
         public String     XMLData;

         /** String of attributed associated (only for tags, otherwise "") */
         public String     XMLAttr;

         /** Pointer to a child trie structure (only for tags, otherwise null) */
         public LinkedList XMLChild;


         /**
           Constructor, initializes the node into a default empty value
         */

         XMLNode() {
             this.XMLType = 0;
             this.XMLData = "";
             this.XMLAttr = "";
             this.XMLChild= null;
         }

}
