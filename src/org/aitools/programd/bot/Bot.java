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
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

import org.aitools.programd.graph.Nodemapper;
import org.aitools.programd.logging.SimpleChatLogFormatter;
import org.aitools.programd.logging.XMLChatLogFormatter;
import org.aitools.programd.multiplexor.PredicateInfo;
import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.FileManager;
import org.aitools.programd.util.InputNormalizer;
import org.aitools.programd.util.Substituter;
import org.aitools.programd.util.UserError;

/**
 * Handles all of the properties of a bot.
 * 
 * @author Noel Bush
 * @author Eion Robb
 * @since 4.1.5
 * @version 4.5
 */
public class Bot
{
    /** The identifier for the bot. */
    private String id;

    /** The files loaded for the bot. */
    private HashMap<URL, HashSet<Nodemapper>> loadedFiles = new HashMap<URL, HashSet<Nodemapper>>();

    /** The bot's properties. */
    private Map<String, String> properties = Collections.checkedMap(new HashMap<String, String>(), String.class, String.class);

    /** The bot's predicate infos. */
    private Map<String, PredicateInfo> predicatesInfo = Collections.checkedMap(new HashMap<String, PredicateInfo>(), String.class, PredicateInfo.class);

    /** The bot's input substitutions map. */
    private Map<String, String> inputSubstitutions = Collections.checkedMap(new HashMap<String, String>(), String.class, String.class);

    /** The bot's person substitutions map. */
    private Map<String, String> personSubstitutions = Collections.checkedMap(new HashMap<String, String>(), String.class, String.class);

    /** The bot's person2 substitutions map. */
    private Map<String, String> person2Substitutions = Collections.checkedMap(new HashMap<String, String>(), String.class, String.class);

    /** The bot's gender substitutions map. */
    private Map<String, String> genderSubstitutions = Collections.checkedMap(new HashMap<String, String>(), String.class, String.class);

    /** The bot's sentence splitter map. */
    private List<String> sentenceSplitters = Collections.checkedList(new ArrayList<String>(), String.class);

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
     * @param botID the id to use for the new bot
     * @param predicateEmptyDefaultToUse the default value for empty predicates for the new bot
     * @param chatlogDirectory the directory in which to store chat logs for the bot
     */
    public Bot(String botID, String predicateEmptyDefaultToUse, String chatlogDirectory)
    {
        this.id = botID;
        this.predicateEmptyDefault = predicateEmptyDefaultToUse;

        this.logger = Logger.getLogger("programd.chat." + this.id);
        this.logger.setUseParentHandlers(false);
        FileManager.checkOrCreateDirectory(chatlogDirectory, "chat log directory");
        
        // Set up regular logging of chat.
        FileHandler chatLogFileHandler;
        try
        {
            chatLogFileHandler = new FileHandler(chatlogDirectory + File.separator + this.id + "-%g.log", 1048576, 10, true);
        }
        catch (IOException e)
        {
            throw new UserError("Could not create XML chat log for bot \"" + this.id + "\" in \"" + chatlogDirectory + "\"!", e);
        }
        chatLogFileHandler.setFormatter(new SimpleChatLogFormatter());
        this.logger.addHandler(chatLogFileHandler);

        // Set up XML logging of chat.
        FileHandler xmlChatLogFileHandler;
        try
        {
            xmlChatLogFileHandler = new FileHandler(chatlogDirectory + File.separator + this.id + "%g.xml", 1048576, 10, true);
        }
        catch (IOException e)
        {
            throw new UserError("Could not create XML chat log for bot \"" + this.id + "\" in \"" + chatlogDirectory + "\"!", e);
        }
        xmlChatLogFileHandler.setFormatter(new XMLChatLogFormatter());
        this.logger.addHandler(xmlChatLogFileHandler);
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
    public HashMap<URL, HashSet<Nodemapper>> getLoadedFilesMap()
    {
        return this.loadedFiles;
    } 

    /**
     * Returns whether the bot has loaded the given file(name).
     * @param filename the filename to check
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

    /**
     * @return the properties
     */
    public Map getProperties()
    {
        return this.properties;
    } 

    /**
     * Sets the bot's properties.
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
    public Map getPredicatesInfo()
    {
        return this.predicatesInfo;
    } 

    /**
     * Returns the predicate cache.
     * 
     * @return the predicate cache
     */
    public Map<String, Map<String, Object>> getPredicateCache()
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
                throw new DeveloperError("userPredicates is null.", new NullPointerException());
            } 
        } 
        return userPredicates;
    } 

    /**
     * Adds the given input substitution.
     * @param find  the find-string part of the substitution
     * @param replace   the replace-string part of the substitution
     */
    public void addInputSubstitution(String find, String replace)
    {
        addSubstitution(this.inputSubstitutions, find, replace);
    } 

    /**
     * Adds the given gender substitution.
     * @param find  the find-string part of the substitution
     * @param replace   the replace-string part of the substitution
     */
    public void addGenderSubstitution(String find, String replace)
    {
        addSubstitution(this.genderSubstitutions, find, replace);
    } 

    /**
     * Adds the given person substitution.
     * @param find  the find-string part of the substitution
     * @param replace   the replace-string part of the substitution
     */
    public void addPersonSubstitution(String find, String replace)
    {
        addSubstitution(this.personSubstitutions, find, replace);
    } 

    /**
     * Adds the given person2 substitution.
     * @param find  the find-string part of the substitution
     * @param replace   the replace-string part of the substitution
     */
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
    private void addSubstitution(Map<String, String> substitutionMap, String find, String replace)
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

    /**
     * @return the input substitution map
     */
    public Map<String, String> getInputSubstitutionsMap()
    {
        return this.inputSubstitutions;
    } 

    /**
     * @return the gender substitution map
     */
    public Map<String, String> getGenderSubstitutionsMap()
    {
        return this.genderSubstitutions;
    } 

    /**
     * @return the person substitution map
     */
    public Map<String, String> getPersonSubstitutionsMap()
    {
        return this.personSubstitutions;
    } 

    /**
     * @return the person2 substitution map
     */
    public Map<String, String> getPerson2SubstitutionsMap()
    {
        return this.person2Substitutions;
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
     * @param input the input to split
     * @return the sentences of the input
     */
    public List<String> sentenceSplit(String input)
    {
        return InputNormalizer.sentenceSplit(this.sentenceSplitters, input);
    } 

    /**
     * Applies input substitutions to the given input
     * @param input the input to which to apply substitutions
     * @return the processed input
     */
    public String applyInputSubstitutions(String input)
    {
        return Substituter.applySubstitutions(this.inputSubstitutions, input);
    } 
}