/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.interfaces.graphical;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

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
import javax.swing.SwingConstants;

import org.aitools.programd.Core;
import org.aitools.programd.graph.Graphmaster;
import org.aitools.programd.interfaces.ConsoleSettings;
import org.aitools.programd.interfaces.Shell;

/**
 * Provides a very simple GUI console for the bot.
 * 
 * @author Noel Bush
 * @since 4.1.5
 * @version 4.2
 */
public class SimpleGUIConsole extends JPanel
{
    /** The core associated with this console. */
    private Core core;

    /** The Shell used by this console. */
    protected Shell shell;
    
    /** The error logger. */
    protected static Logger errorLogger = Logger.getLogger("programd.error");

    /** Where console messages will be displayed. */
    protected JTextArea display;

    /** Contains the input prompt and field. */
    protected InputPanel inputPanel;

    protected ConsoleDisplayStream consoleDisplay = new ConsoleDisplayStream(this);

    private JFrame frame;

    /** The stream to which console display will be directed. */
    private PrintStream displayStream = new PrintStream(this.consoleDisplay);

    /** The stream to which console prompt will be directed. */
    private PrintStream promptStream = new PrintStream(new ConsolePromptStream(this));

    /** The stream which will receive console input. */
    protected ConsoleInputStream inStream = new ConsoleInputStream();

    /** For convenience, the system line separator. */
    protected static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

    private static final Object[] HELP_MESSAGE =
        { "Simple Console for", "Program D version " + Core.VERSION } ;

    private static JMenuBar menuBar;

    private static ImageIcon logo;
    static
    {
        try
        {
            logo = new ImageIcon(ClassLoader.getSystemResource("org/aitools/programd/gui/icons/logo.jpg"));
        } 
        catch (NullPointerException e)
        {
            errorLogger.log(Level.SEVERE, "The logo is missing from available resources.");
        } 
    } 

    private static ImageIcon icon;
    static
    {
        try
        {
            icon = new ImageIcon(ClassLoader.getSystemResource("org/aitools/programd/gui/icons/icon.jpg"));
        } 
        catch (NullPointerException e)
        {
            errorLogger.log(Level.SEVERE, "The icon is missing from available resources.");
        } 
    } 

    /**
     * Constructs a new simple console gui with a new shell.
     */
    public SimpleGUIConsole(String corePropertiesPath, String consolePropertiesPath)
    {
        this.core = new Core(corePropertiesPath);
        this.shell = new Shell(this.inStream, this.displayStream, this.promptStream, new ConsoleSettings(consolePropertiesPath));
        this.shell.attach(this.core);

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        this.display = new JTextArea(40, 90);
        this.display.setFont(new Font("Courier New", Font.PLAIN, 12));
        this.display.setLineWrap(true);
        this.display.setWrapStyleWord(true);
        this.display.setTabSize(4);
        this.display.setForeground(Color.black);
        this.display.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(this.display);
        scrollPane.setAlignmentY(Component.CENTER_ALIGNMENT);

        this.inputPanel = new InputPanel(this);

        this.add(scrollPane);
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.add(Box.createRigidArea(new Dimension(0, 5)));
        this.add(this.inputPanel);

        menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem loadAIMLURL = new JMenuItem("Load AIML from URL...");
        loadAIMLURL.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        loadAIMLURL.setMnemonic(KeyEvent.VK_U);
        loadAIMLURL.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                loadAIMLURLBox();
            } 
        } );

        JMenuItem loadAIMLFilePath = new JMenuItem("Load AIML from file path...");
        loadAIMLFilePath.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        loadAIMLFilePath.setMnemonic(KeyEvent.VK_P);
        loadAIMLFilePath.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                loadAIMLFilePathChooser();
            } 
        } );

        JMenuItem exit = new JMenuItem("Exit");
        exit.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        exit.setMnemonic(KeyEvent.VK_X);
        exit.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                shutdown();
            } 
        } );
        fileMenu.add(loadAIMLURL);
        fileMenu.add(loadAIMLFilePath);
        fileMenu.addSeparator();
        fileMenu.add(exit);

        // Create the Actions menu.
        JMenu actionsMenu = new JMenu("Actions");
        actionsMenu.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        actionsMenu.setMnemonic(KeyEvent.VK_A);

        JCheckBoxMenuItem pause = new JCheckBoxMenuItem("Pause Console");
        pause.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        pause.setMnemonic(KeyEvent.VK_P);
        pause.addActionListener(new ParentAwareActionListener(this)
        {
            public void actionPerformed(ActionEvent ae)
            {
                ((SimpleGUIConsole) this.parent).consoleDisplay.togglePause();
            } 
        } );

        JMenuItem talkToBot = new JMenuItem("Talk to bot...");
        talkToBot.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        talkToBot.setMnemonic(KeyEvent.VK_B);
        talkToBot.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                chooseBot();
            } 
        } );

        JMenuItem botFiles = new JMenuItem("List bot files");
        botFiles.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        botFiles.setMnemonic(KeyEvent.VK_F);
        botFiles.addActionListener(new ParentAwareActionListener(this)
        {
            public void actionPerformed(ActionEvent ae)
            {
                ((SimpleGUIConsole) this.parent).shell.listBotFiles();
            } 
        } );

        JMenuItem listBots = new JMenuItem("List bots");
        listBots.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        listBots.setMnemonic(KeyEvent.VK_L);
        listBots.addActionListener(new ParentAwareActionListener(this)
        {
            public void actionPerformed(ActionEvent ae)
            {
                ((SimpleGUIConsole) this.parent).shell.showBotList();
            } 
        } );

        actionsMenu.add(pause);
        actionsMenu.addSeparator();
        actionsMenu.add(talkToBot);
        actionsMenu.add(listBots);
        actionsMenu.add(botFiles);
        actionsMenu.addSeparator();

        // Create the Help menu.
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        helpMenu.setMnemonic(KeyEvent.VK_H);

        JMenuItem shellHelp = new JMenuItem("Shell Help...");
        shellHelp.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        shellHelp.setMnemonic(KeyEvent.VK_H);
        shellHelp.addActionListener(new ParentAwareActionListener(this)
        {
            public void actionPerformed(ActionEvent ae)
            {
                ((SimpleGUIConsole) this.parent).shell.help();
            } 
        } );
        JMenuItem about = new JMenuItem("About Simple Console...");
        about.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        about.setMnemonic(KeyEvent.VK_A);
        about.addActionListener(new ActionListener()
        {
            public void actionPerformed(ActionEvent ae)
            {
                showAboutBox();
            } 
        } );

        helpMenu.add(about);
        helpMenu.add(shellHelp);

        // Add menus to the menu bar.
        menuBar.add(fileMenu);
        menuBar.add(actionsMenu);
        menuBar.add(helpMenu);

        this.frame = new JFrame();
        this.frame.setTitle("Program D Simple Console");
        this.frame.getContentPane().add(this);
        this.frame.setJMenuBar(menuBar);
        this.frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.frame.pack();
        this.frame.setLocation(50, 50);
        this.frame.setVisible(true);
} 

    /**
     * Attaches this console to the given core.
     * 
     * @param coreToUse  the core to which to attach
     */
    public void start()
    {
        this.core.startup();
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

    class InputPanel extends JPanel
    {
        /** Where the console prompt will be displayed. */
        protected JLabel prompt;

        /** The console input field. */
        protected JTextField input;

        protected SimpleGUIConsole parent;

        public InputPanel(SimpleGUIConsole parentToUse)
        {
            this.parent = parentToUse;

            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

            this.prompt = new JLabel();
            this.prompt.setFont(new Font("Courier New", Font.PLAIN, 12));
            this.prompt.setForeground(Color.black);
            this.prompt.setBackground(Color.white);
            this.prompt.setHorizontalAlignment(SwingConstants.LEFT);
            this.prompt.setAlignmentY(Component.CENTER_ALIGNMENT);

            this.input = new JTextField();
            this.input.setFont(new Font("Courier New", Font.PLAIN, 12));
            this.input.setForeground(Color.black);
            this.input.setMinimumSize(new Dimension(50, 20));
            this.input.setPreferredSize(new Dimension(200, 20));
            this.input.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
            this.input.setHorizontalAlignment(SwingConstants.LEFT);
            this.input.setAlignmentY(Component.CENTER_ALIGNMENT);
            this.input.addActionListener(new InputSender(this));

            JButton enter = new JButton("Enter");
            enter.setFont(new Font("Fixedsys", Font.PLAIN, 10));
            enter.setForeground(Color.black);
            enter.setMinimumSize(new Dimension(70, 20));
            enter.setPreferredSize(new Dimension(70, 20));
            enter.setMaximumSize(new Dimension(70, 20));
            enter.addActionListener(new InputSender(this));
            enter.setAlignmentY(Component.CENTER_ALIGNMENT);

            this.add(this.prompt);
            this.add(this.input);
            this.add(enter);
        } 

        public void setPrompt(String text)
        {
            this.prompt.setText(text);
            this.prompt.revalidate();
            this.input.requestFocus();
        } 

        private class InputSender extends ParentAwareActionListener
        {
            public InputSender(InputPanel parentToUse)
            {
                super(parentToUse);
            } 

            public void actionPerformed(ActionEvent ae)
            {
                String inputText = ae.getActionCommand();
                ((InputPanel) this.parent).parent.display.append(((InputPanel) this.parent).prompt.getText()
                        + inputText + LINE_SEPARATOR);
                ((InputPanel) this.parent).parent.inStream.receive(inputText);
                ((InputPanel) this.parent).input.setText(null);
            } 
        } 
    } 

    /**
     * Extends OutputStream to direct all output to the display textarea.
     */
    public class ConsoleDisplayStream extends OutputStream
    {
        private boolean paused = false;

        protected SimpleGUIConsole parent;

        public ConsoleDisplayStream(SimpleGUIConsole parentToUse)
        {
            super();
            this.parent = parentToUse;
        } 

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
            this.parent.display.append(new String(b, off, len));
            this.parent.display.setCaretPosition(this.parent.display.getText().length());
        } 

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
            this.parent.display.append(String.valueOf((char) b));
            this.parent.display.setCaretPosition(this.parent.display.getText().length());
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
        protected SimpleGUIConsole parent;

        public ConsolePromptStream(SimpleGUIConsole parentToUse)
        {
            super();
            this.parent = parentToUse;
        } 

        public void write(byte[] b, int off, int len)
        {
            this.parent.inputPanel.setPrompt(new String(b, off, len));
        } 

        public void write(int b)
        {
            this.parent.inputPanel.setPrompt(String.valueOf((char) b));
        } 
    } 

    public class ConsoleInputStream extends InputStream
    {
        byte[] content = new byte[] {} ;

        private int mark = 0;

        public ConsoleInputStream()
        {
            // Nothing to do.
        } 

        public void receive(String string)
        {
            this.content = (string + '\n').getBytes();
            this.mark = 0;
        } 

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

        public int available()
        {
            return this.content.length - this.mark - 1;
        } 

        public boolean markSupported()
        {
            return false;
        } 

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
            //          (otherwise...)
            return -1;
        } 

    } 

    protected void loadAIMLURLBox()
    {
        Object response = JOptionPane.showInputDialog(null, "Enter the URL from which to load.", "Load AIML from URL",
                JOptionPane.PLAIN_MESSAGE, null, null, null);
        if (response == null)
        {
            return;
        } 

        Graphmaster graphmaster = this.core.getGraphmaster();
        int categories = graphmaster.getTotalCategories();
        graphmaster.load((String) response, this.shell.getCurrentBotID());
        Logger.getLogger("programd.learn").log(Level.INFO, graphmaster.getTotalCategories() - categories + " categories loaded from \"" + (String) response
                + "\".");
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
            Logger.getLogger("programd.learn").log(Level.INFO, graphmaster.getTotalCategories() - categories + " categories loaded from \"" + newPath + "\".");
        } 
    } 

    protected void chooseBot()
    {
        String[] botIDs = this.core.getBots().getIDs().toArray(new String[] {} );
        ListDialog.initialize(this.frame, botIDs, "Choose a bot", "Choose the bot with whom you want to talk.");
        String choice = ListDialog.showDialog(null, this.shell.getCurrentBotID());
        if (choice != null)
        {
            this.shell.switchToBot(choice);
        } 
    } 

    protected void showAboutBox()
    {
        JOptionPane.showMessageDialog(null, HELP_MESSAGE, "About", JOptionPane.INFORMATION_MESSAGE, logo);
    }

    private static void usage()
    {
        System.out.println("Usage: simple-console -c <CORE_CONFIG> -n <CONSOLE_CONFIG>");
        System.out.println("Start up a simple console version of Program D using the specified config files.");
        System.out.println();
        System.out.println("  -c, --core-properties     the path to the core configuration (XML properties) file");
        System.out.println("  -n, --console-properties  the path to the console configuration (XML properties) file");
        System.out.println();
        System.out.println("Report bugs to <programd@aitools.org>");
    }

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

        new SimpleGUIConsole(corePropertiesPath, consolePropertiesPath);
    } 
}