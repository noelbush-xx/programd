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
    - complete javadoc (except implemented method)
    - removed useless imports
    - removed this class's getArg method and used identical method in AIMLParser
    - allowed configuration of category load notify interval via Globals
    - inlined processing of load tags, instead of calling little method in Toolkit
    - removed useless slowdown call to AIMLParser.processResponse for each template!
*/

/*
    More fixes (4.1.3 [02] - November 2001, Noel Bush)
    - changed to use changed method name (getBotPredicateValue()) of BotProperty
*/

package org.alicebot.server.core.loader;

import java.util.StringTokenizer;

import org.alicebot.server.core.BotProperty;
import org.alicebot.server.core.Globals;
import org.alicebot.server.core.Graphmaster;
import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.node.Nodemapper;
import org.alicebot.server.core.parser.AIMLReaderListener;
import org.alicebot.server.core.parser.AIMLTag;
import org.alicebot.server.core.util.Substituter;
import org.alicebot.server.core.util.Toolkit;
import org.alicebot.server.core.util.Trace;


/** 
 *  A utility class used by the
 *  {@link org.alicebot.core.Graphmaster Graphmaster}
 *  to load AIML files. 
 * 
 *  @author Richard Wallace
 *  @author Kris Drent
 *  @author Jon Baer
 *  @author Thomas Ringate, Pedro Colla
 *  @version 4.1.3
 */
public class AIMLLoader implements AIMLReaderListener
{
    /** The interval at which loaded categories should be notified. */
    private static int NOTIFY_INTERVAL = Globals.getCategoryLoadNotifyInterval();

    /** The file name being loaded. */
    private static String filename; 

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
     *  Initializes the <code>AIMLLoader</code>.
     */
    public AIMLLoader(String filename)
    {
        this.filename = filename;
        this.policy = Globals.getMergePolicy();
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
        

        if (Globals.showConsole())
        {
            if (Graphmaster.getTotalCategories() % NOTIFY_INTERVAL == 0 && Graphmaster.getTotalCategories() > 0)
            {
                Trace.userinfo(Graphmaster.getTotalCategories() + " categories loaded so far.");
            }
        }
        
        // Replace old forms of bot name in pattern.
        while (pattern.indexOf(AIMLTag.NAME_VALUE) >= 0)
        {
            pattern = Substituter.replace(AIMLTag.NAME_VALUE, Globals.getBotName(), pattern);
        }
        while (pattern.indexOf(AIMLTag.BOT_NAME) >= 0)
        {
            pattern = Substituter.replace(AIMLTag.BOT_NAME, Globals.getBotName(), pattern);
        }

        // Replace values of bot predicates in pattern.
        while (pattern.indexOf(AIMLTag.BOT_NEW_OPEN) >= 0)
        {
            String bPredicate = "";
            String bReplace = "";
            StringTokenizer patternTokens = new StringTokenizer(pattern, SPACE);
            while (patternTokens.hasMoreTokens())
            {
                String token = patternTokens.nextToken();
                if (token.endsWith(AIMLTag.BOT_OPEN_NOSPACE))
                {
                    bPredicate = (new StringTokenizer(patternTokens.nextToken(), SLASH)).nextToken();
                    bReplace = AIMLTag.BOT_NEW_OPEN + bPredicate + ATOMIC_CLOSE;
                }
            }
            String bargValue = Toolkit.getAttributeValue(NAME, bPredicate);
            String solvedtag = Globals.getBotName();
            if (!bargValue.endsWith(NAME))
            {
                solvedtag = BotProperty.getPredicateValue(bargValue);
            }
            pattern = Substituter.replace(bReplace,solvedtag,pattern);
        }

        if (process)
        {
            Nodemapper node = Graphmaster.add(pattern, that, topic);
            if (node.get(Graphmaster.TEMPLATE) == null)
            {
                node.put(Graphmaster.FILENAME, filename);
                node.put(Graphmaster.TEMPLATE, template);
                Graphmaster.incrementTotalCategories();
            }
            else
            {
                if (!policy.equals("true"))
                {
                    if (Globals.showConsole())
                    {
                        Log.userinfo(new String[] {"Duplicate category:",
                                                   pattern + " : " + that + " : " + topic,
                                                   " in \"" + filename + "\"",
                                                   "conflicts with category already loaded from",
                                                   (String)node.get(Graphmaster.FILENAME)},
                                     Log.MERGE);
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

