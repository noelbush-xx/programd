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
Linked List implementation of the list using a header node.
This version contains the package LinkedList by Mark Allen Weiss
modified by Ing. Pedro E. Colla
@version 4.1.1
@author  Mark Allen Weiss
@author  Thomas Ringate/Pedro Colla
*/

    public class LinkedList
    {
        /**
         * Construct the list
         */
        public LinkedList( )
        {
            header = new ListNode( null );
        }

        /**
         * Test if the list is logically empty.
         * @return true if empty, false otherwise.
         */
        public boolean isEmpty( )
        {
            return header.next == null;
        }

        /**
         * Make the list logically empty.
         */
        public void makeEmpty( )
        {
            header.next = null;
        }

        /**
         * Return an iterator representing the header node.
         */
        public LinkedListItr zeroth( )
        {
            return new LinkedListItr( header );
        }

        /**
         * Return an iterator representing the first node in the list.
         * This operation is valid for empty lists.
         */

        public LinkedListItr first( )
        {
            return new LinkedListItr( header.next );
        }

        /**
         * Insert after p.
         * @param x the item to insert.
         * @param p the position prior to the newly inserted item.
         */
        public void insert( Object x, LinkedListItr p )
        {
            if( p != null && p.current != null )
                p.current.next = new ListNode( x, p.current.next );
        }

        /**
         * Return iterator corresponding to the first node containing an item.
         * @param x the item to search for.
         * @return an iterator; iterator isPastEnd if item is not found.
         */
        public LinkedListItr find( Object x )
        {
/* 1*/      ListNode itr = header.next;

/* 2*/      while( itr != null && !itr.element.equals( x ) )
/* 3*/          itr = itr.next;

/* 4*/      return new LinkedListItr( itr );
        }

        /**
         * Return iterator prior to the first node containing an item.
         * @param x the item to search for.
         * @return appropriate iterator if the item is found. Otherwise, the
         * iterator corresponding to the last element in the list is returned.
         */
        public LinkedListItr findPrevious( Object x )
        {
/* 1*/      ListNode itr = header;

/* 2*/      while( itr.next != null && !itr.next.element.equals( x ) )
/* 3*/          itr = itr.next;

/* 4*/      return new LinkedListItr( itr );
        }

        /**
         * Remove the first occurrence of an item.
         * @param x the item to remove.
         */
        public void remove( Object x )
        {
            LinkedListItr p = findPrevious( x );

            if( p.current.next != null )
                p.current.next = p.current.next.next;  // Bypass deleted node
        }

        // Simple print method
        public static void printList( LinkedList theList )
        {
            if( theList.isEmpty( ) )
                System.out.print( "Empty list" );
            else
            {
                LinkedListItr itr = theList.first( );
                for( ; !itr.isPastEnd( ); itr.advance( ) )
                    System.out.print( itr.retrieve( ) + " " );
            }

            System.out.println( );
        }

        private ListNode header;
    }
