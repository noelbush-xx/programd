/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.logging;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.aitools.util.xml.XHTML;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Formats a ChatLogEvent by printing a number of extra fields as we like them.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class SimpleChatLogLayout extends SimpleLayout {

  private SimpleDateFormat timestampFormat;

  private boolean showTimestamp;

  /**
   * Creates a new SimpleFormatter with the given Core settings.
   */
  public SimpleChatLogLayout() {
    super();
  }

  /**
   * @param event the ChatLogEvent to format
   * @return the result of formatting the given ChatLogEvent
   */
  public String format(ChatLogEvent event) {
    String[] responseLines = XHTML.breakLines(event.getReply());
    StringBuilder result = new StringBuilder();
    int responseLineCount = responseLines.length;
    String datetime = null;
    if (this.showTimestamp) {
      datetime = this.timestampFormat.format(new Date(event.timeStamp));
      result.append(String.format("[%s] %s> %s%n", datetime, event.getUserID(), event.getInput()));
    }
    else {
      result.append(String.format("%s> %s%n", event.getUserID(), event.getInput()));
    }
    for (int index = 0; index < responseLineCount; index++) {
      if (this.showTimestamp) {
        result.append(String.format("[%s] %s> %s%n", datetime, event.getBotID(), responseLines[index]));
      }
      else {
        result.append(String.format("%s> %s%n", event.getBotID(), responseLines[index]));
      }
    }
    return result.toString();
  }

  /**
   * We insist that the event be a ChatLogEvent.
   * 
   * @param event the ChatLogEvent to format
   * @return the result of formatting the given ChatLogEvent
   * @throws IllegalArgumentException if the record is not a ChatLogEvent
   */
  @Override
  public String format(LoggingEvent event) {
    if (!(event instanceof ChatLogEvent)) {
      throw new IllegalArgumentException("XMLChatLogLayout is intended to handle ChatLogEvents only.");
    }
    return this.format((ChatLogEvent) event);
  }

  /**
   * Sets the timestamp format to the given format.
   * 
   * @param format the timestamp format to use
   */
  public void setTimestampFormat(String format) {
    if (format.length() > 0) {
      this.timestampFormat = new SimpleDateFormat(format);
      this.showTimestamp = true;
    }
    else {
      this.showTimestamp = false;
    }
  }
}
