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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;
import java.util.Random;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import org.alicebot.server.core.Globals;
import org.alicebot.server.core.Graphmaster;
import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.logging.Trace;
import org.alicebot.server.core.logging.XMLLog;
import org.alicebot.server.core.util.InputNormalizer;
import org.alicebot.server.core.util.Toolkit;


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
    /** A flag indicating live targets. */
    public static final int LIVE       = 0;

    /** A flag indicating saved targets. */
    public static final int SAVED      = 1;

    /** A flag indicating discarded targets. */
    public static final int DISCARDED  = 2;

    /** A random number generator. */
    private static final Random RandomNumberGenerator = new Random();

    /** The path where generated targets are stored as AIML. */
    private static String TARGETS_AIML_PATH;

    /** The path where we expect to find/write targeting data. */
    private static String TARGETS_DATA_PATH;

    /** The typespec to use with XMLLog when writing AIML categories. */
    private static String[] AIML_TYPESPEC;

    /** The File from which we will read or write targeting data. */
    private static File targetsData;

     /** The &quot;live&quot; targets. */
    private static HashMap liveTargets = new HashMap();
    
    /** Targets which have been saved. */
    private static HashMap savedTargets = new HashMap();
    
    /** Targets which have been discarded. */
    private static HashMap discardedTargets = new HashMap();

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
            TARGETS_AIML_PATH = Globals.getTargetsAIMLPath();
            TARGETS_DATA_PATH = Globals.getTargetsDataPath();
            targetsData = new File(TARGETS_DATA_PATH);
            AIML_TYPESPEC = new String[] {TARGETS_AIML_PATH, "aiml", null};
            load(TARGETS_DATA_PATH);
        }
    }


    /**
     *  Loads targets from a given data path into
     *  memory, with the default TargetsReaderListener
     *  flag ({@link TargetsReaderListener#LOAD_LIVE})
     *
     *  @param path the path from which to load
     */
    public static void load(String path)
    {
        load(path, LIVE);
    }
    

    /**
     *  Loads targets from a given data path into memory.
     *
     *  @param path the path from which to load
     *  @param set  specifies into which set to load the targets
     */
    public static void load(String path, int set)
    {
        // This BufferedReader will be passed to TargetsReader to read the file.
        BufferedReader buffReader = null;

        // Guess if this is a URL.
        if (path.indexOf("://") != -1)
        {
            // Try to create this as a URL.
            URL url = null;

            try
            {
                url = new URL(path);
            }
            catch (MalformedURLException e)
            {
                Log.userinfo("Malformed URL: \"" + path + "\"", new String[] {Log.ERROR, Log.TARGETING});
            }

            try
            {
                String encoding = Toolkit.getDeclaredXMLEncoding(url.openStream());
                buffReader = new BufferedReader(new InputStreamReader(url.openStream(), encoding));
            }
            catch (IOException e)
            {
                Log.userinfo("I/O error trying to read \"" + path + "\"", new String[] {Log.ERROR, Log.TARGETING});
            }
        }

        // Handle paths which are apparently files.
        else
        {
            File toRead = new File(path);

            if (toRead.exists() && !toRead.isDirectory())
            {
                try
                {
                    String encoding =
                        Toolkit.getDeclaredXMLEncoding(new FileInputStream(path));
                    buffReader = new BufferedReader(
                                    new InputStreamReader(
                                        new FileInputStream(path), encoding));
                }
                catch (IOException e)
                {
                    Log.userinfo("I/O error trying to read \"" + path + "\"", new String[] {Log.ERROR, Log.TARGETING});
                    return;
                }
            }
            else
            {
                // It is possible that no file exists (yet) -- no problem.
                return;
            }
        }
        new TargetsReader(path, buffReader, new TargetsReaderListener(set)).read();

        try
        {
            buffReader.close();
        }
        catch (IOException e)
        {
            Log.userinfo("I/O error trying to close \"" + path + "\"", new String[] {Log.ERROR, Log.TARGETING});
            return;
        }
    }


    /**
     *  Adds a target to the specified set.  If the set
     *  is {@link #LIVE}, then if the target is already in
     *  the live targets set, the new input part of the target
     *  is added to the target and its activations count is
     *  incremented.
     *
     *  @param target   the target to add
     *  @param set      the target set to which to add it
     */
    public static void add(Target target, int set)
    {
        switch (set)
        {
            case LIVE :
                Integer hashCode = new Integer(target.hashCode());
                if (!discardedOrSaved(hashCode))
                {
                    Target alreadyKnown = (Target)liveTargets.get(hashCode);
                    if (alreadyKnown != null)
                    {
                        alreadyKnown.addInputs(target);
                    }
                    else
                    {
                        liveTargets.put(new Integer(target.hashCode()), target);
                    }
                }
                break;

            case SAVED :
                save(target);
                break;

            case DISCARDED :
                discard(target);
                break;
        }
    }

    
    /**
     *  Adds a new target (no extension content) to the live targets set
     *  and to the target data file.  If the target has been discarded or
     *  saved, it is not added.  If the target is already known, the new inputs
     *  are added and the existing target's activation count is incremented.
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
        // Look up this target in the live targets.
        Integer hashCode = new Integer(Target.generateHashCode(matchPattern, matchThat, matchTopic));

        if (discardedOrSaved(hashCode))
        {
            return;
        }

        // If the target is already known, add its inputs.
        Target target = (Target)liveTargets.get(hashCode);
        if (target != null)
        {
            target.addInputs(inputText, inputThat, inputTopic);
        }
        else
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

                // Load this into memory as a new target.
                target = new Target(matchPattern, matchThat, matchTopic, matchTemplate,
                                    inputText, inputThat, inputTopic,
                                    extensionPattern, extensionThat, extensionTopic);
                liveTargets.put(hashCode, target);
            }

            // Write the target to the targets data file.
            TargetWriter.write(target, targetsData);
        }
    }


    /**
     *  Indicates whether the <code>TargetMaster</code>
     *  has previously discarded or saved the target
     *  specified by a given hash code.
     *
     *  @param hashCode hash code that uniquely identifies the target
     */
    public static boolean discardedOrSaved(Integer hashCode)
    {
        return (savedTargets.containsKey(hashCode) ||
                discardedTargets.containsKey(hashCode));
    }


    /**
     *  Indicates whether the <code>TargetMaster</code>
     *  knows the given target.
     *
     *  @param target   the target
     */
    public static boolean discardedOrSaved(Target target)
    {
        Integer hashCode = new Integer(target.hashCode());
        return discardedOrSaved(hashCode);
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
        // Tokenize the pattern and input.
        StringTokenizer patternTokenizer = new StringTokenizer(pattern);
        StringTokenizer inputTokenizer = new StringTokenizer(input);

        // Count the pattern and input tokens.
        int patternTokenCount = patternTokenizer.countTokens();
        int inputTokenCount = inputTokenizer.countTokens();

        // Throw an exception if the pattern-token length is greater than or equal to the input-token length.
        if (patternTokenCount >= inputTokenCount)
        {
            throw new CannotExtendException();
        }

        // Result will be constructed in this buffer.
        StringBuffer result = new StringBuffer();

        // Append a number of words from the input equal to the pattern token count.
        for (int index = 0; index < patternTokenCount; index++)
        {
            result.append(inputTokenizer.nextToken());
            result.append(SPACE);
        }

        // Append a * wildcard.
        result.append(Graphmaster.ASTERISK);

        // Return the result.
        return result.toString();
    }


    /**
     *  Saves a new category to the targets AIML file.
     *
     *  @param target   the target to save
     */
    public static void saveCategory(Target target)
    {
        boolean hasTopic;
        String topic = target.getExtensionTopic();

        if (topic == null)
        {
            hasTopic = false;
        }
        else if (topic.trim().length() == 0 || topic.equals(Graphmaster.ASTERISK))
        {
            hasTopic = false;
        }
        else
        {
            hasTopic = true;
        }

        if (!hasTopic)
        {
            XMLLog.log(     INDENT + CATEGORY_START + LINE_SEPARATOR +
                            INDENT + INDENT + PATTERN_START + target.getExtensionPattern() + PATTERN_END + LINE_SEPARATOR +
                            INDENT + INDENT + THAT_START + target.getExtensionThat() + THAT_END + LINE_SEPARATOR +
                            INDENT + INDENT + TEMPLATE_START + LINE_SEPARATOR +
                            INDENT + INDENT + INDENT + target.getExtensionTemplate() + LINE_SEPARATOR +
                            INDENT + INDENT + TEMPLATE_END + LINE_SEPARATOR +
                            INDENT + CATEGORY_END + LINE_SEPARATOR,
                        AIML_TYPESPEC);
        }
        else
        {
            XMLLog.log(     INDENT + TOPIC_NAME_BEGIN + topic + TOPIC_NAME_END + LINE_SEPARATOR +
                            INDENT + INDENT + CATEGORY_START + LINE_SEPARATOR +
                            INDENT + INDENT + INDENT + PATTERN_START + target.getExtensionPattern() + PATTERN_END + LINE_SEPARATOR +
                            INDENT + INDENT + INDENT + THAT_START + target.getExtensionThat() + THAT_END + LINE_SEPARATOR +
                            INDENT + INDENT + INDENT + TEMPLATE_START + LINE_SEPARATOR +
                            INDENT + INDENT + INDENT + INDENT + target.getExtensionTemplate() + LINE_SEPARATOR +
                            INDENT + INDENT + INDENT + TEMPLATE_END + LINE_SEPARATOR +
                            INDENT + INDENT + CATEGORY_END + LINE_SEPARATOR +
                            INDENT + TOPIC_END + LINE_SEPARATOR,
                        AIML_TYPESPEC);
        }

        // Move the target from liveTargets to savedTargets.
        save(target);
    }


    /**
     *  Rewrites the live targets to a given file.
     *
     *  @param file the file to write
     *  @param set  one of {{{@link #LIVE}, {@link #SAVED}, {@link #DISCARDED}}}
     *
     *  @return the set of live targets
     */
    public static void rewriteTargets(File file, int set)
    {
        switch (set)
        {
            case LIVE :
                TargetWriter.rewriteTargets(liveTargets, file);
                break;
            
            case SAVED :
                TargetWriter.rewriteTargets(savedTargets, file);
                break;
            
            case DISCARDED :
                TargetWriter.rewriteTargets(discardedTargets, file);
                break;
        }
    }


    /**
     *  Returns a new target from the live targets, randomly.
     *
     *  @return a new target from the live targets, randomly
     */
    public static Target nextTarget()
    {
        int targetsCount = liveTargets.size();
        if (targetsCount > 0)
        {
            Iterator targetsKeySetIterator = liveTargets.keySet().iterator();
            double randomDouble = RandomNumberGenerator.nextDouble();
            int randomInt = (int)((double) targetsCount * randomDouble);
            Integer key = null;
            for (int index = 0; index <= randomInt; index++)
            {
                key = (Integer)targetsKeySetIterator.next();
            }
            return (Target)liveTargets.get(key);
        }
        else
        {
            return null;
        }
    }


    /**
     *  Saves a target from the live targets
     *  (does not create a new category.
     *
     *  @param target   the target to discard
     */
    public static void save(Target target)
    {
        Integer hashCode = new Integer(target.hashCode());
        liveTargets.remove(hashCode);
        savedTargets.put(hashCode, target);
    }


    /**
     *  Discards all targets from the live targets.
     */
    public static void discardAll()
    {
        discardedTargets.putAll(liveTargets);
        liveTargets = new HashMap();
    }


    /**
     *  Discards a target from the live targets.
     *
     *  @param target   the target to discard
     */
    public static void discard(Target target)
    {
        Integer hashCode = new Integer(target.hashCode());
        liveTargets.remove(hashCode);
        discardedTargets.put(hashCode, target);
    }


    /**
     *  Returns the number of targets of a given set.
     *
     *  @param set  one of {{{@link #LIVE}, {@link #SAVED}, {@link #DISCARDED}}}
     */
    public static int count(int set)
    {
        switch (set)
        {
            case LIVE :
                return liveTargets.size();
            
            case SAVED :
                return savedTargets.size();
            
            case DISCARDED :
                return discardedTargets.size();

            default :
                return 0;
        }
    }


    /**
     *  Returns the live target set.
     *
     *  @return the live target set
     */
    public static SortedMap getSortedTargets()
    {
        if (liveTargets.size() == 0)
        {
            return null;
        }
        SortedMap sort = new TreeMap();
        Iterator targetsIterator = liveTargets.values().iterator();
        while (targetsIterator.hasNext())
        {
            Target target = (Target)targetsIterator.next();
            Integer activations = new Integer(target.getActivations());
            sort.put(activations, target);
        }
        return sort;
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
