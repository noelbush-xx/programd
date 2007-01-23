/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.graph;

import static org.junit.Assert.*;

import java.sql.Connection;
import java.sql.Statement;

import org.aitools.programd.Core;
import org.aitools.util.resource.Filesystem;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * 
 */
public class DBNodemapperTest
{
    static Core CORE;

    Connection connection;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception
    {
        CORE = new Core(Filesystem.getWorkingDirectory());
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception
    {
        this.connection = CORE.getDBConnection();
        Statement statement = this.connection.createStatement();
        statement.execute("TRUNCATE `bots`");
        statement.execute("TRUNCATE `files`");
        statement.execute("TRUNCATE `nodes`");
        statement.execute("TRUNCATE `templates`");
        statement.execute("TRUNCATE `users`");
        statement.execute("TRUNCATE `bot_file`");
        statement.execute("TRUNCATE `bot_user`");
        statement.execute("TRUNCATE `botidnode_file`");
        statement.execute("TRUNCATE `edges`");
        statement.execute("TRUNCATE `exchanges`");
        statement.execute("TRUNCATE `file_node`");
        statement.execute("TRUNCATE `node_template`");
        statement.execute("TRUNCATE `predicates`");
        statement.close();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception
    {
        this.connection.close();
    }

    /**
     * 
     */
    @Test
    public void testSomething()
    {
        fail("Not yet implemented");
    }
}
