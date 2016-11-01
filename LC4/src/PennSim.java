import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.SwingUtilities;

// 
// Decompiled by Procyon v0.5.30
// 

public class PennSim
{
    public static String version;
    public static boolean GRAPHICAL_MODE;
    public static boolean PIPELINE_MODE;
    public static boolean DOUBLE_BUFFERED_VIDEO_MODE;
    public static boolean LC3;
    public static boolean P37X;
    
    public static boolean isGraphical() {
        return PennSim.GRAPHICAL_MODE;
    }
    
    public static boolean isPipelined() {
        return PennSim.PIPELINE_MODE;
    }
    
    public static boolean isLC3() {
        return PennSim.LC3;
    }
    
    public static boolean isP37X() {
        return PennSim.P37X;
    }
    
    public static boolean isDoubleBufferedVideo() {
        return PennSim.DOUBLE_BUFFERED_VIDEO_MODE;
    }
    
    public static String getISA() {
        if (PennSim.LC3) {
            return "LC4 ISA";
        }
        if (PennSim.P37X) {
            return "P37X ISA";
        }
        return null;
    }
    
    public static String getVersion() {
        return "PennSim Version " + PennSim.version;
    }
    
    private static void printUsage() {
        System.out.println("\nUsage: java PennSim [-lc3] [-p37x] [-pipeline] [-t] [-s script]");
        System.out.println("  -lc3 : simulate the LC-3 ISA");
        System.out.println("  -p37x : simulate the P37X ISA");
        System.out.println("  -pipeline : simulate a 5-stage fully-bypassed pipeline");
        System.out.println("  -t : start in command-line mode");
        System.out.println("  -d : double-buffered video mode");
        System.out.println("  -s script : run 'script' from a script file");
    }
    
    public static void main(final String[] array) {
        String s = null;
        System.out.println(getVersion() + "\n");
        for (int i = 0; i < array.length; ++i) {
            if (array[i].equalsIgnoreCase("-t")) {
                PennSim.GRAPHICAL_MODE = false;
            }
            else if (array[i].equalsIgnoreCase("-d")) {
                PennSim.DOUBLE_BUFFERED_VIDEO_MODE = true;
            }
            else if (array[i].equalsIgnoreCase("-s")) {
                if (++i >= array.length) {
                    System.out.println("Error: -s requires a script filename");
                    return;
                }
                s = array[i];
            }
            else if (array[i].equalsIgnoreCase("-lc3")) {
                PennSim.LC3 = true;
            }
            else if (array[i].equalsIgnoreCase("-p37x")) {
                PennSim.P37X = true;
            }
            else {
                if (!array[i].equalsIgnoreCase("-pipeline")) {
                    System.out.println("Arg '" + array[i] + "' not recognized");
                    printUsage();
                    return;
                }
                PennSim.PIPELINE_MODE = true;
            }
        }
        if (PennSim.LC3 && PennSim.P37X) {
            System.out.println("Error: can't specify more than one ISA");
            printUsage();
            return;
        }
        if (!PennSim.LC3 && !PennSim.P37X) {
            System.out.println("Error: ISA not specified");
            printUsage();
            return;
        }
        System.out.println(getISA());
        final Machine machine = new Machine();
        final CommandLine commandLine = new CommandLine(machine);
        if (s != null) {
            commandLine.scheduleCommand("@script " + s);
        }
        if (PennSim.GRAPHICAL_MODE) {
            System.out.println("Loading graphical interface");
            if (PennSim.DOUBLE_BUFFERED_VIDEO_MODE) {
                System.out.println("(Double buffered video mode)");
            }
            GUI.initLookAndFeel();
            final GUI gui = new GUI(machine, commandLine);
            machine.setGUI(gui);
            SwingUtilities.invokeLater(new TempRun(gui));
        }
        else {
            try {
                final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            Block_25:
                while (true) {
                    if (!machine.isContinueMode()) {
                        System.out.print(CommandLine.PROMPT);
                    }
                    if (s == null) {
                        final String line = bufferedReader.readLine();
                        if (line != null) {
                            commandLine.scheduleCommand(line);
                        }
                    }
                    while (commandLine.hasMoreCommands() && (!machine.isContinueMode() || commandLine.hasQueuedStop())) {
                        final String nextCommand = commandLine.getNextCommand();
                        if (s != null && !nextCommand.startsWith("@")) {
                            s = null;
                        }
                        String s2;
                        try {
                            s2 = commandLine.runCommand(nextCommand);
                        }
                        catch (ExceptionException ex) {
                            s2 = ex.getExceptionDescription();
                        }
                        catch (NumberFormatException ex2) {
                            s2 = "NumberFormatException: " + ex2.getMessage();
                        }
                        if (s2 == null) {
                            break Block_25;
                        }
                        System.out.println(s2);
                    }
                    if (s != null && !commandLine.hasMoreCommands()) {
                        s = null;
                    }
                }
                machine.cleanup();
                System.out.println("Bye!");
            }
            catch (IOException ex3) {
                ErrorLog.logError(ex3);
            }
        }
    }
    
    static {
        PennSim.version = "1.4.2-LC4 $Rev: 1229 $";
        PennSim.GRAPHICAL_MODE = true;
        PennSim.PIPELINE_MODE = false;
        PennSim.DOUBLE_BUFFERED_VIDEO_MODE = false;
        PennSim.LC3 = true;
        PennSim.P37X = false;
    }
}
