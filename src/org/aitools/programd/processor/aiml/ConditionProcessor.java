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

import org.jdom.CDATA;
import org.jdom.Content;
import org.jdom.Element;
import org.jdom.Text;

import org.aitools.programd.Core;
import org.aitools.programd.parser.TemplateParser;
import org.aitools.programd.processor.ProcessorException;
import org.aitools.programd.util.NotAnAIMLPatternException;
import org.aitools.programd.util.PatternArbiter;
import org.aitools.util.runtime.UserError;
import org.apache.log4j.Logger;

/**
 * Handles a <code><a href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-condition">condition</a></code> element.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class ConditionProcessor extends AIMLProcessor
{
    /** Different types of condition. */
    public static enum ListItemType
    {
        /** A nameValueListItem in a &lt;condition/&gt;. */
        NAME_VALUE,

        /** A defaultListItem in a &lt;condition/&gt;. */
        DEFAULT,

        /** A valueOnlyListItem in a &lt;condition/&gt;. */
        VALUE_ONLY
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

        /*
         * Process a multiPredicateCondition: <condition> <li name="xxx" value="xxx"> ... </li><li> ... </li>
         * </condition>
         */
        if ("".equals(name) && "".equals(value))
        {
            if (aimlLogger.isDebugEnabled())
            {
                aimlLogger.debug("Processing multiPredicateCondition.");
            }
            return processListItem(parser, element.getChildren(), ListItemType.NAME_VALUE, name);
        }

        /*
         * Process a blockCondition: <condition name="xxx" value="yyy"> ... </condition>
         */
        if (!"".equals(name) && !"".equals(value))
        {
            if (aimlLogger.isDebugEnabled())
            {
                aimlLogger.debug("Processing blockCondition.");
            }
            try
            {
                if (PatternArbiter.matches(parser.getCore().getPredicateMaster().get(name, parser.getUserID(),
                        parser.getBotID()), value, true))
                {
                    return processListItem(parser, element.getChildren(), ListItemType.DEFAULT, "");
                }
            }
            catch (NotAnAIMLPatternException e)
            {
                logger.warn("ConditionProcessor got a non-AIML pattern in a value attribute.", e);
                return "";
            }
            return "";
        }

        /*
         * Process a singlePredicateCondition: <condition name="xxx"> <li value="yyy"> ... </li><li> ... </li>
         * </condition>
         */
        if (!"".equals(name) && "".equals(value))
        {
            if (aimlLogger.isDebugEnabled())
            {
                aimlLogger.debug("Processing singlePredicateCondition.");
            }
            return processListItem(parser, element.getChildren(), ListItemType.VALUE_ONLY, name);
        }

        // In other cases, return an empty string.
        return "";
    }

    /**
     * Evaluates an &lt;li/&gt; element inside a &lt;condition/&gt;.
     * 
     * @param parser the TemplateParser object responsible for this
     * @param list the XML trie
     * @param type one of "name"_"value", DEFAULT, "value"_ONLY
     * @param name the name attribute of the &lt;li/&gt; (if applicable)
     * @return the result of processing this &lt;li/&gt;
     * @throws ProcessorException 
     */
    public String processListItem(TemplateParser parser, List<Content> list, ListItemType type, String name) throws ProcessorException
    {
        StringBuilder response = new StringBuilder();

        String predicateValue = null;

        /*
         * For <code> valueOnlyListItem </code> s, look at the parent &lt;condition/&gt; to get the predicate <code>
         * name </code> .
         */
        if (type == ListItemType.VALUE_ONLY)
        {
            predicateValue = this._core.getPredicateMaster().get(name, parser.getUserID(), parser.getBotID());
        }

        // Navigate through this entire level.
        for (Content content : list)
        {
            if (content instanceof Text || content instanceof CDATA)
            {
                response.append(content.getValue());
            }
            else if (content instanceof Element)
            {
                processElement(parser, (Element)content, type, predicateValue, response);
            }
        }
        return response.toString();
    }
    
    private void processElement(TemplateParser parser, Element element, ListItemType type, String predicateValue, StringBuilder response) throws ProcessorException
    {
        if (element.getChildren().size() == 0)
        {
            try
            {
                response.append(parser.evaluate(element));
            }
            catch (ProcessorException e)
            {
                throw new UserError(e.getExplanatoryMessage(), e);
            }
        }
        else
        {
            /*
             * Now decide what to do based on the listItemType, which indicates what to expect from the
             * parent &lt;condition/&gt;.
             */
            switch (type)
            {
                // Evaluate listitems with both name and value attributes.
                case NAME_VALUE:
                    processNameValueLI(parser, element, response);
                    break;
                    
                case DEFAULT:
                    processDefaultLI(parser, element, response);
                    break;
                    
                case VALUE_ONLY:
                    processValueOnlyLI(parser, element, predicateValue, response);
                    break;
                    
                default:
                    break;
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private void processNameValueLI(TemplateParser parser, Element element, StringBuilder response) throws ProcessorException
    {
        /*
         * Look for tokens in the XML attributes for name and value. If none are present,
         * this is an unqualified &lt;li/&gt; (defaultListItem) and gets evaluated.
         * (Strange.) Processing will continue even after this case, so the defaultListItem
         * may be anywhere under &lt;condition/&gt;, not necessarily at the end. This is a
         * violation of strict AIML 1.0.1.
         */
        if ((element.getAttribute("name") == null) && (element.getAttribute("value") == null))
        {
            response.append(parser.evaluate(element.getChildren()));
            return;
        }
        
        // Ignore if there is not a name and a value.
        if ((element.getAttribute("name") == null) || (element.getAttribute("value") == null))
        {
            return;
        }
        
        // Recover the values of the name and value
        // attributes.
        String liname = element.getAttributeValue("name");
        String livalue = element.getAttributeValue("value");
        
        /*
         * If the value of the predicate matches the value in the value attribute, process
         * the response, otherwise skip.
         */
        try
        {
            if (PatternArbiter.matches(this._core.getPredicateMaster().get(liname,
                    parser.getUserID(), parser.getBotID()), livalue, true))
            {
                response.append(parser.evaluate(element.getChildren()));
            }
        }
        catch (NotAnAIMLPatternException e)
        {
            Logger.getLogger("programd").warn(
                    "ConditionProcessor got a non-AIML pattern in a value attribute.", e);
        }
    }
    
    private void processDefaultLI(TemplateParser parser, Element element, StringBuilder response) throws ProcessorException
    {
        response.append(parser.evaluate(element));
    }
    
    @SuppressWarnings("unchecked")
    private void processValueOnlyLI(TemplateParser parser, Element element, String predicateValue, StringBuilder response) throws ProcessorException
    {
        // If there is a value attribute, get it.
        if (element.getAttribute("value") != null)
        {
            String livalue = element.getAttributeValue("value");
            /*
             * If the value of the predicate matches the value in the value attribute,
             * process the response, otherwise skip.
             */
            try
            {
                if (PatternArbiter.matches(predicateValue, livalue, true))
                {
                    response.append(parser.evaluate(element.getChildren()));
                }
            }
            catch (NotAnAIMLPatternException e)
            {
                Logger.getLogger("programd").warn(
                        "ConditionProcessor got a non-AIML pattern in a value attribute.",
                        e);
            }
        }
        /*
         * When there is no value attribute, we actually got the wrong li type, but process
         * as a defaultListItem anyway (probably a bad idea).
         */
        else
        {
            response.append(parser.evaluate(element.getChildren()));
        }
    }
}
