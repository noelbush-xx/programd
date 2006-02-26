/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.interfaces.shell;

import org.aitools.programd.util.UserSystem;

/**
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 *
 */
public class MemoryCommand extends ShellCommand
{
    /** Shell command string. */
    public static final String COMMAND_STRING = "/memory";
    
    /** Argument template. */
    public static final String ARGUMENT_TEMPLATE = "";
    
    /** Shell help line. */
    private static final String HELP_LINE = "shows statistics on free/available memory";
    
    /**
     * Creates a new LoadCommand.
     */
    public MemoryCommand()
    {
        super(COMMAND_STRING, ARGUMENT_TEMPLATE, HELP_LINE);
    }

    /**
     * @see org.aitools.programd.interfaces.shell.ShellCommand#handles(java.lang.String)
     */
    @Override
    public boolean handles(String commandLine)
    {
        return commandLine.toLowerCase().equals(COMMAND_STRING);
    }

    /**
     * Attempts to load a specified file into the Graphmaster.
     * 
     * @see org.aitools.programd.interfaces.shell.ShellCommand#handle(java.lang.String, org.aitools.programd.interfaces.shell.Shell)
     */
    @Override
    public void handle(@SuppressWarnings("unused") String commandLine, Shell shell)
    {
        shell.showMessage(UserSystem.memoryReport());
    }
}
