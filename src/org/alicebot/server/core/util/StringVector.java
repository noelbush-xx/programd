package org.alicebot.server.core.util;

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
