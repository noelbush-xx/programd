package org.aitools.programd.interfaces.graphical;

import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

/**
 * In a chain of data manipulators some behaviour is common. TableMap provides
 * most of this behavour and can be subclassed by filters that only need to
 * override a handful of specific methods. TableMap implements TableModel by
 * routing all requests to its model, and TableModelListener by routing all
 * events to its listeners. Inserting a TableMap which has not been subclassed
 * into a chain of table filters should have no effect.
 * 
 * @version 1.4 12/17/97
 * @author Philip Milne
 * @author <a href="mailto:noel@aitools.org">Noel Bush</a>
 */
public class TableMap extends AbstractTableModel implements TableModelListener
{
    protected TableModel model;

    /**
     * @return the table model
     */
    public TableModel getModel()
    {
        return this.model;
    }

    /**
     * Sets the TableMap's model to the given model.
     * 
     * @param modelToSet the model to set
     */
    public synchronized void setModel(TableModel modelToSet)
    {
        this.model = modelToSet;
        this.model.addTableModelListener(this);
    }

    // By default, implement TableModel by forwarding all messages
    // to the model.

    /**
     * @see javax.swing.table.TableModel#getValueAt(int, int)
     */
    public Object getValueAt(int aRow, int aColumn)
    {
        return this.model.getValueAt(aRow, aColumn);
    }

    /**
     * @see javax.swing.table.AbstractTableModel#setValueAt(java.lang.Object,
     *      int, int)
     */
    public void setValueAt(Object aValue, int aRow, int aColumn)
    {
        this.model.setValueAt(aValue, aRow, aColumn);
    }

    /**
     * @see javax.swing.table.TableModel#getRowCount()
     */
    public synchronized int getRowCount()
    {
        return (this.model == null) ? 0 : this.model.getRowCount();
    }

    /**
     * @see javax.swing.table.TableModel#getColumnCount()
     */
    public int getColumnCount()
    {
        return (this.model == null) ? 0 : this.model.getColumnCount();
    }

    /**
     * @see javax.swing.table.AbstractTableModel#getColumnName(int)
     */
    public String getColumnName(int aColumn)
    {
        return this.model.getColumnName(aColumn);
    }

    /**
     * @see javax.swing.table.AbstractTableModel#getColumnClass(int)
     */
    public Class< ? > getColumnClass(int aColumn)
    {
        return this.model.getColumnClass(aColumn);
    }

    /**
     * @see javax.swing.table.AbstractTableModel#isCellEditable(int, int)
     */
    public boolean isCellEditable(int row, int column)
    {
        return this.model.isCellEditable(row, column);
    }

    /**
     * @see javax.swing.event.TableModelListener#tableChanged(javax.swing.event.TableModelEvent)
     */
    public void tableChanged(TableModelEvent e)
    {
        fireTableChanged(e);
    }
}