package org.aitools.programd.logging;

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

public class ChatLogEventFilter extends Filter
{
    @Override
    public int decide(LoggingEvent event)
    {
        return event instanceof ChatLogEvent ? Filter.NEUTRAL : Filter.DENY;
    }
}
