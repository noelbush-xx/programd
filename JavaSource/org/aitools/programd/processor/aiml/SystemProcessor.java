/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.processor.aiml;

import org.w3c.dom.Element;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.aitools.programd.Core;
import org.aitools.programd.CoreSettings;
import org.aitools.programd.parser.TemplateParser;
import org.aitools.programd.processor.ProcessorException;
import org.aitools.programd.util.FileManager;

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
 * @version 4.5
 * @author Jon Baer
 * @author Mark Anacker
 * @author Thomas Ringate, Pedro Colla
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class SystemProcessor extends AIMLProcessor
{
    /** The label (as required by the registration scheme). */
    public static final String label = "system";

    /**
     * Known names of operating systems which tend to require the
     * array form of ProcessBuilder().
     */
    private static final String[] arrayFormOSnames = { "mac os x", "linux", "solaris", "sunos", "mpe", "hp-ux", "pa_risc", "aix", "freebsd", "irix",
            "unix", "windows xp" };

    /** For convenience, the system line separator. */
    protected static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

    /** Whether to use the array form of Runtime.exec(). */
    private static boolean useArrayExecForm;

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

    /** A regex matching any amount of whitespace (for splitting strings into words). */
    private static final String STRING_SPLIT_REGEX = "\\s";

    /**
     * Creates a new SystemProcessor using the given Core.
     * 
     * @param coreToUse the Core object to use
     */
    public SystemProcessor(Core coreToUse)
    {
        super(coreToUse);
    }

    /**
     * @see AIMLProcessor#process(Element, TemplateParser)
     */
    @Override
    public String process(Element element, TemplateParser parser) throws ProcessorException
    {
        CoreSettings coreSettings = parser.getCore().getSettings();

        // Don't use the system tag if not permitted.
        if (!coreSettings.osAccessAllowed())
        {
            logger.warn("Use of <system> prohibited!");
            return EMPTY_STRING;
        }

        String directoryPath = coreSettings.getSystemInterpreterDirectory().getPath();
        String prefix = coreSettings.getSystemInterpreterPrefix();

        String commandLine = parser.evaluate(element.getChildNodes());
        if (prefix != null)
        {
            commandLine = prefix + commandLine;
        }
        String output = EMPTY_STRING;
        commandLine = commandLine.trim();
        logger.debug("<system> call: " + commandLine);
        if (directoryPath == null || EMPTY_STRING.equals(directoryPath))
        {
            logger.error("No programd.interpreter.system.directory defined!");
            return EMPTY_STRING;
        }
        File directory = FileManager.getExistingDirectory(directoryPath);
        logger.debug("Executing <system> call in \"" + directory.getPath() + "\"");
        ProcessBuilder processBuilder = null;
        if (useArrayExecForm)
        {
        	processBuilder = new ProcessBuilder(commandLine.split(STRING_SPLIT_REGEX));
        }
        else
        {
        	processBuilder = new ProcessBuilder(commandLine);
        }
        processBuilder.directory(directory);
        Process child = null;
        try
        {
        	child = processBuilder.start();
        }
        catch (IOException e)
        {
            logger.warn(String.format("Error executing <system> command \"%s\"", commandLine), e);
            return EMPTY_STRING;
        }
        if (child == null)
        {
            logger.error("Could not get separate process for <system> command.");
            return EMPTY_STRING;
        }

        try
        {
            child.waitFor();
        }
        catch (InterruptedException e)
        {
            logger.error("System process interruped; could not complete.");
            return EMPTY_STRING;
        }

        try
        {
            InputStream in = child.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = br.readLine()) != null)
            {
                output = output + line + "\n";
            }

            logger.debug("output: " + output);

            commandLine = output;
            in.close();
            logger.debug("System process exit value: " + child.exitValue());
        }
        catch (IOException e)
        {
            logger.warn(String.format("Error reading result of <system> command \"%s\"", commandLine), e);
        }

        return commandLine.trim();
    }
}