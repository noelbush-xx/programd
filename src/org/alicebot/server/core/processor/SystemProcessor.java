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
    - complete javadoc
    - made all imports explicit
*/

/*
    Further optimizations {4.1.3 [01] - November 2001, Noel Bush)
    - changed to extend (not implement) AIMLProcessor (latter is now an abstract class)
      (includes necessary public field "label")
*/

/*
    More fixes (4.1.3 [02] - November 2001, Noel Bush
    - added check of server property for allowing/disallowing use
*/

package org.alicebot.server.core.processor;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Reader;
import java.util.StringTokenizer;

import org.alicebot.server.core.Globals;
import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.parser.TemplateParser;
import org.alicebot.server.core.parser.XMLNode;
import org.alicebot.server.core.util.Toolkit;
import org.alicebot.server.core.util.Trace;


/**
 *  <p>
 *  Handles a
 *  <code><a href="http://www.alicebot.org/TR/2001/WD-aiml/#section-system">system</a></code>
 *  element.
 *  </p>
 *  <p>
 *  No attempt is made to check whether the command passed to the OS interpreter
 *  is harmful.
 *  </p>
 *
 *  @version    4.1.3
 *  @author     Jon Baer
 *  @author     Mark Anacker
 *  @author     Thomas Ringate, Pedro Colla
 */
public class SystemProcessor extends AIMLProcessor
{
    public static final String label = "system";

    /** Known names of Unix operating systems, which tend to require the array form of Runtime.exec(). */
    private static final String[] arrayFormOSnames = { "mac os x",
                                                       "linux",
                                                       "solaris",
                                                       "sunos",
                                                       "mpe",
                                                       "hp-ux",
                                                       "pa_risc",
                                                       "aix",
                                                       "freebsd",
                                                       "irix",
                                                       "unix" }; 


    /** Whether to use the array form of Runtime.exec(). */
    private static boolean useArrayExecForm;

    /**
     *  Tries to guess whether to use the array form of Runtime.exec().
     */
    static
    {
        String os = System.getProperty("os.name").toLowerCase();
        for (int index = arrayFormOSnames.length; --index >= 0; )
        {
            if (os.indexOf(arrayFormOSnames[index]) != -1)
            {
                useArrayExecForm = true;
            }
        }
    }

    public String process(int level, XMLNode tag, TemplateParser parser) throws AIMLProcessorException
    {
        // Don't use the system tag if not permitted.
        if (!Globals.osAccessAllowed())
        {
            Log.userinfo("Use of <system> prohibited!", Log.SYSTEM);
            return EMPTY_STRING;
        }

        String directoryPath = Globals.getSystemDirectory();
        String prefix = Globals.getSystemPrefix();

        if (tag.XMLType == XMLNode.TAG)
        {
            String response = parser.evaluate(level++, tag.XMLChild);
            if (prefix != null)
            {
                response = prefix + response;
            }
            String output = EMPTY_STRING;
            response = response.trim();
            Log.log("<system> call:", Log.SYSTEM);
            Log.log(response, Log.SYSTEM);
            try
            {
                File directory = null;
                if (directoryPath != null)
                {
                    Log.log("Executing <system> call in \"" + directoryPath + "\"", Log.SYSTEM);
                    directory = new File(directoryPath);
                    if (!directory.isDirectory())
                    {
                        Log.userinfo("programd.interpreter.system.directory (\"" + directoryPath + "\") does not exist or is not a directory.", Log.SYSTEM);
                        return EMPTY_STRING;
                    }
                }
                else
                {
                    Log.userinfo("No programd.interpreter.system.directory defined!", Log.SYSTEM);
                    return EMPTY_STRING;
                }
                Process child;
                if (useArrayExecForm)
                {
                    child = Runtime.getRuntime().exec((String[])Toolkit.wordSplit(response).toArray(new String[]{}),
                                                      null, directory);
                }
                else
                {
                    child = Runtime.getRuntime().exec(response, null, directory);
                }
                if (child == null)
                {
                    Log.userinfo("Could not get separate process for <system> command.", Log.SYSTEM);
                    return EMPTY_STRING;
                }

                try
                {
                    child.waitFor();
                }
                catch (InterruptedException e)
                {
                    Log.userinfo("System process interruped; could not complete.", Log.SYSTEM);
                    return EMPTY_STRING;
                }

                InputStream in = child.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = br.readLine()) != null)
                {
                    output = output + line + "\n";
                }

                Log.log("output:", Log.SYSTEM);
                Log.log(output, Log.SYSTEM);

                response = output;
                in.close();
                Log.userinfo("System process exit value: " + child.exitValue(), Log.SYSTEM);
            }
            catch (IOException e)
            {
                Log.userinfo("Cannot execute <system> command.  Response logged.", Log.SYSTEM);
                StringTokenizer lines = new StringTokenizer(e.getMessage(), System.getProperty("line.separator"));
                while (lines.hasMoreTokens())
                {
                    Log.log(lines.nextToken(), Log.SYSTEM);
                }
            }

            return response.trim();
        }
        else
        {
            throw new AIMLProcessorException("<system></system> must have content!");
        }
    }
}
