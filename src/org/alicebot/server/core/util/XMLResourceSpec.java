/*
    Alicebot Program D
    Copyright (C) 1995-2001, A.L.I.C.E. AI Foundation
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
    USA.
*/

package org.alicebot.server.core.util;

import org.alicebot.server.core.Globals;


/**
 *  This is a simple data type for describing some characteristics
 *  of an XML resource that will be written using XMLWriter.
 *
 *  @author Noel Bush
 */
public class XMLResourceSpec implements Cloneable
{
    /** The description of the resource. */
    public String description;

    /** The path for the XML resource. */
    public String path;

    /** The name of the root element for the XML resource. */
    public String root;

    /** The path to the stylesheet for the XML resource. */
    public String stylesheet;

    /** The encoding for the XML resource. */
    public String encoding;

    /** The DTD for the XML resource. */
    public String dtd;

    /**
     *  A &quot;back-link&quot; for the XML resource: that is,
     *  a URI for another resource that should be presented as a
     *  &quot;previous&quot; link.
     */
    public String backlink;

    /** The starting date time for an XML resource. */
    public String starttime;

    /** Whether the resource should be rolled over when it hits a given maximum. */
    public boolean rolloverAtMax = false;

    /** Whether the resource should always be rolled over at restart. */
    public boolean rolloverAtRestart = false;

    /** Base URL for resources (stylesheets, etc.). */
    private static final String RESOURCE_BASE =
    	Globals.getProperty("programd.logging.xml.resource-base", "../resources/");

    /** A DTD for HTML entities. */
    public static final String HTML_ENTITIES_DTD = "<!DOCTYPE ALLOW_HTML_ENTITIES [" +
                                                   " <!ENTITY % HTMLlat1 PUBLIC" +
                                                   " \"-//W3C//ENTITIES Latin1//EN//HTML\"" +
                                                   "   \"" + RESOURCE_BASE + "DTD/xhtml-lat1.ent\">" +
                                                   " %HTMLlat1;" +
                                                   " <!ENTITY % HTMLsymbol PUBLIC" +
                                                   "   \"-//W3C//ENTITIES Symbols//EN//HTML\"" +
                                                   "   \"" + RESOURCE_BASE + "DTD/xhtml-symbol.ent\">" +
                                                   " %HTMLsymbol;" +
                                                   " <!ENTITY % HTMLspecial PUBLIC" +
                                                   "   \"-//W3C//ENTITIES Special//EN//HTML\"" +
                                                   "   \"" + RESOURCE_BASE + "DTD/xhtml-special.ent\">" +
                                                   " %HTMLspecial;" +
                                                   " ]>";

    public XMLResourceSpec()
    {
    }
    
    
    public Object clone()
    {
        XMLResourceSpec myClone = new XMLResourceSpec();
        myClone.description = this.description == null ? null : new String(this.description);
        myClone.path = this.path == null ? null : new String(this.path);
        myClone.root = this.root == null ? null : new String(this.root);
        myClone.stylesheet = this.stylesheet == null ? null : new String(this.stylesheet);
        myClone.encoding = this.encoding == null ? null : new String(this.encoding);
        myClone.dtd = this.dtd == null ? null : new String(this.dtd);
        myClone.backlink = this.backlink == null ? null : new String(this.backlink);
        myClone.starttime = this.starttime == null ? null : new String(this.starttime);
        myClone.rolloverAtMax = this.rolloverAtMax;
        myClone.rolloverAtRestart = this.rolloverAtRestart;
        return myClone;
    }
}