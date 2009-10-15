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

import org.apache.log4j.Logger;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;

/**
 *
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class SimpleSAXErrorHandler implements ErrorHandler
{
    private Logger _logger;
    
    /**
     * @param logger
     */
    public SimpleSAXErrorHandler(Logger logger)
    {
        this._logger = logger;
    }
    
    /**
     * @see org.xml.sax.ErrorHandler#error(org.xml.sax.SAXParseException)
     */
    public void error(SAXParseException e)
    {
        this._logger.error(e.getMessage());
    }

    /**
     * @see org.xml.sax.ErrorHandler#fatalError(org.xml.sax.SAXParseException)
     */
    public void fatalError(SAXParseException e)
    {
        this._logger.fatal(e.getMessage());
    }

    /**
     * @see org.xml.sax.ErrorHandler#warning(org.xml.sax.SAXParseException)
     */
    public void warning(SAXParseException e)
    {
        this._logger.warn(e.getMessage());
    }
}
