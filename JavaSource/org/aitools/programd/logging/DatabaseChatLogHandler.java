/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.logging;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

import org.aitools.programd.CoreSettings;
import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.UserError;
import org.aitools.programd.util.XMLKit;
import org.aitools.programd.util.sql.DbAccess;
import org.aitools.programd.util.sql.DbAccessRefsPoolMgr;
import org.apache.log4j.Logger;

/**
 * Provides a logging handler that writes to a database.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class DatabaseChatLogHandler extends Handler
{
    /** A manager for database access (is this really necessary?). */
    private static DbAccessRefsPoolMgr dbManager;

    /** The string &quot;{@value}&quot; (for character encoding conversion). */
    private static final String ENC_UTF8 = "utf-8";

    /** The (file-based) logger for database activity. */
    private Logger logger;

    /**
     * Creates a new DatabaseChatLogHandler.
     * 
     * @param settings the Core settings to use
     */
    public DatabaseChatLogHandler(CoreSettings settings)
    {
        this.logger = Logger.getLogger("programd");
        this.logger.debug("Opening database pool.");

        dbManager = new DbAccessRefsPoolMgr(settings.getDatabaseDriver(), settings.getDatabaseUrl(), settings.getDatabaseUser(), settings
                .getDatabasePassword());

        this.logger.debug("Populating database pool.");

        dbManager.populate(settings.getDatabaseConnections());
    }

    @Override
    public void publish(LogRecord record)
    {/*
        if (!(record instanceof ChatLogEvent))
        {
            throw new IllegalArgumentException(
                    "DatabaseChatLogHandler is intended to handle ChatRecords only.");
        }
        publish((ChatLogEvent) record);*/
    }

    /**
     * Publishes the given record to the database.
     * 
     * @param record the record to publish
     */
    public void publish(ChatLogEvent record)
    {
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
            dbaRef.executeQuery("insert into chatlog (userid, botid, input, response) values ('" + URLEncoder.encode(record.getUserID(), ENC_UTF8)
                    + "', '" + URLEncoder.encode(record.getBotID(), ENC_UTF8) + "', '" + URLEncoder.encode(record.getInput(), ENC_UTF8) + "', '"
                    + URLEncoder.encode(XMLKit.removeMarkup(record.getReply()), ENC_UTF8) + "')");
        }
        catch (UnsupportedEncodingException e)
        {
            throw new DeveloperError("This platform does not support UTF-8!", e);
        }

        dbManager.returnDbaRef(dbaRef);
    }

    /**
     * @see java.util.logging.Handler#flush()
     */
    @Override
    public void flush()
    {
        // Nothing to do.
    }

    @Override
    public void close()
    {
        // Nothing to do.
    }
}