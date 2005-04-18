package org.aitools.programd.interfaces.graphical;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 */
public class ListDialog extends JDialog
{
    protected static ListDialog dialog;

    protected static String value = "";

    protected JList list;

    /**
     * Sets up the dialog. The first argument can be null, but it really should
     * be a component in the dialog's controlling frame.
     * 
     * @param comp the component to which to attach the dialog
     * @param possibleValues the possible values to show in the dialog
     * @param title the title of the dialog
     * @param labelText the text of the label in the dialog
     */
    public static void initialize(Component comp, String[] possibleValues, String title, String labelText)
    {
        Frame frame = JOptionPane.getFrameForComponent(comp);
        dialog = new ListDialog(frame, possibleValues, title, labelText);
    }

    /**
     * Show the initialized dialog. The first argument should be null if you
     * want the dialog to come up in the center of the screen. Otherwise, the
     * argument should be the component on top of which the dialog should
     * appear.
     * 
     * @param comp the component to which to set the relative location of the
     *            dialog
     * @param initialValue the initial value of the dialog
     * @return the value of the dialog
     */
    public static String showDialog(Component comp, String initialValue)
    {
        if (dialog != null)
        {
            dialog.setValue(initialValue);
            dialog.setLocationRelativeTo(comp);
            dialog.setVisible(true);
        }
        else
        {
            System.err.println("ListDialog requires you to call initialize before calling showDialog.");
        }
        return value;
    }

    private void setValue(String newValue)
    {
        value = newValue;
        this.list.setSelectedValue(value, true);
    }

    private ListDialog(Frame frame, Object[] data, String title, String labelText)
    {
        super(frame, title, true);

        JButton cancelButton = new JButton("Cancel");
        final JButton setButton = new JButton("Set");
        cancelButton.addActionListener(new ActionEventIgnoringActionListener()
        {
            public void actionPerformed()
            {
                dialog.setVisible(false);
            }
        });
        setButton.addActionListener(new ActionEventIgnoringActionListener()
        {
            public void actionPerformed()
            {
                ListDialog.value = (String) ListDialog.this.list.getSelectedValue();
                ListDialog.dialog.setVisible(false);
            }
        });
        getRootPane().setDefaultButton(setButton);

        this.list = new JList(data);
        this.list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
        this.list.addMouseListener(new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                if (e.getClickCount() == 2)
                {
                    setButton.doClick();
                }
            }
        });
        JScrollPane listScroller = new JScrollPane(this.list);
        listScroller.setPreferredSize(new Dimension(250, 80));

        // Must do the following, too, or else the scroller thinks it's taller
        // than it is:
        listScroller.setMinimumSize(new Dimension(250, 80));
        listScroller.setAlignmentX(LEFT_ALIGNMENT);

        // Create a container so that we can add a title around
        // the scroll pane. Can't add a title directly to the
        // scroll pane because its background would be white.
        // Lay out the label and scroll pane from top to button.
        JPanel listPane = new JPanel();
        listPane.setLayout(new BoxLayout(listPane, BoxLayout.Y_AXIS));
        JLabel label = new JLabel(labelText);
        label.setLabelFor(this.list);
        listPane.add(label);
        listPane.add(Box.createRigidArea(new Dimension(0, 5)));
        listPane.add(listScroller);
        listPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Lay out the buttons from left to right.
        JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));
        buttonPane.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        buttonPane.add(Box.createHorizontalGlue());
        buttonPane.add(cancelButton);
        buttonPane.add(Box.createRigidArea(new Dimension(10, 0)));
        buttonPane.add(setButton);

        // Put everything together, using the content pane's BorderLayout.
        Container contentPane = getContentPane();
        contentPane.add(listPane, BorderLayout.CENTER);
        contentPane.add(buttonPane, BorderLayout.SOUTH);

        pack();
    }
}