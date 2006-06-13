/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
package org.aitools.programd;

import java.io.FileNotFoundException;
import org.aitools.util.resource.URITools;
import org.aitools.util.resource.URLTools;
import org.aitools.util.runtime.UserError;

/**
 * Automatically generated at 2006-06-12T03:34:00.162-04:00.
 */
public class ProgrammaticCoreSettings extends CoreSettings
{
    
    /**
     * Creates a <code>ProgrammaticCoreSettings</code> with default settings values.
     * These are read from the schema when this Java source file is automatically generated.
     */
    public ProgrammaticCoreSettings()
    {
        initialize();
    }
    
    /**
     * Initializes the Settings with default values (from the schema).
     */
    @SuppressWarnings("boxing")
    @Override
    protected void initialize()
    {
        try
        {
            setAIMLNamespaceURI(URITools.createValidURI("http://alicebot.org/2001/AIML-1.0.1", false));
        }
        catch (FileNotFoundException e)
        {
            throw new UserError("Error in settings.", e);
        }
        try
        {
            setBotConfigURL(URLTools.createValidURL("bots.xml", false));
        }
        catch (FileNotFoundException e)
        {
            throw new UserError("Error in settings.", e);
        }
        try
        {
            setPluginConfigURL(URLTools.createValidURL("plugins.xml", false));
        }
        catch (FileNotFoundException e)
        {
            throw new UserError("Error in settings.", e);
        }
        try
        {
            setGossipURL(URLTools.createValidURL("/var/log/programd/gossip.txt", false));
        }
        catch (FileNotFoundException e)
        {
            throw new UserError("Error in settings.", e);
        }
            setPredicateEmptyDefault("undefined");
            setClientNamePredicate("name");
            setBotNameProperty("name");
            setPredicateFlushPeriod(Integer.parseInt("500"));
            setMultiplexorImplementation("org.aitools.programd.multiplexor.FlatFileMultiplexor");
        try
        {
            setFfmDirectory(URLTools.createValidURL("/var/programd/ffm", false));
        }
        catch (FileNotFoundException e)
        {
            throw new UserError("Error in settings.", e);
        }
            setDatabaseURL("jdbc:mysql:///programdbot");
            setDatabaseDriver("com.mysql.jdbc.Driver");
            setDatabaseMaximumConnections(Integer.parseInt("25"));
            setDatabaseUsername("programd");
            setDatabasePassword("yourpassword");
            setMergePolicy(MergePolicy.COMBINE);
            setNoteEachMerge(Boolean.parseBoolean("true"));
            setAppendMergeSeparatorString(" ");
            setResponseTimeout(Integer.parseInt("1000"));
            setInfiniteLoopInput("INFINITE LOOP");
            setPrintStackTraceOnUncaughtExceptions(Boolean.parseBoolean("false"));
            setHeartPulseRate(Integer.parseInt("5"));
            setHeartEnabled(Boolean.parseBoolean(""));
            setAIMLWatcherTimer(Integer.parseInt("2000"));
            setUseAIMLWatcher(Boolean.parseBoolean(""));
        try
        {
            setAIMLSchemaLocation(URLTools.createValidURL("../resources/schema/AIML.xsd", false));
        }
        catch (FileNotFoundException e)
        {
            throw new UserError("Error in settings.", e);
        }
        try
        {
            setPluginSchemaLocation(URLTools.createValidURL("../resources/schema/plugins.xsd", false));
        }
        catch (FileNotFoundException e)
        {
            throw new UserError("Error in settings.", e);
        }
        try
        {
            setTestCaseSchemaLocation(URLTools.createValidURL("../resources/schema/test-cases.xsd", false));
        }
        catch (FileNotFoundException e)
        {
            throw new UserError("Error in settings.", e);
        }
            setJavascriptInterpreterClassname("org.aitools.programd.interpreter.RhinoInterpreter");
            setAllowJavaScript(Boolean.parseBoolean(""));
        try
        {
            setSystemInterpreterDirectory(URLTools.createValidURL("..", false));
        }
        catch (FileNotFoundException e)
        {
            throw new UserError("Error in settings.", e);
        }
            setSystemInterpreterPrefix("");
            setAllowOSAccess(Boolean.parseBoolean(""));
            setCategoryLoadNotificationInterval(Integer.parseInt("1000"));
            setNoteEachLoadedFile(Boolean.parseBoolean("false"));
            setExitImmediatelyOnStartup(Boolean.parseBoolean("false"));
            setConnectString("connect");
            setRandomStrategy(RandomStrategy.NON_REPEATING);
            setNodemapperImplementation("org.aitools.programd.graph.TwoOptimalNodemaster");
            setUseShell(Boolean.parseBoolean("true"));
    }
}
