package org.aitools.programd.interpreter;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 * An implementation of {@link org.aitools.programd.interpreter.Interpreter} 
 * that handles server-side JavaScript using the Rhino package.
 * 
 * @author Jon Baer
 * @author Noel Bush
 * @version 4.2
 */
public class RhinoInterpreter implements Interpreter
{
    /** The string <code>&quot;&lt;cmd&gt;&quot;</code>. */
    private static final String CMD = "<cmd>";

    /** An empty string. */
    private static final String EMPTY_STRING = "";
    
    /** The system line separator. */
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");
    
    /** The logger. */
    private static final Logger LOGGER = Logger.getLogger("programd.interpreter");

    public String evaluate(String expression)
    {
        LOGGER.log(Level.FINE, "evaluate: \"" + expression + "\"");
        Context context = Context.enter();
        Scriptable scope = context.initStandardObjects(null);

        Object result = null;
        try
        {
            result = context.evaluateString(scope, expression, CMD, 1, null);
        } 
        catch (Exception e)
        {
            Logger.getLogger("programd.error").log(Level.WARNING, "JavaScript exception (see interpreter log).");
            LOGGER.log(Level.WARNING, "Got exception:" + LINE_SEPARATOR + e + LINE_SEPARATOR + "when processing:" + LINE_SEPARATOR + expression);
        } 
        Context.exit();
        if (result != null)
        {
            return result.toString();
        } 
        // (otherwise...)
        LOGGER.log(Level.INFO, "JavaScript returned null!");
        return EMPTY_STRING;
    } 
}