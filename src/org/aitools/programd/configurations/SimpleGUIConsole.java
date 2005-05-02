/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.configurations;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import org.aitools.programd.Core;
import org.aitools.programd.CoreShutdownHook;
import org.aitools.programd.interfaces.graphical.GUIConsole;

/**
 * A <code>SimpleGUIConsole</code> gives you running
 * {@link org.aitools.programd.Core Core} attached to a GUI-based
 * {@link org.aitools.programd.interfaces.Console Console}, including a basic
 * {@link org.aitools.programd.interfaces.shell.Shell Shell} attached (if you enable
 * it).
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.2
 */
public class SimpleGUIConsole
{
    /** The Core to which this console will be attached. */
    private Core core;

    /** The console. */
    private GUIConsole console;

    private SimpleGUIConsole(String corePropertiesPath, String consolePropertiesPath)
    {
        this.core = new Core(corePropertiesPath);
        this.console = new GUIConsole(consolePropertiesPath);
        this.console.attachTo(this.core);
        this.core.setup();
    }

    /**
     * Starts the core and the shell (if enabled) and sends the connect string.
     */
    public void run()
    {
        this.core.start();
        // Send the connect string.
        this.core.processResponse(this.core.getSettings().getConnectString());
        this.console.startShell();
    }

    private static void usage()
    {
        System.out.println("Usage: simple-gui-console -c <CORE_CONFIG> -n <CONSOLE_CONFIG>");
        System.out.println("Start up a simple console version of Program D using the specified config files.");
        System.out.println();
        System.out.println("  -c, --core-properties     the path to the core configuration (XML properties) file");
        System.out.println("  -n, --console-properties  the path to the console configuration (XML properties) file");
        System.out.println();
        System.out.println("Report bugs to <programd@aitools.org>");
    }

    /**
     * Starts up the SimpleGUIConsole configuration. Required arguments are:
     * <ul>
     * <li><code>-c, --core-properties     the path to the core configuration (XML properties) file</code></li>
     * <li><code>-n, --console-properties  the path to the console configuration (XML properties) file</code></li>
     * </ul>
     * 
     * @param argv
     */
    public static void main(String[] argv)
    {
        String corePropertiesPath = null;
        String consolePropertiesPath = null;

        int opt;
        LongOpt[] longopts = new LongOpt[2];
        longopts[0] = new LongOpt("core-properties", LongOpt.REQUIRED_ARGUMENT, null, 'c');
        longopts[1] = new LongOpt("console-properties", LongOpt.REQUIRED_ARGUMENT, null, 'n');

        Getopt getopt = new Getopt("simple-gui-console", argv, ":c:n:", longopts);

        while ((opt = getopt.getopt()) != -1)
        {
            switch (opt)
            {
                case 'c':
                    corePropertiesPath = getopt.getOptarg();
                    break;

                case 'n':
                    consolePropertiesPath = getopt.getOptarg();
                    break;
            }
        }

        if (corePropertiesPath == null)
        {
            System.err.println("You must specify a core properties path.");
            usage();
            System.exit(1);
        }

        if (consolePropertiesPath == null)
        {
            System.err.println("You must specify a console properties path.");
            usage();
            System.exit(1);
        }

        SimpleGUIConsole console = new SimpleGUIConsole(corePropertiesPath, consolePropertiesPath);
        // Add a shutdown hook so the Core will be properly shut down if the
        // system exits.
        Runtime.getRuntime().addShutdownHook(new CoreShutdownHook(console.core));
        console.run();
    }
}
