package org.aitools.programd.test.aiml;

import org.w3c.dom.Element;

/**
 * Tests whether a given input does not
 * contain all of a list of expected keywords.
 * 
 * @author Albertas Mickensas
 */
public class AlertKeywordChecker extends ExpectedKeywordChecker
{
    /**
     * Creates a new AlertKeywordChecker with
     * the given list of keywords.
     * 
     * @param element the keyword (possibly a nested set) to add
     */
    public AlertKeywordChecker(Element element)
    {
        super(element);
    }

    /**
     * Tests whether a given input does not
     * contain all of a list of expected keywords.
     * 
     * @see org.aitools.programd.test.aiml.ExpectedKeywordChecker#test(java.lang.String)
     */
    public boolean test(String input)
    {
        return !super.test(input);
    }

}
