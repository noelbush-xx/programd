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

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.SQLException;

import org.alicebot.server.core.Globals;
import org.alicebot.server.core.PredicateMaster;
import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.logging.XMLLog;
import org.alicebot.server.core.util.DeveloperError;
import org.alicebot.server.core.util.UserError;
import org.alicebot.server.sql.pool.DbAccess;
import org.alicebot.server.sql.pool.DbAccessRefsPoolMgr;


/**
 *  Provides a logging method that writes to a database.
 *
 *  @author  Noel Bush
 */
public class ResponderDatabaseLogger
{
    /** A manager for database access (is this really necessary?). */
    private static DbAccessRefsPoolMgr dbManager;
    
    /** Only needs to be one of these! */
    private static final ResponderDatabaseLogger self = new ResponderDatabaseLogger();

    /** The string &quot;utf-8&quot; (for character encoding conversion). */
    private static final String ENC_UTF8              = "utf-8";


    /**
     *	Private constructor to help enforce singleton pattern.
     */
    private ResponderDatabaseLogger()
    {
        Log.devinfo("Opening database pool.", new String[] {Log.DATABASE, Log.STARTUP});

        dbManager = new DbAccessRefsPoolMgr(Globals.getProperty("programd.database.driver", ""),
                                            Globals.getProperty("programd.database.url", ""),
                                            Globals.getProperty("programd.database.user", ""),
                                            Globals.getProperty("programd.database.password", ""));

        Log.devinfo("Populating database pool.", new String[] {Log.DATABASE, Log.STARTUP});

        dbManager.populate(Integer.parseInt(Globals.getProperty("programd.database.connections", "")));

    }
    
    
    public static void log(String input, String response, String hostname, String userid, String botid)
    {
        // Get the client name.
        String clientName = PredicateMaster.get(Globals.getClientNamePredicate(), userid, botid);
        
        // Get a database reference.
        DbAccess dbaRef = null;
        try
        {
            dbaRef = dbManager.takeDbaRef();
        }
        catch (Exception e)
        {
            throw new UserError("Could not get database reference when logging.", e);
        }
        
        // Write the log info.
        try
        {
            dbaRef.executeQuery("insert into chatlog (userid, clientname, botid, input, response) values ('" +
                                URLEncoder.encode(userid, ENC_UTF8) + "', '" +
                                URLEncoder.encode(PredicateMaster.get(Globals.getClientNamePredicate(), userid, botid), ENC_UTF8) + "', '" +
                                URLEncoder.encode(botid, ENC_UTF8) + "', '" +
                                URLEncoder.encode(input, ENC_UTF8) + "', '" +
                                URLEncoder.encode(response, ENC_UTF8) + "')");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new DeveloperError("This platform does not support UTF-8!");
        }
        catch (SQLException e)
        {
            Log.userinfo("Database error: " + e, new String[]{Log.DATABASE, Log.ERROR});
        }

     dbManager.returnDbaRef(dbaRef);
 }
}
