/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.listener;

/**
 * An exception indicating that a listener parameter was invalid.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class InvalidListenerParameterException extends Exception
{
    /**
     * Creates a new InvalidListenerParameterException with the given message.
     * 
     * @param message the message describing the exception
     */
    public InvalidListenerParameterException(String message)
    {
        super(message);
    }
}
