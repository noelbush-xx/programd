/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.interfaces;

import java.util.logging.StreamHandler;

/**
 * A <code>StdErrHandler</code> displays error messages
 * ({@link java.util.logging.Level.WARNING Level.WARNING}
 * and {@link java.util.logging.Level.SEVERE Level.SEVERE})
 * to {@link java.lang.System.out System.out}.
 * 
 * @author Noel Bush
 * @since 4.2
 */
public class StdErrHandler extends StreamHandler
{
    public StdErrHandler(ConsoleSettings consoleSettings)
    {
        super(System.err, new ConsoleFormatter(consoleSettings));
        setFilter(new StdErrFilter());
    }
}
