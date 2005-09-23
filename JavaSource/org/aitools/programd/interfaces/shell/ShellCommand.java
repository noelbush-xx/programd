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
 * A <code>ShellCommand</code> is a command that can be executed from
 * the {@link Shell}.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
abstract public class ShellCommand
{
    /** The command string for the shell command (so it can be returned via a get method). */
    private String commandString;
    
    /** The argument template for the shell command (so it can be returned via a get method). */
    private String argumentTemplate;
    
    /** The help line for the shell command (so it can be returned via a get method). */
    private String helpLine;
    
    /**
     * Creates a <code>ShellCommand</code> that handles the given command string.
     * @param commandStringToUse the command string to handle
     * @param argumentTemplateToUse the argument template to use
     * @param helpLineToUse the help line to provide when asked
     */
    public ShellCommand(String commandStringToUse, String argumentTemplateToUse, String helpLineToUse)
    {
        this.commandString = commandStringToUse;
        this.argumentTemplate = argumentTemplateToUse;
        this.helpLine = helpLineToUse;
    }
    /**
     * Indicates whether the ShellCommand handles
     * a command contained in the given line.
     * 
     * @param commandLine the line of text received
     * @return whether the ShellCommand handles this
     */
    abstract public boolean handles(String commandLine);
    
    /**
     * Returns the command to which this responds.
     * 
     * @return the command to which this responds
     */
    public String getCommandString()
    {
        return this.commandString;
    }
    
    /**
     * Returns the argument template for this command.
     * 
     * @return the argument template for this command
     */
    public String getArgumentTemplate()
    {
        return this.argumentTemplate;
    }
    
    /**
     * Returns a line of help for this command.
     * 
     * @return a line of help for this command
     */
    public String getHelpLine()
    {
        return this.helpLine;
    }
    
    /**
     * Handles the given command line.
     * 
     * @param commandLine the command line to handle
     * @param shell the shell that got the command
     */
    abstract public void handle(String commandLine, Shell shell);
}
