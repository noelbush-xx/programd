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

import org.apache.log4j.Logger;

/**
 * <code>URLTools</code> contains helper methods for dealing with URLs.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.6
 */
public class URLTools
{
    /** A slash. */
    private static final String SLASH = "/";

    /** The string ":/". */
    private static final String COLON_SLASH = ":/";
    
    /** A dot (period). */
    private static final String DOT = ".";
    
    /** The empty string. */
    private static final String EMPTY_STRING = "";
    
    private static final Logger logger = Logger.getLogger("programd");

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
                throw new DeveloperError(String.format("Subject URL is malformed: \"%s\".", subject), e);
            }
            // If the subject has "file" scheme, try to make sure it is absolute (in a file path sense).
            if (subjectURI.getScheme().equals(FileManager.FILE))
            {
                String originalPath = subjectURI.getPath();
                if (originalPath != null)
                {
                    String path;
					try
					{
						path = FileManager.getBestFile(originalPath).toURL().getPath();
					}
					catch (MalformedURLException e)
					{
                        throw new DeveloperError(String.format("Error getting URL from file \"%s\".", originalPath), e);
					}
                    if (!pathsAreEquivalent(path, originalPath))
                    {
                        try
                        {
                            subjectURI = new URI(FileManager.FILE, subjectURI.getAuthority(), path, subjectURI.getQuery(), subjectURI.getFragment());
                        }
                        catch (URISyntaxException e)
                        {
                            throw new DeveloperError(String.format("Error resolving file URI \"%s\".", subjectURI), e);
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
        // If the context *does* specify a file, then we need to remove the file (get the parent) first.
        URL parent = getParent(context);
        if (!parent.getFile().equals(context.getFile()))
        {
            return contextualize(parent, subject);
        }
        // otherwise...
        try
        {
            return new URL(context.getProtocol(), context.getHost(), context.getPort(), subject.getPath());
        }
        catch (MalformedURLException e)
        {
            throw new DeveloperError("Given subject cannot be contextualized in given context.", e);
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
    	// Avoid the most obvious problem...
    	subject = escape(subject.replace(File.separatorChar, '/'));
    	try
    	{
	    	context = new URL(escape(context.toString().replace(File.separatorChar, '/')));
    	}
    	catch (MalformedURLException e)
    	{
    		// Do nothing, but we may fail.
    		logger.warn(String.format("Could not escape context URL \"%s\".", context));
    	}
        if (context.toString().equals(subject))
        {
        	return context;
        }
        // See if the subject seems to specify a URL, and if so, send it to the other method.
        if (subject.matches("^[a-z]+:/.*"))
        {
            try
            {
                return contextualize(context, new URL(subject));
            }
            catch (MalformedURLException e)
            {
                throw new DeveloperError(String.format("Subject URL is malformed: \"%s\".", subject), e);
            }
        }
        if (probablyIsNotFile(context))
        {
            URI resolved = null;
            try
            {
                String contextString = context.toString();
                int colon = contextString.indexOf(COLON_SLASH);
                if (colon > -1)
                {
                	try
                	{
                		resolved = new URI(context.getProtocol() + ':' + new URI(contextString.substring(colon + 1) + SLASH).resolve(subject).toString());
                	}
                	catch (IllegalArgumentException e)
                	{
                		throw new UserError(String.format("Could not resolve \"%s\" against \"%s\".", subject, context), e);
                	}
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
        // If the context *does* specify a file, then we need to remove the file (get the parent) first.
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
    public static URL createValidURL(String path) throws FileNotFoundException
    {
        return createValidURL(path, null, true);
    }

    /**
     * Attempts to create the given <code>path</code> into a valid URL, using
     * a few heuristics.  Tries to validate the given path (if it is a file).
     * 
     * @param path
     * @param context the context in which to resolve relative URLs (may be null)
     * @return a valid URL, if possible
     */
    public static URL createValidURL(String path, URL context) throws FileNotFoundException
    {
        return createValidURL(path, context, true);
    }

    /**
     * Attempts to create the given <code>path</code> into a valid URL, using
     * a few heuristics.
     * 
     * @param path
     * @param context the context in which to resolve relative URLs (may be null)
     * @param tryToValidate whether the method should try to validate the existence of the path
     * @return a valid URL, if possible
     */
    public static URL createValidURL(String path, URL context, boolean tryToValidate) throws FileNotFoundException
    {
        URL url;
        // See if this already appears to be a URL (over-simple heuristic).
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
                try
                {
                    file = FileManager.getExistingFile(path);
                }
                catch (FileNotFoundException e)
                {
                    if (context == null)
                    {
                        throw e;
                    }
                    // Otherwise...
                    url = URLTools.contextualize(context, path);
                    file = FileManager.getExistingFile(url.getFile());
                    // Let the FileNotFoundException be thrown this time, if file isn't found.
                }
            }
            else
            {
                file = new File(path);
            }
            try
            {
                url = file.toURL();
            }
            catch (MalformedURLException e)
            {
                throw new DeveloperError("Malformed URL: \"" + path + "\"", e);
            }
        }
        return url;
    }
    
    /**
     * Take a path spec that may, or may not, use glob-style wildcards to
     * indicate multiple files, and returns a list of URLs pointing to those
     * files.
     * 
     * @param pathspec the path specification that may point to one or many files
     * @return a list of URLs
     */
    public static List<URL> getURLs(String pathspec, URL context)
    {
        ArrayList<URL> result = new ArrayList<URL>();
        if (pathspec.indexOf('*') != -1 || pathspec.indexOf('?') != -1)
        {
            List<File> files;
            try
            {
                files = FileManager.glob(pathspec);
            }
            catch (FileNotFoundException e)
            {
                throw new UserError("File not found when globbing \"" + pathspec + "\".", e);
            }
            int fileCount = files.size();
            for (int index = 0; index < fileCount; index++)
            {
                try
                {
                    result.add(createValidURL(files.get(index).getAbsolutePath(), context));
                }
                catch (FileNotFoundException e)
                {
                    throw new UserError("Could not find file \"" + files.get(index) + "\" from \"" + pathspec + "\".", e);
                }
            }
        }
        else
        {
            try
            {
                result.add(createValidURL(pathspec, context));
            }
            catch (FileNotFoundException e)
            {
                throw new UserError("Could not find file \"" + pathspec + "\".", e);
            }
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
    
    /**
     * A convenience method that calls toString()
     * on the given URL, then returns the result of
     * {@link #unescape(String)}.
     */
    public static String unescape(URL url)
    {
    	return unescape(url.toString());
    }
    
    /**
     * Using some rather uncomfortable heuristics, judges whether
     * two given paths are (probably) equivalent, by ignoring
     * certain differences like platform-specific path separators
     * vs. the URI/URL standard slash, and the use of a Windows
     * drive letter preceded, or not, by a slash.  Yuck.
     * 
     * @param path1
     * @param path2
     * @return whether or not they are (probably) equivalent
     */
    public static boolean pathsAreEquivalent(String path1, String path2)
    {
    	// Get out as fast as possible.
    	if (path1.equals(path2))
    	{
    		return true;
    	}
    	// Now try fixing path separators and check again.
    	path1 = path1.replace(File.separatorChar, '/');
    	path2 = path2.replace(File.separatorChar, '/');
    	if (path1.equals(path2))
    	{
    		return true;
    	}
    	// Now try the yucky drive letter check.
    	path1 = path1.replaceAll("^/(\\p{Upper}:)", "$1");
    	path2 = path2.replaceAll("^/(\\p{Upper}:)", "$1");
    	// This is the last check, so return whether or not these match.
    	return path1.equals(path2);
    }
}
