/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.agent.listener;

import java.util.HashMap;

import org.aitools.programd.bot.Bot;
import org.aitools.programd.bot.BotProcess;
import org.aitools.programd.util.UserError;

/**
 * An abstract listener.
 */
public abstract class Listener implements BotProcess
{
    /** The bot for which this listener works. */
    protected Bot bot;

    /** The id of the bot for which this listener works. */
    protected String botID;

    /** The name of the listener. */
    private String name;

    /** The parameters that can be set for this listener. */
    protected HashMap parameters = new HashMap();

    /**
     * Creates a new listener with name <code>name</code>.
     * 
     * @param botToListen
     *            the bot for which this listener works
     * @param listenerName
     *            the name to give the listener
     * @param listenerParameters
     *            the parameters for the listener and their default values
     */
    public Listener(Bot botToListen, String listenerName, String[][] listenerParameters)
    {
        this.bot = botToListen;
        this.botID = botToListen.getID();
        this.name = listenerName;

        for (int index = listenerParameters.length; --index >= 0;)
        {
            this.parameters.put(listenerParameters[index][0], listenerParameters[index][1]);
        } 
    } 

    /**
     * Sets a parameter for the listener.
     * 
     * @param parameterName
     *            the name of the parameter
     * @param parameterValue
     *            the value to set for the parameter
     */
    public void setParameter(String parameterName, String parameterValue)
    {
        if (!this.parameters.containsKey(parameterName))
        {
            throw new UserError("Invalid parameter \"" + parameterName + "\" for listener \"" + this.name + "\"");
        } 
        this.parameters.put(parameterName, parameterValue);
    } 

    /**
     * Checks that the parameters that have been set for the listener are okay.
     * 
     * @return <code>true</code> if parameters are okay, <code>false</code>
     *         if not
     */
    abstract public boolean checkParameters();
}