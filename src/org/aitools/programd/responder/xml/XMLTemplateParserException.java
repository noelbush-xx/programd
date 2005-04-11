/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.responder.xml;

/**
 * Thrown by {@link XMLTemplateParser} when it is given invalid input.
 * 
 * @since 4.5
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class XMLTemplateParserException extends Exception
{
    /**
     * Creates a new XMLTemplateParserException with the given message.
     * 
     * @param message the message describing the exception
     */
    public XMLTemplateParserException(String message)
    {
        super(message);
    }
}