package org.alicebot.server.core.responder;

/**

ALICEBOT.NET Artificial Intelligence Project

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

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.LinkedList;
import java.util.StringTokenizer;

import org.alicebot.server.core.Globals;
import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.util.Tag;

/**
 * The FlashResponder (Version 4,5). 
 * Works with /templates/template_flash.html
 *
 * @author Chris Fahey
 * @version 1.0
 */

public class FlashResponder implements Responder
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
		// //System.out.println(reply);
		return reply.toString();
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
	
	public FlashResponder() throws IOException
	{  
		this(
			"htdocs" +
			File.separator +
			"templates" +
			File.separator +
			"template_flash.html"
			);
		
	}
	public FlashResponder (String template) throws IOException{
		super();
		//System.out.println("opening "+template);
		FileReader r = new FileReader(template);
		//System.out.println("parsing");
		parse(r);
		r.close();
		
	}
	
}
