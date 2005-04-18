/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.responder;

import org.aitools.programd.server.ServletRequestTransactionEnvelope;

/**
 * Implements a {@link Responder} for a Flash client.
 * 
 * @author Chris Fahey
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @version 4.5
 */
public class FlashResponder extends AbstractXMLResponder implements ServletRequestResponder
{
    private ServletRequestTransactionEnvelope envelope;
    
    /**
     * Creates a new FlashResponder.
     * 
     * @param responsibleManager the manager that is responsible for this
     *            responder
     * @param sourceEnvelope the envelope that generated the request
     */
    public FlashResponder(FlashResponderManager responsibleManager, ServletRequestTransactionEnvelope sourceEnvelope)
    {
        super(responsibleManager, sourceEnvelope.getBotID(), sourceEnvelope.getTemplateName());
        this.envelope = sourceEnvelope;
    }
    
    /**
     * @return the envelope that generated the request
     */
    public ServletRequestTransactionEnvelope getEnvelope()
    {
        return this.envelope;
    }
}