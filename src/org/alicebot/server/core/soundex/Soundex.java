package org.alicebot.server.core.soundex;

/**

ALICEBOT.NET Artificial Intelligence Project
This version is Copyright (C) 2000 Jon Baer.
jonbaer@digitalanywhere.com
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
notice, this list of conditions, and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright
notice, this list of conditions, and the disclaimer that follows 
these conditions in the documentation and/or other materials 
provided with the distribution.

3. The name "ALICEBOT.NET" must not be used to endorse or promote products
derived from this software without prior written permission.  For
written permission, please contact license@alicebot.org.

4. Products derived from this software may not be called "ALICEBOT.NET",
nor may "ALICEBOT.NET" appear in their name, without prior written permission
from the ALICEBOT.NET Project Management (jonbaer@alicebot.net).

In addition, we request (but do not require) that you include in the 
end-user documentation provided with the redistribution and/or in the 
software itself an acknowledgement equivalent to the following:
"This product includes software developed by the
ALICEBOT.NET Project (http://www.alicebot.net)."
Alternatively, the acknowledgment may be graphical using the logos 
available at http://www.alicebot.org/images/logos.

THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED.  IN NO EVENT SHALL THE ALICE SOFTWARE FOUNDATION OR
ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

This software consists of voluntary contributions made by many 
individuals on behalf of the A.L.I.C.E. Nexus and ALICEBOT.NET Project
and was originally created by Dr. Richard Wallace <drwallace@alicebot.net>.

This version was created by Jon Baer <jonbaer@alicebot.net>.

http://www.alicebot.org
http://www.alicebot.net

This version contains open-source technologies from:
Netscape, Apache, HypersonicSQL, JDOM, Jetty, Chris Carlin, IBM

*/

/**
  * Encodes words using the soundex phonetic algorithm.
  * The primary method to call is Soundex.encode(String).<p>
  * The main method encodes arguments to System.out.
  * @author Aaron Hansen
  * @author Jon Baer
  */

import java.util.*;

public class Soundex {

  // Public Fields
  // -------------

    /**
    * Possible code length.
    * @see #setLength(int)
    */
  public static final transient int NO_MAX = -1;

  
  // Protected Fields
  // ----------------
  
    /**
    * If true, the final 's' of the word being encoded is dropped.
    */
  protected static boolean DropLastSBoolean = false;

    /**
    * Length of code to build.
    */
  protected static int LengthInt = 4;

    /**
    * If true, codes are padded to the LengthInt with zeros.
    */
  protected static boolean PadBoolean = true;

    /**
    * Soundex code table.
    */
  protected static int[] SoundexInts = createArray();


  // Private Fields
  // --------------

  private static final String LowerS = "s";

  private static final String UpperS = "S";


      //Fix (4.0.3 b1) PEC 09-2001 sentance--VVVVVVVV
        public static String getValue(String sentence) {
		StringBuffer buffer = new StringBuffer();
               //Fix (4.0.3 b1) PEC 09-2001 sentance-----VVVVVVVV 
                StringTokenizer st = new StringTokenizer(sentence);
		int wordcount = 0;
		while (st.hasMoreTokens()) {
			wordcount++;
			if (wordcount > 1) buffer.append(" ");
			buffer.append(encode(st.nextToken()));
		}
		return buffer.toString();
	}
	
  // Public Methods
  // --------------

    /**
    * Returns the soundex code for the specified word.
    * @param string The word to encode.
    */
  public static String encode(String word) {
    word = word.trim();
    if (DropLastSBoolean) {
      if ( (word.length() > 1) 
        && (word.endsWith(UpperS) || word.endsWith(LowerS)))
        word = word.substring(0, (word.length() - 1));
      }
    word = reduce(word);
    int wordLength = word.length(); //original word size
    int sofar = 0; //how many codes have been created
    int max = LengthInt - 1; //max codes to create (less the first char)
    if (LengthInt < 0) //if NO_MAX
      max = wordLength; //wordLength was the max possible size.
    int code = 0; 
    StringBuffer buf = new StringBuffer(max);
    buf.append(Character.toLowerCase(word.charAt(0)));
    for (int i = 1;(i < wordLength) && (sofar < max); i++) {
      code = getCode(word.charAt(i));
      if (code > 0) {
        buf.append(code);
        sofar++;
        }
      }
    if (PadBoolean && (LengthInt > 0)) {
      for (;sofar < max; sofar++)
        buf.append('0');
      }
    return buf.toString();
    }

    /**
    * Returns the Soundex code for the specified character.
    * @param ch Should be between A-Z or a-z
    * @return -1 if the character has no phonetic code.
    */
  public static int getCode(char ch) {
    int arrayidx = -1;
    if (('a' <= ch) || (ch <= 'z'))
      arrayidx = (int)ch - (int)'a';
    else if (('A' <= ch) || (ch <= 'Z'))
      arrayidx = (int)ch - (int)'A';
    if ((arrayidx >= 0) && (arrayidx < SoundexInts.length))
      return SoundexInts[arrayidx];
    else
      return -1;
    }

    /**
    * If true, a final char of 's' or 'S' of the word being encoded will be 
    * dropped. By dropping the last s, lady and ladies for example,
    * will encode the same. False by default.
    */
  public static boolean getDropLastS() {
    return DropLastSBoolean;
    }

    /**
    * The length of code strings to build, 4 by default.
    * If negative, length is unlimited.
    * @see #NO_MAX
    */
  public static int getLength() {
    return LengthInt;
    }

    /**
    * If true, appends zeros to a soundex code if the code is less than
    * Soundex.getLength().  True by default.
    */
  public static boolean getPad() {
    return PadBoolean;
    }

    /**
    * Encodes the args to stdout.
    */
  public static void main(String[] strings) {
    if ((strings == null) || (strings.length == 0)) {
      System.out.println(
        "Specify some words and this will display a soundex code for each.");
      System.exit(0);
      }
    for (int i = 0; i < strings.length; i++)
      System.out.println(Soundex.encode(strings[i]));
    }

    /**
    * Allows you to modify the default code table
    * @param ch The character to specify the code for.
    * @param code The code to represent ch with, must be -1, or 1 thru 9
    */
  public static void setCode(char ch, int code) {
    int arrayidx = -1;
    if (('a' <= ch) || (ch <= 'z'))
      arrayidx = (int)ch - (int)'a';
    else if (('A' <= ch) || (ch <= 'Z'))
      arrayidx = (int)ch - (int)'A';
    if ((0 <= arrayidx) && (arrayidx < SoundexInts.length))
      SoundexInts[arrayidx] = code;
    }

    /**
    * If true, a final char of 's' or 'S' of the word being encoded will be 
    * dropped.
    */
  public static void setDropLastS(boolean bool) {
    DropLastSBoolean = bool;
    }

    /**
    * Sets the length of code strings to build. 4 by default.
    * @param Length of code to produce, must be &gt;= 1
    */
  public static void setLength(int length) {
    LengthInt = length;
    }


    /**
    * If true, appends zeros to a soundex code if the code is less than
    * Soundex.getLength().  True by default.
    */
  public static void setPad(boolean bool) {
    PadBoolean = bool;
    }


  // Protected Methods
  // -----------------

    /**
    * Creates the Soundex code table.
    */
  protected static int[] createArray() {
    return new int[] {
      -1, //a 
       1, //b
       2, //c 
       3, //d
      -1, //e 
       1, //f
       2, //g 
      -1, //h
      -1, //i 
       2, //j
       2, //k
       4, //l
       5, //m
       5, //n
      -1, //o
       1, //p
       2, //q
       6, //r
       2, //s
       3, //t
      -1, //u
       1, //v
      -1, //w
       2, //x
      -1, //y
       2  //z
      };
    }

    /**
    * Removes adjacent sounds.
    */
  protected static String reduce(String word) {
    int len = word.length();
    StringBuffer buf = new StringBuffer(len);
    char ch = word.charAt(0);
    int currentCode = getCode(ch);
    buf.append(ch);
    int lastCode = currentCode;
    for (int i = 1; i < len; i++) {
      ch = word.charAt(i);
      currentCode = getCode(ch);
      if ((currentCode != lastCode) && (currentCode >= 0)) {
        buf.append(ch);
        lastCode = currentCode;
        }
      }
    if (buf.length() == len)
      return word;
    else
      return buf.toString();
    }


  }//Soundex
