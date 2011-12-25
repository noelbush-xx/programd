package org.aitools.programd.test.aiml;

/**
 * Represents a test result (a success or failure).
 * 
 * @author Albertas Mickensas
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class TestResult {

  /** The test case that failed. */
  protected TestCase _testCase;

  /** The input that produced this result. */
  protected String _input;

  /** The response that constitutes this result. */
  protected String _response;

  /**
   * A protected constructor, for use in persistence.
   */
  protected TestResult() {
    // Do nothing.
  }

  /**
   * Creates an object that represents the test result, with no suite name or test case name attached.
   * 
   * @param in the input
   * @param out the response
   */
  public TestResult(String in, String out) {
    this._input = in;
    this._response = out;
  }

  /**
   * Creates an object that represents the test result.
   * 
   * @param testCase the test case that succeeded or failed
   */
  public TestResult(TestCase testCase) {
    this._testCase = testCase;
    this._input = testCase.getInput();
    this._response = testCase.getLastResponse();
  }

  /**
   * Returns the expected response; note that this is not necessarily a literal string, but may be a description of the
   * expected response.
   * 
   * @return the expected response
   */
  public String getExpected() {
    return this._testCase.getExpected();
  }

  /**
   * @return the input
   */
  public String getInput() {
    return this._input;
  }

  /**
   * @return the response
   */
  public String getResponse() {
    return this._response;
  }

  /**
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Suite:\t" + this._testCase.getSuite().getName() + "\ttestcase:\t " + this._testCase.getName()
        + "\tinput:\t\"" + this._input + "\" response: \"" + this._response + "\"";
  }
}
