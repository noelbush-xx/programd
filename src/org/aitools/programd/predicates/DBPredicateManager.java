/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.predicates;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import org.aitools.programd.Core;
import org.aitools.util.runtime.DeveloperError;

/**
 * A database-oriented {@link PredicateManager} . Uses a database for storage and retrieval of predicates.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class DBPredicateManager extends PredicateManager
{
    private static final String LOAD_PREDICATE_SELECT =
        "SELECT `predicates`.`value` FROM `predicates` INNER JOIN `bots` ON `predicates`.`bot_id` = `bots`.`id` INNER JOIN `users` ON `predicates`.`user_id` = `users`.`id` WHERE `bots`.`name` = ? AND `users`.`name` = ? AND `predicates`.`name` = ?";
    
    private static final String SET_PREDICATE_INSERT = 
        "INSERT INTO `predicates` (`name`, `value`, `user_id`, `bot_id`) VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE `value` = VALUES(`value`)";
    
    private static final String CHECK_USER_SELECT =
        "SELECT FROM `users` INNER JOIN `bot_user` ON `users`.`id` = `bot_user`.`user_id` INNER JOIN `bots` ON `bots`.`id` = `bot_user`.`bot_id` WHERE `users`.`name` = ? AND `bots`.`name` = ?";
    
    private static final String CREATE_USER_INSERT =
        "INSERT INTO `users` (`user`, `bot`) VALUES (?, ?)";

    /**
     * Creates a new DBMultiplexor with the given Core as owner.
     * 
     * @param core the Core that owns this DBMultiplexor
     */
    public DBPredicateManager(Core core)
    {
        super(core);
    }

    /**
     * Prepares the {@link PreparedStatement}s that will be pooled and used for operations.
     */
    @Override
    public void initialize()
    {
        Connection connection = this._core.getDBConnection();
        
        // These will be pooled by the connection manager.
        try
        {
            connection.prepareStatement(LOAD_PREDICATE_SELECT);
            connection.prepareStatement(SET_PREDICATE_INSERT);
            connection.prepareStatement(CHECK_USER_SELECT);
            connection.prepareStatement(CREATE_USER_INSERT);
            connection.close();
        }
        catch (SQLException e)
        {
            throw new DeveloperError("SQL exception creating PreparedStatements.", e);
        }
    }
    
    /**
     * @see org.aitools.programd.predicates.PredicateManager#dumpPredicates()
     * 
     * TODO: get userid first
     */
    @SuppressWarnings("boxing")
    @Override
    public void dumpPredicates()
    {
        Connection connection = this._core.getDBConnection();
        PreparedStatement insert;
        try
        {
            insert = connection.prepareStatement(SET_PREDICATE_INSERT);
            insert.clearBatch();
            for (String bot : this._bots.keySet())
            {
                Map<String, PredicateMap> predicateCache = this._bots.get(bot).getPredicateCache();
                for (String user : predicateCache.keySet())
                {
                    PredicateMap predicateMap = predicateCache.get(user);
                    for (String name : predicateMap.keySet())
                    {
                        PredicateValue value = predicateMap.get(name);
                        insert.clearParameters();
                        if (value.size() == 1)
                        {
                            insert.setString(1, user);
                            insert.setString(2, bot);
                            insert.setString(3, name);
                            insert.setString(4, value.getFirstValue());
                            insert.addBatch();
                        }
                        else
                        {
                            for (int index = 1; index <= value.size(); index++)
                            {
                                insert.setString(1, user);
                                insert.setString(2, bot);
                                insert.setString(3, String.format("%s.%d", name, index));
                                insert.setString(4, value.get(index));
                                insert.addBatch();
                            }
                        }
                    }
                    predicateMap.clear();
                }
            }
            insert.executeBatch();
            connection.close();
        }
        catch (SQLException e)
        {
            throw new DeveloperError("SQL error dumping predicates.", e);
        }
    }

    /**
     * @see org.aitools.programd.predicates.PredicateManager#loadPredicate(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public String loadPredicate(String name, String user, String bot) throws NoSuchPredicateException
    {
        String result = null;
        Connection connection = this._core.getDBConnection();
        try
        {
            PreparedStatement select = connection.prepareStatement(LOAD_PREDICATE_SELECT);
            select.clearParameters();
            select.setString(1, bot);
            select.setString(2, user);
            select.setString(3, name);
            ResultSet records = select.executeQuery();
            int returnCount = 0;
            while (records.next())
            {
                returnCount++;
                result = records.getString("value");
            }
            records.close();
            connection.close();
        }
        catch (SQLException e)
        {
            this._logger.error("Database error.", e);
            throw new NoSuchPredicateException(name);
        }
        if (result == null)
        {
            throw new NoSuchPredicateException(name);
        }
        // If found, return it.
        return result;
    }
}
