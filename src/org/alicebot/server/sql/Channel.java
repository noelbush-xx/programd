/*
 * Channel.java
 */

package org.alicebot.server.sql;
import java.sql.*;
import java.util.Vector;

class Channel {
  private Database dDatabase;
  private User uUser;
  private Vector tTransaction;
  private boolean bAutoCommit;
  private boolean bNestedTransaction;
  private boolean bNestedOldAutoCommit;
  private int iNestedOldTransIndex;
  private boolean bReadOnly;
  private int iMaxRows;
  private int iLastIdentity;
  private boolean bClosed;
  private int iId;

  public void finalize() throws SQLException {
    disconnect();
  }
  Channel(Channel c,int id) {
    this(c.dDatabase,c.uUser,true,c.bReadOnly,id);
  }
  Channel(Database db,User user,boolean autocommit,boolean readonly,int id) {
    iId=id;
    dDatabase=db;
    uUser=user;
    tTransaction=new Vector();
    bAutoCommit=autocommit;
    bReadOnly=readonly;
  }
  int getId() {
    return iId;
  }
  void disconnect() throws SQLException {
    if(bClosed) {
      return;
    }
    rollback();
    dDatabase=null;
    uUser=null;
    tTransaction=null;
    bClosed=true;
  }
  boolean isClosed() {
    return bClosed;
  }
  void setLastIdentity(int i) {
    iLastIdentity=i;
  }
  int getLastIdentity() {
    return iLastIdentity;
  }
  Database getDatabase() {
    return dDatabase;
  }
  String getUsername() {
    return uUser.getName();
  }
  void setUser(User user) {
    uUser=user;
  }
  void checkAdmin() throws SQLException {
    uUser.checkAdmin();
  }
  void check(String object,int right) throws SQLException {
    uUser.check(object,right);
  }
  void checkReadWrite() throws SQLException {
    Trace.check(!bReadOnly,Trace.DATABASE_IS_READONLY);
  }
  void setPassword(String s) {
    uUser.setPassword(s);
  }
  void addTransactionDelete(Table table,Object row[])
  throws SQLException {
    if(!bAutoCommit) {
      Transaction t=new Transaction(true,table,row);
      tTransaction.addElement(t);
    }
  }
  void addTransactionInsert(Table table,Object row[])
  throws SQLException {
    if(!bAutoCommit) {
      Transaction t=new Transaction(false,table,row);
      tTransaction.addElement(t);
    }
  }
  void setAutoCommit(boolean autocommit) throws SQLException {
    commit();
    bAutoCommit=autocommit;
  }
  void commit() throws SQLException {
    tTransaction.removeAllElements();
  }
  void rollback() throws SQLException {
    int i=tTransaction.size()-1;
    while(i>=0) {
      Transaction t=(Transaction)tTransaction.elementAt(i);
      t.rollback();
      i--;
    }
    tTransaction.removeAllElements();
  }
  void beginNestedTransaction() throws SQLException {
    Trace.assert(!bNestedTransaction,"beginNestedTransaction");
    bNestedOldAutoCommit=bAutoCommit;
    // now all transactions are logged
    bAutoCommit=false;
    iNestedOldTransIndex=tTransaction.size();
    bNestedTransaction=true;
  }
  void endNestedTransaction(boolean rollback) throws SQLException {
    Trace.assert(bNestedTransaction,"endNestedTransaction");
    int i=tTransaction.size()-1;
    if(rollback) {
      while(i>=iNestedOldTransIndex) {
        Transaction t=(Transaction)tTransaction.elementAt(i);
        t.rollback();
        i--;
      }
    }
    bNestedTransaction=false;
    bAutoCommit=bNestedOldAutoCommit;
    if(bAutoCommit==true) {
      tTransaction.setSize(iNestedOldTransIndex);
    }
  }
  void setReadOnly(boolean readonly) {
    bReadOnly=readonly;
  }
  boolean isReadOnly() {
    return bReadOnly;
  }
  void setMaxRows(int max) {
    iMaxRows=max;
  }
  int getMaxRows() {
    return iMaxRows;
  }
  boolean isNestedTransaction() {
    return bNestedTransaction;
  }
}

