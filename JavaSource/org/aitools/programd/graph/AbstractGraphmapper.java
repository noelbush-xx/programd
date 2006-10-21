/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.parsers.SAXParser;

import org.aitools.programd.Bot;
import org.aitools.programd.Core;
import org.aitools.programd.CoreSettings;
import org.aitools.programd.parser.AIMLReader;
import org.aitools.programd.processor.aiml.RandomProcessor;
import org.aitools.util.resource.Filesystem;
import org.aitools.util.resource.URLTools;
import org.aitools.util.runtime.DeveloperError;
import org.aitools.util.xml.XML;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXNotRecognizedException;
import org.xml.sax.SAXNotSupportedException;

/**
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
abstract public class AbstractGraphmapper implements Graphmapper
{
    // Instance variables.

    /** The Core with which this Graphmaster is associated. */
    protected Core _core;

    /** The logger. */
    protected Logger _logger = Logger.getLogger("programd");

    /** The match logger. */
    protected Logger _matchLogger = Logger.getLogger("programd.matching");

    /** A map of loaded file URLs to botids. */
    protected Map<URL, Set<String>> _urlCatalog = new HashMap<URL, Set<String>>();

    /** The merge policy. */
    protected CoreSettings.MergePolicy _mergePolicy;
    
    /** The SAXParser used in loading AIML. */
    private SAXParser _parser;

    /** Whether to note each file loaded. */
    protected boolean noteEachLoad;

    /** The separator string to use with the "append" merge policy. */
    protected String mergeAppendSeparator;

    /** Whether to note each merge. */
    protected boolean noteEachMerge;
    
    /** Whether to use the AIML Watcher. */
    protected boolean useAIMLWatcher;

    /** The AIML namespace URI in use. */
    protected String aimlNamespaceURI;

    /** How frequently to provide a category load count. */
    protected int categoryLoadNotifyInterval;

    /** The total number of categories read. */
    protected int totalCategories = 0;

    /** The total number of path-identical categories that have been encountered. */
    protected int duplicateCategories = 0;

    /** The response timeout. */
    protected int responseTimeout;

    /** A count of Nodemappers. */
    protected int nodemapperCount = 1;

    /**
     * Creates a new Graphmaster, reading settings from the given Core.
     * 
     * @param core the CoreSettings object from which to read settings
     */
    protected AbstractGraphmapper(Core core)
    {
        this._core = core;
        this._parser = XML.getSAXParser(this._core.getCatalog().toExternalForm(), this._logger);
        
        CoreSettings settings = this._core.getSettings();
        this.noteEachLoad = settings.noteEachLoadedFile();
        this._mergePolicy = settings.getMergePolicy();
        this.mergeAppendSeparator = settings.getAppendMergeSeparatorString();
        this.noteEachMerge = settings.noteEachMerge();
        this.useAIMLWatcher = settings.useAIMLWatcher();
        this.responseTimeout = settings.getResponseTimeout();
        this.categoryLoadNotifyInterval = settings.getCategoryLoadNotificationInterval();
        this.aimlNamespaceURI = settings.getAIMLNamespaceURI().toString();
    }
    
    /**
     * @see org.aitools.programd.graph.Graphmapper#load(java.net.URL, java.lang.String)
     */
    public void load(URL path, String botid)
    {
        // Handle paths with wildcards that need to be expanded.
        if (path.getProtocol().equals(Filesystem.FILE))
        {
            String spec = path.getFile();
            if (spec.indexOf('*') != -1)
            {
                List<File> files = null;

                try
                {
                    files = Filesystem.glob(spec);
                }
                catch (FileNotFoundException e)
                {
                    this._logger.warn(e.getMessage());
                }
                if (files != null)
                {
                    for (File file : files)
                    {
                        load(URLTools.contextualize(URLTools.getParent(path), file.getAbsolutePath()), botid);
                    }
                }
                return;
            }
        }

        Bot bot = this._core.getBot(botid);

        // Unload first if already loaded.
        if (bot.getLoadedFilesMap().containsKey(path))
        {
            unload(path, bot);
        }

        // Let the Graphmaster use a shortcut if possible.
        if (this._urlCatalog.containsKey(path))
        {
            if (this._urlCatalog.get(path).contains(botid))
            {
                unload(path, bot);
                doLoad(path, botid);
            }
            if (this._logger.isDebugEnabled())
            {
                this._logger.debug(String.format("Graphmaster has already loaded \"%s\" for some other bot.", path));
            }
            addForBot(path, botid);
        }
        else
        {
            if (this.noteEachLoad)
            {
                this._logger.info("Loading " + URLTools.unescape(path) + "....");
            }
            doLoad(path, botid);
            // Add it to the AIMLWatcher, if active.
            if (this.useAIMLWatcher)
            {
                this._core.getAIMLWatcher().addWatchFile(path);
            }
        }
    }

    protected void doLoad(URL path, String botid)
    {
        try
        {
            AIMLReader reader = new AIMLReader(this, path, this._core.getBot(botid), this.aimlNamespaceURI);
            try
            {
                this._parser.getXMLReader().setProperty("http://xml.org/sax/properties/lexical-handler", reader);
            }
            catch (SAXNotRecognizedException e)
            {
                this._logger.warn(
                        "The XML reader in use does not support lexical handlers -- CDATA will not be handled.", e);
            }
            catch (SAXNotSupportedException e)
            {
                this._logger
                        .warn(
                                "The XML reader in use cannot enable the lexical handler feature -- CDATA will not be handled.",
                                e);
            }
            catch (SAXException e)
            {
                this._logger
                        .warn(
                                "An exception occurred when trying to enable the lexical handler feature on the XML reader -- CDATA will not be handled.",
                                e);
            }
            this._parser.parse(path.toString(), reader);
            Set<String> botids;
            if (this._urlCatalog.containsKey(path))
            {
                botids = this._urlCatalog.get(path);
            }
            else
            {
                botids = new HashSet<String>();
                this._urlCatalog.put(path, botids);
            }
            botids.add(botid);
        }
        catch (IOException e)
        {
            this._logger.warn(String.format("Error reading \"%s\": %s", URLTools.unescape(path), e.getMessage()), e);
        }
        catch (SAXException e)
        {
            this._logger.warn(String.format("Error parsing \"%s\": %s", URLTools.unescape(path), e.getMessage()), e);
        }
    }

    /**
     * Adds the given botid to the &lt;botid&gt; node for all branches
     * associated with the given URL. This should only be called using a URL
     * that <i>has</i> previously been loaded for <i>another</i> bot.
     * 
     * @param path
     * @param botid
     * @throws IllegalArgumentException if the given path has not already been
     *         loaded, or if it has been loaded for the same botid
     */
    abstract public void addForBot(URL path, String botid);

    /**
     * Combines two template content strings into a single template, using a
     * random element so that either original template content string has an
     * equal chance of being processed. The order in which the templates are
     * supplied is important: the first one (<code>existingTemplate</code>)
     * is processed as though it has already been stored in the Graphmaster, and
     * hence might itself be the result of a previous <code>combine()</code>
     * operation. If this is the case, the in-memory representation of the
     * template will have a special attribute indicating this fact, which will
     * be used to &quot;balance&quot; the combine operation.
     * 
     * @param existingTemplate the template with which the new template should
     *        be combined
     * @param newTemplate the template which should be combined with the
     *        existing template
     * @return the combined result
     */
    protected String combineTemplates(String existingTemplate, String newTemplate)
    {
        Document existingDoc;
        Element existingRoot;
        NodeList existingContent;

        Document newDoc;
        NodeList newContent;

        try
        {
            existingDoc = XML.parseAsDocumentFragment(existingTemplate);
            existingRoot = existingDoc.getDocumentElement();
            existingContent = existingRoot.getChildNodes();

            newDoc = XML.parseAsDocumentFragment(newTemplate);
            newContent = newDoc.getDocumentElement().getChildNodes();
        }
        catch (DeveloperError e)
        {
            synchronized (this._logger)
            {
                this._logger.warn("Problem with existing or new template when performing merge combine.");
                this._logger.warn("existing template: " + existingTemplate);
                this._logger.warn("new template: " + newTemplate);
                this._logger.warn("Existing template will be retained as-is.");
            }
            return existingTemplate;
        }

        /*
         * If the existing template has a random element as its root, we need to
         * check whether this was the result of a previous combine.
         */
        Node firstNode = existingContent.item(0);
        if (firstNode instanceof Element)
        {
            Element firstElement = (Element) firstNode;
            if (firstElement.getNodeName().equals(RandomProcessor.label) && firstElement.hasAttribute("synthetic"))
            {
                Element newListItem = existingDoc.createElementNS(this.aimlNamespaceURI, RandomProcessor.LI);
                int newContentSize = newContent.getLength();
                for (int index = 0; index < newContentSize; index++)
                {
                    newListItem.appendChild(existingDoc.importNode(newContent.item(index), true));
                }
                firstElement.appendChild(newListItem);
            }
            return XML.renderXML(existingDoc.getChildNodes(), false);
        }
        Element listItemForExisting = existingDoc.createElementNS(this.aimlNamespaceURI, RandomProcessor.LI);
        int existingContentSize = existingContent.getLength();
        for (int index = 0; index < existingContentSize; index++)
        {
            Node child = existingContent.item(index);
            if (child != null)
            {
                listItemForExisting.appendChild(child.cloneNode(true));
                existingRoot.removeChild(child);
            }
        }

        Element listItemForNew = newDoc.createElementNS(this.aimlNamespaceURI, RandomProcessor.LI);
        int newContentSize = newContent.getLength();
        for (int index = 0; index < newContentSize; index++)
        {
            listItemForNew.appendChild(newContent.item(index).cloneNode(true));
        }

        Element newRandom = existingDoc.createElementNS(this.aimlNamespaceURI, RandomProcessor.label);
        newRandom.setAttribute("synthetic", "yes");
        newRandom.appendChild(listItemForExisting);
        newRandom.appendChild(existingDoc.importNode(listItemForNew, true));

        existingRoot.appendChild(newRandom);

        return XML.renderXML(existingDoc.getChildNodes(), false);
    }

    /**
     * Appends the contents of one template to another.
     * 
     * @param existingTemplate the template to which to append
     * @param newTemplate the template whose content should be appended
     * @return the combined result
     */
    protected String appendTemplate(String existingTemplate, String newTemplate)
    {
        Document existingDoc;
        Element existingRoot;

        Document newDoc;
        NodeList newContent;

        try
        {
            existingDoc = XML.parseAsDocumentFragment(existingTemplate);
            existingRoot = existingDoc.getDocumentElement();

            newDoc = XML.parseAsDocumentFragment(newTemplate);
            newContent = newDoc.getDocumentElement().getChildNodes();
        }
        catch (DeveloperError e)
        {
            synchronized (this._logger)
            {
                this._logger.warn("Problem with existing or new template when performing merge append.");
                this._logger.warn("existing template: " + existingTemplate);
                this._logger.warn("new template: " + newTemplate);
                this._logger.warn("Existing template will be retained as-is.");
            }
            return existingTemplate;
        }

        // Append whatever text is configured to be inserted between the
        // templates.
        if (this.mergeAppendSeparator != null)
        {
            existingRoot.appendChild(existingDoc.createTextNode(this.mergeAppendSeparator));
        }

        int newContentLength = newContent.getLength();
        for (int index = 0; index < newContentLength; index++)
        {
            Node newNode = existingDoc.importNode(newContent.item(index), true);
            existingRoot.appendChild(newNode);
        }
        return XML.renderXML(existingDoc.getChildNodes(), false);
    }

    /**
     * @see org.aitools.programd.graph.Graphmapper#getCategoryCount()
     */
    public int getCategoryCount()
    {
        return this.totalCategories;
    }

    /**
     * @see org.aitools.programd.graph.Graphmapper#getCategoryReport()
     */
    @SuppressWarnings("boxing")
    public String getCategoryReport()
    {
        return String.format("%,d total categories currently loaded.", this.totalCategories);
    }

    /**
     * @see org.aitools.programd.graph.Graphmapper#getDuplicateCategoryCount()
     */
    public int getDuplicateCategoryCount()
    {
        return this.duplicateCategories;
    }

    /**
     * Returns an unmodifiable view of the url-to-botid catalog.
     * 
     * @return an unmodifiable view of the url-to-botid catalog
     */
    public Map<URL, Set<String>> getURLCatalog()
    {
        return Collections.unmodifiableMap(this._urlCatalog);
    }
}
