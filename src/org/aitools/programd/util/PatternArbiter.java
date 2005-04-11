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

        // Create iterators for pattern and literal.
        StringCharacterIterator patternIterator;
        StringCharacterIterator literalIterator;

        // Check ignore case flag.
        if (ignoreCase)
        {
            // Iterate over uppercased versions of literal and pattern.
            patternIterator = new StringCharacterIterator(pattern.toUpperCase().trim());
            literalIterator = new StringCharacterIterator(literal.toUpperCase().trim());
        }
        else
        {
            // Iterate over original literal and pattern.
            patternIterator = new StringCharacterIterator(pattern.trim());
            literalIterator = new StringCharacterIterator(literal.trim());
        }

        // Get first characters from both iterators.
        char patternChar = patternIterator.first();
        char literalChar = literalIterator.first();

        // Start with unknown character states.
        int patternCharState = UNKNOWN;
        int literalCharState = UNKNOWN;

        // Start by assuming iterators are not past end.
        int patternIteratorState = NOT_PAST_END;
        int literalIteratorState = NOT_PAST_END;

        // Start by assuming matching should continue.
        int matchState = CONTINUE_MATCHING;

        // Main matching loop.
        while ((matchState & STOP_MATCHING) != STOP_MATCHING)
        {
            // Matching phase 0.
            if ((matchState & CONTINUE_MATCHING) == CONTINUE_MATCHING)
            {
                /*
                 * Advance iterators (if necessary).
                 */

                // Check for advance pattern flag.
                if ((matchState & ADVANCE_PATTERN) == ADVANCE_PATTERN)
                {
                    // Advance the pattern iterator.
                    patternChar = patternIterator.next();
                }

                // Check for advance literal flag.
                if ((matchState & ADVANCE_LITERAL) == ADVANCE_LITERAL)
                {
                    // Advance the literal iterator.
                    literalChar = literalIterator.next();
                }

                // Cancel any other flags besides continue matching.
                matchState = CONTINUE_MATCHING;
            }

            // Matching phase 1.
            if ((matchState & CONTINUE_MATCHING) == CONTINUE_MATCHING)
            {
                /*
                 * Determine character states.
                 */

                /*
                 * Determine literal character state.
                 */

                // Check if literal character is non-letterdigit.
                if (!Character.isLetterOrDigit(literalChar))
                {
                    // Flag literal character is non-letterdigit.
                    literalCharState = IS_NON_LETTERDIGIT;

                    // Check if literal character is whitespace.
                    if (Character.isWhitespace(literalChar))
                    {
                        // Flag literal character is whitespace.
                        literalCharState = literalCharState | IS_WHITESPACE;

                        // Check if literal character is space.
                        if (literalChar == SPACE)
                        {
                            // Flag literal character is space.
                            literalCharState = literalCharState | IS_SPACE;
                        }
                        else
                        {
                            // Stop matching and fail.
                            matchState = STOP_MATCHING | MATCH_FAILURE;
                        }
                    }
                }
                else
                {
                    // Flag literal character is a letterdigit.
                    literalCharState = IS_LETTERDIGIT;
                }

                /*
                 * Determine pattern character state and check pattern character
                 * succession.
                 */

                // Check if pattern character is non-letterdigit.
                if (!Character.isLetterOrDigit(patternChar))
                {
                    // Flag pattern character is non-letterdigit.
                    patternCharState = IS_NON_LETTERDIGIT;

                    // Check if pattern character is whitespace.
                    if (Character.isWhitespace(patternChar))
                    {
                        // Flag pattern character is whitespace.
                        patternCharState = patternCharState | IS_WHITESPACE;

                        // Check if pattern character is space.
                        if (patternChar == SPACE)
                        {
                            // Flag pattern character is space.
                            patternCharState = patternCharState | IS_SPACE;
                        }
                        else
                        {
                            // Stop matching and fail.
                            matchState = STOP_MATCHING | MATCH_FAILURE;
                        }
                    }

                    // Check if pattern character is wildcard.
                    if ((patternChar == ASTERISK) || (patternChar == UNDERSCORE))
                    {
                        // Flag pattern character is wildcard.
                        patternCharState = patternCharState | IS_WILDCARD;
                    }
                }
                else
                {
                    // Flag pattern character is a letterdigit.
                    patternCharState = IS_LETTERDIGIT;
                }
            }

            // Matching phase 2.
            if ((matchState & CONTINUE_MATCHING) == CONTINUE_MATCHING)
            {
                /*
                 * Determine iterator states.
                 */

                // Check if pattern is past end.
                if (patternChar == CharacterIterator.DONE)
                {
                    // Flag pattern is past end.
                    patternIteratorState = PAST_END;
                    // Stop matching.
                    matchState = matchState | STOP_MATCHING;
                }
                else
                {
                    if (patternIterator.getEndIndex() == patternIterator.getIndex() + 1)
                    {
                        // Flag pattern is at end.
                        patternIteratorState = AT_END;
                    }
                }

                // Check if literal is past end.
                if (literalChar == CharacterIterator.DONE)
                {
                    // Flag literal is past end.
                    literalIteratorState = PAST_END;
                    // Stop matching.
                    matchState = matchState | STOP_MATCHING;
                }
                else
                {
                    // Check if literal is at end.
                    if (literalIterator.getEndIndex() == literalIterator.getIndex() + 1)
                    {
                        // Flag literal is at end.
                        literalIteratorState = AT_END;
                    }
                }
            }

            // Matching phase 3.
            if ((matchState & CONTINUE_MATCHING) == CONTINUE_MATCHING)
            {
                /*
                 * Case-based matching.
                 */

                /*
                 * CASE 0: Pattern letterdigit matches only literal letterdigit.
                 */

                // Check if pattern character is exactly a letterdigit.
                if (patternCharState == IS_LETTERDIGIT)
                {
                    // Check if literal character is exactly a letterdigit.
                    if (literalCharState == IS_LETTERDIGIT)
                    {
                        // Check if pattern character equals literal character.
                        if (patternChar == literalChar)
                        {
                            // Flag to advance literal and pattern.
                            matchState = matchState | ADVANCE_LITERAL | ADVANCE_PATTERN;
                        }
                        else
                        {
                            // Stop matching and fail.
                            matchState = STOP_MATCHING | MATCH_FAILURE;
                        }
                    }
                    else
                    {
                        // Stop matching and fail.
                        matchState = STOP_MATCHING | MATCH_FAILURE;
                    }

                    /*
                     * CASE 1: Pattern letterdigit at end means stop matching.
                     */
                    if (patternIteratorState == AT_END)
                    {
                        matchState = matchState | STOP_MATCHING;
                    }
                }

                /*
                 * CASE 2: Pattern wildcard matches literal letterdigit.
                 */

                // Check if pattern character is a wildcard.
                else if ((patternCharState & IS_WILDCARD) == IS_WILDCARD)
                {
                    // Check if literal character is exactly a letterdigit.
                    if (literalCharState == IS_LETTERDIGIT)
                    {
                        // Flag to advance literal.
                        matchState = matchState | ADVANCE_LITERAL;
                    }

                    /*
                     * CASE 3: Pattern wildcard at end matches literal
                     * non-letterdigit.
                     */

                    // Check if literal character is a non-letterdigit.
                    else if ((literalCharState & IS_NON_LETTERDIGIT) == IS_NON_LETTERDIGIT)
                    {
                        // Check if pattern is at end.
                        if (patternIteratorState == AT_END)
                        {
                            // Flag to advance literal.
                            matchState = matchState | ADVANCE_LITERAL;
                        }

                        /*
                         * CASE 4: Pattern wildcard+nonspace is invalid.
                         */
                        else
                        {
                            // Look ahead to next pattern character.
                            char nextPatternChar = patternIterator.next();

                            // Check if it is a space.
                            if (nextPatternChar != SPACE)
                            {
                                // Stop matching and fail.
                                matchState = STOP_MATCHING | MATCH_FAILURE;
                            }

                            /*
                             * CASE 5: Pattern wildcard+space matches literal
                             * space.
                             */
                            else
                            {
                                // Flag to advance literal and pattern.
                                matchState = matchState | ADVANCE_PATTERN | ADVANCE_LITERAL;
                            }
                        }
                    }
                }

                /*
                 * CASE 6: Pattern space matches literal non-letterdigit.
                 */

                // Check if pattern character is a space.
                else if ((patternCharState & IS_SPACE) == IS_SPACE)
                {
                    // Check if literal character is non-letterdigit.
                    if ((literalCharState & IS_NON_LETTERDIGIT) == IS_NON_LETTERDIGIT)
                    {
                        // Flag to advance literal and pattern.
                        matchState = matchState | ADVANCE_PATTERN | ADVANCE_LITERAL;
                    }
                    else
                    {
                        // Stop matching and fail.
                        matchState = STOP_MATCHING | MATCH_FAILURE;
                    }
                }
            }
        }

        /*
         * Matching stopped; check match state and iterator states.
         */

        // Check if match failed.
        if ((matchState & MATCH_FAILURE) == MATCH_FAILURE)
        {
            // Return false because match failed.
            return false;
        }
        // Otherwise, check if literal iterator is at or past end.
        if ((literalIteratorState == AT_END) || (literalIteratorState == PAST_END))
        {
            // Check if pattern iterator is at or past end.
            if ((patternIteratorState == AT_END) || (patternIteratorState == PAST_END))
            {
                // Return true because both iterators are at or past end.
                return true;
            }
            // Otherwise, return false because both iterators are not at or past
            // end.
            return false;
        }
        // Otherwise, return false because literal iterator is not at end.
        return false;
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