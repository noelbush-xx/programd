package org.alicebot.server.core.parser;


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

import java.lang.ref.*;


/**
 * The AIMLTag class represents the current tag set.
 *
 * IMPORTANT!!! The AIMLParser will be superceded by a new one in the future (AIMLParser2).
 * It is provided as a platform testing parser to finalize and debug certain
 * tag sets.  Please do not edit it, instead send feature requests to the Alicebot
 * list (alicebot-subscribe@listbot.com) and voice your opinion.  
 * 
 * @author Jon Baer
 * @version 1.0
 */

public class AIMLTag
{
	
	// AIML 1.0 Pattern Side
	public static final String AIML = "<aiml>";
	public static final String AIML_OPEN = "<aiml ";
	public static final String AIML_CLOSE = "</aiml>";
	public static final String ALICE = "<alice>"; // Deprecated
	public static final String ALICE_OPEN = "<alice "; // Deprecated
	public static final String ALICE_CLOSE = "</alice>"; // Deprecated
	public static final String TOPIC = "<topic>";
	public static final String TOPIC_OPEN = "<topic ";
	public static final String TOPIC_CLOSE = "</topic>";
	public static final String PATTERN = "<pattern>";
	public static final String PATTERN_OPEN = "<pattern ";
	public static final String PATTERN_CLOSE = "</pattern>";
	public static final String THAT = "<that>";
	public static final String THAT_OPEN = "<that ";
	public static final String THAT_CLOSE = "</that>";
	public static final String TEMPLATE = "<template>";
	public static final String TEMPLATE_OPEN = "<template ";
	public static final String TEMPLATE_CLOSE = "</template>";
	public static final String CATEGORY = "<category>";
	public static final String CATEGORY_OPEN = "<category ";
	public static final String CATEGORY_CLOSE = "</category>";


	
	// AIML 1.0 Template Side
	public static final String LOGIN = "<login/>";
	public static final String LOGOUT = "<logout/>";
	public static final String ELSE = "<else/>";
	public static final String LOAD_FILENAME = "<load filename=\""; // Deprecated
	public static final String STAR_VALUE = "<star/>";
	public static final String THATSTAR_VALUE = "<thatstar/>";
	public static final String TOPICSTAR_VALUE = "<topicstar/>";
	public static final String THAT_VALUE = "<that/>";
	public static final String JUSTTHAT_VALUE = "<justthat/>";
	public static final String BEFORETHAT_VALUE = "<beforethat/>";
	public static final String JUSTBEFORETHAT_VALUE = "<justbeforethat/>";
	public static final String IP = "<ip/>";
        public static final String IP_OPEN = "<ip ";
        public static final String IP_CLOSE = "</ip>";
	public static final String IF_OPEN = "<if ";
	public static final String IF_CLOSE = "</if>";
	public static final String CONDITION = "<condition>";
	public static final String CONDITION_OPEN = "<condition ";
	public static final String CONDITION_CLOSE = "</condition>";
	public static final String LISTITEM = "<li>";
	public static final String LISTITEM_OPEN = "<li ";
	public static final String LISTITEM_CLOSE = "</li>";
	public static final String RANDOM = "<random>";
	public static final String RANDOM_OPEN = "<random ";
	public static final String RANDOM_CLOSE = "</random>";
	public static final String UPPERCASE = "<uppercase>";
	public static final String UPPERCASE_CLOSE = "</uppercase>";
	public static final String LOWERCASE = "<lowercase>";
	public static final String LOWERCASE_CLOSE = "</lowercase>";
	public static final String FORMAL = "<formal>";
	public static final String FORMAL_CLOSE = "</formal>";
	public static final String SENTENCE = "<sentence>";
	public static final String SENTENCE_CLOSE = "</sentence>";
	public static final String GOSSIP = "<gossip>";
	public static final String GOSSIP_CLOSE = "</gossip>";
	public static final String THINK = "<think>";
	public static final String THINK_CLOSE = "</think>";
	public static final String SYSTEM = "<system>";
	public static final String SYSTEM_CLOSE = "</system>";
	public static final String SCRIPT = "<script>";
	public static final String SCRIPT_OPEN = "<script ";
	public static final String SCRIPT_CLOSE = "</script>";
	public static final String SR = "<sr/>";
	public static final String SR_STAR = "<srai><star/></srai>";
	public static final String SRAI = "<srai>";
	public static final String SRAI_OPEN = "<srai ";
	public static final String SRAI_CLOSE = "</srai>";
	
	// Memory Actions
	public static final String LEARN = "<learn>";
	public static final String LEARN_CLOSE = "</learn>";
	
	// Display Actions
	
	// Face Actions
	
	// Bot Properties
	public static final String BOT_OPEN = "<bot_";
	public static final String BOT_CLOSE = "</bot_";
	public static final String BOT_NAME = "<bot_name/>";
	public static final String NAME_VALUE = "<name/>"; // Deprecated
	
	// Database Properties
	public static final String GET_OPEN = "<get_";
	public static final String GET_CLOSE = "</get_";
	public static final String SET_OPEN = "<set_";
	public static final String SET_CLOSE = "</set_";
	
	// Template Properties

        //Newly added AIML Tags (1.0 compliance) PEC 09-2001
        public static final String INPUT_OPEN = "<input ";   //New 4.0.3 b2
        public static final String INPUT_VALUE = "<input/>"; //New 4.0.3 b2
        public static final String GENDER_OPEN = "<gender>"; //New 4.0.3 b2
        public static final String GENDER_CLOSE = "</gender>"; //New 4.0.3 b2
        public static final String DATE_VALUE = "<date/>";   //New 4.0.3 b2
        public static final String ID_VALUE = "<id/>";       //New 4.0.3 b2
        public static final String GET_VALUE = "<get ";      //New 4.0.3 b2
        public static final String SET_NEW_OPEN  = "<set ";  //New 4.0.3 b2
        public static final String SET_NEW_CLOSE = "</set>"; //New 4.0.3 b2
        public static final String SIZE_VALUE = "<size/>";   //New 4.0.3 b2
        public static final String VERSION_VALUE = "<version/>"; //New 4.0.3 b2
        public static final String PERSON_VALUE = "<person/>"; //New 4.0.3 b2
        public static final String PERSON_OPEN = "<person>"; //New 4.0.3 b2
        public static final String PERSON_CLOSE = "</person>"; //New 4.0.3 b2
        public static final String PERSON2_OPEN = "<person2>"; //New 4.0.3 b2
        public static final String PERSON2_CLOSE = "</person2>"; //New 4.0.3 b2
        public static final String PERSON2_VALUE = "<person2/>"; //New 4.0.3 b2
        public static final String JAVASCRIPT_OPEN = "<javascript>"; //New 4.0.3 b2
        public static final String JAVASCRIPT_CLOSE = "</javascript>"; //New 4.0.3 b2
        public static final String THAT_NEW = "<that "; //New 4.0.3 b2
        public static final String STAR_NEW = "<star "; //New 4.0.3 b2
        public static final String THATSTAR_NEW = "<thatstar "; //New 4.0.3 b2
        public static final String TOPICSTAR_NEW = "<topicstar "; //New 4.0.3 b2
        public static final String GOSSIP_NEW = "<gossip "; //New 4.0.3 b2
        public static final String BOT_NEW_OPEN = "<bot "; //New 4.0.3 b2

        //Newly added deprecated AIML 0.9 Tags PEC 09-2001 (??)
        public static final String GETNAME_VALUE = "<getname/>"; //New 4.0.3 b2
        public static final String GETSIZE_VALUE = "<getsize/>"; //New 4.0.3 b2
        public static final String GETTOPIC_VALUE = "<gettopic/>"; //New 4.0.3 b2
        public static final String GETVERSION_VALUE = "<getversion/>"; //New 4.0.3 b2
        public static final String SETTOPIC_OPEN = "<settopic>"; //New 4.0.3 b2
        public static final String SETTOPIC_CLOSE = "</settopic>"; //New 4.0.3 b2


/*Removed 4.0.3 b2 Experimental Tags Removed

	public static final String TYPEOF = "<typeof_";
	public static final String EVENT = "<event>";
	public static final String EVENT_OPEN = "<event ";
	public static final String EVENT_CLOSE = "</event>";
	public static final String ERROR = "<error>";
	public static final String ERROR_OPEN = "<error ";
	public static final String ERROR_CLOSE = "</error>";
	public static final String LISTEN = "<listen>";
	public static final String LISTEN_OPEN = "<listen ";
	public static final String LISTEN_CLOSE = "</listen>";
	public static final String ENCODE = "<encode>";
	public static final String ENCODE_CLOSE = "</encode>";
	public static final String DECODE = "<decode>";
	public static final String DECODE_CLOSE = "</decode>";
	public static final String OS = "<os/>";
	public static final String LOG = "<log>";
	public static final String LOG_CLOSE = "</log>";
	public static final String TIMER = "<timer>";
	public static final String TIMER_OPEN = "<timer ";
	public static final String TIMER_CLOSE = "</timer>";
	public static final String NQL = "<nql>";
	public static final String NQL_OPEN = "<nql ";
	public static final String NQL_CLOSE = "</nql>";
	public static final String FACE = "<face>";
	public static final String FACE_OPEN = "<face ";
	public static final String FACE_CLOSE = "</face>";
	public static final String DISPLAY = "<display>";
	public static final String DISPLAY_OPEN = "<display ";
	public static final String DISPLAY_CLOSE = "</display>";
	public static final String ALICE_IN = "<alice_in/>";
	public static final String ALICE_OUT = "<alice_out/>";
	public static final String FORGET = "<forget>";
	public static final String FORGET_CLOSE = "</forget>";
	public static final String ADD_OPEN = "<add_";
	public static final String ADD_CLOSE = "</add_";
	public static final String RND_OPEN = "<random_";
	public static final String RND_CLOSE = "</random_";

*/

}

