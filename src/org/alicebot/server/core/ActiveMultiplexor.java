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
    More fixes (4.1.3 [02] - November 2001, Noel Bush
    - changed *Predicate*() methods to *PredicateValue*()
    - added getInternalResponse(), getReply() and getReplies()
    - changed a server property name
    - introduced use of NoSuchPredicateException
    - added access methods for createUser(), checkUser() and changePassword()
*/

package org.alicebot.server.core;

import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.util.DeveloperError;


/**
 *  <p>
 *  Implements access to a {@link Multiplexor} of
 *  configurable type.  Uses a variant of the Singleton pattern.
 *  </p>
 *  <p>
 *  This is used to allow all clients of a <code>Multiplexor</code>
 *  to simply use method calls on an instance of <code>ActiveMultiplexor</code>,
 *  without having to care about which kind of <code>Multiplexor</code>
 *  is actually in use.
 *  </p>
 *
 *  @since 4.1.3
 *
 *  @author Noel Bush
 */
public class ActiveMultiplexor
{
    /** The {@link Multiplexor} managed by the instance of this class. */
    private static Multiplexor multiplexor;

    /** The private field that (partially) ensures <code>ActiveMultiplexor</code> is a singleton. */
    private static final ActiveMultiplexor myself = new ActiveMultiplexor(Globals.getProperty("programd.multiplexor", "org.alicebot.server.core.FlatFileMultiplexor"));


    /**
     *  Private constructor that initializes the <code>ActiveMultiplexor</code>
     *  with an implementation of {@link Multiplexor}.
     *
     *  @param className    the name of the subclass of {@link Multiplexor} that should be used
     */
    private ActiveMultiplexor(String className)
    {
        try
        {
            multiplexor = (Multiplexor)Class.forName(className).newInstance();
        }
        catch (Exception e)
        {
            throw new DeveloperError(e);
        }
    }


    /**
     *  Prohibits cloning this class.
     */
    protected Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }


    /**
     *  Returns the Multiplexor managed by this class.
     *
     *  @return the Multiplexor managed by this class
     */
    public static Multiplexor getInstance()
    {
        return multiplexor;
    }
}
