/*
    Alicebot Program D
    Copyright (C) 1995-2001, A.L.I.C.E. AI Foundation
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
    USA.
*/

package org.alicebot.server.core.util;


/**
 *  Thrown by {@link PatternArbiter} when it gets a pattern
 *  candidate that does not meet the definition of an AIML pattern.
 *
 *  @author Noel Bush
 */
public class NotAnAIMLPatternException extends Exception
{
    /** The pattern which this exception concerns. */
    private String pattern;
    
    /** First part of a message. */
    private static final String MSG_PART_ONE = "Not an AIML pattern: \"";
    
    /** Second part of a message. */
    private static final String MSG_PART_TWO = "\" - ";
    
    
    /**
     *  Records the pattern and error message for this exception.
     *
     *  @param message	the explanation why this pattern is invalid
     *  @param pattern	the pattern itself
     */
    public NotAnAIMLPatternException(String message, String pattern)
    {
        super(message);
        this.pattern = pattern;
    }
    
    
    public String getMessage()
    {
        return MSG_PART_ONE + pattern + MSG_PART_TWO + super.getMessage();
    }
}

