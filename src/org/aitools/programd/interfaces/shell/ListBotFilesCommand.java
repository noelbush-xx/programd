/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.interfaces.shell;

import java.net.URL;
import java.util.Set;

/**
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 *
 */
public class ListBotFilesCommand extends ShellCommand
{
    /** Shell command. */
    public static final String COMMAND_STRING = "/files";
    
    /** Argument template. */
    public static final String ARGUMENT_TEMPLATE = "";
    
    /** Shell help line. */
    private static final String HELP_LINE = "lists the files loaded by the current bot";

    /**
     * Creates a new ListBotFilesCommand.
     */
    public ListBotFilesCommand()
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
     * Prints a list of the files loaded by the current bot to the shell console.
     * 
     * @see org.aitools.programd.interfaces.shell.ShellCommand#handle(java.lang.String, org.aitools.programd.interfaces.shell.Shell)
     */
    public void handle(String commandLine, Shell shell)
    {
        String botID = shell.getCurrentBotID();
        Set<URL> keys = shell.getBots().getBot(botID).getLoadedFilesMap().keySet();
        int fileCount = keys.size();
        if (fileCount == 0)
        {
            shell.showMessage("No files loaded by \"" + botID + "\".");
        }
        else if (fileCount > 1)
        {
            shell.showMessage(fileCount + " files loaded by \"" + botID + "\":");
        }
        else
        {
            shell.showMessage("1 file loaded by \"" + botID + "\":");
        }
        for (URL url : keys)
        {
            shell.showMessage(url.toExternalForm());
        }
    }
}
