package org.alicebot.server.net.listener;

/**

ALICEBOT.NET Artificial Intelligence Project
This version is Copyright (C) 2000 Jon Baer.
jonbaer@digitalanywhere.com
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
notice, this list of conditions, and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright
notice, this list of conditions, and the disclaimer that follows 
these conditions in the documentation and/or other materials 
provided with the distribution.

3. The name "ALICEBOT.NET" must not be used to endorse or promote products
derived from this software without prior written permission.  For
written permission, please contact license@alicebot.org.

4. Products derived from this software may not be called "ALICEBOT.NET",
nor may "ALICEBOT.NET" appear in their name, without prior written permission
from the ALICEBOT.NET Project Management (jonbaer@alicebot.net).

In addition, we request (but do not require) that you include in the 
end-user documentation provided with the redistribution and/or in the 
software itself an acknowledgement equivalent to the following:
"This product includes software developed by the
ALICEBOT.NET Project (http://www.alicebot.net)."
Alternatively, the acknowledgment may be graphical using the logos 
available at http://www.alicebot.org/images/logos.

THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED.  IN NO EVENT SHALL THE ALICE SOFTWARE FOUNDATION OR
ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

This software consists of voluntary contributions made by many 
individuals on behalf of the A.L.I.C.E. Nexus and ALICEBOT.NET Project
and was originally created by Dr. Richard Wallace <drwallace@alicebot.net>.

This version was created by Jon Baer <jonbaer@alicebot.net>.

http://www.alicebot.org
http://www.alicebot.net

This version contains open-source technologies from:
Netscape, Apache, HypersonicSQL, JDOM, Jetty, Chris Carlin, IBM

*/


import java.io.*;
import java.net.*;
import java.util.*;

import org.alicebot.server.core.*;
import org.alicebot.server.core.responder.*;

/**
 * Alicebot ICQ Chat Listener
 *
 * This code is from the Everybuddy Java Project
 * by Chris Carlin (http://EBJava.sourceforge.net/)
 * modified to work with an Alicebot server.
 *
 * Not working at this time. =(
 *
 * @author Jon Baer
 * @version 1.0
 */

public class AliceICQ extends Thread {
    
    private static final int SERVERPORT = 33338;
    private static final String  SERVER = "login.icq.com";
    private String pass;
    private int    uin;
    protected DatagramSocket socket;
    private DatagramPacket packet;
    private byte[] buffer;
    private short seqNo = 1;
    private static final short  VERSION = (short)2;
    private static InetAddress serverAddy;
    private boolean online = false;
    //private ServerSocket tcpSocket;
    private int clientport;
    
    public AliceICQ(String name,String pass) {
	this.uin = Integer.parseInt(name);
	this.pass = pass;
	//this.tcpSocket = getTCPSocket(1200);
        //this.clientport = tcpSocket.getLocalPort();
        this.clientport = 0;
		start();
    }
    
    public void run() {
	try {
	    socket = new DatagramSocket();
	    serverAddy = InetAddress.getByName(SERVER);
	    System.out.println(serverAddy);
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
	    if (buffer[2] != (byte)0x0A || buffer[3] != (byte)0x00) {
		if (buffer[2] == (byte)0x5A && buffer[3] == (byte)0x00)
		    ack(buffer[4],buffer[5]);
		else {
		    System.out.println("No ack"); 
		    return;
		}
	    }

	    socket.receive(packet);
	    buffer = packet.getData();
	    if (buffer[2] == (byte)0x5A && buffer[3] == (byte)0x00) 
		ack(buffer[4],buffer[5]);
	    else {
		if (buffer[2] != (byte)0x0A || buffer[3] != (byte)0x00) {
		    System.out.println("No Login Reply: "+(byte)buffer[2]+" "+ (byte)buffer[3]); 
		    return;
		}
	    }


	    
	    online = true;
	    System.out.println("Done with ICQ logon");
	    //
	    
	    toICQ(new byte[] {(byte)0x4c,(byte)0x4});

	    // sendBuddies();
	    
	    (new AliceICQKeepAlive(this)).start();
	    socket.setSoTimeout(2000); 
	    while(true) {
		try {
		    buffer = new byte[512];
		    packet = new DatagramPacket(buffer,buffer.length);
		    socket.receive(packet);
		    buffer=packet.getData();
		   
		    if ((buffer[0] == (byte)0x2 && buffer[1] == (byte)0x0) && !(buffer[2] == (byte)0xA && buffer[3] == (byte)0x0)) 
			{
			    fromICQ(buffer);
			    ack(buffer[4],buffer[5]);
			    System.out.println("ICQ Command in: "+ Integer.toHexString((int)buffer[2])+" "+Integer.toHexString((int)buffer[3]));
			}
		}
		catch (InterruptedIOException e) {}
	    }
	}
	catch (UnknownHostException  e) {System.out.println("UHE");}
	catch (SocketException e) {System.out.println("SE");}
	catch (IOException e) {System.out.println("IOE");} 
	System.out.println("Exited AliceICQ while");
	signoff();
    }
    
    public void toICQ(byte[] buffer) throws IOException {
	ByteArrayOutputStream stream = new ByteArrayOutputStream();
	stream.write(toBytes((short)0x2));
	stream.write(buffer[0]);
	stream.write(buffer[1]);
	stream.write(toBytes(seqNo));
	stream.write(toBytes(uin));
	seqNo = (short)((seqNo + 1) & 0xFFFF);
	System.out.println("ICQ Command out: "+ Integer.toHexString((int)buffer[0])+" "+Integer.toHexString((int)buffer[1]));
	//(int)((buffer[0])+(buffer[1]<<8)+(((byte)(0x69))<<16)+(((byte)(0x11))<<24)));
	if (buffer.length > 2) {
	    stream.write(buffer,2,buffer.length-2);
	}
	socket.send(new DatagramPacket(stream.toByteArray(),stream.size(),serverAddy,SERVERPORT));
    }    

    private void fromICQ(byte[] buffer) {	
	if (buffer[2] == (byte)0xDC && buffer[3] == (byte)0x00) {
	    System.out.println("ICQ Message in");
	    int uin = ((buffer[6])&0xFF) +((buffer[7]<<8)&0xFF00) +((buffer[8]<<16)&0xFF0000) +((buffer[9]<<24)&0xFF000000);
	    short length = (short)(((buffer[18])&0xFF)+((buffer[19]<<8)&0xFF00));
	    String message = new String(buffer,20,length-1);
	    // message = ebnet.snLookup(Integer.toString(uin),3) + "\0" + message;
	   //  ebnet.send("MESG",message.getBytes());
	    return;
	}
	if (buffer[2] == (byte)0x04 && buffer[3] == (byte)0x01) {
	    System.out.print("ICQ Message in: ");
	    int uin = ((buffer[6])&0xFF) +((buffer[7]<<8)&0xFF00) +((buffer[8]<<16)&0xFF0000) +((buffer[9]<<24)&0xFF000000);
	    short length = (short)(((buffer[12])&0xFF)+((buffer[13]<<8)&0xFF00));
	    String message = new String(buffer,14,length-1);
	    System.out.println(message);
	    // message = ebnet.snLookup(Integer.toString(uin),3) + "\0" + message;
	    // ebnet.send("MESG",message.getBytes());
	    return;
	}
	if (buffer[2] == (byte)0x6E && buffer[3] == (byte)0x00) {
 	    int uin = ((buffer[6])&0xFF) +((buffer[7]<<8)&0xFF00) +((buffer[8]<<16)&0xFF0000) +((buffer[9]<<24)&0xFF000000);
	    String ip  = ((int)(buffer[10])&0xFF)+"."+((int)(buffer[11])&0xFF)+"."+((int)(buffer[12])&0xFF)+"."+((int)(buffer[13])&0xFF);
	    int port= ((buffer[14])&0xFF) + ((buffer[15]<<8)&0xFF00) + ((buffer[16]<<16)&0xFF0000) + ((buffer[17]<<24)&0xFF000000);
	    // ebnet.updateStat(Integer.toString(uin),"IC",true);
            return;
	}
	if (buffer[2] == (byte)0x78 && buffer[3] == (byte)0x00) {
	    int uin = ((buffer[6])&0xFF) +((buffer[7]<<8)&0xFF00) +((buffer[8]<<16)&0xFF0000) +((buffer[9]<<24)&0xFF000000);
	    // ebnet.updateStat(Integer.toString(uin),"IC",false);
            return;
	}
	if (buffer[2] == (byte)0xE6 && buffer[3] == (byte)0x00) {
	    try {
		toICQ(new byte[] {(byte)0x42,(byte)0x4});
	    }
	    catch (IOException e) {System.out.println(e);}
	}
    }

    public static byte[] toBytes(short x) {
	byte[] b = new byte[2];
	b[0] = (byte)(x&255);
	b[1] = (byte)((x>>8)&255);
	return b;
    }
    
    public static byte[] toBytes(int x) {
	byte[] b = new byte[4];
	b[0] = (byte)(x&255);
	b[1] = (byte)((x>>8)&255);
	b[2] = (byte)((x>>16)&255);
	b[3] = (byte)((x>>24)&255);
	return b;
    }

    public byte[] header(short command) throws IOException {
	ByteArrayOutputStream stream = new ByteArrayOutputStream();
	stream.write(toBytes((short)0x2));
	stream.write(toBytes(command));
	stream.write(toBytes(seqNo));
	stream.write(toBytes(uin));
	seqNo = (short)((seqNo + 1)&0xFFFF);
	return stream.toByteArray();
    }
	
    public void ack(byte a,byte b) throws IOException {
	System.out.println("Ack!");
	ByteArrayOutputStream stream = new ByteArrayOutputStream();
	stream.write(toBytes(VERSION));
	stream.write(toBytes((short)0xA));
	stream.write(a);
	stream.write(b);
	stream.write(toBytes(uin));
	socket.send(new DatagramPacket(stream.toByteArray(),stream.size(),serverAddy,SERVERPORT));
    }

    /* private ServerSocket getTCPSocket(int i) {
	ServerSocket tmpsocket;
	try {
	    tmpsocket = new ServerSocket(i);
	}
	catch (IOException e) {
	    tmpsocket = getTCPSocket(i++);
	}
	return tmpsocket;
    }
     * /

    /*  public void fromChannel(byte[] buffer) {
	byte[] out = new byte[buffer.length+2];
	out[0]=(byte)0x00;
	out[1]=(byte)0x00;
	for (int i=0;i<buffer.length;i++) {
	out[2+i]=buffer[i];
	}
	ebnet.send("IC",out);
	}*/
    
    public void send(byte[] buffer) throws IOException {
	socket.send(new DatagramPacket(buffer,buffer.length,serverAddy,SERVERPORT));
    }

	/*
    private void sendBuddies() throws IOException {
	byte[] buffer;
	int[] uins = new int[ebnet.buddyStruct.size()];
	short next = (short)0;
	for (int i=0;i<uins.length;i++) {
	    if(((String[])ebnet.buddyStruct.elementAt(i))[3] != null) {
		uins[next] = Integer.parseInt(((String[])ebnet.buddyStruct.elementAt(i))[3]);
		next++;
	    } 
	}
	buffer = new byte[(next*4)+3];
	buffer[0] = (byte)0x06;
	buffer[1] = (byte)0x04;
	buffer[2] = (byte)(next&255);
	int count2 = 4;
	for (int i=0;i<next;i++) {
	    int tmp=uins[i];
	    buffer[3+4*i] = ((byte)(tmp&255));
	    buffer[4+4*i] = ((byte)((tmp>>8)&255));
	    buffer[5+4*i] = ((byte)((tmp>>16)&255));
	    buffer[6+4*i] = ((byte)((tmp>>24)&255));
	}
	toICQ(buffer);
    }
	*/
	
	public static void main(String[] args) {
		AliceICQ icq = new AliceICQ("108588920", "airwalk");
	}
    
    public void sendMesg(String to, String mesg) {
	int uin = Integer.parseInt(to);
	try {
	    ByteArrayOutputStream stream = new ByteArrayOutputStream();
	    stream.write((byte)0x0E);
	    stream.write((byte)0x01);
	    stream.write(toBytes(uin));
	    stream.write((byte)0x01);
	    stream.write((byte)0x00);
	    stream.write(toBytes((short)mesg.length()));
	    stream.write(mesg.getBytes());
	    stream.write((byte)'\0');
	    toICQ(stream.toByteArray());
	}
	catch (IOException e) {}
    }

	
    public void signoff() {
	online = false;
	System.out.println("Closing IC");
	try {
	    ByteArrayOutputStream stream = new ByteArrayOutputStream();
	    stream.write(header((short)0x438));
	    stream.write(toBytes((short)0x20));
	    stream.write((new String("B_USER_DISCONNECTED\0")).getBytes());
	    stream.write(toBytes((short)0x5));
	    socket.send(new DatagramPacket(stream.toByteArray(),stream.size(),serverAddy,SERVERPORT));
	    stream.close();
	    //tcpSocket.close();
	}
	catch (IOException e) {}
	// ebnet.icoff();
	socket.close();

    }

} //class

class AliceICQKeepAlive extends Thread {
    AliceICQ parent;

    public AliceICQKeepAlive(AliceICQ parent) {
	this.parent = parent;
    }

    public void run() {
	System.out.println("Start AliceICQKeepAlive");
	while (true) {
	    try {
		System.out.println("Timeout! Goooood");
		parent.send(parent.header((short)0x42E));
		sleep(120000);
	    }
	    catch (IOException e){}
	    catch (InterruptedException e) {}	
	}
    }

}
	    



/*class TCPListen extends Thread {
    private boolean online = true;
    private Vector channels;
    public void run(ServerSocket socket,AliceICQ icnet) {
	try {
	    socket.setSoTimeout(3000);
	}
	catch (SocketException e) {System.out.println(e);}
	while (online) {
	    try {
		Socket csocket = socket.accept();
		csocket.setSoTimeout(3000);
		ICChannel tmp = new ICChannel(csocket,icnet);
		channels.add(tmp);
		tmp.start();
	    }
	    catch(IOException e){}
	}
    }

    public void offline() {
	online = false;
	for (int i=0;i<channels.size();i++) {
	    ((ICChannel)channels.elementAt(i)).offline();
	}
    }
}

class ICChannel extends Thread {
    private boolean online = true;
    DataOutputStream outStream;
    DataInputStream in;
    Socket socket;
    AliceICQ icnet;

    public ICChannel(Socket socket,AliceICQ icnet) {
	this.socket = socket;
	this.icnet = icnet;
	try {
	    in = new DataInputStream(socket.getInputStream());
	    outStream = new DataOutputStream(socket.getOutputStream());
	}
	catch (IOException e) {}
    }

    public void run() {
	while (online) {
	    try {
		short length = in.readShort();
		byte[] buffer = new byte[length-2];
		in.readFully(buffer);
		icnet.fromChannel(buffer);
	    }
	    catch (IOException e) {}
	}
    }

    public void offline() {
	try {
	    online = false;
	    outStream.flush();
	    outStream.close();
	    in.close();
	    socket.close();
	}
	catch (IOException e) {}
    }
}
*/
