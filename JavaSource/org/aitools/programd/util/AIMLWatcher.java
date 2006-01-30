/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.util;

import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.aitools.programd.Core;
import org.apache.log4j.Logger;

/**
 * Watches a set of AIML files. Any file changes will be loaded automatically.
 * 
 * @author Jon Baer
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @version 4.5
 */
public class AIMLWatcher
{
    /** The Timer that handles watching AIML files. */
    private Timer timer;

    private Core core;

    /** Used for storing information about file changes. */
    protected HashMap<String, Map<URL, Long>> watchMaps = new HashMap<String, Map<URL, Long>>();

    protected static final Logger logger = Logger.getLogger("programd");

    /**
     * Creates a new AIMLWatcher using the given Graphmaster
     * 
     * @param coreToUse the Core to use
     */
    public AIMLWatcher(Core coreToUse)
    {
        this.core = coreToUse;
    }

    /**
     * Starts the AIMLWatcher.
     */
    public void start()
    {
        this.timer = new Timer(true);
        this.timer.schedule(new CheckAIMLTask(), 0, this.core.getSettings().getWatcherTimer());
    }
    
    /**
     * Stops the AIMLWatcher.
     */
    public void stop()
    {
        if (this.timer != null)
        {
            this.timer.cancel();
        }
    }

    /**
     * Reloads AIML from a given path.
     * 
     * @param path the path to reload
     * @param botid the bot for whom to reload the file
     */
    protected void reload(URL path, String botid)
    {
        logger.info("AIMLWatcher reloading \"" + path + "\".");
        this.core.load(path, botid);
    }

    /**
     * Adds a file to the watchlist.
     * 
     * @param path the path to the file
     * @param botid
     */
    public void addWatchFile(URL path, String botid)
    {
        if (URITools.seemsToExist(path))
        {
            if (!this.watchMaps.containsKey(botid))
            {
                this.watchMaps.put(botid, Collections.checkedMap(new HashMap<URL, Long>(), URL.class, Long.class));
            }
            this.watchMaps.get(botid).put(path, URITools.getLastModified(path));
        }
        else
        {
            logger.warn("AIMLWatcher cannot read path \"" + path + "\"", new IOException());
        }
    }

    /**
     * A {@link java.util.TimerTask TimerTask} for checking changed AIML files.
     */
    private class CheckAIMLTask extends TimerTask
    {
        /**
         * Creates a new CheckAIMLTask.
         */
        public CheckAIMLTask()
        {
            super();
        }

        /**
         * @see java.util.TimerTask#run()
         */
        @Override
        public void run()
        {
            for (String botid : AIMLWatcher.this.watchMaps.keySet())
            {
                Map<URL, Long> watchMap = AIMLWatcher.this.watchMaps.get(botid);

                for (URL path : watchMap.keySet())
                {
                    long previousTime = watchMap.get(path);
                    if (previousTime != 0)
                    {
                        long lastModified = URITools.getLastModified(path);
                        if (lastModified > previousTime)
                        {
                            watchMap.put(path, lastModified);
                            reload(path, botid);
                        }
                    }
                }
            }
            // This, unfortunately, seems to be the only way to prevent a memory
            // leak.
            System.gc();
        }
    }
}