/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.targeting.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;
import java.util.StringTokenizer;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.text.JTextComponent;

import org.aitools.programd.gui.ParentAwareActionListener;
import org.aitools.programd.targeting.Target;
import org.aitools.programd.util.InputNormalizer;
import org.aitools.programd.util.XMLKit;

/**
 * Presents a panel where users can create new categories from targets.
 * 
 * @author Richard Wallace
 * @author Noel Bush
 */
public class TargetPanel extends JPanel
{
    protected Target selectedTarget = null;

    JLabel activationsField;

    JLabel countField;

    public static Random RNG = new Random();

    TargetingGUI guiparent;

    InputBar inputBar;

    JScrollBar inputScroller;

    MatchedBar matchedBar;

    TargetBar targetBar;

    AIMLTextPane templatePane;

    AIMLTextPane replyPane;

    ActionButtonsBar actionButtonsBar;

    private boolean hasTarget;

    public TargetPanel(TargetingGUI guiparentToUse)
    {
        this.guiparent = guiparentToUse;

        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        PatternsPanel patternsPanel = new PatternsPanel(this);
        patternsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        TemplateAndReplyPanel templateAndReplyPanel = new TemplateAndReplyPanel(this);
        templateAndReplyPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

        this.actionButtonsBar = new ActionButtonsBar(this);
        this.actionButtonsBar.setAlignmentX(Component.LEFT_ALIGNMENT);

        this.add(patternsPanel);
        this.add(Box.createRigidArea(new Dimension(0, 10)));
        this.add(templateAndReplyPanel);
        this.add(Box.createRigidArea(new Dimension(0, 10)));
        this.add(this.actionButtonsBar);
    } 

    public class PatternsPanel extends JPanel
    {
        protected TargetPanel parent;

        public PatternsPanel(TargetPanel parentToUse)
        {
            this.parent = parentToUse;

            this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

            this.parent.activationsField = new JLabel();
            this.parent.activationsField.setFont(new Font("Fixedsys", Font.PLAIN, 12));
            this.parent.activationsField.setForeground(Color.black);
            this.parent.activationsField.setHorizontalAlignment(SwingConstants.LEFT);
            this.parent.activationsField.setAlignmentX(Component.LEFT_ALIGNMENT);
            this.parent.activationsField.setMinimumSize(new Dimension(200, 20));
            this.parent.activationsField.setPreferredSize(new Dimension(200, 20));
            this.parent.activationsField.setMaximumSize(new Dimension(200, 20));

            this.parent.inputScroller = new JScrollBar(JScrollBar.HORIZONTAL, 0, 0, 0, 0);
            this.parent.inputScroller.setAlignmentX(Component.LEFT_ALIGNMENT);
            this.parent.inputScroller.setBackground(Color.gray);
            this.parent.inputScroller.addAdjustmentListener(new NextInput(this.parent));

            this.parent.inputBar = new InputBar();
            this.parent.inputBar.setAlignmentX(Component.LEFT_ALIGNMENT);

            this.parent.matchedBar = new MatchedBar();
            this.parent.matchedBar.setAlignmentX(Component.LEFT_ALIGNMENT);

            this.parent.targetBar = new TargetBar();
            this.parent.targetBar.setAlignmentX(Component.LEFT_ALIGNMENT);

            this.add(Box.createRigidArea(new Dimension(0, 5)));
            this.add(this.parent.activationsField);
            this.add(this.parent.inputScroller);
            this.add(this.parent.inputBar);
            this.add(Box.createRigidArea(new Dimension(0, 5)));
            this.add(this.parent.matchedBar);
            this.add(Box.createRigidArea(new Dimension(0, 5)));
            this.add(this.parent.targetBar);
        } 
    } 

    public class TemplateAndReplyPanel extends JPanel
    {
        protected TargetPanel parent;

        public TemplateAndReplyPanel(TargetPanel parentToUse)
        {
            this.parent = parentToUse;

            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
            this.setPreferredSize(new Dimension(300, 300));

            TemplateButtons templateButtons = new TemplateButtons(this.parent);
            templateButtons.setAlignmentY(Component.TOP_ALIGNMENT);

            this.parent.templatePane = new AIMLTextPane("<template>");
            this.parent.replyPane = new AIMLTextPane("reply");

            JTabbedPane tabbedPane = new JTabbedPane();
            tabbedPane.setAlignmentY(Component.TOP_ALIGNMENT);
            tabbedPane.setFont(new Font("Fixedsys", Font.PLAIN, 12));
            tabbedPane.setTabPlacement(SwingConstants.BOTTOM);
            tabbedPane.add("reply", this.parent.replyPane);
            tabbedPane.add("template", this.parent.templatePane);

            this.add(templateButtons);
            this.add(Box.createRigidArea(new Dimension(10, 0)));
            this.add(tabbedPane);
        } 
    } 

    class AIMLTextPane extends JPanel
    {
        JTextArea textArea;

        public AIMLTextPane(String label)
        {
            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

            this.textArea = new JTextArea();
            this.textArea.setFont(new Font("Courier New", Font.PLAIN, 12));
            this.textArea.setLineWrap(true);
            this.textArea.setWrapStyleWord(true);
            this.textArea.setTabSize(4);

            JScrollPane scrollPane = new JScrollPane(this.textArea);
            scrollPane.setAlignmentY(Component.CENTER_ALIGNMENT);
            scrollPane.setBorder(BorderFactory.createTitledBorder(label));

            this.add(scrollPane);
        } 

        public void setText(String text)
        {
            this.textArea.setText(XMLKit.formatAIML(text));
            this.textArea.setCaretPosition(0);
        } 

        public String getText()
        {
            return this.textArea.getText();
        } 
    } 

    class ActionButtonsBar extends JPanel
    {
        protected TargetPanel parent;

        public ActionButtonsBar(TargetPanel parentToUse)
        {
            this.parent = parentToUse;

            this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));

            this.parent.countField = new JLabel();
            this.parent.countField.setFont(new Font("Fixedsys", Font.PLAIN, 12));
            this.parent.countField.setForeground(Color.black);
            this.parent.countField.setHorizontalAlignment(SwingConstants.LEFT);
            this.parent.countField.setAlignmentX(Component.BOTTOM_ALIGNMENT);

            JButton discard = new JButton("Discard Target");
            discard.setFont(new Font("Fixedsys", Font.PLAIN, 12));
            discard.setBackground(Color.red);
            discard.setForeground(Color.white);
            discard.addActionListener(new DiscardTarget(this.parent));
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

            this.add(this.parent.countField);
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
            this.patternField.setEditable(b);
            this.thatField.setEditable(b);
            this.topicField.setEditable(b);
        } 

        public void setFields(String pattern, String that, String topic)
        {
            this.patternField.setText(pattern);
            this.patternField.setCaretPosition(0);
            this.thatField.setText(that);
            this.thatField.setCaretPosition(0);
            this.topicField.setText(topic);
            this.topicField.setCaretPosition(0);
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

            this.patternField = new JTextArea();
            this.patternField.setLineWrap(true);
            this.patternField.setWrapStyleWord(true);
            this.patternField.setFont(new Font("Courier New", Font.PLAIN, 12));
            this.patternField.addKeyListener(new PatternFitter(this.patternField));

            JScrollPane patternScroll = new JScrollPane(this.patternField);
            patternScroll.setBorder(BorderFactory.createTitledBorder(patternLabel));
            patternScroll.setMinimumSize(new Dimension(200, height));
            patternScroll.setPreferredSize(new Dimension(200, height));
            patternScroll.setMaximumSize(new Dimension(Short.MAX_VALUE, height));
            patternScroll.setAlignmentY(Component.CENTER_ALIGNMENT);

            this.thatField = new JTextArea();
            this.thatField.setFont(new Font("Courier New", Font.PLAIN, 12));
            this.thatField.setLineWrap(true);
            this.thatField.setWrapStyleWord(true);
            this.thatField.addKeyListener(new PatternFitter(this.thatField));

            JScrollPane thatScroll = new JScrollPane(this.thatField);
            thatScroll.setMinimumSize(new Dimension(150, height));
            thatScroll.setPreferredSize(new Dimension(150, height));
            thatScroll.setMaximumSize(new Dimension(Short.MAX_VALUE, height));
            thatScroll.setBorder(BorderFactory.createTitledBorder(thatLabel));
            thatScroll.setAlignmentY(Component.CENTER_ALIGNMENT);

            this.topicField = new JTextArea();
            this.topicField.setFont(new Font("Courier New", Font.PLAIN, 12));
            this.topicField.setLineWrap(true);
            this.topicField.setWrapStyleWord(true);
            this.topicField.addKeyListener(new PatternFitter(this.topicField));

            JScrollPane topicScroll = new JScrollPane(this.topicField);
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

        public class PatternFitter implements KeyListener
        {
            private JTextComponent field;

            public PatternFitter(JTextComponent fieldToUse)
            {
                this.field = fieldToUse;
            } 

            public void keyTyped(KeyEvent ke)
            {
                char keyChar = ke.getKeyChar();
                if (keyChar == '*' || keyChar == '_')
                {
                    int caretPosition = this.field.getCaretPosition();
                    if (caretPosition > 0)
                    {
                        char prevChar = this.field.getText().charAt(caretPosition - 1);
                        if (prevChar != ' ')
                        {
                            this.field.setText(this.field.getText() + ' ');
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
                int caretPosition = this.field.getCaretPosition();
                if (caretPosition > 0)
                {
                    char prevChar = this.field.getText().charAt(caretPosition - 1);
                    if (prevChar == '*' || prevChar == '_')
                    {
                        this.field.setText(this.field.getText() + ' ');
                    } 
                } 
            } 

            public void keyPressed(KeyEvent ke)
            {
                // Do nothing.
            } 

            public void keyReleased(KeyEvent ke)
            {
                // Do nothing.
            } 
        } 
    } 

    class InputBar extends CategoryBar
    {
        public InputBar()
        {
            super("Input:", "text", "<that>", "<topic>", 56);
            this.patternField.setToolTipText("What the user said");
            this.thatField.setToolTipText("What the bot had said previously");
            this.topicField.setToolTipText("The value of <topic> at the time");
            setEditable(false);
        } 
    } 

    class MatchedBar extends CategoryBar
    {
        public MatchedBar()
        {
            super("Matched:", "<pattern>", "<that>", "<topic>", 56);
            this.patternField.setToolTipText("The <pattern> that was matched");
            this.thatField.setToolTipText("The <that> that was matched");
            this.topicField.setToolTipText("The <topic> that was matched");
            setEditable(false);
        } 
    } 

    class TargetBar extends CategoryBar
    {
        public TargetBar()
        {
            super("New category:", "<pattern>", "<that>", "<topic>", 56);
            this.patternField.setToolTipText("Suggestion for a new <pattern>");
            this.thatField.setToolTipText("Suggestion for a new <that>");
            this.topicField.setToolTipText("Suggestion for a new <topic>");
            setEditable(true);
        } 
    } 

    public void setTarget(Target target)
    {
        this.selectedTarget = target;

        int activations = target.getActivations();
        this.activationsField.setText(activations + " activations");

        this.inputBar.setFields(target.getFirstInputText(), target.getFirstInputThat(), target.getFirstInputTopic());

        this.inputScroller.setMinimum(1);
        this.inputScroller.setMaximum(activations);
        this.inputScroller.setValue(1);
        showInput(1);

        this.matchedBar.setFields(target.getMatchPattern(), target.getMatchThat(), target.getMatchTopic());

        this.targetBar.setFields(target.getFirstExtensionPattern(), target.getFirstExtensionThat(), target
                .getFirstExtensionTopic());

        this.replyPane.setText(target.getFirstReply());

        this.templatePane.setText(target.getMatchTemplate());
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
            this.hasTarget = true;
        } 
        else
        {
            this.inputBar.setFields("", "", "");

            this.inputScroller.setMinimum(0);
            this.inputScroller.setMaximum(0);
            this.inputScroller.setValue(0);

            this.matchedBar.setFields("", "", "");

            this.targetBar.setFields("", "", "");
            //targetBar.setEditable(false);

            this.templatePane.setText("");

            this.guiparent.setStatus("No more targets meet your selection criteria.");
            this.hasTarget = false;
        } 
        updateCountDisplay();
    } 

    class NextInput implements AdjustmentListener
    {
        protected TargetPanel parent;

        public NextInput(TargetPanel parentToUse)
        {
            this.parent = parentToUse;
        } 

        public void adjustmentValueChanged(AdjustmentEvent ae)
        {
            showInput(this.parent.inputScroller.getValue());
        } 
    } 

    protected void showInput(int number)
    {
        if (number > 0 && this.selectedTarget != null)
        {
            this.inputBar.setFields(this.selectedTarget.getNthInputText(number - 1), this.selectedTarget
                    .getNthInputThat(number - 1), this.selectedTarget.getNthInputTopic(number - 1));
            this.targetBar.setFields(this.selectedTarget.getNthExtensionPattern(number - 1), this.selectedTarget
                    .getNthExtensionThat(number - 1), this.selectedTarget.getNthExtensionTopic(number - 1));
            this.replyPane.setText(this.selectedTarget.getNthReply(number - 1));
        } 
    } 

    public void scrollToInput(int number)
    {
        this.inputScroller.setValue(number);
        showInput(number);
    } 

    public boolean hasTarget()
    {
        return this.hasTarget;
    } 

    class DiscardTarget extends ParentAwareActionListener
    {
        public DiscardTarget(TargetPanel parentToUse)
        {
            super(parentToUse);
        } 

        public void actionPerformed(ActionEvent ae)
        {
            TargetingTool.discard(((TargetPanel) this.parent).selectedTarget);
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
        this.countField.setText(TargetingTool.countLive() + " live, " + TargetingTool.countSaved() + " saved, "
                + TargetingTool.countDiscarded() + " discarded");
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
        String template = this.templatePane.getText().trim();
        if (template.length() == 0)
        {
            this.templatePane.setText("Template is empty!");
            return;
        } 
        if (this.selectedTarget != null)
        {
            this.selectedTarget.setNewPattern(InputNormalizer.patternFit(this.targetBar.patternField.getText()));
            this.selectedTarget.setNewThat(InputNormalizer.patternFit(this.targetBar.thatField.getText()));
            this.selectedTarget.setNewTopic(InputNormalizer.patternFit(this.targetBar.topicField.getText()));
            this.selectedTarget.setNewTemplate(template);
            TargetingTool.saveCategory(this.selectedTarget);
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

        protected TargetPanel parent;

        public TemplateButtons(TargetPanel parentToUse)
        {
            this.parent = parentToUse;

            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
            this.random.addActionListener(new ParentAwareActionListener(this.parent)
            {
                public void actionPerformed(ActionEvent ae)
                {
                    ((TargetPanel) this.parent).templatePane.setText("<random><li>"
                            + ((TargetPanel) this.parent).templatePane.getText() + "</li><li></random>");
                } 
            } );
            this.think.addActionListener(new ParentAwareActionListener(this.parent)
            {
                public void actionPerformed(ActionEvent ae)
                {
                    ((TargetPanel) this.parent).templatePane.setText("<think>"
                            + ((TargetPanel) this.parent).templatePane.getText() + "</think>");
                } 
            } );
            this.sr.addActionListener(new ParentAwareActionListener(this.parent)
            {
                public void actionPerformed(ActionEvent ae)
                {
                    ((TargetPanel) this.parent).templatePane.setText(((TargetPanel) this.parent).templatePane.getText()
                            + "<sr/>");
                } 
            } );
            this.reduce.addActionListener(new ParentAwareActionListener(this.parent)
            {
                public void actionPerformed(ActionEvent ae)
                {
                    String pattern = ((TargetPanel) this.parent).targetBar.patternField.getText();
                    StringTokenizer st = new StringTokenizer(pattern);
                    int n = st.countTokens();
                    String newpat = "";
                    if (n > 2)
                    {
                        for (int i = 0; i < n - 2; i++)
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
                    ((TargetPanel) this.parent).templatePane.setText(((TargetPanel) this.parent).templatePane.getText()
                            + newpat);
                } 
            } );
            this.srai.addActionListener(new ParentAwareActionListener(this.parent)
            {
                public void actionPerformed(ActionEvent ae)
                {
                    ((TargetPanel) this.parent).templatePane.setText("<srai>"
                            + ((TargetPanel) this.parent).templatePane.getText() + "</srai>");
                } 
            } );
            this.clear.addActionListener(new ParentAwareActionListener(this.parent)
            {
                public void actionPerformed(ActionEvent ae)
                {
                    ((TargetPanel) this.parent).templatePane.setText("");
                } 
            } );

            this.think.setBackground(Color.orange);
            this.think.setFont(new Font("Fixedsys", Font.PLAIN, 12));
            this.think.setMinimumSize(new Dimension(120, 30));
            this.think.setPreferredSize(new Dimension(120, 30));
            this.think.setMaximumSize(new Dimension(120, 30));
            this.think.setAlignmentY(Component.CENTER_ALIGNMENT);
            this.think.setToolTipText("Encloses the current template contents in a <think> tag.");

            this.random.setBackground(Color.orange);
            this.random.setFont(new Font("Fixedsys", Font.PLAIN, 12));
            this.random.setMinimumSize(new Dimension(120, 30));
            this.random.setPreferredSize(new Dimension(120, 30));
            this.random.setMaximumSize(new Dimension(120, 30));
            this.random.setAlignmentY(Component.CENTER_ALIGNMENT);
            this.random.setToolTipText("Encloses the current template contents in a <random> tag.");

            this.sr.setBackground(Color.orange);
            this.sr.setFont(new Font("Fixedsys", Font.PLAIN, 12));
            this.sr.setMinimumSize(new Dimension(120, 30));
            this.sr.setPreferredSize(new Dimension(120, 30));
            this.sr.setMaximumSize(new Dimension(120, 30));
            this.sr.setAlignmentY(Component.CENTER_ALIGNMENT);

            this.srai.setBackground(Color.orange);
            this.srai.setFont(new Font("Fixedsys", Font.PLAIN, 12));
            this.srai.setMinimumSize(new Dimension(120, 30));
            this.srai.setPreferredSize(new Dimension(120, 30));
            this.srai.setMaximumSize(new Dimension(120, 30));
            this.srai.setAlignmentY(Component.CENTER_ALIGNMENT);
            this.srai.setToolTipText("Encloses the current template contents in a <srai> tag.");

            this.reduce.setBackground(Color.orange);
            this.reduce.setFont(new Font("Fixedsys", Font.PLAIN, 12));
            this.reduce.setMinimumSize(new Dimension(120, 30));
            this.reduce.setPreferredSize(new Dimension(120, 30));
            this.reduce.setMaximumSize(new Dimension(120, 30));
            this.reduce.setAlignmentY(Component.CENTER_ALIGNMENT);

            this.clear.setBackground(Color.white);
            this.clear.setFont(new Font("Fixedsys", Font.PLAIN, 12));
            this.clear.setMinimumSize(new Dimension(120, 30));
            this.clear.setPreferredSize(new Dimension(120, 30));
            this.clear.setMaximumSize(new Dimension(120, 30));
            this.clear.setAlignmentY(Component.CENTER_ALIGNMENT);
            this.clear.setToolTipText("Clears the current template contents.");

            this.add(this.think);
            this.add(this.random);
            this.add(this.sr);
            this.add(this.srai);
            this.add(this.reduce);
            this.add(this.clear);
        } 
    } 
}