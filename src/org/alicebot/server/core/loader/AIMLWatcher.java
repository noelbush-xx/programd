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

/*
    Code cleanup (4.1.3 [00] - October 2001, Noel Bush)
    - formatting cleanup
    - general grammar fixes
    - complete javadoc
    - made all imports explicit
    - changed to use Timer rather than extend Thread
    - removed unused stopWatching method
*/

/*
    Further fixes and optimizations (4.1.3 [01] - November 2001, Noel Bush)
    - added addWatchFile() method
    - removed loadFiles() method
    - changed CheckAIMLTask to check list of known files, rather than re-read directory (!)
    - renamed fileMap to watchMap
*/

/*
    More fixes (4.1.3 [02] - November 2001, Noel Bush)
    - more design changes, so that:
      - this is a singleton (more or less)
      - files are added by the addWatchFile() method, instead of by specifying a directory
*/

package org.alicebot.server.core.loader;

import java.io.File;
import java.io.IOException;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.alicebot.server.core.Globals;
import org.alicebot.server.core.Graphmaster;
import org.alicebot.server.core.logging.Log;


/**
 *  Watches a set of AIML files. Any file changes will be loaded
 *  automatically.
 *
 *  @author Jon Baer
 *  @author Noel Bush
 *  @version 4.1.3
 */
public class AIMLWatcher
{
    /** Private instance of itself. */
    private static AIMLWatcher myself = new AIMLWatcher();
    
    /** The Timer that handles watching AIML files. */
    private Timer timer;
    
    /** Used for storing information about file changes. */
    private HashMap watchMaps = new HashMap();


    /**
     *  Prevents anyone but itself from constructing an AIMLWatcher.
     */
    private AIMLWatcher()
    {
    }


    public static void start()
    {
        myself.startTimer();
    }


    /**
     *  Initializes the AIMLWatcher timer as a daemon.
     *  Waits 10 seconds before starting it.
     */
    private void startTimer()
    {
        if (timer == null)
        {
            timer = new Timer(true);
            timer.schedule(new CheckAIMLTask(), 0,
                           Integer.parseInt(Globals.getProperty("programd.watcher.timer")));
        }
    }


    /**
     *  Prohibits cloning this class.
     */
    protected Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }


    /**
     *  Reloads AIML from a given file.
     *
     *  @param file     the file to reload
     *  @param botid    the bot for whom to reload the file
     */
    private void reload(File file, String botid)
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
     *  Adds a file to the watchlist.
     *
     *  @param path the path to the file
     *  @param botid
     */
    public static void addWatchFile(String path, String botid)
    {
        File theFile = new File(path);
        if (theFile.canRead())
        {
            if (!myself.watchMaps.containsKey(botid))
            {
                myself.watchMaps.put(botid, new HashMap());
            }
            ((HashMap)myself.watchMaps.get(botid)).put(theFile, new Long(theFile.lastModified()));
        }
    }

    /**
     *  A {@link java.util.TimerTask TimerTask} for checking
     *  changed AIML files.
     */
    private class CheckAIMLTask extends TimerTask
    {
        public void run()
        {
            Iterator mapsIterator = myself.watchMaps.keySet().iterator();
            while (mapsIterator.hasNext())
            {
                String botid = (String)mapsIterator.next();
                HashMap watchMap = (HashMap)myself.watchMaps.get(botid);
                Iterator iterator = watchMap.keySet().iterator();

                while (iterator.hasNext())
                {
                    File theFile;
                    try
                    {
                        theFile = (File)iterator.next();
                    }
                    catch (ConcurrentModificationException e)
                    {
                        // Try again next time.
                        return;
                    }
                    Long previousTime = (Long)watchMap.get(theFile);
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
            // This, unfortunately, seems to be the only way to prevent a memory leak.
            System.gc();
        }
    }
}