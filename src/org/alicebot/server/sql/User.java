/*
 * User.java
 */

package org.alicebot.server.sql;
import java.sql.*;
import java.util.*;

class User {
  private boolean bAdministrator;
  private Hashtable hRight;
  private String sName,sPassword;
  private User uPublic;

  User(String name,String password,boolean admin,User pub) {
    hRight=new Hashtable();
    sName=name;
    setPassword(password);
    bAdministrator=admin;
    uPublic=pub;
  }
  String getName() {
    return sName;
  }
  String getPassword() {
    // necessary to create the script
    return sPassword;
  }
  Hashtable getRights() {
    // necessary to create the script
    return hRight;
  }
  void setPassword(String password) {
    sPassword=password;
  }
  void checkPassword(String test) throws SQLException {
    Trace.check(test.equals(sPassword),Trace.ACCESS_IS_DENIED);
    // this is a safer (but slower) version:
    // if the password is not over 64 characters then this
    // algorithm needs always about the same amount of time
    /*
    int maxtest=test.length();
    int maxpass=sPassword.length();
    int max=64;
    if(maxtest>max) {
      max=maxtest;
    }
    if(maxpass>max) {
      max=maxpass;
    }
    boolean correct=true,dummy=true;
    for(int i=0;i<max;i++) {
      char a= (i>=maxtest) ? 0 : test.charAt(i);
      char b= (i>=maxpass) ? 0 : sPassword.charAt(i);
      if(a!=b) {
        correct=false;
      } else {
        dummy=true;
      }
    }
    Trace.check(correct,Trace.ACCESS_IS_DENIED);
    */
  }
  void grant(String object,int right) {
    Integer n=(Integer)hRight.get(object);
    if(n==null) {
      n=new Integer(right);
    } else {
      n=new Integer(n.intValue() | right);
    }
    hRight.put(object,n);
  }
  void revoke(String object,int right) {
    Integer n=(Integer)hRight.get(object);
    if(n==null) {
      n=new Integer(right);
    } else {
      n=new Integer(n.intValue() & (Access.ALL-right));
    }
    hRight.put(object,n);
  }
  void revokeAll() {
    hRight=null;
    bAdministrator=false;
  }
  void check(String object,int right) throws SQLException {
    if(bAdministrator) {
      return;
    }
    Integer n;
    n=(Integer)hRight.get(object);
    if(n!=null && (n.intValue()&right)!=0) {
      return;
    }
    if(uPublic!=null) {
      n=(Integer)(uPublic.hRight).get(object);
      if(n!=null && (n.intValue()&right)!=0) {
        return;
      }
    }
    throw Trace.error(Trace.ACCESS_IS_DENIED);
  }
  void checkAdmin() throws SQLException {
    Trace.check(isAdmin(),Trace.ACCESS_IS_DENIED);
  }
  boolean isAdmin() {
    return bAdministrator;
  }
}
