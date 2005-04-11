/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.parser;

/**
 * <code>AIMLReaderListener</code> is the interface specification for
 * listeners that can add AIML to the Graphmaster.
 * 
 * @author Kris Drent
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public interface AIMLReaderListener
{
    /**
     * Adds a new category to the {@link org.aitools.programd.graph.Graphmaster} .
     * Usually called by an AIML parser such as
     * {@link org.aitools.programd.parser.TemplateParser TemplateParser} .
     * 
     * @param pattern the <code>pattern</code> portion of a Graphmaster path
     * @param that the <code>that</code> portion of a Graphmaster path
     * @param topic the <code>topic</code> portion of a Graphmaster path
     * @param template the <code>template</code> portion of a Graphmaster path
     */
    public void newCategory(String pattern, String that, String topic, String template);
}