/*
 * Cache.java
 */

package org.alicebot.server.sql;
import java.io.*;
import java.sql.*;

class Cache {
  private RandomAccessFile rFile;
  private final static int LENGTH=1<<14;
  private final static int MAX_CACHE_SIZE=LENGTH*3/4;
  private Row rData[];
  private Row rWriter[];
  private Row rFirst;  // must point to one of rData[]
  private Row rLastChecked; // can be any row
  private String sName;
  private final static int MASK=(LENGTH)-1;
  private int iFreePos;
  private final static int FREE_POS_POS=16; // where iFreePos is saved
  private final static int INITIAL_FREE_POS=32;
  private final static int MAX_FREE_COUNT=1024;
  private CacheFree fRoot;
  private int iFreeCount;
  private int iCacheSize;

  Cache(String name) {
    sName=name;
    rData=new Row[LENGTH];
    rWriter=new Row[LENGTH];
  }
  void open(boolean readonly) throws SQLException {
    try {
      boolean exists=false;
      File f=new File(sName);
      if(f.exists() && f.length()>FREE_POS_POS) {
        exists=true;
      }
      rFile=new RandomAccessFile(sName,readonly ? "r" : "rw");
      if(exists) {
        rFile.seek(FREE_POS_POS);
        iFreePos=rFile.readInt();
      } else {
        iFreePos=INITIAL_FREE_POS;
      }
    } catch(Exception e) {
      throw Trace.error(Trace.FILE_IO_ERROR,"error "+e+" opening "+sName);
    }
  }
  void flush() throws SQLException {
    try {
      rFile.seek(FREE_POS_POS);
      rFile.writeInt(iFreePos);
      saveAll();
      rFile.close();
    } catch(Exception e) {
      throw Trace.error(Trace.FILE_IO_ERROR,"error "+e+" closing "+sName);
    }
  }
  void shutdown() throws SQLException {
    try {
      rFile.close();
    } catch(Exception e) {
      throw Trace.error(Trace.FILE_IO_ERROR,"error "+e+" in shutdown "+sName);
    }
  }
  void free(Row r,int pos,int length) throws SQLException {
    iFreeCount++;
    CacheFree n=new CacheFree();
    n.iPos=pos;
    n.iLength=length;
    // if more than MAX_FREE_COUNT free positios then probably
    // all are too small anyway; so start a new list
    // todo: this is wrong when deleting lots of records
    if(iFreeCount>MAX_FREE_COUNT) {
      iFreeCount=0;
    } else {
      n.fNext=fRoot;
    }
    fRoot=n;
    // it's possible to remove roots to
    remove(r);
  }
  void add(Row r) throws SQLException {
    int size=r.iSize;
    CacheFree f=fRoot;
    CacheFree last=null;
    int i=iFreePos;
    while(f!=null) {
      if(Trace.TRACE) Trace.stop();
      // first that is long enough
      if(f.iLength>=size) {
        i=f.iPos;
        size=f.iLength-size;
        if(size<8) {
          // remove almost empty blocks
          if(last==null) {
            fRoot=f.fNext;
          } else {
            last.fNext=f.fNext;
          }
          iFreeCount--;
        } else {
          f.iLength=size;
        }
        break;
      }
      last=f;
      f=f.fNext;
    }
    r.iPos=i;
    if(i==iFreePos) {
      iFreePos+=size;
    }
    int k=i&MASK;
    Row before=rData[k];
    if(before==null) {
      before=rFirst;
    }
    r.insert(before);
    iCacheSize++;
    rData[k]=r;
    rFirst=r;
  }
  Row getRow(int pos,Table t) throws SQLException {
    int k=pos&MASK;
    Row r=rData[k];
    Row start=r;
    while(r!=null) {
      if(Trace.STOP) Trace.stop();
      int p=r.iPos;
      if(p==pos) {
        return r;
      } else if((p&MASK) != k) {
        break;
      }
      r=r.rNext;
      if(r==start) {
        break;
      }
    }
    Row before=rData[k];
    if(before==null) {
      before=rFirst;
    }
    try {
      rFile.seek(pos);
      int size=rFile.readInt();
      byte buffer[]=new byte[size];
      rFile.read(buffer);
      ByteArrayInputStream bin=new ByteArrayInputStream(buffer);
      DataInputStream in=new DataInputStream(bin);
      r=new Row(t,in,pos,before);
      r.iSize=size;
    } catch(IOException e) {
      e.printStackTrace();
      throw Trace.error(Trace.FILE_IO_ERROR,"reading: "+e);
    }
    // todo: copy & paste here
    iCacheSize++;
    rData[k]=r;
    rFirst=r;
    return r;
  }
  void cleanUp() throws SQLException {
    if(iCacheSize<MAX_CACHE_SIZE) {
      return;
    }
    int count=0,j=0;
    while(j++<LENGTH && iCacheSize+LENGTH>MAX_CACHE_SIZE && (count*16)<LENGTH) {
      if(Trace.STOP) Trace.stop();
      Row r=getWorst();
      if(r==null) {
        return;
      }
      if(r.bChanged) {
        rWriter[count++]=r;
      } else {
        // here we can't remove roots
        if(!r.canRemove()) {
          remove(r);
        }
      }
    }
    if(count!=0) {
      saveSorted(count);
    }
    for(int i=0;i<count;i++) {
      // here we can't remove roots
      Row r=rWriter[i];
      if(!r.canRemove()) {
        remove(r);
      }
      rWriter[i]=null;
    }
  }
  private void remove(Row r) throws SQLException {
    if(Trace.ASSERT) Trace.assert(!r.bChanged);
    // make sure rLastChecked does not point to r
    if(r==rLastChecked) {
      rLastChecked=rLastChecked.rNext;
      if(rLastChecked==r) {
        rLastChecked=null;
      }
    }
    // make sure rData[k] does not point here
    int k=r.iPos&MASK;
    if(rData[k]==r) {
      Row n=r.rNext;
      rFirst=n;
      if(n==r || (n.iPos&MASK)!=k) {
        n=null;
      }
      rData[k]=n;
    }
    // make sure rFirst does not point here
    if(r==rFirst) {
      rFirst=rFirst.rNext;
      if(r==rFirst) {
        rFirst=null;
      }
    }
    r.free();
    iCacheSize--;
  }
  private Row getWorst() throws SQLException {
    if(rLastChecked==null) {
      rLastChecked=rFirst;
    }
    Row r=rLastChecked;
    if(r==null) {
      return null;
    }
    Row candidate=r;
    int worst=Row.iCurrentAccess;
    // algorithm: check the next rows and take the worst
    for(int i=0;i<6;i++) {
      int w=r.iLastAccess;
      if(w<worst) {
        candidate=r;
        worst=w;
      }
      r=r.rNext;
    }
    rLastChecked=r.rNext;
    return candidate;
  }
  private void saveAll() throws SQLException {
    if(rFirst==null) {
      return;
    }
    Row r=rFirst;
    while(true) {
      int count=0;
      Row begin=r;
      do {
        if(Trace.STOP) Trace.stop();
        if(r.bChanged) {
          rWriter[count++]=r;
        }
        r=r.rNext;
      } while(r!=begin && count<LENGTH);
      if(count==0) {
        return;
      }
      saveSorted(count);
      for(int i=0;i<count;i++) {
        rWriter[i]=null;
      }
    }
  }
  private void saveSorted(int count) throws SQLException {
    sort(rWriter,0,count-1);
    try {
      for(int i=0;i<count;i++) {
        rFile.seek(rWriter[i].iPos);
        rFile.write(rWriter[i].write());
      }
    } catch(Exception e) {
      throw Trace.error(Trace.FILE_IO_ERROR,"saveSorted "+e);
    }
  }
  private static final void sort(Row w[],int l,int r) throws SQLException {
    int i,j,p;
    while(r-l>10) {
      i=(r+l)>>1;
      if(w[l].iPos>w[r].iPos) {
        swap(w,l,r);
      }
      if(w[i].iPos<w[l].iPos) {
        swap(w,l,i);
      } else if(w[i].iPos>w[r].iPos) {
        swap(w,i,r);
      }
      j=r-1;
      swap(w,i,j);
      p=w[j].iPos;
      i=l;
      while(true) {
        if(Trace.STOP) Trace.stop();
        while(w[++i].iPos<p);
        while(w[--j].iPos>p);
        if(i>=j) {
          break;
        }
        swap(w,i,j);
      }
      swap(w,i,r-1);
      sort(w,l,i-1);
      l=i+1;
    }
    for(i=l+1;i<=r;i++) {
      if(Trace.STOP) Trace.stop();
      Row t=w[i];
      for(j=i-1;j>=l && w[j].iPos>t.iPos;j--) {
        w[j+1]=w[j];
      }
      w[j+1]=t;
    }
  }
  private static void swap(Row w[],int a,int b) {
    Row t=w[a];
    w[a]=w[b];
    w[b]=t;
  }
}

