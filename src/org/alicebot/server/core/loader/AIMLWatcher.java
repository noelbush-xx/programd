package org.alicebot.server.core.loader;

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

import org.alicebot.server.core.*;
import org.alicebot.server.core.logging.*;
import org.alicebot.server.core.node.*;
import org.alicebot.server.core.parser.*;
import org.alicebot.server.core.util.*;

import java.io.*;
import java.util.*;

/** 
 * The AIMLWatcher is a utility class used for "watching" over AIML files.
 * If a loaded set is being watched it will load any new files changes automatically
 * into the running Graphmaster.  Pretty experimental =)
 * 
 * @author Jon Baer
 * @version 1.0
 */

public class AIMLWatcher extends Thread {
	
	private static final int timer = Integer.parseInt((String)Globals.getServerProps().get("server.aiml.watcher.timer"));
	private static final String directory = 
		"bots" + 
		File.separator +
		Globals.getBotName();
	
	private long lastChecked;
	private boolean isWatching;
	
	private HashMap fileMap;
	
	public AIMLWatcher() {
		isWatching = true;
		lastChecked = (new Date()).getTime();
		fileMap = new HashMap();
		loadFiles();
	}
	
	public void loadFiles() {
		File[] list = (new File(directory)).listFiles();
		for (int x = 0; x < list.length; x++) {
			fileMap.put((String)list[x].getAbsolutePath(), new Long(list[x].lastModified()));
		}
	}
	
	public void stopWatching() {
		isWatching = false;
	}
	
	public void run() {
		while (isWatching) {
			// System.out.println("*** WATCHER: CHECKING AIML FILES ***");
			checkFiles();
			try {
				Thread.sleep(this.timer);
			} catch (Exception e) {
				System.out.println("*** WATCHER: " + e + " ***");
			}
		}
	}
	
	public void checkFiles() {
		try {
			File[] list = (new File(directory)).listFiles();
			for (int x = 0; x < list.length; x++) {
				Long check = (Long)fileMap.get(list[x].getAbsolutePath());
				if (check == null) {
					fileMap.put(list[x].getAbsolutePath(), new Long(list[x].lastModified()));
					reload(list[x].getAbsolutePath());
				} else {
					long checkValue = check.longValue();
					if ((new File(list[x].getAbsolutePath())).lastModified() > checkValue) {
						fileMap.put(list[x].getAbsolutePath(), new Long(list[x].lastModified()));
						reload(list[x].getAbsolutePath());
					}
				}
				
			}
			
		} catch (Exception e) {
			System.out.println("*** WATCHER: " + e + " ***");
		}
		
	}
	
	public void reload(String path) {
		// if (Globals.showConsole()) System.out.println("*** " + path + " CHANGED, RELOADING ***");
		Graphmaster.load(path);
	}
	
}

