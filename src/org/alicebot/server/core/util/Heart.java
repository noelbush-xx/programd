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

package org.alicebot.server.core.util;

import java.util.Timer;
import java.util.TimerTask;

import org.alicebot.server.core.Globals;
import org.alicebot.server.core.Multiplexor;


/**
 *  A Heart beats.  At a configurable interval, it
 *  calls pulse() methods on some objects (currently only
 *  the parent static Multiplexor) in order to provide assurance
 *  that the bot server is alive.
 *
 *  @author	Noel Bush
 */
public class Heart
{
    /** The Timer object used to beat the heart. */
    private Timer timer;
    
    /** A bot server can have only one Heart. */
    private static final Heart self = new Heart();
    
    
    /**
     *  Do not allow anyone to create a Heart.
     */
    private Heart()
    {
    }
    
    
    /**
     *  Do not allow anyone to clone a Heart.
     */
    protected Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }
    
    
    /**
     *  Starts the heart (if the pulse is greater than zero).
     */
    public static void start()
    {
        int pulse = 0;
        try
        {
            pulse = 60000 / Integer.parseInt(Globals.getProperty("programd.heart.pulserate"));
        }
        catch (NumberFormatException e)
        {
        }
        if (pulse > 0)
        {
            self.startBeating(pulse);
        }
    }
    
    
    /**
     *  Starts the heart beating with a specified pulse.
     *
     *  @param pulse	the period in milliseconds
     */
    private void startBeating(int pulse)
    {
        timer = new Timer();
        timer.schedule(new HeartBeat(), 0, pulse);
    }
    
    
    class HeartBeat extends TimerTask
    {
        public void run()
        {
            Multiplexor.pulse();
        }
    }
}