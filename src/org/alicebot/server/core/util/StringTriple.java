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

/**
 *  Contains three Strings.
 *
 *  @author Noel Bush
 *  @since  4.1.5
 */
public class StringTriple
{
    private String first;

    private String second;

    private String third;


    public StringTriple(String first, String second, String third)
    {
        this.first = first;
        this.second = second;
        this.third = third;
    }


    public String getFirst()
    {
        return this.first;
    }


    public String getSecond()
    {
        return this.second;
    }


    public String getThird()
    {
        return this.third;
    }


    public void setFirst(String text)
    {
        this.first = text;
    }


    public void setSecond(String text)
    {
        this.second = text;
    }


    public void setThird(String text)
    {
        this.third = text;
    }


    public boolean equals(Object object)
    {
        StringTriple tuple;
        try
        {
            tuple = (StringTriple)object;
        }
        catch (ClassCastException e)
        {
            return false;
        }
        return  (tuple.getFirst().equals(this.first)) &
                (tuple.getSecond().equals(this.second)) &
                (tuple.getThird().equals(this.third));
    }
}
