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
  This class represent a simple XML Parser used by AIMLParser.java
  @version 4.1.1
  @author  Thomas Ringate/Pedro Colla
*/

public class XMLParser extends Object {

        /**
         XML Types 
        */
        static final int TAG     = 0;
        static final int EMPTY   = 1;
        static final int DATA    = 2;
        static final int CDATA   = 3;
        static final int COMMENT = 4;
        static final int ENDTAG  = 5;

        /**
         XMLRead

         This method parses an input string which contains a XML
         structure into the XML elements of it.

         Supported XML elements are
             DATA()    - Contents
             TAG()     - XML Tag
             EMPTY()   - XML Empty Tag
             COMMENTS()- Comments
             CDATA()   - CDATA Block

         --------------------------------------------------------
         Developer Information:

         This method has been written as part of the AIML 1.0
         ProgramD migration.

         It's really a Poor's man XML non-validating parser and
         should be replaced by a standard XML parsing object
         when selected. Has been written only as a small
         and centralized alternative to the also custom parsers
         used separately on AIMLReader and the old AIMLParser.
         */

        public LinkedList XMLRead(String input, LinkedList XMLList) {

           /*
            XML decoding Finite State Machine
            Internal States
           */

           final int    Q_TEXT   = 0;
           final int    Q_TAG    = 1;
           final int    Q_ENDTAG = 2;
           final int    Q_CDATA  = 3;
           final int    Q_COMMENT= 4;
           final int    Q_XML    = 5;
           final int    Q_XMLMARK= 6;

           /*
            Linked List local iterator
           */
            LinkedListItr XMLItr;

           /*
            Buffer and processing variables
           */

           XMLNode n;

           String  tag      = "";
           String  text     = "";
           String  response = "";
           String  token    = "";
           int     tagtype  = TAG;
           String  tagdata  = "";
           String  tagargs  = "";

           int     Q        = Q_TEXT;
           int     x        = 0;
           int     i        = 0;
           int     start    = 0;
           int     end      = 0;

           /*
            Initialize the Linked List Iterator
           */

           XMLItr = XMLList.zeroth( );

           /*
            Process the input String
           */

           while (x < input.length()) {

             token = String.valueOf(input.charAt(x));
             switch(Q) {

              /*-------------[ Initial State -> Parse Text ]------------*/
              case Q_TEXT : 

                         /*
                           See if a XML construct begins starting with "<"
                         */
                         if (token.equals("<")) {

                            /*
                             Move internal state of the machine into
                             parsing XML
                            */

                            Q = Q_XML;

                            /*
                             Any text already parsed?
                             If YES, process it as DATA()
                            */

                            if (!text.equals("")) {

                               n = new XMLNode();
                               n.XMLType = DATA;
                               n.XMLData = text;
                               n.XMLAttr = "";

                               XMLList.insert( n, XMLItr );
                               XMLItr.advance();

                               response = response+tagdata;
                            }

                            /*
                             Initialize parsing buffers
                            */
                            text = "";
                            tag  = "";
                         } else {
                           /*
                             No, then just store text
                           */

                           text = text + String.valueOf(token);
                         }
                         break;

              /*-------------[ XML State -> XML just detected ]---------*/

              case Q_XML :

                         /*
                          See if this is a comment
                         */

                         if (input.indexOf("!--",x)-x == 0) {
                            // Comment
                            Q = Q_COMMENT;
                            break;
                         }

                         /*
                          See if this is a XML Markup
                         */

                         if (input.indexOf("?",x)-x == 0) {
                            // Comment
                            Q = Q_XMLMARK;
                            break;
                         }


                         /*
                          See if this is a CDATA block
                         */

                         if (input.indexOf("![CDATA[",x)-x == 0) {
                            // CDATA
                            Q = Q_CDATA;
                            break;
                         }

                         /*
                          See if this is an end tag
                         */

                         if (input.indexOf("/",x)-x == 0) {
                            // EndTag
                            Q = Q_ENDTAG;
                            break;
                         }

                         /*
                          No, then it is a normal tag, change the finite
                          state machine state to process it
                         */

                         Q = Q_TAG;

              /*-------------[ XML Tag --> Normal/Empty Tag   ]---------*/
              case Q_TAG:

                         /*
                          Compute start and end of the tag, extract it
                         */
                         start  = x;
                         end    = input.indexOf(">",start);
                         tag    = input.substring(start,end);
                         tagargs= "";

                         /*
                          If the last character of the extracted block is
                          a "/" then it's an empty tag, otherwise a normal tag
                         */

                         if (String.valueOf(tag.charAt(tag.length()-1)).equals("/")) {
                            //Empty Tag
                            tagtype = EMPTY;

                            tag     = tag.substring(0,tag.length()-1);
                         } else {
                            //Normal Tag
                            tagtype = TAG;
                         }

                         /*
                          Trim leading and trailing blanks
                         */

                         tag = tag.trim();

                         /*
                          Up to the first blank it's the tag itself, anything
                          after it are arguments; split both.
                         */

                         i   = tag.indexOf(" ",0);
                         if (i >= 0) {
                            tagdata = tag.substring(0,i);
                            tagargs = " "+tag.substring(i+1,tag.length());
                         } else {
                            tagdata = tag;
                            tagargs = "";
                         }

                         /*
                          Mark the input buffer as processed up to the closing
                          ">" and reset the status of the FSM into text.
                         */
                         x      = end;
                         Q      = Q_TEXT;
                         text   = "";

                         /*
                          a non-empty tag? then process it as either
                          EMPTY() or TAG()
                         */


                         if (!tagdata.equals("")) {

                            n = new XMLNode();
                            n.XMLType = tagtype;
                            n.XMLData = tagdata;
                            n.XMLAttr = tagargs;
                            XMLList.insert( n, XMLItr );
                            XMLItr.advance();

                            tag      = "";
                         }
                         break;

              /*-------------[ End Tag --> Only normal tags   ]---------*/
              case Q_ENDTAG :
                         /*
                          Compute start and end of the tag, extract it
                         */

                         start = x;
                         end   = input.indexOf(">",start);
                         tag   = input.substring(start,end);

                         /*
                          Trim the tag and remove any arguments or other
                          otherwise creative constructs on it.
                         */

                         tag = tag.trim();
                         i   = tag.indexOf(" ",0);
                         if (i >= 0) {
                            tagdata = tag.substring(0,i);
                         } else {
                            tagdata = tag;
                         }

                         /*
                          Flag it as type ENDDATA()
                         */

                         tagtype = ENDTAG;
                         tagargs = "";

                         if (!tagdata.equals("")) {
                            n = new XMLNode();
                            n.XMLType = tagtype;
                            n.XMLData = tagdata;
                            n.XMLAttr = tagargs;
                            XMLList.insert( n, XMLItr );
                            XMLItr.advance();

                         }

                         /*
                          Mark the input buffer as processed till the end
                          of the tag, also reset the FSM back into text
                         */

                         x     = end;
                         Q     = Q_TEXT;

                         break;

              /*-------------[ CDATA   --> Transparent Block  ]---------*/
              case Q_CDATA  :
                         /*
                          Skip the CDATA leading header
                         */
                         start = x + 7;

                         /*
                          Extract the block of enclosed data
                         */

                         end   = input.indexOf("]]>",start);
                         tag   = input.substring(start,end);

                         /*
                          If not empty just process it as CDATA()
                         */

                         tagtype= CDATA;
                         tagdata= tag;
                         tagargs= "";

                         if (!tagdata.equals("")) {

                            n = new XMLNode();
                            n.XMLType = tagtype;
                            n.XMLData = tagdata;
                            n.XMLAttr = tagargs;

                            XMLList.insert( n, XMLItr );
                            XMLItr.advance();

                         }

                         /*
                          Mark the input buffer to continue after the tag,
                          reset the FSM to continue processing in text mode
                         */

                         x     = end + 2;
                         Q     = Q_TEXT;

                         break;

              /*-------------[ COMMENT --> Transparent Block  ]---------*/
              case Q_COMMENT:
                         /*
                          Skip the Comment leading header
                         */

                         start = x + 2;

                         /*
                          Detect the end of the comment and extract the
                          contents as a block
                         */

                         end   = input.indexOf("-->",start);
                         tag   = input.substring(start,end);

                         /*
                          Process it as COMMENT()
                         */

                         tagtype= COMMENT;
                         tagdata= tag;
                         tagargs= "";

                         if (!tagdata.equals("")) {

                            n = new XMLNode();
                            n.XMLType = tagtype;
                            n.XMLData = tagdata;
                            n.XMLAttr = tagargs;

                            XMLList.insert( n, XMLItr );
                            XMLItr.advance();

                         }

                         /*
                          Mark the input buffer to continue after the
                          closing tag, also reset the FSM into text mode
                         */

                         x     = end + 2;
                         Q     = Q_TEXT;

                         break;

              /*-------------[ XML Markup --> Transparent Block  ]---------*/
              case Q_XMLMARK:
                         /*
                          Skip the Comment leading header
                         */

                         start = x ;

                         /*
                          Detect the end of the comment and extract the
                          contents as a block
                         */

                         end   = input.indexOf("?>",start);
                         tag   = input.substring(start,end);

                         /*
                          Mark the input buffer to continue after the
                          closing tag, also reset the FSM into text mode
                         */

                         x     = end + 1;
                         Q     = Q_TEXT;

                         break;


              /*-------------[ INVALID CONSTRUCT              ]---------*/
              default:
                         /*
                          This option shouldn't really never be exercised
                         */

                         System.out.println("*** ERROR: INVALID TAG FORMAT ***");
                         return null;
             }

             // Advance to next character in the input buffer

             x++;
           }

           /*
            If the buffer ended while processing a text chunk
            it will be left unprocessed in the state machine
            buffers. A good moment as any to process it.
           */

           if ( (!text.equals("")) && (Q == Q_TEXT)) {

              n = new XMLNode();
              n.XMLType = DATA;
              n.XMLData = text;
              n.XMLAttr = "";

              XMLList.insert( n, XMLItr );
              XMLItr.advance();

           }

           return XMLList;
        }
        /**
         XMLFree
         This method frees the XML trie
        */
        public LinkedList XMLFree (LinkedList lt) {

             XMLNode        n;
             LinkedListItr  ltItr;

             ltItr = lt.zeroth();

             while (!ltItr.isPastEnd()) {

                n = (XMLNode)ltItr.retrieve();
                if (n != null) {

                   if (n.XMLChild != null) {
                      XMLFree(n.XMLChild);
                      n.XMLChild.makeEmpty();
                      n.XMLChild = null;
                   }
                   n = null;
                }

                ltItr.advance();

             }

             return null;
        }
        /**
         XMLScan
         This method receives a linear linked list with the parsed
         XML string and returns a XML tree with a hierarchical
         representation of the relation between elements.
        */
        public LinkedList XMLScan(LinkedListItr llItr, LinkedList ll, LinkedList lt) {

             XMLNode        n;
             XMLNode        t;
             LinkedListItr  ltItr;

             /*
               A tokenized representation of the XML stream is held in
               one linear single Linked List (ll) which is scanned and
               transformed into a trie (lt & descendants) using recursion
             */

             ltItr = lt.zeroth();

             while (!llItr.isPastEnd()) {

                /*
                  Retrieve a node from the Linked List
                */

                n = (XMLNode)llItr.retrieve();

                /*
                  Only process valid references
                */

                if (n != null) {

                   switch(n.XMLType) {

                    case TAG    :
                                  /*
                                    This node is a Tag, so create a node
                                    in the trie and recurse for it's childs
                                    <tag>...child...</tag>
                                  */

                                  t = new XMLNode();
                                  t.XMLType = n.XMLType;
                                  t.XMLData = n.XMLData;
                                  t.XMLAttr = n.XMLAttr;
                                  t.XMLChild= new LinkedList();
                                  t.XMLChild.makeEmpty();
                                  llItr.advance();
                                  t.XMLChild= XMLScan(llItr,ll,t.XMLChild);
                                  lt.insert( t, ltItr );
                                  ltItr.advance();
                                  break;
                    case EMPTY  :
                                  /*
                                    This node is Empty, so create a node
                                    on the trie but there is no need to
                                    recurse since empties doesn't have
                                    childs. (<tag/>)
                                  */
                                  t = new XMLNode();
                                  t.XMLType = n.XMLType;
                                  t.XMLData = n.XMLData;
                                  t.XMLAttr = n.XMLAttr;
                                  lt.insert( t, ltItr );
                                  ltItr.advance();
                                  break;
                    case DATA   :
                    case CDATA  :
                                  /*
                                    This is either text or CDATA, handle
                                    both equally creating a node on the
                                    trie.
                                  */

                                  t = new XMLNode();
                                  t.XMLType = n.DATA;
                                  t.XMLData = n.XMLData;
                                  t.XMLAttr = "";
                                  lt.insert( t, ltItr );
                                  ltItr.advance();
                                  break;
                    case ENDTAG :
                                  /*
                                    If there is an end tag is because
                                    this method is recursing on a lower
                                    level (or the XML is non-balanced).
                                  */
                                  return lt;
                    default     :
                                  break;
                   }
                } else {
                   System.out.println("*** ERROR: XML element is null ***");
                }
                llItr.advance();
             }

             return lt;
        }
        /**
         XMLLoad
         This method receives a string with the XML segment to decode
         returns the full evaluation of a trie with the hierarchical
         representation of it.
        */
        public LinkedList XMLLoad(String XMLBuff) {

             LinkedList     ll;
             LinkedList     lt;

             XMLNode        n;
             LinkedListItr  llItr;


             ll = new LinkedList();
             lt = new LinkedList();

             /*
               Initialize a Linked List object, process the XML tags
               on the buffer string and return one token (text or tag)
               per Linked List node (linear representation)
             */

             ll.makeEmpty();
             ll = XMLRead(XMLBuff,ll);

             if (ll == null) {
                System.out.println("*** ERROR Invalid XML("+XMLBuff+") ***");
                return null;
             }

             /*
               Reset to the begining of the linear representation Linked
               List
             */

             llItr = ll.zeroth();
             llItr.advance();

             /*
               The linear Linked List isn't really useful to operate with
               a recursive parser, so we transform it into a trie that
               captures the parent-child relations between elements on the
               list, also possible XML tags are reduced in the process
               as Text, Tags Elements and Empty Tags Elements.
               Performance considerations should counsel to come straigth
               from XML into this tree representation, for clarity sake
               I'm doing this in two steps, if unacceptable performance
               is obtained both could always be merged with a somewhat
               more obscure algorithm. Time will tell.
             */

             lt = XMLScan(llItr,ll,lt);

             /*
               The Linked List is disposed, if this step is not performed
               the JVM garbage collector will dispose it anyway after some
               time of being de-referenced the objects. However, it will
               drain unnecessarily memory resources, so the process is
               expedited here.
             */

             ll = XMLFree(ll);

             return lt;
        }
}
