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
import java.util.List;

import javax.sql.DataSource;

import org.aitools.util.Classes;
import org.aitools.util.resource.Filesystem;
import org.aitools.util.runtime.DeveloperError;
import org.aitools.util.runtime.UserError;
import org.apache.commons.dbcp.ConnectionFactory;
import org.apache.commons.dbcp.DriverManagerConnectionFactory;
import org.apache.commons.dbcp.PoolableConnectionFactory;
import org.apache.commons.dbcp.PoolingDataSource;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.ddlutils.DdlUtilsException;
import org.apache.ddlutils.Platform;
import org.apache.ddlutils.PlatformFactory;
import org.apache.ddlutils.PlatformInfo;
import org.apache.ddlutils.alteration.ModelChange;
import org.apache.ddlutils.alteration.ModelComparator;
import org.apache.ddlutils.io.DatabaseIO;
import org.apache.ddlutils.model.Database;
import org.apache.log4j.Logger;


/**
 * A class for managing pooled connections to the database.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class DBConnectionManager {
  
  private DataSource _dataSource;
  
  private Logger _logger;
  
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
  public DBConnectionManager(Logger logger, String driver, String uri, String username, String password, int minIdle, int maxActive) {
    
    this._logger = logger;
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
    
    this.checkDBSchema();
  }
  
  @SuppressWarnings("boxing")
  private void checkDBSchema() {
    
    Platform platform;
    try {
      platform = PlatformFactory.createNewPlatformInstance(this._dataSource);
    }
    catch (DdlUtilsException e) {
     
      throw new DeveloperError("Error creating platform factory for comparing database schemas.", e);
    }
    Database liveDB;
    try {
      liveDB = platform.readModelFromDatabase("programd");
    }
    catch (Exception e) {
      throw new DeveloperError("Error reading schema from live database.", e);
    }
    Database schema;
    try {
      schema = new DatabaseIO().read(Filesystem.getExistingFile("resources/database/programd-schema.xml"));
    }
    catch (Exception e) {
      throw new DeveloperError("Error reading database schema from file.", e);
    }
    ModelComparator comparator = new ModelComparator(new PlatformInfo(), true);
    
    @SuppressWarnings("unchecked")
    List<ModelChange> changes = comparator.compare(liveDB, schema);
    this._logger.debug(String.format("%d changes between schema specification and live database.", changes.size()));
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
