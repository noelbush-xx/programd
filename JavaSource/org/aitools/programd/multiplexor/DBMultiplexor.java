/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.multiplexor;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.aitools.programd.Core;
import org.aitools.programd.CoreSettings;
import org.aitools.util.sql.DbAccess;
import org.aitools.util.sql.DbAccessRefsPoolMgr;
import org.aitools.util.resource.URLTools;
import org.aitools.util.runtime.DeveloperError;
import org.aitools.util.runtime.UserError;
import org.apache.log4j.Logger;

/**
 * A database-oriented {@link Multiplexor} . Uses a database for storage and retrieval of predicates.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @author Richard Wallace, Jon Baer
 * @author Thomas Ringate/Pedro Colla
 */
public class DBMultiplexor extends Multiplexor<Statement>
{
    /** A manager for database access. */
    private DbAccessRefsPoolMgr dbManager;

    /** The logger for database activity. */
    private Logger dbLogger;

    private Map<String, Map<String, String>> userCacheForBots = new HashMap<String, Map<String, String>>();

    /**
     * Creates a new DBMultiplexor with the given Core as owner.
     * 
     * @param core the Core that owns this DBMultiplexor
     */
    public DBMultiplexor(Core core)
    {
        super(core);
        this.dbLogger = Logger.getLogger("programd");
    }

    /**
     * Loads the database properties from the server configuration.
     */
    @Override
    public void initialize()
    {
        CoreSettings coreSettings = this._core.getSettings();

        this.dbLogger.debug("Opening database pool.");

        this.dbManager = new DbAccessRefsPoolMgr(coreSettings.getDatabaseDriver(), coreSettings.getDatabaseUrl(),
                coreSettings.getDatabaseUser(), coreSettings.getDatabasePassword());

        this.dbLogger.debug("Populating database pool.");

        this.dbManager.populate(coreSettings.getDatabaseConnections());
    }

    @Override
    @SuppressWarnings("unused")
    protected Statement getStorageMechanism(String userid, String botid)
    {
        DbAccess dbaRef = null;
        try
        {
            dbaRef = this.dbManager.takeDbaRef();
        }
        catch (Exception e)
        {
            throw new UserError("Could not get database reference.", e);
        }
        Statement statement = dbaRef.getStatement();
        try
        {
            statement.addBatch(String.format("START TRANSACTION"));
        }
        catch (SQLException e)
        {
            throw new DeveloperError("Error adding to statement batch.", e);
        }
        this.dbManager.returnDbaRef(dbaRef);
        return statement;
    }

    @Override
    protected void preparePredicateForStorage(Statement mechanism, String userid, String botid, String name, PredicateValue value)
    {
        name = URLTools.encodeUTF8(name);
        if (value.size() == 1)
        {
            try
            {
                mechanism.addBatch(createSetQuery(userid, botid, name, URLTools.encodeUTF8(value.getFirstValue())));
            }
            catch (SQLException e)
            {
                throw new DeveloperError("Error adding to statement batch.", e);
            }
        }
        else
        {
            for (int index = 1; index <= value.size(); index++)
            {
                try
                {
                    mechanism.addBatch(createSetQuery(userid, botid, name + '.' + (index), URLTools.encodeUTF8(value.get(index))));
                }
                catch (SQLException e)
                {
                    throw new DeveloperError("Error adding to statement batch.", e);
                }
            }
        }
    }
    
    /**
     * Constructs a query that will insert/update a row for a given predicate.
     * 
     * @param userid
     * @param botid
     * @param name
     * @param value
     * @return the query
     */
    private String createSetQuery(String userid, String botid, String name, String value)
    {
        return String.format("INSERT INTO `predicates` (`userid`, `botid`, `name`, `value`) VALUES ('%s', '%s', '%s', '%s') ON DUPLICATE KEY UPDATE `value` = VALUES(`value`)",
                userid, botid, name, value);
    }

    @Override
    @SuppressWarnings("unused")
    protected void savePredicates(Statement mechanism, String userid, String botid)
    {
        try
        {
            mechanism.addBatch(String.format("COMMIT;%n"));
        }
        catch (SQLException e)
        {
            throw new DeveloperError("Error adding to statement batch.", e);
        }
        try
        {
            mechanism.executeBatch();
        }
        catch (SQLException e)
        {
            throw new DeveloperError("Error executing statement batch.", e);
        }
    }

    /**
     * Loads the value of a predicate from a database.
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
        String result = null;
        DbAccess dbaRef = null;
        try
        {
            dbaRef = this.dbManager.takeDbaRef();
        }
        catch (Exception e)
        {
            throw new UserError("Could not get database reference.", e);
        }
        try
        {
            ResultSet records =
                dbaRef.executeQuery(
                        String.format("SELECT `value` FROM `predicates` WHERE `botid` = '%s' AND `userid` = '%s' AND `name` = '%s'", botid, userid, name));
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
            this.dbLogger.error("Database error.", e);
            throw new NoSuchPredicateException(name);
        }
        if (result == null)
        {
            throw new NoSuchPredicateException(name);
        }
        // If found, return it (don't forget to decode!).
        return URLTools.decodeUTF8(result);
    }

    /**
     * Creates a userid with a given password. If the userid already exists, returns false.
     * 
     * @param userid the userid to create
     * @param password the password to associate with the userid
     * @param botid the botid with whom to associate this userid/password combination
     * @throws DuplicateUserIDError if the given userid was already found in the database
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
            throw new UserError("Could not get database reference.", e);
        }
        try
        {
            ResultSet rs =
                dba.executeQuery(
                        String.format("SELECT * FROM `users` WHERE `userid` = '%s' AND `botid` = '%s'", userid, botid));
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
            dba.executeUpdate(
                    String.format("INSERT INTO `users` (`userid`, `password`, `botid`) VALUES ('%s', '%s', '%s')", userid, password, botid));
            rs.close();
        }
        catch (SQLException e)
        {
            throw new UserError("Error working with database.", e);
        }
        this.dbManager.returnDbaRef(dba);
    }

    /**
     * @see org.aitools.programd.multiplexor.Multiplexor#checkUser(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public boolean checkUser(String userid, String password, String botid)
    {
        // Look first to see if the user is already in the cache.
        if (!this.userCacheForBots.containsKey(botid))
        {
            this.userCacheForBots.put(botid, Collections.checkedMap(new HashMap<String, String>(), String.class,
                    String.class));
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
    protected boolean checkUserInDB(String userid, String password, String botid)
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
            throw new UserError("Could not get database reference.", e);
        }
        try
        {
            ResultSet rs =
                dbaRef.executeQuery(
                        String.format("SELECT * FROM `users` WHERE `userid` = '%s' AND `botid` = '%s'", userid, botid));
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
     * @see org.aitools.programd.multiplexor.Multiplexor#changePassword(java.lang.String, java.lang.String,
     *      java.lang.String)
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
            throw new UserError("Could not get database reference.", e);
        }
        try
        {
            ResultSet rs =
                dbaRef.executeQuery(
                        String.format("SELECT * FROM `users` WHERE `userid = '%s' AND `botid` = '%s'", userid, botid));
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
            dbaRef.executeUpdate(
                    String.format("UPDATE `users` SET `password` = '%s' WHERE `userid` = '%s' AND `botid` = '%s'", password, userid, botid));
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
