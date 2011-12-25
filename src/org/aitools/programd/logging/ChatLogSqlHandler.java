package org.aitools.programd.logging;

import org.apache.log4j.jdbcplus.JDBCSqlHandler;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Prepares statements for writing chat log events to a database.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class ChatLogSqlHandler implements JDBCSqlHandler {

  /**
   * @param event
   * @return a database statement
   */
  public static String getStatement(ChatLogEvent event) {
    return DBChatLogLayout.format(event);
  }

  /**
   * @see org.apache.log4j.jdbcplus.JDBCSqlHandler#getStatement(LoggingEvent)
   */
  @Override
  public String getStatement(LoggingEvent event) throws Exception {
    if (!(event instanceof ChatLogEvent)) {
      throw new IllegalArgumentException("ChatLogSqlHandler is intended to handle ChatLogEvents only.");
    }
    return ChatLogSqlHandler.getStatement((ChatLogEvent) event);
  }
}
