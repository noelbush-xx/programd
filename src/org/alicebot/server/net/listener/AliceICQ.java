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
    - added initialize method and made implementation of AliceChatListener
    - changed some server property names
*/

/*
    4.1.4 [00] - December 2001, Noel Bush
    - made it work :-)
*/

package org.alicebot.server.net.listener;

import java.io.ByteArrayOutputStream;
import java.io.InterruptedIOException;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Properties;

import org.alicebot.server.core.ActiveMultiplexor;
import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.responder.TextResponder;
import org.alicebot.server.core.util.Toolkit;
import org.alicebot.server.core.util.Trace;

/**
 *  This code is from the Everybuddy Java Project
 *  by Chris Carlin (http://EBJava.sourceforge.net/)
 *  modified to work with an Alicebot server.
 *
 *  @author Chris Carling
 *  @author Jon Baer
 *  @author Noel Bush
 *
 *  @see <a href="http://EBJava.sourceforge.net/">http://EBJava.sourceforge.net/</a>
 *  @version 1.0
 */
public class AliceICQ implements AliceChatListener
{
    private static final int SERVERPORT = 4000;
    private static final String  SERVER = "icq.mirabilis.com";
    private String pass;
    private int    uin;
    protected DatagramSocket socket;
    private DatagramPacket packet;
    private byte[] buffer;
    private short seqNo = 1;
    private static final short  VERSION = (short)2;
    private static InetAddress serverAddy;
    private boolean online = false;
    private int clientport;

    
    public boolean initialize(Properties properties)
    {
        // Check if enabled.
        if (Boolean.valueOf(properties.getProperty("programd.listeners.icq.enabled", "false")).booleanValue())
        {
            // Get parameters.
            try
            {
                uin = Integer.parseInt(properties.getProperty("programd.listeners.icq.number", ""));
            }
            catch (NumberFormatException e)
            {
                Log.userinfo("AliceICQ: Invalid user number (try a number!); aborting.", Log.LISTENERS);
                return false;
            }
            pass = properties.getProperty("programd.listeners.icq.password", "");

            // Check parameters.
            if (uin <= 0)
            {
                Log.userinfo("AliceICQ: Invalid user number; aborting.", Log.LISTENERS);
            }
            if (pass.length() == 0)
            {
                Log.userinfo("AliceICQ: Invalid empty password; aborting.", Log.LISTENERS);
                return false;
            }
            clientport = 0;
            return true;
        }
        else
        {
            return false;
        }
    }


    /**
     *  Creates a new AliceICQ chat listener.
     */
    public AliceICQ()
    {
    }
    

    /**
     *  Please document this.
     */
    public void run()
    {
        try
        {
            socket = new DatagramSocket();
            serverAddy = InetAddress.getByName(SERVER);
            Log.userinfo("AliceICQ: logging in to " + serverAddy, Log.LISTENERS);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            
            stream.write(header((short)0x3E8));
            stream.write(toBytes(clientport));
            stream.write(toBytes((short)(pass.length()+1)));
            stream.write((new String(pass+"\0")).getBytes());
            stream.write(toBytes(0x78));
            stream.write(InetAddress.getByName("localhost").getAddress());
            stream.write(0x4);
            stream.write(toBytes(0));
            stream.write(toBytes(0x2));
            stream.write(toBytes((short)0x3E));
            stream.write(toBytes(0));
            stream.write(toBytes(0x780000));
            socket.send(new DatagramPacket(stream.toByteArray(),stream.size(),serverAddy,SERVERPORT));
            stream.close();
            
            buffer = new byte[10];
            packet = new DatagramPacket(buffer,buffer.length);
            socket.receive(packet);
            buffer = packet.getData();
            if (buffer[2] != (byte)0x0A || buffer[3] != (byte)0x00)
            {
                if (buffer[2] == (byte)0x5A && buffer[3] == (byte)0x00)
                {
                    ack(buffer[4],buffer[5]);
                }
                else
                {
                    Log.userinfo("AliceICQ: No acknowledgement from server; aborting.", Log.LISTENERS); 
                    return;
                }
            }
            socket.receive(packet);
            buffer = packet.getData();
            if (buffer[2] == (byte)0x5A && buffer[3] == (byte)0x00)
            {
                ack(buffer[4],buffer[5]);
            }
            else
            {
                if (buffer[2] != (byte)0x0A || buffer[3] != (byte)0x00)
                {
                    Log.userinfo("AliceICQ: No Login Reply: "+(byte)buffer[2]+" "+ (byte)buffer[3], Log.LISTENERS); 
                    return;
                }
            }
            online = true;
            Log.userinfo("AliceICQ: Successfully logged on.", Log.LISTENERS);
            
            toICQ(new byte[] {(byte)0x4c,(byte)0x4});

            AliceICQKeepAlive keepAlive = new AliceICQKeepAlive(this);
            keepAlive.setDaemon(true);
            keepAlive.start();
            socket.setSoTimeout(2000); 
            while(true)
            {
                try
                {
                    buffer = new byte[512];
                    packet = new DatagramPacket(buffer,buffer.length);
                    socket.receive(packet);
                    buffer=packet.getData();
                   
                    if ((buffer[0] == (byte)0x2 && buffer[1] == (byte)0x0) && !(buffer[2] == (byte)0xA && buffer[3] == (byte)0x0)) 
                    {
                        fromICQ(buffer);
                        ack(buffer[4],buffer[5]);
                        Log.devinfo("AliceICQ: ICQ Command in: " +
                            Integer.toHexString((int)buffer[2]) + " " + Integer.toHexString((int)buffer[3]), Log.LISTENERS);
                    }
                }
                catch (InterruptedIOException e)
                {
                }
            }
        }
        catch (UnknownHostException e)
        {
            Log.userinfo("AliceICQ: Unknown host!", Log.LISTENERS);
        }
        catch (SocketException e)
        {
            Log.devinfo("AliceICQ: Socket exception!", Log.LISTENERS);}
        catch (IOException e)
        {
            Log.devinfo("AliceICQ: IO Exception!", Log.LISTENERS);
        }
        signoff();
    }
    

    public void shutdown()
    {
        signoff();
    }


    /**
     *  Please document this.
     */
    public void toICQ(byte[] buffer) throws IOException
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write(toBytes((short)0x2));
        stream.write(buffer[0]);
        stream.write(buffer[1]);
        stream.write(toBytes(seqNo));
        stream.write(toBytes(uin));
        seqNo = (short)((seqNo + 1) & 0xFFFF);
        Log.devinfo("AliceICQ: ICQ Command out: " +
            Integer.toHexString((int)buffer[0]) + " " + Integer.toHexString((int)buffer[1]), Log.LISTENERS);
        Trace.devinfo("Buffer length: " + buffer.length);
        if (buffer.length > 2)
        {
            stream.write(buffer, 2, buffer.length - 2);
        }
        socket.send(new DatagramPacket(stream.toByteArray(), stream.size(), serverAddy, SERVERPORT));
    }    


    /**
     *  Please document this.
     */
    private void fromICQ(byte[] buffer)
    {
        String message;
        int from;

        if (buffer[2] == (byte)0x6E && buffer[3] == (byte)0x00)
        {
            from = ((buffer[6])&0xFF) +((buffer[7]<<8)&0xFF00) +((buffer[8]<<16)&0xFF0000) +((buffer[9]<<24)&0xFF000000);
            String ip  = ((int)(buffer[10])&0xFF)+"."+((int)(buffer[11])&0xFF)+"."+((int)(buffer[12])&0xFF)+"."+((int)(buffer[13])&0xFF);
            int port= ((buffer[14])&0xFF) + ((buffer[15]<<8)&0xFF00) + ((buffer[16]<<16)&0xFF0000) + ((buffer[17]<<24)&0xFF000000);
            // ebnet.updateStat(Integer.toString(uin),"IC",true);
                return;
        }
        else if (buffer[2] == (byte)0x78 && buffer[3] == (byte)0x00)
        {
            from = ((buffer[6])&0xFF) +((buffer[7]<<8)&0xFF00) +((buffer[8]<<16)&0xFF0000) +((buffer[9]<<24)&0xFF000000);
            // ebnet.updateStat(Integer.toString(uin),"IC",false);
            return;
        }
        else if (buffer[2] == (byte)0xE6 && buffer[3] == (byte)0x00)
        {
            try
            {
                toICQ(new byte[] {(byte)0x42,(byte)0x4});
            }
            catch (IOException e)
            {
                Log.userinfo("AliceICQ IO Exception: " + e.getMessage(), Log.LISTENERS);
            }
            return;
        }
        else if (buffer[2] == (byte)0xDC && buffer[3] == (byte)0x00)
        {
            from = ((buffer[6])&0xFF) +((buffer[7]<<8)&0xFF00) +((buffer[8]<<16)&0xFF0000) +((buffer[9]<<24)&0xFF000000);
            short length = (short)(((buffer[18])&0xFF)+((buffer[19]<<8)&0xFF00));
            message = new String(buffer, 20, length - 1);
            Log.userinfo("AliceICQ: Message from " + from + ": ", Log.LISTENERS);
            Log.userinfo("AliceICQ: [" + from + "]" + message, Log.LISTENERS);
            // message = ebnet.snLookup(Integer.toString(uin),3) + "\0" + message;
            // ebnet.send("MESG",message.getBytes());
            return;
        }
        else if (buffer[2] == (byte)0x04 && buffer[3] == (byte)0x01)
        {
            from = ((buffer[6])&0xFF) +((buffer[7]<<8)&0xFF00) +((buffer[8]<<16)&0xFF0000) +((buffer[9]<<24)&0xFF000000);
            short length = (short)(((buffer[12])&0xFF)+((buffer[13]<<8)&0xFF00));
            message = new String(buffer, 14, length - 1);
            Log.userinfo("AliceICQ: Message from " + from + ": ", Log.LISTENERS);
            Log.userinfo("AliceICQ: [" + from + "] " + message, Log.LISTENERS);
            if (message != null)
            {
                String botResponse = ActiveMultiplexor.getInstance().getResponse(message, from + "_ICQ", new TextResponder());
                sendMesg(from, botResponse);
            }
            // message = ebnet.snLookup(Integer.toString(uin),3) + "\0" + message;
            // ebnet.send("MESG",message.getBytes());
            return;
        }
        else
        {
            return;
        }
    }


    public static byte[] toBytes(short x)
    {
        byte[] b = new byte[2];
        b[0] = (byte)(x&255);
        b[1] = (byte)((x>>8)&255);
        return b;
    }
    

    public static byte[] toBytes(int x)
    {
        byte[] b = new byte[4];
        b[0] = (byte)(x&255);
        b[1] = (byte)((x>>8)&255);
        b[2] = (byte)((x>>16)&255);
        b[3] = (byte)((x>>24)&255);
        return b;
    }


    public byte[] header(short command) throws IOException
    {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write(toBytes((short)0x2));
        stream.write(toBytes(command));
        stream.write(toBytes(seqNo));
        stream.write(toBytes(uin));
        seqNo = (short)((seqNo + 1)&0xFFFF);
        return stream.toByteArray();
    }
    

    public void ack(byte a,byte b) throws IOException
    {
        Log.devinfo("AliceICQ: Acknowledgement from server!", Log.LISTENERS);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write(toBytes(VERSION));
        stream.write(toBytes((short)0xA));
        stream.write(a);
        stream.write(b);
        stream.write(toBytes(uin));
        socket.send(new DatagramPacket(stream.toByteArray(),stream.size(),serverAddy,SERVERPORT));
    }


    public void send(byte[] buffer) throws IOException
    {
        socket.send(new DatagramPacket(buffer, buffer.length, serverAddy, SERVERPORT));
    }


    public void sendMesg(int to, String mesg)
    {
        Log.userinfo("AliceICQ response to [" + to + "]: " + mesg, Log.LISTENERS);
        try
        {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            stream.write((byte)0x0E);
            stream.write((byte)0x01);
            stream.write(toBytes(to));
            stream.write((byte)0x01);
            stream.write((byte)0x00);
            stream.write(toBytes((short)mesg.length()));
            stream.write(mesg.getBytes());
            stream.write((byte)'\0');
            toICQ(stream.toByteArray());
        }
        catch (IOException e)
        {
            Log.userinfo("AliceICQ: IO exception!", Log.LISTENERS);
        }
    }

    
    public void signoff()
    {
        online = false;
        Log.userinfo("AliceICQ signing off.", Log.LISTENERS);
        try
        {
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            stream.write(header((short)0x438));
            stream.write(toBytes((short)0x20));
            stream.write((new String("B_USER_DISCONNECTED\0")).getBytes());
            stream.write(toBytes((short)0x5));
            socket.send(new DatagramPacket(stream.toByteArray(),stream.size(),serverAddy,SERVERPORT));
            stream.close();
            //tcpSocket.close();
        }
        catch (IOException e)
        {
            Log.userinfo("AliceICQ: IO exception while trying to sign off!", Log.LISTENERS);
        }
        // ebnet.icoff();
        socket.close();
        Log.userinfo("AliceICQ signed off.", Log.LISTENERS);
    }
}


class AliceICQKeepAlive extends Thread
{
    AliceICQ parent;

    public AliceICQKeepAlive(AliceICQ parent)
    {
        this.parent = parent;
    }

    public void run()
    {
        Log.userinfo("AliceICQ started.", Log.LISTENERS);
        while (true)
        {
            try
            {
                parent.send(parent.header((short)0x42E));
                sleep(120000);
            }
            catch (IOException e)
            {
            }
            catch (InterruptedException e)
            {
            }    
        }
    }
}