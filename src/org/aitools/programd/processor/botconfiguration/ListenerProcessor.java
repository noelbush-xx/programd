/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor.botconfiguration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.w3c.dom.Element;

import org.aitools.programd.Core;
import org.aitools.programd.bot.Bot;
import org.aitools.programd.listener.Listener;
import org.aitools.programd.listener.InvalidListenerParameterException;
import org.aitools.programd.parser.BotsConfigurationFileParser;
import org.aitools.programd.processor.ProcessorException;
import org.aitools.programd.util.ClassUtils;
import org.aitools.programd.util.UserError;
import org.aitools.programd.util.XMLKit;

/**
 * The <code>listener</code> element is a container for defining parameters of
 * a listener.
 * 
 * @version 4.5
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class ListenerProcessor extends BotConfigurationElementProcessor
{
    /** The label (as required by the registration scheme). */
    public static final String label = "listener";

    // Convenience constants.

    /** The string &quot;{@value}&quot;. */
    private static final String CLASS = "class";

    /** The string &quot;{@value}&quot;. */
    private static final String TRUE = "true";

    /** The string &quot;{@value}&quot;. */
    private static final String SEPARATOR = " : ";

    /**
     * Creates a new ListenerProcessor using the given Core.
     * 
     * @param coreToUse the Core object to use
     */
    public ListenerProcessor(Core coreToUse)
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
        // otherwise...

        // Get enabled attribute
        String enabled = element.getAttribute(ENABLED);

        // Is the enabled attribute true?
        if (!enabled.equals(TRUE))
        {
            return;
        }

        // Get the current bot.
        Bot bot = parser.getCurrentBot();

        // Set up the parameters for the listener.
        List<Element> parameterElements = XMLKit.getElementChildrenOf(element);
        
        Map<String, String> parameters = new HashMap<String, String>(parameterElements.size());
        
        for (Element parameter : parameterElements)
        {
            parameters.put(parameter.getAttribute(NAME), parameter.getAttribute(VALUE));
        }

        // Instantiate a new listener for the bot.
        String classname = element.getAttribute(CLASS);
        Listener listener = ClassUtils.getSubclassInstance(Listener.class, classname, "listener", this.core, bot, parameters);
        
        // Check listener parameters.
        try
        {
            listener.checkParameters();
        }
        catch (InvalidListenerParameterException e)
        {
            throw new UserError("Listener is not properly configured!", e);
        }

        // Start listener
        this.core.getManagedProcesses().start(listener, classname + SEPARATOR + bot.getID());

        logger.log(Level.INFO, "Started \"" + classname + "\" listener for bot \"" + bot.getID() + "\".");
    }
}