// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: Form.java,v 1.1.1.1 2001/06/17 19:00:15 noelbu Exp $
// ---------------------------------------------------------------------------

package org.alicebot.server.net.html;
import java.io.IOException;
import java.io.Writer;
import org.alicebot.server.net.http.HttpFields;

/* -------------------------------------------------------------------- */
/** HTML Form.
 * The specialized Block can contain HTML Form elements as well as
 * any other HTML elements
 */
public class Form extends Block
{
    public static final String encodingWWWURL = HttpFields.__WwwFormUrlEncode;
    public static final String encodingMultipartForm = "multipart/form-data";
    private String method="POST";
    
    /* ----------------------------------------------------------------- */
    /** Constructor.
     */
    public Form()
    {
        super("FORM");
    }

    /* ----------------------------------------------------------------- */
    /** Constructor.
     * @param submitURL The URL to submit the form to
     */
    public Form(String submitURL)
    {
        super("FORM");
        action(submitURL);
    }

    /* ----------------------------------------------------------------- */
    /** Constructor.
     * @param submitURL The URL to submit the form to
     */
    public Form action(String submitURL)
    {
        attribute("ACTION",submitURL);
        return this;
    }
    
    /* ----------------------------------------------------------------- */
    /** Set the form target.
     */
    public Form target(String t)
    {
        attribute("TARGET",t);
        return this;
    }
    
    /* ----------------------------------------------------------------- */
    /** Set the form method.
     */
    public Form method(String m)
    {
        method=m;
        return this;
    }
    
    /* ------------------------------------------------------------ */
    /** Set the form encoding type.
     */
    public Form encoding(String encoding){
        attribute("ENCTYPE", encoding);
        return this;
    }
    /* ----------------------------------------------------------------- */
    public void write(Writer out)
         throws IOException
    {
        attribute("METHOD",method);
        super.write(out);
    }
}




