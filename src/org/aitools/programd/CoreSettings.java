/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
 
package org.aitools.programd;

import org.aitools.programd.util.Settings;

/**
 * Automatically generated from properties file, 2005-04-24T09:47:16.382-04:00
 */
public class CoreSettings extends Settings
{
    /**
     *The root directory for all paths. 
     * This is generally relative to the current directory
     * (i.e., the conf directory), although you can change
     * that in your startup script.
     */
    private String rootDirectory;

    /**
     *The namespace URI of AIML to use. 
     */
    private String aimlSchemaNamespaceUri;

    /**
     *The location of the AIML schema (or a copy of it). 
     */
    private String aimlSchemaLocation;

    /**
     *The bot configuration startup file. 
     */
    private String startupFilePath;

    /**
     *Overwrite categories with identical pattern:that:topic? 
     */
    private boolean mergePolicy;

    /**
     *The default value for undefined predicates. 
     */
    private String predicateEmptyDefault;

    /**
     *The maximum allowable time (in milliseconds) to get a response. 
     */
    private int responseTimeout;

    /**
     * How many categories will be loaded before a message is displayed? 
     * Only meaningful if programd.enable-console == true.
     */
    private int categoryLoadNotifyInterval;

    /**
     *The input to match if an infinite loop is found. 
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
     * Log/display match trace messages? 
     */
    private boolean recordMatchTrace;

    /**
     * Print stack trace on uncaught exceptions? 
     */
    private boolean onUncaughtExceptionsPrintStackTrace;

    /**
     *Allow use of <system> element? 
     */
    private boolean osAccessAllowed;

    /**
     *Allow use of <javascript> element? 
     */
    private boolean javascriptAllowed;

    /**
     *Where to write gossip entries.                       
        Gossip entries will be written like this:
        <li>the gossip</li>
        This enables you to use XInclude to provide runtime access to the
        contents of the gossip file.  See resources/aiml/gossip.aiml
        for an example.
     */
    private String gossipPath;

    /**
     *The string to send when first connecting to the bot. 
     * If this value is empty or not defined, no value
           will be sent.
     */
    private String connectString;

    /**
     *The string to send after an inactivity timeout. 
     */
    private String inactivityString;

    /**
     *The Multiplexor to use. 
     */
    private String multiplexorClassname;

    /**
     *The subdirectory in which to save flat-file predicates (FFM only). 
     */
    private String multiplexorFfmDir;

    /**
     *Enable the heart? 
    * The heart can beat and let you know the bot is alive.
    * Right now the only kind of pulse is a message "I'm alive!" printed to the console.
    * You can write a "Pulse" that can do something more useful, like ping a server.
     */
    private boolean heartEnabled;

    /**
     *The pulse rate for the heart (beats per minute). 
     */
    private int heartPulserate;

    /**
     *The maximum size of the cache before writing to disk/database. 
     */
    private int predicateCacheMax;

    /**
     *The directory in which to execute <system> commands. 
     */
    private String systemInterpreterDirectory;

    /**
     *The string to prepend to all <system> calls (platform-specific). 
     * Windows requires something like "cmd /c "; Linux doesn't (just comment out)
     */
    private String systemInterpreterPrefix;

    /**
     *The JavaScript interpreter (fully-qualified class name). 
     */
    private String javascriptInterpreterClassname;

    /**
     *Enable the AIML Watcher? 
    * This will automatically load your AIML files if they are changed.
     */
    private boolean useWatcher;

    /**
     *The delay period when checking changed AIML (milliseconds). 
    * Only applicable if the AIML Watcher is enabled.
     */
    private int watcherTimer;

    /**
     *The general activity log file. 
     */
    private String activityLogPattern;

    /**
     *The log file for matching activity. 
     */
    private String matchingLogPattern;

    /**
     *The date-time format to use in general logs. 
    * See http://java.sun.com/jdk1.5.0/docs/api/java/text/SimpleDateFormat.html
          for formatting codes.
    * Setting the value to blank means no timestamp will be displayed.
     */
    private String logTimestampFormat;

    /**
     *The subdirectory for chat logs. 
     */
    private String chatLogDirectory;

    /**
     *The date-time format to use in chat logs. 
    * See http://java.sun.com/jdk1.5.0/docs/api/java/text/SimpleDateFormat.html
          for formatting codes.
    * Setting the value to blank means no timestamp will be displayed.
     */
    private String chatLogTimestampFormat;

    /**
     *Enable chat logging to the database? 
    * Be sure that the database configuration (later in this file) is valid.
     */
    private boolean loggingToDatabaseChat;

    /**
     *The URL of the database to use. 
     */
    private String databaseUrl;

    /**
     *The database driver to use. 
     */
    private String databaseDriver;

    /**
     *The maximum number of simultaneous connections to the database. 
     */
    private int databaseConnections;

    /**
     *The username which with to access the database. 
     */
    private String databaseUser;

    /**
     *The password for the database. 
     */
    private String databasePassword;

    /**
     *Configuration file for HTMLResponder. 
     */
    private String confLocationHtmlResponder;

    /**
     *Configuration file for FlashResponder. 
     */
    private String confLocationFlashResponder;

    /**
     *Configuration file for HTTPServer. 
     */
    private String confLocationHttpServer;

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
    public CoreSettings(String propertiesPath)
    {
        super(propertiesPath);
    }

    /**
    * Initializes the Settings with values from properties, or defaults.
    */
    protected void initialize()
    {
        setRootDirectory(this.properties.getProperty("programd.root-directory", ".."));

        setAimlSchemaNamespaceUri(this.properties.getProperty("programd.aiml-schema.namespace-uri", "http://alicebot.org/2001/AIML-1.0.1"));

        setAimlSchemaLocation(this.properties.getProperty("programd.aiml-schema.location", "resources/schema/AIML.xsd"));

        setStartupFilePath(this.properties.getProperty("programd.startup-file-path", "conf/bots.xml"));

        setMergePolicy(Boolean.valueOf(this.properties.getProperty("programd.merge-policy", "true")).booleanValue());

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

        setInfiniteLoopInput(this.properties.getProperty("programd.infinite-loop-input", "INFINITE LOOP"));

        setClientNamePredicate(this.properties.getProperty("programd.client-name-predicate", "name"));

        setBotNamePredicate(this.properties.getProperty("programd.bot-name-predicate", "name"));

        setRecordMatchTrace(Boolean.valueOf(this.properties.getProperty("programd.record-match-trace", "true")).booleanValue());

        setOnUncaughtExceptionsPrintStackTrace(Boolean.valueOf(this.properties.getProperty("programd.on-uncaught-exceptions.print-stack-trace", "false")).booleanValue());

        setOsAccessAllowed(Boolean.valueOf(this.properties.getProperty("programd.os-access-allowed", "false")).booleanValue());

        setJavascriptAllowed(Boolean.valueOf(this.properties.getProperty("programd.javascript-allowed", "false")).booleanValue());

        setGossipPath(this.properties.getProperty("programd.gossip.path", "logs/gossip.txt"));

        setConnectString(this.properties.getProperty("programd.connect-string", "CONNECT"));

        setInactivityString(this.properties.getProperty("programd.inactivity-string", "INACTIVITY"));

        setMultiplexorClassname(this.properties.getProperty("programd.multiplexor-classname", "org.aitools.programd.multiplexor.FlatFileMultiplexor"));

        setMultiplexorFfmDir(this.properties.getProperty("programd.multiplexor.ffm-dir", "ffm"));

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

        setSystemInterpreterDirectory(this.properties.getProperty("programd.system-interpreter.directory", "."));

        setSystemInterpreterPrefix(this.properties.getProperty("programd.system-interpreter.prefix", ""));

        setJavascriptInterpreterClassname(this.properties.getProperty("programd.javascript-interpreter.classname", "org.aitools.programd.interpreter.RhinoInterpreter"));

        setUseWatcher(Boolean.valueOf(this.properties.getProperty("programd.use-watcher", "false")).booleanValue());

        try
        {
            setWatcherTimer(Integer.parseInt(this.properties.getProperty("programd.watcher.timer", "2000")));
        }
        catch (NumberFormatException e)
        {
            setWatcherTimer(2000);
        }

        setActivityLogPattern(this.properties.getProperty("programd.activity.log.pattern", "logs/activity.log"));

        setMatchingLogPattern(this.properties.getProperty("programd.matching.log.pattern", "logs/matching.log"));

        setLogTimestampFormat(this.properties.getProperty("programd.log.timestamp-format", "yyyy-MM-dd H:mm:ss"));

        setChatLogDirectory(this.properties.getProperty("programd.chat.log.directory", "logs/chat"));

        setChatLogTimestampFormat(this.properties.getProperty("programd.chat.log.timestamp-format", "yyyy-MM-dd H:mm:ss"));

        setLoggingToDatabaseChat(Boolean.valueOf(this.properties.getProperty("programd.logging.to-database.chat", "false")).booleanValue());

        setDatabaseUrl(this.properties.getProperty("programd.database.url", "jdbc:mysql:///programdbot"));

        setDatabaseDriver(this.properties.getProperty("programd.database.driver", "org.gjt.mm.mysql.Driver"));

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

        setConfLocationHtmlResponder(this.properties.getProperty("programd.conf-location.html-responder", "conf/html-responder.xml"));

        setConfLocationFlashResponder(this.properties.getProperty("programd.conf-location.flash-responder", "conf/flash-responder.xml"));

        setConfLocationHttpServer(this.properties.getProperty("programd.conf-location.http-server", "conf/http-server.xml"));

    }

    /**
     * @return the value of rootDirectory
     */
    public String getRootDirectory()
    {
        return this.rootDirectory;
    }

    /**
     * @return the value of aimlSchemaNamespaceUri
     */
    public String getAimlSchemaNamespaceUri()
    {
        return this.aimlSchemaNamespaceUri;
    }

    /**
     * @return the value of aimlSchemaLocation
     */
    public String getAimlSchemaLocation()
    {
        return this.aimlSchemaLocation;
    }

    /**
     * @return the value of startupFilePath
     */
    public String getStartupFilePath()
    {
        return this.startupFilePath;
    }

    /**
     * @return the value of mergePolicy
     */
    public boolean mergePolicy()
    {
        return this.mergePolicy;
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
     * @return the value of recordMatchTrace
     */
    public boolean recordMatchTrace()
    {
        return this.recordMatchTrace;
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
    public String getGossipPath()
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
     * @return the value of multiplexorClassname
     */
    public String getMultiplexorClassname()
    {
        return this.multiplexorClassname;
    }

    /**
     * @return the value of multiplexorFfmDir
     */
    public String getMultiplexorFfmDir()
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
     * @return the value of systemInterpreterDirectory
     */
    public String getSystemInterpreterDirectory()
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
     * @return the value of activityLogPattern
     */
    public String getActivityLogPattern()
    {
        return this.activityLogPattern;
    }

    /**
     * @return the value of matchingLogPattern
     */
    public String getMatchingLogPattern()
    {
        return this.matchingLogPattern;
    }

    /**
     * @return the value of logTimestampFormat
     */
    public String getLogTimestampFormat()
    {
        return this.logTimestampFormat;
    }

    /**
     * @return the value of chatLogDirectory
     */
    public String getChatLogDirectory()
    {
        return this.chatLogDirectory;
    }

    /**
     * @return the value of chatLogTimestampFormat
     */
    public String getChatLogTimestampFormat()
    {
        return this.chatLogTimestampFormat;
    }

    /**
     * @return the value of loggingToDatabaseChat
     */
    public boolean loggingToDatabaseChat()
    {
        return this.loggingToDatabaseChat;
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
     * @return the value of confLocationHtmlResponder
     */
    public String getConfLocationHtmlResponder()
    {
        return this.confLocationHtmlResponder;
    }

    /**
     * @return the value of confLocationFlashResponder
     */
    public String getConfLocationFlashResponder()
    {
        return this.confLocationFlashResponder;
    }

    /**
     * @return the value of confLocationHttpServer
     */
    public String getConfLocationHttpServer()
    {
        return this.confLocationHttpServer;
    }

    /**
     * @param rootDirectoryToSet   the value to which to set rootDirectory
     */
    public void setRootDirectory(String rootDirectoryToSet)
    {
        this.rootDirectory = rootDirectoryToSet;
    }

    /**
     * @param aimlSchemaNamespaceUriToSet   the value to which to set aimlSchemaNamespaceUri
     */
    public void setAimlSchemaNamespaceUri(String aimlSchemaNamespaceUriToSet)
    {
        this.aimlSchemaNamespaceUri = aimlSchemaNamespaceUriToSet;
    }

    /**
     * @param aimlSchemaLocationToSet   the value to which to set aimlSchemaLocation
     */
    public void setAimlSchemaLocation(String aimlSchemaLocationToSet)
    {
        this.aimlSchemaLocation = aimlSchemaLocationToSet;
    }

    /**
     * @param startupFilePathToSet   the value to which to set startupFilePath
     */
    public void setStartupFilePath(String startupFilePathToSet)
    {
        this.startupFilePath = startupFilePathToSet;
    }

    /**
     * @param mergePolicyToSet   the value to which to set mergePolicy
     */
    public void setMergePolicy(boolean mergePolicyToSet)
    {
        this.mergePolicy = mergePolicyToSet;
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
     * @param recordMatchTraceToSet   the value to which to set recordMatchTrace
     */
    public void setRecordMatchTrace(boolean recordMatchTraceToSet)
    {
        this.recordMatchTrace = recordMatchTraceToSet;
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
    public void setGossipPath(String gossipPathToSet)
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
     * @param multiplexorClassnameToSet   the value to which to set multiplexorClassname
     */
    public void setMultiplexorClassname(String multiplexorClassnameToSet)
    {
        this.multiplexorClassname = multiplexorClassnameToSet;
    }

    /**
     * @param multiplexorFfmDirToSet   the value to which to set multiplexorFfmDir
     */
    public void setMultiplexorFfmDir(String multiplexorFfmDirToSet)
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
     * @param systemInterpreterDirectoryToSet   the value to which to set systemInterpreterDirectory
     */
    public void setSystemInterpreterDirectory(String systemInterpreterDirectoryToSet)
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
     * @param activityLogPatternToSet   the value to which to set activityLogPattern
     */
    public void setActivityLogPattern(String activityLogPatternToSet)
    {
        this.activityLogPattern = activityLogPatternToSet;
    }

    /**
     * @param matchingLogPatternToSet   the value to which to set matchingLogPattern
     */
    public void setMatchingLogPattern(String matchingLogPatternToSet)
    {
        this.matchingLogPattern = matchingLogPatternToSet;
    }

    /**
     * @param logTimestampFormatToSet   the value to which to set logTimestampFormat
     */
    public void setLogTimestampFormat(String logTimestampFormatToSet)
    {
        this.logTimestampFormat = logTimestampFormatToSet;
    }

    /**
     * @param chatLogDirectoryToSet   the value to which to set chatLogDirectory
     */
    public void setChatLogDirectory(String chatLogDirectoryToSet)
    {
        this.chatLogDirectory = chatLogDirectoryToSet;
    }

    /**
     * @param chatLogTimestampFormatToSet   the value to which to set chatLogTimestampFormat
     */
    public void setChatLogTimestampFormat(String chatLogTimestampFormatToSet)
    {
        this.chatLogTimestampFormat = chatLogTimestampFormatToSet;
    }

    /**
     * @param loggingToDatabaseChatToSet   the value to which to set loggingToDatabaseChat
     */
    public void setLoggingToDatabaseChat(boolean loggingToDatabaseChatToSet)
    {
        this.loggingToDatabaseChat = loggingToDatabaseChatToSet;
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
     * @param confLocationHtmlResponderToSet   the value to which to set confLocationHtmlResponder
     */
    public void setConfLocationHtmlResponder(String confLocationHtmlResponderToSet)
    {
        this.confLocationHtmlResponder = confLocationHtmlResponderToSet;
    }

    /**
     * @param confLocationFlashResponderToSet   the value to which to set confLocationFlashResponder
     */
    public void setConfLocationFlashResponder(String confLocationFlashResponderToSet)
    {
        this.confLocationFlashResponder = confLocationFlashResponderToSet;
    }

    /**
     * @param confLocationHttpServerToSet   the value to which to set confLocationHttpServer
     */
    public void setConfLocationHttpServer(String confLocationHttpServerToSet)
    {
        this.confLocationHttpServer = confLocationHttpServerToSet;
    }

}