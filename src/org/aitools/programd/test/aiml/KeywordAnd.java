package org.aitools.programd.test.aiml;

import java.util.List;

import org.w3c.dom.Element;

/**
 * Represents a group of Keywords of which all
 * should be used.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class KeywordAnd extends ItemAll<Keyword, String> implements Keyword
{
    /**
     * Creates a new KeywordAnd with the given content.
     * 
     * @param elements the elements that specify the contents of this ItemAll
     */
    public KeywordAnd(List<Element> elements)
    {
        for (Element element : elements)
        {
            this.contents.add(KeywordFactory.create(element));
        }
    }
}
