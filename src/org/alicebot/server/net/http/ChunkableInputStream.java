// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: ChunkableInputStream.java,v 1.1.1.1 2001/06/17 19:00:28 noelbu Exp $
// ---------------------------------------------------------------------------

package org.alicebot.server.net.http;

import org.alicebot.server.net.http.util.Code;
import org.alicebot.server.net.http.util.LineInput;
import org.alicebot.server.net.http.util.StringUtil;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;


/* ------------------------------------------------------------ */
/** HTTP Chunking InputStream. 
 * This FilterInputStream acts as a BufferedInputStream until
 * setChunking(true) is called.  Once chunking is
 * enabled, the raw stream is chunk decoded as per RFC2616.
 *
 * The "8859-1" encoding is used on underlying LineInput instance for
 * line based reads from the raw stream.
 *
 * This class is not synchronized and should be synchronized
 * explicitly if an instance is used by multiple threads.
 *
 * @see org.alicebot.server.net.http.util.LineInput.
 * @version $Id: ChunkableInputStream.java,v 1.1.1.1 2001/06/17 19:00:28 noelbu Exp $
 * @author Greg Wilkins (gregw)
 */
public class ChunkableInputStream extends FilterInputStream
{
    /* ------------------------------------------------------------ */
    /** Limit max line length */
    public static int __maxLineLength=4096;
    
    public final static Class[] __filterArg = {java.io.InputStream.class};
    private static ClosedStream __closedStream=new ClosedStream();
    
    /* ------------------------------------------------------------ */
    private DeChunker _deChunker;
    private LineInput _realIn;
    private boolean _chunking;
    private int _filters;
    
    /* ------------------------------------------------------------ */
    /** Constructor.
     */
    public ChunkableInputStream( InputStream in)
    {
        super(null);
        try {
            _realIn= new LineInput(in,0,StringUtil.__ISO_8859_1);
        }
        catch(UnsupportedEncodingException e)
        {
            Code.fail(e);
        }
        this.in=_realIn;
    }
    
    /* ------------------------------------------------------------ */
    /** Get the raw stream.
     * A stream without filters or chunking is returned. This stream
     * may still be buffered and uprocessed bytes may be in the buffer.
     * @return Raw InputStream.
     */
    public InputStream getRawStream()
    {
        return _realIn;
    }
    
    /* ------------------------------------------------------------ */
    /** Get chunking mode 
     */
    public boolean isChunking()
    {
        return _chunking;
    }
    
    /* ------------------------------------------------------------ */
    /** Set chunking mode.
     * Chunking can only be turned off with a call to resetStream().
     * @exception IllegalStateException Checking cannot be set if
     * a content length has been set.
     */
    public void setChunking()
        throws IllegalStateException
    {
        if (_realIn.getByteLimit()>=0)
            throw new IllegalStateException("Has Content-Length");
        if (_deChunker==null)
            _deChunker=new DeChunker();
        in=_deChunker;
        
        _chunking=true;
        _deChunker._trailer=null;
    }

    /* ------------------------------------------------------------ */
    /** Reset the stream.
     * Turn chunking off and disable all filters.
     * @exception IllegalStateException The stream cannot be reset if
     * there is some unread chunked input or a content length greater
     * than zero remaining.
     */
    public void resetStream()
        throws IllegalStateException
    {
        if ((_deChunker!=null && _deChunker._chunkSize>0) ||
            _realIn.getByteLimit()>0)
            throw new IllegalStateException("Unread input");
        if (Code.verbose())
            Code.debug("resetStream()");
        in=_realIn;
        _filters=0;
        if (_deChunker!=null)
            _deChunker._trailer=null;
        _chunking=false;
        _realIn.setByteLimit(-1);
    }
 
    /* ------------------------------------------------------------ */
    public void close()
        throws IOException
    {
        in=__closedStream;
    }
 
    /* ------------------------------------------------------------ */
    /** Insert FilterInputStream.
     * Place a Filtering InputStream into this stream, after the
     * chunking stream.  
     * @param filter The Filter constructor.  It must take an InputStream
     *             as the first arguement.
     * @param arg  Optional argument array to pass to filter constructor.
     *             The first element of the array is replaced with the
     *             current input stream.
     */
    public void insertFilter(Constructor filter,
                             Object[] args)
        throws InstantiationException,
               InvocationTargetException,
               IllegalAccessException
    {
        if (args==null || args.length<1)
            args=new Object[1];
        
        args[0]=in;
        in=(InputStream)filter.newInstance(args);
        _filters++;
    }
    
    
    /* ------------------------------------------------------------ */
    /** Set the content length.
     * Only this number of bytes can be read before EOF is returned.
     * @param len length.
     */
    public void setContentLength(int len)
    {
        if (_chunking && len>=0)
            throw new IllegalStateException("Chunking");
        _realIn.setByteLimit(len);
    }
    
    /* ------------------------------------------------------------ */
    /** Get the content length.
     * @return Number of bytes until EOF is returned or -1 for no limit.
     */
    public int getContentLength()
    {
        return _realIn.getByteLimit();
    }

    /* ------------------------------------------------------------ */
    public HttpFields getTrailer()
    {
        return _deChunker._trailer;
    }
    
    /* ------------------------------------------------------------ */
    /** A closed input stream.
     */
    private static class ClosedStream extends InputStream
    {
        /* ------------------------------------------------------------ */
        public int read()
            throws IOException
        {
            return -1;
        }
    }
    
    /* ------------------------------------------------------------ */
    /** Dechunk input.
     * Or limit content length.
     */
    private class DeChunker extends InputStream
    {
        /* ------------------------------------------------------------ */
        int _chunkSize=0;
        HttpFields _trailer=null;
        
        /* ------------------------------------------------------------ */
        /** Constructor.
         */
        public DeChunker()
        {}

        /* ------------------------------------------------------------ */
        public int read()
            throws IOException
        {
            int b=-1;
            if (_chunkSize<=0 && getChunkSize()<=0)
                return -1;
            b=_realIn.read();
            _chunkSize=(b<0)?-1:(_chunkSize-1);
            return b;
        }
 
        /* ------------------------------------------------------------ */
        public int read(byte b[]) throws IOException
        {
            int len = b.length;
            if (_chunkSize<=0 && getChunkSize()<=0)
                return -1;
            if (len > _chunkSize)
                len=_chunkSize;
            len=_realIn.read(b,0,len);
            _chunkSize=(len<0)?-1:(_chunkSize-len);
            return len;
        }
 
        /* ------------------------------------------------------------ */
        public int read(byte b[], int off, int len) throws IOException
        {  
            if (_chunkSize<=0 && getChunkSize()<=0)
                return -1;
            if (len > _chunkSize)
                len=_chunkSize;
            len=_realIn.read(b,off,len);
            _chunkSize=(len<0)?-1:(_chunkSize-len);
            return len;
        }
    
        /* ------------------------------------------------------------ */
        public long skip(long len) throws IOException
        { 
            if (_chunkSize<=0 && getChunkSize()<=0)
                return -1;
            if (len > _chunkSize)
                len=_chunkSize;
            len=_realIn.skip(len);
            _chunkSize=(len<0)?-1:(_chunkSize-(int)len);
            return len;
        }

        /* ------------------------------------------------------------ */
        public int available()
            throws IOException
        {
            int len = _realIn.available();
            if (len<=_chunkSize)
                return len;
            return _chunkSize;
        }
 
        /* ------------------------------------------------------------ */
        public void close()
            throws IOException
        {
            Code.debug("Close");
            _chunkSize=-1;
        }
 
        /* ------------------------------------------------------------ */
        /** Mark is not supported.
         * @return false
         */
        public boolean markSupported()
        {
            return false;
        }
    
        /* ------------------------------------------------------------ */
        /** Not Implemented.
         */
        public void reset()
        {
            Code.notImplemented();
        }

        /* ------------------------------------------------------------ */
        /** Not Implemented.
         * @param readlimit 
         */
        public void mark(int readlimit)
        {
            Code.notImplemented();
        }
    
        /* ------------------------------------------------------------ */
        /* Get the size of the next chunk.
         * @return size of the next chunk or -1 for EOF.
         * @exception IOException 
         */
        private int getChunkSize()
            throws IOException
        {
            if (_chunkSize<0)
                return -1;
        
            _trailer=null;
            _chunkSize=-1;

            // Get next non blank line
            org.alicebot.server.net.http.util.LineInput.LineBuffer line_buffer
                =_realIn.readLineBuffer();
            while(line_buffer!=null && line_buffer.size==0)
                line_buffer=_realIn.readLineBuffer();
            
            // Handle early EOF or error in format
            if (line_buffer==null)
            {
                Code.warning("EOF");
                return -1;
            }
            String line= new String(line_buffer.buffer,0,line_buffer.size);
            
        
            // Get chunksize
            int i=line.indexOf(';');
            if (i>0)
                line=line.substring(0,i).trim();
            _chunkSize = Integer.parseInt(line,16);
        
            // check for EOF
            if (_chunkSize==0)
            {
                _chunkSize=-1;
                // Look for trailers
                _trailer = new HttpFields();
                _trailer.read(_realIn);
            }

            return _chunkSize;
        }
    }
}
