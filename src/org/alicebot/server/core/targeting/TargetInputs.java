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

package org.alicebot.server.core.targeting;

import java.util.LinkedList;

import org.alicebot.server.core.util.StringTriple;
import org.alicebot.server.core.util.StringTripleMatrix;

/**
 *  Represents the inputs that created a target.
 *
 *  @author Noel Bush
 *  @since  4.1.5
 */
public class TargetInputs extends StringTripleMatrix
{
    /**
     *  Creates a new <code>TargetInputs</code> and initializes it
     *  with its first <code>text</code>, <code>that</code> and <code>topic</code>
     *  values.
     *
     *  @param text     an input text
     *  @param that     the value of the <code>that</code> predicate when the input was received
     *  @param topic    the value of the <code>topic</code> predicate when the input was received
     */
    public TargetInputs(String text, String that, String topic)
    {
        super();
        add(new StringTriple(text, that, topic));
    }


    /**
     *  Returns the input <code>text</code>s
     *
     *  @return the input <code>text</code>s
     */
    public LinkedList getTexts()
    {
        return getFirsts();
    }


    /**
     *  Returns the input <code>that</code>s
     *
     *  @return the input <code>that</code>s
     */
    public LinkedList getThats()
    {
        return getSeconds();
    }


    /**
     *  Returns the input <code>topic</code>s
     *
     *  @return the input <code>topic</code>s
     */
    public LinkedList getTopics()
    {
        return getThirds();
    }
}
