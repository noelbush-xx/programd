/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.loader;

import java.io.File;
import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.aitools.programd.graph.Graphmaster;
import org.aitools.programd.util.FileManager;

/**
 * Watches a set of AIML files. Any file changes will be loaded automatically.
 * 
 * @author Jon Baer
 * @author Noel Bush
 * @version 4.2
 */
public class AIMLWatcher
{
    /** The Timer that handles watching AIML files. */
    private Timer timer;

    private Graphmaster graphmaster;

    /** Used for storing information about file changes. */
    protected HashMap<String, HashMap<File, Long>> watchMaps = new HashMap<String, HashMap<File, Long>>();
    
    private static final Logger logger = Logger.getLogger("programd.learn");

    public AIMLWatcher(Graphmaster graphmasterToUse)
    {
        this.graphmaster = graphmasterToUse;
    }

    public void start()
    {
        this.startTimer();
    }

    /**
     * Initializes the AIMLWatcher timer as a daemon. Waits 10 seconds before
     * starting it.
     */
    private void startTimer()
    {
        if (this.timer == null)
        {
            this.timer = new Timer(true);
            this.timer.schedule(new CheckAIMLTask(this.watchMaps), 0, this.graphmaster.getCore().getSettings().getWatcherTimer());
        }
    }

    /**
     * Prohibits cloning this class.
     */
    protected Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }

    /**
     * Reloads AIML from a given file.
     * 
     * @param file
     *            the file to reload
     * @param botid
     *            the bot for whom to reload the file
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
        logger.log(Level.INFO, "Reloading \"" + path + "\".");
        this.graphmaster.load(path, botid);
    }

    /**
     * Adds a file to the watchlist.
     * 
     * @param path
     *            the path to the file
     * @param botid
     */
    public void addWatchFile(String path, String botid)
    {
        File theFile = FileManager.getFile(path);
        if (theFile.canRead())
        {
            if (!this.watchMaps.containsKey(botid))
            {
                this.watchMaps.put(botid, new HashMap<File, Long>());
            }
            this.watchMaps.get(botid).put(theFile, new Long(theFile.lastModified()));
        }
    }

    /**
     * A {@link java.util.TimerTask TimerTask} for checking changed AIML files.
     */
    private class CheckAIMLTask extends TimerTask
    {
        HashMap<String, HashMap<File, Long>> watcherWatchMaps;

        public CheckAIMLTask(HashMap<String, HashMap<File, Long>> watchMapsToUse)
        {
            super();
            this.watcherWatchMaps = watchMapsToUse;
        }

        public void run()
        {
            Iterator mapsIterator = this.watcherWatchMaps.keySet().iterator();
            while (mapsIterator.hasNext())
            {
                String botid = (String) mapsIterator.next();
                HashMap<File, Long> watchMap = this.watcherWatchMaps.get(botid);
                Iterator iterator = watchMap.keySet().iterator();

                while (iterator.hasNext())
                {
                    File theFile;
                    try
                    {
                        theFile = (File) iterator.next();
                    }
                    catch (ConcurrentModificationException e)
                    {
                        // Try again next time.
                        return;
                    }
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