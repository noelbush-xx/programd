// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: Input.java,v 1.1.1.1 2001/06/17 19:00:19 noelbu Exp $
// ---------------------------------------------------------------------------

package org.alicebot.server.net.html;

/* -------------------------------------------------------------------- */
/** HTML Form Input Tag.
 * <p>
 * @see Tag
 * @see Form
 * @version $Id: Input.java,v 1.1.1.1 2001/06/17 19:00:19 noelbu Exp $
 * @author Greg Wilkins
 */
public class Input extends Tag
{
    /* ----------------------------------------------------------------- */
    /** Input types */
    public final static String Text="TEXT";
    public final static String Password="PASSWORD";
    public final static String Checkbox="CHECKBOX";
    public final static String Radio="RADIO";
    public final static String Submit="SUBMIT";
    public final static String Reset="RESET";
    public final static String Hidden="HIDDEN";
    public final static String File="FILE";
    public final static String Image="IMAGE";

    /* ----------------------------------------------------------------- */
    public Input(String type,String name)
    {
        super("INPUT");
        attribute("TYPE",type);
        attribute("NAME",name);
    }

    /* ----------------------------------------------------------------- */
    public Input(String type,String name, String value)
    {
        this(type,name);
        attribute("VALUE",value);
    }

    /* ----------------------------------------------------------------- */
    public Input(Image image,String name, String value)
    {
        super("INPUT");
        attribute("TYPE","IMAGE");
        attribute("NAME",name);
        if (value!=null)
            attribute("VALUE",value);
        attribute(image.attributes());
    }
    
    /* ----------------------------------------------------------------- */
    public Input(Image image,String name)
    {
        super("INPUT");
        attribute("TYPE","IMAGE");
        attribute("NAME",name);
        attribute(image.attributes());
    }

    /* ----------------------------------------------------------------- */
    public Input check()
    {
        attribute("CHECKED");
        return this;
    }

    /* ----------------------------------------------------------------- */
    public Input setSize(int size)
    {
        size(size);
        return this;
    }

    /* ----------------------------------------------------------------- */
    public Input setMaxSize(int size)
    {
        attribute("MAXLENGTH",size);
        return this;
    }

    /* ----------------------------------------------------------------- */
    public Input fixed()
    {
        setMaxSize(size());
        return this;
    }
}
