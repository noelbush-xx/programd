/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.parser;

import org.aitools.programd.processor.BotProcessor;
import org.aitools.programd.processor.GetProcessor;
import org.aitools.programd.processor.IDProcessor;
import org.aitools.programd.processor.InputProcessor;
import org.aitools.programd.processor.LearnProcessor;
import org.aitools.programd.processor.SetProcessor;
import org.aitools.programd.processor.SizeProcessor;
import org.aitools.programd.processor.ThatProcessor;
import org.aitools.programd.processor.VersionProcessor;
import org.aitools.programd.util.XMLKit;

/**
 * Processes deprecated AIML tags. Uses the old &quot;giant conditional
 * tree&quot; approach, so it's slower and not preferable.
 * 
 * @since 4.1.3
 * @author Pedro Colla (moved here by Noel Bush)
 */
public class DeprecatedAIMLParser
{
    // Convenience constants.

    /** An empty string, for convenience. */
    private static final String EMPTY_STRING = "";

    /** A &quot;filename&quot; attribute. */
    private static final String FILENAME = "filename";

    /**
     * A <code>name=&quot;name&quot;</code> attribute (for &lt;name/&gt; and
     * &lt;getname/&gt; support)
     */
    private static final String ATTR_NAME_NAME = "name=\"name\"";

    /**
     * An <code>index=&quot;2,1&quot;</code> attribute (for
     * &lt;justbeforethat/&gt; support)
     */
    private static final String ATTR_INDEX_2_1 = "index=\"2,1\"";

    /**
     * An <code>index=&quot;2&quot;</code> attribute (for &lt;justthat/&gt;
     * support)
     */
    private static final String ATTR_INDEX_2 = "index=\"2\"";

    /**
     * An <code>index=&quot;3&quot;</code> attribute (for &lt;beforethat/&gt;
     * support)
     */
    private static final String ATTR_INDEX_3 = "index=\"3\"";

    /**
     * A <code>name=&quot;topic&quot;</code> attribute (for &lt;gettopic/&gt;
     * support)
     */
    private static final String ATTR_NAME_TOPIC = "name=\"topic\"";

    /**
     * Start of a <code>name=&quot;</code> construction (for
     * &lt;get_predicate&gt;&lt;/get_predicate&gt; support)
     */
    private static final String NAME_EQUALS_QUOTE = "name=\"";

    /** An underscore (for &lt;get_predicate&gt;&lt;/get_predicate&gt; support) */
    private static final String UNDERSCORE = "_";

    /** A quote mark, for convenience. */
    public static final String QUOTE_MARK = "\"";

    // AIML "0.9" constants.

    /** The string &quot;alice&quot;. */
    public static final String ALICE = "alice";

    /** The string &quot;name&quot;. */
    public static final String NAME = "name";

    /** The string &quot;justbeforethat&quot;. */
    public static final String JUSTBEFORETHAT = "justbeforethat";

    /** The string &quot;justthat&quot;. */
    public static final String JUSTTHAT = "justthat";

    /** The string &quot;beforethat&quot;. */
    public static final String BEFORETHAT = "beforethat";

    /** The string &quot;get_&quot;. */
    public static final String GET_OLD = "get_";

    /** The string &quot;get_ip&quot;. */
    public static final String GET_IP = "get_ip";

    /** The string &quot;getname&quot;. */
    public static final String GETNAME = "getname";

    /** The string &quot;getsize&quot;. */
    public static final String GETSIZE = "getsize";

    /** The string &quot;gettopic&quot;. */
    public static final String GETTOPIC = "gettopic";

    /** The string &quot;getversion&quot;. */
    public static final String GETVERSION = "getversion";

    /** The string &quot;load&quot;. */
    public static final String LOAD = "load";

    /** The string &quot;settopic&quot;. */
    public static final String SETTOPIC = "settopic";

    /** The string &quot;setname&quot;. */
    public static final String SETNAME = "setname";

    /** The string &quot;set_&quot;. */
    public static final String SET_OLD = "set_";

    // AIML 0.9 old-fashioned bot properties (deprecated)

    /** The string &quot;birthday&quot;. */
    public static final String BIRTHDAY = "birthday";

    /** The string &quot;birthplace&quot;. */
    public static final String BIRTHPLACE = "birthplace";

    /** The string &quot;boyfriend&quot;. */
    public static final String BOYFRIEND = "boyfriend";

    /** The string &quot;favoriteband&quot;. */
    public static final String FAVORITEBAND = "favoriteband";

    /** The string &quot;favoritebook&quot;. */
    public static final String FAVORITEBOOK = "favoritebook";

    /** The string &quot;favoritecolor&quot;. */
    public static final String FAVORITECOLOR = "favoritecolor";

    /** The string &quot;favoritefood&quot;. */
    public static final String FAVORITEFOOD = "favoritefood";

    /** The string &quot;favoritemovie&quot;. */
    public static final String FAVORITEMOVIE = "favoritemovie";

    /** The string &quot;favoritesong&quot;. */
    public static final String FAVORITESONG = "favoritesong";

    /** The string &quot;for_fun&quot;. */
    public static final String FOR_FUN = "for_fun";

    /** The string &quot;friends&quot;. */
    public static final String FRIENDS = "friends";

    /** The string &quot;gender&quot;. */
    public static final String GENDER = "gender";

    /** The string &quot;girlfriend&quot;. */
    public static final String GIRLFRIEND = "girlfriend";

    /** The string &quot;kind_music&quot;. */
    public static final String KIND_MUSIC = "kind_music";

    /** The string &quot;location&quot;. */
    public static final String LOCATION = "location";

    /** The string &quot;look_like&quot;. */
    public static final String LOOK_LIKE = "look_like";

    /** The string &quot;botmaster&quot;. */
    public static final String BOTMASTER = "botmaster";

    /** The string &quot;question&quot;. */
    public static final String QUESTION = "question";

    /** The string &quot;sign&quot;. */
    public static final String SIGN = "sign";

    /** The string &quot;talk_about&quot;. */
    public static final String TALK_ABOUT = "talk_about";

    /** The string &quot;wear&quot;. */
    public static final String WEAR = "wear";

    /**
     * <p>
     * Recursively processes purportedly deprecated AIML tags, with top-down,
     * inside-out order.
     * </p>
     * <p>
     * Uses the inefficient method of the old TemplateParser, so shouldn't be
     * used if one can help it.
     * </p>
     * <p>
     * The actual tag to process is driven by {@link TemplateParser#evaluate},
     * so strict sequencing is used. For each valid tag, the associated
     * {@link org.aitools.programd.processor.AIMLProcessor AIMLProcessor},
     * resolution code or {@link GenericParser#shortcutTag}processor is
     * activated.
     * </p>
     * <p>
     * If no tag is recognized, an
     * {@link org.aitools.programd.parser.UnknownDeprecatedAIMLException}is
     * thrown.
     * </p>
     * <p>
     * Tags are explored sequentially. The sequence has performance implications
     * (frequently-used tags should be first). However, the actual sequence
     * within the template <i>is </i> preserved.
     * </p>
     * 
     * @param level
     *            the current level in the XML trie
     * @param userid
     *            the user identifier
     * @param tag
     *            the tag being evaluated
     * @param parser
     *            the {@link TemplateParser}that called this
     * @return the result of processing the tag
     * @throws UnknownDeprecatedAIMLException
     *             if no tags are recognized
     */
    public static String processTag(int level, String userid, XMLNode tag, TemplateParser parser)
            throws UnknownDeprecatedAIMLException
    {
        if ((tag.XMLData.equals(LOAD)) && (tag.XMLType == XMLNode.EMPTY))
        {
            return parser.shortcutTag(level, LearnProcessor.label, XMLNode.TAG, EMPTY_STRING, XMLKit.getAttributeValue(
                    FILENAME, tag.XMLAttr), XMLNode.DATA);
        }

        /*
         * Any old forms are converted into AIML 1.0.1 compliant forms and
         * processing handed back.
         */

        // <name/> = <bot name="name"/>
        if ((tag.XMLData.equals(NAME)) && (tag.XMLType == XMLNode.EMPTY))
        {
            return parser.shortcutTag(level, BotProcessor.label, XMLNode.EMPTY, ATTR_NAME_NAME, EMPTY_STRING,
                    XMLNode.EMPTY);
        }

        // <justbeforethat/> = <that index="2,1"/>
        else if ((tag.XMLData.equals(JUSTBEFORETHAT)) && (tag.XMLType == XMLNode.EMPTY))
        {
            return parser.shortcutTag(level, ThatProcessor.label, XMLNode.EMPTY, ATTR_INDEX_2_1, EMPTY_STRING,
                    XMLNode.EMPTY);
        }

        // <justthat/> = <input index="2"/>
        else if ((tag.XMLData.equals(JUSTTHAT)) && (tag.XMLType == XMLNode.EMPTY))
        {
            return parser.shortcutTag(level, InputProcessor.label, XMLNode.EMPTY, ATTR_INDEX_2, EMPTY_STRING,
                    XMLNode.EMPTY);
        }

        // <beforethat/> = <input index="3"/>
        else if ((tag.XMLData.equals(BEFORETHAT)) && (tag.XMLType == XMLNode.EMPTY))
        {
            return parser.shortcutTag(level, InputProcessor.label, XMLNode.EMPTY, ATTR_INDEX_3, EMPTY_STRING,
                    XMLNode.EMPTY);
        }

        // <getname/> = <get name="name"/>
        else if ((tag.XMLData.equals(GETNAME)) && (tag.XMLType == XMLNode.EMPTY))
        {
            return parser.shortcutTag(level, GetProcessor.label, XMLNode.EMPTY, ATTR_NAME_NAME, EMPTY_STRING,
                    XMLNode.EMPTY);
        }

        // <getsize/> = <size/>
        else if ((tag.XMLData.equals(GETSIZE)) && (tag.XMLType == XMLNode.EMPTY))
        {
            return parser.shortcutTag(level, SizeProcessor.label, XMLNode.EMPTY, EMPTY_STRING, EMPTY_STRING,
                    XMLNode.EMPTY);
        }

        // <gettopic/> = <get name="topic"/>
        else if ((tag.XMLData.equals(GETTOPIC)) && (tag.XMLType == XMLNode.EMPTY))
        {
            return parser.shortcutTag(level, GetProcessor.label, XMLNode.EMPTY, ATTR_NAME_TOPIC, EMPTY_STRING,
                    XMLNode.EMPTY);
        }

        // <getversion/> = <version/>
        else if ((tag.XMLData.equals(GETVERSION)) && (tag.XMLType == XMLNode.EMPTY))
        {
            return parser.shortcutTag(level, VersionProcessor.label, XMLNode.EMPTY, EMPTY_STRING, EMPTY_STRING,
                    XMLNode.EMPTY);
        }

        // <get_ip/> = <id/>
        else if ((tag.XMLData.equals(GET_IP)) && (tag.XMLType == XMLNode.EMPTY))
        {
            return parser.shortcutTag(level, IDProcessor.label, XMLNode.EMPTY, EMPTY_STRING, EMPTY_STRING,
                    XMLNode.EMPTY);
        }

        // <settopic></settopic> = <set name="topic"></set>
        else if ((tag.XMLData.equals(SETTOPIC)) && (tag.XMLType == XMLNode.TAG))
        {
            return parser.shortcutTag(level, SetProcessor.label, XMLNode.TAG, ATTR_NAME_TOPIC, parser.evaluate(level,
                    tag.XMLChild), XMLNode.DATA);
        }

        // <setname></setname> = <set name="name"></set>
        else if ((tag.XMLData.equals(SETNAME)) && (tag.XMLType == XMLNode.TAG))
        {
            return parser.shortcutTag(level, SetProcessor.label, XMLNode.TAG, ATTR_NAME_NAME, parser.evaluate(level,
                    tag.XMLChild), XMLNode.DATA);
        }

        // <set_predicate></set_predicate> = <set name="predicate"></set>
        else if ((tag.XMLData.indexOf(SET_OLD, 0) >= 0) && (tag.XMLType == XMLNode.TAG))
        {
            return parser.shortcutTag(level, SetProcessor.label, XMLNode.TAG, NAME_EQUALS_QUOTE
                    + tag.XMLData.substring(tag.XMLData.indexOf(UNDERSCORE, 0) + 1, tag.XMLData.length()) + QUOTE_MARK,
                    parser.evaluate(level, tag.XMLChild), XMLNode.DATA);
        }

        // <get_predicate/> = <get name="predicate"/>
        else if ((tag.XMLData.indexOf(GET_OLD, 0) >= 0) && (tag.XMLType == XMLNode.EMPTY))
        {
            return parser.shortcutTag(level, GetProcessor.label, XMLNode.EMPTY, NAME_EQUALS_QUOTE
                    + tag.XMLData.substring(tag.XMLData.indexOf(UNDERSCORE, 0) + 1, tag.XMLData.length()) + QUOTE_MARK,
                    EMPTY_STRING, XMLNode.DATA);
        }

        /*
         * TAG DISPATCHER: AIML 0.9 custom bot predicates These are implemented
         * as short-cut tags.
         */

        // <birthday/> = <bot name="birthday"/>
        else if ((tag.XMLData.equals(BIRTHDAY)) && (tag.XMLType == XMLNode.EMPTY))
        {
            return parser.shortcutTag(level, BotProcessor.label, XMLNode.EMPTY, "name=\"birthday\"", EMPTY_STRING,
                    XMLNode.EMPTY);
        }

        // <birthplace/> = <bot name="birthplace"/>
        else if ((tag.XMLData.equals(BIRTHPLACE)) && (tag.XMLType == XMLNode.EMPTY))
        {
            return parser.shortcutTag(level, BotProcessor.label, XMLNode.EMPTY, "name=\"birthplace\"", EMPTY_STRING,
                    XMLNode.EMPTY);
        }

        // <boyfriend/> = <bot name="boyfriend"/>
        else if ((tag.XMLData.equals(BOYFRIEND)) && (tag.XMLType == XMLNode.EMPTY))
        {
            return parser.shortcutTag(level, BotProcessor.label, XMLNode.EMPTY, "name=\"boyfriend\"", EMPTY_STRING,
                    XMLNode.EMPTY);
        }

        // <favoriteband/> = <bot name="favoriteband"/>
        else if ((tag.XMLData.equals(FAVORITEBAND)) && (tag.XMLType == XMLNode.EMPTY))
        {
            return parser.shortcutTag(level, BotProcessor.label, XMLNode.EMPTY, "name=\"favoriteband\"", EMPTY_STRING,
                    XMLNode.EMPTY);
        }

        // <favoritebook/> = <bot name="favoritebook"/>
        else if ((tag.XMLData.equals(FAVORITEBOOK)) && (tag.XMLType == XMLNode.EMPTY))
        {
            return parser.shortcutTag(level, BotProcessor.label, XMLNode.EMPTY, "name=\"favoritebook\"", EMPTY_STRING,
                    XMLNode.EMPTY);
        }

        // <favoritecolor/> = <bot name="favoritecolor"/>
        else if ((tag.XMLData.equals(FAVORITECOLOR)) && (tag.XMLType == XMLNode.EMPTY))
        {
            return parser.shortcutTag(level, BotProcessor.label, XMLNode.EMPTY, "name=\"favoritecolor\"", EMPTY_STRING,
                    XMLNode.EMPTY);
        }

        // <favoritefood/> = <bot name="favoritefood"/>
        else if ((tag.XMLData.equals(FAVORITEFOOD)) && (tag.XMLType == XMLNode.EMPTY))
        {
            return parser.shortcutTag(level, BotProcessor.label, XMLNode.EMPTY, "name=\"favoritefood\"", EMPTY_STRING,
                    XMLNode.EMPTY);
        }

        // <favoritemovie/> = <bot name="favoritemovie"/>
        else if ((tag.XMLData.equals(FAVORITEMOVIE)) && (tag.XMLType == XMLNode.EMPTY))
        {
            return parser.shortcutTag(level, BotProcessor.label, XMLNode.EMPTY, "name=\"favoritemovie\"", EMPTY_STRING,
                    XMLNode.EMPTY);
        }

        // <favoritesong/> = <bot name="favoritesong"/>
        else if ((tag.XMLData.equals(FAVORITESONG)) && (tag.XMLType == XMLNode.EMPTY))
        {
            return parser.shortcutTag(level, BotProcessor.label, XMLNode.EMPTY, "name=\"favoritesong\"", EMPTY_STRING,
                    XMLNode.EMPTY);
        }

        // <for_fun/> = <bot name="for_fun"/>
        else if ((tag.XMLData.equals(FOR_FUN)) && (tag.XMLType == XMLNode.EMPTY))
        {
            return parser.shortcutTag(level, BotProcessor.label, XMLNode.EMPTY, "name=\"forfun\"", EMPTY_STRING,
                    XMLNode.EMPTY);
        }

        // <friends/> = <bot name="friends"/>
        else if ((tag.XMLData.equals(FRIENDS)) && (tag.XMLType == XMLNode.EMPTY))
        {
            return parser.shortcutTag(level, BotProcessor.label, XMLNode.EMPTY, "name=\"friends\"", EMPTY_STRING,
                    XMLNode.EMPTY);
        }

        // <gender/> = <bot name="gender"/>
        else if ((tag.XMLData.equals(GENDER)) && (tag.XMLType == XMLNode.EMPTY))
        {
            return parser.shortcutTag(level, BotProcessor.label, XMLNode.EMPTY, "name=\"gender\"", EMPTY_STRING,
                    XMLNode.EMPTY);
        }

        // <girlfriend/> = <bot name="girlfriend"/>
        else if ((tag.XMLData.equals(GIRLFRIEND)) && (tag.XMLType == XMLNode.EMPTY))
        {
            return parser.shortcutTag(level, BotProcessor.label, XMLNode.EMPTY, "name=\"girlfriend\"", EMPTY_STRING,
                    XMLNode.EMPTY);
        }

        // <kind_music/> = <bot name="kind_music"/>
        else if ((tag.XMLData.equals(KIND_MUSIC)) && (tag.XMLType == XMLNode.EMPTY))
        {
            return parser.shortcutTag(level, BotProcessor.label, XMLNode.EMPTY, "name=\"kindmusic\"", EMPTY_STRING,
                    XMLNode.EMPTY);
        }

        // <location/> = <bot name="location"/>
        else if ((tag.XMLData.equals(LOCATION)) && (tag.XMLType == XMLNode.EMPTY))
        {
            return parser.shortcutTag(level, BotProcessor.label, XMLNode.EMPTY, "name=\"location\"", EMPTY_STRING,
                    XMLNode.EMPTY);
        }

        // <look_like/> = <bot name="look_like"/>
        else if ((tag.XMLData.equals(LOOK_LIKE)) && (tag.XMLType == XMLNode.EMPTY))
        {
            return parser.shortcutTag(level, BotProcessor.label, XMLNode.EMPTY, "name=\"looklike\"", EMPTY_STRING,
                    XMLNode.EMPTY);
        }

        // <botmaster/> = <bot name="botmaster"/>
        else if ((tag.XMLData.equals(BOTMASTER)) && (tag.XMLType == XMLNode.EMPTY))
        {
            return parser.shortcutTag(level, BotProcessor.label, XMLNode.EMPTY, "name=\"master\"", EMPTY_STRING,
                    XMLNode.EMPTY);
        }

        // <question/> = <bot name="question"/>
        else if ((tag.XMLData.equals(QUESTION)) && (tag.XMLType == XMLNode.EMPTY))
        {
            return parser.shortcutTag(level, BotProcessor.label, XMLNode.EMPTY, "name=\"question\"", EMPTY_STRING,
                    XMLNode.EMPTY);
        }

        // <sign/> = <bot name="sign"/>
        else if ((tag.XMLData.equals(SIGN)) && (tag.XMLType == XMLNode.EMPTY))
        {
            return parser.shortcutTag(level, BotProcessor.label, XMLNode.EMPTY, "name=\"sign\"", EMPTY_STRING,
                    XMLNode.EMPTY);
        }

        // <talk_about/> = <bot name="talk_about"/>
        else if ((tag.XMLData.equals(TALK_ABOUT)) && (tag.XMLType == XMLNode.EMPTY))
        {
            return parser.shortcutTag(level, BotProcessor.label, XMLNode.EMPTY, "name=\"talkabout\"", EMPTY_STRING,
                    XMLNode.EMPTY);
        }

        // <wear/> = <bot name="wear"/>
        else if ((tag.XMLData.equals(WEAR)) && (tag.XMLType == XMLNode.EMPTY))
        {
            return parser.shortcutTag(level, BotProcessor.label, XMLNode.EMPTY, "name=\"wear\"", EMPTY_STRING,
                    XMLNode.EMPTY);
        }
        else
        {
            throw new UnknownDeprecatedAIMLException();
        }
    }
}