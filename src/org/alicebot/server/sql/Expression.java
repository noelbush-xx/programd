/*
 * Expression.java
 */

package org.alicebot.server.sql;
import java.sql.*;
import java.util.*;

class Expression {
  // leaf types
  final static int VALUE=1,COLUMN=2,QUERY=3,TRUE=4,VALUELIST=5,ASTERIX=6,
  FUNCTION=7;

  // operations
  final static int NEGATE=9,ADD=10,SUBTRACT=11,MULTIPLY=12,DIVIDE=14,CONCAT=15;

  // logical operations
  final static int NOT=20,EQUAL=21,BIGGER_EQUAL=22,BIGGER=23,SMALLER=24,
  SMALLER_EQUAL=25,NOT_EQUAL=26,LIKE=27,AND=28,OR=29,IN=30,EXISTS=31;

  // aggregate functions
  final static int COUNT=40,SUM=41,MIN=42,MAX=43,AVG=44;

  // system functions
  final static int IFNULL=60,CONVERT=61,CASEWHEN=62;

  // temporary used during paring
  final static int PLUS=100,OPEN=101,CLOSE=102,SELECT=103,COMMA=104,
  STRINGCONCAT=105,BETWEEN=106,CAST=107,END=108;

  private int iType;

  // nodes
  private Expression eArg,eArg2;

  // VALUE, VALUELIST
  private Object oData;
  private Hashtable hList;
  private int iDataType;

  // QUERY (correlated subquery)
  private Select sSelect;

  // FUNCTION
  private Function fFunction;

  // LIKE
  private char cLikeEscape;

  // COLUMN
  private String sTable,sColumn;
  private TableFilter tFilter;   // null if not yet resolved
  private int iColumn;
  private String sAlias;          // if it is a column of a select column list
  private boolean bDescending;    // if it is a column in a order by

  Expression(Function f) {
    iType=FUNCTION;
    fFunction=f;
  }
  Expression(Expression e) {
    iType=e.iType;
    iDataType=e.iDataType;
    eArg=e.eArg;
    eArg2=e.eArg2;
    cLikeEscape=e.cLikeEscape;
    sSelect=e.sSelect;
    fFunction=e.fFunction;
  }
  Expression(Select s) {
    iType=QUERY;
    sSelect=s;
  }
  Expression(Vector v) {
    iType=VALUELIST;
    iDataType=Column.VARCHAR;
    int len=v.size();
    hList=new Hashtable(len);
    for(int i=0;i<len;i++) {
      Object o=v.elementAt(i);
      if(o!=null) {
        hList.put(o,this); // todo: don't use such dummy objects
      }
    }
  }
  Expression(int type,Expression e,Expression e2) {
    iType=type;
    eArg=e;
    eArg2=e2;
  }
  Expression(String table,String column) {
    sTable=table;
    if(column==null) {
      iType=ASTERIX;
    } else {
      iType=COLUMN;
      sColumn=column;
    }
  }
  Expression(int datatype,Object o) {
    iType=VALUE;
    iDataType=datatype;
    oData=o;
  }
  void setLikeEscape(char c) {
    cLikeEscape=c;
  }
  void setDataType(int type) {
    iDataType=type;
  }
  void setTrue() {
    iType=TRUE;
  }
  boolean isAggregate() {
    if(iType==COUNT || iType==MAX || iType==MIN || iType==SUM || iType==AVG) {
      return true;
    }
    // todo: recurse eArg and eArg2; maybe they are grouped.
    // grouping 'correctly' would be quite complex
    return false;
  }
  void setDescending() {
    bDescending=true;
  }
  boolean isDescending() {
    return bDescending;
  }
  void setAlias(String s) {
    sAlias=s;
  }
  String getAlias() {
    if(sAlias!=null) {
      return sAlias;
    }
    if(iType==VALUE) {
      return "";
    }
    if(iType==COLUMN) {
      return sColumn;
    }
    // todo
    return "";
  }
  int getType() {
    return iType;
  }
  int getColumnNr() {
    return iColumn;
  }
  Expression getArg() {
    return eArg;
  }
  Expression getArg2() {
    return eArg2;
  }
  TableFilter getFilter() {
    return tFilter;
  }
  void checkResolved() throws SQLException {
    Trace.check(iType!=COLUMN || tFilter!=null,Trace.COLUMN_NOT_FOUND,sColumn);
    if(eArg!=null) {
      eArg.checkResolved();
    }
    if(eArg2!=null) {
      eArg2.checkResolved();
    }
    if(sSelect!=null) {
      sSelect.checkResolved();
    }
    if(fFunction!=null) {
      fFunction.checkResolved();
    }
  }
  void resolve(TableFilter f) throws SQLException {
    if(f!=null && iType==COLUMN) {
      if(sTable==null || f.getName().equals(sTable)) {
        int i=f.getTable().searchColumn(sColumn);
        if(i!=-1) {
          // todo: other error message: multiple tables are possible
          Trace.check(tFilter==null||tFilter==f,Trace.COLUMN_NOT_FOUND,sColumn);
          tFilter=f;
          iColumn=i;
          sTable=f.getName();
          iDataType=f.getTable().getColumnType(i);
        }
      }
    }
    // currently sets only data type
    // todo: calculate fixed expressions if possible
    if(eArg!=null) {
      eArg.resolve(f);
    }
    if(eArg2!=null) {
      eArg2.resolve(f);
    }
    if(sSelect!=null) {
      sSelect.resolve(f,false);
      sSelect.resolve();
    }
    if(fFunction!=null) {
      fFunction.resolve(f);
    }
    if(iDataType!=0) {
      return;
    }
    switch(iType) {
    case FUNCTION:
      iDataType=fFunction.getReturnType();
      break;
    case QUERY:
      iDataType=sSelect.eColumn[0].iDataType;
      break;
    case NEGATE:
      iDataType=eArg.iDataType;
      break;
    case ADD:
    case SUBTRACT:
    case MULTIPLY:
    case DIVIDE:
      iDataType=eArg.iDataType;
      break;
    case CONCAT:
      iDataType=Column.VARCHAR;
      break;
    case NOT:
    case EQUAL:
    case BIGGER_EQUAL:
    case BIGGER:
    case SMALLER:
    case SMALLER_EQUAL:
    case NOT_EQUAL:
    case LIKE:
    case AND:
    case OR:
    case IN:
    case EXISTS:
      iDataType=Column.BIT;
      break;
    case COUNT:
      iDataType=Column.INTEGER;
      break;
    case MAX:
    case MIN:
    case SUM:
    case AVG:
      iDataType=eArg.iDataType;
      break;
    case CONVERT:
      // it is already set
      break;
    case IFNULL:
    case CASEWHEN:
      iDataType=eArg2.iDataType;
      break;
    }
  }
  boolean isResolved() {
    if(iType==VALUE)  {
      return true;
    }
    if(iType==COLUMN) {
      return tFilter!=null;
    }
    // todo: could recurse here, but never miss a 'false'!
    return false;
  }
  static boolean isCompare(int i) {
    switch(i) {
    case EQUAL:
    case BIGGER_EQUAL:
    case BIGGER:
    case SMALLER:
    case SMALLER_EQUAL:
    case NOT_EQUAL:
      return true;
    }
    return false;
  }
  String getTableName() {
    if(iType==ASTERIX) {
      return sTable;
    }
    if(iType==COLUMN) {
      if(tFilter==null) {
        return sTable;
      } else {
        return tFilter.getTable().getName();
      }
    }
    // todo
    return "";
  }
  String getColumnName() {
    if(iType==COLUMN) {
      if(tFilter==null) {
        return sColumn;
      } else {
        return tFilter.getTable().getColumnName(iColumn);
      }
    }
    return getAlias();
  }
  void swapCondition() throws SQLException {
    int i=EQUAL;
    switch(iType) {
    case BIGGER_EQUAL:
      i=SMALLER_EQUAL;
      break;
    case SMALLER_EQUAL:
      i=BIGGER_EQUAL;
      break;
    case SMALLER:
      i=BIGGER;
      break;
    case BIGGER:
      i=SMALLER;
      break;
    case EQUAL:
      break;
    default:
      Trace.assert(false,"Expression.swapCondition");
    }
    iType=i;
    Expression e=eArg;
    eArg=eArg2;
    eArg2=e;
  }
  Object getValue(int type) throws SQLException {
    Object o=getValue();
    if(o==null || iDataType==type) {
      return o;
    }
    String s=Column.convertObject(o);
    return Column.convertString(s,type);
  }
  int getDataType() {
    return iDataType;
  }
  Object getValue() throws SQLException {
    switch(iType) {
    case VALUE:
      return oData;
    case COLUMN:
      try {
        return tFilter.oCurrentData[iColumn];
      } catch(NullPointerException e) {
        throw Trace.error(Trace.COLUMN_NOT_FOUND,sColumn);
      }
    case FUNCTION:
      return fFunction.getValue();
    case QUERY:
      return sSelect.getValue(iDataType);
    case NEGATE:
      return Column.negate(eArg.getValue(iDataType),iDataType);
    case COUNT:
      // count(*): sum(1); count(col): sum(col<>null)
      if(eArg.iType==ASTERIX || eArg.getValue()!=null) {
        return new Integer(1);
      }
      return new Integer(0);
    case MAX:
    case MIN:
    case SUM:
    case AVG:
      return eArg.getValue();
    case EXISTS:
      return new Boolean(test());
    case CONVERT:
      return eArg.getValue(iDataType);
    case CASEWHEN:
      if(eArg.test()) {
        return eArg2.eArg.getValue();
      } else {
        return eArg2.eArg2.getValue();
      }
    }
    // todo: simplify this
    Object a=null,b=null;
    if(eArg!=null) {
      a=eArg.getValue(iDataType);
    }
    if(eArg2!=null) {
      b=eArg2.getValue(iDataType);
    }
    switch(iType) {
    case ADD:
      return Column.add(a,b,iDataType);
    case SUBTRACT:
      return Column.subtract(a,b,iDataType);
    case MULTIPLY:
      return Column.multiply(a,b,iDataType);
    case DIVIDE:
      return Column.divide(a,b,iDataType);
    case CONCAT:
      return Column.concat(a,b);
    case IFNULL:
      return a==null ? b : a;
    default:
      // must be comparisation
      // todo: make sure it is
      return new Boolean(test());
    }
  }
  private boolean testValueList(Object o,int datatype) throws SQLException {
    if(iType==VALUELIST) {
      if(datatype!=iDataType) {
        o=Column.convertObject(o,iDataType);
      }
      return hList.containsKey(o);
    } else if(iType==QUERY) {
      // todo: convert to valuelist before if everything is resolvable
      Result r=sSelect.getResult(0);
      Record n=r.rRoot;
      int type=r.iType[0];
      if(datatype!=type) {
        o=Column.convertObject(o,type);
      }
      while(n!=null) {
        Object o2=n.data[0];
        if(o2!=null && o2==o) {
          return true;
        }
        n=n.next;
      }
      return false;
    }
    throw Trace.error(Trace.WRONG_DATA_TYPE);
  }
  boolean test() throws SQLException {
    switch(iType) {
    case TRUE:
      return true;
    case NOT:
      Trace.assert(eArg2==null,"Expression.test");
      return !eArg.test();
    case AND:
      return eArg.test() && eArg2.test();
    case OR:
      return eArg.test() || eArg2.test();
    case LIKE:
      // todo: now for all tests a new 'like' object required!
      String s=(String)eArg2.getValue(Column.VARCHAR);
      int type=eArg.iDataType;
      Like l=new Like(s,cLikeEscape,type==Column.VARCHAR_IGNORECASE);
      String c=(String)eArg.getValue(Column.VARCHAR);
      return l.compare(c);
    case IN:
      return eArg2.testValueList(eArg.getValue(),eArg.iDataType);
    case EXISTS:
      Result r=eArg.sSelect.getResult(1); // 1 is already enough
      return r.rRoot!=null;
    }
    Trace.check(eArg!=null,Trace.GENERAL_ERROR);
    Object o=eArg.getValue();
    int type=eArg.iDataType;
    Trace.check(eArg2!=null,Trace.GENERAL_ERROR);
    Object o2=eArg2.getValue(type);
    int result=Column.compare(o,o2,type);
    switch(iType) {
    case EQUAL:
      return result==0;
    case BIGGER:
      return result>0;
    case BIGGER_EQUAL:
      return result>=0;
    case SMALLER_EQUAL:
      return result<=0;
    case SMALLER:
      return result<0;
    case NOT_EQUAL:
      return result!=0;
    }
    Trace.assert(false,"Expression.test2");
    return false;
  }
}
