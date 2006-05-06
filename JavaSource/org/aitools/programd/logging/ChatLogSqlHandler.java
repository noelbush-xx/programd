package org.aitools.programd.logging;

import org.apache.log4j.jdbcplus.JDBCSqlHandler;
import org.apache.log4j.spi.LoggingEvent;

public class ChatLogSqlHandler implements JDBCSqlHandler
{
    /**
     * @see org.apache.log4j.jdbcplus.JDBCSqlHandler#getStatement(LoggingEvent)
     */
    public String getStatement(LoggingEvent event) throws Exception
    {
        if (!(event instanceof ChatLogEvent))
        {
            throw new IllegalArgumentException("ChatLogSqlHandler is intended to handle ChatLogEvents only.");
        }
        return getStatement((ChatLogEvent) event);
    }

    public String getStatement(ChatLogEvent event)
    {
        return DBChatLogLayout.format(event);
    }
}
