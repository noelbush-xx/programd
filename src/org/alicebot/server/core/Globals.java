/*
    Alicebot Program D
    Copyright (C) 1995-2001, A.L.I.C.E. AI Foundation
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
    USA.
*/

/*
    Code cleanup (4.1.3 [00] - October 2001, Noel Bush)
    - formatting cleanup
    - general grammar fixes
    - complete javadoc
    - made all imports explicit
    - made constants private (enforce use of get/set methods!)
    - removed unused globals constant
    - constant name changes:
        _serverProps --> serverProperties
        _botName --> botName
        categoryCount --> categoryCount
        version --> version
    - enhanced fromFile to set private field values from properties file
      (avoid reparsing the value each time)
    - changed fromFile method to load, and made it load from a given Properties instead of a file
    - added set & get methods for any properties that need type-checking,
      as well as some others (to define defaults here)
    - changed all file path handling to expect/use canonical
*/

/*
    Further fixes and optimizations (4.1.3 [01] - November 2001, Noel Bush)
    - removed setBotPredicateValue() and getBotPredicateValue()
      (can be handled in BotProperty)
*/

/*
    More fixes (4.1.3 [02] - November 2001, Noel Bush)
    - comment changes
    - changed many server property names
    - added support for preventing <system> and <javascript> tags
*/

package org.alicebot.server.core;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.processor.AIMLProcessorRegistry;
import org.alicebot.server.core.processor.loadtime.StartupElementProcessorRegistry;
import org.alicebot.server.core.util.UserErrorException;
import org.alicebot.server.core.util.InputNormalizer;


/**
 *  <p>
 *  <code>Globals</code> gives access to the server
 *  properties used when starting the bot.
 *  </p>
 *
 *  @author Richard Wallace
 *  @author Jon Baer
 *  @author Thomas Ringate/Pedro Colla
 *  @version 4.1.3
 */
public class Globals
{
    /** Whether Globals is loaded. */
    private static boolean isLoaded = false;

    /** The server properties. */
    private static Properties serverProperties;

    /** The predicate in which to find the name of the bot. */
    private static String botNamePredicate;

    /** The bot id. */
    private static String botID;

    /** The name of the bot. */
    private static String botName;

    /** The predicate in which to find the name of the client. */
    private static String clientNamePredicate;

    /** The version of the software. */
    private static String version;

    /** The default value to return if a predicate is not defined. */
    private static String predicateEmptyDefault;

    /** The input to match if an infinite loop exception is thrown. */
    private static String infiniteLoopInput;

    /** Whether use of the <system> tag is allowed. */
    private static boolean osAccessAllowed;

    /** Whether use of the <javascript> tag is allowed. */
    private static boolean jsAccessAllowed;

    /** Whether to support deprecated &quot;AIML 0.9&quot; tags. */
    private static boolean supportDeprecatedTags;

    /** Whether to warn about deprecated &quot;AIML 0.9&quot; tags. */
    private static boolean warnAboutDeprecatedTags;

    /** Whether to require namespace qualification of non-AIML tags. */
    private static boolean nonAIMLRequireNamespaceQualification;

    /** How many predicate values to cache. */
    private static int predicateValueCacheMax;

    /** The path to the bot startup file. */
    private static String startupFilePath;

    /** The path to the file where targeting data should be written. */
    private static String targetsDataPath;

    /** The path to the file where generated targets should be written. */
    private static String targetsAIMLPath;

    /** The interval at which the user should be notified of how many targets have been loaded. */
    private static int categoryLoadNotifyInterval;

    /** The merge policy for this server. */
    private static String mergePolicy;

    /** Whether to show the console. */
    private static boolean showConsole;

    /** Whether to show a match trace. */
    private static boolean showMatchTrace;

    /** Whether to show the &quot;shell&quot;. */
    private static boolean useShell;

    /** Whether to use the AIML Watcher. */
    private static boolean useWatcher;

    /** Number of responses to wait before invoking targeting. */
    private static int targetSkip;

    /** Whether to use targeting. */
    private static boolean useTargeting;

    /** The host name. */
    private static String hostName;

    /** The port on which the http server is listening. */
    private static int httpPort;

    /** The fully-qualified name of the JavaScript interpreter. */
    private static String javaScriptInterpreter;

    /** An empty string, for convenience. */
    private static final String EMPTY_STRING = "";

    /** The AIML processor registry. */
    private static AIMLProcessorRegistry aimlProcessorRegistry;

    /** The startup element processor registry. */
    private static StartupElementProcessorRegistry startupElementProcessorRegistry;


    /**
     *  Creates a new <code>Globals</code> object.
     */
    public Globals()
    {
    }


    /**
     *  @return the AIMLProcessorRegistry
     */
    public static AIMLProcessorRegistry getAIMLProcessorRegistry()
    {
        return aimlProcessorRegistry;
    }


    /**
     *  @return the StartupElementProcessorRegistry
     */
    public static StartupElementProcessorRegistry getStartupElementProcessorRegistry()
    {
        return startupElementProcessorRegistry;
    }


    /**
     *  Loads some global values from a properties object.
     *
     *  @param properties   the properties object
     */
    public static void load(Properties properties)
    {
        // Globals is the only home for this properties; we don't pass it around.
        serverProperties = properties;

        // Whether to use the watcher; default false.
        useWatcher = Boolean.valueOf(serverProperties.getProperty("programd.watcher", "false")).booleanValue();

        // Whether to use the shell; default true.
        useShell = Boolean.valueOf(serverProperties.getProperty("programd.shell", "true")).booleanValue();

        // Whether to show the console; default true.
        showConsole = Boolean.valueOf(serverProperties.getProperty("programd.console", "true")).booleanValue();

        // Whether to show the match trace; default true.
        showMatchTrace = showConsole ? Boolean.valueOf(serverProperties.getProperty("programd.console.match-trace", "true")).booleanValue() : false;

        // Whether to use targeting; default true.
        useTargeting = Boolean.valueOf(serverProperties.getProperty("programd.targeting", "true")).booleanValue();

        // AIML targets file path; default ./targets/targets.aiml.
        targetsAIMLPath = serverProperties.getProperty("programd.targeting.aiml.path", "./targets/targets.aiml");

        // AIML targets file path; default ./targets/targets.aiml.
        targetsDataPath = serverProperties.getProperty("programd.targeting.data.path", "./targets/targets.xml");

        // Target skip
        try
        {
            targetSkip = Integer.parseInt(serverProperties.getProperty("programd.targeting.targetskip", "1"));
        }
        catch (NumberFormatException e)
        {
            targetSkip = 1;
        }

        // Don't let targetSkip be less than 1.
        targetSkip = targetSkip < 1 ? 1 : targetSkip;

        // The merge policy: default generic "true".
        mergePolicy = serverProperties.getProperty("programd.merge", "true");

        // The bot predicate in which to find the bot's name; default is "name".
        botNamePredicate = serverProperties.getProperty("programd.console.bot-name-predicate", "name");

        // The predicate in which to find the client's name; default is "name".
        clientNamePredicate = serverProperties.getProperty("programd.console.client-name-predicate", "name");

        // Default for predicates with no defined values; default is empty string.
        predicateEmptyDefault = serverProperties.getProperty("programd.emptydefault", "");

        // Input to match if an infinite loop is found; default is &quot;INFINITE LOOP&quot;.
        infiniteLoopInput =
            InputNormalizer.patternFitIgnoreCase(
                serverProperties.getProperty("programd.infinite-loop-input", "INFINITE LOOP"));

        // Whether to allow use of <system> tag; default false.
        osAccessAllowed = Boolean.valueOf(serverProperties.getProperty("programd.os-access-allowed", "false")).booleanValue();

        // Whether to allow use of <javascript> tag; default false.
        jsAccessAllowed = Boolean.valueOf(serverProperties.getProperty("programd.javascript-allowed", "false")).booleanValue();

        // Whether to support deprecated tags; default false.
        supportDeprecatedTags = Boolean.valueOf(serverProperties.getProperty("programd.deprecated-tags-support", "false")).booleanValue();

        // Whether to warn about deprecated tags; default false.
        warnAboutDeprecatedTags = supportDeprecatedTags ? Boolean.valueOf(serverProperties.getProperty("programd.deprecated-tags-warn", "false")).booleanValue() : false;

        // Whether to require namespace qualifiers on non-AIML tags; default false.
        nonAIMLRequireNamespaceQualification = Boolean.valueOf(serverProperties.getProperty("programd.non-aiml-require-namespace-qualifiers", "false")).booleanValue();

        // How many predicate values to cache; default 5000.
        try
        {
            predicateValueCacheMax = Integer.parseInt(serverProperties.getProperty("programd.predicate-cache.max", "5000"));
        }
        catch (NumberFormatException e)
        {
            predicateValueCacheMax = 5000;
        }
        predicateValueCacheMax = predicateValueCacheMax > 0 ? predicateValueCacheMax : 5000;


        // The fully-qualified name of the JavaScript interpreter.
        javaScriptInterpreter = serverProperties.getProperty("programd.interpreter.javascript", "");

        
        // Get the category load notify interval.
        try
        {
            categoryLoadNotifyInterval =
                Integer.parseInt(serverProperties.getProperty("programd.console.category-load-notify-interval", "1000"));
        }
        catch (NumberFormatException e)
        {
            categoryLoadNotifyInterval = 1000;
        }
        categoryLoadNotifyInterval = categoryLoadNotifyInterval > 0 ? categoryLoadNotifyInterval : 1000;

        // Make sure the startup file actually exists.
        try
        {
            startupFilePath =
                new File(serverProperties.getProperty("programd.startup",
                                                      "startup.xml")).getCanonicalPath();
        }
        catch (IOException e)
        {
            String error = "Startup file does not exist (check server properties).";
            Log.log(error, Log.STARTUP);
            throw new UserErrorException(error);
        }

        // Version comes from Graphmaster.
        version = Graphmaster.VERSION;

        // Initialize the processor registries.
        aimlProcessorRegistry = new AIMLProcessorRegistry();
        startupElementProcessorRegistry = new StartupElementProcessorRegistry();

        // Try to set the hostname.
        try
        {
            hostName = InetAddress.getLocalHost().getHostName();
        }
        catch (UnknownHostException e)
        {
            hostName = "unknown-host";
        }
        isLoaded = true;
    }


    /**
     *  Returns whether Globals is loaded.
     *
     *  @return whether Globals is loaded
     */
    public static boolean isLoaded()
    {
        return isLoaded;
    }


    /**
     *  Returns the version string.
     *
     *  @return the version string
     */
    public static String getVersion()
    {
        return version;
    }


    /**
     *  Returns the bot's name (temporary).
     *
     *  @return the bot's name (temporary)
     */
    public static String getBotName()
    {
        return botName;
    }


    /**
     *  Sets the bot's name (temporary).
     */
    public static void setBotName()
    {
        botName = BotProperty.getPredicateValue(botNamePredicate);
    }


    /**
     *  Returns the bot's id (temporary).
     *
     *  @return the bot's id (temporary)
     */
    public static String getBotID()
    {
        return botID;
    }


    /**
     *  Sets the bot's id (temporary).
     *
     *  @param id   the id to set
     */
    public static void setBotID(String id)
    {
        botID = id;
    }


    /**
     *  Returns the startup file path.
     *
     *  @return the startup file path
     */
    public static String getStartupFilePath()
    {
        return startupFilePath;
    }


    /**
     *  Returns the predicate name with which the client's name is associated.
     *
     *  @return the predicate name with which the client's name is associated
     */
    public static String getClientNamePredicate()
    {
        return clientNamePredicate;
    }


    /**
     *  Returns the default value for undefined predicate values.
     *
     *  @return the default value for undefined predicate values
     */
    public static String getPredicateEmptyDefault()
    {
        return predicateEmptyDefault;
    }


    /**
     *  Returns the input to match if an infinite loop exception is thrown.
     *
     *  @return the input to match if an infinite loop exception is thrown
     */
    public static String getInfiniteLoopInput()
    {
        return infiniteLoopInput;
    }


    /**
     *  Returns whether to show the console.
     *
     *  @return whether to show the console
     */
    public static boolean showConsole()
    {
        return showConsole;
    }

    
    /**
     *  Returns whether to show match trace messages on the console.
     *
     *  @return whether to show trace messages on console
     */
    public static boolean showMatchTrace()
    {
        return showMatchTrace;
    }

    
    /**
     *  Returns whether the {@link org.alicebot.server.core.loader.AIMLWatcher AIML Watcher} is active.
     *
     *  @return whether the {@link org.alicebot.server.core.loader.AIMLWatcher AIML Watcher} is active
     */
    public static boolean isWatcherActive()
    {
        return useWatcher;
    }


    /**
     *  Returns whether to use the command-line shell.
     *
     *  @return whether to use the command-line shell
     */
    public static boolean useShell()
    {
        return useShell;
    }


    /**
     *  Returns the merge policy.
     *
     *  @return the merge policy String
     */
    public static String getMergePolicy()
    {
        return mergePolicy;
    }


    /**
     *  Returns whether to use targeting.
     *
     *  @return whether to use targeting
     */
    public static boolean useTargeting()
    {
        return useTargeting;
    }


    /**
     *  Returns the path to the targets file for dumping generated AIML.
     *
     *  @return the path to the targets file for dumping generated AIML
     */
    public static String getTargetsAIMLPath()
    {
        return targetsAIMLPath;
    }


    /**
     *  Returns the path to the data file for dumping targeting data.
     *
     *  @return the path to the data file for dumping targeting data
     */
    public static String getTargetsDataPath()
    {
        return targetsDataPath;
    }


    /**
     *  Returns the response period for invoking targeting.
     *
     *  @return the response period for invoking targeting
     */
    public static int getTargetSkip()
    {
        return targetSkip;
    }


    /**
     *  @return the category load notify interval
     */
    public static int getCategoryLoadNotifyInterval()
    {
        return categoryLoadNotifyInterval;
    }


    /**
     *  Sets the http port number.
     *
     *  @param port the port number
     */
    public static void setHttpPort(int port)
    {
        httpPort = port;
    }


    /**
     *  Returns the http port number.
     *
     *  @return http port number
     */
    public static int getHttpPort()
    {
        return httpPort;
    }


    /**
     *  Returns the host name.
     *
     *  @return the host name
     */
    public static String getHostName()
    {
        return hostName;
    }


    /**
     *  Returns whether to support deprecated &quot;AIML 0.9&quot; tags.
     *
     *  @return whether to support deprecated &quot;AIML 0.9&quot; tags
     */
    public static boolean supportDeprecatedTags()
    {
        return supportDeprecatedTags;
    }


    /**
     *  Returns whether to warn about deprecated &quot;AIML 0.9&quot; tags.
     *
     *  @return whether to warn about deprecated &quot;AIML 0.9&quot; tags
     */
    public static boolean warnAboutDeprecatedTags()
    {
        return warnAboutDeprecatedTags;
    }


    /**
     *  Returns whether to require namespace qualifiers on non-AIML tags.
     *
     *  @return whether to require namespace qualifiers on non-AIML tags
     */
    public static boolean nonAIMLRequireNamespaceQualification()
    {
        return nonAIMLRequireNamespaceQualification;
    }


    /**
     *  Returns the number of predicate values to cache.
     *
     *  @return the number of predicate values to cache
     */
    public static int predicateValueCacheMax()
    {
        return predicateValueCacheMax;
    }


    /**
     *  Returns whether the <code>system</code> tag is allowed.
     *
     *  @return whether the <code>system</code> tag is allowed
     */
    public static boolean osAccessAllowed()
    {
        return osAccessAllowed;
    }


    /**
     *  Returns whether the <code>javascript</code> tag is allowed.
     *
     *  @return whether the <code>javascript</code> tag is allowed
     */
    public static boolean jsAccessAllowed()
    {
        return jsAccessAllowed;
    }


    /**
     *  Returns the fully-qualified class name of the JavaScript interpteter (if any).
     *
     *  @return the fully-qualified class name of the JavaScript interpteter (if any)
     */
    public static String javaScriptInterpreter()
    {
        return javaScriptInterpreter;
    }


    /**
     *  Returns the value of a property string.
     *
     *  @param propertyName the name of the property whose value is wanted
     *
     *  @return the value of the named property
     */
    public static String getProperty(String propertyName)
    {
        if (serverProperties == null)
        {
            return null;
        }
        return serverProperties.getProperty(propertyName);
    }


    /**
     *  Returns the value of a property string (allows specifying a default).
     *
     *  @param propertyName the name of the property whose value is wanted
     *  @param the default if no value is defined for the property
     *
     *  @return the value of the named property
     */
    public static String getProperty(String propertyName, String defaultValue)
    {
        if (serverProperties == null)
        {
            return defaultValue;
        }
        return serverProperties.getProperty(propertyName, defaultValue);
    }
}
