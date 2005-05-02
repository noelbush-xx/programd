/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.test.aiml;

import java.util.HashMap;
import java.util.logging.Logger;

import org.aitools.programd.interfaces.shell.Shell;
import org.aitools.programd.interfaces.shell.ShellCommand;
import org.aitools.programd.logging.LogUtils;
import org.aitools.programd.util.UserError;

/**
 * A TestCommand provides an interface to the AIML testing facility.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class TestCommand extends ShellCommand
{
    /** Shell command. */
    public static final String COMMAND_STRING = "/test";

    /** Argument template. */
    public static final String ARGUMENT_TEMPLATE = "suite [run-count]";

    /** Shell help line. */
    private static final String HELP_LINE = "runs specified test suite on current bot";

    /** The directory where test suites should be found. */
    private String testSuiteDirectory;

    /** The logger to use. */
    private Logger logger;

    /**
     * Creates a new TestCommand.
     * 
     * @param parameters the parameters for configuring the Tester
     */
    public TestCommand(HashMap<String, String> parameters)
    {
        super(COMMAND_STRING, ARGUMENT_TEMPLATE, HELP_LINE);
        this.testSuiteDirectory = parameters.get("test-suite-directory");
        if (this.testSuiteDirectory == null)
        {
            throw new UserError("No test suite directory specified for Tester!",
                    new IllegalArgumentException());
        }
        String logPattern = parameters.get("log-pattern");
        if (logPattern == null)
        {
            throw new UserError("No log pattern specified for Tester!",
                    new IllegalArgumentException());
        }
        String timestampFormat = parameters.get("log-timestamp-format");
        if (timestampFormat == null)
        {
            throw new UserError("No timestamp format specified for Tester!",
                    new IllegalArgumentException());
        }
        this.logger = LogUtils.setupLogger("programd.testing", logPattern, timestampFormat);
    }

    /**
     * @see org.aitools.programd.interfaces.shell.ShellCommand#handles(java.lang.String)
     */
    public boolean handles(String commandLine)
    {
        return commandLine.toLowerCase().startsWith(COMMAND_STRING);
    }

    /**
     * Runs the specified test suite on the current bot. Optionally, a number of
     * test runs can be specified.
     * 
     * @see org.aitools.programd.interfaces.shell.ShellCommand#handle(java.lang.String,
     *      org.aitools.programd.interfaces.shell.Shell)
     */
    public void handle(String commandLine, Shell shell)
    {
        int runCount = 1;
        int space1 = commandLine.indexOf(' ');
        String suite = null;
        if (space1 != -1)
        {
            int space2 = commandLine.indexOf(' ', space1 + 1);
            if (space2 != -1)
            {
                suite = commandLine.substring(space1 + 1, space2);
                runCount = Integer.parseInt(commandLine.substring(space2 + 1));
            }
            else
            {
                try
                {
                    runCount = Integer.parseInt(commandLine.substring(space1 + 1));
                }
                catch (NumberFormatException e)
                {
                    shell.showError("Invalid test run count. Will run 1 time.");
                    runCount = 1;
                    suite = commandLine.substring(space1 + 1);
                }
            }
        }
        new Tester(shell.getCore(), this.logger, this.testSuiteDirectory).run(shell
                .getCurrentBotID(), suite, runCount);
    }

}
