package org.alicebot.server.sql.pool;

/** 
* DbAccess.java
*
* @author Cristian Mircioiu, 1999. 
*/

	// java specific imports:		 
import java.sql.*;
import java.io.PrintStream;
import java.util.Vector;
import java.util.Hashtable;
import java.util.Enumeration;

/**
* 
* Object containing all connection details to a DBMS and allows for getiing the input/output streams.
* 
* It has support for different DBMS and the coding does not rely on a certain one.
* For use with another DBMS one should change the classname for the Java Driver and the specific URL.
* Sample:  
* <pre>
*  driver class name : "oracle.jdbc.dnlddriver.OracleDriver"
*  specific URL : "dbc:oracle:dnldthin:@ora.doxa.ro:1526:ORCL".
* </pre>
*
* @author Cristian Mircioiu, 1999
*/
public class DbAccess
{
	protected Connection conn;
	protected Statement stmt = null;
	protected String driver;
	protected String url;
	protected String user;
	protected String password;
	private Vector srs;

	public DbAccess()
	{
		srs = new Vector(2);
	}
	/**
	* One type of constructor.
	*
	*
	* @param driver the name of the class representing the Driver to be used by the Driver manager.
	* @param url the location of a data source name(dsn).
	* @param user the user.
	* @param password his password.
	* @return
	* @exception java.sql.SQLException
	* 
	* @author Cristian Mircioiu, 1999
	*/
	public DbAccess(String driver, String url, String user, String password)
		throws SQLException, ClassNotFoundException
	{
		srs = new Vector(2);
		this.driver = driver;
		this.url = url;
		this.user = user;
		this.password = password;
		connect();
	}
	public DbAccess(Connection conn)
	{
		srs = new Vector(2);
		this.conn = conn;
	}
	public void connect()
		throws SQLException, ClassNotFoundException
	{
		if (conn == null)
		{
			Class.forName(driver);
			if (user == null || password == null)
				conn = DriverManager.getConnection(url);
			else
				conn = DriverManager.getConnection(url, user, password);
				// creates the statement to be used in queries or updates
			stmt = conn.createStatement();
		}
	}
	public ResultSet executeQuery(String query)
		throws SQLException
	{
	    return stmt.executeQuery(query);
	}
	public int executeUpdate(String sql) throws SQLException
	{
		return stmt.executeUpdate(sql);
	}
	public Connection getConnection()
	{
		return conn;
	}
	public String getDriver()
	{
		return driver;
	}
	public String getPassword()
	{
		return password;
	}
	public Statement getStatement()
	{
		return stmt;
	}
	public String getUrl()
	{
		return url;
	}
	public String getUser()
	{
		return user;
	}
	public static void main(String args[])
	{
		try
		{
			ResultSet rs=null;
			DbAccess dba = new DbAccess("sun.jdbc.odbc.JdbcOdbcDriver",
										"jdbc:odbc:efinance",
										"crimir", "crimir");
			try{
				rs = dba.executeQuery("select country.feedback_email from country ");
				while(rs.next()){
					Debug.println(rs.getString(1));	   
				}
				
				
			}catch(SQLException w ){
				 Debug.println(w.toString());	
				 if (w.getMessage().equals("Exhausted Resultset")) 
					Debug.println("query returned no data !!");
				throw new SQLException(w.toString() + " the query could have returned no data: \n");		
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	public void setConnection(Connection conn)
	{
		this.conn = conn;
	}
	public void setDriver(String driver)
	{
		this.driver = driver;
	}
	public void setPassword(String password)
	{
		this.password = password;
	}
	public void setUrl(String url)
	{
		this.url = url;
	}
	public void setUser(String user)
	{
		this.user = user;
	}
}
