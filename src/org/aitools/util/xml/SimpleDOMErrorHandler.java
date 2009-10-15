/*
 * aitools utilities
 * Copyright (C) 2006 Noel Bush
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.aitools.util.xml;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.w3c.dom.DOMError;
import org.w3c.dom.DOMErrorHandler;

/**
 *
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class SimpleDOMErrorHandler implements DOMErrorHandler
{
    private Logger _logger;
    
    /**
     * @see org.w3c.dom.DOMErrorHandler#handleError(org.w3c.dom.DOMError)
     */
    public boolean handleError(DOMError e)
    {
        Exception exception = null;
        String message = e.getMessage();
        if (message == null)
        {
            Object relatedException = e.getRelatedException();
            if (relatedException instanceof Exception)
            {
                exception = (Exception)relatedException;
                message = exception.getMessage();
                if (message == null)
                {
                    message = exception.getClass().getName();
                }
            }
        }
        Level level;
        switch (e.getSeverity())
        {
            case DOMError.SEVERITY_WARNING:
                level = Level.WARN;
                break;
            case DOMError.SEVERITY_ERROR:
                level = Level.ERROR;
                break;
            case DOMError.SEVERITY_FATAL_ERROR:
            default:
                level = Level.FATAL;
                break;
        }
        if (exception != null)
        {
            this._logger.log(level, message, exception);
        }
        else
        {
            this._logger.log(level, message);
        }
        return (level.toInt() < Priority.FATAL_INT);
    }

    /**
     * @param logger
     */
    public SimpleDOMErrorHandler(Logger logger)
    {
        this._logger = logger;
    }
}
