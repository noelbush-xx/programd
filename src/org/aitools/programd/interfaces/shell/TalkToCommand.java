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
 * Tries to switch conversation to a bot identified on the command line.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.5
 */
public class TalkToCommand extends ShellCommand
{
    /** Shell command string. */
    private static final String COMMAND_STRING = "/talkto";

    /** Argument template. */
    public static final String ARGUMENT_TEMPLATE = "botid";
    
    /** Shell help line. */
    private static final String HELP_LINE = "switches conversation to given bot";
    
    /**
     * Creates a new TalkToCommand.
     */
    public TalkToCommand()
    {
        super(COMMAND_STRING, ARGUMENT_TEMPLATE, HELP_LINE);
    }
    
    /**
     * @see org.aitools.programd.interfaces.shell.ShellCommand#handles(java.lang.String)
     */
    public boolean handles(String commandLine)
    {
        return commandLine.toLowerCase().startsWith(TalkToCommand.COMMAND_STRING);
    }

    /**
     * Tries to switch conversation to a bot identified on the command line.
     * 
     * @see org.aitools.programd.interfaces.shell.ShellCommand#handle(java.lang.String, org.aitools.programd.interfaces.shell.Shell)
     */
    public void handle(String commandLine, Shell shell)
    {
        // See if there is a botid.
        int space = commandLine.indexOf(' ');
        if (space == -1)
        {
            shell.showError("You must specify a bot id.");
        }
        else
        {
            shell.switchToBot(commandLine.substring(space + 1));
        }
    }

}
