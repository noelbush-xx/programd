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

import org.aitools.programd.graph.Graphmaster;
import org.aitools.programd.util.FileManager;
import org.aitools.programd.util.Globals;
import org.aitools.programd.util.logging.Log;

/**
 * Watches a set of AIML files. Any file changes will be loaded automatically.
 * 
 * @author Jon Baer
 * @author Noel Bush
 * @version 4.1.3
 */
public class AIMLWatcher
{
    /** Private instance of itself. */
    protected static AIMLWatcher myself = new AIMLWatcher();

    /** The Timer that handles watching AIML files. */
    private Timer timer;

    /** Used for storing information about file changes. */
    protected HashMap<String, HashMap<File, Long>> watchMaps = new HashMap<String, HashMap<File, Long>>();

    /**
     * Prevents anyone but itself from constructing an AIMLWatcher.
     */
    private AIMLWatcher()
    {
        // Nothing to do.
    } 

    public static void start()
    {
        myself.startTimer();
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
            this.timer
                    .schedule(new CheckAIMLTask(), 0, Integer.parseInt(Globals.getProperty("programd.watcher.timer")));
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
        Log.userinfo("Reloading \"" + path + "\".", Log.LEARN);
        Graphmaster.load(path, botid);
    } 

    /**
     * Adds a file to the watchlist.
     * 
     * @param path
     *            the path to the file
     * @param botid
     */
    public static void addWatchFile(String path, String botid)
    {
        File theFile = FileManager.getFile(path);
        if (theFile.canRead())
        {
            if (!myself.watchMaps.containsKey(botid))
            {
                myself.watchMaps.put(botid, new HashMap<File, Long>());
            } 
            myself.watchMaps.get(botid).put(theFile, new Long(theFile.lastModified()));
        } 
    } 

    /**
     * A {@link java.util.TimerTask TimerTask} for checking changed AIML files.
     */
    private class CheckAIMLTask extends TimerTask
    {
        public void run()
        {
            Iterator mapsIterator = myself.watchMaps.keySet().iterator();
            while (mapsIterator.hasNext())
            {
                String botid = (String) mapsIterator.next();
                HashMap<File, Long> watchMap = myself.watchMaps.get(botid);
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