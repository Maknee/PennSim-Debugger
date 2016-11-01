import java.util.LinkedList;
import java.util.Iterator;
import java.util.List;

// 
// Decompiled by Procyon v0.5.30
// 

public class Console
{
    public static final String NEWLINE;
    private static List consoles;
    
    public Console() {
        throw new UnsupportedOperationException("Console is meant to be used statically.");
    }
    
    public static void registerConsole(final PrintableConsole printableConsole) {
        Console.consoles.add(printableConsole);
    }
    
    public static void println(final String s) {
        if (PennSim.isGraphical()) {
            final Iterator<PrintableConsole> iterator = Console.consoles.iterator();
            while (iterator.hasNext()) {
                iterator.next().print(s + Console.NEWLINE);
            }
        }
        else {
            System.out.println(s);
        }
    }
    
    public static void clear() {
        if (PennSim.isGraphical()) {
            final Iterator<PrintableConsole> iterator = Console.consoles.iterator();
            while (iterator.hasNext()) {
                iterator.next().clear();
            }
        }
    }
    
    static {
        NEWLINE = System.getProperty("line.separator");
        Console.consoles = new LinkedList();
    }
}
