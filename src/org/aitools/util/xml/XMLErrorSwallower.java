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

import org.xmlresolver.sunjaxp.impl.XMLErrorReporter;
import org.xmlresolver.sunjaxp.xni.XMLLocator;
import org.xmlresolver.sunjaxp.xni.XNIException;

/**
 * This is out of laziness -- an error reporter that just
 * swallows errors.  Used only in cases where we really will
 * never care about getting errors.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class XMLErrorSwallower extends XMLErrorReporter
{
    /**
     * @see org.xmlresolver.sunjaxp.impl.XMLErrorReporter#reportError(org.xmlresolver.sunjaxp.xni.XMLLocator, java.lang.String, java.lang.String, java.lang.Object[], short)
     */
    @Override
    @SuppressWarnings("unused")
    public void reportError(XMLLocator location,
            String domain, String key, Object[] arguments, 
            short severity) throws XNIException
    {
        // Do absolutely nothing!
    }

}
