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

/*  Code cleanup (28 Oct 2001, Noel Bush)
    - formatting cleanup
    - complete javadoc
    - made import explicit
*/

/*
    Further fixes and optimizations (4.1.3 [01] - November 2001, Noel Bush)
    - CHANGED INTERFACE!!!!  Now uses String instead of Object as key.
    - added remove() method signature
*/

package org.alicebot.server.core.node;

import java.util.Set;


/**
 *  A <code>Nodemapper</code> maps the branches in a {@link Graphmaster Graphmaster} structure.
 */
public interface Nodemapper
{
    /**
     *  Puts an object into the <code>Nodemapper</code>.
     *
     *  @param key      the key with which the object should be stored / will be retrieved
     *  @param value    the object to be stored
     */
    public Object put(String key, Object value);


    /**
     *  Gets an object from the <code>Nodemapper</code>.
     *
     *  @param key      the key to use in retrieving the object
     *
     *  @return the object with that key (if found)
     */
    public Object get(String key);


    /**
     *  Removes the node mapped to this <code>key</code>
     *  from the <code>Nodemapper</code>.
     *
     *  @param key  key whose node should be removed
     */
    public void remove(String key);

    
    /**
     *  Returns the key set for the <code>Nodemapper</code>.
     *
     *  @return the Set of keys
     */
    public Set keySet();


    /**
     *  Tells whether the <code>Nodemapper</code> contains the given key.
     *
     *  @param key  the key to look for
     *
     *  @return boolean indicating whether the <code>Nodemapper</code> contains the key
     */
    public boolean containsKey(String key);
}
