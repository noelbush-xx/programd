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

/*
    4.1.4 [00] - December 2001, Noel Bush
    - made this an interface and moved implementation to
      org.alicebot.server.core.interpreter.RhinoInterpreter
*/

package org.alicebot.server.core;


/**
 *  An <code>Interpreter</code> handles some server-side script.
 */
abstract public interface Interpreter
{
    /**
     *  Evaluates a given JavaScript expression
     *  for a given userid.
     *
     *  @param expression   the expression to evaluate
     */
    public String evaluate(String expression);
}

