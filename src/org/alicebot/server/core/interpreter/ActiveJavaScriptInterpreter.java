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

package org.alicebot.server.core.interpreter;

import org.alicebot.server.core.Globals;
import org.alicebot.server.core.Interpreter;
import org.alicebot.server.core.util.DeveloperError;


/**
 *  Manages a JavaScript interpreter.
 *
 *  @author Noel Bush
 */
abstract public class ActiveJavaScriptInterpreter implements Interpreter
{
    /** The {@link Interpreter} managed by the instance of this class. */
    private static Interpreter interpreter;


    /**
     *  Private constructor that prevents instantiating this class.
     */
    private ActiveJavaScriptInterpreter()
    {
    }


    /**
     *  Initializes the <code>ActiveInterpreter</code>
     *  with an implementation of {@link Interpreter}.
     *
     *  @param className    the name of the subclass of {@link Interpreter} that should be used
     */
    static
    {
        try
        {
            interpreter = (Interpreter)Class.forName(Globals.javaScriptInterpreter()).newInstance();
        }
        catch (Exception e)
        {
            throw new DeveloperError(e);
        }
    }


    /**
     *  Prohibits cloning this class.
     */
    protected Object clone() throws CloneNotSupportedException
    {
        throw new CloneNotSupportedException();
    }


    public static Interpreter getInstance()
    {
        return interpreter;
    }
}
