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

/**
 * This exception is thrown when a {@link org.aitools.programd.server.ServletRequestResponderManagerRegistry ServletRequestResponderManagerRegistry}
 * does not have a {@link Responder} that can handle a given request.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.5
 */
public class NoResponderHandlesThisException extends Exception
{
    /** The request that could not be handled. */
    private ServletRequest request;

    /**
     * Creates a new exception with the given request.
     * 
     * @param unhandledRequest the request that could not be handled.
     */
    public NoResponderHandlesThisException(ServletRequest unhandledRequest)
    {
        super();
        this.request = unhandledRequest;
    }

    /**
     * @return a message giving information about the request that could not be
     *         handled
     */
    public String getMessage()
    {
        return "Request could not be handled: " + this.request.toString();
    }

    /**
     * @return the request that could not be handled
     */
    public ServletRequest getUnhandledRequest()
    {
        return this.request;
    }
}
