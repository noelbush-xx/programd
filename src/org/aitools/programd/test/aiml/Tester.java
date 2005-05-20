package org.aitools.programd.test.aiml;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.aitools.programd.Core;
import org.aitools.programd.multiplexor.Multiplexor;
import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.FileManager;
import org.aitools.programd.util.UserError;

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

    /** The Multiplexor that this Tester will use. */
    private Multiplexor multiplexor;

    /** The logger to use. */
    private Logger logger;

    /** The path to the directory that contains test suites. */
    private String directory;

    /** The timestamp format to use for reports. */
    private static final SimpleDateFormat timestampFormat = new SimpleDateFormat(
            "yyyy-MM-dd-H-mm-ss");

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
        this.suites.clear();
        this.suites = loadTests(this.directory, this.multiplexor, this.logger);
        runTests(botid, suite, runCount);
    }

    private void runTests(String botid, String suite, int runCount)
    {
        if (null == botid)
        {
            this.logger
                    .log(Level.WARNING, "No botid defined for tests; select a bot with /talkto.");
            return;
        }
        if (this.suites.size() == 0)
        {
            return;
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
        report.write(this.directory + File.separator + "reports" + File.separator + "test-report-"
                + timestampFormat.format(new Date()) + ".xml");
        // Good time to request garbage collection.
        System.gc();
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
        suite.run(botId);
        this.failures.addAll(suite.getFailures());
        this.successes.addAll(suite.getSuccesses());
    }

    /**
     * Loads all test suites from a given directory.
     * @param directory the directory from which to load the tests
     * @param multiplexorToUse the Multiplexor to assign to the suites
     * @param logger the logger to use for tracking progress
     * 
     * @param testFilePath
     * @return the map of suite names to suites
     */
    private static HashMap<String, TestSuite> loadTests(String directory, Multiplexor multiplexorToUse, Logger logger)
    {
        HashMap<String, TestSuite> suites = new HashMap<String, TestSuite>();
        String[] fileList;
        try
        {
            fileList = FileManager.glob("*.xml", directory);
        }
        catch (FileNotFoundException e)
        {
            throw new UserError("Could not find files matching \"" + directory
                    + File.separator + "*.xml", e);
        }
        int fileCount = fileList.length;
        if (null != fileList && fileCount > 0)
        {
            for (int index = 0; index < fileCount; index++)
            {
                String path = fileList[index];
                logger.log(Level.INFO, "Loading tests from \"" + path + "\".");
                TestSuite suite = TestSuite.load(path, multiplexorToUse);
                suites.put(suite.getName(), suite);
            }
        }
        else
        {
            logger.log(Level.WARNING, "No test files found in path \"" + directory
                    + "\".");
        }
        return suites;
    }
}
