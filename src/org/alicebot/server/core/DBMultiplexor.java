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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.util.DeveloperError;
import org.alicebot.server.core.util.UserError;
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
public class DBMultiplexor extends Multiplexor
{
    /** A manager for database access. */
    private static DbAccessRefsPoolMgr dbManager;

    private static HashMap userCacheForBots = new HashMap();

    /** The string &quot;UTF-8&quot; (for character encoding conversion). */
    private static final String ENC_UTF8 = "UTF-8";


    /**
     *  Loads the database properties from the server configuration.
     */
    public void initialize()
    {
        super.initialize();

        Log.devinfo("Opening database pool.", new String[] {Log.DATABASE, Log.STARTUP});

        dbManager = new DbAccessRefsPoolMgr(Globals.getProperty("programd.database.driver", ""),
                                            Globals.getProperty("programd.database.url", ""),
                                            Globals.getProperty("programd.database.user", ""),
                                            Globals.getProperty("programd.database.password", ""));

        Log.devinfo("Populating database pool.", new String[] {Log.DATABASE, Log.STARTUP});

        dbManager.populate(Integer.parseInt(Globals.getProperty("programd.database.connections", "")));
    }


    /**
     *  Saves a predicate in a database.
     */
    public void savePredicate(String name, String value, String userid, String botid)
    {
        /*
            URLEncoder conveniently escapes things that
            would otherwise be problematic.
        */
        String encodedValue;
        try
        {
            encodedValue = URLEncoder.encode(value.trim(), ENC_UTF8);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new DeveloperError("This platform does not support UTF-8!");
        }
        
        DbAccess dbaRef = null;
        try
        {
            dbaRef = dbManager.takeDbaRef();
        }
        catch (Exception e)
        {
            throw new UserError("Could not get database reference when setting predicate name \"" +
                        name + "\" to value \"" + value + "\" for \"" + userid + "\" as known to \"" + botid + "\".", e);
        }
        try
        {
            ResultSet records =
                dbaRef.executeQuery("select value from predicates where botid = '" + botid +
                                    "' and userid = '" + userid + "' and name = '" + name + "'");
            int count = 0;
            while (records.next())
            {
                count++;
            }
            if (count > 0)
            {
                dbaRef.executeUpdate("update predicates set value = '" + encodedValue +
                                     "' where botid = '" + botid + "' and userid= '" + userid +
                                     "' and name = '" + name + "'");
            }
            else
            {
                dbaRef.executeUpdate("insert into predicates (userid, botid, name, value) values ('" + userid +
                                     "', '" + botid +"' , '" + name + "','" + encodedValue + "')");
            }
            records.close();
            dbManager.returnDbaRef(dbaRef);
        }
        catch (SQLException e)
        {
            Log.userinfo("Database error: " + e, new String[]{Log.DATABASE, Log.ERROR});
        }
    }


    /**
     *  Loads the value of a predicate from a database.
     */
    public String loadPredicate(String name, String userid, String botid) throws NoSuchPredicateException
    {
        String result = null;
        DbAccess dbaRef = null;
        try
        {
            dbaRef = dbManager.takeDbaRef();
        }
        catch (Exception e)
        {
            throw new UserError("Could not get database reference when getting value for predicate name \"" +
                        name + "\" for \"" + userid + "\" as known to \"" + botid + "\".", e);
        }
        try
        {
            ResultSet records =
                dbaRef.executeQuery("select value from predicates where botid = '" + botid +
                                    "' and userid = '" + userid + "' and name = '" + name + "'");
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
        try
        {
            return URLDecoder.decode(result, ENC_UTF8);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new DeveloperError("This platform does not support UTF-8!");
        }
    }


    /**
     *  Creates a userid with a given password.  If the
     *  userid already exists, returns false.
     *
     *  @see {@link Multiplexor#createUser}
     */
    public boolean createUser(String userid, String password, String secretKey, String botid)
    {
        if (!secretKey.equals(SECRET_KEY))
        {
            Log.userinfo("ACCESS VIOLATION: Tried to create a user with invalid secret key.", Log.ERROR);
            return false;
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
            throw new UserError("Could not get database reference when creating user \"" +
                        userid + "\" with password \"" + password + "\" and secret key \"" + secretKey + "\".", e);
        }
        try
        {
            ResultSet rs = dba.executeQuery("select * from users where userid = '" + userid + "' and botid = '" + botid + "'");
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
            dba.executeUpdate("insert into users (userid, password, botid) values ('" + userid +"' , '" + password + "' , '" + botid + "')");
            rs.close();
        }
        catch (SQLException e)
        {
            throw new UserError("Error working with database.", e);
        }
        dbManager.returnDbaRef(dba);
        return true;
    }


    public boolean checkUser(String userid, String password, String secretKey, String botid)
    {
        if (!secretKey.equals(SECRET_KEY))
        {
            Log.userinfo("ACCESS VIOLATION: Tried to create a user with invalid secret key.", Log.ERROR);
            return false;
        }
        // Look first to see if the user is already in the cache.
        if (!userCacheForBots.containsKey(botid))
        {
            userCacheForBots.put(botid, new HashMap());
        }
        HashMap userCache = (HashMap)userCacheForBots.get(botid);
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
            if (checkUserInDB(userid, password, botid))
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
     *
     *  @return whether the userid and password combination is valid
     */
    private boolean checkUserInDB(String userid, String password, String botid)
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
            throw new UserError("Could not get database reference when checking user \"" +
                        userid + "\" with password \"" + password + "\".", e);
        }
        try
        {
            ResultSet rs =
                dbaRef.executeQuery("select * from users where userid = '" + userid + "' and botid = '" + botid + "'");
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
                    throw new UserError("Duplicate user name: \"" + userid + "\"");
                }
            }
            rs.close();
            dbManager.returnDbaRef(dbaRef);
        }
        catch (SQLException e)
        {
            throw new UserError("Database error.", e);
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



    public boolean changePassword(String userid, String password, String secretKey, String botid)
    {
        if (!secretKey.equals(SECRET_KEY))
        {
            Log.userinfo("ACCESS VIOLATION: Tried to create a user with invalid secret key.", Log.ERROR);
            return false;
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
            throw new UserError("Could not get database reference when changing password to \"" +
                        password + "\" for \"" + userid + "\" as known to \"" + botid + "\".", e);
        }
        try
        {
            ResultSet rs =
                dbaRef.executeQuery("select * from users where userid = '" + userid + "' and botid = '" + botid + "'");
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
            dbaRef.executeUpdate("update users set password = '" + password +
                                 "' where userid = '" + userid + "' and botid = '" + botid + "'");
            rs.close();
            dbManager.returnDbaRef(dbaRef);
        }
        catch (SQLException e)
        {
            throw new UserError("Database error.", e);
        }
        userCacheForBots.remove(userid);
        userCacheForBots.put(userid, password);
        return true;
    }


    public int useridCount(String botid)
    {
        return ((HashMap)userCacheForBots.get(botid)).size();
    }
} 
