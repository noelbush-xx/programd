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

import java.util.LinkedList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

import org.alicebot.server.core.Graphmaster;
import org.alicebot.server.core.util.DeveloperError;
import org.alicebot.server.core.util.InputNormalizer;
import org.alicebot.server.core.util.StringTriple;
import org.alicebot.server.core.util.StringTripleMatrix;
import org.alicebot.server.core.util.Trace;


/**
 *  Represents an individial target.
 */
public class Target
{
    // Instance variables

    private Category match;

    private TargetInputs inputs;

    private TargetExtensions extensions = new TargetExtensions();

    private LinkedList replies = new LinkedList();

    private Category newCategory = new Category();

    /** The activations count. */
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
     *  @param reply             the reply that the bot provided
     */
    public Target(String matchPattern, String matchThat, String matchTopic, String matchTemplate,
                  String inputText, String inputThat, String inputTopic, String reply)
    {
        match = new Category(matchPattern, matchThat, matchTopic, matchTemplate);
        inputs = new TargetInputs(inputText, inputThat, inputTopic);
        replies.add(reply);
    }


    /**
     *  Returns a hash code representing the target.
     *
     *  @return a hash code representing the target
     */
    public int hashCode()
    {
        return (match.getPattern() + match.getThat() + match.getTopic()).hashCode();
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
     *  Merges input and extension values from one target to this one.
     *
     *  @param target   the target whose inputs will be added
     */
    public void merge(Target target)
    {
        if (hashCode() != target.hashCode())
        {
            throw new DeveloperError("Targets with non-matching <match> segments cannot be merged!");
        }

        // Only add unique inputs.
        Iterator inputIterator = target.getInputs().iterator();
        Iterator replyIterator = target.getReplies().iterator();
        Iterator extensionIterator = target.getExtensions().iterator();
        while (inputIterator.hasNext())
        {
            StringTriple nextInput = (StringTriple)inputIterator.next();
            StringTriple nextExtension = null;
            try
            {
                nextExtension = (StringTriple)extensionIterator.next();
            }
            catch (NoSuchElementException e)
            {
                // It's okay if the target being merged does not have an exception.
            }

            String nextReply = (String)replyIterator.next();

            if (!inputs.contains(nextInput))
            {
                inputs.add(nextInput);
                replies.add(nextReply);
                if (nextExtension != null)
                {
                    extensions.add(nextExtension);
                }
            }
        }

        if (!(inputs.size() == replies.size()))
        {
            throw new DeveloperError("Merge operation failed to maintain stable activation count.");
        }
        activations = inputs.size();
    }


    /**
     *  Returns the match <code>pattern</code>.
     *
     *  @return the match <code>pattern</code>
     */
    public String getMatchPattern()
    {
        return match.getPattern();
    }


    /**
     *  Returns the match <code>that</code>.
     *
     *  @return the match <code>that</code>
     */
    public String getMatchThat()
    {
        return match.getThat();
    }


    /**
     *  Returns the match <code>topic</code>.
     *
     *  @return the match <code>topic</code>
     */
    public String getMatchTopic()
    {
        return match.getTopic();
    }


    /**
     *  Returns the match <code>template</code>.
     *
     *  @return the match <code>template</code>
     */
    public String getMatchTemplate()
    {
        return match.getTemplate();
    }


    /**
     *  Returns the inputs.
     *
     *  @return the inputs
     */
    public StringTripleMatrix getInputs()
    {
        return inputs;
    }


    /**
     *  Returns the input texts.
     *
     *  @return the input texts
     */
    public LinkedList getInputTexts()
    {
        return inputs.getTexts();
    }


    /**
     *  Returns the input <code>that</code>s.
     *
     *  @return the input <code>that</code>s
     */
    public LinkedList getInputThats()
    {
        return inputs.getThats();
    }


    /**
     *  Returns the input <code>topic</code>s.
     *
     *  @return the input <code>topic</code>s
     */
    public LinkedList getInputTopics()
    {
        return inputs.getTopics();
    }


    /**
     *  Returns the first input text.
     *
     *  @return the first input text
     */
    public String getFirstInputText()
    {
        return (String)inputs.getTexts().getFirst();
    }


    /**
     *  Returns the first input <code>that</code>.
     *
     *  @return the first input <code>that</code>
     */
    public String getFirstInputThat()
    {
        return (String)inputs.getThats().getFirst();
    }


    /**
     *  Returns the first input <code>topic</code>.
     *
     *  @return the first input <code>topic</code>
     */
    public String getFirstInputTopic()
    {
        return (String)inputs.getTopics().getFirst();
    }


    /**
     *  Returns the last input text.
     *
     *  @return the last input text
     */
    public String getLastInputText()
    {
        return (String)inputs.getTexts().getLast();
    }


    /**
     *  Returns the last input <code>that</code>.
     *
     *  @return the last input <code>that</code>
     */
    public String getLastInputThat()
    {
        return (String)inputs.getThats().getLast();
    }


    /**
     *  Returns the last input <code>topic</code>.
     *
     *  @return the last input <code>topic</code>
     */
    public String getLastInputTopic()
    {
        return (String)inputs.getTopics().getLast();
    }


    /**
     *  Returns the <code>n</code>th input text.
     *
     *  @param n    the index of the desired input text
     *
     *  @return the <code>n</code>th input text
     */
    public String getNthInputText(int n)
    {
        return (String)inputs.getTexts().get(n);
    }


    /**
     *  Returns the <code>n</code>th input <code>that</code>.
     *
     *  @param n    the index of the desired input that
     *
     *  @return the <code>n</code>th input <code>that</code>
     */
    public String getNthInputThat(int n)
    {
        return (String)inputs.getThats().get(n);
    }


    /**
     *  Returns the <code>n</code>th input <code>topic</code>.
     *
     *  @param n    the index of the desired input topic
     *
     *  @return the <code>n</code>th input <code>topic</code>
     */
    public String getNthInputTopic(int n)
    {
        return (String)inputs.getTopics().get(n);
    }

    
    /**
     *  Returns the extensions.
     *
     *  @return the extensions
     */
    public StringTripleMatrix getExtensions()
    {
        return extensions;
    }


    /**
     *  Returns the extension <code>pattern</code>s.
     *
     *  @return the extension <code>pattern</code>s
     */
    public LinkedList getExtensionPatterns()
    {
        return extensions.getPatterns();
    }


    /**
     *  Returns the extension <code>that</code>s.
     *
     *  @return the extension <code>that</code>s
     */
    public LinkedList getExtensionThats()
    {
        return extensions.getThats();
    }


    /**
     *  Returns the extension <code>topic</code>s.
     *
     *  @return the extension <code>topic</code>s
     */
    public LinkedList getExtensionTopics()
    {
        return extensions.getTopics();
    }


    /**
     *  Returns the first extension <code>pattern</code>.
     *
     *  @return the first extension <code>pattern</code>
     */
    public String getFirstExtensionPattern()
    {
        return (String)extensions.getPatterns().getFirst();
    }


    /**
     *  Returns the first extension <code>that</code>.
     *
     *  @return the first extension <code>that</code>
     */
    public String getFirstExtensionThat()
    {
        return (String)extensions.getThats().getFirst();
    }


    /**
     *  Returns the first extension <code>topic</code>.
     *
     *  @return the first extension <code>topic</code>
     */
    public String getFirstExtensionTopic()
    {
        return (String)extensions.getTopics().getFirst();
    }


    /**
     *  Returns the last extension <code>pattern</code>.
     *
     *  @return the last extension <code>pattern</code>
     */
    public String getLastExtensionPattern()
    {
        return (String)extensions.getPatterns().getLast();
    }


    /**
     *  Returns the last extension <code>that</code>.
     *
     *  @return the last extension <code>that</code>
     */
    public String getLastExtensionThat()
    {
        return (String)extensions.getThats().getLast();
    }


    /**
     *  Returns the last extension <code>topic</code>.
     *
     *  @return the last extension <code>topic</code>
     */
    public String getLastExtensionTopic()
    {
        return (String)extensions.getTopics().getLast();
    }


    /**
     *  Returns the <code>n</code>th extension pattern.
     *
     *  @param n    the index of the desired extension pattern
     *
     *  @return the <code>n</code>th extension pattern
     */
    public String getNthExtensionPattern(int n)
    {
        extend(n);
        return (String)extensions.getPatterns().get(n);
    }


    /**
     *  Returns the <code>n</code>th extension <code>that</code>.
     *
     *  @param n    the index of the desired extension that
     *
     *  @return the <code>n</code>th extension <code>that</code>
     */
    public String getNthExtensionThat(int n)
    {
        extend(n);
        return (String)extensions.getThats().get(n);
    }


    /**
     *  Returns the <code>n</code>th extension <code>topic</code>.
     *
     *  @param n    the index of the desired extension topic
     *
     *  @return the <code>n</code>th extension <code>topic</code>
     */
    public String getNthExtensionTopic(int n)
    {
        extend(n);
        return (String)extensions.getTopics().get(n);
    }

    
    /**
     *  Returns the <code>reply</code>s.
     *
     *  @return the <code>reply</code>s
     */
    public LinkedList getReplies()
    {
        return replies;
    }


    /**
     *  Returns the first <code>reply</code>.
     *
     *  @return the first <code>reply</code>
     */
    public String getFirstReply()
    {
        return (String)replies.getFirst();
    }


    /**
     *  Returns the last <code>reply</code>.
     *
     *  @return the last <code>reply</code>
     */
    public String getLastReply()
    {
        return (String)replies.getLast();
    }


    /**
     *  Returns the <code>n</code>th reply
     *
     *  @param n    the index of the desired reply
     *
     *  @return the <code>n</code>th reply
     */
    public String getNthReply(int n)
    {
        return (String)replies.get(n);
    }


    /**
     *  Returns the new <code>pattern</code>.
     *
     *  @return the new <code>pattern</code>
     */
    public String getNewPattern()
    {
        return newCategory.getPattern();
    }


    /**
     *  Returns the new <code>that</code>.
     *
     *  @return the new <code>that</code>
     */
    public String getNewThat()
    {
        return newCategory.getThat();
    }


    /**
     *  Returns the new <code>topic</code>.
     *
     *  @return the new <code>topic</code>
     */
    public String getNewTopic()
    {
        return newCategory.getTopic();
    }


    /**
     *  Returns the new <code>template</code>.
     *
     *  @return the new <code>template</code>
     */
    public String getNewTemplate()
    {
        return newCategory.getTemplate();
    }


    /**
     *  Sets the new <code>pattern</code>.
     *
     *  @param pattern  the new <code>pattern</code>
     */
    public void setNewPattern(String pattern)
    {
        newCategory.setPattern(pattern);
    }


    /**
     *  Sets the new <code>that</code>.
     *
     *  @param that the new <code>that</code>
     */
    public void setNewThat(String that)
    {
        newCategory.setThat(that);
    }


    /**
     *  Sets the new <code>topic</code>.
     *
     *  @param topic    the new <code>topic</code>
     */
    public void setNewTopic(String topic)
    {
        newCategory.setTopic(topic);
    }


    /**
     *  Sets the new <code>template</code>.
     *
     *  @param template the new <code>template</code>
     */
    public void setNewTemplate(String template)
    {
        newCategory.setTemplate(template);
    }


    /**
     *  Returns the activations count.
     *
     *  @return the activations count
     */
    public int getActivations()
    {
        return activations;
    }


    /**
     *  Generates extension patterns for all inputs.
     */
    public void extend()
    {
        for (int index = activations; --index >= 0; )
        {
            extend(index);
        }
    }


    /**
     *  Generates extension patterns for the target,
     *  at a given input index.
     *
     *  @param index
     */
    private void extend(int index)
    {
        extensions.ensureSize(index + 1);

        String inputText = getNthInputText(index);
        String inputThat = getNthInputThat(index);
        String inputTopic = getNthInputTopic(index);

        String extensionPattern;
        String extensionThat;
        String extensionTopic;

        try
        {
            // Try to extend the match-pattern using the input-text.
            extensionPattern = InputNormalizer.patternFit(extend(match.getPattern(), inputText));

            /*
                If successful (no exception),
                set target -that and -topic to match -that and -topic.
            */
            extensionThat = InputNormalizer.patternFit(match.getThat());
            extensionTopic = InputNormalizer.patternFit(match.getTopic());
        }
        catch (CannotExtendException e0)
        {
            // Couldn't extend the match-pattern, so set target-pattern to match-pattern.
            extensionPattern = InputNormalizer.patternFit(match.getPattern());
            try
            {
                // Try to extend the match-that using the input-that.
                extensionThat = InputNormalizer.patternFit(extend(match.getThat(), inputThat));

                /*
                    If successful (no exception),
                    set target-topic to match-topic.
                */
                extensionTopic = InputNormalizer.patternFit(match.getTopic());
            }
            catch (CannotExtendException e1)
            {
                // Couldn't extend the match-that, so set target-that to match-that.
                extensionThat = InputNormalizer.patternFit(match.getThat());
                try
                {
                    // Try to extend the match-topic using the input-topic.
                    extensionTopic = InputNormalizer.patternFit(extend(match.getTopic(), inputTopic));
                }
                catch (CannotExtendException e2)
                {
                    // Couldn't even extend topic, so return, doing nothing.
                    return;
                }
            }
        }
        extensions.getFirsts().set(index, extensionPattern);
        extensions.getSeconds().set(index, extensionThat);
        extensions.getThirds().set(index, extensionTopic);
    }


    /**
     *  Creates a new target pattern, by extending a pattern using an input.
     *
     *  @param pattern  the pattern part of the target
     *  @param input    the input part of the target
     *
     *  @return a new target
     *
     *  @throws CannotExtendException if the pattern-token length
     *                                is greater than or equal to the input-token length
     */
    private static String extend(String pattern, String input) throws CannotExtendException
    {
        // If the pattern does not contain wildcards, it cannot be extended.
        if ((pattern.indexOf('*') == -1) && (pattern.indexOf('_') == -1))
        {
            throw new CannotExtendException();
        }

        // Tokenize the pattern and input.
        StringTokenizer patternTokenizer = new StringTokenizer(pattern);
        StringTokenizer inputTokenizer = new StringTokenizer(input);

        // Count the pattern and input tokens.
        int patternTokenCount = patternTokenizer.countTokens();
        int inputTokenCount = inputTokenizer.countTokens();
        
        if (patternTokenCount > inputTokenCount)
        {
            patternTokenCount = inputTokenCount;
        }

        // Result will be constructed in this buffer.
        StringBuffer result = new StringBuffer();

        boolean hitWildcard = false;

        // Until hitting a wildcard in the pattern (or the end of the input), append words from the input.
        for (int index = 0; (index < patternTokenCount && !hitWildcard); index++)
        {
            String patternToken = patternTokenizer.nextToken();
            if (patternToken.equals(Graphmaster.ASTERISK) || patternToken.equals(Graphmaster.UNDERSCORE))
            {
                hitWildcard = true;
            }
            result.append(inputTokenizer.nextToken());
            result.append(' ');
        }

        // Append a * wildcard if the end of the input was not reached.
        if (inputTokenizer.hasMoreTokens())
        {
            result.append(Graphmaster.ASTERISK);
        }

        // Return the result.
        return result.toString();
    }
}

/**
 *  An exception thrown by {@link #extend(String, String)}.
 */
class CannotExtendException extends Exception
{
    public CannotExtendException()
    {
    }
}
