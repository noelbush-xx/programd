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
 * Some utilities for handling and describing errors.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class Errors
{
    /**
     * Produces some kind of useful description of the given throwable.
     * 
     * @param e the throwable to describe
     * @return a description
     */
    public static String describe(Throwable e)
    {
        String description = e.getLocalizedMessage();
        if (description == null)
        {
            description = e.getMessage();
        }
        if (description == null)
        {
            description = e.toString();
        }
        if (description == null)
        {
            description = e.getClass().getName();
        }
        return description;
    }
}
