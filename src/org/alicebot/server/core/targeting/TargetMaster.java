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
     *  Loads Globals if it isn't already,
     *  so this can run standalone.  Also loads in
     *  the contents of the target data file.
     */
    static
    {
        if (!Globals.isLoaded())
        {
            Properties serverProperties = new Properties();
            try
            {
                serverProperties.load(new FileInputStream("server.properties"));
            }
            catch (IOException e)
            {
                // Error loading properties
                Log.userinfo("Could not find server.properties!", new String[] {Log.ERROR, Log.STARTUP});
            }
            Globals.load(serverProperties);
        }
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
     */
    public static void add(String matchPattern, String matchThat, String matchTopic, String matchTemplate,
                           String inputText, String inputThat, String inputTopic)
    {
        String extensionPattern;
        String extensionThat;
        String extensionTopic;

        // Create a new target by a successive extend process.
        try
        {
            // Try to extend the match-pattern using the input-text.
            extensionPattern = InputNormalizer.patternFit(extend(matchPattern, inputText));

            /*
                If successful (no exception),
                set target -that and -topic to match -that and -topic.
            */
            extensionThat = InputNormalizer.patternFit(matchThat);
            extensionTopic = InputNormalizer.patternFit(matchTopic);
        }
        catch (CannotExtendException e0)
        {
            // Couldn't extend the match-pattern, so set target-pattern to match-pattern.
            extensionPattern = InputNormalizer.patternFit(matchPattern);
            try
            {
                // Try to extend the match-that using the input-that.
                extensionThat = InputNormalizer.patternFit(extend(matchThat, inputThat));

                /*
                    If successful (no exception),
                    set target-topic to match-topic.
                */
                extensionTopic = InputNormalizer.patternFit(matchTopic);
            }
            catch (CannotExtendException e1)
            {
                // Couldn't extend the match-that, so set target-that to match-that.
                extensionThat = InputNormalizer.patternFit(matchThat);
                try
                {
                    // Try to extend the match-topic using the input-topic.
                    extensionTopic = InputNormalizer.patternFit(extend(matchTopic, inputTopic));
                }
                catch (CannotExtendException e2)
                {
                    // Couldn't even extend topic, so return, doing nothing.
                    return;
                }
            }

            // Create the new target.
            Target target = new Target(matchPattern, matchThat, matchTopic, matchTemplate,
                                inputText, inputThat, inputTopic,
                                extensionPattern, extensionThat, extensionTopic);

            // Write the target to the targets data file.
            TargetWriter.write(target, TARGETS_DATA_RESOURCE);
        }
    }


    /**
     *  Creates a new target pattern, by extending a pattern using an input.
     *
     *  @param pattern  the pattern part of the target
     *  @param input    the input part of the target
     *
     *  @return a new target
     *
     *  @throws CannotExtendException if the pattern-token length
     *                                is greater than or equal to the input-token length
     */
    public static String extend(String pattern, String input) throws CannotExtendException
    {
        // If the pattern does not contain wildcards, it cannot be extended.
        if ((pattern.indexOf('*') == -1) && (pattern.indexOf('_') == -1))
        {
            throw new CannotExtendException();
        }

        // Tokenize the pattern and input.
        StringTokenizer patternTokenizer = new StringTokenizer(pattern);
        StringTokenizer inputTokenizer = new StringTokenizer(input);

        // Count the pattern and input tokens.
        int patternTokenCount = patternTokenizer.countTokens();
        int inputTokenCount = inputTokenizer.countTokens();

        // Result will be constructed in this buffer.
        StringBuffer result = new StringBuffer();

        boolean hitWildcard = false;

        // Until hitting a wildcard in the pattern, append words from the input.
        for (int index = 0; (index < patternTokenCount && !hitWildcard); index++)
        {
            String patternToken = patternTokenizer.nextToken();
            if (patternToken.equals(Graphmaster.ASTERISK) || patternToken.equals(Graphmaster.UNDERSCORE))
            {
                hitWildcard = true;
            }
            result.append(inputTokenizer.nextToken());
            result.append(SPACE);
        }

        // Append a * wildcard if the end of the input was not reached.
        if (inputTokenizer.hasMoreTokens())
        {
            result.append(Graphmaster.ASTERISK);
        }

        // Return the result.
        return result.toString();
    }


    /**
     *  Deletes all saved targeting data.
     *  Rolls over the old file for safekeeping.
     */
    public static void rollTargetData()
    {
        Log.userinfo("Rolling over targeting data.", Log.TARGETING);
        XMLWriter.rollover(TARGETS_DATA_RESOURCE);
        targetsData = new File(TARGETS_DATA_PATH);
        Log.userinfo("Targeting data deleted (old file rolled over).", Log.TARGETING);
    }
}

/**
 *  An exception thrown by {@link #extend}.
 */
class CannotExtendException extends Exception
{
    public CannotExtendException()
    {
    }
}
