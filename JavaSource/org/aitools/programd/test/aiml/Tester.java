package org.aitools.programd.test.aiml;

import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.aitools.programd.Core;
import org.aitools.programd.multiplexor.Multiplexor;
import org.aitools.programd.util.FileManager;
import org.aitools.programd.util.URLTools;
import org.apache.log4j.Logger;

/**
 * A Tester loads one or more test suites and runs them, logging output.
 * 
 * @author Albertas Mickensas
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class Tester
{
    /** The test suites. */
    private HashMap<String, TestSuite> suites = new HashMap<String, TestSuite>();

    /** The test successes. */
    private LinkedList<TestResult> successes = new LinkedList<TestResult>();

    /** The test failures. */
    private LinkedList<TestResult> failures = new LinkedList<TestResult>();
    
    /** The Core that this Tester will use. */
    private Core core;
    
    /** The Multiplexor that this Tester will use. */
    private Multiplexor multiplexor;

    /** The logger to use. */
    private Logger logger;

    /** The pathspec for the test suites. */
    private List<URL> suiteURLs;

    /** The path to the test report directory. */
    private URL testReportDirectory;

    /** The timestamp format to use for reports. */
    private static final SimpleDateFormat timestampFormat = new SimpleDateFormat(
            "yyyy-MM-dd-H-mm-ss");
    
    /** The location of the test cases schema. */
    private static final String TEST_CASES_SCHEMA_LOCATION = "resources/schema/test-cases.xsd";

    /**
     * Creates a new Tester which will use the given Core to find out its
     * configuration and run tests.
     * 
     * @param coreToUse the Core to use for finding plugin configuration, active
     *            multiplexor, etc.
     * @param testLogger the logger to which to send output
     * @param suitePaths the test suites
     * @param testReports the directory in which to store test reports
     */
    public Tester(Core coreToUse, Logger testLogger, List<URL> suitePaths, URL testReports)
    {
        this.core = coreToUse;
        this.multiplexor = this.core.getMultiplexor();
        this.logger = testLogger;
        this.suiteURLs = suitePaths;
        try
        {
            this.testReportDirectory = FileManager.checkOrCreateDirectory(testReports.getFile(),
                    "test report directory").getCanonicalFile().toURL();
        }
        catch (IOException e)
        {
            assert false : "A directory that FileManager found cannot be found anymore.";
        }
    }

    /**
     * Runs the tests in the given suite on the given botid, a given number of
     * times.
     * 
     * @param suite the suite to run
     * @param botid the botid on whom to run the tests
     * @param runCount the number of times to run the tests
     * @return the path where the test report was written
     */
    public String run(String botid, String suite, int runCount)
    {
        this.suites.clear();
        this.suites = loadTests(this.suiteURLs, URLTools.contextualize(FileManager.getRootPath(), TEST_CASES_SCHEMA_LOCATION), this.multiplexor, this.logger);
        if (null == botid)
        {
            this.logger.warn("No botid defined for tests.");
            return "";
        }
        if (this.suites.size() == 0)
        {
            this.logger.warn("No suites defined.");
            return "";
        }
        this.successes.clear();
        this.failures.clear();
        if (suite == null)
        {
            runAllSuites(botid, runCount);
        }
        else
        {
            runOneSuite(botid, suite, runCount);
        }
        TestReport report = new TestReport(this.successes, this.failures);
        report.logSummary(this.logger);
        String reportPath = URLTools.contextualize(this.testReportDirectory,
                "test-report-" + timestampFormat.format(new Date()) + ".xml").getFile();
        report.write(reportPath);
        // Good time to request garbage collection.
        System.gc();
        return reportPath;
    }

    private void runOneSuite(String botid, String suiteName, int runCount)
    {
        TestSuite suite = this.suites.get(suiteName);
        if (suite == null)
        {
            this.logger.warn("No suite \"" + suiteName + "\" could be found.");
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
        suite.run(botId);
        this.failures.addAll(suite.getFailures());
        this.successes.addAll(suite.getSuccesses());
    }

    /**
     * Loads all test suites from a given pathspec (may use wildcards).
     * 
     * @param suiteList the list of suites
     * @param schema the URL to the copy of the schema for test cases
     * @param multiplexorToUse the Multiplexor to assign to the suites
     * @param logger the logger to use for tracking progress
     * 
     * @return the map of suite names to suites
     */
    private static HashMap<String, TestSuite> loadTests(List<URL> suiteList, URL schema, Multiplexor multiplexorToUse, Logger logger)
    {
        HashMap<String, TestSuite> suites = new HashMap<String, TestSuite>();
        for (URL path : suiteList)
        {
            logger.info("Loading tests from \"" + path + "\".");
            TestSuite suite = TestSuite.load(path, schema, multiplexorToUse, logger);
            suites.put(suite.getName(), suite);
        }
        return suites;
    }
}
