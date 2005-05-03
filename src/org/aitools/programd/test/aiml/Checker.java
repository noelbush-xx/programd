package org.aitools.programd.test.aiml;

import org.aitools.programd.util.DeveloperError;
import org.w3c.dom.Element;

/**
 * Performs a specific test on a given input.
 * 
 * @author Albertas Mickensas
 * @since 4.5
 */
abstract public class Checker
{
    /**
     * Determines whether the given input passes the Checker's test.
     * 
     * @param input the input to test
     * @return whether the given input passes the Checker's test
     */
    abstract public boolean test(String input);

    /**
     * Creates a new Checker of the correct type based on the contents of the
     * given XML element.
     * 
     * @param element the element from which to create the Input
     * @return the created Input
     */
    public static Checker create(Element element)
    {
        String tagName = element.getTagName();

        // Create the appropriate type of Checker.
        if (tagName.equals(TestCase.TAG_ALERT_KEYWORDS))
        {
            return new AlertKeywordChecker(element.getTextContent());
        }
        else if (tagName.equals(TestCase.TAG_EXPECTED_ANSWER))
        {
            return new AnswerChecker(element);
        }
        else if (tagName.equals(TestCase.TAG_EXPECTED_KEYWORDS))
        {
            return new ExpectedKeywordChecker(element.getTextContent());
        }
        else if (tagName.equals(TestCase.TAG_EXPECTED_LENGTH))
        {
            return new LengthChecker(element.getTextContent());
        }
        else
        {
            throw new DeveloperError("Some invalid element (\"" + tagName
                    + "\") slipped past the schema!", new IllegalArgumentException());
        }
    }
}
