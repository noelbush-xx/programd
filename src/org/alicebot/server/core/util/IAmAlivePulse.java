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
 *	Prints the message &quot;I am alive!&quot;
 *  to the console.
 *
 *  @author	Noel Bush
 */
public class IAmAlivePulse implements Pulse
{
    /**
     *	Prints the message &quot;I am alive!&quot;
     *  to the console.
     */
    public void emit()
    {
        Trace.userinfo("I am alive!");
    }
}
