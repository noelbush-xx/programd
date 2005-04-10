/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.loader;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.aitools.programd.bot.Bot;
import org.aitools.programd.bot.Bots;
import org.aitools.programd.graph.Graphmaster;
import org.aitools.programd.graph.Nodemapper;
import org.aitools.programd.parser.AIMLReaderListener;

/**
 * A utility class used by the
 * {@link org.aitools.programd.graph.Graphmaster Graphmaster} to load AIML
 * files.
 * 
 * @author Richard Wallace
 * @author Kris Drent
 * @author Jon Baer
 * @author Thomas Ringate, Pedro Colla
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @version 4.5
 */
public class AIMLLoader implements AIMLReaderListener
{
    /** The interval at which loaded categories should be notified. */
    private int notifyInterval;

    /** The file name being loaded. */
    private String filename;

    /** The id of the bot for whom this file is being loaded. */
    private String botid;

    /** The bot for whom this file is being loaded. */
    private Bot bot;
    
    /** The Graphmaster in use. */
    private Graphmaster graphmaster;
    
    /** The Bots object in use. */
    private Bots bots;

    /** The merge policy. */
    private boolean policy;
    
    /** The logger. */
    private Logger logger;

    /**
     * Initializes the <code>AIMLLoader</code>.
     * @param graphmasterToUse the Graphmaster into which to load new categories
     * @param filenameToUse the filename from which to load new categories
     * @param botidToUse the id of the bot for whom to load new categories
     */
    public AIMLLoader(Graphmaster graphmasterToUse, String filenameToUse, String botidToUse)
    {
        this.graphmaster = graphmasterToUse;
        this.bots = this.graphmaster.getCore().getBots();
        this.logger = Logger.getLogger("programd");
        this.filename = filenameToUse;
        this.botid = botidToUse;
        if (this.botid != null)
        {
            this.bot = this.bots.getBot(botidToUse);
        } 
        this.policy = this.graphmaster.getCore().getSettings().mergePolicy();
        this.notifyInterval = this.graphmaster.getCore().getSettings().getCategoryLoadNotifyInterval();
    } 

    /**
     * @see org.aitools.programd.parser.AIMLReaderListener#newCategory(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public void newCategory(String pattern, String that, String topic, String template)
    {
        // Make sure the path components are right.
        if (pattern == null)
        {
            pattern = Graphmaster.ASTERISK;
        } 
        if (that == null)
        {
            that = Graphmaster.ASTERISK;
        } 
        if (topic == null)
        {
            topic = Graphmaster.ASTERISK;
        }

        int currentTotalCategories = this.graphmaster.getTotalCategories();
        if (currentTotalCategories % this.notifyInterval == 0 && currentTotalCategories > 0)
        {
            this.logger.log(Level.INFO, currentTotalCategories + " categories loaded so far.");
        } 

        Nodemapper node = this.graphmaster.add(pattern, that, topic, this.botid);
        if (node.get(Graphmaster.TEMPLATE) == null)
        {
            node.put(Graphmaster.FILENAME, this.filename);
            this.bot.addToFilenameMap(this.filename, node);
            node.put(Graphmaster.TEMPLATE, template);
            this.graphmaster.incrementTotalCategories();
        } 
        else
        {
            if (!this.policy)
            {
                /*
                if (Settings.showConsole())
                {
                    Log.userinfo(new String[]
                        { "Duplicate category:", pattern + " : " + that + " : " + topic,
                                " in \"" + filename + "\"", "conflicts with category already loaded from",
                                (String) node.get(Graphmaster.FILENAME) } , Log.MERGE);
                }
                */
            } 
            else
            {
                node.put(Graphmaster.FILENAME, this.filename);
                node.put(Graphmaster.TEMPLATE, template);
            } 
        } 
    } 
}