/*
 * Function.java
 */

package org.alicebot.server.sql;
import java.sql.*;
import java.util.*;
import java.lang.reflect.*;

class Function {
  private Channel cChannel;
  private String sFunction;
  private Method mMethod;
  private int iReturnType;
  private int iArgCount;
  private int iArgType[];
  private boolean bArgNullable[];
  private Object oArg[];
  private Expression eArg[];
  private boolean bConnection;
  Function(String function,Channel channel) throws SQLException {
    cChannel=channel;
    sFunction=function;
    int i=function.lastIndexOf('.');
    Trace.check(i!=-1,Trace.UNEXPECTED_TOKEN,function);
    String classname=function.substring(0,i);
    channel.check("CLASS \""+classname+"\"",Access.ALL);
    String methodname=function.substring(i+1);
    Class classinstance=null;
    try {
      classinstance=Class.forName(classname);
    } catch(Exception e) {
      throw Trace.error(Trace.ERROR_IN_FUNCTION,classname+" "+e);
    }
    Method method[]=classinstance.getMethods();
    for(i=0;i<method.length;i++) {
      Method m=method[i];
      if(m.getName().equals(methodname)) {
        Trace.check(mMethod==null,Trace.UNKNOWN_FUNCTION,methodname);
        mMethod=m;
      }
    }
    Trace.check(mMethod!=null,Trace.UNKNOWN_FUNCTION,methodname);
    Class returnclass=mMethod.getReturnType();
    iReturnType=Column.getTypeNr(returnclass.getName());
    Class arg[]=mMethod.getParameterTypes();
    iArgCount=arg.length;
    iArgType=new int[iArgCount];
    bArgNullable=new boolean[iArgCount];
    for(i=0;i<arg.length;i++) {
      Class a=arg[i];
      String type=a.getName();
      if(i==0 && type.equals("java.sql.Connection")) {
        // only the first parameter can be a Connection
        bConnection=true;
      } else {
        if(type.equals("[B")) {
          type="byte[]";
        }
        iArgType[i]=Column.getTypeNr(type);
        bArgNullable[i]=!a.isPrimitive();
      }
    }
    eArg=new Expression[iArgCount];
    oArg=new Object[iArgCount];
  }
  Object getValue() throws SQLException {
    int i=0;
    if(bConnection) {
      oArg[i]=new jdbcConnection(cChannel);
      i++;
    }
    for(;i<iArgCount;i++) {
      Expression e=eArg[i];
      Object o=null;
      if(e!=null) {
        // no argument: null
        o=e.getValue(iArgType[i]);
      }
      if(o==null && !bArgNullable[i]) {
        // null argument for primitive datatype: don't call & return null
        return null;
      }
      oArg[i]=o;
    }
    try {
      return mMethod.invoke(null,oArg);
    } catch(Exception e) {
      String s=sFunction+": "+e.toString();
      throw Trace.getError(Trace.FUNCTION_NOT_SUPPORTED,s);
    }
  }
  int getArgCount() {
    return iArgCount - (bConnection?1:0);
  }
  void resolve(TableFilter f) throws SQLException {
    for(int i=0;i<iArgCount;i++) {
      if(eArg[i]!=null) {
        eArg[i].resolve(f);
      }
    }
  }
  void checkResolved() throws SQLException {
    for(int i=0;i<iArgCount;i++) {
      if(eArg[i]!=null) {
        eArg[i].checkResolved();
      }
    }
  }
  int getArgType(int i) {
    return iArgType[i];
  }
  int getReturnType() {
    return iReturnType;
  }
  void setArgument(int i,Expression e) {
    if(bConnection) {
      i++;
    }
    eArg[i]=e;
  }
}
