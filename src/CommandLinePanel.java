import javax.swing.text.Document;
import javax.swing.text.BadLocationException;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Component;
import javax.swing.JScrollPane;
import javax.swing.Action;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.KeyStroke;
import java.awt.LayoutManager;
import java.awt.GridBagLayout;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.event.ActionListener;
import javax.swing.JPanel;

// 
// Decompiled by Procyon v0.5.30
// 

public class CommandLinePanel extends JPanel implements ActionListener, PrintableConsole
{
    protected JTextField textField;
    protected JTextArea textArea;
    private GUI gui;
    private final CommandLine cmd;
    private final Machine mac;
    
    public void setGUI(final GUI gui) {
        this.cmd.setGUI(gui);
        this.gui = gui;
    }
    
    public CommandLinePanel(final Machine mac, final CommandLine cmd) {
        super(new GridBagLayout());
        (this.textField = new JTextField(20)).addActionListener(this);
        this.textField.getInputMap().put(KeyStroke.getKeyStroke("UP"), "prevHistory");
        this.textField.getInputMap().put(KeyStroke.getKeyStroke("DOWN"), "nextHistory");
        this.textField.getActionMap().put("prevHistory", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent actionEvent) {
                CommandLinePanel.this.textField.setText(CommandLinePanel.this.cmd.getPrevHistory());
            }
        });
        this.textField.getActionMap().put("nextHistory", new AbstractAction() {
            @Override
            public void actionPerformed(final ActionEvent actionEvent) {
                CommandLinePanel.this.textField.setText(CommandLinePanel.this.cmd.getNextHistory());
            }
        });
        this.mac = mac;
        this.cmd = cmd;
        (this.textArea = new JTextArea(5, 70)).setEditable(false);
        this.textArea.setLineWrap(true);
        this.textArea.setWrapStyleWord(true);
        final JScrollPane scrollPane = new JScrollPane(this.textArea, 22, 30);
        final GridBagConstraints gridBagConstraints = new GridBagConstraints();
        gridBagConstraints.gridwidth = 0;
        gridBagConstraints.fill = 2;
        this.add(this.textField, gridBagConstraints);
        final GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
        gridBagConstraints2.gridwidth = 0;
        gridBagConstraints2.fill = 1;
        gridBagConstraints2.weightx = 1.0;
        gridBagConstraints2.weighty = 1.0;
        this.add(scrollPane, gridBagConstraints2);
        this.setMinimumSize(new Dimension(20, 1));
    }
    
    @Override
    public void clear() {
        final Document document = this.textArea.getDocument();
        try {
            document.remove(0, document.getLength());
        }
        catch (BadLocationException ex) {
            ErrorLog.logError(ex);
        }
    }
    
    @Override
    public void actionPerformed(final ActionEvent actionEvent) {
        if (actionEvent != null) {
            this.cmd.scheduleCommand(this.textField.getText());
        }
        while (this.cmd.hasMoreCommands()) {
            if (this.mac.isContinueMode()) {
                if (!this.cmd.hasQueuedStop()) {
                    break;
                }
            }
            try {
                final String runCommand = this.cmd.runCommand(this.cmd.getNextCommand());
                if (runCommand != null) {
                    if (runCommand.length() <= 0) {
                        continue;
                    }
                    Console.println(runCommand);
                }
                else {
                    this.gui.confirmExit();
                }
            }
            catch (ExceptionException ex) {
                ex.showMessageDialog(this.getParent());
            }
        }
        this.textField.selectAll();
        this.textArea.setCaretPosition(this.textArea.getDocument().getLength());
    }
    
    @Override
    public void print(final String s) {
        this.textArea.append(s);
    }
    
    public void reset() {
        this.cmd.reset();
    }
}
