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
@version 4.1.1
*/


import java.util.*;
import java.lang.*;
import java.net.*;
import java.io.*;

// A String Ranker is just like a Histogrammer 
// except that it orders the string by rank 
public class StringRanker extends StringHistogrammer implements Serializable {
	// override the parent class add
	// to insertion sort
	// the histogram buckets by rank:
	public void add(String s) {
		super.add(s);
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
} // class StringRanker

