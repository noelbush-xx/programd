/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.bot;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import org.aitools.programd.graph.Nodemapper;
import org.aitools.programd.logging.XMLChatLogFormatter;
import org.aitools.programd.multiplexor.PredicateInfo;
import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.InputNormalizer;
import org.aitools.programd.util.Substituter;
import org.aitools.programd.util.UserError;

/**
 * Handles all of the properties of a bot.
 * 
 * @author Noel Bush
 * @author Eion Robb
 * @since 4.1.5
 * @version 4.2
 */
public class Bot
{
    /** The identifier for the bot. */
    private String id;

    /** The files loaded for the bot. */
    private HashMap<Object, HashSet<Nodemapper>> loadedFiles = new HashMap<Object, HashSet<Nodemapper>>();

    /** The bot's properties. */
    private HashMap<String, String> properties = new HashMap<String, String>();

    /** The bot's predicate infos. */
    private HashMap<String, PredicateInfo> predicatesInfo = new HashMap<String, PredicateInfo>();

    /** The bot's input substitutions map. */
    private HashMap<String, String> inputSubstitutions = new HashMap<String, String>();

    /** The bot's person substitutions map. */
    private HashMap<String, String> personSubstitutions = new HashMap<String, String>();

    /** The bot's person2 substitutions map. */
    private HashMap<String, String> person2Substitutions = new HashMap<String, String>();

    /** The bot's gender substitutions map. */
    private HashMap<String, String> genderSubstitutions = new HashMap<String, String>();

    /** The bot's sentence splitter map. */
    private ArrayList<String> sentenceSplitters = new ArrayList<String>();

    /** Holds cached predicates, keyed by userid. */
    private Map<String, Map<String, Object>> predicateCache = Collections.synchronizedMap(new HashMap<String, Map<String, Object>>());

    /** The predicate empty default. */
    protected String predicateEmptyDefault;

    /** The XML chat logger for this bot. */
    private Logger logger;

    /** An empty string. */
    private static final String EMPTY_STRING = "";


    /**
     * Creates a new Bot with the given id. The bot's chat log is also set up.
     */
    public Bot(String botID, String predicateEmptyDefaultToUse, String chatlogDirectory)
    {
        this.id = botID;
        this.predicateEmptyDefault = predicateEmptyDefaultToUse;

        this.logger = Logger.getLogger("programd.chat." + this.id);
        FileHandler chatLogFileHandler;
        try
        {
            chatLogFileHandler = new FileHandler(chatlogDirectory + File.separator + this.id + File.separator + "chat.xml", 1024, 10);
        }
        catch (IOException e)
        {
            throw new UserError("Could not create XML chat log for bot \"" + this.id + "\" in \"" + chatlogDirectory + "\"!");
        }
        chatLogFileHandler.setFormatter(new XMLChatLogFormatter());
        this.logger.addHandler(chatLogFileHandler);
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
     * @return the XML chat logger for this bot
     */
    public Logger getLogger()
    {
        return this.logger;
    }

    /**
     * Returns a map of the files loaded by this bot.
     * 
     * @return a map of the files loaded by this bot
     */
    public HashMap<Object, HashSet<Nodemapper>> getLoadedFilesMap()
    {
        return this.loadedFiles;
    } 

    /**
     * Returns whether the bot has loaded the given file(name).
     * 
     * @return whether the bot has loaded the given file(name)
     */
    public boolean hasLoaded(String filename)
    {
        return this.loadedFiles.containsKey(filename);
    } 

    /**
     * Adds a nodemapper to the filename map.
     * 
     * @param filename
     *            the filename
     * @param nodemapper
     *            the mapper for the node to add
     */
    public void addToFilenameMap(String filename, Nodemapper nodemapper)
    {
        HashSet<Nodemapper> nodemappers = this.loadedFiles.get(filename);
        if (nodemappers != null)
        {
            nodemappers.add(nodemapper);
        } 
    } 

    /**
     * Retrieves the value of a named bot property.
     * 
     * @param name
     *            the name of the bot property to get
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
     * @param name
     *            the name of the bot predicate to set
     * @param value
     *            the value to set
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

    public Map getProperties()
    {
        return this.properties;
    } 

    public void setProperties(HashMap<String, String> map)
    {
        this.properties = map;
    } 

    /**
     * Registers some information about a predicate in advance. Not required;
     * just used when it is necessary to specify a default value for a predicate
     * and/or specify its type as return-name-when-set.
     * 
     * @param name
     *            the name of the predicate
     * @param defaultValue
     *            the default value (if any) for the predicate
     * @param returnNameWhenSet
     *            whether the predicate should return its name when set
     */
    public void addPredicateInfo(String name, String defaultValue, boolean returnNameWhenSet)
    {
        PredicateInfo info = new PredicateInfo();
        info.name = name;
        info.defaultValue = defaultValue;
        info.returnNameWhenSet = returnNameWhenSet;
        this.predicatesInfo.put(name, info);
    } 

    /**
     * Returns the predicates info map.
     * 
     * @return the predicates info map
     */
    public HashMap getPredicatesInfo()
    {
        return this.predicatesInfo;
    } 

    /**
     * Returns the predicate cache.
     * 
     * @return the predicate cache
     */
    public Map getPredicateCache()
    {
        return this.predicateCache;
    } 

    /**
     * Returns the map of predicates for a userid if it is cached, or a new map
     * if it is not cached.
     * 
     * @param userid
     */
    public Map<String, Object> predicatesFor(String userid)
    {
        Map<String, Object> userPredicates;

        // Find out if any predicates for this userid are cached.
        if (!this.predicateCache.containsKey(userid))
        {
            // Create them if not.
            userPredicates = Collections.synchronizedMap(new HashMap<String, Object>());
            this.predicateCache.put(userid, userPredicates);
        } 
        else
        {
            userPredicates = this.predicateCache.get(userid);
            if (userPredicates == null)
            {
                // This should never happen!
                throw new DeveloperError("userPredicates is null.");
            } 
        } 
        return userPredicates;
    } 

    public void addInputSubstitution(String find, String replace)
    {
        addSubstitution(this.inputSubstitutions, find, replace);
    } 

    public void addGenderSubstitution(String find, String replace)
    {
        addSubstitution(this.genderSubstitutions, find, replace);
    } 

    public void addPersonSubstitution(String find, String replace)
    {
        addSubstitution(this.personSubstitutions, find, replace);
    } 

    public void addPerson2Substitution(String find, String replace)
    {
        addSubstitution(this.person2Substitutions, find, replace);
    } 

    /**
     * Adds a substitution to the substitutions map. The <code>find</code>
     * parameter is stored in uppercase, to do case-insensitive comparisons. The
     * <code>replace</code> parameter is stored as is.
     * 
     * @param substitutionMap
     * @param find
     *            the string to find in the input
     * @param replace
     *            the string with which to replace the found string
     */
    private void addSubstitution(HashMap<String, String> substitutionMap, String find, String replace)
    {
        if (find != null && replace != null)
        {
            substitutionMap.put(find.toUpperCase(), replace);
        } 
    } 

    /**
     * Adds a sentence splitter to the sentence splitters list.
     * 
     * @param splitter
     *            the string on which to divide sentences
     */
    public void addSentenceSplitter(String splitter)
    {
        if (splitter != null)
        {
            this.sentenceSplitters.add(splitter);
        } 
    } 

    public HashMap<String, String> getInputSubstitutionsMap()
    {
        return this.inputSubstitutions;
    } 

    public HashMap<String, String> getGenderSubstitutionsMap()
    {
        return this.genderSubstitutions;
    } 

    public HashMap<String, String> getPersonSubstitutionsMap()
    {
        return this.personSubstitutions;
    } 

    public HashMap<String, String> getPerson2SubstitutionsMap()
    {
        return this.person2Substitutions;
    } 

    public ArrayList<String> getSentenceSplitters()
    {
        return this.sentenceSplitters;
    } 

    public ArrayList<String> sentenceSplit(String input)
    {
        return InputNormalizer.sentenceSplit(this.sentenceSplitters, input);
    } 

    public String applyInputSubstitutions(String input)
    {
        return Substituter.applySubstitutions(this.inputSubstitutions, input);
    } 
}