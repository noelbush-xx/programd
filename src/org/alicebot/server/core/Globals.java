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

@author  Richard Wallace
@author  Jon Baer
@author  Thomas Ringate/Pedro Colla
@version 4.1.2
*/

import java.io.*;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.ResourceBundle;
import java.util.Properties;
import java.util.Locale;


import org.alicebot.server.core.*;
import org.alicebot.server.core.node.*;
import org.alicebot.server.core.parser.*;
import org.alicebot.server.core.util.*;

/**
 * The Globals class represents the loaded bot's static predicate values.
 * These are always stored inside a file called BOT.properties from the loaded bot.
 *
 * Also note that it is a ResourceBundle so it uses i18n notations for internationalization.
 * Examples: BOT_es.properties, BOT_fr.properties, etc.
 *
 * @author Richard Wallace, Jon Baer
 * @author Thomas Ringate/Pedro Colla
 * @version 4.1.1
 */

public class Globals {

	public static ResourceBundle _globals;
	public static Properties _serverProps;
	public static String _bot;

//Add 4.0.3 b4 PEC 09-2001
        public static String _size;
        public static String _version;
//EAdd

//Add 4.1.1 bx PEC 09-2001
        public static int  MAX_LLC         = 25;
        public static int  MAX_INDEX_DEPTH = 5;
//EAdd

//Add 4.1.2 b2 PEC 09-2001
        public static String EmptyDefault  = "";
//EAdd

	public Globals() {
	}

//Add 4.0.3 b4 PEC 09-2001 Methods for version and size

        /** Get Bot Version **/
        public static String getversion() {
          return _version;
        }

        /** Get Bot Size **/
        public static String getsize() {
          return _size + " categories";
        }
//Eadd
        /** Load SERVER.properties configuration from file. */
	public static void fromFile(String bot) {
		_bot = bot;
                //?_globals = ResourceBundle.getBundle("bots." + getBotName() + ".BOT");
		try {
		_serverProps = new Properties();
		_serverProps.load(new FileInputStream("SERVER.properties"));

		} catch (IOException e) {
		// Error loading properties
		System.out.println("Could not find SERVER.properties!");
		System.out.println("Exiting...");
		System.exit(0);
		}
	}

	/** Return the Bot's Name */
	public static String getBotName() {
		return _bot;
	}

        /*Add 4.1.2 b2 PEC 09-2001 Definition of system default when a
          variable or property has not been declared
        */
        public static void getEmptyDefault() {

                EmptyDefault = "";
                EmptyDefault = _serverProps.getProperty("server.engine.emptydefault");
                if (EmptyDefault.equals("")) {
                   return;
                }

                if ( (EmptyDefault.startsWith("\"")) &&
                     (EmptyDefault.endsWith("\""))   &&
                     (EmptyDefault.length() > 2)  ) {
                     EmptyDefault = EmptyDefault.substring(1,EmptyDefault.length()-1);
                } else {
                  EmptyDefault = "";
                }
                return;
        }

        /**
          Return whether trace messages should show off at the local
          console or not
        */
	public static boolean showConsole() {
		return Boolean.valueOf(_serverProps.getProperty("server.engine.console")).booleanValue();
	}

        /**
          Return if the file Watcher is active or not, the watcher reloads
          AIML files when a change is detected
        */
        public static boolean isWatcherActive() {
                return Boolean.valueOf(_serverProps.getProperty("server.engine.watcher")).booleanValue();
	}


	public static boolean showShell() {
		return Boolean.valueOf(_serverProps.getProperty("server.engine.shell")).booleanValue();
	}

        /**
          Return the current merge policy, merge=true means that
          duplicate categories will be merged otherwise the duplicate
          will be rejected
        */

	public static String getMergePolicy() {
		return _serverProps.getProperty("server.engine.merge");
	}

	public static Properties getServerProps() {
		return _serverProps;
	}

	/** Return the default Locale for this bot. */
	public static String getDefaultLocale() {
		return Locale.getDefault().getLanguage();
	}

	/** Return a list of the bot's languages. */
	public static String[] getBotLanguages() {
		Vector languages = new Vector();
		String _bot_language = getValue("LANGUAGE");
		StringTokenizer _st = new StringTokenizer(_bot_language, ",");
		while (_st.hasMoreTokens()) {
			languages.add((String)_st.nextToken());
		}
		return (String[])languages.toArray();
	}
/**Remove 4.1.2 b5 PEC 09-2001
	public static boolean getSpeak() {
		return Boolean.valueOf(_serverProps.getProperty("server.engine.speak")).booleanValue();
	}
*/

	/** Get the startup file for this bot. */
	public static String getBotFile() {
		return "bots" +
			System.getProperty("file.separator") +
			getBotName() +
			System.getProperty("file.separator") +
			_serverProps.getProperty("server.engine.startup");
	}
        /** Get the targets file for this bot
        */
	public static String getTargetFile() {
		return "targets/TARGETS.aiml";
	}

	/**
         * Set a property value for this bot. Could be only set thru a
         * <property name="property" value="value"/> which is a non-AIML
         * compliant tag and thus could only be used at configuration and
         * load time.
	 */

        public static void setValue(String property, String propertyvalue) {
            if (Globals.showConsole()) {
               System.out.println("*** BOT PROPERTY(" + property + ") SET WITH VALUE("+propertyvalue+") ***");
            }
            try {

              /*
               Ensure the property name is meaningful and no case problems exists
              */
              property = property.trim();
              property = property.toLowerCase();
              if (property.equals("")) {
                 return;
              }
              /*
               Set the bot property with the value provided
              */
              BotProperty.set(property,propertyvalue);
            } catch (Exception e) {
              System.out.println("*** ERROR WHILE SETTING PROPERTY " + property + " ***");
            }
            return;
        }

	/**
	 * Get a value for this bot.  This is usually called via AIML tags as such:
         * <bot name="property"/>
	 */
	public static String getValue(String property) {

		String value = "";
                /*
                  Ensure the property is meaningful
                */
                property = property.trim();
                property = property.toLowerCase();
                if (property.equals("")) {
                   return "";
                }

                /*
                  Recover it from the bot properties structure
                */
		try {
                    value = BotProperty.get(property);
                    return value;
		} catch (Exception e) {
                    System.out.println("*** NO BOT PROPERTY NAMED > " + property + " ***");
		}

                /*
                 If the gathering of the bot property failed return {property}
                */
                value = Globals.EmptyDefault;   //4.1.2 b2
		return value;
	}

        /**
         * **MA** 07/10/2001
         *
         * Return a property string by name.  Used by other java methods to
         * read server properties.
         */
        public static String getProperty(String propname)
        {
          return _serverProps.getProperty(propname);
        }
}







