/*
 * Transaction.java
 */

package org.alicebot.server.sql;
import java.sql.SQLException;

class Transaction {
  private boolean bDelete;
  private Table tTable;
  private Object oRow[];
  Transaction(boolean delete,Table table,Object row[]) {
    bDelete=delete;
    tTable=table;
    oRow=row;
  }
  void rollback() throws SQLException {
    if(bDelete) {
      tTable.insertNoCheck(oRow,null,false);
    } else {
      tTable.deleteNoCheck(oRow,null,false);
    }
  }
}

