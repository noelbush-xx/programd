/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.graph;

import java.net.URL;

import org.aitools.programd.Bot;
import org.aitools.programd.util.NoMatchException;

/**
 * A <code>Graphmapper</code> is responsible for managing categories.
 * It can {@link #addCategory add} categories, {@link #removeCategory remove} categories,
 * or {@link #match} categories.  The interface is implementation-
 * independent.  There is a memory-based implementation in {@link MemoryGraphmapper},
 * which loads categories and builds a tree in memory.  There is also
 * a database-based implementation in DBGraphmapper, which should
 * scale much better.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public interface Graphmapper
{
    /**
     * Loads the <code>Graphmapper</code> with the AIML file
     * to be found at <code>path</code>.
     * 
     * @param path path to the file(s) to load
     * @param botid
     */
    public void load(URL path, String botid);

    /**
     * Adds a new category to the <code>Graphmapper</code>.
     * 
     * @param pattern the category's <code>pattern</code>
     * @param that the category's <code>that</code>
     * @param topic the category's <code>topic</code>
     * @param template the category's <code>template</code>
     * @param bot the bot for whom the category is being added
     * @param source the path from which the category comes
     */
    public void addCategory(String pattern, String that, String topic, String template, Bot bot, URL source);
    
    /**
     * Removes all categories associated with a given filename, and removes the file
     * from the list of loaded files.
     * 
     * @param path the filename
     * @param bot the bot for whom to remove the given path
     */
    abstract public void unload(URL path, Bot bot);
    
    /**
     * Removes a category from the <code>Graphmapper</code>.
     * 
     * @param pattern the category's <code>pattern</code>
     * @param that the category's <code>that</code>
     * @param topic the category's <code>topic</code>
     * @param bot the bot for whom the category is being removed
     */
    public void removeCategory(String pattern, String that, String topic, Bot bot);
    
    /**
     * Searches for a match in the <code>Graphmapper</code> to a given path.
     * 
     * @param input &lt;input/&gt; path component
     * @param that &lt;that/&gt; path component
     * @param topic &lt;topic/&gt; path component
     * @param botid &lt;botid/&gt; path component
     * @return the resulting <code>Match</code> object
     * @throws NoMatchException
     *             if no match was found
     */
    public Match match(String input, String that, String topic, String botid) throws NoMatchException;

    /**
     * Returns the number of categories presently loaded.
     * 
     * @return the number of categories presently loaded
     */
    public int getCategoryCount();

    /**
     * Returns a string reporting the current number of total categories
     * 
     * @return a string reporting category information
     */
    public String getCategoryReport();

    /**
     * Returns the number of path-identical categories encountered.
     * 
     * @return the number of path-identical categories encountered
     */
    public int getDuplicateCategoryCount();
    
    /**
     * Prints the entire contents of the graph to the given filename.
     * 
     * @param path the filename to which to print
     */
    public void print(String path);
}
