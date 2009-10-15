package org.aitools.util.taglib;

import org.aitools.util.xml.Characters;

/**
 * Provides access to aitools-utils functions via JSP tags
 * or functions.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class Functions
{
    /**
     * Unescapes a given string by converting XML entities to
     * their character equivalents.
     * 
     * @param string
     * @return the unescaped string
     */
    public static String unescapeXML(String string)
    {
        return Characters.unescapeXMLChars(string);
    }
}
