package org.alicebot.server.sql.pool;

import java.sql.SQLException;

import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.util.DeveloperError;
import org.alicebot.server.core.util.Trace;
import org.alicebot.server.core.util.UserError;

    
/**
 *  Manages a pool of references to a
 *  database.
 *
 *  @author Cristian Mircioiu
 */
public class DbAccessRefsPoolMgr extends ObjectPool
{
    /** The name of the driver used by this object. */
    protected String driver;

    /** The URL string used by this object. */
    protected String url;

    /** The user name used by this object. */
    protected String user;

    /** The password used by this object. */
    protected String password;


    /**
     *  Constructs a <code>DbAccessRefsPoolMgr</code> object given
     *  a driver, url, user name and password.
     *
     *  @param driver   name of the class representing the Driver to be used by the Driver manager
     *  @param url      location of a data source name (dsn)
     *  @param user     user name
     *  @param password password for user
     */
    public DbAccessRefsPoolMgr(String driver, String url, String user, String password)
    {
        this.driver = driver;
        this.url = url;
        this.user = user;
        this.password = password;
    }


    /**
     *  Initializes the object by attempting to get a <code>DbAccess</code>
     *  object based on the parameters set.
     */
    protected Object create()
    {
        return new DbAccess(driver, url, user, password);    
    }


    /**
     *  Builds a pool of the specified number of
     *  connections.
     *
     *  @param connectionCount  the number of connections to create
     */
    public void populate(int connectionCount)
    {
        for (int index = connectionCount; --index >= 0; )
        {
            super.checkIn(create());
        }     
    }


    /**
     *  Checks back in and locks a <code>DbAccess</code>
     *  reference to the database.
     *
     *  @param dba  the reference to return
     */
    public void returnDbaRef(DbAccess dba)
    {
        super.checkIn(dba);
    }


    /**
     *  Checks out and unlocks a <code>DbAccess</code>
     *  reference to the database.
     *
     *  @return a reference to the database
     */
    public DbAccess takeDbaRef()
    {
        return (DbAccess) super.checkOut();
    }


    protected boolean validate(Object o)
    {
        return true;
    }


    protected void expire(Object o)
    {
    }
}
