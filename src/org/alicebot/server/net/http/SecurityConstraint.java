// ========================================================================
// Copyright (c) 2000 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: SecurityConstraint.java,v 1.1.1.1 2001/06/17 19:00:58 noelbu Exp $
// ========================================================================

package org.alicebot.server.net.http;

import org.alicebot.server.net.http.util.Code;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;



/* ------------------------------------------------------------ */
/** Describe an auth and/or data constraint. 
 *
 * @version $Revision: 1.1.1.1 $
 * @author Greg Wilkins (gregw)
 */
public class SecurityConstraint
    implements Cloneable
{
    /* ------------------------------------------------------------ */
    public final static int
        DC_NONE=0,
        DC_INTEGRAL=1,
        DC_CONFIDENTIAL=2;
    /* ------------------------------------------------------------ */
    public final static String NONE="NONE";
    
    /* ------------------------------------------------------------ */
    private String _name;
    private List _methods;
    private List _roles;
    private int _dataConstraint=DC_NONE;


    /* ------------------------------------------------------------ */
    /** Constructor. 
     */
    public SecurityConstraint()
    {}

    /* ------------------------------------------------------------ */
    /** Conveniance Constructor. 
     * @param name 
     * @param role 
     */
    public SecurityConstraint(String name,String role)
    {
        setName(name);
        addRole(role);
    }
    
    /* ------------------------------------------------------------ */
    /**
     * @param name 
     */
    public void setName(String name)
    {
        _name=name;
    }    

    /* ------------------------------------------------------------ */
    /** 
     * @param method 
     */
    public synchronized void addMethod(String method)
    {
        if (_methods==null)
            _methods=new ArrayList(3);
        _methods.add(method);
    }

    /* ------------------------------------------------------------ */
    /** 
     * @param method 
     * @return True if this constraint applies to the method. 
     */
    public boolean forMethod(String method)
    {
        if (_methods==null)
            return true;
        return _methods.contains(method);
    }
    
    
    /* ------------------------------------------------------------ */
    /** 
     * @param role 
     */
    public synchronized void addRole(String role)
    {
        if (_roles==null)
            _roles=new ArrayList(3);
        _roles.add(role);
    }

    /* ------------------------------------------------------------ */
    /** 
     * @return Iterator of role names
     */
    public Iterator roles()
    {
        if (_roles==null)
            return Collections.EMPTY_LIST.iterator();
        return _roles.iterator();
    }

    /* ------------------------------------------------------------ */
    /** 
     * @param role 
     * @return True if the constraint contains the role.
     */
    public boolean hasRole(String role)
    {
        return _roles!=null && _roles.contains(role);
    }
    
    /* ------------------------------------------------------------ */
    /** 
     * @return True if the constraint requires request authentication
     */
    public boolean isAuthenticated()
    {
        return _roles!=null && _roles.size()>0;
    }
    
    /* ------------------------------------------------------------ */
    /** 
     * @param c 
     */
    public void setDataConstraint(int c)
    {
        if (c<0 || c>DC_CONFIDENTIAL)
            throw new IllegalArgumentException("Constraint out of range");
        _dataConstraint=c;
    }


    /* ------------------------------------------------------------ */
    /** 
     * @return 
     */
    public int getDataConstraint()
    {
        return _dataConstraint;
    }

    /* ------------------------------------------------------------ */
    /** 
     * @return True if there is a data constraint.
     */
    public boolean hasDataConstraint()
    {
        return _dataConstraint!=DC_NONE;
    }
    
    
    /* ------------------------------------------------------------ */
    public Object clone()
    {
        SecurityConstraint sc=null;
        try{
            sc = (SecurityConstraint)super.clone();
            if (_methods!=null)
                sc._methods=new ArrayList(_methods);
            if (_roles!=null)
                sc._roles=new ArrayList(_roles);
        }
        catch (CloneNotSupportedException e)
        {
            Code.fail("Oh yes it does");
        }
        return sc;
    }
    
    /* ------------------------------------------------------------ */
    /** 
     * @return 
     */
    public String toString()
    {
        return "SC{"+_name+
            ","+_methods+
            ","+_roles+
            ","+(_dataConstraint==DC_NONE
                 ?"NONE}"
                 :(_dataConstraint==DC_INTEGRAL?"INTEGRAL}":"CONFIDENTIAL}"));
    }
}
