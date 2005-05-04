package org.aitools.programd.test.aiml;

import org.w3c.dom.Element;

import org.aitools.programd.util.StringKit;
import org.aitools.programd.util.XMLKit;

/**
 * Tests whether a given input equals an expected string.
 * 
 * @author Albertas Mickensas
 */
public class AnswerChecker extends Checker
{
    private String expectedAnswer = null;

    /**
     * Creates a new AnswerChecked with the given expected answer.
     * 
     * @param element the element containing the expected answer
     */
    public AnswerChecker(Element element)
    {
        this.expectedAnswer = StringKit.renderAsLines(XMLKit.filterViaHTMLTags(XMLKit.renderXML(
                element.getChildNodes(), false)));
    }

    /**
     * Tests whether the given input matches the expected answer.
     * 
     * @param input the input to test
     * @return whether the given input matches the expected answer
     * @see org.aitools.programd.test.aiml.Checker#test(java.lang.String)
     */
    public boolean test(String input)
    {
        if (null != this.expectedAnswer)
        {
            if (input.equals(this.expectedAnswer))
            {
                return true;
            }
            // otherwise...
            return false;
        }
        // otherwise...
        return false;
    }

}
