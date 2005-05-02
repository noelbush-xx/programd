/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.interfaces.shell;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.aitools.programd.graph.Graphmaster;

/**
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 *
 */
public class LoadCommand extends ShellCommand
{
    /** Shell command string. */
    public static final String COMMAND_STRING = "/load";
    
    /** Argument template. */
    public static final String ARGUMENT_TEMPLATE = "filename";
    
    /** Shell help line. */
    private static final String HELP_LINE = "loads/reloads given filename for active bot";
    
    /**
     * Creates a new LoadCommand.
     */
    public LoadCommand()
    {
        super(COMMAND_STRING, ARGUMENT_TEMPLATE, HELP_LINE);
    }

    /**
     * @see org.aitools.programd.interfaces.shell.ShellCommand#handles(java.lang.String)
     */
    public boolean handles(String commandLine)
    {
        return commandLine.toLowerCase().startsWith(COMMAND_STRING);
    }

    /**
     * Attempts to load a specified file into the Graphmaster.
     * 
     * @see org.aitools.programd.interfaces.shell.ShellCommand#handle(java.lang.String, org.aitools.programd.interfaces.shell.Shell)
     */
    public void handle(String commandLine, Shell shell)
    {
        // See if there is a filename.
        int space = commandLine.indexOf(' ');
        if (space == -1)
        {
            shell.showError("You must specify a filename.");
        }
        else
        {
            Graphmaster graphmaster = shell.getCore().getGraphmaster();
            int categories = graphmaster.getTotalCategories();
            String path = commandLine.substring(space + 1);
            graphmaster.load(path, shell.getCurrentBotID());
            Logger.getLogger("programd").log(Level.INFO,
                    graphmaster.getTotalCategories() - categories + " categories loaded from \"" + path + "\".");
        }
    }
}
