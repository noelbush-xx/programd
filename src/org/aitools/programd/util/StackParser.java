package org.aitools.programd.util;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.StringTokenizer;

/**
 * Parses the stack trace. This code was adapted from a Usenet post by Simeon
 * Fitch.
 * 
 * @author Simeon Fitch sfitch@swri.org
 * @see <a
 *      href="http://groups.google.com/groups?hl=en&threadm=37F455F1.E490748%40mf.uni-lj.si&rnum=2&prev=/groups%3Fq%3Djava%2B%2522get%2Bmethod%2522%2Bcalled%2Bmethod%26hl%3Den%26sa%3DN%26tab%3Dwg">this
 *      message</a>
 */
public class StackParser
{
    /** &quot;at &quot;. */
    private static final String AT = "at ";

    /**
     * Private constructor prevents instantiating this class.
     */
    private StackParser()
    {
        // Nothing to do.
    }

    /**
     * Gets the name of the method in the stack the given number of levels down.
     * 
     * @param throwable throwable from which to get stack trace
     * @param level number of stack frames to go down.
     * @return method name if found, null otherwise
     */
    public static String getStackMethod(Throwable throwable, int level)
    {
        String method = null;

        String backtrace = getStackString(throwable);

        int start = 0;
        while (level-- >= 0 && start >= 0)
        {
            start = backtrace.indexOf(AT, start + 3);
        }

        if (start > 0)
        {
            int finish = backtrace.indexOf('(', start);
            if (finish > 0)
            {
                method = backtrace.substring(start + 3, finish);
            }
        }
        return method;
    }

    /**
     * Gets the name of the method in the stack the given number of levels down.
     * 
     * @param level Number of stack frames to go.
     * @return Method name if found, null otherwise.
     */
    public static String getStackMethod(int level)
    {
        // Increment level to include call to this method.
        level++;
        Throwable throwable = new Throwable().fillInStackTrace();
        return getStackMethod(throwable, level);
    }

    /**
     * Gets the name of the most recent method in the stack that does not
     * include the given search string. If <code>prettify</code> is true, also
     * removes whatever looks like a package name from the result.
     * 
     * @param search the string which, if found in the method name, disqualifies
     *            it
     * @param prettify whether to remove whatever looks like a package name from
     *            the result
     * @return the most recent method in the stack that does not include the
     *         given search string
     */
    public static String getStackMethodBefore(String search, boolean prettify)
    {
        int level = 1;
        String method = getStackMethod(level);
        while ((method.indexOf(search) != -1) && (method != null))
        {
            method = getStackMethod(++level);
        }

        if (prettify)
        {
            int nextToLastDot = method.lastIndexOf('.', method.lastIndexOf('.') - 1);
            if (nextToLastDot != -1)
            {
                return method.substring(nextToLastDot + 1);
            }
            // (otherwise...)
            return method;
        }
        // (otherwise...)
        return method;
    }

    /**
     * Creates a string containing the stack backtrace of the given Throwable
     * object.
     * 
     * @param throwable source of backtrace
     * @return String containing backtrace
     */
    public static String getStackString(Throwable throwable)
    {
        StringWriter writer = new StringWriter();
        throwable.printStackTrace(new PrintWriter(writer));
        return writer.toString();
    }

    /**
     * Returns the backtrace of a given throwable as an enumeration of lines.
     * 
     * @param throwable the Throwable whose backtrace we want
     * @return the backtrace as an enumeration of lines
     */
    public static StringTokenizer getStackTraceFor(Throwable throwable)
    {
        return new StringTokenizer(getStackString(throwable), System.getProperty("line.separator"));
    }
}