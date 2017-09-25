import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.LinkedList;
import java.util.ListIterator;

// 
// Decompiled by Procyon v0.5.30
// 

public class MonitorDevice
{
    private static final Word MONITOR_READY;
    private static final Word MONITOR_NOTREADY;
    private OutputStreamWriter dout;
    private LinkedList<ActionListener> mlist;
    
    public MonitorDevice() {
        if (!PennSim.GRAPHICAL_MODE) {
            this.dout = new OutputStreamWriter(System.out);
        }
        else {
            this.mlist = new LinkedList<ActionListener>();
        }
    }
    
    public MonitorDevice(final OutputStream outputStream) {
        this.dout = new OutputStreamWriter(outputStream);
    }
    
    public void addActionListener(final ActionListener actionListener) {
        this.mlist.add(actionListener);
    }
    
    public Word status() {
        if (this.ready()) {
            return MonitorDevice.MONITOR_READY;
        }
        return MonitorDevice.MONITOR_NOTREADY;
    }
    
    public boolean ready() {
        if (PennSim.GRAPHICAL_MODE) {
            return true;
        }
        try {
            this.dout.flush();
            return true;
        }
        catch (IOException ex) {
            ErrorLog.logError(ex);
            return false;
        }
    }
    
    public void reset() {
        if (PennSim.GRAPHICAL_MODE) {
            final ListIterator<ActionListener> listIterator = this.mlist.listIterator();
            while (listIterator.hasNext()) {
                listIterator.next().actionPerformed(new ActionEvent(new Integer(1), 0, null));
            }
        }
    }
    
    public void write(final char c) {
        if (PennSim.GRAPHICAL_MODE) {
            final ListIterator<ActionListener> listIterator = this.mlist.listIterator();
            while (listIterator.hasNext()) {
                listIterator.next().actionPerformed(new ActionEvent(c + "", 0, null));
            }
        }
        else {
            try {
                this.dout.write(c);
                this.dout.flush();
            }
            catch (IOException ex) {
                ErrorLog.logError(ex);
            }
        }
    }
    
    static {
        MONITOR_READY = new Word(32768);
        MONITOR_NOTREADY = new Word(0);
    }
}
