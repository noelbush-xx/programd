/*
 * Profile.java
 */

package org.alicebot.server.sql;
import java.io.*;
import java.util.*;

/**
 * <font color="#009900">
 * This class is used to test and profile Hypersonic SQL.
 * Before it can be used, the source code of the database must
 * be changed using CodeSwitcher, then compiled, then the test
 * program must be run, and at the end the static function
 * listUnvisited must be called. This will list all not
 * visited lines and it will print out the top 20 lines with
 * the most visits, the longest run and the longest run per call.
 * <P>
 * This class is generic can be used to profile also other
 * programs.
 * </font>
 */

public class Profile {
  static Profile main=new Profile();
  final static int TOP=20;
  final static int VISIT=0,TIME=1,PERCALL=2;
  Hashtable hash=new Hashtable();
  boolean bVisited;
  boolean bStop;
  File fLastFile;
  int iLastLine;
  long lLastTime=System.currentTimeMillis();

  public static void visit(String file,int line,int maxline) {  //+
    main.m_visit(file,line,maxline);                            //+
  }                                                             //+
  public static void listUnvisited() {                          //+
    main.m_listUnvisited();                                     //+
  }                                                             //+
  class File {                                                  //+
    String sName;                                               //+
    int iMaxLine;                                               //+
    long val[][];                                               //+
    File(String name,int max) {                                 //+
      sName=name;                                               //+
      iMaxLine=max;                                             //+
      val=new long[iMaxLine][3];                                //+
    }                                                           //+
    void visit(int line) {                                      //+
      val[line][VISIT]++;                                       //+
      long time=System.currentTimeMillis();                     //+
      if(fLastFile!=null) {                                     //+
        fLastFile.val[iLastLine][TIME]+=time-lLastTime;         //+
      }                                                         //+
      fLastFile=this;                                           //+
      iLastLine=line;                                           //+
      lLastTime=time;                                           //+
    }                                                           //+
  }                                                             //+
  void m_visit(String file,int line,int maxline) {              //+
    if(bStop) {                                                 //+
      return;                                                   //+
    }                                                           //+
    bVisited=true;                                              //+
    File f=(File)hash.get(file);                                //+
    if(f==null) {                                               //+
      f=new File(file,maxline);                                 //+
      hash.put(file,f);                                         //+
    }                                                           //+
    f.visit(line);                                              //+
  }                                                             //+
  void m_listUnvisited() {                                      //+
    bStop=true;                                                 //+
    if(!bVisited) {                                             //+
      return;                                                   //+
    }                                                           //+
    Enumeration e=hash.keys();
    printline('=');
    print("UNVISITED");
    printline('-');
    int total=0,unvisited=0;
    while(e.hasMoreElements()) {
      String file=(String)e.nextElement();
      File f=(File)hash.get(file);
      int maxline=f.iMaxLine;
      total+=maxline;
      for(int l=0;l<maxline;l++) {
        if(f.val[l][VISIT]==0) {
          int lto=l+1;
          for(;lto<maxline;lto++) {
            unvisited++;
            if(f.val[lto][VISIT]!=0) {
              break;
            }
          }
          if(l==lto-1) {
            print(file+" "+l);
          } else {
            print(file+" "+l+" - "+(lto-1));
          }
          l=lto;
        } else {
          f.val[l][PERCALL]=f.val[l][TIME]/f.val[l][VISIT];
        }
      }
    }
    printline('-');
    print("Total    : "+total);
    print("Unvisited: "+(100*unvisited/total)+" %");
    printTimePerFile();
    printHigh("MOST VISITS",VISIT);
    printHigh("LONGEST RUN",TIME);
    printHigh("LONGEST PER CALL",PERCALL);
    printline('=');
  }
  void printTimePerFile() {
    Enumeration e=hash.keys();
    printline('-');
    print("TIME PER FILE");
    printline('-');
    int total=0;
    while(e.hasMoreElements()) {
      String file=(String)e.nextElement();
      File f=(File)hash.get(file);
      int time=0;
      int maxline=f.iMaxLine;
      for(int l=0;l<maxline;l++) {
        time+=f.val[l][TIME];
      }
      print(time+"\t"+file);
      total+=time;
    }
    printline('-');
    print("Total time: "+total);
  }
  void printHigh(String name,int type) {
    printline('-');
    print(name);
    printline('-');
    for(int i=0;i<TOP;i++) {
      File topfile=null;
      int topline=-1;
      long top=-1;
      Enumeration e=hash.keys();
      while(e.hasMoreElements()) {
        String file=(String)e.nextElement();
        File f=(File)hash.get(file);
        int maxline=f.iMaxLine;
        for(int l=0;l<maxline;l++) {
          long v=f.val[l][type];
          if(v>top) {
            topfile=f;
            topline=l;
            top=v;
          }
        }
      }
      print(top+"\t"+topline+"\t"+topfile.sName);
      topfile.val[topline][type]=-1;
    }
  }
  void print(String s) {
    System.out.println(s);
  }
  void printline(char c) {
    for(int i=0;i<60;i++) {
      System.out.print(c);
    }
    print("");
  }
}
