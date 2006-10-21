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
 * Thrown by {@link PatternArbiter} when it gets a pattern candidate that does not meet the definition of an AIML
 * pattern.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class NotAnAIMLPatternException extends Exception
{
    /** The pattern which this exception concerns. */
    private String _pattern;

    /**
     * Records the pattern and error message for this exception.
     * 
     * @param message the explanation why this pattern is invalid
     * @param pattern the pattern itself
     */
    public NotAnAIMLPatternException(String message, String pattern)
    {
        super(message);
        this._pattern = pattern;
    }

    /**
     * @see java.lang.Throwable#getMessage()
     */
    @Override
    public String getMessage()
    {
        return String.format("Not an AIML pattern: \"%s\" - %s", this._pattern, super.getMessage());
    }
}
