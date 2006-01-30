/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.multiplexor;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.aitools.programd.Core;
import org.aitools.programd.CoreSettings;
import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.UserError;
import org.aitools.programd.util.sql.DbAccess;
import org.aitools.programd.util.sql.DbAccessRefsPoolMgr;
import org.apache.log4j.Logger;

/**
 * <p>
 * A database-oriented {@link Multiplexor} . Uses a database for storage and
 * retrieval of predicates.
 * </p>
 * <p>
 * This is adapted from <a
 * href="http://cvs.aitools.org/cgi-bin/viewcvs.cgi/ProgramD/src/org/alicebot/server/core/Attic/Classifier.java?rev=1.1&only_with_tag=v4_1_0&content-type=text/vnd.viewcvs-markup">Classifier
 * </a>, to use a better database structure and to support user authentication.
 * </p>
 * 
 * @author Richard Wallace, Jon Baer
 * @author Thomas Ringate/Pedro Colla
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @version 4.5
 */
public class DBMultiplexor extends Multiplexor
{
    /** A manager for database access. */
    private DbAccessRefsPoolMgr dbManager;

    /** The logger for database activity. */
    private Logger dbLogger;

    private Map<String, Map<String, String>> userCacheForBots = new HashMap<String, Map<String, String>>();

    // Convenience constants.

    /** The string &quot;{@value}&quot; (for character encoding conversion). */
    private static final String ENC_UTF8 = "UTF-8";

    /**
     * Creates a new DBMultiplexor with the given Core as owner.
     * 
     * @param coreOwner the Core that owns this DBMultiplexor
     */
    public DBMultiplexor(Core coreOwner)
    {
        super(coreOwner);
        this.dbLogger = Logger.getLogger("programd");
    }

    /**
     * Loads the database properties from the server configuration.
     */
    @Override
    public void initialize()
    {
        CoreSettings coreSettings = this.core.getSettings();

        this.dbLogger.debug("Opening database pool.");

        this.dbManager = new DbAccessRefsPoolMgr(coreSettings.getDatabaseDriver(), coreSettings.getDatabaseUrl(), coreSettings.getDatabaseUser(),
                coreSettings.getDatabasePassword());

        this.dbLogger.debug("Populating database pool.");

        this.dbManager.populate(coreSettings.getDatabaseConnections());
    }

    /**
     * Saves a predicate in a database.
     * 
     * @param name the name of the predicate to save
     * @param value the value to save for the predicate
     * @param userid the userid with which to associate this predicate
     * @param botid the botid with which to associate this predicate
     */
    @Override
    public void savePredicate(String name, String value, String userid, String botid)
    {
        /*
         * URLEncoder conveniently escapes things that would otherwise be
         * problematic.
         */
        String encodedValue;
        try
        {
            encodedValue = URLEncoder.encode(value.trim(), ENC_UTF8);
        }
        catch (UnsupportedEncodingException e)
        {
            throw new DeveloperError("This platform does not support UTF-8!", e);
        }

        DbAccess dbaRef = null;
        try
        {
            dbaRef = this.dbManager.takeDbaRef();
        }
        catch (Exception e)
        {
            throw new UserError("Could not get database reference when setting predicate name \"" + name + "\" to value \"" + value + "\" for \""
                    + userid + "\" as known to \"" + botid + "\".", e);
        }
        try
        {
            ResultSet records = dbaRef.executeQuery("select value from predicates where botid = '" + botid + "' and userid = '" + userid
                    + "' and name = '" + name + "'");
            int count = 0;
            while (records.next())
            {
                count++;
            }
            if (count > 0)
            {
                dbaRef.executeUpdate("update predicates set value = '" + encodedValue + "' where botid = '" + botid + "' and userid= '" + userid
                        + "' and name = '" + name + "'");
            }
            else
            {
                dbaRef.executeUpdate("insert into predicates (userid, botid, name, value) values ('" + userid + "', '" + botid + "' , '" + name
                        + "','" + encodedValue + "')");
            }
            records.close();
            this.dbManager.returnDbaRef(dbaRef);
        }
        catch (SQLException e)
        {
            this.dbLogger.error("Database error: " + e);
        }
    }

    /**
     * Loads the value of a predicate from a database.
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
        String result = null;
        DbAccess dbaRef = null;
        try
        {
            dbaRef = this.dbManager.takeDbaRef();
        }
        catch (Exception e)
        {
            throw new UserError("Could not get database reference when getting value for predicate name \"" + name + "\" for \"" + userid
                    + "\" as known to \"" + botid + "\".", e);
        }
        try
        {
            ResultSet records = dbaRef.executeQuery("select value from predicates where botid = '" + botid + "' and userid = '" + userid
                    + "' and name = '" + name + "'");
            int returnCount = 0;
            while (records.next())
            {
                returnCount++;
                result = records.getString(VALUE);
            }
            records.close();
            this.dbManager.returnDbaRef(dbaRef);
        }
        catch (SQLException e)
        {
            this.dbLogger.error("Database error: " + e);
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
            throw new DeveloperError("This platform does not support UTF-8!", e);
        }
    }

    /**
     * Creates a userid with a given password. If the userid already exists,
     * returns false.
     * 
     * @param userid the userid to create
     * @param password the password to associate with the userid
     * @param botid the botid with whom to associate this userid/password
     *            combination
     * @throws DuplicateUserIDError if the given userid was already found in the
     *             database
     */
    @Override
    public void createUser(String userid, String password, String botid) throws DuplicateUserIDError
    {
        userid = userid.trim().toLowerCase();
        password = password.trim().toLowerCase();
        DbAccess dba = null;
        try
        {
            dba = this.dbManager.takeDbaRef();
        }
        catch (Exception e)
        {
            throw new UserError("Could not get database reference when creating user \"" + userid + "\" with password \"" + password
                    + "\".", e);
        }
        try
        {
            ResultSet rs = dba.executeQuery("select * from users where userid = '" + userid + "' and botid = '" + botid + "'");
            int returnCount = 0;
            while (rs.next())
            {
                returnCount++;
                if (returnCount == 1)
                {
                    rs.close();
                    this.dbManager.returnDbaRef(dba);
                    throw new DuplicateUserIDError(userid);
                }
            }
            dba.executeUpdate("insert into users (userid, password, botid) values ('" + userid + "' , '" + password + "' , '" + botid + "')");
            rs.close();
        }
        catch (SQLException e)
        {
            throw new UserError("Error working with database.", e);
        }
        this.dbManager.returnDbaRef(dba);
    }

    /**
     * @see org.aitools.programd.multiplexor.Multiplexor#checkUser(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public boolean checkUser(String userid, String password, String botid)
    {
        // Look first to see if the user is already in the cache.
        if (!this.userCacheForBots.containsKey(botid))
        {
            this.userCacheForBots.put(botid, Collections.checkedMap(new HashMap<String, String>(), String.class, String.class));
        }
        Map<String, String> userCache = this.userCacheForBots.get(botid);
        if (userCache.containsKey(userid))
        {
            // If so, check against stored password.
            if ((userCache.get(userid)).equals(password))
            {
                return true;
            }
            // (otherwise...)
            return false;
        }
        // Otherwise, look in the database, and put in the cache if valid.
        if (checkUserInDB(userid, password, botid))
        {
            userCache.put(userid, password);
            return true;
        }
        // (otherwise...)
        return false;
    }

    /**
     * Checks a userid/password combination in the database.
     * 
     * @param userid the userid to check
     * @param password the password to check
     * @param botid the botid for which to check this combination
     * @return whether the userid and password combination is valid
     */
    private boolean checkUserInDB(String userid, String password, String botid)
    {
        String passwordInDatabase = null;

        userid = userid.trim().toLowerCase();
        password = password.trim().toLowerCase();
        DbAccess dbaRef = null;
        try
        {
            dbaRef = this.dbManager.takeDbaRef();
        }
        catch (Exception e)
        {
            throw new UserError("Could not get database reference when checking user \"" + userid + "\" with password \"" + password + "\".", e);
        }
        try
        {
            ResultSet rs = dbaRef.executeQuery("select * from users where userid = '" + userid + "' and botid = '" + botid + "'");
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
                    this.dbManager.returnDbaRef(dbaRef);
                    return false;
                }
                if (returnCount > 1)
                {
                    throw new UserError(new DuplicateUserIDError(userid));
                }
            }
            rs.close();
            this.dbManager.returnDbaRef(dbaRef);
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

    /**
     * @see org.aitools.programd.multiplexor.Multiplexor#changePassword(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    @Override
    public boolean changePassword(String userid, String password, String botid)
    {
        userid = userid.trim().toLowerCase();
        password = password.trim().toLowerCase();
        DbAccess dbaRef = null;
        try
        {
            dbaRef = this.dbManager.takeDbaRef();
        }
        catch (Exception e)
        {
            throw new UserError("Could not get database reference when changing password to \"" + password + "\" for \"" + userid
                    + "\" as known to \"" + botid + "\".", e);
        }
        try
        {
            ResultSet rs = dbaRef.executeQuery("select * from users where userid = '" + userid + "' and botid = '" + botid + "'");
            int returnCount = 0;
            while (rs.next())
            {
                returnCount++;
            }
            if (returnCount == 0)
            {
                rs.close();
                this.dbManager.returnDbaRef(dbaRef);
                return (false);
            }
            dbaRef.executeUpdate("update users set password = '" + password + "' where userid = '" + userid + "' and botid = '" + botid + "'");
            rs.close();
            this.dbManager.returnDbaRef(dbaRef);
        }
        catch (SQLException e)
        {
            throw new UserError("Database error.", e);
        }
        Map<String, String> userCache = this.userCacheForBots.get(botid);
        userCache.remove(userid);
        userCache.put(userid, password);
        return true;
    }

    /**
     * @see org.aitools.programd.multiplexor.Multiplexor#useridCount(java.lang.String)
     */
    @Override
    public int useridCount(String botid)
    {
        return ((HashMap) this.userCacheForBots.get(botid)).size();
    }
}