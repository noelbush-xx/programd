/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.parser;

import java.io.FileNotFoundException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.aitools.programd.Bot;
import org.aitools.programd.Bots;
import org.aitools.programd.Core;
import org.aitools.programd.graph.Graphmapper;
import org.aitools.programd.listener.InvalidListenerParameterException;
import org.aitools.programd.listener.Listener;
import org.aitools.util.ClassUtils;
import org.aitools.util.resource.URLTools;
import org.aitools.util.runtime.DeveloperError;
import org.aitools.util.runtime.UserError;
import org.aitools.util.xml.XML;
import org.apache.log4j.Logger;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 * <code>BotsConfigurationFileParser</code> processes a bots configuration file.
 */
public class BotsConfigurationFileParser
{
    /** The namespace processed by this version of the parser. */
    public static final String NAMESPACE_URI = "http://aitools.org/programd/4.7/bot-configuration";

    /** A JDOM namespace object used in processing. */
    private static final Namespace NS = Namespace.getNamespace(NAMESPACE_URI);

    /** Enum for specifying substitution type. */
    static enum SubstitutionType
    {
        /** an input substitution */
        INPUT,

        /** a gender substitution */
        GENDER,

        /** a person substitution */
        PERSON,

        /** a person2 substitution */
        PERSON2;

        /**
         * @param name
         * @return the type that corresponds with the given name
         */
        public static SubstitutionType get(String name)
        {
            if (name.equals("input"))
            {
                return INPUT;
            }
            else if (name.equals("gender"))
            {
                return GENDER;
            }
            else if (name.equals("person"))
            {
                return PERSON;
            }
            else if (name.equals("person2"))
            {
                return PERSON2;
            }
            throw new IllegalArgumentException(String.format("\"%s\" is not a valid substitution type.", name));
        }
    }

    private Core _core;

    private Logger _logger;

    /**
     * Initializes a <code>BotsConfigurationFileParser</code>.
     * 
     * @param core
     */
    public BotsConfigurationFileParser(Core core)
    {
        this._core = core;
        this._logger = core.getLogger();
    }

    /**
     * Loads the bot config from the given path.
     * 
     * @param path
     */
    @SuppressWarnings("unchecked")
    public void parse(URL path)
    {
        Element root = getDocRoot(path);
        if (root.getName().equals("bots"))
        {
            for (Element bot : (List<Element>) root.getChildren("bot", NS))
            {
                loadBot(bot);
            }
        }
        else if (root.getName().equals("bot"))
        {
            loadBot(root);
        }
        else
        {
            throw new IllegalArgumentException(String.format("Invalid bot config file \"%s\".", path));
        }
    }

    @SuppressWarnings("boxing")
    protected void loadBot(Element element)
    {
        String botid = element.getAttributeValue("id");

        if (XML.getBooleanAttribute(element, "enabled"))
        {
            Bots bots = this._core.getBots();
            if (bots.containsKey(botid))
            {
                this._logger.warn(String.format("Bot \"%s\" has already been configured.", botid));
                return;
            }
            Bot bot = new Bot(botid, this._core.getSettings());

            this._logger.info(String.format("Configuring bot \"%s\".", botid));
            bots.put(botid, bot);

            Graphmapper graphmapper = this._core.getGraphmapper();

            int previousCategoryCount = graphmapper.getCategoryCount();
            int previousDuplicateCount = graphmapper.getDuplicateCategoryCount();

            // Stop the AIMLWatcher while loading.
            if (this._core.getSettings().useAIMLWatcher())
            {
                this._core.getAIMLWatcher().stop();
            }

            // Index the start time before loading.
            long time = new Date().getTime();

            // Load the bot.
            loadProperties(bot, element);
            loadPredicates(bot, element);
            loadSubstitutions(bot, element);
            loadSentenceSplitters(bot, element);
            loadListeners(bot, element);
            setupTesting(bot, element);
            loadAIML(bot, element);

            // Calculate the time used to load all categories.
            time = new Date().getTime() - time;

            // Restart the AIMLWatcher.
            if (this._core.getSettings().useAIMLWatcher())
            {
                this._core.getAIMLWatcher().start();
            }

            this._logger.info(String.format("%,d categories loaded in %.4f seconds.", graphmapper.getCategoryCount()
                    - previousCategoryCount, time / 1000.00));
            this._logger.info(graphmapper.getCategoryReport());

            int dupes = graphmapper.getDuplicateCategoryCount() - previousDuplicateCount;
            if (dupes > 0)
            {
                this._logger
                        .warn(String
                                .format(
                                        "%,d path-identical categories were encountered, and handled according to the %s merge policy.",
                                        dupes, this._core.getSettings().getMergePolicy()));
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void loadProperties(Bot bot, Element element)
    {
        Element properties = element.getChild("properties", NS);
        if (properties != null)
        {
            if (properties.getAttribute("href") != null)
            {
                loadProperties(bot, getDocRoot(XML.contextualize(properties.getAttributeValue("href"), element)));
            }
            else
            {
                for (Element property : (List<Element>) properties.getChildren("property", NS))
                {
                    bot.setPropertyValue(property.getAttributeValue("name"), property.getAttributeValue("value"));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void loadPredicates(Bot bot, Element element)
    {
        Element predicates = element.getChild("predicates", NS);
        if (predicates != null)
        {
            if (predicates.getAttribute("href") != null)
            {
                loadProperties(bot, getDocRoot(XML.contextualize(predicates.getAttributeValue("href"), element)));
            }
            else
            {
                for (Element predicate : (List<Element>) predicates.getChildren("predicate", NS))
                {
                    String name = predicate.getAttributeValue("name");
                    String defaultValue = predicate.getAttributeValue("default");
                    if ("".equals(defaultValue))
                    {
                        defaultValue = null;
                    }
                    String setReturn = predicate.getAttributeValue("set-return");
                    boolean returnNameWhenSet = false;
                    if (setReturn.equals("name"))
                    {
                        returnNameWhenSet = true;
                    }
                    bot.addPredicateInfo(name, defaultValue, returnNameWhenSet);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void loadSubstitutions(Bot bot, Element element)
    {
        Element substitutions = element.getChild("substitutions", NS);
        if (substitutions != null)
        {
            if (substitutions.getAttribute("href") != null)
            {
                loadSubstitutions(bot, getDocRoot(XML.contextualize(substitutions.getAttributeValue("href"), element)));
            }
            else
            {
                for (Element substitutionTypes : (List<Element>) substitutions.getChildren())
                {
                    SubstitutionType type = SubstitutionType.get(substitutionTypes.getName());

                    for (Element substitution : (List<Element>) substitutionTypes.getChildren())
                    {
                        String find = substitution.getAttributeValue("find");

                        // Compile the find pattern.
                        Pattern pattern;
                        try
                        {
                            pattern = Pattern.compile(find, Pattern.CANON_EQ | Pattern.CASE_INSENSITIVE
                                    | Pattern.UNICODE_CASE);
                        }
                        catch (PatternSyntaxException e)
                        {
                            throw new UserError(String.format("Invalid substitution pattern \"%s\".", find), e);
                        }
                        String replace = substitution.getAttributeValue("replace");
                        switch (type)
                        {
                            case INPUT:
                                bot.addInputSubstitution(pattern, replace);
                                break;
                            case GENDER:
                                bot.addSubstitution(org.aitools.programd.processor.aiml.GenderProcessor.class, pattern,
                                        replace);
                                break;
                            case PERSON:
                                bot.addSubstitution(org.aitools.programd.processor.aiml.PersonProcessor.class, pattern,
                                        replace);
                                break;
                            case PERSON2:
                                bot.addSubstitution(org.aitools.programd.processor.aiml.Person2Processor.class,
                                        pattern, replace);
                                break;
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void loadSentenceSplitters(Bot bot, Element element)
    {
        Element splitters = element.getChild("sentence-splitters", NS);
        if (splitters != null)
        {
            if (splitters.getAttribute("href") != null)
            {
                loadSentenceSplitters(bot, getDocRoot(XML.contextualize(splitters.getAttributeValue("href"), element)));
            }
            else
            {
                for (Element splitter : (List<Element>) splitters.getChildren("splitter", NS))
                {
                    bot.addSentenceSplitter(splitter.getAttributeValue("value"));
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void loadListeners(Bot bot, Element element)
    {
        Element listeners = element.getChild("listeners", NS);
        if (listeners != null)
        {
            if (listeners.getAttribute("href") != null)
            {
                loadListeners(bot, getDocRoot(XML.contextualize(listeners.getAttributeValue("href"), element)));
            }
            else
            {
                for (Element listenerElement : (List<Element>) listeners.getChildren("listener", NS))
                {
                    // Get enabled attribute
                    if (!XML.getBooleanAttribute(listenerElement, "enabled"))
                    {
                        return;
                    }

                    // Set up the parameters for the listener.
                    Map<String, String> parameters = new HashMap<String, String>();
                    for (Element parameter : (List<Element>) listenerElement.getChildren())
                    {
                        parameters.put(parameter.getAttributeValue("name"), parameter.getAttributeValue("value"));
                    }

                    // Instantiate a new listener for the bot.
                    String classname = listenerElement.getAttributeValue("class");
                    Listener listener = ClassUtils.getSubclassInstance(Listener.class, classname, "listener",
                            this._core, bot, parameters);

                    // Check listener parameters.
                    try
                    {
                        listener.checkParameters();
                    }
                    catch (InvalidListenerParameterException e)
                    {
                        throw new UserError("Listener is not properly configured!", e);
                    }

                    // Start listener
                    this._core.getManagedProcesses().start(listener, String.format("%s : %s", classname, bot.getID()));

                    this._logger.info(String.format("Started \"%s\" listener for bot \"%s\".", classname, bot.getID()));
                }
            }
        }
    }

    protected void setupTesting(Bot bot, Element element)
    {
        Element testing = element.getChild("testing", NS);
        if (testing != null)
        {
            if (testing.getAttribute("href") != null)
            {
                setupTesting(bot, getDocRoot(XML.contextualize(testing.getAttributeValue("href"), element)));
            }
            else
            {
                URL docURL;
                try
                {
                    docURL = URLTools.createValidURL(element.getDocument().getBaseURI());
                }
                catch (FileNotFoundException e)
                {
                    throw new DeveloperError("Could not get bot config document URL when setting up testing.", e);
                }
                bot.setTestSuitePathspec(URLTools.getURLs(testing.getChildText("test-suite-path", NS), docURL));
                bot.setTestReportDirectory(URLTools.contextualize(docURL, testing.getChildText("report-directory", NS)));
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void loadAIML(Bot bot, Element element)
    {
        for (Element learn : (List<Element>) element.getChildren("learn", NS))
        {
            this._core.load(XML.contextualize(learn.getText(), element), bot.getID());
        }
    }

    /**
     * A common method used in this class for loading a document and getting its root element.
     * 
     * @param path
     * @return the root element of the document at the given path
     */
    protected Element getDocRoot(URL path)
    {
        return XML.getJDOMDocument(path, this._core.getBaseURL(), NAMESPACE_URI, this._core.getXMLCatalog(), this._core.getLogger()).getRootElement();
    }
}
