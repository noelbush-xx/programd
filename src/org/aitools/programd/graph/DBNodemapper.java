/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.graph;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

import org.aitools.util.runtime.DeveloperError;
import org.aitools.util.runtime.UserError;
import org.aitools.util.sql.DbAccess;
import org.aitools.util.sql.DbAccessRefsPoolMgr;

/**
 * This is a database-based Nodemapper.  It does <i>not</i>
 * implement the {@link Nodemapper} interface, however --
 * in the interest of optimal performance, it provides static
 * methods which cover the same functions as <code>Nodemapper</code>,
 * but without the need to create a separate in-memory object
 * for each node.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class DBNodemapper
{
    /**
     * "Instantiates" the DBNodemapper by checking whether it already
     * exists in the database, and creating it if it does not.
     */
    @SuppressWarnings("boxing")
    public void instantiate()
    {
        DbAccess dba = this._dbManager.takeDbaRef();
        try
        {
            ResultSet results =
                dba.executeQuery(
                        String.format("SELECT COUNT(*) FROM `nodes` WHERE `from_node` = '%i'", this._id));
            results.next();
            int result = results.getInt(1);
            results.close();
            
            assert result < 2 : "Database schema is in error!  Allows multiple nodes with same id!";
            if (result == 0)
            {
                dba.executeUpdate(String.format("INSERT INTO `nodes` (`id`) VALUES (%i)", this._id));
            }
        }
        catch (SQLException e)
        {
            throw new UserError("Database error.", e);
        }
        this._dbManager.returnDbaRef(dba);
    }

    /**
     * @see org.aitools.programd.graph.Nodemapper#remove(java.lang.Object)
     */
    @SuppressWarnings("boxing")
    public void remove(Object value)
    {
        DbAccess dba = this._dbManager.takeDbaRef();
        if (value instanceof DBNodemapper)
        {
            dba.executeQuery(
                    String.format(
                            "DELETE FROM `edges` WHERE `from_node` = '%i' AND `to_node` = '%s'", this._id, ((DBNodemapper)value).getID()));
            this._dbManager.returnDbaRef(dba);
        }
        else
        {
            throw new DeveloperError("Not done yet!", new RuntimeException());
        }
    }

    /**
     * @see org.aitools.programd.graph.Nodemapper#get(java.lang.String)
     */
    @SuppressWarnings("boxing")
    public Object get(String key)
    {
        DbAccess dba = this._dbManager.takeDbaRef();
        int toNode = -1;
        try
        {
            ResultSet results =
                dba.executeQuery(
                        String.format(
                                "SELECT `to_node` FROM `edges` WHERE `from_node` = '%i' AND `text` = '%s'", this._id, key));
            if (results.next())
            {
                toNode = results.getInt(1);
            }
            results.close();
        }
        catch (SQLException e)
        {
            throw new UserError("Database error.", e);
        }
        this._dbManager.returnDbaRef(dba);
        if (toNode >= 0)
        {
            return new DBNodemapper(toNode, this._dbManager);
        }
        // otherwise
        return null;
    }

    /**
     * @return the keyset of the Nodemaster
     */
    @SuppressWarnings("boxing")
    public Set<String> keySet()
    {
        Set<String> keys = new HashSet<String>();
        DbAccess dba = this._dbManager.takeDbaRef();
        try
        {
            ResultSet results =
                dba.executeQuery(
                        String.format(
                                "SELECT `text` FROM `edges` WHERE `from_node` = '%i'", this._id));
            while (results.next())
            {
                keys.add(results.getString(1));
            }
            results.close();
        }
        catch (SQLException e)
        {
            throw new UserError("Database error.", e);
        }
        this._dbManager.returnDbaRef(dba);
        return keys;
    }

    /**
     * @see org.aitools.programd.graph.Nodemapper#containsKey(java.lang.String)
     */
    @SuppressWarnings("boxing")
    public boolean containsKey(String key)
    {
        DbAccess dba = this._dbManager.takeDbaRef();
        int count;
        try
        {
            ResultSet results =
                dba.executeQuery(
                        String.format(
                                "SELECT COUNT(*) FROM `edges` WHERE `from_node` = '%i' AND `text` = '%s'", this._id, key));
            results.next();
            count = results.getInt(1);
            results.close();
            
            assert count < 2 : "Database schema is in error!  Allows duplicate edges!";
        }
        catch (SQLException e)
        {
            throw new UserError("Database error.", e);
        }
        this._dbManager.returnDbaRef(dba);
        return (count == 1);
    }

    /**
     * @return the size of the Nodemaster
     */
    @SuppressWarnings("boxing")
    public int size()
    {
        DbAccess dba = this._dbManager.takeDbaRef();
        int count;
        try
        {
            ResultSet results =
                dba.executeQuery(
                        String.format(
                                "SELECT COUNT(*) FROM `edges` WHERE `from_node` = '%i'", this._id));
            results.next();
            count = results.getInt(1);
            results.close();
        }
        catch (SQLException e)
        {
            throw new UserError("Database error.", e);
        }
        this._dbManager.returnDbaRef(dba);
        return count;
    }

    /**
     * @return the parent of the Nodemaster
     */
    public Nodemapper getParent()
    {
        DbAccess dba = this._dbManager.takeDbaRef();
        int parent = -1;
        try
        {
            ResultSet results =
                dba.executeQuery(
                        String.format(
                                "SELECT `from_node` FROM `edges` WHERE `to_node` = '%i'", this._id));
            if (results.next())
            {
                parent = results.getInt(1);
            }
            results.close();
        }
        catch (SQLException e)
        {
            throw new UserError("Database error.", e);
        }
        this._dbManager.returnDbaRef(dba);
        if (parent >= 0)
        {
            return new DBNodemapper(parent, this._dbManager);
        }
        // otherwise
        return null;
    }
}
