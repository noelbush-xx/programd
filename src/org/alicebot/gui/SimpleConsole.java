/*
    Alicebot Program D
    Copyright (C) 1995-2001, A.L.I.C.E. AI Foundation
    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
    USA.
*/

package org.alicebot.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.StringReader;

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
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import org.alicebot.server.core.Bots;
import org.alicebot.server.core.Globals;
import org.alicebot.server.core.Graphmaster;
import org.alicebot.server.core.logging.Log;
import org.alicebot.server.core.util.Shell;
import org.alicebot.server.core.util.Trace;
import org.alicebot.server.net.AliceServer;


/**
 *  Provides a very simple console for the bot.
 *
 *  @author Noel Bush
 *  @since 4.1.5
 */
public class SimpleConsole extends JPanel
{
    /** The server associated with this console. */
    private AliceServer server;

    /** The Shell used by this console. */
    private Shell shell;

    /** Where console messages will be displayed. */
    private JTextArea display;

    /** Contains the input prompt and field. */
    private InputPanel inputPanel;

    private ConsoleDisplayStream consoleDisplay = new ConsoleDisplayStream();

    private JFrame frame;

    /** The stream to which console display will be directed. */
    private PrintStream displayStream = new PrintStream(consoleDisplay);

    /** The stream to which console prompt will be directed. */
    private PrintStream promptStream = new PrintStream(new ConsolePromptStream());

    /** The stream which will receive console input. */
    private ConsoleInputStream inStream = new ConsoleInputStream();

    /** For convenience, the system line separator. */
    private static final String LINE_SEPARATOR = System.getProperty("line.separator", "\n");

    private static final Object[] HELP_MESSAGE = {"Simple Console for",
                                                  "Program D version " + Graphmaster.VERSION,
                                                  "(c) A.L.I.C.E. AI Foundation (http://alicebot.org)"};

    private static JMenuBar menuBar;

    private static final ImageIcon aliceLogo =
        new ImageIcon(ClassLoader.getSystemResource("org/alicebot/icons/aliceLogo.jpg"));

    private static final ImageIcon aliceIcon =
        new ImageIcon(ClassLoader.getSystemResource("org/alicebot/icons/aliceIcon.jpg"));


    /**
     *  Constructs a new simple console gui with a new shell.
     */
    public SimpleConsole()
    {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

        display = new JTextArea(40, 90);
        display.setFont(new Font("Courier New", Font.PLAIN, 12));
        display.setLineWrap(true);
        display.setWrapStyleWord(true);
        display.setTabSize(4);
        display.setForeground(Color.black);
        display.setEditable(false);

        JScrollPane scrollPane = new JScrollPane(display);
        scrollPane.setAlignmentY(Component.CENTER_ALIGNMENT);

        inputPanel = new InputPanel();

        this.add(scrollPane);
        this.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        this.add(Box.createRigidArea(new Dimension(0, 5)));
        this.add(inputPanel);

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
                                          });

        JMenuItem loadAIMLFilePath = new JMenuItem("Load AIML from file path...");
        loadAIMLFilePath.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        loadAIMLFilePath.setMnemonic(KeyEvent.VK_P);
        loadAIMLFilePath.addActionListener(new ActionListener()
                                               {
                                                   public void actionPerformed(ActionEvent ae)
                                                   {
                                                       loadAIMLFilePathChooser();
                                                   }
                                               });
       
        JMenuItem exit = new JMenuItem("Exit");
        exit.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        exit.setMnemonic(KeyEvent.VK_X);
        exit.addActionListener(new ActionListener()
                                   {
                                        public void actionPerformed(ActionEvent ae)
                                        {
                                            shutdown();
                                        }
                                   });
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
        pause.addActionListener(new ActionListener()
                                    {
                                          public void actionPerformed(ActionEvent ae)
                                          {
                                              consoleDisplay.togglePause();
                                          }
                                    });

        JMenuItem talkToBot = new JMenuItem("Talk to bot...");
        talkToBot.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        talkToBot.setMnemonic(KeyEvent.VK_B);
        talkToBot.addActionListener(new ActionListener()
                                        {
                                              public void actionPerformed(ActionEvent ae)
                                              {
                                                  chooseBot();
                                              }
                                        });

        JMenuItem botFiles = new JMenuItem("List bot files");
        botFiles.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        botFiles.setMnemonic(KeyEvent.VK_F);
        botFiles.addActionListener(new ActionListener()
                                       {
                                             public void actionPerformed(ActionEvent ae)
                                             {
                                                 shell.listBotFiles();
                                             }
                                       });

        JMenuItem listBots = new JMenuItem("List bots");
        listBots.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        listBots.setMnemonic(KeyEvent.VK_L);
        listBots.addActionListener(new ActionListener()
                                       {
                                             public void actionPerformed(ActionEvent ae)
                                             {
                                                 shell.showBotList();
                                             }
                                       });

        JMenuItem rollTargets = new JMenuItem("Roll targets");
        rollTargets.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        rollTargets.setMnemonic(KeyEvent.VK_T);
        rollTargets.addActionListener(new ActionListener()
                                          {
                                                public void actionPerformed(ActionEvent ae)
                                                {
                                                    shell.rollTargets();
                                                }
                                          });

        actionsMenu.add(pause);
        actionsMenu.addSeparator();
        actionsMenu.add(talkToBot);
        actionsMenu.add(listBots);
        actionsMenu.add(botFiles);
        actionsMenu.addSeparator();
        actionsMenu.add(rollTargets);

        // Create the Help menu.
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        helpMenu.setMnemonic(KeyEvent.VK_H);

        JMenuItem shellHelp = new JMenuItem("Shell Help...");
        shellHelp.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        shellHelp.setMnemonic(KeyEvent.VK_H);
        shellHelp.addActionListener(new ActionListener()
                                    {
                                        public void actionPerformed(ActionEvent ae)
                                        {
                                            shell.help();
                                        }
                                    });
        JMenuItem about = new JMenuItem("About Simple Console...");
        about.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        about.setMnemonic(KeyEvent.VK_A);
        about.addActionListener(new ActionListener()
                                    {
                                        public void actionPerformed(ActionEvent ae)
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

        frame = new JFrame();
        frame.setTitle("Program D Simple Console");
        frame.getContentPane().add(this);
        frame.setJMenuBar(menuBar);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setLocation(50, 50);
        frame.setVisible(true);
    }


    /**
     *  Starts the simple console and an AliceServer,
     *  given the path to a properties file.
     *
     *  @param propertiesPath   the path to the server properties file
     */
    public void start(String propertiesPath)
    {
        Globals.load(propertiesPath);
        this.shell = new Shell(inStream, displayStream, promptStream);
        this.server = new AliceServer(propertiesPath, shell);
        Trace.setOut(displayStream);
        server.startup();
        shutdown();
    }


    private void shutdown()
    {
        if (server != null)
        {
            server.shutdown();
        }
        // Let the user exit, in case termination was abnormal or messages are otherwise interesting.
    }


    class InputPanel extends JPanel
    {
        /** Where the console prompt will be displayed. */
        private JLabel prompt;

        /** The console input field. */
        private JTextField input;

        public InputPanel()
        {
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

            prompt = new JLabel();
            prompt.setFont(new Font("Courier New", Font.PLAIN, 12));
            prompt.setForeground(Color.black);
            prompt.setBackground(Color.white);
            prompt.setHorizontalAlignment(SwingConstants.LEFT);
            prompt.setAlignmentY(Component.CENTER_ALIGNMENT);

            input = new JTextField();
            input.setFont(new Font("Courier New", Font.PLAIN, 12));
            input.setForeground(Color.black);
            input.setMinimumSize(new Dimension(50, 20));
            input.setPreferredSize(new Dimension(200, 20));
            input.setMaximumSize(new Dimension(Short.MAX_VALUE, 20));
            input.setHorizontalAlignment(SwingConstants.LEFT);
            input.setAlignmentY(Component.CENTER_ALIGNMENT);
            input.addActionListener(new InputSender());

            JButton enter = new JButton("Enter");
            enter.setFont(new Font("Fixedsys", Font.PLAIN, 10));
            enter.setForeground(Color.black);
            enter.setMinimumSize(new Dimension(70, 20));
            enter.setPreferredSize(new Dimension(70, 20));
            enter.setMaximumSize(new Dimension(70, 20));
            enter.addActionListener(new InputSender());
            enter.setAlignmentY(Component.CENTER_ALIGNMENT);

            this.add(prompt);
            this.add(input);
            this.add(enter);
        }

        public void setPrompt(String text)
        {
            prompt.setText(text);
            prompt.revalidate();
            input.requestFocus();
        }


        private class InputSender implements ActionListener
        {
            public void actionPerformed(ActionEvent ae)
            {
                String inputText = ae.getActionCommand();
                display.append(prompt.getText() + inputText + LINE_SEPARATOR);
                inStream.receive(inputText);
                input.setText(null);
            }
        }
    }


    /**
     *  Extends OutputStream to direct all output to the display textarea.
     */
    public class ConsoleDisplayStream extends OutputStream
    {
        private boolean paused = false;


        public ConsoleDisplayStream()
        {
            super();
        }

        public void write(byte[] b, int off, int len)
        {
            while (paused)
            {
                try
                {
                    Thread.sleep(500);
                }
                catch (InterruptedException e)
                {
                }
            }
            display.append(new String(b, off, len));
            display.setCaretPosition(display.getText().length());
        }


        public void write(int b)
        {
            while (paused)
            {
                try
                {
                    Thread.sleep(500);
                }
                catch (InterruptedException e)
                {
                }
            }
            display.append(String.valueOf((char)b));
            display.setCaretPosition(display.getText().length());
        }


        private void togglePause()
        {
            paused = !paused;
        }
    }


    /**
     *  Extends OutputStream to direct all output to the prompt field.
     */
    public class ConsolePromptStream extends OutputStream
    {
        public ConsolePromptStream()
        {
            super();
        }

        public void write(byte[] b, int off, int len)
        {
            inputPanel.setPrompt(new String(b, off, len));
        }


        public void write (int b)
        {
            inputPanel.setPrompt(String.valueOf((char)b));
        }
    }


    public class ConsoleInputStream extends InputStream
    {
        byte[] content = new byte[] {};

        private int mark = 0;

        public ConsoleInputStream()
        {
        }


        public void receive(String string)
        {
            content = (string + '\n').getBytes();
            mark = 0;
        }


        public int read(byte b[], int off, int len) throws IOException
        {
            while (mark >= content.length)
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
            else if ((off < 0) || (off > b.length) || (len < 0) ||
                     ((off + len) > b.length) || ((off + len) < 0))
            {
                throw new IndexOutOfBoundsException();
            }
            else if (len == 0)
            {
                return 0;
            }
            else if (content.length == 0)
            {
                return -1;
            }

            int i = 1;
            b[off] = content[mark++];
            for (; i < len && i < content.length; i++)
            {
                b[off + i] = content[mark++];
            }
            return i;
        }


        public int available() throws IOException
        {
            return content.length - mark - 1;
        }


        public boolean markSupported()
        {
            return false;
        }

        public int read()
        {
            while (mark >= content.length)
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
            if (mark < content.length)
            {
                return content[mark++];
            }
            else
            {
                return -1;
            }
        }



    }


    private void loadAIMLURLBox()
    {
        Object response =
            JOptionPane.showInputDialog(null, "Enter the URL from which to load.", "Load AIML from URL",
                                        JOptionPane.PLAIN_MESSAGE, null, null, null);
        if (response == null)
        {
            return;
        }

        int categories = Graphmaster.getTotalCategories();
        Graphmaster.load((String)response, shell.getCurrentBotID());
        Log.userinfo(Graphmaster.getTotalCategories() - categories +
        " categories loaded from \"" + (String)response + "\".", Log.LEARN);
    }


    private void loadAIMLFilePathChooser()
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
                Trace.userinfo("I/O error trying to access \"" + newPath + "\".");
                return;
            }
            int categories = Graphmaster.getTotalCategories();
            Graphmaster.load(newPath, shell.getCurrentBotID());
            Log.userinfo(Graphmaster.getTotalCategories() - categories +
            " categories loaded from \"" + newPath + "\".", Log.LEARN);
        }
    }


    private void chooseBot()
    {
        String[] botIDs = (String[])Bots.getIDs().toArray(new String[]{});
        ListDialog.initialize(frame, botIDs, "Choose a bot", "Choose the bot with whom you want to talk.");
        String choice = ListDialog.showDialog(null, shell.getCurrentBotID());
        if (choice != null)
        {
            shell.switchToBot(choice);
        }
    }


    private void showAboutBox()
    {
        JOptionPane.showMessageDialog(null, HELP_MESSAGE, "About", JOptionPane.INFORMATION_MESSAGE, aliceLogo);
    }


    public static void main(String[] args)
    {
        String serverPropertiesPath;

        if (args.length > 0)
        {
            serverPropertiesPath = args[0];
        }
        else
        {
            serverPropertiesPath = "server.properties";
        }

        new SimpleConsole().start(serverPropertiesPath);
    }
}