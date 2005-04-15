package org.aitools.programd.interfaces.graphical;

import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Date;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.aitools.programd.util.DeveloperError;

/**
 * A sorter for TableModels. The sorter has a model (conforming to TableModel)
 * and itself implements TableModel. TableSorter does not store or copy the data
 * in the TableModel, instead it maintains an array of integers which it keeps
 * the same size as the number of rows in its model. When the model changes it
 * notifies the sorter that something has changed eg. "rowsAdded" so that its
 * internal array of integers can be reallocated. As requests are made of the
 * sorter (like getValueAt(row, col) it redirects them to its model via the
 * mapping array. That way the TableSorter appears to hold another copy of the
 * table with the rows in a different order. The sorting algorthm used is stable
 * which means that it does not move around rows when its comparison function
 * returns 0 to denote that they are equivalent.
 * 
 * @version 1.5 12/17/97
 * @author Philip Milne
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class TableSorter extends TableMap
{
    int indexes[];

    Vector<Integer> sortingColumns = new Vector<Integer>();

    boolean ascending = true;

    int compares;

    /**
     * Creates a new TableSorter.
     */
    public TableSorter()
    {
        this.indexes = new int[0];
    }

    /**
     * @param modelToSet
     */
    public TableSorter(TableModel modelToSet)
    {
        setModel(modelToSet);
    }

    /**
     * @see org.aitools.programd.interfaces.graphical.TableMap#setModel(javax.swing.table.TableModel)
     */
    public void setModel(TableModel modelToSet)
    {
        super.setModel(modelToSet);
        reallocateIndexes();
    }

    /**
     * Compares the given rows in the given column
     * 
     * @param row1 one row
     * @param row2 the other row
     * @param column the column in which to compare
     * @return an indicator of the comparison of the two columns (?)
     */
    public int compareRowsByColumn(int row1, int row2, int column)
    {
        Class<?> type = this.model.getColumnClass(column);
        TableModel data = this.model;

        // Check for nulls.
        Object o1 = data.getValueAt(row1, column);
        Object o2 = data.getValueAt(row2, column);

        // If both values are null, return 0.
        if (o1 == null && o2 == null)
        {
            return 0;
        }
        // Define null less than everything.
        else if (o1 == null)
        {
            return -1;
        }
        else if (o2 == null)
        {
            return 1;
        }

        /*
         * We copy all returned values from the getValue call in case an
         * optimised model is reusing one object to return many values. The
         * Number subclasses in the JDK are immutable and so will not be used in
         * this way but other subclasses of Number might want to do this to save
         * space and avoid unnecessary heap allocation.
         */

        if (type.getSuperclass() == java.lang.Number.class)
        {
            Number n1 = (Number) data.getValueAt(row1, column);
            double d1 = n1.doubleValue();
            Number n2 = (Number) data.getValueAt(row2, column);
            double d2 = n2.doubleValue();

            if (d1 < d2)
            {
                return -1;
            }
            else if (d1 > d2)
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }
        else if (type == java.util.Date.class)
        {
            Date d1 = (Date) data.getValueAt(row1, column);
            long n1 = d1.getTime();
            Date d2 = (Date) data.getValueAt(row2, column);
            long n2 = d2.getTime();

            if (n1 < n2)
            {
                return -1;
            }
            else if (n1 > n2)
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }
        else if (type == String.class)
        {
            String s1 = (String) data.getValueAt(row1, column);
            String s2 = (String) data.getValueAt(row2, column);
            int result = s1.compareTo(s2);

            if (result < 0)
            {
                return -1;
            }
            else if (result > 0)
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }
        else if (type == Boolean.class)
        {
            Boolean bool1 = (Boolean) data.getValueAt(row1, column);
            boolean b1 = bool1.booleanValue();
            Boolean bool2 = (Boolean) data.getValueAt(row2, column);
            boolean b2 = bool2.booleanValue();

            if (b1 == b2)
            {
                return 0;
            }
            // Define false < true.
            else if (b1)
            {
                return 1;
            }
            else
            {
                return -1;
            }
        }
        else
        {
            Object v1 = data.getValueAt(row1, column);
            String s1 = v1.toString();
            Object v2 = data.getValueAt(row2, column);
            String s2 = v2.toString();
            int result = s1.compareTo(s2);

            if (result < 0)
            {
                return -1;
            }
            else if (result > 0)
            {
                return 1;
            }
            else
            {
                return 0;
            }
        }
    }

    /**
     * Compares two rows.
     * 
     * @param row1 one row
     * @param row2 another row
     * @return the result of comparing the two rows
     */
    public int compare(int row1, int row2)
    {
        this.compares++;
        for (int level = 0; level < this.sortingColumns.size(); level++)
        {
            Integer column = this.sortingColumns.elementAt(level);
            int result = compareRowsByColumn(row1, row2, column.intValue());
            if (result != 0)
            {
                return this.ascending ? result : -result;
            }
        }
        return 0;
    }

    /**
     * Sets up a new array of indices with the correct number of elements for
     * the new data model, and initializes with the identity mapping.
     */
    public void reallocateIndexes()
    {
        int rowCount = this.model.getRowCount();

        /*
         * Set up a new array of indexes with the right number of elements for
         * the new data model.
         */
        this.indexes = new int[rowCount];

        // Initialise with the identity mapping.
        for (int row = 0; row < rowCount; row++)
        {
            this.indexes[row] = row;
        }
    }

    /**
     * @see org.aitools.programd.interfaces.graphical.TableMap#tableChanged(javax.swing.event.TableModelEvent)
     */
    public void tableChanged(TableModelEvent e)
    {
        reallocateIndexes();
        super.tableChanged(e);
    }

    /**
     * Checks that the model is valid (has not been changed without informing
     * the sorter).
     */
    public void checkModel()
    {
        if (this.indexes.length != this.model.getRowCount())
        {
            throw new DeveloperError(new IllegalStateException("Sorter not informed of a change in model."));
        }
    }

    /**
     * 
     */
    public void sort()
    {
        checkModel();

        this.compares = 0;
        shuttlesort(this.indexes.clone(), this.indexes, 0, this.indexes.length);
    }

    /**
     * Sorts the table according to an n2 algorithm.
     */
    public void n2sort()
    {
        for (int i = 0; i < getRowCount(); i++)
        {
            for (int j = i + 1; j < getRowCount(); j++)
            {
                if (compare(this.indexes[i], this.indexes[j]) == -1)
                {
                    swap(i, j);
                }
            }
        }
    }

    /**
     * This is a home-grown implementation which we have not had time to
     * research - it may perform poorly in some circumstances. It requires twice
     * the space of an in-place algorithm and makes NlogN assigments shuttling
     * the values between the two arrays. The number of compares appears to vary
     * between N-1 and NlogN depending on the initial order but the main reason
     * for using it here is that, unlike qsort, it is stable.
     * 
     * @param from ?
     * @param to ?
     * @param low ?
     * @param high ?
     */
    public void shuttlesort(int from[], int to[], int low, int high)
    {
        if (high - low < 2)
        {
            return;
        }
        int middle = (low + high) / 2;
        shuttlesort(to, from, low, middle);
        shuttlesort(to, from, middle, high);

        int p = low;
        int q = middle;

        /*
         * This is an optional short-cut; at each recursive call, check to see
         * if the elements in this subset are already ordered. If so, no further
         * comparisons are needed; the sub-array can just be copied. The array
         * must be copied rather than assigned otherwise sister calls in the
         * recursion might get out of sinc. When the number of elements is three
         * they are partitioned so that the first set, [low, mid), has one
         * element and and the second, [mid, high), has two. We skip the
         * optimisation when the number of elements is three or less as the
         * first compare in the normal merge will produce the same sequence of
         * steps. This optimisation seems to be worthwhile for partially ordered
         * lists but some analysis is needed to find out how the performance
         * drops to Nlog(N) as the initial order diminishes - it may drop very
         * quickly.
         */

        if (high - low >= 4 && compare(from[middle - 1], from[middle]) <= 0)
        {
            for (int i = low; i < high; i++)
            {
                to[i] = from[i];
            }
            return;
        }

        // A normal merge.
        for (int i = low; i < high; i++)
        {
            if (q >= high || (p < middle && compare(from[p], from[q]) <= 0))
            {
                to[i] = from[p++];
            }
            else
            {
                to[i] = from[q++];
            }
        }
    }

    /**
     * Swaps the cells indicated by <code>i</code> and <code>j</code>.
     * 
     * @param i a cell
     * @param j a cell
     */
    public void swap(int i, int j)
    {
        int tmp = this.indexes[i];
        this.indexes[i] = this.indexes[j];
        this.indexes[j] = tmp;
    }

    // The mapping only affects the contents of the data rows.
    // Pass all requests to these rows through the mapping array: "indexes".

    /**
     * @see org.aitools.programd.interfaces.graphical.TableMap#getValueAt(int,
     *      int)
     */
    public Object getValueAt(int aRow, int aColumn)
    {
        checkModel();
        return this.model.getValueAt(this.indexes[aRow], aColumn);
    }

    /**
     * @see org.aitools.programd.interfaces.graphical.TableMap#setValueAt(java.lang.Object,
     *      int, int)
     */
    public void setValueAt(Object aValue, int aRow, int aColumn)
    {
        checkModel();
        this.model.setValueAt(aValue, this.indexes[aRow], aColumn);
    }

    /**
     * Sorts the table by the specified column.
     * 
     * @param column the column by which to sort
     */
    public void sortByColumn(int column)
    {
        sortByColumn(column, true);
    }

    /**
     * Sorts by the specified column, either ascending or descending.
     * 
     * @param column the column by which to sort
     * @param ascendingSetting whether to sort ascending
     */
    public void sortByColumn(int column, boolean ascendingSetting)
    {
        this.ascending = ascendingSetting;
        this.sortingColumns.removeAllElements();
        this.sortingColumns.addElement(new Integer(column));
        sort();
        super.tableChanged(new TableModelEvent(this));
    }

    /**
     * Adds a mouse listener to the header of the given table to trigger a table
     * sort when a column heading is clicked in the JTable.
     * 
     * @param table the table to whose header to add a mouse listener.
     */
    public void addMouseListenerToHeaderInTable(JTable table)
    {
        final TableSorter sorter = this;
        final JTable tableView = table;
        tableView.setColumnSelectionAllowed(false);
        MouseAdapter listMouseListener = new MouseAdapter()
        {
            public void mouseClicked(MouseEvent e)
            {
                TableColumnModel columnModel = tableView.getColumnModel();
                int viewColumn = columnModel.getColumnIndexAtX(e.getX());
                int column = tableView.convertColumnIndexToModel(viewColumn);
                if (e.getClickCount() == 1 && column != -1)
                {
                    int shiftPressed = e.getModifiers() & InputEvent.SHIFT_MASK;
                    sorter.sortByColumn(column, (shiftPressed == 0));
                }
            }
        };
        JTableHeader th = tableView.getTableHeader();
        th.addMouseListener(listMouseListener);
    }
}