// ========================================================================
// Copyright (c) 1997 MortBay Consulting, Sydney
// $Id: LogSink.java,v 1.1.1.1 2001/06/17 19:01:40 noelbu Exp $
// ========================================================================

package org.alicebot.server.net.http;



/* ------------------------------------------------------------ */
/** A Log sink.
 * This class represents both a concrete or abstract sink of
 * Log data.  The default implementation logs to a PrintWriter, but
 * derived implementations may log to files, syslog, or other
 * logging APIs.
 *
 * 
 * @version $Id: LogSink.java,v 1.1.1.1 2001/06/17 19:01:40 noelbu Exp $
 * @author Greg Wilkins (gregw)
 */
public interface LogSink extends LifeCycle
{
    /*-------------------------------------------------------------------*/
    /** Set the log options.
     *
     * @param logOptions A string of characters as defined for the
     * LOG_OPTIONS system parameter.
     */
    public void setOptions(String options);
    
    /* ------------------------------------------------------------ */
    public String getOptions();
    
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
                    long time);
    
    /* ------------------------------------------------------------ */
    /** Log a message.
     * The formatted log string is written to the log sink. The default
     * implementation writes the message to a PrintWriter.
     * @param formattedLog 
     */
    public void log(String formattedLog);

    
};








