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

package org.alicebot.server.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.logging.Trace;
import org.alicebot.server.core.parser.AIMLParser;
import org.alicebot.server.core.responder.Responder;
import org.alicebot.server.core.util.InputNormalizer;
import org.alicebot.server.core.util.Substituter;
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
public class DBMultiplexor extends AbstractClassifier
{
    /** A manager for database access. */
    private static DbAccessRefsPoolMgr dbManager;

    /** The bot name (this will go away for multi-bot support) */
    private static final String botName = Globals.getBotName();

    /** Holds userid/password combinations after they have been verified for the first time. */
    private static HashMap UserCache = new HashMap();


    public String setPredicateValue(String name, String value, String userid)
    {
        return StaticSelf.setPredicateValue(name, value, userid);
    }


    public String getPredicateValue(String name, String userid) throws NoSuchPredicateException
    {
        return StaticSelf.getPredicateValue(name, userid);
    }


    public boolean createUser(String userid, String password, String secretKey)
    {
        return StaticSelf.createUser(userid, password, secretKey);
    }


    public boolean checkUser(String userid, String password, String secretKey)
    {
        return StaticSelf.checkUser(userid, password, secretKey);
    }


    public boolean changePassword(String userid, String password, String secretKey)
    {
        return StaticSelf.changePassword(userid, password, secretKey);
    }


    /**
     *  Loads the database properties from the server configuration.
     */
    public void initialize()
    {
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
            Log.userfail("Error trying to connect to your database.  Check that it is available.", new String[] {Log.ERROR, Log.DATABASE, Log.STARTUP});
            Log.userfail(e.getMessage(), new String[] {Log.ERROR, Log.DATABASE, Log.STARTUP});
        }
    }


    /**
     *  Contains static synchronized versions of
     *  {@link Multiplexor} methods, to ensure thread-safety.
     */
    public static class StaticSelf
    {
        /**
         *  Stores a predicate in a database.
         *
         *  @see Multiplexor#setPredicate(String, String, String)
         */
        public static synchronized String setPredicateValue(String name, String value, String userid)
        {
            if (name.equals(THAT))
            {
                // Get the last sentence for <that/> storage.
                ArrayList sentences = InputNormalizer.sentenceSplit(value);
                value = (String)sentences.get(sentences.size() - 1);

                if (value.length() <= 0)
                {
                    value = THAT;
                }
            }
            
            // URLEncoder conveniently escapes things that would otherwise be problematic.
            String encodedValue = URLEncoder.encode(value.trim());

            DbAccess dbaRef = null;
            try
            {
                dbaRef = dbManager.takeDbaRef();
            }
            catch (Exception e)
            {
                Log.devinfo("Could not get database reference when setting predicate name \"" +
                            name + "\" to value \"" + value + "\" for \"" + userid + "\".",
                            new String[] {Log.DATABASE, Log.ERROR});
                Log.userfail("Fatal database error.", new String[] {Log.DATABASE, Log.ERROR});
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
                    dbaRef.executeUpdate("update predicates set value = '" + value + "' where userid= '" + userid + "' and name = '" + name + "'");
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
            return value;
        }


        /**
         *  Gets the value of a predicate from a database.
         *
         *  @see Multiplexor#getPredicate(String, String)
         */
        public static synchronized String getPredicateValue(String name, String userid)
        {
            String result = EMPTY_STRING;
            boolean foundIt = false;
            DbAccess dbaRef = null;
            try
            {
                dbaRef = dbManager.takeDbaRef();
            }
            catch (Exception e)
            {
                Log.devinfo("Could not get database reference when getting value for predicate name \"" +
                            name + "\" for \"" + userid + "\".", new String[] {Log.DATABASE, Log.ERROR});
                Log.userfail("Fatal database error.", new String[] {Log.DATABASE, Log.ERROR});
            }
            try
            {
                ResultSet records = dbaRef.executeQuery("select value from predicates where userid = '" + userid + "' and name = '" + name + "'");
                int returnCount = 0;
                while (records.next())
                {
                    foundIt = true;
                    returnCount++;
                    result = URLDecoder.decode(records.getString(VALUE).trim());
                }
                records.close();
                dbManager.returnDbaRef(dbaRef);
            }
            catch (SQLException e)
            {
                Log.log("Database error: " + e, Log.ERROR);
                result = Globals.getBotPredicateEmptyDefault();
                return result;
            }
            if (foundIt == false)
            {
                result = Globals.getBotPredicateEmptyDefault();
                return result;
            }
            return URLDecoder.decode(result);
        }


        /**
         *  Creates a userid with a given password.  If the
         *  userid already exists, returns false.
         *
         *  @see {@link Multiplexor#createUser}
         */
        public static synchronized boolean createUser(String userid, String password, String secretKey)
        {
            if (!secretKey.equals(SECRET_KEY))
            {
                Log.userfail("ACCESS VIOLATION!", Log.ERROR);
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
                Log.devinfo("Could not get database reference when creating user \"" +
                            userid + "\" with password \"" + password + "\" and secret key \"" + secretKey + "\".",
                            new String[] {Log.DATABASE, Log.ERROR});
                Log.userfail("Fatal database error.", new String[] {Log.DATABASE, Log.ERROR});
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
                Log.userfail("Error working with database.", new String[] {Log.DATABASE, Log.ERROR});
            }
            dbManager.returnDbaRef(dba);
            return true;
        }


        public static synchronized boolean checkUser(String userid, String password, String secretKey)
        {
            if (!secretKey.equals(SECRET_KEY))
            {
                Log.userfail("ACCESS VIOLATION!", Log.ERROR);
            }
            // Look first to see if the user is already in the cache.
            if (UserCache.containsKey(userid))
            {
                // If so, check against stored password.
                if (((String)UserCache.get(userid)).equals(password))
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
                    UserCache.put(userid, password);
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
        private static synchronized boolean checkUserInDB(String userid, String password)
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
                Log.devinfo("Could not get database reference when checking user \"" +
                            userid + "\" with password \"" + password + "\".",
                            new String[] {Log.DATABASE, Log.ERROR});
                Log.userfail("Fatal database error.", new String[] {Log.DATABASE, Log.ERROR});
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
                        Log.userfail("Duplicate user name: \"" + userid + "\"", new String[] {Log.DATABASE, Log.ERROR});
                    }
                }
                rs.close();
                dbManager.returnDbaRef(dbaRef);
            }
            catch (SQLException e)
            {
                Log.userfail("Database error: " + e.getMessage(), new String[] {Log.DATABASE, Log.ERROR});
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



        public static synchronized boolean changePassword(String userid, String password, String secretKey)
        {
            if (!secretKey.equals(SECRET_KEY))
            {
                Log.userfail("ACCESS VIOLATION!", Log.ERROR);
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
                Log.devinfo("Could not get database reference when changing password to \"" +
                            password + "\" for \"" + userid + "\".",
                            new String[] {Log.DATABASE, Log.ERROR});
                Log.userfail("Fatal database error.", new String[] {Log.DATABASE, Log.ERROR});
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
                Log.userfail("Database error: " + e.getMessage(), new String[] {Log.DATABASE, Log.ERROR});
            }
            UserCache.remove(userid);
            UserCache.put(userid, password);
            return true;
        }
    }
} 
