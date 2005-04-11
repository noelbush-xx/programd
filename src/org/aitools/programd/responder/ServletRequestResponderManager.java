/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.responder;

import javax.servlet.ServletRequest;

import org.aitools.programd.server.ServletRequestTransactionEnvelope;

/**
 * A <code>ServletRequestResponderManager</code> manages a
 * <code>ServletRequestResponder</code>.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.5
 */
abstract public interface ServletRequestResponderManager
{
    /**
     * Indicates whether the Responder can handle the given request object.
     * 
     * @param request the request object to test
     * @return whether the given request can be handled by this responder
     */
    abstract public boolean responderHandles(ServletRequest request);

    /**
     * Performs whatever processing is necessary from the enclosed request,
     * modifying the response as needed, and returns the bot response.
     * 
     * @param envelope the envelope to process
     * @return the bot response
     */
    abstract public String getResponseFor(ServletRequestTransactionEnvelope envelope);
}