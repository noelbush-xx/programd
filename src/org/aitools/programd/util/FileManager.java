/*    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
    USA.
*/

package org.aitools.programd.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Stack;

import org.aitools.programd.util.logging.Log;

/**
 *  FileManager provides a standard interface for getting File objects and paths.
 */
public class FileManager
{
    /** The root path for running Program D. */
    private static String rootPath;

    /** The current working directory. */
    private static Stack workingDirectory = new Stack();
    static
    {
        workingDirectory.push(System.getProperty("user.dir"));
    }
    
    
    /**
     *  Sets the root path.
     *
     *  @param path the value to set for the root path
     */
    public static void setRootPath(String path)
    {
        rootPath = path;
    }
    

    /**
     *  Gets a file from a given path.  First tries to
     *  use the path as-is if it's absolute, then (otherwise)
     *  looks in the defined root directory.
     *
     *  @param path     the path for the file (may be absolute or relative to root directory)
     *
     *  @return the file (may not exist!)
     */
    public static File getFile(String path)
    {
        File file = new File(path);
        if (file.exists())
        {
            return file;
        }
        return new File(rootPath + path);
    }

    /**
     *  Gets a file from a given path.  First tries to
     *  use the path as-is if it's absolute, then (otherwise)
     *  looks in the defined root directory, then finally
     *  looks in the current working directory. The file <i>must</i>
     *  already exist, or an exception will be thrown.
     *
     *  @param path     the path for the file (may be absolute or relative to root directory)
     *
     *  @return the file
     *  @throws FileNotFoundException if the file does not exist
     */
    public static File getExistingFile(String path)
        throws FileNotFoundException
    {
        File file = getFile(path);
        if (!file.exists())
        {
            file = getFile(workingDirectory.peek() + File.separator + path);
            if (!file.exists())
            {
                throw new FileNotFoundException(
                    "Couldn't find \"" + path + "\".");
            }
        }
        return file;
    }
    
    
    /**
     *  Opens and returns a FileInputStream for a given path.
     *  If the specified file doesn't exist, an exception
     *  will be thrown.
     *
     *  @param path
     *
     *  @return a stream pointing to the given path
     *  @throws FileNotFoundException if the file does not exist
     */
    public static FileInputStream getFileInputStream(String path) throws FileNotFoundException
    {
        File file = getExistingFile(path);
        return new FileInputStream(file);
    }
    

    /**
     *  Opens and returns a FileOutputStream for a given path.
     *  If the specified file doesn't exist, an exception
     *  will be thrown.
     *
     *  @param path
     *
     *  @return a stream pointing to the given path
     *  @throws FileNotFoundException if the file does not exist
     */
    public static FileOutputStream getFileOutputStream(String path) throws FileNotFoundException
    {
        File file = getExistingFile(path);
        return new FileOutputStream(file);
    }
    

    /**
     *  Gets a FileWriter from a given path.  First tries to
     *  use the path as-is if it's absolute, then (otherwise)
     *  looks in the defined root directory.
     *
     *  @param path     the path for the file (may be absolute or relative to root directory)
     *  @param append   if <tt>true</tt>, then bytes will be written to the end of the file rather than the beginning
     * 
     *  @return the FileWriter
     *  @throws IOException if the specified file is not found or if some other I/O error occurs
     */
    public static FileWriter getFileWriter(String path, boolean append)
        throws IOException
    {
        File file = getFile(path);
        return new FileWriter(file, append);
    }
    /**
     *  Returns the absolute path.  First checks
     *  whether the path as-is is absolute, then (otherwise)
     *  looks in the defined root directory.
     *
     *  @param path     the path for the file (may be absolute or relative to root directory)
     *
     *  @return the absolute path
     *  @throws FileNotFoundException if a file with the given path cannot be located
     */
    public static String getAbsolutePath(String path)
        throws FileNotFoundException
    {
        File file = new File(path);
        if (file.isAbsolute())
        {
            return file.getAbsolutePath();
        }
        file = new File(rootPath + path);
        if (!file.exists())
        {
            throw new FileNotFoundException("Could not find \"" + path + "\".");
        }
        return file.getAbsolutePath();
    }

    /**
     *  Checks whether a file given by a path exists,
     *  and if not, creates it, along with any necessary
     *  subdirectories.
     *
     *  @param path         denoting the file to create
     *  @param description  describes what the file is for, for trace
     *                      messages. Should fit into a sentence like,
     *                      &quot;created new <i>description</i>&quot;.
     *                      May be null (which will result in less informative messages).
     *
     *  @return the file that is created (or retrieved)
     */
    public static File checkOrCreate(String path, String description)
    {
        File file = getFile(path);
        if (file.exists())
        {
            return file;
        }
        if (description == null)
        {
            description = "file";
        }

        try
        {
            file.createNewFile();
        }
        catch (IOException e0)
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
                    catch (IOException e1)
                    {
                        throw new UserError(
                            "Could not create "
                                + description
                                + " \""
                                + file.getAbsolutePath()
                                + "\".");
                    }
                }
                else
                {
                    throw new UserError(
                        "Could not create directory \""
                            + directory.getAbsolutePath()
                            + "\".");
                }
            }
            else
            {
                throw new UserError(
                    "Could not create " + description + " directory.");
            }
        }
        Trace.devinfo("Created new " + description + " \"" + path + "\".");
        return file;
    }

    /**
     *  Returns the entire contents of a file as a String.
     *
     *  @param path     the path to the file (local file or URL)
     *
     *  @return the entire contents of a file as a String
     */
    public static String getFileContents(String path)
    {
        BufferedReader buffReader = null;

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
                Log.userinfo("Malformed URL: \"" + path + "\"", Log.ERROR);
            }

            try
            {
                String encoding =
                    XMLKit.getDeclaredXMLEncoding(url.openStream());
                buffReader =
                    new BufferedReader(
                        new InputStreamReader(url.openStream(), encoding));
            }
            catch (IOException e)
            {
                Log.userinfo(
                    "I/O error trying to read \"" + path + "\"",
                    Log.ERROR);
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
                Log.userinfo(e.getMessage(), Log.ERROR);
                return null;
            }

            if (toRead.isAbsolute())
            {
                workingDirectory.push(toRead.getParent());
            }

            if (toRead.exists() && !toRead.isDirectory())
            {
                // The path may have been modified.
                path = toRead.getAbsolutePath();
                try
                {
                    String encoding =
                        XMLKit.getDeclaredXMLEncoding(
                            new FileInputStream(path));
                    buffReader =
                        new BufferedReader(
                            new InputStreamReader(
                                new FileInputStream(path),
                                encoding));
                }
                catch (IOException e)
                {
                    Log.userinfo(
                        "I/O error trying to read \"" + path + "\"",
                        Log.ERROR);
                    return null;
                }
            }
            else
            {
                if (!toRead.exists())
                {
                    throw new UserError("\"" + path + "\" does not exist!");
                }
                if (toRead.isDirectory())
                {
                    throw new UserError("\"" + path + "\" is a directory!");
                }
            }
        }
        StringBuffer result = new StringBuffer();
        String line;
        try
        {
            while ((line = buffReader.readLine()) != null)
            {
                result.append(line);
            }
            buffReader.close();
        }
        catch (IOException e)
        {
            Log.userinfo(
                "I/O error trying to read \"" + path + "\"",
                Log.ERROR);
            return null;
        }
        return result.toString();
    }

    /**
     *  Expands a localized file name that may contain wildcards
     *  to an array of file names without wildcards.
     *  All file separators in the file name must preceed any wildcard.
     *  The current directory is assumed to be the working directory.
     *  
     *  @param path
     *
     *  @return array of file names without wildcards
     *
     *  @throws FileNotFoundException if wild card is misused
     */
    public static String[] glob(String path) throws FileNotFoundException
    {
        return glob(path, (String) workingDirectory.peek());
    }

    /**
     *  <p>
     *  Expands a localized file name that may contain wildcards
     *  to an array of file names without wildcards.
     *  All file separators in the file name must preceed any wildcard.
     *  </p>
     *  <p>
     *  Adapted, with gratitude, from the
     *  <a href="http://sourceforge.net/projects/jmk/">JMK</a>
     *  project.  (Under the GNU LGPL)
     *  </p>
     *
     *  @param path             localized file name that may contain wildcards
     *  @param workingDirectory the path to which relative paths should be considered relative
     *
     *  @return array of file names without wildcards
     *
     *  @author John D. Ramsdell
     *  @see <a href="http://sourceforge.net/projects/jmk/">JMK</a>
     *
     *  @throws FileNotFoundException if wild card is misused
     */
    public static String[] glob(String path, String workingDirectoryToUse)
        throws FileNotFoundException
    {
        int wildCardIndex = path.indexOf('*');
        if (wildCardIndex < 0)
        {
            return new String[] { path };
        }
        // (otherwise...)
        int separatorIndex = path.lastIndexOf(File.separatorChar);
        // In case someone used a file separator char that doesn't belong to this system....
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
            throw new FileNotFoundException("Cannot expand " + path);
        }
        String pattern;
        String dirName;
        File dir;
        if (separatorIndex >= 0)
        {
            pattern = path.substring(separatorIndex + 1);
            dirName = path.substring(0, separatorIndex + 1);
            dir = new File(dirName);
            if (!dir.isDirectory())
            {
                dir = new File(workingDirectory + File.separator + dirName);
            }
        }
        else
        {
            pattern = path;
            dirName = workingDirectoryToUse;
            dir = new File(dirName);
        }
        if (!dir.isDirectory())
        {
            throw new UserError(
                "\"" + dirName + "\" is not a valid directory path!");
        }
        String[] list = dir.list(new WildCardFilter(pattern, '*'));
        if (list == null)
        {
            return new String[0];
        }
        for (int i = list.length; --i >= 0;)
        {
            list[i] = dirName + File.separator + list[i];
        }
        return list;
    }

    /**
     *  Pushes a new working directory onto the stack.
     *
     *  @param path the directory path
     */
    public static void pushWorkingDirectory(String path)
    {
        workingDirectory.push(path);
    }

    /**
     *  Pops a working directory off the stack.
     */
    public static void popWorkingDirectory()
    {
        if (workingDirectory.size() > 1)
        {
            workingDirectory.pop();
        }
    }

    /**
     *  Returns the working directory.
     *
     *  @return the working directory
     */
    public static String getWorkingDirectory()
    {
        return (String) workingDirectory.peek();
    }
}
