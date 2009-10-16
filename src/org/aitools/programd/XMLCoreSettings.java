/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package org.aitools.programd;

import java.io.FileNotFoundException;
import java.net.URL;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.IllegalNameException;
import org.jdom.JDOMException;
import org.jdom.xpath.XPath;
import org.aitools.util.resource.URITools;
import org.aitools.util.resource.URLTools;
import org.aitools.util.runtime.DeveloperError;
import org.aitools.util.runtime.UserError;
import org.aitools.util.xml.JDOM;

/**
 * Automatically generated at 2007-03-10T23:39:17.013-05:00.
 */
public class XMLCoreSettings extends CoreSettings
{
    /** The path to the settings file. */
    private URL _path;

    /** A logger. */
    private Logger _logger;
    
    /**
     * Creates a <code>XMLCoreSettings</code> with the XML-formatted settings file
     * located at the given path.
     *
     * @param path the path to the settings file
     * @param logger
     */
    public XMLCoreSettings(URL path, Logger logger)
    {
        this._path = path;
        this._logger = logger;
        initialize();
    }
    
    /**
     * Creates a new XPath object with the given path,
     * with the Program D configuration namespace associated with
     * prefix "d".
     *
     * @param path
     * @return XPath object
     */
    protected static XPath getXPath(String path)
    {
        final String CONFIG_NS_URI = "http://aitools.org/programd/4.7/programd-configuration";
        XPath xpath;

        try
        {
            xpath = XPath.newInstance(path);
        }
        catch (JDOMException e)
        {
            throw new UserError("Error in settings.", e);
        }
        try
        {
            xpath.addNamespace("d", CONFIG_NS_URI);
        }
        catch (IllegalNameException e)
        {
            throw new DeveloperError(String.format("Illegal namespace \"%s\".", CONFIG_NS_URI), e);
        }
        return xpath;
    }

    /**
     * Returns the string value of the given XPath expression, evaluated from the given node.
     *
     * @param path
     * @param context
     * @return value
     */
    protected String getXPathStringValue(String path, Object context)
    {
        XPath xpath = getXPath(path);
        try
        {
            return xpath.valueOf(context);
        }
        catch (JDOMException e)
        {
            throw new UserError("Failed to evaluate XPath expression.", e);
        }
    }

    /**
     * Returns the number value of the given XPath expression, evaluated from the given node.
     *
     * @param path
     * @param context
     * @return value
     */
    protected Number getXPathNumberValue(String path, Object context)
    {
        XPath xpath = getXPath(path);
        try
        {
            return xpath.numberValueOf(context);
        }
        catch (JDOMException e)
        {
            throw new UserError("Failed to evaluate XPath expression.", e);
        }
    }

    /**
     * Initializes the Settings with values from the XML settings file.
     */
    @Override
    protected void initialize()
    {
        Document document = JDOM.getDocument(this._path, this._logger);

        // Initialize AIMLNamespaceURI.
        try
        {
            setAIMLNamespaceURI(URITools.createValidURI(getXPathStringValue("/d:programd/d:aiml.namespace-uri", document), false));
        }
        catch (FileNotFoundException e)
        {
            throw new UserError("Error in settings.", e);
        }

        // Initialize botConfigURL.
        try
        {
            setBotConfigURL(URLTools.createValidURL(getXPathStringValue("/d:programd/d:paths/d:bot-config", document), this._path, false));
        }
        catch (FileNotFoundException e)
        {
            throw new UserError("Error in settings.", e);
        }

        // Initialize pluginConfigURL.
        try
        {
            setPluginConfigURL(URLTools.createValidURL(getXPathStringValue("/d:programd/d:paths/d:plugin-config", document), this._path, false));
        }
        catch (FileNotFoundException e)
        {
            throw new UserError("Error in settings.", e);
        }

        // Initialize gossipURL.
        try
        {
            setGossipURL(URLTools.createValidURL(getXPathStringValue("/d:programd/d:paths/d:gossip", document), this._path, false));
        }
        catch (FileNotFoundException e)
        {
            throw new UserError("Error in settings.", e);
        }

        // Initialize predicateEmptyDefault.
        setPredicateEmptyDefault(getXPathStringValue("/d:programd/d:predicates/d:empty-default", document));

        // Initialize clientNamePredicate.
        setClientNamePredicate(getXPathStringValue("/d:programd/d:predicates/d:client-name-predicate", document));

        // Initialize botNameProperty.
        setBotNameProperty(getXPathStringValue("/d:programd/d:predicates/d:bot-name-property", document));

        // Initialize predicateFlushPeriod.
        setPredicateFlushPeriod(getXPathNumberValue("/d:programd/d:predicates/d:predicate-flush-period", document).intValue());

        // Initialize predicateManagerImplementation.
        setPredicateManagerImplementation(getXPathStringValue("/d:programd/d:predicate-manager/d:implementation", document));

        // Initialize ffpmDirectory.
        try
        {
            setFfpmDirectory(URLTools.createValidURL(getXPathStringValue("/d:programd/d:predicate-manager/d:ffpm-dir", document), this._path, false));
        }
        catch (FileNotFoundException e)
        {
            throw new UserError("Error in settings.", e);
        }

        // Initialize databaseURL.
        setDatabaseURL(getXPathStringValue("/d:programd/d:database/d:url", document));

        // Initialize databaseDriver.
        setDatabaseDriver(getXPathStringValue("/d:programd/d:database/d:driver", document));

        // Initialize databaseMaximumConnections.
        setDatabaseMaximumConnections(getXPathNumberValue("/d:programd/d:database/d:maximum-connections", document).intValue());

        // Initialize databaseUsername.
        setDatabaseUsername(getXPathStringValue("/d:programd/d:database/d:username", document));

        // Initialize databasePassword.
        setDatabasePassword(getXPathStringValue("/d:programd/d:database/d:password", document));

        // Initialize mergePolicy.

        String mergePolicyValue = getXPathStringValue("/d:programd/d:merge/d:policy", document);
        if (mergePolicyValue.equals("skip"))
        {
            setMergePolicy(MergePolicy.SKIP);
        }
        else if (mergePolicyValue.equals("overwrite"))
        {
            setMergePolicy(MergePolicy.OVERWRITE);
        }
        else if (mergePolicyValue.equals("append"))
        {
            setMergePolicy(MergePolicy.APPEND);
        }
        else if (mergePolicyValue.equals("combine"))
        {
            setMergePolicy(MergePolicy.COMBINE);
        }

        // Initialize noteEachMerge.
        setNoteEachMerge(Boolean.parseBoolean(getXPathStringValue("/d:programd/d:merge/d:note-each", document)));

        // Initialize appendMergeSeparatorString.
        setAppendMergeSeparatorString(getXPathStringValue("/d:programd/d:merge/d:append-policy.separator-string", document));

        // Initialize responseTimeout.
        setResponseTimeout(getXPathNumberValue("/d:programd/d:exceptions/d:response-timeout", document).intValue());

        // Initialize infiniteLoopInput.
        setInfiniteLoopInput(getXPathStringValue("/d:programd/d:exceptions/d:infinite-loop-input", document));

        // Initialize printStackTraceOnUncaughtExceptions.
        setPrintStackTraceOnUncaughtExceptions(Boolean.parseBoolean(getXPathStringValue("/d:programd/d:exceptions/d:on-uncaught-exceptions.print-stack-trace", document)));

        // Initialize pulseImplementation.
        setPulseImplementation(getXPathStringValue("/d:programd/d:heart/d:pulse.implementation", document));

        // Initialize heartPulseRate.
        setHeartPulseRate(getXPathNumberValue("/d:programd/d:heart/d:pulse.rate", document).intValue());

        // Initialize heartEnabled.
        setHeartEnabled(Boolean.parseBoolean(getXPathStringValue("/d:programd/d:heart/@enabled", document)));

        // Initialize AIMLWatcherTimer.
        setAIMLWatcherTimer(getXPathNumberValue("/d:programd/d:watchers/d:AIML/d:timer", document).intValue());

        // Initialize useAIMLWatcher.
        setUseAIMLWatcher(Boolean.parseBoolean(getXPathStringValue("/d:programd/d:watchers/d:AIML/@enabled", document)));

        // Initialize javascriptInterpreterClassname.
        setJavascriptInterpreterClassname(getXPathStringValue("/d:programd/d:interpreters/d:javascript/d:interpreter-classname", document));

        // Initialize allowJavaScript.
        setAllowJavaScript(Boolean.parseBoolean(getXPathStringValue("/d:programd/d:interpreters/d:javascript/@allowed", document)));

        // Initialize systemInterpreterDirectory.
        try
        {
            setSystemInterpreterDirectory(URLTools.createValidURL(getXPathStringValue("/d:programd/d:interpreters/d:system/d:directory", document), this._path, false));
        }
        catch (FileNotFoundException e)
        {
            throw new UserError("Error in settings.", e);
        }

        // Initialize systemInterpreterPrefix.
        setSystemInterpreterPrefix(getXPathStringValue("/d:programd/d:interpreters/d:system/d:prefix", document));

        // Initialize allowOSAccess.
        setAllowOSAccess(Boolean.parseBoolean(getXPathStringValue("/d:programd/d:interpreters/d:system/@allowed", document)));

        // Initialize categoryLoadNotificationInterval.
        setCategoryLoadNotificationInterval(getXPathNumberValue("/d:programd/d:loading/d:category-load-notification-interval", document).intValue());

        // Initialize noteEachLoadedFile.
        setNoteEachLoadedFile(Boolean.parseBoolean(getXPathStringValue("/d:programd/d:loading/d:note-each-loaded-file", document)));

        // Initialize exitImmediatelyOnStartup.
        setExitImmediatelyOnStartup(Boolean.parseBoolean(getXPathStringValue("/d:programd/d:loading/d:exit-immediately-on-startup", document)));

        // Initialize connectString.
        setConnectString(getXPathStringValue("/d:programd/d:connect-string", document));

        // Initialize randomStrategy.

        String randomStrategyValue = getXPathStringValue("/d:programd/d:random-strategy", document);
        if (randomStrategyValue.equals("pure-random"))
        {
            setRandomStrategy(RandomStrategy.PURE_RANDOM);
        }
        else if (randomStrategyValue.equals("non-repeating"))
        {
            setRandomStrategy(RandomStrategy.NON_REPEATING);
        }

        // Initialize graphmapperImplementation.
        setGraphmapperImplementation(getXPathStringValue("/d:programd/d:graphmapper.implementation", document));

        // Initialize nodemapperImplementation.
        setNodemapperImplementation(getXPathStringValue("/d:programd/d:nodemapper.implementation", document));

        // Initialize useShell.
        setUseShell(Boolean.parseBoolean(getXPathStringValue("/d:programd/d:use-shell", document)));

        // Initialize xmlParserUseEntityResolver2.
        setXmlParserUseEntityResolver2(Boolean.parseBoolean(getXPathStringValue("/d:programd/d:xml-parsing/d:use-entity-resolver2", document)));

        // Initialize xmlParserUseValidation.
        setXmlParserUseValidation(Boolean.parseBoolean(getXPathStringValue("/d:programd/d:xml-parsing/d:use-validation", document)));

        // Initialize xmlParserUseSchemaValidation.
        setXmlParserUseSchemaValidation(Boolean.parseBoolean(getXPathStringValue("/d:programd/d:xml-parsing/d:use-schema-validation", document)));

        // Initialize xmlParserHonourAllSchemaLocations.
        setXmlParserHonourAllSchemaLocations(Boolean.parseBoolean(getXPathStringValue("/d:programd/d:xml-parsing/d:honour-all-schema-locations", document)));

        // Initialize xmlParserUseXInclude.
        setXmlParserUseXInclude(Boolean.parseBoolean(getXPathStringValue("/d:programd/d:xml-parsing/d:use-xinclude", document)));

        // Initialize xmlParserValidateAnnotations.
        setXmlParserValidateAnnotations(Boolean.parseBoolean(getXPathStringValue("/d:programd/d:xml-parsing/d:validate-annotations", document)));
    }
}
