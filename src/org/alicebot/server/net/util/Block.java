// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: Block.java,v 1.1.1.1 2001/06/17 19:00:13 noelbu Exp $
// ---------------------------------------------------------------------------

package org.alicebot.server.net.html;
import java.io.IOException;
import java.io.Writer;

/* -------------------------------------------------------------------- */
/** HTML Block Composite.
 * Block of predefined or arbitrary type.
 * Block types are predefined for PRE, BLOCKQUOTE, CENTER, LISTING,
 * PLAINTEXT, XMP, DIV (Left and Right) and SPAN.
 * @see  org.alicebot.server.net.html.Composite
 */
public class Block extends Composite
{
    /* ----------------------------------------------------------------- */
    /** Preformatted text */
    public static final String Pre="PRE";
    /** Quoted Text */
    public static final String Quote="BLOCKQUOTE";
    /** Center the block */
    public static final String Center="CENTER";
    /** Code listing style */
    public static final String Listing="LISTING";
    /** Plain text */
    public static final String Plain="PLAINTEXT";
    /** Old pre format - preserve line breaks */
    public static final String Xmp="XMP";
    /** Basic Division */
    public static final String Div="DIV";
    /** Left align */
    public static final String Left="DIVL";
    /** Right align */
    public static final String Right="DIVR";
    /** Bold */
    public static final String Bold="B";
    /** Italic */
    public static final String Italic="I";
    /** Span */
    public static final String Span="SPAN";

    /* ----------------------------------------------------------------- */
    private String tag;

    /* ----------------------------------------------------------------- */
    /** Construct a block using the passed string as the tag.
     * @param tag The tag to use to open and close the block.
     */
    public Block(String tag)
    {
        this.tag=tag;
        if (tag==Left)
        {
            tag=Div;
            left();
        }
        if (tag==Right)
        {
            tag=Div;
            right();
        }
    }

    /* ----------------------------------------------------------------- */
    /** Construct a block using the passed string as the tag.
     * @param tag The tag to use to open and close the block.
     * @param attributes String of attributes for opening tag.
     */
    public Block(String tag, String attributes)
    {
        super(attributes);
        this.tag=tag;
    }
        
    /* ----------------------------------------------------------------- */
    public void write(Writer out)
         throws IOException
    {
        out.write('<'+tag+attributes()+'>');
        super.write(out);
        out.write("</"+tag+"\n>");
    }
}


