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

// A StringVoter is like a Histogrammer 
// except that it allows each "voter" to cast
// only one vote per input string.

public final class StringVoter extends StringSet implements Serializable {

  public Hashtable Countmap=new Hashtable();
  public int votescast = 0;

  public int Count(int i) {
  if (i < size()) 
    return(((StringSorter)Countmap.get((String)elementAt(i))).size());
  else return 0;
  }

  public void setCount(String s, int c) {
     Countmap.put(s, new Integer(c));
  }

  // override the parent class add
  // to increment Countmap:

  public void add(String s, String voter) {  
    votescast++;
    StringSorter Voters;
    if (!contains(s)) {
      super.add(s);
      Voters = new StringSorter();
    }
    else {
      Voters = (StringSorter)Countmap.get(s);
    }
    Voters.add(voter);
    Countmap.put(s, Voters);
    if (size() > 1) { // skip size=1 for efficiency
      int index = super.indexOf(s);
      for (int i = index; i > 0; i--) {
        if (Count(i) >= Count(i-1)) {
           String temp = (String)elementAt(i);
           setElementAt((String)elementAt(i-1), i);
           setElementAt(temp, i-1);
         }  // if
      } // for
    } // size > 1
  } // method add
} // class StringVoter


