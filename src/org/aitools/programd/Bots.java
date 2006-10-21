/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd;

import java.util.HashMap;


/**
 * Contains all descriptions of bots.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class Bots extends HashMap<String, Bot>
{

    /**
     * Returns any bot (probably the last one loaded).
     * 
     * @return any bot (probably the last one loaded)
     */
    public Bot getABot()
    {
        if (this.size() > 0)
        {
            return this.values().iterator().next();
        }
        return null;
    }

    /**
     * Returns a nicely-formatted list of the bots.
     * 
     * @return a nicely-formatted list of the bots
     */
    @Override
    public String toString()
    {
        if (this.size() == 0)
        {
            return "";
        }
        StringBuilder result = new StringBuilder();
        for (String botName : this.keySet())
        {
            if (result.length() > 0)
            {
                result.append(' ');
            }
            result.append(botName);
        }
        return result.toString();
    }

    /**
     * Returns whether any bots have loaded the given file(name).
     * 
     * @param filename
     * @return whether any bots have loaded the given file(name)
     */
    public boolean haveLoaded(String filename)
    {
        for (Bot bot : this.values())
        {
            if (bot.hasLoaded(filename))
            {
                return true;
            }
        }
        return false;
    }
}
