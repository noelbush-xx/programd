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
    - complete javadoc
    - made all imports explicit
*/

/*
    Further optimizations {4.1.3 [01] - November 2001, Noel Bush)
    - changed to extend (not implement) AIMLProcessor (latter is now an abstract class)
      (includes necessary public field "label")
    - removed use of AIML10Tag (was removed)
    - removed lots of uses of String.toLowerCase() (XML *is* case-sensitive!!!)
*/

/*
    More fixes (4.1.3 [02] - November 2001, Noel Bush)
    - changed "predicate" to "name" in most places
    - added catches of NoSuchPredicateException
*/

/*
    Further fixes (4.1.3 [03] - December 2001, Noel Bush)
    - now uses PatternArbiter.matches() instead of String.equals() for
      comparing condition/li value attributes to predicate values
*/

/*
    4.1.4 [00] - December 2001, Noel Bush
    - fixed wrong parameter order in call to PatternArbiter.matches when
      processing blockConditions
    - finally got rid of third-party "LnkdLstItr"
    - changed to use PredicateMaster
*/

package org.alicebot.server.core.processor;

import java.util.LinkedList;
import java.util.ListIterator;

import org.alicebot.server.core.PredicateMaster;
import org.alicebot.server.core.util.Trace;
import org.alicebot.server.core.parser.TemplateParser;
import org.alicebot.server.core.parser.XMLNode;
import org.alicebot.server.core.util.NotAnAIMLPatternException;
import org.alicebot.server.core.util.PatternArbiter;
import org.alicebot.server.core.util.Toolkit;


/**
 *  Handles a
 *  <code><a href="http://www.alicebot.org/TR/2001/WD-aiml/#section-condition">condition</a></code>
 *  element.
 *
 *  @version    4.1.4
 *  @author     Jon Baer
 *  @author     Thomas Ringate, Pedro Colla
 *  @author     Noel Bush
 */
public class ConditionProcessor extends AIMLProcessor
{
    // Public constants.

    /** A nameValueListItem in a &lt;condition/&gt;. */
    public static final int NAME_VALUE_LI      = 1;

    /** A defaultListItem in a &lt;condition/&gt;. */
    public static final int DEFAULT_LI         = 2;

    /** A valueOnlyListItem in a &lt;condition/&gt;. */
    public static final int VALUE_ONLY_LI      = 3;
    
    public static final String label = "condition";

    // Convenience constants.

    /** The string &quot;li&quot;. */
    private static final String LI = "li";


    public String process(int level, XMLNode tag, TemplateParser parser) throws AIMLProcessorException
    {
        if (tag.XMLType == XMLNode.TAG)
        {
            if (tag.XMLChild == null)
            {
                return EMPTY_STRING;
            }

            String name  = Toolkit.getAttributeValue(NAME, tag.XMLAttr);
            String value = Toolkit.getAttributeValue(VALUE, tag.XMLAttr);

            /*
                Process a multiPredicateCondition:
                <condition>
                    <li name="xxx" value="xxx">...</li>
                    <li>...</li>
                </condition>
            */
            if ( (tag.XMLAttr.indexOf(NAME_EQUALS, 0)  < 0) &&
                 (tag.XMLAttr.indexOf(VALUE_EQUALS, 0) < 0) )
            {
                return processListItem(level, parser, tag.XMLChild,
                                       NAME_VALUE_LI, name, value);
            }


            /*
                Process a blockCondition:
                <condition name="xxx" value="yyy">
                    ...
                </condition>
            */
            if ( (tag.XMLAttr.indexOf(NAME_EQUALS,0)  >= 0) &&
                 (tag.XMLAttr.indexOf(VALUE_EQUALS,0) >= 0) )
            {
                try
                {
                    if (PatternArbiter.matches(PredicateMaster.get(name, parser.getUserID(), parser.getBotID()),
                                               value, true))
                    {
                        return processListItem(level, parser, tag.XMLChild,
                                                      DEFAULT_LI, EMPTY_STRING, EMPTY_STRING);
                    }
                }
                catch (NotAnAIMLPatternException e)
                {
                    Trace.devinfo(e.getMessage());
                    return EMPTY_STRING;
                }
                return EMPTY_STRING;
            }

            /*
                Process a singlePredicateCondition:
                <condition name="xxx">
                    <li value="yyy">...</li>
                    <li>...</li>
                </condition>
            */
            if ( (tag.XMLAttr.indexOf(NAME_EQUALS,0)  >= 0) &&
                 (tag.XMLAttr.indexOf(VALUE_EQUALS,0) <  0) )
            {
                return processListItem(level, parser, tag.XMLChild,
                                       VALUE_ONLY_LI, name, EMPTY_STRING);
            }

            // In other cases, return an empty string.
            return EMPTY_STRING;
        }
        else
        {
            throw new AIMLProcessorException("<condition></condition> must have content!");
        }
    }


    /**
     *  Evaluates an &lt;li/&gt; element inside a &lt;condition/&gt;.
     *
     *  @param level        the level we're at in the XML trie
     *  @param parser       the TemplateParser object responsible for this
     *  @param list         the XML trie
     *  @param listItemType one of {@link NAME_VALUE_LI}, {@link DEFAULT_LI} or {@link VALUE_ONLY_LI}
     *  @param name         the name attribute of the &lt;li/&gt; (if applicable)
     *  @param value        the value attribute of the &lt;li/&gt; (if applicable)
     *
     *  @return the result of processing this &lt;li/&gt;
     */
    public String processListItem(int level, TemplateParser parser, LinkedList list, int listItemType, String name, String value)
    {
        String response = EMPTY_STRING;
        ListIterator iterator;
        XMLNode node;

        // Verify there is something to work with.
        if (list == null)
        {
            return EMPTY_STRING;
        }

        // Point to the start of the XML trie to parse.
        iterator = list.listIterator(0);
        //*** iterator.advance();

        String predicateValue = EMPTY_STRING;
        String livalue  = EMPTY_STRING;
        String liname   = EMPTY_STRING;

        /*
            For <code>valueOnlyListItem</code>s, look at the parent
            &lt;condition/&gt; to get the predicate <code>name</code>.
        */
        if (listItemType == VALUE_ONLY_LI)
        {
            predicateValue = PredicateMaster.get(name, parser.getUserID(), parser.getBotID());
        }

        // Navigate through this entire level.
        while (iterator.hasNext())
        {
            node = (XMLNode)iterator.next();
            if (node != null)
            {
                switch(node.XMLType)
                {
                    // If text, just append to the response.
                    case XMLNode.DATA  :
                    case XMLNode.CDATA :
                        response = response + node.XMLData;
                        break;

                    case XMLNode.EMPTY :
                        try
                        {
                            response = response + parser.processTag(level++, node);
                        }
                        catch (ProcessorException e)
                        {
                            // Do nothing.
                        }
                        break;

                    // Collect and process listitems
                    case XMLNode.TAG :
                        // Only &lt;li&gt;&lt;/li&gt; structures allowed
                        if (!node.XMLData.equals(LI))
                        {
                            try
                            {
                                response = response + parser.processTag(level++, node);
                            }
                            catch (ProcessorException e)
                            {
                                // Do nothing.
                            }
                            break;
                        }

                        /*
                            Now decide what to do based on the listItemType,
                            which indicates what to expect from the parent &lt;condition/&gt;.
                        */
                        switch (listItemType)
                        {
                            // Evaluate listitems with both name and value attributes.
                            case NAME_VALUE_LI :
                                /*
                                    Look for tokens in the XML attributes for
                                    name and value.  If none are present, this is
                                    an unqualified &lt;li/&gt; (defaultListItem)
                                    and gets evaluated. (Strange.) Processing will
                                    continue even after this case, so the defaultListItem
                                    may be anywhere under &lt;condition/&gt;, not necessarily
                                    at the end. This is a violation of strict AIML 1.0.1.
                                */
                                if ( (node.XMLAttr.indexOf(NAME_EQUALS, 0)  < 0) &&
                                     (node.XMLAttr.indexOf(VALUE_EQUALS, 0) < 0) )
                                {
                                    response = response + parser.evaluate(level++, node.XMLChild);
                                    break;
                                }

                                // Ignore if there is not a name and a value.
                                if ( (node.XMLAttr.indexOf(NAME_EQUALS,0) < 0) ||
                                     (node.XMLAttr.indexOf(VALUE_EQUALS,0)< 0))
                                {
                                    break;
                                }

                                // Recover the values of the name and value attributes.
                                liname = Toolkit.getAttributeValue(NAME, node.XMLAttr);
                                livalue= Toolkit.getAttributeValue(VALUE, node.XMLAttr);

                                /*
                                    If the value of the predicate matches the value in the value
                                    attribute, process the response, otherwise skip.
                                */
                                try
                                {
                                    if (PatternArbiter.matches(PredicateMaster.get(liname, parser.getUserID(), parser.getBotID()),
                                                               livalue, true))
                                    {
                                        return response + parser.evaluate(level++, node.XMLChild);
                                    }
                                }
                                catch (NotAnAIMLPatternException e)
                                {
                                    Trace.devinfo(e.getMessage());
                                }
                                break;

                            // Evaluate listitems that are designated &quot;defaultListItem&quot; types.
                            case DEFAULT_LI :
                                response = response + parser.evaluate(level++, node.XMLChild);
                                break;

                            // Evaluate valueOnlyListItems.
                            case VALUE_ONLY_LI :
                                // If there is a value attribute, get it.
                                if (node.XMLAttr.indexOf(VALUE_EQUALS, 0) >= 0)
                                {
                                    livalue = Toolkit.getAttributeValue(VALUE, node.XMLAttr);
                                    /*
                                        If the value of the predicate matches the value in the value
                                        attribute, process the response, otherwise skip.
                                    */
                                    try
                                    {
                                        if (PatternArbiter.matches(predicateValue, livalue, true))
                                        {
                                            response = response + parser.evaluate(level++, node.XMLChild);
                                            return response;
                                        }
                                    }
                                    catch (NotAnAIMLPatternException e)
                                    {
                                        Trace.userinfo(e.getMessage());
                                    }
                                }
                                /*
                                    When there is no value attribute, we actually got the wrong li type,
                                    but process as a defaultListItem anyway (probably a bad idea).
                                */
                                else
                                {
                                    response = response + parser.evaluate(level++, node.XMLChild);
                                    return response;
                                }
                                break;

                                default :
                                    break;
                        }
                        default :
                            break;
                }
            }
        }
        return response;
    }

}

