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

package org.alicebot.server.core.responder;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.alicebot.server.core.Bots;
import org.alicebot.server.core.Globals;
import org.alicebot.server.core.PredicateMaster;
import org.alicebot.server.core.logging.XMLLog;
import org.alicebot.server.core.util.Toolkit;


/**
 *  Provides a logging method that generates XML-formatted exchanges.
 *
 *  @author  Noel Bush
 */
public class ResponderXMLLogger
{
    // Class variables.

    /** The timestamp format for logging. */
    private static final String TIMESTAMP_LOG_FORMAT = Globals.getProperty("programd.logging.timestamp-format", "yyyy-MM-dd H:mm:ss");

    /** The start of an exchange. */
    private static final String EXCHANGE_START   = "<exchange>";

    /** The start of a timestamp element. */
    private static final String TIMESTAMP_START  = "<timestamp>";

    /** The end of a timestamp element. */
    private static final String TIMESTAMP_END    = "</timestamp>";

    /** The start of a userid element. */
    private static final String USERID_START     = "<userid>";

    /** The end of a userid element. */
    private static final String USERID_END       = "</userid>";

    /** The start of a clientname element. */
    private static final String CLIENTNAME_START = "<clientname>";

    /** The end of a clientname element. */
    private static final String CLIENTNAME_END   = "</clientname>";

    /** The start of a botid element. */
    private static final String BOTID_START      = "<botid>";

    /** The end of a botid element. */
    private static final String BOTID_END        = "</botid>";

    /** The start of an input. */
    private static final String INPUT_START      = "<input>";

    /** The end of an input. */
    private static final String INPUT_END        = "</input>";

    /** The start of a response. */
    private static final String RESPONSE_START   = "<response>";

    /** The end of a response. */
    private static final String RESPONSE_END     = "</response>";

    /** The end of an exchange. */
    private static final String EXCHANGE_END     = "</exchange>";

    /** The system line separator string. */
    private static final String LINE_SEPARATOR   = System.getProperty("line.separator", "\n");

    /** An indent. */
    private static final String INDENT = "    ";


    public static void log(String input, String response, String hostname, String userid, String botid)
    {
        // Get the client name.
        String clientName = PredicateMaster.get(Globals.getClientNamePredicate(), userid, botid);

        // Log the exchange.
        XMLLog.log(     INDENT + EXCHANGE_START + LINE_SEPARATOR +
                        INDENT + INDENT + TIMESTAMP_START +
                                 new SimpleDateFormat(TIMESTAMP_LOG_FORMAT).format(new Date()).trim() + 
                                 TIMESTAMP_END + LINE_SEPARATOR +
                        INDENT + INDENT + USERID_START + userid + USERID_END + LINE_SEPARATOR +
                        INDENT + INDENT + CLIENTNAME_START + clientName + CLIENTNAME_END + LINE_SEPARATOR +
                        INDENT + INDENT + BOTID_START + botid + BOTID_END + LINE_SEPARATOR +
                        INDENT + INDENT + INPUT_START + Toolkit.escapeXMLChars(input) + INPUT_END + LINE_SEPARATOR +
                        INDENT + INDENT + RESPONSE_START + response + RESPONSE_END + LINE_SEPARATOR +
                        INDENT + EXCHANGE_END + LINE_SEPARATOR,
                   Bots.getBot(botid).getChatlogSpec());
    }
}
