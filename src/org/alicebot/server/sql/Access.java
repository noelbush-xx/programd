/*
 * Access.java
 */

package org.alicebot.server.sql;
import java.sql.*;
import java.util.*;

class Access {
  final static int SELECT=1,DELETE=2,INSERT=4,UPDATE=8,ALL=15;
  private Vector uUser;
  private User uPublic;

  Access() throws SQLException {
    uUser=new Vector();
    uPublic=createUser("PUBLIC",null,false);
  }
  static int getRight(String right) throws SQLException {
    if(right.equals("ALL")) {
      return ALL;
    } else if(right.equals("SELECT")) {
      return SELECT;
    } else if(right.equals("UPDATE")) {
      return UPDATE;
    } else if(right.equals("DELETE")) {
      return DELETE;
    } else if(right.equals("INSERT")) {
      return INSERT;
    }
    throw Trace.error(Trace.UNEXPECTED_TOKEN,right);
  }
  static String getRight(int right) {
    if(right==ALL) {
      return "ALL";
    } else if(right==0) {
      return null;
    }
    StringBuffer b=new StringBuffer();
    if((right & SELECT)!=0) {
      b.append("SELECT,");
    }
    if((right & UPDATE)!=0) {
      b.append("UPDATE,");
    }
    if((right & DELETE)!=0) {
      b.append("DELETE,");
    }
    if((right & INSERT)!=0) {
      b.append("INSERT,");
    }
    String s=b.toString();
    return s.substring(0,s.length()-1);
  }
  User createUser(String name,String password,boolean admin)
  throws SQLException {
    for(int i=0;i<uUser.size();i++) {
      User u=(User)uUser.elementAt(i);
      if(u!=null && u.getName().equals(name)) {
        throw Trace.error(Trace.USER_ALREADY_EXISTS,name);
      }
    }
    User u=new User(name,password,admin,uPublic);
    uUser.addElement(u);
    return u;
  }
  void dropUser(String name) throws SQLException {
    Trace.check(!name.equals("PUBLIC"),Trace.ACCESS_IS_DENIED);
    for(int i=0;i<uUser.size();i++) {
      User u=(User)uUser.elementAt(i);
      if(u!=null && u.getName().equals(name)) {
        // todo: find a better way. Problem: removeElementAt would not
        // work correctly while others are connected
        uUser.setElementAt(null,i);
        u.revokeAll(); // in case the user is referenced in another way
        return;
      }
    }
    throw Trace.error(Trace.USER_NOT_FOUND,name);
  }
  User getUser(String name,String password) throws SQLException {
    Trace.check(!name.equals("PUBLIC"),Trace.ACCESS_IS_DENIED);
    if(name==null) {
      name="";
    }
    if(password==null) {
      password="";
    }
    User u=get(name);
    u.checkPassword(password);
    return u;
  }
  Vector getUsers() {
    return uUser;
  }
  void grant(String name,String object,int right) throws SQLException {
    get(name).grant(object,right);
  }
  void revoke(String name,String object,int right) throws SQLException {
    get(name).revoke(object,right);
  }

  private User get(String name) throws SQLException {
    for(int i=0;i<uUser.size();i++) {
      User u=(User)uUser.elementAt(i);
      if(u!=null && u.getName().equals(name)) {
        return u;
      }
    }
    throw Trace.error(Trace.USER_NOT_FOUND,name);
  }
}
