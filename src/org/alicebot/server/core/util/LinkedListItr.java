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


/**
This version is based on the package LinkedList by Mark Allen Weiss
modified by Ing. Pedro E. Colla

@version 4.1.1
@author  Mark Allen Weiss
@author  Thomas Ringate/Pedro Colla

*/


    /**
     * Linked list implementation of the list iterator
     *    using a header node.
     */
    public class LinkedListItr
    {
        /**
         * Construct the list iterator
         * @param theNode any node in the linked list.
         */
        LinkedListItr( ListNode theNode )
        {
            current = theNode;
        }

        /**
         * Test if the current position is past the end of the list.
         * @return true if the current position is null.
         */
        public boolean isPastEnd( )
        {
            return current == null;
        }

        /**
         * Return the item stored in the current position.
         * @return the stored item or null if the current position
         * is not in the list.
         */
        public Object retrieve( )
        {
            return isPastEnd( ) ? null : current.element;
        }

        /**
         * Advance the current position to the next node in the list.
         * If the current position is null, then do nothing.
         */
        public void advance( )
        {
            if( !isPastEnd( ) )
                current = current.next;
        }

        ListNode current;    // Current position
    }
