/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.util;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Prints the message &quot;I am alive!&quot; to the console/log.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class IAmAlivePulse implements Pulse
{
    private Logger logger = Logger.getLogger("programd");

    /**
     * Prints the message &quot;I am alive!&quot; to the console.
     */
    public void emit()
    {
        this.logger.log(Level.INFO, "I am alive!");
    }
}