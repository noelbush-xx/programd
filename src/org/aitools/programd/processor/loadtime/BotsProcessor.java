/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor.loadtime;

import org.aitools.programd.Core;

/**
 * The <code>bots</code> element is a container for bot configuration
 * definitions.
 * 
 * @version 4.2
 * @author Noel Bush
 */
public class BotsProcessor extends StartupElementProcessor
{
    public static final String label = "bots";

    public BotsProcessor(Core coreToUse)
    {
        super(coreToUse);
    }
}