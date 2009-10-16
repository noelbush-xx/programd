/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.graph;

import java.io.FileNotFoundException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aitools.util.resource.URLTools;
import org.aitools.util.runtime.UserError;
import org.apache.log4j.Logger;

/**
 * This is a database-based Nodemapper.  It does <i>not</i>
 * implement the {@link Nodemapper} interface, however.  Instead,
 * in the interest of optimal performance, it provides static
 * methods which cover the same functions as <code>Nodemapper</code>,
 * but without the need to create a separate in-memory object
 * for each node.  Each method takes a {@link Connection} argument,
 * and an <code>int</code> node identifier, in addition to the usual
 * arguments required by a <code>Nodemapper</code>.
 * 
 * This will work best if the underlying dbms is able to support
 * PreparedStatement pooling.
 * 
 * Some methods having to do with size and height are not implemented,
 * on the theory that they are not particularly needed for a
 * database-based implementation (which has other means of optimization
 * open to it).  There are also some special methods dealing with filenames,
 * templates, etc. because we maintain special tables for those.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class DBNodemapper
{
    private static final Logger LOGGER = Logger.getLogger("programd.database.dbnodemapper");
    
    /**
     * Returns whether the given node points to another node via an
     * edge labeled by the given key.
     * 
     * @param connection 
     * @param node 
     * @param key 
     * @return whether the given node points to another node via an edge labeled by the given key
     * @see org.aitools.programd.graph.Nodemapper#containsKey(java.lang.String)
     */
    public static boolean containsKey(Connection connection, int node, String key)
    {
        int count = -1;
        try
        {
            PreparedStatement select = connection.prepareStatement("SELECT COUNT(*) FROM `edges` WHERE `from_node_id` = ? AND `label` = ?");
            select.setInt(1, node);
            select.setString(2, key);
            ResultSet results = select.executeQuery();
            results.next();
            count = results.getInt(1);
            results.close();
            select.close();
            assert count < 2 : "Database schema is in error!  Allows duplicate edges!";
        }
        catch (SQLException e)
        {
            LOGGER.error(e);
        }
        return (count == 1);
    }

    /**
     * Returns the node (if any) to which the given node points via
     * the given key.
     * 
     * @param connection 
     * @param node 
     * @param key 
     * @return the node to which the given node points via the given key
     * @see org.aitools.programd.graph.Nodemapper#get(java.lang.String)
     */
    public static int get(Connection connection, int node, String key)
    {
        int toNode = -1;
        try
        {
            PreparedStatement select = connection.prepareStatement("SELECT `to_node_id` FROM `edges` WHERE `from_node_id` = ? AND `label` = ?");
            select.setInt(1, node);
            select.setString(2, key);
            ResultSet results = select.executeQuery();
            if (results.next())
            {
                toNode = results.getInt(1);
            }
            results.close();
            select.close();
        }
        catch (SQLException e)
        {
            LOGGER.error(e);
        }
        return toNode;
    }

    /**
     * Returns the number of edges pointing from the given node
     * 
     * @param connection 
     * @param node 
     * @return the number of edges pointing from the given node
     */
    public static int size(Connection connection, int node)
    {
        int count = -1;
        try
        {
            PreparedStatement select = connection.prepareStatement("SELECT COUNT(`from_node_id`) FROM `edges` WHERE `from_node_id` = ?");
            select.setInt(1, node);
            ResultSet results = select.executeQuery();
            if (results.next())
            {
                count = results.getInt(1);
            }
            results.close();
            select.close();
        }
        catch (SQLException e)
        {
            LOGGER.error(e);
        }
        return count;
    }

    /**
     * Returns the parent of the given node.
     * 
     * @param connection 
     * @param node 
     * @return the parent of the given node
     * @see org.aitools.programd.graph.Nodemapper#getParent()
     */
    public static int getParent(Connection connection, int node)
    {
        int result = -1;
        try
        {
            PreparedStatement select = connection.prepareStatement("SELECT `from_node_id` from `edges` WHERE `to_node_id` = ?");
            select.setInt(1, node);
            ResultSet results = select.executeQuery();
            if (results.next())
            {
                result = results.getInt(1);
            }
            results.close();
            select.close();
        }
        catch (SQLException e)
        {
            LOGGER.error(e);
        }
        return result;
    }

    /**
     * Creates a new edge labeled with the given key from the
     * given node to a newly created node.
     * 
     * @param connection 
     * @param node 
     * @param key 
     * @return the node created by mapping the given key to the node
     * @see org.aitools.programd.graph.Nodemapper#put(java.lang.String, java.lang.Object)
     */
    public static int put(Connection connection, int node, String key)
    {
        int to_node = -1;
        try
        {
            // See if this edge exists already.
            PreparedStatement select = connection.prepareStatement("SELECT `to_node_id` FROM `edges` WHERE `from_node_id` = ? AND `label` = ?");
            select.setInt(1, node);
            select.setString(2, key);
            ResultSet edgeCheck = select.executeQuery();
            boolean exists = edgeCheck.next();
            edgeCheck.close();
            select.close();
            if (!exists)
            {
                PreparedStatement insert = connection.prepareStatement("INSERT INTO `nodes` () VALUES ()");
                ResultSet results = insert.executeQuery();
                results.next();
                to_node = results.getInt(1);
                results.close();
                insert.close();
                
                insert = connection.prepareStatement("INSERT INTO `edges` (`from_node_id`, `label`, `to_node_id`) VALUES (?, ?, ?)");
                insert.setInt(1, node);
                insert.setString(2, key);
                insert.setInt(3, to_node);
                insert.execute();
                insert.close();
            }
            else
            {
                throw new IllegalArgumentException("Trying to recreate edge that already exists.");
            }
        }
        catch (SQLException e)
        {
            LOGGER.error(e);
        }
        return to_node;
    }

    /**
     * Removes the edge that maps <code>to_node</code> to <code>from_node</code>.
     * 
     * TODO: Determine under what circumstances this should also remove all
     * instances of <code>to_node</code> in other tables.
     * 
     * @param connection 
     * @param from_node 
     * @param to_node 
     * @see org.aitools.programd.graph.Nodemapper#remove(java.lang.Object)
     */
    public static void remove(Connection connection, int from_node, int to_node)
    {
        try
        {
            PreparedStatement delete = connection.prepareStatement("DELETE FROM `edges` WHERE `from_node_id` = ? AND `to_node_id` = ?");
            delete.setInt(1, from_node);
            delete.setInt(2, to_node);
            delete.execute();
            delete.close();
        }
        catch (SQLException e)
        {
            LOGGER.error(e);
        }
    }
    
    /**
     * Adds the given filename to the list of filenames associated with
     * the given node.
     * 
     * @param connection
     * @param node
     * @param filename
     */
    public static void addFilename(Connection connection, int node, URL filename)
    {
        String path = filename.toExternalForm();
        int filenode = -1;
        
        try
        {
            // See if the file is already in the database.
            PreparedStatement select = connection.prepareStatement("SELECT `id` from `files` WHERE `path` = ?");
            select.setString(1, path);
            ResultSet results = select.executeQuery();
            if (results.next())
            {
                filenode = results.getInt(1);
            }
            results.close();
            select.close();
            
            // If the filename was not found, add it.
            if (filenode == -1)
            {
                PreparedStatement insert = connection.prepareStatement("INSERT INTO `files` (`path`) VALUES (?)");
                insert.setString(1, path);
                results = insert.executeQuery();
                results.next();
                filenode = results.getInt(1);
                results.close();
                insert.close();
            }
            
            // Now create the association.
            PreparedStatement insert = connection.prepareStatement("INSERT INTO `file_node` (`file_id`, `node_id`) VALUES (?, ?))");
            insert.setInt(1, filenode);
            insert.setInt(2, node);
            insert.execute();
            insert.close();
        }
        catch (SQLException e)
        {
            LOGGER.error(e);
        }
    }
    
    /**
     * Removes any filenames currently associated with the given node, and
     * associates the given one with the node.
     * 
     * @param connection
     * @param node
     * @param filename
     */
    public static void setFilename(Connection connection, int node, URL filename)
    {
        try
        {
            PreparedStatement delete = connection.prepareStatement("DELETE FROM `file_node` WHERE `node_id` = ?");
            delete.setInt(1, node);
            delete.close();
            addFilename(connection, node, filename);
        }
        catch (SQLException e)
        {
            LOGGER.error(e);
        }
    }
    
    /**
     * Returns a list of filenames associated with the given node.
     * 
     * @param connection
     * @param node
     * @return the filenames associated with the given node
     */
    public static List<String> getFilenames(Connection connection, int node)
    {
        List<String> result = new ArrayList<String>();
        try
        {
            PreparedStatement select =
                connection.prepareStatement("SELECT `files`.`path` from `file_node` INNER JOIN `files` ON `file_node`.`file_id` = `files`.`id` WHERE `file_node`.`node_id` = ?");
            select.setInt(1, node);
            ResultSet results = select.executeQuery();
            while (results.next())
            {
                result.add(results.getString(1));
            }
            results.close();
            select.close();
        }
        catch (SQLException e)
        {
            LOGGER.error(e);
        }
        return result;
    }
    
    /**
     * Returns the template attached to the given node.
     * 
     * @param connection
     * @param node
     * @return the template attached to the given node
     */
    public static String getTemplate(Connection connection, int node)
    {
        String template = null;
        try
        {
            PreparedStatement select =
                connection.prepareStatement(
                        "SELECT `label` from `templates` INNER JOIN `node_template` ON `node_template`.`template_id` = `templates`.`id` WHERE `node_template`.`node_id` = ?");
            select.setInt(1, node);
            ResultSet results = select.executeQuery();
            if (results.next())
            {
                template = results.getString(1);
            }
            results.close();
            select.close();
        }
        catch (SQLException e)
        {
            LOGGER.error(e);
        }
        return template;
    }
    
    /**
     * Returns the id of the template (but not its content) attached to
     * the given node.
     * 
     * @param connection
     * @param node
     * @return the id of the template (but not its content) attached to the given node
     */
    public static int getTemplateID(Connection connection, int node)
    {
        int id = -1;
        try
        {
            PreparedStatement select =
                connection.prepareStatement("SELECT `template_id` from `node_template` WHERE `node_id` = ?");
            select.setInt(1, node);
            ResultSet results = select.executeQuery();
            if (results.next())
            {
                id = results.getInt(1);
            }
            results.close();
            select.close();
        }
        catch (SQLException e)
        {
            LOGGER.error(e);
        }
        return id;
    }
    
    /**
     * Sets the template associated with the given node.
     * 
     * @param connection
     * @param node
     * @param template
     */
    public static void setTemplate(Connection connection, int node, String template)
    {
        int templateID = -1;
        try
        {
            PreparedStatement insert =
                connection.prepareStatement("INSERT INTO `templates` (`label`) VALUES (?)");
            insert.setString(1, template);
            ResultSet results = insert.executeQuery();
            if (results.next())
            {
                templateID = results.getInt(1);
            }
            results.close();
            insert.close();
        }
        catch (SQLException e)
        {
            LOGGER.error(e);
        }
        setTemplateByID(connection, node, templateID);
    }
    
    /**
     * Sets the id of the template associated with the given node.
     * The template id must correspond to an existing template.
     * 
     * @param connection
     * @param node
     * @param template
     */
    public static void setTemplateByID(Connection connection, int node, int template)
    {
        try
        {
            PreparedStatement insert =
                connection.prepareStatement("INSERT INTO `node_template` (`node_id`, `template_id`) VALUES (?, ?)");
            insert.setInt(1, node);
            insert.setInt(2, template);
            insert.execute();
            insert.close();
        }
        catch (SQLException e)
        {
            LOGGER.error(e);
        }
    }
    
    /**
     * Returns the filenames associated with the given bot.
     * 
     * @param connection
     * @param bot
     * @return the filenames associated with the given bot
     */
    public static List<URL> getFilenamesForBot(Connection connection, String bot)
    {
        List<URL> result = new ArrayList<URL>();
        try
        {
            PreparedStatement select =
                connection.prepareStatement(
                        "SELECT `files`.`path` FROM `bot_file` INNER JOIN `files` ON `bot_file`.`file_id` = `files`.`id` INNER JOIN `bots` ON `bot_file`.`bot_id` = `bots`.`id` WHERE `bots`.`id` = (SELECT `id` FROM `bots` WHERE `label` = ?)");
            select.setString(1, bot);
            ResultSet results = select.executeQuery();
            while (results.next())
            {
                String filename = results.getString(1);
                try
                {
                    result.add(URLTools.createValidURL(filename));
                }
                catch (FileNotFoundException e)
                {
                    throw new UserError("Invalid filename attached to bot!", e);
                }
            }
            results.close();
            select.close();
        }
        catch (SQLException e)
        {
            LOGGER.error(e);
        }
        return result;
    }
    
    /**
     * Returns the bot labels associated with the given filename.
     * 
     * @param connection
     * @param filename
     * @return the botids associated with the given filename
     */
    public static List<String> getBotsForFilename(Connection connection, URL filename)
    {
        List<String> result = new ArrayList<String>();
        try
        {
            PreparedStatement select =
                connection.prepareStatement(
                        "SELECT `bots`.`label` FROM `bot_file` INNER JOIN `files` ON `bot_file`.`file_id` = `files`.`id` INNER JOIN `bots` ON `bots`.`id` = `bot_file`.`id` WHERE `files`.`path` = ?");
            select.setString(1, filename.toExternalForm());
            ResultSet results = select.executeQuery();
            while (results.next())
            {
                result.add(results.getString(1));
            }
            results.close();
            select.close();
        }
        catch (SQLException e)
        {
            LOGGER.error(e);
        }
        return result;
    }
    
    /**
     * Creates an association between the given bot and filename.
     * 
     * @param connection
     * @param bot
     * @param filename
     */
    protected static void associateBotWithFile(Connection connection, String bot, URL filename)
    {
        try
        {
            PreparedStatement insert =
                connection.prepareStatement("INSERT INTO `bot_file` (`bot_id`, `file_id`) VALUES (bot_id, file_id) WHERE bot_id = (SELECT `id` from `bots` WHERE `label` = ?) AND file_id = (SELECT `id` FROM `files` WHERE `path` = ?)");
            insert.setString(1, bot);
            insert.setString(2, filename.toExternalForm());
            insert.execute();
            insert.close();
        }
        catch (SQLException e)
        {
            LOGGER.error(e);
        }
    }
    
    /**
     * Removes the association between the given botid and the given filename.
     * 
     * @param connection
     * @param bot
     * @param filename
     */
    public static void removeBotIDFromFilename(Connection connection, String bot, URL filename)
    {
        try
        {
            PreparedStatement delete =
                connection.prepareStatement(
                        "DELETE FROM `botid_file` WHERE `bot_id` = (SELECT `id` FROM `bots` WHERE `label` = ?) AND `file_id` = (SELECT `id` FROM `files` WHERE `path` = ?)");
            delete.setString(1, bot);
            delete.setString(2, filename.toExternalForm());
            delete.execute();
        }
        catch (SQLException e)
        {
            LOGGER.error(e);
        }
    }
    
    /**
     * Associates the given file with the given botid node.
     * 
     * @param connection
     * @param node
     * @param file
     */
    public static void storeBotIDNodeFile(Connection connection, int node, URL file)
    {
        try
        {
            PreparedStatement insert =
                connection.prepareStatement("INSERT INTO `botidnode_file` (`botidnode_id`, `file_id`) VALUES (?, file_id) WHERE file_id = (SELECT `id` FROM `files` WHERE `path` = ?)");
            insert.setInt(1, node);
            insert.setString(2, file.toExternalForm());
            insert.execute();
            insert.close();
        }
        catch (SQLException e)
        {
            LOGGER.error(e);
        }
    }
    
    /**
     * Returns the botid nodes associated with the given file.
     * 
     * @param connection
     * @param file
     * @return the botid nodes associated with the given file
     */
    @SuppressWarnings("boxing")
    public static Set<Integer> getBotIDNodesForFile(Connection connection, URL file)
    {
        Set<Integer> result = new HashSet<Integer>();
        try
        {
            PreparedStatement select =
                connection.prepareStatement(
                        "SELECT `botidnode` from `botidnode_file` INNER JOIN `botidnode_file` ON `files`.`path` = ?");
            select.setString(2, file.toExternalForm());
            ResultSet results = select.executeQuery();
            while (results.next())
            {
                result.add(results.getInt(1));
            }
            results.close();
            select.close();
        }
        catch (SQLException e)
        {
            LOGGER.error(e);
        }
        return result;
    }
    
    /**
     * Returns the timestamp that the given file was last loaded.
     * 
     * @param connection
     * @param file
     * @return the timestamp that the file was last loaded
     */
    public static long getLastLoaded(Connection connection, URL file)
    {
        long result = -1;
        try
        {
            PreparedStatement select =
                connection.prepareStatement(
                        "SELECT `last_loaded` FROM `files` WHERE `path` = ?");
            select.setString(1, file.toExternalForm());
            ResultSet results = select.executeQuery();
            if (results.next())
            {
                result = results.getLong(1);
            }
            results.close();
            select.close();
        }
        catch (SQLException e)
        {
            LOGGER.error(e);
        }
        return result;
    }
    
    /**
     * Checks whether the given file is already present in the database.
     * 
     * @param connection
     * @param file
     * @return whether the given file is already present in the database
     */
    public static boolean fileIsAlreadyPresent(Connection connection, URL file)
    {
        int count = 0;
        try
        {
            PreparedStatement select =
                connection.prepareStatement(
                        "SELECT COUNT(*) FROM `files` WHERE `path` = ?");
            select.setString(2, file.toExternalForm());
            ResultSet results = select.executeQuery();
            if (results.next())
            {
                count = results.getInt(1);
            }
            results.close();
            select.close();
        }
        catch (SQLException e)
        {
            LOGGER.error(e);
        }
        return (count > 0);
    }
    
    /**
     * Checks whether the given file is already present in the database.
     * 
     * @param connection
     * @param file
     * @param bot
     * @return whether the given file is already present in the database
     */
    public static boolean fileIsAlreadyPresentForBot(Connection connection, URL file, String bot)
    {
        int count = 0;
        try
        {
            PreparedStatement select =
                connection.prepareStatement(
                        "SELECT COUNT(*) FROM `files` INNER JOIN `bot_file` ON `files`.`id` = `bot_file`.`file_id` INNER JOIN `bot_file` ON `bots`.`id` = `bot_file`.`bot_id` WHERE `files`.`path` = ? AND `bots`.`label` = ?");
            select.setString(1, file.toExternalForm());
            select.setString(2, bot);
            ResultSet results = select.executeQuery();
            if (results.next())
            {
                count = results.getInt(1);
            }
            results.close();
            select.close();
        }
        catch (SQLException e)
        {
            LOGGER.error(e);
        }
        return (count > 0);
    }
}
