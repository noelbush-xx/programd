/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor.aiml;

import java.util.List;

import org.jdom.Element;

import org.aitools.programd.Core;
import org.aitools.programd.parser.TemplateParser;
import org.aitools.programd.processor.ProcessorException;
import org.aitools.programd.util.NotAnAIMLPatternException;
import org.aitools.programd.util.PatternArbiter;

/**
 * Handles a <code><a href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-condition">condition</a></code> element.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class ConditionProcessor extends AIMLProcessor
{
    /** The wo types of <code>condition</code> that have <code>li</code> children. */
    public static enum NonBlockConditionType
    {
        /** A <code>singlePredicateCondition</code>. */
        SINGLE_PREDICATE,

        /** A <code>multiPredicateCondition</code>. */
        MULTI_PREDICATE
    }

    /** The label (as required by the registration scheme). */
    public static final String label = "condition";

    /**
     * Creates a new ConditionProcessor using the given Core.
     * 
     * @param core the Core object to use
     */
    public ConditionProcessor(Core core)
    {
        super(core);
    }

    /**
     * @throws ProcessorException 
     * @see AIMLProcessor#process(Element, TemplateParser)
     */
    @SuppressWarnings("unchecked")
    @Override
    public String process(Element element, TemplateParser parser) throws ProcessorException
    {
        String name = element.getAttributeValue("name");
        String value = element.getAttributeValue("value");

        try
        {
            /*
             * Process a blockCondition: <condition name="xxx" value="yyy"> ... </condition>
             */
            if (name != null && value != null)
            {
                if (PatternArbiter.matches(parser.getCore().getPredicateMaster().get(name, parser.getUserID(),
                        parser.getBotID()), value, true))
                {
                    return parser.evaluate(element.getContent());
                }
                return "";
            }
    
            /*
             * Process a multiPredicateCondition: <condition> <li name="xxx" value="xxx"> ... </li><li> ... </li>
             * </condition>
             */
            if (name == null && value == null)
            {
                return processMultiPredicateListItems(parser, element);
            }
    
            /*
             * Process a singlePredicateCondition: <condition name="xxx"> <li value="yyy"> ... </li><li> ... </li>
             * </condition>
             */
            if (name != null && value == null)
            {
                return processSinglePredicateListItems(parser, element, name);
            }
        }
        catch (NotAnAIMLPatternException e)
        {
            logger.warn("ConditionProcessor got a non-AIML pattern in a value attribute.", e);
            return "";
        }

        // In other cases, return an empty string.
        return "";
    }

    /**
     * Evaluates all the &lt;li/&gt; elements inside a multi-predicate &lt;condition/&gt;.
     * 
     * @param parser the TemplateParser object responsible for this
     * @param condition the parent condition
     * @return the result of processing this &lt;li/&gt;
     * @throws ProcessorException 
     * @throws NotAnAIMLPatternException
     */
    @SuppressWarnings("unchecked")
    protected String processMultiPredicateListItems(TemplateParser parser, Element condition) throws ProcessorException, NotAnAIMLPatternException
    {
        List<Element> listItems = condition.getChildren();

        int lastLI = listItems.size() - 1;
        for (int index = 0; index <= lastLI; index++)
        {
            Element listItem = listItems.get(index);
            String liValue = listItem.getAttributeValue("value");
            String liName = listItem.getAttributeValue("name");
            
            if (liName != null && liValue != null)
            {
                if (PatternArbiter.matches(this._core.getPredicateMaster().get(liName,
                        parser.getUserID(), parser.getBotID()), liValue, true))
                {
                    return parser.evaluate(listItem.getContent());
                }
            }
            else if (index == lastLI && liName == null && liValue == null)
            {
                return parser.evaluate(listItem.getContent());
            }
        }
        return "";
    }

    /**
     * Evaluates all the &lt;li/&gt; elements inside a single-predicate &lt;condition/&gt;.
     * 
     * @param parser the TemplateParser object responsible for this
     * @param condition the parent condition
     * @param name the name attribute of the &lt;li/&gt; (if applicable)
     * @return the result of processing this &lt;li/&gt;
     * @throws ProcessorException 
     * @throws NotAnAIMLPatternException
     */
    @SuppressWarnings("unchecked")
    protected String processSinglePredicateListItems(TemplateParser parser, Element condition, String name) throws ProcessorException, NotAnAIMLPatternException
    {
        List<Element> listItems = condition.getChildren();

        int lastLI = listItems.size() - 1;
        for (int index = 0; index <= lastLI; index++)
        {
            Element listItem = listItems.get(index);
            String liValue = listItem.getAttributeValue("value");
            String predicateValue = this._core.getPredicateMaster().get(name, parser.getUserID(), parser.getBotID());
            if (liValue != null)
            {
                if (PatternArbiter.matches(predicateValue, liValue, true))
                {
                    return parser.evaluate(listItem.getContent());
                }
            }
            else if (index == lastLI)
            {
                return parser.evaluate(listItem.getContent());
            }
        }
        return "";
    }
}
