/*
 * Library.java
 */

package org.alicebot.server.sql;
import java.sql.*;
import java.util.*;
import java.text.*;

class Library {
  final static String sNumeric[]={
  "ABS","java.lang.Math.abs",
  "ACOS","java.lang.Math.acos",
  "ASIN","java.lang.Math.asin",
  "ATAN","java.lang.Math.atan",
  "ATAN2","java.lang.Math.atan2",
  "CEILING","java.lang.Math.ceil",
  "COS","java.lang.Math.cos",
  "COT","org.alicebot.server.sql.Library.cot",
  "DEGREES","java.lang.Math.toDegrees",
  "EXP","java.lang.Math.exp",
  "FLOOR","java.lang.Math.floor",
  "LOG","java.lang.Math.log",
  "LOG10","org.alicebot.server.sql.Library.log10",
  "MOD","org.alicebot.server.sql.Library.mod",
  "PI","org.alicebot.server.sql.Library.pi",
  "POWER","java.lang.Math.pow",
  "RADIANS","java.lang.Math.toRadians",
  "RAND","java.lang.Math.random",
  "ROUND","org.alicebot.server.sql.Library.round",
  "SIGN","org.alicebot.server.sql.Library.sign",
  "SIN","java.lang.Math.sin",
  "SQRT","java.lang.Math.sqrt",
  "TAN","java.lang.Math.tan",
  "TRUNCATE","org.alicebot.server.sql.Library.truncate",
  "BITAND","org.alicebot.server.sql.Library.bitand",
  "BITOR","org.alicebot.server.sql.Library.bitor",
  "ROUNDMAGIC","org.alicebot.server.sql.Library.roundMagic",
  };
  final static String sString[]={
  "ASCII","org.alicebot.server.sql.Library.ascii",
  "CHAR","org.alicebot.server.sql.Library.character",
  "CONCAT","org.alicebot.server.sql.Library.concat",
  "DIFFERENCE","org.alicebot.server.sql.Library.difference",
  "INSERT","org.alicebot.server.sql.Library.insert",
  "LCASE","org.alicebot.server.sql.Library.lcase",
  "LEFT","org.alicebot.server.sql.Library.left",
  "LENGTH","org.alicebot.server.sql.Library.length",
  "LOCATE","org.alicebot.server.sql.Library.locate",
  "LTRIM","org.alicebot.server.sql.Library.ltrim",
  "REPEAT","org.alicebot.server.sql.Library.repeat",
  "REPLACE","org.alicebot.server.sql.Library.replace",
  "RIGHT","org.alicebot.server.sql.Library.right",
  "RTRIM","org.alicebot.server.sql.Library.rtrim",
  "SOUNDEX","org.alicebot.server.sql.Library.soundex",
  "SPACE","org.alicebot.server.sql.Library.space",
  "SUBSTRING","org.alicebot.server.sql.Library.substring",
  "UCASE","org.alicebot.server.sql.Library.ucase",
  "LOWER","org.alicebot.server.sql.Library.lcase",
  "UPPER","org.alicebot.server.sql.Library.ucase"
  };
  final static String sTimeDate[]={
  "CURDATE","org.alicebot.server.sql.Library.curdate",
  "CURTIME","org.alicebot.server.sql.Library.curtime",
  "DAYNAME","org.alicebot.server.sql.Library.dayname",
  "DAYOFMONTH","org.alicebot.server.sql.Library.dayofmonth",
  "DAYOFWEEK","org.alicebot.server.sql.Library.dayofweek",
  "DAYOFYEAR","org.alicebot.server.sql.Library.dayofyear",
  "HOUR","org.alicebot.server.sql.Library.hour",
  "MINUTE","org.alicebot.server.sql.Library.minute",
  "MONTH","org.alicebot.server.sql.Library.month",
  "MONTHNAME","org.alicebot.server.sql.Library.monthname",
  "NOW","org.alicebot.server.sql.Library.now",
  "QUARTER","org.alicebot.server.sql.Library.quarter",
  "SECOND","org.alicebot.server.sql.Library.second",
  "WEEK","org.alicebot.server.sql.Library.week",
  "YEAR","org.alicebot.server.sql.Library.year",
  };
  final static String sSystem[]={
  "DATABASE","org.alicebot.server.sql.Library.database",
  "USER","org.alicebot.server.sql.Library.user",
  "IDENTITY","org.alicebot.server.sql.Library.identity"
  };

  static void register(Hashtable h) {
    register(h,sNumeric);
    register(h,sString);
    register(h,sTimeDate);
    register(h,sSystem);
  }
  private static void register(Hashtable h,String s[]) {
    for(int i=0;i<s.length;i+=2) {
      h.put(s[i],s[i+1]);
    }
  }
  static Random rRandom=new Random();

  // NUMERIC
  public static double rand(Integer i) {
    if(i!=null) {
      rRandom.setSeed(i.intValue());
    }
    return rRandom.nextDouble();
  }
  // this magic number works for 100000000000000; but not for 0.1 and 0.01
  static double LOG10_FACTOR=0.43429448190325183;
  public static double log10(double x) {
    return roundMagic(Math.log(x) * LOG10_FACTOR);
  }
  public static double roundMagic(double d) {
    // this function rounds numbers in a good way but slow:
    // - special handling for numbers around 0
    // - only numbers <= +/-1000000000000
    // - convert to a string
    // - check the last 4 characters:
    //   '000x' becomes '0000'
    //   '999x' becomes '999999' (this is rounded automatically)
    if(d<0.0000000000001 && d>-0.0000000000001) {
      return 0.0;
    }
    if(d>1000000000000. || d<-1000000000000.) {
      return d;
    }
    StringBuffer s=new StringBuffer();
    s.append(d);
    int len=s.length();
    if(len<16) {
      return d;
    }
    char cx=s.charAt(len-1);
    char c1=s.charAt(len-2);
    char c2=s.charAt(len-3);
    char c3=s.charAt(len-4);
    if(c1=='0' && c2=='0' && c3=='0' && cx!='.') {
      s.setCharAt(len-1,'0');
    } else if(c1=='9' && c2=='9' && c3=='9' && cx!='.') {
      s.setCharAt(len-1,'9');
      s.append('9');
      s.append('9');
    }
    return new Double(s.toString()).doubleValue();
  }
  public static double cot(double d) {
    return 1./Math.tan(d);
  }
  public static int mod(int i1,int i2) {
    return i1 % i2;
  }
  public static double pi() {
    return Math.PI;
  }
  public static double round(double d,int p) {
    double f=Math.pow(10.,p);
    return Math.round(d*f)/f;
  }
  public static int sign(double d) {
    return d<0 ? -1 : (d>0 ? 1 : 0);
  }
  public static double truncate(double d,int p) {
    double f=Math.pow(10.,p);
    double g=d*f;
    return ((d<0) ? Math.ceil(g) : Math.floor(g)) / f;
  }
  public static int bitand(int i,int j) {
    return i & j;
  }
  public static int bitor(int i,int j) {
    return i | j;
  }

  // STRING
  public static Integer ascii(String s) {
    if(s==null || s.length()==0) {
      return null;
    }
    return new Integer(s.charAt(0));
  }
  public static String character(int code) {
    return ""+(char)code;
  }
  public static String concat(String s1,String s2) {
    if(s1==null) {
      if(s2==null) {
        return null;
      }
      return s2;
    }
    if(s2==null) {
      return s1;
    }
    return s1+s2;
  }
  public static int difference(String s1,String s2) {
    // todo: check if this is the standard algorithm
    if(s1==null || s2==null) {
      return 0;
    }
    s1=soundex(s1);
    s2=soundex(s2);
    int len1=s1.length(),len2=s2.length();
    int e=0;
    for(int i=0;i<4;i++) {
      if(i>=len1 || i>=len2 || s1.charAt(i)!=s2.charAt(i)) {
        e++;
      }
    }
    return e;
  }
  public static String insert(String s1,int start,int length,String s2) {
    if(s1==null) {
      return s2;
    }
    if(s2==null) {
      return s1;
    }
    int len1=s1.length();
    int len2=s2.length();
    start--;
    if(start<0 || length<=0 || len2==0 || start>len1) {
      return s1;
    }
    if(start+length>len1) {
      length=len1-start;
    }
    return s1.substring(0,start)+s2+s1.substring(start+length);
  }
  public static String lcase(String s) {
    return s==null ? null : s.toLowerCase();
  }
  public static String left(String s,int i) {
    return s==null ? null : s.substring(0,(i<0?0:i<s.length()?i:s.length()));
  }
  public static int length(String s) {
    return (s==null || s.length()<1)? 0 : s.length();
  }
  public static int locate(String search,String s,Integer start) {
    if(s==null || search==null) {
      return 0;
    }
    int i=start==null ? 0 : start.intValue()-1;
    return s.indexOf(search,i<0 ? 0 : i)+1;
  }
  public static String ltrim(String s) {
    if(s==null) {
      return s;
    }
    int len=s.length(),i=0;
    while(i<len && s.charAt(i)<=' ') {
      i++;
    }
    return i==0 ? s : s.substring(i);
  }
  public static String repeat(String s,int i) {
    if(s==null) {
      return null;
    }
    StringBuffer b=new StringBuffer();
    while(i-->0) {
      b.append(s);
    }
    return b.toString();
  }
  public static String replace(String s,String replace,String with) {
    if(s==null || replace==null) {
      return s;
    }
    if(with==null) {
      with="";
    }
    StringBuffer b=new StringBuffer();
    int start=0;
    int lenreplace=replace.length();
    while(true) {
      int i=s.indexOf(replace,start);
      if(i==-1) {
        b.append(s.substring(start));
        break;
      }
      b.append(s.substring(start,i-start));
      b.append(with);
      start=i+lenreplace;
    }
    return b.toString();
  }
  public static String right(String s,int i) {
    if(s==null) {
      return null;
    }
    i=s.length()-i;
    return s.substring(i<0?0:i<s.length()?i:s.length());
  }
  public static String rtrim(String s) {
    if(s==null) {
      return s;
    }
    int i=s.length()-1;
    while(i>=0 && s.charAt(i)<=' ') {
      i--;
    }
    return i==s.length() ? s : s.substring(0,i+1);
  }
  public static String soundex(String s) {
    if(s==null) {
      return s;
    }
    s=s.toUpperCase();
    int len=s.length();
    char b[]=new char[4];
    b[0]=s.charAt(0);
    int j=1;
    for(int i=1;i<len && j<4;i++) {
      char c=s.charAt(i);
      if("BFPV".indexOf(c)!=-1) {
        b[j++]='1';
      } else if("CGJKQSXZ".indexOf(c)!=-1) {
        b[j++]='2';
      } else if(c=='D' || c=='T') {
        b[j++]='3';
      } else if(c=='L') {
        b[j++]='4';
      } else if(c=='M' || c=='N') {
        b[j++]='5';
      } else if(c=='R') {
        b[j++]='6';
      }
    }
    return new String(b,0,j);
  }
  public static String space(int i) {
    if(i<0) {
      return null;
    }
    char c[]=new char[i];
    while(i>0) {
      c[--i]=' ';
    }
    return new String(c);
  }
  public static String substring(String s,int start,Integer length) {
    if(s==null) {
      return null;
    }
    int len=s.length();
    start--;
    start=start>len ? len : start;
    if(length==null) {
      return s.substring(start);
    } else {
      int l=length.intValue();
      return s.substring(start,start+l>len ? len : l);
    }
  }
  public static String ucase(String s) {
    return s==null ? null : s.toUpperCase();
  }

  // TIME AND DATE
  public static java.sql.Date curdate() {
    return new java.sql.Date(System.currentTimeMillis());
  }
  public static java.sql.Time curtime() {
    return new java.sql.Time(System.currentTimeMillis());
  }
  public static String dayname(java.sql.Date d) {
    SimpleDateFormat f=new SimpleDateFormat("EEEE");
    return f.format(d).toString();
  }
  private static int getDateTimePart(java.util.Date d,int part) {
    Calendar c=new GregorianCalendar();
    c.setTime(d);
    return c.get(part);
  }
  private static int getTimePart(java.sql.Time t,int part) {
    Calendar c=new GregorianCalendar();
    c.setTime(t);
    return c.get(part);
  }
  public static int dayofmonth(java.sql.Date d) {
    return getDateTimePart(d,Calendar.DAY_OF_MONTH);
  }
  public static int dayofweek(java.sql.Date d) {
    return getDateTimePart(d,Calendar.DAY_OF_WEEK);
  }
  public static int dayofyear(java.sql.Date d) {
    return getDateTimePart(d,Calendar.DAY_OF_YEAR);
  }
  public static int hour(java.sql.Time t) {
    return getDateTimePart(t,Calendar.HOUR);
  }
  public static int minute(java.sql.Time t) {
    return getDateTimePart(t,Calendar.MINUTE);
  }
  public static int month(java.sql.Date d) {
    return getDateTimePart(d,Calendar.MONTH);
  }
  public static String monthname(java.sql.Date d) {
    SimpleDateFormat f=new SimpleDateFormat("MMMM");
    return f.format(d).toString();
  }
  public static Timestamp now() {
    return new Timestamp(System.currentTimeMillis());
  }
  public static int quarter(java.sql.Date d) {
    return (getDateTimePart(d,Calendar.MONTH)/3)+1;
  }
  public static int second(java.sql.Date d) {
    return getDateTimePart(d,Calendar.SECOND);
  }
  public static int week(java.sql.Date d) {
    return getDateTimePart(d,Calendar.WEEK_OF_YEAR);
  }
  public static int year(java.sql.Date d) {
    return getDateTimePart(d,Calendar.YEAR);
  }

  // SYSTEM
  public static String database(Connection conn) throws SQLException {
    Statement stat=conn.createStatement();
    String s="SELECT Value FROM SYSTEM_CONNECTIONINFO WHERE KEY='DATABASE'";
    ResultSet r=stat.executeQuery(s);
    r.next();
    return r.getString(1);
  }
  public static String user(Connection conn) throws SQLException {
    Statement stat=conn.createStatement();
    String s="SELECT Value FROM SYSTEM_CONNECTIONINFO WHERE KEY='USER'";
    ResultSet r=stat.executeQuery(s);
    r.next();
    return r.getString(1);
  }
  public static int identity(Connection conn) throws SQLException {
    Statement stat=conn.createStatement();
    String s="SELECT VALUE FROM SYSTEM_CONNECTIONINFO WHERE KEY='IDENTITY'";
    ResultSet r=stat.executeQuery(s);
    r.next();
    return r.getInt(1);
  }
}
