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

package org.alicebot.server.core.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import org.alicebot.server.core.logging.Log;


/**
 *  <p>
 *  Provides a simplistic XML file writing facility.
 *  </p>
 *  <p>
 *  The facility is simplistic because it does not use any
 *  XML libraries; instead, it just has a hard-coded root element
 *  for each document type, and each time it gets some text,
 *  it looks at the end of a file for the closing tag,
 *  deletes it, appends the text, and then re-adds the closing
 *  tag.  Yuck! :-)
 *  </p>
 *
 *  @author Noel Bush
 *  @version 4.1.3
 */
public class XMLWriter
{
    // Log name and element constants

    /** The string &quot;UTF-8&quot; (for character encoding conversion). */
    private static final String ENC_UTF8 = "UTF-8";

    /** An XML processing instruction header. */
    private static final String XML_PI_START = "<?xml version=\"1.0\" encoding=\"";

    /** The start of an XML stylesheet processing instruction. */
    private static final String STYLESHEET_PI_START = "<?xml-stylesheet type=\"text/xsl\" href=\"";

    /** The end of a processing instruction. */
    private static final String PI_END = "\"?>";

    /** The system line separator string. */
    private static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

    /** The starting character of an element opening tag. */
    private static final String OPEN_MARKER_START = "<";

    /** The starting character of an element closing tag. */
    private static final String CLOSE_MARKER_START = "</";

    /** The ending character of a non-atomic element opening tag. */
    private static final String NONATOMIC_MARKER_END = ">";

    /** The string &quot;rw&quot; (for indicating read-write access on the file). */
    private static final String RW = "rw";

    /** The phrase &quot;xml file&quot;. */
    private static final String XMLFILE = "xml file";

    /** A &quot;backlink&quot; attribute start. */
    private static final String BACKLINK_EQUALS = " backlink=\"";

    /** An &quot;starttime&quot; attribute start. */
    private static final String STARTTIME_EQUALS = " starttime=\"";

    /** A quotation mark. */
    private static final String QUOTE_MARK = "\"";

    /** The date/time format used in generating rollover files. */
    private static final String LINK_DATE_FORMAT = "yyyy-MM-dd-HHmmss";

    /** The date/time format used in generating starttime attributes. */
    private static final String STARTTIME_DATE_FORMAT = "yyyy-MM-dd H:mm:ss";


    /**
     *  Writes a message to an XML file.
     *
     *  @param message  the text of the log event
     *  @param spec     provides information about the XML resource to which to write
     */
    public static void write(String message, XMLResourceSpec spec)
    {
        RandomAccessFile file = null;

        // Create the file if it does not exist.
        Toolkit.checkOrCreate(spec.path, XMLFILE);
        try
        {
            file = new RandomAccessFile(spec.path, RW);
        }
        catch (FileNotFoundException e)
        {
            Log.userinfo("Can't write to file \"" + spec.path + "\".", Log.ERROR);
            return;
        }

        // Get the file length.
        long fileLength = 0;
        try
        {
            fileLength = file.length();
        }
        catch (IOException e)
        {
            Log.userinfo("Error reading file \"" + spec.path + "\".", Log.ERROR);
        }

        // Create the root close marker string.
        String rootCloseMarker = CLOSE_MARKER_START + spec.root + NONATOMIC_MARKER_END + LINE_SEPARATOR;

        /*
            If the file has just been created, write the starting
            XML processing instructions, entity declarations,
            and root element opening and closing tags.
        */
        if (fileLength == 0)
        {
            try
            {
                // Write the XML pi, with specified or default (UTF-8) encoding.
                file.writeBytes(XML_PI_START);
                if (spec.encoding != null)
                {
                    file.writeBytes(spec.encoding);
                }
                else
                {
                    file.writeBytes(ENC_UTF8);
                }
                file.writeBytes(PI_END + LINE_SEPARATOR);

                // Write the stylesheet pi if a stylesheet is defined.
                if (spec.stylesheet != null)
                {
                    file.writeBytes(STYLESHEET_PI_START + spec.stylesheet + PI_END + LINE_SEPARATOR);
                }

                // Write the DTD, if defined.
                if (spec.dtd != null)
                {
                    file.writeBytes(spec.dtd + LINE_SEPARATOR);
                }

                // Start the opening root element.
                file.writeBytes(OPEN_MARKER_START + spec.root);
                
                // Insert a backlink attribute if available.
                if (spec.backlink != null)
                {
                    file.writeBytes(BACKLINK_EQUALS + spec.backlink + QUOTE_MARK);
                }
                // Insert a starttime attribute if available.
                if (spec.starttime != null)
                {
                    file.writeBytes(STARTTIME_EQUALS + spec.starttime + QUOTE_MARK);
                }
                file.writeBytes(NONATOMIC_MARKER_END + LINE_SEPARATOR);
            }
            catch (IOException e)
            {
                Log.userinfo("Error writing to \"" + spec.path + "\".", Log.ERROR);
                return;
            }
        }
        // Otherwise, try to find the root element closing tag at the end of the file.
        else
        {
            long closeMarkerStart = fileLength - (rootCloseMarker.length());
            try
            {
                file.seek(closeMarkerStart);
            }
            catch (IOException e)
            {
                Log.userinfo("Error reading \"" + spec.path + "\".", Log.ERROR);
                return;
            }
        }

        /*
            Now write the message and the root element closing tag
            to the file, and close the file.
        */
        try
        {
            if (spec.encoding == null)
            {
                file.write(message.getBytes(ENC_UTF8));
            }
            else
            {
                file.write(message.getBytes(spec.encoding));
            }
            file.writeBytes(rootCloseMarker);
            file.close();
        }
        catch (IOException e)
        {
            Log.userinfo("Error writing to \"" + spec.encoding + "\".", Log.ERROR);
            return;
        }
    }


    /**
     *  Rolls over a spec (renames current file designated
     *  by a spec, and creates a new one with starttime
     *  and backlink set to the previous one).
     *
     *  @param spec describes the resource
     */
    public static void rollover(XMLResourceSpec spec)
    {
        if (spec == null)
        {
            Trace.userinfo("No resource found to roll over.");
            return;
        }
        if (spec.description != null)
        {
            Trace.devinfo("Rolling over " + spec.description + ".");
        }
        Date starts = new Date();
        File specPath = new File(spec.path);
        String parentPart = specPath.getParent();
        String namePart = specPath.getName();
        int dot = namePart.lastIndexOf('.');
        String dateTime = new SimpleDateFormat(LINK_DATE_FORMAT).format(starts);
        if (dot > 0)
        {
            spec.backlink = namePart.substring(0, dot) +
                            "-end-" + dateTime +
                            namePart.substring(dot);
        }
        else
        {
            spec.backlink = namePart + "-end-" + dateTime;
        }
        spec.starttime = new SimpleDateFormat(STARTTIME_DATE_FORMAT).format(starts);
        File rollover = new File(parentPart + File.separator + spec.backlink);
        new File(spec.path).renameTo(rollover);
    }
}

