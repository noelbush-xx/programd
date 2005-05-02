/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.listener;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Socket;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.logging.Level;

import org.aitools.programd.Core;
import org.aitools.programd.bot.Bot;
import org.aitools.programd.responder.AIMResponder;
import org.aitools.programd.util.XMLKit;

/**
 * This code is from the Everybuddy Java Project by Chris Carlin
 * (http://EBJava.sourceforge.net/) modified to work with a Program D server.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @author Jon Baer
 * @author Sandy McArthur
 * @version 4.5
 */
public class AIMListener extends Listener
{
    // Instance variables.

    /** The Responder to use. */
    private AIMResponder responder;

    /** The screenname to use. */
    private String screenname;

    /** The password to use. */
    private String password;

    /** The background color for messages. */
    private String bgcolor;

    /** The font face for messages. */
    private String fontface;

    /** The font size for messages. */
    private String fontsize;

    /** The font color for messages. */
    private String fontcolor;

    /** The owner name. */
    private String owner;

    /** The list of buddies. */
    private String buddies;

    /** The sequence number used in constructing messages. */
    private int seqNo;

    /** The connection object to use. */
    private Socket connection;

    /** The data input stream for the connection. */
    private DataInputStream in;

    /** The data output stream for the connection. */
    private DataOutputStream out;

    /** Whether or not we are online. */
    protected boolean online;

    // Convenience constants.

    /** The maximum sequence number. */
    private static final int MAX_SEQ = 65535;

    /** The hostname to which to connect. */
    private static final String HOST = "toc.oscar.aol.com";

    /** The port to which to connect. */
    private static final int PORT = 21;

    /** The string &quot;{@value}&quot;. */
    private static final String ROAST = "Tic/Toc";

    /** The string &quot;{@value}&quot;. */
    private static final String ZERO = "0";

    /** The string &quot;{@value}&quot;. */
    private static final String ZERO_X = "0x";

    /** The string &quot;{@value}&quot;. */
    private static final String ONE = "1";

    /** The string &quot;{@value}&quot;. */
    private static final String TWO = "2";

    /** The string &quot;{@value}&quot;. */
    private static final String TWO_POINT_FIVE = "2.5";

    /** The string &quot;{@value}&quot;. */
    private static final String THREE = "3";

    /** The string &quot;{@value}&quot;. */
    private static final String FOUR = "4";

    /** The string &quot;{@value}&quot;. */
    private static final String FIVE = "5";

    /** The string &quot;{@value}&quot;. */
    private static final String SIX = "6";

    /** The string &quot;{@value}&quot;. */
    private static final String SEVEN = "7";

    /** The string &quot;{@value}&quot;. */
    private static final String NINE = "9";

    /** The string &quot;{@value}&quot;. */
    private static final String ERROR = "ERROR";

    /** The string &quot;{@value}&quot;. */
    private static final String IM_IN = "IM_IN";

    /** The string &quot;{@value}&quot;. */
    private static final String MSG_FROM = "Message from [";

    /** The string &quot;{@value}&quot;. */
    private static final String RB_COLON = "]: ";

    /** The string &quot;{@value}&quot;. */
    private static final String SENDIM = "$SENDIM";

    /** The string &quot;{@value}&quot;. */
    private static final String _AIM = "_AIM";

    /** The string &quot;{@value}&quot;. */
    private static final String CHAT_IN = "CHAT_IN";

    /** The string &quot;{@value}&quot;. */
    private static final String _901 = "901";

    /** The string &quot;{@value}&quot;. */
    private static final String _903 = "903";

    /** The string &quot;{@value}&quot;. */
    private static final String _960 = "960";

    /** The string &quot;{@value}&quot;. */
    private static final String _961 = "961";

    /** The string &quot;{@value}&quot;. */
    private static final String _962 = "962";

    /** The string &quot;{@value}&quot;. */
    private static final String SIGNON_ERR = "Signon err";

    /** The string &quot;{@value}&quot;. */
    private static final String TOC_SEND_IM = "toc_send_im ";

    /** The string &quot;{@value}&quot;. */
    private static final String TOC_CHAT_SEND = "toc_chat_send ";

    /** The string &quot;{@value}&quot;. */
    private static final String TOC_ADD_BUDDY = "toc_add_buddy ";

    /** <code>&lt;BODY BGCOLOR=&quot;</code> */
    private static final String MSG_FMT_0 = "<BODY BGCOLOR=\"";

    /** <code>&lt;&quot;&gt;&lt;FONT SIZE=&quot;</code> */
    private static final String MSG_FMT_1 = "\"><FONT SIZE=\"";

    /** <code> FACE=&quot;</code> */
    private static final String MSG_FMT_2 = " FACE=\"";

    /** <code>&quot; COLOR=&quot;</code> */
    private static final String MSG_FMT_3 = " COLOR=\"";

    /** <code>&quot;&gt;</code> */
    private static final String MSG_FMT_4 = "\">";

    /** <code>&lt;/FONT&gt;</code> */
    private static final String MSG_FMT_5 = "</FONT>";

    /** A space and a quote. */
    private static final String SPACE_QUOTE = " \"";

    /** A space. */
    private static final String SPACE = " ";

    /** A backslash. */
    private static final String BACKSLASH = "\\";

    /** &quot;\0&quot; */
    private static final String NULL = "\0";

    /** &quot;\&quot;\0&quot; */
    private static final String QUOTE_NULL = "\"\0";

    /**
     * Constructs a new <code>AIMListener</code> listener and sets up
     * parameters.
     * 
     * @param coreToUse the Core object in use
     * @param botToListenFor the bot for whom to listen
     * @param parametersToUse the parameters for the listener and their default
     *            values
     */
    public AIMListener(Core coreToUse, Bot botToListenFor, Map<String, String> parametersToUse)
    {
        super(coreToUse, botToListenFor, parametersToUse);
        // Get parameters.
        this.owner = this.parameters.get("owner");
        this.screenname = this.parameters.get("screenname");
        this.password = this.parameters.get("password");
        this.bgcolor = this.parameters.get("bgcolor");
        this.fontface = this.parameters.get("fontface");
        this.fontsize = this.parameters.get("fontsize");
        this.fontcolor = this.parameters.get("fontcolor");
        this.buddies = this.parameters.get("buddies");
    }

    /**
     * Checks all the parameters for validity.
     * 
     * @throws InvalidListenerParameterException if a parameter is invalid
     */
    public void checkParameters() throws InvalidListenerParameterException
    {
        if (this.owner.length() == 0)
        {
            throw new InvalidListenerParameterException("No owner specified; aborting.");
        }
        if (this.screenname.length() == 0)
        {
            throw new InvalidListenerParameterException("No screenname specified; aborting.");
        }
        if (this.password.length() == 0)
        {
            throw new InvalidListenerParameterException("No password specified; aborting.");
        }
    }

    /**
     * Signs on the listener and starts waiting for messages.
     */
    public void run()
    {
        int length;
        this.seqNo = (int) Math.floor(Math.random() * MAX_SEQ);
        // Set up the connection.
        this.logger.log(Level.INFO, "AIMListener setting up connection for \"" + this.botID + "\".");
        try
        {
            this.connection = new Socket(HOST, PORT);
            this.connection.setSoTimeout(10000);
            this.in = new DataInputStream(this.connection.getInputStream());
            this.out = new DataOutputStream(new BufferedOutputStream(this.connection.getOutputStream()));
        }
        catch (Exception e)
        {
            signoff(ONE);
            return;
        }
        try
        {
            this.out.writeBytes("FLAPON\r\n\r\n");
            this.out.flush();
            byte[] signon = new byte[10];
            this.in.readFully(signon);
            this.out.writeByte(42);
            this.out.writeByte(1);
            this.out.writeShort(this.seqNo);
            this.seqNo = (this.seqNo + 1) & MAX_SEQ;
            this.out.writeShort(this.screenname.length() + 8);
            this.out.writeInt(1);
            this.out.writeShort(1);
            this.out.writeShort(this.screenname.length());
            this.out.writeBytes(this.screenname);
            this.out.flush();

            frameSend("toc_signon login.oscar.aol.com 5190 " + this.screenname + " " + imRoast(this.password) + " english programdbot\0");

            this.in.skip(4);
            length = this.in.readShort();
            signon = new byte[length];
            this.in.readFully(signon);
            if (String.valueOf(signon).startsWith(ERROR))
            {
                this.logger.log(Level.SEVERE, "Signon error.");
                signoff(TWO);
                return;
            }
            this.in.skip(4);
            length = this.in.readShort();
            signon = new byte[length];
            this.in.readFully(signon);
            this.online = true;

            sendBuddies();

            frameSend("toc_init_done\0");

            this.logger.log(Level.FINER, "Logon complete.");
            this.connection.setSoTimeout(3000);
        }
        catch (InterruptedIOException e)
        {
            this.online = false;
            signoff(TWO_POINT_FIVE);
        }
        catch (IOException e)
        {
            this.online = false;
            signoff(THREE);
        }
        byte[] data;
        while (true)
        {
            try
            {
                this.in.skip(4);
                length = this.in.readShort();
                data = new byte[length];
                this.in.readFully(data);
                fromAIM(data);
            }
            catch (InterruptedIOException e)
            {
                this.logger.log(Level.SEVERE, "Error in main AIMListener loop.", e);
            }
            catch (IOException e)
            {
                this.logger.log(Level.SEVERE, "Error in main AIMListener loop.", e);
                break;
            }
        }
        signoff(FOUR);
    }

    /**
     * Shuts down the listener.
     */
    public void shutdown()
    {
        signoff(FOUR);
    }

    /**
     * Sends the given frame.
     * 
     * @param toBeSent the frame to send
     * @throws IOException
     */
    public void frameSend(String toBeSent) throws IOException
    {
        this.out.writeByte(42);
        this.out.writeByte(2);
        this.out.writeShort(this.seqNo);
        this.seqNo = (this.seqNo + 1) & MAX_SEQ;
        this.out.writeShort(toBeSent.length());
        this.out.writeBytes(toBeSent);
        this.out.flush();
    }

    /**
     * Processes data received from AIM.
     * 
     * @param buffer the data received from AIM
     */
    public void fromAIM(byte[] buffer)
    {
        String inString = new String(buffer);

        this.logger.log(Level.FINEST, "Got: \"" + inString + "\"");

        StringTokenizer inToken = new StringTokenizer(inString, ":");
        String command = inToken.nextToken();
        if (command.equals(IM_IN))
        {
            String from = imNormalize(inToken.nextToken());
            /* String auto = */
            inToken.nextToken();
            StringBuffer mesg = new StringBuffer(inToken.nextToken());
            while (inToken.hasMoreTokens())
            {
                mesg.append(':');
                mesg.append(inToken.nextToken());
            }
            String request = XMLKit.removeMarkup(mesg.toString());
            this.logger.log(Level.INFO, MSG_FROM + from + RB_COLON + request);

            if (request.startsWith(SENDIM) && this.owner.equals(from))
            {
                StringTokenizer st = new StringTokenizer(request);
                /* String imcommand = */
                st.nextToken();
                String imcommandTo = st.nextToken();
                String imcommandText = st.nextToken();
                sendMesg(imcommandTo, imcommandText);
            }
            else
            {
                String[] response = XMLKit.filterViaHTMLTags(this.core.getResponse(request, from + _AIM, this.botID, this.responder));
                if (response.length > 0)
                {
                    for (int line = 0; line < response.length; line++)
                    {
                        sendMesg(from, response[line]);
                    }
                }
            }
            return;
        }
        if (command.equals(CHAT_IN))
        {
            String room_id = imNormalize(inToken.nextToken());
            String from = imNormalize(inToken.nextToken());
            StringBuffer mesg = new StringBuffer(inToken.nextToken());
            while (inToken.hasMoreTokens())
            {
                mesg.append(':');
                mesg.append(inToken.nextToken());
            }
            String request = XMLKit.removeMarkup(mesg.toString());
            if (request.indexOf(this.screenname) > 0)
            {
                String[] response = XMLKit.filterViaHTMLTags(this.core.getResponse(request, from + _AIM, this.botID, this.responder));
                if (response.length > 0)
                {
                    for (int line = 0; line < response.length; line++)
                    {
                        sendChatRoomMesg(room_id, response[line]);
                    }
                }
            }
            return;
        }
        if (command.equals(ERROR))
        {
            String error = inToken.nextToken();
            this.logger.log(Level.SEVERE, "Error: " + error);
            if (error.equals(_901))
            {
                this.logger.log(Level.WARNING, "Not currently available.");
                return;
            }
            if (error.equals(_903))
            {
                this.logger.log(Level.WARNING, "Message dropped; sending too fast.");
                return;
            }
            if (error.equals(_960))
            {
                this.logger.log(Level.WARNING, "Sending messages too fast to " + inToken.nextToken());
                return;
            }
            if (error.equals(_961))
            {
                this.logger.log(Level.WARNING, inToken.nextToken() + " sent you too big a message.");
                return;
            }
            if (error.equals(_962))
            {
                this.logger.log(Level.WARNING, inToken.nextToken() + " sent you a message too fast.");
                return;
            }
            if (error.equals(SIGNON_ERR))
            {
                this.logger.log(Level.WARNING, "AIM signon failure: " + inToken.nextToken());
                signoff(FIVE);
            }
            return;
        }
    }

    /**
     * Sends a message to a designated recipient.
     * 
     * @param to the recipient
     * @param message the message to send
     */
    public void sendMesg(String to, String message)
    {
        String text = new StringBuffer(MSG_FMT_0).append(this.bgcolor).append(MSG_FMT_1).append(this.fontsize).append(MSG_FMT_2)
                .append(this.fontface).append(MSG_FMT_3).append(this.fontcolor).append(MSG_FMT_4).append(message).append(MSG_FMT_5).toString();

        String work = new StringBuffer(TOC_SEND_IM).append(to).append(SPACE_QUOTE).append(imEscape(text)).toString();

        this.logger.log(Level.INFO, message);
        try
        {
            frameSend(work);
        }
        catch (IOException e)
        {
            signoff(NINE);
        }
    }

    /**
     * Sends a message to a chat room.
     * 
     * @param roomID the room identifier
     * @param message the message to send
     */
    public void sendChatRoomMesg(String roomID, String message)
    {
        String work = new StringBuffer(TOC_CHAT_SEND).append(roomID).append(SPACE_QUOTE).append(imEscape(message)).toString();

        this.logger.log(Level.INFO, message);
        try
        {
            frameSend(work);
        }
        catch (IOException e)
        {
            signoff(NINE);
        }
    }

    /**
     * Escapes a string according to the requirements of AIM.
     * 
     * @param text the string to escape
     * @return the escaped string
     */
    private StringBuffer imEscape(String text)
    {
        StringBuffer work = new StringBuffer();
        StringCharacterIterator iterator = new StringCharacterIterator(text);
        for (char aChar = iterator.first(); aChar != CharacterIterator.DONE; aChar = iterator.next())
        {
            switch (aChar)
            {
                case '$':
                case '{':
                case '}':
                case '[':
                case ']':
                case '(':
                case ')':
                case '\"':
                case '\\':
                    work.append(BACKSLASH);
                    break;
                default:
                    break;
            }
            work.append(aChar);
        }
        work.append(QUOTE_NULL);
        return work;
    }

    /**
     * Sends an AIM message. Cannot send a message longer than 2030 bytes.
     * 
     * @param buffer the message
     */
    public void toAIM(byte[] buffer)
    {
        // we can't send responses > 2048 bytes
        if (buffer.length < 2030)
        {
            this.logger.log(Level.WARNING, "Got a message longer than 2030 bytes.");
            return;
        }
        try
        {
            this.out.writeByte(42);
            this.out.writeByte(2);
            this.out.writeShort(this.seqNo);
            this.seqNo = (this.seqNo + 1) & MAX_SEQ;
            this.out.writeShort(buffer.length + 1);
            this.out.write(buffer);
            this.out.writeByte('\0');
            this.out.flush();
        }
        catch (IOException e)
        {
            this.logger.log(Level.SEVERE, "Exception: " + e);
            signoff(SIX);
        }
    }

    /**
     * Please document.
     */
    public void sendBuddies()
    {
        String toBeSent = TOC_ADD_BUDDY;

        this.logger.log(Level.FINEST, "To be sent: \"" + toBeSent + "\"");
        try
        {
            frameSend(toBeSent + ' ' + this.screenname + NULL);
            StringTokenizer st = new StringTokenizer(this.buddies, ",");
            while (st.hasMoreTokens())
            {
                frameSend(toBeSent + ' ' + st.nextToken() + NULL);
            }
        }
        catch (IOException e)
        {
            this.logger.log(Level.SEVERE, "Exception: " + e);
            signoff(SEVEN);
        }
    }

    /**
     * Please document.
     * 
     * @param place
     */
    public void signoff(String place)
    {
        this.online = false;
        this.logger.log(Level.FINEST, "Trying to close IM (" + place + ").....");

        try
        {
            if (this.out != null)
            {
                this.out.close();
            }
            if (this.in != null)
            {
                this.in.close();
            }
            if (this.connection != null)
            {
                this.connection.close();
            }
        }
        catch (IOException e)
        {
            this.logger.log(Level.SEVERE, "IO Exception: " + e);
        }
        this.logger.log(Level.FINEST, "Done.");
    }

    /**
     * Encodes a password according to AIM's stupid requirement.
     * 
     * @param pass the password to encode
     * @return the result of this operation.
     */
    public static String imRoast(String pass)
    {
        String append = null;
        StringBuffer result = new StringBuffer(ZERO_X);

        int passLength = pass.length();

        for (int index = 0; index < passLength; index++)
        {
            append = Long.toHexString(pass.charAt(index) ^ ROAST.charAt(index % 7));
            if (append.length() < 2)
            {
                result.append(ZERO);
            }
            result.append(append);
        }
        return result.toString();
    }

    /**
     * Removes spaces from a string.
     * 
     * @param in the string from which to remove spaces
     * @return the string without spaces
     */
    public static String imNormalize(String in)
    {
        StringBuffer out = new StringBuffer(in);
        int space;
        while (((space = out.toString().indexOf(SPACE)) >= 0) && out.length() > 0)
        {
            out.delete(space, space + 1);
        }
        return out.toString();
    }
}