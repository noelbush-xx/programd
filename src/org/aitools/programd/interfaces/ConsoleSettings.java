/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
 
package org.aitools.programd.interfaces;

import org.aitools.programd.util.Settings;


/**
 * Automatically generated from properties file, 2005-03-02T22:01:09.548-04:00
 */
public class ConsoleSettings extends Settings
{
    public ConsoleSettings()
    {
        super();
    }
    
    public ConsoleSettings(String propertiesPath)
    {
        super(propertiesPath);
    }

    /**
     * Show information on console? 
     */
    boolean enableConsole;

    /**
     * Show developer info messages on console? 
     */
    boolean showDeveloperInfo;

    /**
     * (developer) Show caller methods even for userinfo messages? 
     * This is an advanced debugging feature.  You likely want to leave it set to false.
     */
    boolean showMethodNamesAlways;

    /**
     * Show match-trace messages on console? 
     */
    boolean showMatchTrace;

    /**
     * Show message type flags on console? 
     */
    boolean showMessageFlags;

    /**
     * Which bot predicate contains the bot's name? 
     */
    String botNamePredicate;

    /**
     * Which bot predicate contains the client's name? 
     */
    String clientNamePredicate;

    /**
     * How many categories will be loaded before a message is displayed? 
     * Only meaningful if programd.enable-console == true.
     */
    int categoryLoadNotifyInterval;

    /**
     * The date-time format to use on the console. 
     * See http://java.sun.com/jdk1.5.0_01/docs/api/java/text/SimpleDateFormat.html
           for formatting codes.
     * Setting the value to blank means no timestamp will be displayed.
     */
    String timestampFormat;

    /**
     * Use interactive command-line shell? 
     */
    boolean useShell;

    /**
    * Initializes the Settings with values from properties, or defaults.
    */
    protected void initialize()
    {
        setEnableConsole(Boolean.valueOf(this.properties.getProperty("programd.console.enable-console", "true")).booleanValue());

        setShowDeveloperInfo(Boolean.valueOf(this.properties.getProperty("programd.console.show-developer-info", "true")).booleanValue());

        setShowMethodNamesAlways(Boolean.valueOf(this.properties.getProperty("programd.console.show-method-names-always", "false")).booleanValue());

        setShowMatchTrace(Boolean.valueOf(this.properties.getProperty("programd.console.show-match-trace", "true")).booleanValue());

        setShowMessageFlags(Boolean.valueOf(this.properties.getProperty("programd.console.show-message-flags", "true")).booleanValue());

        setBotNamePredicate(this.properties.getProperty("programd.console.bot-name-predicate", "name"));

        setClientNamePredicate(this.properties.getProperty("programd.console.client-name-predicate", "name"));

        try
        {
            setCategoryLoadNotifyInterval(Integer.parseInt(this.properties.getProperty("programd.console.category-load-notify-interval", "5000")));
        }
        catch (NumberFormatException e)
        {
            setCategoryLoadNotifyInterval(5000);
        }

        setTimestampFormat(this.properties.getProperty("programd.console.timestamp-format", "H:mm:ss"));

        setUseShell(Boolean.valueOf(this.properties.getProperty("programd.console.use-shell", "true")).booleanValue());

    }

    /**
     * @return the value of enableConsole
     */
    public boolean enableConsole()
    {
        return this.enableConsole;
    }

    /**
     * @return the value of showDeveloperInfo
     */
    public boolean showDeveloperInfo()
    {
        return this.showDeveloperInfo;
    }

    /**
     * @return the value of showMethodNamesAlways
     */
    public boolean showMethodNamesAlways()
    {
        return this.showMethodNamesAlways;
    }

    /**
     * @return the value of showMatchTrace
     */
    public boolean showMatchTrace()
    {
        return this.showMatchTrace;
    }

    /**
     * @return the value of showMessageFlags
     */
    public boolean showMessageFlags()
    {
        return this.showMessageFlags;
    }

    /**
     * @return the value of botNamePredicate
     */
    public String getBotNamePredicate()
    {
        return this.botNamePredicate;
    }

    /**
     * @return the value of clientNamePredicate
     */
    public String getClientNamePredicate()
    {
        return this.clientNamePredicate;
    }

    /**
     * @return the value of categoryLoadNotifyInterval
     */
    public int getCategoryLoadNotifyInterval()
    {
        return this.categoryLoadNotifyInterval;
    }

    /**
     * @return the value of timestampFormat
     */
    public String getTimestampFormat()
    {
        return this.timestampFormat;
    }

    /**
     * @return the value of useShell
     */
    public boolean useShell()
    {
        return this.useShell;
    }

    /**
     * @param enableConsoleToSet   the value to which to set enableConsole
     */
    public void setEnableConsole(boolean enableConsoleToSet)
    {
        this.enableConsole = enableConsoleToSet;
    }

    /**
     * @param showDeveloperInfoToSet   the value to which to set showDeveloperInfo
     */
    public void setShowDeveloperInfo(boolean showDeveloperInfoToSet)
    {
        this.showDeveloperInfo = showDeveloperInfoToSet;
    }

    /**
     * @param showMethodNamesAlwaysToSet   the value to which to set showMethodNamesAlways
     */
    public void setShowMethodNamesAlways(boolean showMethodNamesAlwaysToSet)
    {
        this.showMethodNamesAlways = showMethodNamesAlwaysToSet;
    }

    /**
     * @param showMatchTraceToSet   the value to which to set showMatchTrace
     */
    public void setShowMatchTrace(boolean showMatchTraceToSet)
    {
        this.showMatchTrace = showMatchTraceToSet;
    }

    /**
     * @param showMessageFlagsToSet   the value to which to set showMessageFlags
     */
    public void setShowMessageFlags(boolean showMessageFlagsToSet)
    {
        this.showMessageFlags = showMessageFlagsToSet;
    }

    /**
     * @param botNamePredicateToSet   the value to which to set botNamePredicate
     */
    public void setBotNamePredicate(String botNamePredicateToSet)
    {
        this.botNamePredicate = botNamePredicateToSet;
    }

    /**
     * @param clientNamePredicateToSet   the value to which to set clientNamePredicate
     */
    public void setClientNamePredicate(String clientNamePredicateToSet)
    {
        this.clientNamePredicate = clientNamePredicateToSet;
    }

    /**
     * @param categoryLoadNotifyIntervalToSet   the value to which to set categoryLoadNotifyInterval
     */
    public void setCategoryLoadNotifyInterval(int categoryLoadNotifyIntervalToSet)
    {
        this.categoryLoadNotifyInterval = categoryLoadNotifyIntervalToSet;
    }

    /**
     * @param timestampFormatToSet   the value to which to set timestampFormat
     */
    public void setTimestampFormat(String timestampFormatToSet)
    {
        this.timestampFormat = timestampFormatToSet;
    }

    /**
     * @param useShellToSet   the value to which to set useShell
     */
    public void setUseShell(boolean useShellToSet)
    {
        this.useShell = useShellToSet;
    }

}