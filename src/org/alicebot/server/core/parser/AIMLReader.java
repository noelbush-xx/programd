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

	
import java.io.*;
import java.util.*;

/* ----------------------------------------------------------------------/
  AIMLReader.java       - v1.1    11.16.1999
  (Requires AIMLReaderListenter.java, an interface.)
  Implements a simple AIML parser.  It was written with readability in
  mind, so the code is rather straight-forward.... But it sped it up a
  tad to get rid of the multiple procedure blocks being called, so now
  read() is one chunk of code. ...Not as pretty, but slightly quicker.
  Yes I know... It isn't very elegant, it isn't an extensible XML parser.
  It's a brute force AIML parser.  That's what it was designed to be.
  You can e-mail me if you have any problems with it, bug fixes are
  welcome too.
          Kris Drent, drent@facm-kdrent.unl.edu
/-----------------------------------------------------------------------*/

public class  AIMLReader implements Serializable {
  // Parser "States", think of them in layers...
  final int       S_NONE      = 0; // Not inside any tag set
  final int       S_AIML      = 1; // Inside an <aiml> set
  final int       S_CATEGORY  = 2; // Inside a  <category> </category> set
  final int       S_PATTERN   = 3; // Inside a  <pattern> </pattern> set
  final int       S_THAT      = 4; // Inside a  <that> </that> set
  final int       S_TEMPLATE  = 5; // Inside a  <template> </template> set
  final int 	  S_DESCRIPTION = 6; // Inside a <description> </description> set

  AIMLReaderListener  aListener   = null;
  BufferedReader      bReader     = null;
  String              strBuff     = "";
  String              fileName    = "";
  int                 intLineNum  = 0;
  final int           maxtagsize  = 11;

  // Constructor
 public AIMLReader(String fn, BufferedReader br, AIMLReaderListener al){
    this.fileName  = fn;
    this.bReader   = br;
    this.aListener = al;
  }

  public void read(){
    boolean   done           = false;
    boolean   bSearching     = false;
    boolean   inXML          = false;

    int       state          = 0;
    int       intTagStart    = 0;
    int       intStartSearch = 0;

    String    strIn          = null;
    String    strPattern     = null;
    String    strThat        = null;
    String    strTopic       = null;
    String    strTemplate    = null;
    String    localhost      = "localhost"; //4.1.2 b3

    /* ------ AIML tag format ---------------------------------/
      <aiml>
       <topic name="Topic Name"> //optional
        <category>
          <pattern>  pattern phrase         </pattern>
          <that>     that phrase (optional) </that>
          <template> template phrase        </template>
        </category>
       </topic> //optional
      </aiml>
    / --------------------------------------------------------*/

    //- Parse Loop -
    while(!done){

      //- Find tag candidate -
      bSearching = true;
      while(bSearching){
        intTagStart = strBuff.indexOf("<", intStartSearch);
        if(intTagStart<0 || (strBuff.length()-intTagStart)< maxtagsize){
          // didn't find '<' read in another line
          try{
            strIn = bReader.readLine();
            strIn += "\n";
            intLineNum++;
          }
          catch(IOException ex){
            System.err.println("AIMLReader.buffIndexOf(): " + ex);
          }
          // Check for end of file.
          if(strIn==null && intTagStart>=0)
            bSearching = false; // at end of file, with no match
          else{
            strBuff += strIn;   // add to string, back to top to search
          }
        }
        else{  // found '<', leave
          bSearching = false;
        }
      }// end while

//Fix 4.0.3 b8 PEC 09-2001 reject files with just <aiml/>

    int x = strBuff.indexOf("<aiml",0);
    if (x >= 0) {
       int     startfix = x;
       int     endfix   = strBuff.indexOf(">",startfix);
       String  tagfix   = strBuff.substring(startfix,endfix);
       if (String.valueOf(tagfix.charAt(tagfix.length()-1)).equals("/")) {
          //ignore the rest of the file the <aiml> tag is malformed
          System.out.println("*** FILE("+this.fileName+") Malformed <aiml> tag, file ignored");
          done = true;
          bSearching = false;
       }
    }
//End of Fix

      if(intTagStart<0){
        done = true;
        continue;
      }
      else
        intStartSearch = intTagStart;

      /*
       Ability to parse <learn></learn> and <property/> outside the
       context of a category as long as they are on a XML file and not
       in a AIML file and not within any category structure
       4.1.2 b3 PEC 09-2001
      */
      if( (inXML == true) && (state == S_NONE) ) {

        if(strBuff.regionMatches(intTagStart,"<learn>",0,7)) {

          int intLearnEnd = strBuff.indexOf("</learn>",intTagStart);
          if ((intLearnEnd >= 0) && (intLearnEnd > intTagStart)) {
             intLearnEnd     = intLearnEnd + 8;
             String strLearn = strBuff.substring(intTagStart,intLearnEnd);
             strBuff         = strBuff.substring(intLearnEnd++);
             AIMLParser p    = new AIMLParser();
             String dummy    = p.processResponse(localhost,strLearn);
             intStartSearch = 0;
             intTagStart    = -1;
          }

        }
        if(strBuff.regionMatches(intTagStart,"<property",0,9)) {

          int intPropEnd = strBuff.indexOf("/>",intTagStart);
          if ((intPropEnd >= 0) && (intPropEnd > intTagStart)) {
             intPropEnd         = intPropEnd + 2;
             String strProperty = strBuff.substring(intTagStart,intPropEnd);
             strBuff            = strBuff.substring(intPropEnd++);
             AIMLParser p    = new AIMLParser();
             String dummy    = p.processResponse(localhost,strProperty);
             intStartSearch = 0;
             intTagStart    = -1;

          }

        }

      }
      /* ======================{End of Patch}=============================== */

      //- Differentiate tags, check for validity in syntax, get phrase if applicable
      //- Note: the order here just puts the more frequent tags at the front
      if(strBuff.regionMatches(intTagStart,"<template>",0,10)){
          if(state != S_CATEGORY){
          System.err.println("AIMLReader.read(): [" + fileName + " : line " + intLineNum
                    + "] unexpected <template> tag, aborting this category.");
          state = S_AIML;
          strPattern = strThat = strTemplate = null;
          intStartSearch+=10;
        }
        else{
          state = S_TEMPLATE;
          //remove text already parsed from beginning of string (buffer)
          strBuff = strBuff.substring(intTagStart+10);
          intStartSearch = 0;
        }
      }
      else if(strBuff.regionMatches(intTagStart,"</template>",0,11)){
        if(state != S_TEMPLATE){
          System.err.println("AIMLReader.read(): [" + fileName + " : line " + intLineNum
                     + "] unexpected </template> tag, aborting this category.");
          state = S_AIML; // reset to <alice> state to look for next category
          strPattern = strThat = strTemplate = null;
          intStartSearch+=11;
        }
        else{
          state = S_CATEGORY;
          strTemplate = strBuff.substring(0, intTagStart);
          //remove text already parsed from beginning of string (buffer)
          strBuff = strBuff.substring(intTagStart+11);
          intStartSearch = 0;
        }
      }
      else if(strBuff.regionMatches(intTagStart,"<pattern>",0,9)){
        if(state != S_CATEGORY){
          System.err.println("AIMLReader.read(): [" + fileName + " : line " + intLineNum
                     + "] unexpected <pattern> tag, aborting this category.");
          state = S_AIML; // reset to <alice> state to look for next category
          strPattern = strThat = strTemplate = null;
          intStartSearch+=9;
        }
        else{
          state = S_PATTERN;
          //remove text already parsed from beginning of string (buffer)
          strBuff = strBuff.substring(intTagStart+9);
          intStartSearch = 0;
        }
      }
      else if(strBuff.regionMatches(intTagStart,"</pattern>",0,10)){
        if(state != S_PATTERN){
          System.err.println("AIMLReader.read(): [" + fileName + " : line " + intLineNum
                     + "] unexpected </pattern> tag, aborting this category.");
          state = S_AIML; // reset to <alice> state to look for next category
          strPattern = strThat = strTemplate = null;
          intStartSearch+=10;
        }
        else{
          // leaving a pattern tag sequence, get enclosed pattern phrase
          state = S_CATEGORY;
          strPattern = strBuff.substring(0, intTagStart);
          //remove text already parsed from beginning of string (buffer)
          strBuff = strBuff.substring(intTagStart+10);
          intStartSearch = 0;
        }
      }

      else if(strBuff.regionMatches(intTagStart,"<category>",0,10)){
        if(state != S_AIML){
          System.err.println("AIMLReader.read(): [" + fileName + " : line " + intLineNum
                     + "] unexpected <category> tag, aborting this category.");
          state = S_AIML; // reset to <alice> state to look for next category
          strPattern = strThat = strTemplate = null;
          intStartSearch+=10;
        }
        else{
          state = S_CATEGORY;
          //remove text already parsed from beginning of string (buffer)
          strBuff = strBuff.substring(intTagStart+10);
          intStartSearch = 0;
        }
      }
      else if(strBuff.regionMatches(intTagStart,"</category>",0,11)){
        if(state != S_CATEGORY){
          System.err.println("AIMLReader.read(): [" + fileName + " : line " + intLineNum
                     + "] unexpected </category> tag, aborting this category.");
          state = S_AIML; // reset to <alice> state to look for next category
          strPattern = strThat = strTemplate = null;
          intStartSearch+=11;
        }
        else{
          // finished category, check for required parts
          if(strPattern == null || strTemplate == null){
            System.out.println("AIMLReader.read(): [" + fileName + " : line " + intLineNum
                   + "] AIML category did not contain a pattern or did not "
                   + "contain a template. Both are required.");
          }
          else{
            //send off pattern, that, and template to be added
            // [modifeid: added strTopic argument, <topic> support (Drent 10-13-1999)]
            aListener.newCategory(strPattern, strThat, strTopic, strTemplate);
            strPattern = strThat = strTemplate = null; // reset
            state = S_AIML;
            //remove text already parsed from beginning of string (buffer)
            strBuff = strBuff.substring(intTagStart+11);
            intStartSearch = 0;
          }
        }
      }
      else if(strBuff.regionMatches(intTagStart,"<that>",0,6)){
        if(state != S_CATEGORY){
          System.err.println("AIMLReader.read(): [" + fileName + " : line " + intLineNum
                     + "] unexpected <that> tag, aborting this category.");
          state = S_AIML;
          strPattern = strThat = strTemplate = null;
          intStartSearch+=6;
        }
        else{
          state = S_THAT;
          //remove text already parsed from beginning of string (buffer)
          strBuff = strBuff.substring(intTagStart+6);
          intStartSearch = 0;
        }
      }
      else if(strBuff.regionMatches(intTagStart,"</that>",0,7)){
        if(state != S_THAT){
          System.err.println("AIMLReader.read(): [" + fileName + " : line " + intLineNum
                     + "] unexpected </that> tag, aborting this category.");
          state = S_AIML;
          strPattern = strThat = strTemplate = null;
          intStartSearch+=7;
        }
        else{
          state = S_CATEGORY;
          strThat = strBuff.substring(0, intTagStart);
          //remove text already parsed from beginning of string (buffer)
          strBuff = strBuff.substring(intTagStart+7);
          intStartSearch = 0;
        }
      }

      else if(strBuff.regionMatches(intTagStart,"<topic name=\"",0,13)){
        if(state != S_AIML){
          System.err.println("AIMLReader.read(): [" + fileName + " : line " + intLineNum
                     + "] unexpected <topic> tag, looking for clean category.");
          state = S_AIML;
          strPattern = strThat = strTemplate = null;
          intStartSearch+=7;
        }
        else{
          state = S_AIML;
          // Get the name of the topic
          intTagStart += 13;
          int intNameEnd = strBuff.indexOf("\"", intTagStart);
          if(intNameEnd<0 || (intNameEnd-intTagStart) > 100){ // uh-oh
            System.err.println("AIMLReader.read(): [" + fileName + " : line " + intLineNum
                   + "] in <topic> tag, could not find end of name quote. (>100 chars?) Ignoring.");
            //remove text already parsed from beginning of string (buffer)
            strBuff = strBuff.substring(intTagStart);
            intStartSearch = 0;
          }
          else{
            strTopic = strBuff.substring(intTagStart, intNameEnd);
            strTopic = strTopic.toUpperCase();
            intTagStart = strBuff.indexOf(">",intNameEnd);
            if(intTagStart<0){
              System.err.println("AIMLReader.read(): [" + fileName + " : line " + intLineNum
                   + "] <topic> tag missing \">\".");
              intTagStart = intNameEnd;
            }
            //remove text already parsed from beginning of string (buffer)
            strBuff = strBuff.substring(intTagStart);
            intStartSearch = 0;

          }
        }
      }
      else if(strBuff.regionMatches(intTagStart,"</topic>",0,8)){
        if(state != S_AIML){
          System.err.println("AIMLReader.read(): [" + fileName + " : unexpected </topic> tag, ignoring.");
          intStartSearch+=8;
        }
        else{
          strTopic = null;
          //remove text already parsed from beginning of string (buffer)
          strBuff = strBuff.substring(intTagStart+8);
          intStartSearch = 0;
        }
      }

      // We'll put <alice> last, since it will be most infrequent
      else
       if(strBuff.regionMatches(intTagStart,"<aiml>",0,6)){
        if(state != S_NONE){
          System.err.println("AIMLReader.read(): [" + fileName + " : line " + intLineNum
                     + "] unexpected <aiml> tag, aborting this category.");
          state = S_AIML;
          strPattern = strThat = strTemplate = null;
          intStartSearch+=7;
        }
        else{
          state = S_AIML;
          //remove text already parsed from beginning of string (buffer)
          strBuff = strBuff.substring(intTagStart+7);
          intStartSearch = 0;
        }
      }
               else if(strBuff.regionMatches(intTagStart,"<aiml version=\"1.0\">",0,20)){
        if(state != S_NONE){
          System.err.println("AIMLReader.read(): [" + fileName + " : line " + intLineNum
                     + "] unexpected <aiml> tag, aborting this category.");
          state = S_AIML;
          strPattern = strThat = strTemplate = null;
          intStartSearch+=7;
        }
        else{
          state = S_AIML;
          //remove text already parsed from beginning of string (buffer)
          strBuff = strBuff.substring(intTagStart+7);
          intStartSearch = 0;
        }
      }
      else if(strBuff.regionMatches(intTagStart,"</aiml>",0,7)){
        if(state != S_AIML){
          System.err.println("AIMLReader.read(): [" + fileName + " : unexpected </aiml> tag, ending parsing.");
          done = true;
        }
        else{
          state = S_NONE;
          //We're done.
          done = true;
        }
      }
      else {

        /*
         Patch to allow a very primitive handling of generic XML
         files in order to allow the bot to be bootstrapped
         from a regular XML file. Only two tags will be recognized
         in such scenario <learn></learn> and <property/>
         This is a kludge, the most reasonable action should be
         to replace this entire method by a standard XML parser.
         4.1.2 b3 PEC 09-2001
        */

        if(strBuff.regionMatches(intTagStart,"<xml",0,4)) {

          System.out.println("*** Processing XML("+fileName+") ***");
          int intTagEnd = strBuff.indexOf(">",intTagStart);
          intStartSearch= intTagEnd;
          intStartSearch++;
          inXML         = true;

        } else {
          if(strBuff.regionMatches(intTagStart,"</xml>",0,6)) {
            intStartSearch=intTagStart+7;
            inXML        =false;
            done         =true;
          } else {

        //End of Add

          // matched no tags, not an AIML tag.
             intStartSearch= intTagStart + 1;
          }
        }
      }

   }// end while

 } // end read();

} // end class AIMLReader


