package org.alicebot.server.sql.pool;
	
	import java.lang.*;
import java.io.*;
import java.util.Properties;
import java.util.Date;

public class DbAccessRefsPoolMgr extends ObjectPool {
	protected String driver;
	protected String url;
	protected String user;
	protected String password;
		// constructor used to build manager for DBAccess objects.
	public DbAccessRefsPoolMgr( String driver, String url, String user, String password)
	{
		this.driver = driver;
		this.url = url;
		this.user = user;
		this.password = password;
	}
	protected Object create() throws Exception
	{
	    try{
	        return new DbAccess(driver, url, user, password);    
	    }	
	    catch(Exception e){
	        Debug.println("DbAccessRefsPoolMgr:create: FAILED to create new DbAccess object." + e.toString());
	        throw e;
	    }
	}
	protected void expire( Object o )
	{
	}
	public void populate( int no ){
	    Debug.print("building pool of DB connections.");
		for(int i = 0; i< no; i++){
		    try{
			    super.checkIn( create() );
			    Debug.print(".");
			}catch(Exception e){
			    Debug.println("DbAccessRefsPoolMgr:populate: create error propagated to populate.");
			}    
		}	 
		Debug.println();
	}
	public void returnDbaRef( DbAccess dba )
	{
		super.checkIn( dba );
	}
	public DbAccess takeDbaRef() throws Exception
	{
		try
		{
			return( ( DbAccess ) super.checkOut() );
		}
		catch( Exception e )
		{
			Debug.println("DbAccessPoolMgr: Exception while takeDbaRef: " + e.toString());
			throw e;
		}
	}
	protected boolean validate( Object o )
	{
	 return true;	   
	}
}
