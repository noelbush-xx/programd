// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: PathMap.java,v 1.1.1.1 2001/06/17 19:00:57 noelbu Exp $
// ========================================================================

package org.alicebot.server.net.http;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

/* ------------------------------------------------------------ */
/** URI path map to Object.
 * This mapping implements the path specification recommended
 * in the 2.2 Servlet API.
 *
 * Path specifications can be of the following forms:<PRE>
 * /foo/bar           - an exact path specification.
 * /foo/*             - a prefix path specification (must end '/*').
 * *.ext              - a suffix path specification.
 * /                  - the default path specification.       
 * </PRE>
 * Matching is performed in the following order <NL>
 * <LI>Exact match.
 * <LI>Longest prefix match.
 * <LI>Longest suffix match.
 * <LI>default.
 * </NL>
 * Multiple path specifications can be mapped by providing a coma
 * separated list of specifications.
 * <P>
 * Note that this is a very different mapping to that provided by PathMap
 * in Jetty2.
 * <P>
 * Note that exact matches can be terminated my the ; character as
 * used in servlet session rewriting.
 *
 * @version $Id: PathMap.java,v 1.1.1.1 2001/06/17 19:00:57 noelbu Exp $
 * @author Greg Wilkins (gregw)
 */
public class PathMap extends HashMap
{
    /* --------------------------------------------------------------- */
    HashMap _entryMap=new HashMap();
    HashMap _prefixMap=new HashMap();
    
    /* --------------------------------------------------------------- */
    /** Construct empty PathMap.
     */
    public PathMap()
    {
        super(11);
    }
    
    /* --------------------------------------------------------------- */
    /** Construct empty PathMap.
     */
    public PathMap(int capacity)
    {
        super (capacity);
    }
    
    /* --------------------------------------------------------------- */
    /** Construct from dictionary PathMap.
     */
    public PathMap(Map m)
    {
        putAll(m);
    }
    
    /* --------------------------------------------------------------- */
    /** Add a single path match to the PathMap.
     * @param pathSpec The path specification, or coma separated list of
     * path specifications.
     * @param object The object the path maps to
     */
    public synchronized Object put(Object pathSpec, Object object)
    {
        StringTokenizer tok = new StringTokenizer(pathSpec.toString(),",");
        Object old =null;
        
        while (tok.hasMoreTokens())
        {
            String prefix=tok.nextToken();
            
            if (!prefix.startsWith("/") && !prefix.startsWith("*."))
                throw new IllegalArgumentException("PathSpec "+pathSpec+". must start with '/' or '*.'");
            
            old = super.put(prefix,object);
            
            // Look for the entry that was just created.
            Set entries = entrySet();
            Iterator iter=entries.iterator();
            while(iter.hasNext())
            {
                Map.Entry entry =
                    (Map.Entry)iter.next();
                if (entry.getKey().equals(prefix))
                {
                    // Create a map to the entry
                    _entryMap.put(prefix,entry);
                    
                    // Also create a prefix map to the entry
                    if (prefix.endsWith("/*"))
                        _prefixMap.put(prefix.substring(0,prefix.length()-2),entry);
                    break;
                }
            }
        }
            
        return old;
    }

    /* ------------------------------------------------------------ */
    /** Get object matched by the path.
     * @param path the path.
     * @return Best matched object or null.
     */
    public Object match(String path)
    {
        Map.Entry entry = getMatch(path);
        if (entry!=null)
            return entry.getValue();
        return null;
    }
    
    
    /* --------------------------------------------------------------- */
    /** Get the entry mapped by the best specification.
     * @param path the path.
     * @return Map.Entry of the best matched  or null.
     */
    public synchronized Map.Entry getMatch(String path)
    {
        Object entry;

        // try exact match
        entry=_entryMap.get(path);
        if (entry!=null)
            return (Map.Entry) entry;
        
        // try exact prefix prefix search
        entry=_prefixMap.get(path);
        if (entry!=null)
            return (Map.Entry) entry;
            
        // prefix search
        String prefix=path;
        int i;
        while((i=prefix.lastIndexOf('/'))>=0)
        {
            prefix=prefix.substring(0,i);
            entry=_prefixMap.get(prefix);
            if (entry!=null)
                return (Map.Entry) entry;
        }
        
        // Extension search
        i=0;
        while ((i=path.indexOf('.',i+1))>0)
        {
            String extension="*"+path.substring(i);
            entry=_entryMap.get(extension);
            if (entry!=null)
                return (Map.Entry) entry;
        }

        // try exact match upto ';'
        i=path.lastIndexOf(';');
        if (i>0)
            return getMatch(path.substring(0,i));        
        
        // Default
        return (Map.Entry) _entryMap.get("/");
    }
    
    /* --------------------------------------------------------------- */
    /** Get all entries matched by the path.
     * Best match first.
     * @param path Path to match
     * @return List of Map.Entry instances key=pathSpec
     */
    public List getMatches(String path)
    {        
        Object entry;
        ArrayList entries= new ArrayList(8);
        
        // try exact match
        entry=_entryMap.get(path);
        if (entry!=null)
            entries.add(entry);
        
        
        // try exact prefix prefix search
        entry=_prefixMap.get(path);
        if (entry!=null)
            entries.add(entry);
        
        // prefix search
        String prefix=path;
        int i;
        while((i=prefix.lastIndexOf('/'))>=0)
        {
            prefix=prefix.substring(0,i);
            entry=_prefixMap.get(prefix);
            if (entry!=null)
                entries.add(entry);
        }
        
        // Extension search
        i=0;
        while ((i=path.indexOf('.',i+1))>0)
        {
            String extension="*"+path.substring(i);
            entry=_entryMap.get(extension);
            if (entry!=null)
                entries.add(entry);
        }


        // try exact match upto ';'
        i=path.lastIndexOf(';');
        if (i>0)
        {
            entries.addAll(getMatches(path.substring(0,i)));        
            return entries;
        }
        
        // Default
        entry=_entryMap.get("/");
        if (entry!=null)
            entries.add(entry);
        return entries;
    }


    /* --------------------------------------------------------------- */  
    public synchronized Object remove(Object pathSpec)
    {
        if (pathSpec!=null)
        {
            String prefix=pathSpec.toString();
            if (prefix.endsWith("/*"))
                _prefixMap.remove(prefix.substring(0,prefix.length()-2));
        }
        _entryMap.remove(pathSpec);
        return super.remove(pathSpec);
    }
    
    /* --------------------------------------------------------------- */
    public void clear()
    {
        _entryMap.clear();
        _prefixMap.clear();
        super.clear();
    }

    
    
    /* --------------------------------------------------------------- */
    /**
     * @return true if match.
     */
    public static boolean match(String pathSpec, String path)
        throws IllegalArgumentException
    {
        char c = pathSpec.charAt(0);
        if (c=='/')
        {
            if (pathSpec.length()==1 || pathSpec.equals(path))
                return true;
            
            if (pathSpec.endsWith("/*") &&
                pathSpec.regionMatches(0,path,0,pathSpec.length()-2))
                return true;
            
            if (path.startsWith(pathSpec) && path.charAt(pathSpec.length())==';')
                return true;
        }
        else if (c=='*')
            return path.regionMatches(path.length()-pathSpec.length()-1,
                                      pathSpec,1,pathSpec.length()-1);
        return false;
    }
    
    /* --------------------------------------------------------------- */
    /** Return the portion of a path that matches a path spec.
     * @return null if no match at all.
     */
    public static String pathMatch(String pathSpec, String path)
    {  
        char c = pathSpec.charAt(0);
        
        if (c=='/')
        {
            if (pathSpec.length()==1)
                return "";
        
            if (pathSpec.equals(path))
                return path;
            
            if (pathSpec.endsWith("/*") &&
                pathSpec.regionMatches(0,path,0,pathSpec.length()-2))
                return path.substring(0,pathSpec.length()-2);
            
            if (path.startsWith(pathSpec) && path.charAt(pathSpec.length())==';')
                return path;
        }
        else if (c=='*')
        {
            if (path.regionMatches(path.length()-(pathSpec.length()-1),
                                   pathSpec,1,pathSpec.length()-1))
                return path;
        }
        return null;
    }
    
    /* --------------------------------------------------------------- */
    /** Return the portion of a path that is after a path spec.
     * @return The path info string
     */
    public static String pathInfo(String pathSpec, String path)
    {
        char c = pathSpec.charAt(0);
        
        if (c=='/')
        {
            if (pathSpec.length()==1)
                return path;
            
            if (pathSpec.equals(path))
                return null;
            
            if (pathSpec.endsWith("/*") &&
                pathSpec.regionMatches(0,path,0,pathSpec.length()-2))
            {
                if (path.length()==pathSpec.length()-2)
                    return null;
                return path.substring(pathSpec.length()-2);
            }
        } 
        return null;
    }


    /* ------------------------------------------------------------ */
    /** Relative path.
     * @param base The base the path is relative to.
     * @param pathSpec The spec of the path segment to ignore.
     * @param path the additional path
     * @return base plus path with pathspec removed 
     */
    public static String relativePath(String base,
                                      String pathSpec,
                                      String path )
    {
        String info=pathInfo(pathSpec,path);
        if (info==null)
            info=path;

        if( info.startsWith( "./"))
            info = info.substring( 2);
        if( base.endsWith( "/"))
            if( info.startsWith( "/"))
                path = base + info.substring(1);
            else
                path = base + info;
        else
            if( info.startsWith( "/"))
                path = base + info;
            else
                path = base + "/" + info;
        return path;
    }
    
}
