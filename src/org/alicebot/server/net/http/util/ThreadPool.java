// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: ThreadPool.java,v 1.1.1.1 2001/06/17 19:02:24 noelbu Exp $
// ========================================================================

package org.alicebot.server.net.http.util;

import java.io.InterruptedIOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Iterator;

/* ------------------------------------------------------------ */
/** A pool of threads.
 * <p>
 * Avoids the expense of thread creation by pooling threads after
 * their run methods exit for reuse.
 * <p>
 * If the maximum pool size is reached, jobs wait for a free thread.
 * By default there is no maximum pool size.  Idle threads timeout
 * and terminate until the minimum number of threads are running.
 * <p>
 * This implementation uses the run(Object) method to place a
 * job on a queue, which is read by the getJob(timeout) method.
 * Derived implementations may specialize getJob(timeout) to
 * obtain jobs from other sources without queing overheads.
 *
 * @version $Id: ThreadPool.java,v 1.1.1.1 2001/06/17 19:02:24 noelbu Exp $
 * @author Juancarlo Anez <juancarlo@modelistica.com>
 * @author Greg Wilkins <gregw@mortbay.com>
 */
public class ThreadPool
    implements LifeCycle
{
    /* ------------------------------------------------------------ */
    static int __maxThreads =
        Integer.getInteger("THREADPOOL_MAX_THREADS",256).intValue();
    
    /* ------------------------------------------------------------ */
    static int __minThreads =
        Integer.getInteger("THREADPOOL_MIN_THREADS",1).intValue();

    /* ------------------------------------------------------------ */
    static int __stopWaitMs =
        Integer.getInteger("THREADPOOL_STOP_WAIT",5000).intValue();

    /* ------------------------------------------------------------ */
    /** The number of times a null lock check should synchronize.
     */
    public static int __nullLockChecks =
        Integer.getInteger("THREADPOOL_NULL_LOCK_CHECKS",2).intValue();

    /* ------------------------------------------------------------ */
    String __threadClass =
        System.getProperty("THREADPOOL_THREAD_CLASS",
                           "java.lang.Thread");
    
    /* ------------------------------------------------------------------- */
    private HashSet _threadSet;
    private int _maxThreads = __maxThreads;
    private int _minThreads = __minThreads;
    private int _maxIdleTimeMs=0;
    private String _name;
    private int _threadId=0;
    private HashSet _idleSet=new HashSet();
    private boolean _running=false;
    private Class _threadClass;
    private Constructor _constructThread;
    
    private BlockingQueue _queue;
    private int _queueChecks;
    
    /* ------------------------------------------------------------------- */
    /* Construct
     */
    public ThreadPool() 
    {
        try
        {
            _threadClass = Class.forName( __threadClass );
            Code.debug("Using thread class '", _threadClass.getName(),"'");
        }
        catch( Exception e )
        {
            Code.warning( "Invalid thread class (ignored) ",e );
            _threadClass = java.lang.Thread.class;
        }
        setThreadClass(_threadClass);
    }
    
    /* ------------------------------------------------------------------- */
    /* Construct
     * @param name Pool name
     */
    public ThreadPool(String name) 
    {
        this();
        _name=name;
    }


    /* ------------------------------------------------------------ */
    /** 
     * @return 
     */
    public String getName()
    {
        return _name;
    }

    /* ------------------------------------------------------------ */
    /** 
     * @param name 
     */
    public void setName(String name)
    {
        _name=name;
    }
    
    /* ------------------------------------------------------------ */
    /** Set the Thread class.
     * Sets the class used for threads in the thread pool. The class
     * must have a constractor taking a Runnable.
     * @param threadClas The class
     * @exception IllegalStateException If the pool has already
     *            been started.
     */
    public synchronized void setThreadClass(Class threadClass)
        throws IllegalStateException
    {
        if (_running)
            throw new IllegalStateException("Thread Pool Running");
        
        _threadClass=threadClass;
                
        if(_threadClass == null ||
            !Thread.class.isAssignableFrom( _threadClass ) )
        {
            Code.warning( "Invalid thread class (ignored) "+
                          _threadClass.getName() );
            _threadClass = java.lang.Thread.class;
        }

        try
        {
            Class[] args ={java.lang.Runnable.class};
            _constructThread = _threadClass.getConstructor(args);
        }
        catch(Exception e)
        {
            Code.warning("Invalid thread class (ignored)",e);
            setThreadClass(java.lang.Thread.class);
        }

        if (_name==null)
        {
            _name=getClass().getName();
            _name=_name.substring(_name.lastIndexOf(".")+1);
        }
    }

    /* ------------------------------------------------------------ */
    /** Handle a job.
     * Unless the job is an instance of Runnable, then
     * this method must be specialized by a derived class.
     * @param job The Job to handle.  If it implements Runnable,
     * this implementation calls run().
     */
    protected void handle(Object job)
        throws InterruptedException
    {
        if (job!=null && job instanceof Runnable)
            ((Runnable)job).run();
        else
            Code.warning("Invalid job: "+job);
    }

    /* ------------------------------------------------------------ */
    /** Is the pool running jobs.
     * @return True if start() has been called.
     */
    public boolean isStarted()
    {
        return _running && _threadSet!=null;
    }
    
    /* ------------------------------------------------------------ */
    /** 
     * @return False - always started or stopped. 
     */
    public boolean isDestroyed()
    {
        return false;
    }
    
    /* ------------------------------------------------------------ */
    /** Get the number of threads in the pool.
     * @return Number of threads
     */
    public int getThreads()
    {
        if (_threadSet==null)
            return 0;
        return _threadSet.size();
    }
    
    /* ------------------------------------------------------------ */
    /** Get the minimum number of threads.
     * @return minimum number of threads.
     */
    public int getMinThreads()
    {
        return _minThreads;
    }
    
    /* ------------------------------------------------------------ */
    /** Set the minimum number of threads.
     * @param minThreads minimum number of threads
     */
    public void setMinThreads(int minThreads)
    {
        _minThreads=minThreads;
    }
    
    /* ------------------------------------------------------------ */
    /** Set the maximum number of threads.
     * @return maximum number of threads.
     */
    public int getMaxThreads()
    {
        return _maxThreads;
    }
    
    /* ------------------------------------------------------------ */
    /** Set the maximum number of threads.
     * @param maxThreads maximum number of threads.
     */
    public void setMaxThreads(int maxThreads)
    {
        _maxThreads=maxThreads;
    }
    
    /* ------------------------------------------------------------ */
    /** Get the maximum thread idle time.
     * @return Max idle time in ms.
     */
    public int getMaxIdleTimeMs()
    {
        return _maxIdleTimeMs;
    }
    
    /* ------------------------------------------------------------ */
    /** Set the maximum thread idle time.
     * Threads that are idle for longer than this period may be
     * destroyed.
     * @param maxIdleTimeMs Max idle time in ms.
     */
    public void setMaxIdleTimeMs(int maxIdleTimeMs)
    {
        _maxIdleTimeMs=maxIdleTimeMs;
    }

    /* ------------------------------------------------------------ */
    /** Not required.
     * Calls destroy().
     * @param o ignored.
     */
    synchronized public void initialize(Object o)
    {
        destroy();
    }
    
    /* ------------------------------------------------------------ */
    /* Start the ThreadPool.
     * Construct the minimum number of threads.
     */
    synchronized public void start()
    {
        getName();
        if (_running)
        {
            Code.debug("Already started");
            return;
        }
        Code.debug("Start Pool ",_name);

        // Start the threads
        _running=true;
        _threadSet=new HashSet(_maxThreads+_maxThreads/2+13);
        for (int i=0;i<_minThreads;i++)
            newThread();
    }

    /* ------------------------------------------------------------ */
    /** Stop the ThreadPool.
     * New jobs are no longer accepted, idle threads are interrupted
     * and the pool is joined until all jobs are completed.
     */
    public void stop()
        throws InterruptedException
    {
        Code.debug("Stop Pool ",_name);
        _running=false;
        
        while (_threadSet!=null && !_threadSet.isEmpty())
        {
            int size = _threadSet.size();
            synchronized(this)
            {
                Object[] threads=_threadSet.toArray();
                for(int t=0;t<threads.length;t++)
                {
                    Thread thread=(Thread)threads[t];
                    thread.interrupt();
                }
            }

            // This section is here as some JVMs do not interrupt
            // in all situations that they should.
            Thread.yield();
            if (_threadSet.size()>0)
            {
                Thread.sleep(200);
                if (_threadSet.size()==size)
                    forceStop();
            }
        }
    }


    /* ------------------------------------------------------------ */
    /** Take forceful stop actions.
     * This method is called if interrupt is not stopping the
     * threads.
     */
    protected void forceStop()
    {}
    
    
    /* ------------------------------------------------------------ */
    /** Destroy the ThreadPool.
     * All threads are interrupted and if they do not terminate after
     * a short delay, they are stopped.
     */
    synchronized public void destroy() 
    {
        Code.debug("Destroy Pool ",_name);

        if (_threadSet==null)
            return;
        
        _running=false;
        
        // interrupt the threads
        Object[] threads=_threadSet.toArray();
        for(int t=0;t<threads.length;t++)
        {
            Thread thread=(Thread)threads[t];
            thread.interrupt();
        }
        
        // wait a while for all threads to die
        Thread.yield();
        try{
            long end_wait=System.currentTimeMillis()+__stopWaitMs;
            while (_threadSet.size()>0 &&
                   end_wait>System.currentTimeMillis())
                Thread.yield();
            
            // Warn about any still running
            if (_threadSet.size()>0)
            {
                threads=_threadSet.toArray();
                for(int t=0;t<threads.length;t++)
                {
                    Thread thread=(Thread)threads[t];
                    if (thread.isAlive())
                    {
                        thread.interrupt();
                        Thread.yield();
                        thread.stop();
                        if (thread.isAlive())
                            Code.warning("Can't stop "+thread);
                    }
                }
            }
        }
        finally
        {
            _threadSet.clear();
            _threadSet=null;
        }
    }

    
    /* ------------------------------------------------------------ */
    /* Start a new Thread.
     */
    private synchronized void newThread()
    {
        try
        {
            Runnable runner = new PoolThreadRunnable();
            Object[] args = {runner};
            Thread thread=
                (Thread)_constructThread.newInstance(args);
            thread.setName(_name+"-"+(_threadId++));
            _threadSet.add(thread);
            thread.start();
        }
        catch( java.lang.reflect.InvocationTargetException e )
        {
            Code.fail(e);
        }
        catch( IllegalAccessException e )
        {
            Code.fail(e);
        }
        catch( InstantiationException e )
        {
            Code.fail(e);
        }
    }
    
  
    /* ------------------------------------------------------------ */
    /** Join the ThreadPool.
     * Wait for all threads to complete.
     * @exception java.lang.InterruptedException 
     */
    final public void join() 
        throws java.lang.InterruptedException
    {
        while(_threadSet!=null && _threadSet.size()>0)
        {
            Thread thread=null;
            synchronized(this)
            {
                Iterator iter=_threadSet.iterator();
                while(iter.hasNext())
                    thread=(Thread)iter.next();
            }
            if (thread!=null)
                thread.join();
        }
    }
  
    /* ------------------------------------------------------------ */
    /** Get a job.
     * This method is called by the ThreadPool to get jobs.
     * The call blocks until a job is available.
     * The default implementation removes jobs from the BlockingQueue
     * used by the run(Object) method. Derived implementations of
     * ThreadPool may specialize this method to obtain jobs from other
     * sources.
     * @param idleTimeoutMs The timout to wait for a job.
     * @return Job or null if no job available after timeout.
     * @exception InterruptedException 
     * @exception InterruptedIOException 
     */
    protected Object getJob(int idleTimeoutMs)
        throws InterruptedException, InterruptedIOException
    {
        if (_queue==null || _queueChecks<__nullLockChecks)
        {
            synchronized(this)
            {
                if (_queue==null)
                    _queue=new BlockingQueue(_maxThreads);
                _queueChecks++;
            }
        }
        
        return _queue.get(idleTimeoutMs);
    }
    

    /* ------------------------------------------------------------ */
    /** Run job.
     * Give a job to the pool. The job is passed via a BlockingQueue
     * with the same capacity as the ThreadPool.
     * @param job.  If the job is derived from Runnable, the run method
     * is called, otherwise it is passed as the argument to the handle
     * method.
     */
    public void run(Object job)
        throws InterruptedException
    {
        if (!_running)
            start();
        
        if (job==null)
        {
            Code.warning("Null Job");
            return;
        }
        
        if (_queue==null || _queueChecks<2)
        {
            synchronized(this)
            {
                if (_queue==null)
                    _queue=new BlockingQueue(_maxThreads);
                _queueChecks++;
            }
        }
        _queue.put(job);
    }

    /* ------------------------------------------------------------ */
    /** Pool Thread run class.
     */
    private class PoolThreadRunnable
        implements Runnable
    {
        /* -------------------------------------------------------- */
        /** ThreadPool run.
         * Loop getting jobs and handling them until idle or stopped.
         */
        public void run() 
        {
            Thread thread=Thread.currentThread();
            String name=thread.getName();
            int runs=0;
            
            if (Code.verbose(9))
                Code.debug( "Start thread in ", _name );
            try{
                while(_running) 
                {
                    Object job=null;
                    try 
                    {
                        // increment accepting count
                        synchronized(this){_idleSet.add(thread);}
                    
                        // wait for a job
                        job=getJob(_maxIdleTimeMs);

                        // If no job
                        if (job==null && _running)
                        {
                            if (Code.verbose(99))
                                Code.debug("Threads="+_threadSet.size()+
                                           " idle="+_idleSet.size());
                        
                            if (_threadSet.size()>_minThreads &&
                                _idleSet.size()>1)
                            {
                                // interrupt was due to accept timeout
                                // Kill thread if it is in excess of the minimum.
                                if (Code.verbose(99))
                                    Code.debug("Idle death: "+thread);
                                break;
                            }
                        }
                    }
                    catch(InterruptedException e)
                    {
                        Code.ignore(e);
                        continue;
                    }
                    catch(InterruptedIOException e)
                    {
                        Code.ignore(e);
                        continue;
                    }
                    catch(Exception e)
                    {
                        Code.warning(e);
                    }
                    finally
                    {
                        synchronized(this)
                        {
                            _idleSet.remove(thread);
                            // If not more threads accepting - start one
                            if (_idleSet.size()==0 &&
                                _running &&
                                job!=null &&
                                _threadSet.size()<_maxThreads)
                                newThread();
                        }
                    }

                    // handle the connection
                    if (_running && job!=null)
                    {
                        try
                        {
                            // Tag thread if debugging
                            if (Code.debug())
                            {
                                thread.setName(name+"/"+runs++);
                                if (Code.verbose(99))
                                    Code.debug("Handling ",job);
                            }

                            // handle the job
                            handle(job);
                        }
                        catch (Exception e)
                        {
                            Code.warning(e);
                        }
                        finally
                        {
                            job=null;
                        }
                    }
                }
            }
            finally
            {
                synchronized(ThreadPool.this)
                {
                    if (_threadSet!=null)
                        _threadSet.remove(Thread.currentThread());
                }
                if (Code.verbose(9))
                    Code.debug("Stopped thread in ", _name);
            }
        }
    }

    
    /* ------------------------------------------------------------ */
    /** Get the number of threads in the pool.
     * @return Number of threads
     * @deprecated use getThreads
     */
    public int getSize()
    {
        if (_threadSet==null)
            return 0;
        return _threadSet.size();
    }
    
    /* ------------------------------------------------------------ */
    /** Get the minimum number of threads.
     * @return minimum number of threads.
     * @deprecated use getMinThreads
     */
    public int getMinSize()
    {
        return _minThreads;
    }
    
    /* ------------------------------------------------------------ */
    /** Set the minimum number of threads.
     * @param minThreads minimum number of threads
     * @deprecated use setMinThreads
     */
    public void setMinSize(int minThreads)
    {
        _minThreads=minThreads;
    }
    
    /* ------------------------------------------------------------ */
    /** Set the maximum number of threads.
     * @return maximum number of threads.
     * @deprecated use getMaxThreads
     */
    public int getMaxSize()
    {
        return _maxThreads;
    }
    
    /* ------------------------------------------------------------ */
    /** Set the maximum number of threads.
     * @param maxThreads maximum number of threads.
     * @deprecated use setMaxThreads
     */
    public void setMaxSize(int maxThreads)
    {
        _maxThreads=maxThreads;
    }
    
}






