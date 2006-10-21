/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.graph;

import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;

import org.aitools.programd.Core;
import org.aitools.util.runtime.UserError;
import org.aitools.util.sql.DbAccess;
import org.aitools.util.sql.DbAccessRefsPoolMgr;

/**
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 *
 */
public class DBGraphmapper extends AbstractGraphmapper
{
    /** A manager for database access. */
    private DbAccessRefsPoolMgr dbManager;

    /**
     * Creates a new DBGraphmapper, reading settings from the given Core.
     * 
     * @param core the CoreSettings object from which to read settings
     */
    public DBGraphmapper(Core core)
    {
        super(core);
        this.dbManager = this._core.getDBManager();
    }
    
    /**
     * @param parent
     * @param word
     * @return the node to which this edge points
     */
    @SuppressWarnings("boxing")
    private int createEdge(int parent, String word)
    {
        DbAccess dba = this.dbManager.takeDbaRef();
        int to_node;
        try
        {
            ResultSet edgeCheck =
                dba.executeQuery(
                        String.format(
                                "SELECT `to_node` FROM `edges` WHERE `from_node` = '%i' AND `text` = '%s'", parent, word));
            if (!edgeCheck.next())
            {
                ResultSet makeNode =
                    dba.executeQuery(
                            "INSERT INTO `nodes` () VALUES ()");
                makeNode.next();
                to_node = makeNode.getInt(1);
                dba.executeUpdate(
                        String.format(
                                "INSERT INTO `edges` (`from_node`, `text`, `to_node`) VALUES (%i, '%s', %i)", parent, word, to_node));
                makeNode.close();
            }
            else
            {
                throw new IllegalArgumentException("Trying to recreate edge that already exists.");
            }
            edgeCheck.close();
        }
        catch (SQLException e)
        {
            throw new UserError("Database error.", e);
        }
        this.dbManager.returnDbaRef(dba);
        return to_node;
    }
}
