package org.alicebot.server.core;

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

//-->(old parser) import org.alicebot.server.core.parser.*;
import org.alicebot.server.core.AIMLparser.*;
import org.alicebot.server.core.logging.*;

import org.mozilla.javascript.*;

import java.io.*;
import java.util.*;

/**
 * The Interpreter is used for &lt;script&gt; elements and calling functions.
 *
 * @author Jon Baer
 * @version 1.0
 */

public class Interpreter {
	
	public static String evaluate(String ip, String expr) {
		
		Context cx = Context.enter();
		Scriptable scope = cx.initStandardObjects(null);
		
		Object result = null;
		try {
			result = cx.evaluateString(scope, expr, "<cmd>", 1, null);
		} catch (Exception ex) {
			String err = ex.toString();
			if (err.indexOf("java.io.InterruptedIOException") > 0) {
/*Remove 4.1.1 b2 PEC 09-2001
				result = (String)Graphmaster.match("ERROR " + err, "*", "*").get(Graphmaster.TEMPLATE);
				return (String)(new AIMLParser()).processResponse(ip, (String)result);
*/
                                return "";
			} else {
/*Remove 4.1.1 b2 PEC 09-2001
				result = (String)Graphmaster.match("ERROR " + err, "*", "*").get(Graphmaster.TEMPLATE);
                                return (new AIMLParser()).processResponse(ip, (String)result);
*/
                                return "";
			}
		}
		Context.exit();
		return result.toString();
	}
	
}
