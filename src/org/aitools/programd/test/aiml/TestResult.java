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
    private String suite;

    /** The name of the test case that failed. */
    private String testCase;

    /** The input that produced this result. */
    private String input;

    /** The response that constitutes this result. */
    private String response;

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
    public String toString()
    {
        return "Suite:\t" + this.suite + "\ttestcase:\t " + this.testCase + "\tinput:\t\""
                + this.input + "\" response: \"" + this.response + "\"";
    }
}
