/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */
 
package org.aitools.programd.responder;

import org.aitools.programd.util.Settings;

/**
 * Automatically generated from properties file, 2005-03-21T21:57:50.724-04:00
 */
public class HTMLResponderSettings extends Settings
{
    /**
     * The html templates directory (relative to programd.home). 
     */
    private String templateDirectory;

    /**
     * The default chat template.  
     * Note: Any other *.html, *.htm or *.data files in
           programd.responder.flash.template.directory will also be available if
           you specify a template name (without suffixes) parameter in the user request.
     */
    private String chatDefaultTemplatePath;

    /**
     * The registration form. 
     */
    private String registerFormPath;

    /**
     * The login form. 
     */
    private String loginFormPath;

    /**
     * The change password form. 
     */
    private String changePasswordFormPath;

    /**
     * Whether to enable authentication via the HTMLResponder 
     */
    private boolean authenticate;

    /**
     * Whether to automatically generate a cookie for an unknown user 
     * Only applicable if programd.httpserver.authenticate == true
     */
    private boolean autocookie;

    /**
     * Creates a <code>HTMLResponderSettings</code> using default property values.
     */
    public HTMLResponderSettings()
    {
        super();
    }
    
    /**
     * Creates a <code>HTMLResponderSettings</code> with the (XML-formatted) properties
     * located at the given path.
     * @param propertiesPath the path to the settings file to use
     */
    public HTMLResponderSettings(String propertiesPath)
    {
        super(propertiesPath);
    }

    /**
    * Initializes the Settings with values from properties, or defaults.
    */
    protected void initialize()
    {
        setTemplateDirectory(this.properties.getProperty("programd.responder.html.template.directory", "templates/html"));

        setChatDefaultTemplatePath(this.properties.getProperty("programd.responder.html.chat.default-template.path", "chat.html"));

        setRegisterFormPath(this.properties.getProperty("programd.responder.html.register.form.path", "register.html"));

        setLoginFormPath(this.properties.getProperty("programd.responder.html.login.form.path", "login.html"));

        setChangePasswordFormPath(this.properties.getProperty("programd.responder.html.change-password.form.path", "change-password.html"));

        setAuthenticate(Boolean.valueOf(this.properties.getProperty("programd.responder.html.authenticate", "true")).booleanValue());

        setAutocookie(Boolean.valueOf(this.properties.getProperty("programd.responder.html.autocookie", "true")).booleanValue());

    }

    /**
     * @return the value of templateDirectory
     */
    public String getTemplateDirectory()
    {
        return this.templateDirectory;
    }

    /**
     * @return the value of chatDefaultTemplatePath
     */
    public String getChatDefaultTemplatePath()
    {
        return this.chatDefaultTemplatePath;
    }

    /**
     * @return the value of registerFormPath
     */
    public String getRegisterFormPath()
    {
        return this.registerFormPath;
    }

    /**
     * @return the value of loginFormPath
     */
    public String getLoginFormPath()
    {
        return this.loginFormPath;
    }

    /**
     * @return the value of changePasswordFormPath
     */
    public String getChangePasswordFormPath()
    {
        return this.changePasswordFormPath;
    }

    /**
     * @return the value of authenticate
     */
    public boolean authenticate()
    {
        return this.authenticate;
    }

    /**
     * @return the value of autocookie
     */
    public boolean autocookie()
    {
        return this.autocookie;
    }

    /**
     * @param templateDirectoryToSet   the value to which to set templateDirectory
     */
    public void setTemplateDirectory(String templateDirectoryToSet)
    {
        this.templateDirectory = templateDirectoryToSet;
    }

    /**
     * @param chatDefaultTemplatePathToSet   the value to which to set chatDefaultTemplatePath
     */
    public void setChatDefaultTemplatePath(String chatDefaultTemplatePathToSet)
    {
        this.chatDefaultTemplatePath = chatDefaultTemplatePathToSet;
    }

    /**
     * @param registerFormPathToSet   the value to which to set registerFormPath
     */
    public void setRegisterFormPath(String registerFormPathToSet)
    {
        this.registerFormPath = registerFormPathToSet;
    }

    /**
     * @param loginFormPathToSet   the value to which to set loginFormPath
     */
    public void setLoginFormPath(String loginFormPathToSet)
    {
        this.loginFormPath = loginFormPathToSet;
    }

    /**
     * @param changePasswordFormPathToSet   the value to which to set changePasswordFormPath
     */
    public void setChangePasswordFormPath(String changePasswordFormPathToSet)
    {
        this.changePasswordFormPath = changePasswordFormPathToSet;
    }

    /**
     * @param authenticateToSet   the value to which to set authenticate
     */
    public void setAuthenticate(boolean authenticateToSet)
    {
        this.authenticate = authenticateToSet;
    }

    /**
     * @param autocookieToSet   the value to which to set autocookie
     */
    public void setAutocookie(boolean autocookieToSet)
    {
        this.autocookie = autocookieToSet;
    }

}