package org.aitools.programd.test.aiml;

/**
 * Represents a test failure.
 * 
 * @author Albertas Mickensas
 * @since 4.5
 */
public class TestFailure
{
    /** The name of the suite that failed. */
    private String suite;

    /** The name of the test case that failed. */
    private String testCase;

    /** The input that resulted in the failure. */
    private String input;

    /** The response that constituted a failure. */
    private String response;

    /**
     * Creates an object that represents the test failure.
     * 
     * @param suiteName the name of the suite
     * @param testCaseName the name of the test case
     * @param failedInput the input that failed
     * @param failingResponse the response that constituted a failure
     */
    public TestFailure(String suiteName, String testCaseName, String failedInput,
            String failingResponse)
    {
        this.suite = suiteName;
        this.testCase = testCaseName;
        this.input = failedInput;
        this.response = failingResponse;
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
