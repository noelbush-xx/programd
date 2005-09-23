/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor.botconfiguration;

import java.util.Date;
import java.util.logging.Level;

import org.w3c.dom.Element;

import org.aitools.programd.Core;
import org.aitools.programd.bot.Bot;
import org.aitools.programd.bot.Bots;
import org.aitools.programd.graph.Graphmaster;
import org.aitools.programd.parser.BotsConfigurationFileParser;
import org.aitools.programd.processor.ProcessorException;
import org.aitools.programd.util.URITools;

/**
 * Supports configuration of a bot from the startup file.
 * 
 * @version 4.5
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class BotProcessor extends BotConfigurationElementProcessor
{
    /** The label (as required by the registration scheme). */
    public static final String label = "bot";

    /**
     * Creates a new BotProcessor with the given Core.
     * 
     * @param coreToUse the Core to use
     */
    public BotProcessor(Core coreToUse)
    {
        super(coreToUse);
    }
    
    /**
     * @see org.aitools.programd.processor.botconfiguration.BotConfigurationElementProcessor#process(org.w3c.dom.Element, org.aitools.programd.parser.BotsConfigurationFileParser)
     */
    @Override
    public String process(Element element, BotsConfigurationFileParser parser) throws ProcessorException
    {
        return processResponse(element, parser);
    }

    /**
     * Processes a bot element and returns the id of the bot loaded.
     * 
     * @param element the bot element 
     * @param parser the parser in use
     * @return the id of the bot loaded
     * @throws ProcessorException if there was a problem
     * @see BotConfigurationElementProcessor#process(Element,
     *      BotsConfigurationFileParser)
     */
    public String processResponse(Element element, BotsConfigurationFileParser parser) throws ProcessorException
    {
        if (element.hasAttribute(HREF))
        {
            return this.core.loadBot(URITools.createValidURL(element.getAttribute(HREF)));
        }

        String botID = element.getAttribute(ID);

        if (Boolean.valueOf(element.getAttribute(ENABLED)).booleanValue())
        {
            Bots bots = this.core.getBots();
            if (!bots.include(botID))
            {
                Bot bot = new Bot(botID, parser.getCore().getSettings());
                
                logger.log(Level.INFO, "Configuring bot \"" + botID + "\".");
                parser.setCurrentBot(bot);
                bots.addBot(botID, bot);
                
                Graphmaster graphmaster = this.core.getGraphmaster();
                
                int previousCategoryCount = graphmaster.getTotalCategories();
                int previousDuplicateCount = graphmaster.getDuplicateCategories();

                this.core.setLoadtime();
                
                // Stop the AIMLWatcher while loading.
                if (this.core.getSettings().useWatcher())
                {
                    this.core.getAIMLWatcher().stop();
                }
                
                // Index the start time before loading.
                long time = new Date().getTime();
                
                // Load the bot.
                parser.evaluate(element.getChildNodes());

                // Calculate the time used to load all categories.
                time = new Date().getTime() - time;

                this.core.unsetLoadtime();

                // Restart the AIMLWatcher.
                if (this.core.getSettings().useWatcher())
                {
                    this.core.getAIMLWatcher().start();
                }
                
                logger.log(Level.INFO, (graphmaster.getTotalCategories() - previousCategoryCount)
                        + " unique categories loaded in " + time / 1000.00 + " seconds.");
                
                int dupes = graphmaster.getDuplicateCategories() - previousDuplicateCount;
                if (dupes > 0)
                {
                    logger.log(Level.WARNING, dupes
                        + " path-identical categories were encountered, and handled according to the " + this.core.getSettings().getMergePolicy() + " merge policy.");
                }
            }
            else
            {
                logger.log(Level.WARNING, "Bot \"" + botID + "\" has already been configured.");
            }
        }
        return botID;
    }
}