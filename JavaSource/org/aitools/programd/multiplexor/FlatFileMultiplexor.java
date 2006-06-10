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
import org.aitools.util.runtime.DeveloperError;
import org.aitools.util.resource.Filesystem;
import org.aitools.util.runtime.UserError;

/**
 * <p>Presently more a proof-of-concept than anything else, for checking the new {@link Multiplexor} architecture.</p>
 * <p>Uses &quot;flat-file&quot; Java properties files, as in Program B, to store predicate data.</p>
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class FlatFileMultiplexor extends Multiplexor<Properties>
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
     * @param core the Core that owns this FlatFileMultiplexor
     */
    public FlatFileMultiplexor(Core core)
    {
        super(core);
        this.ffmDirName = this._core.getSettings().getMultiplexorFfmDir().getPath();
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
     * Always returns true (FlatFileMultiplexor currently does not support authentication).
     * 
     * @param userid the userid to check
     * @param password the password to try
     * @param botid the id of the bot for whom to check the given userid/password combination
     * @return true always
     */
    @Override
    @SuppressWarnings("unused")
    public boolean checkUser(String userid, String password, String botid)
    {
        return true;
    }

    /**
     * Does nothing (FlatFileMultiplexor currently does not support authentication).
     * 
     * @param userid the userid to create
     * @param password the password to add
     * @param botid the id of the bot for whom to create the given userid/password combination
     */
    @Override
    @SuppressWarnings("unused")
    public void createUser(String userid, String password, String botid)
    {
        // Do nothing.
    }

    /**
     * Always returns true (FlatFileMultiplexor currently does not support authentication).
     * 
     * @param userid the userid for whom to change the passwod
     * @param password the password to change
     * @param botid the id of the bot for whom to change the given userid/password combination
     * @return true always
     */
    @Override
    @SuppressWarnings("unused")
    public boolean changePassword(String userid, String password, String botid)
    {
        return true;
    }

    @Override
    protected Properties getStorageMechanism(String userid, String botid)
    {
        return loadPredicates(userid, botid);
    }

    @Override
    @SuppressWarnings("unused")
    protected void preparePredicateForStorage(Properties mechanism, String userid, String botid, String name, PredicateValue value)
    {
        if (value.size() == 1)
        {
            mechanism.setProperty(name, value.getFirstValue());
        }
        else
        {
            for (int index = 1; index <= value.size(); index++)
            {
                mechanism.setProperty(name + '.' + (index), value.get(index));
            }
        }
    }

    /**
     * Loads the value of a predicate from disk.
     * 
     * @param name the name of the predicate to locate
     * @param userid the userid whose value of the given predicate is desired
     * @param botid the botid whose userid-associated value of the given predicate is desired
     * @return the value of the predicate
     * @throws NoSuchPredicateException if no such predicate has been defined for the given userid and botid pair
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
     * Loads the predicates file for a given userid. Ensures that the directory exists.
     * 
     * @param userid the userid to look for
     * @param botid the botid with which to associate the userid in the search
     * @return the predicates for the userid
     */
    protected Properties loadPredicates(String userid, String botid)
    {
        Properties predicates = new Properties();

        String fileName = this.ffmDirName + File.separator + botid + File.separator + userid + PREDICATES_SUFFIX;

        File predicateFile = Filesystem.checkOrCreate(fileName, FFM_FILE_LABEL);
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
     * Saves the predicates file for a given userid. Ensures that the directory exists.
     * 
     * @param predicates
     * @param userid the userid for which to save the predicates
     * @param botid the botid for which to save the predicates
     */
    @Override
    protected void savePredicates(Properties predicates, String userid, String botid)
    {
        String fileName = this.ffmDirName + File.separator + botid + File.separator + userid + PREDICATES_SUFFIX;
        Filesystem.checkOrCreate(fileName, FFM_FILE_LABEL);
        FileOutputStream outputStream;
        try
        {
            outputStream = Filesystem.getFileOutputStream(fileName);
        }
        catch (FileNotFoundException e)
        {
            throw new DeveloperError(String.format("Could not locate just-created file: \"%s\".", fileName), e);
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
    @SuppressWarnings("unused")
    public int useridCount(String botid)
    {
        return 0;
    }
}
