package org.alicebot.server.core.interpreter;

import java.util.StringTokenizer;

import org.alicebot.server.core.Interpreter;
import org.alicebot.server.core.logging.Log;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;


/**
 *  An implementation of {@link org.alicebot.server.core.Interpreter}
 *  that handles server-side JavaScript using the Rhino package.
 *
 *  @author Jon Baer
 *  @version 1.0
 */
public class RhinoInterpreter implements Interpreter
{
    /** The string <code>&quot;&lt;cmd&gt;&quot;</code>. */
    private static final String CMD = "<cmd>";

    /** An empty string. */
    private static final String EMPTY_STRING = "";

    public String evaluate(String expression)
    {
        Log.log("evaluate: \"" + expression + "\"", Log.INTERPRETER);
        Context context = Context.enter();
        Scriptable scope = context.initStandardObjects(null);

        Object result = null;
        try
        {
            result = context.evaluateString(scope, expression, CMD, 1, null);
        }
        catch (Exception e)
        {
            Log.userinfo("JavaScript exception (see interpreter log).", Log.ERROR);
            Log.log(e, Log.INTERPRETER);
            StringTokenizer lines = new StringTokenizer(expression, System.getProperty("line.separator"));
            while (lines.hasMoreTokens())
            {
                Log.log(lines.nextToken(), Log.INTERPRETER);
            }
        }
        Context.exit();
        if (result != null)
        {
            return result.toString();
        }
        else
        {
            Log.userinfo("JavaScript returned null!", Log.INTERPRETER);
            return EMPTY_STRING;
        }
    }
}
