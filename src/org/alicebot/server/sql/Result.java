/*
 * Result.java
 */

package org.alicebot.server.sql;
import java.sql.SQLException;
import java.io.*;

class Result {
  private Record rTail;
  private int iSize;
  private int iColumnCount;
  final static int UPDATECOUNT=0,ERROR=1,DATA=2;
  int iMode;
  String sError;
  int iUpdateCount;
  Record rRoot;
  String sLabel[];
  String sTable[];
  String sName[];
  int iType[];

  Result() {
    iMode=UPDATECOUNT;
    iUpdateCount=0;
  }
  Result(String error) {
    iMode=ERROR;
    sError=error;
  }
  Result(int columns) {
    prepareData(columns);
    iColumnCount=columns;
  }
  int getSize() {
    return iSize;
  }
  void setColumnCount(int columns) {
    iColumnCount=columns;
  }
  int getColumnCount() {
    return iColumnCount;
  }
  void append(Result a) {
    if(rRoot==null) {
      rRoot=a.rRoot;
    } else {
      rTail.next=a.rRoot;
    }
    rTail=a.rTail;
    iSize+=a.iSize;
  }
  void add(Object d[]) {
    Record r=new Record();
    r.data=d;
    if(rRoot==null) {
      rRoot=r;
    } else {
      rTail.next=r;
    }
    rTail=r;
    iSize++;
  }
  Result(byte b[]) throws SQLException {
    ByteArrayInputStream bin=new ByteArrayInputStream(b);
    DataInputStream in=new DataInputStream(bin);
    try {
      iMode=in.readInt();
      if(iMode==ERROR) {
        throw Trace.getError(in.readUTF());
      } else if(iMode==UPDATECOUNT) {
        iUpdateCount=in.readInt();
      } else if(iMode==DATA) {
        int l=in.readInt();
        prepareData(l);
        iColumnCount=l;
        for(int i=0;i<l;i++) {
          iType[i]=in.readInt();
          sLabel[i]=in.readUTF();
          sTable[i]=in.readUTF();
          sName[i]=in.readUTF();
        }
        while(in.available()!=0) {
          add(Column.readData(in,l));
        }
      }
    } catch(IOException e) {
      Trace.error(Trace.TRANSFER_CORRUPTED);
    }
  }
  byte[] getBytes() throws SQLException {
    ByteArrayOutputStream bout=new ByteArrayOutputStream();
    DataOutputStream out=new DataOutputStream(bout);
    try {
      out.writeInt(iMode);
      if(iMode==UPDATECOUNT) {
        out.writeInt(iUpdateCount);
      } else if(iMode==ERROR) {
        out.writeUTF(sError);
      } else {
        int l=iColumnCount;
        out.writeInt(l);
        Record n=rRoot;
        for(int i=0;i<l;i++) {
          out.writeInt(iType[i]);
          out.writeUTF(sLabel[i]);
          out.writeUTF(sTable[i]);
          out.writeUTF(sName[i]);
        }
        while(n!=null) {
          Column.writeData(out,l,iType,n.data);
          n=n.next;
        }
      }
      return bout.toByteArray();
    } catch(IOException e) {
      throw Trace.error(Trace.TRANSFER_CORRUPTED);
    }
  }
  private void prepareData(int columns) {
    iMode=DATA;
    sLabel=new String[columns];
    sTable=new String[columns];
    sName=new String[columns];
    iType=new int[columns];
  }
}

