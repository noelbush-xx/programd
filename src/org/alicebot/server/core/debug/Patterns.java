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

public class Patterns implements Serializable {
	
	public static void main(String args[]) {
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader( new FileInputStream("German.aiml")));
			PatternPrinter pp = new PatternPrinter();
			AIMLReader ar = new AIMLReader("", br, pp);
			ar.read(); 
			
		} catch (Exception e) {System.out.println(e);}
	}
	
}
