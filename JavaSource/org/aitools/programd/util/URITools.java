/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.util;

import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * <code>URITools</code> contains helper methods for dealing with URIs.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.2
 */
public class URITools
{

    /**
     * Attempts to create the given <code>path</code> into a valid URI, using
     * a few heuristics.  Tries to validate the given path, if it is a file.
     * 
     * @param path
     * @return a valid URI, if possible
     */
    public static URI createValidURI(String path) throws FileNotFoundException
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
    public static URI createValidURI(String path, boolean tryToValidate) throws FileNotFoundException
    {
        try
        {
            return URLTools.createValidURL(path, null, tryToValidate).toURI();
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
    public static URI relativize(URI relativizeTo, String subject) throws FileNotFoundException
    {
        return relativizeTo.relativize(createValidURI(subject, false));
    }

}
