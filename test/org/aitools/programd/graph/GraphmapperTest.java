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

import java.net.URL;

import org.aitools.programd.Bot;
import org.aitools.programd.Core;
import org.aitools.programd.util.NoMatchException;
import org.aitools.util.resource.Filesystem;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Abstract only in the sense that it can be set up,
 * by subclasses, with different implementations of Graphmapper to test them.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
abstract public class GraphmapperTest
{
    protected static URL BASE_URL;
    
    protected static final String TESTBOT_ID = "TestBot";
    
    protected Core _core;
    
    protected Bot _testBot;
    
    protected Graphmapper _graphmapper;
    
    /**
     *
     */
    @BeforeClass
    public static void setUpClass()
    {
        BASE_URL = Filesystem.getWorkingDirectory();
    }
    
    /**
     *
     */
    @Before
    abstract public void setUp();

    /**
     *
     */
    @After
    abstract public void tearDown();

    /**
     * Test method for {@link org.aitools.programd.graph.MemoryGraphmapper#addCategory(java.lang.String, java.lang.String, java.lang.String, java.lang.String, org.aitools.programd.Bot, java.net.URL)}.
     */
    @Test
    public void testAddCategory()
    {
        this._graphmapper.addCategory("test", null, null, "Test passed", this._testBot, BASE_URL);
    }

    /**
     * Test method for {@link org.aitools.programd.graph.MemoryGraphmapper#match(java.lang.String, java.lang.String, java.lang.String, java.lang.String)}.
     * @throws NoMatchException 
     */
    @Test
    public void testMatch() throws NoMatchException
    {
        this._graphmapper.addCategory("test", null, null, "Test passed", this._testBot, BASE_URL);
        assertEquals("Test passed", this._graphmapper.match("test", "*", "*", TESTBOT_ID).getTemplate());
    }
}
