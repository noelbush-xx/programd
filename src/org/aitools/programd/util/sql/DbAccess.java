package org.aitools.programd.util.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.UserError;
import org.aitools.programd.util.logging.Log;

/**
 *  <p>
 *  Represents all connection details to a DBMS and allows
 *  for getting the input/output streams.
 *  </p>
 *  <p>
 *  Uses JDBC so as to support any DBMS with a JDBS driver.
 *  For use with another DBMS one should change the classname
 *  for the Java Driver and the specific URL.
 *  </p>
 *  <p>
 *  Sample:
 *  </p>
 *  <pre>
 *  driver class name : "oracle.jdbc.dnlddriver.OracleDriver"
 *  specific URL : "dbc:oracle:dnldthin:@ora.doxa.ro:1526:ORCL".
 *  </pre>
 *
 *  @author Cristian Mircioiu
 */
public class DbAccess
{
    /** The Connection used by this object. */
    protected Connection connection;

    /** The statement used by this object. */
    protected Statement statement;

    /** The name of the driver used by this object. */
    protected String driver;

    /** The URL string used by this object. */
    protected String url;

    /** The user name used by this object. */
    protected String user;

    /** The password used by this object. */
    protected String password;

    /**
     *  Constructs a <code>DbAccess</code> object given
     *  a driver, url, user name and password.
     *
     *  @param driver   name of the class representing the Driver to be used by the Driver manager
     *  @param url      location of a data source name (dsn)
     *  @param user     user name
     *  @param password password for user
     */
    public DbAccess(String driverToUse, String urlToUse, String userToUse, String passwordToUse)
    {
        this.driver = driverToUse;
        this.url = urlToUse;
        this.user = userToUse;
        this.password = passwordToUse;
        connect();
    }

    /**
     *  Constructs a <code>DbAccess</code> object
     *  given a {@link java.sql.Connection} object.
     *
     *  @param connection   the {@link java.sql.Connection} object
     *                      from which to construct the <code>DbAccess</code> object
     */
    public DbAccess(Connection connectionToUse)
    {
        this.connection = connectionToUse;
    }

    /**
     *  Connects to the database using the values of the
     *  fields already set in this object.
     *
     *  @throws SQLException if the connection cannot be made
     *  @throws ClassNotFoundException if the driver class cannot be found
     */
    public void connect()
    {
        if (this.connection == null)
        {
            try
            {
                Class.forName(this.driver);
            }
            catch (ClassNotFoundException e)
            {
                throw new UserError("Could not find your database driver.");
            }
            try
            {
                if (this.user == null || this.password == null)
                {
                    this.connection = DriverManager.getConnection(this.url);
                }
                else
                {
                    this.connection =
                        DriverManager.getConnection(this.url, this.user, this.password);
                }
            }
            catch (SQLException e)
            {
                throw new UserError(
                    "Could not connect to \""
                        + this.url
                        + "\".  Please check that the parameters specified in your server properties file are correct.",
                    e);
            }
            // Create the statement to be used in queries or updates.
            try
            {
                this.statement = this.connection.createStatement();
            }
            catch (SQLException e)
            {
                throw new UserError("Could not create a SQL statement using your database.");
            }
        }
    }

    /**
     *  Returns the {@link java.sql.ResultSet ResultSet}
     *  from executing a given query.
     *
     *  @param query    the query to execute
     *
     *  @return the {@link java.sql.ResultSet ResultSet} from executing a given query
     *
     *  @throws SQLException if there was a problem.
     */
    public ResultSet executeQuery(String query) throws SQLException
    {
        if (this.statement == null)
        {
            throw new DeveloperError("Tried to execute query before creating Statement object!");
        }
        try
        {
            return this.statement.executeQuery(query);
        }
        catch (SQLException e)
        {
            Log.userinfo(new String[] {
                "Problem executing a query on your database.  Check structure and availability.",
                e.getMessage()},
                new String[] { Log.ERROR, Log.DATABASE });
            throw e;
        }
    }

    /**
     *  Returns the {@link java.sql.ResultSet ResultSet}
     *  from executing a given update.
     *
     *  @param update   the update to execute
     *
     *  @return the row count resulting from executing a given update
     */
    public int executeUpdate(String update)
    {
        if (this.statement == null)
        {
            throw new DeveloperError("Tried to execute query before creating Statement object!");
        }
        try
        {
            return this.statement.executeUpdate(update);
        }
        catch (SQLException e)
        {
            Log.userinfo(new String[] {
                "Problem executing an update on your database.  Check structure and availability.",
                e.getMessage()},
                new String[] { Log.ERROR, Log.DATABASE });
            throw new UserError(e);
        }
    }

    /**
     *  Returns the {@link java.sql.Connection}
     *  object used by this object.
     *
     *  @return the Connection object used by this object.
     */
    public Connection getConnection()
    {
        return this.connection;
    }

    /**
     *  Returns the name of the driver used by this object.
     *
     *  @return the name of the driver used by this object.
     */
    public String getDriver()
    {
        return this.driver;
    }

    /**
     *  Returns the password used by this object.
     *
     *  @return the password used by this object.
     */
    public String getPassword()
    {
        return this.password;
    }

    /**
     *  Returns the {@link java.sql.Statement}
     *  object used by this object.
     *
     *  @return the Statement object used by this object.
     */
    public Statement getStatement()
    {
        return this.statement;
    }

    /**
     *  Returns the URL string used by this object.
     *
     *  @return the URL string used by this object.
     */
    public String getUrl()
    {
        return this.url;
    }

    /**
     *  Returns the user name used by this object.
     *
     *  @return the user name used by this object.
     */
    public String getUser()
    {
        return this.user;
    }

    /**
     *  Sets the {@link java.sql.Connection}
     *  object used by this object.
     *
     *  @param connection   the Connection object to be used
     */
    public void setConnection(Connection connectionToSet)
    {
        this.connection = connectionToSet;
    }

    /**
     *  Sets the name of the driver to be
     *  used by this object.
     *
     *  @param driver   the name of the driver to be used
     */
    public void setDriver(String driverToSet)
    {
        this.driver = driverToSet;
    }

    /**
     *  Sets the password to be
     *  used by this object.
     *
     *  @param password the password to be used
     */
    public void setPassword(String passwordToSet)
    {
        this.password = passwordToSet;
    }

    /**
     *  Sets the URL string to be
     *  used by this object.
     *
     *  @param url  the URL string to be used
     */
    public void setUrl(String urlToSet)
    {
        this.url = urlToSet;
    }

    /**
     *  Sets the user name to be
     *  used by this object.
     *
     *  @param user the user name to be used
     */
    public void setUser(String userToSet)
    {
        this.user = userToSet;
    }
}
