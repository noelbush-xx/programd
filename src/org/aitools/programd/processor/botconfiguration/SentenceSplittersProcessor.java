/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor.botconfiguration;

import java.util.List;
import java.util.logging.Level;

import org.w3c.dom.Element;

import org.aitools.programd.Core;
import org.aitools.programd.bot.Bot;
import org.aitools.programd.parser.BotsConfigurationFileParser;
import org.aitools.programd.processor.ProcessorException;
import org.aitools.programd.util.XMLKit;

/**
 * The <code>sentence-splitters</code> element is a container for defining
 * strings that should cause the input to be split into sentences.
 * 
 * @version 4.5
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class SentenceSplittersProcessor extends BotConfigurationElementProcessor
{
    /** The label (as required by the registration scheme). */
    public static final String label = "sentence-splitters";

    /**
     * Creates a new SentenceSplittersProcessor using the given Core.
     * 
     * @param coreToUse the Core object to use
     */
    public SentenceSplittersProcessor(Core coreToUse)
    {
        super(coreToUse);
    }

    /**
     * @see BotConfigurationElementProcessor#process(Element,
     *      BotsConfigurationFileParser)
     */
    public void process(Element element, BotsConfigurationFileParser parser) throws ProcessorException
    {
        // Does it have an href attribute?
        if (element.hasAttribute(HREF))
        {
            parser.verifyAndProcess(element.getAttribute(HREF));
            return;
        }
        // (otherwise...)
        Bot bot = parser.getCurrentBot();

        List<Element> splitters = XMLKit.getElementChildrenOf(element);

        for (Element splitter : splitters)
        {
            bot.addSentenceSplitter(splitter.getAttribute(VALUE));
        }

        logger.log(Level.INFO, "Loaded " + splitters.size() + " sentence-splitters.");
    }
}