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

