package org.alicebot.server.core.responder;

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
@version 4.1.2
*/


import java.util.*;
import java.net.*;
import java.io.*;

import org.alicebot.server.core.*;
import org.alicebot.server.core.logging.*;
import org.alicebot.server.core.util.*;

/**
  TextResponder is the responsible to create the output for the local
  console as well as the chat log.
  @version 4.1.1
  @author  Richard S. Wallace
  @author  Jon Baer
  @author  Thomas Ringate/Pedro Colla
*/
public class TextResponder implements Responder,Serializable {
	
	public TextResponder() {
	}
	
	public String pre_process(String input, String hname) {
		return input;
	}
	public void log(String input, String response, String hname) {


                //System.out.println("*** LOG: input("+input+") response("+response+") hname("+hname+") ***");

                /**
                  Added current bot name and user name 4.1.1 b14 PEC 09-2001
                */
                String username = Classifier.getValue("name",hname);
                if (username.equals("")) {
                   username = hname;
                }
                input = Toolkit.filterSpaces(input.trim());         //4.1.1 b15
                response = Toolkit.filterSpaces(response.trim());   //4.1.1 b15
                input = Toolkit.filterLF(input);                    //4.1.1 b15
                response = Toolkit.filterLF(response);              //4.1.1 b15

                Log.log(username + " > [" + input + "]", Log.CHAT); //Modify 4.0.3 b5
                response = Toolkit.filterLF(response); //4.1.1 b14 PEC 09-2001
                if ( (!response.equalsIgnoreCase("\n")) && (!response.equalsIgnoreCase("")) ) {  //Modify 4.0.3 b6
                   Log.log(Globals.getValue("name")+" > [" + response + "]", Log.CHAT);        //Modify 4.0.3 b5
                }
	}
	public String append(String input, String response, String scroll) {
                return scroll + Toolkit.filterLF(response);
	}
	
	public String post_process(String reply) {
                reply = Toolkit.filterLF(reply);
		StringTokenizer st = new StringTokenizer(reply);
		StringBuffer buffer = new StringBuffer();
		while (st.hasMoreTokens()) {
			buffer.append(st.nextToken());
			if (st.hasMoreTokens()) buffer.append(" ");
		}
		return buffer.toString();
	}
} 
