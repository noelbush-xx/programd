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
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

import org.alicebot.server.core.targeting.Target;
import org.alicebot.server.core.util.Trace;


/**
 *  A generic panel that displays a sortable, loadable table.
 *
 *  @author Richard Wallace
 *  @author Noel Bush
 */
abstract public class Tabulator extends JPanel
{
    /** The table displayed by the Tabulator. */
    private JTable table;

    /** The number of columns of data managed by the Tabulator (may be more than the number of visible columns!). */
    private int columnCount;

    /** The number of visible columns in the table. */
    private int visibleColumnCount;

    /** The TableModel that contains the data. */
    private TabulatorTableModel dataTableModel;

    /** The TableModel that sorts the data. */
    private TableSorter sorterTableModel;

    TargetingGUI guiparent;


    /**
     *  Creates a new <code>Tabulator</code> with the given column names.
     *
     *  @param columnNames  the column names
     */
    public Tabulator(String[] columnNames)
    {
        visibleColumnCount = columnNames.length;
        dataTableModel = new TabulatorTableModel(columnNames);

        sorterTableModel = new TableSorter(dataTableModel);
        table = new JTable(sorterTableModel);
        table.getTableHeader().setReorderingAllowed(false);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.addMouseListener(new TargetOpener());

        sorterTableModel.addMouseListenerToHeaderInTable(table); 
        JScrollPane scrollPane = new JScrollPane(table);

        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(scrollPane);
    }


    /**
     *  Reloads the table with given data.
     *
     *  @param data the data to load
     */
    public void reloadData(Object[][] data)
    {
        // Reload the data and update the column count.
        dataTableModel.setData(data);
        this.columnCount = data[0].length;

        // Size the columns to fit.
        TableColumn column = null;
        Component component = null;
        int headerWidth = 0;
        int cellWidth = 0;
        Object[] longestRow = dataTableModel.getLongestRow();
        if (longestRow == null)
        {
            return;
        }

        for (int index = 0; index < visibleColumnCount; index++)
        {
            column = table.getColumnModel().getColumn(index);
            component = table.getTableHeader().getDefaultRenderer().
                                 getTableCellRendererComponent(
                                     null, column.getHeaderValue(), 
                                     false, false, 0, 0);
            headerWidth = component.getPreferredSize().width;

            component = table.getDefaultRenderer(sorterTableModel.getColumnClass(index)).
                                 getTableCellRendererComponent(
                                     table, longestRow[index],
                                     false, false, 0, index);

            cellWidth = component.getPreferredSize().width;
            column.setPreferredWidth(Math.max(headerWidth, cellWidth));
        }
    }


    private class TabulatorTableModel extends AbstractTableModel
    {
        /** The names of the columns in the table. */
        private String[] columnNames;

        /** The data of the columns in the table. */
        private Object[][] data;


        public TabulatorTableModel(String[] columnNames)
        {
            this.columnNames = columnNames;
        }

        public int getColumnCount()
        {
            return columnNames.length;
        }
        
        public synchronized int getRowCount()
        {
            if (data == null)
            {
                return 0;
            }
            return data.length;
        }

        public String getColumnName(int col)
        {
            return columnNames[col];
        }

        public Object getValueAt(int row, int col)
        {
            return data[row][col];
        }

        public Class getColumnClass(int c)
        {
            return getValueAt(0, c).getClass();
        }

        public synchronized void setData(Object[][] data)
        {
            this.data = data;
            fireTableDataChanged();
        }

        public Object[] getLongestRow()
        {
            if (this.data == null)
            {
                return null;
            }
            int longestLength = 0;
            int longestRow = 0;
            for (int row = 0; row < data.length; row++)
            {
                int rowLength = 0;
                for (int column = 0; column < columnNames.length; column++)
                {
                    rowLength += data[row][column].toString().length();
                }
                longestLength = rowLength > longestLength ? rowLength : longestLength;
                longestRow = rowLength > longestLength ? row : longestRow;
            }
            return this.data[longestRow];
        }
    }


    private class TargetOpener extends MouseAdapter
    {
        public void mouseClicked(MouseEvent me)
        {
            if (me.getClickCount() == 2)
            {
                Target target = (Target)sorterTableModel.getValueAt(table.rowAtPoint(me.getPoint()),
                                                                    columnCount - 2);
                int input = ((Integer)sorterTableModel.getValueAt(table.rowAtPoint(me.getPoint()),
                                                                  columnCount - 1)).intValue();
                guiparent.targetPanel.setTarget(target);
                guiparent.targetPanel.scrollToInput(input);
                guiparent.viewTargets();
            }
        }
    }
}

