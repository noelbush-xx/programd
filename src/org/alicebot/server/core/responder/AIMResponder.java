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
    More fixes (4.1.3 [02] - November 2001, Noel Bush)
    - formatting cleanup
    - general grammar fixes
    - complete javadoc
    - made all imports explicit
    - made a subclass of StandardLoggingResponder
*/

package org.alicebot.server.core.responder;

public class AIMResponder implements Responder
{
    // Convenience constants.

    /** An empty string. */
    private static final String EMPTY_STRING = "";

    public AIMResponder()
    {
    }
    

    public String preprocess(String input, String hostname)
    {
        return input;
    }


    public String append(String input, String response, String appendTo)
    {
        return appendTo + response;
    }
    

    public void log(String input, String reply, String hostname, String userid, String botid)
    {
        ResponderXMLLogger.log(input, reply, hostname, userid, botid);
    }

    
    public String postprocess(String reply)
    {
        String postReply = EMPTY_STRING;
        if (reply.length() > 1024)
        {
            postReply = "Huh?";
        }
        else
        {
            postReply = reply;
        }
        return postReply;
    }
} 
