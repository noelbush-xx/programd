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

import org.alicebot.server.xml.*;
import org.alicebot.server.xml.input.*;
import org.alicebot.server.xml.output.*;

import java.io.*;
import java.util.*;

/**
  This class implements the management of the TARGET file and the
  targetting process.
  @version 4.1.1
  @author  Richard S. Wallace
  @author  Jon Baer
  @author  Thomas Ringate/Pedro Colla
*/
public class Targets extends Thread {
	
	public static final String TARGETS_AIML = "TARGETS.aiml";
	public static final String TARGETS_DATA = "TARGETS.data";
	public static final String CATEGORY = "category";
	public static final String PATTERN = "pattern";
	public static final String TEMPLATE = "template";
	public String DEFAULT = "Sorry.  I don't know.";
	
	public Targets() {
		start();
	}
	
	public Targets(String defaultString) {
		this.DEFAULT = defaultString;
		start();
	}
	
	public void run() {
		
		int total = 0;
		
		try {

			// Build previous
			File aiml = new File(TARGETS_AIML);
			File input = new File(TARGETS_DATA);

//Fix 4.0.3 b8 PEC 09-2001 Verify file exists
  
                        if ((input.length()   >  0   ) &&
                            (input.exists()   == true) &&
                            (input.canRead()  == true)) {

//End of Fix
  
                            SAXBuilder builder = new SAXBuilder();
                            Document doc = builder.build(aiml);
                            XMLOutputter out = new XMLOutputter(" ", true);
                            out.setOmitEncoding(true);
                            out.setLineSeparator("\n");
                            out.setTrimText(true);
                            Element root = doc.getRootElement();
			
                            LineNumberReader reader = new LineNumberReader(new FileReader(input));
			
                            String in;
			
                            // Read
                            while ((in = reader.readLine()) != null) {
				Element category = new Element(CATEGORY);
				Element pattern = new Element(PATTERN);
				pattern.addContent(in);
				Element template = new Element(TEMPLATE);
				template.addContent("Sorry.  I don't know.");
				category.addContent(pattern);
				category.addContent(template);
				root.addContent(category);
				total++;
                            }
			
                            // Output 
                            out.output(doc, new FileOutputStream(TARGETS_AIML));
  
                        }
  
                        // Cleanup

                        PrintStream ps = new PrintStream(new FileOutputStream("TARGETS.data"));
                        ps.print("");
                        
			System.out.println("Finished reading targets.");
			System.out.println("Created " + total + " categories from TARGETS.data");

		} catch (Exception e) {
			System.out.println(e);
		}
		
	}
	
	public static void main(String[] args) {
		if (args.length > 0) {
			StringBuffer buffer = new StringBuffer();
			for (int x = 0; x < args.length; x++) {
				buffer.append(args[x] + " ");
			}
			new Targets(buffer.toString().trim());
		} else {
			new Targets();
		}
	}
	
}
