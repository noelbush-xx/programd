/*
 * ByteArray.java
 */

package org.alicebot.server.sql;
import java.io.*;
import java.sql.*;

class ByteArray {
  private byte data[];

  ByteArray(String s) {
    data=StringConverter.hexToByte(s);
  }
  byte[] byteValue() {
    return data;
  }
  int compareTo(ByteArray o) {
    int len=data.length;
    int lenb=o.data.length;
    for(int i=0;;i++) {
      int a=0,b=0;
      if(i<len) {
        a=((int)data[i]) & 0xff;
      } else if(i>=lenb) {
        return 0;
      }
      if(i<lenb) {
        b=((int)o.data[i]) & 0xff;
      }
      if(a>b) {
        return 1;
      }
      if(b>a) {
        return -1;
      }
    }
  }
  static byte[] serialize(Object s) throws SQLException {
    ByteArrayOutputStream bo=new ByteArrayOutputStream();
    try {
      ObjectOutputStream os=new ObjectOutputStream(bo);
      os.writeObject(s);
      return bo.toByteArray();
    } catch(Exception e) {
      throw Trace.error(Trace.SERIALIZATION_FAILURE,e.getMessage());
    }
  }
  static String serializeToString(Object s) throws SQLException {
    return createString(serialize(s));
  }
  Object deserialize() throws SQLException {
    try {
      ByteArrayInputStream bi=new ByteArrayInputStream(data);
      ObjectInputStream is=new ObjectInputStream(bi);
      return is.readObject();
    } catch(Exception e) {
      throw Trace.error(Trace.SERIALIZATION_FAILURE,e.getMessage());
    }
  }
  static String createString(byte b[]) {
    return StringConverter.byteToHex(b);
  }
  public String toString() {
    return createString(data);
  }
  public int hashCode() {
    return data.hashCode();
  }
}

