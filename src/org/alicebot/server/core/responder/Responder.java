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
    - removed useless imports
    - added notes about possible method name & signature changes in future
*/

/*
    More fixes (4.1.3 [02] - November 2001, Noel Bush)
    - added log(String, String, String, String, String) method
    - deprecated log(String, String, String)
    - removed log(String, String, String)
    - removed underscores from method names
*/

package org.alicebot.server.core.responder;


/**
 *  A <code>Responder</code> is an object that processes
 *  and logs input via a given channel (text, html, flash, etc.).
 *
 *  @author Jon Baer
 *  @author Noel Bush
 */
public interface Responder
{
    /**
     *  <p>
     *  Preprocesses a message from a bot as appropriate for a
     *  given channel.
     *  </p>
     *  <p>
     *  For some channels, this may involve doing substitutions
     *  on the message; for some channels this may mean formatting
     *  the message in a particular way, etc.
     *  </p>
     *
     *  @param message  the message to be formatted
     *  @param hostname the name of the host that is generating the message
     *
     *  @return the result of preprocessing the message
     */
    String preprocess(String message, String hostname);


    /**
     *  Logs an input/response pair to the appropriate place.
     *
     *  @param input    the client's input
     *  @param reply    the bot's response
     *  @param hostname the name of the host
     */
    void log(String input, String reply, String hostname, String userid, String botid);


    /**
     *  <p>
     *  Response by a <code>Responder</code> is considered an
     *  &quot;append&quot; to some previous string.  That string
     *  might be some form of the previous exchange, or it might
     *  be nothing.
     *  </p>
     *
     *  @param input    an input from the client
     *  @param reply    a reply from the bot
     *  @param appendTo the string to which the client of the
                        <code>Responder</code> may expect the reply to be appended
     *
     *  @return the reply, in whatever fashion the <code>Responder</code> has decided to modify it
     */
    String append(String input, String reply, String appendTo);


    /**
     *  <p>
     *  Postprocesses a message from a bot as appropriate for a
     *  given channel.
     *  </p>
     *  <p>
     *  For some channels, this may involve doing substitutions
     *  on the message; for some channels this may mean formatting
     *  the message in a particular way, etc.
     *  </p>
     *
     *  @param message  the message to be formatted
     *  @param hostname the name of the host that is generating the message
     *
     *  @return the result of postprocessing the message
     */
    String postprocess(String reply);
}

