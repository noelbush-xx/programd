package org.aitools.programd.test.aiml;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;

import javax.xml.parsers.DocumentBuilder;

import org.aitools.programd.multiplexor.Multiplexor;
import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.UserError;
import org.aitools.programd.util.XMLKit;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * A TestSuite comprises a set of TestCases.
 * 
 * @author Albertas Mickensas
 * @since 4.5
 */
public class TestSuite implements Iterable<TestCase>
{
    /** The test cases namespace URI. */
    public static final String TESTCASE_NAMESPACE_URI = "http://aitools.org/programd/4.6/test-cases";

    /** The test cases in this suite. */
    ArrayList<TestCase> testCases = new ArrayList<TestCase>();

    /** The name of the test suite. */
    private String name;

    /** The clearInput to use for this test suite. */
    private String clearInput;

    /** The Multiplexor to use. */
    private Multiplexor multiplexor;
    
    /** The Logger. */
    private Logger logger;

    /** The test auccesses accumulated by this suite. */
    private LinkedList<TestResult> successes = new LinkedList<TestResult>();

    /** The test failures accumulated by this suite. */
    private LinkedList<TestResult> failures = new LinkedList<TestResult>();

    /** The userid to use for the tester. */
    private static final String TESTER_ID = "ProgramD-Tester";

    /**
     * Creates a new TestSuite.
     * 
     * @param nameToUse the name to give the test suite
     * @param clearInputToUse the clearInput for the test suite
     * @param multiplexorToUse the multiplexor to use
     */
    public TestSuite(String nameToUse, String clearInputToUse, Multiplexor multiplexorToUse, Logger loggerToUse)
    {
        this.name = nameToUse;
        this.clearInput = clearInputToUse;
        this.multiplexor = multiplexorToUse;
        this.logger = loggerToUse;
    }

    /**
     * Creates a new TestSuite (with no clearInput).
     * 
     * @param nameToUse the name to give the test suite
     * @param multiplexorToUse the multiplexor to use
     */
    public TestSuite(String nameToUse, Multiplexor multiplexorToUse, Logger loggerToUse)
    {
        this.name = nameToUse;
        this.multiplexor = multiplexorToUse;
        this.logger = loggerToUse;
    }

    /**
     * Creates a new TestSuite (with no clearInput or Multiplexor(!)).
     * 
     * @param nameToUse the name to give the test suite
     */
    @SuppressWarnings("unused")
    private TestSuite(String nameToUse, Logger loggerToUse)
    {
        this.name = nameToUse;
        this.logger = loggerToUse;
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
        return this.name;
    }

    /**
     * Runs the test cases contained in this suite.
     * 
     * @param botid the botid for whom to run the test cases
     * @return whether the test cases all passed successfully
     */
    public boolean run(String botid)
    {
        if (this.clearInput != null)
        {
            this.multiplexor.getResponse(this.clearInput, TESTER_ID, botid);
        }

        this.failures.clear();
        boolean suiteSuccessful = true;
        for (TestCase testCase : this.testCases)
        {
            boolean caseSuccessful = testCase.run(this.multiplexor, TESTER_ID, botid);
            String testcaseName = testCase.getName();
            if (!caseSuccessful)
            {
                this.logger.warn("Test case \"" + testcaseName + "\" failed with response \"" +
                        XMLKit.removeMarkup(testCase.getLastResponse()) + "\".");
                registerFailure(this.name, testCase.getName(), testCase.getInput(), testCase
                        .getLastResponse());
            }
            else
            {
                this.logger.info("Test case " + testcaseName + " succeeded.");
                registerSuccess(this.name, testCase.getName(), testCase.getInput(), testCase
                        .getLastResponse());
            }
            suiteSuccessful &= caseSuccessful;
        }
        return suiteSuccessful;
    }

    private void registerSuccess(String suite, String tcase, String pattern, String response)
    {
        this.successes.add(new TestResult(suite, tcase, pattern, response));
    }

    private void registerFailure(String suite, String tcase, String pattern, String response)
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
     * @param schema the URL to the copy of the schema for test cases
     * @return the loaded test suite
     */
    public static TestSuite load(URL path, URL schema, Logger logger)
    {
        return load(path, schema, null, logger);
    }

    /**
     * Loads a test suite from the given path.
     * 
     * @param path the path from which to load the test suite
     * @param schema the URL to the copy of the schema for test cases
     * @param multiplexor the multiplexor to use
     * @return the loaded test suite
     */
    public static TestSuite load(URL path, URL schema, Multiplexor multiplexor, Logger logger)
    {
        DocumentBuilder builder = XMLKit.getDocumentBuilder(schema, "test cases");
        Document doc;
        try
        {
            doc = builder.parse(path.toString());
        }
        catch (IOException e)
        {
            throw new DeveloperError("IO exception trying to parse test suite file.", e);
        }
        catch (SAXException e)
        {
            throw new UserError("SAX exception trying to parse test suite file: "
                    + e.getMessage(), e);
        }
        String encoding = doc.getXmlEncoding();
        Element testSuiteElement = doc.getDocumentElement();
        TestSuite suite = new TestSuite(testSuiteElement.getAttribute("name"), testSuiteElement
                .getAttribute("clearInput"), multiplexor, logger);
    
        NodeList testCases = doc.getElementsByTagNameNS(TESTCASE_NAMESPACE_URI,
                TestCase.TAG_TESTCASE);
        int testCaseCount = testCases.getLength();
        for (int index = 0; index < testCaseCount; index++)
        {
            Element testCaseElement = (Element) testCases.item(index);
            TestCase testCase = new TestCase(testCaseElement, encoding, index);
            suite.addTestCase(testCase);
        }
        return suite;
    }
}
