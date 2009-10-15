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

package org.aitools.util;

/**
 * A Settings object collects settings for any purpose.  It contains getter and setter
 * methods for every setting. Usually the subclasses of Settings will be generated
 * automatically from some other structure, since it's annoying to
 * create and maintain these by hand.
 *
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
abstract public class Settings
{
    /**
     * Initializes the Settings object with values from properties as read, or defaults (if properties are not
     * provided).
     */
    abstract protected void initialize();
}
