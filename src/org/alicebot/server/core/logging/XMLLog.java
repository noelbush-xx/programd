/*
    Alicebot Program D
    Copyright (C) 1995-2001, A.L.I.C.E. AI Foundation
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
    USA.
*/

package org.alicebot.server.core.logging;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.RandomAccessFile;
import java.io.IOException;

import org.alicebot.server.core.Globals;
import org.alicebot.server.core.util.Toolkit;

/**
 *  <p>
 *  Provides a simplistic XML log generation facility.
 *  </p>
 *  <p>
 *  The facility is simplistic because it does not use any
 *  XML libraries; instead, it just has a hard-coded root element
 *  for each document type, and each time it gets some log text,
 *  it looks at the end of a log file for the closing tag,
 *  deletes it, appends the log text, and then re-adds the closing
 *  tag.  Yuck! :-)
 *  </p>
 *
 *  @author Noel Bush
 *  @version 4.1.3
 */
public class XMLLog
{
    // Log name and element constants

    /** Chat log path, root element name, stylesheet path. */
    public static final String[] CHAT   = new String[]
                                          {Globals.getProperty("programd.logging.xml.chat.log-path", "./logs/chat.xml"),
                                           "exchanges",
                                           Globals.getProperty("programd.logging.xml.chat.stylesheet-path", "../resources/logs/view-chat.xsl")};

    /** An XML processing instruction header. */
    private static final String XML_PI = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";

    /** The start of an XML stylesheet processing instruction. */
    private static final String STYLESHEET_PI_START = "<?xml-stylesheet type=\"text/xsl\" href=\"";

    /** The end of a processing instruction. */
    private static final String PI_END = "\"?>";

    /** Entity declarations to be included in a log. */
    private static final String DTD = "<!DOCTYPE ALLOW_HTML_ENTITIES [" +
                                      " <!ENTITY % HTMLlat1 PUBLIC" +
                                      " \"-//W3C//ENTITIES Latin1//EN//HTML\"" +
                                      "   \"http://www.w3.org/TR/xhtml1/DTD/xhtml-lat1.ent\">" +
                                      " %HTMLlat1;" +
                                      " <!ENTITY % HTMLsymbol PUBLIC" +
                                      "   \"-//W3C//ENTITIES Symbols//EN//HTML\"" +
                                      "   \"http://www.w3.org/TR/xhtml1/DTD/xhtml-symbol.ent\">" +
                                      " %HTMLsymbol;" +
                                      " <!ENTITY % HTMLspecial PUBLIC" +
                                      "   \"-//W3C//ENTITIES Special//EN//HTML\"" +
                                      "   \"http://www.w3.org/TR/xhtml1/DTD/xhtml-special.ent\">" +
                                      " %HTMLspecial;" +
                                      " ]>";


    /** The system line separator string. */
    private static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

    /** The starting character of an element opening tag. */
    private static final String OPEN_MARKER_START = "<";

    /** The starting character of an element closing tag. */
    private static final String CLOSE_MARKER_START = "</";

    /** The ending character of a non-atomic element opening tag. */
    private static final String NONATOMIC_MARKER_END = ">";

    /** The string &quot;UTF-8&quot; (for character encoding conversion). */
    private static final String ENC_UTF8 = "UTF-8";

    /** The string &quot;rw&quot; (for indicating read-write access on the file). */
    private static final String RW = "rw";

    /** The phrase &quot;xml file&quot;. */
    private static final String XMLFILE = "xml file";


    /**
     *  Writes a message to the appropriate logfile.
     *
     *  @param message  the text of the log event
     *  @param type     the type of the log event
     */
    public static synchronized void log(String message, String[] type)
    {
        RandomAccessFile logFile = null;

        // Create the log file if it does not exist.
        Toolkit.checkOrCreate(type[0], XMLFILE);
        try
        {
            logFile = new RandomAccessFile(type[0], RW);
        }
        catch (FileNotFoundException e)
        {
            Log.userinfo("Can't write to file \"" + type[0] + "\".", Log.ERROR);
            return;
        }

        // Get the file length.
        long fileLength = 0;
        try
        {
            fileLength = logFile.length();
        }
        catch (IOException e)
        {
            Log.userinfo("Error reading file \"" + type[0] + "\".", Log.ERROR);
        }

        // Create the root close marker string.
        String rootCloseMarker = CLOSE_MARKER_START + type[1] + NONATOMIC_MARKER_END + LINE_SEPARATOR;

        /*
            If the file has just been created, write the starting
            XML processing instructions, entity declarations,
            and root element opening and closing tags.
        */
        if (fileLength == 0)
        {
            try
            {
                // Write the XML pi.
                logFile.writeBytes(XML_PI + LINE_SEPARATOR);

                // Write the stylesheet pi if a stylesheet is defined.
                if (type[2] != null)
                {
                    logFile.writeBytes(STYLESHEET_PI_START + type[2] + PI_END + LINE_SEPARATOR);
                }

                // Write the DTD and the marker start.
                logFile.writeBytes(DTD + LINE_SEPARATOR +
                                   OPEN_MARKER_START + type[1] + NONATOMIC_MARKER_END + LINE_SEPARATOR);
            }
            catch (IOException e)
            {
                Log.userinfo("Error writing to \"" + type[0] + "\".", Log.ERROR);
                return;
            }
        }
        // Otherwise, try to find the root element closing tag at the end of the file.
        else
        {
            long closeMarkerStart = fileLength - (rootCloseMarker.length());
            try
            {
                logFile.seek(closeMarkerStart);
            }
            catch (IOException e)
            {
                Log.userinfo("Error reading \"" + type[0] + "\".", Log.ERROR);
                return;
            }
        }

        /*
            Now write the message and the root element closing tag
            to the file, and close the file.
        */
        try
        {
            logFile.write(message.getBytes(ENC_UTF8));
            logFile.writeBytes(rootCloseMarker);
            logFile.close();
        }
        catch (IOException e)
        {
            Log.userinfo("Error writing to \"" + type[0] + "\".", Log.ERROR);
            return;
        }
    }
}

