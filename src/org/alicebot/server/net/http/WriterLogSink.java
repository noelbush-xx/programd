// ========================================================================
// Copyright (c) 1997 MortBay Consulting, Sydney
// $Id: WriterLogSink.java,v 1.1.1.1 2001/06/17 19:01:40 noelbu Exp $
// ========================================================================

package org.alicebot.server.net.http;

import java.io.PrintWriter;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.FileWriter;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.StringTokenizer;
import java.util.Calendar;
import java.util.GregorianCalendar;


/* ------------------------------------------------------------ */
/** A Log sink.
 * This class represents both a concrete or abstract sink of
 * Log data.  The default implementation logs to a PrintWriter, but
 * derived implementations may log to files, syslog, or other
 * logging APIs.
 *
 * If a logFilename is specified, output is sent to that file.
 * If the filename contains "yyyy_mm_dd", the log file date format
 * is used to create the actual filename and the log file is rolled
 * over at local midnight.
 * If append is set, existing logfiles are appended to, otherwise
 * a backup is created with a timestamp.
 * Dated log files are deleted after retain days.
 * 
 * @version $Id: WriterLogSink.java,v 1.1.1.1 2001/06/17 19:01:40 noelbu Exp $
 * @author Greg Wilkins (gregw)
 */
public class WriterLogSink
    implements LogSink
{
    /*-------------------------------------------------------------------*/
    public final static char OPT_TIMESTAMP = 't';
    public final static char OPT_LABEL = 'L';
    public final static char OPT_TAG = 'T';
    public final static char OPT_STACKSIZE = 's';
    public final static char OPT_STACKTRACE = 'S';
    public final static char OPT_ONELINE = 'O';
    
    /* ------------------------------------------------------------ */
    private final static String __lineSeparator =
        System.getProperty("line.separator");
    private final static String __indentBase ="";
    private final static String __indentSeparator =
        __lineSeparator+__indentBase;
    private final static int __lineSeparatorLen =
        __lineSeparator.length();
    
    private final static String YYYY_MM_DD="yyyy_mm_dd";
    
    private static SimpleDateFormat __fileBackupFormat =
        new SimpleDateFormat(System.getProperty("LOG_FILE_BACKUP_FORMAT","HHmmssSSS"));    
    
    /*-------------------------------------------------------------------*/
    private SimpleDateFormat _fileDateFormat = 
        new SimpleDateFormat(System.getProperty("LOG_FILE_DATE_FORMAT","yyyy_MM_dd"));
    private int _retainDays =Integer.getInteger("LOG_FILE_RETAIN_DAYS",31).intValue();
    
    protected DateCache _dateFormat=
        new DateCache(System.getProperty("LOG_DATE_FORMAT","HH:mm:ss.SSS "));
    protected String _logTimezone=
	System.getProperty("LOG_TIME_ZONE");    
    {
        if (_logTimezone!=null)
            _dateFormat.getFormat().setTimeZone(TimeZone.getTimeZone(_logTimezone));
    }

    /* ------------------------------------------------------------ */
    protected boolean _logTimeStamps=true;
    protected boolean _logLabels=true;
    protected boolean _logTags=true;
    protected boolean _logStackSize=true;
    protected boolean _logStackTrace=false;
    protected boolean _logOneLine=false;
    
    /*-------------------------------------------------------------------*/
    protected PrintWriter _out;
    protected boolean _started;
    private String _filename;
    private boolean _append=true;
    private StringBuffer _stringBuffer = new StringBuffer(512);
    private Thread _rollover;
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     */
    public WriterLogSink()
        throws IOException
    {
        _filename=System.getProperty("LOG_FILE");
        if (_filename==null)
            _out=new PrintWriter(System.err);
    }
        
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param out 
     */
    public WriterLogSink(PrintWriter out)
    {
        _out=out;
    }
    
    /* ------------------------------------------------------------ */
    public WriterLogSink(String filename)
        throws IOException
    {
        _filename=filename;
    }
    
    /* ------------------------------------------------------------ */
    public void setOptions(String logOptions)
    {  
        setOptions((logOptions.indexOf(OPT_TIMESTAMP) >= 0),
                   (logOptions.indexOf(OPT_LABEL) >= 0),
                   (logOptions.indexOf(OPT_TAG) >= 0),
                   (logOptions.indexOf(OPT_STACKSIZE) >= 0),
                   (logOptions.indexOf(OPT_STACKTRACE) >= 0),
                   (logOptions.indexOf(OPT_ONELINE) >= 0));
    }
    
    /* ------------------------------------------------------------ */
    public String getOptions()
    {
        return
            (_logTimeStamps?"t":"")+
            (_logLabels?"L":"")+
            (_logTags?"T":"")+
            (_logStackSize?"s":"")+
            (_logStackTrace?"S":"")+
            (_logOneLine?"O":"");
    }
    
    
    /*-------------------------------------------------------------------*/
    /** Set the log options.
     *
     * @param logOptions A string of characters as defined for the
     * LOG_OPTIONS system parameter.
     */
    public void setOptions(boolean logTimeStamps,
                           boolean logLabels,
                           boolean logTags,
                           boolean logStackSize,
                           boolean logStackTrace,
                           boolean logOneLine)
    {
        _logTimeStamps      = logTimeStamps;
        _logLabels          = logLabels;
        _logTags            = logTags;
        _logStackSize       = logStackSize;
        _logStackTrace      = logStackTrace;
        _logOneLine         = logOneLine;
    }
    
    /* ------------------------------------------------------------ */
    public DateCache getLogDateFormat()
    {
        return _dateFormat;
    }
    /* ------------------------------------------------------------ */
    public void setLogDateFormat(String logDateFormat)
    {
        logDateFormat = logDateFormat.replace('+',' ');
        _dateFormat = new DateCache(logDateFormat);
        if (_logTimezone!=null)
            _dateFormat.getFormat().setTimeZone(TimeZone.getTimeZone(_logTimezone));
    }
    /* ------------------------------------------------------------ */
    public String getLogTimezone()
    {
        return _logTimezone;
    }
    /* ------------------------------------------------------------ */
    public void setLogTimezone(String logTimezone)
    {
        _logTimezone=logTimezone;
        if (_dateFormat!=null && _logTimezone!=null)
            _dateFormat.getFormat().setTimeZone(TimeZone.getTimeZone(_logTimezone));
    }
    /* ------------------------------------------------------------ */
    public boolean isLogTimeStamps()
    {
        return _logTimeStamps;
    }
    /* ------------------------------------------------------------ */
    public void setLogTimeStamps(boolean logTimeStamps)
    {
        _logTimeStamps = logTimeStamps;
    }
    /* ------------------------------------------------------------ */
    public boolean isLogLabels()
    {
        return _logLabels;
    }
    /* ------------------------------------------------------------ */
    public void setLogLabels(boolean logLabels)
    {
        _logLabels = logLabels;
    }
    /* ------------------------------------------------------------ */
    public boolean isLogTags()
    {
        return _logTags;
    }
    /* ------------------------------------------------------------ */
    public void setLogTags(boolean logTags)
    {
        _logTags = logTags;
    }
    /* ------------------------------------------------------------ */
    public boolean isLogStackSize()
    {
        return _logStackSize;
    }
    /* ------------------------------------------------------------ */
    public void setLogStackSize(boolean logStackSize)
    {
        _logStackSize = logStackSize;
    }
    /* ------------------------------------------------------------ */
    public boolean isLogStackTrace()
    {
        return _logStackTrace;
    }
    /* ------------------------------------------------------------ */
    public void setLogStackTrace(boolean logStackTrace)
    {
        _logStackTrace = logStackTrace;
    }
    /* ------------------------------------------------------------ */
    public boolean isLogOneLine()
    {
        return _logOneLine;
    }
    /* ------------------------------------------------------------ */
    public void setLogOneLine(boolean logOneLine)
    {
        _logOneLine = logOneLine;
    }

    /* ------------------------------------------------------------ */
    public boolean isAppend()
    {
        return _append;
    }

    /* ------------------------------------------------------------ */
    public void setAppend(boolean a)
    {
        _append=a;
    }
    
    /* ------------------------------------------------------------ */
    public void setWriter(PrintWriter out)
    {
        synchronized(_stringBuffer)
        {
            setFilename(null);
            _out=out;
        }
    }

    /* ------------------------------------------------------------ */
    public PrintWriter getWriter()
    {
        return _out;
    }

    /* ------------------------------------------------------------ */
    public void setFilename(String filename)
    {
        synchronized(_stringBuffer)
        {
            try
            {
		if (filename!=null)
		{
		    filename=filename.trim();
		    if (filename.length()==0)
			filename=null;
		}

                // Do we need to close the last file?
		if (filename==null || !filename.equals(_filename))
		{
		    if (_out!=null && _filename!=null)
                    {
                        try{_out.close();}
                        catch(Exception e){e.printStackTrace();}
                        _out=null;
                    }
		    _filename=null;
                    if (_rollover!=null)
                        _rollover.interrupt();
                    _rollover=null;
		}
                
                // Do we have a new file
                if (filename !=null && !filename.equals(_filename))
                {
                    try{
                        _filename=filename;
                        if(isStarted())
                            openFile(filename);
                    }
                    catch(IOException e)
                    {
                        e.printStackTrace();
                        _out=null;
                        _filename=null;
                        setWriter(new PrintWriter(System.err));
		    }
		}
            }
            catch(Exception e)
            {
                e.printStackTrace();
            }
        }
        
        if (_filename==null && _out==null)
            _out=new PrintWriter(System.err);
    }

    /* ------------------------------------------------------------ */
    public String getFilename()
    {
        return _filename;
    }
    
    /* ------------------------------------------------------------ */
    public int getRetainDays()
    {
        return _retainDays;
    }

    /* ------------------------------------------------------------ */
    public void setRetainDays(int retainDays)
    {
        _retainDays = retainDays;
    }

    /* ------------------------------------------------------------ */
    /* 
     * @param filename 
     */
    private void openFile(String filename)
        throws IOException
    {
        synchronized(_stringBuffer)
        {
            try
            {
                File file = new File(filename);
                filename=file.getCanonicalPath();
                file=new File(filename);
                File dir= new File(file.getParent());
                if (!dir.exists() && dir.canWrite())
                    throw new IOException("Cannot write log directory "+dir);
                
                Date now=new Date();
                
                // Is this a rollover file?
                int i=file.getName().toLowerCase().indexOf(YYYY_MM_DD);
                if (i>=0)
                {
                    file=new File(dir,
                                  file.getName().substring(0,i)+
                                  _fileDateFormat.format(now)+
                                  file.getName().substring(i+YYYY_MM_DD.length()));
                    if (_rollover==null)
                        _rollover=new Rollover();
                }
                
                if (file.exists()&&!file.canWrite())
                    throw new IOException("Cannot write log file "+file);
                
                if (!_append && file.exists())
                    file.renameTo(new File(file.toString()+"."+__fileBackupFormat.format(now)));
                
                _out=new PrintWriter(new FileWriter(file.toString(),_append));
                
                if (_rollover!=null && !_rollover.isAlive())
                    _rollover.start();
            
            }
            catch(IOException e)
            {
                e.printStackTrace();
                _out=new PrintWriter(System.err);
                throw e;
            }
        }
    }
    
    /* ------------------------------------------------------------ */
    /** Log a message.
     * This method formats the log information as a string and calls
     * log(String).  It should only be specialized by a derived
     * implementation if the format of the logged messages is to be changed.
     *
     * @param tag Tag for type of log
     * @param msg The message
     * @param frame The frame that generated the message.
     * @param time The time stamp of the message.
     */
    public void log(String tag,
                    Object msg,
                    Frame frame,
                    long time)
    {
        // Lock buffer
        synchronized(_stringBuffer)
        {
            _stringBuffer.setLength(0);
            
            // Log the time stamp
            if (_logTimeStamps)
                _stringBuffer.append(_dateFormat.format(time));
        
            // Log the tag
            if (_logTags)
                _stringBuffer.append(tag);

            // Log the label
            if (_logLabels && frame != null)
            {
                _stringBuffer.append(frame.toString());
            }

            // Log the stack depth.
            if (_logStackSize && frame != null)
            {
                _stringBuffer.append(((frame._depth>9)?"":"0")+frame._depth+"> ");
            }
            
            // Determine the indent string for the message and append it
            // to the buffer. Only put a newline in the buffer if the first
            // line is not blank
            String nl=__lineSeparator+(_logOneLine?"":"+ ");
            
            if (_logLabels && !_logOneLine && _stringBuffer.length() > 0)
            	_stringBuffer.append(nl);

            // Log indented message
            String smsg=(msg==null)
                ?"???"
                :((msg instanceof String)?((String)msg):msg.toString());

            if (_logOneLine)
            {
                smsg=StringUtil.replace(smsg,"\015\012","<|");
                smsg=StringUtil.replace(smsg,"\015","<");
                smsg=StringUtil.replace(smsg,"\012","|");
            }
            else
            {
                smsg=StringUtil.replace(smsg,"\015\012","<|");
                smsg=StringUtil.replace(smsg,"\015","<|");
                smsg=StringUtil.replace(smsg,"\012","<|");
                smsg=StringUtil.replace(smsg,"<|",nl);
            }
            _stringBuffer.append(smsg);

            // Add stack frame to message
            if (_logStackTrace && frame != null)
            {
                _stringBuffer.append(nl);
                _stringBuffer.append(frame._stack);
            }
            
            log(_stringBuffer.toString());
        }
    }
    
    /* ------------------------------------------------------------ */
    /** Log a message.
     * The formatted log string is written to the log sink. The default
     * implementation writes the message to a PrintWriter.
     * @param formattedLog 
     */
    public void log(String formattedLog)
    {
        if (_out==null)
            return;
        synchronized(_stringBuffer)
        {
            _out.println(formattedLog);
            _out.flush();
        }
    }

    
    /* ------------------------------------------------------------ */
    /** Stop a log sink.
     * The default implementation does nothing 
     */
    public void start()
    {
        synchronized(_stringBuffer)
        {
            if (_filename!=null)
            {
                try{openFile(_filename);}
                catch(IOException e){e.printStackTrace();}   
            }
            _started=_out!=null;
        }
    }
    
    
    /* ------------------------------------------------------------ */
    /** Stop a log sink.
     * An opportunity for subclasses to clean up. The default
     * implementation does nothing 
     */
    public void stop()
    {
        _started=false;
        if (_filename!=null)
            _out.close();
        if (_rollover!=null)
            _rollover.interrupt();
        _rollover=null;
    }

    /* ------------------------------------------------------------ */
    public boolean isStarted()
    {
        return _started;
    }

    /* ------------------------------------------------------------ */
    public void destroy()
    {
        if (_filename!=null)
            _out.close();
        _out=null;
    }
    
    /* ------------------------------------------------------------ */
    public boolean isDestroyed()
    {
        return !_started && _out==null;
    }

    
    /* ------------------------------------------------------------ */
    private class Rollover extends Thread
    {
        Rollover()
        {
            setName("Rollover: "+WriterLogSink.this.hashCode());
        }
        
        public void run()
        {
            while(true)
            {
                try
                {
                    // Cleanup old files:
                    if (_retainDays>0)
                    {
                        Calendar retainDate = Calendar.getInstance();
                        retainDate.add(Calendar.DATE,-_retainDays);
                        int borderYear = retainDate.get(java.util.Calendar.YEAR);
                        int borderMonth = retainDate.get(java.util.Calendar.MONTH) + 1;
                        int borderDay = retainDate.get(java.util.Calendar.DAY_OF_MONTH);

                        File file= new File(_filename);
                        File dir = new File(file.getParent());
                        String fn=file.getName();
                        int s=fn.toLowerCase().indexOf(YYYY_MM_DD);
                        String prefix=fn.substring(0,s);
                        String suffix=fn.substring(s+YYYY_MM_DD.length());

                        String[] logList=dir.list();
                        for (int i=0;i<logList.length;i++)
                        {
                            fn = logList[i];
                            if(fn.startsWith(prefix)&&fn.indexOf(suffix,prefix.length())>=0)
                            {        
                                try
                                {
                                    StringTokenizer st = new StringTokenizer
                                        (fn.substring(prefix.length()),
                                         "_.");
                                    int nYear = Integer.parseInt(st.nextToken());
                                    int nMonth = Integer.parseInt(st.nextToken());
                                    int nDay = Integer.parseInt(st.nextToken());
                                    
                                    if (nYear<borderYear ||
                                        (nYear==borderYear && nMonth<borderMonth) ||
                                        (nYear==borderYear &&
                                         nMonth==borderMonth &&
                                         nDay<=borderDay))
                                    {
                                        Log.event("Log age "+fn);
                                        new File(dir,fn).delete();
                                    }
                                }
                                catch(Exception e)
                                {
                                    if (Code.debug())
                                        e.printStackTrace();
                                }
                            }
                        }
                    }

                    // Sleep until midnight
                    Calendar now = Calendar.getInstance();
                    GregorianCalendar midnight =
                        new GregorianCalendar(now.get(Calendar.YEAR),
                                              now.get(Calendar.MONTH),
                                              now.get(Calendar.DAY_OF_MONTH),
                                              23,0);
                    midnight.add(Calendar.HOUR,1);
                    long sleeptime=
                        midnight.getTime().getTime()-
                        now.getTime().getTime();
                    Code.debug("Log rollover sleep until "+midnight.getTime());
                    Thread.sleep(sleeptime);

                    // Update the filename
                    openFile(_filename);
                    Log.event("Rolled over "+_filename);
                }
                catch(InterruptedIOException e){break;}
                catch(InterruptedException e){break;}
                catch(IOException e)
                {
                    e.printStackTrace();
                }
            }
            Code.debug("Log rollover exiting");
        }
    }
};




