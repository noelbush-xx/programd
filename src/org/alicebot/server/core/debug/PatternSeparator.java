package org.alicebot.server.core.debug;

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


import java.io.*;
import java.util.*;
import java.net.*;

import org.alicebot.server.core.*;
import org.alicebot.server.core.parser.*;
import org.alicebot.server.core.util.*;

public class PatternSeparator implements AIMLReaderListener,Serializable {
	PrintStream wps;
	PrintStream aps;
	public PatternSeparator(String afname, String wfname) {
		try {
			aps = new PrintStream(new FileOutputStream(afname));
			wps = new PrintStream(new FileOutputStream(wfname));
		} catch (Exception e) {System.out.println("PATTERNS: "+e);}
	}
	// [modifeid: added String topic argument, <topic> support (Drent 10-13-1999)]
	public void newCategory(String pattern, String that, String topic, String template) {
		try {
			// [modifeid: added <topic> tags, <topic> support (Drent 10-13-1999)]
			String output =((topic==null||topic=="*") ? "" : "<topic name=\""+topic+"\"") +
				"<category>"+"\n"+
				" <pattern>"+pattern+"</pattern>"+"\n"+
				((that == null) ? "" : " <that>"+that+"</that>"+"\n")+
				" <template>"+template+"</template>"+"\n"+
				"</category>"+
				((topic==null||topic=="*") ? "" : "</topic>");
			
			if (pattern.indexOf("*") < 0 && pattern.indexOf("_") < 0) {
				aps.println(output);
			}
			else
				wps.println(output);
		} catch (Exception e) {System.out.println("PATTERNS: "+e);}
	}
}

