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
 * Indicates that no match was found in the {@link org.aitools.programd.graph.Graphmapper} .
 */
public class NoMatchException extends Exception
{
    /** The path for which there was no match. */
    private String _path;

    /**
     * Constructs a new NoMatchException with no path specified.
     */
    public NoMatchException()
    {
        // Nothing to do.
    }

    /**
     * Constructs a new NoMatchException for the given path.
     * 
     * @param path the path for which there was no match
     */
    public NoMatchException(String path)
    {
        this._path = path;
    }

    /**
     * @see java.lang.Throwable#getMessage()
     */
    @Override
    public String getMessage()
    {
        if (this._path != null)
        {
            return "No match found for path \"" + this._path + "\".";
        }
        return "No match found.";
    }
}
