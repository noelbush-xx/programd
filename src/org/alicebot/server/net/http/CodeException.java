// ========================================================================
// Copyright (c) 1997 MortBay Consulting, Sydney
// $Id: CodeException.java,v 1.1.1.1 2001/06/17 19:01:14 noelbu Exp $
// ========================================================================

package org.alicebot.server.net.http;

/* ------------------------------------------------------------ */
/** Code Exception.
 * 
 * Thrown by Code.assert or Code.fail
 * @see Code
 * @version  $Id: CodeException.java,v 1.1.1.1 2001/06/17 19:01:14 noelbu Exp $
 * @author Greg Wilkins
 */
public class CodeException extends RuntimeException
{
    /* ------------------------------------------------------------ */
    /** Default constructor. 
     */
    public CodeException()
    {
        super();
    }

    public CodeException(String msg)
    {
        super(msg);
    }    
}

