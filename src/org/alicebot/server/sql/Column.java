/*
 * Column.java
 */

package org.alicebot.server.sql;
import java.sql.*;
import java.math.BigDecimal;
import java.io.*;
import java.util.*;

class Column {
  private static Hashtable hTypes;
  final static int BIT=Types.BIT;                     //   -7
  final static int TINYINT=Types.TINYINT;             //   -6
  final static int BIGINT=Types.BIGINT;               //   -5
  final static int LONGVARBINARY=Types.LONGVARBINARY; //   -4
  final static int VARBINARY=Types.VARBINARY;         //   -3
  final static int BINARY=Types.BINARY;               //   -2
  final static int LONGVARCHAR=Types.LONGVARCHAR;     //   -1
  final static int CHAR=Types.CHAR;                   //    1
  final static int NUMERIC=Types.NUMERIC;             //    2
  final static int DECIMAL=Types.DECIMAL;             //    3
  final static int INTEGER=Types.INTEGER;             //    4
  final static int SMALLINT=Types.SMALLINT;           //    5
  final static int FLOAT=Types.FLOAT;                 //    6
  final static int REAL=Types.REAL;                   //    7
  final static int DOUBLE=Types.DOUBLE;               //    8
  final static int VARCHAR=Types.VARCHAR;             //   12
  final static int DATE=Types.DATE;                   //   91
  final static int TIME=Types.TIME;                   //   92
  final static int TIMESTAMP=Types.TIMESTAMP;         //   93
  final static int OTHER=Types.OTHER;                 // 1111
  final static int NULL=Types.NULL;                   //    0
  final static int VARCHAR_IGNORECASE=100; // this is the only non-standard type
  // NULL and VARCHAR_IGNORECASE is not part of TYPES
  final static int TYPES[]=
  {BIT,TINYINT,BIGINT,LONGVARBINARY,VARBINARY,BINARY,LONGVARCHAR,CHAR,
  NUMERIC,DECIMAL,INTEGER,SMALLINT,FLOAT,REAL,DOUBLE,VARCHAR,DATE,TIME,
  TIMESTAMP,OTHER};
  String sName;
  int iType;
  private boolean bNullable;
  private boolean bIdentity;
  static {
    hTypes=new Hashtable();
    addTypes(INTEGER,"INTEGER","int","java.lang.Integer");
    addType(INTEGER,"INT");
    addTypes(DOUBLE,"DOUBLE","double","java.lang.Double");
    addType(FLOAT,"FLOAT"); // this is a Double
    addTypes(VARCHAR,"VARCHAR","java.lang.String",null);
    addTypes(CHAR,"CHAR","CHARACTER",null);
    addType(LONGVARCHAR,"LONGVARCHAR");
    // for ignorecase data types, the 'original' type name is lost
    addType(VARCHAR_IGNORECASE,"VARCHAR_IGNORECASE");
    addTypes(DATE,"DATE","java.sql.Date",null);
    addTypes(TIME,"TIME","java.sql.Time",null);
    // DATETIME is for compatibility with MS SQL 7
    addTypes(TIMESTAMP,"TIMESTAMP","java.sql.Timestamp","DATETIME");
    addTypes(DECIMAL,"DECIMAL","java.math.BigDecimal",null);
    addType(NUMERIC,"NUMERIC");
    addTypes(BIT,"BIT","java.lang.Boolean","boolean");
    addTypes(TINYINT,"TINYINT","java.lang.Short","short");
    addType(SMALLINT,"SMALLINT");
    addTypes(BIGINT,"BIGINT","java.lang.Long","long");
    addTypes(REAL,"REAL","java.lang.Float","float");
    addTypes(BINARY,"BINARY","byte[]",null); // maybe better "[B"
    addType(VARBINARY,"VARBINARY");
    addType(LONGVARBINARY,"LONGVARBINARY");
    addTypes(OTHER,"OTHER","java.lang.Object","OBJECT");
  }
  private static void addTypes(int type,String name,String n2,String n3) {
    addType(type,name);
    addType(type,n2);
    addType(type,n3);
  }
  private static void addType(int type,String name) {
    if(name!=null) {
      hTypes.put(name,new Integer(type));
    }
  }

  Column(String name,boolean nullable,int type,boolean identity) {
    sName=name;
    bNullable=nullable;
    iType=type;
    bIdentity=identity;
  }
  boolean isIdentity() {
    return bIdentity;
  }
  static int getTypeNr(String type) throws SQLException {
    Integer i=(Integer)hTypes.get(type);
    Trace.check(i!=null,Trace.WRONG_DATA_TYPE,type);
    return i.intValue();
  }
  static String getType(int type) throws SQLException {
    switch(type) {
    case NULL:
      return "NULL";
    case INTEGER:
      return "INTEGER";
    case DOUBLE:
      return "DOUBLE";
    case VARCHAR_IGNORECASE:
      return "VARCHAR_IGNORECASE";
    case VARCHAR:
      return "VARCHAR";
    case CHAR:
      return "CHAR";
    case LONGVARCHAR:
      return "LONGVARCHAR";
    case DATE:
      return "DATE";
    case TIME:
      return "TIME";
    case DECIMAL:
      return "DECIMAL";
    case BIT:
      return "BIT";
    case TINYINT:
      return "TINYINT";
    case SMALLINT:
      return "SMALLINT";
    case BIGINT:
      return "BIGINT";
    case REAL:
      return "REAL";
    case FLOAT:
      return "FLOAT";
    case NUMERIC:
      return "NUMERIC";
    case TIMESTAMP:
      return "TIMESTAMP";
    case BINARY:
      return "BINARY";
    case VARBINARY:
      return "VARBINARY";
    case LONGVARBINARY:
      return "LONGVARBINARY";
    case OTHER:
      return "OBJECT";
    default:
      throw Trace.error(Trace.WRONG_DATA_TYPE,type);
    }
  }
  boolean isNullable() {
    return bNullable;
  }
  static Object add(Object a,Object b,int type) throws SQLException {
    if(a==null || b==null) {
      return null;
    }
    switch(type) {
    case NULL:
      return null;
    case INTEGER:
      int ai=((Integer)a).intValue();
      int bi=((Integer)b).intValue();
      return new Integer(ai+bi);
    case FLOAT:
    case DOUBLE:
      double ad=((Double)a).doubleValue();
      double bd=((Double)b).doubleValue();
      return new Double(ad+bd);
    case VARCHAR:
    case CHAR:
    case LONGVARCHAR:
    case VARCHAR_IGNORECASE:
      return (String)a+(String)b;
    case NUMERIC:
    case DECIMAL:
      BigDecimal abd=(BigDecimal)a;
      BigDecimal bbd=(BigDecimal)b;
      return abd.add(bbd);
    case TINYINT:
    case SMALLINT:
      short shorta=((Short)a).shortValue();
      short shortb=((Short)b).shortValue();
      return new Short((short)(shorta+shortb));
    case BIGINT:
      long longa=((Long)a).longValue();
      long longb=((Long)b).longValue();
      return new Long(longa+longb);
    case REAL:
      float floata=((Float)a).floatValue();
      float floatb=((Float)b).floatValue();
      return new Float(floata+floatb);
    default:
      throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED,type);
    }
  }
  static Object concat(Object a,Object b) throws SQLException {
    if(a==null) {
      return b;
    } else if(b==null) {
      return a;
    }
    return convertObject(a)+convertObject(b);
  }
  static Object negate(Object a,int type) throws SQLException {
    if(a==null) {
      return null;
    }
    switch(type) {
    case NULL:
      return null;
    case INTEGER:
      return new Integer(-((Integer)a).intValue());
    case FLOAT:
    case DOUBLE:
      return new Double(-((Double)a).doubleValue());
    case NUMERIC:
    case DECIMAL:
      return ((BigDecimal)a).negate();
    case TINYINT:
    case SMALLINT:
      return new Short((short)-((Short)a).shortValue());
    case BIGINT:
      return new Long(-((Long)a).longValue());
    case REAL:
      return new Float(-((Float)a).floatValue());
    default:
      throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED,type);
    }
  }
  static Object multiply(Object a,Object b,int type) throws SQLException {
    if(a==null || b==null) {
      return null;
    }
    switch(type) {
    case NULL:
      return null;
    case INTEGER:
      int ai=((Integer)a).intValue();
      int bi=((Integer)b).intValue();
      return new Integer(ai*bi);
    case FLOAT:
    case DOUBLE:
      double ad=((Double)a).doubleValue();
      double bd=((Double)b).doubleValue();
      return new Double(ad*bd);
    case NUMERIC:
    case DECIMAL:
      BigDecimal abd=(BigDecimal)a;
      BigDecimal bbd=(BigDecimal)b;
      return abd.multiply(bbd);
    case TINYINT:
    case SMALLINT:
      short shorta=((Short)a).shortValue();
      short shortb=((Short)b).shortValue();
      return new Short((short)(shorta*shortb));
    case BIGINT:
      long longa=((Long)a).longValue();
      long longb=((Long)b).longValue();
      return new Long(longa*longb);
    case REAL:
      float floata=((Float)a).floatValue();
      float floatb=((Float)b).floatValue();
      return new Float(floata*floatb);
    default:
      throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED,type);
    }
  }
  static Object divide(Object a,Object b,int type) throws SQLException {
    if(a==null || b==null) {
      return null;
    }
    switch(type) {
    case NULL:
      return null;
    case INTEGER:
      int ai=((Integer)a).intValue();
      int bi=((Integer)b).intValue();
      Trace.check(bi!=0,Trace.DIVISION_BY_ZERO);
      return new Integer(ai/bi);
    case FLOAT:
    case DOUBLE:
      double ad=((Double)a).doubleValue();
      double bd=((Double)b).doubleValue();
      return bd==0 ? null : new Double(ad/bd);
    case NUMERIC:
    case DECIMAL:
      BigDecimal abd=(BigDecimal)a;
      BigDecimal bbd=(BigDecimal)b;
      return bbd.signum()==0?null:abd.divide(bbd,BigDecimal.ROUND_HALF_DOWN);
    case TINYINT:
    case SMALLINT:
      short shorta=((Short)a).shortValue();
      short shortb=((Short)b).shortValue();
      return shortb==0 ? null : new Short((short)(shorta/shortb));
    case BIGINT:
      long longa=((Long)a).longValue();
      long longb=((Long)b).longValue();
      return longb==0 ? null : new Long(longa/longb);
    case REAL:
      float floata=((Float)a).floatValue();
      float floatb=((Float)b).floatValue();
      return floatb==0 ? null : new Float(floata/floatb);
    default:
      throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED,type);
    }
  }
  static Object subtract(Object a,Object b,int type) throws SQLException {
    if(a==null || b==null) {
      return null;
    }
    switch(type) {
    case NULL:
      return null;
    case INTEGER:
      int ai=((Integer)a).intValue();
      int bi=((Integer)b).intValue();
      return new Integer(ai-bi);
    case FLOAT:
    case DOUBLE:
      double ad=((Double)a).doubleValue();
      double bd=((Double)b).doubleValue();
      return new Double(ad-bd);
    case NUMERIC:
    case DECIMAL:
      BigDecimal abd=(BigDecimal)a;
      BigDecimal bbd=(BigDecimal)b;
      return abd.subtract(bbd);
    case TINYINT:
    case SMALLINT:
      short shorta=((Short)a).shortValue();
      short shortb=((Short)b).shortValue();
      return new Short((short)(shorta-shortb));
    case BIGINT:
      long longa=((Long)a).longValue();
      long longb=((Long)b).longValue();
      return new Long(longa-longb);
    case REAL:
      float floata=((Float)a).floatValue();
      float floatb=((Float)b).floatValue();
      return new Float(floata-floatb);
    default:
      throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED,type);
    }
  }
  static Object sum(Object a,Object b,int type) throws SQLException {
    if(a==null) {
      return b;
    }
    if(b==null) {
      return a;
    }
    switch(type) {
    case NULL:
      return null;
    case INTEGER:
      return new Integer(((Integer)a).intValue()+((Integer)b).intValue());
    case FLOAT:
    case DOUBLE:
      return new Double(((Double)a).doubleValue()+((Double)b).doubleValue());
    case NUMERIC:
    case DECIMAL:
      return ((BigDecimal)a).add((BigDecimal)b);
    case TINYINT:
    case SMALLINT:
      return new Short((short)(((Short)a).shortValue()+
      ((Short)b).shortValue()));
    case BIGINT:
      return new Long(((Long)a).longValue()+((Long)b).longValue());
    case REAL:
      return new Float(((Float)a).floatValue()+((Float)b).floatValue());
    default:
      Trace.error(Trace.SUM_OF_NON_NUMERIC);
    }
    return null;
  }
  static Object avg(Object a,int type,int count) throws SQLException {
    if(a==null || count==0) {
      return null;
    }
    switch(type) {
    case NULL:
      return null;
    case INTEGER:
      return new Integer(((Integer)a).intValue()/count);
    case FLOAT:
    case DOUBLE:
      return new Double(((Double)a).doubleValue()/count);
    case NUMERIC:
    case DECIMAL:
      return ((BigDecimal)a).divide(
      new BigDecimal(count),BigDecimal.ROUND_HALF_DOWN);
    case TINYINT:
    case SMALLINT:
      return new Short((short)(((Short)a).shortValue()/count));
    case BIGINT:
      return new Long(((Long)a).longValue()/count);
    case REAL:
      return new Float(((Float)a).floatValue()/count);
    default:
      Trace.error(Trace.SUM_OF_NON_NUMERIC);
    }
    return null;
  }
  static Object min(Object a,Object b,int type) throws SQLException {
    if(a==null) {
      return b;
    }
    if(b==null) {
      return a;
    }
    if(compare(a,b,type)<0) {
      return a;
    }
    return b;
  }
  static Object max(Object a,Object b,int type) throws SQLException {
    if(a==null) {
      return b;
    }
    if(b==null) {
      return a;
    }
    if(compare(a,b,type)>0) {
      return a;
    }
    return b;
  }
  static int compare(Object a,Object b,int type) throws SQLException {
    int i=0;
    // null handling: null==null and smaller any value
    // todo: implement standard SQL null handling
    // it is also used for grouping ('null' is one group)
    if(a==null) {
      if(b==null) {
        return 0;
      }
      return -1;
    }
    if(b==null) {
      return 1;
    }
    switch(type) {
    case NULL:
      return 0;
    case INTEGER:
      int ai=((Integer)a).intValue();
      int bi=((Integer)b).intValue();
      return (ai>bi) ? 1 : (bi>ai ? -1 : 0);
    case FLOAT:
    case DOUBLE:
      double ad=((Double)a).doubleValue();
      double bd=((Double)b).doubleValue();
      return (ad>bd) ? 1 : (bd>ad ? -1 : 0);
    case VARCHAR:
    case CHAR:
    case LONGVARCHAR:
      i=((String)a).compareTo((String)b);
      break;
    case VARCHAR_IGNORECASE:
      // for jdk 1.1 compatibility; jdk 1.2 could use compareToIgnoreCase
      i=((String)a).toUpperCase().compareTo(((String)b).toUpperCase());
      break;
    case DATE:
      if(((java.sql.Date)a).after((java.sql.Date)b)) {
        return 1;
      } else if(((java.sql.Date)a).before((java.sql.Date)b)) {
        return -1;
      } else {
        return 0;
      }
    case TIME:
      if(((Time)a).after((Time)b)) {
        return 1;
      } else if(((Time)a).before((Time)b)) {
        return -1;
      } else {
        return 0;
      }
    case TIMESTAMP:
      if(((Timestamp)a).after((Timestamp)b)) {
        return 1;
      } else if(((Timestamp)a).before((Timestamp)b)) {
        return -1;
      } else {
        return 0;
      }
    case NUMERIC:
    case DECIMAL:
      i=((BigDecimal)a).compareTo((BigDecimal)b);
      break;
    case BIT:
      boolean boola=((Boolean)a).booleanValue();
      boolean boolb=((Boolean)b).booleanValue();
      return (boola==boolb) ? 0 : (boolb ? -1 : 1);
    case TINYINT:
    case SMALLINT:
      short shorta=((Short)a).shortValue();
      short shortb=((Short)b).shortValue();
      return (shorta>shortb) ? 1 : (shortb>shorta ? -1 : 0);
    case BIGINT:
      long longa=((Long)a).longValue();
      long longb=((Long)b).longValue();
      return (longa>longb) ? 1 : (longb>longa ? -1 : 0);
    case REAL:
      float floata=((Float)a).floatValue();
      float floatb=((Float)b).floatValue();
      return (floata>floatb) ? 1 : (floatb>floata ? -1 : 0);
    case BINARY:
    case VARBINARY:
    case LONGVARBINARY:
    case OTHER:
      i=((ByteArray)a).compareTo((ByteArray)b);
      break;
    default:
      throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED,type);
    }
    return (i>0) ? 1 : (i<0 ? -1 : 0);
  }
  static Object convertString(String s,int type) throws SQLException {
    if(s==null) {
      return null;
    }
    switch(type) {
    case NULL:
      return null;
    case INTEGER:
      return new Integer(s);
    case FLOAT:
    case DOUBLE:
      return new Double(s);
    case VARCHAR_IGNORECASE:
    case VARCHAR:
    case CHAR:
    case LONGVARCHAR:
      return s;
    case DATE:
      return java.sql.Date.valueOf(s);
    case TIME:
      return Time.valueOf(s);
    case TIMESTAMP:
      return Timestamp.valueOf(s);
    case NUMERIC:
    case DECIMAL:
      return new BigDecimal(s.trim());
    case BIT:
      return new Boolean(s);
    case TINYINT:
    case SMALLINT:
      return new Short(s);
    case BIGINT:
      return new Long(s);
    case REAL:
      return new Float(s);
    case BINARY:
    case VARBINARY:
    case LONGVARBINARY:
    case OTHER:
      return new ByteArray(s);
    default:
      throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED,type);
    }
  }
  static String convertObject(Object o) {
    if(o==null) {
      return null;
    }
    return o.toString();
  }
  static Object convertObject(Object o,int type) throws SQLException {
    if(o==null) {
      return null;
    }
    return convertString(o.toString(),type);
  }
  static String createString(Object o,int type) throws SQLException {
    if(o==null) {
      return "NULL";
    }
    switch(type) {
    case NULL:
      return "NULL";
    case BINARY:
    case VARBINARY:
    case LONGVARBINARY:
    case DATE:
    case TIME:
    case TIMESTAMP:
    case OTHER:
      return "'"+o.toString()+"'";
    case VARCHAR_IGNORECASE:
    case VARCHAR:
    case CHAR:
    case LONGVARCHAR:
      return createString((String)o);
    default:
      return o.toString();
    }
  }
  static String createString(String s) {
    StringBuffer b=new StringBuffer().append('\'');
    if(s!=null) {
      for(int i=0,len=s.length();i<len;i++) {
        char c=s.charAt(i);
        if(c=='\'') {
          b.append(c);
        }
        b.append(c);
      }
    }
    return b.append('\'').toString();
  }
  static Object[] readData(DataInput in,int l) throws IOException,SQLException {
    Object data[]=new Object[l];
    for(int i=0;i<l;i++) {
      int type=in.readInt();
      Object o=null;
      switch(type) {
      case NULL:
        o=null;
        break;
      case INTEGER:
        o=new Integer(in.readInt());
        break;
      case FLOAT:
      case DOUBLE:
        o=new Double(Double.longBitsToDouble(in.readLong()));
        // some JDKs have a problem with this:
        //o=new Double(in.readDouble());
        break;
      case VARCHAR_IGNORECASE:
      case VARCHAR:
      case CHAR:
      case LONGVARCHAR:
        o=in.readUTF();
        break;
      case DATE:
        o=java.sql.Date.valueOf(in.readUTF());
        break;
      case TIME:
        o=Time.valueOf(in.readUTF());
        break;
      case TIMESTAMP:
        o=Timestamp.valueOf(in.readUTF());
        break;
      case NUMERIC:
      case DECIMAL:
        o=new BigDecimal(in.readUTF());
        break;
      case BIT:
        o=new Boolean(in.readUTF());
        break;
      case TINYINT:
      case SMALLINT:
        o=new Short(in.readUTF());
        break;
      case BIGINT:
        o=new Long(in.readUTF());
        break;
      case REAL:
        o=new Float(in.readUTF());
        break;
      case BINARY:
      case VARBINARY:
      case LONGVARBINARY:
      case OTHER:
        // todo: directly use byte array
        o=new ByteArray(in.readUTF());
        break;
      default:
        throw Trace.error(Trace.FUNCTION_NOT_SUPPORTED,type);
      }
      data[i]=o;
    }
    return data;
  }
  static void writeData(DataOutput out,Object data[],Table t)
  throws IOException {
    int len=t.getInternalColumnCount();
    int type[]=new int[len];
    for(int i=0;i<len;i++) {
      type[i]=t.getType(i);
    }
    writeData(out,len,type,data);
  }
  static void writeData(DataOutput out,int l,int type[],Object data[])
  throws IOException {
    for(int i=0;i<l;i++) {
      Object o=data[i];
      if(o==null) {
        out.writeInt(NULL);
      } else {
        int t=type[i];
        out.writeInt(t);
        switch(t) {
        case INTEGER:
          out.writeInt(((Integer)o).intValue());
          break;
        case FLOAT:
        case DOUBLE:
          out.writeLong(Double.doubleToLongBits(((Double)o).doubleValue()));
          // some JDKs have a problem with this:
          //out.writeDouble(((Double)o).doubleValue());
          break;
        default:
          out.writeUTF(o.toString());
          break;
        }
      }
    }
  }
  static int getSize(Object data[],Table t) {
    int l=data.length;
    int type[]=new int[l];
    for(int i=0;i<l;i++) {
      type[i]=t.getType(i);
    }
    return getSize(data,l,type);
  }
  private static int getSize(Object data[],int l,int type[]) {
    int s=0;
    for(int i=0;i<l;i++) {
      Object o=data[i];
      s+=4; // type
      if(o!=null) {
        switch(type[i]) {
        case INTEGER:
          s+=4;
          break;
        case FLOAT:
        case DOUBLE:
          s+=8;
          break;
        default:
          s+=getUTFsize(o.toString());
        }
      }
    }
    return s;
  }
  private static int getUTFsize(String s) {
    // a bit bigger is not really a problem, but never smaller!
    if(s==null) {
      s="";
    }
    int len=s.length();
    int l=2;
    for(int i=0;i<len;i++) {
      int c=s.charAt(i);
	    if((c>=0x0001) && (c<=0x007F)) {
        l++;
	    } else if(c>0x07FF) {
        l+=3;
	    } else {
        l+=2;
	    }
    }
    return l;
  }
}

