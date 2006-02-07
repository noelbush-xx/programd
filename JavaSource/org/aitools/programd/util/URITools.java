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
    
    /** A dot (period). */
    private static final String DOT = ".";
    
    /** The empty string. */
    private static final String EMPTY_STRING = "";

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
        if (probablyIsNotFile(context))
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
            // If the subject has "file" scheme, try to make sure it is absolute (in a file path sense).
            if (subjectURI.getScheme().equals(FileManager.FILE))
            {
                String originalPath = subjectURI.getPath();
                if (originalPath != null)
                {
                    File file = FileManager.getFile(originalPath);
                    String path = file.getAbsolutePath();
                    if (!path.equals(originalPath))
                    {
                        try
                        {
                            subjectURI = new URI(FileManager.FILE, subjectURI.getAuthority(), path, subjectURI.getQuery(), subjectURI.getFragment());
                        }
                        catch (URISyntaxException e)
                        {
                            throw new DeveloperError("Error resolving file URI.", e);
                        }
                    }
                }
            }
            // If the subject is absolute (in a URI sense),
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
     * <code>subject</code> is a String which is supposed to <i>not</i> be
     * absolute (a quick check is made of this, and if the <code>subject</code>
     * does look absolute, it is made into a URL and sent to {@link #contextualize(URL, URL)}).
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
        if (subject.matches("^[a-z]+:.*"))
        {
            try
            {
                return contextualize(context, new URL(subject));
            }
            catch (MalformedURLException e)
            {
                throw new DeveloperError("Subject URL is malformed.", e);
            }
        }
        if (probablyIsNotFile(context))
        {
            URI resolved;
            try
            {
                String contextString = context.toString();
                int colon = contextString.indexOf(':');
                if (colon > -1)
                {
                    resolved = new URI(context.getProtocol() + ':' + new URI(contextString.substring(colon + 1) + SLASH).resolve(subject).toString());
                }
                else
                {
                    resolved = context.toURI().resolve(subject);
                }
                
            }
            catch (URISyntaxException e)
            {
                throw new DeveloperError("Context URL is malformed. (\"" + context + "\")", e);
            }
            if (resolved.isAbsolute())
            {
                try
                {
                    return resolved.toURL();
                }
                catch (MalformedURLException e)
                {
                    throw new DeveloperError("URI cannot be converted to URL (\"" + resolved.toString() + "\")", e);
                }
            }
            // otherwise...
            throw new DeveloperError("URI is not absolute (\"" + resolved.toString() + "\")", new IllegalArgumentException());
        }
        // If the context *does* specify a file, then we need to remove it
        // first.
        URL parent = getParent(context);
        if (!parent.getFile().equals(context.getFile()))
        {
            return contextualize(parent, subject);
        }
        // otherwise...
        try
        {
            return new URL(context.getProtocol(), context.getHost(), context.getPort(), subject);
        }
        catch (MalformedURLException e)
        {
            throw new DeveloperError("Given subject cannot be contextualized in given context.", e);
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
        if (probablyIsNotFile(context))
        {
            try
            {
                return new URI(context + SLASH).resolve(subject).toURL();
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
     * Uses a couple of simple heuristics to guess whether a
     * given URL probably is not pointing at a file.
     * 
     * NOTE: This is <em>way</em> imperfect! :-)
     * 
     * @param url the URL to check
     * @return whether it probably is not a file
     */
    private static boolean probablyIsNotFile(URL url)
    {
        return probablyIsNotFile(url.getFile());
    }
    
    /**
     * Uses a couple of simple heuristics to guess whether a
     * given URL probably is not pointing at a file.
     * 
     * NOTE: This is <em>way</em> imperfect! :-)
     * 
     * @param file the path to check
     * @return whether it probably is not a file
     */
    private static boolean probablyIsNotFile(String file)
    {
        /*
         * If the part of the context URL after the last "/" does not contain a ".",
         * this is good enough (for our purposes) to regard this as "not specifying
         * a file", even though, of course, it could actually point to one.
         * 
         * We first test the simpler cases that contextFile is "" or "/", or ends with "/".
         */
        int slash = file.lastIndexOf(SLASH);
        return slash == -1 || file.equals(EMPTY_STRING) || file.equals(SLASH) || file.endsWith(SLASH) ||
                (slash < file.length() - 1 && !file.substring(slash).contains(DOT));
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
     * @param url some URL
     * 
     * @return the "parent" of the given URL, if possible
     */
    public static URL getParent(URL url)
    {
        String file = url.getFile();
        try
        {
            int slash = file.lastIndexOf(SLASH);
            if (slash >= 1)
            {
                return new URL(url.getProtocol(), url.getHost(), url.getPort(), file.substring(0, slash));
            }
            return new URL(url.getProtocol(), url.getHost(), url.getPort(), SLASH);
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
        if (url.getProtocol().equals(FileManager.FILE))
        {
            File file = new File(unescape(url.getFile()));
            return file.exists();
        }
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
    
    /**
     * Does very minimal URL escaping -- just enough to avoid complaints
     * from the URI &amp; URL constructors (maybe).
     * 
     * @param url the URL to escape
     * @return the escaped URL
     */
    public static String escape(String url)
    {
        return url.replace(" ", "%20");
    }
    
    /**
     * Reverses {@link #escape(String)}.
     * 
     * @param url the URL to unescape
     * @return the unescaped URL
     */
    public static String unescape(String url)
    {
        return url.replace("%20", " ");
    }
}
