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
@version 4.1.2
*/

import org.alicebot.server.core.node.*;
import java.util.*;
import java.text.*;
import java.lang.*;
import java.net.*;
import java.io.*;   
import javax.swing.*;

/*=======================================================================*/
/**
  This class implements an object to store the results of the matching
  and specially the stacks resulting from multiple input/that/topic stars
  It was extracted essentially "as is" from ProgramB
*/
/*=======================================================================*/
public class Match {

        /**
          Public properties of the method
        */
        public Stack  inputStar = new Stack();
        public Stack  thatStar  = new Stack();
        public Stack  topicStar = new Stack();
	public String inputPattern;
	public String thatPattern;
	public String topicPattern;
        public String path      = "";
	public Nodemapper node;

        /**
          Push a new input star into the input stack
        */
	public void addInputStar(String string) {
		inputStar.push(string);
	}

        /**
          Push a new that star into the thatstar stack
        */
	public void addThatStar(String string) {
		thatStar.push(string);
	}

        /**
          Push a new topic star into the topicstar stack
        */
	public void addTopicStar(String string) {
		topicStar.push(string);
	}

        /**
          Set the input pattern
        */
	public void setInputPattern(String string) {
		this.inputPattern = string;
	}
        /**
          Set the That pattern
        */
	public void setThatPattern(String string) {
		this.thatPattern = string;
	}
        /**
          Set the Topic Pattern
        */
	public void setTopicPattern(String string) {
		this.topicPattern = string;
	}
        /**
          Set the path pattern
        */
	public void setPath(String path) {
		this.path = path;
	}
        /**
          Set the resulting node
        */

	public void setNodemapper(Nodemapper node) {
		this.node = node;
	}
        /**
          Get the template
        */
	public String getTemplate() {
		return (String)this.node.get("<template>");
	}
        /**
          Get the filename the template originally come from
        */
	public String getFileName() {
		return (String)this.node.get("<filename>");
	}
        /**
          Get the input stars out of the stack and return them
          as a vector.
        */
	public Vector getInputStars() {
		Vector v = new Vector();
		while(!this.inputStar.empty()) {
			v.add((String)this.inputStar.pop());
		}
		return v;
	}
        /**
          Get the that stars out of the stack and return them
          as a vector
        */
        public Vector getThatStars() {
		Vector v = new Vector();
                while(!this.thatStar.empty()) {
                        v.add((String)this.thatStar.pop());
		}
		return v;
	}
        /**
          Get the TopicStars out of the stack and return them
          as a vector
        */
        public Vector getTopicStars() {
		Vector v = new Vector();
                while(!this.topicStar.empty()) {
                        v.add((String)this.topicStar.pop());
		}
		return v;
	}
}
