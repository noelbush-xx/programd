/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.configurations;

import java.io.FileNotFoundException;
import java.net.URL;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import org.aitools.programd.Core;
import org.aitools.programd.CoreShutdownHook;
import org.aitools.programd.interfaces.Console;
import org.aitools.programd.util.URLTools;

/**
 * A <code>SimpleConsole</code> gives you a running
 * {@link org.aitools.programd.Core Core} with a regular
 * {@link org.aitools.programd.interfaces.Console Console} and just a basic
 * {@link org.aitools.programd.interfaces.shell.Shell Shell} attached (if you enable
 * it). Input, output and error are routed via standard system objects.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.5
 */
public class SimpleConsole
{
    /** The Core to which this console will be attached. */
    private Core core;

    /** The console. */
    private Console console;

    private SimpleConsole(String corePropertiesPath) throws FileNotFoundException
    {
        URL baseURL = URLTools.createValidURL(System.getProperty("user.dir"));
        this.console = new Console();
        this.core = new Core(baseURL, URLTools.createValidURL(corePropertiesPath));
        this.console.attachTo(this.core);
    }

    /**
     * Starts the shell (if enabled) and sends the connect string.
     */
    public void run()
    {
        // Send the connect string.
        this.core.processResponse(this.core.getSettings().getConnectString());
        this.console.startShell();
    }

    private static void usage()
    {
        System.out.println("Usage: simple-console -c <CORE_CONFIG> -n <CONSOLE_CONFIG>");
        System.out.println("Start up a simple console version of Program D using the specified config files.");
        System.out.println();
        System.out.println("  -c, --core-properties     the path to the core configuration (XML properties) file");
        System.out.println();
        System.out.println("Report bugs to <programd@aitools.org>");
    }

    /**
     * Starts up the SimpleConsole configuration. Required arguments are:
     * <ul>
     * <li><code>-c, --core-properties     the path to the core configuration (XML properties) file</code></li>
     * </ul>
     * 
     * @param argv
     */
    public static void main(String[] argv)
    {
        String corePropertiesPath = null;

        int opt;
        LongOpt[] longopts = new LongOpt[1];
        longopts[0] = new LongOpt("core-properties", LongOpt.REQUIRED_ARGUMENT, null, 'c');

        Getopt getopt = new Getopt("simple-console", argv, ":c:n:", longopts);

        while ((opt = getopt.getopt()) != -1)
        {
            switch (opt)
            {
                case 'c':
                    corePropertiesPath = getopt.getOptarg();
                    break;
            }
        }

        if (corePropertiesPath == null)
        {
            System.err.println("You must specify a core properties path.");
            usage();
            System.exit(1);
        }

        SimpleConsole console = null;
        try
        {
            console = new SimpleConsole(corePropertiesPath);
        }
        catch (FileNotFoundException e)
        {
            System.err.println("Core properties file \"" + corePropertiesPath + "\" not found.");
        }
        // Add a shutdown hook so the Core will be properly shut down if the
        // system exits.
        Runtime.getRuntime().addShutdownHook(new CoreShutdownHook(console.core));
        console.run();
    }
}
