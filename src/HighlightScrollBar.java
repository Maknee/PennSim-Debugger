import java.awt.Component;
import java.awt.Cursor;
import javax.swing.BorderFactory;
import java.awt.Color;
import java.awt.event.ActionListener;
import javax.swing.table.TableModel;
import javax.swing.event.TableModelEvent;
import java.util.Hashtable;
import javax.swing.JButton;
import java.util.Map;
import javax.swing.event.TableModelListener;
import javax.swing.JScrollBar;

// 
// Decompiled by Procyon v0.5.30
// 

public class HighlightScrollBar extends JScrollBar implements TableModelListener
{
    private static final int MARK_HEIGHT = 4;
    private static final int SCROLL_BUTTON_SIZE = 15;
    private Map<Integer, JButton> highlights;
    private double scaleFactor;
    private JButton PCButton;
    private Machine mac;
    
    public HighlightScrollBar(final Machine mac) {
        this.scaleFactor = 1.0;
        this.highlights = new Hashtable<Integer, JButton>();
        this.mac = mac;
    }
    
    @Override
    public void tableChanged(final TableModelEvent tableModelEvent) {
        final TableModel tableModel = (TableModel)tableModelEvent.getSource();
        this.scaleFactor = tableModel.getRowCount() / (this.getHeight() - 30);
        final int firstRow = tableModelEvent.getFirstRow();
        if (tableModel.getValueAt(firstRow, 0).equals(Boolean.TRUE)) {
            final JButton button = new JButton();
            button.setToolTipText((String)tableModel.getValueAt(firstRow, 1));
            button.setActionCommand(String.valueOf(firstRow));
            button.addActionListener(this.mac.getGUI());
            button.setSize(this.getWidth() - 5, 4);
            button.setForeground(GUI.BreakPointColor);
            button.setBackground(GUI.BreakPointColor);
            button.setBorder(BorderFactory.createLineBorder(Color.RED));
            button.setOpaque(true);
            button.setCursor(new Cursor(12));
            button.setLocation(3, (int)(firstRow / this.scaleFactor) + 15);
            this.highlights.put(firstRow, button);
            this.add(button);
        }
        else {
            assert tableModel.getValueAt(firstRow, 0).equals(Boolean.FALSE);
            final JButton button2 = this.highlights.remove(firstRow);
            if (button2 != null) {
                this.remove(button2);
            }
        }
        this.repaint(0L, 0, (int)(firstRow / this.scaleFactor) + 15, this.getWidth(), 4);
    }
}
