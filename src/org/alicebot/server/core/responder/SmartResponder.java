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
    More fixes (4.1.3 [02] - November 2001, Noel Bush)
    - formatting cleanup
    - made all imports explicit
    - changed "virtual_ip" to "userid"
    - removed unused service type checks and constants
    - added support of HTMLResponder authentication
      (should make this part of the Responder interface, maybe)
*/

package org.alicebot.server.core.responder;

import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.alicebot.server.core.Bots;
import org.alicebot.server.core.Globals;
import org.alicebot.server.core.Graphmaster;
import org.alicebot.server.core.Multiplexor;
import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.util.DeveloperError;
import org.alicebot.server.core.util.Trace;
import org.alicebot.server.core.util.UserError;


/**
 *  <p>
 *  The <code>SmartResponder</code> is a broker for requests coming into the server.
 *  Its goal is to accept and process a response and format it for the appropriate
 *  device, formatting the output as needed through other Responders.
 *  What makes it &quot;smart&quot; is its ability to parse the User-Agent and forward
 *  the request to the appropriate Responder.
 *  </p>
 *  <p>
 *  In general a Responder must have:
 *  </p>
 *  <ol>
 *  <li>
 *      A method of output (text output, speech, XML)
 *  </li>
 *  <li>
 *      A method of input (text input, speech, XML)
 *  </li>
 *  <p>
 *  The main method of this class is {@link #doResponse},
 *  which will redirects the HttpRequest as needed.
 *  </p>
 *
 *  @author Jon Baer
 *  @author Thomas Ringate/Pedro Colla
 */
public class SmartResponder
{
    // Instance variables.

    /** The service agent. */
    private String serviceAgent;
    
    /** The service response. */
    private HttpServletResponse serviceResponse;
    
    /** The service request. */
    private HttpServletRequest serviceRequest;
    
    /** The responder. */
    private Responder responder;
    
    /** The bot response. */
    private String botResponse;
    
    /** The user request. */
    private String userRequest;

    /** The user id. */
    private String userid;

    /** The bot id. */
    private String botid;

    /** The template name. */
    private String templateName;
    
    /** The service type. */
    private int serviceType;
    
    /** The output stream for writing the response. */
    private ServletOutputStream serviceOutputStream;


    // Convenience constants.

    /** An empty string. */
    private static final String EMPTY_STRING          = "";

    /** The connect string. */
    private static final String CONNECT               = Globals.getProperty("programd.connect-string", "CONNECT");

    /** The inactivity string. */
    private static final String INACTIVITY            = Globals.getProperty("programd.inactivity-string", "INACTIVITY");

    /** The name of the text parameter in a request (&quot;text&quot;). */
    private static final String TEXT_PARAM            = "text";

    /** The name of the userid parameter in a request (&quot;userid&quot;). */
    private static final String USERID_PARAM          = "userid";

    /** The name of the botid parameter in a request (&quot;botid&quot;). */
    private static final String BOTID_PARAM          = "botid";

    /** The string &quot;text/plain&quot. */
    private static final String TEXT_PLAIN            = "text/plain";

    /** The string &quot;text/html; charset=utf8&quot; (for sending html). */
    private static final String HTML_CONTENT_TYPE     = "text/html; charset=UTF-8";

    /** The string &quot;8859_1&quot; (for character encoding conversion). */
    private static final String ENC_8859_1            = "8859_1";

    /** The string &quot;utf-8&quot; (for character encoding conversion). */
    private static final String ENC_UTF8              = "utf-8";

    /** The string &quot;USER_AGENT&quot;. */
    private static final String USER_AGENT            = "USER-AGENT";

    /** The string &quot;USER_AGENT&quot;. */
    private static final String PLAIN_TEXT            = "plain_text";

    /** The string &quot;USER_AGENT&quot;. */
    private static final String FLASH                 = "flash";

    /** The string &quot;template&quot;. */
    private static final String TEMPLATE              = "template";

    /** Known user agent strings for browsers. */
    private static final String[] HTML_USER_AGENTS    = new String[] { "Mozilla", "MSIE", "Lynx", "Opera" };

    /** Number of known user agents. */
    private static final int HTML_USER_AGENT_COUNT    = HTML_USER_AGENTS.length;

    
    /**
     *  Constructs a new <code>SmartResponder</code>
     *  from a given servlet request, and given a
     *  servlet response object to send back.
     *
     *  @param request  the servlet request
     *  @param response the response to modify and send back
     */
    public SmartResponder(HttpServletRequest request, HttpServletResponse response)
    {
        this.serviceRequest = request;
        this.serviceResponse = response;

        this.userRequest = request.getParameter(TEXT_PARAM);
        this.userid = request.getParameter(USERID_PARAM);
        this.botid = request.getParameter(BOTID_PARAM);

        // If no text parameter then we assume a connection.
        if (this.userRequest == null)
        {
            this.userRequest = CONNECT;
        }
        // Check for blank request.
        else if (this.userRequest.equals(EMPTY_STRING))
        {
            this.userRequest = INACTIVITY;
        }
        // Convert to UTF-8.
        else
        {
            try
            {
                this.userRequest = new String(this.userRequest.getBytes(ENC_8859_1), ENC_UTF8);
            }
            catch (UnsupportedEncodingException e)
            {
                throw new DeveloperError("Encodings are not properly supported!");
            }
        }


        // Check for no userid.
        if (this.userid == null)
        {
            this.userid = request.getRemoteHost();
        }

        // Check for no bot id.
        if (this.botid == null)
        {
            this.botid = Bots.getFirstBot().getID();
        }

        // Look for a named template.
        this.templateName = request.getParameter(TEMPLATE);
        if (this.templateName == null)
        {
            this.templateName = EMPTY_STRING;
        }
    }
    
    /** 
     * Invokes a response.
     */
    public void doResponse()
    {
        try
        {
            this.serviceOutputStream = this.serviceResponse.getOutputStream();
        }
        catch (IOException e)
        {
            throw new DeveloperError("Error getting service response output stream.", e);
        }
            
        switch (getServiceType())
        {
            case ServiceType.PLAIN_TEXT :
                this.serviceResponse.setContentType(TEXT_PLAIN);
                this.botResponse =
                    Multiplexor.getResponse(this.userRequest, this.userid, this.botid, new TextResponder());
                break;
                
            case ServiceType.HTML :
                // Always force response content type to be UTF-8.
                this.serviceResponse.setContentType(HTML_CONTENT_TYPE);

                HTMLResponder responder;
                try
                {
                    responder = new HTMLResponder(this.botid, this.templateName);
                }
                catch (IOException e)
                {
                    throw new DeveloperError("Error initializing HTMLResponder.");
                }

                this.botResponse = responder.authenticate(this.serviceRequest, this.serviceResponse, this.userid);
                // Null response from authenticate means user is authenticated.
                if (this.botResponse == null)
                {
                    this.botResponse =
                            Multiplexor.
                                getResponse(this.userRequest,
                                            (String)this.serviceRequest.getSession(true).
                                            getAttribute(HTMLResponder.USER_COOKIE_NAME),
                                            this.botid,
                                            responder);
                }
                break;
                
            case ServiceType.FLASH :
                this.serviceResponse.setContentType(TEXT_PLAIN);
                this.botResponse =
                        Multiplexor.
                            getResponse(this.userRequest, this.userid, this.botid,
                                        new FlashResponder(this.botid, this.templateName));
                break;

            default :
                this.serviceResponse.setContentType(TEXT_PLAIN);
                this.botResponse =
                    Multiplexor.
                         getResponse(this.userRequest, this.userid, this.botid,
                                        new TextResponder());
                break;
        }
        try
        {
            this.serviceOutputStream.write(this.botResponse.getBytes(ENC_UTF8));
        }
        catch (UnsupportedEncodingException e0)
        {
            throw new UserError("UTF-8 encoding is not supported on your platform!", e0);
        }
        catch (IOException e1)
        {
            throw new DeveloperError("Error writing to service output stream.", e1);
        }
        try
        {
            this.serviceOutputStream.flush();
        }
        catch (IOException e)
        {
            throw new DeveloperError("Error flushing service output stream.", e);
        }
        try
        {
            this.serviceOutputStream.close();
        }
        catch (IOException e)
        {
            throw new DeveloperError("Error closing service output stream.", e);
        }
    }
    
    public int getServiceType()
    {
        if (this.serviceRequest.getParameter(PLAIN_TEXT) != null)
        {
            return ServiceType.PLAIN_TEXT;
        }
        if (this.serviceRequest.getParameter(FLASH) != null)
        {
            return ServiceType.FLASH;
        }
        for (int index = HTML_USER_AGENT_COUNT; --index >= 0; )
        {
            if (this.serviceRequest.getHeader(USER_AGENT).indexOf(HTML_USER_AGENTS[index]) != -1)
            {
                return ServiceType.HTML;
            }
        }
        return ServiceType.UNKNOWN;
    }
    
} 

class ServiceType
{
    public static final int UNKNOWN    = 0;
    public static final int PLAIN_TEXT = 1;
    public static final int HTML       = 2;
    public static final int FLASH      = 3;
}
