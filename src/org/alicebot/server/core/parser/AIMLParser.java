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
import org.alicebot.server.core.parser.*;
import org.alicebot.server.core.*;
import org.alicebot.server.core.util.*;
import org.alicebot.server.core.responder.*;
import org.alicebot.server.core.processor.*;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.Vector;

import javax.swing.*;


public class AIMLParser extends Object {

        public int         length;
        public int         depth;

        public Globals     globals;
	public Interpreter interpreter;
        public Classifier  classifier;
	public Graphmaster graphmaster;
        public String      bot;
        public Vector      INPUT_STAR;
        public Vector      THAT_STAR;
        public Vector      TOPIC_STAR;

        /**
	 * The AIML Parser.
         * This is the class responsible to interpret and execute
         * templates producing actual responses out of them.
         * @version 4.1.1
         * @author  Thomas Ringate/Pedro Colla
         */
	public AIMLParser()  {
		length = 0;
                depth  = 0;
	}

        /**
	 * The AIML Parser (with depth known).
         */
	public AIMLParser(int depth)  {
		this();
		this.depth = depth;
	}

        /**
	 * AIML Parser (with depth known and a given interpreter).
         */
	public AIMLParser(int depth, Interpreter interpreter)  {
		this();
                this.depth       = depth;
		this.interpreter = interpreter;
	}
        /**
          getArg
          A utility method to grab arguments of a tag by name.
          The method looks in the supplied argument string and parses
          the argument name looking for the structure argname= and
          returns whatever is after the = till the next blank or the
          end of the string.
          It is used mostly to extract attributes out of AIML tags
         */
	public static String getArg(String argname, String args) {
		int m;
		String argvalue="";
		String argpattern = new String(argname+"=\"");
		m = args.indexOf(argpattern);
		if (m >= 0) { // locate predicate value:
			args = (m+argpattern.length() >= args.length()) ? "" : args.substring(m+argpattern.length(), args.length());
			m = args.indexOf("\"");
			if (m >= 0) {
				argvalue = args.substring(0, m);
			}
		}
		return (argvalue);
	}
        /**
         This method evaluates recursively the XML trie holding the
         template tags. Both the level and the ip of the client is
         carried thru recursion.
        */
        public String evaluate(int level, String ip,LinkedList lt) {

             String         response = "";
             LinkedListItr  ltItr;
             XMLNode        n;

             /*
               Verify we have something to work with
             */
              
             if (lt == null) {
                return "";
             }

             /*
               Point to the start of the XML trie to parse
             */

             ltItr = lt.zeroth();
             //ltItr.advance();

             /*
               Navigate thru the entire level of it
             */

             while (!ltItr.isPastEnd()) {

               n = (XMLNode)ltItr.retrieve();
               if (n != null) {
                  switch(n.XMLType) {

                   /*
                     Collect and process tags
                   */

                   case n.TAG   :
                   case n.EMPTY :
                               /**
                                If it is during load time only certain
                                tags could be evaluated.
                               */
                               if (Graphmaster.loadtime == true) {
                                  n.XMLData = n.XMLData.toLowerCase();

                               /**
                                 If not LEARN or LOAD bypass it during load
                               */
                                  if ( (!n.XMLData.equals(AIML10Tag.LEARN))    &&
                                       (!n.XMLData.equals(AIML10Tag.PROPERTY)) &&
                                       (!n.XMLData.equals(AIML10Tag.LOAD)) ) {
                                     break;
                                  }
                               }

                               /**
                                 Outside loadtime the full tagset must
                                 be explored
                               */

                               response = response + processtag(level,ip,n);
                               break;

                   /*
                     Text chunks just add them to the response
                   */
                   case n.DATA  :
                   case n.CDATA :
                               response = response + n.XMLData ;
                               break;
                   default    :
                               break;
                  }
               }
               ltItr.advance();
             }
             return response;
        }
        /**
         formattag
         This method format a tag out of a XML node into pure AIML,
         it is used mostly when a tag can not be evaluated and then
         literally included in the output.
        */
        public String formattag (int level, String ip, XMLNode tag) {

          /*
            This is a recursive beast, but a given level always
            starts with an empty answer.
          */

          String response = "";

          /*
            Format according with the XML element type, handling of
            text has been added for generality since no text will
            ever be passed under the main usage of the method.
          */

          switch(tag.XMLType) {

             case tag.TAG    :
                               /* This is a XML tag, so it potentially
                                  might have childs. Format the head
                               */
                               response = response + "<" + tag.XMLData ;

                               /*
                                  Include any attribute present
                               */
                               if (!tag.XMLAttr.equals("")) {
                                  response = response + tag.XMLAttr;
                               }
                               /*
                                  Close the head
                               */
                               response = response + ">";

                               /*
                                  If any child present resolve it
                                  recursively.
                               */
                               if (tag.XMLChild != null) {
                                  response = response + evaluate(level++,ip, tag.XMLChild);
                               }

                               /*
                                  Format now the end tag
                               */
                               response = response + "</" + tag.XMLData + ">" ;
                               break;
             case tag.EMPTY  :
                               /* Same as in the case of TAG but no
                                  child recursion this time.
                               */
                               response = response + "<" + tag.XMLData ;
                               if (!tag.XMLAttr.equals("")) {
                                  response = response + tag.XMLAttr;
                               }
                               response = response + "/>";
                               break;
             case tag.DATA   :
             case tag.CDATA  :
                               /*
                                 Format text
                               */
                               response = response + tag.XMLData;
                               break;

             default     :
                               break;

          }
          return response;


        }

        /**
         countnode
         Method to count the number of nodes of a given type at a
         particular level of the XML trie.
         It is used mostly in connection with the random tag in order to
         see how many candidate listitem structures are beneath it and
         to set the upperlimit of the random number roll
        */
        public int countnode( String tagname, LinkedList lt, boolean allnodes) {

             String         response = "";
             LinkedListItr  ltItr;
             XMLNode        n;
             int            numbernodes = 0;

             /*
               Verify we have something to work with
             */
              
             if (lt == null) {
                return 0;
             }

             /*
               Point to the start of the XML trie to parse
             */

             ltItr = lt.zeroth();
             ltItr.advance();

             /*
               Navigate thru the entire level of it
             */

             while (!ltItr.isPastEnd()) {

               n = (XMLNode)ltItr.retrieve();
               if (n != null) {

                  switch(n.XMLType) {

                   /*
                     Collect and process only tag elements and empty tags
                   */

                   case n.TAG   :
                   case n.EMPTY :

                          /*
                            Only the desired one
                          */

                          n.XMLData = n.XMLData.toLowerCase();

                          if ( (!n.XMLData.equals(tagname)) &&
                               (allnodes == false) ) {
                                break;
                          }

                          numbernodes++;
                          break;


                   /*
                     just ignore everything else
                   */
                   default      :
                                 break;
                  }
               }
               ltItr.advance();
             }
          return numbernodes;


        }

        /**
         getnode
         Method to retrieve the ordernode-th node of a given tag on a
         particular level of the XML trie.
         It is used typically to find out specific tags beneath a given
         tag being evaluated (i.e. a THEN/ELSE tag beneath a IF, a given
         LI tag beneath RANDOM, etc.
        */
        public XMLNode getnode( String tagname, LinkedList lt, int ordernode) {

             String         response = "";
             LinkedListItr  ltItr;
             XMLNode        n;

             /*
               Verify we have something to work with
             */
              
             if (lt == null) {
                return null;
             }

             /*
               Point to the start of the XML trie to parse
             */

             ltItr = lt.zeroth();
             ltItr.advance();

             /*
               Navigate thru the entire level of it
             */

             while (!ltItr.isPastEnd()) {

               n = (XMLNode)ltItr.retrieve();
               if (n != null) {

                  switch(n.XMLType) {

                   /*
                     Collect and process only tag elements and empty tags
                   */

                   case n.TAG   :
                   case n.EMPTY :

                          /*
                            Only the desired one
                          */

                          n.XMLData = n.XMLData.toLowerCase();
                          if ( !n.XMLData.equals(tagname)) {
                                break;
                          }

                          ordernode--;

                          /*
                            we've found the one we're looking for when
                            ordernode is zero
                          */

                          if (ordernode == 0) {
                             return n;
                          }

                          /*
                            otherwise continue
                          */

                          break;


                   /*
                     just ignore everything else
                   */
                   default      :
                                 break;
                  }
               }
               ltItr.advance();
             }
          return null;
        }

        /**
         ProcessListItem
         method to evaluate LI (ListItem) under
         CONDITION; this method is called from the ConditionProcessor
         The ListItemType would define which type of structure has to
         be expected indide the condition
           (1) the LI contains both the name and value.
           (2) the LI does not contains either name nor value
           (3) the LI does not contains the name but contains value
        */
        public String ProcessListItem(int level, String ip, LinkedList lt, int ListItemType, String nameval, String valueval) {

             String         response = "";
             LinkedListItr  ltItr;
             XMLNode        n;

             /*
               Verify we have something to work with
             */
              
             if (lt == null) {
                return "";
             }

             /*
               Point to the start of the XML trie to parse
             */

             ltItr = lt.zeroth();
             ltItr.advance();

             String varvalue = "";
             String livalue  = "";
             String liname   = "";

             /*
               The variable is on the parent <condition> tag, so evaluate
               only once the current value of it.
             */

             if (ListItemType == 3) {
                varvalue = Classifier.getValue(nameval,ip);
             }

             /*
               Navigate thru the entire level of it
             */

             while (!ltItr.isPastEnd()) {

               n = (XMLNode)ltItr.retrieve();
               if (n != null) {

                  switch(n.XMLType) {

                   /*
                     if plain text just pass it, how free floating under
                     a condition has to be handled is actually undefined
                     in the AIML 1.0 spec; I'm choosing to process it.
                   */

                   case n.DATA  :
                   case n.CDATA :
                          response = response + n.XMLData;
                          break;

                   case n.EMPTY :
                          response = response + processtag(level++,ip,n);
                          break;
                   /*
                     Collect and process <LI> tags
                   */

                   case n.TAG   :

                          /*
                            Only <li></li> structures allowed
                          */

                          n.XMLData = n.XMLData.toLowerCase();
                          if (!n.XMLData.equals(AIML10Tag.LI)) {
                             response = response + processtag(level++,ip,n);
                             break;
                          }

                          /*-----
                           Now operate based on the ListItemType which
                           defines what underlying listitem structure
                           has to be expected based on the parent condition
                          ------*/
                          switch(ListItemType) {
                            case 1 : // First form  <li name="xx" value="yy"></li>

                                 /*
                                   Look for tokens in the XML attributes for name and
                                   value, if none are present this is an unqualified
                                   <li> and gets evaluated. The processing of the
                                   structure continues after it so the <li> tag could
                                   be anywhere under <condition> and not necessarily
                                   at the end.
                                 */
                                 if ( (n.XMLAttr.toLowerCase().indexOf("name=",0)  < 0) &&
                                      (n.XMLAttr.toLowerCase().indexOf("value=",0) < 0) ) {
                                    response = response + evaluate(level++,ip,n.XMLChild);
                                    break;
                                 }

                                 /*
                                   If either the name or the value has not been indicated just
                                   ignore the tag.
                                 */

                                 if ( (n.XMLAttr.toLowerCase().indexOf("name=",0) < 0) ||
                                      (n.XMLAttr.toLowerCase().indexOf("value=",0)< 0)) {
                                    break;
                                 }

                                 /*
                                   Now recover the actual values of the attributes name
                                   and value.
                                 */

                                 liname = getArg("name",n.XMLAttr);
                                 livalue= getArg("value",n.XMLAttr);

                                 /*
                                   Get the current value of the variable pointed by name
                                 */

                                 varvalue = Classifier.getValue(liname,ip);

                                 /*
                                  If the value of the variable equals the value stated
                                  in the <li> tag then process, otherwise skip.
                                 */

                                 if (livalue.toLowerCase().equals(varvalue.toLowerCase())) {    //4.1.1 b12 case insensitive comparisson
                                    response = response + evaluate(level++,ip,n.XMLChild);
                                    return response;
                                 }
                                 break;

                                 /*-----
                                   Second form <li></li> unqualified
                                 -------*/

                            case 2 : //2nd form <li *></li>

                                 /*
                                   Unconditionally evaluate as long as it is a
                                   <li></li> structure.
                                 */
                                 response = response + evaluate(level++,ip,n.XMLChild);
                                 break;

                                 /*-----
                                   Third form <li value="yyy"></li>
                                 ------*/

                            case 3 : //3rd form <li value="yy"></li>

                                 /*
                                   If the value token is found compare with the actual
                                   contents of the variable pointed by condition,
                                   otherwise evaluate inconditionally. Hits on either
                                   will stop the current evaluation.
                                 */
                                 if (n.XMLAttr.toLowerCase().indexOf("value=",0) >= 0) {
                                    livalue= getArg("value",n.XMLAttr);

                                    if (livalue.toLowerCase().equals(varvalue.toLowerCase())) { //4.1.1 b12 case insensitive comparisson
                                       response = response + evaluate(level++,ip,n.XMLChild);
                                       return response;
                                    }
                                 } else {
                                   response = response + evaluate(level++,ip,n.XMLChild);
                                   return response;
                                 }

                                 break;
                            default :
                                 break;
                                 }
                   default      :
                                 break;
                  }
               }
               ltItr.advance();
             }
             return response;
        }
        /**
          virtualTag
          This is a method to create a mini template with a given tag
          and an optional child tag, once created it's evaluated
          recursively.
          This method is used mostly to map certain tags as combinations
          of other tags avoiding to write specific evaluation code for
          them. Extensively used to map AIML 0.9 tags into their AIML 1.0
          equivalents and also to solve non-primitive AIML 1.0 tags.
        */
        public String virtualTag (int level, String ip, String roottag,int roottype,  String rootattr, String childtag, int childtype) {

          String response = "";
          /*
            if the root tag is empty there is no point in continuing
          */

          if (roottag.equals("")) {
             return "";
          }

          /*
            Create an AIML node on the fly with the content of the root
            tag to virtualize, including attributes.
          */

          XMLNode n = new XMLNode();

          n.XMLType = roottype;
          n.XMLData = roottag;
          n.XMLAttr = rootattr;

          /*
            Create a XML trie for that tag in order to be able to process
            it with normal methods of this class like a template coming
            from a pattern matching.
          */

          LinkedList lt = new LinkedList();
          lt.makeEmpty();

          /*
            Prepare the list to receive inserts, but don't do them yet
          */

          LinkedListItr ltItr;
          ltItr = lt.zeroth();

          /*
            Now, process the child tag if any.
            Obviously the root tag can not be of empty type and something
            has to be informed on the tag itself
          */

          XMLNode nchild = new XMLNode();
          LinkedList ltchild = new LinkedList();

          if ( ( roottype  == n.TAG) &&
               (!childtag.equals(""))  &&
              ((childtype  == n.EMPTY) || (childtype == n.DATA)) ) {

             /*
               Create a XML node for the child tag now, note the
               child tag is assumed to be an empty one with no
               attributes (reasonable assumption?), it's functional
               for AIML 1.0 anyway.
             */

             switch(childtype) {

               case n.EMPTY    :  
                                 nchild.XMLType = nchild.EMPTY;
                                 nchild.XMLData = childtag;
                                 nchild.XMLAttr = "";
                                 break;
               case n.DATA     :
               case n.CDATA    :
                                 nchild.XMLType = nchild.DATA;
                                 nchild.XMLData = childtag;
                                 nchild.XMLAttr = "";
                                 break;
             }

             /*
               Insert the child tag on the child trie, please note
               we're still holding the root tag
             */

             LinkedListItr ltchildItr;
             ltchildItr = ltchild.zeroth();

             ltchild.insert( nchild, ltchildItr );
             ltchildItr.advance();

             /*
               And this is because the root tag must point to the
               child tag, so make that reference now
             */

             n.XMLChild = ltchild;

          }

          /*
            Now insert the root tag
          */

          lt.insert( n, ltItr );
          ltItr.advance();

          /*
            We have now a XML trie just like the one we got from
            parsing raw templates out of the pattern matching process,
            so evaluate it
          */


          response = response + evaluate(level++,ip,lt);

          /*
            De-reference all the dynamic fiesta, just to expedite the
            garbage processor.
          */

          nchild   = null;
          ltchild  = null;
          n        = null;
          lt       = null;

          return response;

        }
        /**
         processtag
         This method process recursively tags, the processing order
         if from top to bottom and from inside out in nesting.
         The actual tag to be processed is driven by the evaluate
         method so a strict sequencing is performed. For each valid
         tag detected the associated processor, resolution code or
         virtualtag processor is activated.
         If the tag is not catched by the method it's resolved back
         into XML and included literally on the output
        */
        public String processtag(int level, String ip, XMLNode tag) {

          AIMLProcessor p;
          String        response = "";

          /*
           Tags are explored sequentially now until all the tagset
           is addressed. Please note the sequence of exploration has
           performance implications (often used tags should be
           handled first) but the actual sequence within the template
           is preserved since it's driven by the caller method
           (usually evaluate) and not by the order in which the tags
           are solved.
          */

           /*
             Is it a valid tag object?
           */

           if (tag == null) {
              return "";
           }

           /*
             Normalize it for processing
           */
             tag.XMLData = tag.XMLData.toLowerCase();

           /*--------------------------------------------------------*/
           /*                Tag Dispatcher                          */
           /*--------------------------------------------------------*/

           /**
            An optimization is performed here, at load time only certain
            tags could be evaluated, so evaluate them first both for
            AIML 1.0 and AIML 0.9
           */

           /*
             <learn></learn>
           */
             if ( (tag.XMLData.equals(AIML10Tag.LEARN)) && (tag.XMLType == tag.TAG) ) {
                p = new LearnProcessor();
                return p.processAIML(level++,ip,tag,this);
             }

           /*
             <load filename="xxxx"/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.LOAD)) && (tag.XMLType == tag.EMPTY) ) {
                return virtualTag(level,ip,AIML10Tag.LEARN,tag.TAG,"",getArg("filename",tag.XMLAttr),tag.DATA);
             }

           /*
             <property name="property" value="value"/>
             This tag is non-standard AIML and it's allowed to be used ONLY
             during load time.
           */

             if ( (tag.XMLData.equals(AIML10Tag.PROPERTY)) &&
                  (tag.XMLType == tag.EMPTY)               &&
                  (Graphmaster.loadtime == true) ) {

                p = new PropertyProcessor();
                return p.processAIML(level++,ip,tag,this);
             }

           /**
             If it happens to be load time then the rest of the dispatcher
             is not enabled yet
           */

             if (Graphmaster.loadtime == true) {
                return "";
             }

           /*********************************************************
            *AIML 1.0 Official AIML Tagset                          *
            *Each tag get associated with it's own processor unless *
            *the resolution is really trivial and made inline.      *
            *********************************************************/

           /**
            Tags are evaluated in roughly the priority dictated by the  
            current usage of them in the AIML standard set in order 
            to minimize the search latency for the most frequently used
           */

           /*
             <srai></srai>
           */
             if ( (tag.XMLData.equals(AIML10Tag.SRAI)) && (tag.XMLType == tag.TAG) ) {
                p = new SRAIProcessor();
                return p.processAIML(level++,ip,tag,this);
             }

           /*
             <star index="N"/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.STAR)) && (tag.XMLType == tag.EMPTY) ) {
                p = new StarProcessor();
                return p.processAIML(level++,ip,tag,this);
             }


           /*
             <set name="varname"></set>
           */
             if ( (tag.XMLData.equals(AIML10Tag.SET)) && (tag.XMLType == tag.TAG) ) {
                p = new SetProcessor();
                return p.processAIML(level++,ip,tag,this);
             }

           /*
             <think></think>
           */
             if ( (tag.XMLData.equals(AIML10Tag.THINK)) && (tag.XMLType == tag.TAG) ) {
                p = new ThinkProcessor();
                return p.processAIML(level++,ip,tag,this);
             }

           /*
             <get name="varname"/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.GET)) ) {  //4.1.2 b1 Support for <get/> & <get></get>
                p = new GetProcessor();
                return p.processAIML(level++,ip,tag,this);
             }

           /*
             <person></person>
           */
             if ( (tag.XMLData.equals(AIML10Tag.PERSON)) && (tag.XMLType == tag.TAG) ) {
                p = new PersonProcessor();
                return p.processAIML(level++,ip,tag,this);
             }

           /*
             <random></random>
           */
             if ( (tag.XMLData.equals(AIML10Tag.RANDOM)) && (tag.XMLType == tag.TAG) ) {
                p = new RandomProcessor();
                return p.processAIML(level++,ip,tag,this);
             }

           /*
             <bot name="property"/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.BOT)) && (tag.XMLType == tag.EMPTY) ) {
                p = new BotProcessor();
                return p.processAIML(level++,ip,tag,this);
             }

           /*
             <that index="N,M"/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.THAT)) && (tag.XMLType == tag.EMPTY) ) {
                p = new ThatProcessor();
                return p.processAIML(level++,ip,tag,this);
             }

           /*
             <condition></condition>
           */
             if ( (tag.XMLData.equals(AIML10Tag.CONDITION)) && (tag.XMLType == tag.TAG) ) {
                p = new ConditionProcessor();
                return p.processAIML(level++,ip,tag,this);
             }


           /* ---> Removed by AIML Archcomm decission 09-2001
             <if></if>
             if ( (tag.XMLData.equals(AIML10Tag.IF)) && (tag.XMLType == tag.TAG) ) {
                p = new IfProcessor();
                return p.processAIML(level++,ip,tag,this);
             }
           */

           /*
             <formal></formal>
           */
             if ( (tag.XMLData.equals(AIML10Tag.FORMAL)) && (tag.XMLType == tag.TAG) ) {
                p = new FormalProcessor();
                return p.processAIML(level++,ip,tag,this);
             }

           /*
             <input index="N"/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.INPUT)) && (tag.XMLType == tag.EMPTY) ) {
                p = new InputProcessor();
                return p.processAIML(level++,ip,tag,this);
             }

           /*
             <id/> (see comment on processor IdProcessor.java)
           */
             if ( (tag.XMLData.equals(AIML10Tag.ID)) && (tag.XMLType == tag.EMPTY) ) {
                p = new IdProcessor();
                return p.processAIML(level++,ip,tag,this);
             }

           /*
             <sentence></sentence>
           */
             if ( (tag.XMLData.equals(AIML10Tag.SENTENCE)) && (tag.XMLType == tag.TAG) ) {
                p = new SentenceProcessor();
                return p.processAIML(level++,ip,tag,this);
             }

           /*
             <version/>  (no way I'll use a processor to return a constant!)
           */
             if ( (tag.XMLData.equals(AIML10Tag.VERSION)) && (tag.XMLType == tag.EMPTY) ) {
                return Globals.getversion();
             }

           /*
             <gossip src="gossipfile"></gossip>
           */
             if ( (tag.XMLData.equals(AIML10Tag.GOSSIP)) && (tag.XMLType == tag.TAG) ) {
                p = new GossipProcessor();
                return p.processAIML(level++,ip,tag,this);
             }

           /*
             <uppercase></uppercase>
           */
             if ( (tag.XMLData.equals(AIML10Tag.UPPERCASE)) && (tag.XMLType == tag.TAG) ) {
                p = new UpperCaseProcessor();
                return p.processAIML(level++,ip,tag,this);
             }

           /*
             <lowercase></lowercase>
           */
             if ( (tag.XMLData.equals(AIML10Tag.LOWERCASE)) && (tag.XMLType == tag.TAG) ) {
                p = new LowerCaseProcessor();
                return p.processAIML(level++,ip,tag,this);
             }

           /*
             <date/> (see comment on processor DateProcessor.java)
           */
             if ( (tag.XMLData.equals(AIML10Tag.DATE)) && (tag.XMLType == tag.EMPTY) ) {
                p = new DateProcessor();
                return p.processAIML(level++,ip,tag,this);
             }

           /*
             <system></system>
           */
             if ( (tag.XMLData.equals(AIML10Tag.SYSTEM)) && (tag.XMLType == tag.TAG) ) {
                p = new SystemProcessor();
                System.out.println("*** SYSTEM: TAG TO BE EXECUTED ***");
                response = p.processAIML(level++,ip,tag,this);
                System.out.println("*** SYSTEM: TAG EXECUTED RESULT("+response+") ***");
                return response;
             }

           /*
             <javascript></javascript>  (not implemented yet)
           */
             if ( (tag.XMLData.equals(AIML10Tag.JAVASCRIPT)) && (tag.XMLType == tag.TAG) ) {
                p = new JavaScriptProcessor();
                return p.processAIML(level++,ip,tag,this);
             }

           /*
             <size/> 
           */
             if ( (tag.XMLData.equals(AIML10Tag.SIZE)) && (tag.XMLType == tag.EMPTY) ) {
                return Globals.getsize();
             }





           /*
             <person2></person2>
           */
             if ( (tag.XMLData.equals(AIML10Tag.PERSON2)) && (tag.XMLType == tag.TAG) ) {
                p = new Person2Processor();
                return p.processAIML(level++,ip,tag,this);
             }

           /*
             <gender></gender>
           */
             if ( (tag.XMLData.equals(AIML10Tag.GENDER)) && (tag.XMLType == tag.TAG) ) {
                p = new GenderProcessor();
                return p.processAIML(level++,ip,tag,this);
             }

           /*
             <thatstar index="N"/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.THATSTAR)) && (tag.XMLType == tag.EMPTY) ) {
                p = new ThatStarProcessor();
                return p.processAIML(level++,ip,tag,this);
             }

           /*
             <topicstar index="N"/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.TOPICSTAR)) && (tag.XMLType == tag.EMPTY) ) {
                p = new TopicStarProcessor();
                return p.processAIML(level++,ip,tag,this);
             }

           /*********************************************************
            *Virtual Tags                                           *
            *The following tags aren't real but just implemented as *
            *the combination of other tags, so they lack a processor*
            *on their own.                                          *
            *********************************************************/

           /*
             <sr/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.SR)) && (tag.XMLType == tag.EMPTY) ) {
                return virtualTag(level,ip,AIML10Tag.SRAI,tag.TAG,"",AIML10Tag.STAR,tag.EMPTY);
             }

           /*
             <person/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.PERSON)) && (tag.XMLType == tag.EMPTY) ) {
                return virtualTag(level,ip,AIML10Tag.PERSON,tag.TAG,"",AIML10Tag.STAR,tag.EMPTY);
             }

           /*
             <person2/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.PERSON2)) && (tag.XMLType == tag.EMPTY) ) {
                return virtualTag(level,ip,AIML10Tag.PERSON2,tag.TAG,"",AIML10Tag.STAR,tag.EMPTY);
             }

           /*********************************************************
            *AIML 0.9 (deprecated) tags                             *
            *The AIML 0.9 set is implemented as virtual tags mostly *
            *********************************************************/
           /*
             <name/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.NAME)) && (tag.XMLType == tag.EMPTY) ) {
                return virtualTag(level,ip,AIML10Tag.BOT,tag.EMPTY,"name=\"name\"","",tag.EMPTY);
             }

           /*
             <justbeforethat/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.JUSTBEFORETHAT)) && (tag.XMLType == tag.EMPTY) ) {
                return virtualTag(level,ip,AIML10Tag.THAT,tag.EMPTY,"index=\"2,1\"","",tag.EMPTY);
             }

           /*
             <justthat/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.JUSTTHAT)) && (tag.XMLType == tag.EMPTY) ) {
                return virtualTag(level,ip,AIML10Tag.INPUT,tag.EMPTY,"index=\"2\"","",tag.EMPTY);
             }

           /*
             <beforethat/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.BEFORETHAT)) && (tag.XMLType == tag.EMPTY) ) {
                return virtualTag(level,ip,AIML10Tag.INPUT,tag.EMPTY,"index=\"3\"","",tag.EMPTY);
             }

           /*
             <getname/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.GETNAME)) && (tag.XMLType == tag.EMPTY) ) {
                return virtualTag(level,ip,AIML10Tag.GET,tag.EMPTY,"name=\"name\"","",tag.EMPTY);
             }

           /*
             <getsize/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.GETSIZE)) && (tag.XMLType == tag.EMPTY) ) {
                return virtualTag(level,ip,AIML10Tag.SIZE,tag.EMPTY,"","",tag.EMPTY);
             }

           /*
             <gettopic/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.GETTOPIC)) && (tag.XMLType == tag.EMPTY) ) {
                return virtualTag(level,ip,AIML10Tag.GET,tag.EMPTY,"name=\"topic\"","",tag.EMPTY);
             }

           /*
             <getversion/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.GETVERSION)) && (tag.XMLType == tag.EMPTY) ) {
                return virtualTag(level,ip,AIML10Tag.VERSION,tag.EMPTY,"","",tag.EMPTY);
             }

           /*
             <getversion/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.GETVERSION)) && (tag.XMLType == tag.EMPTY) ) {
                return virtualTag(level,ip,AIML10Tag.VERSION,tag.EMPTY,"","",tag.EMPTY);
             }

           /*
             <get_ip/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.GET_IP)) && (tag.XMLType == tag.EMPTY) ) {
                return virtualTag(level,ip,AIML10Tag.ID,tag.EMPTY,"","",tag.EMPTY);
             }

           /*
             <settopic></settopic>
           */
             if ( (tag.XMLData.equals(AIML10Tag.SETTOPIC)) && (tag.XMLType == tag.TAG) ) {
                response = evaluate(level,ip,tag.XMLChild);
                return virtualTag(level,ip,AIML10Tag.SET,tag.TAG,"name=\"topic\"",response,tag.DATA);
             }

           /*
             <setname></setname>
           */
             if ( (tag.XMLData.equals(AIML10Tag.SETNAME)) && (tag.XMLType == tag.TAG) ) {
                response = evaluate(level,ip,tag.XMLChild);
                return virtualTag(level,ip,AIML10Tag.SET,tag.TAG,"name=\"name\"",response,tag.DATA);
             }


           /*
             <set_varname></set_varname>
             This is a special transformation between the old (deprecated)
             way <set_varname> into the newer <set name="varname">
           */
             int dashpos = tag.XMLData.indexOf("_",0);

             if ( (tag.XMLData.indexOf(AIML10Tag.SET_OLD,0) >= 0) &&
                  (tag.XMLType == tag.TAG) ) {

                String start = tag.XMLData.substring(0,dashpos-1);
                String end   = tag.XMLData.substring(dashpos+1,tag.XMLData.length());
                response = evaluate(level,ip,tag.XMLChild);
                // System.out.println("*** <set_xxx>: var("+end+") content("+response+") ***");

                response = virtualTag(level,ip,AIML10Tag.SET,tag.TAG,"name=\""+end+"\"",response,tag.DATA);

                //System.out.println("*** <set_xxx>: Result("+response+") ***");
                return response;
             }

           /*
             <get_varname/>
             This is a special transformation between the old (deprecated)
             way <get_varname/> into the newer <get name="varname">
           */
             if ( (tag.XMLData.indexOf(AIML10Tag.GET_OLD,0) >= 0) &&
                  (tag.XMLType == tag.EMPTY) ) {

                String start = tag.XMLData.substring(0,dashpos-1);
                String end   = tag.XMLData.substring(dashpos+1,tag.XMLData.length());
                // System.out.println("*** <get_xxx>: var("+end+") ***");
                response = virtualTag(level,ip,AIML10Tag.GET,tag.EMPTY,"name=\""+end+"\"","",tag.DATA);
                // System.out.println("*** <set_xxx>: Result("+response+") ***");
                return response;
             }



           /*********************************************************
            *AIML 0.9 (deprecated) custom bot properties            *
            *Implemented as virtual tags using <bot/>               *
            *********************************************************/

           /*
             <birthday/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.BIRTHDAY)) && (tag.XMLType == tag.EMPTY) ) {
                return virtualTag(level,ip,AIML10Tag.BOT,tag.EMPTY,"name=\"birthday\"","",tag.EMPTY);
             }

           /*
             <birthplace/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.BIRTHPLACE)) && (tag.XMLType == tag.EMPTY) ) {
                return virtualTag(level,ip,AIML10Tag.BOT,tag.EMPTY,"name=\"birthplace\"","",tag.EMPTY);
             }

           /*
             <boyfriend/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.BOYFRIEND)) && (tag.XMLType == tag.EMPTY) ) {
                return virtualTag(level,ip,AIML10Tag.BOT,tag.EMPTY,"name=\"boyfriend\"","",tag.EMPTY);
             }

           /*
             <favoriteband/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.FAVORITEBAND)) && (tag.XMLType == tag.EMPTY) ) {
                return virtualTag(level,ip,AIML10Tag.BOT,tag.EMPTY,"name=\"favoriteband\"","",tag.EMPTY);
             }

           /*
             <favoritebook/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.FAVORITEBOOK)) && (tag.XMLType == tag.EMPTY) ) {
                return virtualTag(level,ip,AIML10Tag.BOT,tag.EMPTY,"name=\"favoritebook\"","",tag.EMPTY);
             }

           /*
             <favoritecolor/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.FAVORITECOLOR)) && (tag.XMLType == tag.EMPTY) ) {
                return virtualTag(level,ip,AIML10Tag.BOT,tag.EMPTY,"name=\"favoritecolor\"","",tag.EMPTY);
             }

           /*
             <favoritefood/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.FAVORITEFOOD)) && (tag.XMLType == tag.EMPTY) ) {
                return virtualTag(level,ip,AIML10Tag.BOT,tag.EMPTY,"name=\"favoritefood\"","",tag.EMPTY);
             }

           /*
             <favoritemovie/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.FAVORITEMOVIE)) && (tag.XMLType == tag.EMPTY) ) {
                return virtualTag(level,ip,AIML10Tag.BOT,tag.EMPTY,"name=\"favoritemovie\"","",tag.EMPTY);
             }

           /*
             <favoritesong/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.FAVORITESONG)) && (tag.XMLType == tag.EMPTY) ) {
                return virtualTag(level,ip,AIML10Tag.BOT,tag.EMPTY,"name=\"favoritesong\"","",tag.EMPTY);
             }

           /*
             <for_fun/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.FOR_FUN)) && (tag.XMLType == tag.EMPTY) ) {
                return virtualTag(level,ip,AIML10Tag.BOT,tag.EMPTY,"name=\"forfun\"","",tag.EMPTY);
             }

           /*
             <friends/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.FRIENDS)) && (tag.XMLType == tag.EMPTY) ) {
                return virtualTag(level,ip,AIML10Tag.BOT,tag.EMPTY,"name=\"friends\"","",tag.EMPTY);
             }

           /*
             <gender/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.GENDER)) && (tag.XMLType == tag.EMPTY) ) {
                return virtualTag(level,ip,AIML10Tag.BOT,tag.EMPTY,"name=\"gender\"","",tag.EMPTY);
             }

           /*
             <girlfriend/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.GIRLFRIEND)) && (tag.XMLType == tag.EMPTY) ) {
                return virtualTag(level,ip,AIML10Tag.BOT,tag.EMPTY,"name=\"girlfriend\"","",tag.EMPTY);
             }

           /*
             <kind_music/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.KIND_MUSIC)) && (tag.XMLType == tag.EMPTY) ) {
                return virtualTag(level,ip,AIML10Tag.BOT,tag.EMPTY,"name=\"kindmusic\"","",tag.EMPTY);
             }

           /*
             <location/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.LOCATION)) && (tag.XMLType == tag.EMPTY) ) {
                return virtualTag(level,ip,AIML10Tag.BOT,tag.EMPTY,"name=\"location\"","",tag.EMPTY);
             }

           /*
             <look_like/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.LOOK_LIKE)) && (tag.XMLType == tag.EMPTY) ) {
                return virtualTag(level,ip,AIML10Tag.BOT,tag.EMPTY,"name=\"looklike\"","",tag.EMPTY);
             }

           /*
             <botmaster/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.BOTMASTER)) && (tag.XMLType == tag.EMPTY) ) {
                return virtualTag(level,ip,AIML10Tag.BOT,tag.EMPTY,"name=\"master\"","",tag.EMPTY);
             }

           /*
             <question/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.QUESTION)) && (tag.XMLType == tag.EMPTY) ) {
                return virtualTag(level,ip,AIML10Tag.BOT,tag.EMPTY,"name=\"question\"","",tag.EMPTY);
             }

           /*
             <sign/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.SIGN)) && (tag.XMLType == tag.EMPTY) ) {
                return virtualTag(level,ip,AIML10Tag.BOT,tag.EMPTY,"name=\"sign\"","",tag.EMPTY);
             }

           /*
             <talk_about/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.TALK_ABOUT)) && (tag.XMLType == tag.EMPTY) ) {
                return virtualTag(level,ip,AIML10Tag.BOT,tag.EMPTY,"name=\"talkabout\"","",tag.EMPTY);
             }

           /*
             <wear/>
           */
             if ( (tag.XMLData.equals(AIML10Tag.WEAR)) && (tag.XMLType == tag.EMPTY) ) {
                return virtualTag(level,ip,AIML10Tag.BOT,tag.EMPTY,"name=\"wear\"","",tag.EMPTY);
             }

          /*
           If the tag wasn't catched up to this point it's because is
           an unknown tag. Then just expand it into text.
          */
          response = formattag(level,ip,tag);
          return response;
        }
        /**
         processResponse
         This method receives a string with the Template expressed
         as a XML coded string and returns the full evaluation of
         it. This method is the one invoked from external classes
         when a given template is required to be evaluated.
        */
        public String processResponse(String ip, String template) {

             LinkedList     lt;
             XMLParser      xml;
             int            level    = 0;
             String         response = "";

             /*
              A XML parser object instance is created and the XML string
              of the template is loaded into it.
             */

             xml= new XMLParser();
             lt = xml.XMLLoad(template);

             /*
              If something went really wrong with the XML processing
              just return error and produce a console message
             */
             if (lt == null) {
                System.out.println("*** ERROR: INVALID TEMPLATE ("+template+") ***");
                return "{aiml error}";
             }

             /*
              Now evaluate the template starting from the first token
              of it. The method will recurse itself to explore the full trie
             */

             response = evaluate(level,ip,lt);

             /*
              Might be unnecessary but a controlled clean-up is performed
             */
             lt = xml.XMLFree(lt);

             return response;
        }


        /**
          Main method as a placeholder for debug
        */
	public static void main(String[] args)
	{
                System.out.println("*** AIMLParser main() ***");
	}
	
}
