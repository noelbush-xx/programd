/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor.loadtime;

import org.aitools.programd.bot.Bot;
import org.aitools.programd.bot.Bots;
import org.aitools.programd.parser.StartupFileParser;
import org.aitools.programd.parser.XMLNode;
import org.aitools.programd.util.XMLKit;
import org.aitools.programd.util.logging.Log;

/**
 * Supports configuration of a bot from the startup file.
 */
public class BotProcessor extends StartupElementProcessor
{
    public static final String label = "bot";

    public String process(int level, XMLNode tag, StartupFileParser parser) throws InvalidStartupElementException
    {
        String botID = XMLKit.getAttributeValue(ID, tag.XMLAttr);

        if (!botID.equals(EMPTY_STRING))
        {
            if (Boolean.valueOf(XMLKit.getAttributeValue(ENABLED, tag.XMLAttr)).booleanValue())
            {
                if (!Bots.include(botID))
                {
                    Bot bot = new Bot(botID);
                    Log.userinfo("Configuring bot \"" + botID + "\".", Log.STARTUP);
                    parser.setCurrentBot(bot);
                    Bots.addBot(botID, bot);
                    return parser.evaluate(level++, tag.XMLChild);
                } 
                // (otherwise...)
                Log.userinfo("Bot \"" + botID + "\" has already been configured.", Log.STARTUP);
                return EMPTY_STRING;
            } 
        } 
        return EMPTY_STRING;
    } 
}