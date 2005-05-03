package org.aitools.programd.test.aiml;

/**
 * Performs a specific test on a given input.
 * 
 * @author Albertas Mickensas
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.5
 */
public interface Checker extends Item<Checker, Checker>
{
    /**
     * Determines whether the given input passes the Checker's test.
     * @param input the input to test
     * @return whether the given input passes the Checker's test
     */
    public boolean test(String input);
}
