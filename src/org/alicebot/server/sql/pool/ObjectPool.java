package org.alicebot.server.sql.pool;
	
import java.util.Enumeration;
import java.util.Hashtable;

public abstract class ObjectPool 
{
	private long expirationTime;
	private long lastCheckOut;

	private Hashtable locked;
	private Hashtable unlocked;
	
	private CleanUpThread cleaner;
	
	protected ObjectPool()
	{
		expirationTime = ( 1000 * 60 *	60); // default = 1 hour   

		locked = new Hashtable();		  
		unlocked = new Hashtable();
		
		lastCheckOut = System.currentTimeMillis();

		cleaner = new CleanUpThread( this, expirationTime );
		cleaner.start();
	}
	protected synchronized void checkIn( Object o )
	{
		if( o != null )
		{
			locked.remove( o );
			unlocked.put( o, new Long( System.currentTimeMillis() ) );
		}
	}
	protected synchronized Object checkOut() throws Exception
	{
		long now = System.currentTimeMillis();		
		lastCheckOut = now;
		Object o;			   
		
		if( unlocked.size() > 0 )
		{
			Enumeration e = unlocked.keys();  
			
			while( e.hasMoreElements() )
			{
				o = e.nextElement();		

				if( validate( o ) )
				{
					unlocked.remove( o );
					locked.put( o, new Long( now ) );				 
					return( o );
				}
				else
				{
					unlocked.remove( o );
					expire( o );
					o = null;
				}
			}
		}		 
		
		o = create();		 
		
		locked.put( o, new Long( now ) ); 
		return( o );
	}
	protected synchronized void cleanUp()
	{
		Object o;

		long now = System.currentTimeMillis();
		
		Enumeration e = unlocked.keys();  
		
		while( e.hasMoreElements() )
		{
			o = e.nextElement();		

			if( ( now - ( ( Long ) unlocked.get( o ) ).longValue() ) > expirationTime )
			{
				unlocked.remove( o );
				expire( o );
				o = null;
			}
		}

		System.gc();
	}
	protected  abstract Object create() throws Exception;
	protected abstract void expire( Object o );
	public int getNoOfConnsInPool(){
		return unlocked.size();
	}
	public int getNoOfRefs(){
		return locked.size() + unlocked.size();
	}
	protected synchronized void setExpirationTime( long expirationTime)
	{
		this.expirationTime = expirationTime;
	}
	protected abstract boolean validate( Object o );
}
