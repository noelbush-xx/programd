// ===========================================================================
// Copyright (c) 1996 Mort Bay Consulting Pty. Ltd. All rights reserved.
// $Id: JarResource.java,v 1.1.1.1 2001/06/17 19:01:54 noelbu Exp $
// ---------------------------------------------------------------------------
package org.alicebot.server.net.http.util;

import java.io.InputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.File;
import java.net.URL;
import java.net.URLConnection;
import java.net.JarURLConnection;
import java.util.jar.JarInputStream;
import java.util.jar.JarEntry;
import java.util.Enumeration;


/* ------------------------------------------------------------ */
public class JarResource extends Resource
{
    protected JarURLConnection _jarConnection;
    
    /* -------------------------------------------------------- */
    JarResource(URL url)
    {
        super(url,null);
    }

    /* ------------------------------------------------------------ */
    protected boolean checkConnection()
    {
        boolean check=super.checkConnection();
        try{
            if (_jarConnection!=_connection)
                newConnection();
        }
        catch(IOException e)
        {
            Code.ignore(e);
            _jarConnection=null;
        }
        
        return _jarConnection!=null;
    }

    /* ------------------------------------------------------------ */
    protected void newConnection()
        throws IOException
    {
        _jarConnection=(JarURLConnection)_connection;
    }
    
    /* ------------------------------------------------------------ */
    /**
     * Returns true if the respresenetd resource exists.
     */
    public boolean exists()
    {
        if (_urlString.endsWith("!/"))
            return checkConnection();
        else
            return super.exists();
    }    

    /* ------------------------------------------------------------ */
    public InputStream getInputStream()
        throws java.io.IOException
    {
        if (!_urlString.endsWith("!/"))
            return super.getInputStream();
        
        URL url = new URL(_urlString.substring(4,_urlString.length()-2));
        return url.openStream();
    }
    
    /* ------------------------------------------------------------ */
    public void extract(File directory, boolean deleteOnExit)
        throws IOException
    {
        Code.debug("Extract ",this," to ",directory);
        JarInputStream jin = new JarInputStream(getInputStream());
        JarEntry entry=null;
        while((entry=jin.getNextJarEntry())!=null)
        {
            File file=new File(directory,entry.getName());
            if (entry.isDirectory())
            {
                // Make directory
                if (!file.exists())
                    file.mkdirs();
            }
            else
            {
                // make directory (some jars don't list dirs)
                File dir = new File(file.getParent());
                if (!dir.exists())
                    dir.mkdirs();

                // Make file
                FileOutputStream fout = new FileOutputStream(file);
                IO.copy(jin,fout);
                fout.close();
            }
            if (deleteOnExit)
                file.deleteOnExit();
        }
    }   
}
