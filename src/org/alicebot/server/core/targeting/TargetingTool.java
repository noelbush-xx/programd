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
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

import org.alicebot.server.core.Globals;
import org.alicebot.server.core.Graphmaster;
import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.targeting.gui.TargetingGUI;
import org.alicebot.server.core.util.UserError;
import org.alicebot.server.core.util.Toolkit;
import org.alicebot.server.core.util.Trace;
import org.alicebot.server.core.util.XMLResourceSpec;
import org.alicebot.server.core.util.XMLWriter;


/**
 *  Manages the use of the Targeting GUI.
 *
 *  @author Noel Bush
 */
public class TargetingTool extends Targeting implements Runnable
{
    /** Version string. */
    public static final String VERSION = Graphmaster.VERSION;

    /** The default path for the targets data file. */
    private static final String DEFAULT_TARGETS_DATA_PATH = "./targets/targets.xml";

    /** The actual path for the targets data file. */
    private static String targetsDataPath;

    /** The path for the live targets cache. */
    private static final String LIVE_CACHE_PATH = "./targets/live.cache";

    /** The file for the live targets cache. */
    private static File liveCache = new File(LIVE_CACHE_PATH);

    /** The path for the discarded targets cache. */
    private static final String DISCARDED_CACHE_PATH = "./targets/discarded.cache";

    /** The file for the discarded targets cache. */
    private static File discardedCache = new File(DISCARDED_CACHE_PATH);

    /** The path for the saved targets cache. */
    private static final String SAVED_CACHE_PATH = "./targets/saved.cache";

    /** The file for the saved targets cache. */
    private static File savedCache = new File(SAVED_CACHE_PATH);

    /** The Timer that handles watching the targeting data file. */
    private static Timer timer;

    /** The timer frequency. */
    private static int timerFrequency = 0;

    /** The instance of TargetingGUI managed by this class. */
    private static TargetingGUI gui;

    /** The resource spec to use with XMLWriter when writing AIML categories. */
    private static XMLResourceSpec AIML_RESOURCE;

    /** The &quot;live&quot; targets. */
    private static HashMap liveTargets = new HashMap();
    
    /** Targets which have been saved. */
    private static HashMap savedTargets = new HashMap();
    
    /** Targets which have been discarded. */
    private static HashMap discardedTargets = new HashMap();

    /** The index of the next live target to serve (via {@link #nextTarget}). */
    private static int nextTargetToServe = 0;

    /** The properties object. */
    private static Properties properties;

    /** The properties path. */
    private static String propertiesPath;


    // Configuration flags.

    /** Whether to include targets with complete patterns but incomplete match <code>that</code> patterns. */
    private static boolean includeIncompleteThats;
    
    /** Whether to include targets with complete patterns but incomplete match <code>topic</code> patterns. */
    private static boolean includeIncompleteTopics;
    

    /**
     *  Creates a new instance of TargetingTool,
     *  loading targets from targets data path.
     */
    public TargetingTool()
    {
        targetsDataPath = Globals.getProperty("programd.targeting.data.path", "./targets/targets.xml");

        Trace.userinfo("Launching Targeting Tool with data path \"" + targetsDataPath + "\".");

        Trace.devinfo("Checking target cache files.");

        Toolkit.checkOrCreate(LIVE_CACHE_PATH, "targeting cache file");
        Toolkit.checkOrCreate(DISCARDED_CACHE_PATH, "targeting cache file");
        Toolkit.checkOrCreate(SAVED_CACHE_PATH, "targeting cache file");

        // Start the GUI.
        gui = new TargetingGUI(this);
        gui.start();

        try
        {
            // Load in the live targets from the cache.
            gui.setStatus("Loading live targets cache....");
            load(LIVE_CACHE_PATH);

            // Now load the saved and discarded targets.
            gui.setStatus("Loading saved targets cache....");
            load(SAVED_CACHE_PATH, savedTargets);

            gui.setStatus("Loading discarded targets cache....");
            load(DISCARDED_CACHE_PATH, discardedTargets);
        }
        catch (Exception e)
        {
            Log.userfail(e.getMessage(), new String[] {Log.ERROR, Log.TARGETING});
            System.exit(1);
        }

        // Get the timer frequency.
        try
        {
            timerFrequency = Integer.parseInt(Globals.getProperty("programd.targeting.tool.reload-timer", "0")) * 1000;
        }
        catch (NumberFormatException e)
        {
            // Leave it at 0.
        }

        /*
            Call "reload" to load in targets
            from the actual target data file, and
            subsequently rewrite the liveCache.  This
            will cause the liveCache to contain only
            unprocessed targets.
        */
        try
        {
            reload(targetsDataPath);
        }
        catch (IOException e)
        {
            Log.userfail(e.getMessage(), new String[] {Log.ERROR, Log.TARGETING});
        }

        // Open the first target (if any).
        gui.targetPanel.nextTarget();

        gui.setStatus("Ready.");
    }


    /**
     *  Reloads targets from the targets data path into the live targets cache.
     */
    public void reload() throws IOException, MalformedURLException
    {
        reload(targetsDataPath);
    }


    /**
     *  Loads targets from a given path and merges them with
     *  the contents of a local working file, then rewrites
     *  the working files.
     *
     *  @param path the target data file to read
     *
     *  @throws IOException if the path cannot be found
     *  @throws MalformedURLException if a URL is malformed
     */
    private void reload(String path) throws IOException, MalformedURLException
    {
        // Set the status bar.
        gui.setStatus("Reloading targets from " + path + "....");

        /*
            Load in targets from the targets data file specified.
            Some or all of these might duplicate what is in the working file, but
            such duplicates will not be re-added to memory.
        */
        load(path);

        /*
            Rewrite the liveCache.  This effectively merges the targets
            from the liveCache with any new targets in the target data file.
        */
        gui.setStatus("Rewriting live targets cache....");
        TargetWriter.rewriteTargets(liveTargets, liveCache);

        // Rewrite the saved and discarded caches.
        gui.setStatus("Rewriting saved targets cache....");
        TargetWriter.rewriteTargets(savedTargets, savedCache);

        gui.setStatus("Rewriting discarded targets cache....");
        TargetWriter.rewriteTargets(discardedTargets, discardedCache);

        gui.setStatus("Ready.");
    }


    /**
     *  Loads targets from a given data path into the live targets.
     *
     *  @param path the path from which to load
     *
     *  @throws IOException if the path cannot be found
     *  @throws MalformedURLException if a URL is malformed
     */
    private static void load(String path) throws IOException, MalformedURLException
    {
        load(path, liveTargets);
    }
    

    /**
     *  Loads targets from a given data path into memory.
     *
     *  @param path the path from which to load
     *  @param set  the set into which to load the targets
     *
     *  @throws IOException if the path cannot be found
     *  @throws MalformedURLException if a URL is malformed
     */
    private static void load(String path, HashMap set) throws IOException, MalformedURLException
    {
        // This BufferedReader will be passed to TargetsReader to read the file.
        BufferedReader buffReader = null;

        String encoding;

        long length;

        // Guess if this is a URL.
        if (path.indexOf("://") != -1)
        {
            // Try to create this as a URL.
            URL url = new URL(path);

            encoding = Toolkit.getDeclaredXMLEncoding(url.openStream());
            buffReader = new BufferedReader(new InputStreamReader(url.openStream(), encoding));
            length = url.openConnection().getContentLength();
        }

        // Handle paths which are apparently files.
        else
        {
            File toRead = new File(path);
            if (!toRead.exists())
            {
                // Try to create the file with empty content.
                Toolkit.checkOrCreate(path, "targets data file");
                TargetWriter.rewriteTargets(new HashMap(), toRead);
            }

            encoding = Toolkit.getDeclaredXMLEncoding(new FileInputStream(path));
            buffReader = new BufferedReader(
                            new InputStreamReader(
                                new FileInputStream(path), encoding));
            length = toRead.length();
        }
        if (buffReader != null)
        {
            TargetsReader reader = new TargetsReader(path, buffReader, new TargetsReaderListener(set),
                                                     encoding, length, gui);
            Thread readerThread = new Thread(reader);
            readerThread.setDaemon(true);
            readerThread.run();
            reader.closeMonitor();

            buffReader.close();
        }
        else
        {
            throw new IOException("I/O error trying to read \"" + path + "\".");
        }
    }


    /**
     *  Starts the Targeting GUI (and the timer, if configured).
     */
    public void run()
    {
        restartTimer(timerFrequency);
    }


    /**
     *  Starts the target data checking task with a given frequency.
     *
     *  @param frequency    seconds in between target data checks
     */
    public void restartTimer(int frequency)
    {
        if (timer != null)
        {
            try
            {
                timer.cancel();
            }
            catch (IllegalStateException e)
            {
                // Timer was already cancelled.
            }
        }
        if (frequency > 0)
        {
            timer = new Timer();
            timerFrequency = frequency * 1000;
            timer.schedule(new CheckTargetDataTask(), timerFrequency, timerFrequency);
        }
    }


    /**
     *  Returns the current timer frequency, in seconds.
     *
     *  @return the current timer frequency, in seconds
     */
    public int getReloadFrequency()
    {
        return timerFrequency / 1000;
    }


    /**
     *  A {@link java.util.TimerTask TimerTask} for checking
     *  the targets data file for changes.
     */
    private class CheckTargetDataTask extends TimerTask
    {
        public void run()
        {
            try
            {
                reload(targetsDataPath);
            }
            catch (ConcurrentModificationException e0)
            {
                // Try again later.
            }
            catch (Exception e1)
            {
                gui.showError(e1.getMessage());
                Log.userinfo(e1.getMessage(), new String[] {Log.ERROR, Log.TARGETING});
            }
        }
    }


    /**
     *  Adds a target to the specified set.  If the set
     *  is {@link #liveTargets}, then if the target is already in
     *  the live targets set, the new target is merged with
     *  the already-known target.
     *
     *  @param target   the target to add
     *  @param set      the target set to which to add it
     *
     *  @see {@link Target#merge}
     */
    static void add(Target target, HashMap set)
    {
        if (set == liveTargets)
        {
            Integer hashCode = new Integer(target.hashCode());
            if (!discardedOrSaved(hashCode))
            {
                Target alreadyKnown = (Target)liveTargets.get(hashCode);
                if (alreadyKnown != null)
                {
                    alreadyKnown.merge(target);
                }
                else
                {
                    liveTargets.put(new Integer(target.hashCode()), target);
                }
            }
        }
        else if (set == savedTargets)
        {
            save(target);
        }
        else if (set == discardedTargets)
        {
            discard(target);
        }

        updatePanels();
    }

    
    /**
     *  Indicates whether the <code>TargetingTool</code>
     *  has previously discarded or saved the target
     *  specified by a given hash code.
     *
     *  @param hashCode hash code that uniquely identifies the target
     */
    private static boolean discardedOrSaved(Integer hashCode)
    {
        return (savedTargets.containsKey(hashCode) ||
                discardedTargets.containsKey(hashCode));
    }


    /**
     *  Indicates whether the <code>TargetingTool</code>
     *  has previously discarded or saved the given target.
     *
     *  @param target   the target
     */
    private static boolean discardedOrSaved(Target target)
    {
        Integer hashCode = new Integer(target.hashCode());
        return discardedOrSaved(hashCode);
    }


    /**
     *  Saves a new category to the targets AIML file.
     *
     *  @param target   the target to save
     */
    public static void saveCategory(Target target)
    {
        boolean hasTopic;
        boolean hasThat;
        String topic = target.getNewTopic();
        String that = target.getNewThat();

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

        if (that == null)
        {
            hasThat = false;
        }
        else if (that.trim().length() == 0 || that.equals(Graphmaster.ASTERISK))
        {
            hasThat = false;
        }
        else
        {
            hasThat = true;
        }

        if (!hasTopic)
        {
            XMLWriter.write(     INDENT + CATEGORY_START + LINE_SEPARATOR +
                                 INDENT + INDENT + PATTERN_START + target.getNewPattern() + PATTERN_END + LINE_SEPARATOR,
                            AIML_RESOURCE);
            if (hasThat)
            {
                XMLWriter.write(    INDENT + INDENT + THAT_START + that + THAT_END + LINE_SEPARATOR,
                                AIML_RESOURCE);
            }
            XMLWriter.write(     INDENT + INDENT + TEMPLATE_START + LINE_SEPARATOR +
                                 INDENT + INDENT + INDENT + target.getNewTemplate() + LINE_SEPARATOR +
                                 INDENT + INDENT + TEMPLATE_END + LINE_SEPARATOR +
                                 INDENT + CATEGORY_END + LINE_SEPARATOR,
                            AIML_RESOURCE);
        }
        else
        {
            XMLWriter.write(     INDENT + TOPIC_NAME_BEGIN + topic + TOPIC_NAME_END + LINE_SEPARATOR +
                                 INDENT + INDENT + CATEGORY_START + LINE_SEPARATOR +
                                 INDENT + INDENT + INDENT + PATTERN_START + target.getNewPattern() + PATTERN_END + LINE_SEPARATOR,
                            AIML_RESOURCE);
            if (hasThat)
            {
                XMLWriter.write(    INDENT + INDENT + INDENT + THAT_START + that + THAT_END + LINE_SEPARATOR,
                                AIML_RESOURCE);
            }
            XMLWriter.write(     INDENT + INDENT + INDENT + TEMPLATE_START + LINE_SEPARATOR +
                                 INDENT + INDENT + INDENT + INDENT + target.getNewTemplate() + LINE_SEPARATOR +
                                 INDENT + INDENT + INDENT + TEMPLATE_END + LINE_SEPARATOR +
                                 INDENT + INDENT + CATEGORY_END + LINE_SEPARATOR +
                                 INDENT + TOPIC_END + LINE_SEPARATOR,
                            AIML_RESOURCE);
        }

        // Move the target from targets to savedTargets.
        save(target);
    }


    /**
     *  Returns a new target from the live targets.
     *
     *  @return a new target from the live targets
     */
    public static Target nextTarget()
    {
        List sortedTargets = getSortedTargets();

        // Check the number of currently live targets.
        int targetsCount = sortedTargets.size();

        // If this is more than zero, begin looking for the next target to return.
        if (targetsCount > 0)
        {
            // Reset the next target marker to 0 if it has exceeded the live targets max index.
            if (nextTargetToServe == targetsCount)
            {
                nextTargetToServe = 0;
            }

            // Remember the first one checked.
            int firstChecked = nextTargetToServe;

            // At the end of all this we may still have a null result.
            Target toReturn = null;

            // Start checking each target.
            do
            {
                // Get the target at the index specified by the next target marker.
                Target toCheck = (Target)sortedTargets.toArray()[nextTargetToServe];

                // Get its match pattern, for comparison.
                String matchPattern = toCheck.getMatchPattern();

                // Extend the target (will quickly return if already extended).
                toCheck.extend();

                // If its match pattern does not match one of its extension patterns, return it no matter what.
                Iterator iterator = toCheck.getExtensionPatterns().iterator();
                while (iterator.hasNext())
                {
                    String anExtensionPattern = (String)iterator.next();
                    if (!matchPattern.equals(anExtensionPattern))
                    {
                        // Be sure to increment the marker for next time.
                        nextTargetToServe++;

                        // Return this as the next target.
                        return toCheck;
                    }
                }

                // If no return yet, see if it meets the criteria specified by the calling arguments.

                // If we are to include incomplete thats,
                if (includeIncompleteThats)
                {
                    // Get its match that, for comparison.
                    String matchThat = toCheck.getMatchThat();

                    // If its match that does not match one of its extension thats, return it.
                    iterator = toCheck.getExtensionThats().iterator();
                    while (iterator.hasNext())
                    {
                        String anExtensionThat = (String)iterator.next();
                        if (!matchThat.equals(anExtensionThat))
                        {
                            // Be sure to increment the marker for next time.
                            nextTargetToServe++;

                            return toCheck;
                        }
                    }
                }
                // Otherwise, if we are to include incomplete topics,
                else if (includeIncompleteTopics)
                {
                    // Get its match topic, for comparison.
                    String matchTopic = toCheck.getMatchTopic();

                    // If its match topic does not match one of its extension topics, return it.
                    iterator = toCheck.getExtensionTopics().iterator();
                    while (iterator.hasNext())
                    {
                        String anExtensionTopic = (String)iterator.next();
                        if (!matchTopic.equals(anExtensionTopic))
                        {
                            // Be sure to increment the marker for next time.
                            nextTargetToServe++;

                            return toCheck;
                        }
                    }
                }

                // In any case where there has not yet been a return, increment the marker.
                nextTargetToServe++;

                // (Again) ensure that it does not exceed the max index for the live targets.
                if (nextTargetToServe == targetsCount)
                {
                    nextTargetToServe = 0;
                }
            }
            // Keep doing this until we cycle back to the first one checked.
            while(nextTargetToServe != firstChecked);

            // Return whatever result was found (may be null).
            return toReturn;
        }
        else
        {
            // Return null in the case that no next target was found.
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
        liveTargets.clear();
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
     *  Returns the live target set, sorted by the activations count.
     *
     *  @return the live target set
     */
    public static List getSortedTargets()
    {
        List sort = new ArrayList(liveTargets.values());
        Collections.sort(sort, new TargetActivationsComparator());
        Collections.reverse(sort);
        return sort;
    }


    /**
     *  Returns the number of live targets.
     *
     *  @return the number of live targets
     */
    public static int countLive()
    {
        return liveTargets.size();
    }


    /**
     *  Returns the number of saved targets.
     *
     *  @return the number of saved targets
     */
    public static int countSaved()
    {
        return savedTargets.size();
    }


    /**
     *  Returns the number of discarded targets.
     *
     *  @return the number of discarded targets
     */
    public static int countDiscarded()
    {
        return discardedTargets.size();
    }


    /**
     *  Performs any steps necessary before shutdown of the tool.
     */
    public static void shutdown()
    {
        if (timer != null)
        {
            timer.cancel();
        }

        // Rewrite the caches.
        TargetWriter.rewriteTargets(liveTargets, liveCache);
        TargetWriter.rewriteTargets(savedTargets, savedCache);
        TargetWriter.rewriteTargets(discardedTargets, discardedCache);

        // Update possibly changed values in the properties.
        /*
        properties.setProperty("programd.targeting.tool.reload-timer", new Integer(timerFrequency).toString());
        properties.setProperty("programd.targeting.data.path", targetsDataPath);
        properties.setProperty("programd.targeting.tool.include-incomplete-thats", new Boolean(includeIncompleteThats).toString());
        properties.setProperty("programd.targeting.tool.include-incomplete-topics", new Boolean(includeIncompleteTopics).toString());
        
        try
        {
            properties.store(new FileOutputStream(propertiesPath), "Targeting Tool Properties");
        }
        catch (IOException e)
        {
            gui.showError("I/O error while trying to save properties.");
        }*/
    }


    /**
     *  Returns the current targets data path.
     *
     *  @return the current targets data path
     */
    public String getTargetsDataPath()
    {
        return targetsDataPath;
    }


    /**
     *  Changes the targets data path and loads data from the new file.
     *
     *  @param path     the desired targets data path
     */
    public void changeTargetsDataPath(String path)
    {
        targetsDataPath = path;
        liveTargets.clear();
        liveCache.delete();
        savedTargets.clear();
        savedCache.delete();
        discardedTargets.clear();
        discardedCache.delete();
        try
        {
            reload(path);
            restartTimer(timerFrequency);
        }
        catch (Exception e)
        {
            gui.showError(e.getMessage());
            Log.userinfo(e.getMessage(), new String[] {Log.ERROR, Log.TARGETING});
            timer.cancel();
        }
    }


    /**
     *  Returns whether incomplete-that targets should be included.
     *
     *  @return whether incomplete-that targets should be included
     */
    public boolean includeIncompleteThats()
    {
        return includeIncompleteThats;
    }


    /**
     *  Returns whether incomplete-topic targets should be included.
     *
     *  @return whether incomplete-topic targets should be included
     */
    public boolean includeIncompleteTopics()
    {
        return includeIncompleteTopics;
    }


    /**
     *  Specifies whether incomplete-that targets should be included.
     *
     *  @param b    whether incomplete-that targets should be included
     */
    public void includeIncompleteThats(boolean b)
    {
        includeIncompleteThats = b;
    }


    /**
     *  Specifies whether incomplete-topic targets should be included.
     *
     *  @param b    whether incomplete-topic targets should be included
     */
    public void includeIncompleteTopics(boolean b)
    {
        includeIncompleteTopics = b;
    }


    /**
     *  Updates the panels in the GUI.
     */
    private static void updatePanels()
    {
        gui.targetPanel.updateCountDisplay();
        gui.inputPanel.updateFromTargets();
        gui.categoryPanel.updateFromTargets();
    }

    
    /**
     *  Starts up a new Targets, managed by a Thread.
     */
    public static void main(String[] args)
    {
        if (args.length == 0)
        {
            propertiesPath = "targeting.properties";
        }
        else
        {
            propertiesPath = args[0];
        }

        // Initialize properties.
        // Load Globals.
        Globals.load(propertiesPath);

        includeIncompleteThats =
            Boolean.valueOf(Globals.getProperty("programd.targeting.tool.include-incomplete-thats", "false")).booleanValue();
        includeIncompleteTopics =
            Boolean.valueOf(Globals.getProperty("programd.targeting.tool.include-incomplete-topics", "false")).booleanValue();
            
        // Set up the targets AIML resource spec.
        AIML_RESOURCE = new XMLResourceSpec();
        AIML_RESOURCE.description = "Targeting-Generated AIML";
        AIML_RESOURCE.path        = Globals.getProperty("programd.targeting.tool.aiml.path", "./targets/targets.aiml");
        AIML_RESOURCE.root        = "aiml";
        AIML_RESOURCE.dtd         = XMLResourceSpec.HTML_ENTITIES_DTD;
        AIML_RESOURCE.encoding    = Globals.getProperty("programd.targeting.aiml.encoding", "UTF-8");

        Runtime.getRuntime().addShutdownHook(
            new Thread("Shutdown Thread")
            {
                public void run()
                {
                    shutdown();
                }
            });

        new Thread (new TargetingTool()).start();
    }
}
