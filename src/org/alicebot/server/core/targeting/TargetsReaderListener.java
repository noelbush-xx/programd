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

import java.util.HashMap;

import org.alicebot.server.core.parser.GenericReaderListener;


/**
 *  Reads in new targets to a given Targets object.
 *  This version is based on {@link org.alicebot.server.core.loader.AIMLLoader}.
 *  Obviously this and its companion {@link TargetsReaderListener}
 *  duplicate a lot from AIMLReader and AIMLLoader,
 *  so once this is stabilized these should all be combined.
 *
 *  @author Noel Bush
 */
public class TargetsReaderListener implements GenericReaderListener
{
    /** The set in which to load the targets. */
    private static HashMap set;


    /**
     *  Loads a target into {@link TargetMaster}.
     *
     *  @param matchPattern     the <code>pattern</code> part of the matched path
     *  @param matchThat        the <code>that</code> part of the matched path
     *  @param matchTopic       the <code>topic</code> part of the matched path
     *  @param matchTemplate    the <code>template</code> associated with the matched path
     *  @param inputText        the input text that was matched
     *  @param inputThat        the value of the <code>that</code> predicate when the input was received
     *  @param inputTopic       the value of the <code>topic</code> predicate when the input was received
     *  @param reply            the reply from the bot
     */
    public void loadTarget(String matchPattern, String matchThat, String matchTopic, String matchTemplate,
                           String inputText, String inputThat, String inputTopic, String reply)
    {
        // Add new target to TargetingTool.
        TargetingTool.add(new Target(matchPattern, matchThat, matchTopic, matchTemplate,
                                    inputText, inputThat, inputTopic, reply),
                          this.set);
    }

    /**
     *  Initializes a TargetsReaderListener with the set
     *  in which to load the given targets.
     *
     *  @param set the set in which to load the targets
     */
    public TargetsReaderListener(HashMap set)
    {
        this.set = set;
    }
}
