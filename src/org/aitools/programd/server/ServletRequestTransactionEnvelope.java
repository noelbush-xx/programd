/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.server;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.aitools.programd.Core;
import org.aitools.programd.responder.NoResponderHandlesThisException;
import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.UserError;

/**
 * <p>
 * The <code>ServletRequestTransactionEnvelope</code> is a broker for requests
 * coming into the server. Its goal is to accept and process a response and
 * format it for the appropriate device, formatting the output as needed through
 * the appropriate Responder.
 * </p>
 * <p>
 * In general a Responder must have:
 * </p>
 * <ol>
 * <li>A method of output (text output, speech, XML)</li>
 * <li>A method of input (text input, speech, XML)</li>
 * <p>
 * The main method of this class is {@link #process} , which will process the
 * HttpRequest as needed.
 * </p>
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @author Jon Baer
 * @author Thomas Ringate/Pedro Colla
 * @version 4.5
 */
public class ServletRequestTransactionEnvelope
{
    // Instance variables.

    protected Core core;

    protected ServletRequestResponderManagerRegistry managerRegistry;

    /** The service response. */
    private HttpServletResponse serviceResponse;

    /** The service request. */
    private HttpServletRequest serviceRequest;

    /** The bot response. */
    private String botResponse;

    /** The encoding to be used for the bot response. */
    private String responseEncoding;

    /** The user request. */
    private String userRequest;

    /** The user id. */
    private String userid;

    /** The bot id. */
    private String botid;

    /** The template name. */
    private String templateName;

    /** The output stream for writing the response. */
    private ServletOutputStream serviceOutputStream;

    // Convenience constants.

    /** An empty string. */
    private static final String EMPTY_STRING = "";

    /** The connect string. */
    private static String connectString;

    /** The inactivity string. */
    private static String inactivityString;

    /** The name of the text parameter in a request (&quot;text&quot;). */
    private static final String TEXT_PARAM = "text";

    /** The name of the userid parameter in a request (&quot;userid&quot;). */
    private static final String USERID_PARAM = "userid";

    /** The name of the botid parameter in a request (&quot;botid&quot;). */
    private static final String BOTID_PARAM = "botid";

    /**
     * The name of the response encoding parameter in a request
     * (&quot;response_encoding&quot;).
     */
    private static final String RESPONSE_ENCODING_PARAM = "response_encoding";

    /** The string &quot;{@value}&quot; (for character encoding conversion). */
    private static final String ENC_8859_1 = "8859_1";

    /** The string &quot;{@value}&quot; (for character encoding conversion). */
    private static final String ENC_UTF8 = "utf-8";

    /** The string &quot;{@value}&quot;. */
    private static final String TEMPLATE = "template";
    
    /**
     * Constructs a new <code>ServletRequestTransactionEnvelope</code> from a
     * given servlet request, and given a servlet response object to send back.
     * 
     * @param request the servlet request
     * @param response the response to modify
     * @param coreToUse the core to use
     * @param managerRegistryToUse the ServletRequestResponderManagerRegistry in
     *            which to find responders
     */
    public ServletRequestTransactionEnvelope(HttpServletRequest request, HttpServletResponse response, Core coreToUse,
            ServletRequestResponderManagerRegistry managerRegistryToUse)
    {
        this.core = coreToUse;
        checkStaticVariables();
        this.serviceRequest = request;
        this.serviceResponse = response;
        this.managerRegistry = managerRegistryToUse;
    }

    private void checkStaticVariables()
    {
        if (connectString == null)
        {
            connectString = this.core.getSettings().getConnectString();
        }
        if (inactivityString == null)
        {
            inactivityString = this.core.getSettings().getInactivityString();
        }
    }

    /**
     * Invokes a response.
     * 
     * @throws NoResponderHandlesThisException if no responder can be found to
     *             handle the request
     */
    public void process() throws NoResponderHandlesThisException
    {
        this.userRequest = this.serviceRequest.getParameter(TEXT_PARAM);
        this.userid = this.serviceRequest.getParameter(USERID_PARAM);
        this.botid = this.serviceRequest.getParameter(BOTID_PARAM);
        this.responseEncoding = this.serviceRequest.getParameter(RESPONSE_ENCODING_PARAM);

        // If no text parameter then we assume a new connection.
        if (this.userRequest == null)
        {
            this.userRequest = connectString;
        }
        // Check for blank request.
        else if (this.userRequest.equals(EMPTY_STRING))
        {
            this.userRequest = inactivityString;
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
                throw new DeveloperError("Encodings are not properly supported!", e);
            }
        }

        // If no response encoding specified, use UTF-8.
        if (this.responseEncoding == null)
        {
            this.responseEncoding = ENC_UTF8;
        }

        // Check for no userid.
        if (this.userid == null)
        {
            this.userid = this.serviceRequest.getRemoteHost();
        }

        // Check for no bot id.
        if (this.botid == null)
        {
            this.botid = this.core.getBots().getABot().getID();
        }

        // Look for a named template.
        this.templateName = this.serviceRequest.getParameter(TEMPLATE);
        if (this.templateName == null)
        {
            this.templateName = EMPTY_STRING;
        }

        try
        {
            this.serviceOutputStream = this.serviceResponse.getOutputStream();
        }
        catch (IOException e)
        {
            throw new DeveloperError("Error getting service response output stream.", e);
        }

        this.botResponse = this.managerRegistry.getHandlerFor(this.serviceRequest).getResponseFor(this);

        try
        {
            this.serviceOutputStream.write(this.botResponse.getBytes(this.responseEncoding));
        }
        catch (UnsupportedEncodingException e)
        {
            throw new UserError("UTF-8 encoding is not supported on your platform!", e);
        }
        catch (IOException e)
        {
            throw new DeveloperError("Error writing to service output stream.", e);
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

    /**
     * @return the servlet request object
     */
    public HttpServletRequest getServiceRequest()
    {
        return this.serviceRequest;
    }

    /**
     * @return the servlet response object
     */
    public HttpServletResponse getServiceResponse()
    {
        return this.serviceResponse;
    }

    /**
     * @return the Core object
     */
    public Core getCore()
    {
        return this.core;
    }

    /**
     * @return the user request
     */
    public String getUserRequest()
    {
        return this.userRequest;
    }

    /**
     * @return the userid
     */
    public String getUserID()
    {
        return this.userid;
    }

    /**
     * @return the botid
     */
    public String getBotID()
    {
        return this.botid;
    }

    /**
     * @return the template name
     */
    public String getTemplateName()
    {
        return this.templateName;
    }

    /**
     * @param name the attribute name to look for
     * @param value the value to look for
     * @return whether the given request has a session with an attribute with the given name and value
     */
    public boolean sessionAttributeEquals(String name, Object value)
    {
        HttpSession session = this.serviceRequest.getSession(true);
        Object attributeValue = session.getAttribute(name);
        if (attributeValue == null)
        {
            return false;
        }
        // otherwise...
        return (attributeValue.equals(value));
    }
}