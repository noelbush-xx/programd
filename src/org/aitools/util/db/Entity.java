/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.util.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;

import org.aitools.util.runtime.DeveloperError;


/**
 * Some utility methods for working with database representations of simple entities.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class Entity {
  
  private static HashMap<String, PreparedStatement> PREPARED_SELECTS = new HashMap<String, PreparedStatement>();
  private static HashMap<String, PreparedStatement> PREPARED_INSERTS = new HashMap<String, PreparedStatement>();

  /**
   * Try to find an entity in the given table that is identified by the given value
   * for the given field.  If the entity is not found, create it.  Then, in either case,
   * return the id of the entity.  (This assumes a standard field "id" for such "entities".)
   * 
   * @param connection
   * @param table
   * @param field
   * @param value
   * @return the id of the entity
   */
  public static int getOrCreate(Connection connection, String table, String field, String value) {
    
    String key = table + field;
    PreparedStatement statement;
    if (PREPARED_SELECTS.containsKey(key)) {
      statement = PREPARED_SELECTS.get(key);
    }
    else {
      try {
        statement = connection.prepareStatement(
            String.format("SELECT id from %s WHERE %s = ?", table, field));
      }
      catch (SQLException e) {
        throw new DeveloperError("SQL error trying to initialize prepare statement.", e);
      }
      PREPARED_SELECTS.put(key, statement);
    }
    
    try {
      // Try to find an existing entity in the given table with the given value for the given field.
      int id = -1;
      statement.setString(1, value);
      ResultSet results = statement.executeQuery();
      if (results.next()) {
        id = results.getInt(1);
      }
      results.close();
      
      // If the entity was not found, create it.
      if (id == -1) {
        if (PREPARED_INSERTS.containsKey(key)) {
          statement = PREPARED_INSERTS.get(key);
        }
        else {
          statement = connection.prepareStatement(
              String.format("INSERT INTO %s (%s) VALUES (?)", table, field), Statement.RETURN_GENERATED_KEYS);
          PREPARED_INSERTS.put(key, statement);
        }
        statement.setString(1, value);
        statement.execute();
        results = statement.getGeneratedKeys();
        if (results.next()) {
          id = results.getInt(1);
        }
        else {
          throw new DeveloperError(String.format("No %s id generated!", table));
        }
        results.close();
      }
      return id;
    }
    catch (SQLException e) {
      throw new DeveloperError(
          String.format("SQL error when trying to retrieve/create %s with %s \"%s\".", table, field, value), e);
    }
  }
}
