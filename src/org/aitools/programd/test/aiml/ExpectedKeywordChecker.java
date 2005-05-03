package org.aitools.programd.test.aiml;

import java.util.Arrays;
import java.util.List;

/**
 * Checks that a string contains all of a list of expected keywords.
 * 
 * @author Albertas Mickensas
 */
public class ExpectedKeywordChecker extends Checker
{
    private List<String> keywords;

    /**
     * Creates a new ExpectedKeyWordChecker with a given list of expected
     * keywords.
     * 
     * @param list the comma-separated list of keywords
     */
    public ExpectedKeywordChecker(String list)
    {
        this.keywords = Arrays.asList(list.split(","));
    }

    /**
     * Checks that the given <code>input</code> contains all of this checker's
     * configured list of expected keywords.
     * 
     * @see org.aitools.programd.test.aiml.Checker#test(java.lang.String)
     */
    public boolean test(String input)
    {
        boolean result = true;
        for (String keyword : this.keywords)
        {
            result &= input.contains(keyword);
        }
        return result;
    }
}
