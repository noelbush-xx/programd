package org.alicebot.server.core.processor;

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

//import java.util.*;
import java.lang.*;
import java.net.*;
import java.io.*;

import org.alicebot.server.core.*;
import org.alicebot.server.core.util.*;
import org.alicebot.server.core.parser.*;

/**
 SystemProcessor is the responsible to evaluate the SYSTEM tag, the
 content of the AIML beneath is evaluated and the result passed to the
 OS Shell for execution, no validation is performed the command passed
 to the shell is either viable nor non-harmful.
 @version 4.1.1
 @author  Thomas Ringate/Pedro Colla
*/
public class SystemProcessor implements AIMLProcessor, Serializable {
      public String processAIML(int level, String ip, XMLNode tag, AIMLParser p) {

        String response = "";
        response = p.evaluate(level++,"localhost", tag.XMLChild);
        String output = "";
        try {
                 String command = response;
                 File cwd = null; // ** MA ** 06/11/2001
                 String currentdir = Globals.getProperty("server.system.cwd");
                 if (currentdir != null)
                    cwd = new File(currentdir);

                 Process child = Runtime.getRuntime().exec(command, null, cwd); // cmd,env,dir

                 InputStream in = child.getInputStream();
                 BufferedReader br = new BufferedReader(new InputStreamReader(in));
                 String line;
                 while ((line = br.readLine()) != null) {
                         output = output+line+"<br>\n";
                 }
                 response = output;
                 in.close();
        }
        catch (IOException e) {
                 System.out.println("Cannot execute "+response+" "+e);
        }

        return response;
      }
}
