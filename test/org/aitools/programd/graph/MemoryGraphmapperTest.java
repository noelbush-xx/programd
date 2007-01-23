package org.aitools.programd.graph;

import org.aitools.programd.Bot;
import org.aitools.programd.Core;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class MemoryGraphmapperTest extends GraphmapperTest
{
    /**
     * Creates the core and the test bot.
     * @see org.aitools.programd.graph.GraphmapperTest#setUp()
     */
    @Override
    @Before
    public void setUp()
    {
        this._core = new Core(BASE_URL);
        this._testBot = new Bot(TESTBOT_ID, this._core.getSettings());
        this._core.addBot(this._testBot);
        this._graphmapper = this._core.getGraphmapper();
    }

    /**
     * @see org.aitools.programd.graph.GraphmapperTest#tearDown()
     */
    @Override
    @After
    public void tearDown()
    {
        // Nothing to do right now.
    }

    /**
     * 
     */
    @Test
    public void testDummy()
    {
        // Do nothing.
    }

}
