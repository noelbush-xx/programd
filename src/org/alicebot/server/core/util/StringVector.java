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

// the base class of concern here is a StringVector
// Basically this is just an array or Vector of Strings
// 

class StringVector implements Serializable {
	
	private String[] Elements = null;
	private int asize = 0; // the physical size of the list in memory
	private int size = 0; // the logical size of the array 
	public int size() { return size; }
	
	// because it is a Set, the StringSet has
	// a unique inverse map to indeces,
	// but for a Vector we just find the first match:
	
	public int indexOf(String s) {
		for (int i = 0; i < size(); i++) {
			if (s.compareTo((String)elementAt(i))==0) return i;
		}
		return -1;  }
	//
	// the boolean member contains(s)
	public final boolean contains (String s) {
		if (indexOf(s) < 0) return false;
		else return true;
	}
	public final void setElementAt(String s, int index) {
		Elements[index] = s;
	}
	
	public final String elementAt(int index) {
		if (index < size) return(Elements[index]);
		else return null;
	}
	
	public final void addElement(String s) {
		// 1. first, take care of array memory management:
		if (size >= asize) { // 0 >= 0 first time
			// 1 >= 1
			// 2 >= 2
			// 4 >= 4
			asize = (asize == 0) ? 1 : (2*asize);
			//    System.out.println("Array size "+asize);
			String[] nElements = new String[asize];
			if (size > 0) {
				System.arraycopy(Elements, 0, nElements, 0, size);
			}
			Elements = nElements;
		}
		// if (size >= asize)
		// now size < asize
		// 2. Add the new string s to the end of the array:
		Elements[size] = s;
		size++;
	} // method add
} // class StringVector
