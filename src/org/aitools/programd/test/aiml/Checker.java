package org.aitools.programd.test.aiml;

/**
 * Performs a specific test on a given input.
 * 
 * @author Albertas Mickensas
 */
public interface Checker
{
    /**
     * Performs the Checker's defined test on the given input.
     * 
     * @param input the input to test
     * @return whether or not the input passes the test
     */
    public abstract boolean test(String input);
}
