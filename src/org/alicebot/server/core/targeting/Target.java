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

package org.alicebot.server.core.targeting;

import java.util.ArrayList;


/**
 *  Represents an individial target.
 */
public class Target
{
    // Instance variables

    /** The <code>pattern</code> part of the matched path. */
    private String matchPattern;

    /** The <code>that</code> part of the matched path. */
    private String matchThat;

    /** The <code>topic</code> part of the matched path. */
    private String matchTopic;

    /** The <code>template</code> associated with the matched path. */
    private String matchTemplate;

    /** The input text(s) that were matched. */
    private ArrayList inputTexts = new ArrayList();

    /** The value(s) of the <code>that</code> predicate when the input was received. */
    private ArrayList inputThats = new ArrayList();

    /** The value(s) of the <code>topic</code> predicate when the input was received. */
    private ArrayList inputTopics = new ArrayList();

    /** An extension <code>pattern</code>. */
    private String extensionPattern;

    /** An extension <code>that</code>. */
    private String extensionThat;

    /** An extension <code>topic</code>. */
    private String extensionTopic;

    /** An extension <code>template</code>. */
    private String extensionTemplate;

    /** The activations count (starts at 1). */
    private int activations = 1;


    /**
     *  Creates a new Target object.
     *
     *  @param matchPattern         the <code>pattern</code> part of the matched path
     *  @param matchThat            the <code>that</code> part of the matched path
     *  @param matchTopic           the <code>topic</code> part of the matched path
     *  @param matchTemplate        the <code>template</code> associated with the matched path
     *  @param inputText            the input text that was matched
     *  @param inputThat            the value of the <code>that</code> predicate when the input was received
     *  @param inputTopic           the value of the <code>topic</code> predicate when the input was received
     *  @param extensionPattern     a target <code>pattern</code>
     *  @param extensionThat        a target <code>that</code>
     *  @param extensionTopic       a target <code>topic</code>
     */
    public Target(String matchPattern, String matchThat, String matchTopic, String matchTemplate,
                  String inputText, String inputThat, String inputTopic,
                  String extensionPattern, String extensionThat, String extensionTopic)
    {
        this.matchPattern = matchPattern;
        this.matchThat = matchThat;
        this.matchTopic = matchTopic;
        this.matchTemplate = matchTemplate;
        this.inputTexts.add(inputText);
        this.inputThats.add(inputThat);
        this.inputTopics.add(inputTopic);
        this.extensionPattern = extensionPattern;
        this.extensionThat = extensionThat;
        this.extensionTopic = extensionTopic;
    }


    /**
     *  Returns a hash code representing the target.
     *
     *  @return a hash code representing the target
     */
    public int hashCode()
    {
        return (this.matchPattern + this.matchThat + this.matchTopic).hashCode();
    }


    /**
     *  Returns a hash code that would be generated for
     *  a target with the given parameters.
     *
     *  @param matchPattern
     *  @param matchThat
     *  @param matchTopic
     *
     *  @return a hash code that would be generated for a target with the given parameters
     */
    public static int generateHashCode(String matchPattern, String matchThat, String matchTopic)
    {
        return (matchPattern + matchThat + matchTopic).hashCode();
    }
    

    /**
     *  Returns the match <code>that</code>.
     *
     *  @return the match <code>pattern</code>
     */
    public String getMatchPattern()
    {
        return this.matchPattern;
    }


    /**
     *  Returns the match <code>that</code>.
     *
     *  @return the match <code>that</code>
     */
    public String getMatchThat()
    {
        return this.matchThat;
    }


    /**
     *  Returns the match <code>topic</code>.
     *
     *  @return the match <code>topic</code>
     */
    public String getMatchTopic()
    {
        return this.matchTopic;
    }


    /**
     *  Returns the match <code>template</code>.
     *
     *  @return the match <code>template</code>
     */
    public String getMatchTemplate()
    {
        return this.matchTemplate;
    }


    /**
     *  Returns the input texts.
     *
     *  @return the input texts
     */
    public ArrayList getInputTexts()
    {
        return this.inputTexts;
    }


    /**
     *  Returns the input <code>that</code>s.
     *
     *  @return the input <code>that</code>s
     */
    public ArrayList getInputThats()
    {
        return this.inputThats;
    }


    /**
     *  Returns the input <code>topic</code>s.
     *
     *  @return the input <code>topic</code>s
     */
    public ArrayList getInputTopics()
    {
        return this.inputTopics;
    }


    /**
     *  Returns the first input text.
     *
     *  @return the first input text
     */
    public String getFirstInputText()
    {
        return (String)this.inputTexts.get(0);
    }


    /**
     *  Returns the first input <code>that</code>.
     *
     *  @return the first input <code>that</code>
     */
    public String getFirstInputThat()
    {
        return (String)this.inputThats.get(0);
    }


    /**
     *  Returns the first input <code>topic</code>.
     *
     *  @return the first input <code>topic</code>
     */
    public String getFirstInputTopic()
    {
        return (String)this.inputTopics.get(0);
    }


    /**
     *  Returns the last input text.
     *
     *  @return the last input text
     */
    public String getLastInputText()
    {
        return (String)this.inputTexts.get(this.inputTexts.size() - 1);
    }


    /**
     *  Returns the last input <code>that</code>.
     *
     *  @return the last input <code>that</code>
     */
    public String getLastInputThat()
    {
        return (String)this.inputThats.get(this.inputTexts.size() - 1);
    }


    /**
     *  Returns the last input <code>topic</code>.
     *
     *  @return the last input <code>topic</code>
     */
    public String getLastInputTopic()
    {
        return (String)this.inputTopics.get(this.inputTexts.size() - 1);
    }


    /**
     *  Adds input values.
     *
     *  @param inputText        the input text that was matched
     *  @param inputThat        the value of the <code>that</code> predicate when the input was received
     *  @param inputTopic       the value of the <code>topic</code> predicate when the input was received
     */
    public void addInputs(String inputText, String inputThat, String inputTopic)
    {
        this.inputTexts.add(inputText);
        this.inputThats.add(inputThat);
        this.inputTopics.add(inputTopic);
        this.activations++;
    }


    /**
     *  Adds the inputs from one target to this one.
     *
     *  @param target   the target whose inputs will be added
     */
    public void addInputs(Target target)
    {
        // Be sure this target isn't identical to the argument!
        if (hashCode() == target.hashCode())
        {
            return;
        }
        this.inputTexts.addAll(target.getInputTexts());
        this.inputThats.addAll(target.getInputThats());
        this.inputTopics.addAll(target.getInputTopics());

        this.activations += target.getActivations();
    }


    /**
     *  Returns the extension <code>pattern</code>.
     *
     *  @return the extension <code>pattern</code>
     */
    public String getExtensionPattern()
    {
        return this.extensionPattern;
    }


    /**
     *  Returns the extension <code>that</code>.
     *
     *  @return the extension <code>that</code>
     */
    public String getExtensionThat()
    {
        return this.extensionThat;
    }


    /**
     *  Returns the extension <code>topic</code>.
     *
     *  @return the extension <code>topic</code>
     */
    public String getExtensionTopic()
    {
        return this.extensionTopic;
    }


    /**
     *  Returns the extension <code>template</code>.
     *
     *  @return the extension <code>template</code>
     */
    public String getExtensionTemplate()
    {
        return this.extensionTemplate;
    }


    /**
     *  Sets the extension <code>pattern</code>.
     *
     *  @param pattern  the extension <code>pattern</code>
     */
    public void setExtensionPattern(String pattern)
    {
        this.extensionPattern = pattern;
    }


    /**
     *  Sets the extension <code>that</code>.
     *
     *  @param that the extension <code>that</code>
     */
    public void setExtensionThat(String that)
    {
        this.extensionThat = that;
    }


    /**
     *  Sets the extension <code>topic</code>.
     *
     *  @param topic    the extension <code>topic</code>
     */
    public void setExtensionTopic(String topic)
    {
        this.extensionTopic = topic;
    }


    /**
     *  Sets the extension <code>template</code>.
     *
     *  @param template the extension <code>template</code>
     */
    public void setExtensionTemplate(String template)
    {
        this.extensionTemplate = template;
    }


    /**
     *  Increments the activation count by 1.
     */
    public void incrementActivations()
    {
        this.activations++;
    }


    /**
     *  Increments the activation count by <code>increment</code>.
     */
    public void incrementActivations(int increment)
    {
        this.activations += increment;
    }


    /**
     *  Returns the activations count.
     *
     *  @return the activations count
     */
    public int getActivations()
    {
        return this.activations;
    }
}
