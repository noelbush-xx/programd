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
import java.util.ConcurrentModificationException;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.alicebot.server.core.Globals;
import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.logging.Trace;
import org.alicebot.server.core.targeting.TargetMaster;
import org.alicebot.server.core.targeting.gui.TargetingGUI;
import org.alicebot.server.core.targeting.gui.TargetPanel;

/**
 *  Manages the use of the Targeting GUI.
 *
 *  @author Noel Bush
 */
public class TargetingTool implements Runnable
{
    /** Version string. */
    public static final String VERSION = "4.1.3 [04]";

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

    /** The instance of TargetingGUI managed by this class. */
    private static TargetingGUI gui;
    

    /**
     *  Creates a new instance of TargetingTool,
     *  loading targets from targets data path.
     */
    public TargetingTool()
    {
        this.gui = new TargetingGUI(this);

        // Be sure that caches exist.
        try
        {
            liveCache.createNewFile();
            savedCache.createNewFile();
            discardedCache.createNewFile();
        }
        catch (IOException e)
        {
            Trace.userfail("I/O error creating target cache files.");
        }

        // Load in the live targets from the cache.
        TargetMaster.load(LIVE_CACHE_PATH);

        // Now load the saved and discarded targets.
        TargetMaster.load(SAVED_CACHE_PATH, TargetMaster.SAVED);
        TargetMaster.load(DISCARDED_CACHE_PATH, TargetMaster.DISCARDED);

        /*
            Call "reload" to load in targets
            from the actual target data file, and
            subsequently rewrite the liveCache.  This
            will cause the liveCache to contain only
            unprocessed targets.
        */
        reload(targetsDataPath);

        // Lastly, go to the next target.
        gui.targetPanel.nextTarget();
    }


    /**
     *  Reloads from the currently-specified targets data path.
     */
    public void reload()
    {
        reload(targetsDataPath);
    }


    /**
     *  Loads targets from a given path and merges them with
     *  the contents of a local working file, then rewrites
     *  the working files.
     *
     *  @param path the target data file to read
     */
    public void reload(String path)
    {
        /*
            Load in targets from the targets data file specified.
            Some or all of these might duplicate what is in the working file, but
            such duplicates will not be re-added to memory.
        */
        TargetMaster.load(path);

        /*
            Rewrite the liveCache.  This effectively merges the targets
            from the liveCache with any new targets in the target data file.
        */
        TargetMaster.rewriteTargets(liveCache, TargetMaster.LIVE);

        // Rewrite the saved and discarded caches.
        TargetMaster.rewriteTargets(savedCache, TargetMaster.SAVED);
        TargetMaster.rewriteTargets(discardedCache, TargetMaster.DISCARDED);

        // Update the counter in the Targets panel.
        gui.targetPanel.updateCountDisplay();
    }


    /**
     *  Starts the Targeting GUI.
     */
    public void run()
    {
        // Start up the timer.
        if (timer == null)
        {
            timer = new Timer(true);
            timer.schedule(new CheckTargetDataTask(), 0,
                           Integer.parseInt(Globals.getProperty("programd.targeting-tool.reload-timer")));
        }
        gui.start();
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
            catch (ConcurrentModificationException e)
            {
                // Try again later.
            }
        }
    }


    /**
     *  Discards a target from the TargetMaster.
     *
     *  @param target   the target to discard
     */
    public static void discard(Target target)
    {
        if (target != null)
        {
            TargetMaster.discard(target);
        }
    }

    
    /**
     *  Discards all targets from the TargetMaster.
     *
     *  @param target   the target to discard
     */
    public static void discardAll()
    {
        TargetMaster.discardAll();
    }

    
    /**
     *  Sends a target to the TargetMaster.
     *
     *  @param target   the target to discard
     */
    public static void saveCategory(Target target)
    {
        if (target != null)
        {
            TargetMaster.saveCategory(target);
        }
    }


    /**
     *  Returns a next target from the TargetMaster.
     */
    public static Target getNextTarget()
    {
        return TargetMaster.nextTarget();
    }


    /**
     *  Returns the number of live targets.
     */
    public static int countLive()
    {
        return TargetMaster.count(TargetMaster.LIVE);
    }


    /**
     *  Returns the number of saved targets.
     */
    public static int countSaved()
    {
        return TargetMaster.count(TargetMaster.SAVED);
    }


    /**
     *  Returns the number of discarded targets.
     */
    public static int countDiscarded()
    {
        return TargetMaster.count(TargetMaster.DISCARDED);
    }


    /**
     *  Performs any steps necessary before shutdown of the tool,
     *  then exits.
     */
    public static void shutdown()
    {
        // Rewrite the caches.
        TargetMaster.rewriteTargets(liveCache, TargetMaster.LIVE);
        TargetMaster.rewriteTargets(savedCache, TargetMaster.SAVED);
        TargetMaster.rewriteTargets(discardedCache, TargetMaster.DISCARDED);

        // Exit
        System.exit(0);
    }

    
    /**
     *  Starts up a new Targets, managed by a Thread.
     */
    public static void main(String[] args)
    {
        String serverPropertiesPath;

        if (args.length < 2)
        {
            serverPropertiesPath = "server.properties";
        }
        else
        {
            serverPropertiesPath = args[1];
        }
        if (args.length == 0)
        {
            targetsDataPath = DEFAULT_TARGETS_DATA_PATH;
        }
        else
        {
            targetsDataPath = args[0];
        }

        // Initialize Globals.
        if (!Globals.isLoaded())
        {
            Properties serverProperties = new Properties();
            try
            {
                serverProperties.load(new FileInputStream(serverPropertiesPath));
            }
            catch (IOException e)
            {
                // Error loading properties
                Log.userinfo("Could not find \"" + serverPropertiesPath + "\"!", new String[] {Log.ERROR, Log.STARTUP});
            }
            Globals.load(serverProperties);
        }

        Trace.userinfo("Launching Targeting GUI with data path \"" + targetsDataPath + "\".");
        new Thread (new TargetingTool()).start();
    }
}
