/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.multiplexor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.aitools.programd.Core;
import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.FileManager;
import org.aitools.programd.util.UserError;

/**
 * <p>
 * Presently more a proof-of-concept than anything else, for checking the new
 * {@link Multiplexor} architecture.
 * </p>
 * <p>
 * Uses &quot;flat-file&quot; Java properties files, as in Program B, to store
 * predicate data.
 * </p>
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @version 4.5
 */
public class FlatFileMultiplexor extends Multiplexor
{
    /** The name of the subdirectory for the predicate files. */
    private String ffmDirName;

    /** The suffix for a predicates storage file. */
    private static final String PREDICATES_SUFFIX = ".predicates";

    /** The string &quot;{@value}&quot;. */
    private static final String FFM_FILE_LABEL = "FlatFileMultiplexor predicates file";

    /**
     * Creates a new FlatFileMultiplexor with the given Core as owner.
     * 
     * @param coreOwner the Core that owns this FlatFileMultiplexor
     */
    public FlatFileMultiplexor(Core coreOwner)
    {
        super(coreOwner);
        this.ffmDirName = this.core.getSettings().getMultiplexorFfmDir().getPath();
    }

    /**
     * @see org.aitools.programd.multiplexor.Multiplexor#initialize()
     */
    @Override
    public void initialize()
    {
        // No initialization necessary.
    }

    /**
     * Always returns true (FlatFileMultiplexor currently does not support
     * authentication).
     * 
     * @param userid the userid to check
     * @param password the password to try
     * @param botid the id of the bot for whom to check the given
     *            userid/password combination
     * @return true always
     */
    @Override
    public boolean checkUser(@SuppressWarnings("unused") String userid, @SuppressWarnings("unused") String password, @SuppressWarnings("unused") String botid)
    {
        return true;
    }

    /**
     * Does nothing (FlatFileMultiplexor currently does not support
     * authentication).
     * 
     * @param userid the userid to create
     * @param password the password to add
     * @param botid the id of the bot for whom to create the given
     *            userid/password combination
     */
    @Override
    public void createUser(@SuppressWarnings("unused") String userid, @SuppressWarnings("unused") String password, @SuppressWarnings("unused") String botid)
    {
        // Do nothing.
    }

    /**
     * Always returns true (FlatFileMultiplexor currently does not support
     * authentication).
     * 
     * @param userid the userid for whom to change the passwod
     * @param password the password to change
     * @param botid the id of the bot for whom to change the given
     *            userid/password combination
     * @return true always
     */
    @Override
    public boolean changePassword(@SuppressWarnings("unused") String userid, @SuppressWarnings("unused") String password, @SuppressWarnings("unused") String botid)
    {
        return true;
    }

    /**
     * Saves a predicate to disk.
     * 
     * @param name the name of the predicate to save
     * @param value the value to save for the predicate
     * @param userid the userid with which to associate this predicate
     * @param botid the botid with which to associate this predicate
     */
    @Override
    public void savePredicate(String name, String value, String userid, String botid)
    {
        Properties predicates = loadPredicates(userid, botid);

        // Store the predicate value.
        predicates.setProperty(name, value);

        // Write predicates to disk immediately.
        savePredicates(predicates, userid, botid);
    }

    /**
     * Loads the value of a predicate from disk.
     * 
     * @param name the name of the predicate to locate
     * @param userid the userid whose value of the given predicate is desired
     * @param botid the botid whose userid-associated value of the given
     *            predicate is desired
     * @return the value of the predicate
     * @throws NoSuchPredicateException if no such predicate has been defined
     *             for the given userid and botid pair
     */
    @Override
    public String loadPredicate(String name, String userid, String botid) throws NoSuchPredicateException
    {
        Properties predicates = loadPredicates(userid, botid);

        // Try to get the predicate value.
        String result = predicates.getProperty(name);

        if (result == null)
        {
            throw new NoSuchPredicateException(name);
        }
        return result;
    }

    /**
     * Loads the predicates file for a given userid. Ensures that the directory
     * exists.
     * 
     * @param userid the userid to look for
     * @param botid the botid with which to associate the userid in the search
     * @return the predicates for the userid
     */
    private Properties loadPredicates(String userid, String botid)
    {
        Properties predicates = new Properties();

        String fileName = this.ffmDirName + File.separator + botid + File.separator + userid + PREDICATES_SUFFIX;

        File predicateFile = FileManager.checkOrCreate(fileName, FFM_FILE_LABEL);
        if (predicateFile.canRead())
        {
            try
            {
                predicates.load(new FileInputStream(predicateFile));
            }
            catch (IOException e)
            {
                throw new UserError("Error trying to load predicates.", e);
            }
        }
        return predicates;
    }

    /**
     * Saves the predicates file for a given userid. Ensures that the directory
     * exists.
     * 
     * @param predicates
     * @param userid the userid for which to save the predicates
     * @param botid the botid for which to save the predicates
     */
    private void savePredicates(Properties predicates, String userid, String botid)
    {
        String fileName = this.ffmDirName + File.separator + botid + File.separator + userid + PREDICATES_SUFFIX;
        FileManager.checkOrCreate(fileName, FFM_FILE_LABEL);
        FileOutputStream outputStream;
        try
        {
            outputStream = FileManager.getFileOutputStream(fileName);
        }
        catch (FileNotFoundException e)
        {
            throw new DeveloperError("Could not locate just-created file: \"" + fileName + "\".", e);
        }

        try
        {
            predicates.store(outputStream, null);
        }
        catch (IOException e)
        {
            throw new UserError("Error trying to save predicates.", e);
        }
    }

    /**
     * @see org.aitools.programd.multiplexor.Multiplexor#useridCount(java.lang.String)
     */
    @Override
    public int useridCount(@SuppressWarnings("unused") String botid)
    {
        return 0;
    }
}