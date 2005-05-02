/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.interfaces.shell;

import java.util.Collection;

import org.aitools.programd.util.ObjectExemplarRegistry;

/**
 * Maps ShellCommands to their command strings.
 * 
 * @since 4.5
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class ShellCommandRegistry extends ObjectExemplarRegistry<ShellCommand>
{
    /** The list of built-in shell commands (fully-qualified class names). */
    private static final String[] COMMAND_LIST = { "org.aitools.programd.interfaces.shell.AIMLCommand",
                                                   "org.aitools.programd.interfaces.shell.HelpCommand",
                                                   "org.aitools.programd.interfaces.shell.LoadCommand",
                                                   "org.aitools.programd.interfaces.shell.UnloadCommand",
                                                   "org.aitools.programd.interfaces.shell.BotListCommand",
                                                   "org.aitools.programd.interfaces.shell.TalkToCommand",
                                                   "org.aitools.programd.interfaces.shell.WhoCommand",
                                                   "org.aitools.programd.interfaces.shell.ListBotFilesCommand",
                                                   "org.aitools.programd.interfaces.shell.ListCommandablesCommand" };
    
    /**
     * Creates a new <code>AIMLProcessorRegistry</code>, with
     * no additional commands.
     */
    public ShellCommandRegistry()
    {
        super(COMMAND_LIST, ShellCommand.class);
    }

    /**
     * @param commandLine the command line to be processed
     * @return the <code>ShellCommand</code> associated with the given command string
     * @throws NoSuchCommandException if no matching <code>ShellCommand</code> is available
     */
    public ShellCommand getHandlerFor(String commandLine) throws NoSuchCommandException
    {
        for (ShellCommand command : this.registry.values())
        {
            if (command.handles(commandLine))
            {
                return command;
            }
        }
        throw new NoSuchCommandException(commandLine);
    }
    
    /**
     * @return the values stored in this map
     */
    public Collection<ShellCommand> getValues()
    {
        return this.registry.values();
    }
}