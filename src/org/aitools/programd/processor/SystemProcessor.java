/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor;

import org.w3c.dom.Element;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.aitools.programd.Core;
import org.aitools.programd.CoreSettings;
import org.aitools.programd.parser.TemplateParser;
import org.aitools.programd.util.FileManager;
import org.aitools.programd.util.StringKit;

/**
 * <p>
 * Handles a
 * <code><a href="http://aitools.org/aiml/TR/2001/WD-aiml/#section-system">system</a></code>
 * element.
 * </p>
 * <p>
 * No attempt is made to check whether the command passed to the OS interpreter
 * is harmful.
 * </p>
 * 
 * @version 4.2
 * @author Jon Baer
 * @author Mark Anacker
 * @author Thomas Ringate, Pedro Colla
 * @author Noel Bush
 */
public class SystemProcessor extends AIMLProcessor
{
    public static final String label = "system";

    /**
     * Known names of Unix-like operating systems, which tend to require the array
     * form of Runtime.exec().
     */
    private static final String[] arrayFormOSnames = { "mac os x", "linux", "solaris", "sunos", "mpe", "hp-ux",
            "pa_risc", "aix", "freebsd", "irix", "unix" };

    /** For convenience, the system line separator. */
    protected static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

    /** Whether to use the array form of Runtime.exec(). */
    private static boolean useArrayExecForm;
    
    private static final Logger errorLogger = Logger.getLogger("programd.error");

    private static final Logger systemLogger = Logger.getLogger("programd.system");

    /**
     * Tries to guess whether to use the array form of Runtime.exec().
     */
    static
    {
        String os = System.getProperty("os.name").toLowerCase();
        for (int index = arrayFormOSnames.length; --index >= 0;)
        {
            if (os.indexOf(arrayFormOSnames[index]) != -1)
            {
                useArrayExecForm = true;
            }
        }
    }

    public SystemProcessor(Core coreToUse)
    {
        super(coreToUse);
    }
    
    public String process(Element element, TemplateParser parser)
    {
        CoreSettings coreSettings = parser.getCore().getSettings();
        
        // Don't use the system tag if not permitted.
        if (!coreSettings.osAccessAllowed())
        {
            errorLogger.log(Level.WARNING, "Use of <system> prohibited!");
            return EMPTY_STRING;
        }

        String directoryPath = coreSettings.getSystemInterpreterDirectory();
        String prefix = coreSettings.getSystemInterpreterPrefix();

        String response = parser.evaluate(element.getChildNodes());
        if (prefix != null)
        {
            response = prefix + response;
        }
        String output = EMPTY_STRING;
        response = response.trim();
        systemLogger.log(Level.FINEST, "<system> call:" + LINE_SEPARATOR + response);
        try
        {
            File directory = null;
            if (directoryPath != null)
            {
                systemLogger.log(Level.FINEST, "Executing <system> call in \"" + directoryPath + "\"");
                directory = FileManager.getFile(directoryPath);
                if (!directory.isDirectory())
                {
                    systemLogger.log(Level.WARNING, "programd.system-interpreter.directory (\"" + directoryPath
                            + "\") does not exist or is not a directory.");
                    return EMPTY_STRING;
                }
            }
            else
            {
                systemLogger.log(Level.SEVERE, "No programd.interpreter.system.directory defined!");
                return EMPTY_STRING;
            }
            Process child;
            if (useArrayExecForm)
            {
                child = Runtime.getRuntime().exec(StringKit.wordSplit(response).toArray(new String[] {}), null,
                        directory);
            }
            else
            {
                child = Runtime.getRuntime().exec(response, null, directory);
            }
            if (child == null)
            {
                systemLogger.log(Level.SEVERE, "Could not get separate process for <system> command.");
                return EMPTY_STRING;
            }

            try
            {
                child.waitFor();
            }
            catch (InterruptedException e)
            {
                systemLogger.log(Level.SEVERE, "System process interruped; could not complete.");
                return EMPTY_STRING;
            }

            InputStream in = child.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null)
            {
                output = output + line + "\n";
            }

            systemLogger.log(Level.FINEST, "output:" + LINE_SEPARATOR + output);

            response = output;
            in.close();
            systemLogger.log(Level.FINEST, "System process exit value: " + child.exitValue());
        }
        catch (IOException e)
        {
            systemLogger.log(Level.WARNING, "Cannot execute <system> command:" + LINE_SEPARATOR + e.getMessage());
        }

        return response.trim();
    }
}