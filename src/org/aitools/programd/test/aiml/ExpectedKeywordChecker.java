package org.aitools.programd.test.aiml;

import org.w3c.dom.Element;

/**
 * Checks that a string contains all of a list
 * of expected keywords.
 * 
 * @author Albertas Mickensas
 */
public class ExpectedKeywordChecker extends CheckerBase
{
    private Keyword keywords;

    /**
     * Creates a new ExpectedKeyWordChecker with a given
     * list of expected keywords.
     * 
     * @param element the keyword (possibly a nested set) to add
     */
    public ExpectedKeywordChecker(Element element)
    {
        this.keywords = KeywordFactory.create(element);
    }

    /**
     * Checks that the given <code>input</code> contains all of this
     * checker's configured list of expected keywords.
     * @see org.aitools.programd.test.aiml.Checker#test(java.lang.String)
     */
    public boolean test(String input)
    {
        boolean result = false;
        for (String keyword : this.keywords)
        {
            result |= input.contains(keyword);
        }
        return result;
    }
}
