package org.alicebot.server.core.util;

/**

ALICEBOT.NET Artificial Intelligence Project
This version is Copyright (C) 2000 Jon Baer.
jonbaer@digitalanywhere.com
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions
are met:

1. Redistributions of source code must retain the above copyright
notice, this list of conditions, and the following disclaimer.

2. Redistributions in binary form must reproduce the above copyright
notice, this list of conditions, and the disclaimer that follows 
these conditions in the documentation and/or other materials 
provided with the distribution.

3. The name "ALICEBOT.NET" must not be used to endorse or promote products
derived from this software without prior written permission.  For
written permission, please contact license@alicebot.org.

4. Products derived from this software may not be called "ALICEBOT.NET",
nor may "ALICEBOT.NET" appear in their name, without prior written permission
from the ALICEBOT.NET Project Management (jonbaer@alicebot.net).

In addition, we request (but do not require) that you include in the 
end-user documentation provided with the redistribution and/or in the 
software itself an acknowledgement equivalent to the following:
"This product includes software developed by the
ALICEBOT.NET Project (http://www.alicebot.net)."
Alternatively, the acknowledgment may be graphical using the logos 
available at http://www.alicebot.org/images/logos.

THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED.  IN NO EVENT SHALL THE ALICE SOFTWARE FOUNDATION OR
ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
SUCH DAMAGE.

This software consists of voluntary contributions made by many 
individuals on behalf of the A.L.I.C.E. Nexus and ALICEBOT.NET Project
and was originally created by Dr. Richard Wallace <drwallace@alicebot.net>.

This version was created by Jon Baer <jonbaer@alicebot.net>.

http://www.alicebot.org
http://www.alicebot.net

This version contains open-source technologies from:
Netscape, Apache, HypersonicSQL, JDOM, Jetty, Chris Carlin, IBM

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
