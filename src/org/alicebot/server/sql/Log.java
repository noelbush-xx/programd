/*
 * Log.java
 */

package org.alicebot.server.sql;
import java.sql.*;
import java.io.*;
import java.util.*;
import java.util.zip.*;

/**
 * <P>This class is responsible for most file handling.
 * A Hypersonic SQL database consists of a .properties file, a
 * .script file (contains a SQL script), a
 * .data file (contains data of cached tables) and a
 * .backup file (contains the compressed .data file)
 *
 * <P>This is an example of the .properties file. The version and the
 * modified properties are automatically created by the database and
 * should not be changed manually:
 * <pre>
 * modified=no
 * version=1.3
 * </pre>
 * The following lines are optional, this means they are not
 * created automatically by the database, but they are interpreted
 * if they exist in the .script file. They have to be created
 * manually if required. If they don't exist the default is used.
 * This are the defaults of the database 'test':
 * <pre>
 * script=test.script
 * data=test.data
 * backup=test.backup
 * readonly=false
 * </pre>
 */
class Log implements Runnable {
  private final static int COPY_BLOCK_SIZE=1<<16; // block size for copying data
  private FileInputStream fProperties; // kept open until closed
  private Properties pProperties;
  private String sName;
  private Database dDatabase;
  private Channel cSystem;
  private Writer wScript;
  private String sFileProperties;
  private String sFileScript;
  private String sFileCache;
  private String sFileBackup;
  private boolean bRestoring;
  private boolean bReadOnly;
  private int iLogSize=200;  // default: .script file is max 200 MB big
  private int iLogCount;
  private Thread tRunner;
  private volatile boolean bNeedFlush;
  private volatile boolean bWriteDelay;
  private int mLastId;
  Cache cCache;

  Log(Database db,Channel system,String name) throws SQLException {
    dDatabase=db;
    cSystem=system;
    sName=name;
    sFileProperties=sName+".properties";
    pProperties=new Properties();
    tRunner=new Thread(this);
    tRunner.start();
  }
  public void run() {
    while(tRunner!=null) {
      try {
        tRunner.sleep(1000);
        if(bNeedFlush) {
          wScript.flush();
          bNeedFlush=false;
        }
        // todo: try to do Cache.cleanUp() here, too
      } catch(Exception e) {
        // ignore exceptions; may be InterruptedException or IOException
      }
    }
  }
  void setWriteDelay(boolean delay) {
    bWriteDelay=delay;
  }
  boolean open() throws SQLException {
    if(Trace.TRACE) Trace.trace();
    if(!(new File(sFileProperties)).exists()) {
      create();
      open();
      // this is a new database
      return true;
    }
    // todo: some parts are not necessary for ready-only access
    loadProperties();
    sFileScript=pProperties.getProperty("script",sName+".script");
    sFileCache=pProperties.getProperty("data",sName+".data");
    sFileBackup=pProperties.getProperty("backup",sName+".backup");
    String version=pProperties.getProperty("version","1.0");
    boolean check=version.equals(jdbcDriver.VERSION);
    Trace.check(check,Trace.WRONG_DATABASE_FILE_VERSION);
    if(pProperties.getProperty("readonly","false").equals("true")) {
      bReadOnly=true;
      dDatabase.setReadOnly();
      cCache=new Cache(sFileCache);
      cCache.open(true);
      runScript();
      return false;
    }
    boolean needbackup=false;
    String state=pProperties.getProperty("modified","no");
    if(state.equals("yes-new-files")) {
      renameNewToCurrent(sFileScript);
      renameNewToCurrent(sFileBackup);
    } else if(state.equals("yes")) {
      if(isAlreadyOpen()) {
        throw Trace.error(Trace.DATABASE_ALREADY_IN_USE);
      }
      // recovering after a crash (or forgot to close correctly)
      restoreBackup();
      needbackup=true;
    }
    pProperties.put("modified","yes");
    saveProperties();
    cCache=new Cache(sFileCache);
    cCache.open(false);
    runScript();
    if(needbackup) {
      close(false);
      pProperties.put("modified","yes");
      saveProperties();
      cCache.open(false);
    }
    openScript();
    // this is a existing database
    return false;
  }
  void stop() {
    tRunner=null;
  }
  void close(boolean compact) throws SQLException {
    if(Trace.TRACE) Trace.trace();
    if(bReadOnly) {
      return;
    }
    // no more scripting
    closeScript();
    // create '.script.new' (for this the cache may be still required)
    writeScript(compact);
    // flush the cache (important: after writing the script)
    cCache.flush();
    // create '.backup.new' using the '.data'
    backup();
    // we have the new files
    pProperties.put("modified","yes-new-files");
    saveProperties();
    // old files can be removed and new files renamed
    renameNewToCurrent(sFileScript);
    renameNewToCurrent(sFileBackup);
    // now its done completely
    pProperties.put("modified","no");
    saveProperties();
    closeProperties();
    if(compact) {
      // stop the runner thread of this process (just for security)
      stop();
      // delete the .data so then a new file is created
      (new File(sFileCache)).delete();
      (new File(sFileBackup)).delete();
      // all files are closed now; simply open & close this database
      Database db=new Database(sName);
      db.getLog().close(false);
    }
  }
  void checkpoint() throws SQLException {
    close(false);
    pProperties.put("modified","yes");
    saveProperties();
    cCache.open(false);
    openScript();
  }
  void setLogSize(int mb) {
    iLogSize=mb;
  }
  void write(Channel c,String s) throws SQLException {
    if(bRestoring || s==null || s.equals("")) {
      return;
    }
    int id=0;
    if(c!=null) {
      id=c.getId();
    }
    if(id!=mLastId) {
      s="/*C"+id+"*/"+s;
      mLastId=id;
    }
    try {
      writeLine(wScript,s);
      if(bWriteDelay) {
        bNeedFlush=true;
      } else {
        wScript.flush();
      }
    } catch(IOException e) {
      Trace.error(Trace.FILE_IO_ERROR,sFileScript);
    }
    if(iLogSize>0 && iLogCount++>100) {
      iLogCount=0;
      if((new File(sFileScript)).length()>iLogSize*1024*1024) {
        checkpoint();
      }
    }
  }
  void shutdown() throws SQLException {
    tRunner=null;
    cCache.shutdown();
    closeScript();
    closeProperties();
  }
  static void scriptToFile(Database db,String file,boolean full,
  Channel channel) throws SQLException {
    if((new File(file)).exists()) {
      // there must be no such file; overwriting not allowed for security
      throw Trace.error(Trace.FILE_IO_ERROR,file);
    }
    try {
      long time=System.currentTimeMillis();
      // only ddl commands; needs not so much memory
      Result r;
      if(full) {
        // no drop, no insert, and no positions for cached tables
        r=db.getScript(false,false,false,channel);
      } else {
        // no drop, no insert, but positions for cached tables
        r=db.getScript(false,false,true,channel);
      }
      Record n=r.rRoot;
      FileWriter w=new FileWriter(file);
      while(n!=null) {
        writeLine(w,(String)n.data[0]);
        n=n.next;
      }
      // inserts are done separetely to save memory
      Vector tables=db.getTables();
      for(int i=0;i<tables.size();i++) {
        Table t=(Table)tables.elementAt(i);
        // cached tables have the index roots set in the ddl script
        if(full || !t.isCached()) {
          Index primary=t.getPrimaryIndex();
          Node x=primary.first();
          while(x!=null) {
            writeLine(w,t.getInsertStatement(x.getData()));
            x=primary.next(x);
          }
        }
      }
      w.close();
      time=System.currentTimeMillis()-time;
      if(Trace.TRACE) Trace.trace(time);
    } catch(IOException e) {
      Trace.error(Trace.FILE_IO_ERROR,file+" "+e);
    }
  }

  private void renameNewToCurrent(String file) {
    // even if it crashes here, recovering is no problem
    if((new File(file+".new")).exists()) {
      // if we have a new file
      // delete the old (maybe already deleted)
      (new File(file)).delete();
      // rename the new to the current
      new File(file+".new").renameTo(new File(file));
    }
  }
  private void closeProperties() throws SQLException {
    try {
      if(fProperties!=null) {
        if(Trace.TRACE) Trace.trace();
        fProperties.close();
        fProperties=null;
      }
    } catch(Exception e) {
      throw Trace.error(Trace.FILE_IO_ERROR,sFileProperties+" "+e);
    }
  }
  private void create() throws SQLException {
    if(Trace.TRACE) Trace.trace(sName);
    pProperties.put("modified","no");
    pProperties.put("version",jdbcDriver.VERSION);
    saveProperties();
  }
  private boolean isAlreadyOpen() throws SQLException {
    // reading the last modified, wait 3 seconds, read again.
    // if the same information was read the file was not changed
    // and is probably, except the other process is blocked
    if(Trace.TRACE) Trace.trace();
    File f=new File(sName+".lock");
    long l1=f.lastModified();
    try {
      Thread.sleep(3000);
    } catch(Exception e) {
    }
    long l2=f.lastModified();
    if(l1!=l2) {
      return true;
    }
    // check by trying to delete the properties file
    // this will not work if some application has the file open
    // this is why the properties file is kept open when running ;-)
    // todo: check if this works in all operating systems
    closeProperties();
    if(Trace.TRACE) Trace.trace();
    if((new File(sFileProperties)).delete()==false) {
      return true;
    }
    // the file was deleted, so recreate it now
    saveProperties();
    return false;
  }
  private void loadProperties() throws SQLException {
    File f=new File(sFileProperties);
    closeProperties();
    if(Trace.TRACE) Trace.trace();
    try {
      // the file is closed only when the database is closed
      fProperties=new FileInputStream(f);
      pProperties.load(fProperties);
    } catch(Exception e) {
      throw Trace.error(Trace.FILE_IO_ERROR,sFileProperties);
    }
  }
  private void saveProperties() throws SQLException {
    File f=new File(sFileProperties);
    closeProperties();
    if(Trace.TRACE) Trace.trace();
    try {
      FileOutputStream out=new FileOutputStream(f);
//#ifdef JAVA2
      pProperties.store(out,"Hypersonic SQL database");
//#else
/*
      pProperties.save(out,"Hypersonic SQL database");
*/
//#endif
      out.close();
      // after saving, open the file again
      loadProperties();
    } catch(Exception e) {
      throw Trace.error(Trace.FILE_IO_ERROR,sFileProperties);
    }
  }
  private void backup() throws SQLException {
    if(Trace.TRACE) Trace.trace();
    // if there is no cache file then backup is not necessary
    if(!(new File(sFileCache)).exists()) {
      return;
    }
    try {
      long time=System.currentTimeMillis();
      // create a '.new' file; rename later
      DeflaterOutputStream f=new DeflaterOutputStream(
      new FileOutputStream(sFileBackup+".new"),
      new Deflater(Deflater.BEST_SPEED),COPY_BLOCK_SIZE);
      byte b[]=new byte[COPY_BLOCK_SIZE];
      FileInputStream in=new FileInputStream(sFileCache);
      while(true) {
        int l=in.read(b,0,COPY_BLOCK_SIZE);
        if(l==-1) {
          break;
        }
        f.write(b,0,l);
      }
      f.close();
      in.close();
      time=System.currentTimeMillis()-time;
      if(Trace.TRACE) Trace.trace(time);
    } catch(Exception e) {
      throw Trace.error(Trace.FILE_IO_ERROR,sFileBackup);
    }
  }
  private void restoreBackup() throws SQLException {
    if(Trace.TRACE) Trace.trace("not closed last time!");
    if(!(new File(sFileBackup)).exists()) {
      // the backup don't exists because it was never made or is empty
      // the cache file must be deleted in this case
      (new File(sFileCache)).delete();
      return;
    }
    try {
      long time=System.currentTimeMillis();
      InflaterInputStream f=new InflaterInputStream(
          new FileInputStream(sFileBackup),new Inflater());
          FileOutputStream cache=new FileOutputStream(sFileCache);
      byte b[]=new byte[COPY_BLOCK_SIZE];
      while(true) {
        int l=f.read(b,0,COPY_BLOCK_SIZE);
        if(l==-1) {
          break;
        }
        cache.write(b,0,l);
      }
      cache.close();
      f.close();
      time=System.currentTimeMillis()-time;
      if(Trace.TRACE) Trace.trace(time);
    } catch(Exception e) {
      throw Trace.error(Trace.FILE_IO_ERROR,sFileBackup);
    }
  }
  private void openScript() throws SQLException {
    if(Trace.TRACE) Trace.trace();
    try {
      // todo: use a compressed stream
      wScript=new BufferedWriter(new FileWriter(sFileScript,true),4096);
    } catch(Exception e) {
      Trace.error(Trace.FILE_IO_ERROR,sFileScript);
    }
  }
  private void closeScript() throws SQLException {
    if(Trace.TRACE) Trace.trace();
    try {
      if(wScript!=null) {
        wScript.close();
        wScript=null;
      }
    } catch(Exception e) {
      Trace.error(Trace.FILE_IO_ERROR,sFileScript);
    }
  }
  private void runScript() throws SQLException {
    if(Trace.TRACE) Trace.trace();
    if(!(new File(sFileScript)).exists()) {
      return;
    }
    bRestoring=true;
    dDatabase.setReferentialIntegrity(false);
    Vector channel=new Vector();
    channel.addElement(cSystem);
    Channel current=cSystem;
    int size=1;
    try {
      long time=System.currentTimeMillis();
      LineNumberReader r=new LineNumberReader(new FileReader(sFileScript));
      while(true) {
        String s=readLine(r);
        if(s==null) {
          break;
        }
        if(s.startsWith("/*C")) {
          int id=Integer.parseInt(s.substring(3,s.indexOf('*',4)));
          if(id>=size) {
            channel.setSize(id+1);
          }
          current=(Channel)channel.elementAt(id);
          if(current==null) {
            current=new Channel(cSystem,id);
            channel.setElementAt(current,id);
            dDatabase.registerChannel(current);
          }
          s=s.substring(s.indexOf('/',1)+1);
        }
        if(!s.equals("")) {
          dDatabase.execute(s,current);
        }
        if(s.equals("DISCONNECT")) {
          int id=current.getId();
          current=new Channel(cSystem,id);
          channel.setElementAt(current,id);
        }
      }
      r.close();
      for(int i=0;i<size;i++) {
        current=(Channel)channel.elementAt(i);
        if(current!=null) {
          current.rollback();
        }
      }
      time=System.currentTimeMillis()-time;
      if(Trace.TRACE) Trace.trace(time);
    } catch(IOException e) {
      throw Trace.error(Trace.FILE_IO_ERROR,sFileScript+" "+e);
    }
    dDatabase.setReferentialIntegrity(true);
    bRestoring=false;
  }
  private void writeScript(boolean full) throws SQLException {
    if(Trace.TRACE) Trace.trace();
    // create script in '.new' file
    (new File(sFileScript+".new")).delete();
    // script; but only positions of cached tables, not full
    scriptToFile(dDatabase,sFileScript+".new",full,cSystem);
  }
  private static void writeLine(Writer w,String s) throws IOException {
    w.write(StringConverter.unicodeToAscii(s)+"\r\n");
  }
  private static String readLine(LineNumberReader r) throws IOException {
    String s=r.readLine();
    return StringConverter.asciiToUnicode(s);
  }
}

