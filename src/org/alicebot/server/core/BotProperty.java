package org.alicebot.server.core;

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

@author  Thomas Ringate/Pedro Colla
@version 4.1.2
*/

import java.io.*;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.ResourceBundle;
import java.util.Properties;
import java.util.Locale;
import java.util.Hashtable;

import org.alicebot.server.core.*;
import org.alicebot.server.core.node.*;
import org.alicebot.server.core.parser.*;
import org.alicebot.server.core.util.*;

/**
 * The Properties class represents the bot static properties definitions
 * They should be created thru the usage of <property name="property" value="value"/>
 * tags in the startup.aiml file.
 * Since this is not an AIML compliant tag it will only be used for
 * configuration purposes and made active during load time.
 *
 * @author Thomas Ringate/Pedro Colla
 * @version 4.1.1
 */

public class BotProperty {

        public static Hashtable PropertyList = null;

        public BotProperty() {
           PropertyList = new Hashtable();
	}

        /** Get a Bot Property **/
        public static String get(String propertyname) {

           /**
             Some property set already?
           */
           if (PropertyList == null) {
              return Globals.EmptyDefault; //4.1.2 b2
           }

           /**
             Verify the property name is valid
           */

           if (propertyname.equals("")) {
              return Globals.EmptyDefault;  //4.1.2 b2
           }

           /**
             Retrieve the content of the property
           */
           String propertyvalue = (String)PropertyList.get(propertyname);
           if (propertyvalue != null) {
              return propertyvalue;
           } else {
              return Globals.EmptyDefault;
           }
        }

        /** Set a Bot Property **/
        public static void set(String propertyname, String propertyvalue) {

            /**
             The property name must be informed
            */
            if (propertyname.equals("")) {
               return;
            }

            /**
             If the bot property structure (hashtable) not defined yet
             this is the first attempt to create a property
            */
            if (PropertyList == null) {
               PropertyList = new Hashtable();
            }

            /**
             Store the property on the hashtable
            */
            PropertyList.put(propertyname,new String(propertyvalue));
        }
}
