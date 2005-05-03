/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.test.aiml;

/**
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 *
 */
public class InputBase extends ItemBase<Input, String> implements Input
{
    /**
     * Creates a new InputBase with the given content.
     * 
     * @param string the content for the InputBase
     */
    public InputBase(String string)
    {
        super(string);
    }
}
