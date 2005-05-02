package org.aitools.programd.test.aiml;

import java.util.List;

/**
 * Checks that a string contains all of a list
 * of expected keywords.
 * 
 * @author Albertas Mickensas
 */
public class ExpectedKeywordChecker implements Checker
{
    private List<String> expectedKeywords = null;

    /**
     * Creates a new ExpectedKeyWordChecker with a given
     * list of expected keywords.
     * 
     * @param list the list of keywords that are expected to appear in tested inputs
     */
    public ExpectedKeywordChecker(List<String> list)
    {
        this.expectedKeywords = list;
    }

    /**
     * Checks that the given <code>input</code> contains all of this
     * checker's configured list of expected keywords.
     * @see org.aitools.programd.test.aiml.Checker#test(java.lang.String)
     */
    public boolean test(String input)
    {
        if (null != this.expectedKeywords)
        {
            boolean containsAllKeywords = true;
            String testInput = input.toUpperCase();
            for (String s : this.expectedKeywords)
            {
                containsAllKeywords = containsAllKeywords
                        && (testInput.indexOf(s) > -1);
            }
            return containsAllKeywords;
        }
        // otherwise...
        return false;
    }

}
