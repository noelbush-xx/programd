/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package org.aitools.programd;

import java.net.URL;
import java.net.MalformedURLException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Document;
import org.aitools.util.resource.URITools;
import org.aitools.util.resource.URLTools;
import org.aitools.util.runtime.DeveloperError;
import org.aitools.util.runtime.UserError;
import org.aitools.util.xml.Loader;
import org.aitools.util.xml.NamespaceContextImpl;

/**
 * Automatically generated at 2006-07-09T23:31:23.554-05:00.
 */
public class XMLCoreSettings extends CoreSettings
{
    /** The path to the settings file. */
    private URL _path;

    /** A base URL for resolving relative URLs. */
    private URL _base;
    
    /**
     * Creates a <code>XMLCoreSettings</code> with the XML-formatted settings file
     * located at the given path.
     *
     * @param path the path to the settings file
     * @param base the URL against which to resolve relative URLs
     */
    public XMLCoreSettings(URL path, URL base)
    {
        this._path = path;
        this._base = base;
        initialize();
    }
    
    /**
     * Initializes the Settings with values from the XML settings file.
     */
    @SuppressWarnings("boxing")
    @Override
    protected void initialize()
    {
        final String CONFIG_NS_URI = "http://aitools.org/programd/4.7/programd-configuration";
        Loader loader = new Loader(this._base, CONFIG_NS_URI);
        Document document = loader.parse(this._path);
        XPath xpath = XPathFactory.newInstance().newXPath();
        NamespaceContextImpl ns = new NamespaceContextImpl();
        ns.add(CONFIG_NS_URI, "d");
        xpath.setNamespaceContext(ns);

        try
        {
            setAIMLNamespaceURI(URITools.createValidURI(xpath.evaluate("/d:programd/d:aiml.namespace-uri", document), false));
            try
            {
                setBotConfigURL(URLTools.createValidURL(xpath.evaluate("/d:programd/d:paths/d:bot-config", document), this._path, false));
            }
            catch (MalformedURLException e)
            {
                throw new UserError("Error in settings.", e);
            }
            try
            {
                setPluginConfigURL(URLTools.createValidURL(xpath.evaluate("/d:programd/d:paths/d:plugin-config", document), this._path, false));
            }
            catch (MalformedURLException e)
            {
                throw new UserError("Error in settings.", e);
            }
            try
            {
                setGossipURL(URLTools.createValidURL(xpath.evaluate("/d:programd/d:paths/d:gossip", document), this._path, false));
            }
            catch (MalformedURLException e)
            {
                throw new UserError("Error in settings.", e);
            }
            setPredicateEmptyDefault(xpath.evaluate("/d:programd/d:predicates/d:empty-default", document));
            setClientNamePredicate(xpath.evaluate("/d:programd/d:predicates/d:client-name-predicate", document));
            setBotNameProperty(xpath.evaluate("/d:programd/d:predicates/d:bot-name-property", document));
            setPredicateFlushPeriod(Integer.parseInt(xpath.evaluate("/d:programd/d:predicates/d:predicate-flush-period", document)));
            setMultiplexorImplementation(xpath.evaluate("/d:programd/d:multiplexor/d:implementation", document));
            try
            {
                setFfmDirectory(URLTools.createValidURL(xpath.evaluate("/d:programd/d:multiplexor/d:ffm-dir", document), this._path, false));
            }
            catch (MalformedURLException e)
            {
                throw new UserError("Error in settings.", e);
            }
            setDatabaseURL(xpath.evaluate("/d:programd/d:database/d:url", document));
            setDatabaseDriver(xpath.evaluate("/d:programd/d:database/d:driver", document));
            setDatabaseMaximumConnections(Integer.parseInt(xpath.evaluate("/d:programd/d:database/d:maximum-connections", document)));
            setDatabaseUsername(xpath.evaluate("/d:programd/d:database/d:username", document));
            setDatabasePassword(xpath.evaluate("/d:programd/d:database/d:password", document));

            String mergePolicyValue = xpath.evaluate("/d:programd/d:merge/d:policy", document);
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

            setNoteEachMerge(Boolean.parseBoolean(xpath.evaluate("/d:programd/d:merge/d:note-each", document)));
            setAppendMergeSeparatorString(xpath.evaluate("/d:programd/d:merge/d:append-policy.separator-string", document));
            setResponseTimeout(Integer.parseInt(xpath.evaluate("/d:programd/d:exceptions/d:response-timeout", document)));
            setInfiniteLoopInput(xpath.evaluate("/d:programd/d:exceptions/d:infinite-loop-input", document));
            setPrintStackTraceOnUncaughtExceptions(Boolean.parseBoolean(xpath.evaluate("/d:programd/d:exceptions/d:on-uncaught-exceptions.print-stack-trace", document)));
            setHeartPulseRate(Integer.parseInt(xpath.evaluate("/d:programd/d:heart/d:pulserate", document)));
            setHeartEnabled(Boolean.parseBoolean(xpath.evaluate("/d:programd/d:heart/@enabled", document)));
            setAIMLWatcherTimer(Integer.parseInt(xpath.evaluate("/d:programd/d:watchers/d:AIML/d:timer", document)));
            setUseAIMLWatcher(Boolean.parseBoolean(xpath.evaluate("/d:programd/d:watchers/d:AIML/@enabled", document)));
            try
            {
                setAIMLSchemaLocation(URLTools.createValidURL(xpath.evaluate("/d:programd/d:schema-locations/d:AIML", document), this._path, false));
            }
            catch (MalformedURLException e)
            {
                throw new UserError("Error in settings.", e);
            }
            try
            {
                setBotConfigSchemaLocation(URLTools.createValidURL(xpath.evaluate("/d:programd/d:schema-locations/d:bot-configuration", document), this._path, false));
            }
            catch (MalformedURLException e)
            {
                throw new UserError("Error in settings.", e);
            }
            try
            {
                setPluginSchemaLocation(URLTools.createValidURL(xpath.evaluate("/d:programd/d:schema-locations/d:plugins", document), this._path, false));
            }
            catch (MalformedURLException e)
            {
                throw new UserError("Error in settings.", e);
            }
            try
            {
                setTestCaseSchemaLocation(URLTools.createValidURL(xpath.evaluate("/d:programd/d:schema-locations/d:test-cases", document), this._path, false));
            }
            catch (MalformedURLException e)
            {
                throw new UserError("Error in settings.", e);
            }
            setJavascriptInterpreterClassname(xpath.evaluate("/d:programd/d:interpreters/d:javascript/d:interpreter-classname", document));
            setAllowJavaScript(Boolean.parseBoolean(xpath.evaluate("/d:programd/d:interpreters/d:javascript/@allowed", document)));
            try
            {
                setSystemInterpreterDirectory(URLTools.createValidURL(xpath.evaluate("/d:programd/d:interpreters/d:system/d:directory", document), this._path, false));
            }
            catch (MalformedURLException e)
            {
                throw new UserError("Error in settings.", e);
            }
            setSystemInterpreterPrefix(xpath.evaluate("/d:programd/d:interpreters/d:system/d:prefix", document));
            setAllowOSAccess(Boolean.parseBoolean(xpath.evaluate("/d:programd/d:interpreters/d:system/@allowed", document)));
            setCategoryLoadNotificationInterval(Integer.parseInt(xpath.evaluate("/d:programd/d:loading/d:category-load-notification-interval", document)));
            setNoteEachLoadedFile(Boolean.parseBoolean(xpath.evaluate("/d:programd/d:loading/d:note-each-loaded-file", document)));
            setExitImmediatelyOnStartup(Boolean.parseBoolean(xpath.evaluate("/d:programd/d:loading/d:exit-immediately-on-startup", document)));
            setConnectString(xpath.evaluate("/d:programd/d:connect-string", document));

            String randomStrategyValue = xpath.evaluate("/d:programd/d:random-strategy", document);
            if (randomStrategyValue.equals("pure-random"))
            {
                setRandomStrategy(RandomStrategy.PURE_RANDOM);
            }
            else if (randomStrategyValue.equals("non-repeating"))
            {
                setRandomStrategy(RandomStrategy.NON_REPEATING);
            }

            setNodemapperImplementation(xpath.evaluate("/d:programd/d:nodemapper.implementation", document));
            setUseShell(Boolean.parseBoolean(xpath.evaluate("/d:programd/d:use-shell", document)));
        }
        catch (XPathExpressionException e)
        {
            throw new DeveloperError("Error in generated XPath expression for retrieving setting.", e);
        }
    }
}
