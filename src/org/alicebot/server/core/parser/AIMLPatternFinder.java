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
	
	public class AIMLPatternFinder implements AIMLReaderListener,Serializable {
	StringSet w = new StringSet();
	public AIMLPatternFinder() { 
	}
	// [modifeid: added String topic argument, <topic> support (Drent 10-13-1999)]
	public void newCategory(String pattern, String that, String topic, String template) {
		if (pattern.indexOf("*") < 0 && pattern.indexOf("_") < 0) {
			pattern=pattern.trim();
			w.add(pattern);
		}
	}
}

