/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.multiplexor;

/**
 * Thrown when a duplicate userid has somehow been entered in user info storage.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.2
 */
public class DuplicateUserIDError extends Exception
{
    /**
     * Creates a new DuplicateUserIDError.
     * 
     * @param userid the userid that was duplicated
     */
    public DuplicateUserIDError(String userid)
    {
        super("Duplicate userid: \"" + userid + "\"");
    }
}
