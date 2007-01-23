/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor;

import org.aitools.programd.Core;
import org.aitools.programd.parser.GenericParser;
import org.jdom.Element;

/**
 * A <code>Processor</code> is responsible for processing an element. Subclasses of this base class need only
 * implement the {@link #process} method and set <code>label</code> to the appropriate string.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
abstract public class Processor
{
    /** The Core to use. */
    protected Core _core;

    /*
     * Every Processor should have a String called label. But we don't specify this here, to avoid a situation in which
     * implementors are accused of "hiding" the member in the parent.
     */

    /**
     * Creates a new Processor using the given Core.
     * 
     * @param core the Core object to use
     */
    public Processor(Core core)
    {
        this._core = core;
    }

    /**
     * Processes an element.
     * 
     * @param <P> the type of processor
     * @param element the element to process
     * @param parser the parser calling the processor
     * @return the result of processing the given element
     * @throws ProcessorException if the <code>tag</code> or its contents are invalid
     */
    abstract public <P extends Processor> String process(Element element, GenericParser<P> parser)
            throws ProcessorException;
}
