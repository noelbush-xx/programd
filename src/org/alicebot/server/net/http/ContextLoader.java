// ========================================================================
// Copyright (c) 1999 Mort Bay Consulting (Australia) Pty. Ltd.
// $Id: ContextLoader.java,v 1.1.1.1 2001/06/17 19:00:31 noelbu Exp $
// ========================================================================

package org.alicebot.server.net.http;

import org.alicebot.server.net.http.util.Code;
import org.alicebot.server.net.http.util.IO;
import org.alicebot.server.net.http.util.Resource;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.StringTokenizer;
import java.util.HashMap;
import java.security.Policy;
import java.security.PermissionCollection;
import java.security.CodeSource;

/* ------------------------------------------------------------ */
/** ClassLoader for HandlerContext.
 * Specializes URLClassLoader with some utility and file mapping
 * methods.
 *
 * @version $Id: ContextLoader.java,v 1.1.1.1 2001/06/17 19:00:31 noelbu Exp $
 * @author Greg Wilkins (gregw)
 */
public class ContextLoader extends URLClassLoader
{
    private static HashMap __infoMap = new HashMap(3);
    private PathInfo _info;
    private String _path;
    private static Class __servletClass = javax.servlet.http.HttpServlet.class;
    private PermissionCollection _permissions;
    
    /* ------------------------------------------------------------ */
    /** Constructor.
     * @param servletClassPath Coma separated path of filenames or URLs
     * pointing to directories or jar files. Directories should end
     * with '/'.
     * @param quiet If true, non existant paths are not reported
     * @exception IOException
     */
    public ContextLoader(String classPath,
                         ClassLoader parent,
                         PermissionCollection permisions)
    {
        super(decodePath(classPath),parent);
        _info=(PathInfo)__infoMap.get(classPath);
        _path=_info._classPath;
        _permissions=permisions;
        
        Code.debug("ContextLoader: ",_path,",",parent,
                   " == ",_info._fileClassPath);
        Code.debug("Permissions=",_permissions);
    }

    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private static URL[] decodePath(String classPath)
    {
        synchronized(__infoMap)
        {
            PathInfo info=(PathInfo)__infoMap.get(classPath);
            if (info!=null && info._urls!=null)
                return info._urls;

            info = new PathInfo();
            
            try{
                StringTokenizer tokenizer =
                    new StringTokenizer(classPath,",;");
                info._urls = new URL[tokenizer.countTokens()];
                int i=0;
                while (tokenizer.hasMoreTokens())
                {
                    Resource resource =
                        Resource.newResource(tokenizer.nextToken());

                    info._classPath=(info._classPath==null)
                        ?resource.toString()
                        :(info._classPath+File.pathSeparator+resource.toString());
                    
                    // Resolve file path if possible
                    File pfile=resource.getFile();
                    if (pfile==null)
                    {
                        info._unresolved=true;
                        info._fileClassPath=(info._fileClassPath==null)
                            ?resource.toString()
                            :(info._fileClassPath+
                              File.pathSeparator+
                              resource.toString());
                    }
                    else
                    {
                        info._fileClassPath=(info._fileClassPath==null)
                            ?pfile.getCanonicalPath()
                            :(info._fileClassPath+
                              File.pathSeparator+
                              pfile.getAbsolutePath());            
                    }
                    
                    // Add resource or expand jar/
                    if (resource.isDirectory() || pfile!=null)
                        info._urls[i++]=resource.getURL();
                    else
                    {
                        // XXX - this is a jar in a jar, so we must
                        // extract it - probably should be to an in memory
                        // structure, but this will do for now.
                        // XXX - Need to do better with the temp dir
                        InputStream in =resource.getInputStream();
                        File file=File.createTempFile("Jetty-",".jar");
                        file.deleteOnExit();
                        Code.debug("Extract ",resource," to ",file);
                        FileOutputStream out = new FileOutputStream(file);
                        IO.copy(in,out);
                        out.close();
                        info._urls[i++]=file.toURL();
                    }
                }
            }
            catch(Exception e){Code.warning(e);info=null;}
            catch(Error e){Code.warning(e);info=null;}
            
            if (info==null)
                info=new PathInfo();
            if (info._urls==null)
                info._urls=new URL[0];
            __infoMap.put(classPath,info);
            
            return info._urls;        
        }
    }
    
    /* ------------------------------------------------------------ */
    protected Class findClass(String name)
        throws ClassNotFoundException
    {
        if (Code.verbose())
            Code.debug("findClass(",name,") from ",_path);
        return super.findClass(name);
    }

    /* ------------------------------------------------------------ */
    protected synchronized Class loadClass(String name, boolean resolve)
        throws ClassNotFoundException
    {
        if (Code.verbose())
            Code.debug("loadClass(",name,","+resolve,") from ",_path);
         
         return super.loadClass(name,resolve);   
    }

    /* ------------------------------------------------------------ */
    public String getFileClassPath()
    {
        if (_info._unresolved)
            Code.warning("Non file CLASSPATH "+_path+
                         ". If JSP compiles are affected, try extracting WARs");
        
        return _info._fileClassPath;
    }
    
    /* ------------------------------------------------------------ */
    /** Return number of servlets this classloader has loaded.
     * Any class which is a subclass of javax.servlet.http.HttpServlet
     * will count. Does not include javax.servlet.http.HttpServlet
     * itself.
     * @deprecated. Removed due to bizarre class loading bug
     * associated with classes as local variables.
     */
    public int getLoadedServletsCount()
    {
        return 0;
    }
    
     /* ------------------------------------------------------------ */
    /** Return total number of classes this classloader has loaded.
     * @deprecated. Removed due to bizarre class loading bug
     * associated with classes as local variables.
     */
    public int getLoadedClassesCount()
    {
        return 0;
    }
    
    /* ------------------------------------------------------------ */
    public String toString()
    {
        return "org.alicebot.server.net.http.ContextLoader("+
            _path+") / "+getParent();
    }
    
    /* ------------------------------------------------------------ */
    public PermissionCollection getPermissions(CodeSource cs)
    {
        PermissionCollection pc =(_permissions==null)?
            super.getPermissions(cs):_permissions;
        Code.debug("loader.getPermissions("+cs+")="+pc);
        return pc;    
    }
    
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    /* ------------------------------------------------------------ */
    private static class PathInfo
    {
        URL[] _urls=null;
        String _classPath=null;
        String _fileClassPath=null;
        boolean _unresolved=false;
    }
    
}


