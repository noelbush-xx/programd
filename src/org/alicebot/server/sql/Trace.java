/*
 * Trace.java
 */

package org.alicebot.server.sql;
import java.sql.*;
import java.sql.SQLException;
import java.io.*;

class Trace extends PrintWriter {
//#ifdef TRACE
/*
  public static final boolean TRACE=true;
*/
//#else
  public static final boolean TRACE=false;
//#endif
  public static final boolean STOP=false;
  public static final boolean ASSERT=false;
  private static Trace tTracer=new Trace();
  private static int iLine;
  private static String sTrace;
  private static int iStop=0;
  final static int
    DATABASE_ALREADY_IN_USE=0,
    CONNECTION_IS_CLOSED=1,
    CONNECTION_IS_BROKEN=2,
    DATABASE_IS_SHUTDOWN=3,
    COLUMN_COUNT_DOES_NOT_MATCH=4,
    DIVISION_BY_ZERO=5,
    INVALID_ESCAPE=6,
    INTEGRITY_CONSTRAINT_VIOLATION=7,
    VIOLATION_OF_UNIQUE_INDEX=8,
    TRY_TO_INSERT_NULL=9,
    UNEXPECTED_TOKEN=10,
    UNEXPECTED_END_OF_COMMAND=11,
    UNKNOWN_FUNCTION=12,
    NEED_AGGREGATE=13,
    SUM_OF_NON_NUMERIC=14,
    WRONG_DATA_TYPE=15,
    SINGLE_VALUE_EXPECTED=16,
    SERIALIZATION_FAILURE=17,
    TRANSFER_CORRUPTED=18,
    FUNCTION_NOT_SUPPORTED=19,
    TABLE_ALREADY_EXISTS=20,
    TABLE_NOT_FOUND=21,
    INDEX_ALREADY_EXISTS=22,
    SECOND_PRIMARY_KEY=23,
    DROP_PRIMARY_KEY=24,
    INDEX_NOT_FOUND=25,
    COLUMN_ALREADY_EXISTS=26,
    COLUMN_NOT_FOUND=27,
    FILE_IO_ERROR=28,
    WRONG_DATABASE_FILE_VERSION=29,
    DATABASE_IS_READONLY=30,
    ACCESS_IS_DENIED=31,
    INPUTSTREAM_ERROR=32,
    NO_DATA_IS_AVAILABLE=33,
    USER_ALREADY_EXISTS=34,
    USER_NOT_FOUND=35,
    ASSERT_FAILED=36,
    EXTERNAL_STOP=37,
    GENERAL_ERROR=38,
    WRONG_OUT_PARAMETER=39,
    ERROR_IN_FUNCTION=40;
  private static String sDescription[]= {
    "08001 The database is already in use by another process",
    "08003 Connection is closed",
    "08003 Connection is broken",
    "08003 The database is shutdown",
    "21S01 Column count does not match",
    "22012 Division by zero",
    "22019 Invalid escape character",
    "23000 Integrity constraint violation",
    "23000 Violation of unique index",
    "23000 Try to insert null into a non-nullable column",
    "37000 Unexpected token",
    "37000 Unexpected end of command",
    "37000 Unknown function",
    "37000 Need aggregate function or group by",
    "37000 Sum on non-numeric data not allowed",
    "37000 Wrong data type",
    "37000 Single value expected",
    "40001 Serialization failure",
    "40001 Transfer corrupted",
    "IM001 This function is not supported",
    "S0001 Table already exists",
    "S0002 Table not found",
    "S0011 Index already exists",
    "S0011 Attempt to define a second primary key",
    "S0011 Attempt to drop the primary key",
    "S0012 Index not found",
    "S0021 Column already exists",
    "S0022 Column not found",
    "S1000 File input/output error",
    "S1000 Wrong database file version",
    "S1000 The database is in read only mode",
    "S1000 Access is denied",
    "S1000 InputStream error",
    "S1000 No data is available",
    "S1000 User already exists",
    "S1000 User not found",
    "S1000 Assert failed",
    "S1000 External stop request",
    "S1000 General error",
    "S1009 Wrong OUT parameter",
    "S1010 Error in function"
  };

  static SQLException getError(int code,String add) {
    String s=getMessage(code);
    if(add!=null) {
      s+=": "+add;
    }
    return getError(s);
  }
  static String getMessage(int code) {
    return sDescription[code];
  }
  static String getMessage(SQLException e) {
    return e.getSQLState()+" "+e.getMessage();
  }
  static SQLException getError(String msg) {
    return new SQLException(msg.substring(6),msg.substring(0,5));
  }
  static SQLException error(int code) {
    return getError(code,null);
  }
  static SQLException error(int code,String s) {
    return getError(code,s);
  }
  static SQLException error(int code,int i) {
    return getError(code,""+i);
  }
  static void assert(boolean condition) throws SQLException {
    assert(condition,null);
  }
  static void assert(boolean condition,String error) throws SQLException {
    if(!condition) {
      printStack();
      throw getError(ASSERT_FAILED,error);
    }
  }
  static void check(boolean condition,int code) throws SQLException {
    check(condition,code,null);
  }
  static void check(boolean condition,int code,String s) throws SQLException {
    if(!condition) {
      throw getError(code,s);
    }
  }
  // for the PrinterWriter interface
  public void println(char c[]) {
    if(iLine++==2) {
      String s=new String(c);
      int i=s.indexOf('.');
      if(i!=-1) {
        s=s.substring(i+1);
      }
      i=s.indexOf('(');
      if(i!=-1) {
        s=s.substring(0,i);
      }
      sTrace=s;
    }
  }
  Trace() {
    super(System.out);
  }
  static void trace(long l) {
    traceCaller(""+l);
  }
  static void trace(int i) {
    traceCaller(""+i);
  }
  static void trace() {
    traceCaller("");
  }
  static void trace(String s) {
    traceCaller(s);
  }
  static void stop() throws SQLException {
    stop(null);
  }
  static void stop(String s) throws SQLException {
    if(iStop++ % 10000 != 0) {
      return;
    }
    if(new File("trace.stop").exists()) {
      printStack();
      throw getError(EXTERNAL_STOP,s);
    }
  }
  static private void printStack() {
    Exception e=new Exception();
    e.printStackTrace();
  }
  static private void traceCaller(String s) {
    Exception e=new Exception();
    iLine=0;
    e.printStackTrace(tTracer);
    s=sTrace+"\t"+s;
    // trace to System.out is handy if only trace messages of hsql are required
//#ifdef TRACESYSTEMOUT
    System.out.println(s);
//#else
/*
    DriverManager.println(s);
*/
//#endif
  }
}

