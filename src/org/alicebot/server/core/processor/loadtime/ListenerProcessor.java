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


package org.alicebot.server.core.processor.loadtime;

import java.lang.reflect.InvocationTargetException;

import org.alicebot.server.core.Bot;
import org.alicebot.server.core.BotProcesses;
import org.alicebot.server.core.Globals;
import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.parser.StartupFileParser;
import org.alicebot.server.core.parser.XMLNode;
import org.alicebot.server.core.processor.ProcessorException;
import org.alicebot.server.core.util.DeveloperError;
import org.alicebot.server.core.util.Trace;
import org.alicebot.server.core.util.Toolkit;
import org.alicebot.server.core.util.UserError;
import org.alicebot.server.net.listener.AliceChatListener;
import org.alicebot.server.net.listener.AliceChatListenerRegistry;


/**
 *  The <code>listener</code> element is a container
 *  for defining parameters of a listener.
 */
public class ListenerProcessor extends StartupElementProcessor
{
    public static final String label = "listener";


    // Convenience constants.

    /** The string &quot;parameter&quot;. */
    private static final String PARAMETER = "parameter";
    
    /** The string &quot;type&quot;. */
    private static final String TYPE = "type";
    
    /** The string &quot;enabled&quot;. */
    private static final String ENABLED = "enabled";

    /** The string &quot;name&quot;. */
    private static final String NAME = "name";

    /** The string &quot;value&quot;. */
    private static final String VALUE = "value";
    
    /** The string &quot;true&quot;. */
    private static final String TRUE = "true";
    
    /** The string &quot; : &quot;. */
    private static final String SEPARATOR = " : ";


    public String process(int level, XMLNode tag, StartupFileParser parser) throws InvalidStartupElementException
    {
        // Does it have an href attribute?
        String href = getHref(tag);

        if (href.length() > 0)
        {
            try
            {
                return parser.processResponse(Toolkit.getFileContents(href));
            }
            catch (ProcessorException e)
            {
                throw new UserError(e);
            }
        }
        else
        {
            // Does it have a type attribute?
            String type = Toolkit.getAttributeValue(TYPE, tag.XMLAttr);
            if (type == null)
            {
                throw new UserError("You did not specify a type for a listener.");
            }
            
            // Does its type correspond to a registered listener type?
            Class listenerClass = (Class)AliceChatListenerRegistry.getSelf().get(type);
            if (listenerClass == null)
            {
                throw new UserError("You specified an unknown listener \"" + type + "\".");
            }
            
            // Does it have an enabled attribute?
            String enabled = Toolkit.getAttributeValue(ENABLED, tag.XMLAttr);
            if (enabled == null)
            {
                throw new UserError("<listener type=\"" + type + "\"> is missing an enabled attribute.");
            }
            
            // Is the enabled attribute true?
            if (!enabled.equals(TRUE))
            {
                return EMPTY_STRING;
            }
            
            // Get the current bot.
            Bot bot = parser.getCurrentBot();
    
            // Instantiate a new listener for the bot.
            AliceChatListener listener;
            try
            {
                listener = (AliceChatListener)listenerClass.
                                getConstructor(new Class[] {Bot.class}).
                                    newInstance(new Object[] {bot});
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
                        
            // Count the parameters
            int parameterCount = parser.nodeCount(PARAMETER, tag.XMLChild, true);
    
            // Set each parameter for the listener.
            for (int index = parameterCount; index > 0; index--)
            {
                XMLNode node = parser.getNode(PARAMETER, tag.XMLChild, index);
                if (node.XMLType == XMLNode.EMPTY)
                {
                    String name = Toolkit.getAttributeValue(NAME, node.XMLAttr);
                    String value = Toolkit.getAttributeValue(VALUE, node.XMLAttr);
                    if (name != null && value != null)
                    {
                        listener.setParameter(name, value);
                    }
                }
                else
                {
                    throw new InvalidStartupElementException("<" + node.XMLData + "/> cannot have content!");
                }
            }
            
            // Check the parameters for the listener.
            if (!listener.checkParameters())
            {
                Log.userinfo("Listener \"" + type + "\" is incorrectly configured; will not be started.", Log.STARTUP);
                return EMPTY_STRING;
            }   
            
            // Start listener
            BotProcesses.start(listener, type + SEPARATOR + bot.getID());
            
            if (Globals.showConsole())
            {
                Log.userinfo("Started \"" + type + "\" listener for bot \"" + bot.getID() + "\".", Log.STARTUP);
            }
        }
        return EMPTY_STRING;
    }
}

