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

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import org.alicebot.server.core.logging.Trace;
import org.alicebot.server.core.logging.XMLLog;

public class TargetWriter extends Targeting
{
    /**
     *  Writes out a set of targets to a given file,
     *  deleting the file first.
     *
     *  @param file the File to which to write
     */
    public static void rewriteTargets(HashMap targets, File file)
    {
        if (!file.canWrite())
        {
            Trace.userinfo("Cannot write targets.");
            return;
        }
        file.delete();

        // Construct the type spec to use with XMLLog.
        String[] typeSpec = new String[] {file.getAbsolutePath(), TARGETS, null};

        Iterator targetsIterator = targets.values().iterator();
        if (targetsIterator.hasNext())
        {
            while (targetsIterator.hasNext())
            {
                write((Target)targetsIterator.next(), typeSpec);
            }
        }
        else
        {
            write(null, typeSpec);
        }
    }


    /**
     *  Writes a target to a given file.
     *
     *  @param target   the target to write
     *  @param file     the file to which to write
     */
    public static void write(Target target, File file)
    {
        // Create the file if it does not exist.
        if (!file.exists())
        {
            try
            {
                if (!file.createNewFile())
                {
                    return;
                }
            }
            catch (IOException e)
            {
                Trace.userinfo("Could not create new file \"" + file.getAbsolutePath() + "\".");
            }
        }

        // Be sure that the file is writable.
        if (!file.canWrite())
        {
            Trace.userinfo("Cannot write target to \"" + file.getAbsolutePath() + "\".");
            return;
        }

        // Construct the type spec to use with XMLLog.
        String[] typeSpec = new String[] {file.getAbsolutePath(), TARGETS, null};
        write(target, typeSpec);
    }


    /**
     *  Writes a target to a file defined by a given typespec.
     *
     *  @param target   the target to write
     *  @param typeSpec the typeSpec to use with XMLLog
     */
    public static void write(Target target, String[] typeSpec)
    {
        if (target != null)
        {
            // Store the target in the given file.
            XMLLog.log(     INDENT + TARGET_START + LINE_SEPARATOR +
                            INDENT + INDENT + INPUT_START + LINE_SEPARATOR +
                            INDENT + INDENT + INDENT + PATTERN_START + target.getLastInputText() + PATTERN_END + LINE_SEPARATOR +
                            INDENT + INDENT + INDENT + THAT_START + target.getLastInputThat() + THAT_END + LINE_SEPARATOR +
                            INDENT + INDENT + INDENT + TOPIC_START + target.getLastInputTopic() + TOPIC_END + LINE_SEPARATOR +
                            INDENT + INDENT + INPUT_END + LINE_SEPARATOR +
                            INDENT + INDENT + MATCH_START + LINE_SEPARATOR +
                            INDENT + INDENT + INDENT + PATTERN_START + target.getMatchPattern() + PATTERN_END + LINE_SEPARATOR +
                            INDENT + INDENT + INDENT + THAT_START + target.getMatchThat() + THAT_END + LINE_SEPARATOR +
                            INDENT + INDENT + INDENT + TOPIC_START + target.getMatchTopic() + TOPIC_END + LINE_SEPARATOR +
                            INDENT + INDENT + MATCH_END + LINE_SEPARATOR +
                            INDENT + INDENT + EXTENSION_START + LINE_SEPARATOR +
                            INDENT + INDENT + INDENT + PATTERN_START + target.getExtensionPattern() + PATTERN_END + LINE_SEPARATOR +
                            INDENT + INDENT + INDENT + THAT_START + target.getExtensionThat() + THAT_END + LINE_SEPARATOR +
                            INDENT + INDENT + INDENT + TOPIC_START + target.getExtensionTopic() + TOPIC_END + LINE_SEPARATOR +
                            INDENT + INDENT + EXTENSION_END + LINE_SEPARATOR +
                            INDENT + TARGET_END + LINE_SEPARATOR,
                        typeSpec);
        }
        else
        {
            XMLLog.log(EMPTY_STRING, typeSpec);
        }
    }
}
