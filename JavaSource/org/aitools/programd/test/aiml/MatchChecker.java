package org.aitools.programd.test.aiml;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tests whether a given input matches a regular expression.
 * 
 * @author <a href="noel@aitools.org">Noel Bush</a>
 */
public class MatchChecker extends Checker
{
    private Matcher matcher = null;
    
    private static final String EMPTY_STRING = "";

    /**
     * Creates a new MatchChecker with the given regular expression.
     * 
     * @param pattern the regular expression
     */
    public MatchChecker(String pattern)
    {
        this.matcher = Pattern.compile(pattern, Pattern.CANON_EQ).matcher(EMPTY_STRING);
    }

    /**
     * Tests whether the given input matches the expected answer.
     * 
     * @param input the input to test
     * @return whether the given input matches the expected answer
     * @see org.aitools.programd.test.aiml.Checker#test(java.lang.String)
     */
    @Override
    public boolean test(String input)
    {
        if (null != this.matcher)
        {
            this.matcher.reset(input);
            return this.matcher.matches();
        }
        // otherwise...
        return false;
    }

    /**
     * @see org.aitools.programd.test.aiml.Checker#getContent()
     */
    @Override
    public String getContent()
    {
        return this.matcher.pattern().toString();
    }

    /**
     * @see org.aitools.programd.test.aiml.Checker#getTagName()
     */
    @Override
    public String getTagName()
    {
        return Checker.TAG_EXPECTED_MATCH;
    }
}
