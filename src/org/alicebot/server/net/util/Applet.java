// ========================================================================
// Copyright (c) 1996 Intelligent Switched Systems, Sydney
// $Id: Applet.java,v 1.1.1.1 2001/06/17 19:00:25 noelbu Exp $
// ========================================================================

package org.alicebot.server.net.html;

import org.alicebot.server.net.http.util.Code;
import java.io.IOException;
import java.io.Writer;
import java.util.Enumeration;
import java.util.Hashtable;

/* ---------------------------------------------------------------- */
/** An Applet Block.
 * <p> Lets you set the class name from the program, and optionally, the
 * size and the codebase.
 *
 * <p> This class uses any attributes set in Element.
 *
 * <p><h4>Usage</h4>
 * <pre>
 * com.mortbay.Page page = new org.alicebot.server.net.html.Page();
 * page.add(new com.mortbay.Applet("com.mortbay.Foo.App"));
 * </pre>
 *
 * @see org.alicebot.server.net.html.Block
 * @version $Id: Applet.java,v 1.1.1.1 2001/06/17 19:00:25 noelbu Exp $
 * @author Matthew Watson
*/
public class Applet extends Block
{
    /* ------------------------------------------------------------ */
    public String codeBase = null;

    /* ------------------------------------------------------------ */
    private boolean debug = Code.debug();
    private Hashtable params = null;
    private Composite paramHolder = new Composite();
    
    /* ------------------------------------------------------------ */
    /** Create an Applet Element.
     * @param className The name of the class to give for the applet
     */
    public Applet(String className)
    {
        super("APPLET");
        add(paramHolder);
        attribute("CODE",className);
    }
    
    /* ------------------------------------------------------------ */
    /** Set the dimensions of the Applet.
     */
    public Applet setDimensions(int height, int width)
    {
        width(width);
        height(height);
        return this;
    }
    
    /* ------------------------------------------------------------ */
    /** Set whether debugging is on in the Applet.
     * <p> This controls whether the org.alicebot.server.net.http.util.Code debug messages
     * will be printed to the java console.
     * @see org.alicebot.server.net.http.util.Code#initParamsFromApplet
     * <p> Defaults to whether debug is turned on in the generating app */
    public Applet setDebug(boolean debug){
        this.debug = debug;
        return this;
    }
    
    /* ------------------------------------------------------------ */
    /** 
     * Set an alternate display for non-java browsers.
     * @param alt The alternate element to display
     * @deprecated use add
     */
    public Applet setAlternate(Element alt)
    {
        add(alt);
        return this;
    }
    
    /* ------------------------------------------------------------ */
    /** Set an alternate display for non-java browsers.
     * @param alt The alternate element to display 
     * @deprecated use add
     */
    public Applet setAlternate(String alt)
    {
        add(alt);
        return this;
    }
    
    /* ------------------------------------------------------------ */
    /** Set the codebase */
    public Applet codeBase(String cb)
    {
        codeBase = cb;
        return this;
    }
    
    /* ------------------------------------------------------------ */
    public Applet setParam(String name, String value)
    {
        if (params == null)
            params = new Hashtable(10);
        params.put(name, value);
        return this;
    }
    
    /* ------------------------------------------------------------ */
    /** Write out the HTML */
    public void write(Writer out)
         throws IOException
    {
        if (codeBase != null)
            attribute("CODEBASE",codeBase);
        
        if (debug)
            paramHolder.add("<PARAM NAME=DEBUG VALUE=yes>");
        if (params != null)
            for (Enumeration enum = params.keys(); enum.hasMoreElements();)
            {
                String key = enum.nextElement().toString();
                paramHolder.add("<PARAM NAME=\"" + key + "\" VALUE=\"" +
                                params.get(key).toString() + "\">");
            }
        super.write(out);
    }
};





