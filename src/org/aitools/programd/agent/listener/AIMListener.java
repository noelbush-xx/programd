/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.agent.listener;

import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.Socket;
import java.text.StringCharacterIterator;
import java.util.StringTokenizer;

import org.aitools.programd.agent.responder.AIMResponder;
import org.aitools.programd.bot.Bot;
import org.aitools.programd.multiplexor.Multiplexor;
import org.aitools.programd.util.XMLKit;
import org.aitools.programd.util.logging.Log;

/**
 * This code is from the Everybuddy Java Project by Chris Carlin
 * (http://EBJava.sourceforge.net/) modified to work with a Program D server.
 * 
 * @author Jon Baer
 * @author Sandy McArthur
 * @version 1.0
 */
public class AIMListener extends Listener
{
    // Instance variables.

    /** Please describe. */
    private String name;

    /** Please describe. */
    private String pass;

    /** Please describe. */
    private String bgcolor;

    /** Please describe. */
    private String fontface;

    /** Please describe. */
    private String fontsize;

    /** Please describe. */
    private String fontcolor;

    /** Please describe. */
    private String owner;

    /** Please describe. */
    private String buddies;

    /** Please describe. */
    private int seqNo;

    /** Please describe. */
    private Socket connection;

    /** Please describe. */
    private DataInputStream in;

    /** Please describe. */
    private DataOutputStream out;

    /** Please describe. */
    protected boolean online;

    // Convenience constants.

    /** Please describe. */
    private static final int MAX_SEQ = 65535;

    /** Please describe. */
    private static final String HOST = "toc.oscar.aol.com";

    /** Please describe. */
    private static final int PORT = 21;

    /** The string &quot;Tic/Toc&quot;. */
    private static final String ROAST = "Tic/Toc";

    /** The string &quot;0&quot;. */
    private static final String ZERO = "0";

    /** The string &quot;0x&quot;. */
    private static final String ZERO_X = "0x";

    /** The string &quot;1&quot;. */
    private static final String ONE = "1";

    /** The string &quot;2&quot;. */
    private static final String TWO = "2";

    /** The string &quot;2.5&quot;. */
    private static final String TWO_POINT_FIVE = "2.5";

    /** The string &quot;3&quot;. */
    private static final String THREE = "3";

    /** The string &quot;4&quot;. */
    private static final String FOUR = "4";

    /** The string &quot;5&quot;. */
    private static final String FIVE = "5";

    /** The string &quot;6&quot;. */
    private static final String SIX = "6";

    /** The string &quot;7&quot;. */
    private static final String SEVEN = "7";

    /** The string &quot;8&quot;. */
    private static final String EIGHT = "8";

    /** The string &quot;9&quot;. */
    private static final String NINE = "9";

    /** The string &quot;ERROR&quot;. */
    private static final String ERROR = "ERROR";

    /** The string &quot;IM_IN&quot;. */
    private static final String IM_IN = "IM_IN";

    /** The string &quot; Message from [&quot;. */
    private static final String MSG_FROM = "Message from [";

    /** The string &quot;]: &quot;. */
    private static final String RB_COLON = "]: ";

    /** The string &quot;$SENDIM&quot;. */
    private static final String SENDIM = "$SENDIM";

    /** The string &quot;_AIM&quot;. */
    private static final String _AIM = "_AIM";

    /** The string &quot;CHAT_IN&quot;. */
    private static final String CHAT_IN = "CHAT_IN";

    /** The string &quot;901&quot;. */
    private static final String _901 = "901";

    /** The string &quot;903&quot;. */
    private static final String _903 = "903";

    /** The string &quot;960&quot;. */
    private static final String _960 = "960";

    /** The string &quot;961&quot;. */
    private static final String _961 = "961";

    /** The string &quot;962&quot;. */
    private static final String _962 = "962";

    /** The string &quot;Signon err&quot;. */
    private static final String SIGNON_ERR = "Signon err";

    /** The string &quot;toc_send_im &quot;. */
    private static final String TOC_SEND_IM = "toc_send_im ";

    /** The string &quot;toc_chat_send &quot;. */
    private static final String TOC_CHAT_SEND = "toc_chat_send ";

    /** The string &quot;toc_add_buddy &quot;. */
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

    /** An empty string. */
    private static final String EMPTY_STRING = "";

    /** The label (as required by the registration scheme). */
    public static final String label = "ProgramD-AIM";

    /** The message label. */
    private static final String MSG = "ProgramD-AIM: ";

    /**
     * Constructs a new <code>AIMListener</code> listener and sets up
     * parameters.
     */
    public AIMListener(Bot botToListen)
    {
        super(botToListen, "AIMListener", new String[][]
            {
                { "owner", "" } ,
                { "screenname", "" } ,
                { "password", "" } ,
                { "bgcolor", "White" } ,
                { "fontface", "Verdana,Arial" } ,
                { "fontsize", "2" } ,
                { "fontcolor", "Black" } ,
                { "buddies", "" } } );
    } 

    public boolean checkParameters()
    {
        // Get parameters.
        this.owner = (String) this.parameters.get("owner");
        this.name = (String) this.parameters.get("screenname");
        this.pass = (String) this.parameters.get("password");
        this.bgcolor = (String) this.parameters.get("bgcolor");
        this.fontface = (String) this.parameters.get("fontface");
        this.fontsize = (String) this.parameters.get("fontsize");
        this.fontcolor = (String) this.parameters.get("fontcolor");
        this.buddies = (String) this.parameters.get("buddies");

        // Check parameters.
        if (this.owner.length() == 0)
        {
            logMessage("No owner specified; aborting.");
            return false;
        } 
        if (this.name.length() == 0)
        {
            logMessage("No screen name specified; aborting.");
            return false;
        } 
        if (this.pass.length() == 0)
        {
            logMessage("No password specified; aborting.");
            return false;
        } 
        return true;
    } 

    /**
     * Signs on the listener and starts waiting for messages.
     */
    public void run()
    {
        int length;
        this.seqNo = (int) Math.floor(Math.random() * MAX_SEQ);
        logMessage("Starting for \"" + this.botID + "\".");
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
            this.out.writeShort(this.name.length() + 8);
            this.out.writeInt(1);
            this.out.writeShort(1);
            this.out.writeShort(this.name.length());
            this.out.writeBytes(this.name);
            this.out.flush();

            frameSend("toc_signon login.oscar.aol.com 5190 " + this.name + " " + imRoast(this.pass)
                    + " english programdbot\0");

            this.in.skip(4);
            length = this.in.readShort();
            signon = new byte[length];
            this.in.readFully(signon);
            if (String.valueOf(signon).startsWith(ERROR))
            {
                logMessage("Signon error.");
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

            logMessage("Logon complete.");
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
                Log.devinfo(MSG + "Error*: " + e, Log.LISTENERS);
            } 
            catch (IOException e)
            {
                Log.devinfo(MSG + "Error**: " + e, Log.LISTENERS);
                break;
            } 
        } 
        signoff(FOUR);
    } 

    public void shutdown()
    {
        signoff(FOUR);
    } 

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
     * @param buffer
     *            the data received from AIM
     */
    public void fromAIM(byte[] buffer)
    {
        String inString = new String(buffer);

        Log.devinfo(MSG + "Got: " + inString, Log.LISTENERS);

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
            logMessage(MSG_FROM + from + RB_COLON + request);

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
                String[] botResponse = XMLKit.breakLinesAtTags(Multiplexor.getResponse(request, from + _AIM,
                        this.botID, new AIMResponder()));
                if (botResponse.length > 0)
                {
                    for (int line = 0; line < botResponse.length; line++)
                    {
                        sendMesg(from, botResponse[line]);
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
            if (request.indexOf(this.name) > 0)
            {
                String[] botResponse = XMLKit.breakLinesAtTags(Multiplexor.getResponse(request, from + _AIM,
                        this.botID, new AIMResponder()));
                if (botResponse.length > 0)
                {
                    for (int line = 0; line < botResponse.length; line++)
                    {
                        sendChatRoomMesg(room_id, botResponse[line]);
                    } 
                } 
            } 
            return;
        } 
        if (command.equals(ERROR))
        {
            String error = inToken.nextToken();
            logMessage("Error: " + error);
            if (error.equals(_901))
            {
                logMessage("Not currently available.");
                return;
            } 
            if (error.equals(_903))
            {
                logMessage("Message dropped; sending too fast.");
                return;
            } 
            if (error.equals(_960))
            {
                logMessage("Sending messages too fast to " + inToken.nextToken());
                return;
            } 
            if (error.equals(_961))
            {
                logMessage(inToken.nextToken() + " sent you too big a message.");
                return;
            } 
            if (error.equals(_962))
            {
                logMessage(inToken.nextToken() + " sent you a message too fast.");
                return;
            } 
            if (error.equals(SIGNON_ERR))
            {
                logMessage("AIM signon failure: " + inToken.nextToken());
                signoff(FIVE);
            } 
            return;
        } 
    } 

    /**
     * Sends a message to a designated recipient.
     * 
     * @param to
     *            the recipient
     * @param message
     *            the message to send
     */
    public void sendMesg(String to, String message)
    {
        String text = new StringBuffer(MSG_FMT_0).append(this.bgcolor).append(MSG_FMT_1).append(this.fontsize).append(
                MSG_FMT_2).append(this.fontface).append(MSG_FMT_3).append(this.fontcolor).append(MSG_FMT_4).append(
                message).append(MSG_FMT_5).toString();

        String work = new StringBuffer(TOC_SEND_IM).append(to).append(SPACE_QUOTE).append(imEscape(text)).toString();

        logMessage(message);
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
     * @param roomID
     *            the room identifier
     * @param message
     *            the message to send
     */
    public void sendChatRoomMesg(String roomID, String message)
    {
        String work = new StringBuffer(TOC_CHAT_SEND).append(roomID).append(SPACE_QUOTE).append(imEscape(message))
                .toString();

        logMessage(message);
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
     * @param text
     *            the string to escape
     * @return the escaped string
     */
    private StringBuffer imEscape(String text)
    {
        StringBuffer work = new StringBuffer();
        StringCharacterIterator iterator = new StringCharacterIterator(text);
        for (char aChar = iterator.first(); aChar != StringCharacterIterator.DONE; aChar = iterator.next())
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
     * @param buffer
     *            the message
     */
    public void toAIM(byte[] buffer)
    {
        // we can't send responses > 2048 bytes
        if (buffer.length < 2030)
        {
            logMessage("Got a message longer than 2030 bytes.");
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
            logMessage("Exception: " + e);
            signoff(SIX);
        } 
    } 

    /**
     * Please document.
     */
    public void sendBuddies()
    {
        String toBeSent = TOC_ADD_BUDDY;

        Log.devinfo(MSG + toBeSent, Log.LISTENERS);
        try
        {
            frameSend(toBeSent + ' ' + this.name + NULL);
            StringTokenizer st = new StringTokenizer(this.buddies, ",");
            while (st.hasMoreTokens())
            {
                frameSend(toBeSent + ' ' + st.nextToken() + NULL);
            } 
        } 
        catch (IOException e)
        {
            logMessage("Exception: " + e);
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
        logMessage("Trying to close IM (" + place + ").....");

        try
        {
            this.out.close();
            this.in.close();
            this.connection.close();
        } 
        catch (IOException e)
        {
            logMessage("Exception: " + e);
        } 
        logMessage("Done.");
    } 

    /**
     * Encodes a password according to AIM's stupid requirement.
     * 
     * @param pass
     *            the password to encode
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
     * @param in
     *            the string from which to remove spaces
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

    /**
     * Standard method for logging and notifying of a message.
     * 
     * @param message
     *            the message
     */
    private void logMessage(String message)
    {
        Log.userinfo(MSG + message, Log.LISTENERS);
    } 
}