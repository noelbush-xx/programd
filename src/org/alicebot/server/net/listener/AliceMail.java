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
import java.net.InetAddress;
import java.util.Properties;
import java.util.Date;

import javax.mail.*;
import javax.mail.internet.*;
import javax.activation.*;

import org.alicebot.server.core.*;
import org.alicebot.server.core.parser.*;
import org.alicebot.server.core.responder.*;
import org.alicebot.server.core.util.*;

/**
 * An Alicebot E-Mail Listener
 *
 * This is a threaded listener which will peep into your inbox and 
 * respond to your emails with your Alicebot.  It is designed to be as 
 * flexible and configurable as possible.  Currently it uses JavaMail with
 * POP3 and IMAP support but could technically be used for anything.
 *
 * @author Jon Baer
 * @version 1.0
 */

public class AliceMail extends Thread {
	
	public String address, host, user, password, type, fromFilter, subjectFilter, replyToFilter;
	public String smtpHost, smtpUser, smtpPassword, subject;
	public String signature, cc, replyTo, contentType;
	public boolean deleteMessages = true;
	
	public int timer;
	
	public static String mbox = "INBOX";
	public static int port = -1;
	
	public Properties props = System.getProperties();
	public Session session = Session.getDefaultInstance(props, null);
	
	public static final String POP3 = "pop3";
	public static final String IMAP = "imap";
	public static final String NNTP = "nntp";
	public static final String MBOX = "mbox";
	
	/**
	 * Plain constructor.
	 */
	public AliceMail() {
	}
	
	/**
	 * Advanced constructor.
	 */
	public AliceMail(
		String address,
		String host, 
		String user, 
		String password, 
		String type,
		String timer,
		String fromFilter,
		String subjectFilter,
		String smtpHost,
		String smtpUser,
		String smtpPassword,
		String subject,
		String signature,
		String cc,
		String replyTo,
		String contentType) {
		
		this.address = address;
		this.host = host;
		this.user = user;
		this.password = password;
		this.type = type;
		this.timer = Integer.parseInt(timer);
		this.fromFilter = fromFilter;
		this.subjectFilter = subjectFilter;
		this.smtpHost = smtpHost;
		this.smtpUser = smtpUser;
		this.smtpPassword = smtpPassword;
		this.subject = subject;
		this.signature = signature;
		this.cc = cc;
		this.replyTo = replyTo;
		this.contentType = contentType;
		
	}
	
	public void run() {
		
		props.put("mail.smtp.host", this.smtpHost);
		session = Session.getInstance(props, null);
		
		while (true) {
			checkMail();
			try {
				Thread.sleep(this.timer);
			}
			catch (Exception e) {
				System.out.println("Error Reading AliceMail: " + e.toString());
			}
		}
	}
	
	public void checkMail() {
		int respondTotal = 0;
		// System.out.println("*** ALICEMAIL > CHECKING MAIL ***");
		Store store = null;
		
		try {
			store = session.getStore(this.type);
		} catch (javax.mail.NoSuchProviderException e) {
			System.out.println("Error Reading AliceMail: " + e.toString());
		}
		
		try {
			store.connect(this.host, this.user, this.password);
			Folder folder = store.getDefaultFolder();
			folder = folder.getFolder(this.mbox);
			try {
				folder.open(Folder.READ_WRITE);
			} catch (MessagingException ex) {
				folder.open(Folder.READ_ONLY);
			}
			int totalMessages = folder.getMessageCount();
			
			if (totalMessages == 0) {
				// We have no messages in our inbox =(
				// System.out.println("*** ALICEMAIL > NO MAIL ***");
				folder.close(false);
				store.close();
				return;
			}
			
			Message[] msgs = folder.getMessages();
			
			for (int i = 0; i < msgs.length; i++) {
				
				String from = "";
				String replyTo = "";
				String returnSubject = "";
				String bodyText = "";
				boolean validFrom = false;
				boolean validTo = false;
				boolean validMessage = true;
				
				Message m = msgs[i];
				
				Address[] a;
				
				if ((a = m.getFrom()) != null) {
					for (int j = 0; j < a.length; j++) {
						from = a[j].toString();
						if (this.fromFilter.indexOf(from) >= 0 || this.fromFilter.equals("*")) validFrom = true;
					}
				}
				
				if ((a = m.getReplyTo()) != null) {
					for (int j = 0; j < a.length; j++) {
						from = a[j].toString();
						if (this.fromFilter.indexOf(from) >= 0 || this.fromFilter.equals("*")) validFrom = true;
					}
				}
				
				
				if (this.subject != null) {
					returnSubject = this.subject;
				} else {
					returnSubject = "Re: " + m.getSubject();
				}
				
				Object obj = m.getContent();
				if (obj instanceof Multipart) {
					Multipart mp = (Multipart)m.getContent();
					Part p = mp.getBodyPart(0);
					Object o = p.getContent();
					if (o instanceof String) {
						bodyText = (String)p.getContent();
					}
				}
				if (obj instanceof String) {
					bodyText = (String)m.getContent();
				}
				
				if (m.getSubject().indexOf(this.subjectFilter) >= 0 || this.subjectFilter.indexOf("*") >= 0) {
					bodyText = Substituter.stripHTML(bodyText);
					m.setFlag(Flags.Flag.DELETED, true);
					sendMessage(from, replyTo, returnSubject, bodyText);
					
				}
				
			}
			
			folder.close(true);
			
			store.close();
			
		} catch (javax.mail.MessagingException e) {
			System.out.println("Error Reading AliceMail: " + e.toString());
		} catch (IOException e) {
			System.out.println("Error Reading AliceMail: " + e.toString());
		}
		return;
	}
	
	public void sendMessage(String from, String replyTo, String subject, String request) {
		
		// First get a response from the Alicebot, we want plain text here.
		// TODO: We can probably change the format to HTML later on.
		
		String bot_response = Classifier.doResponse(request, from, new MailResponder());
		
		AIMLParser p = new AIMLParser();
		String name = p.processResponse(from, "<get_name>Hello</get_name>,\n\n");
		
		try {
			
			Message msg = new MimeMessage(session);
			
			Address toEmail = new InternetAddress(from);
			msg.setRecipient(Message.RecipientType.TO, toEmail);
			
			Address fromEmail = new InternetAddress(this.address);
			msg.setFrom(fromEmail);
			
			msg.setSubject(subject);
			
			if (this.signature != null) {
				bot_response = bot_response + this.signature;
			}
			
			msg.setContent(name + bot_response, "text/plain");
			
			System.out.println("AliceMail\nRequest: " + request + "\nFrom: " + from + "\nResponse:" + name + bot_response);
			
			Transport.send(msg);
			
		} catch (Exception e) {
			System.out.println("Error Sending AliceMail: " + e.toString());
		}
		
	}
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public void setCC(String cc) {
		this.cc = cc;
	}
	
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	
	public void setFromFilter(String fromFilter) {
		this.fromFilter = fromFilter;
	}
	
	public void setSubjectFilter(String subjectFilter) {
		this.subjectFilter = subjectFilter;
	}
	
	public void setHost(String host) {
		this.host = host;
	}
	
	public void setPassword(String password) {
		this.password = password;
	}
	
	public void setReplyTo(String replyTo) {
		this.replyTo = replyTo;
	}
	
	public void setSignature(String signature) {
		this.signature = signature;
	}
	
	public void setSmtpHost(String smtpHost) {
		this.smtpHost = smtpHost;
	}
	
	public void setSmtpPassword(String smtpPassword) {
		this.smtpPassword = smtpPassword;
	}
	
	public void setSmtpUser(String smtpUser) {
		this.smtpUser = smtpUser;
	}
	
	public void setSubject(String subject) {
		this.subject = subject;
	}
	
	public void setTimer(int timer) {
		this.timer = timer;
	}
	
	public void setType(String type) {
		this.type = type;
	}
	
	public void setDeleteMessages(boolean deleteMessages) {
		this.deleteMessages = deleteMessages;
	}
	
	public void setUser(String user) {
		this.user = user;
	}
	
	public void setReplyToFilter(String replyToFilter) {
		this.replyToFilter = replyToFilter;
	}
	
	public String getAddress() {
		return this.address;
	}
	
	public String getCC() {
		return this.cc;
	}
	
	public String getContentType() {
		return this.contentType;
	}
	
	public String getFromFilter() {
		return this.fromFilter;
	}
	
	public String getSubjectFilter() {
		return this.subjectFilter;
	}
	
	public String getHost() {
		return this.host;
	}
	
	public String getPassword() {
		return this.password;
	}
	
	public String getReplyTo() {
		return this.replyTo;
	}
	
	public String getSignature() {
		return this.signature;
	}
	
	public String getSmtpHost() {
		return this.smtpHost;
	}
	
	public String getSmtpPassword() {
		return this.smtpPassword;
	}
	
	public String getSmtpUser() {
		return this.smtpUser;
	}
	
	public String getSubject() {
		return this.subject;
	}
	
	public int getTimer() {
		return this.timer;
	}
	
	public String getType() {
		return this.type;
	}
	
	public String getUser() {
		return this.user;
	}
	
	public String getReplyToFilter() {
		return this.replyToFilter;
	}
	
	public boolean getDeleteMessages() {
		return this.deleteMessages;
	}
	
	public static void main(String[] args) {
		// Testing method
	}
	
}
