/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.test.aiml;

import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedList;

import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.FileManager;
import org.aitools.programd.util.XMLKit;
import org.apache.log4j.Logger;

/**
 * Contains a list of test successes and failures, and can generate a report
 * about them.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class TestReport
{
    /** The test successes. */
    private LinkedList<TestResult> successes = new LinkedList<TestResult>();

    /** The test failures. */
    private LinkedList<TestResult> failures = new LinkedList<TestResult>();

    /**
     * Creates a new TestReport with the given reported successes and failures.
     * 
     * @param reportedSuccesses the successes
     * @param reportedFailures the failures
     */
    public TestReport(LinkedList<TestResult> reportedSuccesses,
            LinkedList<TestResult> reportedFailures)
    {
        this.successes = reportedSuccesses;
        this.failures = reportedFailures;
    }

    /**
     * Prints a summary of the test report to the given logger.
     * 
     * @param logger the logger to which to print the summary
     */
    public void logSummary(Logger logger)
    {
        logger.info(this.successes.size() + "/" + (this.successes.size() + this.failures.size())
                + " tests succeeded.");
    }

    /**
     * Writes a report to the given filename.
     * 
     * @param path the path to which to write the report
     */
    public void write(String path)
    {
        try
        {
            FileWriter writer = new FileWriter(FileManager.checkOrCreate(path, "test report"));
            writer.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n");
            writer.write("<TestReport>\n");
            writer.write("  <Successes>\n");
            writer.flush();
            for (TestResult success : this.successes)
            {
                writer.write("    <Success>\n");
                writer.write("      <Input>");
                writer.write(XMLKit.escapeXMLChars(success.getInput()));
                writer.write("</Input>\n");
                writer.write("      <Response>");
                writer.write(XMLKit.escapeXMLChars(success.getResponse()));
                writer.write("</Response>\n");
                writer.write("    </Success>\n");
                writer.flush();
            }
            writer.write("  </Successes>\n");
            writer.write("  <Failures>\n");
            for (TestResult failure : this.failures)
            {
                writer.write("    <Failure>\n");
                writer.write("      <Input>");
                writer.write(XMLKit.escapeXMLChars(failure.getInput()));
                writer.write("</Input>\n");
                writer.write("      <Response>");
                writer.write(XMLKit.escapeXMLChars(failure.getResponse()));
                writer.write("</Response>\n");
                writer.write("    </Failure>\n");
                writer.flush();
            }
            writer.write("  </Failures>\n");
            writer.write("</TestReport>\n");
            writer.flush();
            writer.close();
        }
        catch (IOException e)
        {
            throw new DeveloperError("Error while trying to write test report.", e);
        }
    }
}
