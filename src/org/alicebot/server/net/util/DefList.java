// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: DefList.java,v 1.1.1.1 2001/06/17 19:00:14 noelbu Exp $
// ---------------------------------------------------------------------------

package org.alicebot.server.net.html;
import java.io.IOException;
import java.io.Writer;
import java.util.Vector;

// =======================================================================
public class DefList extends Element
{

    // ------------------------------------------------------------
    public DefList()
    {
        terms = new Vector();
        defs = new Vector();
    }

    // ------------------------------------------------------------
    public void add(Element term, Element def)
    {
        terms.addElement(term);
        defs.addElement(def);
    }

    // ------------------------------------------------------------
    public void write(Writer out)
         throws IOException
    {
        out.write("<DL"+attributes()+">");

        if (terms.size() != defs.size())
            throw new Error("mismatched Vector sizes");

        for (int i=0; i <terms.size() ; i++)
        {
            out.write("<DT>");
            ((Element)terms.elementAt(i)).write(out);
            out.write("<DD>");
            ((Element)defs.elementAt(i)).write(out);
        }

        out.write("</DL>");
    }

    // ------------------------------------------------------------
    private Vector terms;
    private Vector defs;
}

