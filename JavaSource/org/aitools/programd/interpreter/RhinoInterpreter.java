package org.aitools.programd.interpreter;

import org.apache.log4j.Logger;
import org.mozilla.javascript.Context;
import org.mozilla.javascript.Scriptable;

/**
 * An implementation of {@link org.aitools.programd.interpreter.Interpreter} that handles server-side JavaScript using
 * the Rhino package.
 * 
 * @author Jon Baer
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class RhinoInterpreter implements Interpreter
{
    /** The logger. */
    private static final Logger logger = Logger.getLogger("programd");

    /**
     * @see org.aitools.programd.interpreter.Interpreter#evaluate(java.lang.String)
     */
    public String evaluate(String expression)
    {
        logger.debug("evaluate: \"" + expression + "\"");
        Context context;
        try
        {
            context = Context.enter();
        }
        // If the Rhino js library is somehow missing....
        catch (NoClassDefFoundError e)
        {
            logger.error("Rhino JavaScript library is missing!", e);
            return "";
        }
        Scriptable scope = context.initStandardObjects(null);

        Object result = null;
        try
        {
            result = context.evaluateString(scope, expression, "<cmd>", 1, null);
        }
        catch (Exception e)
        {
            logger.warn(String.format(
                    "JavaScript exception (see interpreter log).%nGot exception:%n%s%nwhen processing:%n%s", e,
                    expression));
        }
        Context.exit();
        if (result != null)
        {
            return result.toString();
        }
        // (otherwise...)
        logger.info("JavaScript returned null!");
        return "";
    }
}
