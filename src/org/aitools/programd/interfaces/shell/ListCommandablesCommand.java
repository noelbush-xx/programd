/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.interfaces.shell;

import org.aitools.programd.util.ManagedProcess;

/**
 * Prints a list of the available ShellCommandables to the shell console.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.5
 */
public class ListCommandablesCommand extends ShellCommand
{
    /** Shell command. */
    public static final String COMMAND_STRING = "/commandables";

    /** Argument template. */
    public static final String ARGUMENT_TEMPLATE = "";
    
    /** Shell help line. */
    private static final String HELP_LINE = "lists available \"shell commandables\" (such as some listeners)";
    
    /**
     * Creates a new ListCommandablesCommand.
     */
    public ListCommandablesCommand()
    {
        super(COMMAND_STRING, ARGUMENT_TEMPLATE, HELP_LINE);
    }

    /**
     * @see org.aitools.programd.interfaces.shell.ShellCommand#handles(java.lang.String)
     */
    public boolean handles(String commandLine)
    {
        return commandLine.toLowerCase().equals(COMMAND_STRING);
    }

    /**
     * Prints a list of the available ShellCommandables to the shell console.
     * 
     * @see org.aitools.programd.interfaces.shell.ShellCommand#handle(java.lang.String, org.aitools.programd.interfaces.shell.Shell)
     */
    public void handle(String commandLine, Shell shell)
    {
        int commandableCount = 0;
        shell.showMessage("Available shell commandables:");
        for (ManagedProcess process : shell.getCore().getManagedProcesses().values())
        {
            if (process instanceof ShellCommandable)
            {
                ShellCommandable commandable = (ShellCommandable) process;
                shell.showMessage("/" + commandable.getShellID() + " - " + commandable.getShellDescription());
                commandableCount++;
            }
        }
        if (commandableCount == 0)
        {
            shell.showError("No shell commandables are loaded.");
        }
        else
        {
            shell.showMessage("Commands after the shell commandable will be sent to the commandable.");
            shell.showMessage("Example: \"/irc /JOIN #foo\" tells thIRCListenerRC listener to join channel \"#foo\".");
        }
    }
}
