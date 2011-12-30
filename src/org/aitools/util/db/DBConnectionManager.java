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
import java.sql.SQLException;

import javax.sql.DataSource;

import org.aitools.util.Classes;
import org.aitools.util.runtime.UserError;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;


/**
 * A class for managing pooled connections to the database.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class DBConnectionManager {
  
  private DataSource _dataSource;
  
  /**
   * Create a new database connection manager using the given driver (classname)
   * and database URI (DBMS-specific).
   * 
   * @param logger
   * @param driver
   * @param uri
   * @param username
   * @param password
   * @param minIdle
   * @param maxActive
   */
  public DBConnectionManager(String driver, String uri, String username, String password, int minIdle, int maxActive) {
    
    Classes.verifyAvailable(driver, "database driver");
    
    GenericObjectPool connectionPool = new GenericObjectPool(null);
    connectionPool.setMinIdle(minIdle);
    connectionPool.setMaxActive(maxActive);
    
    ConnectionFactory connectionFactory =
        new DriverManagerConnectionFactory(uri, username, password);
    
    @SuppressWarnings("unused")
    PoolableConnectionFactory poolableConnectionFactory =
        new PoolableConnectionFactory(connectionFactory, connectionPool, null, null, false, true);
    
    this._dataSource = new PoolingDataSource(connectionPool);
    
    // Was using DdlUtils here, but it did not correctly work for all column properties.
    //this.checkDBSchema();
  }

  /**
   * Returns a database connection from a pooling data source.
   * 
   * @return the database connection
   */
  public Connection getDBConnection() {
    
    try {
      return this._dataSource.getConnection();
    }
    catch (SQLException e) {
      throw new UserError("Error connecting to database.", e);
    }
  }
}
