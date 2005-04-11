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
 * A Responder for interfacing with the AOL Instant Messenger protocol.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class AIMResponder implements Responder
{
    /**
     * Creates a new AIMResponder.
     */
    public AIMResponder()
    {
        // Nothing to do.
    }

    /**
     * @see org.aitools.programd.responder.Responder#preprocess(java.lang.String)
     */
    public String preprocess(String input)
    {
        return input;
    }

    /**
     * @see org.aitools.programd.responder.Responder#append(java.lang.String,
     *      java.lang.String, java.lang.String)
     */
    public String append(String input, String response, String appendTo)
    {
        return appendTo + response;
    }

    /**
     * @see org.aitools.programd.responder.Responder#postprocess(java.lang.String)
     */
    public String postprocess(String reply)
    {
        return reply.substring(0, Math.min(1024, reply.length()));
    }
}