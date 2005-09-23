package org.aitools.programd.util.sql;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.UserError;

/**
 * <p>
 * Represents all connection details to a DBMS and allows for getting the
 * input/output streams.
 * </p>
 * <p>
 * Uses JDBC so as to support any DBMS with a JDBS driver. For use with another
 * DBMS one should change the classname for the Java Driver and the specific
 * URL.
 * </p>
 * <p>
 * Sample:
 * </p>
 * 
 * <pre>
 * 
 *  
 *   
 *    
 *      driver class name : &quot;oracle.jdbc.dnlddriver.OracleDriver&quot;
 *      specific URL : &quot;dbc:oracle:dnldthin:@ora.doxa.ro:1526:ORCL&quot;.
 *      
 *    
 *   
 *  
 * </pre>
 * 
 * @author Cristian Mircioiu
 */
public class DbAccess
{
    private static Logger logger = Logger.getLogger("programd");

    private static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

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
     * Constructs a <code>DbAccess</code> object given a driver, url, user
     * name and password.
     * 
     * @param driverToUse name of the class representing the Driver to be used
     *            by the Driver manager
     * @param urlToUse location of a data source name (dsn)
     * @param userToUse user name
     * @param passwordToUse password for user
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
     * Constructs a <code>DbAccess</code> object given a
     * {@link java.sql.Connection} object.
     * 
     * @param connectionToUse the {@link java.sql.Connection} object from which
     *            to construct the <code>DbAccess</code> object
     */
    public DbAccess(Connection connectionToUse)
    {
        this.connection = connectionToUse;
    }

    /**
     * Connects to the database using the values of the fields already set in
     * this object.
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
                throw new UserError("Could not find your database driver.", e);
            }
            try
            {
                if (this.user == null || this.password == null)
                {
                    this.connection = DriverManager.getConnection(this.url);
                }
                else
                {
                    this.connection = DriverManager.getConnection(this.url, this.user, this.password);
                }
            }
            catch (SQLException e)
            {
                throw new UserError("Could not connect to \"" + this.url
                        + "\".  Please check that the parameters specified in your server properties file are correct.", e);
            }
            // Create the statement to be used in queries or updates.
            try
            {
                this.statement = this.connection.createStatement();
            }
            catch (SQLException e)
            {
                throw new UserError("Could not create a SQL statement using your database.", e);
            }
        }
    }

    /**
     * Returns the {@link java.sql.ResultSet ResultSet} from executing a given
     * query.
     * 
     * @param query the query to execute
     * @return the {@link java.sql.ResultSet ResultSet} from executing a given
     *         query
     */
    public ResultSet executeQuery(String query)
    {
        if (this.statement == null)
        {
            throw new DeveloperError("Tried to execute query before creating Statement object!", new NullPointerException());
        }
        try
        {
            return this.statement.executeQuery(query);
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, "Problem executing a query on the database.  Check structure and availability." + LINE_SEPARATOR
                    + e.getMessage());
            throw new DeveloperError("SQL error while executing query: " + query, e);
        }
    }

    /**
     * Returns the {@link java.sql.ResultSet ResultSet} from executing a given
     * update.
     * 
     * @param update the update to execute
     * @return the row count resulting from executing a given update
     */
    public int executeUpdate(String update)
    {
        if (this.statement == null)
        {
            throw new DeveloperError("Tried to execute query before creating Statement object!", new NullPointerException());
        }
        try
        {
            return this.statement.executeUpdate(update);
        }
        catch (SQLException e)
        {
            logger.log(Level.SEVERE, "Problem executing an update on your database.  Check structure and availability." + LINE_SEPARATOR
                    + e.getMessage());
            throw new UserError("SQL error while executing update: " + update, e);
        }
    }

    /**
     * Returns the {@link java.sql.Connection} object used by this object.
     * 
     * @return the Connection object used by this object.
     */
    public Connection getConnection()
    {
        return this.connection;
    }

    /**
     * Returns the name of the driver used by this object.
     * 
     * @return the name of the driver used by this object.
     */
    public String getDriver()
    {
        return this.driver;
    }

    /**
     * Returns the password used by this object.
     * 
     * @return the password used by this object.
     */
    public String getPassword()
    {
        return this.password;
    }

    /**
     * Returns the {@link java.sql.Statement} object used by this object.
     * 
     * @return the Statement object used by this object.
     */
    public Statement getStatement()
    {
        return this.statement;
    }

    /**
     * Returns the URL string used by this object.
     * 
     * @return the URL string used by this object.
     */
    public String getUrl()
    {
        return this.url;
    }

    /**
     * Returns the user name used by this object.
     * 
     * @return the user name used by this object.
     */
    public String getUser()
    {
        return this.user;
    }

    /**
     * Sets the {@link java.sql.Connection} object used by this object.
     * 
     * @param connectionToSet the Connection object to be used
     */
    public void setConnection(Connection connectionToSet)
    {
        this.connection = connectionToSet;
    }

    /**
     * Sets the name of the driver to be used by this object.
     * 
     * @param driverToSet the name of the driver to be used
     */
    public void setDriver(String driverToSet)
    {
        this.driver = driverToSet;
    }

    /**
     * Sets the password to be used by this object.
     * 
     * @param passwordToSet the password to be used
     */
    public void setPassword(String passwordToSet)
    {
        this.password = passwordToSet;
    }

    /**
     * Sets the URL string to be used by this object.
     * 
     * @param urlToSet the URL string to be used
     */
    public void setUrl(String urlToSet)
    {
        this.url = urlToSet;
    }

    /**
     * Sets the user name to be used by this object.
     * 
     * @param userToSet the user name to be used
     */
    public void setUser(String userToSet)
    {
        this.user = userToSet;
    }
}