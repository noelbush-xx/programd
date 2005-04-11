/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor.botconfiguration;

import org.aitools.programd.processor.ProcessorException;

/**
 * Should be thrown by {@link BotConfigurationElementProcessor} s when they find
 * invalid AIML.
 * 
 * @version 4.5
 * @since 4.1.3
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class InvalidStartupElementException extends ProcessorException
{
    /**
     * Creates a new InvalidStartupElement exception with the given message and
     * associated with the given Throwable.
     * 
     * @param message the message to associate with the exception
     * @param e the Throwable that caused the exception
     */
    public InvalidStartupElementException(String message, Throwable e)
    {
        super(message, e);
    }

    /**
     * Creates a new InvalidStartupElement exception with the given message and
     * associated with the given Throwable and input.
     * 
     * @param message the message to associate with the exception
     * @param e the Throwable that caused the exception
     * @param input the input that caused the exception
     */
    public InvalidStartupElementException(String message, Throwable e, String input)
    {
        super(message, e, input);
    }
}