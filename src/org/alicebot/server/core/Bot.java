/*
    Alicebot Program D
    Copyright (C) 1995-2001, A.L.I.C.E. AI Foundation
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
    USA.
*/

package org.alicebot.server.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.alicebot.server.core.logging.XMLLog;
import org.alicebot.server.core.node.Nodemapper;
import org.alicebot.server.core.util.DeveloperError;
import org.alicebot.server.core.util.InputNormalizer;
import org.alicebot.server.core.util.Substituter;
import org.alicebot.server.core.util.XMLResourceSpec;


/**
 *  Describes all of the properties of a bot.
 *
 *  @author Noel Bush
 *  @since 4.1.5
 */
public class Bot
{
    /** The identifier for the bot. */
    private String id;

    /** The files loaded for the bot. */
    private HashMap loadedFiles = new HashMap();

    /** The bot's properties. */
    private HashMap properties = new HashMap();

    /** The bot's predicate infos. */
    private HashMap predicatesInfo = new HashMap();

    /** The bot's input substitutions map. */
    private HashMap inputSubstitutions = new HashMap();

    /** The bot's person substitutions map. */
    private HashMap personSubstitutions = new HashMap();

    /** The bot's person2 substitutions map. */
    private HashMap person2Substitutions = new HashMap();

    /** The bot's gender substitutions map. */
    private HashMap genderSubstitutions = new HashMap();

    /** The bot's sentence splitter map. */
    private ArrayList sentenceSplitters = new ArrayList();

    /** Holds cached predicates, keyed by userid. */
    private Map predicateCache = Collections.synchronizedMap(new HashMap());

    /** The predicate empty default. */
    protected String PREDICATE_EMPTY_DEFAULT = Globals.getPredicateEmptyDefault();
    
    /** An empty string. */
    private static final String EMPTY_STRING = "";

    /** The resource spec for logs from this bot. */
    protected XMLResourceSpec chatlogSpec;


    public Bot(String id)
    {
        this.id = id;
        this.chatlogSpec = XMLLog.getChatlogSpecClone();
        this.chatlogSpec.path
        	= Globals.getProperty("programd.logging.xml.chat.log-directory", "./logs")
        	  + File.separator + id + File.separator + "chat.xml";
    }


    public String getID()
    {
        return id;
    }


    public HashMap getLoadedFilesMap()
    {
        return loadedFiles;
    }


    /**
     *  Adds a nodemapper to the filename map.
     *
     *  @param filename     the filename
     *  @param nodemapper   the mapper for the node to add
     */
    public void addToFilenameMap(String filename, Nodemapper nodemapper)
    {
        HashSet nodemappers = (HashSet)loadedFiles.get(filename);
        if (nodemappers != null)
        {
            nodemappers.add(nodemapper);
        }
    }


    /**
     *  Retrieves the value of a named bot property.
     *
     *  @param property    the name of the bot property to get
     *
     *  @return the value of the bot property
     */
    public String getPropertyValue(String name)
    {
        // Don't bother with empty property names.
        if (name.equals(EMPTY_STRING))
        {
            return PREDICATE_EMPTY_DEFAULT;
        }

        // Retrieve the contents of the property.
        String value = (String)properties.get(name);
        if (value != null)
        {
            return value;
        }
        else
        {
            return PREDICATE_EMPTY_DEFAULT;
        }
    }


    /**
     *  Sets the value of a bot property.
     *
     *  @param property    the name of the bot predicate to set
     */
    public void setPropertyValue(String name, String value)
    {
        // Property name must not be empty.
        if (name.equals(EMPTY_STRING))
        {
           return;
        }

        // Store the property.
        properties.put(name, new String(value));
    }


    /**
     *  Registers some information about a predicate in advance.
     *  Not required; just used when it is necessary to specify a
     *  default value for a predicate and/or specify its type
     *  as return-name-when-set.
     *
     *  @param name                 the name of the predicate
     *  @param defaultValue         the default value (if any) for the predicate
     *  @param returnNameWhenSet    whether the predicate should return its name when set
     *  @param botid                the bot id for whom to register the predicate this way (not yet used)
     */
    public void addPredicateInfo(String name, String defaultValue, boolean returnNameWhenSet)
    {
        PredicateInfo info = new PredicateInfo();
        info.name = name;
        info.defaultValue = defaultValue;
        info.returnNameWhenSet = returnNameWhenSet;
        predicatesInfo.put(name, info);
    }


    /**
     *  Returns the predicates info map.
     *
     *  @return the predicates info map
     */
    public HashMap getPredicatesInfo()
    {
        return predicatesInfo;
    }


    /**
     *  Returns the predicate cache.
     *
     *  @return the predicate cache
     */
    public Map getPredicateCache()
    {
        return predicateCache;
    }


    /**
     *  Returns the map of predicates for a userid if it is cached,
     *  or a new map if it is not cached.
     *
     *  @param userid
     */
    public Map predicatesFor(String userid)
    {
        Map userPredicates;

        // Find out if any predicates for this userid are cached.
        if (!predicateCache.containsKey(userid))
        {
            // Create them if not.
            userPredicates = Collections.synchronizedMap(new HashMap());
            predicateCache.put(userid, userPredicates);
        }
        else
        {
            userPredicates = (Map)predicateCache.get(userid);
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
        addSubstitution(inputSubstitutions, find, replace);
    }


    public void addGenderSubstitution(String find, String replace)
    {
        addSubstitution(genderSubstitutions, find, replace);
    }


    public void addPersonSubstitution(String find, String replace)
    {
        addSubstitution(personSubstitutions, find, replace);
    }


    public void addPerson2Substitution(String find, String replace)
    {
        addSubstitution(person2Substitutions, find, replace);
    }


    /**
     *  Adds a substitution to the substitutions map.  The
     *  <code>find</code> parameter is stored in uppercase,
     *  to do case-insensitive comparisons.  The <code>replace</code>
     *  parameter is stored as is.
     *
     *  @param substitutionMap
     *  @param find     the string to find in the input
     *  @param replace  the string with which to replace the found string
     */
    private void addSubstitution(HashMap substitutionMap, String find, String replace)
    {
        if (find != null && replace != null)
        {
            substitutionMap.put(find.toUpperCase(), replace);
        }
    }


    /**
     *  Adds a sentence splitter to the sentence splitters list.
     *
     *  @param splitter the string on which to divide sentences
     */
    public void addSentenceSplitter(String splitter)
    {
        if (splitter != null)
        {
            sentenceSplitters.add(splitter);
        }
    }

    
    public HashMap getInputSubstitutionsMap()
    {
        return inputSubstitutions;
    }


    public HashMap getGenderSubstitutionsMap()
    {
        return genderSubstitutions;
    }


    public HashMap getPersonSubstitutionsMap()
    {
        return personSubstitutions;
    }


    public HashMap getPerson2SubstitutionsMap()
    {
        return person2Substitutions;
    }


    public ArrayList getSentenceSplitters()
    {
        return sentenceSplitters;
    }


    public ArrayList sentenceSplit(String input)
    {
        return InputNormalizer.sentenceSplit(sentenceSplitters, input);
    }


    public String applyInputSubstitutions(String input)
    {
        return Substituter.applySubstitutions(inputSubstitutions, input);
    }
    
    
    public XMLResourceSpec getChatlogSpec()
    {
        return chatlogSpec;
    }
}