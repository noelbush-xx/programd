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

/*
    More fixes (4.1.3 [02] - November 2001, Noel Bush
    - changed to write to "ffm" subdirectory
    - added generic methods for user authentication
*/

/*
    4.1.4 [00] - December 2001, Noel Bush
    - added support of *not* saving special predicates <that/>, <input/> and <star/>
*/

package org.alicebot.server.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Properties;

import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.util.Trace;
import org.alicebot.server.core.util.UserErrorException;


/**
 *  <p>
 *  Presently more a proof-of-concept than anything else,
 *  for checking the new {@link Multiplexor}
 *  architecture.
 *  </p>
 *  <p>
 *  Uses &quot;flat-file&quot; Java properties files,
 *  as in Program B, to store predicate data.
 *  </p>
 *
 *  @author Noel Bush
 *  @version 4.1.3
 */
public class FlatFileMultiplexor extends AbstractClassifier
{
    /** The container for all predicate sets. */
    private static Hashtable predicateSets;

    /** The name of the subdirectory for the predicate files. */
    private static String FFM_DIR_NAME;

    /** The subdirectory for the predicate files. */
    private static File FFM_DIR;

    /** The suffix for a predicates storage file. */
    private static final String PREDICATES_SUFFIX = ".predicates";


    /**
     *  Always returns true (FlatFileMultiplexor currently
     *  does not support authentication).
     */
    public boolean checkUser(String userid, String password, String secretKey)
    {
        return true;
    }


    /**
     *  Always returns true (FlatFileMultiplexor currently
     *  does not support authentication).
     */
    public boolean createUser(String userid, String password, String secretKey)
    {
        return true;
    }


    /**
     *  Always returns true (FlatFileMultiplexor currently
     *  does not support authentication).
     */
    public boolean changePassword(String userid, String password, String secretKey)
    {
        return true;
    }


    /**
     *  Saves a predicate to disk.
     */
    public void savePredicate(String name, String value, String userid)
    {
        // Test whether predicateSets is intialized.
        if (predicateSets == null)
        {
            predicateSets = new Hashtable();
        }

        Properties predicates = null;

        // Do we have predicates for this user in memory?
        if (predicateSets.containsKey(userid))
        {
            predicates = (Properties)predicateSets.get(userid);
        }
        else
        {
            predicates = loadPredicates(userid);
            predicateSets.put(userid, predicates);
        }

        // Store the predicate value.
        predicates.setProperty(name, value);

        // Write predicates to disk immediately.
        try
        {
            predicates.store(new FileOutputStream(FFM_DIR_NAME + File.separator + userid + PREDICATES_SUFFIX), null);
        }
        catch (IOException e)
        {
            throw new UserErrorException("Error trying to save predicates.", e);
        }
    }


    /**
     *  Loads the value of a predicate from disk.
     */
    public String loadPredicate(String name, String userid) throws NoSuchPredicateException
    {
         // Test whether predicateSets is intialized.
        if (predicateSets == null)
        {
            predicateSets = new Hashtable();
        }

        Properties predicates;

        // Do we have predicates for this user in memory?
        if (predicateSets.containsKey(userid))
        {
            predicates = (Properties)predicateSets.get(userid);
        }
        else
        {
            predicates = loadPredicates(userid);
            predicateSets.put(userid, predicates);
        }

        // Try to get the predicate value.
        String result = predicates.getProperty(name);

        // Cache the result, if found.
        if (result == null)
        {
            throw new NoSuchPredicateException(name);
        }
        return result;
    }


    /**
     *  Loads the predicates file for a given userid.
     *  Ensures that the directory exists.
     *
     *  @param userid
     */
    private static Properties loadPredicates(String userid)
    {
        // Check that FFM_DIR_NAME and FFM_DIR are initialized.
        if (FFM_DIR_NAME == null)
        {
            FFM_DIR_NAME = "ffm/" + Globals.getBotID();
            FFM_DIR = new File(FFM_DIR_NAME);
            try
            {
                if (FFM_DIR.mkdirs())
                {
                    Log.userinfo("Created \"" + FFM_DIR_NAME + "\".", Log.STARTUP);
                }
            }
            catch (SecurityException e)
            {
                throw new UserErrorException("Permission denied to create \"" + FFM_DIR_NAME + "\".", e);
            }
        }

        Properties predicates = new Properties();
        File predicateFile = new File(FFM_DIR_NAME + File.separator + userid + PREDICATES_SUFFIX);
        if (predicateFile.canRead())
        {
            try
            {
                predicates.load(new FileInputStream(predicateFile));
            }
            catch (IOException e)
            {
                throw new UserErrorException("Error trying to load predicates.", e);
            }
        }
        return predicates;
    }
} 
