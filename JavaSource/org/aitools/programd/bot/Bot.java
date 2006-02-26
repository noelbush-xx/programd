/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.bot;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.aitools.programd.CoreSettings;
import org.aitools.programd.graph.Nodemapper;
import org.aitools.programd.multiplexor.PredicateInfo;
import org.aitools.programd.multiplexor.PredicateMap;
import org.aitools.programd.processor.Processor;
import org.aitools.programd.util.InputNormalizer;
import org.aitools.programd.util.Substituter;

/**
 * Handles all of the properties of a bot.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @author Eion Robb
 * @since 4.1.5
 * @version 4.5
 */
public class Bot
{
    /** The identifier for the bot. */
    private String id;

    /** The files loaded for the bot. */
    private Map<URL, Set<Nodemapper>> loadedFiles = new HashMap<URL, Set<Nodemapper>>();

    /** The bot's properties. */
    private Map<String, String> properties = Collections.checkedMap(new HashMap<String, String>(),
            String.class, String.class);

    /** The bot's predicate infos. */
    private Map<String, PredicateInfo> predicatesInfo = Collections.checkedMap(
            new HashMap<String, PredicateInfo>(), String.class, PredicateInfo.class);

    /** The bot's processor-specific substitution maps. */
    private Map<Class<? extends Processor>, LinkedHashMap<Pattern, String>> substitutionMaps = new HashMap<Class<? extends Processor>, LinkedHashMap<Pattern, String>>();

    /** The bot's input substitution map. */
    private Map<Pattern, String> inputSubstitutions = Collections.checkedMap(
            new LinkedHashMap<Pattern, String>(), Pattern.class, String.class);

    /** The bot's sentence splitter map. */
    private List<String> sentenceSplitters = Collections.checkedList(new ArrayList<String>(),
            String.class);

    /** Holds cached predicates, keyed by userid. */
    private Map<String, PredicateMap> predicateCache = Collections
            .synchronizedMap(new HashMap<String, PredicateMap>());
    
    /** The page to use for this bot when communicating via the servlet interface. */
    private String servletPage = EMPTY_STRING;
    
    /** The files containing test suites. */
    private List<URL> testSuites;
    
    /** The directory where test reports are to be written. */
    private URL testReportDirectory;
    
    /** The predicate empty default. */
    protected String predicateEmptyDefault;

    /** An empty string. */
    private static final String EMPTY_STRING = "";

    /**
     * Creates a new Bot with the given id. The bot's chat log is also set up.
     * A default servlet page is set.
     * 
     * @param botID the id to use for the new bot
     * @param coreSettings the core settings to use
     */
    public Bot(String botID, CoreSettings coreSettings)
    {
        this.id = botID;
        this.predicateEmptyDefault = coreSettings.getPredicateEmptyDefault();
    }

    /**
     * Returns the id of the bot.
     * 
     * @return the id of the bot
     */
    public String getID()
    {
        return this.id;
    }

    /**
     * Returns a map of the files loaded by this bot.
     * 
     * @return a map of the files loaded by this bot
     */
    public Map<URL, Set<Nodemapper>> getLoadedFilesMap()
    {
        return this.loadedFiles;
    }

    /**
     * Returns whether the bot has loaded the given file(name).
     * 
     * @param filename the filename to check
     * @return whether the bot has loaded the given file(name)
     */
    public boolean hasLoaded(String filename)
    {
        return this.loadedFiles.containsKey(filename);
    }

    /**
     * Adds a nodemapper to the path map.
     * 
     * @param path the path
     * @param nodemapper the mapper for the node to add
     */
    public void addToPathMap(URL path, Nodemapper nodemapper)
    {
        Set<Nodemapper> nodemappers = this.loadedFiles.get(path);
        if (nodemappers == null)
        {
            nodemappers = new HashSet<Nodemapper>();
            this.loadedFiles.put(path, nodemappers);
        }
        nodemappers.add(nodemapper);
    }

    /**
     * Retrieves the value of a named bot property.
     * 
     * @param name the name of the bot property to get
     * @return the value of the bot property
     */
    public String getPropertyValue(String name)
    {
        // Don't bother with empty property names.
        if (name.equals(EMPTY_STRING))
        {
            return this.predicateEmptyDefault;
        }

        // Retrieve the contents of the property.
        String value = this.properties.get(name);
        if (value != null)
        {
            return value;
        }
        // (otherwise...)
        return this.predicateEmptyDefault;
    }

    /**
     * Sets the value of a bot property.
     * 
     * @param name the name of the bot predicate to set
     * @param value the value to set
     */
    public void setPropertyValue(String name, String value)
    {
        // Property name must not be empty.
        if (name.equals(EMPTY_STRING))
        {
            return;
        }

        // Store the property.
        this.properties.put(name, value);
    }

    /**
     * @return the properties
     */
    public Map<String, String> getProperties()
    {
        return this.properties;
    }

    /**
     * Sets the bot's properties.
     * 
     * @param map the properties to set.
     */
    public void setProperties(HashMap<String, String> map)
    {
        this.properties = map;
    }

    /**
     * Registers some information about a predicate in advance. Not required;
     * just used when it is necessary to specify a default value for a predicate
     * and/or specify its type as return-name-when-set.
     * 
     * @param name the name of the predicate
     * @param defaultValue the default value (if any) for the predicate
     * @param returnNameWhenSet whether the predicate should return its name
     *            when set
     */
    public void addPredicateInfo(String name, String defaultValue, boolean returnNameWhenSet)
    {
        PredicateInfo info = new PredicateInfo(name, defaultValue, returnNameWhenSet);
        this.predicatesInfo.put(name, info);
    }

    /**
     * Returns the predicates info map.
     * 
     * @return the predicates info map
     */
    public Map<String, PredicateInfo> getPredicatesInfo()
    {
        return this.predicatesInfo;
    }

    /**
     * Returns the predicate cache.
     * 
     * @return the predicate cache
     */
    public Map<String, PredicateMap> getPredicateCache()
    {
        return this.predicateCache;
    }

    /**
     * Returns the map of predicates for a userid if it is cached, or a new map
     * if it is not cached.
     * 
     * @param userid
     * @return the map of predicates for the given userid
     */
    public PredicateMap predicatesFor(String userid)
    {
        PredicateMap userPredicates;

        // Find out if any predicates for this userid are cached.
        if (!this.predicateCache.containsKey(userid))
        {
            // Create them if not.
            userPredicates = new PredicateMap();
            this.predicateCache.put(userid, userPredicates);
        }
        else
        {
            userPredicates = this.predicateCache.get(userid);
            assert userPredicates != null : "userPredicates is null!";
        }
        return userPredicates;
    }

    /**
     * Adds a substitution to the indicated map. If the map does not yet exist,
     * it is created. The <code>find</code> parameter is stored in uppercase,
     * to do case-insensitive comparisons. The <code>replace</code> parameter
     * is stored as is.
     * 
     * @param processor the processor with which the map is associated
     * @param find the find-string part of the substitution
     * @param replace the replace-string part of the substitution
     */
    public void addSubstitution(Class<? extends Processor> processor, Pattern find, String replace)
    {
        if (!this.substitutionMaps.containsKey(processor))
        {
            this.substitutionMaps.put(processor, new LinkedHashMap<Pattern, String>());
        }
        this.substitutionMaps.get(processor).put(find, replace);
    }

    /**
     * Adds an input substitution. The <code>find</code> parameter is stored
     * in uppercase, to do case-insensitive comparisons. The
     * <code>replace</code> parameter is stored as is.
     * 
     * @param find the find-string part of the substitution
     * @param replace the replace-string part of the substitution
     */
    public void addInputSubstitution(Pattern find, String replace)
    {
        this.inputSubstitutions.put(find, replace);
    }

    /**
     * Adds a sentence splitter to the sentence splitters list.
     * 
     * @param splitter the string on which to divide sentences
     */
    public void addSentenceSplitter(String splitter)
    {
        if (splitter != null)
        {
            this.sentenceSplitters.add(splitter);
        }
    }

    /**
     * @param processor the processor whose substitution map is desired
     * @return the substitution map associated with the given processor class.
     */
    public Map<Pattern, String> getSubstitutionMap(Class<? extends Processor> processor)
    {
        return this.substitutionMaps.get(processor);
    }

    /**
     * @return the sentence splitters
     */
    public List<String> getSentenceSplitters()
    {
        return this.sentenceSplitters;
    }

    /**
     * Splits the given input into sentences.
     * 
     * @param input the input to split
     * @return the sentences of the input
     */
    public List<String> sentenceSplit(String input)
    {
        return InputNormalizer.sentenceSplit(this.sentenceSplitters, input);
    }

    /**
     * Applies input substitutions to the given input
     * 
     * @param input the input to which to apply substitutions
     * @return the processed input
     */
    public String applyInputSubstitutions(String input)
    {
        return Substituter.applySubstitutions(this.inputSubstitutions, input);
    }

    /**
     * @return Returns the testReportDirectory.
     */
    public URL getTestReportDirectory()
    {
        return this.testReportDirectory;
    }

    /**
     * @param url The testReportDirectory to set.
     */
    public void setTestReportDirectory(URL url)
    {
        this.testReportDirectory = url;
    }

    /**
     * @return Returns the list of test suite files.
     */
    public List<URL> getTestSuites()
    {
        return this.testSuites;
    }

    /**
     * @param files The list of test suite files to set
     */
    public void setTestSuitePathspec(List<URL> files)
    {
        this.testSuites = files;
    }
    
    /**
     * @param page the servlet servletPage to user
     */
    public void setServletPage(String page)
    {
        this.servletPage = page;
    }
    
    /**
     * @return the servlet servletPage
     */
    public String getServletPage()
    {
        return this.servletPage;
    }
}