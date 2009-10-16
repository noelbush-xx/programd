/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.graph;

import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.aitools.util.Text;
import org.aitools.util.runtime.DeveloperError;

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
public class MemoryGraphmapper extends AbstractGraphmapper
{
    /** The factory that will be used to create Nodemappers. */
    protected ObjectFactory<Nodemapper> NodemapperFactory;

    /** A map of loaded file URLs to botids. */
    protected Map<URL, Set<String>> _urlCatalog = new HashMap<URL, Set<String>>();

    /** A map of KB URLs to &lt;BOTID&gt; nodes. */
    protected Map<URL, Set<Nodemapper>> botidNodes = new HashMap<URL, Set<Nodemapper>>();

    /** The root {@link Nodemapper}. */
    protected Nodemapper root;

    /** A count of Nodemappers. */
    protected int nodemapperCount = 1;
    
    /**
     * Creates a new <code>Graphmaster</code>, reading settings from the
     * given Core.
     * 
     * @param core the CoreSettings object from which to read settings
     */
    public MemoryGraphmapper(Core core)
    {
        super(core);
        this.NodemapperFactory = new ObjectFactory<Nodemapper>(this._core.getSettings().getNodemapperImplementation());
        this.root = this.NodemapperFactory.getNewInstance();        
    }
    
    @Override
    protected boolean isAlreadyLoaded(URL filename)
    {
        return this._urlCatalog.containsKey(filename);
    }
    
    @Override
    protected boolean isAlreadyLoadedForBot(URL filename, String botid)
    {
        return isAlreadyLoaded(filename) && this._urlCatalog.get(filename).contains(botid);
    }

    @Override
    protected void associateBotIDWithFilename(String botid, URL filename)
    {
        Set<String> botids;
        if (this._urlCatalog.containsKey(filename))
        {
            botids = this._urlCatalog.get(filename);
        }
        else
        {
            botids = new HashSet<String>();
            this._urlCatalog.put(filename, botids);
        }
        botids.add(botid);
    }

    /**
     * Always returns true, since the Graphmaster only works in memory.
     * 
     * @see org.aitools.programd.graph.AbstractGraphmapper#necessaryToLoad(java.net.URL)
     */
    @Override
    @SuppressWarnings("unused")
    protected boolean necessaryToLoad(URL path)
    {
        return true;
    }

    /**
     * @see org.aitools.programd.graph.AbstractGraphmapper#addForBot(java.net.URL, java.lang.String)
     */
    @Override
    protected void addForBot(URL path, String botid)
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
        for (Nodemapper nodemapper : this.botidNodes.get(path))
        {
            // Hook up with the existing template.
            Object t = nodemapper.get(nodemapper.keySet().iterator().next());
            nodemapper.put(botid, t);
            this._totalCategories++;
        }
        this._urlCatalog.get(path).add(botid);
    }
    
    /**
     * @see org.aitools.programd.graph.Graphmapper#addCategory(java.lang.String, java.lang.String, java.lang.String, java.lang.String, org.aitools.programd.Bot, java.net.URL)
     */
    @Override
    public void add(String pattern, String that, String topic, String template, Bot bot, URL source)
    {
        Nodemapper nodemapper = add(pattern, that, topic, bot.getID(), source);
        String storedTemplate = (String) nodemapper.get(TEMPLATE);
        if (storedTemplate == null)
        {
            nodemapper.put(FILENAME, source.toExternalForm());
            bot.addToPathMap(source, nodemapper);
            nodemapper.put(TEMPLATE, template);
            this._totalCategories++;
        }
        else
        {
            this._duplicateCategories++;
            switch (this._mergePolicy)
            {
                case SKIP:
                    if (this._noteEachMerge)
                    {
                        this._logger
                                .warn(String
                                        .format(
                                                "Skipping path-identical category from \"%s\" which duplicates path of category from \"%s\": %s:%s:%s",
                                                source, nodemapper.get(FILENAME), pattern, that, topic));
                    }
                    break;
    
                case OVERWRITE:
                    if (this._noteEachMerge)
                    {
                        this._logger
                                .warn(String
                                        .format(
                                                "Overwriting path-identical category from \"%s\" with new category from \"%s\".  Path: %s:%s:%s",
                                                nodemapper.get(FILENAME), source, pattern, that, topic));
                    }
                    nodemapper.put(FILENAME, source);
                    nodemapper.put(TEMPLATE, template);
                    break;
    
                case APPEND:
                    if (this._noteEachMerge)
                    {
                        this._logger
                                .warn(String
                                        .format(
                                                "Appending template of category from \"%s\" to template of path-identical category from \"%s\": %s:%s:%s",
                                                source, nodemapper.get(FILENAME), pattern, that, topic));
                    }
                    nodemapper.put(FILENAME, String.format("%s, %s", nodemapper.get(FILENAME), source));
                    nodemapper.put(TEMPLATE, appendTemplate(storedTemplate, template));
                    break;
    
                case COMBINE:
                    if (this._noteEachMerge)
                    {
                        this._logger
                                .warn(String
                                        .format(
                                                "Combining template of category from \"%s\" with template of path-identical category from \"%s\": %s:%s:%s",
                                                source, nodemapper.get(FILENAME), pattern, that, topic));
                    }
                    nodemapper.put(FILENAME, String.format("%s, %s", nodemapper.get(FILENAME),  source));
                    String combined = combineTemplates(storedTemplate, template);
                    nodemapper.put(TEMPLATE, combined);
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
        List<String> path = Text.wordSplit(pattern);
        path.add(THAT);
        path.addAll(Text.wordSplit(that));
        path.add(TOPIC);
        path.addAll(Text.wordSplit(topic));
        path.add(BOT);
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

        Nodemapper nodemapper;

        // If the parent contains this word, get the nodemapper with the word.
        if (parent.containsKey(word))
        {
            nodemapper = (Nodemapper) parent.get(word);
        }
        else
        {
            // Otherwise create a new nodemapper with this word.
            nodemapper = this.NodemapperFactory.getNewInstance();
            this.nodemapperCount++;

            parent.put(word, nodemapper);
            nodemapper.setParent(parent);
        }
        // Associate <BOTID> nodes with their sources.
        if (word.equals(BOT))
        {
            Set<Nodemapper> nodemappers;
            if (this.botidNodes.containsKey(source))
            {
                nodemappers = this.botidNodes.get(source);
            }
            else
            {
                nodemappers = new HashSet<Nodemapper>();
                this.botidNodes.put(source, nodemappers);
            }
            nodemappers.add(nodemapper);
        }
        // Return the result of adding the new nodemapper to the parent.
        return add(pathIterator, nodemapper, source);
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
        Nodemapper result = match(this.root, this.root, composeInputPath(input, that, topic, botid), "", new StringBuilder(), match, Match.State.IN_INPUT, System.currentTimeMillis()
                + this._responseTimeout);
        if (result != null)
        {
            return match;
        }
        throw new NoMatchException(String.format("%s:%s:%s:%s", input, that, topic, botid));
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
     * @throws NoMatchException if match time expires
     */
    @SuppressWarnings("boxing")
    protected Nodemapper match(Nodemapper nodemapper, Nodemapper parent, List<String> input, String wildcardContent, StringBuilder path,
            Match match, Match.State matchState, long expiration) throws NoMatchException
    {
        if (nodemapper == null)
        {
            return null;
        }
        // Return null if expiration has been reached.
        if (System.currentTimeMillis() >= expiration)
        {
            throw new NoMatchException("Match time expired.");
        }
        
        Nodemapper nextNodemapper = null;

        // Halt matching if this nodemapper is higher than the length of the input.
        if (input.size() < nodemapper.getHeight())
        {
            if (this._matchLogger.isDebugEnabled())
            {
                this._matchLogger.debug(String.format(
                        "Halting match because input size %d < nodemapper height %d.%ninput: %s%nnodemapper: %s", input.size(), nodemapper
                                .getHeight(), input.toString(), nodemapper.toString()));
            }
            return null;
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
                match.setFilenames(Arrays.asList(((String)nodemapper.get(FILENAME)).split(",")));
                return nodemapper;
            }
            // (otherwise...)
            return null;
        }

        // Take the first word of the input as the head.
        String head = input.get(0).trim();

        // Take the rest as the tail.
        List<String> tail = input.subList(1, input.size());
        
        // Now proceed through the AIML matching sequence: _, a-z, *.
        Match.State _matchState = matchState;

        // See if this nodemapper has a _ wildcard. _ comes first in the AIML "alphabet".
        nextNodemapper = match(UNDERSCORE,        // key
                               _matchState,        // target match state for wildcard content
                               nodemapper,        // current nodemapper
                               tail,              // current tail
                               true,              // append new path? yes
                               wildcardContent,   // current wildcard content
                               head,              // new wildcard content
                               path,              // current path
                               match,             // match object
                               _matchState,        // current match state
                               expiration         // expiration timestamp
                               );
        if (nextNodemapper != null)
        {
            return nextNodemapper;
        }

        /*
         * The nodemapper may have contained a _, but this led to no match. Or it didn't contain a _ at all.
         * So let's see if it contains the head.
         */
        if (nodemapper.containsKey(head))
        {
            /*
             * Check now whether this head is a marker for the <that>, <topic> or <botid> segments of the path. If it
             * is, save the contents of the thereby terminated path component, and then set the new match state variable accordingly.
             */
            boolean isMarker = false;
            if (head.startsWith("<"))
            {
                match.setPathComponent(_matchState, path.toString().toUpperCase());
                if (head.equals(THAT))
                {
                    isMarker = true;
                    _matchState = Match.State.IN_THAT;
                }
                else if (head.equals(TOPIC))
                {
                    isMarker = true;
                    _matchState = Match.State.IN_TOPIC;
                }
                else if (head.equals(BOT))
                {
                    isMarker = true;
                    _matchState = Match.State.IN_BOTID;
                }
            }
            nextNodemapper = match(head,                                      // key
                                   isMarker ? _matchState.preceding() : null,  // target match state for wildcard content
                                   nodemapper,                                // current nodemapper
                                   tail,                                      // current tail
                                   !isMarker,                                 // append new path? (only if this is not a marker)
                                   wildcardContent,                           // current wildcard content (empty if this is a marker)
                                   isMarker ? "" : wildcardContent,           // new wildcard content
                                   path,                                      // current path
                                   match,                                     // match object
                                   _matchState,                                // current match state
                                   expiration                                 // expiration timestamp
                                   );
            if (nextNodemapper != null)
            {
                return nextNodemapper;
            }
        }

        /*
         * The nodemapper may have contained the head, but this led to no match. Or it didn't contain the head at all.
         * In any case, check to see if it contains a * wildcard. * comes last in the AIML "alphabet".
         */
        nextNodemapper = match(ASTERISK,          // key
                               _matchState,        // target match state for wildcard content
                               nodemapper,        // current nodemapper
                               tail,              // current tail
                               true,              // append new path?
                               wildcardContent,   // current wildcard content
                               head,              // new wildcard content
                               path,              // current path
                               match,             // match object
                               _matchState,        // current match state
                               expiration         // expiration timestamp
                               );
        if (nextNodemapper != null)
        {
            return nextNodemapper;
        }

        /*
         * The nodemapper has failed to match at all: it contains neither _, nor the head, nor *. However, if its parent
         * is a wildcard, then the match continues to be valid and can proceed with the tail, the current path, and the
         * star content plus the head as the new star.
         */
        if (nodemapper.equals(parent.get(ASTERISK)) || nodemapper.equals(parent.get(UNDERSCORE)))
        {
            nextNodemapper = match(nodemapper,                                        // current nodemapper
                                   parent,                                            // current path
                                   tail,                                              // current tail
                                   String.format("%s %s", wildcardContent, head),     // head = wildcard content + head
                                   path,                                              // current path
                                   match,                                             // match object
                                   _matchState,                                        // current match state
                                   expiration                                         // expiration timestamp
                                   );
            if (nextNodemapper != null)
            {
                return nextNodemapper;
            }
        }

        /*
         * If we get here, we've hit a dead end; this null value will be passed back up the recursive chain of matches,
         * perhaps even hitting the high-level match method and causing a NoMatchException, though this is assumed to be the rarest occurence.
         */
        return null;
    }
    
    /**
     * An internal method used for matching.
     * This method <i>assumes</i> that nodemapper.containsKey(key)!
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
        // Construct a new path from the current path plus the key.
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
                                  newWildcard,                                  // current wildcardContent
                                  appendToPath ? newPath : new StringBuilder(), // either the new path, or a blank one
                                  match,                                        // match object
                                  matchState,                                   // current match state
                                  expiration                                    // expiration timestamp
                                  );
        // capture and push the wildcard content appropriate to the current match state.
        if (wildcardDestination != null && wildcardDestination.compareTo(Match.State.IN_BOTID) < 0 && currentWildcard.length() > 0)
        {
            match.pushWildcardContent(wildcardDestination, currentWildcard);
        }
        return result;
    }

    /**
     * @see org.aitools.programd.graph.Graphmapper#removeCategory(java.lang.String, java.lang.String, java.lang.String, org.aitools.programd.Bot)
     */
    public void removeCategory(String pattern, String that, String topic, Bot bot)
    {
        Nodemapper nodemapper = null;
        try
        {
            nodemapper = match(this.root, this.root, composeInputPath(pattern, that, topic, bot.getID()), "", new StringBuilder(), new Match(), Match.State.IN_INPUT, System.currentTimeMillis()
                    + this._responseTimeout);
        }
        catch (NoMatchException e)
        {
            throw new DeveloperError("Could not remove category.", e);
        }
        if (nodemapper != null)
        {
            remove(nodemapper);
        }
        else
        {
            this._logger.error(String.format("Could not find category to remove (%s:%s:%s)", pattern, that, topic, bot));
        }
    }

    /**
     * Removes a node, as well as as many of its ancestors as have no
     * descendants other than this nodemapper or its ancestors.
     * 
     * @param nodemapper the mapper for the nodemapper to remove
     */
    protected void remove(Nodemapper nodemapper)
    {
        Nodemapper parent = nodemapper.getParent();
        if (parent != null)
        {
            parent.remove(nodemapper);
            if (parent.size() == 0 && parent != this.root)
            {
                remove(parent);
            }
        }
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
            this._totalCategories--;
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

    @Override
    protected void print(PrintWriter out)
    {
        print(this.root, out);
        out.close();
    }
    
    private void print(Nodemapper nodemapper, PrintWriter out)
    {
        ArrayList<String> keyList = new ArrayList<String>(nodemapper.keySet());
        int keyCount = keyList.size();
        for (int index = 0; index < keyCount; index++)
        {
            String key = keyList.get(index);
            out.print(key);
            out.print(' ');
            Object value = nodemapper.get(key);
            if (value instanceof Nodemapper)
            {
                print((Nodemapper)value, out);
            }
            else
            {
                out.print(org.jdom.Text.normalizeString((String)value));
                if (index == keyCount - 1)
                {
                    out.println();
                }
            }
        }
    }
}
