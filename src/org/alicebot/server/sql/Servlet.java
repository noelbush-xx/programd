/*
 * Servlet.java
 */

package org.alicebot.server.sql;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.util.*;

/**
 * <font color="#009900">
 * Servlet acts as a interface between the applet and the database for the
 * the client / server mode of Hypersonic SQL. It is not required if
 * the included Hypersonic SQL WebServer is used, but if another
 * HTTP server is used. The HTTP Server must support the Servlet API.
 * <P>
 * This class should not be used directly by the application. It will be
 * called by the HTTP Server. The applet / application should use the
 * jdbc* classes.
 * </font>
 */

public class Servlet extends HttpServlet {
  private String sError;
  private Database dDatabase;
  private String sDatabase;
  public Servlet() {
    init("test");
  }
  public Servlet(String database) {
    init(database);
  }
  void init(String database) {
    try {
      sDatabase=database;
      dDatabase=new Database(database);
    } catch(Exception e) {
      sError=e.getMessage();
    }
  }
  private static long lModified=0;
  protected long getLastModified (HttpServletRequest req) {
    // this is made so that the cache of the http server is not used
    // maybe there is some other way
    return lModified++;
  }
  public void doGet(HttpServletRequest request,HttpServletResponse response)
  throws IOException, ServletException {
    String query=request.getQueryString();
    if(query=="" || query==null) {
      response.setContentType("text/html");
      PrintWriter out=response.getWriter();
      out.println("<html><head><title>Hypersonic SQL Servlet</title>");
      out.println("</head><body><h1>Hypersonic SQL Servlet</h1>");
      out.println("The servlet is running.<P>");
      if(dDatabase!=null) {
        out.println("The database is also running.<P>");
        out.println("Database name: "+sDatabase+"<P>");
        out.println("Queries processed: "+iQueries+"<P>");
      } else {
        out.println("<h2>The database is not running!</h2>");
        out.println("The error message is:<P>");
        out.println(sError);
      }
      out.println("</body></html>");
    }
  }
  public void doPost(HttpServletRequest request,HttpServletResponse response)
  throws IOException, ServletException {
    ServletInputStream input=request.getInputStream();
    int len=request.getContentLength();
    byte b[]=new byte[len];
    input.read(b,0,len);
    String s=new String(b);
    int p=s.indexOf('+');
    int q=s.indexOf('+',p+1);
    if(p==-1 || q==-1) {
      doGet(request,response);
    }
    String user=s.substring(0,p);
    user=StringConverter.hexStringToUnicode(user);
    String password=s.substring(p+1,q);
    password=StringConverter.hexStringToUnicode(password);
    s=s.substring(q+1);
    s=StringConverter.hexStringToUnicode(s);
    response.setContentType("application/octet-stream");
    ServletOutputStream out=response.getOutputStream();
    byte result[]=dDatabase.execute(user,password,s);
    response.setContentLength(result.length);
    out.write(result);
    out.flush();
    out.close();
    iQueries++;
    // System.out.print("Queries processed: "+iQueries+"  \r");
  }
  static private int iQueries;
}

