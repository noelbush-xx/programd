// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: UserPrincipal.java,v 1.1.1.1 2001/06/17 19:01:08 noelbu Exp $
// ---------------------------------------------------------------------------

package org.alicebot.server.net.http;

import org.alicebot.server.net.http.util.Code;
import java.security.Principal;


/* ------------------------------------------------------------ */
/** User Principal.
 * Extends the security principal with a method to check if the user is in a
 * role. 
 *
 * @version $Id: UserPrincipal.java,v 1.1.1.1 2001/06/17 19:01:08 noelbu Exp $
 * @author Greg Wilkins (gregw)
 */
public interface UserPrincipal extends Principal
{
    static public String __ATTR="org.alicebot.server.net.http.UserPrincipal";
    
    public UserRealm getUserRealm();
    public boolean authenticate(String password);
    public boolean isUserInRole(String role);
}
