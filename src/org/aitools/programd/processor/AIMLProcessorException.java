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
 * Should be thrown by {@link AIMLProcessor} s when they find invalid AIML.
 * 
 * @since 4.1.3
 * @author Noel Bush
 */
public class AIMLProcessorException extends ProcessorException
{
    /**
     * Creates a new AIMLProcessorException with the given message, associated with the given Throwable.
     * @param message the message to use
     * @param e the Throwable to associate with the exception
     */
    public AIMLProcessorException(String message, Throwable e)
    {
        super(message, e);
    } 
}