/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor.loadtime;

import java.util.logging.Level; 

import org.w3c.dom.Element;

import org.aitools.programd.Core;
import org.aitools.programd.CoreSettings;
import org.aitools.programd.bot.Bot;
import org.aitools.programd.bot.Bots;
import org.aitools.programd.parser.StartupFileParser;

/**
 * Supports configuration of a bot from the startup file.
 * 
 * @version 4.2
 * @author Noel Bush
 */
public class BotProcessor extends StartupElementProcessor
{
    public static final String label = "bot";
    
    public BotProcessor(Core coreToUse)
    {
        super(coreToUse);
    }
    
    public void process(Element element, StartupFileParser parser)
    {
        String botID = element.getAttribute(ID);

        if (Boolean.valueOf(element.getAttribute(ENABLED)).booleanValue())
        {
            Bots bots = parser.getCore().getBots();
            if (!bots.include(botID))
            {
                CoreSettings coreSettings = parser.getCore().getSettings();
                Bot bot = new Bot(botID, coreSettings.getPredicateEmptyDefault(), coreSettings.getLoggingXmlChatLogDirectory());
                logger.log(Level.INFO, "Configuring bot \"" + botID + "\".");
                parser.setCurrentBot(bot);
                bots.addBot(botID, bot);
                parser.evaluate(element.getChildNodes());
            } 
            // (otherwise...)
            logger.log(Level.WARNING, "Bot \"" + botID + "\" has already been configured.");
        }
    }
}