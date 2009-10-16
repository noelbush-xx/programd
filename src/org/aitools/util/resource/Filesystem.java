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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipOutputStream;

import org.aitools.util.runtime.DeveloperError;
import org.aitools.util.runtime.UserError;
import org.aitools.util.xml.Characters;
import org.apache.log4j.Logger;

/**
 * FileManager provides a standard interface for getting File objects and paths.
 */
public class Filesystem
{
    /** The root path for running Program D. */
    private static URL root;

    /** The logger. */
    private static Logger LOGGER = Logger.getLogger("programd");

    /** The current working directory. */
    private static Stack<URL> workingDirectory = new Stack<URL>();

    /** The string &quot;{@value}&quot;. */
    public static final String FILE = "file";

    /** Put the working directory onto the stack right away. */
    static
    {
        try
        {
            // We do it directly, instead of using the push method, so we don't get complaints about a logger not being
            // ready.
            workingDirectory.add(URLTools.createValidURL(System.getProperty("user.dir")));
        }
        catch (FileNotFoundException e)
        {
            throw new DeveloperError("Current working directory (according to system properties) does not exist!", e);
        }
    }

    private static final String SLASH = "/";

    /**
     * Sets the root path.
     * 
     * @param url the root path
     */
    public static void setRootPath(URL url)
    {
        root = url;
        workingDirectory.push(url);
    }

    /**
     * Returns the root path.
     * 
     * @return the root path
     */
    public static URL getRootPath()
    {
        return root;
    }

    /**
     * Gets a file from a given path. Tries to return it as a canonical file; failing that, returns the absolute file,
     * (which may not be valid, but we do not check that here).
     * 
     * @param path the path for the file
     * @return the file (may not exist!)
     */
    public static File getBestFile(String path)
    {
        File file = new File(URLTools.unescape(path));
        try
        {
            return file.getCanonicalFile();
        }
        catch (IOException e)
        {
            return file.getAbsoluteFile();
        }
    }

    /**
     * Gets a file from a given path. First tries to use the path as-is if it's absolute, then looks in the current
     * working directory. The file <i>must</i> already exist, or an exception will be thrown.
     * 
     * @param path the path for the file (may be absolute or relative to root directory)
     * @return the file
     * @throws FileNotFoundException
     */
    public static File getExistingFile(String path) throws FileNotFoundException
    {
        File file = getBestFile(path);
        if (!file.exists())
        {
            file = getBestFile(workingDirectory.peek().getPath() + path);
            if (!file.exists())
            {
                throw new FileNotFoundException(String.format("Couldn't find \"%s\".", path));
            }
        }
        try
        {
            return file.getCanonicalFile();
        }
        catch (IOException e)
        {
            throw new DeveloperError(String.format("I/O Error creating the canonical form of file \"%s\".", path), e);
        }
    }

    /**
     * Sames as {@link #getExistingFile} except that it also checks that the given path is a directory.
     * 
     * @param path the path for the directory (may be absolute or relative to root directory)
     * @return the directory
     */
    public static File getExistingDirectory(String path)
    {
        File file = getBestFile(path);
        if (!file.exists())
        {
            file = getBestFile(workingDirectory.peek().getPath() + path);
            if (!file.exists())
            {
                throw new DeveloperError(String.format("Couldn't find \"%s\".", path), new FileNotFoundException(path));
            }
        }
        try
        {
            if (!file.isDirectory())
            {
                throw new DeveloperError(String.format("Could not find directory \"%s\".", path),
                        new FileAlreadyExistsAsFileException(file));
            }
            // otherwise...
            return file.getCanonicalFile();
        }
        catch (IOException e)
        {
            throw new DeveloperError(String.format("I/O Error creating the canonical form of file \"%s\".", path), e);
        }
    }

    /**
     * Opens and returns a FileInputStream for a given path. If the specified file doesn't exist, an exception will be
     * thrown.
     * 
     * @param path
     * @return a stream pointing to the given path
     * @throws FileNotFoundException if the file does not exist
     */
    public static FileInputStream getFileInputStream(String path) throws FileNotFoundException
    {
        File file = getExistingFile(path);
        return new FileInputStream(file);
    }

    /**
     * Opens and returns a FileOutputStream for a given path. If the specified file doesn't exist, an exception will be
     * thrown.
     * 
     * @param path the path to which to return a stream
     * @return a stream pointing to the given path
     * @throws FileNotFoundException if the file does not exist
     */
    public static FileOutputStream getFileOutputStream(String path) throws FileNotFoundException
    {
        File file = getExistingFile(path);
        return new FileOutputStream(file);
    }

    /**
     * Gets a FileWriter from a given path. First tries to use the path as-is if it's absolute, then (otherwise) looks
     * in the defined root directory.
     * 
     * @param path the path for the file (may be absolute or relative to root directory)
     * @param append if <tt>true</tt>, then bytes will be written to the end of the file rather than the beginning
     * @return the FileWriter
     * @throws IOException if the specified file is not found or if some other I/O error occurs
     */
    public static FileWriter getFileWriter(String path, boolean append) throws IOException
    {
        File file = getBestFile(path);
        return new FileWriter(file, append);
    }

    /**
     * Returns the absolute path. First checks whether the path as-is is absolute, then (otherwise) looks in the defined
     * root directory.
     * 
     * @param path the path for the file (may be absolute or relative to root directory)
     * @return the absolute path
     * @throws FileNotFoundException if a file with the given path cannot be located
     */
    public static String getAbsolutePath(String path) throws FileNotFoundException
    {
        File file = new File(path);
        if (file.isAbsolute())
        {
            return file.getAbsolutePath();
        }
        file = new File(root.getPath() + path);
        if (!file.exists())
        {
            throw new FileNotFoundException(String.format("Could not find \"%s\".", path));
        }
        return file.getAbsolutePath();
    }

    /**
     * Checks whether a file given by a path exists, and if not, creates it, along with any necessary subdirectories.
     * 
     * @param path denoting the file to create
     * @param description describes what the file is for, for trace messages. Should fit into a sentence like,
     *            &quot;created new <i>description </i>&quot;. May be null (which will result in less informative
     *            messages).
     * @return the file that is created (or retrieved)
     */
    public static File checkOrCreate(String path, String description)
    {
        File file = getBestFile(path);
        if (file.exists())
        {
            return file;
        }
        String _description = description;
        if (_description == null)
        {
            _description = "file";
        }

        try
        {
            file.createNewFile();
        }
        catch (IOException e)
        {
            // This may mean that some necessary directories don't exist.
            File directory = file.getParentFile();
            if (directory != null)
            {
                if (directory.mkdirs())
                {
                    try
                    {
                        file.createNewFile();
                    }
                    catch (IOException ee)
                    {
                        throw new UserError(String.format("Could not create %s \"%s\".", _description, path),
                                new CouldNotCreateFileException(file.getAbsolutePath()));
                    }
                }
                else
                {
                    throw new UserError(String.format("Could not create %s directory \"%s\".", _description, directory.getAbsolutePath()),
                            new CouldNotCreateFileException(directory.getAbsolutePath()));
                }
            }
            else
            {
                throw new UserError(String.format("Could not create %s directory for \"%s\".", _description, file.getAbsolutePath()),
                        new CouldNotCreateFileException(file.getAbsolutePath()));
            }
        }
        assert file.exists();
        LOGGER.info(String.format("Created new %s \"%s\".", _description, file.getAbsolutePath()));
        return file;
    }

    /**
     * Checks whether a file given by a path exists, and if not, creates it, along with any necessary subdirectories,
     * then returns a PrintWriter configured with it.
     * 
     * @param path denoting the file to create
     * @param description describes what the file is for, for trace messages. Should fit into a sentence like,
     *            &quot;created new <i>description </i>&quot;. May be null (which will result in less informative
     *            messages).
     * @return the file that is created (or retrieved)
     */
    public static PrintWriter checkOrCreatePrintWriter(String path, String description)
    {
        PrintWriter out = null;
        try
        {
            out = new PrintWriter(checkOrCreate(path, description));
        }
        catch (FileNotFoundException e)
        {
            throw new UserError(String.format("Could not find just-created %s \"%s\".", description, path), e);
        }
        return out;
    }
    
    /**
     * Checks whether a directory given by a path exists, and if not, creates it, along with any necessary
     * subdirectories.
     * 
     * @param path denoting the directory to create
     * @param description describes what the directory is for, for trace messages. Should fit into a sentence like,
     *            &quot;created new <i>description </i>&quot;. May be null (which will result in less informative
     *            messages).
     * @return the directory that is created (or retrieved)
     */
    public static File checkOrCreateDirectory(String path, String description)
    {
        File file = getBestFile(path);
        if (file.exists())
        {
            if (!file.isDirectory())
            {
                throw new UserError(new FileAlreadyExistsAsFileException(file));
            }
            // otherwise
            return file;
        }
        String _description = description;
        if (_description == null)
        {
            _description = "file";
        }

        if (!file.mkdirs())
        {
            throw new UserError(String.format("Could not create %s directory at \"%s\".", _description, path),
                    new CouldNotCreateFileException(file.getAbsolutePath()));
        }
        LOGGER.debug(String.format("Created new %s \"%s\".", _description, path));
        return file;
    }

    /**
     * Returns the entire contents of a file as a String.
     * 
     * @param path the path to the file (local file or URL)
     * @return the entire contents of a file as a String
     */
    public static String getFileContents(String path)
    {
        BufferedReader buffReader = null;
        InputStream stream = null;

        // Guess if this is a URL.
        if (path.indexOf("://") != -1)
        {
            // Try to create this as a URL.
            URL url = null;

            try
            {
                url = new URL(path);
            }
            catch (MalformedURLException e)
            {
                LOGGER.warn(String.format("Malformed URL: \"%s\"", path));
            }
            if (url == null)
            {
                throw new DeveloperError(String.format("Cannot create URL from path: \"%s\"", path), new NullPointerException());
            }

            try
            {
                String encoding = Characters.getDeclaredXMLEncoding(url);
                stream = url.openStream();
                buffReader = new BufferedReader(new InputStreamReader(stream, encoding));
            }
            catch (IOException e)
            {
                LOGGER.warn(String.format("I/O error trying to read \"%s\"", path));
            }
        }
        // Handle paths which are apparently files.
        else
        {
            File toRead = null;
            try
            {
                toRead = getExistingFile(path);
            }
            catch (FileNotFoundException e)
            {
                throw new UserError(new FileNotFoundException(path));
            }

            if (toRead.isAbsolute())
            {
                String parent = toRead.getParent();
                try
                {
                    workingDirectory.push(URLTools.createValidURL(parent));
                }
                catch (FileNotFoundException e)
                {
                    throw new DeveloperError(String.format("Created an invalid parent file: \"%s\".", parent), e);
                }
            }

            if (toRead.exists() && !toRead.isDirectory())
            {
                // The path may have been modified.
                String _path = toRead.getAbsolutePath();
                try
                {
                    String encoding = Characters.getDeclaredXMLEncoding(URLTools.createValidURL(_path));
                    stream = new FileInputStream(_path);
                    buffReader = new BufferedReader(new InputStreamReader(stream, encoding));
                }
                catch (IOException e)
                {
                    LOGGER.warn(String.format("I/O error trying to read \"%s\"", _path));
                    return null;
                }
            }
            else
            {
                assert toRead.exists() : "getExistingFile() returned a non-existent file";
                if (toRead.isDirectory())
                {
                    throw new UserError(new FileAlreadyExistsAsDirectoryException(toRead));
                }
            }
        }
        StringBuilder result = new StringBuilder();
        String line;
        if (buffReader != null && stream != null)
        {
          try
          {
              while ((line = buffReader.readLine()) != null)
              {
                  result.append(line);
              }
              buffReader.close();
              stream.close();
          }
          catch (IOException e)
          {
              LOGGER.warn(String.format("I/O error trying to read \"%s\"", path));
              return null;
          }
        }
        return result.toString();
    }

    /**
     * Expands a localized file name that may contain wildcards to an array of file names without wildcards. All file
     * separators in the file name must preceed any wildcard. The current directory is assumed to be the working
     * directory.
     * 
     * @param path
     * @return array of file names without wildcards
     * @throws FileNotFoundException if wild card is misused
     */
    public static List<File> glob(String path) throws FileNotFoundException
    {
        return glob(path, workingDirectory.peek().getPath());
    }

    /**
     * <p> Expands a localized file name that may contain wildcards to an array of file names without wildcards. All
     * file separators in the file name must preceed any wildcard. </p> <p> Adapted, with gratitude, from the <a
     * href="http://sourceforge.net/projects/jmk/">JMK </a> project. (Under the GNU LGPL) </p>
     * 
     * @param path the path string to glob
     * @param workingDirectoryToUse the path to which relative paths should be considered relative
     * @return array of file names without wildcards
     * @see <a href="http://sourceforge.net/projects/jmk/">JMK</a>
     * @throws FileNotFoundException if wild card is misused
     */
    public static List<File> glob(String path, String workingDirectoryToUse) throws FileNotFoundException
    {
        int wildCardIndex = path.indexOf('*');
        if (wildCardIndex < 0)
        {
            List<File> list = new ArrayList<File>(1);
            list.add(new File(path));
            return list;
        }
        // (otherwise...)
        int separatorIndex = path.lastIndexOf(File.separatorChar);
        // In case someone used a file separator char that doesn't belong to
        // this system....
        if (separatorIndex < 0)
        {
            separatorIndex = path.lastIndexOf('\\');
            if (separatorIndex < 0)
            {
                separatorIndex = path.lastIndexOf('/');
                if (separatorIndex < 0)
                {
                    // This is really going the extra mile....
                    separatorIndex = path.lastIndexOf(':');
                }
            }
        }
        if (separatorIndex > wildCardIndex)
        {
            throw new FileNotFoundException(String.format("Cannot expand %s", path));
        }
        String pattern;
        String dirName;
        File dir = null;
        if (separatorIndex >= 0)
        {
            pattern = path.substring(separatorIndex + 1);
            dirName = URLTools.unescape(path.substring(0, separatorIndex + 1));
            if (!dirName.startsWith(File.separator) && !dirName.startsWith(SLASH))
            {
                dir = new File(workingDirectory.peek().getPath() + dirName);
            }
            else
            {
                dir = new File(dirName);
            }
        }
        else
        {
            pattern = path;
            dirName = URLTools.unescape(workingDirectoryToUse);
            dir = new File(dirName);
            try
            {
                dir = dir.getCanonicalFile();
            }
            catch (IOException e)
            {
                throw new DeveloperError(String.format("Could not get canonical file for \"%s\".", dir
                        .getAbsolutePath()), e);
            }
        }
        if (!dir.isDirectory())
        {
            throw new FileNotFoundException(String.format("\"%s\" is not a valid directory path!", dirName));
        }
        String[] files = dir.list(new WildCardFilter(pattern, '*'));
        if (files == null)
        {
            return new ArrayList<File>();
        }
        List<File> list = new ArrayList<File>(files.length);
        for (int i = files.length; --i >= 0;)
        {
            list.add(new File(dirName + files[i]));
        }
        return list;
    }

    /**
     * Pushes a new working directory onto the stack.
     * 
     * @param path the directory path
     */
    @SuppressWarnings("boxing")
    public static void pushWorkingDirectory(URL path)
    {
        workingDirectory.push(path);
        if (LOGGER.isDebugEnabled())
        {
            LOGGER.debug(String.format("Pushed working directory \"%s\".  Stack size now: %,d", path, workingDirectory
                    .size()));
        }
    }

    /**
     * Pops a working directory off the stack.
     */
    @SuppressWarnings("boxing")
    public static void popWorkingDirectory()
    {
        URL popped;
        if (workingDirectory.size() > 1)
        {
            popped = workingDirectory.pop();
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug(String.format("Popped working directory \"%s\".  Stack size now: %,d", popped,
                        workingDirectory.size()));
            }
        }
        else
        {
            if (LOGGER.isDebugEnabled())
            {
                LOGGER.debug("No more working directories to pop.");
            }
        }
    }

    /**
     * Returns the working directory.
     * 
     * @return the working directory
     */
    public static URL getWorkingDirectory()
    {
        return workingDirectory.peek();
    }

    /**
     * Loads a file into a String.
     * 
     * @param path the path to the file
     * @return the loaded template
     * @throws FileNotFoundException
     */
    public static String loadFileAsString(String path) throws FileNotFoundException
    {
        return loadFileAsString(getExistingFile(path));
    }

    /**
     * Loads a file into a String.
     * 
     * @param file the file
     * @return the loaded template
     */
    public static String loadFileAsString(File file)
    {
        String templateLine;
        StringBuilder result = new StringBuilder(1000);

        BufferedReader reader;
        try
        {
            String encoding = Characters.getDeclaredXMLEncoding(file.toURI().toURL());
            reader = new BufferedReader(new InputStreamReader(new FileInputStream(file), encoding));
        }
        catch (IOException e)
        {
            return null;
        }

        try
        {
            while ((templateLine = reader.readLine()) != null)
            {
                result.append(templateLine);
            }
            reader.close();
        }
        catch (IOException e)
        {
            throw new UserError(String.format("I/O error reading \"%s\".", file.getAbsolutePath()), e);
        }

        return result.toString();
    }

    /**
     * Deletes the contents of the given directory (but not the directory itself).
     * 
     * @param directory
     */
    public static void deleteDirectoryContents(File directory)
    {
        File[] contents = directory.listFiles();
        for (int index = 0; index < contents.length; index++)
        {
            File file = contents[index];
            if (file.isDirectory())
            {
                deleteDirectoryContents(file);
                file.delete();
            }
            else
            {
                file.delete();
            }
        }
    }

    /**
     * Adds the given path to the given ZipOutputStream, omitting the given prefix from the ZipEntry created.
     * 
     * @param out
     * @param path
     * @param omitPrefix
     */
    public static void addToZip(ZipOutputStream out, URL path, String omitPrefix)
    {
        addToZip(out, path, omitPrefix, null);
    }

    /**
     * Adds the given path to the given ZipOutputStream, omitting the given prefix from the ZipEntry created.
     * 
     * @param out
     * @param path
     * @param omitPrefix
     * @param logger a logger to use in reporting errors like duplicate entries -- may be null
     */
    public static void addToZip(ZipOutputStream out, URL path, String omitPrefix, Logger logger)
    {
        byte[] buffer = new byte[1024];
        try
        {
            InputStream in = path.openStream();
            out.putNextEntry(new ZipEntry(URLTools.unescape(path.getPath().replace(omitPrefix, ""))));
            for (int length = 0; (length = in.read(buffer)) > 0;)
            {
                out.write(buffer, 0, length);
            }
            out.closeEntry();
            in.close();
        }
        catch (IOException e)
        {
            if (logger != null && e instanceof ZipException)
            {
                logger.warn(e);
                return;
            }
            throw new RuntimeException("Error adding file to zip file.", e);
        }
    }
    
    /**
     * If a file exists at the given source, moves to the given destination.
     * 
     * @param source
     * @param destination
     * @return whether the move succeeded
     */
    public static boolean move(URL source, URL destination)
    {
        File src = new File(source.getPath());
        if (!src.exists() || !src.canWrite())
        {
            return false;
        }
        File dest = new File(destination.getPath());
        if (dest.exists())
        {
            return false;
        }
        return src.renameTo(dest);
    }
}
