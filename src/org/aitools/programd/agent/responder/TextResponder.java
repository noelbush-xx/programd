/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.agent.responder;

import org.aitools.programd.agent.logger.DatabaseLogger;
import org.aitools.programd.agent.logger.XMLLogger;
import org.aitools.programd.util.Globals;

/**
 * Logs output of all chat.
 * 
 * @author Jon Baer
 * @author Thomas Ringate, Pedro Colla
 * @author Noel Bush
 */
public class TextResponder implements Responder
{
    // Class variables.

    /** A space, for convenience. */
    protected static final String SPACE = " ";

    /** Whether to log the chat to the database. */
    private static final boolean LOG_CHAT_TO_DATABASE = Boolean.valueOf(
            Globals.getProperty("programd.logging.to-database.chat", "false")).booleanValue();

    /** Whether to log the chat to xml text files. */
    private static final boolean LOG_CHAT_TO_XML = Boolean.valueOf(
            Globals.getProperty("programd.logging.to-xml.chat", "true")).booleanValue();

    /**
     * Creates a new <code>TextResponder</code> using encoding for
     * {@link System#in} .
     */
    public TextResponder()
    {
        // Nothing to do.
    } 

    /**
     * Returns the input, converted into Unicode from the request encoding.
     * 
     * @param input
     *            the input
     * @param hostname
     *            not used
     * @return the input
     */
    public String preprocess(String input, String hostname)
    {
        return input;
    } 

    /**
     * Simply appends the response to <code>appendTo</code>.
     */
    public String append(String input, String response, String appendTo)
    {
        if (appendTo.length() > 0)
        {
            return appendTo + SPACE + response;
        } 
        // (otherwise...)
        return appendTo + response;
    } 

    public void log(String input, String reply, String hostname, String userid, String botid)
    {
        if (LOG_CHAT_TO_DATABASE)
        {
            DatabaseLogger.log(input, reply, hostname, userid, botid);
        } 
        if (LOG_CHAT_TO_XML)
        {
            XMLLogger.log(input, reply, hostname, userid, botid);
        } 
    } 

    /**
     * Simply returns the reply.
     * 
     * @param reply
     *            the reply from the bot to be processed
     * @return the reply
     */
    public String postprocess(String reply)
    {
        return reply;
    } 
}