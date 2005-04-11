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
 * Automatically generated from properties file, 2005-04-07T14:50:51.2-04:00
 */
public class HTMLResponderSettings extends Settings
{
    /**
     * The html templates directory (relative to programd.home).
     */
    private String templateDirectory;

    /**
     * The default chat template name. Note: Any other *.html, *.htm or *.data
     * files in programd.responder.flash.template.directory will also be
     * available if you specify a template name (without suffixes) parameter in
     * the user request.
     */
    private String chatDefaultTemplateName;

    /**
     * Use user authentication?
     */
    private boolean useUserAuthentication;

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
     * The access denied page.
     */
    private String accessDeniedPagePath;

    /**
     * The login success page.
     */
    private String loginSuccessPagePath;

    /**
     * The login failed page.
     */
    private String loginFailedPagePath;

    /**
     * The logout success page.
     */
    private String logoutSuccessPagePath;

    /**
     * The password change succeeded page.
     */
    private String passwordChangeSucceededPagePath;

    /**
     * The old password invalid page.
     */
    private String oldPasswordInvalidPagePath;

    /**
     * The username format invalid page.
     */
    private String usernameFormatInvalidPagePath;

    /**
     * The password format invalid page.
     */
    private String passwordFormatInvalidPagePath;

    /**
     * The password not confirmed page.
     */
    private String passwordNotConfirmedPagePath;

    /**
     * The registration succeeded page.
     */
    private String registrationSucceededPagePath;

    /**
     * Whether to enable authentication via the HTMLResponder
     */
    private boolean authenticate;

    /**
     * Whether to automatically generate a cookie for an unknown user Only
     * applicable if programd.httpserver.authenticate == true
     */
    private boolean autocookie;

    /**
     * Creates a <code>HTMLResponderSettings</code> using default property
     * values.
     */
    public HTMLResponderSettings()
    {
        super();
    }

    /**
     * Creates a <code>HTMLResponderSettings</code> with the (XML-formatted)
     * properties located at the given path.
     * 
     * @param propertiesPath the path to the configuration file
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

        setChatDefaultTemplateName(this.properties.getProperty("programd.responder.html.chat.default-template.name", "chat"));

        setUseUserAuthentication(Boolean.valueOf(this.properties.getProperty("programd.responder.html.use-user-authentication", "true"))
                .booleanValue());

        setRegisterFormPath(this.properties.getProperty("programd.responder.html.register.form.path", "register.html"));

        setLoginFormPath(this.properties.getProperty("programd.responder.html.login.form.path", "login.html"));

        setChangePasswordFormPath(this.properties.getProperty("programd.responder.html.change-password.form.path", "change-password.html"));

        setAccessDeniedPagePath(this.properties.getProperty("programd.responder.html.access-denied.page.path", "access-denied.html"));

        setLoginSuccessPagePath(this.properties.getProperty("programd.responder.html.login-success.page.path", "login-success.html"));

        setLoginFailedPagePath(this.properties.getProperty("programd.responder.html.login-failed.page.path", "login-failed.html"));

        setLogoutSuccessPagePath(this.properties.getProperty("programd.responder.html.logout-success.page.path", "logout-success.html"));

        setPasswordChangeSucceededPagePath(this.properties.getProperty("programd.responder.html.password-change-succeeded.page.path",
                "password-change-succeeded.html"));

        setOldPasswordInvalidPagePath(this.properties.getProperty("programd.responder.html.old-password-invalid.page.path",
                "old-password-invalid.html"));

        setUsernameFormatInvalidPagePath(this.properties.getProperty("programd.responder.html.username-format-invalid.page.path",
                "username-format-invalid.html"));

        setPasswordFormatInvalidPagePath(this.properties.getProperty("programd.responder.html.password-format-invalid.page.path",
                "password-format-invalid.html"));

        setPasswordNotConfirmedPagePath(this.properties.getProperty("programd.responder.html.password-not-confirmed.page.path",
                "password-not-confirmed.html"));

        setRegistrationSucceededPagePath(this.properties.getProperty("programd.responder.html.registration-succeeded.page.path",
                "registration-succeeded.html"));

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
     * @return the value of chatDefaultTemplateName
     */
    public String getChatDefaultTemplateName()
    {
        return this.chatDefaultTemplateName;
    }

    /**
     * @return the value of useUserAuthentication
     */
    public boolean useUserAuthentication()
    {
        return this.useUserAuthentication;
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
     * @return the value of accessDeniedPagePath
     */
    public String getAccessDeniedPagePath()
    {
        return this.accessDeniedPagePath;
    }

    /**
     * @return the value of loginSuccessPagePath
     */
    public String getLoginSuccessPagePath()
    {
        return this.loginSuccessPagePath;
    }

    /**
     * @return the value of loginFailedPagePath
     */
    public String getLoginFailedPagePath()
    {
        return this.loginFailedPagePath;
    }

    /**
     * @return the value of logoutSuccessPagePath
     */
    public String getLogoutSuccessPagePath()
    {
        return this.logoutSuccessPagePath;
    }

    /**
     * @return the value of passwordChangeSucceededPagePath
     */
    public String getPasswordChangeSucceededPagePath()
    {
        return this.passwordChangeSucceededPagePath;
    }

    /**
     * @return the value of oldPasswordInvalidPagePath
     */
    public String getOldPasswordInvalidPagePath()
    {
        return this.oldPasswordInvalidPagePath;
    }

    /**
     * @return the value of usernameFormatInvalidPagePath
     */
    public String getUsernameFormatInvalidPagePath()
    {
        return this.usernameFormatInvalidPagePath;
    }

    /**
     * @return the value of passwordFormatInvalidPagePath
     */
    public String getPasswordFormatInvalidPagePath()
    {
        return this.passwordFormatInvalidPagePath;
    }

    /**
     * @return the value of passwordNotConfirmedPagePath
     */
    public String getPasswordNotConfirmedPagePath()
    {
        return this.passwordNotConfirmedPagePath;
    }

    /**
     * @return the value of registrationSucceededPagePath
     */
    public String getRegistrationSucceededPagePath()
    {
        return this.registrationSucceededPagePath;
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
     * @param templateDirectoryToSet the value to which to set templateDirectory
     */
    public void setTemplateDirectory(String templateDirectoryToSet)
    {
        this.templateDirectory = templateDirectoryToSet;
    }

    /**
     * @param chatDefaultTemplateNameToSet the value to which to set
     *            chatDefaultTemplateName
     */
    public void setChatDefaultTemplateName(String chatDefaultTemplateNameToSet)
    {
        this.chatDefaultTemplateName = chatDefaultTemplateNameToSet;
    }

    /**
     * @param useUserAuthenticationToSet the value to which to set
     *            useUserAuthentication
     */
    public void setUseUserAuthentication(boolean useUserAuthenticationToSet)
    {
        this.useUserAuthentication = useUserAuthenticationToSet;
    }

    /**
     * @param registerFormPathToSet the value to which to set registerFormPath
     */
    public void setRegisterFormPath(String registerFormPathToSet)
    {
        this.registerFormPath = registerFormPathToSet;
    }

    /**
     * @param loginFormPathToSet the value to which to set loginFormPath
     */
    public void setLoginFormPath(String loginFormPathToSet)
    {
        this.loginFormPath = loginFormPathToSet;
    }

    /**
     * @param changePasswordFormPathToSet the value to which to set
     *            changePasswordFormPath
     */
    public void setChangePasswordFormPath(String changePasswordFormPathToSet)
    {
        this.changePasswordFormPath = changePasswordFormPathToSet;
    }

    /**
     * @param accessDeniedPagePathToSet the value to which to set
     *            accessDeniedPagePath
     */
    public void setAccessDeniedPagePath(String accessDeniedPagePathToSet)
    {
        this.accessDeniedPagePath = accessDeniedPagePathToSet;
    }

    /**
     * @param loginSuccessPagePathToSet the value to which to set
     *            loginSuccessPagePath
     */
    public void setLoginSuccessPagePath(String loginSuccessPagePathToSet)
    {
        this.loginSuccessPagePath = loginSuccessPagePathToSet;
    }

    /**
     * @param loginFailedPagePathToSet the value to which to set
     *            loginFailedPagePath
     */
    public void setLoginFailedPagePath(String loginFailedPagePathToSet)
    {
        this.loginFailedPagePath = loginFailedPagePathToSet;
    }

    /**
     * @param logoutSuccessPagePathToSet the value to which to set
     *            logoutSuccessPagePath
     */
    public void setLogoutSuccessPagePath(String logoutSuccessPagePathToSet)
    {
        this.logoutSuccessPagePath = logoutSuccessPagePathToSet;
    }

    /**
     * @param passwordChangeSucceededPagePathToSet the value to which to set
     *            passwordChangeSucceededPagePath
     */
    public void setPasswordChangeSucceededPagePath(String passwordChangeSucceededPagePathToSet)
    {
        this.passwordChangeSucceededPagePath = passwordChangeSucceededPagePathToSet;
    }

    /**
     * @param oldPasswordInvalidPagePathToSet the value to which to set
     *            oldPasswordInvalidPagePath
     */
    public void setOldPasswordInvalidPagePath(String oldPasswordInvalidPagePathToSet)
    {
        this.oldPasswordInvalidPagePath = oldPasswordInvalidPagePathToSet;
    }

    /**
     * @param usernameFormatInvalidPagePathToSet the value to which to set
     *            usernameFormatInvalidPagePath
     */
    public void setUsernameFormatInvalidPagePath(String usernameFormatInvalidPagePathToSet)
    {
        this.usernameFormatInvalidPagePath = usernameFormatInvalidPagePathToSet;
    }

    /**
     * @param passwordFormatInvalidPagePathToSet the value to which to set
     *            passwordFormatInvalidPagePath
     */
    public void setPasswordFormatInvalidPagePath(String passwordFormatInvalidPagePathToSet)
    {
        this.passwordFormatInvalidPagePath = passwordFormatInvalidPagePathToSet;
    }

    /**
     * @param passwordNotConfirmedPagePathToSet the value to which to set
     *            passwordNotConfirmedPagePath
     */
    public void setPasswordNotConfirmedPagePath(String passwordNotConfirmedPagePathToSet)
    {
        this.passwordNotConfirmedPagePath = passwordNotConfirmedPagePathToSet;
    }

    /**
     * @param registrationSucceededPagePathToSet the value to which to set
     *            registrationSucceededPagePath
     */
    public void setRegistrationSucceededPagePath(String registrationSucceededPagePathToSet)
    {
        this.registrationSucceededPagePath = registrationSucceededPagePathToSet;
    }

    /**
     * @param authenticateToSet the value to which to set authenticate
     */
    public void setAuthenticate(boolean authenticateToSet)
    {
        this.authenticate = authenticateToSet;
    }

    /**
     * @param autocookieToSet the value to which to set autocookie
     */
    public void setAutocookie(boolean autocookieToSet)
    {
        this.autocookie = autocookieToSet;
    }

}