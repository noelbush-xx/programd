/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.bot;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.aitools.programd.util.DeveloperError;

/**
 * Contains all descriptions of bots.
 * 
 * @author Noel Bush
 * @since 4.1.5
 * @version 4.2
 */
public class Bots
{
    /** The private HashMap that is used by this. */
    private HashMap<String, Bot> hashmap;

    /**
     * <code>Bots</code> cannot be instantiated except by itself.
     */
    public Bots()
    {
        this.hashmap = new HashMap<String, Bot>();
    } 

    /**
     * Returns whether the loaded bots include one with the given id.
     * 
     * @return whether the loaded bots include one with the given id
     */
    public boolean include(String botid)
    {
        return !(this.hashmap.get(botid) == null);
    }

    /**
     * Adds the given bot with the given id. No check is made to see whether a
     * bot is already loaded with the given id!
     * 
     * @param botid
     *            the id to use for the bot
     * @param bot
     *            the bot to add
     */
    public void addBot(String botid, Bot bot)
    {
        this.hashmap.put(botid, bot);
    } 

    /**
     * Returns the bot with the given id.
     * 
     * @param botid
     *            the id of the bot to return
     * @return the bot with the given id
     */
    public Bot getBot(String botid)
    {
        Bot wanted;
        try
        {
            wanted = this.hashmap.get(botid);
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

    /**
     * Returns any bot (probably the last one loaded).
     * 
     * @return any bot (probably the last one loaded)
     */
    public Bot getABot()
    {
        if (this.hashmap.size() > 0)
        {
            return this.hashmap.values().iterator().next();
        } 
        // (otherwise...)
        return null;
    } 

    /**
     * Returns the number of bots (the size)
     * 
     * @return the number of bots (the size)
     */
    public int getCount()
    {
        return this.hashmap.size();
    } 

    /**
     * Returns a nicely-formatted list of the bots.
     * 
     * @return a nicely-formatted list of the bots
     */
    public String getNiceList()
    {
        if (this.hashmap.size() == 0)
        {
            return "";
        } 
        StringBuffer result = new StringBuffer();
        Iterator iterator = this.hashmap.keySet().iterator();
        while (iterator.hasNext())
        {
            if (result.length() > 0)
            {
                result.append(' ');
            } 
            result.append((String) iterator.next());
        } 
        return result.toString();
    } 

    /**
     * Returns the IDs (the key set)
     * 
     * @return the IDs (the key set)
     */
    public Set<String> getIDs()
    {
        return this.hashmap.keySet();
    } 

    /**
     * Returns an iterator over the key set
     * 
     * @return an iterator over the key set
     */
    public Iterator keysIterator()
    {
        return this.hashmap.keySet().iterator();
    } 

    /**
     * Returns whether any bots have loaded the given file(name).
     * 
     * @param filename
     * @return whether any bots have loaded the given file(name)
     */
    public boolean haveLoaded(String filename)
    {
        Iterator bots = this.hashmap.values().iterator();
        while (bots.hasNext())
        {
            if (((Bot) bots.next()).hasLoaded(filename))
            {
                return true;
            } 
        } 
        return false;
    } 
}