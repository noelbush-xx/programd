/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor;

/**
 * Should be thrown by processors when they find content that they cannot
 * handle.
 * 
 * @since 4.1.3
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @version 4.5
 */
public class ProcessorException extends Exception
{
    private String offendingInput;

    private static final String DUE_TO = " due to: ";

    /**
     * @param message the message describing the error
     * @param exception the exception that generated the error
     */
    public ProcessorException(String message, Throwable exception)
    {
        super(message, exception);
    }

    /**
     * @param message the message describing the error
     * @param exception the exception that generated the error
     * @param input the offending input
     */
    public ProcessorException(String message, Throwable exception, String input)
    {
        super(message, exception);
        this.offendingInput = input;
    }

    /**
     * @return the offending input, if available, that generated this exception
     */
    public String getOffendingInput()
    {
        return this.offendingInput;
    }

    /**
     * @return whether this exception contains an offending input
     */
    public boolean hasOffendingInput()
    {
        return (this.offendingInput != null);
    }

    /**
     * If an {@link #offendingInput} has been specified, this message will be
     * the <code>ProcessorException</code>'s regular message, plus the string
     * &quot; due to: &quot; followed by the offending input.
     * 
     * @return a message including the offending input, if available
     */
    public String getExplanatoryMessage()
    {
        if (this.offendingInput == null)
        {
            return this.getMessage();
        }
        return this.getMessage() + DUE_TO + this.offendingInput;
    }

}