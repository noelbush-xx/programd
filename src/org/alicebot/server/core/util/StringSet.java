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

// a String Set is a Vector containing only one instance
// of each String:
//
// extending Vector results in a slightly faster version
//
public class StringSet extends Vector implements Serializable { // or extends StringVector
	// override the root class add
	// to add each string only once:
	public void add(String s) {
		if (!contains(s)) {
			super.addElement(s);
		}
		else {
			//      System.out.println("Duplicate: "+s+" discareded.");
		}
	} // add
} // class StringSet
