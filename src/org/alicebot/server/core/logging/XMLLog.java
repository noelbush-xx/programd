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

import java.util.HashMap;

import org.alicebot.server.core.Globals;
import org.alicebot.server.core.util.Trace;
import org.alicebot.server.core.util.XMLResourceSpec;
import org.alicebot.server.core.util.XMLWriter;


/**
 *  Provides a simplistic XML file writing facility.
 *
 *  @author Noel Bush
 *  @version 4.1.3
 */
public class XMLLog
{
    // Log name and element constants

    /** The string &quot;UTF-8&quot; (for character encoding conversion). */
    private static final String ENC_UTF8 = "UTF-8";

    /** Keeps a count of entries made in a log file. */
    private static HashMap entryCounts = new HashMap();
    
    /** Base URL for resources (stylesheets, etc.). */
    private static final String RESOURCE_BASE =
    	Globals.getProperty("programd.logging.xml.resource-base", "../resources/");

    /** Generic chat log spec. */
    private static final XMLResourceSpec GENERIC_CHAT   = new XMLResourceSpec();
    static
    {
        GENERIC_CHAT.description       = "Chat Log";        
        GENERIC_CHAT.rolloverAtMax     = true;
        GENERIC_CHAT.rolloverAtRestart =
        	Boolean.valueOf(Globals.getProperty("programd.logging.xml.chat.rollover-at-restart", "true")).booleanValue();
        GENERIC_CHAT.root              = "exchanges";
        GENERIC_CHAT.stylesheet = Globals.getProperty("programd.logging.xml.chat.stylesheet-path",
        											  RESOURCE_BASE + "logs/view-chat.xsl");
        GENERIC_CHAT.encoding   = Globals.getProperty("programd.logging.xml.chat.encoding",
        											  ENC_UTF8);
        GENERIC_CHAT.dtd        = XMLResourceSpec.HTML_ENTITIES_DTD;
    }

    /** Limits the number of responses written to a log file before it is rolled over. */
    private static int ROLLOVER;
    static
    {
        try
        {
            ROLLOVER = Integer.parseInt(Globals.getProperty("programd.logging.xml.rollover", "2000"));
        }
        catch (NumberFormatException e)
        {
            ROLLOVER = 2000;
        }
    }
    
    
    /**
     *  Returns a generic chat log spec that can be customized.
     *
     *  @return a generic chat log spec that can be customized
     */
    public static XMLResourceSpec getChatlogSpecClone()
    {
        return (XMLResourceSpec)GENERIC_CHAT.clone();
    }


    /**
     *  Writes a message to an XML log file.  If the number of entries
     *  has exceeded {@link #ROLLOVER}, the file will be renamed and a
     *  new file created.  Note that the approach currently used has an
     *  important defect: it only counts entries during the runtime of
     *  the bot; it will not count entries in an existing log file, so
     *  if the bot is restarted many times and {@link #ROLLOVER} is rather
     *  large, actual logfiles may exceed the limit significantly.
     *
     *  @param message  the text of the log event
     *  @param spec     the log spec
     */
    public static void log(String message, XMLResourceSpec spec)
    {
        int entryCount;

        // Find out if there is an entry count record in memory for this spec.
        Object atSpec = entryCounts.get(spec);

        // If there is, get its value.
        if (atSpec != null)
        {
            entryCount = ((Integer)atSpec).intValue();

            // If the spec requires rollover at max.
            if (spec.rolloverAtMax)
            {
                // And if it has hit max, roll it over.
                if (++entryCount % ROLLOVER == 0)
                {
                    XMLWriter.rollover(spec);
                }

                // Update the entry count (since the spec cares).
                entryCounts.put(spec, new Integer(entryCount));
            }
        }

        // If there is not an entry count, this means we have just restarted -- roll over if necessary.
        else if (spec.rolloverAtRestart)
        {
            XMLWriter.rollover(spec);

            // Create the entry count.
            entryCounts.put(spec, new Integer(1));
        }

        XMLWriter.write(message, spec);
    }
}

