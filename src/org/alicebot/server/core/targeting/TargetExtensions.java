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

import org.alicebot.server.core.util.StringTripleMatrix;


/**
 *  Represents the extensions of a target.
 *
 *  @author Noel Bush
 *  @since  4.1.5
 */
public class TargetExtensions extends StringTripleMatrix
{
    /**
     *  Creates a new <code>TargetExtensions</code>.
     */
    public TargetExtensions()
    {
        super();
    }


    /**
     *  Returns the extension <code>pattern</code>s
     *
     *  @return the extension <code>pattern</code>s
     */
    public LinkedList getPatterns()
    {
        return getFirsts();
    }


    /**
     *  Returns the extension <code>that</code>s
     *
     *  @return the extension <code>that</code>s
     */
    public LinkedList getThats()
    {
        return getSeconds();
    }


    /**
     *  Returns the extension <code>topic</code>s
     *
     *  @return the extension <code>topic</code>s
     */
    public LinkedList getTopics()
    {
        return getThirds();
    }
}
