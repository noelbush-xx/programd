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
 * Automatically generated from properties file, 2005-03-23T22:39:46.625-04:00
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
     * Log/display match trace messages? 
     */
    private boolean recordMatchTrace;

    /**
     *Allow use of <system> element? 
     */
    private boolean osAccessAllowed;

    /**
     *Allow use of <javascript> element? 
     */
    private boolean javascriptAllowed;

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
    private String activityLogPath;

    /**
     *The log file for matching activity. 
     */
    private String matchingLogPath;

    /**
     *The date-time format to use in logging. 
    * See http://java.sun.com/jdk1.5.0_02/docs/api/java/text/SimpleDateFormat.html
          for formatting codes.
    * Setting the value to blank means no timestamp will be displayed.
     */
    private String logTimestampFormat;

    /**
     *Enable chat logging to XML text files? 
    * Be sure that the database configuration (later in this file) is valid.
     */
    private boolean loggingToXmlChat;

    /**
     *How many log entries to collect before "rolling over" an XML log file. 
    * "Rolling over" means that the current file is renamed using the date & time,
    * and a fresh log file is created using the path name.  The new log file will
    * contain links to all of the previous log files of the same type.
     */
    private int loggingXmlRollover;

    /**
     *The subdirectory for XML chat logs. 
     */
    private String loggingXmlChatLogDirectory;

    /**
     *The path to the stylesheet for viewing chat logs. 
     */
    private String loggingXmlChatStylesheetPath;

    /**
     *Roll over the chat log at restart? 
     */
    private boolean loggingXmlChatRolloverAtRestart;

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
     * @param propertiesPath the path to the core properties file
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
        setRootDirectory(this.properties.getProperty("programd.root-directory", "..")); //$NON-NLS-1$ //$NON-NLS-2$

        setAimlSchemaNamespaceUri(this.properties.getProperty("programd.aiml-schema.namespace-uri", "http://alicebot.org/2001/AIML-1.0.1")); //$NON-NLS-1$ //$NON-NLS-2$

        setAimlSchemaLocation(this.properties.getProperty("programd.aiml-schema.location", "resources/schema/AIML.xsd")); //$NON-NLS-1$ //$NON-NLS-2$

        setStartupFilePath(this.properties.getProperty("programd.startup-file-path", "conf/bots.xml")); //$NON-NLS-1$ //$NON-NLS-2$

        setMergePolicy(Boolean.valueOf(this.properties.getProperty("programd.merge-policy", "true")).booleanValue()); //$NON-NLS-1$ //$NON-NLS-2$

        setPredicateEmptyDefault(this.properties.getProperty("programd.predicate-empty-default", "undefined")); //$NON-NLS-1$ //$NON-NLS-2$

        try
        {
            setResponseTimeout(Integer.parseInt(this.properties.getProperty("programd.response-timeout", "1000"))); //$NON-NLS-1$ //$NON-NLS-2$
        }
        catch (NumberFormatException e)
        {
            setResponseTimeout(1000);
        }

        try
        {
            setCategoryLoadNotifyInterval(Integer.parseInt(this.properties.getProperty("programd.category-load-notify-interval", "5000"))); //$NON-NLS-1$ //$NON-NLS-2$
        }
        catch (NumberFormatException e)
        {
            setCategoryLoadNotifyInterval(5000);
        }

        setInfiniteLoopInput(this.properties.getProperty("programd.infinite-loop-input", "INFINITE LOOP")); //$NON-NLS-1$ //$NON-NLS-2$

        setClientNamePredicate(this.properties.getProperty("programd.client-name-predicate", "name")); //$NON-NLS-1$ //$NON-NLS-2$

        setRecordMatchTrace(Boolean.valueOf(this.properties.getProperty("programd.record-match-trace", "true")).booleanValue()); //$NON-NLS-1$ //$NON-NLS-2$

        setOsAccessAllowed(Boolean.valueOf(this.properties.getProperty("programd.os-access-allowed", "false")).booleanValue()); //$NON-NLS-1$ //$NON-NLS-2$

        setJavascriptAllowed(Boolean.valueOf(this.properties.getProperty("programd.javascript-allowed", "false")).booleanValue()); //$NON-NLS-1$ //$NON-NLS-2$

        setConnectString(this.properties.getProperty("programd.connect-string", "CONNECT")); //$NON-NLS-1$ //$NON-NLS-2$

        setInactivityString(this.properties.getProperty("programd.inactivity-string", "INACTIVITY")); //$NON-NLS-1$ //$NON-NLS-2$

        setMultiplexorClassname(this.properties.getProperty("programd.multiplexor-classname", "org.aitools.programd.multiplexor.FlatFileMultiplexor")); //$NON-NLS-1$ //$NON-NLS-2$

        setMultiplexorFfmDir(this.properties.getProperty("programd.multiplexor.ffm-dir", "ffm")); //$NON-NLS-1$ //$NON-NLS-2$

        setHeartEnabled(Boolean.valueOf(this.properties.getProperty("programd.heart.enabled", "false")).booleanValue()); //$NON-NLS-1$ //$NON-NLS-2$

        try
        {
            setHeartPulserate(Integer.parseInt(this.properties.getProperty("programd.heart.pulserate", "5"))); //$NON-NLS-1$ //$NON-NLS-2$
        }
        catch (NumberFormatException e)
        {
            setHeartPulserate(5);
        }

        try
        {
            setPredicateCacheMax(Integer.parseInt(this.properties.getProperty("programd.predicate-cache.max", "500"))); //$NON-NLS-1$ //$NON-NLS-2$
        }
        catch (NumberFormatException e)
        {
            setPredicateCacheMax(500);
        }

        setSystemInterpreterDirectory(this.properties.getProperty("programd.system-interpreter.directory", ".")); //$NON-NLS-1$ //$NON-NLS-2$

        setSystemInterpreterPrefix(this.properties.getProperty("programd.system-interpreter.prefix", "")); //$NON-NLS-1$ //$NON-NLS-2$

        setJavascriptInterpreterClassname(this.properties.getProperty("programd.javascript-interpreter.classname", "org.aitools.programd.interpreter.RhinoInterpreter")); //$NON-NLS-1$ //$NON-NLS-2$

        setUseWatcher(Boolean.valueOf(this.properties.getProperty("programd.use-watcher", "false")).booleanValue()); //$NON-NLS-1$ //$NON-NLS-2$

        try
        {
            setWatcherTimer(Integer.parseInt(this.properties.getProperty("programd.watcher.timer", "2000"))); //$NON-NLS-1$ //$NON-NLS-2$
        }
        catch (NumberFormatException e)
        {
            setWatcherTimer(2000);
        }

        setActivityLogPath(this.properties.getProperty("programd.activity.log.path", "logs/activity.log")); //$NON-NLS-1$ //$NON-NLS-2$

        setMatchingLogPath(this.properties.getProperty("programd.matching.log.path", "logs/matching.log")); //$NON-NLS-1$ //$NON-NLS-2$

        setLogTimestampFormat(this.properties.getProperty("programd.log.timestamp-format", "yyyy-MM-dd H:mm:ss")); //$NON-NLS-1$ //$NON-NLS-2$

        setLoggingToXmlChat(Boolean.valueOf(this.properties.getProperty("programd.logging.to-xml.chat", "true")).booleanValue()); //$NON-NLS-1$ //$NON-NLS-2$

        try
        {
            setLoggingXmlRollover(Integer.parseInt(this.properties.getProperty("programd.logging.xml.rollover", "500"))); //$NON-NLS-1$ //$NON-NLS-2$
        }
        catch (NumberFormatException e)
        {
            setLoggingXmlRollover(500);
        }

        setLoggingXmlChatLogDirectory(this.properties.getProperty("programd.logging.xml.chat.log-directory", "logs/chat")); //$NON-NLS-1$ //$NON-NLS-2$

        setLoggingXmlChatStylesheetPath(this.properties.getProperty("programd.logging.xml.chat.stylesheet-path", "../resources/logs/view-chat.xsl")); //$NON-NLS-1$ //$NON-NLS-2$

        setLoggingXmlChatRolloverAtRestart(Boolean.valueOf(this.properties.getProperty("programd.logging.xml.chat.rollover-at-restart", "false")).booleanValue()); //$NON-NLS-1$ //$NON-NLS-2$

        setLoggingToDatabaseChat(Boolean.valueOf(this.properties.getProperty("programd.logging.to-database.chat", "false")).booleanValue()); //$NON-NLS-1$ //$NON-NLS-2$

        setDatabaseUrl(this.properties.getProperty("programd.database.url", "jdbc:mysql:///programdbot")); //$NON-NLS-1$ //$NON-NLS-2$

        setDatabaseDriver(this.properties.getProperty("programd.database.driver", "org.gjt.mm.mysql.Driver")); //$NON-NLS-1$ //$NON-NLS-2$

        try
        {
            setDatabaseConnections(Integer.parseInt(this.properties.getProperty("programd.database.connections", "25"))); //$NON-NLS-1$ //$NON-NLS-2$
        }
        catch (NumberFormatException e)
        {
            setDatabaseConnections(25);
        }

        setDatabaseUser(this.properties.getProperty("programd.database.user", "programd")); //$NON-NLS-1$ //$NON-NLS-2$

        setDatabasePassword(this.properties.getProperty("programd.database.password", "yourpassword")); //$NON-NLS-1$ //$NON-NLS-2$

        setConfLocationHtmlResponder(this.properties.getProperty("programd.conf-location.html-responder", "conf/html-responder.xml")); //$NON-NLS-1$ //$NON-NLS-2$

        setConfLocationFlashResponder(this.properties.getProperty("programd.conf-location.flash-responder", "conf/flash-responder.xml")); //$NON-NLS-1$ //$NON-NLS-2$

        setConfLocationHttpServer(this.properties.getProperty("programd.conf-location.http-server", "conf/http-server.xml")); //$NON-NLS-1$ //$NON-NLS-2$

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
     * @return the value of recordMatchTrace
     */
    public boolean recordMatchTrace()
    {
        return this.recordMatchTrace;
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
     * @return the value of activityLogPath
     */
    public String getActivityLogPath()
    {
        return this.activityLogPath;
    }

    /**
     * @return the value of matchingLogPath
     */
    public String getMatchingLogPath()
    {
        return this.matchingLogPath;
    }

    /**
     * @return the value of logTimestampFormat
     */
    public String getLogTimestampFormat()
    {
        return this.logTimestampFormat;
    }

    /**
     * @return the value of loggingToXmlChat
     */
    public boolean loggingToXmlChat()
    {
        return this.loggingToXmlChat;
    }

    /**
     * @return the value of loggingXmlRollover
     */
    public int getLoggingXmlRollover()
    {
        return this.loggingXmlRollover;
    }

    /**
     * @return the value of loggingXmlChatLogDirectory
     */
    public String getLoggingXmlChatLogDirectory()
    {
        return this.loggingXmlChatLogDirectory;
    }

    /**
     * @return the value of loggingXmlChatStylesheetPath
     */
    public String getLoggingXmlChatStylesheetPath()
    {
        return this.loggingXmlChatStylesheetPath;
    }

    /**
     * @return the value of loggingXmlChatRolloverAtRestart
     */
    public boolean loggingXmlChatRolloverAtRestart()
    {
        return this.loggingXmlChatRolloverAtRestart;
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
     * @param recordMatchTraceToSet   the value to which to set recordMatchTrace
     */
    public void setRecordMatchTrace(boolean recordMatchTraceToSet)
    {
        this.recordMatchTrace = recordMatchTraceToSet;
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
     * @param activityLogPathToSet   the value to which to set activityLogPath
     */
    public void setActivityLogPath(String activityLogPathToSet)
    {
        this.activityLogPath = activityLogPathToSet;
    }

    /**
     * @param matchingLogPathToSet   the value to which to set matchingLogPath
     */
    public void setMatchingLogPath(String matchingLogPathToSet)
    {
        this.matchingLogPath = matchingLogPathToSet;
    }

    /**
     * @param logTimestampFormatToSet   the value to which to set logTimestampFormat
     */
    public void setLogTimestampFormat(String logTimestampFormatToSet)
    {
        this.logTimestampFormat = logTimestampFormatToSet;
    }

    /**
     * @param loggingToXmlChatToSet   the value to which to set loggingToXmlChat
     */
    public void setLoggingToXmlChat(boolean loggingToXmlChatToSet)
    {
        this.loggingToXmlChat = loggingToXmlChatToSet;
    }

    /**
     * @param loggingXmlRolloverToSet   the value to which to set loggingXmlRollover
     */
    public void setLoggingXmlRollover(int loggingXmlRolloverToSet)
    {
        this.loggingXmlRollover = loggingXmlRolloverToSet;
    }

    /**
     * @param loggingXmlChatLogDirectoryToSet   the value to which to set loggingXmlChatLogDirectory
     */
    public void setLoggingXmlChatLogDirectory(String loggingXmlChatLogDirectoryToSet)
    {
        this.loggingXmlChatLogDirectory = loggingXmlChatLogDirectoryToSet;
    }

    /**
     * @param loggingXmlChatStylesheetPathToSet   the value to which to set loggingXmlChatStylesheetPath
     */
    public void setLoggingXmlChatStylesheetPath(String loggingXmlChatStylesheetPathToSet)
    {
        this.loggingXmlChatStylesheetPath = loggingXmlChatStylesheetPathToSet;
    }

    /**
     * @param loggingXmlChatRolloverAtRestartToSet   the value to which to set loggingXmlChatRolloverAtRestart
     */
    public void setLoggingXmlChatRolloverAtRestart(boolean loggingXmlChatRolloverAtRestartToSet)
    {
        this.loggingXmlChatRolloverAtRestart = loggingXmlChatRolloverAtRestartToSet;
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