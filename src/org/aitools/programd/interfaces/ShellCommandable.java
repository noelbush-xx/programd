/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.interfaces;

/**
 * Defines the interface for processes that can receive commands from the
 * interactive {@link Shell} .
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @version 4.5
 */
public interface ShellCommandable
{
    /**
     * Returns a string that can be used to send commands to the listener from
     * the interactive shell.
     * 
     * @return a string that can be used to send commands to the listener from
     *         the interactive shell
     */
    public String getShellID();

    /**
     * Returns a string that describes the listener when getting a listener list
     * in the interactive shell.
     * 
     * @return a string that describes the listener
     */

    public String getShellDescription();

    /**
     * Returns a list of shell commands that are available.
     * 
     * @return a list of shell commands that are available
     */
    public String getShellCommands();

    /**
     * Sends a command to the shell. The <code>ShellCommandable</code> itself
     * is responsible for putting back any output to the shell.
     * 
     * @param command the command to send to the shell
     */
    public void processShellCommand(String command);
}