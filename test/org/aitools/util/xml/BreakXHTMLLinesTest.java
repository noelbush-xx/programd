/*
 * aitools utilities
 * Copyright (C) 2006 Noel Bush
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.aitools.util.xml;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

/**
 * Tests {@link XHTML#breakLines(String)}.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 *
 */
public class BreakXHTMLLinesTest
{
    private static final String[] THREE_LINE_TEST_ARRAY = new String[] {"line 1", "line 2", "line 3"};
    
    /**
     * A generic test method used by all the tests here.
     * 
     * @param input the raw input to test the break lines function against
     */
    private void testBreakLines(String input)
    {
        String[] lines = XHTML.breakLines(input);
        assertEquals(Arrays.toString(lines), THREE_LINE_TEST_ARRAY, lines);
    }
    
    /** */ @Test
    public void testBreakLinesAtUnqualifiedBR_000()
    {
        testBreakLines("line 1<br/>line 2<br/>line 3");
    }

    /** */ @Test
    public void testBreakLinesAtUnqualifiedBR_001()
    {
        testBreakLines("line 1<br/>line 2<br/>line 3<br/>");
    }

    /** */ @Test
    public void testBreakLinesAtUnqualifiedBR_002()
    {
        testBreakLines("<br/>line 1<br/>line 2<br/>line 3");
    }

    /** */ @Test
    public void testBreakLinesAtUnqualifiedBR_003()
    {
        testBreakLines("<br />line 1<br />line 2<br />line 3<br />");
    }

    /** */ @Test
    public void testBreakLinesAtUnqualifiedBR_004()
    {
        testBreakLines("line 1<br />line 2<br />line 3");
    }

    /** */ @Test
    public void testBreakLinesAtUnqualifiedBR_005()
    {
        testBreakLines("line 1<br />line 2<br />line 3<br />");
    }

    /** */ @Test
    public void testBreakLinesAtUnqualifiedBR_006()
    {
        testBreakLines("<br />line 1<br />line 2<br />line 3");
    }

    /** */ @Test
    public void testBreakLinesAtUnqualifiedBR_007()
    {
        testBreakLines("<br />line 1<br />line 2<br />line 3<br />");
    }

    /** */ @Test
    public void testBreakLinesAtUnqualifiedP()
    {
        testBreakLines("<p>line 1</p><p>line 2</p><p>line 3</p>");
    }
    
    /** */ @Test
    public void testBreakLinesInUnqualifiedPre()
    {
        testBreakLines("<pre>line 1\nline 2\nline 3\n</pre>");
    }

    /** */ @Test
    public void testBreakLinesAtNamespacedBR_000()
    {
        testBreakLines("line 1<br xmlns=\"http://www.w3.org/1999/xhtml\"/>line 2<br xmlns=\"http://www.w3.org/1999/xhtml\"/>line 3");
    }

    /** */ @Test
    public void testBreakLinesAtNamespacedBR_001()
    {
        testBreakLines("line 1<br xmlns=\"http://www.w3.org/1999/xhtml\"/>line 2<br xmlns=\"http://www.w3.org/1999/xhtml\"/>line 3<br xmlns=\"http://www.w3.org/1999/xhtml\"/>");
    }

    /** */ @Test
    public void testBreakLinesAtNamespacedBR_002()
    {
        testBreakLines("<br xmlns=\"http://www.w3.org/1999/xhtml\"/>line 1<br xmlns=\"http://www.w3.org/1999/xhtml\"/>line 2<br xmlns=\"http://www.w3.org/1999/xhtml\"/>line 3");
    }

    /** */ @Test
    public void testBreakLinesAtNamespacedBR_003()
    {
        testBreakLines("<br xmlns=\"http://www.w3.org/1999/xhtml\" />line 1<br xmlns=\"http://www.w3.org/1999/xhtml\" />line 2<br xmlns=\"http://www.w3.org/1999/xhtml\" />line 3<br xmlns=\"http://www.w3.org/1999/xhtml\" />");
    }

    /** */ @Test
    public void testBreakLinesAtNamespacedBR_004()
    {
        testBreakLines("line 1<br xmlns=\"http://www.w3.org/1999/xhtml\" />line 2<br xmlns=\"http://www.w3.org/1999/xhtml\" />line 3");
    }

    /** */ @Test
    public void testBreakLinesAtNamespacedBR_005()
    {
        testBreakLines("line 1<br xmlns=\"http://www.w3.org/1999/xhtml\" />line 2<br xmlns=\"http://www.w3.org/1999/xhtml\" />line 3<br xmlns=\"http://www.w3.org/1999/xhtml\" />");
    }

    /** */ @Test
    public void testBreakLinesAtNamespacedBR_006()
    {
        testBreakLines("<br xmlns=\"http://www.w3.org/1999/xhtml\" />line 1<br xmlns=\"http://www.w3.org/1999/xhtml\" />line 2<br xmlns=\"http://www.w3.org/1999/xhtml\" />line 3");
    }

    /** */ @Test
    public void testBreakLinesAtNamespacedBR_007()
    {
        testBreakLines("<br xmlns=\"http://www.w3.org/1999/xhtml\" />line 1<br xmlns=\"http://www.w3.org/1999/xhtml\" />line 2<br xmlns=\"http://www.w3.org/1999/xhtml\" />line 3<br xmlns=\"http://www.w3.org/1999/xhtml\" />");
    }

    /** */ @Test
    public void testBreakLinesAtNamespacedP()
    {
        testBreakLines("<p xmlns=\"http://www.w3.org/1999/xhtml\">line 1</p><p xmlns=\"http://www.w3.org/1999/xhtml\">line 2</p><p xmlns=\"http://www.w3.org/1999/xhtml\">line 3</p>");
    }
    
    /** */ @Test
    public void testBreakLinesInNamespacedPre()
    {
        testBreakLines("<pre xmlns=\"http://www.w3.org/1999/xhtml\">line 1\nline 2\nline 3\n</pre>");
    }

    /** */ @Test
    public void testBreakLinesAtQualifiedBR_000()
    {
        testBreakLines("line 1<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\"/>line 2<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\"/>line 3");
    }

    /** */ @Test
    public void testBreakLinesAtQualifiedBR_001()
    {
        testBreakLines("line 1<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\"/>line 2<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\"/>line 3<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\"/>");
    }

    /** */ @Test
    public void testBreakLinesAtQualifiedBR_002()
    {
        testBreakLines("<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\"/>line 1<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\"/>line 2<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\"/>line 3");
    }

    /** */ @Test
    public void testBreakLinesAtQualifiedBR_003()
    {
        testBreakLines("<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\" />line 1<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\" />line 2<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\" />line 3<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\" />");
    }

    /** */ @Test
    public void testBreakLinesAtQualifiedBR_004()
    {
        testBreakLines("line 1<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\" />line 2<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\" />line 3");
    }

    /** */ @Test
    public void testBreakLinesAtQualifiedBR_005()
    {
        testBreakLines("line 1<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\" />line 2<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\" />line 3<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\" />");
    }

    /** */ @Test
    public void testBreakLinesAtQualifiedBR_006()
    {
        testBreakLines("<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\" />line 1<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\" />line 2<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\" />line 3");
    }

    /** */ @Test
    public void testBreakLinesAtQualifiedBR_007()
    {
        testBreakLines("<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\" />line 1<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\" />line 2<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\" />line 3<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\" />");
    }

    /** */ @Test
    public void testBreakLinesAtQualifiedP()
    {
        testBreakLines("<html:p xmlns:html=\"http://www.w3.org/1999/xhtml\">line 1</html:p><html:p xmlns:html=\"http://www.w3.org/1999/xhtml\">line 2</html:p><html:p xmlns:html=\"http://www.w3.org/1999/xhtml\">line 3</html:p>");
    }
    
    /** */ @Test
    public void testBreakLinesInQualifiedPre()
    {
        testBreakLines("<html:pre xmlns:html=\"http://www.w3.org/1999/xhtml\">line 1\nline 2\nline 3\n</html:pre>");
    }

    /** */ @Test
    public void testBreakLinesAtQualifiedBRWithLinefeeds_000()
    {
        testBreakLines("line\n1\r\n<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\"/>line\n2\r\n<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\"/>line\n3");
    }

    /** */ @Test
    public void testBreakLinesAtQualifiedBRWithLinefeeds_001()
    {
        testBreakLines("line\n1\r\n<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\"/>line\n2\r\n<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\"/>line\n3\r\n<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\"/>");
    }

    /** */ @Test
    public void testBreakLinesAtQualifiedBRWithLinefeeds_002()
    {
        testBreakLines("<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\"/>line\n1\r\n<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\"/>line\n2\r\n<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\"/>line\n3");
    }

    /** */ @Test
    public void testBreakLinesAtQualifiedBRWithLinefeeds_003()
    {
        testBreakLines("<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\" />line\n1\r\n<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\" />line\n2\r\n<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\" />line\n3\r\n<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\" />");
    }

    /** */ @Test
    public void testBreakLinesAtQualifiedBRWithLinefeeds_004()
    {
        testBreakLines("line\n1\r\n<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\" />line\n2\r\n<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\" />line\n3");
    }

    /** */ @Test
    public void testBreakLinesAtQualifiedBRWithLinefeeds_005()
    {
        testBreakLines("line\n1\r\n<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\" />line\n2\r\n<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\" />line\n3\r\n<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\" />");
    }

    /** */ @Test
    public void testBreakLinesAtQualifiedBRWithLinefeeds_006()
    {
        testBreakLines("<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\" />line\n1\r\n<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\" />line\n2\r\n<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\" />line\n3");
    }

    /** */ @Test
    public void testBreakLinesAtQualifiedBRWithLinefeeds_007()
    {
        testBreakLines("<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\" />line\n1\r\n<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\" />line\n2\r\n<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\" />line\n3\r\n<html:br xmlns:html=\"http://www.w3.org/1999/xhtml\" />");
    }

    /** */ @Test
    public void testBreakLinesAtQualifiedPWithLinefeeds()
    {
        testBreakLines("<html:p xmlns:html=\"http://www.w3.org/1999/xhtml\">\r\nline\n1\r\n</html:p><html:p xmlns:html=\"http://www.w3.org/1999/xhtml\">\r\nline\n2\r\n</html:p><html:p xmlns:html=\"http://www.w3.org/1999/xhtml\">\r\nline\n3\r\n</html:p>");
    }
    
    /** */ @Test
    public void testBreakLinesInQualifiedPreWithLinefeeds()
    {
        testBreakLines("<html:pre xmlns:html=\"http://www.w3.org/1999/xhtml\">\r\nline 1\nline 2\nline 3\n\r\n</html:pre>");
    }
}
