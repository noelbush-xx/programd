/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.predicates;

import java.util.Map;

import org.aitools.programd.Bots;
import org.aitools.programd.Core;
import org.aitools.programd.CoreSettings;
import org.aitools.util.xml.Characters;
import org.apache.log4j.Logger;

/**
 * <p>
 * Maintains predicate values for userids. Every public set and get method checks the size of the cache, and saves out
 * part of it if it has exceeded a configurable limit.
 * </p>
 * <p>
 * This currently has the defect that it doesn't choose intelligently which userids' predicates to cache (it should do
 * this for the ones who have not been heard from the longest). The HashMap that contains the predicates (keyed by
 * userid) makes no guarantees about order. :-(
 * </p>
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
abstract public class PredicateManager
{
    /** Maximum index of indexed predicates. */
    public static final int MAX_INDEX = 5;

    /** The number of predicate set operations to allow before saving predicates. */
    private int _flushSize;

    /** A counter for tracking the number of predicate set operations. */
    protected int _setCount = 0;

    /** The predicate empty default. */
    protected String _predicateEmptyDefault;

    /** The Core that owns this. */
    protected Core _core;

    /** The Bots object in use. */
    protected Bots _bots;

    /** The general Program D logger. */
    protected Logger _logger;

    /**
     * Creates a new PredicateMaster with the given Core as its owner.
     * 
     * @param core the Core that owns this PredicateMaster
     */
    public PredicateManager(Core core)
    {
        this._core = core;
        this._bots = this._core.getBots();
        CoreSettings coreSettings = this._core.getSettings();
        this._predicateEmptyDefault = coreSettings.getPredicateEmptyDefault();
        this._logger = Logger.getLogger("programd");
        this._flushSize = coreSettings.getPredicateFlushPeriod();
        initialize();
    }

    /**
     * Does whatever initialization is needed for the particular PredicateManager.
     */
    abstract public void initialize();

    /**
     * Sets a predicate <code>value</code> against a predicate <code>name</code> for a given userid, and returns
     * either the <code>name</code> or the <code>value</code>, depending on the predicate type.
     * 
     * @param name the predicate name
     * @param value the predicate value
     * @param userid the userid
     * @param botid
     * @return the <code>name</code> or the <code>value</code>, depending on the predicate type
     */
    public synchronized String set(String name, String value, String userid, String botid)
    {
        // Get existing or new predicates map for userid.
        PredicateMap predicates = this._bots.get(botid).predicatesFor(userid);

        // Put the new value into the predicate.
        predicates.put(name, new PredicateValue(value));

        // Increment the set count.
        this._setCount++;

        // Flush if necessary.
        flushIfNecessary();

        // Return the name or value.
        return nameOrValue(name, value, botid);
    }

    /**
     * Sets a <code>value</code> of an indexed predicate <code>name</code> for a given <code>userid</code>, and
     * returns either the <code>name</code> or the <code>value</code>, depending on the predicate type.
     * 
     * @param name the predicate name
     * @param index the index at which to set the value
     * @param valueToSet the predicate value to set
     * @param userid the userid
     * @param botid
     * @return the <code>name</code> or the <code>value</code>, depending on the predicate type
     */
    public synchronized String set(String name, int index, String valueToSet, String userid, String botid)
    {
        // Get existing or new predicates map for userid.
        PredicateMap predicates = this._bots.get(botid).predicatesFor(userid);

        // Get, load or create the list of values.
        PredicateValue value = getLoadOrCreateMultivaluedPredicate(name, predicates, userid, botid);

        // Try to set the predicate value at the index.
        value.add(index, valueToSet);

        // Increment the set count.
        this._setCount++;

        // Flush if necessary.
        flushIfNecessary();

        // Return the name or value.
        return nameOrValue(name, valueToSet, botid);
    }

    /**
     * Pushes a new <code>value</code> onto an indexed predicate <code>name</code> for a given <code>userid</code>,
     * and returns either the <code>name</code> or the <code>value</code>, depending on the predicate type.
     * 
     * @param name the predicate name
     * @param newValue the new predicate value
     * @param userid the userid
     * @param botid
     * @return the <code>name</code> or the <code>value</code>, depending on the predicate type
     */
    public synchronized String push(String name, String newValue, String userid, String botid)
    {
        // Get existing or new predicates map for userid.
        PredicateMap userPredicates = this._bots.get(botid).predicatesFor(userid);

        // Get, load or create the list of values.
        PredicateValue value = getLoadOrCreateMultivaluedPredicate(name, userPredicates, userid, botid);

        // Push the new value onto the indexed predicate list.
        value.push(Characters.removeMarkup(newValue));

        // Increment the set count.
        this._setCount++;

        // Flush if necessary.
        flushIfNecessary();

        // Return the name or value.
        return nameOrValue(name, newValue, botid);
    }

    /**
     * Gets the predicate <code>value</code> associated with a <code>name</code> for a given <code>userid</code>.
     * 
     * @param name the predicate name
     * @param userid the userid
     * @param botid
     * @return the <code>value</code> associated with the given <code>name</code>, for the given
     *         <code>userid</code>
     */
    public synchronized String get(String name, String userid, String botid)
    {
        // Get existing or new predicates map for userid.
        PredicateMap predicates = this._bots.get(botid).predicatesFor(userid);

        // Try to get the predicate value from the cache.
        if (predicates.containsKey(name))
        {
            return predicates.get(name).getFirstValue();
        }
        // otherwise...
        if (this._logger.isDebugEnabled())
        {
            this._logger.debug(String.format("Predicate \"%s\" is not cached.", name));
        }
        String loadedValue;
        try
        {
            loadedValue = loadPredicate(name, userid, botid);
            if (this._logger.isDebugEnabled())
            {
                this._logger.debug(String.format("Successfully loaded predicate \"%s\".", name));
            }
        }
        catch (NoSuchPredicateException e)
        {
            // If not found, set and cache the best available default.
            if (this._logger.isDebugEnabled())
            {
                this._logger.debug(String.format("Could not load predicate \"%s\"; setting to best available default.",
                        name));
            }
            loadedValue = bestAvailableDefault(name, botid);
        }

        // Cache it.
        predicates.put(name, new PredicateValue(loadedValue));

        // Return the loaded value.
        return loadedValue;
    }

    /**
     * Gets the predicate <code>value</code> associated with a <code>name</code> at a given <code>index</code> for
     * a given <code>userid</code>.
     * 
     * @param name the predicate name
     * @param index the index
     * @param userid the userid
     * @param botid
     * @return the <code>value</code> associated with the given <code>name</code> at the given <code>index</code>,
     *         for the given <code>userid</code>
     */
    @SuppressWarnings("boxing")
    public synchronized String get(String name, int index, String userid, String botid)
    {
        // Get existing or new predicates map for userid.
        PredicateMap predicates = this._bots.get(botid).predicatesFor(userid);

        String result = null;

        // Get the list of values.
        PredicateValue value = null;
        if (!predicates.containsKey(name))
        {
            // No values cached; try loading.
            if (this._logger.isDebugEnabled())
            {
                this._logger.debug(String.format("Predicate \"%s\" is not cached; attempting to load.", name));
            }
            try
            {
                value = loadMultivaluedPredicate(name, predicates, userid, botid);
                if (this._logger.isDebugEnabled())
                {
                    this._logger.debug(String.format("Successfully loaded predicate \"%s\".", name));
                }
                predicates.put(name, value);
            }
            catch (NoSuchPredicateException e)
            {
                // Still no list, so set and cache default.
                if (this._logger.isDebugEnabled())
                {
                    this._logger.debug(String.format(
                            "Could not load predicate \"%s\"; setting to best available default.", name));
                }
                result = bestAvailableDefault(name, botid);
                predicates.put(name, result);
            }
        }
        else
        {
            try
            {
                value = getMultivaluedPredicateValue(name, predicates);
                if (this._logger.isDebugEnabled())
                {
                    this._logger.debug(String.format(
                            "Successfully retrieved multi-valued predicate \"%s\" from cache.", name));
                }
            }
            catch (NoSuchPredicateException e)
            {
                assert false : "predicates.containsKey(name) but getMultivaluedPredicateValue(name, predicates) throws NoSuchPredicateException!";
            }
        }

        if (value != null)
        {
            // The index may be invalid.
            try
            {
                // Get the value at index.
                result = value.get(index);
            }
            catch (IndexOutOfBoundsException e)
            {
                try
                {
                    value = loadMultivaluedPredicate(name, predicates, userid, botid);
                    if (this._logger.isDebugEnabled())
                    {
                        this._logger.debug(String.format("Successfully loaded predicate \"%s\".", name));
                    }
                    predicates.put(name, value);
                }
                catch (NoSuchPredicateException ee)
                {
                    assert false;
                }
                try
                {
                    // Get the value at index.
                    result = value.get(index);
                }
                catch (IndexOutOfBoundsException ee)
                {
                    // Return the best available default.
                    result = bestAvailableDefault(name, botid);
                    this._logger
                            .warn(String
                                    .format(
                                            "Index %d not available for predicate \"%s\" (user \"%s\", bot \"%s\").  Returning best available default.",
                                            index, name, userid, botid));
                }
            }
        }

        // Return the value.
        return result;
    }

    /**
     * Returns, from the cache, an ArrayList of values assigned to a <code>name</code> for a predicate for a
     * <code>userid</code>. If the <code>name</code> exists in a predicate for the <code>userid</code> but it is
     * not indexed, it is converted into an indexed value. If it does not exist at all, a
     * <code>NoSuchPredicateException</code> is thrown.
     * 
     * @param name the name of the predicate
     * @param predicates the existing map of predicates
     * @return a list of values assigned to a <code>name</code> for a predicate for a <code>userid</code>
     * @throws NoSuchPredicateException if no values are assigned to the <code>name</code>
     */
    protected static PredicateValue getMultivaluedPredicateValue(String name, PredicateMap predicates)
            throws NoSuchPredicateException
    {
        if (predicates.size() > 0 && predicates.containsKey(name))
        {
            return predicates.get(name).becomeMultiValued();
        }
        // If the predicate is not found, throw an exception.
        throw new NoSuchPredicateException(name);

    }

    /**
     * Tries to load a predicate with <code>name</code> for <code>userid</code> from the Multiplexor into the
     * <code>predicates</code>. If successful, tries to get the value list for name. If unsuccessful, throws a
     * NoSuchPredicateException.
     * 
     * @param name the predicate <code>name</code>
     * @param predicates the user predicates (must not be null!)
     * @param userid the userid
     * @param botid
     * @return an ArrayList of values assigned to a <code>name</code> for a predicate for a <code>userid</code>
     * @throws NoSuchPredicateException if no values are assigned to the <code>name</code>
     * @throws NullPointerException if <code>userPredicates</code> is null
     */
    @SuppressWarnings("boxing")
    protected PredicateValue loadMultivaluedPredicate(String name, PredicateMap predicates, String userid, String botid)
            throws NoSuchPredicateException, NullPointerException
    {
        // Prevent this from being called with a null predicates map.
        if (predicates == null)
        {
            throw new NullPointerException("Cannot call loadMultivaluedPredicate with null predicates!");
        }

        // Try to load the predicate as an indexed predicate.
        int index = 1;
        String loadedValue;
        try
        {
            loadedValue = loadPredicate(String.format("%s.%d", name, index), userid, botid);
        }
        catch (NoSuchPredicateException e)
        {
            throw new NoSuchPredicateException(name);
        }

        // If this succeeded, get/create the new values list in the predicates.
        PredicateValue value = predicates.get(name);
        if (value == null)
        {
            value = new PredicateValue(loadedValue);
            predicates.put(name, value);
        }
        else
        {
            value.add(1, loadedValue);
        }

        // Now load as many more as possible up to MAX_INDEX.
        for (index = 2; index <= MAX_INDEX; index++)
        {
            try
            {
                value.add(index, loadPredicate(String.format("%s.%d", name, index), userid, botid));
            }
            catch (NoSuchPredicateException e)
            {
                if (this._logger.isDebugEnabled())
                {
                    this._logger.debug(String.format("Exceeded maximum index for \"%s\" with %d.", name, index));
                }
                break;
            }
        }

        return value;
    }

    /**
     * Returns a value list one way or another: first tries to get it from the cache, then tries to load it from the
     * ActiveMultiplexor; finally creates a new one if the preceding failed.
     * 
     * @param name the predicate <code>name</code>
     * @param predicates the user predicates map
     * @param userid the userid for which to return the value list
     * @param botid the botid for which to return the value list
     * @return a multi-valued <code>PredicateValue</code> from <code>userPredicates</code> for <code>name</code>
     *         for <code>userid</code>
     */
    protected PredicateValue getLoadOrCreateMultivaluedPredicate(String name, PredicateMap predicates, String userid,
            String botid)
    {
        PredicateValue value = null;
        if (!predicates.containsKey(name))
        {
            // No list found in cache; try load.
            if (this._logger.isDebugEnabled())
            {
                this._logger.debug(String.format("Predicate \"%s\" not cached; attempting to load.", name));
            }
            try
            {
                value = loadMultivaluedPredicate(name, predicates, userid, botid);
                if (this._logger.isDebugEnabled())
                {
                    this._logger.debug(String.format("Successfully loaded predicate \"%s\".", name));
                }
                predicates.put(name, value);
            }
            catch (NoSuchPredicateException ee)
            {
                // Still no list, so create new one.
                if (this._logger.isDebugEnabled())
                {
                    this._logger.debug(String.format(
                            "Could not load predicate \"%s\"; setting to best available default.", name));
                }
                value = new PredicateValue(this._predicateEmptyDefault).becomeMultiValued();
                predicates.put(name, value);
            }
        }
        else
        {
            try
            {
                value = getMultivaluedPredicateValue(name, predicates);
            }
            catch (NoSuchPredicateException e)
            {
                assert false : "predicates.containsKey(name) but getMultivaluedPredicateValue(name, predicates) throws NoSuchPredicateException!";
            }
        }
        return value;
    }

    /**
     * Returns the best available default predicate <code>value</code> for a predicate <code>name</code>
     * 
     * @param name the predicate name
     * @param botid
     * @return the best available default predicate
     */
    protected String bestAvailableDefault(String name, String botid)
    {
        Map<String, PredicateInfo> predicatesInfo = this._bots.get(botid).getPredicatesInfo();

        // There may be an individual default defined.
        if (predicatesInfo.containsKey(name))
        {
            return predicatesInfo.get(name).getDefaultValue();
        }
        // If not, return the global empty default.
        if (this._logger.isDebugEnabled())
        {
            this._logger.debug(String.format(
                    "No default value available for \"%s\"; returning predicate empty default.", name));
        }
        return this._predicateEmptyDefault;
    }

    /**
     * Returns the name or value of a predicate, depending on whether or not it is &quot;return-name-when-set&quot;.
     * 
     * @param name the predicate name
     * @param value the predicate value
     * @param botid
     * @return the appropriate result (name or value depending on predicate settings)
     */
    protected String nameOrValue(String name, String value, String botid)
    {
        Map<String, PredicateInfo> predicatesInfo = this._bots.get(botid).getPredicatesInfo();

        // Check if any info is known about this predicate.
        if (predicatesInfo.containsKey(name))
        {
            // If so, find out whether to return its name or the value.
            if ((predicatesInfo.get(name)).returnNameWhenSet())
            {
                return name;
            }
        }
        return value;
    }

    /**
     * Checks the predicate cache, and saves out predicates if necessary.
     */
    @SuppressWarnings("boxing")
    protected void flushIfNecessary()
    {
        // See if we have exceeded the cacheMax.
        if (this._setCount > this._flushSize)
        {
            if (this._logger.isDebugEnabled())
            {
                this._logger.debug(String
                        .format("Set count %d exceeds flush size %d.", this._setCount, this._flushSize));
            }
            saveAll();
        }
    }

    /**
     * Saves all predicates and empties the caches.
     */
    public void saveAll()
    {
        if (this._logger.isDebugEnabled())
        {
            this._logger.debug("Saving all predicates.");
        }
        dumpPredicates();
        this._setCount = 0;
    }

    /**
     * Loads a predicate for a given user/bot combination.
     * 
     * @param name predicate name
     * @param user user identifier
     * @param bot bot identifier
     * @return the predicate value
     * @throws NoSuchPredicateException if there is no predicate with this name
     */
    abstract protected String loadPredicate(String name, String user, String bot) throws NoSuchPredicateException;

    /**
     * Saves all predicates and removes them from memory.
     */
    abstract protected void dumpPredicates();
}
