/*
 * This code is copyright (c) 2005-2006, botmachine inc.
 * All rights reserved.  Use of this code, in whole or in part,
 * without express written permission from botmachine inc. is
 * a violation of law.
 */

package org.aitools.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Provides some simple operations on lists.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class Lists
{
    /**
     * Produces a new list that contains a single given item.
     * @param item 
     * @param <T> 
     * @return a new list that contains just <code>item</code>
     */
    public static <T> List<T> singleItem(T item)
    {
        List<T> result = new ArrayList<T>(1);
        result.add(item);
        return result;
    }
    
    /**
     * Produces a new list that combines the elements of two lists.
     * 
     * @param <T>
     * @param list1
     * @param list2
     * @return a new list that combines the elements of <code>list1</code> and <code>list2</code>
     */
    public static <T> List<T> join(List<T> list1, List<T> list2)
    {
        List<T> result = new ArrayList<T>(list1);
        result.addAll(list2);
        return result;
    }
    
    /**
     * Produces a new list that combines a single element and the elements of a list.
     * 
     * @param <T>
     * @param item
     * @param list
     * @return a new list that combines <code>item</code> and <code>list</code>
     */
    public static <T> List<T> join(T item, List<T> list)
    {
        List<T> result = new ArrayList<T>();
        result.add(item);
        result.addAll(list);
        return result;
    }

    /**
     * Produces a list of lists encompassing all permutations of a given list.
     * 
     * @param <T>
     * @param list
     * @return a list of lists encompassing all permutations of <code>list</code>
     */
    public static <T> List<List<T>> getPermutations(List<T> list)
    {
        int listSize = list.size();
        if (listSize > 0)
        {
            List<List<T>> results = new ArrayList<List<T>>(factorial(list.size()));
            
            results.add(list);
            if (listSize > 1)
            {
                for (T item : list)
                {
                    List<T> sublist = new ArrayList<T>(list);
                    sublist.remove(list.indexOf(item));
                    for (List<T> permutation : getPermutations(sublist))
                    {
                        List<T> newlist = join(item, permutation);
                        if (!results.contains(newlist))
                        {
                            results.add(newlist);
                        }
                    }
                }
            }
            return results;
        }
        return new ArrayList<List<T>>();
    }

    /**
     * Returns the factorial of a given number.
     * 
     * @param number
     * @return the factorial of <code>number</code>
     */
    public static int factorial(int number)
    {
        if (number > 0)
        {
            return number * factorial(number - 1);
        }
        return 1;
    }
    
    /**
     * Produces a regular expression that will match any member of a given list.
     * 
     * @param list
     * @param quote whether or not the items in the list should be quoted (do not quote if items are themselves regexps)
     * @return a regular expression that will match any member of a given list
     */
    public static String asRegexAlternatives(List<String> list, boolean quote)
    {
        int listSize = list.size();
        if (listSize == 0)
        {
            return "";
        }
        if (listSize == 1)
        {
            String string = list.get(0);
            if (quote && !string.startsWith("\\Q") && !string.endsWith("\\E"))
            {
                return Pattern.quote(string);
            }
            return string;
        }
        StringBuilder result = new StringBuilder("(");
        for (String string : list.subList(0, listSize - 1))
        {
            if (quote && !string.startsWith("\\Q") && !string.endsWith("\\E"))
            {
                result.append(Pattern.quote(string) + "|");
            }
            else
            {
                result.append(string + "|");
            }
        }
        if (quote)
        {
            result.append(Pattern.quote(list.get(listSize - 1)) + ")");
        }
        else
        {
            result.append(list.get(listSize - 1) + ")");
        }
        return result.toString();
    }
    
    /**
     * Compares two lists of equal type, by returning a number
     * composed of the sum of all differences of all non-overlapping
     * members of the two lists.
     * 
     * @param <T>
     * @param oneList
     * @param anotherList
     * @return the difference between the two lists.
     */
    public static <T extends Comparable<?>> int compare(List<T> oneList, List<T> anotherList)
    {
        return compareOneWay(oneList, anotherList) + compareOneWay(anotherList, oneList);
    }
    
    /**
     * Compares two lists of equal type in one direction only (checking
     * the items from the second list that do not occur in the first).
     * 
     * @param <T>
     * @param oneList
     * @param anotherList
     * @return the comparison between the items of the second list that do not appear in the first, and null
     */
    private static <T extends Comparable<?>> int compareOneWay(List<T> oneList, List<T> anotherList)
    {
        int result = 0;
        if (!oneList.containsAll(anotherList))
        {
            for (T item : anotherList)
            {
                if (!oneList.contains(item))
                {
                    result += item.compareTo(null);
                }
            }
        }
        return result;
    }
}
