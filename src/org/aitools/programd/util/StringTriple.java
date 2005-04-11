/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.util;

/**
 * Contains three Strings.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.1.5
 */
public class StringTriple
{
    private String first;

    private String second;

    private String third;

    /**
     * Constructs a StringTriple from the given three strings.
     * 
     * @param firstString the first string
     * @param secondString the second string
     * @param thirdString the third string
     */
    public StringTriple(String firstString, String secondString, String thirdString)
    {
        this.first = firstString;
        this.second = secondString;
        this.third = thirdString;
    }

    /**
     * @return the first string
     */
    public String getFirst()
    {
        return this.first;
    }

    /**
     * @return the second string
     */
    public String getSecond()
    {
        return this.second;
    }

    /**
     * @return the third string
     */
    public String getThird()
    {
        return this.third;
    }

    /**
     * Sets the text of the first string.
     * 
     * @param text the text to set
     */
    public void setFirst(String text)
    {
        this.first = text;
    }

    /**
     * Sets the text of the second string.
     * 
     * @param text the text to set
     */
    public void setSecond(String text)
    {
        this.second = text;
    }

    /**
     * Sets the text of the third string.
     * 
     * @param text the text to set
     */
    public void setThird(String text)
    {
        this.third = text;
    }

    /**
     * @see java.lang.Object#equals(java.lang.Object)
     */
    public boolean equals(Object object)
    {
        StringTriple tuple;
        try
        {
            tuple = (StringTriple) object;
        }
        catch (ClassCastException e)
        {
            return false;
        }
        return (tuple.getFirst().equals(this.first)) & (tuple.getSecond().equals(this.second)) & (tuple.getThird().equals(this.third));
    }
}