/*
 * Server.java
 */

/**
 * <font color="#009900">
 * Server acts as a database server and is one way of using
 * the client / server mode of Hypersonic SQL. This server
 * can only process database queries.
 * An applet will need only the JDBC classes to access the database.
 *
 * The Server can be configured with the file 'Server.properties'.
 * This is an example of the file:
 * <pre>
 * port=9001
 * database=test
 * silent=true
 * </font>
 */

package org.alicebot.server.sql;
import java.sql.*;
import java.net.*;
import java.io.*;
import java.util.*;

public class Server {
  boolean mSilent;
  Database mDatabase;

  public static void main(String arg[]) {
    Server server=new Server();
    server.run(arg);
  }

  private void run(String arg[]) {
    ServerSocket socket=null;
    try {
      Properties prop=new Properties();
      // load parameters from properties file
      File f=new File("Server.properties");
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
      int port=jdbcConnection.DEFAULT_HSQL_PORT;
      port=Integer.parseInt(prop.getProperty("port",""+port));
      String database=prop.getProperty("database","test");
      mSilent=prop.getProperty("silent","true").equalsIgnoreCase("true");
      if(prop.getProperty("trace","false").equalsIgnoreCase("true")) {
        DriverManager.setLogStream(System.out);
      }
      socket=new ServerSocket(port);
      trace("port    ="+port);
      trace("database="+database);
      trace("silent  ="+mSilent);
      mDatabase=new Database(database);
      System.out.println("Server "+jdbcDriver.VERSION+" is running");
      System.out.println("Press [Ctrl]+[C] to abort");
    } catch(Exception e) {
      traceError("Server.run/init: "+e);
      e.printStackTrace();
      return;
    }
    try {
      while(true) {
        Socket s=socket.accept();
        ServerConnection c=new ServerConnection(s,this);
        c.start();
      }
    } catch(IOException e) {
      traceError("Server.run/loop: "+e.getMessage());
      e.printStackTrace();
    }
  }
  void printHelp() {
    System.out.println(
    "Usage: java Server [-options]\n"+
    "where options include:\n"+
    "    -port <nr>            port where the server is listening\n"+
    "    -database <name>      name of the database\n"+
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

