/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor.aiml;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import org.aitools.programd.Core;
import org.aitools.programd.parser.TemplateParser;
import org.aitools.programd.processor.ProcessorException;
import org.aitools.programd.util.NotAnAIMLPatternException;
import org.aitools.programd.util.PatternArbiter;
import org.aitools.programd.util.UserError;

/**
 * Handles a
 * <code><a href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-condition">condition</a></code>
 * element.
 * 
 * @version 4.5
 * @author Jon Baer
 * @author Thomas Ringate, Pedro Colla
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class ConditionProcessor extends AIMLProcessor
{
    // Public constants.

    /** A nameValueListItem in a &lt;condition/&gt;. */
    public static final int NAME_VALUE_LI = 1;

    /** A defaultListItem in a &lt;condition/&gt;. */
    public static final int DEFAULT_LI = 2;

    /** A valueOnlyListItem in a &lt;condition/&gt;. */
    public static final int VALUE_ONLY_LI = 3;

    /** The label (as required by the registration scheme). */
    public static final String label = "condition";

    /**
     * Creates a new ConditionProcessor using the given Core.
     * 
     * @param coreToUse the Core object to use
     */
    public ConditionProcessor(Core coreToUse)
    {
        super(coreToUse);
    }

    /**
     * @see AIMLProcessor#process(Element, TemplateParser)
     */
    public String process(Element element, TemplateParser parser) throws ProcessorException
    {
        String name = element.getAttribute(NAME);
        String value = element.getAttribute(VALUE);

        /*
         * Process a multiPredicateCondition: <condition>
         * <li name="xxx" value="xxx"> ... </li><li> ... </li> </condition>
         */
        if (name.equals(EMPTY_STRING) && value.equals(EMPTY_STRING))
        {
            return processListItem(parser, element.getChildNodes(), NAME_VALUE_LI, name);
        }

        /*
         * Process a blockCondition: <condition name="xxx" value="yyy"> ...
         * </condition>
         */
        if (!name.equals(EMPTY_STRING) && !value.equals(EMPTY_STRING))
        {
            try
            {
                if (PatternArbiter.matches(parser.getCore().getPredicateMaster().get(name, parser.getUserID(), parser.getBotID()), value, true))
                {
                    return processListItem(parser, element.getChildNodes(), DEFAULT_LI, EMPTY_STRING);
                }
            }
            catch (NotAnAIMLPatternException e)
            {
                Logger.getLogger("programd").log(Level.WARNING, "ConditionProcessor got a non-AIML pattern in a value attribute.", e);
                return EMPTY_STRING;
            }
            return EMPTY_STRING;
        }

        /*
         * Process a singlePredicateCondition: <condition name="xxx">
         * <li value="yyy"> ... </li><li> ... </li> </condition>
         */
        if (!name.equals(EMPTY_STRING) && value.equals(EMPTY_STRING))
        {
            return processListItem(parser, element.getChildNodes(), VALUE_ONLY_LI, name);
        }

        // In other cases, return an empty string.
        return EMPTY_STRING;
    }

    /**
     * Evaluates an &lt;li/&gt; element inside a &lt;condition/&gt;.
     * 
     * @param parser the TemplateParser object responsible for this
     * @param list the XML trie
     * @param listItemType one of {@link #NAME_VALUE_LI},{@link #DEFAULT_LI}or
     *            {@link #VALUE_ONLY_LI}
     * @param name the name attribute of the &lt;li/&gt; (if applicable)
     * @return the result of processing this &lt;li/&gt;
     * @throws ProcessorException if there is an error in processing
     */
    public String processListItem(TemplateParser parser, NodeList list, int listItemType, String name) throws ProcessorException
    {
        String response = EMPTY_STRING;
        Node node;

        String predicateValue = EMPTY_STRING;
        String livalue = EMPTY_STRING;
        String liname = EMPTY_STRING;

        /*
         * For <code> valueOnlyListItem </code> s, look at the parent
         * &lt;condition/&gt; to get the predicate <code> name </code> .
         */
        if (listItemType == VALUE_ONLY_LI)
        {
            predicateValue = this.core.getPredicateMaster().get(name, parser.getUserID(), parser.getBotID());
        }

        // Navigate through this entire level.
        int listSize = list.getLength();
        for (int index = 0; index < listSize; index++)
        {
            node = list.item(index);
            if (node != null)
            {
                switch (node.getNodeType())
                {
                    // If text, just append to the response.
                    case Node.TEXT_NODE:
                    case Node.CDATA_SECTION_NODE:
                        response = response + node.getTextContent();
                        break;

                    case Node.ELEMENT_NODE:
                        if (node.getChildNodes().getLength() == 0)
                        {
                            try
                            {
                                response = response + parser.processElement((Element) node);
                            }
                            catch (ProcessorException e)
                            {
                                throw new UserError(e.getExplanatoryMessage(), e);
                            }
                        }
                        else
                        {
                            /*
                             * Now decide what to do based on the listItemType,
                             * which indicates what to expect from the parent
                             * &lt;condition/&gt;.
                             */
                            switch (listItemType)
                            {
                                // Evaluate listitems with both name and value
                                // attributes.
                                case NAME_VALUE_LI:
                                    /*
                                     * Look for tokens in the XML attributes for
                                     * name and value. If none are present, this
                                     * is an unqualified &lt;li/&gt;
                                     * (defaultListItem) and gets evaluated.
                                     * (Strange.) Processing will continue even
                                     * after this case, so the defaultListItem
                                     * may be anywhere under &lt;condition/&gt;,
                                     * not necessarily at the end. This is a
                                     * violation of strict AIML 1.0.1.
                                     */
                                    if ((node.getAttributes().getNamedItem(NAME) == null) && (node.getAttributes().getNamedItem(VALUE) == null))
                                    {
                                        response = response + parser.evaluate(node.getChildNodes());
                                        break;
                                    }

                                    // Ignore if there is not a name and a
                                    // value.
                                    if ((node.getAttributes().getNamedItem(NAME) == null) || (node.getAttributes().getNamedItem(VALUE) == null))
                                    {
                                        break;
                                    }

                                    // Recover the values of the name and value
                                    // attributes.
                                    liname = node.getAttributes().getNamedItem(NAME).getNodeValue();
                                    livalue = node.getAttributes().getNamedItem(VALUE).getNodeValue();

                                    /*
                                     * If the value of the predicate matches the
                                     * value in the value attribute, process the
                                     * response, otherwise skip.
                                     */
                                    try
                                    {
                                        if (PatternArbiter.matches(this.core.getPredicateMaster().get(liname, parser.getUserID(), parser.getBotID()),
                                                livalue, true))
                                        {
                                            return response + parser.evaluate(node.getChildNodes());
                                        }
                                    }
                                    catch (NotAnAIMLPatternException e)
                                    {
                                        Logger.getLogger("programd").log(Level.WARNING,
                                                "ConditionProcessor got a non-AIML pattern in a value attribute.", e);
                                    }
                                    break;

                                // Evaluate listitems that are designated
                                // &quot;defaultListItem&quot; types.
                                case DEFAULT_LI:
                                    response = response + parser.evaluate(node.getChildNodes());
                                    break;

                                // Evaluate valueOnlyListItems.
                                case VALUE_ONLY_LI:
                                    // If there is a value attribute, get it.
                                    if (node.getAttributes().getNamedItem(VALUE) != null)
                                    {
                                        livalue = node.getAttributes().getNamedItem(VALUE).getNodeValue();
                                        /*
                                         * If the value of the predicate matches
                                         * the value in the value attribute,
                                         * process the response, otherwise skip.
                                         */
                                        try
                                        {
                                            if (PatternArbiter.matches(predicateValue, livalue, true))
                                            {
                                                response = response + parser.evaluate(node.getChildNodes());
                                                return response;
                                            }
                                        }
                                        catch (NotAnAIMLPatternException e)
                                        {
                                            Logger.getLogger("programd").log(Level.WARNING,
                                                    "ConditionProcessor got a non-AIML pattern in a value attribute.", e);
                                        }
                                    }
                                    /*
                                     * When there is no value attribute, we
                                     * actually got the wrong li type, but
                                     * process as a defaultListItem anyway
                                     * (probably a bad idea).
                                     */
                                    else
                                    {
                                        response = response + parser.evaluate(node.getChildNodes());
                                        return response;
                                    }
                                    break;

                                default:
                                    break;
                            }
                        }

                    default:
                        break;
                }
            }
        }
        return response;
    }

}