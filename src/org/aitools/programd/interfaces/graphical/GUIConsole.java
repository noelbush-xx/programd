/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.interfaces.graphical;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URL;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.SwingConstants;
import javax.swing.UnsupportedLookAndFeelException;

import org.aitools.programd.Core;
import org.aitools.programd.graph.Graphmaster;
import org.aitools.programd.interfaces.Console;
import org.aitools.programd.interfaces.shell.BotListCommand;
import org.aitools.programd.interfaces.shell.HelpCommand;
import org.aitools.programd.interfaces.shell.ListBotFilesCommand;
import org.aitools.programd.interfaces.shell.NoSuchCommandException;
import org.aitools.programd.interfaces.shell.Shell;
import org.aitools.programd.util.DeveloperError;

/**
 * Provides a very simple GUI console for the bot.
 * 
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 * @since 4.1.5
 * @version 4.5
 */
public class GUIConsole extends JPanel
{
    /** The core associated with this console. */
    private Core core;

    /** The underlying Console. */
    protected Console console;

    /** The Shell that will (may) be used by the underlying console. */
    protected Shell shell;

    /** The logger. */
    protected static Logger logger = Logger.getLogger("programd");

    /** Where console messages will be displayed. */
    protected JTextArea display;

    /** Contains the input prompt and field. */
    protected InputPanel inputPanel;

    protected ConsoleDisplayStream outDisplay = new ConsoleDisplayStream(this);

    protected ConsoleDisplayStream errDisplay = new ConsoleDisplayStream(this);

    private JFrame frame;

    /** The stream to which console stdout will be directed. */
    private PrintStream outStream = new PrintStream(this.outDisplay);

    /** The stream to which console stdout will be directed. */
    private PrintStream errStream = new PrintStream(this.errDisplay);

    /** The stream to which console prompt will be directed. */
    private PrintStream promptStream = new PrintStream(new ConsolePromptStream(this));

    /** The stream which will receive console input. */
    protected ConsoleInputStream inStream = new ConsoleInputStream();

    /** For convenience, the system line separator. */
    protected static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

    private static final Object[] HELP_MESSAGE = { "Simple Console for", "Program D version " + Core.VERSION };

    private static JMenuBar menuBar;

    private static String LOGO_PATH = "org/aitools/programd/gui/icons/logo.jpg";

    private ImageIcon logo;

    private static String ICON_PATH = "org/aitools/programd/gui/icons/icon.jpg";

    private ImageIcon icon;

    /**
     * Constructs a new simple console gui with a new shell.
     * 
     * @param consolePropertiesPath the path to the console properties file
     */
    public GUIConsole(String consolePropertiesPath)
    {
        try
        {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        }
        catch (ClassNotFoundException e)
        {
            throw new DeveloperError("The LookAndFeel class could not be found.", e);
        }
        catch (InstantiationException e)
        {
            throw new DeveloperError("A new instance of the LookAndFeel class couldn't be created.", e);
        }
        catch (IllegalAccessException e)
        {
            throw new DeveloperError("The requested LookAndFeel class or initializer isn't accessible;", e);
        }
        catch (UnsupportedLookAndFeelException e)
        {
            throw new DeveloperError("The requested LookAndFeel is not supported.", e);
        }

        URL logoURL = ClassLoader.getSystemResource(LOGO_PATH);
        if (logoURL != null)
        {
            this.logo = new ImageIcon(logoURL);
        }
        else
        {
            throw new DeveloperError("Logo is missing from \"" + LOGO_PATH + "\"!", new NullPointerException());
        }

        URL iconURL = ClassLoader.getSystemResource(ICON_PATH);
        if (iconURL != null)
        {
            this.icon = new ImageIcon(iconURL);
        }
        else
        {
            throw new DeveloperError("Icon is missing from \"" + ICON_PATH + "\"!", new NullPointerException());
        }

        this.console = new Console(consolePropertiesPath, this.outStream, this.errStream);

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.display = new JTextArea(40, 90);
        this.display.setFont(new Font("Monospaced", Font.PLAIN, 12));
        this.display.setLineWrap(true);
        this.display.setWrapStyleWord(true);
        this.display.setTabSize(4);
        this.display.setForeground(Color.black);
        this.display.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(this.display);
        scrollPane.setAlignmentY(Component.CENTER_ALIGNMENT);

        this.inputPanel = new InputPanel(this);
        this.inputPanel.setEnabled(false);

        this.add(scrollPane);
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.add(Box.createRigidArea(new Dimension(0, 5)));
        this.add(this.inputPanel);

        menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        // fileMenu.setFont(new Font("Sans-serif", Font.PLAIN, 12));
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem loadAIMLURL = new JMenuItem("Load AIML from URL...");
        // loadAIMLURL.setFont(new Font("Sans-serif", Font.PLAIN, 12));
        loadAIMLURL.setMnemonic(KeyEvent.VK_U);
        loadAIMLURL.addActionListener(new ActionEventIgnoringActionListener()
        {
            public void actionPerformed()
            {
                loadAIMLURLBox();
            }
        });

        JMenuItem loadAIMLFilePath = new JMenuItem("Load AIML from file path...");
        // loadAIMLFilePath.setFont(new Font("Sans-serif", Font.PLAIN, 12));
        loadAIMLFilePath.setMnemonic(KeyEvent.VK_P);
        loadAIMLFilePath.addActionListener(new ActionEventIgnoringActionListener()
        {
            public void actionPerformed()
            {
                loadAIMLFilePathChooser();
            }
        });

        JMenuItem shutdown = new JMenuItem("Shutdown Program D");
        // shutdown.setFont(new Font("Sans-serif", Font.PLAIN, 12));
        shutdown.setMnemonic(KeyEvent.VK_X);
        shutdown.addActionListener(new ActionEventIgnoringActionListener()
        {
            public void actionPerformed()
            {
                shutdown();
            }
        });

        JMenuItem quit = new JMenuItem("Quit");
        // quit.setFont(new Font("Sans-serif", Font.PLAIN, 12));
        quit.setMnemonic(KeyEvent.VK_X);
        quit.addActionListener(new ActionEventIgnoringActionListener()
        {
            public void actionPerformed()
            {
                quit();
            }
        });
        fileMenu.add(loadAIMLURL);
        fileMenu.add(loadAIMLFilePath);
        fileMenu.addSeparator();
        fileMenu.add(shutdown);
        fileMenu.addSeparator();
        fileMenu.add(quit);

        // Create the Actions menu.
        JMenu actionsMenu = new JMenu("Actions");
        // actionsMenu.setFont(new Font("Sans-serif", Font.PLAIN, 12));
        actionsMenu.setMnemonic(KeyEvent.VK_A);

        JCheckBoxMenuItem pause = new JCheckBoxMenuItem("Pause Console");
        // pause.setFont(new Font("Sans-serif", Font.PLAIN, 12));
        pause.setMnemonic(KeyEvent.VK_P);
        pause.addActionListener(new ActionEventIgnoringActionListener()
        {
            public void actionPerformed()
            {
                GUIConsole.this.outDisplay.togglePause();
                GUIConsole.this.errDisplay.togglePause();
            }
        });

        JMenuItem talkToBot = new JMenuItem("Talk to bot...");
        // talkToBot.setFont(new Font("Sans-serif", Font.PLAIN, 12));
        talkToBot.setMnemonic(KeyEvent.VK_B);
        talkToBot.addActionListener(new ActionEventIgnoringActionListener()
        {
            public void actionPerformed()
            {
                chooseBot();
            }
        });

        JMenuItem botFiles = new JMenuItem("List bot files");
        // botFiles.setFont(new Font("Sans-serif", Font.PLAIN, 12));
        botFiles.setMnemonic(KeyEvent.VK_F);
        botFiles.addActionListener(new ActionEventIgnoringActionListener()
        {
            public void actionPerformed()
            {
                try
                {
                    GUIConsole.this.shell.processCommandLine(ListBotFilesCommand.COMMAND_STRING);
                }
                catch (NoSuchCommandException e)
                {
                    throw new DeveloperError("GUIConsole sent an invalid command string to the shell!", e);
                }
            }
        });

        JMenuItem listBots = new JMenuItem("List bots");
        // listBots.setFont(new Font("Sans-serif", Font.PLAIN, 12));
        listBots.setMnemonic(KeyEvent.VK_L);
        listBots.addActionListener(new ActionEventIgnoringActionListener()
        {
            public void actionPerformed()
            {
                try
                {
                    GUIConsole.this.shell.processCommandLine(BotListCommand.COMMAND_STRING);
                }
                catch (NoSuchCommandException e)
                {
                    throw new DeveloperError("GUIConsole sent an invalid command string to the shell!", e);
                }
            }
        });

        actionsMenu.add(pause);
        actionsMenu.addSeparator();
        actionsMenu.add(talkToBot);
        actionsMenu.add(listBots);
        actionsMenu.add(botFiles);
        actionsMenu.addSeparator();

        // Create the Help menu.
        JMenu helpMenu = new JMenu("Help");
        // helpMenu.setFont(new Font("Sans-serif", Font.PLAIN, 12));
        helpMenu.setMnemonic(KeyEvent.VK_H);

        JMenuItem shellHelp = new JMenuItem("Shell Help...");
        // shellHelp.setFont(new Font("Sans-serif", Font.PLAIN, 12));
        shellHelp.setMnemonic(KeyEvent.VK_H);
        shellHelp.addActionListener(new ActionEventIgnoringActionListener()
        {
            public void actionPerformed()
            {
                try
                {
                    GUIConsole.this.shell.processCommandLine(HelpCommand.COMMAND_STRING);
                }
                catch (NoSuchCommandException e)
                {
                    throw new DeveloperError("GUIConsole sent an invalid command string to the shell!", e);
                }
            }
        });
        JMenuItem about = new JMenuItem("About Simple GUI Console...");
        // about.setFont(new Font("Sans-serif", Font.PLAIN, 12));
        about.setMnemonic(KeyEvent.VK_A);
        about.addActionListener(new ActionEventIgnoringActionListener()
        {
            public void actionPerformed()
            {
                showAboutBox();
            }
        });

        helpMenu.add(about);
        helpMenu.add(shellHelp);

        // Add menus to the menu bar.
        menuBar.add(fileMenu);
        menuBar.add(actionsMenu);
        menuBar.add(helpMenu);

        this.frame = new JFrame();
        this.frame.setTitle("Program D Simple GUI Console");
        this.frame.getContentPane().add(this);
        this.frame.setJMenuBar(menuBar);
        this.frame.setIconImage(this.icon.getImage());
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.pack();
        this.frame.setLocation(50, 50);
        this.frame.setVisible(true);
    }

    /**
     * Attaches the GUIConsole to the given Core.
     * 
     * @param coreToUse the Core to which to attach
     */
    public void attachTo(Core coreToUse)
    {
        if (this.console.getSettings().useShell())
        {
            this.shell = new Shell(this.console.getSettings(), this.inStream, this.outStream, this.errStream, this.promptStream);
            this.console.addShell(this.shell, coreToUse);
        }
        else
        {
            this.outStream.println("Interactive shell disabled.  Awaiting manual shut down.");
        }
    }

    /**
     * Starts the attached Core.
     */
    public void start()
    {
        this.core.start();
    }

    protected void shutdown()
    {
        if (this.core != null)
        {
            this.core.shutdown();
        }
        // Let the user exit, in case termination was abnormal or messages are
        // otherwise interesting.
    }

    protected void quit()
    {
        this.frame.dispose();
    }

    class InputPanel extends JPanel
    {
        /** Where the console prompt will be displayed. */
        protected JLabel prompt;

        /** The console input field. */
        protected JTextField input;

        /** The enter button. */
        protected JButton enter;

        protected GUIConsole parent;

        /**
         * Creates a new InputPanel.
         * 
         * @param parentToUse the parent GUIConsole to use
         */
        public InputPanel(GUIConsole parentToUse)
        {
            this.parent = parentToUse;

            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

            this.prompt = new JLabel();
            this.prompt.setFont(new Font("Monospaced", Font.PLAIN, 12));
            this.prompt.setForeground(Color.black);
            this.prompt.setBackground(Color.white);
            this.prompt.setHorizontalAlignment(SwingConstants.LEFT);
            this.prompt.setAlignmentY(Component.CENTER_ALIGNMENT);

            this.input = new JTextField();
            this.input.setFont(new Font("Monospaced", Font.PLAIN, 12));
            this.input.setForeground(Color.black);
            this.input.setMinimumSize(new Dimension(50, 20));
            this.input.setPreferredSize(new Dimension(200, 20));
            this.input.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
            this.input.setHorizontalAlignment(SwingConstants.LEFT);
            this.input.setAlignmentY(Component.CENTER_ALIGNMENT);
            this.input.addActionListener(new InputSender());

            this.enter = new JButton("Enter");
            this.enter.setFont(new Font("Sans-serif", Font.PLAIN, 10));
            this.enter.setForeground(Color.black);
            this.enter.setMinimumSize(new Dimension(70, 20));
            this.enter.setPreferredSize(new Dimension(70, 20));
            this.enter.setMaximumSize(new Dimension(70, 20));
            this.enter.addActionListener(new InputSender());
            this.enter.setAlignmentY(Component.CENTER_ALIGNMENT);

            this.add(this.prompt);
            this.add(this.input);
            this.add(this.enter);
        }

        /**
         * Sets the prompt.
         * 
         * @param text the text of the prompt
         */
        public void setPrompt(String text)
        {
            this.prompt.setText(text);
            this.prompt.revalidate();
            this.input.requestFocus();
        }

        /**
         * Sets the components of this panel to the given state.
         * 
         * @param enabled whether or not the panel should be enabled
         */
        public void setEnabled(boolean enabled)
        {
            this.input.setEnabled(enabled);
            this.enter.setEnabled(enabled);
        }

        private class InputSender implements ActionListener
        {
            /**
             * @see ActionListener#actionPerformed(java.awt.event.ActionEvent)
             */
            public void actionPerformed(ActionEvent ae)
            {
                String inputText = ae.getActionCommand();
                GUIConsole.this.display.append(InputPanel.this.prompt.getText() + inputText + LINE_SEPARATOR);
                GUIConsole.this.inStream.receive(inputText);
                InputPanel.this.input.setText(null);
            }
        }
    }

    /**
     * Extends OutputStream to direct all output to the display textarea.
     */
    public class ConsoleDisplayStream extends OutputStream
    {
        private boolean paused = false;

        protected GUIConsole parent;

        /**
         * Creates a new ConsoleDisplayStream.
         * 
         * @param parentToUse the GUIConsole parent to use
         */
        public ConsoleDisplayStream(GUIConsole parentToUse)
        {
            super();
            this.parent = parentToUse;
        }

        /**
         * @see java.io.OutputStream#write(byte[], int, int)
         */
        public void write(byte[] b, int off, int len)
        {
            while (this.paused)
            {
                try
                {
                    Thread.sleep(500);
                }
                catch (InterruptedException e)
                {
                    // Nothing to do.
                }
            }
            GUIConsole.this.display.append(new String(b, off, len));
            GUIConsole.this.display.setCaretPosition(GUIConsole.this.display.getText().length());
        }

        /**
         * @see java.io.OutputStream#write(int)
         */
        public void write(int b)
        {
            while (this.paused)
            {
                try
                {
                    Thread.sleep(500);
                }
                catch (InterruptedException e)
                {
                    // Do nothing.
                }
            }
            GUIConsole.this.display.append(String.valueOf((char) b));
            GUIConsole.this.display.setCaretPosition(GUIConsole.this.display.getText().length());
        }

        protected void togglePause()
        {
            this.paused = !this.paused;
        }
    }

    /**
     * Extends OutputStream to direct all output to the prompt field.
     */
    public class ConsolePromptStream extends OutputStream
    {
        protected GUIConsole parent;

        /**
         * @param parentToUse
         */
        public ConsolePromptStream(GUIConsole parentToUse)
        {
            super();
            this.parent = parentToUse;
        }

        /**
         * @see java.io.OutputStream#write(byte[], int, int)
         */
        public void write(byte[] b, int off, int len)
        {
            GUIConsole.this.inputPanel.setPrompt(new String(b, off, len));
        }

        /**
         * @see java.io.OutputStream#write(int)
         */
        public void write(int b)
        {
            GUIConsole.this.inputPanel.setPrompt(String.valueOf((char) b));
        }
    }

    /**
     * Extends InputStream to suit our purposes in handling user input for the
     * GUIConsole.
     * 
     * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
     */
    public class ConsoleInputStream extends InputStream
    {
        byte[] content = new byte[] {};

        private int mark = 0;

        /**
         * Creates a new ConsoleInputStream object.
         */
        public ConsoleInputStream()
        {
            // Nothing to do.
        }

        /**
         * Receives the given string.
         * 
         * @param string the string to receive
         */
        public void receive(String string)
        {
            this.content = (string + '\n').getBytes();
            this.mark = 0;
        }

        /**
         * @see java.io.InputStream#read(byte[], int, int)
         */
        public int read(byte b[], int off, int len)
        {
            while (this.mark >= this.content.length)
            {
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e)
                {
                    return -1;
                }
            }
            if (b == null)
            {
                throw new NullPointerException();
            }
            else if ((off < 0) || (off > b.length) || (len < 0) || ((off + len) > b.length) || ((off + len) < 0))
            {
                throw new IndexOutOfBoundsException();
            }
            else if (len == 0)
            {
                return 0;
            }
            else if (this.content.length == 0)
            {
                return -1;
            }

            int i = 1;
            b[off] = this.content[this.mark++];
            for (; i < len && i < this.content.length; i++)
            {

                b[off + i] = this.content[this.mark++];
            }
            return i;
        }

        /**
         * @see java.io.InputStream#available()
         */
        public int available()
        {
            return this.content.length - this.mark - 1;
        }

        /**
         * @see java.io.InputStream#markSupported()
         */
        public boolean markSupported()
        {
            return false;
        }

        /**
         * @see java.io.InputStream#read()
         */
        public int read()
        {
            while (this.mark >= this.content.length)
            {
                try
                {
                    Thread.sleep(100);
                }
                catch (InterruptedException e)
                {
                    return -1;
                }
            }
            if (this.mark < this.content.length)
            {
                return this.content[this.mark++];
            }
            // (otherwise...)
            return -1;
        }

    }

    protected void loadAIMLURLBox()
    {
        Object response = JOptionPane.showInputDialog(null, "Enter the URL from which to load.", "Load AIML from URL", JOptionPane.PLAIN_MESSAGE,
                null, null, null);
        if (response == null)
        {
            return;
        }

        Graphmaster graphmaster = this.core.getGraphmaster();
        int categories = graphmaster.getTotalCategories();
        graphmaster.load((String) response, this.shell.getCurrentBotID());
        Logger.getLogger("programd").log(Level.INFO,
                graphmaster.getTotalCategories() - categories + " categories loaded from \"" + (String) response + "\".");
    }

    protected void loadAIMLFilePathChooser()
    {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Choose AIML File");
        int action = chooser.showDialog(this, "Choose");

        if (action == JFileChooser.APPROVE_OPTION)
        {
            File chosen = chooser.getSelectedFile();
            String newPath = null;
            try
            {
                newPath = chosen.getCanonicalPath();
            }
            catch (IOException e)
            {
                return;
            }
            int categories = this.core.getGraphmaster().getTotalCategories();
            Graphmaster graphmaster = this.core.getGraphmaster();
            graphmaster.load(newPath, this.shell.getCurrentBotID());
            Logger.getLogger("programd").log(Level.INFO,
                    graphmaster.getTotalCategories() - categories + " categories loaded from \"" + newPath + "\".");
        }
    }

    protected void chooseBot()
    {
        String[] botIDs = this.core.getBots().getIDs().toArray(new String[] {});
        ListDialog.initialize(this.frame, botIDs, "Choose a bot", "Choose the bot with whom you want to talk.");
        String choice = ListDialog.showDialog(null, this.shell.getCurrentBotID());
        if (choice != null)
        {
            this.shell.switchToBot(choice);
        }
    }

    protected void showAboutBox()
    {
        JOptionPane.showMessageDialog(null, HELP_MESSAGE, "About", JOptionPane.INFORMATION_MESSAGE, this.logo);
    }

    /**
     * Enables the input panel and starts the underlying console's shell.
     */
    public void startShell()
    {
        this.inputPanel.setEnabled(true);
        this.console.startShell();
    }
}