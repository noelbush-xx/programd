package org.aitools.programd.test.aiml;

import java.util.List;

import org.w3c.dom.Element;

/**
 * Represents a group of Inputs of which all
 * should be used.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class InputAnd extends ItemAll<Input, String> implements Input
{
    /**
     * Creates a new InputAnd with the given content.
     * 
     * @param elements the elements that specify the contents of this InputAnd
     */
    public InputAnd(List<Element> elements)
    {
        for (Element element : elements)
        {
            this.contents.add(InputFactory.create(element));
        }
    }
}
