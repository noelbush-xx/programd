package org.alicebot.server.core;

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
 * @version 1.0
 */

public class Globals {

	public static ResourceBundle _globals;
	public static Properties _serverProps;
	public static String _bot;

//Add 4.0.3 b4 PEC 09-2001
        public static String _size;
        public static String _version;
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
	/** Load it from file. */
	public static void fromFile(String bot) {
		_bot = bot;
		//_globals = ResourceBundle.getBundle("bots." + getBotName() + ".BOT");
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

	public static boolean showConsole() {
		return Boolean.valueOf(_serverProps.getProperty("server.engine.console")).booleanValue();
	}

	public static boolean showShell() {
		return Boolean.valueOf(_serverProps.getProperty("server.engine.shell")).booleanValue();
	}

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

	public static boolean getSpeak() {
		return Boolean.valueOf(_serverProps.getProperty("server.engine.speak")).booleanValue();
	}

	/** Get the startup file for this bot. */
	public static String getBotFile() {
		return "bots" +
			System.getProperty("file.separator") +
			getBotName() +
			System.getProperty("file.separator") +
			_serverProps.getProperty("server.engine.startup");
	}

	public static String getTargetFile() {
		return "targets/TARGETS.aiml";
	}

	/**
	 * Get a value for this bot.  This is usually called via AIML tags as such:
	 * <bot_favoritecolor/>, <bot_description/>, etc.
	 */
	public static String getValue(String property) {
		String value = "";
		try {
			value = Classifier.doRespond("BOT " + Substituter.normalize(property), "127.0.0.1", 1);
			return value;
		} catch (Exception e) {
			System.out.println("*** NO BOT PROPERTY NAMED > " + property + " ***");
		}
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







