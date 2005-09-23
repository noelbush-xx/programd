/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.parser;

import org.aitools.programd.Core;
import org.aitools.programd.bot.Bot;
import org.aitools.programd.processor.botconfiguration.BotConfigurationElementProcessor;

/**
 * <code>BotsConfigurationFileParser</code> processes a bots configuration
 * file.
 */
public class BotsConfigurationFileParser extends GenericParser<BotConfigurationElementProcessor>
{
    private Bot currentBot;

    /**
     * Initializes a <code>BotsConfigurationFileParser</code>.
     * 
     * @param coreToUse
     */
    public BotsConfigurationFileParser(Core coreToUse)
    {
        super(coreToUse.getBotConfigurationElementProcessorRegistry(), coreToUse);
    }

    /**
     * Sets the current Bot to the given Bot.
     * 
     * @param bot the Bot to set the current Bot to
     */
    public void setCurrentBot(Bot bot)
    {
        this.currentBot = bot;
    }

    /**
     * @return the current Bot
     */
    public Bot getCurrentBot()
    {
        return this.currentBot;
    }
}