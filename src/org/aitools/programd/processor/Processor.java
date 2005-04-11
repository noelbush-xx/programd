/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor;

import org.w3c.dom.Element;

import org.aitools.programd.Core;
import org.aitools.programd.parser.GenericParser;

/**
 * A <code>Processor</code> is responsible for processing an element.
 * Subclasses of this base class need only implement the {@link #process} method
 * and set <code>label</code> to the appropriate string.
 * 
 * @since 4.1.3
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
abstract public class Processor
{
    /** The Core to use. */
    protected Core core;

    /*
     * Every Processor should have a String called label. But we don't specify
     * this here, to avoid a situation in which implementors are accused of
     * "hiding" the member in the parent.
     */

    // Convenience constants.
    /** An empty string, for convenience. */
    protected static final String EMPTY_STRING = "";

    /** The string &quot;{@value}&quot;, for convenience. */
    protected static final String NAME = "name";

    /** The string &quot;{@value}&quot;, for convenience. */
    protected static final String VALUE = "value";

    /** The string &quot;{@value}&quot;, for convenience. */
    protected static final String NAME_EQUALS = "name=";

    /** The string &quot;{@value}&quot;, for convenience. */
    protected static final String VALUE_EQUALS = "value=";

    /** The string &quot;{@value}&quot;, for convenience. */
    protected static final String ID = "id";

    /** The string &quot;{@value}&quot;. */
    protected static final String ENABLED = "enabled";

    /**
     * Creates a new Processor using the given Core.
     * 
     * @param coreToUse the Core object to use
     */
    public Processor(Core coreToUse)
    {
        this.core = coreToUse;
    }

    /**
     * Processes an element.
     * 
     * @param element the element to process
     * @param parser the parser calling the processor
     * @return the result of processing the given element
     * @throws ProcessorException if the <code>tag</code> or its contents are
     *             invalid
     */
    abstract public String process(Element element, GenericParser parser) throws ProcessorException;
}