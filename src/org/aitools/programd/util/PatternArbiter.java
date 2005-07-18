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
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.regex.Pattern;

/**
 * Provides utility methods for pattern-oriented tasks.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.1.3
 */
public class PatternArbiter
{
    // Constants used for comparison.

    /** The asterisk AIML wildcard. */
    private static final char ASTERISK = '*';

    /** The underscore AIML wildcard. */
    private static final char UNDERSCORE = '_';

    /** A space. */
    private static final char SPACE = ' ';

    /** A tag start. */
    private static final char TAG_START = '<';

    /** A quote mark. */
    private static final char QUOTE_MARK = '"';

    /** Start of a bot property element. */
    private static final String BOT_NAME_EQUALS = "<bot name=\"";

    /** End of an atomic [bot property] element. */
    private static final String ATOMIC_ELEMENT_END = "\"/>";

    // Character states. See comments in matches().

    /** Character is unknown (yet). */
    private static final int UNKNOWN = 1;

    /** Character is a wildcard. */
    private static final int IS_WILDCARD = 2;

    /** Character is a letter or digit. */
    private static final int IS_LETTERDIGIT = 4;

    /** Character is a space. */
    private static final int IS_SPACE = 8;

    /** Character is any whitespace. */
    private static final int IS_WHITESPACE = 16;

    /** Character is any non-letter/digit. */
    private static final int IS_NON_LETTERDIGIT = 32;

    // Iterator states.

    /** Iterator is not past end. */
    private static final int NOT_PAST_END = 0;

    /** Iterator is at end. */
    private static final int AT_END = 1;

    /** Iterator is past end. */
    private static final int PAST_END = 2;

    // Matching action states.

    /** Continue matching. */
    private static final int CONTINUE_MATCHING = 1;

    /** Stop matching. */
    private static final int STOP_MATCHING = 2;

    /** Match failure. */
    private static final int MATCH_FAILURE = 4;

    /** Advance pattern iterator. */
    private static final int ADVANCE_LITERAL = 8;

    /** Advance literal iterator. */
    private static final int ADVANCE_PATTERN = 16;

    /**
     * Decides whether a given pattern matches a given literal, in an isolated
     * context, according to the AIML pattern-matching specification.
     * 
     * Indicates whether the given literal is matched by the given pattern.
     * Note that the mechanism here is very simple: the AIML pattern is
     * converted into an equivalent regular expression, and a match test
     * is performed.  This appears to be much more reliable than an old
     * method that "manually" checked the match.
     * 
     * @param literal the literal string to check
     * @param pattern the pattern to try to match against it
     * @param ignoreCase whether or not to ignore case
     * @return <code>true</code> if <code>pattern</code> matches
     *         <code>literal</code>,<code>false</code> if not
     * @throws NotAnAIMLPatternException if the pattern is not a valid AIML
     *             pattern (conditioned by <code>ignoreCase</code>
     */
    public static boolean matches(String literal, String pattern, boolean ignoreCase) throws NotAnAIMLPatternException
    {
        /*
         * Check the pattern for validity. If it is invalid, an exception with a
         * helpful message will be thrown.
         */
        checkAIMLPattern(pattern, ignoreCase);

        Pattern regex = Pattern.compile(pattern.replaceAll("(\\*|_)", "[^ ]+( [^ ]+)*"),
                        (ignoreCase ? Pattern.UNICODE_CASE : 0));
        return regex.matcher(literal).matches();
    }
    
    /**
     * Determines whether a given string is a valid AIML pattern. Conditioned by
     * <code>ignoreCase</code>.
     * 
     * @param pattern the string to check
     * @param ignoreCase whether to ignore case
     * @throws NotAnAIMLPatternException with a helpful message if the pattern
     *             is not valid
     */
    public static void checkAIMLPattern(String pattern, boolean ignoreCase) throws NotAnAIMLPatternException
    {
        // Create iterator for pattern candidate.
        StringCharacterIterator iterator = new StringCharacterIterator(pattern);

        // Start with unknown character and previous character states.
        int charState = UNKNOWN;
        int previousCharState = UNKNOWN;

        // Iterate over all characters.
        for (char theChar = iterator.first(); theChar != CharacterIterator.DONE; theChar = iterator.next())
        {
            /*
             * Determine character state and check character succession.
             */

            // Check if pattern character is non-letterdigit.
            if (!Character.isLetterOrDigit(theChar))
            {
                // Flag pattern character is non-letterdigit.
                charState = IS_NON_LETTERDIGIT;

                // Check if pattern character is whitespace.
                if (Character.isWhitespace(theChar))
                {
                    // Flag pattern character is whitespace.
                    charState = charState | IS_WHITESPACE;

                    // Check if pattern character is space.
                    if (theChar == SPACE)
                    {
                        // Flag pattern character is space.
                        charState = charState | IS_SPACE;
                    }
                    else
                    {
                        // Throw an explanatory exception.
                        throw new NotAnAIMLPatternException("The only allowed whitespace is a space (\u0020).", pattern);
                    }
                }

                // Check if pattern character is wildcard.
                if ((theChar == ASTERISK) || (theChar == UNDERSCORE))
                {
                    // Flag pattern character is wildcard.
                    charState = charState | IS_WILDCARD;

                    // Check if previous pattern character state is known.
                    if (previousCharState != UNKNOWN)
                    {
                        /*
                         * Check if previous pattern character state is neither
                         * a wildcard nor exactly a letterdigit.
                         */
                        if ((previousCharState == IS_LETTERDIGIT) || ((previousCharState & IS_WILDCARD) == IS_WILDCARD))
                        {
                            // Throw an explanatory exception.
                            throw new NotAnAIMLPatternException("A wildcard cannot be preceded by a wildcard, a letter or a digit.", pattern);
                        }
                    }
                }

                // Check if pattern character is a tag start.
                if (theChar == TAG_START)
                {
                    /*
                     * Check if <bot name=" appears now. This is (currently) the
                     * only allowed element inside pattern.
                     */
                    int currentIndex = iterator.getIndex();

                    if (pattern.regionMatches(false, currentIndex, BOT_NAME_EQUALS, 0, 11))
                    {
                        /*
                         * Now iterate through the chars until reaching a quote
                         * mark, checking that each char is valid for a property
                         * name.
                         */
                        iterator.setIndex(currentIndex + 11);
                        theChar = iterator.next();
                        while ((theChar != CharacterIterator.DONE) && (theChar != QUOTE_MARK)
                                && (Character.isLetterOrDigit(theChar) || (theChar == SPACE) || (theChar == UNDERSCORE)))
                        {
                            theChar = iterator.next();
                        }

                        // Finally, check that the attribute is ended correctly.
                        currentIndex = iterator.getIndex();
                        if (!pattern.regionMatches(false, currentIndex, ATOMIC_ELEMENT_END, 0, 3))
                        {
                            throw new NotAnAIMLPatternException("Invalid or malformed <bot/> element.", pattern);
                        }
                        // If we got this far, update the index.
                        iterator.setIndex(currentIndex + 3);
                    }
                    else
                    {
                        throw new NotAnAIMLPatternException("Invalid or malformed inner element.", pattern);
                    }
                }
            }
            else
            {
                // Flag pattern character is a letterdigit.
                charState = IS_LETTERDIGIT;

                // Check case if ignoreCase is false.
                if (!ignoreCase)
                {
                    if (Character.toUpperCase(theChar) != theChar)
                    {
                        // Throw an explanatory exception.
                        throw new NotAnAIMLPatternException("Characters with case mappings must be uppercase.", pattern);
                    }
                }

                // Check if previous pattern character state is known.
                if (previousCharState != UNKNOWN)
                {
                    /*
                     * Check if previous pattern character state is exactly a
                     * wildcard.
                     */
                    if ((previousCharState & IS_WILDCARD) == IS_WILDCARD)
                    {
                        // Throw an explanatory exception.
                        throw new NotAnAIMLPatternException("A letter or digit may not be preceded by a wildcard.", pattern);
                    }
                }
            }

            // Remember pattern character state for next time.
            previousCharState = charState;
        }
    }

    /**
     * For testing.
     * 
     * @param args not used
     */
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
                    System.out.println("Exception: " + e.getMessage());
                    matched = false;
                }
                time = new Date().getTime() - time;

                if (matched == prediction)
                {
                    successes++;
                    System.out.print("TEST PASSED] ");
                }
                else
                {
                    failures++;
                    System.out.print("TEST FAILED] ");
                }
                if (matched)
                {
                    System.out.print("match: " + literal + " | " + pattern + (ignoreCase ? " (ignoreCase)" : ""));
                }
                else
                {
                    System.out.print("no match: " + literal + " | " + pattern + (ignoreCase ? " (ignoreCase)" : ""));
                }
                System.out.println(" (" + time + " ms)");
            }
            else
            {
                System.out.println(theLine);
            }
        }
        System.out.println((successes + failures) + " tests: " + successes + " successes, " + failures + " failures");
    }
}