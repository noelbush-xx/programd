/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.predicates;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.aitools.programd.Core;
import org.aitools.util.runtime.DeveloperError;
import org.aitools.util.resource.Filesystem;
import org.aitools.util.runtime.UserError;

/**
 * Uses &quot;flat-file&quot; Java properties files to store predicate data.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class FlatFilePredicateManager extends PredicateManager
{
    /** The name of the subdirectory for the predicate files. */
    private String _dirname;

    /** The suffix for a predicates storage file. */
    private static final String PREDICATES_SUFFIX = ".predicates";

    /** The string &quot;{@value}&quot;. */
    private static final String FILE_LABEL = "FlatFilePredicateManager predicates file";

    /**
     * Creates a new FlatFilePredicateManager with the given Core as owner.
     * 
     * @param core the Core that owns this FlatFilePredicateManager
     */
    public FlatFilePredicateManager(Core core)
    {
        super(core);
        this._dirname = this._core.getSettings().getFfpmDirectory().getPath();
    }

    /**
     * @see org.aitools.programd.predicates.PredicateManager#initialize()
     */
    @Override
    public void initialize()
    {
        // No initialization necessary.
    }

    /**
     * @see org.aitools.programd.predicates.PredicateManager#loadPredicate(java.lang.String, java.lang.String,
     *      java.lang.String)
     */
    @Override
    public String loadPredicate(String name, String user, String bot) throws NoSuchPredicateException
    {
        Properties predicates = loadPredicates(user, bot);

        // Try to get the predicate value.
        String result = predicates.getProperty(name);

        if (result == null)
        {
            throw new NoSuchPredicateException(name);
        }
        return result;
    }

    /**
     * Loads the predicates file for a given user. Ensures that the directory exists.
     * 
     * @param user the user to look for
     * @param bot the bot with which to associate the user in the search
     * @return the predicates for the user
     */
    protected Properties loadPredicates(String user, String bot)
    {
        Properties predicates = new Properties();

        File predicateFile = Filesystem.checkOrCreate(composeFilename(bot, user), FILE_LABEL);
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
     * @see org.aitools.programd.predicates.PredicateManager#dumpPredicates()
     */
    @Override
    public void dumpPredicates()
    {
        {
            for (String bot : this._bots.keySet())
            {
                Map<String, PredicateMap> predicateCache = this._bots.get(bot).getPredicateCache();
                for (String user : predicateCache.keySet())
                {
                    Properties predicates = loadPredicates(user, bot);
                    PredicateMap predicateMap = predicateCache.get(user);
                    for (String name : predicateMap.keySet())
                    {
                        PredicateValue value = predicateMap.get(name);
                        if (value.size() == 1)
                        {
                            predicates.setProperty(name, value.getFirstValue());
                        }
                        else
                        {
                            for (int index = 1; index <= value.size(); index++)
                            {
                                predicates.setProperty(name + '.' + (index), value.get(index));
                            }
                        }
                    }
                    predicateMap.clear();
                    String fileName = composeFilename(user, bot);
                    Filesystem.checkOrCreate(fileName, FILE_LABEL);
                    FileOutputStream outputStream;
                    try
                    {
                        outputStream = Filesystem.getFileOutputStream(fileName);
                    }
                    catch (FileNotFoundException e)
                    {
                        throw new DeveloperError(
                                String.format("Could not locate just-created file: \"%s\".", fileName), e);
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
            }
        }
    }

    /**
     * Composes a filename for storing/retrieving predicates.
     * 
     * @param user
     * @param bot
     * @return a filename
     */
    @SuppressWarnings("boxing")
    protected String composeFilename(String user, String bot)
    {
        return String.format("%s%c%s%c%s%s", this._dirname, File.separatorChar, bot, File.separatorChar, user,
                PREDICATES_SUFFIX);
    }
}
