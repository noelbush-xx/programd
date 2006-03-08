/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
 
package org.aitools.programd;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.aitools.programd.util.URLTools;
import org.aitools.programd.util.UserError;

import org.aitools.programd.util.Settings;

/**
 * Automatically generated from properties file, 2006-03-08T14:08:00.612-05:00
 */
public class CoreSettings extends Settings
{
    /**
     * The namespace URI of AIML to use.
     */
    private URI aimlSchemaNamespaceUri;
        
    /**
     * The bot configuration startup file.
     */
    private URL startupFilePath;
        
    /**
     * What to do when a category is loaded whose pattern:that:topic path is identical to one already loaded (for the same bot).
     */
    private MergePolicy mergePolicy;
    
    /** The possible values for MergePolicy. */
    public static enum MergePolicy
    {
        /** Leave the currently loaded template in place and ignore the new one. */
        SKIP,
        
        /** Overwrite the loaded template with the new one. */
        OVERWRITE,
        
        /** Append the content of the new template to the currently loaded one. */
        APPEND,
        
        /** Store the new template as well, so it will have an equal chance of being used as the currently loaded one. */
        COMBINE
    }

            /**
     * If the append merge policy is used, what text content (if any) should be inserted between the contents of the two templates?
     */
    private String mergeAppendSeparatorString;
        
    /**
     * Produce a note in the console/log for each merge?
     */
    private boolean mergeNoteEach;
        
    /**
     * The default value for undefined predicates.
     */
    private String predicateEmptyDefault;
        
    /**
     * The maximum allowable time (in milliseconds) to get a response.
     */
    private int responseTimeout;
        
    /**
     * How many categories will be loaded before a message is displayed?
     */
    private int categoryLoadNotifyInterval;
        
    /**
     * Whether or not to print a message as each file is loaded.
     */
    private boolean loadNotifyEachFile;
        
    /**
     * The input to match if an infinite loop is found.
     */
    private String infiniteLoopInput;
        
    /**
     * Which bot predicate contains the client's name?
     */
    private String clientNamePredicate;
        
    /**
     * Which bot predicate contains the bot's name?
     */
    private String botNamePredicate;
        
    /**
     * Print stack trace on uncaught exceptions?
     */
    private boolean onUncaughtExceptionsPrintStackTrace;
        
    /**
     * Execute contents of <system> elements?
     */
    private boolean osAccessAllowed;
        
    /**
     * Execute contents of <javascript> elements?
     */
    private boolean javascriptAllowed;
        
    /**
     * Where to write gossip entries.
     */
    private URL gossipPath;
        
    /**
     * The string to send when first connecting to the bot.
     */
    private String connectString;
        
    /**
     * The string to send after an inactivity timeout.
     */
    private String inactivityString;
        
    /**
     * The Nodemapper implementation to use.
     */
    private String nodemapperImplementation;
        
    /**
     * The Multiplexor implementation to use.
     */
    private String multiplexorImplementation;
        
    /**
     * The directory in which to save flat-file predicates (FFM only).
     */
    private URL multiplexorFfmDir;
        
    /**
     * Enable the heart?
     */
    private boolean heartEnabled;
        
    /**
     * The pulse rate for the heart (beats per minute).
     */
    private int heartPulserate;
        
    /**
     * The maximum size of the cache before writing to disk/database.
     */
    private int predicateCacheMax;
        
    /**
     * Use interactive command-line shell?
     */
    private boolean consoleUseShell;
        
    /**
     * Exit immediately upon startup?
     */
    private boolean exitImmediatelyOnStartup;
        
    /**
     * The location of the AIML schema (or a copy of it).
     */
    private URL schemaLocationAIML;
        
    /**
     * The location of the plugin schema (or a copy of it).
     */
    private URL schemaLocationPlugins;
        
    /**
     * The location of the test cases schema (or a copy of it).
     */
    private URL schemaLocationTestCases;
        
    /**
     * The directory in which to execute <system> commands.
     */
    private URL systemInterpreterDirectory;
        
    /**
     * The string to prepend to all <system> calls (platform-specific).
     */
    private String systemInterpreterPrefix;
        
    /**
     * The JavaScript interpreter (fully-qualified class name).
     */
    private String javascriptInterpreterClassname;
        
    /**
     * Enable the AIML Watcher?
     */
    private boolean useWatcher;
        
    /**
     * The delay period when checking changed AIML (milliseconds).
     */
    private int watcherTimer;
        
    /**
     * The URL of the database to use.
     */
    private String databaseUrl;
        
    /**
     * The database driver to use.
     */
    private String databaseDriver;
        
    /**
     * The maximum number of simultaneous connections to the database.
     */
    private int databaseConnections;
        
    /**
     * The username which with to access the database.
     */
    private String databaseUser;
        
    /**
     * The password for the database.
     */
    private String databasePassword;
        
    /**
     * Configuration file for plugins.
     */
    private URL confLocationPlugins;
        
    /**
     * Creates a <code>CoreSettings</code> using default property values.
     */
    public CoreSettings()
    {
        super();
    }
    
    /**
     * Creates a <code>CoreSettings</code> with the (XML-formatted) properties
     * located at the given path.
     *
     * @param propertiesPath the path to the configuration file
     */
    public CoreSettings(URL propertiesPath)
    {
        super(propertiesPath);
    }

    /**
    * Initializes the Settings with values from properties, or defaults.
    */
    @Override
    protected void initialize()
    {
        try
        {
            setAimlSchemaNamespaceUri(new URI(this.properties.getProperty("programd.aiml-schema.namespace-uri", "http://alicebot.org/2001/AIML-1.0.1")));
        }
        catch (URISyntaxException e)
        {
            throw new UserError(e);
        }

        setStartupFilePath(URLTools.contextualize(this.path, this.properties.getProperty("programd.startup-file-path", "bots.xml")));

        String mergePolicyValue = this.properties.getProperty("programd.merge.policy", "combine");
         
         if (mergePolicyValue.equals("skip"))
         {
             this.mergePolicy = MergePolicy.SKIP;
         }
             else if (mergePolicyValue.equals("overwrite"))
         {
             this.mergePolicy = MergePolicy.OVERWRITE;
         }
             else if (mergePolicyValue.equals("append"))
         {
             this.mergePolicy = MergePolicy.APPEND;
         }
             else if (mergePolicyValue.equals("combine"))
         {
             this.mergePolicy = MergePolicy.COMBINE;
         }
             

        setMergeAppendSeparatorString(this.properties.getProperty("programd.merge.append.separator-string", "&#x10;"));

        setMergeNoteEach(Boolean.valueOf(this.properties.getProperty("programd.merge.note-each", "true")).booleanValue());

        setPredicateEmptyDefault(this.properties.getProperty("programd.predicate-empty-default", "undefined"));

        try
        {
            setResponseTimeout(Integer.parseInt(this.properties.getProperty("programd.response-timeout", "1000")));
        }
        catch (NumberFormatException e)
        {
            setResponseTimeout(1000);
        }

        try
        {
            setCategoryLoadNotifyInterval(Integer.parseInt(this.properties.getProperty("programd.category-load-notify-interval", "5000")));
        }
        catch (NumberFormatException e)
        {
            setCategoryLoadNotifyInterval(5000);
        }

        setLoadNotifyEachFile(Boolean.valueOf(this.properties.getProperty("programd.load.notify-each-file", "true")).booleanValue());

        setInfiniteLoopInput(this.properties.getProperty("programd.infinite-loop-input", "INFINITE LOOP"));

        setClientNamePredicate(this.properties.getProperty("programd.client-name-predicate", "name"));

        setBotNamePredicate(this.properties.getProperty("programd.bot-name-predicate", "name"));

        setOnUncaughtExceptionsPrintStackTrace(Boolean.valueOf(this.properties.getProperty("programd.on-uncaught-exceptions.print-stack-trace", "false")).booleanValue());

        setOsAccessAllowed(Boolean.valueOf(this.properties.getProperty("programd.os-access-allowed", "false")).booleanValue());

        setJavascriptAllowed(Boolean.valueOf(this.properties.getProperty("programd.javascript-allowed", "false")).booleanValue());

        setGossipPath(URLTools.contextualize(this.path, this.properties.getProperty("programd.gossip.path", "/var/log/programd/gossip.txt")));

        setConnectString(this.properties.getProperty("programd.connect-string", "CONNECT"));

        setInactivityString(this.properties.getProperty("programd.inactivity-string", "INACTIVITY"));

        setNodemapperImplementation(this.properties.getProperty("programd.nodemapper-implementation", "org.aitools.programd.graph.TwoOptimalNodemaster"));

        setMultiplexorImplementation(this.properties.getProperty("programd.multiplexor-implementation", "org.aitools.programd.multiplexor.FlatFileMultiplexor"));

        setMultiplexorFfmDir(URLTools.contextualize(this.path, this.properties.getProperty("programd.multiplexor.ffm-dir", "/var/programd/ffm")));

        setHeartEnabled(Boolean.valueOf(this.properties.getProperty("programd.heart.enabled", "false")).booleanValue());

        try
        {
            setHeartPulserate(Integer.parseInt(this.properties.getProperty("programd.heart.pulserate", "5")));
        }
        catch (NumberFormatException e)
        {
            setHeartPulserate(5);
        }

        try
        {
            setPredicateCacheMax(Integer.parseInt(this.properties.getProperty("programd.predicate-cache.max", "500")));
        }
        catch (NumberFormatException e)
        {
            setPredicateCacheMax(500);
        }

        setConsoleUseShell(Boolean.valueOf(this.properties.getProperty("programd.console.use-shell", "true")).booleanValue());

        setExitImmediatelyOnStartup(Boolean.valueOf(this.properties.getProperty("programd.exit-immediately-on-startup", "false")).booleanValue());

        setSchemaLocationAIML(URLTools.contextualize(this.path, this.properties.getProperty("programd.schema-location.AIML", "../resources/schema/AIML.xsd")));

        setSchemaLocationPlugins(URLTools.contextualize(this.path, this.properties.getProperty("programd.schema-location.plugins", "../resources/schema/plugins.xsd")));

        setSchemaLocationTestCases(URLTools.contextualize(this.path, this.properties.getProperty("programd.schema-location.test-cases", "../resources/schema/test-cases.xsd")));

        setSystemInterpreterDirectory(URLTools.contextualize(this.path, this.properties.getProperty("programd.system-interpreter.directory", "..")));

        setSystemInterpreterPrefix(this.properties.getProperty("programd.system-interpreter.prefix", ""));

        setJavascriptInterpreterClassname(this.properties.getProperty("programd.javascript-interpreter.classname", "org.aitools.programd.interpreter.RhinoInterpreter"));

        setUseWatcher(Boolean.valueOf(this.properties.getProperty("programd.use-watcher", "true")).booleanValue());

        try
        {
            setWatcherTimer(Integer.parseInt(this.properties.getProperty("programd.watcher.timer", "2000")));
        }
        catch (NumberFormatException e)
        {
            setWatcherTimer(2000);
        }

        setDatabaseUrl(this.properties.getProperty("programd.database.url", "jdbc:mysql:///programdbot"));

        setDatabaseDriver(this.properties.getProperty("programd.database.driver", "com.mysql.jdbc.Driver"));

        try
        {
            setDatabaseConnections(Integer.parseInt(this.properties.getProperty("programd.database.connections", "25")));
        }
        catch (NumberFormatException e)
        {
            setDatabaseConnections(25);
        }

        setDatabaseUser(this.properties.getProperty("programd.database.user", "programd"));

        setDatabasePassword(this.properties.getProperty("programd.database.password", "yourpassword"));

        setConfLocationPlugins(URLTools.contextualize(this.path, this.properties.getProperty("programd.conf-location.plugins", "plugins.xml")));

    }

    /**
     * @return the value of aimlSchemaNamespaceUri
     */
    public URI getAimlSchemaNamespaceUri()
    {
        return this.aimlSchemaNamespaceUri;
    }

    /**
     * @return the value of startupFilePath
     */
    public URL getStartupFilePath()
    {
        return this.startupFilePath;
    }

    /**
     * @return the value of mergePolicy
     */
    public MergePolicy getMergePolicy()
    {
        return this.mergePolicy;
    }

    /**
     * @return the value of mergeAppendSeparatorString
     */
    public String getMergeAppendSeparatorString()
    {
        return this.mergeAppendSeparatorString;
    }

    /**
     * @return the value of mergeNoteEach
     */
    public boolean mergeNoteEach()
    {
        return this.mergeNoteEach;
    }

    /**
     * @return the value of predicateEmptyDefault
     */
    public String getPredicateEmptyDefault()
    {
        return this.predicateEmptyDefault;
    }

    /**
     * @return the value of responseTimeout
     */
    public int getResponseTimeout()
    {
        return this.responseTimeout;
    }

    /**
     * @return the value of categoryLoadNotifyInterval
     */
    public int getCategoryLoadNotifyInterval()
    {
        return this.categoryLoadNotifyInterval;
    }

    /**
     * @return the value of loadNotifyEachFile
     */
    public boolean loadNotifyEachFile()
    {
        return this.loadNotifyEachFile;
    }

    /**
     * @return the value of infiniteLoopInput
     */
    public String getInfiniteLoopInput()
    {
        return this.infiniteLoopInput;
    }

    /**
     * @return the value of clientNamePredicate
     */
    public String getClientNamePredicate()
    {
        return this.clientNamePredicate;
    }

    /**
     * @return the value of botNamePredicate
     */
    public String getBotNamePredicate()
    {
        return this.botNamePredicate;
    }

    /**
     * @return the value of onUncaughtExceptionsPrintStackTrace
     */
    public boolean onUncaughtExceptionsPrintStackTrace()
    {
        return this.onUncaughtExceptionsPrintStackTrace;
    }

    /**
     * @return the value of osAccessAllowed
     */
    public boolean osAccessAllowed()
    {
        return this.osAccessAllowed;
    }

    /**
     * @return the value of javascriptAllowed
     */
    public boolean javascriptAllowed()
    {
        return this.javascriptAllowed;
    }

    /**
     * @return the value of gossipPath
     */
    public URL getGossipPath()
    {
        return this.gossipPath;
    }

    /**
     * @return the value of connectString
     */
    public String getConnectString()
    {
        return this.connectString;
    }

    /**
     * @return the value of inactivityString
     */
    public String getInactivityString()
    {
        return this.inactivityString;
    }

    /**
     * @return the value of nodemapperImplementation
     */
    public String getNodemapperImplementation()
    {
        return this.nodemapperImplementation;
    }

    /**
     * @return the value of multiplexorImplementation
     */
    public String getMultiplexorImplementation()
    {
        return this.multiplexorImplementation;
    }

    /**
     * @return the value of multiplexorFfmDir
     */
    public URL getMultiplexorFfmDir()
    {
        return this.multiplexorFfmDir;
    }

    /**
     * @return the value of heartEnabled
     */
    public boolean heartEnabled()
    {
        return this.heartEnabled;
    }

    /**
     * @return the value of heartPulserate
     */
    public int getHeartPulserate()
    {
        return this.heartPulserate;
    }

    /**
     * @return the value of predicateCacheMax
     */
    public int getPredicateCacheMax()
    {
        return this.predicateCacheMax;
    }

    /**
     * @return the value of consoleUseShell
     */
    public boolean consoleUseShell()
    {
        return this.consoleUseShell;
    }

    /**
     * @return the value of exitImmediatelyOnStartup
     */
    public boolean exitImmediatelyOnStartup()
    {
        return this.exitImmediatelyOnStartup;
    }

    /**
     * @return the value of schemaLocationAIML
     */
    public URL getSchemaLocationAIML()
    {
        return this.schemaLocationAIML;
    }

    /**
     * @return the value of schemaLocationPlugins
     */
    public URL getSchemaLocationPlugins()
    {
        return this.schemaLocationPlugins;
    }

    /**
     * @return the value of schemaLocationTestCases
     */
    public URL getSchemaLocationTestCases()
    {
        return this.schemaLocationTestCases;
    }

    /**
     * @return the value of systemInterpreterDirectory
     */
    public URL getSystemInterpreterDirectory()
    {
        return this.systemInterpreterDirectory;
    }

    /**
     * @return the value of systemInterpreterPrefix
     */
    public String getSystemInterpreterPrefix()
    {
        return this.systemInterpreterPrefix;
    }

    /**
     * @return the value of javascriptInterpreterClassname
     */
    public String getJavascriptInterpreterClassname()
    {
        return this.javascriptInterpreterClassname;
    }

    /**
     * @return the value of useWatcher
     */
    public boolean useWatcher()
    {
        return this.useWatcher;
    }

    /**
     * @return the value of watcherTimer
     */
    public int getWatcherTimer()
    {
        return this.watcherTimer;
    }

    /**
     * @return the value of databaseUrl
     */
    public String getDatabaseUrl()
    {
        return this.databaseUrl;
    }

    /**
     * @return the value of databaseDriver
     */
    public String getDatabaseDriver()
    {
        return this.databaseDriver;
    }

    /**
     * @return the value of databaseConnections
     */
    public int getDatabaseConnections()
    {
        return this.databaseConnections;
    }

    /**
     * @return the value of databaseUser
     */
    public String getDatabaseUser()
    {
        return this.databaseUser;
    }

    /**
     * @return the value of databasePassword
     */
    public String getDatabasePassword()
    {
        return this.databasePassword;
    }

    /**
     * @return the value of confLocationPlugins
     */
    public URL getConfLocationPlugins()
    {
        return this.confLocationPlugins;
    }

    /**
     * @param aimlSchemaNamespaceUriToSet   the value to which to set aimlSchemaNamespaceUri
     */
    public void setAimlSchemaNamespaceUri(URI aimlSchemaNamespaceUriToSet)
    {
        this.aimlSchemaNamespaceUri = aimlSchemaNamespaceUriToSet;
    }

    /**
     * @param startupFilePathToSet   the value to which to set startupFilePath
     */
    public void setStartupFilePath(URL startupFilePathToSet)
    {
        this.startupFilePath = startupFilePathToSet;
    }

    /**
     * @param mergePolicyToSet   the value to which to set mergePolicy
     */
    public void setMergePolicy(MergePolicy mergePolicyToSet)
    {
        this.mergePolicy = mergePolicyToSet;
    }

    /**
     * @param mergeAppendSeparatorStringToSet   the value to which to set mergeAppendSeparatorString
     */
    public void setMergeAppendSeparatorString(String mergeAppendSeparatorStringToSet)
    {
        this.mergeAppendSeparatorString = mergeAppendSeparatorStringToSet;
    }

    /**
     * @param mergeNoteEachToSet   the value to which to set mergeNoteEach
     */
    public void setMergeNoteEach(boolean mergeNoteEachToSet)
    {
        this.mergeNoteEach = mergeNoteEachToSet;
    }

    /**
     * @param predicateEmptyDefaultToSet   the value to which to set predicateEmptyDefault
     */
    public void setPredicateEmptyDefault(String predicateEmptyDefaultToSet)
    {
        this.predicateEmptyDefault = predicateEmptyDefaultToSet;
    }

    /**
     * @param responseTimeoutToSet   the value to which to set responseTimeout
     */
    public void setResponseTimeout(int responseTimeoutToSet)
    {
        this.responseTimeout = responseTimeoutToSet;
    }

    /**
     * @param categoryLoadNotifyIntervalToSet   the value to which to set categoryLoadNotifyInterval
     */
    public void setCategoryLoadNotifyInterval(int categoryLoadNotifyIntervalToSet)
    {
        this.categoryLoadNotifyInterval = categoryLoadNotifyIntervalToSet;
    }

    /**
     * @param loadNotifyEachFileToSet   the value to which to set loadNotifyEachFile
     */
    public void setLoadNotifyEachFile(boolean loadNotifyEachFileToSet)
    {
        this.loadNotifyEachFile = loadNotifyEachFileToSet;
    }

    /**
     * @param infiniteLoopInputToSet   the value to which to set infiniteLoopInput
     */
    public void setInfiniteLoopInput(String infiniteLoopInputToSet)
    {
        this.infiniteLoopInput = infiniteLoopInputToSet;
    }

    /**
     * @param clientNamePredicateToSet   the value to which to set clientNamePredicate
     */
    public void setClientNamePredicate(String clientNamePredicateToSet)
    {
        this.clientNamePredicate = clientNamePredicateToSet;
    }

    /**
     * @param botNamePredicateToSet   the value to which to set botNamePredicate
     */
    public void setBotNamePredicate(String botNamePredicateToSet)
    {
        this.botNamePredicate = botNamePredicateToSet;
    }

    /**
     * @param onUncaughtExceptionsPrintStackTraceToSet   the value to which to set onUncaughtExceptionsPrintStackTrace
     */
    public void setOnUncaughtExceptionsPrintStackTrace(boolean onUncaughtExceptionsPrintStackTraceToSet)
    {
        this.onUncaughtExceptionsPrintStackTrace = onUncaughtExceptionsPrintStackTraceToSet;
    }

    /**
     * @param osAccessAllowedToSet   the value to which to set osAccessAllowed
     */
    public void setOsAccessAllowed(boolean osAccessAllowedToSet)
    {
        this.osAccessAllowed = osAccessAllowedToSet;
    }

    /**
     * @param javascriptAllowedToSet   the value to which to set javascriptAllowed
     */
    public void setJavascriptAllowed(boolean javascriptAllowedToSet)
    {
        this.javascriptAllowed = javascriptAllowedToSet;
    }

    /**
     * @param gossipPathToSet   the value to which to set gossipPath
     */
    public void setGossipPath(URL gossipPathToSet)
    {
        this.gossipPath = gossipPathToSet;
    }

    /**
     * @param connectStringToSet   the value to which to set connectString
     */
    public void setConnectString(String connectStringToSet)
    {
        this.connectString = connectStringToSet;
    }

    /**
     * @param inactivityStringToSet   the value to which to set inactivityString
     */
    public void setInactivityString(String inactivityStringToSet)
    {
        this.inactivityString = inactivityStringToSet;
    }

    /**
     * @param nodemapperImplementationToSet   the value to which to set nodemapperImplementation
     */
    public void setNodemapperImplementation(String nodemapperImplementationToSet)
    {
        this.nodemapperImplementation = nodemapperImplementationToSet;
    }

    /**
     * @param multiplexorImplementationToSet   the value to which to set multiplexorImplementation
     */
    public void setMultiplexorImplementation(String multiplexorImplementationToSet)
    {
        this.multiplexorImplementation = multiplexorImplementationToSet;
    }

    /**
     * @param multiplexorFfmDirToSet   the value to which to set multiplexorFfmDir
     */
    public void setMultiplexorFfmDir(URL multiplexorFfmDirToSet)
    {
        this.multiplexorFfmDir = multiplexorFfmDirToSet;
    }

    /**
     * @param heartEnabledToSet   the value to which to set heartEnabled
     */
    public void setHeartEnabled(boolean heartEnabledToSet)
    {
        this.heartEnabled = heartEnabledToSet;
    }

    /**
     * @param heartPulserateToSet   the value to which to set heartPulserate
     */
    public void setHeartPulserate(int heartPulserateToSet)
    {
        this.heartPulserate = heartPulserateToSet;
    }

    /**
     * @param predicateCacheMaxToSet   the value to which to set predicateCacheMax
     */
    public void setPredicateCacheMax(int predicateCacheMaxToSet)
    {
        this.predicateCacheMax = predicateCacheMaxToSet;
    }

    /**
     * @param consoleUseShellToSet   the value to which to set consoleUseShell
     */
    public void setConsoleUseShell(boolean consoleUseShellToSet)
    {
        this.consoleUseShell = consoleUseShellToSet;
    }

    /**
     * @param exitImmediatelyOnStartupToSet   the value to which to set exitImmediatelyOnStartup
     */
    public void setExitImmediatelyOnStartup(boolean exitImmediatelyOnStartupToSet)
    {
        this.exitImmediatelyOnStartup = exitImmediatelyOnStartupToSet;
    }

    /**
     * @param schemaLocationAIMLToSet   the value to which to set schemaLocationAIML
     */
    public void setSchemaLocationAIML(URL schemaLocationAIMLToSet)
    {
        this.schemaLocationAIML = schemaLocationAIMLToSet;
    }

    /**
     * @param schemaLocationPluginsToSet   the value to which to set schemaLocationPlugins
     */
    public void setSchemaLocationPlugins(URL schemaLocationPluginsToSet)
    {
        this.schemaLocationPlugins = schemaLocationPluginsToSet;
    }

    /**
     * @param schemaLocationTestCasesToSet   the value to which to set schemaLocationTestCases
     */
    public void setSchemaLocationTestCases(URL schemaLocationTestCasesToSet)
    {
        this.schemaLocationTestCases = schemaLocationTestCasesToSet;
    }

    /**
     * @param systemInterpreterDirectoryToSet   the value to which to set systemInterpreterDirectory
     */
    public void setSystemInterpreterDirectory(URL systemInterpreterDirectoryToSet)
    {
        this.systemInterpreterDirectory = systemInterpreterDirectoryToSet;
    }

    /**
     * @param systemInterpreterPrefixToSet   the value to which to set systemInterpreterPrefix
     */
    public void setSystemInterpreterPrefix(String systemInterpreterPrefixToSet)
    {
        this.systemInterpreterPrefix = systemInterpreterPrefixToSet;
    }

    /**
     * @param javascriptInterpreterClassnameToSet   the value to which to set javascriptInterpreterClassname
     */
    public void setJavascriptInterpreterClassname(String javascriptInterpreterClassnameToSet)
    {
        this.javascriptInterpreterClassname = javascriptInterpreterClassnameToSet;
    }

    /**
     * @param useWatcherToSet   the value to which to set useWatcher
     */
    public void setUseWatcher(boolean useWatcherToSet)
    {
        this.useWatcher = useWatcherToSet;
    }

    /**
     * @param watcherTimerToSet   the value to which to set watcherTimer
     */
    public void setWatcherTimer(int watcherTimerToSet)
    {
        this.watcherTimer = watcherTimerToSet;
    }

    /**
     * @param databaseUrlToSet   the value to which to set databaseUrl
     */
    public void setDatabaseUrl(String databaseUrlToSet)
    {
        this.databaseUrl = databaseUrlToSet;
    }

    /**
     * @param databaseDriverToSet   the value to which to set databaseDriver
     */
    public void setDatabaseDriver(String databaseDriverToSet)
    {
        this.databaseDriver = databaseDriverToSet;
    }

    /**
     * @param databaseConnectionsToSet   the value to which to set databaseConnections
     */
    public void setDatabaseConnections(int databaseConnectionsToSet)
    {
        this.databaseConnections = databaseConnectionsToSet;
    }

    /**
     * @param databaseUserToSet   the value to which to set databaseUser
     */
    public void setDatabaseUser(String databaseUserToSet)
    {
        this.databaseUser = databaseUserToSet;
    }

    /**
     * @param databasePasswordToSet   the value to which to set databasePassword
     */
    public void setDatabasePassword(String databasePasswordToSet)
    {
        this.databasePassword = databasePasswordToSet;
    }

    /**
     * @param confLocationPluginsToSet   the value to which to set confLocationPlugins
     */
    public void setConfLocationPlugins(URL confLocationPluginsToSet)
    {
        this.confLocationPlugins = confLocationPluginsToSet;
    }

}