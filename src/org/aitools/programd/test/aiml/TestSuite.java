package org.aitools.programd.test.aiml;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.aitools.programd.multiplexor.Multiplexor;

/**
 * A TestSuite comprises a set of TestCases.
 * 
 * @author Albertas Mickensas
 * @since 4.5
 */
public class TestSuite
{
    /** The test cases in this suite. */
    ArrayList<TestCase> testCases = new ArrayList<TestCase>();

    /** The name of the test suite. */
    private String name;

    /** The clearInput to use for this test suite. */
    private String clearInput;

    /** The Multiplexor to use. */
    private Multiplexor multiplexor;

    /** The Logger to use. */
    private Logger logger;

    /** The test failures accumulated by this suite. */
    private LinkedList<TestFailure> failures = new LinkedList<TestFailure>();

    /** The userid to use for the tester. */
    private static final String TESTER_ID = "ProgramD-Tester";

    /**
     * Creates a new TestSuite.
     * 
     * @param nameToUse the name to give the test suite
     * @param clearInputToUse the clearInput for the test suite
     * @param multiplexorToUse the multiplexor to use
     * @param loggerToUse the logger to use
     */
    public TestSuite(String nameToUse, String clearInputToUse, Multiplexor multiplexorToUse,
            Logger loggerToUse)
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
     * @param loggerToUse the logger to use
     */
    public TestSuite(String nameToUse, Multiplexor multiplexorToUse,
            Logger loggerToUse)
    {
        this.name = nameToUse;
        this.multiplexor = multiplexorToUse;
        this.logger = loggerToUse;
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
    public ArrayList getTestCases()
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
        this.logger.log(Level.INFO, "Running TestSuite \"" + this.name + "\".");
        for (TestCase testCase : this.testCases)
        {
            boolean caseSuccessful = testCase.run(this.multiplexor, TESTER_ID, botid);
            if (!caseSuccessful)
            {
                registerFailure(this.name, testCase.getName(), testCase.getInput(), testCase
                        .getLastResponse());
            }
            suiteSuccessful &= caseSuccessful;
        }
        return suiteSuccessful;
    }

    private void registerFailure(String suite, String tcase, String pattern, String response)
    {
        this.failures.add(new TestFailure(suite, tcase, pattern, response));
    }

    /**
     * @return the failures accumulated by this suite
     */
    public LinkedList<TestFailure> getFailures()
    {
        return this.failures;
    }
}
