/*
    Alicebot Program D
    Copyright (C) 1995-2001, A.L.I.C.E. AI Foundation
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
    USA.
 */

/*
    Code cleanup (4.1.3 [00] - October 2001, Noel Bush)
    - formatting cleanup
    - general grammar fixes
    - removed unnecessary import
    - removed if, else, login, logout
*/

package org.alicebot.server.core.parser;


/**
 *  <p>
 *  Holds constants used for parsing AIML.
 *  </p>
 *  <p>
 *  There is some redundancy with {@link AIML10Tag} at present;
 *  both classes should be consolidated at some point.
 *  </p>
 *
 *  @author Jon Baer
 *  @version 1.0
 */
public class AIMLTag
{
    /** '<code>&lt;aiml&gt;</code>' */
    public static final String AIML                  = "<aiml>";

    /** '<code>&lt;aiml </code>' */
    public static final String AIML_OPEN             = "<aiml ";

    /** '<code>&lt;/aiml&gt;</code>' */
    public static final String AIML_CLOSE            = "</aiml>";

    /** '<code>&lt;alice&gt;</code>' (deprecated) */
    public static final String ALICE                 = "<alice>";

    /** '<code>&lt;alice </code>' (deprecated) */
    public static final String ALICE_OPEN            = "<alice ";

    /** '<code>&lt;/alice&gt;</code>' (deprecated) */
    public static final String ALICE_CLOSE           = "</alice>";

    /** '<code>&lt;topic&gt;</code>' */
    public static final String TOPIC                 = "<topic>";

    /** '<code>&lt;topic </code>' */
    public static final String TOPIC_OPEN            = "<topic ";

    /** '<code>&lt;/topic&gt;</code>' */
    public static final String TOPIC_CLOSE           = "</topic>";

    /** '<code>&lt;pattern&gt;</code>' */
    public static final String PATTERN               = "<pattern>";

    /** '<code>&lt;pattern </code>' */
    public static final String PATTERN_OPEN          = "<pattern ";

    /** '<code>&lt;/pattern&gt;</code>' */
    public static final String PATTERN_CLOSE         = "</pattern>";

    /** '<code>&lt;that&gt;</code>' */
    public static final String THAT                  = "<that>";

    /** '<code>&lt;that </code>' */
    public static final String THAT_OPEN             = "<that ";

    /** '<code>&lt;/that&gt;</code>' */
    public static final String THAT_CLOSE            = "</that>";

    /** '<code>&lt;template&gt;</code>' */
    public static final String TEMPLATE              = "<template>";

    /** '<code>&lt;template </code>' */
    public static final String TEMPLATE_OPEN         = "<template ";

    /** '<code>&lt;/template&gt;</code>' */
    public static final String TEMPLATE_CLOSE        = "</template>";

    /** '<code>&lt;category&gt;</code>' */
    public static final String CATEGORY              = "<category>";

    /** '<code>&lt;category </code>' */
    public static final String CATEGORY_OPEN         = "<category ";

    /** '<code>&lt;/category&gt;</code>' */
    public static final String CATEGORY_CLOSE        = "</category>";

    /** '<code>&lt;load filename=&quot; </code>' */
    public static final String LOAD_FILENAME         = "<load filename=\"";

    /** '<code>&lt;star/&gt;</code>' */
    public static final String STAR_VALUE            = "<star/>";

    /** '<code>&lt;thatstar/&gt;</code>' */
    public static final String THATSTAR_VALUE        = "<thatstar/>";

    /** '<code>&lt;topicstar/&gt;</code>' */
    public static final String TOPICSTAR_VALUE       = "<topicstar/>";

    /** '<code>&lt;that/&gt;</code>' */
    public static final String THAT_VALUE            = "<that/>";

    /** '<code>&lt;justthat/&gt;</code>' */
    public static final String JUSTTHAT_VALUE        = "<justthat/>";

    /** '<code>&lt;beforethat/&gt;</code>' */
    public static final String BEFORETHAT_VALUE      = "<beforethat/>";

    /** '<code>&lt;justbeforethat/&gt;</code>' */
    public static final String JUSTBEFORETHAT_VALUE  = "<justbeforethat/>";

    /** '<code>&lt;ip/&gt;</code>' */
    public static final String IP                    = "<ip/>";

    /** '<code>&lt;ip </code>' */
    public static final String IP_OPEN               = "<ip ";

    /** '<code>&lt;/ip&gt;</code>' */
    public static final String IP_CLOSE              = "</ip>";

    /** '<code>&lt;condition&gt;</code>' */
    public static final String CONDITION             = "<condition>";

    /** '<code>&lt;condition </code>' */
    public static final String CONDITION_OPEN        = "<condition ";

    /** '<code>&lt;/condition&gt;</code>' */
    public static final String CONDITION_CLOSE       = "</condition>";

    /** '<code>&lt;li&gt;</code>' */
    public static final String LISTITEM              = "<li>";

    /** '<code>&lt;li </code>' */
    public static final String LISTITEM_OPEN         = "<li ";

    /** '<code>&lt;/li&gt;</code>' */
    public static final String LISTITEM_CLOSE        = "</li>";

    /** '<code>&lt;random&gt;</code>' */
    public static final String RANDOM                = "<random>";

    /** '<code>&lt;random </code>' */
    public static final String RANDOM_OPEN           = "<random ";

    /** '<code>&lt;/random&gt;</code>' */
    public static final String RANDOM_CLOSE          = "</random>";

    /** '<code>&lt;uppercase&gt;</code>' */
    public static final String UPPERCASE             = "<uppercase>";

    /** '<code>&lt;/uppercase&gt;</code>' */
    public static final String UPPERCASE_CLOSE       = "</uppercase>";

    /** '<code>&lt;lowercase&gt;</code>' */
    public static final String LOWERCASE             = "<lowercase>";

    /** '<code>&lt;/lowercase&gt;</code>' */
    public static final String LOWERCASE_CLOSE       = "</lowercase>";

    /** '<code>&lt;formal&gt;</code>' */
    public static final String FORMAL                = "<formal>";

    /** '<code>&lt;/formal&gt;</code>' */
    public static final String FORMAL_CLOSE          = "</formal>";

    /** '<code>&lt;sentence&gt;</code>' */
    public static final String SENTENCE              = "<sentence>";

    /** '<code>&lt;/sentence&gt;</code>' */
    public static final String SENTENCE_CLOSE        = "</sentence>";

    /** '<code>&lt;gossip&gt;</code>' */
    public static final String GOSSIP                = "<gossip>";

    /** '<code>&lt;/gossip&gt;</code>' */
    public static final String GOSSIP_CLOSE          = "</gossip>";

    /** '<code>&lt;think&gt;</code>' */
    public static final String THINK                 = "<think>";

    /** '<code>&lt;/think&gt;</code>' */
    public static final String THINK_CLOSE           = "</think>";

    /** '<code>&lt;system&gt;</code>' */
    public static final String SYSTEM                = "<system>";

    /** '<code>&lt;/system&gt;</code>' */
    public static final String SYSTEM_CLOSE          = "</system>";

    /** '<code>&lt;script&gt;</code>' */
    public static final String SCRIPT                = "<script>";

    /** '<code>&lt;sr/&gt;</code>' */
    public static final String SR                    = "<sr/>";

    /** '<code>&lt;srai&gt;&lt;star/&gt;&lt;/srai&gt;</code>' */
    public static final String SR_STAR               = "<srai><star/></srai>";

    /** '<code>&lt;srai&gt;</code>' */
    public static final String SRAI                  = "<srai>";

    /** '<code>&lt;srai </code>' */
    public static final String SRAI_OPEN             = "<srai ";

    /** '<code>&lt;/srai&gt;</code>' */
    public static final String SRAI_CLOSE            = "</srai>";

    /** '<code>&lt;learn&gt;</code>' */
    public static final String LEARN                 = "<learn>";

    /** '<code>&lt;/learn&gt;</code>' */
    public static final String LEARN_CLOSE           = "</learn>";

    /** '<code>&lt;bot_</code>' */
    public static final String BOT_OPEN              = "<bot_";

    /** '<code>&lt;/bot_</code>' */
    public static final String BOT_CLOSE             = "</bot_";

    /** '<code>&lt;bot_name/&gt;</code>' */
    public static final String BOT_NAME              = "<bot_name/>";

    /** '<code>&lt;name/&gt;</code>' (deprecated) */
    public static final String NAME_VALUE            = "<name/>";

    /** '<code>&lt;get_</code>' */
    public static final String GET_OPEN              = "<get_";

    /** '<code>&lt;/get_</code>' */
    public static final String GET_CLOSE             = "</get_";

    /** '<code>&lt;set_</code>' */
    public static final String SET_OPEN              = "<set_";

    /** '<code>&lt;/set_</code>' */
    public static final String SET_CLOSE             = "</set_";
    
    /** '<code>&lt;input </code>' */
    public static final String INPUT_OPEN            = "<input ";

    /** '<code>&lt;input/&gt;</code>' */
    public static final String INPUT_VALUE           = "<input/>";

    /** '<code>&lt;gender&gt;</code>' */
    public static final String GENDER_OPEN           = "<gender>";

    /** '<code>&lt;/gender&gt;</code>' */
    public static final String GENDER_CLOSE          = "</gender>";

    /** '<code>&lt;date/&gt;</code>' */
    public static final String DATE_VALUE            = "<date/>";

    /** '<code>&lt;id/&gt;</code>' */
    public static final String ID_VALUE              = "<id/>";

    /** '<code>&lt;get </code>' */
    public static final String GET_VALUE             = "<get ";

    /** '<code>&lt;set </code' */
    public static final String SET_NEW_OPEN          = "<set ";

    /** '<code>&lt;/set&gt;</code>' */
    public static final String SET_NEW_CLOSE         = "</set>";

    /** '<code>&lt;size/&gt;</code>' */
    public static final String SIZE_VALUE            = "<size/>";

    /** '<code>&lt;version/&gt;</code>' */
    public static final String VERSION_VALUE         = "<version/>";

    /** '<code>&lt;person/&gt;</code>' */
    public static final String PERSON_VALUE          = "<person/>";

    /** '<code>&lt;person&gt;</code>' */
    public static final String PERSON_OPEN           = "<person>";

    /** '<code>&lt;/person&gt;</code>' */
    public static final String PERSON_CLOSE          = "</person>";

    /** '<code>&lt;person2&gt;</code>' */
    public static final String PERSON2_OPEN          = "<person2>";

    /** '<code>&lt;/person2&gt;</code>' */
    public static final String PERSON2_CLOSE         = "</person2>";

    /** '<code>&lt;person2/&gt;</code>' */
    public static final String PERSON2_VALUE         = "<person2/>";

    /** '<code>&lt;javascript&gt;</code>' */
    public static final String JAVASCRIPT_OPEN       = "<javascript>";

    /** '<code>&lt;/javascript&gt;</code>' */
    public static final String JAVASCRIPT_CLOSE      = "</javascript>";

    /** '<code>&lt;that </code>' */
    public static final String THAT_NEW              = "<that ";

    /** '<code>&lt;star </code>' */
    public static final String STAR_NEW              = "<star ";

    /** '<code>&lt;thatstar </code>' */
    public static final String THATSTAR_NEW          = "<thatstar ";

    /** '<code>&lt;topicstar </code>' */
    public static final String TOPICSTAR_NEW         = "<topicstar ";

    /** '<code>&lt;gossip </code>' */
    public static final String GOSSIP_NEW            = "<gossip ";

    /** '<code>&lt;bot </code>' */
    public static final String BOT_NEW_OPEN          = "<bot ";

    /** '<code>&lt;bot</code>' */
    public static final String BOT_OPEN_NOSPACE      = "<bot";

    /** '<code>&lt;getname/&gt;</code>' */
    public static final String GETNAME_VALUE         = "<getname/>";

    /** '<code>&lt;getsize/&gt;</code>' */
    public static final String GETSIZE_VALUE         = "<getsize/>";

    /** '<code>&lt;gettopic/&gt;</code>' */
    public static final String GETTOPIC_VALUE        = "<gettopic/>";

    /** '<code>&lt;getversion/&gt;</code>' */
    public static final String GETVERSION_VALUE      = "<getversion/>";

    /** '<code>&lt;settopic&gt;</code>' */
    public static final String SETTOPIC_OPEN         = "<settopic>";

    /** '<code>&lt;/settopic&gt;</code>' */
    public static final String SETTOPIC_CLOSE        = "</settopic>";
}

