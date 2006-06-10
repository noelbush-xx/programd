/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.logging;

import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;

/**
 * Accepts a ChatLogEvent if it comes from a specific bot id;
 * denies it otherwise.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class BotIDFilter extends Filter
{
    private String botid;

    /**
     * @see org.apache.log4j.spi.Filter#decide(org.apache.log4j.spi.LoggingEvent)
     */
    @Override
    public int decide(LoggingEvent event)
    {
        if (!(event instanceof ChatLogEvent))
        {
            return Filter.DENY;
        }
        return ((ChatLogEvent) event).getBotID().equals(this.botid) ? Filter.ACCEPT : Filter.DENY;
    }

    /**
     * @param id
     */
    public void setBotID(String id)
    {
        this.botid = id;
    }
}
