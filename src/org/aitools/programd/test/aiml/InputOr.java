package org.aitools.programd.test.aiml;

import java.util.List;

import org.w3c.dom.Element;

/**
 * Represents a group of alternative Inputs.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class InputOr extends ItemOnlyOne<Input, String> implements Input
{
    /**
     * Creates a new InputOr with the given content.
     * 
     * @param elements the elements that specify the contents of this InputOr
     */
    public InputOr(List<Element> elements)
    {
        for (Element element : elements)
        {
            this.contents.add(InputFactory.create(element));
        }
    }
}
