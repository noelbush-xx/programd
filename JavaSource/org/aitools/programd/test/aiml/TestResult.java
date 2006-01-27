package org.aitools.programd.test.aiml;

/**
 * Represents a test result (a success or failure).
 * 
 * @author Albertas Mickensas
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.5
 */
public class TestResult
{
    /** The name of the suite that failed. */
    protected String suite;

    /** The name of the test case that failed. */
    protected String testCase;

    /** The input that produced this result. */
    protected String input;

    /** The response that constitutes this result. */
    protected String response;

    /**
     * Creates an object that represents the test result.
     * 
     * @param suiteName the name of the suite
     * @param testCaseName the name of the test case
     * @param in the input
     * @param out the response
     */
    public TestResult(String suiteName, String testCaseName, String in,
            String out)
    {
        this.suite = suiteName;
        this.testCase = testCaseName;
        this.input = in;
        this.response = out;
    }
    
    /**
     * Creates an object that represents the test result,
     * with no suite name or test case name attached.
     * 
     * @param in the input
     * @param out the response
     */
    public TestResult(String in, String out)
    {
        this.input = in;
        this.response = out;
    }

    /**
     * A protected constructor, for use in persistence.
     */
    protected TestResult()
    {
        // Do nothing.
    }

    /**
     * @return the input
     */
    public String getInput()
    {
        return this.input;
    }

    /**
     * @return the response
     */
    public String getResponse()
    {
        return this.response;
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return "Suite:\t" + this.suite + "\ttestcase:\t " + this.testCase + "\tinput:\t\""
                + this.input + "\" response: \"" + this.response + "\"";
    }
}
