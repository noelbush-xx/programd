package org.alicebot.server.core.AIMLparser;


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

import java.lang.ref.*;

/**
 * The AIMLTag class represents the current tag set, it is used
 * by the parser to define which are valid AIML tags.
 *
 * @author Thomas Ringate/Pedro Colla
 * @version 4.1.1
 */

public class AIML10Tag
{

        // AIML 1.0 Main Tagset (Pattern Side)
        public static final String AIML          = "aiml";
        public static final String CATEGORY      = "category";
        public static final String PATTERN       = "pattern";
        public static final String TOPIC         = "topic";
        public static final String TEMPLATE      = "template";


        // AIML 1.0 Main Tagset (Pattern & Template Side)
        public static final String BOT           = "bot";
        public static final String THAT          = "that";

        // AIML 1.0 Main Tagset (Template Side)
        public static final String INPUT         = "input";
        public static final String CONDITION     = "condition";
        public static final String GENDER        = "gender";
        public static final String DATE          = "date";
        public static final String ID            = "id";
        public static final String GET           = "get";
        public static final String SIZE          = "size";
        public static final String STAR          = "star";
        public static final String THATSTAR      = "thatstar";
        public static final String TOPICSTAR     = "topicstar";
        public static final String VERSION       = "version";
        public static final String GOSSIP        = "gossip";
        public static final String LEARN         = "learn";
        public static final String PERSON        = "person";
        public static final String PERSON2       = "person2";
        public static final String RANDOM        = "random";
        public static final String SET           = "set";
        public static final String SR            = "sr";
        public static final String SRAI          = "srai";
        public static final String SYSTEM        = "system";
        public static final String THINK         = "think";
        public static final String UPPERCASE     = "uppercase";
        public static final String LOWERCASE     = "lowercase";
        public static final String SENTENCE      = "sentence";
        public static final String FORMAL        = "formal";
        public static final String IF            = "if";
        public static final String JAVASCRIPT    = "javascript";


        // AIML 1.0 Child Only Tags
        public static final String LI            = "li";
        public static final String THEN          = "then";
        public static final String ELSE          = "else";


        // AIML 0.9 TagSet (deprecated)
        public static final String ALICE         = "alice";
        public static final String NAME          = "name";
        public static final String JUSTBEFORETHAT= "justbeforethat";
        public static final String JUSTTHAT      = "justthat";
        public static final String BEFORETHAT    = "beforethat";
        public static final String GET_OLD       = "get_";
        public static final String GET_IP        = "get_ip";
        public static final String GETNAME       = "getname";
        public static final String GETSIZE       = "getsize";
        public static final String GETTOPIC      = "gettopic";
        public static final String GETVERSION    = "getversion";
        public static final String LOAD          = "load";
        public static final String SETTOPIC      = "settopic";
        public static final String SETNAME       = "setname";
        public static final String SET_OLD       = "set_";


        // AIML 0.9 Old fashion bot properties (deprecated)
        public static final String BIRTHDAY      = "birthday";
        public static final String BIRTHPLACE    = "birthplace";
        public static final String BOYFRIEND     = "boyfriend";
        public static final String FAVORITEBAND  = "favoriteband";
        public static final String FAVORITEBOOK  = "favoritebook";
        public static final String FAVORITECOLOR = "favoritecolor";
        public static final String FAVORITEFOOD  = "favoritefood";
        public static final String FAVORITEMOVIE = "favoritemovie";
        public static final String FAVORITESONG  = "favoritesong";
        public static final String FOR_FUN       = "for_fun";
        public static final String FRIENDS       = "friends";
        public static final String GIRLFRIEND    = "girlfriend";
        public static final String KIND_MUSIC    = "kind_music";
        public static final String LOCATION      = "location";
        public static final String LOOK_LIKE     = "look_like";
        public static final String BOTMASTER     = "botmaster";
        public static final String QUESTION      = "question";
        public static final String SIGN          = "sign";
        public static final String TALK_ABOUT    = "talk_about";
        public static final String WEAR          = "wear";

        //Non-standard AIML tags used thru the configuration phase only
        public static final String PROPERTY      = "property";


}
