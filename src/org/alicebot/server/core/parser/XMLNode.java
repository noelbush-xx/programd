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

/*
    4.1.4 [00] - December 2001, Noel Bush
    - cleaned up formatting and removed unnecessary imports
    - changed to use java.util.LinkedList
*/

package org.alicebot.server.core.parser;

import java.util.LinkedList;
import java.util.ListIterator;


/**
 *  Implements a node of the XML trie
 *  created by {@link XMLParser}.
 *
 *  @version 4.1.4
 *  @author  Thomas Ringate/Pedro Colla
*/
public class XMLNode
{
    // XML Types

    /** XML Tag &lt;tag&gt; */
    public static final int TAG     = 0;

    /** XML Empty &lt;tag/&gt; */
    public static final int EMPTY   = 1;

    /** DATA = Text */
    public static final int DATA    = 2;

    /** Character Data. */
    public static final int CDATA   = 3;

    /** XML Comment. */
    public static final int COMMENT = 4;

    /** &lt;/tag&gt; */
    public static final int ENDTAG  = 5;

    // Structure of an XML node

    /**
     * Type of the node ({@link #TAG}, {@link #EMPTY},
     * {@link #DATA}, {@link #CDATA}, {@link #COMMENT}, or {@link #ENDTAG}).
     */
    public    int     XMLType;

    /** Actual data on the node or tag content.  */
    public String     XMLData;

    /** String of attributes associated (only for tags, otherwise &quot;&quot;) */
    public String     XMLAttr;

    /** Pointer to a child trie structure (only for tags, otherwise null). */
    public LinkedList XMLChild;


    /** An empty string. */
    private static final String EMPTY_STRING = "";


     /**
      * Initializes the node with empty default values.
     */
     XMLNode()
     {
        this.XMLType  = 0;
        this.XMLData  = EMPTY_STRING;
        this.XMLAttr  = EMPTY_STRING;
        this.XMLChild = null;
     }
     
     
     /**
      *  Produces a simple string representation of this node.
      *  Any two nodes with unique contents will produce unique
      *  string representations.  Two nodes with the same content
      *  will produce identical string representations.
      *
      *  @return a string representation of this node
      */
     public String toString()
     {
         StringBuffer result = new StringBuffer(this.XMLData);
         result.append(this.XMLAttr);
         if (this.XMLChild != null)
         {
             ListIterator iterator = this.XMLChild.listIterator();
             while (iterator.hasNext())
             {
                 result.append(iterator.next().toString());
             }
         }
         return result.toString();
     }
}
