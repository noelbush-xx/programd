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

package org.alicebot.server.core.targeting;

import org.alicebot.server.core.util.StringTriple;

/**
 *  Represents a category.
 *
 *  @author Noel Bush
 *  @since  4.1.5
 */
public class Category
{
    /** Contains the <code>pattern</code>, <code>that</code> and <code>topic</code>. */
    private StringTriple address;

    /** The <code>template</code> associated with the matched path. */
    private String template;
    
    private static final String EMPTY_STRING = "";
    
    
    /**
     *  Creates a new <code>Category</code> with no values.
     */
    public Category()
    {
        this.address = new StringTriple(EMPTY_STRING, EMPTY_STRING, EMPTY_STRING);
    }


    /**
     *  Creates a new <code>Category</code>.
     *
     *  @param pattern  the category's <code>pattern</code> contents
     *  @param that     the category's <code>that</code> contents
     *  @param topic    the category's <code>topic</code> contents
     *  @param template the category's <code>template</code> contents
     */
    public Category(String pattern, String that, String topic, String template)
    {
        this.address = new StringTriple(pattern, that, topic);
        this.template = template;
    }


    /**
     *  Returns the <code>pattern</code>.
     *
     *  @return the <code>pattern</code>
     */
    public String getPattern()
    {
        return this.address.getFirst();
    }


    /**
     *  Returns the <code>that</code>.
     *
     *  @return the <code>that</code>
     */
    public String getThat()
    {
        return this.address.getSecond();
    }


    /**
     *  Returns the <code>topic</code>.
     *
     *  @return the <code>topic</code>
     */
    public String getTopic()
    {
        return this.address.getThird();
    }


    /**
     *  Returns the <code>template</code>.
     *
     *  @return the <code>template</code>
     */
    public String getTemplate()
    {
        return this.template;
    }


    /**
     *  Sets the <code>pattern</code>.
     *
     *  @param pattern  the <code>pattern</code>
     */
    public void setPattern(String pattern)
    {
        this.address.setFirst(pattern);
    }


    /**
     *  Sets the <code>that</code>.
     *
     *  @param topic    the <code>topic</code>
     */
    public void setThat(String that)
    {
        this.address.setSecond(that);
    }


    /**
     *  Sets the <code>topic</code>.
     *
     *  @param topic    the <code>topic</code>
     */
    public void setTopic(String topic)
    {
        this.address.setThird(topic);
    }


    /**
     *  Sets the <code>template</code>.
     *
     *  @param template the <code>template</code>
     */
    public void setTemplate(String template)
    {
        this.template = template;
    }
}
