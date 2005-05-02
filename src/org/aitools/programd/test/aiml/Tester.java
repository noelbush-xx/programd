package org.aitools.programd.test.aiml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.aitools.programd.Core;
import org.aitools.programd.multiplexor.Multiplexor;
import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.FileManager;
import org.aitools.programd.util.URITools;
import org.aitools.programd.util.UserError;
import org.aitools.programd.util.XMLKit;

/**
 * A Tester loads one or more test suites and runs them, logging output.
 * 
 * @author Albertas Mickensas
 */
public class Tester
{
    /** The test cases namespace URI. */
    private static final String TESTCASE_NAMESPACE_URI = "http://aitools.org/programd/4.5/test-cases";

    /** The test cases schema location (local). */
    private static final String SCHEMA_LOCATION = "resources/schema/test-cases.xsd";

    /** The test suites. */
    private HashMap<String, TestSuite> suites = new HashMap<String, TestSuite>();

    /** The test failures. */
    private LinkedList<TestFailure> failures = new LinkedList<TestFailure>();

    /** The Multiplexor that this Tester will use. */
    private Multiplexor multiplexor;

    /** The logger to use. */
    private Logger logger;

    /** The path to the directory that contains test suites. */
    private String directory;

    /**
     * Creates a new Tester which will use the given Core to find out its
     * configuration and run tests.
     * 
     * @param core the Core to use for finding plugin configuration, active
     *            multiplexor, etc.
     * @param testLogger the logger to which to send output
     * @param testSuiteDirectory the name of the directory that contains test
     *            suites
     */
    public Tester(Core core, Logger testLogger, String testSuiteDirectory)
    {
        this.multiplexor = core.getMultiplexor();
        this.logger = testLogger;
        try
        {
            this.directory = FileManager.getExistingDirectory(testSuiteDirectory)
                    .getCanonicalPath();
        }
        catch (IOException e)
        {
            throw new DeveloperError("A directory that FileManager found cannot be found anymore.",
                    e);
        }
    }

    /**
     * Runs the tests in the given suite on the given botid, a given number of
     * times.
     * 
     * @param suite the suite to run
     * @param botid the botid on whom to run the tests
     * @param runCount the number of times to run the tests
     */
    public void run(String botid, String suite, int runCount)
    {
        loadTests();
        runTests(botid, suite, runCount);
    }

    private void runTests(String botid, String suite, int runCount)
    {
        if (null == botid)
        {
            this.logger.log(Level.WARNING, "No botid defined for tests; select a bot with /talkto.");
            return;
        }
        if (this.suites.size() == 0)
        {
            return;
        }
        this.failures.clear();
        if (suite == null)
        {
            runAllSuites(botid, runCount);
        }
        else
        {
            runOneSuite(botid, suite, runCount);
        }
        if (this.failures.size() > 0)
        {
            this.logger.log(Level.WARNING, "There were failures while running tests.");
            for (TestFailure failure : this.failures)
            {
                this.logger.log(Level.WARNING, failure.toString());
            }
        }
        else
        {
            this.logger.log(Level.INFO, "Tests passed.");
        }

    }

    private void runOneSuite(String botid, String suiteName, int runCount)
    {
        TestSuite suite = this.suites.get(suiteName);
        if (suite == null)
        {
            this.logger.log(Level.WARNING, "No suite \"" + suiteName + "\" could be found.");
            return;
        }
        while (runCount-- > 0)
        {
            runSuite(botid, suite);
        }
    }

    private void runAllSuites(String botid, int runCount)
    {
        while (runCount-- > 0)
        {
            for (TestSuite suite : this.suites.values())
            {
                runSuite(botid, suite);
            }
        }
    }

    private void runSuite(String botId, TestSuite suite)
    {
        if (!suite.run(botId))
        {
            this.failures.addAll(suite.getFailures());
        }
    }

    /**
     * Loads all test suites from the tests directory.
     * 
     * @param testFilePath
     */
    private void loadTests()
    {
        this.suites.clear();
        String[] fileList;
        try
        {
            fileList = FileManager.glob("*.xml", this.directory);
        }
        catch (FileNotFoundException e)
        {
            throw new UserError("Could not find files matching \"" + this.directory
                    + File.separator + "*.xml", e);
        }
        int fileCount = fileList.length;
        if (null != fileList && fileCount > 0)
        {
            DocumentBuilder builder = XMLKit.getDocumentBuilder(SCHEMA_LOCATION, "test cases");
            for (int index = 0; index < fileCount; index++)
            {
                String path = fileList[index];
                this.logger.log(Level.INFO, "Loading tests from \"" + path + "\".");
                try
                {
                    loadTestSuite(builder.parse(URITools.createValidURL(path).toExternalForm()));
                }
                catch (IOException e)
                {
                    throw new DeveloperError("IO exception trying to parse test suite file.", e);
                }
                catch (SAXException e)
                {
                    throw new DeveloperError("SAX exception trying to parse test suite file.", e);
                }
            }
        }
        else
        {
            this.logger.log(Level.WARNING, "No test files found in path \"" + this.directory
                    + "\".");
        }
    }

    /**
     * Creates a TestSuite object from an XML document, presumed to be a test
     * suite
     * 
     * @param doc the document from which to create the test suite
     */
    private void loadTestSuite(Document doc)
    {
        // We assume that the document has been validated!
        Element testSuiteElement = doc.getDocumentElement();
        TestSuite suite = new TestSuite(testSuiteElement.getAttribute("name"), testSuiteElement
                .getAttribute("clearInput"), this.multiplexor, this.logger);
        this.suites.put(testSuiteElement.getAttribute("name"), suite);

        NodeList testCases = doc.getElementsByTagNameNS(TESTCASE_NAMESPACE_URI,
                TestCase.TAG_TESTCASE);
        int testCaseCount = testCases.getLength();
        for (int index = 0; index < testCaseCount; index++)
        {
            Element testCaseElement = (Element) testCases.item(index);
            Element input = (Element) testCaseElement.getElementsByTagName(TestCase.TAG_INPUT)
                    .item(0);
            TestCase testCase = new TestCase(input.getTextContent());
            suite.addTestCase(testCase);

            NodeList children = testCaseElement.getChildNodes();
            int childCount = children.getLength();

            // Start at 1 (skip the pattern).
            for (int checkerIndex = 1; checkerIndex < childCount; checkerIndex++)
            {
                Node node = children.item(checkerIndex);
                if (node instanceof Element)
                {
                    Element checker = (Element)node;
                    String checkerName = checker.getTagName();
                    if (checkerName.equals(TestCase.TAG_ALERT_KEYWORDS))
                    {
                        testCase.addAlertKeywords(checker.getTextContent().split(","));
                    }
                    else if (checkerName.equals(TestCase.TAG_EXPECTED_ANSWER))
                    {
                        testCase.addExpectedAnswer(checker.getTextContent());
                    }
                    else if (checkerName.equals(TestCase.TAG_EXPECTED_KEYWORDS))
                    {
                        testCase.addExpectedKeywords(checker.getTextContent().split(","));
                    }
                    else if (checkerName.equals(TestCase.TAG_EXPECTED_LENGTH))
                    {
                        try
                        {
                            testCase.addExpectedLength(Integer.parseInt(checker.getTextContent()));
                        }
                        catch (NumberFormatException e)
                        {
                            throw new DeveloperError("A non-integer slipped by: schema must be bad!", e);
                        }
                    }
                }
            }
        }
    }
}
