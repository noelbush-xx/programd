package org.alicebot.server.core.util;

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

import java.util.*;
import java.lang.*;
import java.net.*;
import java.io.*;   

// SentenceTokenzier generalized StringTokenizer by
// remembering the remaining tokens in the string
// after each call to nextToken();
// The call remainder() retrievs the remaining tokens
// as a string.

public class SentenceTokenizer extends StringTokenizer implements Serializable {
    protected String r; // remainder
    protected String w; // current word
    protected String delims;
    public String remainder() {return r;}
    public String currToken() {return w;}
    public SentenceTokenizer(String s) {
      super(s);
      delims = " ";
      r = s;
    }
    public SentenceTokenizer(String s, String delim) {
      super(s, delim);
      delims = delim;
      r = s;
    }
    public synchronized String nextToken() {
      if (hasMoreTokens()) {
        w = super.nextToken();
        r = (w.length() < r.length()) ? r.substring(w.length()+1, r.length()) : "";
        for (int i = 0; i < delims.length(); i++)         
          while (r.length() > 0 && r.charAt(0) == delims.charAt(i))
            r = r.substring(1, r.length());
        return w;
      }
      else return null;
    }
    public static void main (String arg[]) {
       SentenceTokenizer st = new SentenceTokenizer("THIS IS A TEST");
       while (st.hasMoreTokens()) {
        String x = st.nextToken();
        String m = st.remainder();
        System.out.println(x+"/"+m+"("+st.currToken()+")");
       }
    }
} // class



