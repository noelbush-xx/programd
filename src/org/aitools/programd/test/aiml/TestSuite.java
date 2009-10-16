package org.aitools.programd.test.aiml;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.aitools.programd.Core;
import org.aitools.util.runtime.DeveloperError;
import org.aitools.util.xml.Characters;
import org.aitools.util.xml.JDOM;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 * A TestSuite comprises a set of TestCases.
 * 
 * @author Albertas Mickensas
 */
public class TestSuite implements Iterable<TestCase>
{
    /** The test suite namespace URI. */
    public static final String TESTSUITE_NAMESPACE_URI = "http://aitools.org/xaiml/test-suite";

    private static final Namespace TESTSUITE_NAMESPACE = Namespace.getNamespace("http://aitools.org/xaiml/test-suite");

    /** The test cases in this suite. */
    ArrayList<TestCase> testCases = new ArrayList<TestCase>();

    /** The name of the test suite. */
    private String _name;

    /** The clearInput to use for this test suite. */
    private String _clearInput;

    /** The Core in use. */
    private Core _core;

    /** The Logger. */
    private Logger _logger;

    /** The test auccesses accumulated by this suite. */
    private LinkedList<TestResult> successes = new LinkedList<TestResult>();

    /** The test failures accumulated by this suite. */
    private LinkedList<TestResult> failures = new LinkedList<TestResult>();

    /** The userid to use for the tester. */
    private static final String TESTER_ID = "ProgramD-Tester";

    /**
     * Creates a new TestSuite.
     * 
     * @param name the name to give the test suite
     * @param clearInput the clearInput for the test suite
     * @param core the core to use
     * @param logger
     */
    public TestSuite(String name, String clearInput, Core core, Logger logger)
    {
        this._name = name;
        this._clearInput = clearInput;
        this._core = core;
        this._logger = logger;
    }

    /**
     * Creates a new TestSuite (with no clearInput).
     * 
     * @param name the name to give the test suite
     * @param core the multiplexor to use
     * @param logger
     */
    public TestSuite(String name, Core core, Logger logger)
    {
        this._name = name;
        this._core = core;
        this._logger = logger;
    }

    /**
     * Creates a new TestSuite (with no clearInput or Multiplexor(!)).
     * 
     * @param name the name to give the test suite
     * @param logger
     */
    protected TestSuite(String name, Logger logger)
    {
        this._name = name;
        this._logger = logger;
    }

    /**
     * @return an iterator over this suite's test cases
     * @see java.lang.Iterable#iterator()
     */
    public Iterator<TestCase> iterator()
    {
        return this.testCases.iterator();
    }

    /**
     * Adds a test case to this suite.
     * 
     * @param testCase the test case to add.
     */
    public void addTestCase(TestCase testCase)
    {
        this.testCases.add(testCase);
    }

    /**
     * @return the test cases in this suite
     */
    public ArrayList<TestCase> getTestCases()
    {
        return this.testCases;
    }

    /**
     * @return the name of this suite
     */
    public String getName()
    {
        return this._name;
    }

    /**
     * Runs the test cases contained in this suite.
     * 
     * @param botid the botid for whom to run the test cases
     * @return whether the test cases all passed successfully
     */
    public boolean run(String botid)
    {
        if (this._clearInput != null && this._clearInput.length() > 0)
        {
            this._core.getResponse(this._clearInput, TESTER_ID, botid);
        }

        this.failures.clear();
        boolean suiteSuccessful = true;
        for (TestCase testCase : this.testCases)
        {
            boolean caseSuccessful = testCase.run(this._core, TESTER_ID, botid);
            String testcaseName = testCase.getName();
            if (!caseSuccessful)
            {
                this._logger.warn(String.format("Test case \"%s\" failed with response \"%s\".", testcaseName, Characters
                        .removeMarkup(testCase.getLastResponse())));
                registerFailure(this._name, testCase.getName(), testCase.getInput(), testCase.getLastResponse());
            }
            else
            {
                this._logger.info(String.format("Test case \"%s\" succeeded.", testcaseName));
                registerSuccess(this._name, testCase.getName(), testCase.getInput(), testCase.getLastResponse());
            }
            suiteSuccessful &= caseSuccessful;
        }
        return suiteSuccessful;
    }

    protected void registerSuccess(String suite, String tcase, String pattern, String response)
    {
        this.successes.add(new TestResult(suite, tcase, pattern, response));
    }

    protected void registerFailure(String suite, String tcase, String pattern, String response)
    {
        this.failures.add(new TestResult(suite, tcase, pattern, response));
    }

    /**
     * @return the successes accumulated by this suite
     */
    public LinkedList<TestResult> getSuccesses()
    {
        return this.successes;
    }

    /**
     * @return the failures accumulated by this suite
     */
    public LinkedList<TestResult> getFailures()
    {
        return this.failures;
    }

    /**
     * Loads a test suite from the given path.
     * 
     * @param path the path from which to load the test suite
     * @param logger
     * @return the loaded test suite
     */
    public static TestSuite load(URL path, Logger logger)
    {
        return load(path, null, logger);
    }

    /**
     * Loads a test suite from the given path.
     * 
     * @param path the path from which to load the test suite
     * @param core the core to use
     * @param logger
     * @return the loaded test suite
     */
    @SuppressWarnings("unchecked")
    public static TestSuite load(URL path, Core core, Logger logger)
    {
        Document document = JDOM.getDocument(path, logger);
        String encoding;
        try
        {
            encoding = Characters.getDeclaredXMLEncoding(path);
        }
        catch (IOException e)
        {
            throw new DeveloperError("Could not get encoding of test suite file.", e);
        }
        Element testSuiteElement = document.getRootElement();
        TestSuite suite = new TestSuite(testSuiteElement.getAttributeValue("name"), testSuiteElement
                .getAttributeValue("clearInput"), core, logger);
        int index = 0;
        for (Element testCaseElement : (List<Element>) testSuiteElement.getChildren(TestCase.TAG_TESTCASE,
                TESTSUITE_NAMESPACE))
        {
            TestCase testCase = new TestCase(testCaseElement, encoding, index++);
            suite.addTestCase(testCase);
        }
        return suite;
    }
}
