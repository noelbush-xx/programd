/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.interfaces.shell;

/**
 * Prints shell help.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class HelpCommand extends ShellCommand
{
    /** Shell command. */
    public static final String COMMAND_STRING = "/help";
    
    /** Argument template. */
    public static final String ARGUMENT_TEMPLATE = "";
    
    /** Shell help line. */
    private static final String HELP_LINE = "prints this help";
    
    /** Shell help text. */
    private static final String HELP_PREFACE = "Shell commands are preceded by a \"/\".  Available commands:";

    /*
            "/exit             - shuts down the bot server",
            "/load filename    - loads/reloads given filename for active bot",
            "/unload filename  - unloads given filename for active bot",
            "/bots             - lists loaded bots",
            "/talkto botid     - switches conversation to given bot",
            "/who              - prints the id of the current bot",
            "/files            - lists the files loaded by the current bot",
            "/commandables     - lists available \"shell commandables\" (such as some listeners)" };
*/
    /**
     * Creates a new HelpCommand.
     */
    public HelpCommand()
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
     * Prints a help summary of all available commands to the shell console.
     * @see org.aitools.programd.interfaces.shell.ShellCommand#handle(java.lang.String, org.aitools.programd.interfaces.shell.Shell)
     */
    public void handle(String commandLine, Shell shell)
    {
        shell.showMessage(HELP_PREFACE);
        for (ShellCommand command : shell.getCommands())
        {
            shell.showMessage(String.format("%-30s", command.getCommandString() + ' ' + command.getArgumentTemplate()) + command.getHelpLine());
        }
    }
}
