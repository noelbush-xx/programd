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
    4.1.4 [00] - December 2001, Noel Bush
    - changed response time display back so that it shows when console is in use
    - added support of *not* saving special predicates <that/>, <input/> and <star/>
*/


package org.alicebot.server.core;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.util.UserErrorException;
import org.alicebot.server.core.util.Toolkit;
import org.alicebot.server.sql.pool.DbAccess;
import org.alicebot.server.sql.pool.DbAccessRefsPoolMgr;


/**
 *  <p>
 *  A database-oriented {@link Multiplexor}. Uses a database for storage
 *  and retrieval of predicates.
 *  </p>
 *  <p>
 *  This is adapted from {@link Classifier}, to use a better database
 *  structure and to support user authentication.
 *  </p>
 *
 *  @author Richard Wallace, Jon Baer
 *  @author Thomas Ringate/Pedro Colla
 *  @author Noel Bush
 *  @version 4.1.3
 */
public class DBMultiplexor extends AbstractClassifier implements PredicateMasterListener
{
    /** A manager for database access. */
    private static DbAccessRefsPoolMgr dbManager;

    /** The bot name (this will go away for multi-bot support) */
    private static final String botName = Globals.getBotName();

    /** Holds userid/password combinations after they have been verified for the first time. */
    private static HashMap userCache = new HashMap();


    /**
     *  Loads the database properties from the server configuration.
     */
    public synchronized void initialize()
    {
        super.initialize();
        // Exception handling needs to be better here!
        try
        {
            Log.devinfo("DBMultiplexor: Opening database pool.", new String[] {Log.DATABASE, Log.STARTUP});
            dbManager = new DbAccessRefsPoolMgr(Globals.getProperty("programd.database.driver", ""),
                                                Globals.getProperty("programd.database.url", ""),
                                                Globals.getProperty("programd.database.user", ""),
                                                Globals.getProperty("programd.database.password", ""));
            Log.devinfo("DBMultiplexor: Populating database pool.", new String[] {Log.DATABASE, Log.STARTUP});
            dbManager.populate(Integer.parseInt(Globals.getProperty("programd.database.connections", "")));
        }
        catch (Exception e)
        {
            throw new UserErrorException("Error trying to connect to your database.  Check that it is available.", e);
        }
        PredicateMaster.registerListener(this);
    }


    /**
     *  Saves a predicate in a database.
     */
    public void savePredicate(String name, String value, String userid)
    {
        /*
            URLEncoder conveniently escapes things that
            would otherwise be problematic.
        */
        String encodedValue = URLEncoder.encode(value.trim());

        DbAccess dbaRef = null;
        try
        {
            dbaRef = dbManager.takeDbaRef();
        }
        catch (Exception e)
        {
            throw new UserErrorException("Could not get database reference when setting predicate name \"" +
                        name + "\" to value \"" + value + "\" for \"" + userid + "\".", e);
        }
        try
        {
            ResultSet records = dbaRef.executeQuery("select value from predicates where userid = '" + userid + "' and name = '" + name + "'");
            int count = 0;
            while (records.next())
            {
                count++;
            }
            if (count > 0)
            {
                dbaRef.executeUpdate("update predicates set value = '" + encodedValue + "' where userid= '" + userid + "' and name = '" + name + "'");
            }
            else
            {
                dbaRef.executeUpdate("insert into predicates values ('" + userid +"' , '" + name + "','" + encodedValue + "')");
            }
            records.close();
            dbManager.returnDbaRef(dbaRef);
        }
        catch (SQLException e)
        {
            Log.log("Database error: " + e, Log.ERROR);
        }
    }


    /**
     *  Loads the value of a predicate from a database.
     */
    public String loadPredicate(String name, String userid) throws NoSuchPredicateException
    {
        String result = null;
        DbAccess dbaRef = null;
        try
        {
            dbaRef = dbManager.takeDbaRef();
        }
        catch (Exception e)
        {
            throw new UserErrorException("Could not get database reference when getting value for predicate name \"" +
                        name + "\" for \"" + userid + "\".", e);
        }
        try
        {
            ResultSet records = dbaRef.executeQuery("select value from predicates where userid = '" + userid + "' and name = '" + name + "'");
            int returnCount = 0;
            while (records.next())
            {
                returnCount++;
                result = records.getString(VALUE);
            }
            records.close();
            dbManager.returnDbaRef(dbaRef);
        }
        catch (SQLException e)
        {
            Log.log("Database error: " + e, Log.ERROR);
            throw new NoSuchPredicateException(name);
        }
        if (result == null)
        {
            throw new NoSuchPredicateException(name);
        }
        // If found, return it (don't forget to decode!).
        return URLDecoder.decode(result);
    }


    /**
     *  Creates a userid with a given password.  If the
     *  userid already exists, returns false.
     *
     *  @see {@link Multiplexor#createUser}
     */
    public synchronized boolean createUser(String userid, String password, String secretKey)
    {
        if (!secretKey.equals(SECRET_KEY))
        {
            Log.userinfo("ACCESS VIOLATION: Tried to create a user with invalid secret key.", Log.ERROR);
        }
        userid = userid.trim().toLowerCase();
        password = password.trim().toLowerCase();
        DbAccess dba = null;
        try
        {
            dba = dbManager.takeDbaRef();
        }
        catch (Exception e)
        {
            throw new UserErrorException("Could not get database reference when creating user \"" +
                        userid + "\" with password \"" + password + "\" and secret key \"" + secretKey + "\".", e);
        }
        try
        {
            ResultSet rs = dba.executeQuery("select * from users where userid = '" + userid + "'");
            int returnCount = 0;
            while (rs.next())
            {
                returnCount++;
                if(returnCount==1)
                {
                    rs.close();
                    dbManager.returnDbaRef(dba);
                    return false;
                }
            }
            dba.executeUpdate("insert into users values ('" + userid +"' , '" + password + "')");
            rs.close();
        }
        catch (SQLException e)
        {
            throw new UserErrorException("Error working with database.", e);
        }
        dbManager.returnDbaRef(dba);
        return true;
    }


    public synchronized boolean checkUser(String userid, String password, String secretKey)
    {
        if (!secretKey.equals(SECRET_KEY))
        {
            Log.userinfo("ACCESS VIOLATION: Tried to create a user with invalid secret key.", Log.ERROR);
        }
        // Look first to see if the user is already in the cache.
        if (userCache.containsKey(userid))
        {
            // If so, check against stored password.
            if (((String)userCache.get(userid)).equals(password))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        // Otherwise, look in the database, and put in the cache if valid.
        else
        {
            if (checkUserInDB(userid, password))
            {
                userCache.put(userid, password);
                return true;
            }
            else
            {
                return false;
            }
        }
    }


    /**
     *  Checks a userid/password combination in the database.
     *
     *  @param userid       the userid to check
     *  @param password     the password to check
     *  @param secretKey    the secret key that should authenticate this request
     *
     *  @return whether the userid and password combination is valid
     */
    private synchronized boolean checkUserInDB(String userid, String password)
    {
        String passwordInDatabase = null;

        userid = userid.trim().toLowerCase();
        password = password.trim().toLowerCase();
        DbAccess dbaRef = null;
        try
        {
            dbaRef = dbManager.takeDbaRef();
        }
        catch (Exception e)
        {
            throw new UserErrorException("Could not get database reference when checking user \"" +
                        userid + "\" with password \"" + password + "\".", e);
        }
        try
        {
            ResultSet rs = dbaRef.executeQuery("select * from users where userid = '" + userid + "'");
            int returnCount = 0;
            while (rs.next())
            {
                returnCount++;
                if (returnCount == 1)
                {
                    passwordInDatabase = rs.getString("password");
                }
                if (returnCount == 0)
                {
                    rs.close();
                    dbManager.returnDbaRef(dbaRef);
                    return false;
                }
                if (returnCount > 1)
                {
                    throw new UserErrorException("Duplicate user name: \"" + userid + "\"");
                }
            }
            rs.close();
            dbManager.returnDbaRef(dbaRef);
        }
        catch (SQLException e)
        {
            throw new UserErrorException("Database error.", e);
        }
        if (passwordInDatabase == null)
        {
            return false;
        }
        if (!password.equals(passwordInDatabase))
        {
            return false;
        }
        return true;
    }



    public synchronized boolean changePassword(String userid, String password, String secretKey)
    {
        if (!secretKey.equals(SECRET_KEY))
        {
            Log.userinfo("ACCESS VIOLATION: Tried to create a user with invalid secret key.", Log.ERROR);
        }
        userid = userid.trim().toLowerCase();
        password = password.trim().toLowerCase();
        DbAccess dbaRef = null;
        try
        {
            dbaRef = dbManager.takeDbaRef();
        }
        catch (Exception e)
        {
            throw new UserErrorException("Could not get database reference when changing password to \"" +
                        password + "\" for \"" + userid + "\".", e);
        }
        try
        {
            ResultSet rs = dbaRef.executeQuery("select * from users where userid = '" + userid + "'");
            int returnCount = 0;
            while (rs.next())
            {
                returnCount++;
            }
            if (returnCount==0)
            {
                rs.close();
                dbManager.returnDbaRef(dbaRef);
                return(false);
            }
            dbaRef.executeUpdate("update users set password = '" + password +"' where userid = '" + userid + "'");
            rs.close();
            dbManager.returnDbaRef(dbaRef);
        }
        catch (SQLException e)
        {
            throw new UserErrorException("Database error.", e);
        }
        userCache.remove(userid);
        userCache.put(userid, password);
        return true;
    }


    /**
     *  Removes any userids from the cache that are no longer
     *  in the PredicateMaster's cache (essentially causes the
     *  <code>DBMultiplexor</code> to rely on the PredicateMaster's
     *  cache maintenance to maintain its own.
     *
     *  @param userids  the set of userids
     */
    public synchronized void updateUserids(Set userids)
    {
        Iterator myUserids = userCache.keySet().iterator();
        while (myUserids.hasNext())
        {
            String userid = (String)myUserids.next();
            if (!userids.contains(userid))
            {
                myUserids.remove();
            }
        }
    }
} 
