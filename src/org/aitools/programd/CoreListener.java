/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd;


/**
 * A <code>CoreListener</code> can be notified by the {@link org.aitools.programd.Core Core}
 * about some events, and respond to them.
 * 
 * @author Noel Bush
 * @since 4.2
 */
public interface CoreListener
{
    /**
     * Signalling a <code>CoreListener</code> that the {@link org.aitools.programd.Core Core}
     * is <code>ready</code> means that that {@link org.aitools.programd.Core Core} is
     * prepared to interact.
     */
    public void coreReady();
    
    /**
     * Signals a <code>CoreListener</code> that a failure has occurred and the program will exit.
     * The {@link java.lang.Throwable Throwable} parameter allows an implementation of
     * <code>failure</code> to print a stack trace, or do whatever might be useful for
     * understanding the failure.
     * 
     * @param e     the {@link java.lang.Throwable Throwable} that, presumably, generated the failure
     */
    public void failure(Throwable e);
}
