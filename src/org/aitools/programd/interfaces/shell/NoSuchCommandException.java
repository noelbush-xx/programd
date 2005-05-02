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
 * Thrown by {@link ShellCommandRegistry} if a command line
 * is passed that does not contain a recognized command.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.5
 */
public class NoSuchCommandException extends Exception
{
    /**
     * Constructs a new exception
     * @param commandLine the command line that generated the exception
     */
    public NoSuchCommandException(String commandLine)
    {
        super("No shell command could be found to parse the command line \"" + commandLine + "\".");
    }
}
