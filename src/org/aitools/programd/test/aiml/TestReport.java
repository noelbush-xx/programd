/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.test.aiml;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.aitools.util.resource.Filesystem;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * Contains a list of test successes and failures, and can generate a report
 * about them.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class TestReport
{
    /** The test successes. */
    private LinkedList<TestResult> _successes = new LinkedList<TestResult>();

    /** The test failures. */
    private LinkedList<TestResult> _failures = new LinkedList<TestResult>();

    /**
     * Creates a new TestReport with the given reported successes and failures.
     * 
     * @param successes
     * @param failures
     */
    public TestReport(LinkedList<TestResult> successes, LinkedList<TestResult> failures)
    {
        this._successes = successes;
        this._failures = failures;
    }

    /**
     * Prints a summary of the test report to the given logger.
     * 
     * @param logger the logger to which to print the summary
     */
    @SuppressWarnings("boxing")
    public void logSummary(Logger logger)
    {
        int failureCount = this._failures.size();
        if (failureCount == 0)
        {
            logger.info(String.format("All %d tests succeeded.", this._successes.size()));
        }
        else
        {
            logger.warn(String.format("%d out of %d tests failed.", failureCount, (this._successes.size() + failureCount)));
        }
    }

    /**
     * Writes a report to the given filename.
     * 
     * @param path the path to which to write the report
     * 
     * @throws IOException 
     * @throws FileNotFoundException 
     */
    public void write(String path) throws FileNotFoundException, IOException
    {
        Document doc = new Document();
        
        Element root = new Element("TestReport");
        root.addContent(addResults(new Element("Successes"), "Success", this._successes));
        root.addContent(addResults(new Element("Failures"), "Failure", this._failures));
        doc.addContent(root);

        new XMLOutputter(Format.getPrettyFormat()).output(doc, new FileOutputStream(Filesystem.checkOrCreate(path, "test results")));
    }
    
    private Element addResults(Element group, String name, List<TestResult> results)
    {
        for (TestResult result : results)
        {
            Element element = new Element(name);
            element.addContent(new Element("Input").setText(result.getInput()));
            element.addContent(new Element("Response").setText(result.getResponse()));
            group.addContent(element);
        }
        return group;
    }
}
