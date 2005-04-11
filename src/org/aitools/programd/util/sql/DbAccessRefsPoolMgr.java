package org.aitools.programd.util.sql;

/**
 * Manages a pool of references to a database.
 * 
 * @author Cristian Mircioiu
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
     * Constructs a <code>DbAccessRefsPoolMgr</code> object given a driver,
     * url, user name and password.
     * 
     * @param driverToUse name of the class representing the Driver to be used
     *            by the Driver manager
     * @param urlToUse location of a data source name (dsn)
     * @param userToUse user name
     * @param passwordToUse password for user
     */
    public DbAccessRefsPoolMgr(String driverToUse, String urlToUse, String userToUse, String passwordToUse)
    {
        this.driver = driverToUse;
        this.url = urlToUse;
        this.user = userToUse;
        this.password = passwordToUse;
    }

    /**
     * Initializes the object by attempting to get a <code>DbAccess</code>
     * object based on the parameters set.
     * 
     * @return the created object
     */
    protected Object create()
    {
        return new DbAccess(this.driver, this.url, this.user, this.password);
    }

    /**
     * Builds a pool of the specified number of connections.
     * 
     * @param connectionCount the number of connections to create
     */
    public void populate(int connectionCount)
    {
        for (int index = connectionCount; --index >= 0;)
        {
            super.checkIn(create());
        }
    }

    /**
     * Checks back in and locks a <code>DbAccess</code> reference to the
     * database.
     * 
     * @param dba the reference to return
     */
    public void returnDbaRef(DbAccess dba)
    {
        super.checkIn(dba);
    }

    /**
     * Checks out and unlocks a <code>DbAccess</code> reference to the
     * database.
     * 
     * @return a reference to the database
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
        // Nothing to do.
    }
}