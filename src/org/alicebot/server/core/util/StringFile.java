package org.alicebot.server.core.util;

/**
Alice Program D
Copyright (C) 1995-2001, A.L.I.C.E. AI Foundation

This program is free software; you can redistribute it and/or
modify it under the terms of the GNU General Public License
as published by the Free Software Foundation; either version 2
of the License, or (at your option) any later version.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
USA.

@author  Richard Wallace
@author  Jon Baer
@author  Thomas Ringate/Pedro Colla
@version 4.1.1
*/

	
import java.io.*;
import java.util.*;
 
// class StringFile is a general-purpose high-level string-file
// handling abstraction.  

public class StringFile extends Object implements Serializable {

protected FileInputStream fis;
protected BufferedReader br;
protected FileOutputStream fos;
protected PrintStream ps;

public void close() {
try {fis.close();} catch (Exception e) {System.out.print("-");}
try {fos.close();} catch (Exception e) {System.out.print("-");}
try {br.close();} catch (Exception e) {System.out.print("-");}
try {ps.close();} catch (Exception e) {System.out.print("-");}
}
   
public boolean open(String fname, String mode) {
  try {
    if (mode.compareTo("r") == 0) { 
      fis = new FileInputStream(fname);
      br = new BufferedReader(new InputStreamReader(fis));
    }
    else 
    {
      fos = new FileOutputStream(fname);
      ps = new PrintStream(fos);
    }
  }
  catch (Exception e) {System.out.println("STRINGFILE: "+e);}
return true;
}

public String readLine () { 
  try {
    return(br.readLine());
  }
  catch (Exception e) {System.out.println("STRINGFILE: "+e);}
  return null;
}


// method getfile() 
// 
// getfile() reads the contents of a file fname (presumably ascii)
// and returns it as a string
//
public String getfile(String fname) {
    String outstring = "";
    try {
      fis = new FileInputStream(fname);
      br = new BufferedReader(new InputStreamReader(fis));
      FileInputStream tfis = new FileInputStream(fname);
      BufferedReader tbr = new BufferedReader(new InputStreamReader(fis));
      String s;
      while ((s = tbr.readLine()) != null) outstring += (s+"\n");
      tfis.close();
     } catch (Exception e) {System.out.println("STRINGFILE: "+e);}
return(outstring);
} // end of method getfile()

public int countLines(String fname) {
int sum = 0;
     try {
        RandomAccessFile thefile = new RandomAccessFile(fname, "r");
	String s;
	while ((s = thefile.readLine()) != null) sum ++;
        thefile.close();
     }
     catch (Exception e) { }
return(sum);} // end of method countLines()	 

public void delete(String fname) {
  try {
    File hfile = new File(fname);
    hfile.delete();
  }
  catch (Exception e) {System.out.println("STRINGFILE: "+e);}
}

public void appendLine(String s) {
  try {
    ps.println(s);
  }
  catch (Exception e) {System.out.println("STRINGFILE: "+e);}
}   
  
public void appendLine (String fname, String s) {
     try {
        RandomAccessFile logfile = new RandomAccessFile(fname, "rw");
        int n = (int) logfile.length();
        logfile.skipBytes(n); 	
        logfile.writeBytes(s+"\n");
        logfile.close();
        }
     catch (Exception e) { System.out.println("APPENDLINE: "+e);}
} // end of method StringFile.Line()

} // end of class StringFile{System.out.println("STRINGFILE: "+e);}
