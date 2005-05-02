package org.aitools.programd.test.aiml;

import java.util.ArrayList;
import java.util.Arrays;

import org.aitools.programd.multiplexor.Multiplexor;

/**
 * A TestCase contains an input and a set of checkers
 * that test the response to that input.
 * 
 * @author Albertas Mickensas
 */
public class TestCase
{
    /** The string &quot;{@value}&quot;. */
    public static String TAG_TESTCASE = "TestCase";

    /** The string &quot;{@value}&quot;. */
    public static String TAG_INPUT = "Input";

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

    /** The input that this test case should send. */ 
    private String input;

    /** The last response received by this test case. */
    private String lastResponse;

    /** The checkers contained in this test case. */
    ArrayList<Checker> checkers = new ArrayList<Checker>();
    
    /**
     * Creates a new TestCase with the given input.
     * 
     * @param inputToUse the input that this test case should send
     */
    public TestCase(String inputToUse)
    {
        this.input = inputToUse;
    }

    /**
     * Adds an ExpectedAnswer checker to this test case.
     * 
     * @param answer the expected answer to add
     */
    public void addExpectedAnswer(String answer)
    {
        this.checkers.add(new AnswerChecker(answer));
    }

    /**
     * Adds an ExpectedKeywords checker to this test case.
     * 
     * @param keywords the keywords to add
     */
    public void addExpectedKeywords(String[] keywords)
    {
        this.checkers.add(new ExpectedKeywordChecker(Arrays.asList(keywords)));
    }

    /**
     * Adds an AlertKeywords checker to this test case.
     * 
     * @param keywords the keywords to add
     */
    public void addAlertKeywords(String[] keywords)
    {
        this.checkers.add(new AlertKeywordChecker(Arrays.asList(keywords)));
    }

    /**
     * Adds an ExpectedLength checker to this test case.
     * 
     * @param length the expected length to add
     */
    public void addExpectedLength(int length)
    {
        this.checkers.add(new LengthChecker(length));
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
     * Response is valid if none of the checkers return negative.
     * 
     * @param response the response to check
     * @return whether or not the response is valud
     */
    private boolean responseIsValid(String response)
    {
        boolean result = false;
        if (null != this.checkers && this.checkers.size() > 0)
        {
            for (Checker checker : this.checkers)
            {
                result = result || checker.test(response);
            }
        }
        else
        {
            result = true;
        }
        return result;
    }
}
