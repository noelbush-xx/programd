package org.alicebot.server.net.servlet;

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

import org.alicebot.server.core.*;
import org.alicebot.server.core.node.*;
import org.alicebot.server.core.util.*;

import org.alicebot.server.net.html.*;
import org.alicebot.server.net.http.HttpServer;
import org.alicebot.server.net.http.HttpHandler;
import org.alicebot.server.net.http.LifeCycle;
import org.alicebot.server.net.http.PathMap;
import org.alicebot.server.net.http.UrlEncoded;
import org.alicebot.server.net.http.HandlerContext;
import org.alicebot.server.net.http.HttpListener;
import org.alicebot.server.net.http.Code;
import org.alicebot.server.net.http.Log;
import org.alicebot.server.net.http.HttpException;
import org.alicebot.server.net.http.HttpResponse;
import org.alicebot.server.net.http.server.Server;
import org.alicebot.server.net.http.handler.servlet.*;
import java.io.*;
import java.util.Map;
import java.util.Iterator;
import java.util.Collection;
import java.util.StringTokenizer;
import javax.servlet.*;
import javax.servlet.http.*;

public class Admin extends HttpServlet
{
	private java.util.List _servers;
	
	/* ------------------------------------------------------------ */
	public void init(ServletConfig config)
		throws ServletException
	{
		super.init(config);
		_servers=org.alicebot.server.net.http.HttpServer.getHttpServerList();
	}
	
	/* ------------------------------------------------------------ */
	private String doAction(HttpServletRequest request,
		HttpServletResponse response) 
		throws ServletException, IOException {
		
		String action=request.getParameter("A");
		
		if (request.getParameter("pattern") != null) {
			Nodemapper node = Graphmaster.add(request.getParameter("pattern"), request.getParameter("that"), request.getParameter("topic"));
			node.put(Graphmaster.TEMPLATE, request.getParameter("template"));
			node.put(Graphmaster.FILENAME, "Learned via web");
			Graphmaster.TOTAL_CATEGORIES++;
			return "";
		}
		
		if ("exit all servers".equalsIgnoreCase(action))
		{
			new Thread(new Runnable()
				{
					public void run()
					{
						try{Thread.sleep(1000);}
						catch(Exception e){Code.ignore(e);}
						Log.event("Stopping All servers");
						for (int s=0;s<_servers.size();s++)
						{
							HttpServer server=(HttpServer)_servers.get(s);
							try{server.stop();}
							catch(Exception e){Code.ignore(e);}
						}
						Log.event("Exiting JVM");
						System.exit(1);
					}
				}).start();
			
			throw new HttpException(HttpResponse.__503_Service_Unavailable);
		}
		
		boolean start="start".equalsIgnoreCase(action);
		String id=request.getParameter("ID");
		
		StringTokenizer tok=new StringTokenizer(id,":");
		int tokens=tok.countTokens();
		String target=null;
		
		try{
			target=tok.nextToken();
			HttpServer server=(HttpServer)
				_servers.get(Integer.parseInt(target));
			
			if (tokens==1)
			{
				// Server stop/start
				if (start) server.start();
				else server.stop();
			}
			else if (tokens==3)
			{
				// Listener stop/start
				String l=tok.nextToken()+":"+tok.nextToken();
				Collection listeners=server.getListeners();
				Iterator i2 = listeners.iterator();
				while(i2.hasNext())
				{
					HttpListener listener = (HttpListener) i2.next();
					if (listener.toString().indexOf(l)>=0)
					{
						if (start) listener.start();
						else listener.stop();
					}
				}
			}
			else
			{
				String host=tok.nextToken();
				if ("null".equals(host))
					host=null;
				
				String contextPath=tok.nextToken();
				target+=":"+host+":"+contextPath;
				if (contextPath.length()>1)
					contextPath+="/*";
				int contextIndex=Integer.parseInt(tok.nextToken());
				target+=":"+contextIndex;
				HandlerContext
					context=server.getContext(host,contextPath,contextIndex);
				
				if (tokens==4)
				{
					// Context stop/start
					if (start) context.start();
					else context.stop();
				}
				else if (tokens==5)
				{
					// Handler stop/start
					int handlerIndex=Integer.parseInt(tok.nextToken());
					HttpHandler handler=context.getHandler(handlerIndex);
					
					if (start) handler.start();
					else handler.stop();
				}
			}
		}
		catch(Exception e)
		{
			Code.warning(e);
		}
		catch(Error e)
		{
			Code.warning(e);
		}
		
		return target;
	}
	
	/* ------------------------------------------------------------ */
	public void doGet(HttpServletRequest request,
		HttpServletResponse response) 
		throws ServletException, IOException
	{
		if (request.getQueryString()!=null &&
			request.getQueryString().length()>0)
		{
			String target=doAction(request,response);
			response.sendRedirect(request.getContextPath()+
				request.getServletPath()+
				(request.getPathInfo()!=null
				?request.getPathInfo():"")+
				(target!=null?("#"+target):""));
			return;
		}
		
		
		
		Page page= new Page();
		
		page.title(getServletInfo());
		page.addHeader("");
		page.attribute("text","#000000");
		page.attribute(Page.BGCOLOR,"#FFFFFF");
		page.attribute("link","#606CC0");
		page.attribute("vlink","#606CC0");
		page.attribute("alink","#606CC0");
		
		page.add("<font face=\"verdana,arial\" size=\"2\">");
		page.add(new Block(Block.Bold).add(new Font(3,true).add(getServletInfo())));
		page.add("<hr>");
		page.add(new Heading(3,"Graphmaster:"));
		page.add("<b><font color=green>" + Graphmaster.ROOT.toString() + "</font></b> {" + Graphmaster.TOTAL_CATEGORIES + " categories} <b>[<a href=#Add>Add</a>]</b>"); page.add(Break.line);
		page.add(new Heading(3,"Services:"));
		
		List sList=new List(List.Ordered);
		page.add(sList);
		String id1;
		
		for(int i1=0;i1<_servers.size();i1++)
		{
			id1=""+i1;
			HttpServer server=(HttpServer)_servers.get(i1);
			Composite sItem = sList.newItem();
			sItem.add("<B>HttpServer&nbsp;");
			sItem.add(lifeCycle(request,id1,(LifeCycle)server));
			sItem.add("</B>");
			sItem.add(Break.line);
			sItem.add("<B>Listeners:</B>");
			List lList=new List(List.Unordered);
			sItem.add(lList);
			
			Collection listeners=server.getListeners();
			Iterator i2 = listeners.iterator();
			while(i2.hasNext())
			{
				HttpListener listener = (HttpListener) i2.next();
				String id2=id1+":"+listener;
				lList.add(lifeCycle(request,id2,(LifeCycle)listener));
			}
			
			Map hostMap = server.getHostMap();
			
			sItem.add("<B>Contexts:</B>");
			List hcList=new List(List.Unordered);
			sItem.add(hcList);
			i2=hostMap.entrySet().iterator();
			while(i2.hasNext())
			{
				Map.Entry hEntry=(Map.Entry)(i2.next());
				String host=(String)hEntry.getKey();
				
				PathMap contexts=(PathMap)hEntry.getValue();
				Iterator i3=contexts.entrySet().iterator();
				while(i3.hasNext())
				{
					Map.Entry cEntry=(Map.Entry)(i3.next());
					String contextPath=(String)cEntry.getKey();
					java.util.List contextList=(java.util.List)cEntry.getValue();
					
					Composite hcItem = hcList.newItem();
					if (host!=null)
						hcItem.add("Host="+host+":");
					hcItem.add("ContextPath="+contextPath);
					
					String id3=id1+":"+host+":"+
						(contextPath.length()>2
						?contextPath.substring(0,contextPath.length()-2)
						:contextPath);
					
					List cList=new List(List.Ordered);
					hcItem.add(cList);
					for (int i4=0;i4<contextList.size();i4++)
					{
						String id4=id3+":"+i4;
						Composite cItem = cList.newItem();
						HandlerContext hc=
							(HandlerContext)contextList.get(i4);
						cItem.add(lifeCycle(request,id4,(LifeCycle)hc));
						cItem.add("<BR>ResourceBase="+hc.getResourceBase());
						cItem.add("<BR>ClassPath="+hc.getClassPath());
						
						
						List hList=new List(List.Ordered);
						cItem.add(hList);
						for(int i5=0;i5<hc.getHandlerSize();i5++)
						{
							String id5=id4+":"+i5;
							HttpHandler handler = hc.getHandler(i5);
							Composite hItem=hList.newItem();
							hItem.add(lifeCycle(request,
								id5,
								(LifeCycle)handler,
								handler.getName()));
							if (handler instanceof ServletHandler)
							{
								hItem.add("<BR>"+
									((ServletHandler)handler)
									.getServletMap());
							}
						}
					}
				}
			}
			sItem.add("<P>");
		}
		
		page.add(new Heading(3,"Listeners:"));
		page.add("<a name=Add>");
		page.add(new Heading(3,"Category:"));
		page.add("<form method=get action=.>");
		page.add("<input type=hidden name=A value=add>");
		page.add("Pattern: <input type=text size=30 name=pattern>");
		page.add("<br><br>");
		page.add("Topic: <input type=text size=30 name=topic value=*>");
		page.add("<br><br>");
		page.add("That: <input type=text size=30 name=that value=*>");
		page.add("<br><br>");
		page.add("Template: <br> <textarea name=template cols=50 rows=10></textarea>");
		page.add("<br><br>");
		page.add("Save To: <select name=file>");
		
		String[] files = new File("bots" + File.separator + Globals.getBotName()).list();
		
		page.add("<option value=\"xxx\">Select A File");
		
		for (int x = 0; x < files.length; x++) {
			page.add("<option value=\"" + files[x] + "\">" + files[x]);
		}
		
		page.add("</select> or to a new file: <input type=text size=15 name=new><br><br>");
		
		page.add("<input type=submit value=\"Add To Memory\"> &nbsp; <input type=submit value=\"Add To File\">");
		page.add(Break.line);
		page.add("</form><hr>");
		
		Form form=new Form(request.getContextPath()+
			request.getServletPath()+
			"?A=exit");
		form.method("GET");
		form.add(new Input(Input.Submit,"A","Kill All Servers"));
		page.add(form);
		
		page.add("</font>");
		response.setContentType("text/html");
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache,no-store");
		Writer writer=response.getWriter();
		page.write(writer);
		writer.flush();
	}
	
	/* ------------------------------------------------------------ */
	public void doPost(HttpServletRequest request,
		HttpServletResponse response) 
		throws ServletException, IOException
	{
		String target=null;
		response.sendRedirect(request.getContextPath()+
			request.getServletPath()+"/"+
			Long.toString(System.currentTimeMillis(),36)+
			(target!=null?("#"+target):""));
	}
	
	/* ------------------------------------------------------------ */
	private Element lifeCycle(HttpServletRequest request,
		String id,
		LifeCycle lc)
	{
		return lifeCycle(request,id,lc,lc.toString());
	}
	
	/* ------------------------------------------------------------ */
	private Element lifeCycle(HttpServletRequest request,
		String id,
		LifeCycle lc,
		String name)
	{
		Composite comp=new Composite();
		comp.add(new Target(id));
		Font font = new Font();
		comp.add(font);
		font.color(lc.isStarted()?"green":"red");
		font.add(name);
		
		String action=lc.isStarted()?"Stop":"Start";
		
		comp.add("&nbsp;[");
		comp.add(new Link(request.getContextPath()+
			request.getServletPath()+"/"+
			Long.toString(System.currentTimeMillis(),36)+
			"?A="+action+"&ID="+UrlEncoded.encodeString(id),
			action));
		comp.add("]");
		return comp;
	}
	
	public String getServletInfo()
	{
		return "Alicebot Server Admin";
	}
	
	public void init() throws ServletException {
		
	}
}










