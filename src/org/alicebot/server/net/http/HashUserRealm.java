// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: HashUserRealm.java,v 1.1.1.1 2001/06/17 19:00:36 noelbu Exp $
// ---------------------------------------------------------------------------

package org.alicebot.server.net.http;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import org.alicebot.server.net.http.util.Code;
import org.alicebot.server.net.http.util.Password;
import org.alicebot.server.net.http.util.Resource;

/* ------------------------------------------------------------ */
/** HashMapped User Realm.
 *
 * @see Password
 * @version $Id: HashUserRealm.java,v 1.1.1.1 2001/06/17 19:00:36 noelbu Exp $
 * @author Greg Wilkins (gregw)
 */
public class HashUserRealm extends HashMap
    implements UserRealm
{
    private String _name;
    private HashMap _roles=new HashMap(7);

    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param name 
     */
    public HashUserRealm(String name)
    {
        _name=name;
    }
    
    /* ------------------------------------------------------------ */
    /** Constructor. 
     * @param name Realm name
     * @param config Filename or url of user properties file.
     */
    public HashUserRealm(String name, String config)
        throws IOException
    {
        _name=name;
        load(config);
    }

    /* ------------------------------------------------------------ */
    /** Load realm users from properties file.
     * The property file maps usernames to password specs followed by
     * an optional coma separated list of role names.
     * @param config Filename or url of user properties file.
     * @exception IOException 
     */
    public void load(String config)
        throws IOException
    {
        Code.debug("Load ",this," from ",config);
        Properties properties = new Properties();
        Resource resource=Resource.newResource(config);
        properties.load(resource.getInputStream());

        Iterator iter = properties.entrySet().iterator();
        while(iter.hasNext())
        {
            Map.Entry entry = (Map.Entry)iter.next();

            String username=entry.getKey().toString().trim();
            String credentials=entry.getValue().toString().trim();
            String roles=null;
            int c=credentials.indexOf(",");
            if (c>0)
            {
                roles=credentials.substring(c+1).trim();
                credentials=credentials.substring(0,c).trim();
            }

            if (username!=null && username.length()>0 &&
                credentials!=null && credentials.length()>0)
            {
                put(username,credentials);
                if(roles!=null && roles.length()>0)
                {
                    StringTokenizer tok = new StringTokenizer(roles,", ");
                    while (tok.hasMoreTokens())
                        addUserToRole(username,tok.nextToken());
                }
            }
        }
    }
    
    /* ------------------------------------------------------------ */
    public String getName()
    {
        return _name;
    }

    /* ------------------------------------------------------------ */
    public synchronized UserPrincipal getUser(String username,
                                              HttpRequest request)
    {
        return (UserPrincipal)get(username);
    }

    /* ------------------------------------------------------------ */
    /** Put user into realm.
     * @param name User name
     * @param credentials String password, Password or UserPrinciple
     *                    instance. 
     * @return Old UserPrinciple value or null
     */
    public Object put(Object name, Object credentials)
    {
        if (credentials instanceof UserPrincipal)
            return super.put(name.toString(),credentials);
        if (credentials instanceof Password)
            return super.put(name,new User(name.toString(),(Password)credentials));
        if (credentials != null)
            return super.put(name,new User(name.toString(),new Password(_name,credentials.toString())));
        return null;
    }

    /* ------------------------------------------------------------ */
    /** Add a user to a role.
     * @param userName 
     * @param roleName 
     */
    public synchronized void addUserToRole(String userName, String roleName)
    {
        HashSet userSet = (HashSet)_roles.get(roleName);
        if (userSet==null)
        {
            userSet=new HashSet(11);
            _roles.put(roleName,userSet);
        }
        userSet.add(userName);
    }
    
    /* ------------------------------------------------------------ */
    /** Check if a user is in a role.
     * All users are in the role "org.alicebot.server.net.http.User".
     * @param user The user, which must be from this realm 
     * @param roleName 
     * @return True if the user can act in the role.
     */
    public boolean isUserInRole(UserPrincipal user, String roleName)
    {
        if (user==null || user.getUserRealm()!=this)
            return false;

        if (UserRealm.__UserRole.equals(roleName))
            return true;
        
        HashSet userSet = (HashSet)_roles.get(roleName);
        return userSet!=null && userSet.contains(user.getName());
    }

    /* ------------------------------------------------------------ */
    public String toString()
    {
        return "Realm["+_name+"]";
    }
    
    /* ------------------------------------------------------------ */
    public void dump(PrintStream out)
    {
        out.println(this+":");
        out.println(super.toString());
        out.println(_roles);
    }
    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private class User implements UserPrincipal
    {
        private String _name;
        private Password _pw;
        
        /* -------------------------------------------------------- */
        User(String name,Password password)
        {
            _name=name;
            _pw=password;
            _pw.zero();
        }

        /* ------------------------------------------------------------ */
        public UserRealm getUserRealm()
        {
            return HashUserRealm.this;
        }
        
        /* ------------------------------------------------------------ */
        public String getName()
        {
            return _name;
        }
        
        /* -------------------------------------------------------- */
        public boolean authenticate(String password)
        {
            return _pw!=null && _pw.equals(password);
        }
        
        /* -------------------------------------------------------- */
        public boolean isUserInRole(String role)
        {
            return HashUserRealm.this.isUserInRole(this,role);
        }

        /* ------------------------------------------------------------ */
        public String toString()
        {
            return _name+"["+_pw+"]";
        }
    }
}
