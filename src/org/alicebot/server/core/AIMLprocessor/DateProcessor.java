package org.alicebot.server.core.AIMLprocessor;

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

import java.util.Date;
import java.lang.*;
import java.net.*;
import java.io.*;

import org.alicebot.server.core.*;
import org.alicebot.server.core.util.*;
import org.alicebot.server.core.AIMLparser.*;

/**
  Date Processor is the responsible to return a date string
  @version 4.1.1
  @author  Thomas Ringate/Pedro Colla
*/
public class DateProcessor implements AIMLProcessor, Serializable {
        public String processAIML(int level, String ip, XMLNode tag, AIMLParser p) {

               /*
                This tag is way too simple to justify a processor, however one
                is used to allow the future implementation of a NLS oriented
                date according with the bot configuration and/or the customer
                preferences (i.e. mm-dd-yyyy, dd-mm-yyyy, etc) as well as different
                contents of the string itself.
               */

                Date   t = new Date();
                return t.toString();
	}
}

