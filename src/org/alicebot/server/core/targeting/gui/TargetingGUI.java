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

package org.alicebot.server.core.targeting.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import java.io.File;
import java.io.IOException;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;

import org.alicebot.server.core.targeting.TargetingTool;
import org.alicebot.server.core.util.Trace;


/**
 *  Implements a demo targeting GUI.
 *
 *  @author Richard Wallace
 *  @author Noel Bush
 */
public class TargetingGUI extends JPanel
{
    static final int MIN_WIDTH = 600;
    static final int MIN_HEIGHT = 400;
    static final int PREF_WIDTH = 700;
    static final int PREF_HEIGHT = 500;

    static final Dimension minDimension = new Dimension(MIN_WIDTH, MIN_HEIGHT);
    static final Dimension prefDimension = new Dimension(PREF_WIDTH, PREF_HEIGHT);

    private static TargetingTool targetingTool;

    private JFrame frame;
    private JTabbedPane tabbedPane;
    public TargetPanel targetPanel;
    public InputPanel inputPanel;
    public CategoryPanel categoryPanel;
    private static JMenuBar menuBar;
    public JLabel statusBar;

    private static final Object[] HELP_MESSAGE = {"AIML Targeting Tool",
                                                  "Program D version " + targetingTool.VERSION,
                                                  "(c) A.L.I.C.E. AI Foundation (http://alicebot.org)"};

    private static final ImageIcon aliceLogo =
        new ImageIcon(ClassLoader.getSystemResource("org/alicebot/icons/aliceLogo.jpg"));

    private static final ImageIcon aliceIcon =
        new ImageIcon(ClassLoader.getSystemResource("org/alicebot/icons/aliceIcon.jpg"));

    public void start()
    {
        frame = new JFrame();
        updateTitle();
        frame.getContentPane().add(this);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setJMenuBar(menuBar);
        frame.pack();
        frame.setLocation(50, 50);
        frame.setIconImage(aliceIcon.getImage());
        frame.setVisible(true);

        // Go to the next (first) target.
        targetPanel.nextTarget();
    }


    public TargetingGUI(TargetingTool targetingTool)
    {
        this.targetingTool = targetingTool;

        // Create and configure the targetPanel.
        targetPanel = new TargetPanel(this);
        targetPanel.setMinimumSize(minDimension);
        targetPanel.setPreferredSize(prefDimension);
        targetPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Create and configure the inputPanel.
        inputPanel = new InputPanel(this);
        inputPanel.setMinimumSize(minDimension);
        inputPanel.setPreferredSize(prefDimension);
        inputPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Create and configure the categoryPanel.
        categoryPanel = new CategoryPanel(this);
        categoryPanel.setMinimumSize(minDimension);
        categoryPanel.setPreferredSize(prefDimension);
        categoryPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        // Create the status bar.
        statusBar = new JLabel();
        statusBar.setAlignmentX(Component.RIGHT_ALIGNMENT);
        statusBar.setHorizontalAlignment(SwingConstants.RIGHT);
        statusBar.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        statusBar.setForeground(Color.black);
        statusBar.setMinimumSize(new Dimension(MIN_WIDTH, 14));
        statusBar.setPreferredSize(new Dimension(PREF_WIDTH, 14));
        statusBar.setMaximumSize(new Dimension(Short.MAX_VALUE, 14));

        // Create and configure the tabbed pane.
        tabbedPane = new JTabbedPane();
        tabbedPane.setAlignmentX(Component.RIGHT_ALIGNMENT);
        tabbedPane.setMinimumSize(new Dimension(MIN_WIDTH, PREF_HEIGHT));
        tabbedPane.setPreferredSize(new Dimension(PREF_WIDTH, PREF_HEIGHT));
        tabbedPane.setMaximumSize(new Dimension(Short.MAX_VALUE, Short.MAX_VALUE));
        tabbedPane.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        tabbedPane.setTabPlacement(SwingConstants.BOTTOM);

        // Add the panels to the tabbed pane.
        tabbedPane.add("Targets", targetPanel);
        tabbedPane.add("Inputs", inputPanel);
        tabbedPane.add("Categories", categoryPanel);

        // Add the tabbed pane and the status bar to the main panel.
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(tabbedPane);
        add(statusBar);

        // Create the File menu.
        menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        fileMenu.setMnemonic(KeyEvent.VK_F);

        JMenuItem loadDataURL = new JMenuItem("Load targets data from URL...");
        loadDataURL.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        loadDataURL.setMnemonic(KeyEvent.VK_U);
        loadDataURL.addActionListener(new ActionListener()
                                          {
                                              public void actionPerformed(ActionEvent ae)
                                              {
                                                  loadDataURLBox();
                                              }
                                          });

        JMenuItem loadDataFilePath = new JMenuItem("Load targets data from file path...");
        loadDataFilePath.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        loadDataFilePath.setMnemonic(KeyEvent.VK_P);
        loadDataFilePath.addActionListener(new ActionListener()
                                               {
                                                   public void actionPerformed(ActionEvent ae)
                                                   {
                                                       loadDataFilePathChooser();
                                                   }
                                               });
       
        JMenuItem reload = new JMenuItem("Reload target data");
        reload.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        reload.setMnemonic(KeyEvent.VK_R);
        reload.addActionListener(new ActionListener()
                                     {
                                        public void actionPerformed(ActionEvent ae)
                                        {
                                            reloadTargets();
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
        fileMenu.add(loadDataURL);
        fileMenu.add(loadDataFilePath);
        fileMenu.addSeparator();
        fileMenu.add(reload);
        fileMenu.addSeparator();
        fileMenu.add(exit);

        // Create the Edit menu.
        JMenu editMenu = new JMenu("Edit");
        editMenu.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        editMenu.setMnemonic(KeyEvent.VK_E);

        // Create the View menu.
        JMenu viewMenu = new JMenu("View");
        viewMenu.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        viewMenu.setMnemonic(KeyEvent.VK_V);

        JMenuItem viewTargets = new JMenuItem("Targets");
        viewTargets.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        viewTargets.setMnemonic(KeyEvent.VK_T);
        viewTargets.addActionListener(new ActionListener()
                                          {
                                              public void actionPerformed(ActionEvent ae)
                                              {
                                                  viewTargets();
                                              }
                                          });

        JMenuItem viewInputs = new JMenuItem("Inputs");
        viewInputs.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        viewInputs.setMnemonic(KeyEvent.VK_I);
        viewInputs.addActionListener(new ActionListener()
                                         {
                                             public void actionPerformed(ActionEvent ae)
                                             {
                                                 viewInputs();
                                             }
                                         });
        
        JMenuItem viewCategories = new JMenuItem("Inputs");
        viewCategories.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        viewCategories.setMnemonic(KeyEvent.VK_C);
        viewCategories.addActionListener(new ActionListener()
                                             {
                                                 public void actionPerformed(ActionEvent ae)
                                                 {
                                                     viewCategories();
                                                 }
                                             });
        
        viewMenu.add(viewTargets);
        viewMenu.add(viewInputs);
        viewMenu.add(viewCategories);


        // Create the Options menu.
        JMenu optionsMenu = new JMenu("Options");
        optionsMenu.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        optionsMenu.setMnemonic(KeyEvent.VK_O);

        JCheckBoxMenuItem includeIncompleteThats =
            new JCheckBoxMenuItem("Include incomplete <that>s", targetingTool.includeIncompleteThats());
        includeIncompleteThats.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        includeIncompleteThats.setMnemonic(KeyEvent.VK_I);
        includeIncompleteThats.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_T, Event.CTRL_MASK));
        includeIncompleteThats.addActionListener(new ActionListener()
                                                     {
                                                         public void actionPerformed(ActionEvent ae)
                                                         {
                                                             includeIncompleteThats(((JCheckBoxMenuItem)ae.getSource()).getState());
                                                         }
                                                     });

        JCheckBoxMenuItem includeIncompleteTopics =
            new JCheckBoxMenuItem("Include incomplete <topic>s", targetingTool.includeIncompleteTopics());
        includeIncompleteTopics.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        includeIncompleteTopics.setMnemonic(KeyEvent.VK_N);
        includeIncompleteTopics.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK));
        includeIncompleteTopics.addActionListener(new ActionListener()
                                                     {
                                                         public void actionPerformed(ActionEvent ae)
                                                         {
                                                             includeIncompleteTopics(((JCheckBoxMenuItem)ae.getSource()).getState());
                                                         }
                                                     });

        JMenuItem changeReloadFrequency = new JMenuItem("Change reload frequency...");
        changeReloadFrequency.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        changeReloadFrequency.setMnemonic(KeyEvent.VK_R);
        changeReloadFrequency.addActionListener(new ActionListener()
                                                    {
                                                        public void actionPerformed(ActionEvent ae)
                                                        {
                                                            showSetReloadFrequencyBox();
                                                        }
                                                    });

        optionsMenu.add(includeIncompleteThats);
        optionsMenu.add(includeIncompleteTopics);
        optionsMenu.addSeparator();
        optionsMenu.add(changeReloadFrequency);

        // Create the Actions menu.
        JMenu actionsMenu = new JMenu("Actions");
        actionsMenu.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        actionsMenu.setMnemonic(KeyEvent.VK_A);

        JMenuItem discard = new JMenuItem("Discard target");
        discard.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        discard.addActionListener(targetPanel.new DiscardTarget());

        JMenuItem discardAll = new JMenuItem("Discard all targets");
        discardAll.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        discardAll.addActionListener(targetPanel.new DiscardAllTargets());

        JMenuItem save = new JMenuItem("Save new category from target");
        save.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        save.setMnemonic(KeyEvent.VK_S);
        save.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK));
        save.addActionListener(targetPanel.new SaveTarget());

        JMenuItem next = new JMenuItem("Get next target");
        next.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        next.setMnemonic(KeyEvent.VK_N);
        next.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK));
        next.addActionListener(targetPanel.new NextTarget());

        actionsMenu.add(save);
        actionsMenu.add(next);
        actionsMenu.add(discard);
        actionsMenu.add(discardAll);

        // Create the Help menu.
        JMenu helpMenu = new JMenu("Help");
        helpMenu.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        helpMenu.setMnemonic(KeyEvent.VK_H);

        JMenuItem about = new JMenuItem("About...");
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

        // Add menus to the menu bar.
        menuBar.add(fileMenu);
        menuBar.add(editMenu);
        menuBar.add(viewMenu);
        menuBar.add(optionsMenu);
        menuBar.add(actionsMenu);
        menuBar.add(helpMenu);
    }


    public void shutdown()
    {
        targetingTool.shutdown();
    }


    public void reloadTargets()
    {
        try
        {
            targetingTool.reload();
        }
        catch (Exception e)
        {
            showError(e.getMessage());
        }
    }

    private void includeIncompleteThats(boolean b)
    {
        targetingTool.includeIncompleteThats(b);
        if (!targetPanel.hasTarget())
        {
            targetPanel.nextTarget();
        }
    }


    private void includeIncompleteTopics(boolean b)
    {
        targetingTool.includeIncompleteTopics(b);
        if (!targetPanel.hasTarget())
        {
            targetPanel.nextTarget();
        }
    }


    private void showAboutBox()
    {
        JOptionPane.showMessageDialog(null, HELP_MESSAGE, "About", JOptionPane.INFORMATION_MESSAGE, aliceLogo);
    }


    private void showSetReloadFrequencyBox()
    {
        int currentFrequency = targetingTool.getReloadFrequency();
        Object response =
            JOptionPane.showInputDialog(null, "Please input a value in seconds.", "Set Reload Frequency",
                                        JOptionPane.PLAIN_MESSAGE, null, null, new Integer(currentFrequency));
        if (response == null)
        {
            return;
        }

        int newFrequency;
        try
        {
            newFrequency = Integer.parseInt((String)response);
        }
        catch (NumberFormatException e)
        {
            JOptionPane.showMessageDialog(null, "Invalid entry. Reload frequency unchanged from " +
                currentFrequency + ".", "Invalid entry.", JOptionPane.WARNING_MESSAGE);
            return;
        }
        targetingTool.restartTimer(newFrequency);
        JOptionPane.showMessageDialog(null, "Reload frequency changed to " +
            newFrequency + ".", "Frequency changed.", JOptionPane.PLAIN_MESSAGE);
    }


    private void loadDataURLBox()
    {
        String currentPath = targetingTool.getTargetsDataPath();
        Object response =
            JOptionPane.showInputDialog(null, "Enter the targets data URL from which to load.", "Change data URL",
                                        JOptionPane.PLAIN_MESSAGE, null, null, currentPath);
        if (response == null)
        {
            return;
        }

        JOptionPane.showMessageDialog(null, "Targets data URL changed to " +
            (String)response + ".", "Data path changed.", JOptionPane.PLAIN_MESSAGE);
        setStatus("Loading targets data....");
        targetingTool.changeTargetsDataPath((String)response);
        targetPanel.nextTarget();
        setStatus("");
        updateTitle();
    }


    private void loadDataFilePathChooser()
    {
        String currentPath = targetingTool.getTargetsDataPath();

        JFileChooser chooser = new JFileChooser(currentPath);
        chooser.setDialogTitle("Choose Targets Data File");
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
                showError("I/O error trying to access \"" + newPath + "\".");
                Trace.userinfo("I/O error trying to access \"" + newPath + "\".");
                return;
            }
            JOptionPane.showMessageDialog(null, "Targets data file path changed to " +
                newPath + ".", "Data path changed.", JOptionPane.PLAIN_MESSAGE);
            setStatus("Loading targets data....");
            targetingTool.changeTargetsDataPath(newPath);
            targetPanel.nextTarget();
            setStatus("");
            updateTitle();
        }
    }


    public void showError(String error)
    {
        JOptionPane.showMessageDialog(null, error, "Error", JOptionPane.ERROR_MESSAGE);
    }


    public void setStatus(String status)
    {
        statusBar.setText(status);
        Trace.devinfo(status);
    }


    private void updateTitle()
    {
        frame.setTitle("AIML Targeting Tool, Program D version " + targetingTool.VERSION +
            " - " + targetingTool.getTargetsDataPath());
    }


    public void viewTargets()
    {
        tabbedPane.setSelectedIndex(0);
    }


    public void viewInputs()
    {
        tabbedPane.setSelectedIndex(1);
    }


    public void viewCategories()
    {
        tabbedPane.setSelectedIndex(2);
    }
}






