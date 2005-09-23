/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

/**
 * <code>URITools</code> contains helper methods for dealing with URIs and
 * URLs.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.2
 */
public class URITools
{
    /** A slash. */
    private static final String SLASH = "/";

    /** The string "://". */
    private static final String COLON_SLASH = ":/";

    /**
     * <p>
     * Tries to put the <code>subject</code> in the &quot;context&quot; of the
     * <code>context</code>. If the <code>context</code> URL does not
     * appear to specify a file, this will essentially be the equivalent of
     * {@link java.net.URI#resolve(URI) URI.resolve}; if a file <i>is</i>
     * specified by <code>context</code>, and if <code>subject</code> is
     * relative, then this will replace the file component of
     * <code>context</code> with <code>subject</code>.
     * </p>
     * <p>
     * If <code>subject</code> is not relative, this will throw a
     * {@link java.net.MalformedURLException MalformedURLException} is thrown.
     * </p>
     * 
     * @param context
     * @param subject
     * @return the result of &quot;contextualizing&quot; the given
     *         <code>subject</code> in the <code>context</code>
     */
    public static URL contextualize(URL context, URL subject)
    {
        if (context.equals(subject))
        {
            return subject;
        }
        String contextFile = context.getFile();
        /*
         * If the context URL ends with a /, this is good enough (for our
         * purposes) to regard this as "not specifying a file", even though, of
         * course, it could actually point to one.
         */
        if (contextFile.endsWith(SLASH))
        {
            // Transform the subject into a URI for manipulation.
            URI subjectURI = null;
            try
            {
                subjectURI = subject.toURI();
            }
            catch (URISyntaxException e)
            {
                throw new DeveloperError("Subject URL is malformed.", e);
            }
            // If the subject is absolute,
            if (subjectURI.isAbsolute())
            {
                // then we just return it.
                try
                {
                    return subjectURI.toURL();
                }
                catch (MalformedURLException e)
                {
                    throw new DeveloperError("Subject URL is malformed.", e);
                }
            }
            // otherwise, we try resolving it against the context.
            try
            {
                return context.toURI().resolve(subjectURI).toURL();
            }
            catch (URISyntaxException e)
            {
                throw new DeveloperError("Context URL is malformed.", e);
            }
            catch (MalformedURLException e)
            {
                throw new DeveloperError("Given subject cannot be contextualized in given context.", e);
            }
        }
        // If the context *does* specify a file, then we need to remove it
        // first.
        String contextString = context.toString();
        try
        {
            return contextualize(new URL(contextString.substring(0, contextString.lastIndexOf('/') + 1)), subject);
        }
        catch (MalformedURLException e)
        {
            throw new DeveloperError("Cannot remove file part from context URL.", e);
        }
    }

    /**
     * Same as {@link #contextualize(URL, URL)}, except the
     * <code>subject</code> is a String which is assumed to <i>not</i> be
     * absolute.
     * 
     * @param context
     * @param subject
     * @return the result of &quot;contextualizing&quot; the given
     *         <code>subject</code> in the <code>context</code>
     */
    public static URL contextualize(URL context, String subject)
    {
        if (context.toString().equals(subject))
        {
            try
            {
                return new URL(subject);
            }
            catch (MalformedURLException e)
            {
                throw new DeveloperError("Subject URL is malformed.", e);
            }
        }
        String contextFile = context.getFile();
        /*
         * If the context URL ends with a /, this is good enough (for our
         * purposes) to regard this as "not specifying a file", even though, of
         * course, it could actually point to one.
         */
        if (contextFile.endsWith(SLASH))
        {
            try
            {
                return context.toURI().resolve(subject).toURL();
            }
            catch (URISyntaxException e)
            {
                throw new DeveloperError("Context URL is malformed.", e);
            }
            catch (MalformedURLException e)
            {
                throw new DeveloperError("Given subject cannot be contextualized in given context.", e);
            }
        }
        // If the context *does* specify a file, then we need to remove it
        // first.
        String contextString = context.toString();
        try
        {
            return contextualize(new URL(contextString.substring(0, contextString.lastIndexOf('/') + 1)), subject);
        }
        catch (MalformedURLException e)
        {
            throw new DeveloperError("Cannot remove file part from context URL.", e);
        }
    }

    /**
     * Same as {@link #contextualize(URL, String)}, except the
     * <code>context</code> is also a String.
     * 
     * @param context
     * @param subject
     * @return the result of &quot;contextualizing&quot; the given
     *         <code>subject</code> in the <code>context</code>
     */
    public static URL contextualize(String context, String subject)
    {
        if (context.equals(subject))
        {
            try
            {
                return new URL(subject);
            }
            catch (MalformedURLException e)
            {
                throw new DeveloperError("Subject URL is malformed.", e);
            }
        }
        /*
         * If the context URL ends with a /, this is good enough (for our
         * purposes) to regard this as "not specifying a file", even though, of
         * course, it could actually point to one.
         */
        if (context.endsWith(SLASH))
        {
            try
            {
                return new URI(context).resolve(subject).toURL();
            }
            catch (URISyntaxException e)
            {
                throw new DeveloperError("Context URL is malformed.", e);
            }
            catch (MalformedURLException e)
            {
                throw new DeveloperError("Given subject cannot be contextualized in given context.", e);
            }
        }
        // If the context *does* specify a file, then we need to remove it
        // first.
        try
        {
            return contextualize(new URL(context.substring(0, context.lastIndexOf('/') + 1)), subject);
        }
        catch (MalformedURLException e)
        {
            throw new DeveloperError("Cannot remove file part from context URL.", e);
        }
    }

    /**
     * Attempts to create the given <code>path</code> into a valid URL, using
     * a few heuristics.  Tries to validate the given path (if it is a file).
     * 
     * @param path
     * @return a valid URL, if possible
     */
    public static URL createValidURL(String path)
    {
        return createValidURL(path, true);
    }

    /**
     * Attempts to create the given <code>path</code> into a valid URL, using
     * a few heuristics.
     * 
     * @param path
     * @param tryToValidate whether the method should try to validate the existence of the path
     * @return a valid URL, if possible
     */
    public static URL createValidURL(String path, boolean tryToValidate)
    {
        URL url;
        if (path.indexOf(COLON_SLASH) > 0)
        {
            try
            {
                url = new URL(path);
            }
            catch (MalformedURLException e)
            {
                throw new DeveloperError("Cannot convert to URL: \"" + path + "\"", e);
            }
        }
        else
        {
            File file;
            if (tryToValidate)
            {
                file = FileManager.getExistingFile(path);
            }
            else
            {
                file = new File(path);
            }
            try
            {
                url = file.toURI().toURL();
            }
            catch (MalformedURLException e)
            {
                throw new DeveloperError("Malformed URL: \"" + path + "\"", e);
            }
        }
        return url;
    }
    
    /**
     * Attempts to create the given <code>path</code> into a valid URI, using
     * a few heuristics.  Tries to validate the given path, if it is a file.
     * 
     * @param path
     * @return a valid URI, if possible
     */
    public static URI createValidURI(String path)
    {
        return createValidURI(path, true);
    }
    
    /**
     * Attempts to create the given <code>path</code> into a valid URI, using
     * a few heuristics.
     * 
     * @param path
     * @param tryToValidate whether to try to validate the given path (if it is a file)
     * @return a valid URI, if possible
     */
    public static URI createValidURI(String path, boolean tryToValidate)
    {
        try
        {
            return createValidURL(path, tryToValidate).toURI();
        }
        catch (URISyntaxException e)
        {
            throw new DeveloperError("Could not construct a valid URI from \"" + path + "\".", e);
        }
    }
    
    /**
     * A wrapper for {@link URI#relativize(URI)}, allowing the second
     * argument to specified as a String.  Does <i>not</i> attempt to
     * validate the subject.
     * 
     * @param relativizeTo the URI against which to relativize
     * @param subject the URI to relativize
     * @return the subject relativized to the first argument
     * 
     */
    public static URI relativize(URI relativizeTo, String subject)
    {
        return relativizeTo.relativize(createValidURI(subject, false));
    }
    
    /**
     * Take a path spec that may, or may not, use glob-style wildcards to
     * indicate multiple files, and returns a list of URLs pointing to those
     * files.
     * 
     * @param pathspec the path specification that may point to one or many files
     * @return a list of URLs
     */
    public static List<URL> getURLs(String pathspec)
    {
        ArrayList<URL> result = new ArrayList<URL>();
        if (pathspec.indexOf('*') != -1 || pathspec.indexOf('?') != -1)
        {
            String[] files;
            try
            {
                files = FileManager.glob(pathspec);
            }
            catch (FileNotFoundException e)
            {
                throw new DeveloperError("File not found when processing glob!", e);
            }
            int fileCount = files.length;
            for (int index = 0; index < fileCount; index++)
            {
                result.add(createValidURL(files[index]));
            }
        }
        else
        {
            result.add(createValidURL(pathspec));
        }
        return result;
    }
    
    /**
     * Tries to get a URL for a resource, first by looking in the root directory,
     * then looking in jar files and the classpath (via Class.getResource()).
     * 
     * @param name the resource identifier
     * @return the URL of the resource
     * @throws FileNotFoundException if the resource could not be found at all
     */
    public static URL getResource(String name) throws FileNotFoundException
    {
        File file = FileManager.getExistingFile(name);
        if (file != null)
        {
            URL result = createValidURL(file.getAbsolutePath());
            if (result != null)
            {
                return result;
            }
        }
        URL result = URITools.class.getClass().getResource(name);
        if (result != null)
        {
            return result;
        }
        throw new FileNotFoundException(name);
    }
    
    /**
     * @param url some URL
     * 
     * @return the "parent" of the given URL, if possible
     */
    public static URL getParent(URL url)
    {
        String file = url.getFile();
        try
        {
            return new URL(url.getProtocol(), url.getHost(), url.getPort(), file.substring(0, file.lastIndexOf('/')));
        }
        catch (MalformedURLException e)
        {
            return url;
        }
    }
    
    /**
     * Tests whether a resource seems to exist at the given URL
     * 
     * @param url the URL to test
     * @return whether a resource seems to exist at the URL
     */
    public static boolean seemsToExist(URL url)
    {
        InputStream test = null;
        try
        {
            test = url.openStream();
        }
        catch (IOException e)
        {
            return false;
        }
        if (test != null)
        {
            try
            {
                test.close();
            }
            catch (IOException e)
            {
                return false;
            }
            return true;
        }
        return false;
    }
    
    /**
     * Tries to get the last modified timestamp for the path.
     * 
     * @param path the URL to check
     * @return the apparent last modified timestamp, or 0 if cannot be determined
     */
    public static long getLastModified(URL path)
    {
        URLConnection connection = null;
        try
        {
            connection = path.openConnection();
        }
        catch (IOException e)
        {
            return 0;
        }
        if (connection == null)
        {
            return 0;
        }
        return connection.getLastModified();
    }
}
