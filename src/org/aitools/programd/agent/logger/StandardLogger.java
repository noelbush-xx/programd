/*    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
    USA.
*/

package org.aitools.programd.agent.logger;

import org.aitools.programd.multiplexor.PredicateMaster;
import org.aitools.programd.util.Globals;
import org.aitools.programd.util.XMLKit;
import org.aitools.programd.util.logging.Log;

/**
 *  Provides a common logging method that should be used by
 *  all Responders unless there's a good reason not to.
 *
 *  @author  Noel Bush
 */
public class StandardLogger
{
    /** The &quot;prompt&quot; to use when displaying/logging output. */
    protected static final String PROMPT = "> ";

    public static void log(String input, String response, String hostname, String userid, String botid)
    {
        // Break lines at tags.
        String[] lines = XMLKit.breakLinesAtTags(input);

        String clientName = PredicateMaster.get(Globals.getClientNamePredicate(), userid, botid);

        // Log the input.
        for (int index = 0; index < lines.length; index++)
        {
            Log.log(clientName + PROMPT + lines[index], Log.CHAT);
        }

        // Break lines at tags.
        lines = XMLKit.breakLinesAtTags(response);

        // Log the response.
        for (int index = 0; index < lines.length; index++)
        {
            Log.log(botid + PROMPT + lines[index], Log.CHAT);
        }
    }
}
