// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: ChunkableOutputStream.java,v 1.1.1.1 2001/06/17 19:00:30 noelbu Exp $
// ---------------------------------------------------------------------------

package org.alicebot.server.net.http;

import org.alicebot.server.net.http.util.Code;
import org.alicebot.server.net.http.util.ByteBufferOutputStream;
import org.alicebot.server.net.http.util.IO;
import org.alicebot.server.net.http.util.StringUtil;
import java.io.ByteArrayOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

/* ---------------------------------------------------------------- */
/** HTTP Chunkable OutputStream.
 * Acts as a BufferedOutputStream until setChunking(true) is called.
 * Once chunking is enabled, the raw stream is chunk encoded as per RFC2616.
 *
 * Implements the following HTTP and Servlet features: <UL>
 * <LI>Filters for content and transfer encodings.
 * <LI>Allows output to be reset if not committed (buffer never flushed).
 * <LI>Notification of significant output events for filter triggering,
 *     header flushing, etc.
 * </UL>
 *
 * This class is not synchronized and should be synchronized
 * explicitly if an instance is used by multiple threads.
 *
 * @version $Id: ChunkableOutputStream.java,v 1.1.1.1 2001/06/17 19:00:30 noelbu Exp $
 * @author Greg Wilkins
*/
public class ChunkableOutputStream extends FilterOutputStream
{
    /* ------------------------------------------------------------ */
    final static String
        __CRLF      = "\015\012";
    final static byte[]
        __CRLF_B    = {(byte)'\015',(byte)'\012'};
    final static byte[]
        __CHUNK_EOF_B ={(byte)'0',(byte)'\015',(byte)'\012'};
    
    public final static Class[] __filterArg = {java.io.OutputStream.class};
        
    
    /* ------------------------------------------------------------ */
    OutputStream _realOut;
    ByteBufferOutputStream _buffer;
    boolean _chunking;
    HttpFields _trailer;
    boolean _committed;
    boolean _written;
    int _filters;
    ArrayList _observers;
    OutputStreamWriter _rawWriter;
    RawOutputStream _rawWriterBuffer;
    boolean _nulled=false;
    
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param outputStream The outputStream to buffer or chunk to.
     */
    public ChunkableOutputStream(OutputStream outputStream)
    {
        super(new ByteBufferOutputStream());
        _buffer=(ByteBufferOutputStream)out;
        _realOut=outputStream;
        _committed=false;
        _written=false;
    }

    /* ------------------------------------------------------------ */
    /** Get the raw stream.
     * A stream without filters or chunking is returned.
     * @return Raw OutputStream.
     */
    public OutputStream getRawStream()
    {
        return _realOut;
    }

    
    /* ------------------------------------------------------------ */
    /** Get Writer for the raw stream.
     * A writer without filters or chunking is returned, which uses
     * the 8859-1 encoding. The converted bytes from this writer will be
     * writen to the rawStream when writeRawWriter() is called.
     * These methods allow Character encoded data to be mixed with
     * raw data on the same stream without excessive buffering or flushes.
     * @return Raw Writer
     */
    public Writer getRawWriter()
    {
        if (_rawWriter==null)
        {
            try
            {
                _rawWriterBuffer=new RawOutputStream(1024);
                _rawWriter=new OutputStreamWriter(_rawWriterBuffer,StringUtil.__ISO_8859_1);
            }
            catch(IOException e)
            {
                Code.warning(e);
            }
        }
        
        return _rawWriter;
    }
    
    
    /* ------------------------------------------------------------ */
    /** Has any data been written to the stream.
     * @return True if write has been called.
     */
    public boolean isWritten()
    {
        return _written;
    }
    
    /* ------------------------------------------------------------ */
    /** Has any data been sent from this stream.
     * @return True if buffer has been flushed to destination.
     */
    public boolean isCommitted()
    {
        return _committed;
    }
        
    /* ------------------------------------------------------------ */
    /** Get the output buffer capacity.
     * @return Buffer capacity in bytes.
     */
    public int getBufferCapacity()
    {
        return _buffer.getCapacity();
    }
    
    /* ------------------------------------------------------------ */
    /** Set the output buffer capacity.
     * Note that this is the minimal buffer capacity and that installed
     * filters may perform their own buffering and are likely to change
     * the size of the output.
     * @param capacity Minimum buffer capacity in bytes
     * @exception IllegalStateException If output has been written.
     */
    public void setBufferCapacity(int capacity)
        throws IllegalStateException
    {
        if (capacity<=getBufferCapacity())
            return;
        
        if (_buffer.size()>0)
            throw new IllegalStateException("Buffer is not empty");
        if (_committed)
            throw new IllegalStateException("Output committed");
        if (out!=_buffer)
            throw new IllegalStateException("Filter(s) installed");

        _buffer.ensureCapacity(capacity);
    }

    
    /* ------------------------------------------------------------ */
    /** Reset Buffered output.
     * If no data has been committed, the buffer output is discarded and
     * the filters may be reinitialized.
     * @exception IllegalStateException
     * @exception Problem with observer notification.
     */
    public void resetBuffer()
        throws IllegalStateException
    {
        if (_committed)
            throw new IllegalStateException("Output committed");

        if (Code.verbose())
            Code.debug("resetBuffer()");
        
        // Shutdown filters without observation
        ArrayList save_observers=_observers;
        _observers=null;
        try
        {
            out.flush();
            out.close();
        }
        catch(Exception e)
        {
            Code.ignore(e);
        }
        finally
        {
            _observers=save_observers;
        }

        // discard current buffer and set it to output
        _buffer.reset();
        out=_buffer;
        _filters=0;
        _written=false;
        _committed=false;
        try
        {
            notify(OutputObserver.__RESET_BUFFER);
        }
        catch(IOException e)
        {
            Code.ignore(e);
        }
    }

    /* ------------------------------------------------------------ */
    /** Add an Output Observer.
     * Output Observers get notified of significant events on the
     * output stream. They are removed when the stream is closed.
     * @param observer The observer. 
     */
    public void addObserver(OutputObserver observer)
    {
        if (_observers==null)
            _observers=new ArrayList(4);
        _observers.add(observer);
    }
    
    
    /* ------------------------------------------------------------ */
    /** Null the output.
     * All output written is discarded until the stream is reset. Used
     * for HEAD requests.
     */
    public void nullOutput()
        throws IOException
    {
        _nulled=true;
    }
    
    /* ------------------------------------------------------------ */
    /** is the output Nulled?
     */
    public boolean isNullOutput()
        throws IOException
    {
        return _nulled;
    }
    
    /* ------------------------------------------------------------ */
    /** Set chunking mode.
     */
    public void setChunking()
        throws IOException
    {
        _chunking=true;
    }
    
    /* ------------------------------------------------------------ */
    /** Reset the stream.
     * Turn disable all filters.
     * @exception IllegalStateException The stream cannot be
     * reset if chunking is enabled.
     */
    public void resetStream()
        throws IllegalStateException
    {
        if (isChunking())
            throw new IllegalStateException("Chunking");
        
        if (Code.verbose())
            Code.debug("resetStream()");
        
        _trailer=null;
        _committed=false;
        _written=false;
        _buffer.reset();
        out=_buffer;    
        _filters=0;
        _nulled=false;

        if (_rawWriter!=null)
        {
            try
            {    
                _rawWriter.flush();
                _rawWriterBuffer.reset();
            }
            catch(IOException e)
            {
                Code.warning(e);
                _rawWriterBuffer=null;
                _rawWriter=null;
            }
        }
    }
        
    /* ------------------------------------------------------------ */
    /** Get chunking mode 
     */
    public boolean isChunking()
    {
        return _chunking;
    }
    
    /* ------------------------------------------------------------ */
    /** Insert FilterOutputStream.
     * Place a Filtering OutputStream into this stream, but before the
     * chunking stream.  
     * @param filter The Filter constructor.  It must take an OutputStream
     *             as the first arguement.
     * @param arg  Optional argument array to pass to filter constructor.
     *             The first element of the array is replaced with the
     *             current output stream.
     */
    public void insertFilter(Constructor filter,
                                          Object[] args)
        throws InstantiationException,
               InvocationTargetException,
               IllegalAccessException
    {
        if (args==null || args.length<1)
            args=new Object[1];
        
        args[0]=out;
        out=(OutputStream)filter.newInstance(args);
        _filters++;
    }

    /* ------------------------------------------------------------ */
    /** Set the trailer to send with a chunked close.
     * @param trailer 
     */
    public void setTrailer(HttpFields trailer)
    {
        if (!isChunking())
            throw new IllegalStateException("Not Chunking");
        _trailer=trailer;
    }
    
    /* ------------------------------------------------------------ */
    public void write(int b) throws IOException
    {
        if (!_written)
        {
            _written=true;
            notify(OutputObserver.__FIRST_WRITE);
        }
        
        out.write(b);
        if (_buffer.isFull())
            flush();
    }

    /* ------------------------------------------------------------ */
    public void write(byte b[]) throws IOException
    {
        if (!_written)
        {
            _written=true;
            notify(OutputObserver.__FIRST_WRITE);
        }
        out.write(b);
        if (_buffer.isFull())
            flush();
    }

    /* ------------------------------------------------------------ */
    public void write(byte b[], int off, int len) throws IOException
    {
        if (!_written)
        {
            _written=true;
            notify(OutputObserver.__FIRST_WRITE);
        }
        out.write(b,off,len);
        if (_buffer.isFull())
            flush();
    }

    /* ------------------------------------------------------------ */
    public void flush() throws IOException
    {
        flush(false);
    }
    
    /* ------------------------------------------------------------ */
    public void flush(boolean endChunking) throws IOException
    {
        // Flush filters
        if (out!=null)
            out.flush();
        if (_rawWriter!=null)
            _rawWriter.flush();

        // Save non-raw size
        int size=_buffer.size();
        
        // Do we need to commit?
        boolean commiting=false;
        if (!_committed && (size>0 || (_rawWriterBuffer!=null && _rawWriterBuffer.size()>0)))
        {
            // this may recurse to flush so set committed now
            _committed=true;
            commiting=true;
            notify(OutputObserver.__COMMITING);
            if (out!=null)
                out.flush();
            if (_rawWriter!=null)
                _rawWriter.flush();
            size=_buffer.size();
        }

        try
        {
            if (_nulled)
            {
                // Just write the contents of the rawWriter
                _rawWriterBuffer.writeTo(_realOut);
            }
            else
            {
                // Handle chunking
                if (_chunking)
                {
                    Writer writer=getRawWriter();
                    if (size>0)
                    {
                        writer.write(Integer.toString(size,16));
                        writer.write(__CRLF);
                        writer.flush();
                        _buffer.write(__CRLF_B);
                    }

                    if (endChunking)
                    {
                        _buffer.write(__CHUNK_EOF_B);
                        if (_trailer==null)
                            _buffer.write(__CRLF_B);
                    }
                }
                
                // Pre write the raw writer to the buffer
                if (_rawWriterBuffer!=null && _rawWriterBuffer.size()>0)
                    _buffer.prewrite(_rawWriterBuffer.getBuf(),0,_rawWriterBuffer.getCount());
                
                // Handle any trailers
                if (_trailer!=null && endChunking)
                {
                    Writer writer=getRawWriter();
                    _rawWriterBuffer.reset();
                    _trailer.write(writer);
                    writer.flush();
                    _rawWriterBuffer.writeTo(_buffer);
                }
                
                // Write the buffer
                if (_buffer.size()>0)
                    _buffer.writeTo(_realOut);                
            }
            _realOut.flush();
        }
        finally
        {
            _buffer.reset();
            if (_rawWriterBuffer!=null)
                _rawWriterBuffer.reset();

            if (endChunking)
            {
                if (!_chunking)
                    throw new IllegalStateException("Not Chunking");
                _chunking=false;
            }
            
            if (commiting)
                notify(OutputObserver.__COMMITED);
        }
    }

    /* ------------------------------------------------------------ */
    /** Close the stream.
     * In chunking mode, the underlying stream is not closed.
     * All filters are closed and discarded.
     * @exception IOException 
     */
    public void close()
        throws IOException
    {
        // Are we already closed?
        if (out==null)
            return;

        // Close
        try {
            notify(OutputObserver.__CLOSING);
            
            // close filters
            out.close();
            out=null;

            if (_chunking)
                flush(true);
            else
            {
                flush(false);
                _realOut.close();
            }
            
            notify(OutputObserver.__CLOSED);
        }
        catch (IOException e)
        {
            Code.ignore(e);
        }
    }

    /* ------------------------------------------------------------ */
    /* Notify observers of action.
     * @see OutputObserver
     * @param action the action.
     */
    private void notify(int action)
        throws IOException
    {
        if (_observers!=null)
            for (int i=0;i<_observers.size();i++)
                ((OutputObserver)_observers.get(i))
                    .outputNotify(this,action);
    }


    /* ------------------------------------------------------------ */
    public void write(InputStream in, int len)
        throws IOException
    {
        IO.copy(in,this,len);
    }
    
    /* ------------------------------------------------------------ */
    public void println()
        throws IOException
    {
        write("\n".getBytes());
    }
    
    /* ------------------------------------------------------------ */
    public void println(Object o)
        throws IOException
    {
        if (o!=null)
            write(o.toString().getBytes());
        write("\n".getBytes());
    }
    
    /* ------------------------------------------------------------ */
    public void print(Object o)
        throws IOException
    {
        if (o!=null)
            write(o.toString().getBytes());
    }
    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private static class RawOutputStream extends ByteArrayOutputStream
    {
        RawOutputStream(int size){super(size);}
        byte[] getBuf(){return buf;}
        int getCount(){return count;}
    }
}
