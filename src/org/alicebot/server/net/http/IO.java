// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: IO.java,v 1.1.1.1 2001/06/17 19:01:18 noelbu Exp $
// ---------------------------------------------------------------------------

package org.alicebot.server.net.http;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.io.ByteArrayOutputStream;

/* ======================================================================== */
/** IO Utilities.
 * Provides stream handling utilities in
 * singleton Threadpool implementation accessed by static members.
 */
public class IO extends ThreadPool
{
    /* ------------------------------------------------------------------- */
    public static int bufferSize = 8192;
    
    /* ------------------------------------------------------------------- */
    private static class Singleton {static final IO __instance=new IO();}
    public static IO instance()
    {
        return Singleton.__instance;
    }
    
    /* ------------------------------------------------------------------- */
    static class Job
    {
        InputStream in;
        OutputStream out;
        Reader read;
        Writer write;

        Job(InputStream in,OutputStream out)
        {
            this.in=in;
            this.out=out;
            this.read=null;
            this.write=null;
        }
        Job(Reader read,Writer write)
        {
            this.in=null;
            this.out=null;
            this.read=read;
            this.write=write;
        }
    }
    
    /* ------------------------------------------------------------------- */
    /** Copy Stream in to Stream out until EOF or exception.
     * in own thread
     */
    public static void copyThread(InputStream in, OutputStream out)
    {
        try{
            instance().run(new Job(in,out));
        }
        catch(InterruptedException e)
        {
            Code.warning(e);
        }
    }
    
    /* ------------------------------------------------------------------- */
    /** Copy Stream in to Stream out until EOF or exception.
     */
    public static void copy(InputStream in, OutputStream out)
         throws IOException
    {
        copy(in,out,-1);
    }
    
    /* ------------------------------------------------------------------- */
    /** Copy Stream in to Stream out until EOF or exception
     * in own thread
     */
    public static void copyThread(Reader in, Writer out)
    {
        try
        {
            instance().run(new Job(in,out));
        }
        catch(InterruptedException e)
        {
            Code.warning(e);
        }
    }
    
    /* ------------------------------------------------------------------- */
    /** Copy Reader to Writer out until EOF or exception.
     */
    public static void copy(Reader in, Writer out)
         throws IOException
    {
        copy(in,out,-1);
    }
    
    /* ------------------------------------------------------------------- */
    /** Copy Stream in to Stream for byteCount bytes or until EOF or exception.
     */
    public static void copy(InputStream in,
                            OutputStream out,
                            long byteCount)
         throws IOException
    {     
        byte buffer[] = new byte[bufferSize];
        int len=bufferSize;
        
        if (byteCount>=0)
        {
            while (byteCount>0)
            {
                if (byteCount<bufferSize)
                    len=in.read(buffer,0,(int)byteCount);
                else
                    len=in.read(buffer,0,bufferSize);                   
                
                if (len==-1)
                    break;
                
                byteCount -= len;
                out.write(buffer,0,len);
            }
        }
        else
        {
            while (true)
            {
                len=in.read(buffer,0,bufferSize);
                if (len<0 )
                    break;
                out.write(buffer,0,len);
            }
        }
    }  
    
    /* ------------------------------------------------------------------- */
    /** Copy Reader to Writer for byteCount bytes or until EOF or exception.
     */
    public static void copy(Reader in,
                            Writer out,
                            long byteCount)
         throws IOException
    {  
        char buffer[] = new char[bufferSize];
        int len=bufferSize;
        
        if (byteCount>=0)
        {
            while (byteCount>0)
            {
                if (byteCount<bufferSize)
                    len=in.read(buffer,0,(int)byteCount);
                else
                    len=in.read(buffer,0,bufferSize);                   
                
                if (len==-1)
                    break;
                
                byteCount -= len;
                out.write(buffer,0,len);
            }
        }
        else
        {
            while (true)
            {
                len=in.read(buffer,0,bufferSize);
                if (len==-1)
                    break;
                out.write(buffer,0,len);
            }
        }
    }

    /* ------------------------------------------------------------ */
    /** Read input stream to string 
     * @param in 
     * @return 
     */
    public static String toString(InputStream in)
        throws IOException
    {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        copy(in,out);
        return new String(out.toByteArray());
    }

    
    /* ------------------------------------------------------------ */
    /** Run copy for copyThread()
     */
    public void handle(Object o)
    {
        Job job=(Job)o;
        try {
            if (job.in!=null)
                copy(job.in,job.out,-1);
            else
                copy(job.read,job.write,-1);
        }
        catch(IOException e)
        {
            Code.ignore(e);
            try{
                if (job.out!=null)
                    job.out.close();
                if (job.write!=null)
                    job.write.close();
            }
            catch(IOException e2)
            {
                Code.ignore(e2);
            }
        }
    }

    /* ------------------------------------------------------------ */
    /** 
     * @return An outputstream to nowhere
     */
    public static OutputStream getNullStream()
    {
        return __nullStream;
    }
    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private static class NullOS extends OutputStream                                    
    {
        public void close(){}
        public void flush(){}
        public void write(byte[]b){}
        public void write(byte[]b,int i,int l){}
        public void write(int b){}
    }
    private static NullOS __nullStream = new NullOS();
    
    /* ------------------------------------------------------------ */
    /** 
     * @return An writer to nowhere
     */
    public static Writer getNullWriter()
    {
        return __nullWriter;
    }
    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private static class NullWrite extends Writer                                    
    {
        public void close(){}
        public void flush(){}
        public void write(char[]b){}
        public void write(char[]b,int o,int l){}
        public void write(int b){}
        public void write(String s){}
        public void write(String s,int o,int l){}
    }
    private static NullWrite __nullWriter = new NullWrite();
}









