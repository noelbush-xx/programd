/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.interfaces;

import java.util.logging.Filter;

/**
 * A <code>StdFilter</code> filters out messages for passing to &quot;standard
 * stream&quot; (<code>stdout</code>, <code>stderr</code>).
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.2
 */
public interface StdFilter extends Filter
{
    // No new methods are defined.
}