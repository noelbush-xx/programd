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
 * A <code>Responder</code> is an object that can manipulate the user input
 * and bot responses at several stages of the response production loop. It is
 * specific to a particular output channel (such as HTML, an IM client, etc.).
 * 
 * @author Jon Baer
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @version 4.5
 */
abstract public interface Responder
{
    /**
     * Preprocesses a user input <i>prior to</i> the application of input
     * substitutions and sentence splitting performed by the
     * {@link org.aitools.programd.multiplexor.Multiplexor Multiplexor}, before
     * it (the Multiplexor) obtains bot replies for each sentence.
     * 
     * @param userInput the user input to be preprocessed
     * @return the result of preprocessing the userinput
     */
    String preprocess(String userInput);

    /**
     * Appends a per-sentence reply from the bot to the Responder's total record
     * of the bot response.
     * 
     * @param userInput the input (sentence) from the client that generated the
     *            reply
     * @param botReply the reply from the bot
     * @param appendTo the string to which the client of the
     *            <code>Responder</code> may expect the reply to be appended
     * @return the reply, in whatever fashion the <code>Responder</code> has
     *         decided to modify it. This is likely to be fed in as the
     *         <code>appendTo</code> argument on subsequent calls to this
     *         function for the same input
     */
    String append(String userInput, String botReply, String appendTo);

    /**
     * Postprocesses the complete bot response.
     * 
     * @param finalBotResponse the complete bot response
     * @return the result of postprocessing the message
     */
    String postprocess(String finalBotResponse);
}