package org.alicebot.server.core.responder;

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
@version 4.1.2
*/

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.alicebot.server.core.Globals;
import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.util.Substituter;
import org.alicebot.server.core.util.Tag;

/**
 * The HTMLResponder (Netscape, MSIE, Opera). 
 * Works with /templates/template.html
 *
 * Changes to 1.1 - now replaces XML - style &lt;br/&gt; with &lt;br&gt;
 *
 * @author Kim Sullivan
 * @version 1.1
 */

public class HTMLResponder implements Responder
{   
	LinkedList header = new LinkedList();
	LinkedList replyPart = new LinkedList();
	LinkedList footer = new LinkedList();
	StringBuffer reply = new StringBuffer();
	
	static String[] tags = {"", "REPLY", "/REPLY", "BOT_","ALICE_IN","ALICE_OUT","HNAME","SCROLL" }; 
	
	public String pre_process(String input, String hname) { 
		reply.setLength(0);
		for (int i=0; i<header.size(); i++) {
			Object thisNode=header.get(i);
			if ( thisNode instanceof String) {
				reply.append(thisNode);
			} else
				if (thisNode instanceof Tag) {
					Tag tag=(Tag)thisNode;
					if (tag.getName().equals("HNAME")) { reply.append(hname); } else
						if (tag.getName().equals("ALICE_IN")) { reply.append(input); } else
							if (tag.getName().startsWith("BOT_")) {
								StringTokenizer st = new StringTokenizer(tag.getName(), "_");
								while (st.hasMoreTokens()) {
									st.nextToken(); reply.append(Globals.getValue(st.nextToken().toLowerCase()));
								}
							}
				}
		}
		return input;
	}
	
	public void log(String input, String response, String hname) {
		Date dt = new Date();
		Log.log(dt.toString() + " " + hname + "\nClient: " + input + "\nRobot:" + response+"\n", Log.CHAT);
	}
	
	public String append(String input, String response, String scroll){
		response=Substituter.replace("<br/>","<br>",response);
		response=Substituter.replace("<br />","<br>",response);
		response=Substituter.replace("<BR/>","<BR>",response);
		response=Substituter.replace("<BR />","<BR>",response);
		
		for (int i=0; i<replyPart.size(); i++) {
			Object thisNode=replyPart.get(i);
			if ( thisNode instanceof String) {
				reply.append(thisNode);
			} else
				if (thisNode instanceof Tag) {
					Tag tag=(Tag)thisNode;
					if (tag.getName().equals("ALICE_IN")) { reply.append(input); } else
						if (tag.getName().equals("ALICE_OUT")) { reply.append(response); } else
							if (tag.getName().equals("SCROLL")) { reply.append(scroll); } else
								if (tag.getName().startsWith("BOT_")) {
									StringTokenizer st = new StringTokenizer(tag.getName(), "_");
									while (st.hasMoreTokens()) {
										st.nextToken(); reply.append(Globals.getValue(st.nextToken().toLowerCase()));
									}
								}
				}
		}
		return scroll + response;
	}
	
	public String post_process(String bot_reply){
		////System.out.println("post: footsize=="+footer.size());
		for (int i=0; i<footer.size(); i++) {
			////System.out.println(i);
			Object thisNode=footer.get(i);
			if ( thisNode instanceof String) {
				reply.append(thisNode);
			} else
				if (thisNode instanceof Tag) {
					Tag tag=(Tag)thisNode;
					if (tag.getName().equals("ALICE_OUT")) { reply.append(bot_reply); } else
						if (tag.getName().startsWith("BOT_")) {
							StringTokenizer st = new StringTokenizer(tag.getName(), "_");
							while (st.hasMoreTokens()) {
								st.nextToken(); reply.append(Globals.getValue(st.nextToken().toLowerCase()));
							}
						}
				}
		}
		StringTokenizer st = new StringTokenizer(reply.toString());
		StringBuffer buffer = new StringBuffer();
		while (st.hasMoreTokens()) {
			buffer.append(st.nextToken());
			if (st.hasMoreTokens()) buffer.append(" ");
		}
		return buffer.toString();
	}
	
	void parse(FileReader file) throws IOException{
		//System.out.println("parsing");
		int i;
		char c;
		StringBuffer buf = new StringBuffer();
		StringBuffer tag = new StringBuffer();
		List currentList = header;
		while ((i=file.read())!=-1){
			c=(char)i;
			if (c=='<') {                         //process a tag
				tag.setLength(0);
				while ((c=(char)file.read())!='>') tag.append(c);   //read the tag
				String tagString=tag.toString().toUpperCase();
				int j;
				for (j=(tags.length-1);j>0;j--) {                    //if the tag is an alicetag, flush the buffer
					if (tagString.startsWith(tags[j])) {
						currentList.add(buf.toString());
						buf.setLength(0);
						break;
					}
				}
				if (j==0) buf.append("<"+tag+">");
				if (j==1) currentList=replyPart;
				if (j==2) currentList=footer;
				if (j>2) currentList.add(new Tag(tagString));
				
			} else {
				buf.append(c);
			}
		}
		//if (buf.length()>0)
		currentList.add(buf.toString());
		//System.out.println("parsing done");
		
	}
	
	public HTMLResponder() throws IOException
	{  
		this(
			"templates" +
			File.separator +
			"html" +
			File.separator +
			"TEMPLATE.data"
			);
		
	}
	public HTMLResponder (String template) throws IOException{
		super();
		//System.out.println("opening "+template);
		FileReader r = new FileReader(template);
		//System.out.println("parsing");
		parse(r);
		r.close();
		
	}
	
}

