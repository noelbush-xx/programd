/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.agent.listener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.aitools.programd.agent.responder.TextResponder;
import org.aitools.programd.bot.Bot;
import org.aitools.programd.multiplexor.Multiplexor;
import org.aitools.programd.util.Trace;
import org.aitools.programd.util.logging.Log;

/**
 * This code is from the Everybuddy Java Project by Chris Carlin
 * (http://EBJava.sourceforge.net/) modified to work with a Program D server.
 * 
 * @author Chris Carlin
 * @author Jon Baer
 * @author Noel Bush
 * @see <a href="http://EBJava.sourceforge.net/">Everybuddy </a>
 * @version 1.0
 */
public class ICQListener extends Listener
{
    /** Please document. */
    private static final int SERVERPORT = 4000;

    /** Please document. */
    private static final String SERVER = "icq.mirabilis.com";

    /** Please document. */
    private static final String _ICQ = "_ICQ";

    /** Please document. */
    private String pass;

    /** Please document. */
    private int uin;

    /** Please document. */
    protected DatagramSocket socket;

    /** Please document. */
    private DatagramPacket packet;

    /** Please document. */
    private byte[] buffer;

    /** Please document. */
    private short seqNo = 1;

    /** Please document. */
    private static final short VERSION = (short) 2;

    /** Please document. */
    private static InetAddress serverAddy;

    /** Please document. */
    protected boolean online = false;

    /** Please document. */
    private int clientport;

    /** Please document. */
    public static final String label = "ProgramD-ICQ";

    /** Please document. */
    private static final String MSG = "ProgramD-ICQ: ";

    /**
     * Creates a new ICQListener chat listener for a given bot.
     * 
     * @param botToListen
     *            the bot for whom to listen
     */
    public ICQListener(Bot botToListen)
    {
        super(botToListen, "ICQListener", new String[][]
            {
                { "number", "" } ,
                { "password", "" } } );
    } 

    public boolean checkParameters()
    {
        // Get parameters.
        try
        {
            this.uin = Integer.parseInt((String) this.parameters.get("number"));
        } 
        catch (NumberFormatException e)
        {
            logMessage("Invalid user number (try a number!); aborting.");
            return false;
        } 
        this.pass = (String) this.parameters.get("password");

        // Check parameters.
        if (this.uin <= 0)
        {
            logMessage("Invalid user number; aborting.");
        } 
        if (this.pass.length() == 0)
        {
            logMessage("Invalid empty password; aborting.");
            return false;
        } 
        this.clientport = 0;
        return true;
    } 

    /**
     * Please document this.
     */
    public void run()
    {
        try
        {
            this.socket = new DatagramSocket();
            serverAddy = InetAddress.getByName(SERVER);
            logMessage("logging in to " + serverAddy);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();

            stream.write(header((short) 0x3E8));
            stream.write(toBytes(this.clientport));
            stream.write(toBytes((short) (this.pass.length() + 1)));
            stream.write((new String(this.pass + "\0")).getBytes());
            stream.write(toBytes(0x78));
            stream.write(InetAddress.getByName("localhost").getAddress());
            stream.write(0x4);
            stream.write(toBytes(0));
            stream.write(toBytes(0x2));
            stream.write(toBytes((short) 0x3E));
            stream.write(toBytes(0));
            stream.write(toBytes(0x780000));
            this.socket.send(new DatagramPacket(stream.toByteArray(), stream.size(), serverAddy, SERVERPORT));
            stream.close();

            this.buffer = new byte[10];
            this.packet = new DatagramPacket(this.buffer, this.buffer.length);
            this.socket.receive(this.packet);
            this.buffer = this.packet.getData();
            if (this.buffer[2] != (byte) 0x0A || this.buffer[3] != (byte) 0x00)
            {
                if (this.buffer[2] == (byte) 0x5A && this.buffer[3] == (byte) 0x00)
                {
                    ack(this.buffer[4], this.buffer[5]);
                } 
                else
                {
                    logMessage("No acknowledgement from server; aborting.");
                    return;
                } 
            } 
            this.socket.receive(this.packet);
            this.buffer = this.packet.getData();
            if (this.buffer[2] == (byte) 0x5A && this.buffer[3] == (byte) 0x00)
            {
                ack(this.buffer[4], this.buffer[5]);
            } 
            else
            {
                if (this.buffer[2] != (byte) 0x0A || this.buffer[3] != (byte) 0x00)
                {
                    logMessage("No Login Reply: " + this.buffer[2] + " " + this.buffer[3]);
                    return;
                } 
            } 
            this.online = true;
            logMessage("Successfully logged on.");

            toICQ(new byte[]
                { (byte) 0x4c, (byte) 0x4 } );

            ProgramDICQKeepAlive keepAlive = new ProgramDICQKeepAlive(this);
            keepAlive.setDaemon(true);
            logMessage("Starting for \"" + this.botID + "\".");
            keepAlive.start();
            this.socket.setSoTimeout(2000);
            while (true)
            {
                try
                {
                    this.buffer = new byte[512];
                    this.packet = new DatagramPacket(this.buffer, this.buffer.length);
                    this.socket.receive(this.packet);
                    this.buffer = this.packet.getData();

                    if ((this.buffer[0] == (byte) 0x2 && this.buffer[1] == (byte) 0x0)
                            && !(this.buffer[2] == (byte) 0xA && this.buffer[3] == (byte) 0x0))
                    {
                        fromICQ(this.buffer);
                        ack(this.buffer[4], this.buffer[5]);
                        Log.devinfo("ICQListener: ICQ Command in: " + Integer.toHexString(this.buffer[2]) + " "
                                + Integer.toHexString(this.buffer[3]), Log.LISTENERS);
                    } 
                } 
                catch (InterruptedIOException e)
                {
                    // Please document this.
                } 
            } 
        } 
        catch (UnknownHostException e)
        {
            logMessage("Unknown host!");
        } 
        catch (SocketException e)
        {
            logMessage("Socket exception!");
        } 
        catch (IOException e)
        {
            logMessage("IO Exception!");
        } 
        signoff();
    } 

    public void shutdown()
    {
        signoff();
    } 

    /**
     * Please document this.
     */
    public void toICQ(byte[] msgBuffer) throws IOException
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write(toBytes((short) 0x2));
        stream.write(msgBuffer[0]);
        stream.write(msgBuffer[1]);
        stream.write(toBytes(this.seqNo));
        stream.write(toBytes(this.uin));
        this.seqNo = (short) ((this.seqNo + 1) & 0xFFFF);
        Log.devinfo(MSG + "ICQ Command out: " + Integer.toHexString(msgBuffer[0]) + " "
                + Integer.toHexString(msgBuffer[1]), Log.LISTENERS);
        Trace.devinfo("Buffer length: " + msgBuffer.length);
        if (msgBuffer.length > 2)
        {
            stream.write(msgBuffer, 2, msgBuffer.length - 2);
        } 
        this.socket.send(new DatagramPacket(stream.toByteArray(), stream.size(), serverAddy, SERVERPORT));
    } 

    /**
     * Please document this.
     */
    private void fromICQ(byte[] msgBuffer)
    {
        String message;
        int from;

        if (msgBuffer[2] == (byte) 0x6E && msgBuffer[3] == (byte) 0x00)
        {
            from = ((msgBuffer[6]) & 0xFF) + ((msgBuffer[7] << 8) & 0xFF00) + ((msgBuffer[8] << 16) & 0xFF0000)
                    + ((msgBuffer[9] << 24) & 0xFF000000);
            // String ip =
            // ((int)(buffer[10])&0xFF)+"."+((int)(buffer[11])&0xFF)+"."+((int)(buffer[12])&0xFF)+"."+((int)(buffer[13])&0xFF);
            // int port = ((buffer[14])&0xFF) + ((buffer[15]<<8)&0xFF00) +
            // ((buffer[16]<<16)&0xFF0000) + ((buffer[17]<<24)&0xFF000000);
            // ebnet.updateStat(Integer.toString(uin),"IC",true);
            return;
        } 
        else if (msgBuffer[2] == (byte) 0x78 && msgBuffer[3] == (byte) 0x00)
        {
            from = ((msgBuffer[6]) & 0xFF) + ((msgBuffer[7] << 8) & 0xFF00) + ((msgBuffer[8] << 16) & 0xFF0000)
                    + ((msgBuffer[9] << 24) & 0xFF000000);
            // ebnet.updateStat(Integer.toString(uin),"IC",false);
            return;
        } 
        else if (msgBuffer[2] == (byte) 0xE6 && msgBuffer[3] == (byte) 0x00)
        {
            try
            {
                toICQ(new byte[]
                    { (byte) 0x42, (byte) 0x4 } );
            } 
            catch (IOException e)
            {
                logMessage("IO Exception: " + e.getMessage());
            } 
            return;
        } 
        else if (msgBuffer[2] == (byte) 0xDC && msgBuffer[3] == (byte) 0x00)
        {
            from = ((msgBuffer[6]) & 0xFF) + ((msgBuffer[7] << 8) & 0xFF00) + ((msgBuffer[8] << 16) & 0xFF0000)
                    + ((msgBuffer[9] << 24) & 0xFF000000);
            short length = (short) (((msgBuffer[18]) & 0xFF) + ((msgBuffer[19] << 8) & 0xFF00));
            message = new String(msgBuffer, 20, length - 1);
            logMessage("Message from [" + from + "]: " + message);
            return;
        } 
        else if (msgBuffer[2] == (byte) 0x04 && msgBuffer[3] == (byte) 0x01)
        {
            from = ((msgBuffer[6]) & 0xFF) + ((msgBuffer[7] << 8) & 0xFF00) + ((msgBuffer[8] << 16) & 0xFF0000)
                    + ((msgBuffer[9] << 24) & 0xFF000000);
            short length = (short) (((msgBuffer[12]) & 0xFF) + ((msgBuffer[13] << 8) & 0xFF00));
            message = new String(msgBuffer, 14, length - 1);
            logMessage("Message from [" + from + "]: " + message);
            if (message != null)
            {
                String botResponse = Multiplexor.getResponse(message, from + _ICQ, this.botID, new TextResponder());
                sendMesg(from, botResponse);
            } 
            return;
        } 
        else
        {
            return;
        } 
    } 

    /**
     * Converts a <code>short</code> to a <code>byte[]</code>.
     * 
     * @param x
     *            the short to convert
     * @return the short as a byte[]
     */
    public static byte[] toBytes(short x)
    {
        byte[] b = new byte[2];
        b[0] = (byte) (x & 255);
        b[1] = (byte) ((x >> 8) & 255);
        return b;
    } 

    /**
     * Converts an <code>int</code> to a <code>byte[]</code>.
     * 
     * @param x
     *            the int to convert
     * @return the int as a byte[]
     */
    public static byte[] toBytes(int x)
    {
        byte[] b = new byte[4];
        b[0] = (byte) (x & 255);
        b[1] = (byte) ((x >> 8) & 255);
        b[2] = (byte) ((x >> 16) & 255);
        b[3] = (byte) ((x >> 24) & 255);
        return b;
    } 

    public byte[] header(short command) throws IOException
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write(toBytes((short) 0x2));
        stream.write(toBytes(command));
        stream.write(toBytes(this.seqNo));
        stream.write(toBytes(this.uin));
        this.seqNo = (short) ((this.seqNo + 1) & 0xFFFF);
        return stream.toByteArray();
    } 

    public void ack(byte a, byte b) throws IOException
    {
        Log.devinfo(MSG + "Acknowledgement from server!", Log.LISTENERS);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write(toBytes(VERSION));
        stream.write(toBytes((short) 0xA));
        stream.write(a);
        stream.write(b);
        stream.write(toBytes(this.uin));
        this.socket.send(new DatagramPacket(stream.toByteArray(), stream.size(), serverAddy, SERVERPORT));
    } 

    public void send(byte[] msgBuffer) throws IOException
    {
        this.socket.send(new DatagramPacket(msgBuffer, msgBuffer.length, serverAddy, SERVERPORT));
    } 

    public void sendMesg(int to, String mesg)
    {
        logMessage("Response to [" + to + "]: " + mesg);
        try
        {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            stream.write((byte) 0x0E);
            stream.write((byte) 0x01);
            stream.write(toBytes(to));
            stream.write((byte) 0x01);
            stream.write((byte) 0x00);
            stream.write(toBytes((short) mesg.length()));
            stream.write(mesg.getBytes());
            stream.write((byte) '\0');
            toICQ(stream.toByteArray());
        } 
        catch (IOException e)
        {
            logMessage("IO exception!");
        } 
    } 

    public void signoff()
    {
        this.online = false;
        logMessage("Signing off.");
        try
        {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            stream.write(header((short) 0x438));
            stream.write(toBytes((short) 0x20));
            stream.write((new String("B_USER_DISCONNECTED\0")).getBytes());
            stream.write(toBytes((short) 0x5));
            this.socket.send(new DatagramPacket(stream.toByteArray(), stream.size(), serverAddy, SERVERPORT));
            stream.close();
            //tcpSocket.close();
        } 
        catch (IOException e)
        {
            logMessage("IO exception while trying to sign off!");
        } 
        // ebnet.icoff();
        this.socket.close();
        logMessage("Signed off.");
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

class ProgramDICQKeepAlive extends Thread
{
    ICQListener parent;

    public ProgramDICQKeepAlive(ICQListener parentListener)
    {
        this.parent = parentListener;
    } 

    public void run()
    {
        while (true)
        {
            try
            {
                this.parent.send(this.parent.header((short) 0x42E));
                sleep(120000);
            } 
            catch (IOException e)
            {
                // Please document this.
            } 
            catch (InterruptedException e)
            {
                // Please document this.
            } 
        } 
    } 
}