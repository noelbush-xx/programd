/*
 * WebServer.java
 */

package org.alicebot.server.sql;
import java.sql.*;
import java.net.*;
import java.io.*;
import java.util.*;

/**
 * <font color="#009900">
 * WebServer acts as a HTTP server and is one way of using
 * the client / server mode of Hypersonic SQL. This server
 * can deliver static files and can also process database queries.
 * An applet will need only the JDBC classes to access the database.
 *
 * The WebServer can be configured with the file 'WebServer.properties'.
 * This is an example of the file:
 * <pre>
 * port=80
 * database=test
 * root=./
 * default=index.html
 * silent=true
 *
 * .htm=text/html
 * .html=text/html
 * .txt=text/plain
 * .gif=image/gif
 * .class=application/octet-stream
 * .jpg=image/jpeg
 * .jgep=image/jpeg
 * .zip=application/x-zip-compressed</pre>
 * Root: use / as separator even for DOS/Windows, it will be replaced<BR>
 * Mime-types: file ending must be lowercase<BR>
 * </font>
 */

public class WebServer {
  static final String mServerName="HypersonicSQL/1.0";
  String mRoot;
  String mDefaultFile;
  char mPathSeparatorChar;
  boolean mSilent;
  Database mDatabase;
  Properties mProperties;

  public static void main(String arg[]) {
    WebServer w=new WebServer();
    w.run(arg);
  }

  private void run(String arg[]) {
    ServerSocket socket=null;
    try {
      Properties prop=new Properties();
      mProperties=prop;
      // load parameters from properties file
      File f=new File("WebServer.properties");
      if(f.exists()) {
        FileInputStream fi=new FileInputStream(f);
        prop.load(fi);
        fi.close();
      }
      // overwrite parameters with command line parameters
      for(int i=0;i<arg.length;i++) {
        String p=arg[i];
        if(p.equals("-?")) {
          printHelp();
        }
        if(p.charAt(0)=='-') {
          prop.put(p.substring(1),arg[i+1]);
          i++;
        }
      }
      int port=Integer.parseInt(prop.getProperty("port","80"));
      String database=prop.getProperty("database","test");
      mRoot=prop.getProperty("root","./");
      mDefaultFile=prop.getProperty("default","index.html");
      mSilent=prop.getProperty("silent","true").equalsIgnoreCase("true");
      if(prop.getProperty("trace","false").equalsIgnoreCase("true")) {
        DriverManager.setLogStream(System.out);
      }
      socket=new ServerSocket(port);
      mPathSeparatorChar=File.separatorChar;
      trace("port    ="+port);
      trace("database="+database);
      trace("root    ="+mRoot);
      trace("default ="+mDefaultFile);
      trace("silent  ="+mSilent);
      mDatabase=new Database(database);
      System.out.println("WebServer "+jdbcDriver.VERSION+" is running");
      System.out.println("Press [Ctrl]+[C] to abort");
    } catch(Exception e) {
      traceError("WebServer.run/init: "+e);
      return;
    }
    try {
      while(true) {
        Socket s=socket.accept();
        WebServerConnection c=new WebServerConnection(s,this);
        c.start();
      }
    } catch(IOException e) {
      traceError("WebServer.run/loop: "+e.getMessage());
    }
  }
  void printHelp() {
    System.out.println(
    "Usage: java WebServer [-options]\n"+
    "where options include:\n"+
    "    -port <nr>            port where the server is listening\n"+
    "    -database <name>      name of the database\n"+
    "    -root <path>          root path for sending files\n"+
    "    -default <file>       default file when filename is missing\n"+
    "    -silent <true/false>  false means display all queries\n"+
    "    -trace <true/false>   display print JDBC trace messages\n"+
    "The command line arguments override the values in the properties file.");
    System.exit(0);
  }
  void trace(String s) {
    if(!mSilent) {
      System.out.println(s);
    }
  }
  void traceError(String s) {
    System.out.println(s);
  }
}

