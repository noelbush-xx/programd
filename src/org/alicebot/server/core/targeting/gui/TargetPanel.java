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
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.Box.Filler;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.text.JTextComponent;

import org.alicebot.server.core.targeting.Target;
import org.alicebot.server.core.targeting.TargetingTool;
import org.alicebot.server.core.util.InputNormalizer;
import org.alicebot.server.core.util.Toolkit;


/**
 *  Presents a panel where users can create new categories from targets.
 *
 *  @author Richard Wallace
 *  @author Noel Bush
 */
public class TargetPanel extends JPanel
{
    private Target selectedTarget = null;
    JLabel countField;

    public static Random RNG = new Random();
    TargetingGUI guiparent;
    InputBar inputBar;
    TargetBar targetBar;
    ActionButtonsBar actionButtonsBar;
    MatchedBar matchedBar;
    TemplatePanel templatePanel;

    private boolean hasTarget;


    public TargetPanel (TargetingGUI guiparent)
    {
        this.guiparent = guiparent;

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

            this.add(Box.createRigidArea(new Dimension(0, 5)));
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
            this.setPreferredSize(new Dimension(300, 300));

            TemplateButtons templateButtons = new TemplateButtons();
            templateButtons.setAlignmentY(Component.TOP_ALIGNMENT);

            textArea = new JTextArea();
            textArea.setFont(new Font("Courier New", Font.PLAIN, 14));
            textArea.setLineWrap(true);
            textArea.setWrapStyleWord(true);
            textArea.setTabSize(4);

            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setBorder(BorderFactory.createTitledBorder("<template>"));
            scrollPane.setAlignmentY(Component.TOP_ALIGNMENT);

            this.add(templateButtons);
            this.add(Box.createRigidArea(new Dimension(10, 0)));
            this.add(scrollPane);
        }
        public void setText(String text)
        {
            textArea.setText(Toolkit.formatAIML(text));
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
            discard.setToolTipText("Discards the current target.");

            JButton discardAll = new JButton("Discard All Targets");
            discardAll.setFont(new Font("Fixedsys", Font.BOLD, 12));
            discardAll.setBackground(Color.red);
            discardAll.setForeground(Color.white);
            discardAll.addActionListener(new DiscardAllTargets());
            discardAll.setAlignmentX(Component.BOTTOM_ALIGNMENT);
            discardAll.setToolTipText("Discards all targets.");

            JButton save = new JButton("Save Category");
            save.setFont(new Font("Fixedsys", Font.PLAIN, 12));
            save.setBackground(Color.green);
            save.setForeground(Color.black);
            save.addActionListener(new SaveTarget());
            save.setAlignmentX(Component.BOTTOM_ALIGNMENT);
            save.setToolTipText("Saves a new category using the information you have entered.");

            JButton next = new JButton("Next Target");
            next.setFont(new Font("Fixedsys", Font.PLAIN, 12));
            next.setBackground(Color.yellow);
            next.setForeground(Color.black);
            next.addActionListener(new NextTarget());
            next.setAlignmentX(Component.BOTTOM_ALIGNMENT);
            save.setToolTipText("Gets the next live target.");

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
        JTextArea patternField;
        JTextArea thatField;
        JTextArea topicField;


        public void setEditable(boolean b)
        {
            patternField.setEditable(b);
            thatField.setEditable(b);
            topicField.setEditable(b);
        }


        public void setFields(String pattern, String that, String topic)
        {
            patternField.setText(pattern);
            patternField.setSelectionStart(0);
            thatField.setText(that);
            thatField.setSelectionStart(0);
            topicField.setText(topic);
            topicField.setSelectionStart(0);
        }


        public CategoryBar(String barLabel, String patternLabel, String thatLabel, String topicLabel, int height)
        {
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

            JLabel label = new JLabel(barLabel);
            label.setMinimumSize(new Dimension(80, height));
            label.setPreferredSize(new Dimension(80, height));
            label.setMaximumSize(new Dimension(80, height));
            label.setHorizontalAlignment(SwingConstants.LEFT);
            label.setFont(new Font("Fixedsys", Font.PLAIN, 12));
            label.setForeground(Color.black);
            label.setAlignmentY(Component.CENTER_ALIGNMENT);

            patternField = new FieldlikeTextArea();
            patternField.setLineWrap(true);
            patternField.setWrapStyleWord(true);
            patternField.setFont(new Font("Courier New", Font.PLAIN, 12));
            patternField.addKeyListener(new PatternFitter(patternField));

            JScrollPane patternScroll = new JScrollPane(patternField);
            patternScroll.setBorder(BorderFactory.createTitledBorder(patternLabel));
            patternScroll.setMinimumSize(new Dimension(200, height));
            patternScroll.setPreferredSize(new Dimension(200, height));
            patternScroll.setMaximumSize(new Dimension(Short.MAX_VALUE, height));
            patternScroll.setAlignmentY(Component.CENTER_ALIGNMENT);

            thatField = new FieldlikeTextArea();
            thatField.setFont(new Font("Courier New", Font.PLAIN, 12));
            thatField.setLineWrap(true);
            thatField.setWrapStyleWord(true);
            thatField.addKeyListener(new PatternFitter(thatField));

            JScrollPane thatScroll = new JScrollPane(thatField);
            thatScroll.setMinimumSize(new Dimension(150, height));
            thatScroll.setPreferredSize(new Dimension(150, height));
            thatScroll.setMaximumSize(new Dimension(Short.MAX_VALUE, height));
            thatScroll.setBorder(BorderFactory.createTitledBorder(thatLabel));
            thatScroll.setAlignmentY(Component.CENTER_ALIGNMENT);

            topicField = new FieldlikeTextArea();
            topicField.setFont(new Font("Courier New", Font.PLAIN, 12));
            topicField.setLineWrap(true);
            topicField.setWrapStyleWord(true);
            topicField.addKeyListener(new PatternFitter(topicField));

            JScrollPane topicScroll = new JScrollPane(topicField);
            topicScroll.setMinimumSize(new Dimension(150, height));
            topicScroll.setPreferredSize(new Dimension(150, height));
            topicScroll.setMaximumSize(new Dimension(Short.MAX_VALUE, height));
            topicScroll.setBorder(BorderFactory.createTitledBorder(topicLabel));
            topicScroll.setAlignmentY(Component.CENTER_ALIGNMENT);

            this.add(label);
            this.add(patternScroll);
            this.add(thatScroll);
            this.add(topicScroll);
        }


        public class FieldlikeTextArea extends JTextArea
        {
            // Overrides JTextArea's prevention of tabs.
            protected void processKeyEvent(KeyEvent e)
            {
            }
        }


        public class PatternFitter implements KeyListener
        {
            private JTextComponent field;


            public PatternFitter(JTextComponent field)
            {
                this.field = field;
            }


            public void keyTyped(KeyEvent ke)
            {
                char keyChar = ke.getKeyChar();
                if (keyChar == '*' || keyChar == '_')
                {
                    int caretPosition = field.getCaretPosition();
                    if (caretPosition > 0)
                    {
                        char prevChar = field.getText().charAt(caretPosition - 1);
                        if (prevChar != ' ')
                        {
                            field.setText(field.getText() + ' ');
                        }
                    }
                    return;
                }
                if (!Character.isLetterOrDigit(keyChar) && keyChar != ' ')
                {
                    ke.consume();
                }
                if (!Character.isUpperCase(keyChar))
                {
                    ke.setKeyChar(Character.toUpperCase(keyChar));
                }
                int caretPosition = field.getCaretPosition();
                if (caretPosition > 0)
                {
                    char prevChar = field.getText().charAt(caretPosition - 1);
                    if (prevChar == '*' || prevChar == '_')
                    {
                        field.setText(field.getText() + ' ');
                    }
                }
            }


            public void keyPressed(KeyEvent ke)
            {
            }


            public void keyReleased(KeyEvent ke)
            {
            }
        }
    } 


    class InputBar extends CategoryBar
    {
        public InputBar()
        {
            super("Input:", "text", "<that>", "<topic>", 56);
            patternField.setToolTipText("What the user said");
            thatField.setToolTipText("What the bot had said previously");
            topicField.setToolTipText("The value of <topic> at the time");
            setEditable(false);
        }
    }


    class MatchedBar extends CategoryBar
    {
        public MatchedBar()
        {
            super("Matched:", "<pattern>", "<that>", "<topic>", 56);
            patternField.setToolTipText("The <pattern> that was matched");
            thatField.setToolTipText("The <that> that was matched");
            topicField.setToolTipText("The <topic> that was matched");
            setEditable(false);
        }
    }


    class TargetBar extends CategoryBar
    {
        public TargetBar()
        {
            super("New category:", "<pattern>", "<that>", "<topic>", 56);
            patternField.setToolTipText("Suggestion for a new <pattern>");
            thatField.setToolTipText("Suggestion for a new <that>");
            topicField.setToolTipText("Suggestion for a new <topic>");
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
        templatePanel.setText(target.getMatchTemplate());
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
        Target next = TargetingTool.nextTarget();
        if (next != null)
        {
            setTarget(next);
            hasTarget = true;
        }
        else
        {
            inputBar.setFields("*","*","*");
            matchedBar.setFields("*","*","*");
            targetBar.setFields("*","*","*");
            guiparent.setStatus("No more targets!");
            hasTarget = false;
        }
        updateCountDisplay();
    }


    public boolean hasTarget()
    {
        return hasTarget;
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
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            random.addActionListener(new ActionListener ()
                                         {
                                            public void actionPerformed(ActionEvent ae)
                                            {
                                                String text = templatePanel.getText();
                                                text = text + "<random>\n    <li>" + text + "</li>\n    <li></li>\n    <li></li>\n</random>";
                                                templatePanel.setText(text);
                                            }
                                         });
            think.addActionListener(new ActionListener ()
                                        {
                                            public void actionPerformed(ActionEvent ae)
                                            {
                                                String text = templatePanel.getText();
                                                text = "<think>\n" + "    " + text + "\n</think>";
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
                                                text = "<srai>" + text + "</srai>";
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
            think.setMinimumSize(new Dimension(120, 30));
            think.setPreferredSize(new Dimension(120, 30));
            think.setMaximumSize(new Dimension(120, 30));
            think.setAlignmentY(Component.CENTER_ALIGNMENT);
            think.setToolTipText("Encloses the current template contents in a <think> tag.");

            random.setBackground(Color.orange);
            random.setFont(new Font("Fixedsys", Font.PLAIN, 12));
            random.setMinimumSize(new Dimension(120, 30));
            random.setPreferredSize(new Dimension(120, 30));
            random.setMaximumSize(new Dimension(120, 30));
            random.setAlignmentY(Component.CENTER_ALIGNMENT);
            random.setToolTipText("Encloses the current template contents in a <random> tag.");

            sr.setBackground(Color.orange);
            sr.setFont(new Font("Fixedsys", Font.PLAIN, 12));
            sr.setMinimumSize(new Dimension(120, 30));
            sr.setPreferredSize(new Dimension(120, 30));
            sr.setMaximumSize(new Dimension(120, 30));
            sr.setAlignmentY(Component.CENTER_ALIGNMENT);

            srai.setBackground(Color.orange);
            srai.setFont(new Font("Fixedsys", Font.PLAIN, 12));
            srai.setMinimumSize(new Dimension(120, 30));
            srai.setPreferredSize(new Dimension(120, 30));
            srai.setMaximumSize(new Dimension(120, 30));
            srai.setAlignmentY(Component.CENTER_ALIGNMENT);
            srai.setToolTipText("Encloses the current template contents in a <srai> tag.");

            reduce.setBackground(Color.orange);
            reduce.setFont(new Font("Fixedsys", Font.PLAIN, 12));
            reduce.setMinimumSize(new Dimension(120, 30));
            reduce.setPreferredSize(new Dimension(120, 30));
            reduce.setMaximumSize(new Dimension(120, 30));
            reduce.setAlignmentY(Component.CENTER_ALIGNMENT);

            clear.setBackground(Color.white);
            clear.setFont(new Font("Fixedsys", Font.PLAIN, 12));
            clear.setMinimumSize(new Dimension(120, 30));
            clear.setPreferredSize(new Dimension(120, 30));
            clear.setMaximumSize(new Dimension(120, 30));
            clear.setAlignmentY(Component.CENTER_ALIGNMENT);
            clear.setToolTipText("Clears the current template contents.");

            this.add(Box.createRigidArea(new Dimension(0, 10)));
            this.add(think);
            this.add(random);
            this.add(sr);
            this.add(srai);
            this.add(reduce);
            this.add(clear);
        }
    }
}
