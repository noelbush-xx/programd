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

package org.aitools.util;

/**
 * An error condition in which a necessary parameter has not been specified.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class UnspecifiedParameterError extends Error
{
    /**
     * Creates a new UnspecifiedParameterError about the given parameter name
     * 
     * @param paramName the name of the parameter that was not specified
     */
    public UnspecifiedParameterError(String paramName)
    {
        super(String.format("Parameter \"%s\" was not specified.", paramName));
    }
}
