package org.aitools.programd.test.aiml;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import org.aitools.programd.multiplexor.Multiplexor;
import org.aitools.programd.util.XMLKit;

/**
 * A TestCase contains an inputs and a set of checkers that test the response to
 * that inputs.
 * 
 * @author Albertas Mickensas
 */
public class TestCase
{
    /** The string &quot;{@value}&quot;. */
    public static String TAG_TESTCASE = "TestCase";

    /** The string &quot;{@value}&quot;. */
    public static String TAG_EXPECTED_ANSWER = "ExpectedAnswer";

    /** The string &quot;{@value}&quot;. */
    public static String TAG_EXPECTED_KEYWORDS = "ExpectedKeywords";

    /** The string &quot;{@value}&quot;. */
    public static String TAG_EXPECTED_LENGTH = "ExpectedLength";

    /** The string &quot;{@value}&quot;. */
    public static String TAG_ALERT_KEYWORDS = "AlertKeywords";

    /** The name of this test case. */
    private String name;

    /** The inputs that this test case should send. */
    private String input;

    /** The checker(s) contained in this test case. */
    private ArrayList<Checker> checkers = new ArrayList<Checker>();

    /** The last response received by this test case. */
    private String lastResponse;

    /**
     * Creates a new TestCase from the given XML element.
     * 
     * @param element the TestCase element
     */
    public TestCase(Element element)
    {
        this.name = element.getAttribute("name");

        List<Element> elements = XMLKit.getElementChildrenOf(element);

        // We rely on the schema here!
        this.input = elements.get(0).getTextContent();
        for (Element checker : elements.subList(1, elements.size()))
        {
            this.checkers.add(Checker.create(checker));
        }

    }

    /**
     * @return the name of this test case
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @return the input to be sent by this test case
     */
    public String getInput()
    {
        return this.input;
    }

    /**
     * @return the last response received by this test case
     */
    public String getLastResponse()
    {
        return this.lastResponse;
    }

    /**
     * Runs this test case for the given botid.
     * 
     * @param multiplexor the Multiplexor to use for testing
     * @param userid the userid to use when testing
     * @param botid the bot for whom to run this test case
     * @return whether the test passed
     */
    public boolean run(Multiplexor multiplexor, String userid, String botid)
    {
        this.lastResponse = multiplexor.getResponse(this.input, userid, botid);
        return responseIsValid(this.lastResponse);
    }

    /**
     * Response is valid if at least one of the checkers returns a positive
     * result.
     * 
     * @param response the response to check
     * @return whether or not the response is valud
     */
    private boolean responseIsValid(String response)
    {
        boolean result = false;
        for (Checker checker : this.checkers)
        {
            result |= checker.test(response);
        }
        return result;
    }
}
