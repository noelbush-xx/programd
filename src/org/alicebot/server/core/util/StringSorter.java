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


import java.util.*;
import java.lang.*;
import java.net.*;
import java.io.*;

// A String Sorter is a string Set refined
// so that the Strings are sorted :
//
// this version by Yiannis Paschalidis
// aliasx@geocities.com
//
public class StringSorter extends StringSet implements Serializable
{  
	//Since the data is sorted, we can use a 
	//binary search to save time...
	
	public int indexOf(String s)
	{
		int top=size()-1,bottom=0,mid,diff;
		
		while (top>=bottom)
		{
			mid = (top+bottom)/2;
			diff=((String)(elementAt(mid))).compareTo(s);
			
			if (diff>0)
				top=mid-1;
			else if (diff<0)
				bottom=mid+1;
			else
				return mid; //found it!
		}
		
		//Haven't found it    
		return -1;
	}
	
	public boolean contains(String s)
	{
		return indexOf(s)>=0;
	}
	
	public void add(String s)
	{
		//trivial case
		
		if (isEmpty())
		{
			super.add(s);
			return;
		}
		
		//otherwise, perform a binary search to see where it should go
		
		int top=size()-1,bottom=0,mid=top/2,diff=0;
		
		while (top>=bottom)
		{
			mid = (top+bottom)/2;
			diff=((String)(elementAt(mid))).compareTo(s);
			
			if (diff>0)
				top=mid-1;
			else if (diff<0)
				bottom=mid+1;
			else
				return; //equal, so no need to insert
		}
		
		//Ok, we've found the right spot. Insert before or after?
		
		mid = (top+bottom)/2;
		diff=((String)(elementAt(mid))).compareTo(s);
		
		if (diff>0)
			insertElementAt(s,mid);
		else
			insertElementAt(s,mid+1);
	}  
} // class StringSorter
