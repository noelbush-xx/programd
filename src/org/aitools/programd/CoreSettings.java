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
 * Automatically generated from properties file, 2005-03-19T21:01:42.234-04:00
 */
public class CoreSettings extends Settings
{
    /**
     *The root directory for all paths. 
     * This is generally relative to the current directory
     * (i.e., the conf directory), although you can change
     * that in your startup script.
     */
    String rootDirectory;

    /**
     *The bot configuration startup file. 
     */
    String startupFilePath;

    /**
     *Overwrite categories with identical pattern:that:topic? 
     */
    boolean mergePolicy;

    /**
     *The default value for undefined predicates. 
     */
    String predicateEmptyDefault;

    /**
     *The maximum allowable time (in milliseconds) to get a response. 
     */
    int responseTimeout;

    /**
     *The input to match if an infinite loop is found. 
     */
    String infiniteLoopInput;

    /**
     *Allow use of <system> element? 
     */
    boolean osAccessAllowed;

    /**
     *Allow use of <javascript> element? 
     */
    boolean javascriptAllowed;

    /**
     *The string to send when first connecting to the bot. 
     * If this value is empty or not defined, no value
           will be sent.
     */
    String connectString;

    /**
     *The string to send after an inactivity timeout. 
     */
    String inactivityString;

    /**
     *The Multiplexor to use. 
     */
    String multiplexorClassname;

    /**
     *The subdirectory in which to save flat-file predicates (FFM only). 
     */
    String multiplexorFfmDir;

    /**
     *Enable the heart? 
    * The heart can beat and let you know the bot is alive.
    * Right now the only kind of pulse is a message "I'm alive!" printed to the console.
    * You can write a "Pulse" that can do something more useful, like ping a server.
     */
    boolean heartEnabled;

    /**
     *The pulse rate for the heart (beats per minute). 
     */
    int heartPulserate;

    /**
     *The maximum size of the cache before writing to disk/database. 
     */
    int predicateCacheMax;

    /**
     *The directory in which to execute <system> commands. 
     */
    String systemInterpreterDirectory;

    /**
     *The string to prepend to all <system> calls (platform-specific). 
     * Windows requires something like "cmd /c "; Linux doesn't (just comment out)
     */
    String systemInterpreterPrefix;

    /**
     *The JavaScript interpreter (fully-qualified class name). 
     */
    String javascriptInterpreterClassname;

    /**
     *Enable the AIML Watcher? 
    * This will automatically load your AIML files if they are changed.
     */
    boolean useWatcher;

    /**
     *The delay period when checking changed AIML (milliseconds). 
    * Only applicable if the AIML Watcher is enabled.
     */
    int watcherTimer;

    /**
     *The log file for core activity. 
     */
    String loggingCorePath;

    /**
     *The log file for database activity. 
     */
    String loggingDatabasePath;

    /**
     *The log file for any errors. 
     */
    String loggingErrorPath;

    /**
     *The log file for gossip. 
     */
    String loggingGossipPath;

    /**
     *The log file for interpreter activity. 
     */
    String loggingInterpreterPath;

    /**
     *The log file for learning activity. 
     */
    String loggingLearnPath;

    /**
     *The log file for listener activity. 
     */
    String loggingListenerPath;

    /**
     *The log file for matching activity. 
     */
    String loggingMatchingPath;

    /**
     *The log file for merge activity. 
     */
    String loggingMergePath;

    /**
     *The log file for responder activity. 
     */
    String loggingResponderPath;

    /**
     *The log file for startup activity. 
     */
    String loggingStartupPath;

    /**
     *The log file for system command activity. 
     */
    String loggingSystemPath;

    /**
     *The log file for targeting activity. 
     */
    String loggingTargetingPath;

    /**
     *The date-time format to use in logging. 
    * See http://java.sun.com/jdk1.5.0_01/docs/api/java/text/SimpleDateFormat.html
          for formatting codes.
    * Setting the value to blank means no timestamp will be displayed.
     */
    String loggingTimestampFormat;

    /**
     *The generic userid to use in logs when old responders don't have it. 
     */
    String loggingGenericUsername;

    /**
     *Enable chat logging to XML text files? 
    * Be sure that the database configuration (later in this file) is valid.
     */
    boolean loggingToXmlChat;

    /**
     *How many log entries to collect before "rolling over" an XML log file. 
    * "Rolling over" means that the current file is renamed using the date & time,
    * and a fresh log file is created using the path name.  The new log file will
    * contain links to all of the previous log files of the same type.
     */
    int loggingXmlRollover;

    /**
     *The subdirectory for XML chat logs. 
     */
    String loggingXmlChatLogDirectory;

    /**
     *The path to the stylesheet for viewing chat logs. 
     */
    String loggingXmlChatStylesheetPath;

    /**
     *Roll over the chat log at restart? 
     */
    boolean loggingXmlChatRolloverAtRestart;

    /**
     *Enable chat logging to the database? 
    * Be sure that the database configuration (later in this file) is valid.
     */
    boolean loggingToDatabaseChat;

    /**
     *The URL of the database to use. 
     */
    String databaseUrl;

    /**
     *The database driver to use. 
     */
    String databaseDriver;

    /**
     *The maximum number of simultaneous connections to the database. 
     */
    int databaseConnections;

    /**
     *The username which with to access the database. 
     */
    String databaseUser;

    /**
     *The password for the database. 
     */
    String databasePassword;

    /**
     *Configuration file for HTMLResponder. 
     */
    String confLocationHtmlResponder;

    /**
     *Configuration file for FlashResponder. 
     */
    String confLocationFlashResponder;

    /**
     *Configuration file for HTTPServer. 
     */
    String confLocationHttpServer;

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

        setInfiniteLoopInput(this.properties.getProperty("programd.infinite-loop-input", "INFINITE LOOP"));

        setOsAccessAllowed(Boolean.valueOf(this.properties.getProperty("programd.os-access-allowed", "false")).booleanValue());

        setJavascriptAllowed(Boolean.valueOf(this.properties.getProperty("programd.javascript-allowed", "false")).booleanValue());

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

        setLoggingCorePath(this.properties.getProperty("programd.logging.core.path", "logs/core.log"));

        setLoggingDatabasePath(this.properties.getProperty("programd.logging.database.path", "logs/database.log"));

        setLoggingErrorPath(this.properties.getProperty("programd.logging.error.path", "logs/error.log"));

        setLoggingGossipPath(this.properties.getProperty("programd.logging.gossip.path", "logs/gossip.log"));

        setLoggingInterpreterPath(this.properties.getProperty("programd.logging.interpreter.path", "logs/interpreter.log"));

        setLoggingLearnPath(this.properties.getProperty("programd.logging.learn.path", "logs/learn.log"));

        setLoggingListenerPath(this.properties.getProperty("programd.logging.listener.path", "logs/listener.log"));

        setLoggingMatchingPath(this.properties.getProperty("programd.logging.matching.path", "logs/matching.log"));

        setLoggingMergePath(this.properties.getProperty("programd.logging.merge.path", "logs/merge.log"));

        setLoggingResponderPath(this.properties.getProperty("programd.logging.responder.path", "logs/responder.log"));

        setLoggingStartupPath(this.properties.getProperty("programd.logging.startup.path", "logs/startup.log"));

        setLoggingSystemPath(this.properties.getProperty("programd.logging.system.path", "logs/system.log"));

        setLoggingTargetingPath(this.properties.getProperty("programd.logging.targeting.path", "logs/targeting.log"));

        setLoggingTimestampFormat(this.properties.getProperty("programd.logging.timestamp-format", "yyyy-MM-dd H:mm:ss"));

        setLoggingGenericUsername(this.properties.getProperty("programd.logging.generic-username", "client"));

        setLoggingToXmlChat(Boolean.valueOf(this.properties.getProperty("programd.logging.to-xml.chat", "true")).booleanValue());

        try
        {
            setLoggingXmlRollover(Integer.parseInt(this.properties.getProperty("programd.logging.xml.rollover", "500")));
        }
        catch (NumberFormatException e)
        {
            setLoggingXmlRollover(500);
        }

        setLoggingXmlChatLogDirectory(this.properties.getProperty("programd.logging.xml.chat.log-directory", "logs/chat"));

        setLoggingXmlChatStylesheetPath(this.properties.getProperty("programd.logging.xml.chat.stylesheet-path", "../resources/logs/view-chat.xsl"));

        setLoggingXmlChatRolloverAtRestart(Boolean.valueOf(this.properties.getProperty("programd.logging.xml.chat.rollover-at-restart", "false")).booleanValue());

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
     * @return the value of infiniteLoopInput
     */
    public String getInfiniteLoopInput()
    {
        return this.infiniteLoopInput;
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
     * @return the value of loggingCorePath
     */
    public String getLoggingCorePath()
    {
        return this.loggingCorePath;
    }

    /**
     * @return the value of loggingDatabasePath
     */
    public String getLoggingDatabasePath()
    {
        return this.loggingDatabasePath;
    }

    /**
     * @return the value of loggingErrorPath
     */
    public String getLoggingErrorPath()
    {
        return this.loggingErrorPath;
    }

    /**
     * @return the value of loggingGossipPath
     */
    public String getLoggingGossipPath()
    {
        return this.loggingGossipPath;
    }

    /**
     * @return the value of loggingInterpreterPath
     */
    public String getLoggingInterpreterPath()
    {
        return this.loggingInterpreterPath;
    }

    /**
     * @return the value of loggingLearnPath
     */
    public String getLoggingLearnPath()
    {
        return this.loggingLearnPath;
    }

    /**
     * @return the value of loggingListenerPath
     */
    public String getLoggingListenerPath()
    {
        return this.loggingListenerPath;
    }

    /**
     * @return the value of loggingMatchingPath
     */
    public String getLoggingMatchingPath()
    {
        return this.loggingMatchingPath;
    }

    /**
     * @return the value of loggingMergePath
     */
    public String getLoggingMergePath()
    {
        return this.loggingMergePath;
    }

    /**
     * @return the value of loggingResponderPath
     */
    public String getLoggingResponderPath()
    {
        return this.loggingResponderPath;
    }

    /**
     * @return the value of loggingStartupPath
     */
    public String getLoggingStartupPath()
    {
        return this.loggingStartupPath;
    }

    /**
     * @return the value of loggingSystemPath
     */
    public String getLoggingSystemPath()
    {
        return this.loggingSystemPath;
    }

    /**
     * @return the value of loggingTargetingPath
     */
    public String getLoggingTargetingPath()
    {
        return this.loggingTargetingPath;
    }

    /**
     * @return the value of loggingTimestampFormat
     */
    public String getLoggingTimestampFormat()
    {
        return this.loggingTimestampFormat;
    }

    /**
     * @return the value of loggingGenericUsername
     */
    public String getLoggingGenericUsername()
    {
        return this.loggingGenericUsername;
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
     * @param infiniteLoopInputToSet   the value to which to set infiniteLoopInput
     */
    public void setInfiniteLoopInput(String infiniteLoopInputToSet)
    {
        this.infiniteLoopInput = infiniteLoopInputToSet;
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
     * @param loggingCorePathToSet   the value to which to set loggingCorePath
     */
    public void setLoggingCorePath(String loggingCorePathToSet)
    {
        this.loggingCorePath = loggingCorePathToSet;
    }

    /**
     * @param loggingDatabasePathToSet   the value to which to set loggingDatabasePath
     */
    public void setLoggingDatabasePath(String loggingDatabasePathToSet)
    {
        this.loggingDatabasePath = loggingDatabasePathToSet;
    }

    /**
     * @param loggingErrorPathToSet   the value to which to set loggingErrorPath
     */
    public void setLoggingErrorPath(String loggingErrorPathToSet)
    {
        this.loggingErrorPath = loggingErrorPathToSet;
    }

    /**
     * @param loggingGossipPathToSet   the value to which to set loggingGossipPath
     */
    public void setLoggingGossipPath(String loggingGossipPathToSet)
    {
        this.loggingGossipPath = loggingGossipPathToSet;
    }

    /**
     * @param loggingInterpreterPathToSet   the value to which to set loggingInterpreterPath
     */
    public void setLoggingInterpreterPath(String loggingInterpreterPathToSet)
    {
        this.loggingInterpreterPath = loggingInterpreterPathToSet;
    }

    /**
     * @param loggingLearnPathToSet   the value to which to set loggingLearnPath
     */
    public void setLoggingLearnPath(String loggingLearnPathToSet)
    {
        this.loggingLearnPath = loggingLearnPathToSet;
    }

    /**
     * @param loggingListenerPathToSet   the value to which to set loggingListenerPath
     */
    public void setLoggingListenerPath(String loggingListenerPathToSet)
    {
        this.loggingListenerPath = loggingListenerPathToSet;
    }

    /**
     * @param loggingMatchingPathToSet   the value to which to set loggingMatchingPath
     */
    public void setLoggingMatchingPath(String loggingMatchingPathToSet)
    {
        this.loggingMatchingPath = loggingMatchingPathToSet;
    }

    /**
     * @param loggingMergePathToSet   the value to which to set loggingMergePath
     */
    public void setLoggingMergePath(String loggingMergePathToSet)
    {
        this.loggingMergePath = loggingMergePathToSet;
    }

    /**
     * @param loggingResponderPathToSet   the value to which to set loggingResponderPath
     */
    public void setLoggingResponderPath(String loggingResponderPathToSet)
    {
        this.loggingResponderPath = loggingResponderPathToSet;
    }

    /**
     * @param loggingStartupPathToSet   the value to which to set loggingStartupPath
     */
    public void setLoggingStartupPath(String loggingStartupPathToSet)
    {
        this.loggingStartupPath = loggingStartupPathToSet;
    }

    /**
     * @param loggingSystemPathToSet   the value to which to set loggingSystemPath
     */
    public void setLoggingSystemPath(String loggingSystemPathToSet)
    {
        this.loggingSystemPath = loggingSystemPathToSet;
    }

    /**
     * @param loggingTargetingPathToSet   the value to which to set loggingTargetingPath
     */
    public void setLoggingTargetingPath(String loggingTargetingPathToSet)
    {
        this.loggingTargetingPath = loggingTargetingPathToSet;
    }

    /**
     * @param loggingTimestampFormatToSet   the value to which to set loggingTimestampFormat
     */
    public void setLoggingTimestampFormat(String loggingTimestampFormatToSet)
    {
        this.loggingTimestampFormat = loggingTimestampFormatToSet;
    }

    /**
     * @param loggingGenericUsernameToSet   the value to which to set loggingGenericUsername
     */
    public void setLoggingGenericUsername(String loggingGenericUsernameToSet)
    {
        this.loggingGenericUsername = loggingGenericUsernameToSet;
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