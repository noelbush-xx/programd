/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
 
package org.aitools.programd.configurations;

import org.aitools.programd.util.Settings;

/**
 * Automatically generated from properties file, 2005-03-21T21:58:10.337-04:00
 */
public class WebServerSettings extends Settings
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
     *log file for web server messages. 
     */
    private String logPath;

    /**
     * Creates a <code>WebServerSettings</code> using default property values.
     */
    public WebServerSettings()
    {
        super();
    }
    
    /**
     * Creates a <code>WebServerSettings</code> with the (XML-formatted) properties
     * located at the given path.
     * @param propertiesPath the path to the properties file to use
     */
    public WebServerSettings(String propertiesPath)
    {
        super(propertiesPath);
    }

    /**
    * Initializes the Settings with values from properties, or defaults.
    */
    protected void initialize()
    {
        setHttpserverClassname(this.properties.getProperty("programd.webserver.httpserver-classname", "org.aitools.programd.server.JettyWrapper"));

        setHttpserverConfig(this.properties.getProperty("programd.webserver.httpserver-config", "conf/jetty.xml"));

        setLogPath(this.properties.getProperty("programd.webserver.log.path", "logs/web-server.log"));

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
     * @return the value of logPath
     */
    public String getLogPath()
    {
        return this.logPath;
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
     * @param logPathToSet   the value to which to set logPath
     */
    public void setLogPath(String logPathToSet)
    {
        this.logPath = logPathToSet;
    }

}