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

import java.awt.Component;
import java.awt.Container;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import org.alicebot.server.core.util.InputNormalizer;
import org.alicebot.server.core.targeting.Target;
import org.alicebot.server.core.targeting.TargetingTool;


public class TargetPanel extends JPanel
{
    private Target selectedTarget = null;
    JLabel countField;

    public static Random RNG = new Random();
    InputBar inputBar;
    TargetBar targetBar;
    ActionButtonsBar actionButtonsBar;
    MatchedBar matchedBar;
    TemplatePanel templatePanel;


    public TargetPanel ()
    {
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        PatternsPanel patternsPanel = new PatternsPanel();
        patternsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        templatePanel = new TemplatePanel();
        templatePanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        actionButtonsBar = new ActionButtonsBar();
        actionButtonsBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        this.add(patternsPanel);
        this.add(Box.createRigidArea(new Dimension(0, 10)));
        this.add(templatePanel);
        this.add(Box.createRigidArea(new Dimension(0, 10)));
        this.add(actionButtonsBar);
    }


    public class PatternsPanel extends JPanel
    {
        public PatternsPanel()
        {
            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            inputBar = new InputBar();
            inputBar.setAlignmentY(Component.LEFT_ALIGNMENT);

            matchedBar = new MatchedBar();
            matchedBar.setAlignmentY(Component.LEFT_ALIGNMENT);

            targetBar = new TargetBar();
            targetBar.setAlignmentY(Component.LEFT_ALIGNMENT);

            this.add(inputBar);
            this.add(Box.createRigidArea(new Dimension(0, 5)));
            this.add(matchedBar);
            this.add(Box.createRigidArea(new Dimension(0, 5)));
            this.add(targetBar);
        }
    }


    public class TemplatePanel extends JPanel
    {
        JTextArea textArea;

        public TemplatePanel()
        {
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            this.setPreferredSize(new Dimension(300, 200));

            TemplateButtons templateButtons = new TemplateButtons();
            templateButtons.setAlignmentY(Component.TOP_ALIGNMENT);
            templateButtons.setPreferredSize(new Dimension(120, 180));
            templateButtons.setMaximumSize(new Dimension(120, 180));

            textArea = new JTextArea();
            textArea.setFont(new Font("Lucida Sans", Font.PLAIN, 14));

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setBorder(BorderFactory.createTitledBorder("<template>"));
            scrollPane.setAlignmentY(Component.TOP_ALIGNMENT);

            this.add(templateButtons);
            this.add(Box.createRigidArea(new Dimension(10, 0)));
            this.add(scrollPane);
        }
        public void setText(String text)
        {
            textArea.setText(text);
        }
        public String getText()
        {
            return textArea.getText();
        }
    }


    class ActionButtonsBar extends JPanel
    {
        public ActionButtonsBar()
        {
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

            countField = new JLabel();
            countField.setFont(new Font("Fixedsys", Font.PLAIN, 12));
            countField.setForeground(Color.black);
            countField.setHorizontalAlignment(SwingConstants.LEFT);
            countField.setAlignmentX(Component.BOTTOM_ALIGNMENT);

            JButton discard = new JButton("Discard Target");
            discard.setFont(new Font("Fixedsys", Font.PLAIN, 12));
            discard.setBackground(Color.red);
            discard.setForeground(Color.white);
            discard.addActionListener(new DiscardTarget());
            discard.setAlignmentX(Component.BOTTOM_ALIGNMENT);

            JButton discardAll = new JButton("Discard All Targets");
            discardAll.setFont(new Font("Fixedsys", Font.BOLD, 12));
            discardAll.setBackground(Color.red);
            discardAll.setForeground(Color.white);
            discardAll.addActionListener(new DiscardAllTargets());
            discardAll.setAlignmentX(Component.BOTTOM_ALIGNMENT);

            JButton save = new JButton("Save Category");
            save.setFont(new Font("Fixedsys", Font.PLAIN, 12));
            save.setBackground(Color.green);
            save.setForeground(Color.black);
            save.addActionListener(new SaveTarget());
            save.setAlignmentX(Component.BOTTOM_ALIGNMENT);

            JButton next = new JButton("Next Target");
            next.setFont(new Font("Fixedsys", Font.PLAIN, 12));
            next.setBackground(Color.yellow);
            next.setForeground(Color.black);
            next.addActionListener(new NextTarget());
            next.setAlignmentX(Component.BOTTOM_ALIGNMENT);

            Dimension minSize = new Dimension(50, 30);
            Dimension prefSize = new Dimension(50, 30);
            Dimension maxSize = new Dimension(Short.MAX_VALUE, 30);

            this.add(countField);
            this.add(new Box.Filler(minSize, prefSize, maxSize));
            this.add(discard);
            this.add(discardAll);
            this.add(save);
            this.add(next);
        }
    }


    class CategoryBar extends JPanel
    {
        JTextField patternField;
        JTextField thatField;
        JTextField topicField;


        public void setEditable(boolean b)
        {
            patternField.setEditable(b);
            thatField.setEditable(b);
            topicField.setEditable(b);
        }


        public void setFields(String pattern, String that, String topic)
        {
            patternField.setText(pattern);
            thatField.setText(that);
            topicField.setText(topic);
        }


        public CategoryBar(String text)
        {
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

            JLabel label = new JLabel(text);
            label.setMinimumSize(new Dimension(80, 40));
            label.setPreferredSize(new Dimension(80, 40));
            label.setMaximumSize(new Dimension(80, 40));
            label.setHorizontalAlignment(SwingConstants.LEFT);
            label.setFont(new Font("Fixedsys", Font.PLAIN, 12));
            label.setForeground(Color.black);
            label.setAlignmentY(Component.CENTER_ALIGNMENT);

            patternField = new JTextField();
            patternField.setMinimumSize(new Dimension(200, 40));
            patternField.setPreferredSize(new Dimension(200, 40));
            patternField.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
            patternField.setFont(new Font("Lucida Sans", Font.PLAIN, 14));
            patternField.setBorder(BorderFactory.createTitledBorder("<pattern>"));
            patternField.setAlignmentY(Component.CENTER_ALIGNMENT);

            thatField = new JTextField();
            thatField.setMinimumSize(new Dimension(200, 40));
            thatField.setPreferredSize(new Dimension(200, 40));
            thatField.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
            thatField.setFont(new Font("Lucida Sans", Font.PLAIN, 14));
            thatField.setBorder(BorderFactory.createTitledBorder("<that>"));
            thatField.setAlignmentY(Component.CENTER_ALIGNMENT);

            topicField = new JTextField();
            topicField.setMinimumSize(new Dimension(200, 40));
            topicField.setPreferredSize(new Dimension(200, 40));
            topicField.setMaximumSize(new Dimension(Short.MAX_VALUE, 40));
            topicField.setFont(new Font("Lucida Sans", Font.PLAIN, 14));
            topicField.setBorder(BorderFactory.createTitledBorder("<topic>"));
            topicField.setAlignmentY(Component.CENTER_ALIGNMENT);

            this.add(label);
            this.add(patternField);
            this.add(thatField);
            this.add(topicField);
        }
    } 


    class MatchedBar extends CategoryBar
    {
        public MatchedBar()
        {
            super("Matched: ");
            setEditable(false);
        }
    }


    class InputBar extends CategoryBar
    {
      public InputBar()
       {
        super("Input: ");
        setEditable(false);
      }
    }


    class TargetBar extends CategoryBar
    {
        public TargetBar()
        {
            super("New AIML: ");
        }
    }


    public void setTarget(Target target)
    {
        inputBar.setFields(target.getFirstInputText(),
                           target.getFirstInputThat(),
                           target.getFirstInputTopic());
        matchedBar.setFields(target.getMatchPattern(),
                             target.getMatchThat(),
                             target.getMatchTopic());
        targetBar.setFields(target.getExtensionPattern(),
                            target.getExtensionThat(),
                            target.getExtensionTopic());
        templatePanel.setText("");
        selectedTarget = target;
    }


    class NextTarget implements ActionListener
    {
        public void actionPerformed(ActionEvent ae)
        {
            nextTarget();
        }
    }


    public void nextTarget()
    {
        Target next = TargetingTool.getNextTarget();
        if (next != null)
        {
            setTarget(next);
        }
        else
        {
            inputBar.setFields("*","*","*");
            matchedBar.setFields("*","*","*");
            targetBar.setFields("*","*","*");
            templatePanel.setText("No more targets!");
        }
        updateCountDisplay();
    }


    class DiscardTarget implements ActionListener
    {
        public void actionPerformed(ActionEvent ae)
        {
            TargetingTool.discard(selectedTarget);
            nextTarget();
        }
    }


    class DiscardAllTargets implements ActionListener
    {
        public void actionPerformed(ActionEvent ae)
        {
            TargetingTool.discardAll();
            nextTarget();
        }
    }


    public void updateCountDisplay()
    {
        countField.setText(TargetingTool.countLive() + " live, " +
                           TargetingTool.countSaved() + " saved, " +
                           TargetingTool.countDiscarded() + " discarded");
    }


    class SaveTarget implements ActionListener
    {
        public void actionPerformed(ActionEvent ae)
        {
            saveTarget();
        }
    }


    public void saveTarget()
    {
        String template = templatePanel.getText().trim();
        if (template.length() == 0)
        {
            templatePanel.setText("Template is empty!");
            return;
        }
        if (selectedTarget != null)
        {
            selectedTarget.setExtensionPattern(InputNormalizer.patternFit(targetBar.patternField.getText()));
            selectedTarget.setExtensionThat(InputNormalizer.patternFit(targetBar.thatField.getText()));
            selectedTarget.setExtensionTopic(InputNormalizer.patternFit(targetBar.topicField.getText()));
            selectedTarget.setExtensionTemplate(template);
            TargetingTool.saveCategory(selectedTarget);
            nextTarget();
        }
    }


    class TemplateButtons extends JPanel
    {
        private JButton random = new JButton("<random>");
        private JButton sr = new JButton("<sr/>");
        private JButton srai = new JButton("<srai>");
        private JButton think = new JButton("<think>");
        private JButton reduce = new JButton("Reduce");
        private JButton clear = new JButton("Clear");

        public TemplateButtons ()
        {
            setLayout(new GridLayout(6, 1));
            random.addActionListener(new ActionListener ()
                                         {
                                            public void actionPerformed(ActionEvent ae)
                                            {
                                                String text = templatePanel.getText();
                                                text = text + "<random>\n    <li></li>\n    <li></li>\n    <li></li>\n</random>";
                                                templatePanel.setText(text);
                                            }
                                         });
            think.addActionListener(new ActionListener ()
                                        {
                                            public void actionPerformed(ActionEvent ae)
                                            {
                                                String text = templatePanel.getText();
                                                text = text + "<think>\n    <set name=\"it\"><set name=\"topic\"><person/></set></set>\n</think>";
                                                templatePanel.setText(text);
                                            }
                                        });
            sr.addActionListener(new ActionListener ()
                                     {
                                         public void actionPerformed(ActionEvent ae)
                                         {
                                             String text = templatePanel.getText();
                                             text = text + "<sr/>";
                                             templatePanel.setText(text);
                                         }
                                     });
            reduce.addActionListener(new ActionListener ()
                                         {
                                             public void actionPerformed(ActionEvent ae)
                                             {
                                                 String pattern = targetBar.patternField.getText();
                                                 StringTokenizer st = new StringTokenizer(pattern);
                                                 int n = st.countTokens();
                                                 String newpat="";
                                                 if (n > 2)
                                                 {
                                                     for (int i = 0; i < n-2; i++)
                                                     {
                                                         newpat += st.nextToken() + " ";
                                                     }
                                                     newpat += "<star/>";
                                                     newpat = "<srai>" + newpat + "</srai>";
                                                 }
                                                 else
                                                 {
                                                     newpat = "<sr/>";
                                                 }
                                                 String text = templatePanel.getText();
                                                 text = text + newpat;
                                                 templatePanel.setText(text);
                                             }
                                         });
            srai.addActionListener(new ActionListener ()
                                       {
                                            public void actionPerformed(ActionEvent ae)
                                            {
                                                String text = templatePanel.getText();
                                                text = text + "<srai> </srai>";
                                                templatePanel.setText(text);
                                            }
                                       });
            clear.addActionListener(new ActionListener ()
                                        {
                                            public void actionPerformed(ActionEvent ae)
                                            {
                                                templatePanel.setText("");
                                            }
                                        });
            think.setBackground(Color.orange);
            think.setFont(new Font("Fixedsys", Font.PLAIN, 12));

            random.setBackground(Color.orange);
            random.setFont(new Font("Fixedsys", Font.PLAIN, 12));

            sr.setBackground(Color.orange);
            sr.setFont(new Font("Fixedsys", Font.PLAIN, 12));

            srai.setBackground(Color.orange);
            srai.setFont(new Font("Fixedsys", Font.PLAIN, 12));

            reduce.setBackground(Color.orange);
            reduce.setFont(new Font("Fixedsys", Font.PLAIN, 12));

            clear.setBackground(Color.white);
            clear.setFont(new Font("Fixedsys", Font.PLAIN, 12));

            this.add(think);
            this.add(random);
            this.add(sr);
            this.add(srai);
            this.add(reduce);
            this.add(clear);
        }
    }
}
