/*
    Alicebot Program D
    Copyright (C) 1995-2001, A.L.I.C.E. AI Foundation
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
    USA.
*/

/*
    Code cleanup (4.1.3 [00] - October 2001, Noel Bush)
    - formatting cleanup
    - general grammar fixes
    - complete javadoc
    - made all imports explicit
*/

package org.alicebot.server.core;

import java.util.StringTokenizer;

import org.alicebot.server.core.logging.Log;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.JavaScriptException;
import org.mozilla.javascript.Scriptable;


/**
 *  Handles server-side JavaScript.
 *
 *  @author Jon Baer
 *  @version 1.0
 */
public class Interpreter
{
    /** The string <code>&quot;&lt;cmd&gt;&quot;</code>. */
    private static final String CMD = "<cmd>";

    public static String evaluate(String userid, String expression)
    {
        Context context = Context.enter();
        Scriptable scope = context.initStandardObjects(null);

        Object result = null;
        try
        {
            result = context.evaluateString(scope, expression, CMD, 1, null);
        }
        catch (JavaScriptException e)
        {
            Log.userinfo("JavaScript exception (see interpreter log).", Log.ERROR);
            Log.userinfo("JavaScript exception when processing:", Log.INTERPRETER);
            StringTokenizer lines = new StringTokenizer(expression, System.getProperty("line.separator"));
            while (lines.hasMoreTokens())
            {
                Log.userinfo(lines.nextToken(), Log.INTERPRETER);
            }
        }
        Context.exit();
        return result.toString();
    }
}
