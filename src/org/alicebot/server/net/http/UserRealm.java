// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: UserRealm.java,v 1.1.1.1 2001/06/17 19:01:08 noelbu Exp $
// ---------------------------------------------------------------------------

package org.alicebot.server.net.http;

/* ------------------------------------------------------------ */
/** User Realm.
 *
 * @version $Id: UserRealm.java,v 1.1.1.1 2001/06/17 19:01:08 noelbu Exp $
 * @author Greg Wilkins (gregw)
 */
public interface UserRealm
{
    static public String __UserRole="org.alicebot.server.net.http.User";
    
    public String getName();

    public UserPrincipal getUser(String username, HttpRequest request);
}
