/*
 * Constraint.java
 */

package org.alicebot.server.sql;
import java.sql.SQLException;

class Constraint {
  final static int FOREIGN_KEY=0,MAIN=1,UNIQUE=2;
  private int iType;
  private int iLen;
  // Main is the table that is referenced
  private Table tMain;
  private int iColMain[];
  private Index iMain;
  private Object oMain[];
  // Ref is the table that has a reference to the main table
  private Table tRef;
  private int iColRef[];
  private Index iRef;
  private Object oRef[];

  Constraint(int type,Table t,int col[]) {
    iType=type;
    tMain=t;
    iColMain=col;
    iLen=col.length;
  }
  Constraint(int type,Table main,Table ref,int cmain[],int cref[])
  throws SQLException {
    iType=type;
    tMain=main;
    tRef=ref;
    iColMain=cmain;
    iColRef=cref;
    iLen=cmain.length;
    if(Trace.ASSERT) Trace.assert(cmain.length==cref.length);
    oMain=tMain.getNewRow();
    oRef=tRef.getNewRow();
    iMain=tMain.getIndexForColumns(cmain);
    iRef=tRef.getIndexForColumns(cref);
  }
  int getType() {
    return iType;
  }
  Table getMain() {
    return tMain;
  }
  Table getRef() {
    return tRef;
  }
  int[] getMainColumns() {
    return iColMain;
  }
  int[] getRefColumns() {
    return iColRef;
  }
  void replaceTable(Table old,Table n) throws SQLException {
    if(old==tMain) {
      tMain=n;
    } else if(old==tRef) {
      tRef=n;
    } else {
      Trace.assert(false,"could not replace");
    }
  }
  void checkInsert(Object row[]) throws SQLException {
    if(iType==MAIN || iType==UNIQUE) {
      // inserts in the main table are never a problem
      // unique constraints are checked by the unique index
      return;
    }
    // must be called synchronized because of oMain
    for(int i=0;i<iLen;i++) {
      Object o=row[iColRef[i]];
      if(o==null) {
        // if one column is null then integrity is not checked
        return;
      }
      oMain[iColMain[i]]=o;
    }
    // a record must exist in the main table
    Trace.check(iMain.find(oMain)!=null,Trace.INTEGRITY_CONSTRAINT_VIOLATION);
  }
  void checkDelete(Object row[]) throws SQLException {
    if(iType==FOREIGN_KEY || iType==UNIQUE) {
      // deleting references are never a problem
      // unique constraints are checked by the unique index
      return;
    }
    // must be called synchronized because of oRef
    for(int i=0;i<iLen;i++) {
      Object o=row[iColMain[i]];
      if(o==null) {
        // if one column is null then integrity is not checked
        return;
      }
      oRef[iColRef[i]]=o;
    }
    // there must be no record in the 'slave' table
    Trace.check(iRef.find(oRef)==null,Trace.INTEGRITY_CONSTRAINT_VIOLATION);
  }
  void checkUpdate(int col[],Result deleted,Result inserted)
  throws SQLException {
    if(iType==UNIQUE) {
      // unique constraints are checked by the unique index
      return;
    }
    if(iType==MAIN) {
      if(!isAffected(col,iColMain,iLen)) {
        return;
      }
      // check deleted records
      Record r=deleted.rRoot;
      while(r!=null) {
        // if a identical record exists we don't have to test
        if(iMain.find(r.data)==null) {
          checkDelete(r.data);
        }
        r=r.next;
      }
    } else if(iType==FOREIGN_KEY) {
      if(!isAffected(col,iColMain,iLen)) {
        return;
      }
      // check inserted records
      Record r=inserted.rRoot;
      while(r!=null) {
        checkInsert(r.data);
        r=r.next;
      }
    }
  }

  private boolean isAffected(int col[],int col2[],int len) {
    if(iType==UNIQUE) {
      // unique constraints are checked by the unique index
      return false;
    }
    for(int i=0;i<col.length;i++) {
      int c=col[i];
      for(int j=0;j<len;j++) {
        if(c==col2[j]) {
          return true;
        }
      }
    }
    return false;
  }
}
