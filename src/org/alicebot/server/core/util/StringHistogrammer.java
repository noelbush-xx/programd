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

// A String Histogrammer is a String set with
// a map from strings to integers

public class StringHistogrammer extends StringSet implements Serializable {
	// "total" refers to the total
	// number of items counted by the histogrammer.
	// total should equal the sum of Countmap
	// "size" refers to the number of elements in the set
	protected int total=0;
	protected Hashtable Countmap=new Hashtable();
	
	public int getTotal() {
		return total;
	}
	public int Count(int i) {
		if (i < size()) 
			return(((Integer)Countmap.get((String)elementAt(i))).intValue());
		else return 0;
	}
	
	public void setCount(String s, int c) {
		Countmap.put(s, new Integer(c));
	}
	
	// override the parent class add
	// to increment Countmap:
	
	public void add(String s) {  
		Integer D; int d;
		if (!contains(s)) {
			super.add(s);
			d = 1;
		}
		else {
			D = (Integer)Countmap.get(s);
			d = D.intValue() + 1;
		}
		D = new Integer(d);
		Countmap.put(s, D);
		total++;
		// assert total = sum Countmap()
	} // method add
}

