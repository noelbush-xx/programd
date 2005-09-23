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
 * A <code>Pulse</code> does something (anything) at a regular interval to
 * indicate that the bot is alive.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public interface Pulse
{
    /**
     * Emit can do anything you want it to. It will be called at a regular
     * interval.
     */
    public void emit();
}