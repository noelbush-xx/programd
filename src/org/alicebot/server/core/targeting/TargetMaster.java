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

package org.alicebot.server.core.targeting;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Random;
import java.util.StringTokenizer;

import org.alicebot.server.core.Globals;
import org.alicebot.server.core.Graphmaster;
import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.logging.XMLLog;
import org.alicebot.server.core.util.InputNormalizer;
import org.alicebot.server.core.util.Toolkit;
import org.alicebot.server.core.util.Trace;
import org.alicebot.server.core.util.XMLResourceSpec;
import org.alicebot.server.core.util.XMLWriter;


/**
 *  Manages the creation of new targets.
 *
 *  @author Richard Wallace
 *  @author Jon Baer
 *  @author Thomas Ringate, Pedro Colla
 *  @author Noel Bush
 */
public class TargetMaster extends Targeting
{
    /** A random number generator. */
    private static final Random RandomNumberGenerator = new Random();

    /** The path where we expect to write targeting data. */
    private static String TARGETS_DATA_PATH;

    /** The resource spec to use with XMLWriter when writing targeting data. */
    private static XMLResourceSpec TARGETS_DATA_RESOURCE;

    /** The File from which we will read or write targeting data. */
    private static File targetsData;

    
    /**
     *  Loads in the contents of the target data file.
     */
    static
    {
        if (Globals.useTargeting())
        {
            // Set up the targets data fields.
            TARGETS_DATA_PATH = Globals.getTargetsDataPath();
            TARGETS_DATA_RESOURCE = new XMLResourceSpec();
            TARGETS_DATA_RESOURCE.description = "Targeting Data";
            TARGETS_DATA_RESOURCE.path        = TARGETS_DATA_PATH;
            TARGETS_DATA_RESOURCE.root        = TARGETS;
            TARGETS_DATA_RESOURCE.dtd         = XMLResourceSpec.HTML_ENTITIES_DTD;
            TARGETS_DATA_RESOURCE.encoding    = Globals.getProperty("programd.targeting.data.encoding", "UTF-8");

            // Load existing targets data.
            targetsData = new File(TARGETS_DATA_PATH);
        }
    }


    /**
     *  Adds a new target (no extension content) to the targets data file.
     *
     *  @param matchPattern     the <code>pattern</code> part of the matched path
     *  @param matchThat        the <code>that</code> part of the matched path
     *  @param matchTopic       the <code>topic</code> part of the matched path
     *  @param matchTemplate    the <code>template</code> associated with the matched path
     *  @param inputText        the input text that was matched
     *  @param inputThat        the value of the <code>that</code> predicate when the input was received
     *  @param inputTopic       the value of the <code>topic</code> predicate when the input was received
     *  @param response         the response to the input
     */
    public static void add(String matchPattern, String matchThat, String matchTopic, String matchTemplate,
                           String inputText, String inputThat, String inputTopic, String response)
    {
        // Create the new target.
        Target target = new Target(matchPattern, matchThat, matchTopic, matchTemplate,
                                   inputText, inputThat, inputTopic, response);

        // Write the target to the targets data file.
        TargetWriter.write(target, TARGETS_DATA_RESOURCE);
    }


    /**
     *  Deletes all saved targeting data.
     *  Rolls over the old file for safekeeping.
     */
    public static void rollTargetData()
    {
        if (Globals.useTargeting())
        {
            Log.userinfo("Rolling over targeting data.", Log.TARGETING);
            XMLWriter.rollover(TARGETS_DATA_RESOURCE);
            targetsData = new File(TARGETS_DATA_PATH);
            Log.userinfo("Targeting data deleted (old file rolled over).", Log.TARGETING);
        }
        else
        {
            Trace.userinfo("Targeting is switched off. Turn it on in server.properties.");
        }
    }
}