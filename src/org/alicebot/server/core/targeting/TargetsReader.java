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

import javax.swing.ProgressMonitor;

import java.awt.Component;
import java.io.BufferedReader;
import java.io.File;
import java.lang.reflect.Field;

import org.alicebot.server.core.parser.GenericReader;
import org.alicebot.server.core.parser.GenericReader.TransitionMade;
import org.alicebot.server.core.util.DeveloperError;
import org.alicebot.server.core.util.Trace;


/**
 *  Reads a targets data file.
 *  This version is based on {@link org.alicebot.server.core.parser.AIMLReader}.
 *  Obviously this and its companion {@link TargetsReaderListener}
 *  duplicate a lot from AIMLReader and AIMLLoader,
 *  so once this is stabilized these should all be combined.
 *
 *  @author Noel Bush
 */
public class TargetsReader extends GenericReader implements Runnable
{
    // Convenience constants.

    /** The string &quot;inputText&quot;. */
    private static final String INPUT_TEXT          = "inputText";

    /** The string &quot;inputThat&quot;. */
    private static final String INPUT_THAT          = "inputThat";

    /** The string &quot;inputTopic&quot;. */
    private static final String INPUT_TOPIC         = "inputTopic";

    /** The string &quot;matchPattern&quot;. */
    private static final String MATCH_PATTERN       = "matchPattern";

    /** The string &quot;matchThat&quot;. */
    private static final String MATCH_THAT          = "matchThat";

    /** The string &quot;matchTopic&quot;. */
    private static final String MATCH_TOPIC         = "matchTopic";

    /** The string &quot;matchTemplate&quot;. */
    private static final String MATCH_TEMPLATE      = "matchTemplate";

    /** The string &quot;reply&quot;. */
    private static final String REPLY               = "reply";


    /*
        Parser states.
    */

    /** Parser state: not within any element. */
    private final int S_NONE                        = 1;

    /** Parser state: entered a &lt;targets&gt; element. */
    private final int S_IN_TARGETS                  = 2;

    /** Parser state: exited a &lt;targets&gt; element. */
    private final int S_OUT_TARGETS                 = 3;

    /** Parser state: entered a &lt;target&gt; element. */
    private final int S_IN_TARGET                   = 4;

    /** Parser state: exited a &lt;target&gt; element. */
    private final int S_OUT_TARGET                  = 5;

    /** Parser state: entered an &lt;input&gt; element. */
    private final int S_IN_INPUT                    = 6;

    /** Parser state: exited an &lt;input&gt; element. */
    private final int S_OUT_INPUT                   = 7;

    /** Parser state: entered a &lt;text&gt; element. */
    private final int S_IN_TEXT                     = 8;

    /** Parser state: exited a &lt;text&gt; element. */
    private final int S_OUT_TEXT                    = 9;

    /** Parser state: entered a &lt;match&gt; element. */
    private final int S_IN_MATCH                    = 10;

    /** Parser state: exited a &lt;match&gt; element. */
    private final int S_OUT_MATCH                   = 11;

    /** Parser state: entered a &lt;pattern&gt; element. */
    private final int S_IN_PATTERN                  = 12;

    /** Parser state: exited a &lt;pattern&gt; element. */
    private final int S_OUT_PATTERN                 = 13;

    /** Parser state: entered a &lt;that&gt; element. */
    private final int S_IN_THAT                     = 14;

    /** Parser state: exited a &lt;that&gt; element. */
    private final int S_OUT_THAT                    = 15;

    /** Parser state: entered a &lt;topic&gt; element. */
    private final int S_IN_TOPIC                    = 16;

    /** Parser state: exited a &lt;template&gt; element. */
    private final int S_OUT_TOPIC                   = 17;

    /** Parser state: entered a &lt;template&gt; element. */
    private final int S_IN_TEMPLATE                 = 18;

    /** Parser state: exited a &lt;template&gt; element. */
    private final int S_OUT_TEMPLATE                = 19;

    /** Parser state: entered a &lt;reply&gt; element. */
    private final int S_IN_REPLY                    = 20;

    /** Parser state: exited a &lt;reply&gt; element. */
    private final int S_OUT_REPLY                   = 21;


    /*
        Parser actions.
    */

    /** Parser action: set input context. */
    private final int SET_INPUT_CONTEXT             = 0;

    /** Parser action: set match context. */
    private final int SET_MATCH_CONTEXT             = 1;

    /** Parser action: set reply context. */
    private final int SET_REPLY_CONTEXT             = 2;

    /** Parser action: deliver a category. */
    private final int DELIVER_TARGET                = 3;

    /** Parser action: set done to true. */
    private final int SET_DONE                      = 4;

    /** Parser action: abort unexpectedly. */
    private final int ABORT                         = 5;


    /*
        Instance variables.
    */

    /** The most recently collected match &lt;pattern&gt;&lt;/pattern&gt; contents. */
    public String                 matchPattern       = ASTERISK;

    /** The most recently collected match &lt;that&gt;&lt;/that&gt; contents. */
    public String                 matchThat          = ASTERISK;

    /** The most recently collected match &lt;topic&gt;&lt;/topic&gt; contents. */
    public String                 matchTopic         = ASTERISK;

    /** The most recently collected match &lt;template&gt;&lt;/template&gt; contents. */
    public String                 matchTemplate      = EMPTY_STRING;

    /** The most recently collected input text contents. */
    public String                 inputText          = EMPTY_STRING;

    /** The most recently collected input &lt;that&gt;&lt;/that&gt; contents. */
    public String                 inputThat          = EMPTY_STRING;

    /** The most recently collected input &lt;topic&gt;&lt;/topic&gt; contents. */
    public String                 inputTopic         = EMPTY_STRING;

    /** The most recently collected &lt;reply&gt;&lt;/reply&gt; contents. */
    public String                 reply              = EMPTY_STRING;

    /** A reference to some pattern/text field. */
    public Field                  patternField;

    /** A reference to some that field. */
    public Field                  thatField;

    /** A reference to some topic field. */
    public Field                  topicField;

    /** A reference to some template/reply field. */
    public Field                  templateField;

    /** The progress monitor that will be maintained if this is created with a parent component. */
    private ProgressMonitor       monitor;

    /** A progress index that will be maintained if this is being monitored. */
    private double                progress = 0;

    /** The length of the file to be read (used if a progress monitor is invoked). */
    private double                progressScaleFactor;


    /**
     *  Initializes a <code>TargetsReader</code> with a parent component,
     *  indicating that a <code>ProgressMonitor</code> should be created
     *  and updated for that component.
     *
     *  @param parent   the parent component
     *  @param length   the length of the file to be read
     */
    public TargetsReader(String fileName, BufferedReader buffReader, TargetsReaderListener targetsListener,
                         String encoding, long length, Component parent)
    {
        super(fileName, buffReader, encoding, true, targetsListener);
        super.readerInstance = this;
        state = S_NONE;

        this.monitor = new ProgressMonitor(parent, "Reading targets from \"" + fileName + "\"",
                                           null, 0, 100);
        this.monitor.setProgress(0);
        this.monitor.setMillisToPopup(0);
        this.progressScaleFactor = 100.00 / length;
    }


    public void run()
    {
        super.read();
    }


    protected void initialize()
    {
        try
        {
            this.patternField = this.getClass().getDeclaredField(INPUT_TEXT);
            this.thatField = this.getClass().getDeclaredField(INPUT_THAT);
            this.topicField = this.getClass().getDeclaredField(INPUT_TOPIC);
            this.templateField = this.getClass().getDeclaredField(REPLY);
        }
        catch (NoSuchFieldException e)
        {
            throw new DeveloperError("The developer has specified a field that does not exist in TargetsReader.");
        }
        catch (SecurityException e)
        {
            throw new DeveloperError("Security manager prevents TargetsReader from functioning.");
        }
    }


    protected void tryStates() throws TransitionMade
    {
        // Update the progress monitor (if in use).
        if (monitor != null)
        {
            // Check if it has been cancelled.
            if (monitor.isCanceled())
            {
                monitor.close();
                done = true;
                return;
            }

            // Update the progress.
            monitor.setProgress((int)(byteCount * progressScaleFactor));
        }

        switch (state)
        {
            case S_NONE :
                transition(Targeting.TARGETS_START, S_IN_TARGETS);
                break;

            case S_IN_TARGETS :
                transition(Targeting.TARGET_START, S_IN_TARGET);
                break;

            case S_IN_TARGET :
                transition(Targeting.INPUT_START, S_IN_INPUT, SET_INPUT_CONTEXT);
                transition(Targeting.MATCH_START, S_IN_MATCH, SET_MATCH_CONTEXT);
                transition(Targeting.REPLY_START, S_IN_REPLY, SET_REPLY_CONTEXT);
                transition(Targeting.TARGET_END, S_OUT_TARGET, DELIVER_TARGET);
                break;

            case S_IN_INPUT :
                transition(Targeting.TEXT_START, S_IN_TEXT);
                break;

            case S_IN_TEXT :
                transition(Targeting.TEXT_END, S_OUT_TEXT, patternField);
                break;

            case S_IN_MATCH :
                transition(Targeting.PATTERN_START, S_IN_PATTERN);
                break;

            case S_IN_PATTERN :
                transition(Targeting.PATTERN_END, S_OUT_PATTERN, patternField);
                break;

            case S_OUT_TEXT :
            case S_OUT_PATTERN :
                transition(Targeting.THAT_START, S_IN_THAT);
                break;

            case S_IN_THAT :
                transition(Targeting.THAT_END, S_OUT_THAT, thatField);
                break;

            case S_OUT_THAT :
                transition(Targeting.TOPIC_START, S_IN_TOPIC);
                break;

            case S_IN_TOPIC :
                transition(Targeting.TOPIC_END, S_OUT_TOPIC, topicField);
                break;

            case S_OUT_TOPIC :
                transition(Targeting.INPUT_END, S_IN_TARGET);
                transition(Targeting.TEMPLATE_START, S_IN_TEMPLATE);
                break;

            case S_IN_TEMPLATE :
                transition(Targeting.TEMPLATE_END, S_OUT_TEMPLATE, templateField);
                break;

            case S_OUT_TEMPLATE :
                transition(Targeting.MATCH_END, S_IN_TARGET);
                break;

            case S_IN_REPLY :
                transition(Targeting.REPLY_END, S_IN_TARGET, templateField);
                break;

            case S_OUT_TARGET :
                transition(Targeting.TARGET_START, S_IN_TARGET);
                transition(Targeting.TARGETS_END, SET_DONE);
                break;

            default :
                break;
        }
    }


    /**
     *  <p>
     *  If {@link #bufferString} contains
     *  <code>tag</code> at {@link #tagStart},
     *  sets {@link state} to <code>toState</code>,
     *  and performs the action indicated by <code>action</code>.
     *  </p>
     *  <p>
     *  If <code>action</code> is {@link #SET_INPUT_CONTEXT},
     *  sets {@link #patternField}, {@link #thatField} and {@link #topicField}
     *  to the appropriate field references for the input part of the target.
     *  </p>
     *  <p>
     *  If <code>action</code> is {@link #SET_MATCH_CONTEXT},
     *  sets {@link #patternField}, {@link #thatField}, {@link #topicField} and {@link #templateField}
     *  to the appropriate field references for the match part of the target.
     *  </p>
     *  <p>
     *  If <code>action</code> is {@link #SET_REPLY_CONTEXT},
     *  sets {@link #topicField} to the appropriate field reference for the reply part of the target.
     *  </p>
     *  <p>
     *  If <code>action</code> is {@link #DELIVER_TARGET}, calls the
     *  {@link loadTarget} method of the <code>targetsListener</code>.
     *  </p>
     *  If <code>action</code> is {@link #SET_DONE}, sets {@link #done} to
     *  <code>true</code>, so that parsing of this file is halted (no message given).
     *  </p>
     *
     *  @param tag          the tag to look for in {@link #buffer}
     *  @param toState      the parser {@link #state} to assign if successful
     *  @param action       one of {{{@link #SET_INPUT_CONTEXT}, {@link #SET_MATCH_CONTEXT},
     *                      {@link #SET_REPLY_CONTEXT}, {@link #DELIVER_TARGET}, {@link #SET_DONE}}}.
     *
     *  @throws TransitionMade if the transition is successfully made
     */
    private void transition(String tag, int toState, int action) throws TransitionMade
    {
        if (succeed(tag, toState))
        {
            switch (action)
            {
                case DELIVER_TARGET :
                    // Deliver new target to targetsListener.
                    ((TargetsReaderListener)super.listener).loadTarget(matchPattern,
                                                                       matchThat,
                                                                       matchTopic,
                                                                       matchTemplate,
                                                                       inputText,
                                                                       inputThat,
                                                                       inputTopic,
                                                                       reply);

                    // Reset all fields to defaults.
                    matchPattern = matchThat = matchTopic = ASTERISK;
                    inputText = inputThat = inputTopic =
                        matchTemplate = reply = EMPTY_STRING;

                    // Recreate the buffer (otherwise it gets huge).
                    buffer = new StringBuffer(Math.max(bufferStartCapacity, buffer.length()));
                    buffer.append(bufferString);

                    searchStart = 0;

                    break;

                case SET_DONE :
                    done = true;
                    break;

                case SET_INPUT_CONTEXT :
                    try
                    {
                        this.patternField = this.getClass().getDeclaredField(INPUT_TEXT);
                        this.thatField = this.getClass().getDeclaredField(INPUT_THAT);
                        this.topicField = this.getClass().getDeclaredField(INPUT_TOPIC);
                        this.templateField = null;
                    }
                    catch (NoSuchFieldException e)
                    {
                        throw new DeveloperError("The developer has specified a field that does not exist in TargetsReader.");
                    }
                    catch (SecurityException e)
                    {
                        throw new DeveloperError("Security manager prevents TargetsReader from functioning.");
                    }
                    break;

                case SET_MATCH_CONTEXT :
                    try
                    {
                        this.patternField = this.getClass().getDeclaredField(MATCH_PATTERN);
                        this.thatField = this.getClass().getDeclaredField(MATCH_THAT);
                        this.topicField = this.getClass().getDeclaredField(MATCH_TOPIC);
                        this.templateField = this.getClass().getDeclaredField(MATCH_TEMPLATE);
                    }
                    catch (NoSuchFieldException e)
                    {
                        throw new DeveloperError("The developer has specified a field that does not exist in TargetsReader.");
                    }
                    catch (SecurityException e)
                    {
                        throw new DeveloperError("Security manager prevents TargetsReader from functioning.");
                    }
                    break;


                case SET_REPLY_CONTEXT :
                    try
                    {
                        this.patternField = null;
                        this.thatField = null;
                        this.topicField = null;
                        this.templateField = this.getClass().getDeclaredField(REPLY);
                    }
                    catch (NoSuchFieldException e)
                    {
                        throw new DeveloperError("The developer has specified a field that does not exist in TargetsReader.");
                    }
                    catch (SecurityException e)
                    {
                        throw new DeveloperError("Security manager prevents TargetsReader from functioning.");
                    }
                    break;
            }
            throw(super.TRANSITION_MADE);
        }
    }


    /**
     *  Closes the progress monitor (if it is in use).
     */
    public void closeMonitor()
    {
        if (monitor != null)
        {
            monitor.close();
        }
    }
}
