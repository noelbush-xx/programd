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
    More fixes (4.1.3 [02] - November 2001, Noel Bush
    - formatting cleanup
    - made all imports explicit
    - complete replace of most content, now supporting user authentication
    - changed to extend AbstractMarkupResponder, moving most
      methods to there
*/

package org.alicebot.server.core.responder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

import javax.servlet.ServletRequest;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.alicebot.server.core.ActiveMultiplexor;
import org.alicebot.server.core.Globals;
import org.alicebot.server.core.Multiplexor;
import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.util.DeveloperError;
import org.alicebot.server.core.util.SuffixFilenameFilter;
import org.alicebot.server.core.util.Trace;


/**
 *  Responsible for handling requests that
 *  come via HTTP, and delivering the response
 *  via dynamically-generated HTML based on
 *  user-designed templates.
 *
 *  @author  Kim Sullivan
 *  @author  Sergey Zenyuk, Noel Bush, X-31
 */
public class HTMLResponder extends AbstractMarkupResponder
{

    // Class variables.

    /** A filename filter for finding html templates. */
    private static final SuffixFilenameFilter htmlFilenameFilter =
        new SuffixFilenameFilter(new String[] {".html", ".htm", ".data", ".php"});

    /** Map of template names to filenames. */
    private static HashMap templates;

    /** Location of html templates. */
    private static final String templatesDirectoryName =
        Globals.getProperty("programd.responder.html.template.directory", "templates" + File.separator + "html");

    /** Path to the default chat template. */
    private static String chatTemplatePath =
        templatesDirectoryName + File.separator + Globals.getProperty("programd.responder.html.template.chat-default", "chat.html");

    /** Path to the register template. */
    private static final String registerTemplatePath =
        templatesDirectoryName + File.separator + Globals.getProperty("programd.responder.html.template.register", "register.html");

    /** Path to the login template. */
    private static final String loginTemplatePath =
        templatesDirectoryName + File.separator + Globals.getProperty("programd.responder.html.template.login", "login.html");

    /** Path to the change password template. */
    private static final String changePasswordTemplatePath =
        templatesDirectoryName + File.separator + Globals.getProperty("programd.responder.html.template.change-password", "change-password.html");

    /** Whether to require login (regardless of cookie presence or not). */
    private static boolean requirelogin =
        Boolean.valueOf(Globals.getProperty("programd.httpserver.requirelogin", "false")).booleanValue();

    /** Whether to automatically generate a cookie for each unique visitor. */
    private static boolean autocookie =
        Boolean.valueOf(Globals.getProperty("programd.httpserver.autocookie", "true")).booleanValue();

    /** The registration template. */
    private static LinkedList registerTemplate;

    /** The login template. */
    private static LinkedList loginTemplate;

    /** The change password template. */
    private static LinkedList changePasswordTemplate;
    
    /** Message when a userid/password combination is invalid. */
    private static final String badUserIDOrPasswordMessage      = "<p>Invalid user id or password.</p>";

    /** Message when two passwords don't match. */
    private static final String passwordMismatchMessage         = "<p>Password mismatch.</p>";

    /** Message when a userid already exists. */
    private static final String userAlreadyExistsMessage        = "<p>User id already exists.</p>";

    /** Message when successfully registered. */
    private static final String successfulRegistrationMessage   = "<p>Successfully registered.</p>";

    /** Message when password successfully changed. */
    private static final String successfulPasswordChangeMessage = "<p>Successfully changed password.</p>";

    /** Message when developer has made an error. */
    private static final String developerErrorMessage           = "<p>Developer error.  Please login.</p>";

    /** The ActiveMultiplexor secret key (loaded at runtime). */
    private static String SECRET_KEY;


    // Flags for authentication

    // Required state flags

    /** State flag: Action request that must be qualified. */
    private static final int QUALIFY                =    1;

    /** State flag: Action request that has been qualified. */
    private static final int DO                     =    2;

    /** State flag: Request to send a form. */
    private static final int SEND_FORM              =    4;

    // Request type flags

    /** Request type flag: Concerning user authentication. */
    private static final int LOGIN                  =    8;

    /** Request type flag: Concerning new user registration. */
    private static final int REGISTER               =    16;

    /** Request type flag: Concerning password change. */
    private static final int CHANGE_PASSWORD        =    32;

    // Result flags

    /** Result flag: User has complete &quot;go-ahead&quot;. */
    private static final int GO_USER                =    128;

    /** Result flag: Result of some process was good. */
    private static final int PROCESS_OK             =    256;

    /** Result flag: Current password as provided is bad. */
    private static final int BAD_PASSWORD           =    512;

    /** Result flag: New password and repeat do not match. */
    private static final int PASSWORD_MISMATCH      =    1024;


    // Convenience constants.
    
    /** The user id cookie name. */
    public static final String USER_COOKIE_NAME     = "alicebot_user";

    /** The password cookie name. */
    public static final String PASSWORD_COOKIE_NAME = "alicebot_password";


    // Instance variables.

    /** The user name (if authentication is being used). */
    private static String user;

    /** The password (if authentication is being used). */
    private static String password;


    public HTMLResponder(String botid, String templateName) throws IOException
    {
        super(botid);
        if (templateName.equals(EMPTY_STRING))
        {
            parseTemplate(chatTemplatePath);
        }
        else
        {
            // Otherwise, try to find the named template.
            String templateFileName = (String)templates.get(templateName);
            if (templateFileName != null)
            {
                parseTemplate(templateFileName);
            }
            else
            {
                parseTemplate(chatTemplatePath);
            }
        }
    }


    /**
     *  Loads the registration, login and change password templates.
     *  Also scans the html templates path for any files ending in &quot;.html&quot;,
     *  &quot;*.htm&quot;, or &quot;*.data&quot;
     *  and adds their names (without the suffix) to the list of available templates.
     */
    static 
    {
        // Load the register, login and change password templates.
        registerTemplate = loadTemplate(registerTemplatePath);
        loginTemplate = loadTemplate(loginTemplatePath);
        changePasswordTemplate = loadTemplate(changePasswordTemplatePath);

        // Get the secret key, if available.
        if (SECRET_KEY == null)
        {
            try
            {
                BufferedReader keyReader = new BufferedReader(new FileReader("secret.key"));
                SECRET_KEY = keyReader.readLine();

            }
            catch (FileNotFoundException e)
            {
                Trace.userinfo("Could not find secret.key file!");
                SECRET_KEY = EMPTY_STRING;
            }
            catch (IOException e)
            {
                Trace.userinfo("I/O error reading secret.key file!");
                SECRET_KEY = EMPTY_STRING;
            }
        }

        // Scan and register other templates.
        templates = registerTemplates(templatesDirectoryName, htmlFilenameFilter);
    }


    
    /**
     *    loginRequest with no parameters means unknown username, password
     */
    public static String loginRequest()
    {
        return (loginRequest("", "")); 
    }


    /**
     *  Processes a login request
     *
     *  @param user     the username
     *  @param password the password
     */
    public static String loginRequest(String user, String password)
    {
        if (loginTemplate != null)
        {
            StringBuffer output = new StringBuffer();
            ListIterator li = loginTemplate.listIterator(0);
            while(li.hasNext())
            {
                String item = (String)li.next();
                StringBuffer sb = new StringBuffer(item);
                int index=0;
                if ((index = item.indexOf("name=\"user\"")) != -1)
                {
                    if ((index = item.indexOf("value=\"\"")) != -1)
                    {
                        sb.replace(index+6, index+7, "\"" + user + "\"");
                    }
                }
                if ((index=item.indexOf("name=\"password\"")) != -1)
                {
                    if ((index = item.indexOf("value=\"\"")) != -1)
                    {
                        sb.replace(index + 6, index+7, "\"" + password + "\"");
                    }
                }
                output.append(sb.toString());
            }
            return output.toString();
        }
        return EMPTY_STRING;
    }

    
    /**
     *  Processes a registration request
     */
    public static String registerRequest()
    {
        StringBuffer output = new StringBuffer("");
        if (registerTemplate != null)
        {
            ListIterator li = registerTemplate.listIterator(0);
            while(li.hasNext())
            {
                output.append((String)li.next());
            }
            return output.toString();
        }
        return EMPTY_STRING;
    }


    /**
     *  Processes a change password request.
     */
    public static String changePasswordRequest()
    {
        if (changePasswordTemplate != null)
        {
            StringBuffer output = new StringBuffer("");
            ListIterator li = changePasswordTemplate.listIterator(0);
            while(li.hasNext())
            {
                output.append((String)li.next());
            }
            return output.toString();
        }
        return EMPTY_STRING;
    }


    /**
     *    <p>
     *        Tries to authenticate a user.
     *        Different things can happen, depending on parameters in the request.
     *        Here are the meanings of the parameter values:
     *    </p>
     *    <ul>
     *        <li>
     *            login - if yes, return a login page
     *        </li>
     *        <li>
     *            checkauth - if auth, try to authenticate a username/password combination
     *        </li>
     *        <li>
     *            register - if auth, try to register a username/password combination
     *        </li>
     *        <li>
     *            register - if yes, return a registration page
     *        </li>
     *        <li>
     *            change-password - if yes, try to change a password
     *        </li>
     *    </ul>
     *    <p>
     *        Most important goal is to get out of here as quickly as possible.  This relies
     *        on the DBMultiplexor having a quick method to check a user -- ideally not going to
     *        the database each time.  Also relies on creating as few objects as possible, even
     *        avoiding Strings if possible, but never checking the same parameter more than once.
     *        Of course, the order is the most important thing.  We have to first see if there are
     *        any of the relevant parameters supplied, then check a couple of server properties,
     *        to know what we have to do.  It's okay if the registration and login steps are a little
     *        slower, but not okay if each reply is slowed down in a conversation.  That's what we
     *        tried hard to avoid here.
     *    </p>
     *
     *  @param request      the HttpServletRequest
     *  @param response     the HttpServletResponse
     *  @param userid       the userid given with the request (will not be the same as final userid)
     */
    public String authenticate(HttpServletRequest request, HttpServletResponse response, String userid)
    {
        int state = 0;

        // The cookie array
        Cookie[] cookies = request.getCookies();

        // The http session
        HttpSession session;

        // Assume no cookie is set.
        boolean userCookieSet = false;
        
        // Get the http session.
        session = request.getSession(true);        

        // Try to get user and password from cookies.
        int cookiesLength = cookies.length;

        for(int index = 0; index < cookiesLength; index++)
        {
            if(cookies[index].getName().equals(USER_COOKIE_NAME))
            {
                this.user = cookies[index].getValue();
                userCookieSet = true;        
            }
            if(cookies[index].getName().equals(PASSWORD_COOKIE_NAME)) {
                this.password = cookies[index].getValue();
            }
        }

        // Get parameters (so we don't do this repeatedly).  Don't care yet if they exist or not.
        String userParam            =    request.getParameter("user");
        String passwordParam        =    request.getParameter("password");
        String oldPasswordParam     =    request.getParameter("oldPassword");
        String repeatPasswordParam  =    request.getParameter("password1");

        // Get the request type (or more if obvious)

        // User is requesting login form
        if (parameterEquals(request, "login", "yes"))
        {
            state = state | LOGIN;
            state = state | SEND_FORM;
        }
        // User request concerns password change (need to qualify)
        else if (parameterEquals(request, "change-password", "yes"))
        {
            state = state | CHANGE_PASSWORD;
            state = state | QUALIFY;
        }
        // User is requesting authentication (need to qualify)
        else if (parameterEquals(request, "checkauth", "auth"))
        {
            state = state | LOGIN;
            state = state | QUALIFY;
        }
        // User is requesting registration form
        else if (parameterEquals(request, "register", "yes"))
        {
            state = state | REGISTER;
            state = state | SEND_FORM;
        }
        // User is requesting registration (need to qualify)
        else if (parameterEquals(request, "register", "auth"))
        {
            state = state | REGISTER;
            state = state | QUALIFY;
        }
        // Otherwise user is requesting nothing -- let's get out fast!
        // If the cookie is set (meaning we have user and password values to test)
        else if (userCookieSet)
        {
            // If user is known by ActiveMultiplexor
            if (ActiveMultiplexor.getInstance().checkUser(this.user, this.password, SECRET_KEY, botid))
            {
                // Return the all-clear
                state = state | GO_USER;
            }
            // If user is not okay
            else
            {
                // If autocookie is on, be forgiving and make a new cookie.
                if (makeNewCookies(response))
                {
                    Trace.devinfo("Found invalid cookie but created new one because autocookie is on.");
                    state = state | GO_USER;
                }
                else
                {
                    Log.userinfo("Server error: Could not create new user using autocookie.", Log.ERROR);
                    return loginRequest();
                }
            }
        }
        // User requested nothing but had no cookie.
        else
        {
            // Try for the fantabulous autocookie approach.
            if (autocookie)
            {
                if (makeNewCookies(response))
                {
                    state = state | GO_USER;
                }
                else
                {
                    Log.userinfo("Server error: Could not create new user using autocookie.", Log.ERROR);
                    return loginRequest();
                }
            }
            // So sad, no auto-cookie....
            else
            {
                return loginRequest();
            }
        }
        
        // Situations that need to be qualified
        if ((state & QUALIFY) == QUALIFY)
        {
            // All such situations which require that user is not null:
            if (userParam != null)
            {
                // All such situations which require that password is not null:
                if (passwordParam != null)
                {
                    // Enough material to try login
                    if ((state & LOGIN) == LOGIN)
                    {
                        state = state | DO;
                    }
                    // All such situations which require the repeatPassword param
                    else if (repeatPasswordParam != null)
                    {
                        // Enough material to try either a register or a change password request
                        if ((state & REGISTER) == REGISTER)
                        {
                            // PROCESS_OK if password and repeatPassword params match
                            if (passwordParam.equals(repeatPasswordParam))
                            {
                                state = state | DO;
                            }
                            // Not otherwise
                            else
                            {
                                return badUserIDOrPasswordMessage + registerRequest();
                            }
                        }
                        else if ((state & CHANGE_PASSWORD) == CHANGE_PASSWORD)
                        {
                            if (passwordParam.equals(oldPasswordParam))
                            {
                                state = state | DO;
                            }
                            else
                            {
                                return passwordMismatchMessage + changePasswordRequest();
                            }
                        }
                    }
                    // Invalid situation means send form
                    else
                    {
                        state = state | SEND_FORM;
                    }
                }
                // Empty password param means send form
                else
                {
                    state = state | SEND_FORM;
                }
            }
            // Empty user param means send form
            else
            {
                state = state | SEND_FORM;
            }
        }

        // DO flags
        if ((state & DO) == DO)
        {
            // Try login
            if((state & LOGIN) == LOGIN)
            {
                // Check user/password combo
                if (ActiveMultiplexor.getInstance().checkUser(userParam, passwordParam, SECRET_KEY, botid))
                {
                    Cookie ucookie = new Cookie(USER_COOKIE_NAME, userParam);
                    Cookie pcookie = new Cookie(PASSWORD_COOKIE_NAME, passwordParam);
                    ucookie.setMaxAge(1000000);
                    pcookie.setMaxAge(1000000);
                    response.addCookie(ucookie);
                    response.addCookie(pcookie);

                    session.setAttribute(USER_COOKIE_NAME, this.user);
                    state = state | PROCESS_OK;
                }
                else
                {
                    return badUserIDOrPasswordMessage + loginRequest();
                }
            }
            // Try change password
            else if ((state & CHANGE_PASSWORD) == CHANGE_PASSWORD)
            {
                // Check user/password combo
                if (ActiveMultiplexor.getInstance().checkUser(this.user, oldPasswordParam, SECRET_KEY, botid))
                {
                    Cookie pcookie = new Cookie(PASSWORD_COOKIE_NAME, passwordParam);
                    pcookie.setMaxAge(1000000);
                    response.addCookie(pcookie);
                    state = state | PROCESS_OK;
                }
                else
                {
                    return badUserIDOrPasswordMessage + loginRequest();
                }
            }
            // Try registration
            else if ((state & REGISTER) == REGISTER)
            {
                if(ActiveMultiplexor.getInstance().createUser(userParam, passwordParam, SECRET_KEY, botid))
                {
                    // sessionID = session.getId();
                    state = state | PROCESS_OK;
                }
                else
                {
                    return userAlreadyExistsMessage + registerRequest();
                }
            }
        }
        // SEND_FORM flags
        else if ((state & SEND_FORM) == SEND_FORM)
        {
            if ((state & LOGIN) == LOGIN)
            {
                return loginRequest();
            }
            else if ((state & REGISTER) == REGISTER)
            {
                return registerRequest();
            }
            else if ((state & CHANGE_PASSWORD) == CHANGE_PASSWORD)
            {
                return changePasswordRequest();
            }
        }

        // If user has all clear, set the alicebot_user session attribute and return null.
        if ((state & GO_USER) == GO_USER)
        {
            session.setAttribute(USER_COOKIE_NAME, this.user);
            return null;
        }
        // If a process was successful (should have exited by now otherwise)
        else if((state & PROCESS_OK) == PROCESS_OK)
        {
            // Successful login?  Set alicebot_user session attribute and return null.
            if ((state & LOGIN) == LOGIN)
            {
                session.setAttribute(USER_COOKIE_NAME, this.user);
                return null;
            }
            // Successful registration?  Return login form with success message (lets the user see that registration was successful).
            else if ((state & REGISTER) == REGISTER)
            {
                return successfulRegistrationMessage + loginRequest(userParam, passwordParam);
            }
            // Successful password change?  Return login form with success message (lets the user see that password change was successful).
            else if ((state & CHANGE_PASSWORD) == REGISTER)
            {
                return successfulPasswordChangeMessage + loginRequest(userParam, passwordParam);
            }
            // Something strange.  Unclear state.  Notify the console; send a blank login form with a developer error message.
            else
            {
                return developerErrorMessage + loginRequest();
            }
        }
        // No GO_USER, no PROCESS_OK -- some kind of developer error.
        else
        {
            return developerErrorMessage + loginRequest();
        }
    }


    /**
     *  Convenience method; checks that a parameter
     *  value is not null before doing equals() comparison.
     */
    private static boolean parameterEquals(HttpServletRequest request, String parameterName, String comparisonValue)
    {
        String parameterValue = request.getParameter(parameterName);
        if (parameterValue != null)
        {
            if (parameterValue.equals(comparisonValue))
            {
                return true;
            }
            else
            {
                return false;
            }
        }
        return false;
    }


    /**
     *  Generates a new username/password cookie pair for a user.
     *
     *  @param response the response to which to add the cookies
     */
    private boolean makeNewCookies(HttpServletResponse response)
    {
        StringBuffer newusername = new StringBuffer(17);
        StringBuffer newpassword = new StringBuffer(10);

        // Generate the new username as "webuser" + the date in milliseconds + a random five digits, password same
        newusername.append("webuser");
        long timeInMillis = System.currentTimeMillis();
        newusername.append(timeInMillis);
        newpassword.append(timeInMillis);

        int digit;

        for (int index = 6; --index > 0; )
        {
            digit = (int)(Math.random() * (double)5.0);
            newusername.append(digit);
            newpassword.append(digit);
        }

        this.user = newusername.toString();
        this.password = newpassword.toString();

        // Create the cookie and add it to the response.
        Cookie ucookie, pcookie;
        ucookie = new Cookie(USER_COOKIE_NAME, this.user);
        pcookie = new Cookie(PASSWORD_COOKIE_NAME, this.password);
        ucookie.setMaxAge(1000000);
        pcookie.setMaxAge(1000000);
        response.addCookie(ucookie);
        response.addCookie(pcookie);

        // Create the new user (and ensure that it worked).
        return ActiveMultiplexor.getInstance().createUser(this.user, this.password, SECRET_KEY, botid);
    }
}
