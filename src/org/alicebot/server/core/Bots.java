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

package org.alicebot.server.core;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.alicebot.server.core.util.DeveloperError;
import org.alicebot.server.core.util.Trace;


/**
 *  Contains all descriptions of bots.
 *
 *  @author Noel Bush
 *  @since 4.1.5
 */
public class Bots extends HashMap
{
    private static final Bots myself = new Bots();


    private Bots()
    {
        super();
    }


    public static boolean knowsBot(String botid)
    {
        return !(myself.get(botid) == null);
    }


    public static void addBot(String botid, Bot bot)
    {
        myself.put(botid, bot);
    }


    public static Bot getBot(String botid)
    {
        Bot wanted;
        try
        {
            wanted = (Bot)myself.get(botid);
        }
        catch (ClassCastException e)
        {
            throw new DeveloperError("Something other than a Bot stored in Bots!");
        }
        if (wanted == null)
        {
            throw new DeveloperError("Tried to get unknown bot \"" + botid + "\".");
        }
        return wanted;
    }


    public static Bot getFirstBot()
    {
        if (myself.size() > 0)
        {
            return (Bot)myself.values().iterator().next();
        }
        else
        {
            return null;
        }
    }


    public static int getCount()
    {
        return myself.size();
    }


    public static String getNiceList()
    {
        if (myself.size() == 0)
        {
            return "";
        }
        StringBuffer result = new StringBuffer();
        Iterator iterator = myself.keySet().iterator();
        while (iterator.hasNext())
        {
            if (result.length() > 0)
            {
                result.append(' ');
            }
            result.append((String)iterator.next());
        }
        return result.toString();
    }


    public static Set getIDs()
    {
        return myself.keySet();
    }


    public static Iterator keysIterator()
    {
        return myself.keySet().iterator();
    }
}