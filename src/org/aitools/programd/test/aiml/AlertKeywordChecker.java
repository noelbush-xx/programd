package org.aitools.programd.test.aiml;

/**
 * Tests whether a given input does <i>not</i> contain all of a list of
 * expected keywords.
 * 
 * @author Albertas Mickensas
 */
public class AlertKeywordChecker extends ExpectedKeywordChecker
{
    /**
     * Creates a new AlertKeywordChecker with the given list of keywords.
     * 
     * @param list the comma-separated list of keywords
     */
    public AlertKeywordChecker(String list)
    {
        super(list);
    }

    /**
     * Tests whether a given input does <i>not</i> contain all of a list of
     * expected keywords.
     * 
     * @see org.aitools.programd.test.aiml.ExpectedKeywordChecker#test(java.lang.String)
     */
    public boolean test(String input)
    {
        return !super.test(input);
    }

}
