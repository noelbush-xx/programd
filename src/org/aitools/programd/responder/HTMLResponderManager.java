/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.responder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aitools.programd.Core;
import org.aitools.programd.CoreSettings;
import org.aitools.programd.multiplexor.Multiplexor;
import org.aitools.programd.multiplexor.DuplicateUserIDError;
import org.aitools.programd.server.ServletRequestTransactionEnvelope;
import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.FileManager;
import org.aitools.programd.util.SuffixFilenameFilter;
import org.aitools.programd.util.UserError;

/**
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class HTMLResponderManager extends AbstractXMLResponderManager implements
        ServletRequestResponderManager
{
    /** The HTMLResponder settings. */
    private HTMLResponderSettings settings;

    /** The Multiplexor in use. */
    private Multiplexor multiplexor;

    /** The templates directory path. */
    private String templatesDirectoryPath;

    /** Path to the registration template. */
    private String registrationForm;

    /** Path to the login template. */
    private String loginFormPath;

    /** Path to the change password template. */
    private String changePasswordFormPath;

    /** Path to the access denied page. */
    private String accessDeniedPagePath;

    private String loginSuccessPagePath;

    private String loginFailedPagePath;

    private String logoutSuccessPagePath;

    private String passwordChangeSucceededPagePath;

    private String oldPasswordInvalidPagePath;

    private String passwordFormatInvalidPagePath;

    private String passwordNotConfirmedPagePath;

    private String registrationSucceededPagePath;

    private String usernameFormatInvalidPagePath;

    /** A filename filter for finding html templates. */
    private static final SuffixFilenameFilter FILENAME_FILTER = new SuffixFilenameFilter(
            new String[] { ".html", ".htm", ".data", ".php" });

    /** The Multiplexor secret key. */
    private String secretKey;

    private static final String USERNAME_FORMAT = "\\p{Alnum}{4,}";

    private static final String PASSWORD_FORMAT = "\\p{Graph}{6,}";

    // Convenice constants

    /** The string &quot;{@value}&quot;. */
    private static final String USER_AGENT = "USER-AGENT";

    /** The string &quot;{@value}&quot; (for sending html). */
    private static final String HTML_CONTENT_TYPE = "text/html; charset=UTF-8";

    /** Known user agent strings for browsers. */
    private static final String[] HTML_USER_AGENTS = new String[] { "Mozilla", "MSIE", "Lynx",
            "Opera" };

    /** Number of known user agents. */
    private static final int HTML_USER_AGENT_COUNT = HTML_USER_AGENTS.length;

    /** The user id cookie name. */
    public static final String USER_COOKIE_NAME = "programd_user";

    /** The password cookie name. */
    public static final String PASSWORD_COOKIE_NAME = "programd_password";

    /** The string &quot;{@value}&quot;. */
    private static final String REQUEST = "request";

    /** The string &quot;{@value}&quot;. */
    private static final String LOGGED_IN = "logged-in";

    /**
     * Creates this object.
     * 
     * @param coreToUse
     *            the core to use when getting some values
     */
    public HTMLResponderManager(Core coreToUse)
    {
        super(coreToUse);
        this.multiplexor = this.core.getMultiplexor();
        CoreSettings coreSettings = this.core.getSettings();
        this.settings = new HTMLResponderSettings(coreSettings.getConfLocationHtmlResponder());
        setDefaultTemplateName(this.settings.getChatDefaultTemplateName());
        // Scan and register other templates.
        registerTemplates(FileManager.getExistingFile(this.settings.getTemplateDirectory())
                .getAbsolutePath(), FILENAME_FILTER);
        this.templatesDirectoryPath = FileManager.getExistingFile(
                this.settings.getTemplateDirectory()).getAbsolutePath();

        // Read the registration, login, etc. paths.
        this.registrationForm = validTemplatePath(this.settings.getRegisterFormPath());
        this.loginFormPath = validTemplatePath(this.settings.getLoginFormPath());
        this.changePasswordFormPath = validTemplatePath(this.settings.getChangePasswordFormPath());
        this.accessDeniedPagePath = validTemplatePath(this.settings.getAccessDeniedPagePath());
        this.loginSuccessPagePath = validTemplatePath(this.settings.getLoginSuccessPagePath());
        this.loginFailedPagePath = validTemplatePath(this.settings.getLoginFailedPagePath());
        this.logoutSuccessPagePath = validTemplatePath(this.settings.getLogoutSuccessPagePath());
        this.passwordChangeSucceededPagePath = validTemplatePath(this.settings
                .getPasswordChangeSucceededPagePath());
        this.oldPasswordInvalidPagePath = validTemplatePath(this.settings
                .getOldPasswordInvalidPagePath());
        this.passwordFormatInvalidPagePath = validTemplatePath(this.settings
                .getPasswordFormatInvalidPagePath());
        this.passwordNotConfirmedPagePath = validTemplatePath(this.settings
                .getPasswordNotConfirmedPagePath());
        this.registrationSucceededPagePath = validTemplatePath(this.settings
                .getRegistrationSucceededPagePath());
        this.usernameFormatInvalidPagePath = validTemplatePath(this.settings
                .getUsernameFormatInvalidPagePath());
        try
        {
            BufferedReader keyReader = new BufferedReader(new FileReader(FileManager
                    .getFile("secret.key")));
            this.secretKey = keyReader.readLine();

        }
        catch (FileNotFoundException e)
        {
            throw new DeveloperError("Could not find secret.key file!", e);
        }
        catch (IOException e)
        {
            throw new DeveloperError("I/O error reading secret.key file!", e);
        }
    }

    /**
     * Convenience method used by constructor to attempt to turn the path into a
     * valid path that points at a real file.
     * 
     * @param path
     *            the path to use
     * @return the (possibly modified) path after validation
     */
    private String validTemplatePath(String path)
    {
        try
        {
            return FileManager.getExistingFile(this.templatesDirectoryPath + File.separator + path)
                    .getCanonicalPath();
        }
        catch (IOException e)
        {
            throw new UserError("IO error trying to locate \"" + path + "\".", e);
        }
    }

    /**
     * This will return true if <code>request</code>
     * {@link javax.servlet.http.HttpServletRequest HttpServletRequest}, and if
     * it contains a user-agent header with a recognized user agent name.
     * 
     * @see ServletRequestResponderManager#responderHandles(ServletRequest)
     */
    public boolean responderHandles(ServletRequest request)
    {
        if (!(request instanceof HttpServletRequest))
        {
            return false;
        }
        HttpServletRequest servletRequest = (HttpServletRequest) request;
        for (int index = HTML_USER_AGENT_COUNT; --index >= 0;)
        {
            if (servletRequest.getHeader(USER_AGENT).indexOf(HTML_USER_AGENTS[index]) != -1)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * @see ServletRequestResponderManager#getResponseFor(ServletRequestTransactionEnvelope)
     */
    public String getResponseFor(ServletRequestTransactionEnvelope envelope)
    {
        // Always force response content type to be UTF-8.
        envelope.getServiceResponse().setContentType(HTML_CONTENT_TYPE);

        RequiredAction action = determineRequiredAction(envelope);
        switch (action)
        {
            // The user is already logged in, or perhaps authentication is off.
            case proceed:
                return envelope.getCore().getResponse(envelope.getUserRequest(),
                        getCookieValue(envelope.getServiceRequest(), USER_COOKIE_NAME),
                        envelope.getBotID(), new HTMLResponder(this, envelope));

            // The user needs to be authenticated.
            case authenticate:
                AuthenticationResult authResult;
                try
                {
                    authResult = authenticate(envelope);
                }
                catch (DuplicateUserIDError e)
                {
                    return "";
                }

                switch (authResult)
                {
                    // Authentication was successful.
                    case authenticated:
                        return envelope.getCore().getResponse(envelope.getUserRequest(),
                                getCookieValue(envelope.getServiceRequest(), USER_COOKIE_NAME),
                                envelope.getBotID(), new HTMLResponder(this, envelope));

                    // Authentication was unsuccessful.
                    case denied:
                        return FileManager.getFileContents(this.accessDeniedPagePath);

                    // The login form needs to be sent.
                    case loginRequired:
                        return FileManager.getFileContents(this.loginFormPath);

                    // Unknown result -- should not happen!
                    default:
                        throw new DeveloperError("Invalid AuthenticationResult \"" + authResult
                                + "\" returned!", new IllegalArgumentException());
                }

            // The login form needs to be sent.
            case sendLoginForm:
                return FileManager.getFileContents(this.loginFormPath);

            // The login form is being sent and needs to be processed.
            case processLoginForm:
                LoginResult loginResult = processLogin(envelope.getServiceRequest(), envelope
                        .getServiceResponse(), envelope.getBotID());
                switch (loginResult)
                {
                    // Processing of the login form was successful (user was
                    // successfully logged in).
                    case succeeded:
                        return FileManager.getFileContents(this.loginSuccessPagePath);

                    // Processing of the login form failed (user was not
                    // successfully logged in).
                    case failed:
                        return FileManager.getFileContents(this.loginFailedPagePath);

                    // Unknown result -- should not happen!
                    default:
                        throw new DeveloperError("Invalid LoginResult \"" + loginResult
                                + "\" returned!", new IllegalArgumentException());
                }

            // User has requested to log out.
            case logout:
                return processLogout(envelope.getServiceRequest());

            // Change Password form needs to be sent.
            case sendPasswordChangeForm:
                return FileManager.getFileContents(this.changePasswordFormPath);

            // Change Password form is being sent and needs to be processed.
            case processPasswordChangeForm:
                PasswordChangeResult pwChangeResult = processPasswordChange(envelope
                        .getServiceRequest(), envelope.getServiceResponse(), envelope.getBotID());
                switch (pwChangeResult)
                {
                    // The password form was processed successfully (the
                    // password was changed).
                    case succeeded:
                        return FileManager.getFileContents(this.passwordChangeSucceededPagePath);

                    // The old password was invalid.
                    case oldInfoInvalid:
                        return FileManager.getFileContents(this.oldPasswordInvalidPagePath);

                    // The password format was invalid.
                    case passwordFormatInvalid:
                        return FileManager.getFileContents(this.passwordFormatInvalidPagePath);

                    // The new password confirmation did not match the new
                    // password.
                    case passwordNotConfirmed:
                        return FileManager.getFileContents(this.passwordNotConfirmedPagePath);

                    // Unknown result -- should not happen!
                    default:
                        throw new DeveloperError("Invalid PasswordChangeResult \"" + pwChangeResult
                                + "\" returned!", new IllegalArgumentException());
                }

            // The registration form needs to be sent.
            case sendRegistrationForm:
                return FileManager.getFileContents(this.registrationForm);

            // The registration form is being sent and needs to be processed.
            case processRegistrationForm:
                RegistrationResult regResult = processRegistration(envelope.getServiceRequest(),
                        envelope.getServiceResponse(), envelope.getBotID());
                switch (regResult)
                {
                    // The registration form was processed successfully (the
                    // user has been registered).
                    case succeeded:
                        return FileManager.getFileContents(this.registrationSucceededPagePath);

                    // The username format was invalid.
                    case usernameFormatInvalid:
                        return FileManager.getFileContents(this.usernameFormatInvalidPagePath);

                    // The password format was invalid.
                    case passwordFormatInvalid:
                        return FileManager.getFileContents(this.passwordFormatInvalidPagePath);

                    // The given userid already exists.
                    case userAlreadyExists:
                        return FileManager.getFileContents(this.passwordChangeSucceededPagePath);

                    // Unknown result -- should not happen!
                    default:
                        throw new DeveloperError("Invalid RegistrationResult \"" + regResult
                                + "\" returned!", new IllegalArgumentException());
                }

            // Unknown result -- should not happen!
            case unknown:
                throw new DeveloperError(
                        "Could not determine appropriate action for servlet request!",
                        new IllegalArgumentException());

            // Unknown result -- should not happen!
            default:
                throw new DeveloperError("Invalid RequiredAction \"" + action + "\" returned!",
                        new IllegalArgumentException());
        }
    }

    /**
     * The possible required actions based on an initial evaulation of the
     * request.
     */
    public static enum RequiredAction
    {
        /** Proceed with responding. */
        proceed,

        /** Authenticate the user. */
        authenticate,

        /** Send the login form. */
        sendLoginForm,

        /** Process the login form. */
        processLoginForm,

        /** Log the user out. */
        logout,

        /** Send the password change form. */
        sendPasswordChangeForm,

        /** Process the password change form. */
        processPasswordChangeForm,

        /** Send the registration form. */
        sendRegistrationForm,

        /** Process the registration form. */
        processRegistrationForm,

        /** Unknown request. */
        unknown
    }

    /**
     * Determines the appropriate action, based on request parameters. If there
     * is a parameter &quot;request&quot; set to one of the following values,
     * then the corresponding action is indicated. Otherwise, we attempt to
     * authenticate the user.
     * 
     * @param envelope
     *            the envelope that generated the request
     * @return the required action to take
     */
    public RequiredAction determineRequiredAction(ServletRequestTransactionEnvelope envelope)
    {
        // Look for the request parameter.
        String requestParam = envelope.getServiceRequest().getParameter(REQUEST);

        // If requestParam is null, we must try to authenticate with cookies.
        if (requestParam == null)
        {
            return RequiredAction.authenticate;
        }
        // User is requesting login form
        if (requestParam.equals("send-login-form"))
        {
            return RequiredAction.sendLoginForm;
        }
        // User is requesting login
        if (requestParam.equals("process-login-form"))
        {
            return RequiredAction.processLoginForm;
        }
        // User is requesting logout
        if (requestParam.equals("logout"))
        {
            return RequiredAction.logout;
        }
        // User is requesting password change form
        if (requestParam.equals("send-password-change-form"))
        {
            return RequiredAction.sendPasswordChangeForm;
        }
        // User is requesting processing of password change
        if (requestParam.equals("process-password-change"))
        {
            return RequiredAction.processPasswordChangeForm;
        }
        // User is requesting registration form
        if (requestParam.equals("send-registration-form"))
        {
            return RequiredAction.sendRegistrationForm;
        }
        // User is requesting processing of registration form
        if (requestParam.equals("process-registration-form"))
        {
            return RequiredAction.processRegistrationForm;
        }
        return RequiredAction.unknown;
    }

    /** Possible results of attempting to authenticate a request. */
    public static enum AuthenticationResult
    {
        /** Authentication is not needed. */
        notNeeded,

        /** The user is authenticated. */
        authenticated,

        /** Authentication was denied. */
        denied,

        /** The user needs to log in. */
        loginRequired
    }

    /**
     * Attempts to authenticate the request by examining cookies, and looking up
     * the user in the Multiplexor. If the autocookie setting is on, will create
     * cookies if the user is not known.
     * 
     * @param envelope
     *            the envelope that generated the request
     * @return the result of attempting the authentication
     * @throws DuplicateUserIDError
     *             if an attempt was made to automatically create a new userid
     *             but it failed because of an existing duplicate
     */
    public AuthenticationResult authenticate(ServletRequestTransactionEnvelope envelope)
            throws DuplicateUserIDError
    {
        // If authentication is off, send a proceed response immediately.
        if (!this.settings.useUserAuthentication())
        {
            return AuthenticationResult.notNeeded;
        }

        // If the session is marked as logged in, proceed.
        if (envelope.sessionAttributeEquals(LOGGED_IN, true))
        {
            return AuthenticationResult.authenticated;
        }

        HttpServletRequest request = envelope.getServiceRequest();
        HttpServletResponse response = envelope.getServiceResponse();
        String botid = envelope.getBotID();

        Cookie[] cookies = request.getCookies();

        String user = null;
        String password = null;

        // Try to get user and password from cookies.
        if (cookies != null)
        {
            int cookiesLength = cookies.length;

            for (int index = 0; index < cookiesLength; index++)
            {
                if (USER_COOKIE_NAME.equals(cookies[index].getName()))
                {
                    user = cookies[index].getValue();
                }
                if (PASSWORD_COOKIE_NAME.equals(cookies[index].getName()))
                {
                    password = cookies[index].getValue();
                }
            }
        }

        if (user != null && password != null)
        {
            // If user is known by the multiplexor
            if (this.multiplexor.checkUser(user, password, this.secretKey, botid))
            {
                // Set the session as logged in, and return the all-clear
                request.getSession(true).setAttribute(LOGGED_IN, true);
                return AuthenticationResult.authenticated;
            }
            // Otherwise, if user is not okay...
            // If autocookie is on, be forgiving and make a new cookie.
            if (this.settings.autocookie())
            {
                makeNewCookiesAndRegister(response, botid);
                request.getSession(true).setAttribute(LOGGED_IN, true);
                return AuthenticationResult.authenticated;
            }
            // Otherwise, deny request.
            return AuthenticationResult.denied;
        }
        // Otherwise, user had no cookie.
        // Try the autocookie approach.
        if (this.settings.autocookie())
        {
            makeNewCookiesAndRegister(response, botid);
            request.getSession(true).setAttribute(LOGGED_IN, true);
            return AuthenticationResult.authenticated;
        }
        // Otherwise, require login....
        return AuthenticationResult.loginRequired;
    }

    private static enum LoginResult
    {

        /** Login succeeded. */
        succeeded,

        /** Login failed. */
        failed
    }

    private LoginResult processLogin(HttpServletRequest request, HttpServletResponse response,
            String botid)
    {
        String userParam = request.getParameter("user");
        String passwordParam = request.getParameter("password");
        if (this.multiplexor.checkUser(userParam, passwordParam, this.secretKey, botid))
        {
            Cookie ucookie = new Cookie(USER_COOKIE_NAME, userParam);
            Cookie pcookie = new Cookie(PASSWORD_COOKIE_NAME, passwordParam);
            ucookie.setMaxAge(1000000);
            pcookie.setMaxAge(1000000);
            response.addCookie(ucookie);
            response.addCookie(pcookie);
            request.getSession(true).setAttribute(LOGGED_IN, true);
            return LoginResult.succeeded;
        }
        // otherwise...
        return LoginResult.failed;
    }

    private String processLogout(HttpServletRequest request)
    {
        request.getSession(true).setAttribute(LOGGED_IN, false);
        return FileManager.getFileContents(this.logoutSuccessPagePath);
    }

    private static enum PasswordChangeResult
    {
        /** Password change was successful. */
        succeeded,

        /** The old password supplied was incorrect. */
        oldInfoInvalid,

        /** The password format was invalid. */
        passwordFormatInvalid,

        /** The new password and its confirmation did not match. */
        passwordNotConfirmed
    }

    private PasswordChangeResult processPasswordChange(HttpServletRequest request,
            HttpServletResponse response, String botid)
    {
        String userParam = request.getParameter("user");
        String oldPasswordParam = request.getParameter("old-password");
        String newPasswordParam = request.getParameter("new-password");
        String newPasswordConfirmParam = request.getParameter("new-password-confirm");

        // Check that password confirmation matches.
        if (!(newPasswordParam.equals(newPasswordConfirmParam)))
        {
            return PasswordChangeResult.passwordNotConfirmed;
        }

        // Check that new password has valid format.
        if (!newPasswordParam.matches(PASSWORD_FORMAT))
        {
            return PasswordChangeResult.passwordFormatInvalid;
        }

        // Check user/password combo.
        if (this.multiplexor.checkUser(userParam, oldPasswordParam, this.secretKey, botid))
        {
            Cookie pcookie = new Cookie(PASSWORD_COOKIE_NAME, newPasswordParam);
            pcookie.setMaxAge(1000000);
            response.addCookie(pcookie);
            this.multiplexor.changePassword(userParam, newPasswordParam, this.secretKey, botid);
            return PasswordChangeResult.succeeded;
        }
        // otherwise...
        return PasswordChangeResult.oldInfoInvalid;
    }

    private static enum RegistrationResult
    {
        /** Registration was successful. */
        succeeded,

        /** The username format was invalid. */
        usernameFormatInvalid,

        /** The password format was invalid. */
        passwordFormatInvalid,

        /** The password was not confirmed. */
        passwordNotConfirmed,

        /** The given userid already exists. */
        userAlreadyExists
    }

    private RegistrationResult processRegistration(HttpServletRequest request,
            HttpServletResponse response, String botid)
    {
        String userParam = request.getParameter("user");
        String passwordParam = request.getParameter("password");
        String passwordConfirmParam = request.getParameter("password-confirm");

        // Check that username has valid format.
        if (!userParam.matches(USERNAME_FORMAT))
        {
            return RegistrationResult.usernameFormatInvalid;
        }

        // Check that password has valid format.
        if (!passwordParam.matches(PASSWORD_FORMAT))
        {
            return RegistrationResult.passwordFormatInvalid;
        }

        // Check that password confirmation matches.
        if (!(passwordParam.equals(passwordConfirmParam)))
        {
            return RegistrationResult.passwordNotConfirmed;
        }

        // Try to create user.
        try
        {
            this.multiplexor.createUser(userParam, passwordParam, this.secretKey, botid);
        }
        catch (DuplicateUserIDError e)
        {
            return RegistrationResult.userAlreadyExists;
        }
        Cookie ucookie, pcookie;
        ucookie = new Cookie(USER_COOKIE_NAME, userParam);
        pcookie = new Cookie(PASSWORD_COOKIE_NAME, passwordParam);
        ucookie.setMaxAge(1000000);
        pcookie.setMaxAge(1000000);
        response.addCookie(ucookie);
        response.addCookie(pcookie);
        return RegistrationResult.succeeded;
    }

    /**
     * Generates a new username/password cookie pair for a user and registers
     * the user automatically with the multiplexor.
     * 
     * @param servletResponse
     *            the response to which to add the cookies
     * @param botid
     *            the botid for whom to create the user
     * @throws DuplicateUserIDError
     *             if the generated userid already exists (yikes!)
     */
    private void makeNewCookiesAndRegister(HttpServletResponse servletResponse, String botid)
            throws DuplicateUserIDError
    {
        StringBuffer newusername = new StringBuffer(17);
        StringBuffer newpassword = new StringBuffer(10);

        // Generate the new username as "webuser" + the date in milliseconds + a
        // random five digits, password same
        newusername.append("webuser");
        long timeInMillis = System.currentTimeMillis();
        newusername.append(timeInMillis);
        newpassword.append(timeInMillis);

        int digit;

        for (int index = 6; --index > 0;)
        {
            digit = (int) (Math.random() * 5.0);
            newusername.append(digit);
            newpassword.append(digit);
        }

        String user = newusername.toString();
        String password = newpassword.toString();

        // Create the cookie and add it to the response.
        Cookie ucookie, pcookie;
        ucookie = new Cookie(USER_COOKIE_NAME, user);
        pcookie = new Cookie(PASSWORD_COOKIE_NAME, password);
        ucookie.setMaxAge(1000000);
        pcookie.setMaxAge(1000000);
        servletResponse.addCookie(ucookie);
        servletResponse.addCookie(pcookie);

        // Create the new user.
        this.multiplexor.createUser(user, password, this.secretKey, botid);
    }

    private static String getCookieValue(HttpServletRequest request, String cookieName)
    {
        Cookie[] cookies = request.getCookies();
        if (cookies != null)
        {
            int cookiesLength = cookies.length;
            for (int index = 0; index < cookiesLength; index++)
            {
                if (cookieName.equals(cookies[index].getName()))
                {
                    return cookies[index].getValue();
                }
            }
        }
        // if nothing was found...
        return null;
    }

    /**
     * @return the Multiplexor
     */
    public Multiplexor getMultiplexor()
    {
        return this.multiplexor;
    }

    /**
     * @return the secret key
     */
    public String getSecretKey()
    {
        return this.secretKey;
    }

    /**
     * @return the HTMLResponder settings
     */
    public HTMLResponderSettings getSettings()
    {
        return this.settings;
    }
}
