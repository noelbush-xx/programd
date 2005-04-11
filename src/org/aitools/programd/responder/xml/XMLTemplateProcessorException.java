/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.responder.xml;

import org.aitools.programd.processor.ProcessorException;

/**
 * Should be thrown by {@link XMLTemplateProcessor} s when they find invalid
 * template tags.
 * 
 * @since 4.1.3
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class XMLTemplateProcessorException extends ProcessorException
{
    /**
     * Creates a new XMLTemplateProcessorException with the given message,
     * associated with the given Throwable.
     * 
     * @param message the message to use
     * @param e the Throwable to associate with the exception
     */
    public XMLTemplateProcessorException(String message, Throwable e)
    {
        super(message, e);
    }
}