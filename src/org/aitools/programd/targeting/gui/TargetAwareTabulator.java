/*    
    This program is free software; you can redistribute it and/or
    modify it under the terms of the GNU General Public License
    as published by the Free Software Foundation; either version 2
    of the License, or (at your option) any later version.
    
    You should have received a copy of the GNU General Public License
    along with this program; if not, write to the Free Software
    Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, 
    USA.
*/

package org.aitools.programd.targeting.gui;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import org.aitools.programd.gui.Tabulator;
import org.aitools.programd.targeting.Target;

/**
 *  A target-aware Tabulator is a Tabulator whose rows when clicked open a target.
 *
 *  @author Noel Bush
 */
abstract public class TargetAwareTabulator extends Tabulator
{
    protected TargetingGUI guiparent;

    /**
     *  Creates a new <code>TargetAwareTabulator</code> with the given column names.
     *
     *  @param columnNames  the column names
     */
    public TargetAwareTabulator(String[] columnNames, TargetingGUI guiparent)
    {
        super(columnNames);
        this.guiparent = guiparent;
        table.addMouseListener(new TargetOpener(this));
    }

    private class TargetOpener extends MouseAdapter
    {
        private Tabulator parent;
        
        public TargetOpener(Tabulator parent)
        {
            this.parent = parent;
        }
        
        public void mouseClicked(MouseEvent me)
        {
            if (me.getClickCount() == 2)
            {
                Target target =
                    (Target) parent.getSorterTableModel().getValueAt(
                        parent.getTable().rowAtPoint(me.getPoint()),
                        parent.getColumnCount() - 2);
                int input =
                    ((Integer) parent.getSorterTableModel()
                        .getValueAt(
                            parent.getTable().rowAtPoint(me.getPoint()),
                            parent.getColumnCount() - 1))
                        .intValue();
                guiparent.targetPanel.setTarget(target);
                guiparent.targetPanel.scrollToInput(input);
                guiparent.viewTargets();
            }
        }
    }
}
