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
import java.net.URL;
import org.aitools.util.Settings;

/**
 * Automatically generated at 2007-03-10T23:39:17.013-05:00.
 */
abstract public class CoreSettings extends Settings
{
    /** The namespace URI of AIML to use. */
    private URI AIMLNamespaceURI;
        
    /** The bot configuration file. */
    private URL botConfigURL;
        
    /** Configuration file for plugins. */
    private URL pluginConfigURL;
        
    /** Where to write gossip entries. Gossip entries will be written like this: <li>the gossip</li> */
    private URL gossipURL;
        
    /** The default value for undefined predicates. */
    private String predicateEmptyDefault;
        
    /** Which predicate contains the client's name. */
    private String clientNamePredicate;
        
    /** Which bot property contains the bot's name. */
    private String botNameProperty;
        
    /** The number of predicate set operations before flushing predicates to storage. */
    private int predicateFlushPeriod;
        
    /** The PredicateManager implementation to use. */
    private String predicateManagerImplementation;
        
    /** The directory in which to save flat-file predicates (if the FlatFilePredicateManager is used). */
    private URL ffpmDirectory;
        
    /** The URL of the database to use. */
    private String databaseURL;
        
    /** The database driver to use. */
    private String databaseDriver;
        
    /** The maximum number of simultaneous connections to the database. */
    private int databaseMaximumConnections;
        
    /** The username which with to access the database. */
    private String databaseUsername;
        
    /** The password for the database. */
    private String databasePassword;
        
    /** What to do when a category is loaded whose pattern:that:topic path is identical to one already loaded (for the same bot). */
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

    /** Produce a note in the console/log for each merge? */
    private boolean noteEachMerge;
        
    /** If the append merge policy is used, what text content (if any) should be inserted between the contents of the two templates? (The default value is a space.) */
    private String appendMergeSeparatorString;
        
    /** The maximum allowable time (in milliseconds) to get a response. */
    private int responseTimeout;
        
    /** The input to match if an infinite loop is found. */
    private String infiniteLoopInput;
        
    /** Whether to print a stack trace on uncaught exceptions. */
    private boolean printStackTraceOnUncaughtExceptions;
        
    /** The Pulse implementation to use. */
    private String pulseImplementation;
        
    /** The pulse rate for the heart (beats per minute). */
    private int heartPulseRate;
        
    /** Enable the heart? */
    private boolean heartEnabled;
        
    /** The delay period when checking changed AIML (milliseconds). */
    private int AIMLWatcherTimer;
        
    /** Use the AIML watcher? */
    private boolean useAIMLWatcher;
        
    /** The JavaScript interpreter. */
    private String javascriptInterpreterClassname;
        
    /** Allow the use of JavaScript? */
    private boolean allowJavaScript;
        
    /** The directory in which to execute <system/> element contents. */
    private URL systemInterpreterDirectory;
        
    /** The string to prepend to all <system/> calls (platform-specific). Windows requires something like "cmd /c "; Linux doesn't (just leave empty). */
    private String systemInterpreterPrefix;
        
    /** Allow access to the OS via the system element? */
    private boolean allowOSAccess;
        
    /** How frequently (in categories) to notify as categories are being loaded. */
    private int categoryLoadNotificationInterval;
        
    /** Produce a notification message for each file that is loaded. */
    private boolean noteEachLoadedFile;
        
    /** After all bots have been loaded, exit immediately (useful for timing). */
    private boolean exitImmediatelyOnStartup;
        
    /** The string to send when first connecting to the bot. If this value is empty, no value will be sent. */
    private String connectString;
        
    /** How to interpret random elements. */
    private RandomStrategy randomStrategy;
    
    /** The possible values for RandomStrategy. */
    public static enum RandomStrategy
    {
        /** Each choice is made from the full set, randomly. */
        PURE_RANDOM,

        /** A choice is not repeated until all others have been used. */
        NON_REPEATING
    }

    /** The Graphmapper implementation to use. */
    private String graphmapperImplementation;
        
    /** The Nodemapper implementation to use. */
    private String nodemapperImplementation;
        
    /** Use interactive command-line shell? */
    private boolean useShell;
        
    /** http://xml.org/sax/features/use-entity-resolver2 */
    private boolean xmlParserUseEntityResolver2;
        
    /** http://xml.org/sax/features/validation */
    private boolean xmlParserUseValidation;
        
    /** http://apache.org/xml/features/validation/schema */
    private boolean xmlParserUseSchemaValidation;
        
    /** http://apache.org/xml/features/honour-all-schemaLocations */
    private boolean xmlParserHonourAllSchemaLocations;
        
    /** http://apache.org/xml/features/xinclude */
    private boolean xmlParserUseXInclude;
        
    /** http://apache.org/xml/features/validate-annotations */
    private boolean xmlParserValidateAnnotations;
        
    /**
     * @return the value of AIMLNamespaceURI
     */
    public URI getAIMLNamespaceURI()
    {
        return this.AIMLNamespaceURI;
    }

    /**
     * @return the value of botConfigURL
     */
    public URL getBotConfigURL()
    {
        return this.botConfigURL;
    }

    /**
     * @return the value of pluginConfigURL
     */
    public URL getPluginConfigURL()
    {
        return this.pluginConfigURL;
    }

    /**
     * @return the value of gossipURL
     */
    public URL getGossipURL()
    {
        return this.gossipURL;
    }

    /**
     * @return the value of predicateEmptyDefault
     */
    public String getPredicateEmptyDefault()
    {
        return this.predicateEmptyDefault;
    }

    /**
     * @return the value of clientNamePredicate
     */
    public String getClientNamePredicate()
    {
        return this.clientNamePredicate;
    }

    /**
     * @return the value of botNameProperty
     */
    public String getBotNameProperty()
    {
        return this.botNameProperty;
    }

    /**
     * @return the value of predicateFlushPeriod
     */
    public int getPredicateFlushPeriod()
    {
        return this.predicateFlushPeriod;
    }

    /**
     * @return the value of predicateManagerImplementation
     */
    public String getPredicateManagerImplementation()
    {
        return this.predicateManagerImplementation;
    }

    /**
     * @return the value of ffpmDirectory
     */
    public URL getFfpmDirectory()
    {
        return this.ffpmDirectory;
    }

    /**
     * @return the value of databaseURL
     */
    public String getDatabaseURL()
    {
        return this.databaseURL;
    }

    /**
     * @return the value of databaseDriver
     */
    public String getDatabaseDriver()
    {
        return this.databaseDriver;
    }

    /**
     * @return the value of databaseMaximumConnections
     */
    public int getDatabaseMaximumConnections()
    {
        return this.databaseMaximumConnections;
    }

    /**
     * @return the value of databaseUsername
     */
    public String getDatabaseUsername()
    {
        return this.databaseUsername;
    }

    /**
     * @return the value of databasePassword
     */
    public String getDatabasePassword()
    {
        return this.databasePassword;
    }

    /**
     * @return the value of mergePolicy
     */
    public MergePolicy getMergePolicy()
    {
        return this.mergePolicy;
    }

    /**
     * @return the value of noteEachMerge
     */
    public boolean noteEachMerge()
    {
        return this.noteEachMerge;
    }

    /**
     * @return the value of appendMergeSeparatorString
     */
    public String getAppendMergeSeparatorString()
    {
        return this.appendMergeSeparatorString;
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
     * @return the value of printStackTraceOnUncaughtExceptions
     */
    public boolean printStackTraceOnUncaughtExceptions()
    {
        return this.printStackTraceOnUncaughtExceptions;
    }

    /**
     * @return the value of pulseImplementation
     */
    public String getPulseImplementation()
    {
        return this.pulseImplementation;
    }

    /**
     * @return the value of heartPulseRate
     */
    public int getHeartPulseRate()
    {
        return this.heartPulseRate;
    }

    /**
     * @return the value of heartEnabled
     */
    public boolean heartEnabled()
    {
        return this.heartEnabled;
    }

    /**
     * @return the value of AIMLWatcherTimer
     */
    public int getAIMLWatcherTimer()
    {
        return this.AIMLWatcherTimer;
    }

    /**
     * @return the value of useAIMLWatcher
     */
    public boolean useAIMLWatcher()
    {
        return this.useAIMLWatcher;
    }

    /**
     * @return the value of javascriptInterpreterClassname
     */
    public String getJavascriptInterpreterClassname()
    {
        return this.javascriptInterpreterClassname;
    }

    /**
     * @return the value of allowJavaScript
     */
    public boolean allowJavaScript()
    {
        return this.allowJavaScript;
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
     * @return the value of allowOSAccess
     */
    public boolean allowOSAccess()
    {
        return this.allowOSAccess;
    }

    /**
     * @return the value of categoryLoadNotificationInterval
     */
    public int getCategoryLoadNotificationInterval()
    {
        return this.categoryLoadNotificationInterval;
    }

    /**
     * @return the value of noteEachLoadedFile
     */
    public boolean noteEachLoadedFile()
    {
        return this.noteEachLoadedFile;
    }

    /**
     * @return the value of exitImmediatelyOnStartup
     */
    public boolean exitImmediatelyOnStartup()
    {
        return this.exitImmediatelyOnStartup;
    }

    /**
     * @return the value of connectString
     */
    public String getConnectString()
    {
        return this.connectString;
    }

    /**
     * @return the value of randomStrategy
     */
    public RandomStrategy getRandomStrategy()
    {
        return this.randomStrategy;
    }

    /**
     * @return the value of graphmapperImplementation
     */
    public String getGraphmapperImplementation()
    {
        return this.graphmapperImplementation;
    }

    /**
     * @return the value of nodemapperImplementation
     */
    public String getNodemapperImplementation()
    {
        return this.nodemapperImplementation;
    }

    /**
     * @return the value of useShell
     */
    public boolean useShell()
    {
        return this.useShell;
    }

    /**
     * @return the value of xmlParserUseEntityResolver2
     */
    public boolean xmlParserUseEntityResolver2()
    {
        return this.xmlParserUseEntityResolver2;
    }

    /**
     * @return the value of xmlParserUseValidation
     */
    public boolean xmlParserUseValidation()
    {
        return this.xmlParserUseValidation;
    }

    /**
     * @return the value of xmlParserUseSchemaValidation
     */
    public boolean xmlParserUseSchemaValidation()
    {
        return this.xmlParserUseSchemaValidation;
    }

    /**
     * @return the value of xmlParserHonourAllSchemaLocations
     */
    public boolean xmlParserHonourAllSchemaLocations()
    {
        return this.xmlParserHonourAllSchemaLocations;
    }

    /**
     * @return the value of xmlParserUseXInclude
     */
    public boolean xmlParserUseXInclude()
    {
        return this.xmlParserUseXInclude;
    }

    /**
     * @return the value of xmlParserValidateAnnotations
     */
    public boolean xmlParserValidateAnnotations()
    {
        return this.xmlParserValidateAnnotations;
    }

    /**
     * @param value the value for AIMLNamespaceURI
     */
    public void setAIMLNamespaceURI(URI value)
    {
        this.AIMLNamespaceURI = value;
    }

    /**
     * @param value the value for botConfigURL
     */
    public void setBotConfigURL(URL value)
    {
        this.botConfigURL = value;
    }

    /**
     * @param value the value for pluginConfigURL
     */
    public void setPluginConfigURL(URL value)
    {
        this.pluginConfigURL = value;
    }

    /**
     * @param value the value for gossipURL
     */
    public void setGossipURL(URL value)
    {
        this.gossipURL = value;
    }

    /**
     * @param value the value for predicateEmptyDefault
     */
    public void setPredicateEmptyDefault(String value)
    {
        this.predicateEmptyDefault = value;
    }

    /**
     * @param value the value for clientNamePredicate
     */
    public void setClientNamePredicate(String value)
    {
        this.clientNamePredicate = value;
    }

    /**
     * @param value the value for botNameProperty
     */
    public void setBotNameProperty(String value)
    {
        this.botNameProperty = value;
    }

    /**
     * @param value the value for predicateFlushPeriod
     */
    public void setPredicateFlushPeriod(int value)
    {
        this.predicateFlushPeriod = value;
    }

    /**
     * @param value the value for predicateManagerImplementation
     */
    public void setPredicateManagerImplementation(String value)
    {
        this.predicateManagerImplementation = value;
    }

    /**
     * @param value the value for ffpmDirectory
     */
    public void setFfpmDirectory(URL value)
    {
        this.ffpmDirectory = value;
    }

    /**
     * @param value the value for databaseURL
     */
    public void setDatabaseURL(String value)
    {
        this.databaseURL = value;
    }

    /**
     * @param value the value for databaseDriver
     */
    public void setDatabaseDriver(String value)
    {
        this.databaseDriver = value;
    }

    /**
     * @param value the value for databaseMaximumConnections
     */
    public void setDatabaseMaximumConnections(int value)
    {
        this.databaseMaximumConnections = value;
    }

    /**
     * @param value the value for databaseUsername
     */
    public void setDatabaseUsername(String value)
    {
        this.databaseUsername = value;
    }

    /**
     * @param value the value for databasePassword
     */
    public void setDatabasePassword(String value)
    {
        this.databasePassword = value;
    }

    /**
     * @param value the value for mergePolicy
     */
    public void setMergePolicy(MergePolicy value)
    {
        this.mergePolicy = value;
    }

    /**
     * @param value the value for noteEachMerge
     */
    public void setNoteEachMerge(boolean value)
    {
        this.noteEachMerge = value;
    }

    /**
     * @param value the value for appendMergeSeparatorString
     */
    public void setAppendMergeSeparatorString(String value)
    {
        this.appendMergeSeparatorString = value;
    }

    /**
     * @param value the value for responseTimeout
     */
    public void setResponseTimeout(int value)
    {
        this.responseTimeout = value;
    }

    /**
     * @param value the value for infiniteLoopInput
     */
    public void setInfiniteLoopInput(String value)
    {
        this.infiniteLoopInput = value;
    }

    /**
     * @param value the value for printStackTraceOnUncaughtExceptions
     */
    public void setPrintStackTraceOnUncaughtExceptions(boolean value)
    {
        this.printStackTraceOnUncaughtExceptions = value;
    }

    /**
     * @param value the value for pulseImplementation
     */
    public void setPulseImplementation(String value)
    {
        this.pulseImplementation = value;
    }

    /**
     * @param value the value for heartPulseRate
     */
    public void setHeartPulseRate(int value)
    {
        this.heartPulseRate = value;
    }

    /**
     * @param value the value for heartEnabled
     */
    public void setHeartEnabled(boolean value)
    {
        this.heartEnabled = value;
    }

    /**
     * @param value the value for AIMLWatcherTimer
     */
    public void setAIMLWatcherTimer(int value)
    {
        this.AIMLWatcherTimer = value;
    }

    /**
     * @param value the value for useAIMLWatcher
     */
    public void setUseAIMLWatcher(boolean value)
    {
        this.useAIMLWatcher = value;
    }

    /**
     * @param value the value for javascriptInterpreterClassname
     */
    public void setJavascriptInterpreterClassname(String value)
    {
        this.javascriptInterpreterClassname = value;
    }

    /**
     * @param value the value for allowJavaScript
     */
    public void setAllowJavaScript(boolean value)
    {
        this.allowJavaScript = value;
    }

    /**
     * @param value the value for systemInterpreterDirectory
     */
    public void setSystemInterpreterDirectory(URL value)
    {
        this.systemInterpreterDirectory = value;
    }

    /**
     * @param value the value for systemInterpreterPrefix
     */
    public void setSystemInterpreterPrefix(String value)
    {
        this.systemInterpreterPrefix = value;
    }

    /**
     * @param value the value for allowOSAccess
     */
    public void setAllowOSAccess(boolean value)
    {
        this.allowOSAccess = value;
    }

    /**
     * @param value the value for categoryLoadNotificationInterval
     */
    public void setCategoryLoadNotificationInterval(int value)
    {
        this.categoryLoadNotificationInterval = value;
    }

    /**
     * @param value the value for noteEachLoadedFile
     */
    public void setNoteEachLoadedFile(boolean value)
    {
        this.noteEachLoadedFile = value;
    }

    /**
     * @param value the value for exitImmediatelyOnStartup
     */
    public void setExitImmediatelyOnStartup(boolean value)
    {
        this.exitImmediatelyOnStartup = value;
    }

    /**
     * @param value the value for connectString
     */
    public void setConnectString(String value)
    {
        this.connectString = value;
    }

    /**
     * @param value the value for randomStrategy
     */
    public void setRandomStrategy(RandomStrategy value)
    {
        this.randomStrategy = value;
    }

    /**
     * @param value the value for graphmapperImplementation
     */
    public void setGraphmapperImplementation(String value)
    {
        this.graphmapperImplementation = value;
    }

    /**
     * @param value the value for nodemapperImplementation
     */
    public void setNodemapperImplementation(String value)
    {
        this.nodemapperImplementation = value;
    }

    /**
     * @param value the value for useShell
     */
    public void setUseShell(boolean value)
    {
        this.useShell = value;
    }

    /**
     * @param value the value for xmlParserUseEntityResolver2
     */
    public void setXmlParserUseEntityResolver2(boolean value)
    {
        this.xmlParserUseEntityResolver2 = value;
    }

    /**
     * @param value the value for xmlParserUseValidation
     */
    public void setXmlParserUseValidation(boolean value)
    {
        this.xmlParserUseValidation = value;
    }

    /**
     * @param value the value for xmlParserUseSchemaValidation
     */
    public void setXmlParserUseSchemaValidation(boolean value)
    {
        this.xmlParserUseSchemaValidation = value;
    }

    /**
     * @param value the value for xmlParserHonourAllSchemaLocations
     */
    public void setXmlParserHonourAllSchemaLocations(boolean value)
    {
        this.xmlParserHonourAllSchemaLocations = value;
    }

    /**
     * @param value the value for xmlParserUseXInclude
     */
    public void setXmlParserUseXInclude(boolean value)
    {
        this.xmlParserUseXInclude = value;
    }

    /**
     * @param value the value for xmlParserValidateAnnotations
     */
    public void setXmlParserValidateAnnotations(boolean value)
    {
        this.xmlParserValidateAnnotations = value;
    }

}
