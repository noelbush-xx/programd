/*
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2 of the License, or (at your option) any later
 * version. You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 */

package org.aitools.programd.interfaces.graphical;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumn;

/**
 * A generic panel that displays a sortable, loadable table.
 * 
 * @author Richard Wallace
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
abstract public class Tabulator extends JPanel
{
    /** The table displayed by the Tabulator. */
    protected JTable table;

    /**
     * The number of columns of data managed by the Tabulator (may be more than
     * the number of visible columns!).
     */
    protected int columnCount;

    /** The number of visible columns in the table. */
    private int visibleColumnCount;

    /** The TableModel that contains the data. */
    private TabulatorTableModel dataTableModel;

    /** The TableModel that sorts the data. */
    private TableSorter sorterTableModel;

    /**
     * Creates a new <code>Tabulator</code> with the given column names.
     * 
     * @param columnNames the column names
     */
    public Tabulator(String[] columnNames)
    {
        this.visibleColumnCount = columnNames.length;
        this.dataTableModel = new TabulatorTableModel(columnNames);

        this.sorterTableModel = new TableSorter(this.dataTableModel);
        this.table = new JTable(this.sorterTableModel);
        this.table.getTableHeader().setReorderingAllowed(false);
        this.table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        this.sorterTableModel.addMouseListenerToHeaderInTable(this.table);
        JScrollPane scrollPane = new JScrollPane(this.table);

        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(scrollPane);
    }

    /**
     * Reloads the table with given data.
     * 
     * @param data the data to load
     */
    public void reloadData(Object[][] data)
    {
        // Reload the data and update the column count.
        this.dataTableModel.setData(data);
        this.columnCount = data[0].length;

        // Size the columns to fit.
        TableColumn column = null;
        Component component = null;
        int headerWidth = 0;
        int cellWidth = 0;
        Object[] longestRow = this.dataTableModel.getLongestRow();
        if (longestRow == null)
        {
            return;
        }

        for (int index = 0; index < this.visibleColumnCount; index++)
        {
            column = this.table.getColumnModel().getColumn(index);
            component = this.table.getTableHeader().getDefaultRenderer().getTableCellRendererComponent(null, column.getHeaderValue(), false, false,
                    0, 0);
            headerWidth = component.getPreferredSize().width;

            component = this.table.getDefaultRenderer(this.sorterTableModel.getColumnClass(index)).getTableCellRendererComponent(this.table,
                    longestRow[index], false, false, 0, index);

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

        /**
         * @param columnNamesToSet
         */
        public TabulatorTableModel(String[] columnNamesToSet)
        {
            this.columnNames = columnNamesToSet;
        }

        /**
         * @see javax.swing.table.TableModel#getColumnCount()
         */
        public int getColumnCount()
        {
            return this.columnNames.length;
        }

        /**
         * @see javax.swing.table.TableModel#getRowCount()
         */
        public synchronized int getRowCount()
        {
            if (this.data == null)
            {
                return 0;
            }
            return this.data.length;
        }

        /**
         * @see javax.swing.table.AbstractTableModel#getColumnName(int)
         */
        public String getColumnName(int col)
        {
            return this.columnNames[col];
        }

        /**
         * @see javax.swing.table.TableModel#getValueAt(int, int)
         */
        public Object getValueAt(int row, int col)
        {
            return this.data[row][col];
        }

        /**
         * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
         */
        public Class< ? > getColumnClass(int c)
        {
            return getValueAt(0, c).getClass();
        }

        /**
         * Sets the table's data to the given data.
         * 
         * @param dataToSet
         */
        public synchronized void setData(Object[][] dataToSet)
        {
            this.data = dataToSet;
            fireTableDataChanged();
        }

        /**
         * @return the longest row
         */
        public Object[] getLongestRow()
        {
            if (this.data == null)
            {
                return null;
            }
            int longestLength = 0;
            int longestRow = 0;
            for (int row = 0; row < this.data.length; row++)
            {
                int rowLength = 0;
                for (int column = 0; column < this.columnNames.length; column++)
                {
                    rowLength += this.data[row][column].toString().length();
                }
                longestLength = rowLength > longestLength ? rowLength : longestLength;
                longestRow = rowLength > longestLength ? row : longestRow;
            }
            return this.data[longestRow];
        }
    }

    /**
     * @return the sorter table model
     */
    public TableSorter getSorterTableModel()
    {
        return this.sorterTableModel;
    }

    /**
     * @return the number of columns in the table
     */
    public int getColumnCount()
    {
        return this.columnCount;
    }

    /**
     * @return the table
     */
    public JTable getTable()
    {
        return this.table;
    }
}