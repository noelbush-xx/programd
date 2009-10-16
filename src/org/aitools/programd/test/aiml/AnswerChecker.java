package org.aitools.programd.test.aiml;

import java.io.UnsupportedEncodingException;

import org.aitools.util.runtime.DeveloperError;
import org.aitools.util.xml.Characters;
import org.aitools.util.xml.XHTML;
import org.aitools.util.Text;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Tests whether a given input equals an expected string.
 * 
 * @author Albertas Mickensas
 * @author <a href="noel@aitools.org">Noel Bush</a>
 */
public class AnswerChecker extends Checker
{
    private String expectedAnswer = null;

    /** A formatter used for outputting XML. */
    private Format _xmlFormat = Format.getPrettyFormat();

    /**
     * Creates a new AnswerChecker with the given expected answer.
     * 
     * @param element the element containing the expected answer
     * @param encoding the encoding of the document from which the element comes
     */
    public AnswerChecker(Element element, String encoding)
    {
        try
        {
            this.expectedAnswer = (new String(Text.renderAsLines(
                    XHTML.breakLines(Characters.unescapeXMLChars(new XMLOutputter(this._xmlFormat).outputString(element
                            .getContent())))).getBytes(encoding))).intern();
        }
        catch (UnsupportedEncodingException e)
        {
            throw new DeveloperError(String.format("Platform does not support encoding \"%s\"!", encoding), e);
        }
    }

    /**
     * Creates a new AnswerChecked with the given expected answer.
     * 
     * @param answer the expected answer
     */
    public AnswerChecker(String answer)
    {
        this.expectedAnswer = answer;
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

    /**
     * @see org.aitools.programd.test.aiml.Checker#getContent()
     */
    @Override
    public String getContent()
    {
        return this.expectedAnswer;
    }

    /**
     * @see org.aitools.programd.test.aiml.Checker#getTagName()
     */
    @Override
    public String getTagName()
    {
        return Checker.TAG_EXPECTED_ANSWER;
    }
}
