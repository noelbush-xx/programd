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

package org.alicebot.server.core.targeting;


/**
 *  Contains constants used by targeting.
 *
 *  @author Noel Bush
 */
abstract public class Targeting
{
    /** The string &quot;targets&quot;. */
    public static final String TARGETS            = "targets";

    /** The start of a targets element. */
    public static final String TARGETS_START      = "<targets>";

    /** The end of a targets element. */
    public static final String TARGETS_END        = "</targets>";

    /** The start of a target element. */
    public static final String TARGET_START       = "<target>";

    /** The end of a target element. */
    public static final String TARGET_END         = "</target>";

    /** The start of an input element. */
    public static final String INPUT_START        = "<input>";

    /** The end of an input element. */
    public static final String INPUT_END          = "</input>";

    /** The start of a text element. */
    public static final String TEXT_START         = "<text>";

    /** The end of a text element. */
    public static final String TEXT_END           = "</text>";

    /** The start of a match element. */
    public static final String MATCH_START        = "<match>";

    /** The end of a match element. */
    public static final String MATCH_END          = "</match>";

    /** The start of a pattern element. */
    public static final String PATTERN_START      = "<pattern>";

    /** The end of a pattern element. */
    public static final String PATTERN_END        = "</pattern>";

    /** The start of a that element. */
    public static final String THAT_START         = "<that>";

    /** The end of a that element. */
    public static final String THAT_END           = "</that>";

    /** The start of a topic element. */
    public static final String TOPIC_START        = "<topic>";

    /** The end of a topic element. */
    public static final String TOPIC_END          = "</topic>";

    /** The beginning of a topic start marker with a name attribute. */
    public static final String TOPIC_NAME_BEGIN   = "<topic name=\"";

    /** The end of a topic start marker with a name attribute. */
    public static final String TOPIC_NAME_END     = "\">";

    /** The start of a category element. */
    public static final String CATEGORY_START     = "<category>";

    /** The end of a category element. */
    public static final String CATEGORY_END       = "</category>";

    /** The start of a template element. */
    public static final String TEMPLATE_START     = "<template>";

    /** The end of a template element. */
    public static final String TEMPLATE_END       = "</template>";

    /** The start of a reply element. */
    public static final String REPLY_START        = "<reply>";

    /** The end of a reply element. */
    public static final String REPLY_END          = "</reply>";

    /** An indent. */
    public static final String INDENT             = "    ";

    /** The system line separator string. */
    public static final String LINE_SEPARATOR     = System.getProperty("line.separator", "\n");

    /** A space. */
    public static final String SPACE              = " ";

    /** An empty string. */
    public static final String EMPTY_STRING       = "";
}

