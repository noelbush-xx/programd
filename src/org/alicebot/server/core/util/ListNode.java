package org.alicebot.server.core.util;

/**
Alice Program D
Copyright (C) 1995-2001, A.L.I.C.E. AI Foundation

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
USA.

@author  Richard Wallace
@author  Jon Baer
@author  Thomas Ringate/Pedro Colla
@version 4.1.1
*/

/**Basic node stored in a linked list
   Note that this class is not accessible outside
   the package org.alicebot.server.core.AIMLparser
   @version 4.1.1
   @author  Mark Allen Weiss
   @author  Thomas Ringate/Pedro Colla
*/
       
    class ListNode
    {
            // Constructors
        ListNode( Object theElement )
        {
            this( theElement, null );
        }

        ListNode( Object theElement, ListNode n )
        {
            element = theElement;
            next    = n;
        }

            // Friendly data; accessible by other package routines
        Object   element;
        ListNode next;
    }
