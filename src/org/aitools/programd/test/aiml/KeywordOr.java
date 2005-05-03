package org.aitools.programd.test.aiml;

import java.util.List;

import org.w3c.dom.Element;

/**
 * Represents a group of alternative Keywords.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class KeywordOr extends ItemAll<Keyword, String> implements Keyword
{
    /**
     * Creates a new KeywordOr with the given content.
     * 
     * @param elements the elements that specify the contents of this ItemOnlyOne
     */
    public KeywordOr(List<Element> elements)
    {
        for (Element element : elements)
        {
            this.contents.add(KeywordFactory.create(element));
        }
    }
}
