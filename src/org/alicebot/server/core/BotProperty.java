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
    Code cleanup (4.1.3 [00] - October 2001, Noel Bush)
    - formatting cleanup
    - general grammar fixes
    - complete javadoc
    - removed useless imports
    - added empty string constant
*/

package org.alicebot.server.core;

import java.util.Hashtable;


/**
 *  <p>
 *  The <code>BotProperty</code> object stores the static values of bot predicates.
 *  </p>
 *  <p>
 *  Confusion about the words &quot;predicate&quot; and predciates abounds;
 *  these items are properly called &quot;predicates&quot;, but the name of this
 *  class and its associated misnamed tag (&lt;predicate/&gt;) will stand (for now).
 *  </p>
 *
 *  @author Thomas Ringate/Pedro Colla
 *  @version 4.1.3
 */
public class BotProperty
{

    /** Storage for all bot predicates. */
    public static Hashtable botPredicates = null;

    /** An empty string. */
    private static final String EMPTY_STRING = "";

    public BotProperty()
    {
        botPredicates = new Hashtable();
    }


    /**
     *  Retrieves the value of a named bot predicate.
     *
     *  @param predicate    the name of the bot predicate to get
     *
     *  @return the value of the bot predicate
     */
    public static String getPredicateValue(String name)
    {
        // If no predicates have been set, don't bother looking.
        if (botPredicates == null)
        {
            return Globals.getPredicateEmptyDefault();
        }

        // Don't bother with empty predicate names.
        if (name.equals(EMPTY_STRING))
        {
            return Globals.getPredicateEmptyDefault();
        }

        // Retrieve the contents of the predicate.
        String value = (String)botPredicates.get(name);
        if (value != null)
        {
            return value;
        }
        else
        {
            return Globals.getPredicateEmptyDefault();
        }
    }


    /**
     *  Sets the value of a bot predicate.
     *
     *  @param predicate    the name of the bot predicate to set
     */
    public static void setPredicateValue(String name, String value)
    {

        // Predicate name must not be empty.
        if (name.equals(EMPTY_STRING))
        {
           return;
        }

        // Ensure that the Hashtable is initialized (this really looks like overkill).
        if (botPredicates == null)
        {
            botPredicates = new Hashtable();
        }

        // Store the predicate.
        botPredicates.put(name, new String(value));
    }
}
