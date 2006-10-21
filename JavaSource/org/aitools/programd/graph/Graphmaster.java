/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.graph;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.aitools.programd.Bot;
import org.aitools.programd.Core;
import org.aitools.programd.util.NoMatchException;
import org.aitools.util.ObjectFactory;
import org.aitools.util.StringKit;

/**
 * <p>
 * The <code>Graphmaster</code> consists of a collection of nodes called
 * <code>Nodemapper</code>s. These <code>Nodemapper</code> s map the
 * branches from each node. The branches are either single words or wildcards.
 * </p>
 * <p>
 * The root of the <code>Graphmaster</code> is a <code>Nodemapper</code>
 * with many branches, one for each of the first words of all the patterns. The
 * number of leaf nodes in the graph is equal to the number of categories, and
 * each leaf node contains the <code>&lt;template&gt;</code> tag.
 * </p>

 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class Graphmaster extends AbstractGraphmapper
{
    /** The factory that will be used to create Nodemappers. */
    protected ObjectFactory<Nodemapper> NodemapperFactory;

    /** A map of KB URLs to &lt;BOTID&gt; nodes. */
    protected Map<URL, Set<Nodemapper>> botidNodes = new HashMap<URL, Set<Nodemapper>>();

    /** The root {@link Nodemapper}. */
    protected Nodemapper root;
    
    // Constants.

    /** A template marker. */
    public static final String TEMPLATE = "<template>";

    /** A that marker. */
    public static final String THAT = "<that>";

    /** A topic marker. */
    public static final String TOPIC = "<topic>";

    /** A botid marker. */
    public static final String BOTID = "<botid>";

    /** A filename marker. */
    public static final String FILENAME = "<filename>";

    /** The <code>*</code> wildcard. */
    public static final String ASTERISK = "*";

    /** The <code>_</code> wildcard. */
    public static final String UNDERSCORE = "_";

    /**
     * Creates a new <code>Graphmaster</code>, reading settings from the
     * given Core.
     * 
     * @param core the CoreSettings object from which to read settings
     */
    public Graphmaster(Core core)
    {
        super(core);
        this.NodemapperFactory = new ObjectFactory<Nodemapper>(this._core.getSettings().getNodemapperImplementation());
        this.root = this.NodemapperFactory.getNewInstance();        
    }
    
    /**
     * @see org.aitools.programd.graph.Graphmapper#addCategory(java.lang.String, java.lang.String, java.lang.String, java.lang.String, org.aitools.programd.Bot, java.net.URL)
     */
    @SuppressWarnings("boxing")
    public void addCategory(String pattern, String that, String topic, String template, Bot bot, URL source)
    {
        // Make sure the path components are right.
        if (pattern == null)
        {
            pattern = ASTERISK;
        }
        if (that == null)
        {
            that = ASTERISK;
        }
        if (topic == null)
        {
            topic = ASTERISK;
        }
    
        if (this.totalCategories % this.categoryLoadNotifyInterval == 0 && this.totalCategories > 0)
        {
            this._logger.info(String.format("%,d categories loaded so far.", this.totalCategories));
        }
    
        Nodemapper node = add(pattern, that, topic, bot.getID(), source);
        String storedTemplate = (String) node.get(TEMPLATE);
        if (storedTemplate == null)
        {
            node.put(FILENAME, source.toExternalForm());
            bot.addToPathMap(source, node);
            node.put(TEMPLATE, template);
            this.totalCategories++;
        }
        else
        {
            this.duplicateCategories++;
            switch (this._mergePolicy)
            {
                case SKIP:
                    if (this.noteEachMerge)
                    {
                        this._logger
                                .warn(String
                                        .format(
                                                "Skipping path-identical category from \"%s\" which duplicates path of category from \"%s\": %s:%s:%s",
                                                source, node.get(FILENAME), pattern, that, topic));
                    }
                    break;
    
                case OVERWRITE:
                    if (this.noteEachMerge)
                    {
                        this._logger
                                .warn(String
                                        .format(
                                                "Overwriting path-identical category from \"%s\" with new category from \"%s\".  Path: %s:%s:%s",
                                                node.get(FILENAME), source, pattern, that, topic));
                    }
                    node.put(FILENAME, source);
                    node.put(TEMPLATE, template);
                    break;
    
                case APPEND:
                    if (this.noteEachMerge)
                    {
                        this._logger
                                .warn(String
                                        .format(
                                                "Appending template of category from \"%s\" to template of path-identical category from \"%s\": %s:%s:%s",
                                                source, node.get(FILENAME), pattern, that, topic));
                    }
                    node.put(FILENAME, String.format("%s, %s", node.get(FILENAME), source));
                    node.put(TEMPLATE, appendTemplate(storedTemplate, template));
                    break;
    
                case COMBINE:
                    if (this.noteEachMerge)
                    {
                        this._logger
                                .warn(String
                                        .format(
                                                "Combining template of category from \"%s\" with template of path-identical category from \"%s\": %s:%s:%s",
                                                source, node.get(FILENAME), pattern, that, topic));
                    }
                    node.put(FILENAME, String.format("%s, %s", node.get(FILENAME),  source));
                    String combined = combineTemplates(storedTemplate, template);
                    node.put(TEMPLATE, combined);
                    break;
            }
        }
    }
    
    /**
     * Adds a new pattern-that-topic path to the <code>Graphmaster</code> root.
     * 
     * @param pattern &lt;pattern/&gt; path component
     * @param that &lt;that/&gt; path component
     * @param topic &lt;topic/&gt; path component
     * @param botid
     * @param source the source of this path
     * @return <code>Nodemapper</code> which is the result of adding the path.
     */
    protected Nodemapper add(String pattern, String that, String topic, String botid, URL source)
    {
        List<String> path = StringKit.wordSplit(pattern);
        path.add(THAT);
        path.addAll(StringKit.wordSplit(that));
        path.add(TOPIC);
        path.addAll(StringKit.wordSplit(topic));
        path.add(BOTID);
        path.add(botid);
        return add(path.listIterator(), this.root, source);
    }

    
    /**
     * Adds a new path to the <code>Graphmaster</code> at a given node.
     * 
     * @param pathIterator an iterator over the List containing the elements of
     *        the path
     * @param parent the <code>Nodemapper</code> parent to which the child
     *        should be appended
     * @param source the source of the original path
     * @return <code>Nodemapper</code> which is the result of adding the node
     */
    protected Nodemapper add(ListIterator<String> pathIterator, Nodemapper parent, URL source)
    {
        // If there are no more words in the path, return the parent node
        if (!pathIterator.hasNext())
        {
            parent.setTop();
            return parent;
        }
        // Otherwise, get the next word.
        String word = pathIterator.next();

        Nodemapper node;

        // If the parent contains this word, get the node with the word.
        if (parent.containsKey(word))
        {
            node = (Nodemapper) parent.get(word);
        }
        else
        {
            // Otherwise create a new node with this word.
            node = this.NodemapperFactory.getNewInstance();
            this.nodemapperCount++;

            parent.put(word, node);
            node.setParent(parent);
        }
        // Associate <BOTID> nodes with their sources.
        if (word.equals(BOTID))
        {
            Set<Nodemapper> nodes;
            if (this.botidNodes.containsKey(source))
            {
                nodes = this.botidNodes.get(source);
            }
            else
            {
                nodes = new HashSet<Nodemapper>();
                this.botidNodes.put(source, nodes);
            }
            nodes.add(node);
        }
        // Return the result of adding the new node to the parent.
        return add(pathIterator, node, source);
    }

    /**
     * @see org.aitools.programd.graph.Graphmapper#unload(java.net.URL, org.aitools.programd.Bot)
     */
    public void unload(URL path, Bot bot)
    {
        Set<Nodemapper> nodemappers = bot.getLoadedFilesMap().get(path);

        for (Nodemapper nodemapper : nodemappers)
        {
            remove(nodemapper);
            this.totalCategories--;
        }
        nodemappers.clear();
        Set<String> botids = this._urlCatalog.get(path);
        // It can end up being null if there was an error in loading
        // (non-existent file).
        if (botids != null)
        {
            botids.remove(bot.getID());
        }
        if (botids == null || botids.size() == 0)
        {
            this._urlCatalog.remove(path);
        }
    }

    /**
     * @see org.aitools.programd.graph.Graphmapper#removeCategory(java.lang.String, java.lang.String, java.lang.String, org.aitools.programd.Bot)
     */
    public void removeCategory(String pattern, String that, String topic, Bot bot)
    {
        try
        {
            remove(match(this.root, this.root, composeInputPath(pattern, that, topic, bot.getID()), "", new StringBuilder(), new Match(), Match.State.IN_INPUT, System.currentTimeMillis()
                + this.responseTimeout));
        }
        catch (NoMatchException e)
        {
            this._logger.error(String.format("Could not find category to remove (%s:%s:%s)", pattern, that, topic, bot));
        }
    }

    /**
     * Removes a node, as well as as many of its ancestors as have no
     * descendants other than this node or its ancestors.
     * 
     * @param nodemapper the mapper for the node to remove
     */
    protected void remove(Nodemapper nodemapper)
    {
        Nodemapper parent = nodemapper.getParent();
        if (parent != null)
        {
            parent.remove(nodemapper);
            if (parent.size() == 0)
            {
                if (parent != this.root)
                {
                    remove(parent);
                }
            }
        }
        nodemapper = null;
    }

    /**
     * @param input
     * @param that
     * @param topic
     * @param botid
     * @return the match
     * @see org.aitools.programd.graph.Graphmapper#match(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     * @throws NoMatchException
     */
    public Match match(String input, String that, String topic, String botid) throws NoMatchException
    {
        // Get the match, starting at the root, with an empty star and path, starting in "in input" mode.
        Match match = new Match();
        match(this.root, this.root, composeInputPath(input, that, topic, botid), "", new StringBuilder(), match, Match.State.IN_INPUT, System.currentTimeMillis()
                + this.responseTimeout);
        return match;
    }
    
    /**
     * Composes an input path as a list of tokens, given the components.
     * Empty components are represented with asterisks.
     * 
     * @param input
     * @param that
     * @param topic
     * @param botid
     * @return the new path
     */
    protected List<String> composeInputPath(String input, String that, String topic, String botid)
    {
        List<String> inputPath = new ArrayList<String>();

        // Input text part.
        if (input.length() > 0)
        {
            inputPath = StringKit.wordSplit(input);
        }
        else
        {
            inputPath = new ArrayList<String>();
            inputPath.add(ASTERISK);
        }

        // <that> marker.
        inputPath.add(THAT);

        // Input <that> part.
        if (that.length() > 0)
        {
            inputPath.addAll(StringKit.wordSplit(that));
        }
        else
        {
            inputPath.add(ASTERISK);
        }

        // <topic> marker.
        inputPath.add(TOPIC);

        // Input <topic> part.
        if (topic.length() > 0)
        {
            inputPath.addAll(StringKit.wordSplit(topic));
        }
        else
        {
            inputPath.add(ASTERISK);
        }

        // <botid> marker.
        inputPath.add(BOTID);

        // Input [directed to] botid.
        inputPath.add(botid);
        
        return inputPath;
    }
    
    /**
     * Searches for a match in the <code>Graphmaster</code> to a given path.
     * This is a low-level prototype, used for internal recursion.
     * 
     * @see #match(String, String, String, String)
     * @param nodemapper the nodemapper where we start matching
     * @param parent the parent of the nodemapper where we start matching
     * @param input the input path (possibly a sublist of the original)
     * @param wildcardContent contents absorbed by a wildcard
     * @param path the path matched so far
     * @param match an object containing information about the match
     * @param matchState state variable tracking which part of the path we're in
     * @param expiration when this response process expires
     * @return the leaf nodemapper at which the match ends
     * @throws NoMatchException
     */
    @SuppressWarnings("boxing")
    protected Nodemapper match(Nodemapper nodemapper, Nodemapper parent, List<String> input, String wildcardContent, StringBuilder path,
            Match match, Match.State matchState, long expiration) throws NoMatchException
    {
        // Return null if expiration has been reached.
        if (System.currentTimeMillis() >= expiration)
        {
            throw new NoMatchException("Match time expired.");
        }

        // Halt matching if this nodemapper is higher than the length of the input.
        if (input.size() < nodemapper.getHeight())
        {
            if (this._matchLogger.isDebugEnabled())
            {
                this._matchLogger.debug(String.format(
                        "Halting match because input size %d < nodemapper height %d.%ninput: %s%nnodemapper: %s", input.size(), nodemapper
                                .getHeight(), input.toString(), nodemapper.toString()));
            }
            throw new NoMatchException();
        }

        // If no more tokens in the input, see if this is a template.
        if (input.size() == 0)
        {
            // If so, the path
            // component is the botid.
            if (nodemapper.containsKey(TEMPLATE))
            {
                match.setBotID(path.toString());
                match.setTemplate((String)nodemapper.get(TEMPLATE));
                match.setFilename((String)nodemapper.get(FILENAME));
                return nodemapper;
            }
            // (otherwise...)
            throw new NoMatchException();
        }

        // Take the first word of the input as the head.
        String head = input.get(0).trim();

        // Take the rest as the tail.
        List<String> tail = input.subList(1, input.size());
        
        // Now proceed through the AIML matching sequence: _, a-z, *.

        // See if this nodemapper has a _ wildcard. _ comes first in the AIML "alphabet".
        try
        {
            return match(UNDERSCORE,        // key
                         matchState,        // target match state for wildcard content
                         nodemapper,        // current nodemapper
                         tail,              // current tail
                         true,              // append new path? yes
                         wildcardContent,   // current wildcard content
                         head,              // new wildcard content
                         path,              // current path
                         match,             // match object
                         matchState,        // current match state
                         expiration         // expiration timestamp
                         );
        }
        catch (NoMatchException e)
        {
            // Not necessarily an error; drop through.
        }

        /*
         * The nodemapper may have contained a _, but this led to no match. Or it didn't contain a _ at all.
         * So let's see if it contains the head.
         */
        if (nodemapper.containsKey(head))
        {
            /*
             * Check now whether this head is a marker for the <that>, <topic> or <botid> segments of the path. If it
             * is, set the match state variable accordingly.
             */
            boolean isMarker = false;
            if (head.startsWith("<"))
            {
                if (head.equals(THAT))
                {
                    isMarker = true;
                    matchState = Match.State.IN_THAT;
                }
                else if (head.equals(TOPIC))
                {
                    isMarker = true;
                    matchState = Match.State.IN_TOPIC;
                }
                else if (head.equals(BOTID))
                {
                    isMarker = true;
                    matchState = Match.State.IN_BOTID;
                }
            }
            try
            {
                return match(head,                                      // key
                             isMarker ? matchState.preceding() : null,  // target match state for wildcard content
                             nodemapper,                                // current nodemapper
                             tail,                                      // current tail
                             !isMarker,                                 // append new path? (only it this is not a marker)
                             wildcardContent,                           // current wildcard content (empty if this is a marker)
                             isMarker ? "" : wildcardContent,           // new wildcard content
                             path,                                      // current path
                             match,                                     // match object
                             matchState,                                // current match state
                             expiration                                 // expiration timestamp
                             );
            }
            catch (NoMatchException e)
            {
                // Not necessarily an error; drop through.
            }
        }

        /*
         * The nodemapper may have contained the head, but this led to no match. Or it didn't contain the head at all.
         * In any case, check to see if it contains a * wildcard. * comes last in the AIML "alphabet".
         */
        try
        {
            return match(ASTERISK,          // key
                         matchState,        // target match state for wildcard content
                         nodemapper,        // current nodemapper
                         tail,              // current tail
                         true,              // append new path?
                         wildcardContent,   // current wildcard content
                         head,              // new wildcard content
                         path,              // current path
                         match,             // match object
                         matchState,        // current match state
                         expiration         // expiration timestamp
                         );
        }
        catch (NoMatchException e)
        {
            // Not necessarily an error; drop through.
        }

        /*
         * The nodemapper has failed to match at all: it contains neither _, nor the head, nor *. However, if its parent
         * is a wildcard, then the match continues to be valid and can proceed with the tail, the current path, and the
         * star content plus the head as the new star.
         */
        if (nodemapper.equals(parent.get(ASTERISK)) || nodemapper.equals(parent.get(UNDERSCORE)))
        {
            return match(nodemapper,                                        // current nodemapper
                         parent,                                            // current path
                         tail,                                              // current tail
                         String.format("%s %s", wildcardContent, head),     // head = wildcard content + head
                         path,                                              // current path
                         match,                                             // match object
                         matchState,                                        // current match state
                         expiration                                         // expiration timestamp
                         );
        }

        /*
         * If we get here, we've hit a dead end; this exception will be passed back up the recursive chain of matches,
         * perhaps even hitting the high-level match method, though this is assumed to be the rarest occurence.
         */
        throw new NoMatchException();
    }
    
    /**
     * An internal method used for matching.
     * 
     * @param key
     * @param wildcardDestination
     * @param nodemapper
     * @param tail
     * @param appendToPath 
     * @param currentWildcard
     * @param newWildcard
     * @param path
     * @param match
     * @param matchState
     * @param expiration
     * @return the resulting leaf nodemapper
     * @throws NoMatchException
     */
    protected Nodemapper match(String key, Match.State wildcardDestination, Nodemapper nodemapper, List<String> tail,
            boolean appendToPath, String currentWildcard, String newWildcard, StringBuilder path, Match match, Match.State matchState, long expiration) throws NoMatchException
    {
        // Does the nodemapper contain the key?
        if (nodemapper.containsKey(key))
        {
            // If so, construct a new path from the current path plus the key.
            StringBuilder newPath = new StringBuilder();
            if (path.length() > 0)
            {
                newPath.append(path);
                newPath.append(' ');
            }
            newPath.append(key);

            // Try to get a match with the tail and this new path (may throw exception)
            Nodemapper result = match((Nodemapper) nodemapper.get(key),             // newly matched nodemapper
                                      nodemapper,                                   // current nodemapper as parent
                                      tail,                                         // current tail
                                      newWildcard,                              // current wildcardContent
                                      appendToPath ? newPath : new StringBuilder(), // either the new path, or a blank one
                                      match,                                        // match object
                                      matchState,                                   // current match state
                                      expiration                                    // expieration timestamp
                                      );
            // capture and push the wildcard content appropriate to the current match state.
            if (wildcardDestination != null && currentWildcard.length() > 0)
            {
                match.pushWildcardContent(wildcardDestination, currentWildcard);
            }
            return result;
        }
        throw new NoMatchException();
    }
    
    /**
     * @see org.aitools.programd.graph.AbstractGraphmapper#addForBot(java.net.URL, java.lang.String)
     */
    @Override
    public void addForBot(URL path, String botid)
    {
        if (!this._urlCatalog.containsKey(path))
        {
            throw new IllegalArgumentException("Must not call addForBot() using a URL that has not already been loaded.");
        }
        if (this._urlCatalog.get(path).contains(botid))
        {
            throw new IllegalArgumentException("Must not call addForBot() using a URL and botid that have already been associated.");
        }
        if (this._logger.isDebugEnabled())
        {
            this._logger.debug(String.format("Adding botid \"%s\" to all paths associated with \"%s\".", botid, path));
        }
        for (Nodemapper node : this.botidNodes.get(path))
        {
            // Hook up with the existing template.
            Object t = node.get(node.keySet().iterator().next());
            node.put(botid, t);
            this.totalCategories++;
        }
        this._urlCatalog.get(path).add(botid);
    }
}
