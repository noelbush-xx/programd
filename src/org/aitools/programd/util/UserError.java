/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.util;

/**
 * A user error.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class UserError extends Error
{
    /**
     * Creates a new UserError with the given cause.
     * 
     * @param cause the cause of the UserError
     */
    public UserError(Throwable cause)
    {
        super("Developer did not describe exception.");
        initCause(cause);
    }

    /**
     * Creates a new UserError with the given message cause.
     * 
     * @param message a message about the UserError
     * @param cause the cause of the UserError
     */
    public UserError(String message, Throwable cause)
    {
        super(message);
        initCause(cause);
    }
}