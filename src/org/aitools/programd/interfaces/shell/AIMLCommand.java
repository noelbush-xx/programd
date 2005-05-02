/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.interfaces.shell;

import org.aitools.programd.parser.TemplateParser;
import org.aitools.programd.parser.TemplateParserException;
import org.aitools.programd.processor.ProcessorException;
import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.UserError;

/**
 * Tries to process a given fragment of template-side AIML.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.5
 */
public class AIMLCommand extends ShellCommand
{
    /** Shell command. */
    public static final String COMMAND_STRING = "/aiml";
    
    /** Argument template. */
    public static final String ARGUMENT_TEMPLATE = "aiml-fragment";
    
    /** Shell help line. */
    private static final String HELP_LINE = "tries to process a fragment of template-side AIML";
    
    /** Template start tag. */
    private static final String TEMPLATE_START = "<template>";

    /** Template end tag. */
    private static final String TEMPLATE_END = "</template>";

    /**
     * Creates a new AIMLCommand.
     */
    public AIMLCommand()
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
     * Tries to process a given fragment of template-side AIML.
     * @see org.aitools.programd.interfaces.shell.ShellCommand#handle(java.lang.String, org.aitools.programd.interfaces.shell.Shell)
     */
    public void handle(String commandLine, Shell shell)
    {
        // See if there is some content.
        int space = commandLine.indexOf(' ');
        if (space == -1)
        {
            shell.showError("You must specify some template content.");
        }
        else
        {
            // Create a new TemplateParser.
            TemplateParser parser;
            try
            {
                parser = new TemplateParser("", "", shell.getCurrentBotID(), shell.getCore());
            }
            catch (TemplateParserException e)
            {
                throw new DeveloperError("Error occurred while creating new TemplateParser.", e);
            }
            try
            {
                shell.showMessage(parser.processResponse(TEMPLATE_START + commandLine.substring(space + 1) + TEMPLATE_END));
            }
            catch (ProcessorException e)
            {
                throw new UserError(e);
            }
        }
    }
}
