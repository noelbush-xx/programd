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
 * This exception is thrown by
 * {@link org.aitools.programd.util.ObjectExemplarRegistry#get ObjectExemplarRegistry.get}
 * when its <code>label</code> does not correspond to a class that is
 * registered by the
 * {@link org.aitools.programd.util.ObjectExemplarRegistry ObjectExemplarRegistry}.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class NotARegisteredObjectException extends Exception
{
    /**
     * Creates a new NotARegisteredObjectException.
     * 
     * @param classname the classname for which there is not a registered
     *            exemplar object
     */
    public NotARegisteredObjectException(String classname)
    {
        super("No exemplar object with classname \"" + classname + "\" is registered by this ObjectExemplarRegistry.");
    }
}
