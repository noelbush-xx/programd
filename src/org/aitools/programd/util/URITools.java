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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

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
    private static final String COLON_SLASH_SLASH = "://";

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
     * a few heuristics.
     * 
     * @param path
     * @return a valid URL, if possible
     */
    public static URL createValidURL(String path)
    {
        URL url;
        if (path.indexOf(COLON_SLASH_SLASH) > 0)
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
            File file = FileManager.getExistingFile(path);
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
}
