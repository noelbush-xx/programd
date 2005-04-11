/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.responder;

/**
 * Manages simple text responses.
 * 
 * @author Jon Baer
 * @author Thomas Ringate, Pedro Colla
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class TextResponder implements Responder
{
    /**
     * Creates a new <code>TextResponder</code> using encoding for
     * {@link System#in} .
     */
    public TextResponder()
    {
        // Nothing to do.
    }

    /**
     * Returns the input.
     * 
     * @param input the input
     * @return the input
     */
    public String preprocess(String input)
    {
        return input;
    }

    /**
     * Simply appends the response to <code>appendTo</code>.
     * 
     * @param input unused but required by interface
     * @param response the string to append to the <code>appendTo</code>
     *            argument
     * @param appendTo the string to which to append the <code>response</code>
     * @return the result of this appending
     */
    public String append(String input, String response, String appendTo)
    {
        if (appendTo.length() > 0)
        {
            return appendTo + ' ' + response;
        }
        // (otherwise...)
        return appendTo + response;
    }

    /**
     * Simply returns the reply.
     * 
     * @param reply the reply from the bot to be processed
     * @return the reply
     */
    public String postprocess(String reply)
    {
        return reply;
    }
}