package org.alicebot.server.core.node;

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

import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.Set;

import org.alicebot.server.core.Graphmaster;

/**
 * The non-trivial implementation uses a HashMap internally,
 * but only allocates it when the number of keys is two or
 * more.
 * The Nodemaster saves space when many of the Nodemappers
 * have only one branch, as is the case in Graphmaster.
 */

public class Nodemaster implements Nodemapper {
	protected int size=0;
	protected Object key;
	protected Object value;
	protected Map Hidden;
	public Object put(Object key, Object value) {
		if (size==0) {
			this.key = key; this.value = value; size=1;
			return value;
		}
		else if (size==1) {
			Hidden = new TreeMap();
			Hidden.put(this.key, this.value);
			size=2;
			return Hidden.put(key, value);
		}
		else return Hidden.put(key, value);
	}
	public Object get(Object key) {
		if (size <= 1) {
			if (key.equals(this.key)) return this.value;
			else return null;
		}
		else return Hidden.get(key);
	}
	public Set keySet() {
		if (size <= 1) {
			Set S = new HashSet();
			if (this.key != null) S.add(this.key);
			return S;
		}
		else return Hidden.keySet();
	}
	public boolean containsKey(Object key) {
		if (size <= 1) {
			return (key.equals(this.key));
		}
		else return Hidden.containsKey(key);
	} // containsKey
	
}
