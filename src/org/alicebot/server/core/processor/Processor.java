/*
    Alicebot Program D
    Copyright (C) 1995-2001, A.L.I.C.E. AI Foundation
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
    USA.
*/

package org.alicebot.server.core.processor;

import org.alicebot.server.core.parser.GenericParser;
import org.alicebot.server.core.parser.XMLNode;

/**
 *  A <code>Processor</code> is responsible for processing an
 *  element. Subclasses of this base class need
 *  only implement the {@link #processElement} method and set
 *  <code>label</code> to the appropriate string.
 *
 *  @since  4.1.3
 *  @author Noel Bush
 */
abstract public class Processor
{
    /** The label of a tag that this processor should process. */
    public static final String label = null;


    // Convenience constants.

    /** An empty string, for convenience. */
    protected static final String EMPTY_STRING = "";

    /** The string &quot;name&quot;, for convenience. */
    protected static final String NAME = "name";

    /** The string &quot;value&quot;, for convenience. */
    protected static final String VALUE = "value";

    /** The string &quot;name=&quot;, for convenience. */
    protected static final String NAME_EQUALS = "name=";

    /** The string &quot;value=&quot;, for convenience. */
    protected static final String VALUE_EQUALS = "value=";

    /** The string &quot;id&quot;, for convenience. */
    protected static final String ID = "id";

    /** The string &quot;enabled&quot;. */
    protected static final String ENABLED = "enabled";


    /**
     *  Processes an element.
     *
     *  @param level    the starting level in the XML trie
     *  @param tag      the element to process
     *  @param parser   the parser calling the processor
     *
     *  @return the result of processing the given element
     *
     *  @throws ProcessorException if the <code>tag</code> or its contents are invalid
     */
    abstract public String process(int level, XMLNode tag, GenericParser parser) throws ProcessorException;
}
