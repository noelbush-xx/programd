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
 *  A developer error.
 *
 *  @author Noel Bush
 */
public class DeveloperError extends Error
{
    private Throwable throwable;

    public DeveloperError(String message)
    {
        super(message);
    }


    public DeveloperError(Throwable e)
    {
        super("Developer did not describe exception.");
        this.throwable = e;
    }


    public DeveloperError(String message, Throwable e)
    {
        super(message);
        this.throwable = e;
    }


    public Throwable getEmbedded()
    {
        return this.throwable;
    }
}

