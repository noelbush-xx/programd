/*
 * WebServerConnection.java
 */

package org.alicebot.server.sql;
import java.net.*;
import java.io.*;
import java.util.*;

class WebServerConnection extends Thread {
  final static String ENCODING="8859_1";
  private Socket mSocket;
  private WebServer mServer;
  private final static int GET=1,HEAD=2,POST=3,
  BAD_REQUEST=400,FORBIDDEN=403,NOT_FOUND=404;

  WebServerConnection(Socket socket,WebServer server) {
    mServer=server;
    mSocket=socket;
  }
  public void run() {
    try {
      BufferedReader input=new BufferedReader(
      new InputStreamReader(mSocket.getInputStream(),ENCODING));
      String request,name=null;
      int method=BAD_REQUEST;
      int len=-1;
      while(true) {
        request=input.readLine();
        if(request==null) {
          break;
        }
        StringTokenizer tokenizer=new StringTokenizer(request," ");
        if(!tokenizer.hasMoreTokens()) {
          break;
        }
        String first=tokenizer.nextToken();
        if(first.equals("GET")) {
          method=GET;
          name=tokenizer.nextToken();
        } else if(first.equals("HEAD")) {
          method=HEAD;
          name=tokenizer.nextToken();
        } else if(first.equals("POST")) {
          method=POST;
          name=tokenizer.nextToken();
        } else if(request.toUpperCase().startsWith("CONTENT-LENGTH:")) {
          len=Integer.parseInt(tokenizer.nextToken());
        }
      }
      switch(method) {
      case BAD_REQUEST:
        processError(BAD_REQUEST);
        break;
      case GET:
        processGet(name,true);
        break;
      case HEAD:
        processGet(name,false);
        break;
      case POST:
        processPost(input,name,len);
        break;
      }
      input.close();
    } catch(Exception e) {
      e.printStackTrace();
    }
  }
  private void processGet(String name,boolean send) {
    try {
      if(name.endsWith("/")) {
        name=name+mServer.mDefaultFile;
      }
      if(name.indexOf("..")!=-1) {
        processError(FORBIDDEN);
        return;
      }
      name=mServer.mRoot+name;
      if(mServer.mPathSeparatorChar!='/') {
        name=name.replace('/',mServer.mPathSeparatorChar);
      }
      String mime=null;
      int i=name.lastIndexOf(".");
      if(i!=-1) {
        String ending=name.substring(i).toLowerCase();
        mime=(String)mServer.mProperties.getProperty(ending);
      }
      if(mime==null) {
        mime="text/html";
      }
      BufferedInputStream file=null;
      String header;
      try {
        file=new BufferedInputStream(new FileInputStream(new File(name)));
        int len=file.available();
        header=getHead("HTTP/1.0 200 OK",
        "Content-Type: "+mime+"\n"+
        "Content-Length: "+len);
      } catch(IOException e) {
        processError(NOT_FOUND);
        return;
      }
      DataOutputStream output=new DataOutputStream(new BufferedOutputStream(
      mSocket.getOutputStream()));
      output.write(header.getBytes(ENCODING));
      if(send) {
        int b;
        while(true) {
          b=file.read();
          if(b==-1) {
            break;
          }
          output.writeByte(b);
        }
      }
      output.flush();
      output.close();
    } catch(Exception e) {
      mServer.traceError("processGet: "+e.getMessage());
      e.printStackTrace();
    }
  }
  private String getHead(String start,String end) {
    return start+"\nAllow: GET, HEAD, POST\nMIME-Version: 1.0\n"+
    "Server: "+mServer.mServerName+"\n"+end+"\n\n";
  }
  private void processPost(BufferedReader input,String name,int len) {
    if(len<0) {
      processError(BAD_REQUEST);
      return;
    }
    char b[]=new char[len];
    try {
      input.read(b,0,len);
    } catch(IOException e) {
      processError(BAD_REQUEST);
      return;
    }
    String s=new String(b);
    int p=s.indexOf('+');
    int q=s.indexOf('+',p+1);
    if(p==-1 || q==-1) {
      processError(BAD_REQUEST);
      return;
    }
    String user=s.substring(0,p);
    user=StringConverter.hexStringToUnicode(user);
    String password=s.substring(p+1,q);
    password=StringConverter.hexStringToUnicode(password);
    s=s.substring(q+1);
    s=StringConverter.hexStringToUnicode(s);
    processQuery(user,password,s);
  }
  private void processError(int code) {
    mServer.trace("processError "+code);
    String message=null;
    switch(code) {
    case BAD_REQUEST:
      message=getHead("HTTP/1.0 400 Bad Request","")+
      "<HTML><BODY><H1>Bad Request</H1>"+
      "The server could not understand this request.<P></BODY></HTML>";
      break;
    case NOT_FOUND:
      message=getHead("HTTP/1.0 404 Not Found","")+
      "<HTML><BODY><H1>Not Found</H1>"+
      "The server could not find this file.<P></BODY></HTML>";
      break;
    case FORBIDDEN:
      message=getHead("HTTP/1.0 403 Forbidden","")+
      "<HTML><BODY><H1>Forbidden</H1>"+
      "Access is not allowed.<P></BODY></HTML>";
      break;
    }
    try {
      DataOutputStream output=new DataOutputStream(new BufferedOutputStream(
      mSocket.getOutputStream()));
      output.write(message.getBytes(ENCODING));
      output.flush();
      output.close();
    } catch(Exception e) {
      mServer.traceError("processError: "+e.getMessage());
      e.printStackTrace();
    }
  }
  private void processQuery(String user,String password,String statement) {
    try {
      mServer.trace(statement);
      byte result[]=mServer.mDatabase.execute(user,password,statement);
      int len=result.length;
      String header=getHead("HTTP/1.0 200 OK",
      "Content-Type: application/octet-stream\n"+
      "Content-Length: "+len);
      DataOutputStream output=new DataOutputStream(new BufferedOutputStream(
      mSocket.getOutputStream()));
      output.write(header.getBytes(ENCODING));
      output.write(result);
      output.flush();
      output.close();
    } catch(Exception e) {
      mServer.traceError("processQuery: "+e.getMessage());
      e.printStackTrace();
    }
    // System.out.print("Queries processed: "+(iQueries++)+"  \r");
    if(mServer.mDatabase.isShutdown()) {
      mServer.trace("The database is shutdown");
      System.exit(0);
    }
  }
  // static private int iQueries=0;
}

