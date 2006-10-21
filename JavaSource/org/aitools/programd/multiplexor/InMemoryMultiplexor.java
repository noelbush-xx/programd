/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.multiplexor;

import java.util.HashMap;
import java.util.Map;

import org.aitools.programd.Core;

/**
 * This is a generic Multiplexor that doesn't store anything anywhere;
 * mostly useful for testing, or for situations where there's no need
 * to remember user predicates, dialogue history, etc. across restarts
 * of the server.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class InMemoryMultiplexor extends Multiplexor<Object>
{
    /** A map of userids to botid-keyed predicate maps. */
    private Map<String, Map<String, Map<String, String>>> _predicateMaps = new HashMap<String, Map<String,Map<String,String>>>();
    
    /**
     * Creates a new InMemoryMultiplexor with the given Core as owner.
     * 
     * @param core the Core that owns this FlatFileMultiplexor
     */
    public InMemoryMultiplexor(Core core)
    {
        super(core);
    }

    /**
     * Does nothing.
     */
    @Override
    public void initialize()
    {
        // No initialization necessary.
    }

    /**
     * Always returns true.
     */
    @Override
    @SuppressWarnings("unused")
    public boolean checkUser(String userid, String password, String botid)
    {
        return true;
    }

    /**
     * Does nothing.
     */
    @Override
    @SuppressWarnings("unused")
    public void createUser(String userid, String password, String botid)
    {
        // Do nothing.
    }

    /**
     * Always returns true.
     */
    @Override
    @SuppressWarnings("unused")
    public boolean changePassword(String userid, String password, String botid)
    {
        return true;
    }

    /**
     * Always returns null.
     */
    @Override
    @SuppressWarnings("unused")
    protected Object getStorageMechanism(String userid, String botid)
    {
        return null;
    }

    /**
     * Does nothing.
     */
    @Override
    @SuppressWarnings("unused")
    protected void preparePredicateForStorage(Object mechanism, String userid, String botid, String name, PredicateValue value)
    {
        // Do nothing.
    }

    /**
     * Does nothing.
     */
    @Override
    public String loadPredicate(String name, String userid, String botid) throws NoSuchPredicateException
    {
        Map<String, Map<String, String>> userPredicates;
        if (this._predicateMaps.containsKey(userid))
        {
            userPredicates = this._predicateMaps.get(userid);
        }
        else
        {
            userPredicates = new HashMap<String, Map<String, String>>();
            this._predicateMaps.put(userid, userPredicates);
        }
        
        Map<String, String> predicates;
        if (userPredicates.containsKey(botid))
        {
            predicates = userPredicates.get(botid);
        }
        else
        {
            predicates = new HashMap<String, String>();
            userPredicates.put(botid, predicates);
        }
        
        if (!predicates.containsKey(name))
        {
            throw new NoSuchPredicateException(name);
        }
        return predicates.get(name);
    }

    /**
     * Does nothing.
     */
    @Override
    @SuppressWarnings("unused")
    protected void savePredicates(Object predicates, String userid, String botid)
    {
        // Do nothing.
    }

    /**
     * @see org.aitools.programd.multiplexor.Multiplexor#useridCount(java.lang.String)
     */
    @Override
    @SuppressWarnings("unused")
    public int useridCount(String botid)
    {
        return 0;
    }
}
