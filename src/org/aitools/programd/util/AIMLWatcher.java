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
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.aitools.programd.Core;
import org.aitools.util.resource.URLTools;
import org.apache.log4j.Logger;

/**
 * Watches a set of AIML files. Any file changes will be loaded automatically.
 * 
 * @author Jon Baer
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class AIMLWatcher
{
    /** The Timer that handles watching AIML files. */
    private Timer timer;

    private Core _core;

    /** Used for storing information about file changes. */
    protected Map<URL, Long> watchMap = new HashMap<URL, Long>();

    protected Logger logger = Logger.getLogger("programd");

    /**
     * Creates a new AIMLWatcher using the given Graphmaster
     * 
     * @param core the Core to use
     */
    public AIMLWatcher(Core core)
    {
        this._core = core;
        this.logger = this._core.getLogger();
    }

    /**
     * Starts the AIMLWatcher.
     */
    public void start()
    {
        this.timer = new Timer(true);
        this.timer.schedule(new CheckAIMLTask(), 0, this._core.getSettings().getAIMLWatcherTimer());
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
     */
    protected void reload(URL path)
    {
        this.logger.info(String.format("AIMLWatcher reloading \"%s\".", URLTools.unescape(path)));
        this._core.reload(path);
    }

    /**
     * Adds a file to the watchlist.
     * 
     * @param path the path to the file
     */
    @SuppressWarnings("boxing")
    public void addWatchFile(URL path)
    {
        /*
         * if (this.logger.isDebugEnabled()) { this.logger.debug(String.format("Adding watch file \"%s\".", path)); }
         */
        synchronized (this)
        {
            if (URLTools.seemsToExist(path))
            {
                if (this.watchMap.containsKey(path))
                {
                    this.watchMap.put(path, URLTools.getLastModified(path));
                }
            }
            else
            {
                this.logger.warn(String.format("AIMLWatcher cannot read path \"%s\"", URLTools.unescape(path)),
                        new IOException());
            }
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
        @SuppressWarnings("boxing")
        @Override
        public void run()
        {
            synchronized (AIMLWatcher.this)
            {
                for (URL path : AIMLWatcher.this.watchMap.keySet())
                {
                    long previousTime = AIMLWatcher.this.watchMap.get(path);
                    if (previousTime != 0)
                    {
                        long lastModified = URLTools.getLastModified(path);
                        if (lastModified > previousTime)
                        {
                            AIMLWatcher.this.watchMap.put(path, lastModified);
                            reload(path);
                        }
                    }
                }
            }
        }
    }
}
