package org.aitools.programd.test.aiml;

import java.util.List;

import org.w3c.dom.Element;

/**
 * Represents a group of Checkers of which all
 * should be used.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class CheckerAnd extends ItemAll<Checker, Checker> implements Checker
{
    /**
     * Creates a new CheckerAnd with the given content.
     * 
     * @param elements the elements that specify the contents of this CheckerAnd
     */
    public CheckerAnd(List<Element> elements)
    {
        for (Element element : elements)
        {
            this.contents.add(CheckerFactory.create(element));
        }
    }

    /**
     * Returns <code>true</code> only if <i>all</i> of the enclosed
     * checkers return a positive result.  As soon as the first negative
     * result is found, the method returns (so not all checkers may
     * actually be activated).
     * 
     * @see org.aitools.programd.test.aiml.Checker#test(java.lang.String)
     */
    public boolean test(String input)
    {
        this.contents = null;
        for (Checker checker : this)
        {
            if (!checker.test(input))
            {
                return false;
            }
        }
        // otherwise...
        return true;
    }
}
