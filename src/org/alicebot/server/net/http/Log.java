// ========================================================================
// Copyright (c) 1997 MortBay Consulting, Sydney
// $Id: Log.java,v 1.1.1.1 2001/06/17 19:01:18 noelbu Exp $
// ========================================================================

package org.alicebot.server.net.http;

import java.applet.Applet;
import java.io.IOException;
import java.io.PrintStream;
import java.util.StringTokenizer;

/*-----------------------------------------------------------------------*/
/** Log formatted and tagged messages.
 * Multiple LogSinks instances can be configured, but by default a
 * System.err sink is created.
 * <p>
 * The Log log format is controlled by the LOG_OPTIONS property
 * supplied to the VM. 
 * <p>If LOG_OPTIONS is set, then the default output format is controlled
 * by the option characters in the string:
 * <PRE>
 * t Timestamp log output
 * T Show the log tag name
 * L Show log label (thread, method and file names).
 * s Show indication of stack depth
 * S Stack trace for each output line (VERY VERBOSE)
 * O Place each log one One line of output
 * </PRE>
 *
 * <p> If the property LOG_CLASSES is set, it is interpreted as a 
 * semi-colon-separated list of fully-qualified LogSink class names.
 * An instance of each class, created with a default constructor,
 * is added to the list of log sinks.
 * 
 * Some possibilities for LOG_CLASSES are
 *  org.alicebot.server.net.http.util.LogSink - log to System.err
 *  org.alicebot.server.net.http.util.FileLogSink - log to file whose name is in LOG_FILE
 *  org.alicebot.server.net.http.util.RolloverFileLogSink - log to daily rollover logs
 *
 * <p> If the property LOG_CLASSES is missing, a single LogSink is
 * used to output to System.err.
 *
 * <p> If the property LOG_DATE_FORMAT is set, then it is interpreted
 * as a format string for java.text.SimpleDateFormat and used to
 * format the log timestamps. Note: The character '+' is replaced with
 * space in the date format string.  If LOG_TIMEZONE is set, it is used
 * to set the timezone of the log date format, otherwise GMT is used.
 *
 * <p> As an alternative to the above behavior, you can create LogSinks
 * in code and add() them to the Log. If you do this before the first
 * use of the log, the default initialization will be skipped.
 */
public class Log 
{
    /*-------------------------------------------------------------------*/
    public final static String DEBUG= "DEBUG  ";
    public final static String EVENT= "EVENT  ";
    public final static String WARN=  "WARN!! ";
    public final static String ASSERT="ASSERT ";
    public final static String FAIL=  "FAIL!! ";

    /*-------------------------------------------------------------------*/
    public LogSink[] _sinks = null;
    public String _logOptions=null;
    private boolean _initialized = false;

    /*-------------------------------------------------------------------*/
    private static class Singleton {static final Log __instance = new Log();}
    
    /*-------------------------------------------------------------------*/
    public static Log instance()
    {
        return Singleton.__instance;
    }
    
    /*-------------------------------------------------------------------*/
    /** Default initialization is used the first time we have to log
     *	unless a sink has been added with add(). _needInit allows us to
     *	distinguish between initial state and disabled state.
     */
    private synchronized void defaultInit() 
    {
        if (!_initialized)
        {
            _logOptions=System.getProperty("LOG_OPTIONS",
                                           Code.debug()?"stLT":"tT");
            String sinkClasses = System.getProperty("LOG_CLASSES",
                                                    "org.alicebot.server.net.http.util.WriterLogSink");
            StringTokenizer sinkTokens = new StringTokenizer(sinkClasses, ";");
                    
            LogSink sink= null;
            while (sinkTokens.hasMoreTokens())
            {
                String sinkClassName = sinkTokens.nextToken();
                    	
                try
                {
                    Class sinkClass = Class.forName(sinkClassName);
                    if (org.alicebot.server.net.http.util.LogSink.class.isAssignableFrom(sinkClass)) {
                        sink = (LogSink)sinkClass.newInstance();
                        sink.setOptions(_logOptions);
                        sink.start();
                        Singleton.__instance.add(sink);
                    }
                    else
                        // Can't use Code.fail here, that's what we're setting up
                        System.err.println(sinkClass+" is not a org.alicebot.server.net.http.util.LogSink");
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
            _initialized = true;
        }
    }
    
    /*-------------------------------------------------------------------*/
    /** Construct the shared instance of Log that decodes the
     * options setup in the environments properties.
     */
    private Log()
    {}


    /* ------------------------------------------------------------ */
    /** Add a Log Sink.
     * @param logSink 
     */
    public synchronized void add(LogSink logSink)
    {
        if (_sinks==null)
        {
            _sinks=new LogSink[1];
            _sinks[0]=logSink;
        }
        else
        {
            LogSink[] ns = new LogSink[_sinks.length+1];
            for (int i=_sinks.length;i-->0;)
                ns[i]=_sinks[i];
            ns[_sinks.length]=logSink;
            _sinks=ns;
        }
        _initialized = true;
    }
    
    /* ------------------------------------------------------------ */
    /** 
     * @return 
     */
    public LogSink[] getLogSinks()
    {
        return _sinks;
    }
    
    /* ------------------------------------------------------------ */
    /** 
     */
    public synchronized void deleteStoppedLogSinks()
    {
        if (_sinks!=null)
        {
            for (int s=_sinks.length;s-->0;)
            {
                if (_sinks[s]==null)
                    continue;
                if (!_sinks[s].isStarted())
                    _sinks[s]=null;
            }
        }
    }
    
    /* ------------------------------------------------------------ */
    /** No logging.
     * All log sinks are stopped and removed.
     */
    public void disableLog()
    {
        if (_sinks!=null) {
            for (int s=_sinks.length;s-->0;)
            {
                try{
                    if (_sinks[s]!=null)
                        _sinks[s].stop();
                }
                catch(InterruptedException e)
                {
                    Code.ignore(e);
                }
            }
            _sinks=null;
        }
        _initialized=true;
    }
    
    
    /*-------------------------------------------------------------------*/
    public static void message(String tag,
                               Object msg,
                               Frame frame)
    {
        long time = System.currentTimeMillis();
        instance().message(tag,msg,frame,time);
    }
    
    /* ------------------------------------------------------------ */
    /** Log an event.
     */
    public static void event(Object message, int stackDepth)
    {
        // Log.message(Log.EVENT,message,new Frame(stackDepth));
    }
    
    /* ------------------------------------------------------------ */
    /** Log an event.
     */
    public static void event(Object message)
    {
        // Log.message(Log.EVENT,message,new Frame(1));
    }
    
    /* ------------------------------------------------------------ */
    /** Log an warning.
     */
    public static void warning(Object message, int stackDepth)
    {
        Log.message(Log.WARN,message,new Frame(1));
    }
    
    /* ------------------------------------------------------------ */
    /** Log an warning.
     */
    public static void warning(Object message)
    {
        Log.message(Log.WARN,message,new Frame(1));
    }
    
    /* ------------------------------------------------------------ */
    /** Log a message.
     * @param tag Tag for type of log
     * @param msg The message
     * @param frame The frame that generated the message.
     * @param time The time stamp of the message.
     */
    public synchronized void message(String tag,
                                     Object msg,
                                     Frame frame,
                                     long time)
    {
        if (!_initialized)
            defaultInit();
        
        if (_sinks==null)
            return;
        for (int s=_sinks.length;s-->0;)
        {
            if (_sinks[s]==null)
                continue;
            
            if (_sinks[s].isStarted())
                _sinks[s].log(tag,msg,frame,time);
        }
    }

    
    /*-------------------------------------------------------------------*/
    public synchronized void setOptions(String logOptions)
    {
        _logOptions=logOptions;
        
        for (int s=_sinks.length;s-->0;)
        {
            if (_sinks[s]==null)
                continue;
            _sinks[s].setOptions(logOptions);
        }
    }
    
    /* ------------------------------------------------------------ */
    public String getOptions()
    {
        return _logOptions;
    }
}

