/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.loader;

import org.aitools.programd.bot.Bot;
import org.aitools.programd.bot.Bots;
import org.aitools.programd.graph.Graphmaster;
import org.aitools.programd.graph.Nodemapper;
import org.aitools.programd.parser.AIMLReaderListener;
import org.aitools.programd.util.Globals;
import org.aitools.programd.util.Trace;
import org.aitools.programd.util.logging.Log;

/**
 * A utility class used by the
 * {@link org.aitools.programd.graph.Graphmaster Graphmaster}to load AIML
 * files.
 * 
 * @author Richard Wallace
 * @author Kris Drent
 * @author Jon Baer
 * @author Thomas Ringate, Pedro Colla
 * @version 4.1.3
 */
public class AIMLLoader implements AIMLReaderListener
{
    /** The interval at which loaded categories should be notified. */
    private static int NOTIFY_INTERVAL = Globals.getCategoryLoadNotifyInterval();

    /** Whether to show the console. */
    protected static final boolean SHOW_CONSOLE = Globals.showConsole();

    /** The file name being loaded. */
    private static String filename;

    /** The id of the bot for whom this file is being loaded. */
    private static String botid;

    /** The bot for whom this file is being loaded. */
    private static Bot bot;

    /** The merge policy. */
    private static String policy;

    /** A space. */
    private static final String SPACE = " ";

    /** An empty string. */
    private static final String EMPTY_STRING = "";

    /** An atomic tag close marker. */
    private static final String ATOMIC_CLOSE = "/>";

    /** A slash. */
    private static final String SLASH = "/";

    /** The word &quot;name&quot;. */
    private static final String NAME = "name";

    /** The word &quot;localhost&quot;. */
    private static final String LOCALHOST = "localhost";

    /**
     * Initializes the <code>AIMLLoader</code>.
     */
    public AIMLLoader(String filenameToUse, String botidToUse)
    {
        AIMLLoader.filename = filenameToUse;
        AIMLLoader.botid = botidToUse;
        if (botid != null)
        {
            AIMLLoader.bot = Bots.getBot(botidToUse);
        }
        AIMLLoader.policy = Globals.getMergePolicy();
    }

    public void newCategory(String pattern, String that, String topic, String template)
    {
        boolean process = true;

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
        if (template == null)
        {
            template = Graphmaster.ASTERISK;
        }

        if (SHOW_CONSOLE)
        {
            if (Graphmaster.getTotalCategories() % NOTIFY_INTERVAL == 0 && Graphmaster.getTotalCategories() > 0)
            {
                Trace.userinfo(Graphmaster.getTotalCategories() + " categories loaded so far.");
            }
        }

        if (process)
        {
            Nodemapper node = Graphmaster.add(pattern, that, topic, botid);
            if (node.get(Graphmaster.TEMPLATE) == null)
            {
                node.put(Graphmaster.FILENAME, filename);
                bot.addToFilenameMap(filename, node);
                node.put(Graphmaster.TEMPLATE, template);
                Graphmaster.incrementTotalCategories();
            }
            else
            {
                if (!policy.equals("true"))
                {
                    if (Globals.showConsole())
                    {
                        Log.userinfo(new String[]
                            { "Duplicate category:", pattern + " : " + that + " : " + topic,
                                    " in \"" + filename + "\"", "conflicts with category already loaded from",
                                    (String) node.get(Graphmaster.FILENAME) }, Log.MERGE);
                    }
                }
                else
                {
                    node.put(Graphmaster.FILENAME, filename);
                    node.put(Graphmaster.TEMPLATE, template);
                }
            }
        }
    }
}