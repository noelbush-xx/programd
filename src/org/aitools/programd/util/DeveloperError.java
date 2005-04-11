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
 * A developer error.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class DeveloperError extends Error
{
    /**
     * Creates a new DeveloperError associated with the given Throwable.
     * 
     * @param e the Throwable that is responsible for this DeveloperError
     */
    public DeveloperError(Throwable e)
    {
        super("Developer did not describe exception.");
        initCause(e);
    }

    /**
     * Creates a new DeveloperError associated with the given Throwable.
     * 
     * @param message the message describing the error
     * @param e the Throwable that is responsible for this DeveloperError
     */
    public DeveloperError(String message, Throwable e)
    {
        super(message);
        initCause(e);
    }
}