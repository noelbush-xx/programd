// ===========================================================================
//	RolloverFileLogSink.java
// ===========================================================================

/*
** (c) Copyright V. Lipovetsky, 1998-2000
** www.fuib.com 
** E-mail: vit@fuib.com, lipov99@yahoo.com
** Modified for use with Jetty by Kent Johnson <KJohnson@transparent.com>
** Multiple log file modification by Jonathon Parker, jparker@transparent.com
*/

package org.alicebot.server.net.http;

/* ------------------------------------------------------------ */
/** Rollover File Log Sink.
 * This implementation of Log Sink writes logs to a file. Files
 * are rolled over every day and old files are deleted.
 *
 * The default constructor looks for these System properties:
 * ROLLOVER_LOG_DIR		The path to the directory containing the logs
 * ROLLOVER_LOG_RETAIN_DAYS	The number of days to retain logs
 * ROLLOVER_LOG_EXTENSION	The file extension for log files
 * ROLLOVER_LOG_STOP_TIMEOUT    How long to wait to kill the cleanup thread
 * ROLLOVER_LOG_TIMER_INTERVAL  How long the cleanup thread sleeps
 * ROLLOVER_LOG_MULT_DAY        If true, Jetty will keep multiple log files
 *                              for same day if server is halted and restored.
 * ROLLOVER_LOG_APPEND          If true and not multi-day, append to existing
 *                              log files.
 *
 * @deprecated Use WriterLogSink
 * @version $Id: RolloverFileLogSink.java,v 1.1.1.1 2001/06/17 19:01:29 noelbu Exp $
 * @author V. Lipovetsky
 * @author Kent Johnson
 */
public class RolloverFileLogSink 
        extends WriterLogSink implements Runnable
{
    private boolean created = false;    
    private java.io.PrintWriter logWriter;
    private java.io.File logFile;
    private Thread clearThread;
    private ThreadEvent threadEvent = new ThreadEvent();
    private java.text.SimpleDateFormat fileDateFormat = 
        new java.text.SimpleDateFormat("yyyy_MM_dd");
    private boolean multDay;
    private boolean append;

    private java.io.File logDir = new java.io.File("./");
    private String logExt = ".log";
    private long timerInterval = 20*1000;
    private long threadStopTimeout = 20*1000;
    private int retainDays = 1;


    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @deprecated Use WriterLogSink
     * @exception java.io.IOException 
     */
    public RolloverFileLogSink()
        throws java.io.IOException
    {
    	String logDir = System.getProperty("ROLLOVER_LOG_DIR");
    	if (logDir != null)
            setLogDir(logDir);
    		
    	Integer retain = Integer.getInteger("ROLLOVER_LOG_RETAIN_DAYS");
    	if (retain != null)
            setRetainDays(retain.intValue());
    		
    	String extension = System.getProperty("ROLLOVER_LOG_EXTENSION");
    	if (extension != null)
            setLogExt(extension);
    		
    	Integer stopTimeout = Integer.getInteger("ROLLOVER_LOG_STOP_TIMEOUT");
    	if (stopTimeout != null)
            setThreadStopTimeout(stopTimeout.intValue());
    		
    	Integer timerInterval = Integer.getInteger("ROLLOVER_LOG_TIMER_INTERVAL");
    	if (timerInterval != null)
            setTimerInterval(timerInterval.intValue());

        multDay = Boolean.getBoolean("ROLLOVER_LOG_MULT_DAY");
        append = Boolean.getBoolean("ROLLOVER_LOG_APPEND");
    }


    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param newLogDir 
     * @param newRetainDays 
     * @param newLogExt 
     * @param newThreadStopTimeout 
     * @param newTimerInterval 
     * @exception java.io.IOException 
     * @deprecated Use WriterLogSink
     */
    public RolloverFileLogSink(String newLogDir,
                               int newRetainDays , 
                               String newLogExt,
                               long newThreadStopTimeout,
                               long newTimerInterval)
        throws java.io.IOException
    {
    	setLogDir(newLogDir);
    	setRetainDays(newRetainDays);
    	setLogExt(newLogExt);
        setTimerInterval(newTimerInterval);
        setThreadStopTimeout(newThreadStopTimeout);
    }
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param newLogDir 
     * @param newRetainDays 
     * @param newLogExt 
     * @param newThreadStopTimeout 
     * @param newTimerInterval 
     * @param multiDay 
     * @exception java.io.IOException 
     * @deprecated Use WriterLogSink
     */
    public RolloverFileLogSink(String newLogDir)
        throws java.io.IOException
    {
    	setLogDir(newLogDir);
        setMultiDay(true);
    }
        
    /* ------------------------------------------------------------ */
    /* 
     * @exception java.io.IOException 
     */
    public void start()
    {
        Code.warning("RolloverLogSink is deprecated. Use WriterLogSink");
        try
        {
            // Set new log file to name of current date
            setLogNameToDate(new java.util.Date());
            clearOldLogFiles(new java.util.Date());
            startClearThread();
            
            setCreated(true);
            super.start();
        }
        catch(java.io.IOException e)
        {
            Code.warning(e);
        }
        
    }//start
    
    
    /* ------------------------------------------------------------ */
    public void stop()
    {
    	cleanup();
        super.stop();
    }

    /* ------------------------------------------------------------ */
    /* 
     * @param curDate 
     * @exception java.io.IOException 
     */
    private synchronized void setLogNameToDate(java.util.Date curDate)
    	throws java.io.IOException
    {
        java.io.File newLogFile = 
            new java.io.File(logDir, fileDateFormat.format(curDate) +
                             logExt);

        //** If new name eq old do nothing
        if (newLogFile.equals(logFile)) return;
            
        // Make sure we start fresh if multDay option not set
        if (!multDay && newLogFile.exists())
        {
            if (!append)
                newLogFile.delete();
        }
        else
        {
            // Make additional files appended with _n2, _n3, etc.
            int num = 1;
            while (newLogFile.exists())
            {
                num++;
                newLogFile =
                    new java.io.File(logDir, 
                                     fileDateFormat.format(curDate) +
                                     "_n" + num + logExt);
            }
        }
        logFile = newLogFile;

        //** Open new log file
        java.io.PrintWriter newLogWriter =
            new java.io.PrintWriter(new java.io.FileWriter(logFile.getPath(),
                                                           true), true);

        long now = System.currentTimeMillis();
        if (logWriter != null)
        {
            synchronized (logWriter)
            {
                                //** Close old log file
                logWriter.close();
                logWriter = newLogWriter;
                                        
                // This is what sets logWriter to get the log output
                super.setWriter(logWriter);
            }
        }
        else
        {
            logWriter = newLogWriter;
            super.setWriter(logWriter);
        }
                        
    }//setLogNameToDate


    /* ------------------------------------------------------------ */
    /* 
     * @param curDate 
     */
    private synchronized void clearOldLogFiles(java.util.Date curDate)
    {
        String[] logFileList =
            logDir.list(new java.io.FilenameFilter() {
                                public boolean accept(java.io.File dir,
                                                      String n)
                                {
                                    return n.indexOf(logExt) != -1;
                                }//accept
                            }//FilenameFilter
                        );

        //** Compute Border date
        java.util.Calendar calendar = java.util.Calendar.getInstance();
        calendar.setTime(curDate);
        calendar.add(java.util.Calendar.DAY_OF_MONTH, -retainDays);

        int borderYear = calendar.get(java.util.Calendar.YEAR);
        int borderMonth = calendar.get(java.util.Calendar.MONTH) + 1;
        int borderDay = calendar.get(java.util.Calendar.DAY_OF_MONTH);
                        
        for (int i = 0; i < logFileList.length; i++)
        {
            java.io.File logFile = new java.io.File(logDir, logFileList[i]);
            java.util.StringTokenizer st = 
                new java.util.StringTokenizer(logFile.getName(),
                                              "_.");
            try
            {
                int nYear = Integer.parseInt(st.nextToken());
                int nMonth = Integer.parseInt(st.nextToken());
                int nDay = Integer.parseInt(st.nextToken());
                
                if (nYear < borderYear ||
                    (nYear == borderYear && nMonth < borderMonth) ||
                    (nYear == borderYear && nMonth == borderMonth && nDay <= borderDay)) {
                    logFile.delete();
                }
            }
            catch(Exception e)
            {
                if (Code.debug() && Code.getVerbose()>0)
                    e.printStackTrace();
            }
        }
    }
    
    /* ------------------------------------------------------------ */
    /* 
     */
    private synchronized void startClearThread()
    {
        if (clearThread == null)
        {
            clearThread = new Thread(this);
            clearThread.setDaemon(true);
            clearThread.start();
        }
    }


    /* ------------------------------------------------------------ */
    /* 
     * @param timeout 
     */
    private synchronized void stopClearThread(long timeout)
    {
        if (clearThread != null)
        {
            //** Send signal about exit from program
            threadEvent.setOn(true);

            //** wait unitl thread is stopped
            try {
                clearThread.join(timeout);
            }
            catch (java.lang.InterruptedException ignored)
            { Code.ignore(ignored); }
            
            //** if timeout is out time let's interrupt thread
            if (clearThread.isAlive())
            {
        	clearThread.interrupt();
            }//if
            clearThread = null;
        }//if
    }


    /* ------------------------------------------------------------ */
    public void run()
    {
        try {
            while(true)
            {
                synchronized(threadEvent)
                {
                    threadEvent.wait(timerInterval);
                    if (threadEvent.isOn())
                    {
                        break;
                    }//if

                }//synchronized

                //** Get current datetime and store in member variable
                java.util.Date curTime = new java.util.Date();
        	java.util.Calendar calendar = java.util.Calendar.getInstance();
                calendar.setTime(curTime);

                //logWriter.printTimestamp();
                //logWriter.println("thread in run !");

                if (calendar.get(java.util.Calendar.HOUR_OF_DAY) == 0 
                    && calendar.get(java.util.Calendar.MINUTE) == 0) {
                    setLogNameToDate(curTime);
                    clearOldLogFiles(curTime);
                }//if
            }//while
        } catch(Exception e) {
                                //System.out.println(e);
            e.printStackTrace();
        }//try

    }//run


    /* ------------------------------------------------------------ */
    /** 
     */
    public synchronized void cleanup()
    {
        if (isCreated())
        {
            stopClearThread(threadStopTimeout);
            logWriter.close();
            setCreated(false);
        }//if
    }

    /* ------------------------------------------------------------ */
    /** 
     * @param newValue 
     * @exception java.io.IOException 
     */
    public void setLogDir(String newValue)
        throws java.io.IOException
    {
        logDir = new java.io.File(newValue);
        logDir.mkdirs();	// Make sure it exists
    }

    /* ------------------------------------------------------------ */
    /** 
     * @return 
     */
    public String getLogExt() {
        return logExt;
    }

    /* ------------------------------------------------------------ */
    /** 
     * @param newValue 
     */
    public void setLogExt(String newValue) {
        logExt = newValue;
    }
    
    /* ------------------------------------------------------------ */
    /** 
     * @return 
     */
    public int getRetainDays()
    {
        return retainDays;
    }

    /* ------------------------------------------------------------ */
    /** 
     * @param newValue 
     */
    public void setRetainDays(int newValue)
    {
        retainDays = newValue;
    }

    /* ------------------------------------------------------------ */
    /** 
     * @return 
     */
    public long getThreadStopTimeout()
    {
        return threadStopTimeout;
    }

    /* ------------------------------------------------------------ */
    /** 
     * @param newValue 
     */
    public void setThreadStopTimeout(long newValue)
    {
        threadStopTimeout = newValue==0?20000L:newValue;
    }
    

    /* ------------------------------------------------------------ */
    public long getTimerInterval()
    {
        return timerInterval;
    }

    /* ------------------------------------------------------------ */
    /** 
     * @param newValue TimerInterval or 0 for default
     */
    public void setTimerInterval(long newValue)
    {
        timerInterval = (newValue==0)?20000L:newValue;
    }

    /* ------------------------------------------------------------ */
    public boolean isMultiDay()
    {
        return multDay;
    }

    /* ------------------------------------------------------------ */
    public void setMultiDay(boolean md)
    {
        multDay=md;
    }
    
    /* ------------------------------------------------------------ */
    public boolean isAppend()
    {
        return append;
    }

    /* ------------------------------------------------------------ */
    public void setAppend(boolean a)
    {
        append=a;
    }
    
    /* ------------------------------------------------------------ */
    /** 
     * @return 
     */
    public boolean isCreated()
    {
  	return created;
    }

    /* ------------------------------------------------------------ */
    /* 
     * @param newValue 
     */
    private void setCreated(boolean newValue)
    {
  	created = newValue;
    }


    /* ------------------------------------------------------------ */
    /** A helper class that is used to signal the cleanup thread to stop. 
     */
    static final private class ThreadEvent 
    {
        private boolean on = false;

        public synchronized boolean isOn() {
            return on;
        }

        public synchronized void setOn(boolean newValue) {
            on = newValue;
            if (on) this.notifyAll();
        }
    }
}
