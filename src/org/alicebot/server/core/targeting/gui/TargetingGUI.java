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

import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.alicebot.server.core.targeting.TargetingTool;


/**
 *  Implements a demo targeting GUI.
 *
 *  @author Richard Wallace
 */
public class TargetingGUI extends JPanel
{
    private static final Dimension minDimension = new Dimension(700, 400);
    private static final Dimension prefDimension = new Dimension(700, 400);

    private static TargetingTool targetingTool;

    public TargetPanel targetPanel;
    private static JMenuBar menuBar;


    public void start()
    {
        JFrame frame = new JFrame("AIML Targeting Tool, Program D version " + targetingTool.VERSION);
        frame.getContentPane().add(this);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setJMenuBar(TargetingGUI.menuBar);
        frame.pack();
        frame.setVisible(true);
    }


    public TargetingGUI(TargetingTool targetingTool)
    {
        this.targetingTool = targetingTool;

        // Create and configure the targetPanel.
        this.targetPanel = new TargetPanel();
        this.targetPanel.setMinimumSize(minDimension);
        this.targetPanel.setPreferredSize(prefDimension);

        this.setLayout(new GridLayout());
        this.add(this.targetPanel);

        // Create the File menu.
        menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        fileMenu.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        /*JMenuItem load = new JMenuItem("Load target data...");
        load.setFont(new Font("Fixedsys", Font.PLAIN, 12));*/
        JMenuItem exit = new JMenuItem("Exit");
        exit.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        exit.addActionListener(new ActionListener()
                                   {
                                        public void actionPerformed(ActionEvent ae)
                                        {
                                            shutdown();
                                        }
                                   });
        //fileMenu.add(load);
        fileMenu.add(exit);

        // Create the Actions menu.
        JMenu actionsMenu = new JMenu("Actions");
        actionsMenu.setFont(new Font("Fixedsys", Font.PLAIN, 12));

        JMenuItem discard = new JMenuItem("Discard target");
        discard.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        discard.addActionListener(targetPanel.new DiscardTarget());

        JMenuItem discardAll = new JMenuItem("Discard all targets");
        discardAll.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        discardAll.addActionListener(targetPanel.new DiscardAllTargets());

        JMenuItem save = new JMenuItem("Save new category from target");
        save.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        save.addActionListener(targetPanel.new SaveTarget());

        JMenuItem next = new JMenuItem("Get next target");
        next.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        next.addActionListener(targetPanel.new NextTarget());

        JMenuItem reload = new JMenuItem("Reload target data");
        reload.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        reload.addActionListener(new ActionListener()
                                     {
                                        public void actionPerformed(ActionEvent ae)
                                        {
                                            reloadTargets();
                                        }
                                     });

        actionsMenu.add(save);
        actionsMenu.add(next);
        actionsMenu.add(discard);
        actionsMenu.add(discardAll);
        actionsMenu.addSeparator();
        actionsMenu.add(reload);


        // Create the Options menu.
        /*JMenu optionsMenu = new JMenu("Options");
        optionsMenu.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        JMenuItem setReloadTime = new JMenuItem("Set reload frequency...");
        setReloadTime.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        optionsMenu.add(autoReload);
        optionsMenu.add(setReloadTime);*/

        // Create the Help menu.
        /*JMenu helpMenu = new JMenu("Help");
        helpMenu.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        JMenuItem about = new JMenuItem("About...");
        about.setFont(new Font("Fixedsys", Font.PLAIN, 12));
        helpMenu.add(about);*/

        // Add menus to the menu bar.
        menuBar.add(fileMenu);
        menuBar.add(actionsMenu);
        //menuBar.add(optionsMenu);
        //menuBar.add(helpMenu);
    }


    public void shutdown()
    {
        targetingTool.shutdown();
    }


    public void reloadTargets()
    {
        targetingTool.reload();
    }
}






