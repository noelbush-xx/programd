package org.alicebot.server.sql.pool;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.alicebot.server.core.Globals;
import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.util.DeveloperError;
import org.alicebot.server.core.util.UserError;


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
    public DbAccess(String driver, String url, String user, String password)
    {
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
        connect();
    }


    /**
     *  Constructs a <code>DbAccess</code> object
     *  given a {@link java.sql.Connection} object.
     *
     *  @param connection   the {@link java.sql.Connection} object
     *                      from which to construct the <code>DbAccess</code> object
     */
    public DbAccess(Connection connection)
    {
        this.connection = connection;
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
        if (connection == null)
        {
            try
            {
                Class.forName(driver);
            }
            catch (ClassNotFoundException e)
            {
                throw new UserError("Could not find your database driver.");
            }
            try
            {
                if (user == null || password == null)
                {
                    connection = DriverManager.getConnection(url);
                }
                else
                {
                    connection = DriverManager.getConnection(url, user, password);
                }
            }
            catch (SQLException e)
            {
                throw new UserError("Could not connect to \"" + url + "\".  Please check that the parameters specified in your server properties file are correct.", e);
            }
            // Create the statement to be used in queries or updates.
            try
            {
                statement = connection.createStatement();
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
        if (statement == null)
        {
            throw new DeveloperError("Tried to execute query before creating Statement object!");
        }
        try
        {
            return statement.executeQuery(query);
        }
        catch (SQLException e)
        {
            Log.userinfo("Problem executing a query on your database.  Check structure and availability.",
            new String[] {Log.ERROR, Log.DATABASE});
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
        if (statement == null)
        {
            throw new DeveloperError("Tried to execute query before creating Statement object!");
        }
        try
        {
            return statement.executeUpdate(update);
        }
        catch (SQLException e)
        {
            throw new UserError("Problem executing an update on your database.  Check structure and availability.", e);
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
        return connection;
    }


    /**
     *  Returns the name of the driver used by this object.
     *
     *  @return the name of the driver used by this object.
     */
    public String getDriver()
    {
        return driver;
    }


    /**
     *  Returns the password used by this object.
     *
     *  @return the password used by this object.
     */
    public String getPassword()
    {
        return password;
    }


    /**
     *  Returns the {@link java.sql.Statement}
     *  object used by this object.
     *
     *  @return the Statement object used by this object.
     */
    public Statement getStatement()
    {
        return statement;
    }


    /**
     *  Returns the URL string used by this object.
     *
     *  @return the URL string used by this object.
     */
    public String getUrl()
    {
        return url;
    }


    /**
     *  Returns the user name used by this object.
     *
     *  @return the user name used by this object.
     */
    public String getUser()
    {
        return user;
    }



    /**
     *  Sets the {@link java.sql.Connection}
     *  object used by this object.
     *
     *  @param connection   the Connection object to be used
     */
    public void setConnection(Connection connection)
    {
        this.connection = connection;
    }


    /**
     *  Sets the name of the driver to be
     *  used by this object.
     *
     *  @param driver   the name of the driver to be used
     */
    public void setDriver(String driver)
    {
        this.driver = driver;
    }


    /**
     *  Sets the password to be
     *  used by this object.
     *
     *  @param password the password to be used
     */
    public void setPassword(String password)
    {
        this.password = password;
    }


    /**
     *  Sets the URL string to be
     *  used by this object.
     *
     *  @param url  the URL string to be used
     */
    public void setUrl(String url)
    {
        this.url = url;
    }


    /**
     *  Sets the user name to be
     *  used by this object.
     *
     *  @param user the user name to be used
     */
    public void setUser(String user)
    {
        this.user = user;
    }
}
