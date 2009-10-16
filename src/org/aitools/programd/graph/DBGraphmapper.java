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
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import org.aitools.programd.Bot;
import org.aitools.programd.Core;
import org.aitools.programd.util.NoMatchException;
import org.aitools.util.Text;
import org.aitools.util.resource.URLTools;

/**
 * This is an implementation of the {@link Graphmapper} interface
 * that uses a database.  There is a certain amount of duplication of
 * code from {@link MemoryGraphmapper} that cannot be avoided, without
 * accepting an additional performance penalty of lots of boxing and unboxing,
 * because <code>int</code> is a primitive type in Java (as opposed to
 * {@link Integer}.
 * 
 * TODO: Remove SuppressWarnings
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 *
 */
@SuppressWarnings("unused")
public class DBGraphmapper extends AbstractGraphmapper
{
    /**
     * Creates a new DBGraphmapper, reading settings from the given Core.
     * 
     * @param core the CoreSettings object from which to read settings
     */
    public DBGraphmapper(Core core)
    {
        super(core);
    }
    
    @Override
    protected boolean necessaryToLoad(URL path)
    {
        boolean result = false;
        Connection connection = this._core.getDBConnection();
        long lastModified = URLTools.getLastModified(path);
        long lastLoaded = DBNodemapper.getLastLoaded(connection, path);
        if (lastModified > lastLoaded)
        {
            unload(connection, path);
            result = true;
        }
        close(connection);
        return result;
    }

    @Override
    protected boolean isAlreadyLoaded(URL filename)
    {
        Connection connection = this._core.getDBConnection();
        boolean result = DBNodemapper.fileIsAlreadyPresent(connection, filename);
        close(connection);
        return result;
    }

    @Override
    protected boolean isAlreadyLoadedForBot(URL filename, String botid)
    {
        Connection connection = this._core.getDBConnection();
        boolean result = DBNodemapper.fileIsAlreadyPresentForBot(connection, filename, botid);
        close(connection);
        return result;
    }

    @Override
    protected void associateBotIDWithFilename(String botid, URL path)
    {
        Connection connection = this._core.getDBConnection();
        DBNodemapper.associateBotWithFile(connection, botid, path);
        close(connection);
    }

    /**
     * @see org.aitools.programd.graph.AbstractGraphmapper#addForBot(java.net.URL, java.lang.String)
     */
    @Override
    public void addForBot(URL path, String botid)
    {
        Connection connection = this._core.getDBConnection();
        List<URL> filenames = DBNodemapper.getFilenamesForBot(connection, botid); 
        if (filenames.size() < 1)
        {
            throw new IllegalArgumentException("Must not call addForBot() using a URL that has not already been loaded.");
        }
        if (filenames.contains(path))
        {
            throw new IllegalArgumentException("Must not call addForBot() using a URL and botid that have already been associated.");
        }
        if (this._logger.isDebugEnabled())
        {
            this._logger.debug(String.format("Adding botid \"%s\" to all paths associated with \"%s\".", botid, path));
        }
        for (int node : DBNodemapper.getBotIDNodesForFile(connection, path))
        {
            int botidnode = DBNodemapper.put(connection, DBNodemapper.getParent(connection, node), botid);
            DBNodemapper.setTemplateByID(connection, botidnode, DBNodemapper.getTemplateID(connection, node));
            this._totalCategories++;
        }
        DBNodemapper.associateBotWithFile(connection, botid, path);
        close(connection);
    }

    /**
     * @see org.aitools.programd.graph.AbstractGraphmapper#add(java.lang.String, java.lang.String, java.lang.String, java.lang.String, org.aitools.programd.Bot, java.net.URL)
     */
    @Override
    public void add(String pattern, String that, String topic, String template, Bot bot, URL source)
    {
        Connection connection = this._core.getDBConnection();
        int node = add(connection, pattern, that, topic, bot.getID(), source);
        String storedTemplate = DBNodemapper.getTemplate(connection, node);
        if (storedTemplate == null)
        {
            DBNodemapper.setFilename(connection, node, source);
            DBNodemapper.setTemplate(connection, node, template);
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
                                                source, DBNodemapper.getFilenames(connection, node).get(0), pattern, that, topic));
                    }
                    break;
    
                case OVERWRITE:
                    if (this._noteEachMerge)
                    {
                        this._logger
                                .warn(String
                                        .format(
                                                "Overwriting path-identical category from \"%s\" with new category from \"%s\".  Path: %s:%s:%s",
                                                DBNodemapper.getFilenames(connection, node).get(0), source, pattern, that, topic));
                    }
                    DBNodemapper.setFilename(connection, node, source);
                    DBNodemapper.setTemplate(connection, node, template);
                    break;
    
                case APPEND:
                    if (this._noteEachMerge)
                    {
                        this._logger
                                .warn(String
                                        .format(
                                                "Appending template of category from \"%s\" to template of path-identical category from \"%s\": %s:%s:%s",
                                                source, DBNodemapper.getFilenames(connection, node), pattern, that, topic));
                    }
                    DBNodemapper.addFilename(connection, node, source);
                    DBNodemapper.setTemplate(connection, node, appendTemplate(storedTemplate, template));
                    break;
    
                case COMBINE:
                    if (this._noteEachMerge)
                    {
                        this._logger
                                .warn(String
                                        .format(
                                                "Combining template of category from \"%s\" with template of path-identical category from \"%s\": %s:%s:%s",
                                                source, DBNodemapper.getFilenames(connection, node), pattern, that, topic));
                    }
                    DBNodemapper.addFilename(connection, node, source);
                    DBNodemapper.setTemplate(connection, node, combineTemplates(storedTemplate, template));
                    break;
            }
        }
        close(connection);
    }

    /**
     * Adds a new pattern-that-topic path to the <code>DBGraphmapper</code> root.
     * 
     * @param connection database access object
     * @param pattern &lt;pattern/&gt; path component
     * @param that &lt;that/&gt; path component
     * @param topic &lt;topic/&gt; path component
     * @param botid
     * @param source the source of this path
     * @return node which is the result of adding the path.
     */
    protected int add(Connection connection, String pattern, String that, String topic, String botid, URL source)
    {
        List<String> path = Text.wordSplit(pattern);
        path.add(THAT);
        path.addAll(Text.wordSplit(that));
        path.add(TOPIC);
        path.addAll(Text.wordSplit(topic));
        path.add(BOT);
        path.add(botid);
        return add(connection, path.listIterator(), 0, source);
    }
    
    /**
     * Adds a new path to the <code>DBGraphmapper</code> at a given node.
     * 
     * @param connection database access object
     * @param pathIterator an iterator over the List containing the elements of the path
     * @param parent the node parent to which the child should be appended
     * @param source the source of the original path
     * @return node which is the result of adding the node
     */
    protected int add(Connection connection, ListIterator<String> pathIterator, int parent, URL source)
    {
        // If there are no more words in the path, return the parent node
        if (!pathIterator.hasNext())
        {
            //setTop(parent);
            return parent;
        }
        // Otherwise, get the next word.
        String word = pathIterator.next();

        int node;

        // If the parent contains this word, get the nodemapper with the word.
        if (DBNodemapper.containsKey(connection, parent, word))
        {
            node = DBNodemapper.get(connection, parent, word);
            assert node >= 0;
        }
        else
        {
            // Otherwise create a new node with this word.
            node = DBNodemapper.put(connection, parent, word);
        }
        // Associate botid nodes with their sources.
        if (word.equals(BOT))
        {
            DBNodemapper.storeBotIDNodeFile(connection, node, source);
        }
        // Return the result of adding the new nodemapper to the parent.
        return add(connection, pathIterator, node, source);
    }
    
    /**
     * @see org.aitools.programd.graph.Graphmapper#match(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public Match match(String input, String that, String topic, String botid) throws NoMatchException
    {
        Connection connection = this._core.getDBConnection();
        // Get the match, starting at the root, with an empty star and path, starting in "in input" mode.
        Match match = new Match();
        match(connection, 0, 0, composeInputPath(input, that, topic, botid), "", new StringBuilder(), match, Match.State.IN_INPUT, System.currentTimeMillis()
                + this._responseTimeout);
        close(connection);
        return match;
    }
    
    /**
     * Searches for a match in the <code>DBGraphmapper</code> to a given path.
     * This is a low-level prototype, used for internal recursion.
     * 
     * @param connection 
     * @param node the node where we start matching
     * @param parent the parent of the node where we start matching
     * @param input the input path (possibly a sublist of the original)
     * @param wildcardContent contents absorbed by a wildcard
     * @param path the path matched so far
     * @param match an object containing information about the match
     * @param matchState state variable tracking which part of the path we're in
     * @param expiration when this response process expires
     * @return the leaf node at which the match ends
     * @throws NoMatchException
     * @see #match(String, String, String, String)
     */
    protected int match(Connection connection, int node, int parent, List<String> input, String wildcardContent, StringBuilder path,
            Match match, Match.State matchState, long expiration) throws NoMatchException
    {
        // Return null if expiration has been reached.
        if (System.currentTimeMillis() >= expiration)
        {
            throw new NoMatchException("Match time expired.");
        }

        // If no more tokens in the input, see if this is a template.
        if (input.size() == 0)
        {
            // If so, the path component is the botid.
            String template = DBNodemapper.getTemplate(connection, node);
            if (template != null)
            {
                match.setBotID(path.toString());
                match.setTemplate(template);
                match.setFilenames(DBNodemapper.getFilenames(connection, node));
                return node;
            }
            // (otherwise...)
            throw new NoMatchException();
        }

        // Take the first word of the input as the head.
        String head = input.get(0).trim();

        // Take the rest as the tail.
        List<String> tail = input.subList(1, input.size());
        
        // Now proceed through the AIML matching sequence: _, a-z, *.
        Match.State _matchState = matchState;

        // See if this nodemapper has a _ wildcard. _ comes first in the AIML "alphabet".
        try
        {
            return match(connection,               // db access object
                         UNDERSCORE,        // key
                         _matchState,        // target match state for wildcard content
                         node,              // current node
                         tail,              // current tail
                         true,              // append new path? yes
                         wildcardContent,   // current wildcard content
                         head,              // new wildcard content
                         path,              // current path
                         match,             // match object
                         _matchState,        // current match state
                         expiration         // expiration timestamp
                         );
        }
        catch (NoMatchException e)
        {
            // Not necessarily an error; drop through.
        }

        /*
         * The node may have contained a _, but this led to no match. Or it didn't contain a _ at all.
         * So let's see if it contains the head.
         */
        if (DBNodemapper.containsKey(connection, node, head))
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
            try
            {
                return match(connection,                                       // db access object
                             head,                                      // key
                             isMarker ? _matchState.preceding() : null,  // target match state for wildcard content
                             node,                                      // current node
                             tail,                                      // current tail
                             !isMarker,                                 // append new path? (only it this is not a marker)
                             wildcardContent,                           // current wildcard content (empty if this is a marker)
                             isMarker ? "" : wildcardContent,           // new wildcard content
                             path,                                      // current path
                             match,                                     // match object
                             _matchState,                                // current match state
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
            return match(connection,               // db access object
                         ASTERISK,          // key
                         _matchState,        // target match state for wildcard content
                         node,              // current node
                         tail,              // current tail
                         true,              // append new path?
                         wildcardContent,   // current wildcard content
                         head,              // new wildcard content
                         path,              // current path
                         match,             // match object
                         _matchState,        // current match state
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
        if (node == DBNodemapper.get(connection, parent, ASTERISK) || node == DBNodemapper.get(connection, parent, UNDERSCORE))
        {
            return match(connection,                                               // db access object
                         node,                                              // current node
                         parent,                                            // current path
                         tail,                                              // current tail
                         String.format("%s %s", wildcardContent, head),     // head = wildcard content + head
                         path,                                              // current path
                         match,                                             // match object
                         _matchState,                                        // current match state
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
     * @param connection 
     * @param key
     * @param wildcardDestination
     * @param node
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
    protected int match(Connection connection, String key, Match.State wildcardDestination, int node, List<String> tail,
            boolean appendToPath, String currentWildcard, String newWildcard, StringBuilder path, Match match, Match.State matchState, long expiration) throws NoMatchException
    {
        // Does the nodemapper contain the key?
        if (DBNodemapper.containsKey(connection, node, key))
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
            int result = match(connection,                                          // db access object
                               DBNodemapper.get(connection, node, key),             // newly matched nodemapper
                               node,                                         // current nodemapper as parent
                               tail,                                         // current tail
                               newWildcard,                                  // current wildcardContent
                               appendToPath ? newPath : new StringBuilder(), // either the new path, or a blank one
                               match,                                        // match object
                               matchState,                                   // current match state
                               expiration                                    // expiration timestamp
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
     * @see org.aitools.programd.graph.Graphmapper#removeCategory(java.lang.String, java.lang.String, java.lang.String, org.aitools.programd.Bot)
     */
    public void removeCategory(String pattern, String that, String topic, Bot bot)
    {
        Connection connection = this._core.getDBConnection();
        try
        {
            remove(connection, match(connection, 0, 0, composeInputPath(pattern, that, topic, bot.getID()), "", new StringBuilder(), new Match(), Match.State.IN_INPUT, System.currentTimeMillis()
                + this._responseTimeout));
        }
        catch (NoMatchException e)
        {
            this._logger.error(String.format("Could not find category to remove (%s:%s:%s)", pattern, that, topic, bot));
        }
        close(connection);
    }

    /**
     * Removes a node, as well as as many of its ancestors as have no
     * descendants other than this nodemapper or its ancestors.
     * 
     * @param connection db access object
     * @param node the mapper for the nodemapper to remove
     */
    protected void remove(Connection connection, int node)
    {
        int parent = DBNodemapper.getParent(connection, node);
        if (parent >= 0)
        {
            DBNodemapper.remove(connection, parent, node);
            if (DBNodemapper.size(connection, parent) == 0 && parent != 0)
            {
                remove(connection, parent);
            }
        }
    }

    /**
     * @see org.aitools.programd.graph.Graphmapper#unload(java.net.URL, org.aitools.programd.Bot)
     */
    public void unload(URL path, Bot bot)
    {
        Connection connection = this._core.getDBConnection();
        unload(connection, path, bot);
    }

    protected void unload(Connection connection, URL path, Bot bot)
    {
        Set<Integer> nodes = DBNodemapper.getBotIDNodesForFile(connection, path);

        for (int node : nodes)
        {
            remove(connection, node);
            this._totalCategories--;
        }
        DBNodemapper.removeBotIDFromFilename(connection, bot.getID(), path);
    }
    
    /**
     * Unloads a path without concern for which bot(s) has/have loaded it.
     * 
     * @param connection
     * @param path
     */
    protected void unload(Connection connection, URL path)
    {
        //TODO
    }
    
    protected void close(Connection connection)
    {
        try
        {
            connection.close();
        }
        catch (SQLException e)
        {
            this._logger.error("DB error when closing connection.", e);
        }
    }

    @Override
    protected void print(PrintWriter out)
    {
        print(0, 0, out);
        out.close();
    }
    
    private void print(int node, int indent, PrintWriter out)
    {
        /*
        for (String key : nodemapper.keySet())
        {
            out.print(Text.repeat(' ', indent));
            out.print(key);
            out.print(' ');
            Object value = nodemapper.get(key);
            if (value instanceof Nodemapper)
            {
                print((Nodemapper)value, indent + key.length(), out);
            }
            else
            {
                out.println(value);
            }
        }
        */
    }
}
