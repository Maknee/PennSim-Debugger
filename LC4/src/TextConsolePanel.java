import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;
import java.awt.event.ActionEvent;
import java.io.InputStream;
import java.io.IOException;
import java.awt.Component;
import java.io.PipedOutputStream;
import java.io.PipedInputStream;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.event.ActionListener;
import java.awt.event.FocusListener;
import java.awt.event.KeyListener;
import javax.swing.JPanel;

// 
// Decompiled by Procyon v0.5.30
// 

public class TextConsolePanel extends JPanel implements KeyListener, FocusListener, ActionListener
{
    private JTextArea screen;
    private JScrollPane spane;
    private KeyboardDevice kbd;
    private MonitorDevice monitor;
    private PipedInputStream kbin;
    private PipedOutputStream kbout;
    
    TextConsolePanel(final KeyboardDevice kbd, final MonitorDevice monitor) {
        (this.screen = new JTextArea(5, 21)).setEditable(false);
        this.screen.addKeyListener(this);
        this.screen.addFocusListener(this);
        this.screen.setLineWrap(true);
        this.screen.setWrapStyleWord(true);
        this.spane = new JScrollPane(this.screen, 22, 30);
        this.kbd = kbd;
        this.kbout = new PipedOutputStream();
        try {
            this.kbin = new PipedInputStream(this.kbout);
        }
        catch (IOException ex) {
            ErrorLog.logError(ex);
        }
        kbd.setInputStream(this.kbin);
        kbd.setDefaultInputStream();
        kbd.setInputMode(KeyboardDevice.INTERACTIVE_MODE);
        kbd.setDefaultInputMode();
        (this.monitor = monitor).addActionListener(this);
        this.add(this.spane);
    }
    
    @Override
    public void actionPerformed(final ActionEvent actionEvent) {
        if (actionEvent.getSource() instanceof Integer) {
            final Document document = this.screen.getDocument();
            try {
                document.remove(0, document.getLength());
            }
            catch (BadLocationException ex) {
                Console.println(ex.getMessage());
            }
        }
        else {
            this.screen.append((String)actionEvent.getSource());
        }
    }
    
    @Override
    public void keyReleased(final KeyEvent keyEvent) {
    }
    
    @Override
    public void keyPressed(final KeyEvent keyEvent) {
    }
    
    @Override
    public void keyTyped(final KeyEvent keyEvent) {
        final char keyChar = keyEvent.getKeyChar();
        try {
            this.kbout.write(keyChar);
            this.kbout.flush();
        }
        catch (IOException ex) {
            ErrorLog.logError(ex);
        }
    }
    
    @Override
    public void focusGained(final FocusEvent focusEvent) {
        this.screen.setBackground(Color.yellow);
    }
    
    @Override
    public void focusLost(final FocusEvent focusEvent) {
        this.screen.setBackground(Color.white);
    }
    
    @Override
    public void setEnabled(final boolean enabled) {
        this.screen.setEnabled(enabled);
        if (enabled) {
            this.screen.setBackground(Color.white);
        }
        else {
            this.screen.setBackground(Color.gray);
        }
    }
}
