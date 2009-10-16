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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
import org.aitools.util.Classes;
import org.aitools.util.resource.URLTools;
import org.aitools.util.runtime.DeveloperError;
import org.aitools.util.runtime.UserError;
import org.aitools.util.xml.JDOM;
import org.apache.log4j.Logger;
import org.jdom.DataConversionException;
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
        if (element.getAttribute("href") != null)
        {
            parse(JDOM.contextualize(element.getAttributeValue("href"), element));
        }
        else
        {
            String botid = element.getAttributeValue("id");
            boolean enabled = false;
            try
            {
                enabled = element.getAttribute("enabled").getBooleanValue();
            }
            catch (DataConversionException e)
            {
                assert false : "Schema did not catch invalid valid for \"enabled\" attribute.";
            } 
    
            if (enabled)
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
                loadConfig(bot, element, "properties");
                loadConfig(bot, element, "predicates");
                loadConfig(bot, element, "substitutions");
                loadConfig(bot, element, "sentence-splitters");
                loadConfig(bot, element, "listeners");
                loadConfig(bot, element, "testing");
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
    }

    /**
     * A generic method for loading configuration data.
     * 
     * @param bot the bot object into which to load data
     * @param parent the parent "bot" element
     * @param name the name of the child element from which to load config data; is used to determine method name to call to load config data
     */
    protected void loadConfig(Bot bot, Element parent, String name)
    {
        Element child = parent.getChild(name, NS);
        if (child != null)
        {
            String methodName = String.format("load%s%s", name.substring(0, 1).toUpperCase(), name.substring(1).replaceAll("\\W", ""));
            Method method;
            try
            {
                method = this.getClass().getDeclaredMethod(methodName, new Class[]{Bot.class, Element.class});
            }
            catch (SecurityException e)
            {
                throw new DeveloperError(String.format("Cannot access method %s", methodName), e);
            }
            catch (NoSuchMethodException e)
            {
                throw new DeveloperError(String.format("No such method %s", methodName), e);
            }
            try
            {
                if (child.getAttribute("href") != null)
                {
                    method.invoke(this, bot, getDocRoot(JDOM.contextualize(child.getAttributeValue("href"), child)));
                }
                else
                {
                    method.invoke(this, bot, child);
                }
            }
            catch (InvocationTargetException e)
            {
                throw new DeveloperError(String.format("Could not invoke method %s", methodName), e);
            }
            catch (IllegalAccessException e)
            {
                throw new DeveloperError(String.format("Could not invoke method %s", methodName), e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    protected void loadProperties(Bot bot, Element element)
    {
        for (Element property : (List<Element>) element.getChildren("property", NS))
        {
            bot.setPropertyValue(property.getAttributeValue("name"), property.getAttributeValue("value"));
        }
    }

    @SuppressWarnings("unchecked")
    protected void loadPredicates(Bot bot, Element element)
    {
        for (Element predicate : (List<Element>) element.getChildren("predicate", NS))
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

    @SuppressWarnings("unchecked")
    protected void loadSubstitutions(Bot bot, Element element)
    {
        for (Element substitutionTypes : (List<Element>) element.getChildren())
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

    @SuppressWarnings("unchecked")
    protected void loadSentencesplitters(Bot bot, Element element)
    {
        for (Element splitter : (List<Element>) element.getChildren("splitter", NS))
        {
            bot.addSentenceSplitter(splitter.getAttributeValue("value"));
        }
    }

    @SuppressWarnings("unchecked")
    protected void loadListeners(Bot bot, Element element)
    {
        for (Element listenerElement : (List<Element>) element.getChildren("listener", NS))
        {
            // Enabled?
            boolean enabled = false;
            try
            {
                enabled = listenerElement.getAttribute("enabled").getBooleanValue();
            }
            catch (DataConversionException e1)
            {
                assert false : "Schema did not catch invalid value for \"enabled\" attribute.";
            }
            if (!enabled)
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
            Listener listener = Classes.getSubclassInstance(Listener.class, classname, "listener",
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

    protected void loadTesting(Bot bot, Element element)
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
        bot.setTestSuitePathspec(URLTools.getURLs(element.getChildText("test-suite-path", NS), docURL));
        bot.setTestReportDirectory(URLTools.contextualize(docURL, element.getChildText("report-directory", NS)));
    }

    @SuppressWarnings("unchecked")
    protected void loadAIML(Bot bot, Element element)
    {
        for (Element learn : (List<Element>) element.getChildren("learn", NS))
        {
            this._core.load(JDOM.contextualize(learn.getText(), element), bot.getID());
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
        return JDOM.getDocument(path, this._core.getLogger()).getRootElement();
    }
}
