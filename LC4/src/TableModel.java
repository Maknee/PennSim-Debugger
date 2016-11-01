import javax.swing.event.TableModelEvent;
import javax.swing.table.AbstractTableModel;

// 
// Decompiled by Procyon v0.5.30
// 

public abstract class TableModel extends AbstractTableModel
{
    @Override
    public void fireTableCellUpdated(final int n, final int n2) {
        if (PennSim.GRAPHICAL_MODE) {
            super.fireTableCellUpdated(n, n2);
        }
    }
    
    @Override
    public void fireTableChanged(final TableModelEvent tableModelEvent) {
        if (PennSim.GRAPHICAL_MODE) {
            super.fireTableChanged(tableModelEvent);
        }
    }
    
    @Override
    public void fireTableDataChanged() {
        if (PennSim.GRAPHICAL_MODE) {
            super.fireTableDataChanged();
        }
    }
    
    @Override
    public void fireTableRowsUpdated(final int n, final int n2) {
        if (PennSim.GRAPHICAL_MODE) {
            super.fireTableRowsUpdated(n, n2);
        }
    }
    
    @Override
    public void fireTableRowsInserted(final int n, final int n2) {
        if (PennSim.GRAPHICAL_MODE) {
            super.fireTableRowsInserted(n, n2);
        }
    }
    
    @Override
    public void fireTableRowsDeleted(final int n, final int n2) {
        if (PennSim.GRAPHICAL_MODE) {
            super.fireTableRowsDeleted(n, n2);
        }
    }
    
    @Override
    public void fireTableStructureChanged() {
        if (PennSim.GRAPHICAL_MODE) {
            super.fireTableStructureChanged();
        }
    }
}
