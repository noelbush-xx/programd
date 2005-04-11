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
 * Automatically generated from properties file, 2005-04-09T14:46:52.88-04:00
 */
public class ConsoleSettings extends Settings
{
    /**
     * Show information on console?
     */
    private boolean enableConsole;

    /**
     * Show developer info messages on console?
     */
    private boolean showDeveloperInfo;

    /**
     * (developer) Show caller methods for errors? This is an advanced debugging
     * feature. You likely want to leave it set to false.
     */
    private boolean showMethodNamesForErrors;

    /**
     * (developer) Show caller methods for *all* messages? This is an advanced
     * debugging feature. You likely want to leave it set to false.
     */
    private boolean showMethodNamesAlways;

    /**
     * The date-time format to use on the console. See
     * http://java.sun.com/jdk1.5.0_02/docs/api/java/text/SimpleDateFormat.html
     * for formatting codes. Setting the value to blank means no timestamp will
     * be displayed.
     */
    private String timestampFormat;

    /**
     * Use interactive command-line shell?
     */
    private boolean useShell;

    /**
     * Creates a <code>ConsoleSettings</code> using default property values.
     */
    public ConsoleSettings()
    {
        super();
    }

    /**
     * Creates a <code>ConsoleSettings</code> with the (XML-formatted)
     * properties located at the given path.
     * 
     * @param propertiesPath the path to the configuration file
     */
    public ConsoleSettings(String propertiesPath)
    {
        super(propertiesPath);
    }

    /**
     * Initializes the Settings with values from properties, or defaults.
     */
    protected void initialize()
    {
        setEnableConsole(Boolean.valueOf(this.properties.getProperty("programd.console.enable-console", "true")).booleanValue());

        setShowDeveloperInfo(Boolean.valueOf(this.properties.getProperty("programd.console.show-developer-info", "true")).booleanValue());

        setShowMethodNamesForErrors(Boolean.valueOf(this.properties.getProperty("programd.console.show-method-names-for-errors", "false"))
                .booleanValue());

        setShowMethodNamesAlways(Boolean.valueOf(this.properties.getProperty("programd.console.show-method-names-always", "false")).booleanValue());

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
     * @return the value of showMethodNamesForErrors
     */
    public boolean showMethodNamesForErrors()
    {
        return this.showMethodNamesForErrors;
    }

    /**
     * @return the value of showMethodNamesAlways
     */
    public boolean showMethodNamesAlways()
    {
        return this.showMethodNamesAlways;
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
     * @param enableConsoleToSet the value to which to set enableConsole
     */
    public void setEnableConsole(boolean enableConsoleToSet)
    {
        this.enableConsole = enableConsoleToSet;
    }

    /**
     * @param showDeveloperInfoToSet the value to which to set showDeveloperInfo
     */
    public void setShowDeveloperInfo(boolean showDeveloperInfoToSet)
    {
        this.showDeveloperInfo = showDeveloperInfoToSet;
    }

    /**
     * @param showMethodNamesForErrorsToSet the value to which to set
     *            showMethodNamesForErrors
     */
    public void setShowMethodNamesForErrors(boolean showMethodNamesForErrorsToSet)
    {
        this.showMethodNamesForErrors = showMethodNamesForErrorsToSet;
    }

    /**
     * @param showMethodNamesAlwaysToSet the value to which to set
     *            showMethodNamesAlways
     */
    public void setShowMethodNamesAlways(boolean showMethodNamesAlwaysToSet)
    {
        this.showMethodNamesAlways = showMethodNamesAlwaysToSet;
    }

    /**
     * @param timestampFormatToSet the value to which to set timestampFormat
     */
    public void setTimestampFormat(String timestampFormatToSet)
    {
        this.timestampFormat = timestampFormatToSet;
    }

    /**
     * @param useShellToSet the value to which to set useShell
     */
    public void setUseShell(boolean useShellToSet)
    {
        this.useShell = useShellToSet;
    }

}