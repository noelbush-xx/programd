/*
 * Database.java
 */

package org.alicebot.server.sql;
import java.sql.*;
import java.io.*;
import java.net.*;
import java.util.*;

/**
 * Database is the root class for Hypersonic SQL database.
 * This class should not be used directly by the application,
 * instead the jdbc* classes should be used.               cChannel
 */

public class Database {
  private String sName;
  private Access aAccess;
  private Vector tTable;
  private DatabaseInformation dInfo;
  private Log lLog;
  private boolean bReadOnly;
  private boolean bShutdown;
  private Hashtable hAlias;
  private boolean bIgnoreCase;
  private boolean bReferentialIntegrity;
  private Vector cChannel;

  public Database(String name) throws SQLException {
    if(Trace.TRACE) Trace.trace();
    sName=name;
    tTable=new Vector();
    aAccess=new Access();
    cChannel=new Vector();
    hAlias=new Hashtable();
    bReferentialIntegrity=true;
    Library.register(hAlias);
    dInfo=new DatabaseInformation(this,tTable,aAccess);
    boolean newdatabase=false;
    Channel sys=new Channel(this,new User(null,null,true,null),true,false,0);
    registerChannel(sys);
    if(name.equals(".")) {
      newdatabase=true;
    } else {
      lLog=new Log(this,sys,name);
      newdatabase=lLog.open();
    }
    if(newdatabase) {
      execute("CREATE USER ALICEBOT PASSWORD \"\" ADMIN",sys);
    }
    aAccess.grant("PUBLIC","CLASS \"java.lang.Math\"",Access.ALL);
    aAccess.grant("PUBLIC","CLASS \"org.alicebot.server.sql.Library\"",Access.ALL);
  }
  String getName() {
    return sName;
  }
  boolean isShutdown() {
    return bShutdown;
  }
  synchronized Channel connect(String username,String password)
  throws SQLException {
    User user=aAccess.getUser(username.toUpperCase(),password.toUpperCase());
    int size=cChannel.size(),id=size;
    for(int i=0;i<size;i++) {
      if(cChannel.elementAt(i)==null) {
        id=i;
        break;
      }
    }
    Channel c=new Channel(this,user,true,bReadOnly,id);
    if(lLog!=null) {
      lLog.write(c,"CONNECT USER "+username+" PASSWORD \""+password+"\"");
    }
    registerChannel(c);
    return c;
  }
  void registerChannel(Channel channel) {
    int size=cChannel.size();
    int id=channel.getId();
    if(id>=size) {
      cChannel.setSize(id+1);
    }
    cChannel.setElementAt(channel,id);
  }
  public byte[] execute(String user,String password,String statement) {
    Result r=null;
    try {
      Channel channel=connect(user,password);
      r=execute(statement,channel);
      execute("DISCONNECT",channel);
    } catch(Exception e) {
      r=new Result(e.getMessage());
    }
    try {
      return r.getBytes();
    } catch(Exception e) {
      return new byte[0];
    }
  }
  public synchronized Result execute(String statement,Channel channel) {
    if(Trace.TRACE) Trace.trace(statement);
    Tokenizer c=new Tokenizer(statement);
    Parser p=new Parser(this,c,channel);
    Result rResult=new Result();
    try {
      if(lLog!=null && lLog.cCache!=null) {
        lLog.cCache.cleanUp();
      }
      if(Trace.ASSERT) Trace.assert(!channel.isNestedTransaction());
      Trace.check(channel!=null,Trace.ACCESS_IS_DENIED);
      Trace.check(!bShutdown,Trace.DATABASE_IS_SHUTDOWN);
      while(true) {
        int begin=c.getPosition();
        boolean script=false;
        String sToken=c.getString();
        if(sToken.equals("")) {
          break;
        } else if(sToken.equals("SELECT")) {
          rResult=p.processSelect();
        } else if(sToken.equals("INSERT")) {
          rResult=p.processInsert();
        } else if(sToken.equals("UPDATE")) {
          rResult=p.processUpdate();
        } else if(sToken.equals("DELETE")) {
          rResult=p.processDelete();
        } else if(sToken.equals("CREATE")) {
          rResult=processCreate(c,channel);
          script=true;
        } else if(sToken.equals("DROP")) {
          rResult=processDrop(c,channel);
          script=true;
        } else if(sToken.equals("GRANT")) {
          rResult=processGrantOrRevoke(c,channel,true);
          script=true;
        } else if(sToken.equals("REVOKE")) {
          rResult=processGrantOrRevoke(c,channel,false);
          script=true;
        } else if(sToken.equals("CONNECT")) {
          rResult=processConnect(c,channel);
        } else if(sToken.equals("DISCONNECT")) {
          rResult=processDisconnect(c,channel);
          script=true;
        } else if(sToken.equals("SET")) {
          rResult=processSet(c,channel);
          script=true;
        } else if(sToken.equals("SCRIPT")) {
          rResult=processScript(c,channel);
        } else if(sToken.equals("COMMIT")) {
          rResult=processCommit(c,channel);
          script=true;
        } else if(sToken.equals("ROLLBACK")) {
          rResult=processRollback(c,channel);
          script=true;
        } else if(sToken.equals("SHUTDOWN")) {
          rResult=processShutdown(c,channel);
        } else if(sToken.equals("CHECKPOINT")) {
          rResult=processCheckpoint(channel);
        } else if(sToken.equals("CALL")) {
          rResult=p.processCall();
        } else if(sToken.equals(";")) {
          // ignore
        } else {
          throw Trace.error(Trace.UNEXPECTED_TOKEN,sToken);
        }
        if(script && lLog!=null) {
          int end=c.getPosition();
          lLog.write(channel,c.getPart(begin,end));
        }
      }
    } catch(SQLException e) {
      // e.printStackTrace();
      rResult=new Result(Trace.getMessage(e)+" in statement ["+statement+"]");
    } catch(Exception e) {
       e.printStackTrace();
      String s=Trace.getMessage(Trace.GENERAL_ERROR)+" "+e;
      rResult=new Result(s+" in statement ["+statement+"]");
    }
    return rResult;
  }
  void setReadOnly() {
    bReadOnly=true;
  }
  Vector getTables() {
    return tTable;
  }
  void setReferentialIntegrity(boolean ref) {
    bReferentialIntegrity=ref;
  }
  boolean isReferentialIntegrity() {
    return bReferentialIntegrity;
  }
  Hashtable getAlias() {
    return hAlias;
  }
  String getAlias(String s) {
    Object o=hAlias.get(s);
    if(o==null) {
      return s;
    }
    return (String)o;
  }
  Log getLog() {
    return lLog;
  }
  Table getTable(String name,Channel channel) throws SQLException {
    Table t=null;
    for(int i=0;i<tTable.size();i++) {
      t=(Table)tTable.elementAt(i);
      if(t.getName().equals(name)) {
        return t;
      }
    }
    t=dInfo.getSystemTable(name,channel);
    if(t==null) {
      throw Trace.error(Trace.TABLE_NOT_FOUND,name);
    }
    return t;
  }
  Result getScript(boolean drop,boolean insert,boolean cached,Channel channel)
  throws SQLException {
    return dInfo.getScript(drop,insert,cached,channel);
  }
  void linkTable(Table t) throws SQLException {
    String name=t.getName();
    for(int i=0;i<tTable.size();i++) {
      Table o=(Table)tTable.elementAt(i);
      if(o.getName().equals(name)) {
        throw Trace.error(Trace.TABLE_ALREADY_EXISTS,name);
      }
    }
    tTable.addElement(t);
  }
  boolean isIgnoreCase() {
    return bIgnoreCase;
  }

  private Result processScript(Tokenizer c,Channel channel)
  throws SQLException {
    String sToken=c.getString();
    if(c.wasValue()) {
      sToken=(String)c.getAsValue();
      Log.scriptToFile(this,sToken,true,channel);
      return new Result();
    } else {
      c.back();
      // try to script all: drop, insert; but no positions for cached tables
      return getScript(true,true,false,channel);
    }
  }
  private Result processCreate(Tokenizer c,Channel channel)
  throws SQLException {
    channel.checkReadWrite();
    channel.checkAdmin();
    String sToken=c.getString();
    if(sToken.equals("TABLE")) {
      processCreateTable(c,channel,false);
    } else if(sToken.equals("MEMORY")) {
      c.getThis("TABLE");
      processCreateTable(c,channel,false);
    } else if(sToken.equals("CACHED")) {
      c.getThis("TABLE");
      processCreateTable(c,channel,true);
    } else if(sToken.equals("USER")) {
      String u=c.getStringToken();
      c.getThis("PASSWORD");
      String p=c.getStringToken();
      boolean admin;
      if(c.getString().equals("ADMIN")) {
        admin=true;
      } else {
        admin=false;
      }
      aAccess.createUser(u,p,admin);
    } else if(sToken.equals("ALIAS")) {
      String name=c.getString();
      sToken=c.getString();
      Trace.check(sToken.equals("FOR"),Trace.UNEXPECTED_TOKEN,sToken);
      sToken=c.getString();
      hAlias.put(name,sToken);
    } else {
      boolean unique=false;
      if(sToken.equals("UNIQUE")) {
        unique=true;
        sToken=c.getString();
      }
      if(!sToken.equals("INDEX")) {
        throw Trace.error(Trace.UNEXPECTED_TOKEN,sToken);
      }
      String name=c.getName();
      c.getThis("ON");
      Table t=getTable(c.getString(),channel);
      addIndexOn(c,channel,name,t,unique);
    }
    return new Result();
  }

  private int[] processColumnList(Tokenizer c,Table t) throws SQLException {
    Vector v=new Vector();
    c.getThis("(");
    while(true) {
      v.addElement(c.getString());
      String sToken=c.getString();
      if(sToken.equals(")")) {
        break;
      }
      if(!sToken.equals(",")) {
        throw Trace.error(Trace.UNEXPECTED_TOKEN,sToken);
      }
    }
    int s=v.size();
    int col[]=new int[s];
    for(int i=0;i<s;i++) {
      col[i]=t.getColumnNr((String)v.elementAt(i));
    }
    return col;
  }
  private void createIndex(Channel channel,Table t,int col[],
  String name,boolean unique) throws SQLException {
    channel.commit();
    if(t.isEmpty()) {
      t.createIndex(col,name,unique);
    } else {
      Table tn=t.moveDefinition(null);
      tn.createIndex(col,name,unique);
      tn.moveData(t);
      dropTable(t.getName());
      linkTable(tn);
    }
  }
  private void addForeignKeyOn(Tokenizer c,Channel channel,String name,Table t)
  throws SQLException {
    int col[]=processColumnList(c,t);
    c.getThis("REFERENCES");
    Table t2=getTable(c.getString(),channel);
    int col2[]=processColumnList(c,t2);
    if(t.getIndexForColumns(col)==null) {
      createIndex(channel,t,col,"SYSTEM_FOREIGN_KEY_"+name,false);
    }
    if(t2.getIndexForColumns(col2)==null) {
      createIndex(channel,t2,col2,"SYSTEM_REFERENCE_"+name,false);
    }
    t.addConstraint(new Constraint(Constraint.FOREIGN_KEY,t2,t,col2,col));
    t2.addConstraint(new Constraint(Constraint.MAIN,t2,t,col2,col));
  }
  private void addUniqueConstraintOn(Tokenizer c,Channel channel,String name,
  Table t) throws SQLException {
    int col[]=processColumnList(c,t);
    createIndex(channel,t,col,name,true);
    t.addConstraint(new Constraint(Constraint.UNIQUE,t,col));
  }
  private void addIndexOn(Tokenizer c,Channel channel,String name,
  Table t,boolean unique) throws SQLException {
    int col[]=processColumnList(c,t);
    createIndex(channel,t,col,name,unique);
  }
  private void processCreateTable(Tokenizer c,Channel channel,
  boolean cached) throws SQLException {
    Table t;
    String sToken=c.getName();
    if(cached && lLog!=null) {
      t=new Table(this,true,sToken,true);
    } else {
      t=new Table(this,true,sToken,false);
    }
    c.getThis("(");
    int primarykeycolumn=-1;
    int column=0;
    boolean constraint=false;
    while(true) {
      boolean identity=false;
      sToken=c.getString();
      if(sToken.equals("CONSTRAINT")
      || sToken.equals("PRIMARY")
      || sToken.equals("FOREIGN")
      || sToken.equals("UNIQUE")) {
        c.back();
        constraint=true;
        break;
      }
      String sColumn=sToken;
      int iType=Column.getTypeNr(c.getString());
      if(iType==Column.VARCHAR && bIgnoreCase) {
        iType=Column.VARCHAR_IGNORECASE;
      }
      sToken=c.getString();
      if(iType==Column.DOUBLE && sToken.equals("PRECISION")) {
        sToken=c.getString();
      }
      if(sToken.equals("(")) {
        // overread length
        do {
          sToken=c.getString();
        } while(!sToken.equals(")"));
        sToken=c.getString();
      }
      boolean nullable=true;
      if(sToken.equals("NULL")) {
        sToken=c.getString();
      } else if(sToken.equals("NOT")) {
        c.getThis("NULL");
        nullable=false;
        sToken=c.getString();
      }
      if(sToken.equals("IDENTITY")) {
        identity=true;
        Trace.check(primarykeycolumn==-1,Trace.SECOND_PRIMARY_KEY,sColumn);
        sToken=c.getString();
        primarykeycolumn=column;
      }
      if(sToken.equals("PRIMARY")) {
        c.getThis("KEY");
        Trace.check(identity || primarykeycolumn==-1,
        Trace.SECOND_PRIMARY_KEY,sColumn);
        primarykeycolumn=column;
        sToken=c.getString();
      }
      t.addColumn(sColumn,iType,nullable,identity);
      if(sToken.equals(")")) {
        break;
      }
      if(!sToken.equals(",")) {
        throw Trace.error(Trace.UNEXPECTED_TOKEN,sToken);
      }
      column++;
    }
    if(primarykeycolumn!=-1) {
      t.createPrimaryKey(primarykeycolumn);
    } else {
      t.createPrimaryKey();
    }
    if(constraint) {
      int i=0;
      while(true) {
        sToken=c.getString();
        String name="SYSTEM_CONSTRAINT"+i;
        i++;
        if(sToken.equals("CONSTRAINT")) {
          name=c.getString();
          sToken=c.getString();
        }
        if(sToken.equals("PRIMARY")) {
          c.getThis("KEY");
          addUniqueConstraintOn(c,channel,name,t);
        } else if(sToken.equals("UNIQUE")) {
          addUniqueConstraintOn(c,channel,name,t);
        } else if(sToken.equals("FOREIGN")) {
          c.getThis("KEY");
          addForeignKeyOn(c,channel,name,t);
        }
        sToken=c.getString();
        if(sToken.equals(")")) {
          break;
        }
        if(!sToken.equals(",")) {
          throw Trace.error(Trace.UNEXPECTED_TOKEN,sToken);
        }
      }
    }
    channel.commit();
    linkTable(t);
  }
  private Result processDrop(Tokenizer c,Channel channel)
  throws SQLException {
    channel.checkReadWrite();
    channel.checkAdmin();
    String sToken=c.getString();
    if(sToken.equals("TABLE")) {
      dropTable(c.getString());
      channel.commit();
    } else if(sToken.equals("USER")) {
      aAccess.dropUser(c.getStringToken());
    } else if(sToken.equals("INDEX")) {
      sToken=c.getString();
      if(!c.wasLongName()) {
        throw Trace.error(Trace.UNEXPECTED_TOKEN,sToken);
      }
      String table=c.getLongNameFirst();
      String index=c.getLongNameLast();
      Table t=getTable(table,channel);
      t.checkDropIndex(index);
      Table tn=t.moveDefinition(index);
      tn.moveData(t);
      dropTable(table);
      linkTable(tn);
      channel.commit();
    } else {
      throw Trace.error(Trace.UNEXPECTED_TOKEN,sToken);
    }
    return new Result();
  }
  private Result processGrantOrRevoke(Tokenizer c,Channel channel,
  boolean grant) throws SQLException {
    channel.checkReadWrite();
    channel.checkAdmin();
    int right=0;
    String sToken;
    do {
      String sRight=c.getString();
      right|=Access.getRight(sRight);
      sToken=c.getString();
    } while(sToken.equals(","));
    if(!sToken.equals("ON")) {
      throw Trace.error(Trace.UNEXPECTED_TOKEN,sToken);
    }
    String table=c.getString();
    if(table.equals("CLASS")) {
      // object is saved as 'CLASS "java.lang.Math"'
      // tables like 'CLASS "xy"' should not be created
      table+=" \""+c.getString()+"\"";
    } else {
      getTable(table,channel); // to make sure the table exists
    }
    c.getThis("TO");
    String user=c.getStringToken();
    String command;
    if(grant) {
      aAccess.grant(user,table,right);
      command="GRANT";
    } else {
      aAccess.revoke(user,table,right);
      command="REVOKE";
    }
    return new Result();
  }
  private Result processConnect(Tokenizer c,Channel channel)
  throws SQLException {
    c.getThis("USER");
    String username=c.getStringToken();
    c.getThis("PASSWORD");
    String password=c.getStringToken();
    User user=aAccess.getUser(username,password);
    channel.commit();
    channel.setUser(user);
    return new Result();
  }
  private Result processDisconnect(Tokenizer c,Channel channel)
  throws SQLException {
    if(!channel.isClosed()) {
      channel.disconnect();
      cChannel.setElementAt(null,channel.getId());
    }
    return new Result();
  }
  private Result processSet(Tokenizer c,Channel channel)
  throws SQLException {
    String sToken=c.getString();
    if(sToken.equals("PASSWORD")) {
      channel.checkReadWrite();
      channel.setPassword(c.getStringToken());
    } else if(sToken.equals("READONLY")) {
      channel.commit();
      channel.setReadOnly(processTrueOrFalse(c));
    } else if(sToken.equals("LOGSIZE")) {
      channel.checkAdmin();
      int i=Integer.parseInt(c.getString());
      if(lLog!=null) {
        lLog.setLogSize(i);
      }
    } else if(sToken.equals("IGNORECASE")) {
      channel.checkAdmin();
      bIgnoreCase=processTrueOrFalse(c);
    } else if(sToken.equals("MAXROWS")) {
      int i=Integer.parseInt(c.getString());
      channel.setMaxRows(i);
    } else if(sToken.equals("AUTOCOMMIT")) {
      channel.setAutoCommit(processTrueOrFalse(c));
    } else if(sToken.equals("TABLE")) {
      channel.checkReadWrite();
      channel.checkAdmin();
      Table t=getTable(c.getString(),channel);
      c.getThis("INDEX");
      c.getString();
      t.setIndexRoots((String)c.getAsValue());
    } else if(sToken.equals("REFERENCIAL_INTEGRITY")
           || sToken.equals("REFERENTIAL_INTEGRITY")) {
      channel.checkAdmin();
      bReferentialIntegrity=processTrueOrFalse(c);
    } else if(sToken.equals("WRITE_DELAY")) {
      channel.checkAdmin();
      boolean delay=processTrueOrFalse(c);
      if(lLog!=null) {
        lLog.setWriteDelay(delay);
      }
    } else {
      throw Trace.error(Trace.UNEXPECTED_TOKEN,sToken);
    }
    return new Result();
  }
  private boolean processTrueOrFalse(Tokenizer c) throws SQLException {
    String sToken=c.getString();
    if(sToken.equals("TRUE")) {
      return true;
    } else if(sToken.equals("FALSE")) {
      return false;
    } else {
      throw Trace.error(Trace.UNEXPECTED_TOKEN,sToken);
    }
  }
  private Result processCommit(Tokenizer c,Channel channel)
  throws SQLException {
    String sToken=c.getString();
    if(!sToken.equals("WORK")) {
      c.back();
    }
    channel.commit();
    return new Result();
  }
  private Result processRollback(Tokenizer c,Channel channel)
  throws SQLException {
    String sToken=c.getString();
    if(!sToken.equals("WORK")) {
      c.back();
    }
    channel.rollback();
    return new Result();
  }
  public void finalize() {
    try {
      close(0);
    } catch(SQLException e) {
      // it's too late now
    }
  }
  private void close(int type) throws SQLException {
    if(lLog==null) {
      return;
    }
    lLog.stop();
    if(type==-1) {
      lLog.shutdown();
    } else if(type==0) {
      lLog.close(false);
    } else if(type==1) {
      lLog.close(true);
    }
    lLog=null;
    bShutdown=true;
  }
  private Result processShutdown(Tokenizer c,Channel channel)
  throws SQLException {
    channel.checkAdmin();
    // don't disconnect system user; need it to save database
    for(int i=1;i<cChannel.size();i++) {
      Channel d=(Channel)cChannel.elementAt(i);
      if(d!=null) {
        d.disconnect();
      }
    }
    cChannel.removeAllElements();
    String token=c.getString();
    if(token.equals("IMMEDIATELY")) {
      close(-1);
    } else if(token.equals("COMPACT")) {
      close(1);
    } else {
      c.back();
      close(0);
    }
    processDisconnect(c,channel);
    return new Result();
  }
  private Result processCheckpoint(Channel channel) throws SQLException {
    channel.checkAdmin();
    if(lLog!=null) {
      lLog.checkpoint();
    }
    return new Result();
  }
  private void dropTable(String name) throws SQLException {
    for(int i=0;i<tTable.size();i++) {
      Table o=(Table)tTable.elementAt(i);
      if(o.getName().equals(name)) {
        tTable.removeElementAt(i);
        return;
      }
    }
    throw Trace.error(Trace.TABLE_NOT_FOUND,name);
  }
}

