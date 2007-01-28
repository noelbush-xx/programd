/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

import org.aitools.util.runtime.Errors;

/**
 * Provides utility methods for pattern-oriented tasks.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class PatternArbiter
{
    /** The regular expression that defines AIML pattern syntax. */
    private static final Pattern AIML_PATTERN = Pattern
            .compile("(\\*|_|[\\p{javaUpperCase}\\p{javaDigit}]+)( (\\*|_|[\\p{javaUpperCase}\\p{javaDigit}]+))*");

    /** The generic normalization regex that matches any nonalphanumeric. */
    private static final Pattern NON_ALPHANUMERIC = Pattern
            .compile("[^\\p{javaUpperCase}\\p{javaLowerCase}\\p{javaWhitespace}\\p{javaDigit} ]+");
    
    /** A pattern that matches multiple consecutive spaces. */
    private static final Pattern MLC_SPACES = Pattern.compile("  +");

    /**
     * Applies a generic set of normalizations to an input, to prepare it for pattern matching.
     * 
     * @param string the input to normalize
     * @return the normalized input
     */
    public static String genericallyNormalize(String string)
    {
        return MLC_SPACES.matcher(NON_ALPHANUMERIC.matcher(string).replaceAll(" ")).replaceAll(" ").trim();
    }

    /**
     * Translates the given AIML pattern to a regular expression and compiles it into a Pattern object. Useful if you
     * need to do a ton of tests with a pattern.
     * 
     * @param pattern the pattern to compile
     * @param ignoreCase whether to ignore case in matching
     * @return the compiled pattern (translated to regex)
     * 
     * @throws NotAnAIMLPatternException if the pattern is not a valid AIML pattern (conditioned by
     *             <code>ignoreCase</code>
     */
    public static Pattern compile(String pattern, boolean ignoreCase) throws NotAnAIMLPatternException
    {
        // Check the pattern for validity. If it is invalid, throw an exception with a helpful message.
        if (!isValidAIMLPattern(pattern))
        {
            throw new NotAnAIMLPatternException(String.format("\"%s\" does not match the definition of AIML pattern.", pattern), pattern);
        }

        return Pattern
                .compile(pattern.replaceAll("(\\*|_)", "[^ ]+( [^ ]+)*"), (ignoreCase ? Pattern.UNICODE_CASE : 0));
    }

    /**
     * Decides whether a given pattern matches a given literal, in an isolated context, according to the AIML
     * pattern-matching specification.
     * 
     * Indicates whether the given literal is matched by the given pattern. Note that the mechanism here is very simple:
     * the AIML pattern is converted into an equivalent regular expression, and a match test is performed. This appears
     * to be much more reliable than an old method that "manually" checked the match.
     * 
     * This method uses a generic normalization that removes all punctuation from the input.
     * 
     * @param literal the literal string to check
     * @param pattern the pattern to try to match against it
     * @param ignoreCase whether or not to ignore case
     * @return <code>true</code> if <code>pattern</code> matches <code>literal</code>,<code>false</code> if
     *         not
     * @throws NotAnAIMLPatternException if the pattern is not a valid AIML pattern (conditioned by
     *             <code>ignoreCase</code>
     */
    public static boolean matches(String literal, String pattern, boolean ignoreCase) throws NotAnAIMLPatternException
    {
        Pattern regex = compile(pattern, ignoreCase);
        return regex.matcher(genericallyNormalize(literal)).matches();
    }

    /**
     * Determines whether a given string is a valid AIML pattern.
     * 
     * @param pattern the string to check
     * @return whether the string is a valid AIML pattern
     */
    public static boolean isValidAIMLPattern(String pattern)
    {
        return AIML_PATTERN.matcher(pattern).matches();
    }

    /**
     * For testing.
     * 
     * @param args not used
     */
    @SuppressWarnings("boxing")
    public static void main(String[] args)
    {
        BufferedReader console = new BufferedReader(new InputStreamReader(System.in));
        String literal = null;
        String pattern = null;
        boolean ignoreCase = false;
        boolean prediction = false;
        boolean matched;
        StringTokenizer tokenizer;

        int failures = 0;
        int successes = 0;

        console: while (true)
        {
            String theLine = null;
            try
            {
                theLine = console.readLine();
            }
            catch (IOException e)
            {
                System.out.println("Cannot read from console!");
                return;
            }
            if (theLine == null)
            {
                break;
            }
            if (theLine.toLowerCase().equals("exit"))
            {
                System.out.println("Exiting.");
                return;
            }
            if (!theLine.startsWith(";") && theLine.trim().length() > 0)
            {
                tokenizer = new StringTokenizer(theLine, "|");
                try
                {
                    literal = tokenizer.nextToken();
                    pattern = tokenizer.nextToken();
                    ignoreCase = tokenizer.nextToken().equals("y") ? true : false;
                    prediction = tokenizer.nextToken().equals("t") ? true : false;
                }
                catch (NoSuchElementException e)
                {
                    System.out.println("Improperly formatted input. Use: literal|PATTERN|(y/n)|(t/f)");
                    continue console;
                }

                long time = new Date().getTime();

                try
                {
                    matched = matches(literal, pattern, ignoreCase);
                }
                catch (NotAnAIMLPatternException e)
                {
                    System.out.println(String.format("Exception: \"%s\"", Errors.describe(e)));
                    matched = false;
                }
                time = new Date().getTime() - time;

                System.out.print("TEST ");
                if (matched == prediction)
                {
                    successes++;
                    System.out.print("PASSED] ");
                }
                else
                {
                    failures++;
                    System.out.print("FAILED] ");
                }
                if (!matched)
                {
                    System.out.print("no ");
                }
                System.out.print(String.format("match: %s | %s%s (%d ms)", literal, pattern, (ignoreCase ? " (ignoreCase)" : ""), time));
            }
            else
            {
                System.out.println(theLine);
            }
        }
        System.out.println(String.format("%d tests: %d successes, %d failures.", (successes + failures), successes, failures));
    }
}
