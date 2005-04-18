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
 * Automatically generated from properties file, 2005-04-16T12:02:50.358-04:00
 */
public class HTTPServerSettings extends Settings
{
    /**
     * HTTP server (wrapper) to use (fully-qualified class name) 
     */
    private String classname;

    /**
     * configuration parameter for the HTTP server (not always applicable) 
     */
    private String config;

    /**
     * Pattern for naming the log file for web server messages. 
     */
    private String logPathPattern;

    /**
     * Creates a <code>HTTPServerSettings</code> using default property values.
     */
    public HTTPServerSettings()
    {
        super();
    }
    
    /**
     * Creates a <code>HTTPServerSettings</code> with the (XML-formatted) properties
     * located at the given path.
     *
     * @param propertiesPath the path to the configuration file
     */
    public HTTPServerSettings(String propertiesPath)
    {
        super(propertiesPath);
    }

    /**
    * Initializes the Settings with values from properties, or defaults.
    */
    protected void initialize()
    {
        setClassname(this.properties.getProperty("programd.httpserver.classname", "org.aitools.programd.server.JettyWrapper"));

        setConfig(this.properties.getProperty("programd.httpserver.config", "conf/jetty.xml"));

        setLogPathPattern(this.properties.getProperty("programd.httpserver.log.path-pattern", "logs/web-server-%g.log"));

    }

    /**
     * @return the value of classname
     */
    public String getClassname()
    {
        return this.classname;
    }

    /**
     * @return the value of config
     */
    public String getConfig()
    {
        return this.config;
    }

    /**
     * @return the value of logPathPattern
     */
    public String getLogPathPattern()
    {
        return this.logPathPattern;
    }

    /**
     * @param classnameToSet   the value to which to set classname
     */
    public void setClassname(String classnameToSet)
    {
        this.classname = classnameToSet;
    }

    /**
     * @param configToSet   the value to which to set config
     */
    public void setConfig(String configToSet)
    {
        this.config = configToSet;
    }

    /**
     * @param logPathPatternToSet   the value to which to set logPathPattern
     */
    public void setLogPathPattern(String logPathPatternToSet)
    {
        this.logPathPattern = logPathPatternToSet;
    }

}