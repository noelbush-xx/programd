/*
 * ServerConnection.java
 */

package org.alicebot.server.sql;
import java.sql.*;
import java.net.*;
import java.io.*;
import java.util.*;

class ServerConnection extends Thread {
  private Database mDatabase;
  private Socket mSocket;
  private Server mServer;
  private DataInputStream mInput;
  private DataOutputStream mOutput;
  private static int mCurrentThread=0;
  private int mThread;

  ServerConnection(Socket socket,Server server) {
    mSocket=socket;
    mDatabase=server.mDatabase;
    mServer=server;
    synchronized(this) {
      mThread=mCurrentThread++;
    }
  }
  private Channel init() {
    try {
      mSocket.setTcpNoDelay(true);
      mInput=new DataInputStream(
            new BufferedInputStream(mSocket.getInputStream()));
      mOutput=new DataOutputStream(
          new BufferedOutputStream(mSocket.getOutputStream()));
      String user=mInput.readUTF();
      String password=mInput.readUTF();
      Channel c;
      try {
        mServer.trace(mThread+":trying to connect user "+user);
        return mDatabase.connect(user,password);
      } catch(SQLException e) {
        write(new Result(e.getMessage()).getBytes());
      }
    } catch(Exception e) {
    }
    return null;
  }
  public void run() {
    Channel c=init();
    if(c!=null) {
      try {
        while(true) {
          String sql=mInput.readUTF();
          mServer.trace(mThread+":"+sql);
          if(sql==null) {
            break;
          }
          write(mDatabase.execute(sql,c).getBytes());
        }
      } catch(Exception e) {
      }
    }
    try {
      mSocket.close();
    } catch(IOException e) {
    }
    if(mDatabase.isShutdown()) {
      System.out.println("The database is shutdown");
      System.exit(0);
    }
  }
  void write(byte b[]) throws IOException {
    mOutput.writeInt(b.length);
    mOutput.write(b);
    mOutput.flush();
  }
}

