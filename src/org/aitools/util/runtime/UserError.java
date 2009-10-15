/*
 * aitools utilities
 * Copyright (C) 2006 Noel Bush
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.aitools.util.runtime;

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
