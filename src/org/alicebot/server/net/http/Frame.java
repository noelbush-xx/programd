// ========================================================================
// Copyright (c) 1997 MortBay Consulting, Sydney
// $Id: Frame.java,v 1.1.1.1 2001/06/17 19:01:17 noelbu Exp $
// ========================================================================

package org.alicebot.server.net.http;

import java.io.PrintWriter;
import java.io.StringWriter;


/*-----------------------------------------------------------------------*/
/** Access the current execution frame.
 */
public class Frame
{
    /*-------------------------------------------------------------------*/
    /** Shared static instances, reduces object creation at expense
     * of lock contention in multi threaded debugging */
    private static Throwable __throwable = new Throwable();
    private static StringWriter __stringWriter = new StringWriter();
    private static StringBuffer __writerBuffer = __stringWriter.getBuffer();
    private static PrintWriter __out = new PrintWriter(__stringWriter,false);
    private static final String __lineSeparator = System.getProperty("line.separator");
    private static final int __lineSeparatorLen = __lineSeparator.length();
    
    /*-------------------------------------------------------------------*/
    /** The full stack of where the Frame was created. */
    public String _stack;
    /** The Method (including the "(file.java:99)") the Frame was created in */
    public String _method= "unknownMethod";
    /** The stack depth where the Frame was created (main is 1) */
    public int _depth=0;
    /** Name of the Thread the Frame was created in */
    public String _thread= "unknownThread";
    /** The file and linenumber of where the Frame was created. */
    public String _file= "UnknownFile";

    String _where;
    private int _lineStart=0;
    private int _lineEnd;
    /*-------------------------------------------------------------------*/
    /** Construct a frame.
     */
    public Frame()
    {
        // Dump the stack
        synchronized(__writerBuffer)
        {
            __writerBuffer.setLength(0);
            __throwable.fillInStackTrace();
            __throwable.printStackTrace(__out);
            __out.flush();
            _stack = __writerBuffer.toString();
        }
        internalInit(0, false);
    }
    
    /*-------------------------------------------------------------------*/
    /** Construct a frame.
     * @param ignoreFrames number of levels of stack to ignore
     */
    public Frame(int ignoreFrames)
    {
        // Dump the stack
        synchronized(__writerBuffer)
        {
            __writerBuffer.setLength(0);
            __throwable.fillInStackTrace();
            __throwable.printStackTrace(__out);
            __out.flush();
            _stack = __writerBuffer.toString();
        }
        internalInit(ignoreFrames, false);
    }
    
    /* ------------------------------------------------------------ */
    /** package private Constructor. 
     * @param ignoreFrames Number of frames to ignore
     * @param partial Partial construction if true
     */
    Frame(int ignoreFrames, boolean partial)
    {
        // Dump the stack
        synchronized(__writerBuffer)
        {
            __writerBuffer.setLength(0);
            __throwable.fillInStackTrace();
            __throwable.printStackTrace(__out);
            __out.flush();
            _stack = __writerBuffer.toString();
        }
        internalInit(ignoreFrames, partial);
    }
    
    /* ------------------------------------------------------------ */
    /** Internal only Constructor. */
    protected Frame(String stack, int ignoreFrames, boolean partial)
    {
        _stack = stack;
        internalInit(ignoreFrames, partial);
    }
    
    /* ------------------------------------------------------------ */
    protected void internalInit(int ignoreFrames, boolean partial)
    {   
        // Extract stack components, after we look for the Frame constructor
 	// itself and pull that off the stack!
 	_lineStart = _stack.indexOf("Frame.<init>(",_lineStart);
        _lineStart = _stack.indexOf(__lineSeparator,_lineStart)+
 	    __lineSeparatorLen;
        for (int i = 0; _lineStart > 0 && i < ignoreFrames; i++)
        {
            _lineStart = _stack.indexOf(__lineSeparator,_lineStart)+
                         __lineSeparatorLen;
        }
        _lineEnd = _stack.indexOf(__lineSeparator,_lineStart);
        
        if (_lineEnd < _lineStart || _lineStart < 0){
            _where = null;
            _stack = null;
        }
        else
        {
            _where = _stack.substring(_lineStart,_lineEnd);
            if (!partial) complete();
        }
    }
    
    /* ------------------------------------------------------------ */
    /** Complete partial constructor.
     */
    void complete()
    {
        // trim stack
        if (_stack != null) 
            _stack = _stack.substring(_lineStart);
        else
        {
            // Handle nulls
            if (_method==null)
                _method= "unknownMethod";
            if (_file==null)
                _file= "UnknownFile";
            return;
        }

        // calculate stack depth
        int i=0-__lineSeparatorLen;
        while ((i=_stack.indexOf(__lineSeparator,i+__lineSeparatorLen))>0)
                _depth++;
        
        // extract details
        if (_where!=null)
        {
            int lb = _where.indexOf('(');
            int rb = _where.indexOf(')');
            if (lb>=0 && rb >=0 && lb<rb)
                _file = _where.substring(lb+1,rb).trim();
            
            int at = _where.indexOf("at");
            if (at >=0 && (at+3)<_where.length())
                _method = _where.substring(at+3);
        }
        
        // Get Thread name
        _thread = Thread.currentThread().getName();

        // Handle nulls
        if (_method==null)
            _method= "unknownMethod";
        if (_file==null)
            _file= "UnknownFile";
    }
    
    
    /*-------------------------------------------------------------------*/
    public String file()
    {
        return _file;
    }
    
    /*-------------------------------------------------------------------*/
    public String toString()
    {
        return "["+_thread + "]" + _method;
    }
    
    /* ------------------------------------------------------------ */
    /** Get a Frame representing the function one level up in this frame.
     * @return parent frame or null if none
     */
    public Frame getParent(){
        Frame f = new Frame(_stack, 0, false);
        if (f._where == null) return null;
        f._thread = _thread;
        return f;
    }
    
    
}




