package org.aitools.programd.test.aiml;

import java.util.List;

import org.w3c.dom.Element;

/**
 * Represents a group of alternative Checkers.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class CheckerOr extends ItemOnlyOne<Checker, Checker> implements Checker
{
    /**
     * Creates a new CheckerOr with the given content.
     * 
     * @param elements the elements that specify the contents of this ItemOnlyOne
     */
    public CheckerOr(List<Element> elements)
    {
        for (Element element : elements)
        {
            this.contents.add(CheckerFactory.create(element));
        }
    }

    /**
     * Returns <code>true</code> if <i>any</i> of the checkers returns a positive value.
     * As soon as the first <code>true</code> result is found, the methods returns
     * (so not all checkers may actually be activated).
     * 
     * @see org.aitools.programd.test.aiml.Checker#test(java.lang.String)
     */
    public boolean test(String input)
    {
        for (Checker checker : this)
        {
            if (checker.test(input))
            {
                return true;
            }
        }
        // otherwise...
        return false;
    }
}
