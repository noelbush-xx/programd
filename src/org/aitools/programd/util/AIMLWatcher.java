/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.util;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.aitools.programd.graph.Graphmaster;

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

    private Graphmaster graphmaster;

    /** Used for storing information about file changes. */
    protected HashMap<String, Map<File, Long>> watchMaps = new HashMap<String, Map<File, Long>>();

    protected static final Logger logger = Logger.getLogger("programd");

    /**
     * Creates a new AIMLWatcher using the given Graphmaster
     * 
     * @param graphmasterToUse the Graphmaster to use
     */
    public AIMLWatcher(Graphmaster graphmasterToUse)
    {
        this.graphmaster = graphmasterToUse;
    }

    /**
     * Starts the AIMLWatcher.
     */
    public void start()
    {
        if (this.timer == null)
        {
            this.timer = new Timer(true);
            this.timer.schedule(new CheckAIMLTask(), 0, this.graphmaster.getCore().getSettings().getWatcherTimer());
        }
    }

    /**
     * Reloads AIML from a given file.
     * 
     * @param file the file to reload
     * @param botid the bot for whom to reload the file
     */
    protected void reload(File file, String botid)
    {
        String path;
        try
        {
            path = file.getCanonicalPath();
        }
        catch (IOException e)
        {
            return;
        }
        logger.log(Level.INFO, "AIMLWatcher reloading \"" + path + "\".");
        this.graphmaster.load(path, botid);
    }

    /**
     * Adds a file to the watchlist.
     * 
     * @param path the path to the file
     * @param botid
     */
    public void addWatchFile(String path, String botid)
    {
        File theFile = FileManager.getFile(path);
        if (theFile.canRead())
        {
            if (!this.watchMaps.containsKey(botid))
            {
                this.watchMaps.put(botid, Collections.checkedMap(new HashMap<File, Long>(), File.class, Long.class));
            }
            this.watchMaps.get(botid).put(theFile, new Long(theFile.lastModified()));
        }
        else
        {
            throw new DeveloperError("AIMLWatcher cannot read path \"" + path + "\"", new IOException());
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
        public void run()
        {
            for (String botid : AIMLWatcher.this.watchMaps.keySet())
            {
                Map<File, Long> watchMap = AIMLWatcher.this.watchMaps.get(botid);

                for (File theFile : watchMap.keySet())
                {
                    Long previousTime = watchMap.get(theFile);
                    if (previousTime == null)
                    {
                        watchMap.put(theFile, new Long(theFile.lastModified()));
                        reload(theFile, botid);
                    }
                    else
                    {
                        long lastModified = theFile.lastModified();
                        if (lastModified > previousTime.longValue())
                        {
                            watchMap.put(theFile, new Long(lastModified));
                            reload(theFile, botid);
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