/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.responder;

import java.io.File;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Document;

import org.aitools.programd.Core;
import org.aitools.programd.bot.Bots;
import org.aitools.programd.processor.ProcessorException;
import org.aitools.programd.responder.xml.XMLTemplateParser;
import org.aitools.programd.responder.xml.XMLTemplateProcessorRegistry;
import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.FileManager;
import org.aitools.programd.util.SuffixFilenameFilter;
import org.aitools.programd.util.UserError;
import org.aitools.programd.util.URITools;

/**
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.5
 */
abstract public class AbstractXMLResponderManager extends ResponderManager
{
    /** The Bots object from which to obtain bot info. */
    protected Bots bots;

    /** The default template name. */
    protected String defaultTemplateName;

    /** Map of template names to parsed templates. */
    protected HashMap<String, Document> templates = new HashMap<String, Document>();

    /** The logger. */
    protected Logger logger = Logger.getLogger("programd");

    /** The template processor registry. */
    protected XMLTemplateProcessorRegistry templateProcessorRegistry;
    
    /** Whether to convert HTML line breakers to line breaks in the output. */
    private boolean convertHTMLLineBreakers;
    
    /** Whether to strip markup from the output. */
    private boolean stripMarkup;

    /**
     * @param coreToUse the core to use when getting some values
     */
    public AbstractXMLResponderManager(Core coreToUse)
    {
        super(coreToUse);
        this.bots = this.core.getBots();
        this.templateProcessorRegistry = new XMLTemplateProcessorRegistry();
    }

    /**
     * Sets the default template name to the given value.
     * 
     * @param name the name of the default template
     */
    public void setDefaultTemplateName(String name)
    {
        this.defaultTemplateName = name;
    }

    /**
     * Scans a given directory for templates that match a given filename filter
     * and returns a map of template names (filenames minus suffixes) to
     * filenames.
     * 
     * @param directoryName the name of the directory to scan
     * @param filter the filename filter to use
     */
    protected void registerTemplates(String directoryName, SuffixFilenameFilter filter)
    {
        File directory = FileManager.getFile(directoryName);
        XMLTemplateParser parser = new XMLTemplateParser(this.templateProcessorRegistry, this.core, this.convertHTMLLineBreakers, this.stripMarkup);
        if (directory.isDirectory())
        {
            String[] templateFilenames = directory.list(filter);
            int templateCount = templateFilenames.length;
            if (templateCount > 0)
            {
                for (int index = templateCount; --index >= 0;)
                {
                    String templateFilename = templateFilenames[index];
                    try
                    {
                        this.templates.put(templateFilename.substring(0, templateFilename.lastIndexOf('.')), parser.parse(URITools
                                .createValidURL(directoryName + File.separator + templateFilename)));
                    }
                    catch (ProcessorException e)
                    {
                        throw new UserError("Could not parse template \"" + templateFilename + "\".", e);
                    }
                }
            }
        }
    }

    /**
     * @return the default template name
     */
    public String getDefaultTemplateName()
    {
        return this.defaultTemplateName;
    }

    /**
     * @param name the name of the template to get
     * @return the template corresponding to the given name
     */
    public Document getTemplate(String name)
    {
        if (this.templates.containsKey(name))
        {
            return this.templates.get(name);
        }
        this.logger.log(Level.WARNING, "Could not find template name \"" + name + "\"; searching for default template.");
        if (this.templates.containsKey(this.defaultTemplateName))
        {
            return this.templates.get(this.defaultTemplateName);
        }
        throw new DeveloperError("Invalid template name \"" + name + "\", and default template also could not be found!", new NullPointerException());
    }

    /**
     * @return the Bots object
     */
    public Bots getBots()
    {
        return this.bots;
    }

    /**
     * @return the XMLTemplateProcessor registry
     */
    public XMLTemplateProcessorRegistry getProcessorRegistry()
    {
        return this.templateProcessorRegistry;
    }
    
    /**
     * @return the Core in use
     */
    public Core getCore()
    {
        return this.core;
    }
    
    /** 
     * @return whether to convert HTML line breakers to line breaks in the output.
     * */
    public boolean convertHTMLLineBreakers()
    {
        return this.convertHTMLLineBreakers;
    }
    
    /**
     * @return whether to strip markup from the output.
     */
    public boolean stripMarkup()
    {
        return this.stripMarkup;
    }

    /** 
     * @param setting whether to convert HTML line breakers to line breaks in the output.
     * */
    public void setConvertHTMLLineBreakers(boolean setting)
    {
        this.convertHTMLLineBreakers = setting;
    }
    
    /**
     * @param setting whether to strip markup from the output.
     */
    public void setStripMarkup(boolean setting)
    {
        this.stripMarkup = setting;
    }

}