// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: SecurityHandler.java,v 1.1.1.1 2001/06/17 19:02:27 noelbu Exp $
// ========================================================================

package org.alicebot.server.net.http.handler;

import org.alicebot.server.net.http.HttpException;
import org.alicebot.server.net.http.HttpFields;
import org.alicebot.server.net.http.HttpRequest;
import org.alicebot.server.net.http.HttpResponse;
import org.alicebot.server.net.http.PathMap;
import org.alicebot.server.net.http.SecurityConstraint;
import org.alicebot.server.net.http.HashUserRealm;
import org.alicebot.server.net.http.UserRealm;
import org.alicebot.server.net.http.UserPrincipal;
import org.alicebot.server.net.http.util.B64Code;
import org.alicebot.server.net.http.util.Code;
import org.alicebot.server.net.http.util.Password;
import org.alicebot.server.net.http.util.StringUtil;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/* ------------------------------------------------------------ */
/** Handler to enforce SecurityConstraints.
 *
 * @version $Id: SecurityHandler.java,v 1.1.1.1 2001/06/17 19:02:27 noelbu Exp $
 * @author Greg Wilkins (gregw)
 */
public class SecurityHandler extends NullHandler
{
    public final static String __BASIC_AUTH="BASIC";
    
    PathMap _constraintMap=new PathMap();
    String _authMethod=__BASIC_AUTH;
    Map _authRealmMap;
    String _realmName ;
    UserRealm _realm ;
    
    /* ------------------------------------------------------------ */
    /** 
     * @return 
     */
    public UserRealm getRealm()
    {        
        return _realm;
    }
    
    /* ------------------------------------------------------------ */
    /** 
     * @param authRealm 
     */
    public void setRealm(String realmName)
    {
        _realmName=realmName;
    }
    
    /* ------------------------------------------------------------ */
    /** 
     * @return 
     */
    public String getAuthMethod()
    {
        return _authMethod;
    }
    
    /* ------------------------------------------------------------ */
    /** 
     * @param authRealm 
     */
    public void setAuthMethod(String method)
    {
        if (!__BASIC_AUTH.equals(method))
            throw new IllegalArgumentException("Not supported: "+method);
        _authMethod = method;
    }
    
    /* ------------------------------------------------------------ */
    public void addSecurityConstraint(String pathSpec,
                                      SecurityConstraint sc)
    {
        List scs = (List)_constraintMap.get(pathSpec);
        if (scs==null)
        {
            scs=new ArrayList(2);
            _constraintMap.put(pathSpec,scs);
        }
        scs.add(sc);
        
        Code.debug("added ",sc," at ",pathSpec);
    }

    /* ------------------------------------------------------------ */
    public void start()
    {
        if (_realmName!=null && _realmName.length()>0)
        {
            _realm = getHandlerContext().getHttpServer()
                .getRealm(_realmName);
            super.start();
            if (_realm==null)
                Code.warning("Unknown realm: "+_realmName+" for "+this);
        }
        else if (_constraintMap.size()>0)
        {
            Iterator i = _constraintMap.values().iterator();
            while(i.hasNext())
            {
                Iterator j= ((ArrayList)i.next()).iterator();
                while(j.hasNext())
                {
                    SecurityConstraint sc = (SecurityConstraint)j.next();
                    if (sc.isAuthenticated())
                    {
                        Code.warning("No Realm set for "+this);
                        super.start();
                        return;
                    }
                }
            }
            super.start();
        }
    }
    
    /* ------------------------------------------------------------ */
    public void handle(String pathInContext,
                       HttpRequest request,
                       HttpResponse response)
        throws HttpException, IOException
    {
        Code.debug("Authenticate "+pathInContext);

        // Get all path matches
        List scss =_constraintMap.getMatches(pathInContext);
        if (scss!=null)
        {            
            // for each path match 
            for (int m=0;m<scss.size();m++)
            {
                // Get all constraints
                Map.Entry entry=(Map.Entry)scss.get(m);
                if (Code.verbose())
                    Code.debug("Auth ",pathInContext," against ",entry);
                
                List scs = (List)entry.getValue();
                // for each constraint
                for (int c=0;c<scs.size();c++)
                {
                    SecurityConstraint sc=(SecurityConstraint)scs.get(c);

                    // Check the method applies
                    if (!sc.forMethod(request.getMethod()))
                        continue;

                    // Does this forbid everything?
                    if (!sc.isAuthenticated() && !sc.hasDataConstraint())    
                        response.sendError(HttpResponse.__403_Forbidden);

                    
                    // Does it fail a role check?
                    if (sc.isAuthenticated() &&
                        !sc.hasRole(SecurityConstraint.NONE) &&
                        authenticatedInRole(request,response,sc.roles()))
                        // return as an auth challenge will have been set
                        return;
                    
                    // Does it fail a data constraint
                    if (sc.hasDataConstraint() &&
                        "https".equalsIgnoreCase(request.getScheme()))   
                        response.sendError(HttpResponse.__403_Forbidden);
                        
                    // Matches a constraint that does not fail
                    // anything, so must be OK
                    return;    
                }
            }
        }
    }

    /* ------------------------------------------------------------ */
    /** 
     * @return 
     */
    private boolean authenticatedInRole(HttpRequest request,
                                        HttpResponse response,
                                        Iterator roles)
        throws IOException
    {
        boolean userAuth=false;
        
        if (__BASIC_AUTH.equals(_authMethod))
            userAuth=basicAuthenticated(request,response);
        else
        {
            response.setField(HttpFields.__WwwAuthenticate,
                              "basic realm=\""+_realmName+'"');
            response.sendError(HttpResponse.__401_Unauthorized);
        }
        
        if (!userAuth)
            return false;

        // Check if user is in a role that is suitable
        boolean inRole=false;
        while(roles.hasNext())
        {
            String role=roles.next().toString();            
            if (request.isUserInRole(role))
            {
                inRole=true;
                break;
            }
        }

        // If no role reject authentication.
        if (!inRole)
        {
            Code.warning("AUTH FAILURE: role for "+
                         request.getUserPrincipal().getName());
            if (__BASIC_AUTH.equals(_authMethod))
            {
                response.setField(HttpFields.__WwwAuthenticate,
                                  "basic realm=\""+_realmName+'"');
                response.sendError(HttpResponse.__401_Unauthorized);
            }
            else
                response.sendError(HttpResponse.__403_Forbidden);
        }
        
        return userAuth && inRole;
    }
    

    /* ------------------------------------------------------------ */
    /** 
     * @return 
     */
    private boolean basicAuthenticated(HttpRequest request,
                                       HttpResponse response)
        throws IOException
    {
        String credentials =
            request.getField(HttpFields.__Authorization);
        
        if (credentials!=null )
        {
            Code.debug("Credentials: "+credentials);
            credentials =
                credentials.substring(credentials.indexOf(' ')+1);
            credentials = B64Code.decode(credentials,StringUtil.__ISO_8859_1);
            int i = credentials.indexOf(':');
            String username = credentials.substring(0,i);
            String password = credentials.substring(i+1);
            

            if (_realm!=null)
            {
                UserPrincipal user = _realm.getUser(username,request);
                if (user!=null && user.authenticate(password))
                {
                    request.setAttribute(HttpRequest.__AuthType,"BASIC");
                    request.setAttribute(HttpRequest.__AuthUser,username);
                    request.setAttribute(UserPrincipal.__ATTR,user);
                    return true;
                }
                
                Code.warning("AUTH FAILURE: user "+username);
            }
        }
        
        Code.debug("Unauthorized in "+_realmName);
        response.setField(HttpFields.__WwwAuthenticate,
                          "basic realm=\""+_realmName+'"');
        response.sendError(HttpResponse.__401_Unauthorized);
        return false;
    }

    /* ------------------------------------------------------------ */
    /** 
     * @deprecated use HttpServer.addRealm()
     */
    public synchronized void addUser(String username, String password)
    {
        Code.warning("addUser deprecated, use HttpServer.addRealm()");
    }    
}

