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
 * Automatically generated from properties file, 2005-04-10T10:20:36.865-04:00
 */
public class HTTPServerSettings extends Settings
{
    /**
     * HTTP server (wrapper) to use (fully-qualified class name) 
     */
    private String httpserverClassname;

    /**
     * configuration parameter for the HTTP server (not always applicable) 
     */
    private String httpserverConfig;

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
        setHttpserverClassname(this.properties.getProperty("programd.httpserver.httpserver-classname", "org.aitools.programd.server.JettyWrapper"));

        setHttpserverConfig(this.properties.getProperty("programd.httpserver.httpserver-config", "conf/jetty.xml"));

        setLogPathPattern(this.properties.getProperty("programd.httpserver.log.path-pattern", "logs/web-server-%g.log"));

    }

    /**
     * @return the value of httpserverClassname
     */
    public String getHttpserverClassname()
    {
        return this.httpserverClassname;
    }

    /**
     * @return the value of httpserverConfig
     */
    public String getHttpserverConfig()
    {
        return this.httpserverConfig;
    }

    /**
     * @return the value of logPathPattern
     */
    public String getLogPathPattern()
    {
        return this.logPathPattern;
    }

    /**
     * @param httpserverClassnameToSet   the value to which to set httpserverClassname
     */
    public void setHttpserverClassname(String httpserverClassnameToSet)
    {
        this.httpserverClassname = httpserverClassnameToSet;
    }

    /**
     * @param httpserverConfigToSet   the value to which to set httpserverConfig
     */
    public void setHttpserverConfig(String httpserverConfigToSet)
    {
        this.httpserverConfig = httpserverConfigToSet;
    }

    /**
     * @param logPathPatternToSet   the value to which to set logPathPattern
     */
    public void setLogPathPattern(String logPathPatternToSet)
    {
        this.logPathPattern = logPathPatternToSet;
    }

}