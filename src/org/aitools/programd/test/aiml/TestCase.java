package org.aitools.programd.test.aiml;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.aitools.programd.Core;
import org.aitools.util.Text;
import org.aitools.util.runtime.DeveloperError;
import org.aitools.util.xml.XHTML;
import org.jdom.Element;

/**
 * A TestCase contains an input and a set of checkers that test the response to that input.
 * 
 * @author Albertas Mickensas
 * @author <a href="noel@aitools.org">Noel Bush</a>
 */
public class TestCase {

  /** The string &quot;{@value} &quot;. */
  public static String TAG_TESTCASE = "TestCase";

  /** The string &quot;{@value} &quot;. */
  public static String TAG_DESCRIPTION = "Description";

  /** The string &quot;{@value} &quot;. */
  public static String TAG_INPUT = "Input";

  /** The name of this test case. */
  protected String _name;

  /** The suite to which this test case belongs. */
  protected TestSuite _suite;

  /** The inputs that this test case should send. */
  protected String _input;

  /** The checker(s) contained in this test case. */
  protected List<Checker> _checkers = new ArrayList<Checker>();

  /** The last response received by this test case. */
  protected String _lastResponse;

  /**
   * A private constructor, for use in persistence.
   */
  @SuppressWarnings("unused")
  private TestCase() {
    // Do nothing.
  }

  /**
   * Creates a new TestCase from the given XML element.
   * 
   * @param element the TestCase element
   * @param encoding the encoding of the document from which this element comes
   * @param index a default index to use for automatically naming this case
   * @param suite the test suite to which this case belongs
   */
  @SuppressWarnings("unchecked")
  public TestCase(Element element, String encoding, int index, TestSuite suite) {
    this._suite = suite;

    if (element.getAttribute("name") != null) {
      try {
        this._name = new String(element.getAttributeValue("name").getBytes(encoding)).intern();
      }
      catch (UnsupportedEncodingException e) {
        throw new DeveloperError(String.format("Platform does not support encoding \"%s\"!", encoding), e);
      }
    }
    else {
      this._name = "case-" + index;
    }

    List<Element> children = element.getChildren();

    int checkersStart = 0;
    // Might be a description here.
    Element child = children.get(0);
    if (child.getName().equals(TAG_DESCRIPTION)) {
      checkersStart = 2;
    }
    else {
      checkersStart = 1;
    }

    try {
      this._input = new String(children.get(checkersStart - 1).getText().getBytes(encoding)).intern();
    }
    catch (UnsupportedEncodingException e) {
      throw new DeveloperError(String.format("Platform does not support encoding \"%s\"!", encoding), e);
    }
    for (Element checker : children.subList(checkersStart, children.size())) {
      this._checkers.add(Checker.create(checker, encoding));
    }

  }

  /**
   * Constructs a basic TestCase with just an input.
   * 
   * @param testInput the input to use
   */
  public TestCase(String testInput) {
    this._name = "testcase-" + System.currentTimeMillis();
    this._input = testInput;
  }

  /**
   * Constructs a basic TestCase with an input and an expected answer (utility constructor).
   * 
   * @param testInput the input to use
   * @param expectedAnswer the answer to expect
   */
  public TestCase(String testInput, String expectedAnswer) {
    this._name = "testcase-" + System.currentTimeMillis();
    this._input = testInput;
    this.addChecker(new AnswerChecker(expectedAnswer));
  }

  /**
   * Adds a given checker.
   * 
   * @param checker the checker to add
   */
  public void addChecker(Checker checker) {
    this._checkers.add(checker);
  }

  /**
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof TestCase)) {
      return false;
    }
    TestCase other = (TestCase) obj;
    return other._name.equals(this._name) && other._input.equals(this._input) && other._checkers.equals(this._checkers)
        && other._lastResponse.equals(this._lastResponse);
  }

  /**
   * Produces a map of checker names to contents that can be used to describe the test case textually.
   * 
   * @return a map of checker names to contents that can be used to describe the test case textually
   */
  public List<String[]> getDescription() {
    List<String[]> result = new ArrayList<String[]>();
    for (Checker checker : this._checkers) {
      result.add(new String[] { checker.getTagName(), checker.getContent() });
    }
    return result;
  }

  /**
   * Returns the expected response, which may or may not be a literal string.
   * 
   * @return the expected response, or a description of it
   */
  public String getExpected() {
    return "Expected" + Text.mergeStringArrays(this.getDescription());
  }

  /**
   * @return the input to be sent by this test case
   */
  public String getInput() {
    return this._input;
  }

  /**
   * @return the last response received by this test case
   */
  public String getLastResponse() {
    return this._lastResponse;
  }

  /**
   * @return the name of this test case
   */
  public String getName() {
    return this._name;
  }

  /**
   * @return the suite to which this case belongs
   */
  public TestSuite getSuite() {
    return this._suite;
  }

  /**
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    return (this._name + this._input + this._checkers.toString() + this._lastResponse).hashCode();
  }

  /**
   * Removes all checkers.
   */
  public void removeCheckers() {
    this._checkers = new ArrayList<Checker>();
  }

  /**
   * Response is valid if at least one of the checkers returns a positive result.
   * 
   * @param response the response to check
   * @return whether or not the response is valud
   */
  private boolean responseIsValid(String response) {
    boolean result = false;
    for (Checker checker : this._checkers) {
      result |= checker.test(response);
    }
    return result;
  }

  /**
   * Runs this test case for the given botid.
   * 
   * @param core the Core to use for testing
   * @param userid the userid to use when testing
   * @param botid the bot for whom to run this test case
   * @return whether the test passed
   */
  public boolean run(Core core, String userid, String botid) {
    this._lastResponse = org.jdom.Text.normalizeString(core.getResponse(this._input, userid, botid));
    return this.responseIsValid(Text.renderAsLines(XHTML.breakLines(this._lastResponse)));
  }
}
