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
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.aitools.util.db.Entity;
import org.aitools.util.resource.URLTools;
import org.aitools.util.runtime.DeveloperError;
import org.aitools.util.runtime.UserError;

/**
 * This is a database-based Nodemapper. It does <i>not</i> implement the {@link Nodemapper} interface, however. Instead,
 * in the interest of optimal performance, it provides static methods which cover the same functions as
 * <code>Nodemapper</code>, but without the need to create a separate in-memory object for each node. Each method takes
 * a {@link Connection} argument, and an <code>int</code> node identifier, in addition to the usual arguments required
 * by a <code>Nodemapper</code>.
 * 
 * This will work best if the underlying DBMS is able to support PreparedStatement pooling.
 * 
 * Some methods having to do with size and height are not implemented, on the theory that they are not particularly
 * needed for a database-based implementation (which has other means of optimization open to it). There are also some
 * special methods dealing with filenames, templates, etc. because we maintain special tables for those.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class SlowDBNodemapper {

  /**
   * Adds the given filename to the list of filenames associated with the given node.
   * 
   * @param connection
   * @param node
   * @param filename
   */
  @SuppressWarnings("boxing")
  public static void addFilename(Connection connection, int node, URL filename) {

    int file_id = Entity.getOrCreate(connection, "file", "path", filename.toExternalForm());

    try {
      PreparedStatement insert = connection
          .prepareStatement("INSERT INTO file_node (file_id, node_id) VALUES (?, ?)");
      insert.setInt(1, file_id);
      insert.setInt(2, node);
      insert.execute();
      insert.close();
    }
    catch (SQLException e) {
      throw new DeveloperError(String.format("SQL error trying to associate filename \"%s\" with node %d.", filename.toExternalForm(), node), e);
    }
  }

  /**
   * Creates an association between the given bot and filename.
   * 
   * @param connection
   * @param bot
   * @param filename
   */
  protected static void associateBotWithFile(Connection connection, String bot, URL filename) {

    int bot_id = Entity.getOrCreate(connection, "bot", "label", bot);
    int file_id = Entity.getOrCreate(connection, "file", "path", filename.toExternalForm());
    
    try {
      PreparedStatement insert = connection
          .prepareStatement("INSERT INTO bot_file (bot_id, file_id) VALUES (?, ?)");
      insert.setInt(1, bot_id);
      insert.setInt(2, file_id);
      insert.execute();
      insert.close();
    }
    catch (SQLException e) {
      throw new DeveloperError(String.format("SQL error trying to associate bot \"%s\" with file \"%s\".", bot, filename.toExternalForm()), e);
    }
  }

  /**
   * Returns whether the given node points to another node via an edge labeled by the given key.
   * 
   * @param connection
   * @param node
   * @param key
   * @return whether the given node points to another node via an edge labeled by the given key
   * @see org.aitools.programd.graph.Nodemapper#containsKey(java.lang.String)
   */
  @SuppressWarnings("boxing")
  public static boolean containsKey(Connection connection, int node, String key) {
    boolean result;
    try {
      PreparedStatement select = connection
          .prepareStatement("SELECT 1 FROM edge WHERE from_node_id = ? AND label = ?");
      select.setInt(1, node);
      select.setString(2, key);
      ResultSet results = select.executeQuery();
      result = results.next();
      results.close();
      select.close();
    }
    catch (SQLException e) {
      throw new DeveloperError(String.format("SQL error trying to check whether node %d maps to key \"%s\".", node, key), e);
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
  public static boolean fileIsAlreadyPresent(Connection connection, URL file) {
    boolean result;
    try {
      PreparedStatement select = connection.prepareStatement("SELECT 1 FROM file WHERE path = ?");
      select.setString(1, file.toExternalForm());
      ResultSet results = select.executeQuery();
      result = results.next();
      results.close();
      select.close();
    }
    catch (SQLException e) {
      throw new DeveloperError(String.format("SQL error trying to check whether file \"%s\" is already present.", file.toExternalForm()), e);
    }
    return result;
  }

  /**
   * Checks whether the given file is already present in the database.
   * 
   * @param connection
   * @param file
   * @param bot
   * @return whether the given file is already present in the database
   */
  public static boolean fileIsAlreadyPresentForBot(Connection connection, URL file, String bot) {
    boolean result;
    try {
      PreparedStatement select = connection
          .prepareStatement("SELECT 1 FROM file INNER JOIN bot_file ON file.id = bot_file.file_id INNER JOIN bot ON bot.id = bot_file.bot_id WHERE file.path = ? AND bot.label = ?");
      select.setString(1, file.toExternalForm());
      select.setString(2, bot);
      ResultSet results = select.executeQuery();
      result = results.next();
      results.close();
      select.close();
    }
    catch (SQLException e) {
      throw new DeveloperError(String.format("SQL error trying to check whether file \"%s\" is already present for bot \"%s\".", file.toExternalForm(), bot), e);
    }
    return result;
  }

  /**
   * Returns the node (if any) to which the given node points via the given key.
   * 
   * @param connection
   * @param node
   * @param key
   * @return the node to which the given node points via the given key
   * @see org.aitools.programd.graph.Nodemapper#get(java.lang.String)
   */
  @SuppressWarnings("boxing")
  public static int get(Connection connection, int node, String key) {
    int toNode = -1;
    try {
      PreparedStatement select = connection
          .prepareStatement("SELECT to_node_id FROM edge WHERE from_node_id = ? AND label = ?");
      select.setInt(1, node);
      select.setString(2, key);
      ResultSet results = select.executeQuery();
      if (results.next()) {
        toNode = results.getInt(1);
      }
      results.close();
      select.close();
    }
    catch (SQLException e) {
      throw new DeveloperError(String.format("SQL error trying to get node %d mapped via key \"%s\".", node, key), e);
    }
    return toNode;
  }

  /**
   * Returns the botid nodes associated with the given file.
   * 
   * @param connection
   * @param file
   * @return the botid nodes associated with the given file
   */
  @SuppressWarnings("boxing")
  public static Set<Integer> getBotIDNodesForFile(Connection connection, URL file) {
    Set<Integer> result = new HashSet<Integer>();
    try {
      PreparedStatement select = connection
          .prepareStatement("SELECT botidnode_id from botidnode_file INNER JOIN file ON botidnode_file.file_id = file.id WHERE file.path = ?");
      select.setString(1, file.toExternalForm());
      ResultSet results = select.executeQuery();
      while (results.next()) {
        result.add(results.getInt(1));
      }
      results.close();
      select.close();
    }
    catch (SQLException e) {
      throw new DeveloperError(String.format("SQL error trying to get botid nodes for file \"%s\".", file.toExternalForm()), e);
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
  public static List<String> getBotsForFilename(Connection connection, URL filename) {
    List<String> result = new ArrayList<String>();
    try {
      PreparedStatement select = connection
          .prepareStatement("SELECT bot.label FROM bot_file INNER JOIN file ON bot_file.file_id = file.id INNER JOIN bot ON bot.id = bot_file.id WHERE file.path = ?");
      select.setString(1, filename.toExternalForm());
      ResultSet results = select.executeQuery();
      while (results.next()) {
        result.add(results.getString(1));
      }
      results.close();
      select.close();
    }
    catch (SQLException e) {
      throw new DeveloperError(String.format("SQL error trying to get bots for filename \"%s\".", filename.toExternalForm()), e);
    }
    return result;
  }

  /**
   * Returns a list of filenames associated with the given node.
   * 
   * @param connection
   * @param node
   * @return the filenames associated with the given node
   */
  @SuppressWarnings("boxing")
  public static List<String> getFilenames(Connection connection, int node) {
    List<String> result = new ArrayList<String>();
    try {
      PreparedStatement select = connection
          .prepareStatement("SELECT file.path from file_node INNER JOIN file ON file_node.file_id = file.id WHERE file_node.node_id = ?");
      select.setInt(1, node);
      ResultSet results = select.executeQuery();
      while (results.next()) {
        result.add(results.getString(1));
      }
      results.close();
      select.close();
    }
    catch (SQLException e) {
      throw new DeveloperError(String.format("SQL error trying to get filenames associated with node %d.", node), e);
    }
    return result;
  }

  /**
   * Returns the filenames associated with the given bot.
   * 
   * @param connection
   * @param bot
   * @return the filenames associated with the given bot
   */
  public static List<URL> getFilenamesForBot(Connection connection, String bot) {
    List<URL> result = new ArrayList<URL>();
    try {
      PreparedStatement select = connection
          .prepareStatement("SELECT file.path FROM bot_file INNER JOIN file ON bot_file.file_id = file.id INNER JOIN bot ON bot_file.bot_id = bot.id WHERE bot.id = (SELECT id FROM bot WHERE label = ?)");
      select.setString(1, bot);
      ResultSet results = select.executeQuery();
      while (results.next()) {
        String filename = results.getString(1);
        try {
          result.add(URLTools.createValidURL(filename));
        }
        catch (FileNotFoundException e) {
          throw new UserError(String.format("Invalid filename \"%s\" attached to bot \"%s\"!", filename, bot), e);
        }
      }
      results.close();
      select.close();
    }
    catch (SQLException e) {
      throw new DeveloperError(String.format("SQL error trying to get filenames associated with bot \"%s\".", bot), e);
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
  public static long getLastLoaded(Connection connection, URL file) {
    long result = -1;
    try {
      PreparedStatement select = connection.prepareStatement("SELECT last_loaded FROM file WHERE path = ?");
      select.setString(1, file.toExternalForm());
      ResultSet results = select.executeQuery();
      if (results.next()) {
        result = results.getTimestamp(1).getTime();
      }
      results.close();
      select.close();
    }
    catch (SQLException e) {
      throw new DeveloperError(String.format("SQL error trying to get last-loaded time for file \"%s\".", file.toExternalForm()), e);
    }
    return result;
  }

  /**
   * Returns the parent of the given node.
   * 
   * @param connection
   * @param node
   * @return the parent of the given node
   * @see org.aitools.programd.graph.Nodemapper#getParent()
   */
  @SuppressWarnings("boxing")
  public static int getParent(Connection connection, int node) {
    int result = -1;
    try {
      PreparedStatement select = connection
          .prepareStatement("SELECT from_node_id FROM edge WHERE to_node_id = ?");
      select.setInt(1, node);
      ResultSet results = select.executeQuery();
      if (results.next()) {
        result = results.getInt(1);
      }
      results.close();
      select.close();
    }
    catch (SQLException e) {
      throw new DeveloperError(String.format("SQL error trying to get parent of node %d.", node), e);
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
  @SuppressWarnings("boxing")
  public static String getTemplate(Connection connection, int node) {
    String template = null;
    try {
      PreparedStatement select = connection
          .prepareStatement("SELECT text from template INNER JOIN node_template ON node_template.template_id = template.id WHERE node_template.node_id = ?");
      select.setInt(1, node);
      ResultSet results = select.executeQuery();
      if (results.next()) {
        template = results.getString(1);
      }
      results.close();
      select.close();
    }
    catch (SQLException e) {
      throw new DeveloperError(String.format("SQL error trying to get template attached to node %d.", node), e);
    }
    return template;
  }

  /**
   * Returns the id of the template (but not its content) attached to the given node.
   * 
   * @param connection
   * @param node
   * @return the id of the template (but not its content) attached to the given node
   */
  @SuppressWarnings("boxing")
  public static int getTemplateID(Connection connection, int node) {
    int id = -1;
    try {
      PreparedStatement select = connection
          .prepareStatement("SELECT template_id FROM node_template WHERE node_id = ?");
      select.setInt(1, node);
      ResultSet results = select.executeQuery();
      if (results.next()) {
        id = results.getInt(1);
      }
      results.close();
      select.close();
    }
    catch (SQLException e) {
      throw new DeveloperError(String.format("SQL error trying to get id of template attached to node %d.", node), e);
    }
    return id;
  }

  /**
   * Creates a new edge labeled with the given key from the given node to a newly created node.
   * 
   * @param connection
   * @param from_node
   * @param key
   * @return the node created by mapping the given key to the node
   * @see org.aitools.programd.graph.Nodemapper#put(java.lang.String, java.lang.Object)
   */
  @SuppressWarnings("boxing")
  public static int put(Connection connection, int from_node, String key) {
    int to_node = -1;
    try {
      // See if this edge exists already.
      PreparedStatement select = connection
          .prepareStatement("SELECT to_node_id FROM edge WHERE from_node_id = ? AND label = ?");
      select.setInt(1, from_node);
      select.setString(2, key);
      ResultSet edgeCheck = select.executeQuery();
      boolean exists = edgeCheck.next();
      edgeCheck.close();
      select.close();
      if (!exists) {
        PreparedStatement insert = connection.prepareStatement("INSERT INTO node () VALUES ()", Statement.RETURN_GENERATED_KEYS);
        insert.execute();
        ResultSet results = insert.getGeneratedKeys();
        if (results.next()) {
          to_node = results.getInt(1);
        }
        else {
          throw new DeveloperError(String.format("No node id generated!"), new NullPointerException());
        }
        results.close();
        insert.close();

        insert = connection
            .prepareStatement("INSERT INTO edge (from_node_id, label, to_node_id) VALUES (?, ?, ?)");
        insert.setInt(1, from_node);
        insert.setString(2, key);
        insert.setInt(3, to_node);
        insert.execute();
        insert.close();
      }
      else {
        throw new IllegalArgumentException("Trying to recreate edge that already exists.");
      }
    }
    catch (SQLException e) {
      throw new DeveloperError(String.format("SQL error trying to create edge mapping from node %d to node %d via key \"%s\".", from_node, to_node, key), e);
    }
    return to_node;
  }

  /**
   * Removes the edge that maps <code>to_node</code> to <code>from_node</code>.
   * 
   * TODO: Determine under what circumstances this should also remove all instances of <code>to_node</code> in other
   * tables.
   * 
   * @param connection
   * @param from_node
   * @param to_node
   * @see org.aitools.programd.graph.Nodemapper#remove(java.lang.Object)
   */
  @SuppressWarnings("boxing")
  public static void remove(Connection connection, int from_node, int to_node) {
    try {
      PreparedStatement delete = connection
          .prepareStatement("DELETE FROM edge WHERE from_node_id = ? AND to_node_id = ?");
      delete.setInt(1, from_node);
      delete.setInt(2, to_node);
      delete.execute();
      delete.close();
    }
    catch (SQLException e) {
      throw new DeveloperError(String.format("SQL error trying to remove edge from node %d to node %d.", from_node, to_node), e);
    }
  }

  /**
   * Removes the association between the given botid and the given filename.
   * 
   * @param connection
   * @param bot
   * @param filename
   */
  public static void removeBotIDFromFilename(Connection connection, String bot, URL filename) {

    int bot_id = Entity.getOrCreate(connection, "bot", "label", bot);
    int file_id = Entity.getOrCreate(connection, "file", "path", filename.toExternalForm());
    
    try {
      PreparedStatement delete = connection
          .prepareStatement("DELETE FROM botidnode_file WHERE botidnode_id = ? AND file_id = ?");
      delete.setInt(1, bot_id);
      delete.setInt(2, file_id);
      delete.execute();
    }
    catch (SQLException e) {
      throw new DeveloperError(String.format("SQL error trying to remove association between botid for \"%s\" and filename \"%s\".", bot, filename.toExternalForm()), e);
    }
  }

  /**
   * Removes any filenames currently associated with the given node, and associates the given one with the node.
   * 
   * @param connection
   * @param node
   * @param filename
   */
  @SuppressWarnings("boxing")
  public static void setFilename(Connection connection, int node, URL filename) {
    try {
      PreparedStatement delete = connection.prepareStatement("DELETE FROM file_node WHERE node_id = ?");
      delete.setInt(1, node);
      delete.close();
      addFilename(connection, node, filename);
    }
    catch (SQLException e) {
      throw new DeveloperError(String.format("SQL error trying to remove filenames associated with node %d.", node), e);
    }
  }

  /**
   * Sets the template associated with the given node.
   * 
   * @param connection
   * @param node
   * @param template
   */
  @SuppressWarnings("boxing")
  public static void setTemplate(Connection connection, int node, String template) {
    int templateID = -1;
    try {
      PreparedStatement insert = connection.prepareStatement("INSERT INTO template (text) VALUES (?)");
      insert.setString(1, template);
      insert.execute();
      ResultSet results = insert.getGeneratedKeys();
      if (results.next()) {
        templateID = results.getInt(1);
      }
      else {
        throw new DeveloperError(String.format("No template id generated!"), new NullPointerException());
      }
      results.close();
      insert.close();
    }
    catch (SQLException e) {
      throw new DeveloperError(String.format("SQL error trying to attach template to node %d.", node), e);
    }
    setTemplateByID(connection, node, templateID);
  }

  /**
   * Sets the id of the template associated with the given node. The template id must correspond to an existing
   * template.
   * 
   * @param connection
   * @param node
   * @param template
   */
  @SuppressWarnings("boxing")
  public static void setTemplateByID(Connection connection, int node, int template) {
    try {
      PreparedStatement insert = connection
          .prepareStatement("INSERT INTO node_template (node_id, template_id) VALUES (?, ?)");
      insert.setInt(1, node);
      insert.setInt(2, template);
      insert.execute();
      insert.close();
    }
    catch (SQLException e) {
      throw new DeveloperError(String.format("SQL error trying to attach template id %d to node %d.", template, node), e);
    }
  }

  /**
   * Returns the number of edges pointing from the given node
   * 
   * @param connection
   * @param node
   * @return the number of edges pointing from the given node
   */
  @SuppressWarnings("boxing")
  public static int size(Connection connection, int node) {
    int count = -1;
    try {
      PreparedStatement select = connection
          .prepareStatement("SELECT COUNT(from_node_id) FROM edge WHERE from_node_id = ?");
      select.setInt(1, node);
      ResultSet results = select.executeQuery();
      if (results.next()) {
        count = results.getInt(1);
      }
      results.close();
      select.close();
    }
    catch (SQLException e) {
      throw new DeveloperError(String.format("SQL error trying to get edge count from node %d.", node), e);
    }
    return count;
  }

  /**
   * Associates the given file with the given botid node.
   * 
   * @param connection
   * @param node
   * @param file
   */
  @SuppressWarnings("boxing")
  public static void storeBotIDNodeFile(Connection connection, int node, URL file) {
    int file_id = Entity.getOrCreate(connection, "file", "path", file.toExternalForm());
    try {
      PreparedStatement insert = connection
          .prepareStatement("INSERT INTO botidnode_file (botidnode_id, file_id) VALUES (?, ?)");
      insert.setInt(1, node);
      insert.setInt(2, file_id);
      insert.execute();
      insert.close();
    }
    catch (SQLException e) {
      throw new DeveloperError(String.format("SQL error trying to associate file \"%s\" with node %d.", file.toExternalForm(), node), e);
    }
  }
  
  /**
   * Creates (or finds) the root node and returns its id.
   * @param connection 
   * 
   * @return the id of the root node
   */
  public static int getRoot(Connection connection) {
    
    try {
      // The root node (will? should?) be the lowest-numbered node.
      int id = -1;
      PreparedStatement select = connection.prepareStatement("SELECT MIN(id) from node");
      ResultSet results = select.executeQuery();
      if (results.next()) {
        id = results.getInt(1);
        
        // Weird, but this is necessary.  Otherwise a null will get converted silently to 0.
        if (results.wasNull()) {
          id = -1;
        }
      }
      results.close();
      
      // If no node was found, create the root.
      if (id == -1) {
        PreparedStatement insert =
            connection.prepareStatement("INSERT INTO node () VALUES ()", Statement.RETURN_GENERATED_KEYS);
        insert.execute();
        results = insert.getGeneratedKeys();
        if (results.next()) {
          id = results.getInt(1);
        }
        else {
          throw new DeveloperError("No node id generated when trying to create root node!");
        }
        results.close();
        insert.close();
      }
      return id;
    }
    catch (SQLException e) {
      throw new DeveloperError("SQL error when trying to retrieve/create root node.", e);
    }
  }
  
  /**
   * Completely erase all nodes and related information from the database.
   * For now this just brutally truncates all tables related to node mapping.
   * If there's a reason to do this more surgically, then this should be changed.
   * 
   * @param connection
   */
  public static void eraseAll(Connection connection) {
    try {
      Statement statement = connection.createStatement();
      statement.execute("TRUNCATE node_template");
      statement.execute("TRUNCATE template");
      statement.execute("TRUNCATE edge");
      statement.execute("TRUNCATE botidnode_file");
      statement.execute("TRUNCATE bot_file");
      statement.execute("TRUNCATE file_node");
      statement.execute("TRUNCATE node");
      statement.execute("TRUNCATE file");
    }
    catch (SQLException e) {
      throw new DeveloperError("SQL error when trying to reset the graph.", e);
    }
  }
}
