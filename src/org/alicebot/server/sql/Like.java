/*
 * Like.java
 */

package org.alicebot.server.sql;

class Like {
  private char cLike[];
  private int[] iType;
  private int iLen;
  private boolean bIgnoreCase;

  Like(String s,char escape,boolean ignorecase) {
    if(ignorecase) {
      s=s.toUpperCase();
    }
    normalize(s,true,escape);
    bIgnoreCase=ignorecase;
  }
  String getStartsWith() {
    String s="";
    int i=0;
    for(;i<iLen && iType[i]==0;i++) {
      s=s+cLike[i];
    }
    if(i==0) {
      return null;
    }
    return s;
  }
  boolean compare(Object o) {
    if(o==null) {
      return iLen==0;
    }
    String s=o.toString();
    if(bIgnoreCase) {
      s=s.toUpperCase();
    }
    return compareAt(s,0,0,s.length());
  }

  private boolean compareAt(String s,int i,int j,int jLen) {
    for(;i<iLen;i++) {
      switch(iType[i]) {
      case 0: // general character
        if(j>=jLen || cLike[i]!=s.charAt(j++)) {
          return false;
        }
        break;
      case 1: // underscore: do not test this character
        if(j++>=jLen) {
          return false;
        }
        break;
      case 2: // percent: none or any character(s)
        if(++i>=iLen) {
          return true;
        }
        while(j<jLen) {
          if(cLike[i]==s.charAt(j) && compareAt(s,i,j,jLen)) {
            return true;
          }
          j++;
        }
        return false;
      }
    }
    if(j!=jLen) {
      return false;
    }
    return true;
  }
  private void normalize(String s,boolean b,char e) {
    iLen=0;
    if(s==null) {
      return;
    }
    int l=s.length();
    cLike=new char[l];
    iType=new int[l];
    boolean bEscaping=false,bPercent=false;
    for(int i=0;i<l;i++) {
      char c=s.charAt(i);
      if(bEscaping==false) {
        if(b && c==e) {
          bEscaping=true;
          continue;
        } else if(c=='_') {
          iType[iLen]=1;
        } else if(c=='%') {
          if(bPercent) {
            continue;
          }
          bPercent=true;
          iType[iLen]=2;
        } else {
          bPercent=false;
        }
      } else {
        bPercent=false;
        bEscaping=false;
      }
      cLike[iLen++]=c;
    }
    for(int i=0;i<iLen-1;i++) {
      if(iType[i]==2 && iType[i+1]==1) {
        iType[i]=1;
        iType[i+1]=2;
      }
    }
  }
}

