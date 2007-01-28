/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.graph;

import java.util.ArrayList;
import java.util.List;

import org.aitools.programd.util.InputNormalizer;

/**
 * <code>Match</code> implements an object to store the results of matching, most notably the stacks resulting from
 * multiple input/that/topic wildcards.
 */
public class Match
{
    // Instance variables.

    /** The portions of the input captured by wildcards. */
    private ArrayList<String> _inputStars = new ArrayList<String>();

    /** The portions of the &lt;that/&gt; captured by wildcards. */
    private ArrayList<String> _thatStars = new ArrayList<String>();

    /** The portions of the &lt;topic/&gt; <code>name</code> captured by wildcards. */
    private ArrayList<String> _topicStars = new ArrayList<String>();

    /** The <code>pattern</code> part of the matched path. */
    private String _pattern;

    /** The <code>that</code> part of the matched path. */
    private String _that;

    /** The <code>topic</code> part of the matched path. */
    private String _topic;

    /** The <code>botid</code> part of the matched path. */
    private String _botid;

    /** The <code>template</code> associated with the matched path. */
    private String _template;

    /** The <code>filenames</code> from which the matched path came. */
    private List<String> _filenames;

    /** Match states. */
    public static enum State implements Comparable<Match.State>
    {
        /** Trying to match the input part of the path. */
        IN_INPUT,

        /** Trying to match the that part of the path. */
        IN_THAT,

        /** Trying to match the topic part of the path. */
        IN_TOPIC,

        /** Trying to match the botid part of the path. */
        IN_BOTID;
        
        /**
         * @return the value of the state that precedes this one
         */
        public State preceding()
        {
            switch (this)
            {
                case IN_BOTID:
                    return IN_TOPIC;
                case IN_TOPIC:
                    return IN_THAT;
                case IN_THAT:
                    return IN_INPUT;
                case IN_INPUT:
                default:
                    throw new IllegalArgumentException("No state precedes IN_INPUT!");
            }
        }
    }

    /**
     * Pushes content onto the wildcard stack for the given match state.
     *
     * @param state the state onto whose wildcard stack the string should be pushed
     * @param string the string to push onto the stack
     */
    public void pushWildcardContent(State state, String string)
    {
        switch (state)
        {
            case IN_INPUT:
                this._inputStars.add(0, string);
                break;
                
            case IN_THAT:
                this._thatStars.add(0, string);
                break;
                
            case IN_TOPIC:
                this._topicStars.add(0, string);
                break;
                
            case IN_BOTID:
                throw new IllegalArgumentException("No wildcard content for botids!");
        }
    }

    /**
     * Sets the indicated component of the matched path.
     * 
     * @param state the match state corresponding to the match path component
     * @param string the value of the match path component
     */
    public void setPathComponent(Match.State state, String string)
    {
        switch (state)
        {
            case IN_INPUT:
                this._pattern = string;
                break;
                
            case IN_THAT:
                this._that = string;
                break;
                
            case IN_TOPIC:
                this._topic = string;
                break;
                
            case IN_BOTID:
                this._botid = string;
                break;
        }
    }

    /**
     * Sets the <code>botid</code> part of the matched path.
     * 
     * @param string the <code>botid</code> part of the matched path
     */
    public void setBotID(String string)
    {
        this._botid = string;
    }

    /**
     * Sets the <code>template</code> part of the matched path.
     * 
     * @param string the <code>botid</code> part of the matched path
     */
    public void setTemplate(String string)
    {
        this._template = string;
    }

    /**
     * Sets the <code>filename</code> part of the matched path
     * (this list may have 1 or more members).
     * 
     * @param filenames the <code>filename</code> part of the matched path
     */
    public void setFilenames(List<String> filenames)
    {
        this._filenames = filenames;
    }

    /**
     * Returns the <code>pattern</code> part of the matched path.
     * 
     * @return the <code>pattern</code> part of the matched path
     */
    public String getPattern()
    {
        return this._pattern;
    }

    /**
     * Returns the <code>that</code> part of the matched path.
     * 
     * @return the <code>that</code> part of the matched path
     */
    public String getThat()
    {
        return this._that;
    }

    /**
     * Returns the <code>topic</code> part of the matched path.
     * 
     * @return the <code>topic</code> part of the matched path
     */
    public String getTopic()
    {
        return this._topic;
    }

    /**
     * Returns the match path.
     * 
     * @return the match path
     */
    public String getPath()
    {
        return String.format("%s:%s:%s:%s", InputNormalizer.patternFit(this._pattern), InputNormalizer.patternFit(this._that),
                InputNormalizer.patternFit(this._topic), this._botid);
    }

    /**
     * Gets the template attached to this node.
     * 
     * @return the &lt;template/&gt; attached to this node
     */
    public String getTemplate()
    {
        return this._template;
    }

    /**
     * Gets the filenames from which the &lt;template/&gt; originally came.
     * 
     * @return the filenames from which the &lt;template/&gt; originally came
     */
    public List<String> getFileNames()
    {
        return this._filenames;
    }

    /**
     * Returns the input stars.
     * 
     * @return the input stars
     */
    public ArrayList<String> getInputStars()
    {
        return this._inputStars;
    }

    /**
     * Returns the &lt;that/&gt; stars.
     * 
     * @return the &lt;that/&gt; stars
     */
    public ArrayList<String> getThatStars()
    {
        return this._thatStars;
    }

    /**
     * Returns the &lt;topic/&gt; stars.
     * 
     * @return the &lt;topic/&gt; stars
     */
    public ArrayList<String> getTopicStars()
    {
        return this._topicStars;
    }
}
