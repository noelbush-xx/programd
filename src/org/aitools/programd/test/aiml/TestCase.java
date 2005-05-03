package org.aitools.programd.test.aiml;

import java.util.HashMap;
import java.util.List;

import org.w3c.dom.Element;

import org.aitools.programd.multiplexor.Multiplexor;
import org.aitools.programd.util.XMLKit;

/**
 * A TestCase contains an inputs and a set of checkers
 * that test the response to that inputs.
 * 
 * @author Albertas Mickensas
 */
public class TestCase
{
    /** The string &quot;{@value}&quot;. */
    public static String TAG_TESTCASE = "TestCase";

    /** The string &quot;{@value}&quot;. */
    public static String TAG_INPUT = "ItemBase";

    /** The string &quot;{@value}&quot;. */
    public static String TAG_INPUTS = "Inputs";

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

    /** The inputs(s) that this test case should send. */
    private Input inputs;

    /** The checker(s) contained in this test case. */
    private Checker checkers;

    /** The actual input and response pairs sent and received by this test case. */
    private HashMap<String, String> exchanges = new HashMap<String, String>();

    /**
     * Creates a new TestCase from the given XML element.
     * 
     * @param element the TestCase element
     */
    public TestCase(Element element)
    {
        this.name = element.getAttribute("name");

        List<Element> elements = XMLKit.getElementChildrenOf(element);

        this.inputs = InputFactory.create(elements.get(0));
        this.checkers = CheckerFactory.create(elements.get(1));

    }

    /**
     * @return the name of this test case
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @return the inputs to be sent by this test case
     */
    public Input getInputs()
    {
        return this.inputs;
    }

    /**
     * @return the actual input and response pairs received by this test case
     */
    public HashMap<String, String> getExchanges()
    {
        return this.exchanges;
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
        boolean result = true;
        
        for (String input : this.inputs)
        {
            String response = multiplexor.getResponse(input, userid, botid); 
            this.exchanges.put(input, response);
            result &= responseIsValid(response);
        }
        return result;
    }

    /**
     * Response is valid if the checkers return a positive result.
     * 
     * @param response the response to check
     * @return whether or not the response is valud
     */
    private boolean responseIsValid(String response)
    {
        boolean result = true;
        for (Checker checker : this.checkers)
        {
            result &= checker.test(response);
        }
        return result;
    }
}
