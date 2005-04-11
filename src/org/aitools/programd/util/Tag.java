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
 * A simple little utility class for working with a pseudo-XML concept of
 * &quot;tag&quot;. TODO: Get rid of this!
 * 
 * @author Jon Baer
 */
public class Tag
{
    /** A left angle bracket. */
    private static final String LEFT_ANGLE_BRACKET = "<";

    /** A right angle bracket. */
    private static final String RIGHT_ANGLE_BRACKET = ">";

    /** Name of the tag. */
    private String name;

    /** The result to return from a toString call. */
    private String toString;

    /**
     * Creates a new tag.
     * 
     * @param nameToUse the name of the tag
     */
    public Tag(String nameToUse)
    {
        this.name = nameToUse;
        this.toString = LEFT_ANGLE_BRACKET + this.name + RIGHT_ANGLE_BRACKET;
    }

    /**
     * @return the tag's name surrounded by angle brackets
     */
    public String toString()
    {
        return this.toString;
    }

    /**
     * @return the tag's name
     */
    public String getName()
    {
        return this.name;
    }
}