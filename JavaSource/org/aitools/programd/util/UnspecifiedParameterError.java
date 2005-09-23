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
        super("Parameter \"" + paramName + "\" was not specified.");
    }
}
