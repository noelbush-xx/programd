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

/*
    Code cleanup (4.1.3 [00] - October 2001, Noel Bush)
    - formatting cleanup
    - general grammar fixes
    - complete javadoc
    - made all imports explicit
*/

package org.alicebot.server.core.util;

import java.util.Stack;
import java.util.Vector;

import org.alicebot.server.core.Graphmaster;
import org.alicebot.server.core.node.Nodemapper;


/**
 *  <code>Match</code> implements an object to store the results of
 *  matching, most notably the stacks resulting from multiple input/that/topic
 *  wildcards.
 */
public class Match
{
    // Instance variables.

    /** The portions of the input captured by wildcards. */
    private Stack  inputStar = new Stack();

    /** The portions of the &lt;that/&gt; captured by wildcards. */
    private Stack  thatStar  = new Stack();

    /** The portions of the &lt;topic/&gt; <code>name</code> captured by wildcards. */
    private Stack  topicStar = new Stack();

    /** The <code>pattern</code> part of the matched path. */
    private String pattern;

    /** The <code>that</code> part of the matched path. */
    private String that;

    /** The <code>topic</code> part of the matched path. */
    private String topic;

    /** An empty string. */
    private static final String EMPTY_STRING = "";

    /** The match path. */
    private String path = EMPTY_STRING;

    /** The nodemapper that resulted from this match. */
    private Nodemapper nodemapper;


    // Convenience constants.

    /** A space. */
    public static final String SPACE = " ";

    /** The string used to separate path components. */
    private static final String SPACED_PATH_SEPARATOR = SPACE + Graphmaster.PATH_SEPARATOR + SPACE;


    /**
     *  Pushes a new input star onto the input stack.
     *
     *  @param string   the string to push onto the input stack
     */
    public void pushInputStar(String string)
    {
        inputStar.push(string);
    }


    /**
     *  Pushes a new that star onto the thatstar stack.
     *
     *  @param string   the string to push onto the thatstar stack
     */
    public void pushThatStar(String string)
    {
        thatStar.push(string);
    }


    /**
     *  Pushes a new topic star onto the topicstar stack.
     *
     *  @param string   the string to push onto the topicstar stack
     */
    public void pushTopicStar(String string)
    {
        topicStar.push(string);
    }


    /**
     *  Sets the <code>pattern</code> part of the matched path.
     *
     *  @param string   the <code>pattern</code> part of the matched path
     */
    public void setPattern(String string)
    {
        this.pattern = string;
    }


    /**
     *  Sets the <code>that</code> part of the matched path.
     *
     *  @param string   the <code>that</code> part of the matched path
     */
    public void setThat(String string)
    {
        this.that = string;
    }


    /**
     *  Sets the <code>topic</code> part of the matched path.
     *
     *  @param string   the <code>topic</code> part of the matched path
     */
    public void setTopic(String string)
    {
        this.topic = string;
    }


    /**
     *  Set the match-resulting nodemapper.
     *
     *  @param node the match-resulting nodemapper
     */

    public void setNodemapper(Nodemapper nodemapper)
    {
        this.nodemapper = nodemapper;
    }


    /**
     *  Returns the <code>pattern</code> part of the matched path.
     *
     *  @return the <code>pattern</code> part of the matched path
     */
    public String getPattern()
    {
        return this.pattern;
    }


    /**
     *  Returns the <code>that</code> part of the matched path.
     *
     *  @return the <code>that</code> part of the matched path
     */
    public String getThat()
    {
        return this.that;
    }


    /**
     *  Returns the <code>topic</code> part of the matched path.
     *
     *  @return the <code>topic</code> part of the matched path
     */
    public String getTopic()
    {
        return this.topic;
    }


    /**
     *  Returns the match path.
     *
     *  @return the match path
     */
    public String getPath()
    {
        return InputNormalizer.patternFit(pattern) + SPACED_PATH_SEPARATOR +
               InputNormalizer.patternFit(that) + SPACED_PATH_SEPARATOR +
               InputNormalizer.patternFit(topic);
    }


    /**
     *  Returns the match-resulting nodemapper.
     *
     *  @return the match-resulting nodemapper
     */

    public Nodemapper getNodemapper()
    {
        return this.nodemapper;
    }


    /**
     *  Gets the template attached to this node.
     *
     *  @return the &lt;template/&gt; attached to this node
     */
    public String getTemplate()
    {
        return (String)this.nodemapper.get(Graphmaster.TEMPLATE);
    }


    /**
     *  Gets the filename from which the &lt;template/&gt; originally came.
     *
     *  @return the filename from which the &lt;template/&gt; originally came
     */
    public String getFileName()
    {
        return (String)this.nodemapper.get(Graphmaster.FILENAME);
    }


    /**
     *  Gets the input stars out of the stack and returns them
     *  as a Vector.
     *
     *  @return Vector of input stars
     */
    public Vector getInputStars()
    {
        Vector result = new Vector();
        while(!this.inputStar.empty())
        {
            result.add((String)this.inputStar.pop());
        }
        return result;
    }


    /**
     *  Gets the &lt;that/&gt; stars out of the stack and returns them
     *  as a Vector.
     *
     *  @return Vector of &lt;that/&gt; stars
     */
    public Vector getThatStars()
    {
        Vector result = new Vector();
        while(!this.thatStar.empty())
        {
            result.add((String)this.thatStar.pop());
        }
        return result;
    }


    /**
     *  Get the &lt;topic/&gt; stars out of the stack and returns them
     *  as a vector
     *
     *  @return Vector of &lt;topic/&gt; stars
     */
    public Vector getTopicStars()
    {
        Vector result = new Vector();
        while(!this.topicStar.empty())
        {
            result.add((String)this.topicStar.pop());
        }
        return result;
    }
}
