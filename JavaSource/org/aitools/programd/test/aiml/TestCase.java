package org.aitools.programd.test.aiml;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;

import org.aitools.programd.multiplexor.Multiplexor;
import org.aitools.programd.util.DeveloperError;
import org.aitools.programd.util.StringKit;
import org.aitools.programd.util.XMLKit;

/**
 * A TestCase contains an inputs and a set of checkers that test the response to
 * that inputs.
 * 
 * @author Albertas Mickensas
 * @author <a href="noel@aitools.org">Noel Bush</a>
 */
public class TestCase
{
    /** The string &quot;{@value}&quot;. */
    public static String TAG_TESTCASE = "TestCase";

    /** The string &quot;{@value}&quot;. */
    public static String TAG_DESCRIPTION = "Description";

    /** The string &quot;{@value}&quot;. */
    public static String TAG_INPUT = "Input";

    /** The name of this test case. */
    protected String name;

    /** The inputs that this test case should send. */
    protected String input;

    /** The checker(s) contained in this test case. */
    protected List<Checker> checkers = new ArrayList<Checker>();

    /** The last response received by this test case. */
    protected String lastResponse;

    /**
     * Creates a new TestCase from the given XML element.
     * 
     * @param element the TestCase element
     * @param encoding the encoding of the document from which this element comes
     * @param index a default index to use for automatically naming this case
     */
    public TestCase(Element element, String encoding, int index)
    {
        if (element.hasAttribute("name"))
        {
            try
            {
                this.name = new String(element.getAttribute("name").getBytes(encoding)).intern();
            }
            catch (UnsupportedEncodingException e)
            {
                throw new DeveloperError("Platform does not support encoding \"" + encoding + "\"!", e);
            }
        }
        else
        {
            this.name = "case-" + index;
        }

        List<Element> children = XMLKit.getElementChildrenOf(element);

        int checkersStart = 0;
        // Might be a description here.
        Element child = children.get(0);
        if (child.getTagName().equals(TAG_DESCRIPTION))
        {
            checkersStart = 2;
        }
        else
        {
            checkersStart = 1;
        }

        try
        {
            this.input = new String(children.get(checkersStart - 1).getTextContent().getBytes(encoding)).intern();
        }
        catch (UnsupportedEncodingException e)
        {
            throw new DeveloperError("Platform does not support encoding \"" + encoding + "\"!", e);
        }
        for (Element checker : children.subList(checkersStart, children.size()))
        {
            this.checkers.add(Checker.create(checker, encoding));
        }

    }
    
    /**
     * Constructs a basic TestCase with an input and an expected answer
     * (utility constructor).
     * 
     * @param testInput the input to use
     * @param expectedAnswer the answer to expect
     */
    public TestCase(String testInput, String expectedAnswer)
    {
        this.name = "testcase-" + System.currentTimeMillis();
        this.input = testInput;
        this.addChecker(new AnswerChecker(expectedAnswer));
    }
    
    /**
     * Constructs a basic TestCase with just an input.
     * 
     * @param testInput the input to use
     */
    public TestCase(String testInput)
    {
        this.name = "testcase-" + System.currentTimeMillis();
        this.input = testInput;
    }

    /**
     * A private constructor, for use in persistence.
     */
    private TestCase()
    {
        // Do nothing.
    }
    
    /**
     * @return the name of this test case
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @return the input to be sent by this test case
     */
    public String getInput()
    {
        return this.input;
    }

    /**
     * @return the last response received by this test case
     */
    public String getLastResponse()
    {
        return this.lastResponse;
    }

    /**
     * Runs this test case for the given botid.
     * 
     * @param multiplexor the Multiplexor to use for testing
     * @param userid the userid to use when testing
     * @param botid the bot for whom to run this test case
     * @return whether the test passed
     */
    public boolean run(Multiplexor multiplexor, String userid, String botid)
    {
        this.lastResponse = XMLKit.filterWhitespace(multiplexor.getResponse(this.input, userid, botid));
        return responseIsValid(StringKit.renderAsLines(XMLKit.filterViaHTMLTags(this.lastResponse)));
    }

    /**
     * Response is valid if at least one of the checkers returns a positive
     * result.
     * 
     * @param response the response to check
     * @return whether or not the response is valud
     */
    private boolean responseIsValid(String response)
    {
        boolean result = false;
        for (Checker checker : this.checkers)
        {
            result |= checker.test(response);
        }
        return result;
    }
    
    /**
     * Produces a map of checker names to contents that can
     * be used to describe the test case textually.
     * 
     * @return a map of checker names to contents that can be used to describe the test case textually
     */
    public List<String[]> getDescription()
    {
        List<String[]> result = new ArrayList<String[]>();
        for (Checker checker : this.checkers)
        {
            result.add(new String[] {checker.getTagName(), checker.getContent()});
        }
        return result;
    }
    
    /**
     * Removes all checkers.
     */
    public void removeCheckers()
    {
        this.checkers = new ArrayList<Checker>();
    }
    
    /**
     * Adds a given checker.
     * 
     * @param checker the checker to add
     */
    public void addChecker(Checker checker)
    {
        this.checkers.add(checker);
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj)
    {
        if (!(obj instanceof TestCase))
        {
            return false;
        }
        TestCase other = (TestCase)obj;
        return (other.name.equals(this.name) &&
                other.input.equals(this.input) &&
                other.checkers.equals(this.checkers) &&
                other.lastResponse.equals(this.lastResponse));
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode()
    {
        return (this.name + this.input + this.checkers.toString() + this.lastResponse).hashCode();
    }
}
