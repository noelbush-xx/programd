/*
 * aitools utilities
 * Copyright (C) 2006 Noel Bush
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.

 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package org.aitools.util.resource;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.aitools.util.runtime.DeveloperError;
import org.aitools.util.runtime.UserError;
import org.apache.log4j.Logger;

/**
 * <code>URLTools</code> contains helper methods for dealing with URLs.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
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
    
    private URLTools()
    {
        // Nobody can instantiate this.
    }

    /**
     * <p> Tries to put the <code>subject</code> in the &quot;context&quot; of the <code>context</code>. If the
     * <code>context</code> URL does not appear to specify a file, this will essentially be the equivalent of
     * {@link java.net.URI#resolve(URI) URI.resolve}; if a file <i>is</i> specified by <code>context</code>, and if
     * <code>subject</code> is relative, then this will replace the file component of <code>context</code> with
     * <code>subject</code>. </p> <p> If <code>subject</code> is not relative, this will throw a
     * {@link java.net.MalformedURLException MalformedURLException} is thrown. </p>
     * 
     * @param context
     * @param subject
     * @return the result of &quot;contextualizing&quot; the given <code>subject</code> in the <code>context</code>
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
            if (subjectURI.getScheme().equals(Filesystem.FILE))
            {
                String originalPath = subjectURI.getPath();
                if (originalPath != null)
                {
                    String path;
                    try
                    {
                        path = Filesystem.getBestFile(originalPath).toURI().toURL().getPath();
                    }
                    catch (MalformedURLException e)
                    {
                        throw new DeveloperError(String.format("Error getting URL from file \"%s\".", originalPath), e);
                    }
                    if (!pathsAreEquivalent(path, originalPath))
                    {
                        try
                        {
                            subjectURI = new URI(Filesystem.FILE, subjectURI.getAuthority(), path, subjectURI
                                    .getQuery(), subjectURI.getFragment());
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
     * Same as {@link #contextualize(URL, URL)}, except the <code>subject</code> is a String which is supposed to
     * <i>not</i> be absolute (a quick check is made of this, and if the <code>subject</code> does look absolute, it
     * is made into a URL and sent to {@link #contextualize(URL, URL)}).
     * 
     * @param context
     * @param subject
     * @return the result of &quot;contextualizing&quot; the given <code>subject</code> in the <code>context</code>
     */
    public static URL contextualize(URL context, String subject)
    {
        // Avoid the most obvious problem...
        String _subject = escape(subject.replace(File.separatorChar, '/'));
        URL _context = null;
        try
        {
            _context = new URL(escape(context.toString().replace(File.separatorChar, '/')));
        }
        catch (MalformedURLException e)
        {
            // Do nothing, but we may fail.
            logger.warn(String.format("Could not escape context URL \"%s\".", _context));
        }
        if (_context == null) {
            throw new DeveloperError(String.format("Escaped context became null: \"%s\".", context), new NullPointerException());
        }
        if (_context.toString().equals(_subject))
        {
            return _context;
        }
        // See if the subject seems to specify a URL, and if so, send it to the other method.
        if (_subject.matches("^[a-z]+:/.*"))
        {
            try
            {
                return contextualize(_context, new URL(_subject));
            }
            catch (MalformedURLException e)
            {
                throw new DeveloperError(String.format("Subject URL is malformed: \"%s\".", _subject), e);
            }
        }
        if (probablyIsNotFile(_context))
        {
            URI resolved = null;
            try
            {
                String contextString = _context.toString();
                int colon = contextString.indexOf(COLON_SLASH);
                if (colon > -1)
                {
                    try
                    {
                        resolved = new URI(_context.getProtocol() + ':'
                                + new URI(contextString.substring(colon + 1) + SLASH).resolve(_subject).toString());
                    }
                    catch (IllegalArgumentException e)
                    {
                        throw new UserError(
                                String.format("Could not resolve \"%s\" against \"%s\".", _subject, _context), e);
                    }
                }
                else
                {
                    resolved = _context.toURI().resolve(_subject);
                }

            }
            catch (URISyntaxException e)
            {
                throw new DeveloperError(String.format("Context URL is malformed. (\"%s\")", _context), e);
            }
            if (resolved.isAbsolute())
            {
                try
                {
                    return resolved.toURL();
                }
                catch (MalformedURLException e)
                {
                    throw new DeveloperError(String.format("URI cannot be converted to URL (\"%s\")", resolved
                            .toString()), e);
                }
            }
            // otherwise...
            throw new DeveloperError(String.format("URI is not absolute (\"%s\")", resolved.toString()),
                    new IllegalArgumentException());
        }
        // If the context *does* specify a file, then we need to remove the file (get the parent) first.
        URL parent = getParent(_context);
        if (!parent.getFile().equals(_context.getFile()))
        {
            return contextualize(parent, _subject);
        }
        // otherwise...
        try
        {
            return new URL(_context.getProtocol(), _context.getHost(), _context.getPort(), _subject);
        }
        catch (MalformedURLException e)
        {
            throw new DeveloperError("Given subject cannot be contextualized in given context.", e);
        }
    }
    
    /**
     * Same as {@link #contextualize(URL, URL)}, except both <code>context</code> and <code>subject</code>
     * are Strings.
     * @param context 
     * @param subject 
     * @return the result of &quot;contextualizing&quot; the given <code>subject</code> in the <code>context</code>
     */
    public static URL contextualize(String context, String subject)
    {
        try
        {
            return contextualize(createValidURL(context, false), subject);
        }
        catch (FileNotFoundException e)
        {
            throw new DeveloperError("Given subject cannot be contextualized in given context.", e);
        }
    }

    /**
     * A smarter version of {@link URI#relativize(URI)}. In addition to performing the relativation (and catching
     * exceptions when converting from URL to URI and back again), this will try to replace common "parent" portions of
     * the resulting path with "../" structures.
     * 
     * @param context
     * @param subject
     * @return the subject relative to the context
     */
    public static URL relativize(URL context, URL subject)
    {
        // Quick shortcut if they're equal
        if (areEffectivelyEqual(context, subject))
        {
            if (probablyIsNotFile(context))
            {
                try
                {
                    return new URL(context.getProtocol() + ":.");
                }
                catch (MalformedURLException e)
                {
                    throw new DeveloperError("Couldn't construct a \".\" URL.", e);
                }
            }
            return context;
        }
        /*
        // Quick shortcut if they're files in the same directory.
        if (getParent(context).equals(getParent(subject)))
        {
            return (getLastPathComponent(subject));
        }
        */
        
        URI contextURI = null;
        try
        {
            contextURI = context.toURI();
        }
        catch (URISyntaxException e)
        {
            throw new DeveloperError(String.format("Cannot create URI from context URL \"%s\".", context), e);
        }
        URI relativizedURI = null;
        try
        {
            relativizedURI = contextURI.relativize(subject.toURI());
        }
        catch (URISyntaxException e)
        {
            throw new DeveloperError(String.format("Cannot create URI from subject URL \"%s\".", subject), e);
        }
        URL relativizedURL = null;
        try
        {
            /*
             * Do this instead of URI.toURL(), since the latter will fail
             * if the URI is not absolute!
             */
            String path = relativizedURI.getPath();
            if ("".equals(path))
            {
                path = ".";
            }
            relativizedURL = new URL(context.getProtocol() + ':' + path);
        }
        catch (MalformedURLException e)
        {
            throw new DeveloperError(String.format("Cannot create URL from relativization result \"%s\".",
                    relativizedURI), e);
        }
        String relativizedString = relativizedURL.toString();

        URL parent = getParent(context);
        int levelUp = 0;
        if (probablyIsNotFile(subject))
        {
            levelUp = 1;
        }
        do
        {
            relativizedString = relativizedString.replace(parent.toString(), upLevel(levelUp));
            parent = getParent(parent);
            levelUp++;
        } while (!parent.equals(getParent(parent)));

        try
        {
            return new URL(relativizedString);
        }
        catch (MalformedURLException e)
        {
            try
            {
                return new URL(String.format("%s:%s", context.getProtocol(), relativizedString));
            }
            catch (MalformedURLException ee)
            {
                throw new DeveloperError(String.format("Cannot create URL from relativization result \"%s\".",
                        relativizedString), ee);
            }
        }
    }
    
    /**
     * Produces a string version of {@link #relativize(URL, URL)}.
     * @param context 
     * @param subject 
     * @return a string version of {@link #relativize(URL, URL)}
     */
    public static String relativizeToString(URL context, URL subject)
    {
        String result = relativize(context, subject).getPath();
        if ("".equals(result))
        {
            result = ".";
        }
        if (result.endsWith("/"))
        {
            return result.substring(0, result.length() - 1);
        }
        return result;
    }
    
    /**
     * Attempts to "relativize" the given subject to the given context,
     * first trying to create a URL from the subject.
     * 
     * @param context
     * @param subject
     * @return the subject, relative to the context
     */
    public static URL relativize(URL context, String subject)
    {
        try
        {
            return relativize(context, new URL(subject));
        }
        catch (MalformedURLException e)
        {
            throw new DeveloperError(String.format("Cannot create URL from subject \"%s\".",
                    subject), e);
        }
    }

    private static String upLevel(int count)
    {
        if (count == 0)
        {
            return ".";
        }
        StringBuilder result = new StringBuilder(count * 3);
        for (int index = 0; index < count; index++)
        {
            if (result.length() > 0)
            {
                result.append('/');
            }
            result.append("..");
        }
        return result.toString();
    }
    
    /**
     * Returns whatever part of the given path follows its final
     * slash, unless that slash is the last character, in which case
     * the portion returned is that which follows the second-to-last slash.
     * If the path does not contain a slash, then it is returned unaltered.
     * 
     * @param path
     * @return the last component of the path
     */
    public static URL getLastPathComponent(URL path)
    {
        if (path.getPath().indexOf('/') == -1)
        {
            return path;
        }
        try
        {
            return new URL(path.getProtocol(),
                           path.getHost(),
                           path.getPath().replaceAll("^.*/(.+)/?$", "$1") + (path.getQuery() != null ? "?" + path.getQuery() : ""));
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(String.format("Could not get the last path component of \"%s\".", path));
        }
    }

    /**
     * Uses a couple of simple heuristics to guess whether a given URL probably is not pointing at a file.
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
     * Uses a couple of simple heuristics to guess whether a given URL probably is not pointing at a file.
     * 
     * NOTE: This is <em>way</em> imperfect! :-)
     * 
     * @param file the path to check
     * @return whether it probably is not a file
     */
    private static boolean probablyIsNotFile(String file)
    {
        /*
         * If the part of the context URL after the last "/" does not contain a ".", this is good enough (for our
         * purposes) to regard this as "not specifying a file", even though, of course, it could actually point to one.
         * 
         * We first test the simpler cases that contextFile is "" or "/", or ends with "/".
         */
        int slash = file.lastIndexOf(SLASH);
        return slash == -1 || file.equals(EMPTY_STRING) || file.equals(SLASH) || file.endsWith(SLASH)
                || (slash < file.length() - 1 && !file.substring(slash).contains(DOT));
    }

    /**
     * Attempts to create the given <code>path</code> into a valid URL, using a few heuristics. Tries to validate the
     * given path (if it is a file).
     * 
     * @param path
     * @return a valid URL, if possible
     * @throws FileNotFoundException
     */
    public static URL createValidURL(String path) throws FileNotFoundException
    {
        return createValidURL(path, null, true);
    }

    /**
     * Attempts to create the given <code>path</code> into a valid URL, using a few heuristics.
     * 
     * @param path
     * @param tryToValidate whether the method should try to validate the existence of the path
     * @return a valid URL, if possible
     * @throws FileNotFoundException
     */
    public static URL createValidURL(String path, boolean tryToValidate) throws FileNotFoundException
    {
        return createValidURL(path, null, tryToValidate);
    }

    /**
     * Attempts to create the given <code>path</code> into a valid URL, using a few heuristics. Tries to validate the
     * given path (if it is a file).
     * 
     * @param path
     * @param context the context in which to resolve relative URLs (may be null)
     * @return a valid URL, if possible
     * @throws FileNotFoundException
     */
    public static URL createValidURL(String path, URL context) throws FileNotFoundException
    {
        return createValidURL(path, context, true);
    }

    /**
     * Attempts to create the given <code>path</code> into a valid URL, using a few heuristics.
     * 
     * @param path
     * @param context the context in which to resolve relative URLs (may be null)
     * @param tryToValidate whether the method should try to validate the existence of the path
     * @return a valid URL, if possible
     * @throws FileNotFoundException
     */
    public static URL createValidURL(String path, URL context, boolean tryToValidate) throws FileNotFoundException
    {
        URL url = null;
        if (path == null)
        {
            throw new NullPointerException("path may not be null for createValidURL().");
        }
        // See if this already appears to be a URL (over-simple heuristic).
        if (path.indexOf(COLON_SLASH) > 0)
        {
            try
            {
                url = new URL(path);
            }
            catch (MalformedURLException e)
            {
                throw new DeveloperError(String.format("Cannot convert to URL: \"%s\"", path), e);
            }
        }
        // If it is not a URL by itself, try contextualizing it.
        else if (context != null)
        {
            // Otherwise...
            url = URLTools.contextualize(context, path);
        }
        // But if there's no context, try treating it as a file.
        else
        {
            File file = new File(path);
            try
            {
                url = file.toURI().toURL();
            }
            catch (MalformedURLException e)
            {
                throw new DeveloperError(String.format("Malformed URL: \"%s\"", path), e);
            }
        }
        
        // Now try to validate, if requested.
        if (tryToValidate)
        {
            if (seemsToExist(url))
            {
                return url;
            }
            throw new FileNotFoundException(String.format("Could not find \"%s\"", url));
        }
        // otherwise
        return url;
    }

    /**
     * Take a path spec that may, or may not, use glob-style wildcards to indicate multiple files, and returns a list of
     * URLs pointing to those files.
     * 
     * @param pathspec the path specification that may point to one or many files
     * @param context
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
                files = Filesystem.glob(pathspec);
            }
            catch (FileNotFoundException e)
            {
                throw new UserError(String.format("File not found when globbing \"%s\".", pathspec), e);
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
                    throw new UserError(String.format("Could not find file \"%s\" from \"%s\".", files.get(index),
                            pathspec), e);
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
                throw new UserError(String.format("Could not find file \"%s\".", pathspec), e);
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
        String parent = file.replaceAll("^(.+)/[^/]+/?$", "$1");
        try
        {
            return new URL(url.getProtocol(), url.getHost(), url.getPort(), parent);
        }
        catch (MalformedURLException e)
        {
            throw new RuntimeException(String.format("Could not determine parent of \"%s\".", url));
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
        if (url.getProtocol().equals(Filesystem.FILE))
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
     * Does very minimal URL escaping -- just enough to avoid complaints from the URI &amp; URL constructors (maybe).
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
     * A convenience method that calls toString() on the given URL, then returns the result of {@link #unescape(String)}.
     * 
     * @param url
     * @return the unescaped URL
     */
    public static String unescape(URL url)
    {
        return unescape(url.toString());
    }

    /**
     * Using some rather uncomfortable heuristics, judges whether two given paths are (probably) equivalent, by ignoring
     * certain differences like platform-specific path separators vs. the URI/URL standard slash, and the use of a
     * Windows drive letter preceded, or not, by a slash. Yuck.
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
        String _path1 = path1.replace(File.separatorChar, '/');
        String _path2 = path2.replace(File.separatorChar, '/');
        if (_path1.equals(_path2))
        {
            return true;
        }
        
        // Try removing trailing slashes.
        _path1 = _path1.replaceAll("^(.+)/+$", "$1");
        _path2 = _path2.replaceAll("^(.+)/+$", "$1");
        if (_path1.equals(_path2))
        {
            return true;
        }
        
        // Now try the yucky drive letter check.
        _path1 = _path1.replaceAll("^/(\\p{Upper}:)", "$1");
        _path2 = _path2.replaceAll("^/(\\p{Upper}:)", "$1");
        
        // This is the last check, so return whether or not these match.
        return _path1.equals(_path2);
    }
    
    /**
     * Indicates whether the given URLs are effectively equal, in terms of "effectively"
     * resolving to the same thing.  Probably there is a more standard way to do this.
     * 
     * @param path1
     * @param path2
     * @return whether the given URLs are effectively equal
     */
    public static boolean areEffectivelyEqual(URL path1, URL path2)
    {
        if (path1 == null || path2 == null)
        {
            return false;
        }
        if (!((path1.getProtocol() == null && path2.getProtocol() == null)
                || (!path1.getProtocol().equals(path2.getProtocol()))))
        {
            return false;
        }
        if (!((path1.getAuthority() == null && path2.getAuthority() == null)
                || (path1.getAuthority().equals(path2.getAuthority()))))
        {
            return false;
        }
        if (path1.getPort() != path2.getPort())
        {
            return false;
        }
        if (!((path1.getHost() == null && path2.getHost() == null)
                || (!path1.getHost().equals(path2.getHost()))))
        {
            return false;
        }
        if (!((path1.getQuery() == null && path2.getQuery() == null)
                || (!path1.getQuery().equals(path2.getQuery()))))
        {
            return false;
        }
        if (!((path1.getRef() == null && path2.getRef() == null)
                || (!path1.getRef().equals(path2.getRef()))))
        {
            return false;
        }
        if (!((path1.getUserInfo() == null && path2.getUserInfo() == null)
                || (!path1.getUserInfo().equals(path2.getUserInfo()))))
        {
            return false;
        }
        return pathsAreEquivalent(path1.getPath(), path2.getPath());
    }

    /**
     * Returns the common parent of all the given URLs. If they don't have one, returns null.
     * 
     * @param urls
     * @return the common parent
     */
    public static URL getCommonParent(URL... urls)
    {
        return getCommonParent(null, null, urls);
    }

    /**
     * Returns the common parent of all the given URLs. If they don't have one, returns the given fallback.
     * 
     * @param fallback
     * @param urls
     * @return the common parent
     */
    public static URL getCommonParent(URL fallback, URL... urls)
    {
        return getCommonParent(fallback, null, urls);
    }

    private static URL getCommonParent(URL fallback, URL candidate, URL... urls)
    {
        int length = urls.length;
        if (length == 0)
        {
            return fallback;
        }
        if (length == 1)
        {
            return getParent(urls[0]);
        }
        URL url0 = urls[0];
        URL url1 = urls[1];
        URL parent1 = url1;
        do
        {
            URL parent0 = url0;
            parent1 = getParent(parent1);
            do
            {
                parent0 = getParent(parent0);
                if (parent0.equals(parent1))
                {
                    if (candidate == null || candidate.equals(parent0))
                    {
                        if (length == 2)
                        {
                            return parent0;
                        }
                        return getCommonParent(fallback, parent0, Arrays.asList(urls).subList(1, length).toArray(
                                new URL[] {}));
                    }
                }
            } while (!parent0.equals(getParent(parent0)));
        } while (!parent1.equals(getParent(parent1)));

        return fallback;
    }
    
    /**
     * URL-encodes the given string using UTF-8.
     * 
     * @param value
     * @return the encoded string
     */
    public static String encodeUTF8(String value)
    {
        try
        {
            return URLEncoder.encode(value, "utf-8");
        }
        catch (UnsupportedEncodingException e)
        {
            assert false : "This platform does not support UTF-8!";
        }
        return "";
    }
    
    /**
     * URL-decodes the given string using UTF-8.
     * 
     * @param value
     * @return the encoded string
     */
    public static String decodeUTF8(String value)
    {
        try
        {
            return URLDecoder.decode(value, "utf-8");
        }
        catch (UnsupportedEncodingException e)
        {
            assert false : "This platform does not support UTF-8!";
        }
        return "";
    }
}
