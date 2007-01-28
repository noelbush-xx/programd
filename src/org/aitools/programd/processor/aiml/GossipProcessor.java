/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor.aiml;

import java.io.FileWriter;
import java.io.IOException;

import org.jdom.Element;

import org.aitools.programd.Core;
import org.aitools.programd.parser.TemplateParser;
import org.aitools.programd.processor.ProcessorException;
import org.aitools.util.runtime.DeveloperError;
import org.aitools.util.resource.Filesystem;
import org.aitools.util.runtime.UserError;

/**
 * Handles a <code><a href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-gossip">gossip</a></code> element.
 * 
 * @author Jon Baer
 * @author Thomas Ringate, Pedro Colla
 */
public class GossipProcessor extends AIMLProcessor
{
    /** The label (as required by the registration scheme). */
    public static final String label = "gossip";

    private static FileWriter gossipFile;

    /**
     * Creates a new GossipProcessor using the given Core.
     * 
     * @param core the Core object to use
     */
    public GossipProcessor(Core core)
    {
        super(core);
    }

    /**
     * @see AIMLProcessor#process(Element, TemplateParser)
     */
    @SuppressWarnings("unchecked")
    @Override
    public String process(Element element, TemplateParser parser) throws ProcessorException
    {
        // Get the gossip.
        String response = parser.evaluate(element.getContent());

        // Initialize the FileWriter if necessary.
        if (gossipFile == null)
        {
            try
            {
                gossipFile = new FileWriter(Filesystem.checkOrCreate(parser.getCore().getSettings().getGossipURL()
                        .getPath(), "gossip file"));
            }
            catch (IOException e)
            {
                throw new UserError(e);
            }
        }

        // Put the gossip in the log.
        try
        {
            gossipFile.append(String.format("<li>%s</li>%n", response));
            gossipFile.flush();
        }
        catch (IOException e)
        {
            throw new DeveloperError("Error trying to write gossip.", e);
        }
        return "";
    }
}
