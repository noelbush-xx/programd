// ========================================================================
// Copyright (c) 1997 MortBay Consulting, Sydney
// $Id: FileLogSink.java,v 1.1.1.1 2001/06/17 19:01:16 noelbu Exp $
// ========================================================================

package org.alicebot.server.net.http;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;


/* ------------------------------------------------------------ */
/** File Log Sink.
 * @deprecated Use WriterLogSink
 * @version $Id: FileLogSink.java,v 1.1.1.1 2001/06/17 19:01:16 noelbu Exp $
 * @author Greg Wilkins (gregw)
 */
public class FileLogSink extends WriterLogSink
{
    /*-------------------------------------------------------------------*/
    private String _fileName=null;
    
    /* ------------------------------------------------------------ */
    /** Constructor.
     * @deprecated Use WriterLogSink
     */
    public FileLogSink()
        throws IOException
    {
    	super(System.getProperty("LOG_FILE","log.txt"));
    }
    
    /* ------------------------------------------------------------ */
    /** Constructor.
     * @deprecated Use WriterLogSink
     */
    public FileLogSink(String filename)
        throws IOException
    {
        super(filename);
    }
    
}




