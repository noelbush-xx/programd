package org.alicebot.server.core.parser;

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

import java.io.*;
import java.util.*;
import java.net.*;

import org.alicebot.server.core.util.*;

public class AIMLPatternSaver implements AIMLReaderListener,Serializable {
	PrintStream ps;
	public AIMLPatternSaver(String fname) {
		try {
			ps = new PrintStream(new FileOutputStream(fname));
		} catch (Exception e) {System.out.println("PATTERNS: "+e);}
	}
	// [modifeid: added String topic argument, <topic> support (Drent 10-13-1999)]
	public void newCategory(String pattern, String that, String topic,String template) {
		try {
			String t = template;
			t = t.replace('\n',' ');
			if (t.indexOf("+~srai()+") >= 0) {
				t = Substituter.replace("+~srai()+","<srai>",t);
				t = Substituter.replace("+~endai()+","</srai>",t);
			}
			if (t.indexOf("+~set_it()+") >= 0) {
				t = Substituter.replace("+~set_it()+","<set_it>",t);
				t = Substituter.replace("+~endai()+","</set_it>",t);
			}
			if (t.indexOf("+~set_he()+") >= 0) {
				t = Substituter.replace("+~set_he()+","<set_he>",t);
				t = Substituter.replace("+~endai()+","</set_he>",t);
			}
			if (t.indexOf("+~set_they()+") >= 0) {
				t = Substituter.replace("+~set_they()+","<set_they>",t);
				t = Substituter.replace("+~endai()+","</set_they>",t);
			}
			if (t.indexOf("+~set_she()+") >= 0) {
				t = Substituter.replace("+~set_she()+","<set_she>",t);
				t = Substituter.replace("+~endai()+","</set_she>",t);
			}
			if (that==null) that="*";
			if (topic==null) topic="*"; // [added, <topic> support (Drent 10-13-1999)]
			String c = "<category>\n<pattern>"+pattern+"</pattern>\n"+
				"<that>"+that+"</that>"+
				"<template>"+t+"</template>\n</category>";
			ps.println(c);
			System.out.println(pattern);
		}
		catch (Exception e) {System.out.println(pattern+e);}
	}
}

