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
import java.util.StringTokenizer;

import org.alicebot.server.core.Globals;
import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.parser.AIMLParser;
import org.alicebot.server.core.parser.XMLNode;

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


    public String process(int level, String userid, XMLNode tag, AIMLParser parser) throws InvalidAIMLException
    {
        // Don't use the system tag if not permitted.
        if (!Globals.osAccessAllowed())
        {
            Log.userinfo("Use of <system> prohibited!", Log.SYSTEM);
            return EMPTY_STRING;
        }

        if (tag.XMLType == XMLNode.TAG)
        {
            String response = parser.evaluate(level++, "localhost", tag.XMLChild);
            String output = EMPTY_STRING;
            try
            {
                File cwd = null;
                String currentdir = Globals.getProperty("programd.home");
                if (currentdir != null)
                {
                    cwd = new File(currentdir);
                }
                Process child = Runtime.getRuntime().exec(response, null, cwd);

                InputStream in = child.getInputStream();
                BufferedReader br = new BufferedReader(new InputStreamReader(in));
                String line;
                while ((line = br.readLine()) != null)
                {
                    output = output + line + "<br />\n";
                }

                Log.log("<system> call:", Log.SYSTEM);
                Log.log(response, Log.SYSTEM);
                Log.log("output:", Log.SYSTEM);
                Log.log(output, Log.SYSTEM);

                response = output;
                in.close();
            }
            catch (IOException e)
            {
                Log.userinfo("Cannot execute \"" + response + "\".  Response:", Log.SYSTEM);
                StringTokenizer lines = new StringTokenizer(e.getMessage(), System.getProperty("line.separator"));
                while (lines.hasMoreTokens())
                {
                    Log.userinfo(lines.nextToken(), Log.SYSTEM);
                }
            }

            return response;
        }
        else
        {
            throw new InvalidAIMLException("<system></system> must have content!");
        }
    }
}
