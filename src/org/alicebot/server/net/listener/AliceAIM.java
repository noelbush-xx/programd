package org.alicebot.server.net.listener;

/**
Alice Program D
Copyright (C) 1995-2001, A.L.I.C.E. AI Foundation

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
USA.

@author  Richard Wallace
@author  Jon Baer
@author  Thomas Ringate/Pedro Colla
@version 4.1.1
*/


import java.io.*;
import java.net.*;
import java.util.*;
import java.lang.*;
import java.lang.String.*;

import org.alicebot.server.core.*;
import org.alicebot.server.net.html.*;
import org.alicebot.server.core.responder.*;

/**
 * Alicebot AIM Chat Listener
 *
 * This code is from the Everybuddy Java Project
 * by Chris Carlin (http://EBJava.sourceforge.net/)
 * modified to work with an Alicebot server.
 *
 * @author Jon Baer
 * @author Sandy McArthur
 * @version 1.0
 */

public class AliceAIM extends Thread {
	
	private static final int MAX_SEQ = 65535;
	private static final String HOST    =  "toc.oscar.aol.com";
	private static final int PORT       = 21;
	private String name, pass, bgcolor, fontface, fontsize, fontcolor, owner, buddies, message;
	private int seqNo;
	private Socket connection;
	private DataInputStream in;
	private DataOutputStream out;
	private boolean online;
	
	private final String ROAST = "Tic/Toc";
	
	public AliceAIM(String owner, String name, String pass, String bgcolor, String fontface, String fontsize, String fontcolor, String buddies, String message) {
		this.owner = owner;
		this.name = name;
		this.pass = pass;
		this.bgcolor = bgcolor;
		this.fontface = fontface;
		this.fontsize = fontsize;
		this.fontcolor = fontcolor;
		this.buddies = buddies;
		this.message = message;
		start();
	}
	
	public void run() {
		int length;
		seqNo = (int)Math.floor(Math.random() * 65535.0);
		System.out.println("*** Starting Alicebot AIM Listener ***");
		try
		{
			connection = new Socket(HOST, PORT);
			connection.setSoTimeout(10000);
			in = new DataInputStream(connection.getInputStream());
			out = new DataOutputStream(new BufferedOutputStream(connection.getOutputStream()));
		}
		catch (Exception e)
		{
			signoff("1");
			return;
		}
		try
		{
			out.writeBytes("FLAPON\r\n\r\n");
			out.flush();
			byte[] signon = new byte[10];
			in.readFully(signon);	
			out.writeByte(42);
			out.writeByte(1);
			out.writeShort(seqNo);
			seqNo = (seqNo +  1) & 65535;
			out.writeShort(name.length()+8);
			out.writeInt(1);
			out.writeShort(1);
			out.writeShort(name.length());
			out.writeBytes(name);
			out.flush();
			
			frameSend("toc_signon login.oscar.aol.com 5190 "+name+" "+ imRoast(pass)+" english alicebot\0");
			
			in.skip(4);
			length = in.readShort();
			signon = new byte[length];
			in.readFully(signon);
			if (String.valueOf(signon).startsWith("ERROR")) {
				// ebnet.send("ERRR",(new String("Signon err:dunno")).getBytes());
				System.out.println("Signon error");
				signoff("2");
				return;
			}
			in.skip(4);
			length = in.readShort();
			signon = new byte[length];
			in.readFully(signon);
			online = true;
			
			sendBuddies();
			
			frameSend("toc_init_done\0");
			
			// // ebnet.imon();
			System.out.println("Done with AIM logon");
			connection.setSoTimeout(3000);
		}
		catch (InterruptedIOException e) 
		{
			online = false;
			signoff("2.5");
		}
		catch (IOException e)
		{
			online = false;
			signoff("3");
		}
		byte[] data;
		while (true) {
			try {
				in.skip(4);
				length = in.readShort();
				data = new byte[length];
				in.readFully(data);
				fromAIM(data);
			}
			catch (InterruptedIOException e) {
				//System.out.println("*** AIM ERROR: " + e + " ***");
			}
			catch (IOException e) {
				//System.out.println("*** AIM ERROR: " + e + " ***");
				break;
			}
		}
		signoff("4");
	}
	
	public void frameSend(String toBeSent) throws IOException {
		out.writeByte(42);
		out.writeByte(2);
		out.writeShort(seqNo);
		seqNo = (seqNo + 1) & 65535;
		out.writeShort(toBeSent.length());
		out.writeBytes(toBeSent);
		out.flush();
	}
	
	public void fromAIM(byte[] buffer) {
		
		String inString = new String(buffer);
		
		System.out.println("*** AIM: " + inString + " ***");
		
		StringTokenizer inToken = new StringTokenizer(inString,":");
		String command = inToken.nextToken();
		if (command.equals("IM_IN")) {	    
			String from = imNormalize(inToken.nextToken());
			String auto = inToken.nextToken();
			String mesg = inToken.nextToken();
			while (inToken.hasMoreTokens()) {
				mesg = mesg +":"+ inToken.nextToken();
			}
			String request = stripHTML(mesg);
			System.out.println("*** AIM MESSAGE: " + from + " > " + request + " ***");
			// mesg = ebnet.snLookup(from,2)+"\0"+mesg;
			// ebnet.send("MESG",mesg.getBytes());
			if (request.startsWith("$SENDIM") && this.owner.equals(from)) {
				StringTokenizer st = new StringTokenizer(request);
				String imcommand = st.nextToken();
				String imcommandTo = st.nextToken();
				String imcommandText = st.nextToken();
				sendMesg(imcommandTo, imcommandText);
			} else {
				String bot_response = Classifier.doResponse(request, from+"_AIM", new AIMResponder());
				sendMesg(from, bot_response);
			}
			return;
		}  	
		if (command.equals("CHAT_IN")) {
			String room_id = imNormalize(inToken.nextToken());
			String from = imNormalize(inToken.nextToken());
			String mesg = inToken.nextToken();
			while (inToken.hasMoreTokens()) {
				mesg = mesg +":"+ inToken.nextToken();
			}
			String request = stripHTML(mesg);
			if (request.indexOf(this.name) > 0) {
				String bot_response = Classifier.doResponse(request, from+"_AIM", new AIMResponder());
				sendChatRoomMesg(room_id, bot_response);
			}
			return;
		}
		if (command.equals("UPDATE_BUDDY")) {
			String name = imNormalize(inToken.nextToken());
			boolean stat = false;
			if (inToken.nextToken().equals("T")) stat = true;	
			// // ebnet.updateStat(name,"IM",stat);
			return;
		}
		if (command.equals("ERROR")) {
			String error = inToken.nextToken();
			System.out.println("*** AIM ERROR: " + error + " ***");
			if (error.equals("901")) {
				// ebnet.send("ERRR",(" not currently available").getBytes());
				return;
			}
			if (error.equals("903")) {
				// ebnet.send("ERRR",("Message dropped, sending too fast").getBytes());
				return;
			}
			if (error.equals("960")) {
				// ebnet.send("ERRR",("Sending messages too fast to "+inToken.nextToken()).getBytes());
				return;
			}
			if (error.equals("961")) {
				// ebnet.send("ERRR",(inToken.nextToken() +" sent you too big a message").getBytes());
				return;
			}
			if (error.equals("962")) {
				// ebnet.send("ERRR",(inToken.nextToken() +" sent you a message too fast").getBytes());
				return;
			}
			if (error.equals("Signon err")) {
				// ebnet.send("ERRR",("AIM Signon failure: "+inToken.nextToken()).getBytes());
				signoff("5");
			}
			return;
		}
	}
	
	public void sendMesg(String to, String text) {
		text = "<BODY BGCOLOR=\"" + bgcolor + "\"><FONT SIZE=" + fontsize + " FACE=\"" + fontface + "\" COLOR=\"" + fontcolor + "\">" + text + "</FONT>";
		try {
			Thread.sleep(7200);
		} catch (Exception e) {
			System.out.println("*** AIM LISTENER EXCEPTION: " + e.toString() + " ***");
		}
		String work = "toc_send_im ";
		work = work.concat(to);
		work = work.concat(" \"");
		for (int i=0;i<text.length();i++) {
			switch (text.charAt(i)) {
			case '$':
			case '{':
			case '}':
			case '[':
			case ']':
			case '(':
			case ')':
			case '\"':
			case '\\':
				work = work.concat("\\"+text.charAt(i));
				break;
			default:
				work = work.concat(""+text.charAt(i));
				break;
			}
		}
		work = work.concat("\"\0");
		System.out.println(work);
		try {frameSend(work);}
		catch (IOException e) {
			signoff("9");
		}
	}
	
	public void sendChatRoomMesg(String room_id, String text) {
		String work = "toc_chat_send ";
		work = work.concat(room_id);
		work = work.concat(" \"");
		for (int i=0;i<text.length();i++) {
			switch (text.charAt(i)) {
			case '$':
			case '{':
			case '}':
			case '[':
			case ']':
			case '(':
			case ')':
			case '\"':
			case '\\':
				work = work.concat("\\"+text.charAt(i));
				break;
			default:
				work = work.concat(""+text.charAt(i));
				break;
			}
		}
		work = work.concat("\"\0");
		System.out.println(work);
		try {frameSend(work);}
		catch (IOException e) {
			signoff("9");
		}
	}
	
	public void toAIM(byte[] buffer) {
		// we cant send responses > 2048 bytes
		if (buffer.length < 2030) return; 
		try {
			out.writeByte(42);
			out.writeByte(2);
			out.writeShort(seqNo);
			seqNo = (seqNo + 1) & 65535;
			out.writeShort(buffer.length + 1);
			out.write(buffer);
			out.writeByte('\0');
			out.flush();
		}
		catch (IOException e) {
			System.out.println(e);
			signoff("6");
		}
	}
	
	public void sendBuddies() {
		String toBeSent = "toc_add_buddy";
		// for (int i=0;i<// ebnet.buddyStruct.size();i++) {
		//    if (((String[]) // ebnet.buddyStruct.elementAt(i))[2] != null) toBeSent = toBeSent + " " + ((String[]) // ebnet.buddyStruct.elementAt(i))[2];
		// } 
		System.out.println(toBeSent);
		try {
			frameSend(toBeSent+" " + this.name + "\0");
			StringTokenizer st = new StringTokenizer(this.buddies, ",");
			while (st.hasMoreTokens()) {
				frameSend(toBeSent+" " + st.nextToken() + "\0");
			}
			/*
			out.writeByte(42);
			out.writeByte(2);
			out.writeShort(seqNo);
			seqNo = (seqNo + 1) & 65535;
			out.writeShort(toBeSent.length() + 1);
			out.writeBytes(toBeSent);
			out.writeByte('\0');
			out.flush();
			*/
		}
		catch (IOException e) {
			System.out.println(e);
			signoff("7");
		}
	}
	
	public void signoff(String place) {
		online = false;
		System.out.print("Trying to close IM ("+place+").....");
		// ebnet.imoff();
		try {
			out.close();
			in.close();
			connection.close();
		}
		catch (IOException e) {System.out.println(e);}
		System.out.println("done");
	}
	
	public static String imRoast(String pass) {
		String roast = "Tic/Toc";
		String out = "";
		String in = pass;
		String out2 = "0x";
		for (int i = 0; i < in.length(); i++) {
			out = java.lang.Long.toHexString(in.charAt(i) ^ roast.charAt(i % 7));
			if (out.length() < 2) {out2 = out2 + "0";}
			out2 = out2 + out;
		}
		return out2;
	}
	
	public static String imNormalize(String in) {
		String out = "";
		in = in.toLowerCase();
		char[] arr = in.toCharArray();
		for (int i=0;i<arr.length;i++) {
			if (arr[i] != ' ') out = out + "" + arr[i];
		}
		return out;
	}
	
	public static String stripHTML(String line) {
		StringBuffer sb = new StringBuffer(line);
		String out = "";
		
		for (int i=0; i < sb.length()-1; i++) {
			if (sb.charAt(i) == '<') {
				// Most tags
				if (sb.charAt(i+1) == '/'
					|| (sb.charAt(i+1) >= 'a' && sb.charAt(i+1) <= 'z')
					|| (sb.charAt(i+1) >= 'A' && sb.charAt(i+1) <= 'Z')) {
					for (int j=i+1; j < sb.length(); j++) {
						if (sb.charAt(j) == '>') {
							sb = sb.replace(i,j+1,"");
							i--;
							break;
						}
					}
				} else if (sb.charAt(i+1) == '!') { // Comments
					for (int j=i+1; j < sb.length(); j++) {
						if (sb.charAt(j) == '>'
							&& sb.charAt(j-1) == '-'
							&& sb.charAt(j-2) == '-') {
							sb = sb.replace(i,j+1,"");
							i--;
							break;
						}
					}
				}
			}
		}
		out = sb.toString();
		return out;
		
	}  // stripHTML
	
} //class
