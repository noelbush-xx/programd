/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor.loadtime;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;

import org.w3c.dom.Element;

import java.lang.reflect.InvocationTargetException;

import org.aitools.programd.Core;
import org.aitools.programd.bot.Bot;
import org.aitools.programd.bot.BotProcesses;
import org.aitools.programd.listener.Listener;
import org.aitools.programd.listener.ListenerRegistry;
import org.aitools.programd.parser.StartupFileParser;
import org.aitools.programd.processor.ProcessorException;
import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.UserError;

/**
 * The <code>listener</code> element is a container for defining parameters of
 * a listener.
 * 
 * @version 4.2
 * @author Noel Bush
 */
public class ListenerProcessor extends StartupElementProcessor
{
    public static final String label = "listener";

    // Convenience constants.

    /** The string &quot;parameter&quot;. */
    private static final String PARAMETER = "parameter";

    /** The string &quot;type&quot;. */
    private static final String TYPE = "type";

    /** The string &quot;true&quot;. */
    private static final String TRUE = "true";

    /** The string &quot; : &quot;. */

    private static final String SEPARATOR = " : ";

    public ListenerProcessor(Core coreToUse)
    {
        super(coreToUse);
    }
    
    public void process(Element element, StartupFileParser parser)
    {
        // Does it have an href attribute?
        if (element.hasAttribute(HREF))
        {
            String href = element.getAttribute(HREF);
            try
            {
                parser.processResponse(new URI(href));
            } 
            catch (ProcessorException e)
            {
                throw new UserError(e.getMessage());
            }
			catch (URISyntaxException e)
			{
                throw new UserError(e.getMessage());
			}
            return;
        }
        
        // Get type attribute
        String type = element.getAttribute(TYPE);

        // Does its type correspond to a registered listener type?
        Class listenerClass = ListenerRegistry.getSelf().get(type);
        if (listenerClass == null)
        {
            throw new UserError("You specified an unknown listener \"" + type + "\".");
        } 

        // Get enabled attribute
        String enabled = element.getAttribute(ENABLED);

        // Is the enabled attribute true?
        if (!enabled.equals(TRUE))
        {
            return;
        } 

        // Get the current bot.
        Bot bot = parser.getCurrentBot();

        // Count the parameters
        int parameterCount = parser.elementCount(PARAMETER, element.getChildNodes());

        // Set up the parameters for the listener.
        HashMap<String, String> parameters = new HashMap<String, String>(parameterCount);
        for (int index = parameterCount; index > 0; index--)
        {
            Element parameter = (Element)parser.getNode(PARAMETER, element.getChildNodes(), index);
            parameters.put(parameter.getAttribute(NAME), parameter.getAttribute(VALUE));
        }

        // Instantiate a new listener for the bot.
        Listener listener;
        try
        {
            listener = (Listener) listenerClass.getConstructor(new Class[]
                { Core.class, Bot.class, String.class, HashMap.class } ).newInstance(new Object[]
                { this.core, bot,  } );
        } 
        catch (IllegalAccessException e)
        {
            throw new DeveloperError("The constructor for the \"" + type + "\" listener class is inaccessible.");
        } 
        catch (IllegalArgumentException e)
        {
            throw new DeveloperError("The constructor for the \"" + type + "\" listener class is incorrectly specifed.");
        } 
        catch (InstantiationException e)
        {
            throw new DeveloperError("The \"" + type + "\" listener class is abstract.");
        } 
        catch (NoSuchMethodException e)
        {
            throw new DeveloperError("The constructor for the \"" + type + "\" listener class is incorrectly specifed.");
        } 
        catch (InvocationTargetException e)
        {
            throw new DeveloperError("The constructor for the \"" + type + "\" listener class threw an exception.", e);
        } 

        // Start listener
        BotProcesses.start(listener, type + SEPARATOR + bot.getID());

        /*
        if (Settings.showConsole())
        {
            Log.userinfo("Started \"" + type + "\" listener for bot \"" + bot.getID() + "\".", Log.STARTUP);
        }
        */
    } 
}