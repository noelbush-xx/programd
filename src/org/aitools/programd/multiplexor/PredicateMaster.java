/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.multiplexor;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.aitools.programd.Core;
import org.aitools.programd.CoreSettings;
import org.aitools.programd.bot.Bot;
import org.aitools.programd.bot.Bots;
import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.IllegalObjectStateException;

/**
 * <p>
 * Maintains in-memory predicate values for userids. Every public set and get
 * method checks the size of the cache, and saves out part of it if it has
 * exceeded a configurable limit.
 * </p>
 * <p>
 * This currently has the defect that it doesn't choose intelligently which
 * userids' predicates to cache (it should do this for the ones who have not
 * been heard from the longest). The HashMap that contains the predicates (keyed
 * by userid) makes no guarantees about order. :-(
 * </p>
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.1.4'
 */
public class PredicateMaster
{
    /** Maximum index of indexed predicates. */
    private static final int MAX_INDEX = 5;

    /** The maximum size of the cache. */
    private int cacheMax;

    /**
     * The preferred minimum value for the cache (starts at half of
     * {@link #cacheMax}, may be adjusted).
     */
    private int cacheMin;

    /** A counter for tracking the number of predicate value cache operations. */
    protected static int cacheSize = 0;

    /** The predicate empty default. */
    protected String predicateEmptyDefault;

    /** The Core that owns this. */
    protected Core core;

    /** The Multiplexor in use. */
    protected Multiplexor multiplexor;

    /** The Bots object in use. */
    protected Bots bots;

    /** The general Program D logger. */
    protected Logger logger;

    /**
     * Creates a new PredicateMaster with the given Core as its owner.
     * 
     * @param coreOwner the Core that owns this PredicateMaster
     */
    public PredicateMaster(Core coreOwner)
    {
        this.core = coreOwner;
        this.bots = this.core.getBots();
        CoreSettings coreSettings = this.core.getSettings();
        this.multiplexor = this.core.getMultiplexor();
        this.predicateEmptyDefault = coreSettings.getPredicateEmptyDefault();
        this.logger = Logger.getLogger("programd");
        this.cacheMax = coreSettings.getPredicateCacheMax();
        this.cacheMin = Math.max(this.cacheMax / 2, 1);
    }

    /**
     * Sets a predicate <code>value</code> against a predicate
     * <code>name</code> for a given userid, and returns either the
     * <code>name</code> or the <code>value</code>, depending on the
     * predicate type.
     * 
     * @param name the predicate name
     * @param value the predicate value
     * @param userid the userid
     * @param botid
     * @return the <code>name</code> or the <code>value</code>, depending
     *         on the predicate type
     */
    public String set(String name, String value, String userid, String botid)
    {
        // Get existing or new predicates map for userid.
        Map<String, Object> userPredicates = this.bots.getBot(botid).predicatesFor(userid);

        // Put the new value into the predicate.
        userPredicates.put(name, value);

        // Increment the cache count.
        cacheSize++;

        // Check the cache.
        checkCache();

        // Return the name or value.
        return nameOrValue(name, value, botid);
    }

    /**
     * Sets a <code>value</code> against an indexed predicate
     * <code>name</code> for a given <code>userid</code>, and returns
     * either the <code>name</code> or the <code>value</code>, depending on
     * the predicate type.
     * 
     * @param name the predicate name
     * @param index the index
     * @param value the predicate value
     * @param userid the userid
     * @param botid
     * @return the <code>name</code> or the <code>value</code>, depending
     *         on the predicate type
     */
    public synchronized String set(String name, int index, String value, String userid, String botid)
    {
        // Get existing or new predicates map for userid.
        Map<String, Object> userPredicates = this.bots.getBot(botid).predicatesFor(userid);

        // Get, load or create the list of values.
        ArrayList<String> values = getLoadOrCreateValueList(name, userPredicates, userid, botid);

        // Try to set the predicate value at the index (minus one -- no 0 in
        // AIML index).
        try
        {
            values.add(index - 1, value);
        }
        catch (IndexOutOfBoundsException e)
        {
            // If failed, push the value onto the front of the list.
            pushOnto(values, value);
        }

        // Increment the cache count.
        cacheSize++;

        // Check the cache.
        checkCache();

        // Return the name or value.
        return nameOrValue(name, value, botid);
    }

    /**
     * Pushes a <code>value</code> onto an indexed predicate <code>name</code>
     * for a given <code>userid</code>, and returns either the
     * <code>name</code> or the <code>value</code>, depending on the
     * predicate type.
     * 
     * @param name the predicate name
     * @param value the predicate value
     * @param userid the userid
     * @param botid
     * @return the <code>name</code> or the <code>value</code>, depending
     *         on the predicate type
     */
    public synchronized String push(String name, String value, String userid, String botid)
    {
        // Get existing or new predicates map for userid.
        Map<String, Object> userPredicates = this.bots.getBot(botid).predicatesFor(userid);

        // Get, load or create the list of values.
        ArrayList<String> values = getLoadOrCreateValueList(name, userPredicates, userid, botid);

        // Push the new value onto the indexed predicate list.
        pushOnto(values, value);

        // Increment the cache count.
        cacheSize++;

        // Check the cache.
        checkCache();

        // Return the name or value.
        return nameOrValue(name, value, botid);
    }

    /**
     * Gets the predicate <code>value</code> associated with a
     * <code>name</code> for a given <code>userid</code>.
     * 
     * @param name the predicate name
     * @param userid the userid
     * @param botid
     * @return the <code>value</code> associated with the given
     *         <code>name</code>, for the given <code>userid</code>
     */
    public synchronized String get(String name, String userid, String botid)
    {
        String value;

        // Get existing or new predicates map for userid.
        Map<String, Object> userPredicates = this.bots.getBot(botid).predicatesFor(userid);

        // Try to get the predicate value from the cache.
        Object valueObject = userPredicates.get(name);

        // If it is null, try loading the predicate.
        if (valueObject == null)
        {
            try
            {
                value = this.multiplexor.loadPredicate(name, userid, botid);
            }
            catch (NoSuchPredicateException e0)
            {
                // If not found, set and cache the best available default.
                value = bestAvailableDefault(name, botid);
            }

            // Check that the result is not null.
            if (value != null)
            {
                // Cache it.
                userPredicates.put(name, value);
            }
            else
            {
                // This should never, ever happen!
                throw new DeveloperError("Null string found in user predicates cache!", new NullPointerException());
            }

        }

        // Determine whether this is an indexed or unindexed predicate.
        else if (valueObject instanceof String)
        {
            // This is an unindexed predicate.
            value = (String) valueObject;
        }
        else if (valueObject instanceof ArrayList)
        {
            // This is an indexed predicate.
            ArrayList values = (ArrayList) valueObject;
            value = (String) values.get(0);
        }
        else
        {
            // This should never, ever happen!
            throw new DeveloperError(new IllegalObjectStateException("Something other than a String or a ArrayList found in user predicates cache!"));
        }

        // Check the cache.
        checkCache();

        // Cache and return the value.
        return value;
    }

    /**
     * Gets the predicate <code>value</code> associated with a
     * <code>name</code> at a given <code>index</code> for a given
     * <code>userid</code>.
     * 
     * @param name the predicate name
     * @param index the index
     * @param userid the userid
     * @param botid
     * @return the <code>value</code> associated with the given
     *         <code>name</code> at the given <code>index</code>, for the
     *         given <code>userid</code>
     */
    public synchronized String get(String name, int index, String userid, String botid)
    {
        // Get existing or new predicates map for userid.
        Map<String, Object> userPredicates = this.bots.getBot(botid).predicatesFor(userid);

        String value = null;

        // Get the list of values.
        ArrayList values = null;

        try
        {
            values = getValueList(name, userPredicates);
        }
        catch (NoSuchPredicateException e0)
        {
            // No values cached; try loading.
            try
            {
                values = loadValueList(name, userPredicates, userid, botid);
            }
            catch (NoSuchPredicateException e1)
            {
                // Still no list, so set and cache default.
                value = bestAvailableDefault(name, botid);
                userPredicates.put(name, value);
            }
        }

        if (values != null)
        {
            // The index may be invalid.
            try
            {
                // Get the value at index (minus one -- no zero index in AIML).
                value = (String) values.get(index - 1);
            }
            catch (IndexOutOfBoundsException e)
            {
                // Return the best available default.
                value = bestAvailableDefault(name, botid);
            }
        }

        // Check the cache.
        checkCache();

        // Return the value.
        return value;
    }

    /**
     * Pushes a value onto the front of a list, and pops off any values at the
     * end of the list so that the list size is no more than {@link #MAX_INDEX}.
     * 
     * @param values the list onto which to push
     * @param value the value to push onto the list
     */
    private static void pushOnto(ArrayList<String> values, String value)
    {
        values.add(0, value);
        while (values.size() > MAX_INDEX)
        {
            values.remove(values.size() - 1);
        }
    }

    /**
     * Returns, from the cache, an ArrayList of values assigned to a
     * <code>name</code> for a predicate for a <code>userid</code>. If the
     * <code>name</code> exists in a predicate for the <code>userid</code>
     * but it is not indexed, it is converted into an indexed value. If it does
     * not exist at all, a <code>NoSuchPredicateException</code> is thrown.
     * 
     * @param name the name of the predicate
     * @param userPredicates the existing map of predicates
     * @return a list of values assigned to a <code>name</code> for a
     *         predicate for a <code>userid</code>
     * @throws NoSuchPredicateException if no values are assigned to the
     *             <code>name</code>
     */
    private static ArrayList<String> getValueList(String name, Map<String, Object> userPredicates) throws NoSuchPredicateException
    {
        if (userPredicates.size() > 0 && userPredicates.containsKey(name))
        {
            Object valueObject = userPredicates.get(name);
            if (valueObject != null)
            {
                if (valueObject instanceof String)
                {
                    ArrayList<String> values = createValueList(name, userPredicates);
                    values.add((String) valueObject);
                    return values;
                }
                else if (valueObject instanceof ArrayList)
                {
                    return (ArrayList<String>) valueObject;
                }
                // This should never happen!
                throw new DeveloperError(new IllegalObjectStateException("Something other than a String or ArrayList found in predicates!"));
            }
            // This should never, ever happen!
            throw new DeveloperError("Null String found as value in predicates!", new NullPointerException());
        }
        // If the predicate is not found, throw an exception.
        throw new NoSuchPredicateException(name);

    }

    /**
     * Tries to load a predicate with <code>name</code> for
     * <code>userid</code> from the ActiveMultiplexor into the
     * <code>userPredicates</code>. If successful, tries to get the value
     * list for name. If unsuccessful, throws a NoSuchPredicateException.
     * 
     * @param name the predicate <code>name</code>
     * @param userPredicates the user predicates (must not be null!)
     * @param userid the userid
     * @param botid
     * @return an ArrayList of values assigned to a <code>name</code> for a
     *         predicate for a <code>userid</code>
     * @throws NoSuchPredicateException if no values are assigned to the
     *             <code>name</code>
     * @throws NullPointerException if <code>userPredicates</code> is null
     */
    private ArrayList<String> loadValueList(String name, Map<String, Object> userPredicates, String userid, String botid)
            throws NoSuchPredicateException, NullPointerException
    {
        // Prevent this from being called with a null predicates map.
        if (userPredicates == null)
        {
            throw new NullPointerException("Cannot call loadValueList with null userPredicates!");
        }

        // Try to load the predicate as an indexed predicate.
        int index = 1;
        String value;
        try
        {
            value = this.multiplexor.loadPredicate(name + '.' + index, userid, botid);
        }
        catch (NoSuchPredicateException e0)
        {
            throw new NoSuchPredicateException(name);
        }

        // If this succeeded, create the new values list in the predicates.
        ArrayList<String> values = createValueList(name, userPredicates);

        // Add the first value that was found.
        values.add(value);

        // Now load as many more as possible up to MAX_INDEX.
        try
        {
            // This will either hit the limit, or throw an exception.
            while (index <= MAX_INDEX)
            {
                index++;
                values.add(this.multiplexor.loadPredicate(name + '.' + index, userid, botid));
            }
        }
        catch (NoSuchPredicateException e1)
        {
            // Do nothing if the exception is thrown; that's fine (there is at
            // least one).
        }

        return values;
    }

    /**
     * Creates a value list for the given predicate <code>name</code> and
     * given <code>userid</code>.
     * 
     * @param name the predicate name
     * @param userPredicates the predicates map to which to add the list
     * @return the new list
     */
    private static ArrayList<String> createValueList(String name, Map<String, Object> userPredicates)
    {
        // Create the new list.
        ArrayList<String> values = new ArrayList<String>();
        userPredicates.put(name, values);
        return values;
    }

    /**
     * Returns a value list one way or another: first tries to get it from the
     * cache, then tries to load it from the ActiveMultiplexor; finally creates
     * a new one if the preceding failed.
     * 
     * @param name the predicate <code>name</code>
     * @param userPredicates the user predicates map
     * @param userid the userid for which to return the value list
     * @param botid the botid for which to return the value list
     * @return a value list in <code>userPredicates</code> for
     *         <code>name</code> for <code>userid</code>
     */
    private ArrayList<String> getLoadOrCreateValueList(String name, Map<String, Object> userPredicates, String userid, String botid)
    {
        ArrayList<String> values;

        try
        {
            values = getValueList(name, userPredicates);
        }
        catch (NoSuchPredicateException e0)
        {
            // No list found in cache; try load.
            try
            {
                values = loadValueList(name, userPredicates, userid, botid);
            }
            catch (NoSuchPredicateException e1)
            {
                // Still no list, so create new one.
                values = createValueList(name, userPredicates);
            }
        }
        return values;
    }

    /**
     * Returns the best available default predicate <code>value</code> for a
     * predicate <code>name</code>
     * 
     * @param name the predicate name
     * @param botid
     * @return the best available default predicate
     */
    private String bestAvailableDefault(String name, String botid)
    {
        Map predicatesInfo = this.bots.getBot(botid).getPredicatesInfo();

        // There may be an individual default defined.
        if (predicatesInfo.containsKey(name))
        {
            String value = ((PredicateInfo) predicatesInfo.get(name)).defaultValue;
            if (value != null)
            {
                return value;
            }
        }
        // If not, return the global empty default.
        return this.predicateEmptyDefault;
    }

    /**
     * Returns the name or value of a predicate, depending on whether or not it
     * is &quot;return-name-when-set&quot;.
     * 
     * @param name the predicate name
     * @param value the predicate value
     * @param botid
     * @return the appropriate result (name or value depending on predicate
     *         settings)
     */
    private String nameOrValue(String name, String value, String botid)
    {
        Map predicatesInfo = this.bots.getBot(botid).getPredicatesInfo();

        // Check if any info is known about this predicate.
        if (predicatesInfo.containsKey(name))
        {
            // If so, find out whether to return its name or the value.
            if (((PredicateInfo) predicatesInfo.get(name)).returnNameWhenSet)
            {
                return name;
            }
        }
        return value;
    }

    /**
     * Attempts to dump a given number of predicate values from the cache,
     * starting with the oldest userid first.
     * 
     * @param dumpCount the number of values to try to dump
     * @return the number that were actually dumped
     */
    private int save(int dumpCount)
    {
        int saveCount = 0;

        Iterator botsIterator = this.bots.keysIterator();

        while (botsIterator.hasNext() && saveCount < dumpCount)
        {
            String botid = (String) botsIterator.next();
            Bot bot = this.bots.getBot(botid);
            Map<String, Map<String, Object>> cache = bot.getPredicateCache();

            if (!cache.isEmpty())
            {
                Iterator userids = cache.keySet().iterator();
                while (userids.hasNext() && saveCount < dumpCount)
                {
                    // Get a userid.
                    String userid = null;
                    try
                    {
                        userid = (String) userids.next();
                    }
                    catch (ConcurrentModificationException e)
                    {
                        throw new DeveloperError("Some problem with PredicateMaster design: ConcurrentModificationException in save() [1].", e);
                    }

                    // Get the cached predicates for this user.
                    Map<String, Object> userPredicates = cache.get(userid);

                    // Iterate over all cached predicates and save them.
                    for (String name : userPredicates.keySet())
                    {
                        Object valueObject = userPredicates.get(name);

                        // Save single-valued predicates.
                        if (valueObject instanceof String)
                        {
                            String value = (String) userPredicates.get(name);

                            // Do not save default values.
                            if (!value.equals(bestAvailableDefault(name, botid)))
                            {
                                this.multiplexor.savePredicate(name, value, userid, botid);
                                saveCount++;
                            }
                        }
                        // Save indexed predicates.
                        else if (valueObject instanceof ArrayList)
                        {
                            // Try to get this as an indexed predicate.
                            ArrayList values = (ArrayList) userPredicates.get(name);

                            int valueCount = values.size();

                            for (int index = valueCount; --index > 0;)
                            {
                                // Do not save default values.
                                String value = (String) values.get(index - 1);
                                if (!value.equals(bestAvailableDefault(name, botid)))
                                {
                                    this.multiplexor.savePredicate(name + '.' + index, value, userid, botid);
                                    saveCount++;
                                }
                            }
                        }
                        else
                        {
                            // This should never, ever happen.
                            throw new DeveloperError(new IllegalObjectStateException(
                                    "Something other than a String or ArrayList found in predicates!"));
                        }
                    }

                    // Remove the userid from the cache.
                    try
                    {
                        userids.remove();
                    }
                    catch (ConcurrentModificationException e)
                    {
                        throw new DeveloperError("Some problem with PredicateMaster design: ConcurrentModificationException in save() [2].", e);
                    }
                }
            }
        }

        cacheSize -= saveCount;

        // Return the cacheSize now.
        return cacheSize;
    }

    /**
     * Dumps the entire cache.
     */
    public void saveAll()
    {
        this.logger.log(Level.INFO, "PredicateMaster saving all cached predicates (" + cacheSize + ")");
        save(cacheSize);
    }

    /**
     * Checks the predicate cache, and saves out predicates if necessary.
     */
    private void checkCache()
    {
        // See if we have exceeded or reached the cacheMax.
        if (cacheSize >= this.cacheMax)
        {
            // Remove at least enough so that cacheMin is reached.
            int resultSize = save((cacheSize - this.cacheMin));

            // Adjust cacheMin upward if this removed too many.
            if (resultSize < this.cacheMin)
            {
                this.cacheMin = (resultSize + this.cacheMin) / 2;
            }
        }
    }
}