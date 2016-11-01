import java.awt.Component;
import javax.swing.JOptionPane;
import java.awt.Container;

// 
// Decompiled by Procyon v0.5.30
// 

public abstract class ExceptionException extends Exception
{
    public ExceptionException() {
    }
    
    public ExceptionException(final String s) {
        super(s);
    }
    
    public String getExceptionDescription() {
        return "Generic Exception: " + this.getMessage();
    }
    
    public void showMessageDialog(final Container container) {
        JOptionPane.showMessageDialog(container, this.getExceptionDescription());
        Console.println("Exception: " + this.getExceptionDescription());
    }
}
