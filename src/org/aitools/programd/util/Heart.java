/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.util;

import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A Heart beats. At a configurable interval, it calls pulse() methods on some
 * objects (currently only the parent static Multiplexor) in order to provide
 * assurance that the bot server is alive.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class Heart
{
    /** The Timer object used to beat the heart. */
    private Timer timer;

    /** The pulse rate. */
    private int pulserate;

    /** Will hold a set of Pulses. */
    private ArrayList<Pulse> pulses = new ArrayList<Pulse>();

    /**
     * Creates a new Heart with the given pulse rate.
     * 
     * @param pulserateToUse the pulse rate to use
     */
    public Heart(int pulserateToUse)
    {
        this.pulserate = pulserateToUse;
    }

    /**
     * Starts the heart (if the pulse is greater than zero).
     */
    public void start()
    {
        int pulse = 0;
        try
        {
            pulse = 60000 / this.pulserate;
        }
        catch (NumberFormatException e)
        {
            // Do nothing.
        }
        if (pulse > 0)
        {
            startBeating(pulse);
        }
    }

    /**
     * Starts the heart beating with a specified pulse.
     * 
     * @param pulse the period in milliseconds
     */
    private void startBeating(int pulse)
    {
        this.timer = new Timer();
        this.timer.schedule(new HeartBeat(), 0, pulse);
    }

    /**
     * Adds a Pulse to the registered list.
     * 
     * @param pulse the Pulse to be added
     */
    public void addPulse(Pulse pulse)
    {
        this.pulses.add(pulse);
    }

    /**
     * Emits any registered pulses.
     */
    public synchronized void pulse()
    {
        for (Pulse pulse : this.pulses)
        {
            pulse.emit();
        }
    }

    class HeartBeat extends TimerTask
    {
        /**
         * @see java.util.TimerTask#run()
         */
        public void run()
        {
            pulse();
        }
    }
}