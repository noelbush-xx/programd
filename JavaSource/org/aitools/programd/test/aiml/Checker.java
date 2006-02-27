package org.aitools.programd.test.aiml;

import java.io.UnsupportedEncodingException;

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
    /** The string &quot;{@value}&quot;. */
    public static String TAG_EXPECTED_ANSWER = "ExpectedAnswer";

    /** The string &quot;{@value}&quot;. */
    public static String TAG_EXPECTED_KEYWORDS = "ExpectedKeywords";

    /** The string &quot;{@value}&quot;. */
    public static String TAG_EXPECTED_LENGTH = "ExpectedLength";

    /** The string &quot;{@value}&quot;. */
    public static String TAG_EXPECTED_MATCH = "ExpectedMatch";

    /** The string &quot;{@value}&quot;. */
    public static String TAG_ALERT_KEYWORDS = "AlertKeywords";

    /**
     * Determines whether the given input passes the Checker's test.
     * 
     * @param input the input to test
     * @return whether the given input passes the Checker's test
     */
    abstract public boolean test(String input);

    /**
     * A protected constructor, for use in persistence.
     */
    protected Checker()
    {
        // Do nothing.
    }
    
    /**
     * Creates a new Checker of the correct type based on the contents of the
     * given XML element.
     * 
     * @param element the element from which to create the Input
     * @param encoding the encoding of the document from which this element comes
     * @return the created Input
     */
    public static Checker create(Element element, String encoding)
    {
        String tagName = element.getTagName();

        // Create the appropriate type of Checker.
        if (tagName.equals(TAG_ALERT_KEYWORDS))
        {
            try
            {
                return new AlertKeywordChecker(new String(element.getTextContent().getBytes(encoding)).intern());
            }
            catch (UnsupportedEncodingException e)
            {
                throw new DeveloperError("Platform does not support \"" + encoding + "\" encoding!", e);
            }
        }
        else if (tagName.equals(TAG_EXPECTED_ANSWER))
        {
            return new AnswerChecker(element, encoding);
        }
        else if (tagName.equals(TAG_EXPECTED_KEYWORDS))
        {
            try
            {
                return new ExpectedKeywordChecker(new String(element.getTextContent().getBytes(encoding)).intern());
            }
            catch (UnsupportedEncodingException e)
            {
                throw new DeveloperError("Platform does not support \"" + encoding + "\" encoding!", e);
            }
        }
        else if (tagName.equals(TAG_EXPECTED_LENGTH))
        {
            try
            {
                return new LengthChecker(new String(element.getTextContent().getBytes(encoding)).intern());
            }
            catch (UnsupportedEncodingException e)
            {
                throw new DeveloperError("Platform does not support \"" + encoding + "\" encoding!", e);
            }
        }
        else if (tagName.equals(TAG_EXPECTED_MATCH))
        {
            try
            {
                return new MatchChecker(new String(element.getTextContent().getBytes(encoding)).intern());
            }
            catch (UnsupportedEncodingException e)
            {
                throw new DeveloperError("Platform does not support \"" + encoding + "\" encoding!", e);
            }
        }
        else
        {
            throw new DeveloperError("Some invalid element (\"" + tagName
                    + "\") slipped past the schema!", new IllegalArgumentException());
        }
    }
    
    /**
     * @return the textual content of the checker
     */
    abstract public String getContent();
    
    /**
     * @return the tag name that the checker uses
     */
    abstract public String getTagName();
}
