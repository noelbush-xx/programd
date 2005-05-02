package org.aitools.programd.test.aiml;

/**
 * Tests whether a given input has an expected length.
 * 
 * @author Albertas Mickensas
 */
public class LengthChecker implements Checker
{
    private int expectedLength = -1;

    /**
     * @param length
     */
    public LengthChecker(int length)
    {
        this.expectedLength = length;
    }

    /**
     * Tests whether the given input has the expected length.
     * 
     * @see org.aitools.programd.test.aiml.Checker#test(java.lang.String)
     */
    public boolean test(String input)
    {
        if (-1 != this.expectedLength)
        {
            if (input.trim().length() == this.expectedLength)
            {
                return true;
            }
            // otherwise...
            return false;
        }
        // otherwise...
        return false;
    }
}
