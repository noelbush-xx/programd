/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.listener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.logging.Level;

import org.aitools.programd.Core;
import org.aitools.programd.bot.Bot;
import org.aitools.programd.responder.TextResponder;

/**
 * This code is from the Everybuddy Java Project by Chris Carlin
 * (http://EBJava.sourceforge.net/) modified to work with a Program D server.
 * This needs more documentation.
 * 
 * @author Chris Carlin
 * @author Jon Baer
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @see <a href="http://EBJava.sourceforge.net/">Everybuddy </a>
 * @version 1.0
 */
public class ICQListener extends Listener
{
    private TextResponder responder;

    /** ? */
    private static final int SERVERPORT = 4000;

    /** ? */
    private static final String SERVER = "icq.mirabilis.com";

    /** ? */
    private static final String _ICQ = "_ICQ";

    /** ? */
    private String pass;

    /** ? */
    private int uin;

    /** ? */
    protected DatagramSocket socket;

    /** ? */
    private DatagramPacket packet;

    /** ? */
    private byte[] buffer;

    /** ? */
    private short seqNo = 1;

    /** ? */
    private static final short VERSION = (short) 2;

    /** ? */
    private static InetAddress serverAddy;

    /** ? */
    protected boolean online = false;

    /** ? */
    private int clientport;

    /** The label (as required by the registration scheme). */
    public static final String label = "ProgramD-ICQ";

    /** ? */
    private static final String MSG = "ProgramD-ICQ: ";

    /**
     * Creates a new ICQListener chat listener for a given bot.
     * 
     * @param coreToUse the Core object in use
     * @param botToListenFor the bot for whom to listen
     * @param parametersToUse the parameters for the listener and their default
     *            values
     * @throws InvalidListenerParameterException
     */
    public ICQListener(Core coreToUse, Bot botToListenFor, Map<String, String> parametersToUse) throws InvalidListenerParameterException
    {
        super(coreToUse, botToListenFor, parametersToUse);
        this.responder = new TextResponder();
        // Get parameters.
        try
        {
            this.uin = Integer.parseInt(this.parameters.get("number"));
        }
        catch (NumberFormatException e)
        {
            throw new InvalidListenerParameterException("Invalid user number (try a number!)");
        }
        this.pass = this.parameters.get("password");
    }

    /**
     * @see org.aitools.programd.listener.Listener#checkParameters()
     */
    public void checkParameters() throws InvalidListenerParameterException
    {
        // Check parameters.
        if (this.uin <= 0)
        {
            logMessage("Invalid user number; aborting.");
        }
        if (this.pass.length() == 0)
        {
            throw new InvalidListenerParameterException("Invalid empty password.");
        }
        this.clientport = 0;
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

            toICQ(new byte[] { (byte) 0x4c, (byte) 0x4 });

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
                        this.logger.log(Level.FINEST, "ICQListener: ICQ Command in: " + Integer.toHexString(this.buffer[2]) + " "
                                + Integer.toHexString(this.buffer[3]));
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

    /**
     * @see org.aitools.programd.util.ManagedProcess#shutdown()
     */
    public void shutdown()
    {
        signoff();
    }

    /**
     * Sends a message to the ICQ system.
     * 
     * @param msgBuffer the message to send
     * @throws IOException if there was a problem sending the message
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
        this.logger.log(Level.FINEST, MSG + "ICQ Command out: " + Integer.toHexString(msgBuffer[0]) + " " + Integer.toHexString(msgBuffer[1]));
        this.logger.log(Level.FINEST, "Buffer length: " + msgBuffer.length);
        if (msgBuffer.length > 2)
        {
            stream.write(msgBuffer, 2, msgBuffer.length - 2);
        }
        this.socket.send(new DatagramPacket(stream.toByteArray(), stream.size(), serverAddy, SERVERPORT));
    }

    /**
     * Handles a message from the ICQ system
     * 
     * @param msgBuffer the message to handle
     */
    private void fromICQ(byte[] msgBuffer)
    {
        String message;
        int from;

        if (msgBuffer[2] == (byte) 0x6E && msgBuffer[3] == (byte) 0x00)
        {
            from = ((msgBuffer[6]) & 0xFF) + ((msgBuffer[7] << 8) & 0xFF00) + ((msgBuffer[8] << 16) & 0xFF0000) + ((msgBuffer[9] << 24) & 0xFF000000);
            // String ip =
            // ((int)(buffer[10])&0xFF)+"."+((int)(buffer[11])&0xFF)+"."+((int)(buffer[12])&0xFF)+"."+((int)(buffer[13])&0xFF);
            // int port = ((buffer[14])&0xFF) + ((buffer[15]<<8)&0xFF00) +
            // ((buffer[16]<<16)&0xFF0000) + ((buffer[17]<<24)&0xFF000000);
            // ebnet.updateStat(Integer.toString(uin),"IC",true);
            return;
        }
        else if (msgBuffer[2] == (byte) 0x78 && msgBuffer[3] == (byte) 0x00)
        {
            from = ((msgBuffer[6]) & 0xFF) + ((msgBuffer[7] << 8) & 0xFF00) + ((msgBuffer[8] << 16) & 0xFF0000) + ((msgBuffer[9] << 24) & 0xFF000000);
            // ebnet.updateStat(Integer.toString(uin),"IC",false);
            return;
        }
        else if (msgBuffer[2] == (byte) 0xE6 && msgBuffer[3] == (byte) 0x00)
        {
            try
            {
                toICQ(new byte[] { (byte) 0x42, (byte) 0x4 });
            }
            catch (IOException e)
            {
                logMessage("IO Exception: " + e.getMessage());
            }
            return;
        }
        else if (msgBuffer[2] == (byte) 0xDC && msgBuffer[3] == (byte) 0x00)
        {
            from = ((msgBuffer[6]) & 0xFF) + ((msgBuffer[7] << 8) & 0xFF00) + ((msgBuffer[8] << 16) & 0xFF0000) + ((msgBuffer[9] << 24) & 0xFF000000);
            short length = (short) (((msgBuffer[18]) & 0xFF) + ((msgBuffer[19] << 8) & 0xFF00));
            message = new String(msgBuffer, 20, length - 1);
            logMessage("Message from [" + from + "]: " + message);
            return;
        }
        else if (msgBuffer[2] == (byte) 0x04 && msgBuffer[3] == (byte) 0x01)
        {
            from = ((msgBuffer[6]) & 0xFF) + ((msgBuffer[7] << 8) & 0xFF00) + ((msgBuffer[8] << 16) & 0xFF0000) + ((msgBuffer[9] << 24) & 0xFF000000);
            short length = (short) (((msgBuffer[12]) & 0xFF) + ((msgBuffer[13] << 8) & 0xFF00));
            message = new String(msgBuffer, 14, length - 1);
            logMessage("Message from [" + from + "]: " + message);
            if (message != null)
            {
                String response = this.core.getResponse(message, from + _ICQ, this.botID, this.responder);
                sendMesg(from, response);
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
     * @param x the short to convert
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
     * @param x the int to convert
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

    /**
     * ?
     * 
     * @param command ?
     * @return ?
     * @throws IOException ?
     */
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

    /**
     * Processes an acknowledgement from the server
     * 
     * @param a ?
     * @param b ?
     * @throws IOException ?
     */
    public void ack(byte a, byte b) throws IOException
    {
        this.logger.log(Level.FINE, MSG + "Acknowledgement from server!");
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write(toBytes(VERSION));
        stream.write(toBytes((short) 0xA));
        stream.write(a);
        stream.write(b);
        stream.write(toBytes(this.uin));
        this.socket.send(new DatagramPacket(stream.toByteArray(), stream.size(), serverAddy, SERVERPORT));
    }

    /**
     * Sends a message to the ICQ system.
     * 
     * @param msgBuffer the message to send
     * @throws IOException if there is a problem sending the message
     */
    public void send(byte[] msgBuffer) throws IOException
    {
        this.socket.send(new DatagramPacket(msgBuffer, msgBuffer.length, serverAddy, SERVERPORT));
    }

    /**
     * Sends a message to the ICQ system.
     * 
     * @param to the UIN to whom to send the message
     * @param mesg the message to send
     */
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

    /**
     * 
     */
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
            if (this.socket != null && serverAddy != null)
            {
                this.socket.send(new DatagramPacket(stream.toByteArray(), stream.size(), serverAddy, SERVERPORT));
            }
            stream.close();
            // tcpSocket.close();
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
     * @param message the message
     */
    private void logMessage(String message)
    {
        this.logger.log(Level.INFO, MSG + message);
    }
}

class ProgramDICQKeepAlive extends Thread
{
    ICQListener parent;

    /**
     * Creates a new ProgramDICQKeepAlive thread
     * 
     * @param parentListener the parent listener
     */
    public ProgramDICQKeepAlive(ICQListener parentListener)
    {
        this.parent = parentListener;
    }

    /**
     * @see java.lang.Thread#run()
     */
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