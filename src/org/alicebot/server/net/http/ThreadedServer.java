// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: ThreadedServer.java,v 1.1.1.1 2001/06/17 19:01:32 noelbu Exp $
// ---------------------------------------------------------------------------

package org.alicebot.server.net.http;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;


/* ======================================================================= */
/** Threaded socket server.
 * This class listens at a socket and gives the connections received
 * to a pool of Threads
 * <P>
 * The class is abstract and derived classes must provide the handling
 * for the connections.
 * <P>
 * The properties THREADED_SERVER_MIN_THREADS and THREADED_SERVER_MAX_THREADS
 * can be set to control the number of threads created.
 * <P>
 * @version $Id: ThreadedServer.java,v 1.1.1.1 2001/06/17 19:01:32 noelbu Exp $
 * @author Greg Wilkins
 */
abstract public class ThreadedServer extends ThreadPool
{    
    /* ------------------------------------------------------------------- */
    private InetAddrPort _address = null;    
    ServerSocket _listen = null;
    int _soTimeOut=-1;
    int _maxReadTimeMs=-1;
    int _lingerTimeSecs=30;
    
    /* ------------------------------------------------------------------- */
    /* Construct
     */
    public ThreadedServer() 
    {}

    /* ------------------------------------------------------------------- */
    /** Construct for specific port.
     */
    public ThreadedServer(int port)
    {
        setAddress(new InetAddrPort(port));
    }
    
    /* ------------------------------------------------------------------- */
    /** Construct for specific address and port.
     */
    public ThreadedServer(InetAddress address, int port) 
    {
        setAddress(new InetAddrPort(address,port));
    }
    
    /* ------------------------------------------------------------------- */
    /** Construct for specific address and port.
     */
    public ThreadedServer(String host, int port) 
        throws UnknownHostException
    {
        setAddress(new InetAddrPort(host,port));
    }
    
    /* ------------------------------------------------------------------- */
    /** Construct for specific address and port.
     */
    public ThreadedServer(InetAddrPort address) 
    {
        setAddress(address);
    }
    
    
    /* ------------------------------------------------------------ */
    /** Set the server InetAddress and port.
     * @param address The Address to listen on, or 0.0.0.0:port for
     * all interfaces.
     */
    public synchronized void setAddress(InetAddrPort address) 
    {
        if (isStarted())
        {
            Code.debug( "Restart for ", address );
            try{stop();}catch(InterruptedException e){Code.warning(e);}
            _address = address;
            start();
        }
        else
            _address = address;
    }

    /* ------------------------------------------------------------ */
    /** 
     * @param host 
     */
    public synchronized void setHost(String host)
        throws UnknownHostException
    {
        setAddress(new InetAddrPort(host,_address==null?0:_address.getPort()));
    }
    
    /* ------------------------------------------------------------ */
    /** 
     * @param addr 
     */
    public synchronized void setInetAddress(InetAddress addr)
    {
        setAddress(new InetAddrPort(addr,_address==null?0:_address.getPort()));
    }
    
    /* ------------------------------------------------------------ */
    /** 
     * @param port 
     */
    public synchronized void setPort(int port)
    {
        setAddress(_address==null?new InetAddrPort(port)
            :new InetAddrPort(_address.getInetAddress(),port));
    }

    /* ------------------------------------------------------------ */
    /** Set Max Read Time.
     * Setting this to a none zero value results in setSoTimeout being
     * called for all accepted sockets.  This causes an
     * InterruptedIOException if a read blocks for this period of time.
     * @param ms 
     */
    public void setMaxReadTimeMs(int ms)
    {
        _maxReadTimeMs=ms;
    }
    
    /* ------------------------------------------------------------ */
    /** 
     * @return milliseconds
     */
    public int getMaxReadTimeMs()
    {
        return _maxReadTimeMs;
    }
    
    /* ------------------------------------------------------------ */
    /** 
     * @param sec seconds to linger or -1 to disable linger.
     */
    public void setLingerTimeSecs(int ls)
    {
        _lingerTimeSecs=ls;
    }
    
    /* ------------------------------------------------------------ */
    /** 
     * @return seconds.
     */
    public int getLingerTimeSecs()
    {
        return _lingerTimeSecs;
    }
    
    
    /* ------------------------------------------------------------ */
    /** 
     * @return IP Address and port in a new Instance of InetAddrPort.
     */
    public InetAddrPort getInetAddrPort()
    {
        return new InetAddrPort(_address);
    }
    
    /* ------------------------------------------------------------ */
    /** 
     * @return IP Address
     */
    public InetAddress getInetAddress()
    {
        return _address.getInetAddress();
    }
    
    /* ------------------------------------------------------------ */
    /** 
     * @return Host name
     */
    public String getHost()
    {
        return _address.getInetAddress().getHostName();
    }
    
    /* ------------------------------------------------------------ */
    /** 
     * @return port number
     */
    public int getPort()
    {
        return _address.getPort();
    }
    
    /* ------------------------------------------------------------------- */
    /** Handle new connection.
     * This method should be overridden by the derived class to implement
     * the required handling.  It is called by a thread created for it and
     * does not need to return until it has finished it's task
     */
    protected void handleConnection(InputStream in,OutputStream out)
    {
        throw new Error("Either handlerConnection must be overridden");
    }

    /* ------------------------------------------------------------------- */
    /** Handle new connection.
     * If access is required to the actual socket, override this method
     * instead of handleConnection(InputStream in,OutputStream out).
     * The default implementation of this just calls
     * handleConnection(InputStream in,OutputStream out).
     */
    protected void handleConnection(Socket connection)
        throws IOException
    {
        Code.debug("Handle ",connection);
        InputStream in  = connection.getInputStream();
        OutputStream out = connection.getOutputStream();
        
        handleConnection(in,out);
        out.flush();
        
        in=null;
        out=null;
    }
    
    /* ------------------------------------------------------------ */
    /** Handle Job.
     * Implementation of ThreadPool.handle(), calls handleConnection.
     * @param job A Connection.
     */
    public final void handle(Object job)
    {
        Socket socket =(Socket)job;
        try
        {
            try {
  		if (_lingerTimeSecs>=0)
  		    socket.setSoLinger(true,_lingerTimeSecs);
  		else
  		    socket.setSoLinger(false,0);
  	    }
            catch ( Exception e ){Code.ignore(e);}
            
            handleConnection(socket); 

        }
        catch ( Exception e ){Code.warning("Connection problem",e);}
        finally
        {
            try {socket.close();}
            catch ( Exception e ){Code.warning("Connection problem",e);}
        }
    }
    
    
    
    /* ------------------------------------------------------------ */
    /** New server socket.
     * Creates a new servers socket. May be overriden by derived class
     * to create specialist serversockets (eg SSL).
     * @param address Address and port
     * @param acceptQueueSize Accept queue size
     * @return The new ServerSocket
     * @exception java.io.IOException 
     */
    protected ServerSocket newServerSocket(InetAddrPort address,
                                           int acceptQueueSize)
         throws java.io.IOException
    {
        if (address==null)
            return new ServerSocket(0,acceptQueueSize);

        return new ServerSocket(address.getPort(),
                                acceptQueueSize,
                                address.getInetAddress());
    }
    
    /* ------------------------------------------------------------ */
    /** Accept socket connection.
     * May be overriden by derived class
     * to create specialist serversockets (eg SSL).
     * @param serverSocket
     * @param timeout The time to wait for a connection. Normally
     *                 passed the ThreadPool maxIdleTime.
     * @return Accepted Socket
     */
    protected Socket acceptSocket(ServerSocket serverSocket,
                                  int timeout)
    {
        try
        {
            Socket s;
            
            if (_soTimeOut!=timeout)
            {
                _soTimeOut=timeout;
                _listen.setSoTimeout(_soTimeOut);
            }
            
            s=_listen.accept();
            
            if (_maxReadTimeMs>0)
                s.setSoTimeout(_maxReadTimeMs);
            return s;
        }
        catch ( java.net.SocketException e )
        {
            // XXX - this is caught and ignored due strange
            // exception from linux java1.2.v1a
            Code.ignore(e);
        }
        catch(InterruptedIOException e)
        {
            if (Code.verbose(99))
                Code.ignore(e);
        }
        catch(IOException e)
        {
            Code.warning(e);
        }
        return null;
    }
    
        
    /* ------------------------------------------------------------ */
    /** Get a job.
     * Implementation of ThreadPool.getJob that calls acceptSocket
     * @param timeoutMs Time to wait for a Job.  This is ignored as the
     *                  accept timeout has already been set on the server
     *                  socket.
     * @return An accepted connection.
     */
    protected final Object getJob(int timeoutMs)
    {
        return acceptSocket(_listen,timeoutMs);
    }
    
    /* ------------------------------------------------------------------- */
    /* Start the ThreadedServer listening
     */
    synchronized public void start()
    {
        if (isStarted())
        {
            Code.warning("Already started on "+_address);
            return;
        }

        try
        {
            _listen=newServerSocket(_address,
                                    getMaxThreads()>0?(getMaxThreads()+1):50);
            _address=new InetAddrPort(_listen.getInetAddress(),
                                      _listen.getLocalPort());

            _soTimeOut=getMaxIdleTimeMs();
            if (_soTimeOut>0)
                _listen.setSoTimeout(_soTimeOut);
            
            super.start();
        }
        catch(IOException e)
        {
            Code.warning(e);
        }        
    }

    /* --------------------------------------------------------------- */
    public void stop()
        throws InterruptedException
    {
        try{if (_listen!=null) _listen.setSoTimeout(100);}
        catch(SocketException e){Code.warning(e);}
        try {super.stop();}
        finally
        {
            try{if (_listen!=null) _listen.close();}
            catch(IOException e){Code.warning(e);}
            _listen=null;
        }
    }
    
    /* ------------------------------------------------------------ */
    /** Force a stop.
     * Close the socket, then make a connection to.
     * called from stop if interrupt is not enough
     */
    protected void forceStop()
    {
        try{
            if (_listen!=null)
                _listen.close();
            if (_address!=null)
            {
                InetAddress addr=_address.getInetAddress();
                if (addr==null)
                    addr=InetAddress.getLocalHost();
                new Socket(addr,_address.getPort());
                Code.debug("Socket is still listening!!!");
            }
        }
        catch(Exception e)
        {Code.debug(e);}
    }
    
    /* --------------------------------------------------------------- */
    synchronized public void destroy()
    {
        if (_listen!=null)
        {
            try{_listen.setSoTimeout(0);}
            catch(SocketException e){Code.warning(e);}
            try{_listen.close();}
            catch(IOException e){Code.warning(e);}
            _listen=null;
        }
        _address=null;
        super.destroy();
    }

    /* ------------------------------------------------------------ */
    /** Disabled.
     * This ThreadPool method is not applicable to the ThreadedServer.
     * @param job 
     */
    public final void run(Object job)
    {
        throw new IllegalStateException("Can't run jobs on ThreadedServer");
    }


    /* ------------------------------------------------------------ */
    public String toString()
    {
        return getName()+"@"+getInetAddrPort();
    }   
}





