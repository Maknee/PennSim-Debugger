import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Stack;

import javax.swing.SwingUtilities;

// 
// Decompiled by Procyon v0.5.30
// 

public class Machine implements Runnable
{
    private Memory memory;
    private RegisterFile registers;
    private BranchPredictor bpred;
    private String src_filename;
    private int src_line_number;
    private GUI gui;
    private Stack<Integer> callStack;
    private LinkedList<ActionListener> NotifyOnStop;
    private PrintWriter traceWriter;
    private final SymbolTable symTable;
    private final SimSrcTable srcTable;
    public int CYCLE_COUNT;
    public int INSTRUCTION_COUNT;
    public int LOAD_STALL_COUNT;
    public int BRANCH_STALL_COUNT;
    public static final int NUM_CONTINUES = 400;
    boolean stopImmediately;
    private boolean continueMode;
    
    public SymbolTable getSymTable() {
        return this.symTable;
    }
    
    public SimSrcTable getSrcTable() {
        return this.srcTable;
    }
    
    public Machine() {
        this.src_filename = null;
        this.src_line_number = -1;
        this.gui = null;
        this.callStack = null;
        this.traceWriter = null;
        this.symTable = new SymbolTable();
        this.srcTable = new SimSrcTable();
        this.CYCLE_COUNT = 0;
        this.INSTRUCTION_COUNT = 0;
        this.LOAD_STALL_COUNT = 0;
        this.BRANCH_STALL_COUNT = 0;
        this.stopImmediately = false;
        this.continueMode = false;
        if (PennSim.isP37X()) {
            new P37X().init();
        }
        else if (PennSim.isLC3()) {
            new LC3().init();
        }
        this.memory = new Memory(this);
        this.registers = new RegisterFile(this);
        this.bpred = new BranchPredictor(this, 8);
        this.NotifyOnStop = new LinkedList<ActionListener>();
        this.callStack = new Stack<Integer>();
    }
    
    public void setBranchPredictor(final int n) {
        this.bpred = new BranchPredictor(this, n);
    }
    
    public void setGUI(final GUI gui) {
        this.gui = gui;
    }
    
    public GUI getGUI() {
        return this.gui;
    }
    
    public void setStoppedListener(final ActionListener actionListener) {
        this.NotifyOnStop.add(actionListener);
    }
    
    public void reset() {
        this.symTable.clear();
        this.srcTable.reset();
        this.memory.reset();
        this.registers.reset();
        if (this.gui != null) {
            this.gui.reset();
        }
        if (this.isTraceEnabled()) {
            this.disableTrace();
        }
    }
    
    public void cleanup() {
        ErrorLog.logClose();
        if (this.isTraceEnabled()) {
            this.disableTrace();
        }
    }
    
    public Memory getMemory() {
        return this.memory;
    }
    
    public RegisterFile getRegisterFile() {
        return this.registers;
    }
    
    public BranchPredictor getBranchPredictor() {
        return this.bpred;
    }
    
    public Stack getCallStack() {
    	return callStack;
    }
    
    public void setTraceWriter(final PrintWriter traceWriter) {
        this.traceWriter = traceWriter;
    }
    
    public PrintWriter getTraceWriter() {
        return this.traceWriter;
    }
    
    public boolean isTraceEnabled() {
        return this.traceWriter != null;
    }
    
    public void disableTrace() {
        this.traceWriter.close();
        this.traceWriter = null;
    }
    
    public boolean isContinueMode() {
        return this.continueMode;
    }
    
    public void setContinueMode() {
        this.continueMode = true;
    }
    
    public void clearContinueMode() {
        this.continueMode = false;
    }
    
    public String loadObjectFile(final String s) {
        final String string = s + ".obj";
        return "Loading object file " + string + ":" + this.loadImage(string) + this.symTable.loadSymbolTable(string) + this.srcTable.loadSrcTable(string);
    }
    
    public String loadImage(final String s) {
        final byte[] array = new byte[2];
        String s2;
        try {
            final FileInputStream fileInputStream = new FileInputStream(new File(s));
            while (fileInputStream.read(array) == 2) {
                final int convertByteArray = Word.convertByteArray(array[0], array[1]);
                final String hex = Word.toHex(convertByteArray);
                if (convertByteArray == 51934 || convertByteArray == 56026) {
                    if (fileInputStream.read(array) < 2) {
                        throw new IOException("Missing " + hex + " section address");
                    }
                    int convertByteArray2 = Word.convertByteArray(array[0], array[1]);
                    if (fileInputStream.read(array) < 2) {
                        throw new IOException("Missing " + hex + " section size");
                    }
                    final int convertByteArray3 = Word.convertByteArray(array[0], array[1]);
                    if (convertByteArray2 + convertByteArray3 > 65536) {
                        throw new IOException("Bad " + hex + " section size");
                    }
                    int n = convertByteArray3;
                    while (n-- > 0) {
                        if (fileInputStream.read(array) < 2) {
                            throw new IOException("Unexpectedly short " + hex + " section");
                        }
                        this.memory.write(convertByteArray2, Word.convertByteArray(array[0], array[1]));
                        ++convertByteArray2;
                    }
                }
                else if (convertByteArray == 50103) {
                    if (fileInputStream.read(array) < 2) {
                        throw new IOException("Missing " + hex + " section address");
                    }
                    if (fileInputStream.read(array) < 2) {
                        throw new IOException("Missing " + hex + " section size");
                    }
                    final long n2 = Word.convertByteArray(array[0], array[1]);
                    if (fileInputStream.skip(n2) < n2) {
                        throw new IOException("Unexpectedly short " + hex + " section");
                    }
                    continue;
                }
                else if (convertByteArray == 61822) {
                    if (fileInputStream.read(array) < 2) {
                        throw new IOException("Missing " + hex + " section size");
                    }
                    final long n3 = Word.convertByteArray(array[0], array[1]);
                    if (fileInputStream.skip(n3) < n3) {
                        throw new IOException("Unexpectedly short " + hex + " section");
                    }
                    continue;
                }
                else {
                    if (convertByteArray != 29022) {
                        throw new IOException("Unknown section " + hex);
                    }
                    if (fileInputStream.skip(6L) < 6L) {
                        throw new IOException("Unexpectedly short " + hex + " section");
                    }
                    continue;
                }
            }
            fileInputStream.close();
            s2 = " code and data ... ";
        }
        catch (IOException ex) {
            return "Error: Could not load code or data (" + ex.toString() + ")";
        }
        return s2;
    }
    
    public String setKeyboardInputStream(final File file) {
        String s;
        try {
            this.memory.getKeyBoardDevice().setInputStream(new FileInputStream(file));
            this.memory.getKeyBoardDevice().setInputMode(KeyboardDevice.SCRIPT_MODE);
            s = "Keyboard input file '" + file.getPath() + "' enabled";
            if (this.gui != null) {
                this.gui.setTextConsoleEnabled(false);
            }
        }
        catch (FileNotFoundException ex) {
            s = "Could not open keyboard input file '" + file.getPath() + "'";
            if (this.gui != null) {
                this.gui.setTextConsoleEnabled(true);
            }
        }
        return s;
    }
    
    public void executeStep() throws ExceptionException {
        this.registers.setClockMCR(true);
        this.stopImmediately = false;
        this.executePumpedContinues(1);
        this.updateStatusLabel();
        if (this.gui != null) {
            this.gui.scrollToPC(0);
            this.updateSourceWindow();
        }
    }
    
    public void executeFin() throws ExceptionException {
        if (!this.callStack.empty()) {
            this.memory.setNextBreakPoint((this.callStack.peek() + 1) % 65536);
        }
        this.executeMany();
    }
    
    public void executeNext() throws ExceptionException {
        if (ISA.isCall(this.memory.read(this.registers.getPC()))) {
            this.memory.setNextBreakPoint((this.registers.getPC() + 1) % 65536);
            this.executeMany();
        }
        else {
            this.executeStep();
        }
    }
    
    public synchronized String stopExecution(final boolean b) {
        return this.stopExecution(0, b);
    }
    
    public synchronized String stopExecution(final int n, final boolean b) {
        this.stopImmediately = true;
        this.clearContinueMode();
        this.updateStatusLabel();
        if (this.gui != null) {
            this.gui.scrollToPC(n);
            this.updateSourceWindow();
        }
        this.memory.fireTableDataChanged();
        if (b) {
            final ListIterator<ActionListener> listIterator = this.NotifyOnStop.listIterator(0);
            while (listIterator.hasNext()) {
                listIterator.next().actionPerformed(null);
            }
        }
        return "Stopped at " + Word.toHex(this.registers.getPC());
    }
    
    public void executePumpedContinues() throws ExceptionException {
        this.executePumpedContinues(400);
    }
    
    /*
     * IMPORTANT - This function is what is called to execute each instruction*/
    
    public void executePumpedContinues(final int n) throws ExceptionException {
        int n2 = n;
        this.registers.setClockMCR(true);
        if (this.gui != null) {
            this.gui.setStatusLabelRunning();
        }
        while (!this.stopImmediately && n2 > 0) {
            try {
                final boolean privMode = this.registers.getPrivMode();
                final int pc = this.registers.getPC();
                final Word checkAndFetch = this.memory.checkAndFetch(pc, privMode);
                final InstructionDef instructionDef = ISA.lookupTable[checkAndFetch.getValue()];
                if (instructionDef == null) {
                    throw new IllegalInstructionException("Undefined instruction:  " + checkAndFetch.toHex());
                }
                if (instructionDef.isCall()) {
                    this.callStack.push(new Integer(pc + 1));
                }
                this.memory.clearRecent();
                this.registers.clearRecent();
                final boolean b = false;
                if (b && this.isTraceEnabled()) {
                    this.getTraceWriter().printf("%s ", Word.toHex(pc, false));
                    this.getTraceWriter().printf("%s ", checkAndFetch.toHex(false));
                    this.getTraceWriter().printf("%d ", this.registers.getNZP());
                }
                final int execute = instructionDef.execute(checkAndFetch, pc, this.registers, this.memory, this);
                ControlSignals signals = instructionDef.decodeSignals();
                gui.update(signals);
                this.registers.setPC(execute);
                if (b && this.isTraceEnabled()) {
                    this.getTraceWriter().printf("%s ", Word.toHex(execute, false));
                    this.getTraceWriter().printf("%s ", Word.toHex(execute, false));
                    this.getTraceWriter().println("");
                    this.getTraceWriter().flush();
                }
                if (!b && this.isTraceEnabled()) {
                    this.generateTrace(pc, checkAndFetch);
                }
                ++this.CYCLE_COUNT;
                ++this.INSTRUCTION_COUNT;
                if (!this.callStack.empty() && execute == this.callStack.peek()) {
                    this.callStack.pop();
                }
                if (this.memory.isRecentlyStored() && this.memory.isBreakPointSet(this.memory.getRecentAddress())) {
                    Console.println("Hit watchpoint at " + Word.toHex(this.memory.getRecentAddress()));
                    this.stopExecution(true);
                }
                if (this.memory.isBreakPointSet(this.registers.getPC())) {
                    Console.println("Hit breakpoint at " + Word.toHex(this.registers.getPC()));
                    this.stopExecution(true);
                }
                if (this.memory.isNextBreakPointSet(this.registers.getPC())) {
                    this.stopExecution(true);
                    this.memory.clearNextBreakPoint(this.registers.getPC());
                }
                --n2;
                continue;
            }
            catch (ExceptionException ex) {
                this.stopExecution(true);
                throw ex;
            }
            //break;
        }
        if (this.isContinueMode()) {
            SwingUtilities.invokeLater(this);
        }
    }
    
    public synchronized void executeMany() throws ExceptionException {
        this.setContinueMode();
        this.stopImmediately = false;
        try {
            this.executePumpedContinues();
        }
        catch (ExceptionException ex) {
            this.stopExecution(true);
            throw ex;
        }
    }
    
    public void generateTrace(final int n, final Word word) {
        if (this.isTraceEnabled()) {
            final PrintWriter traceWriter = this.getTraceWriter();
            traceWriter.printf("%s ", Word.toHex(n, false));
            traceWriter.printf("%s ", word.toBinary(false));
            if (this.registers.isRecentlyWritten()) {
                traceWriter.printf("%d %d %s ", 1, this.registers.getRecentlyWrittenRegister(), Word.toHex(this.registers.getRecentlyWrittenValue(), false));
            }
            else {
                traceWriter.printf("%d %d %s ", 0, 0, Word.toHex(0, false));
            }
            if (this.registers.isNZPRecentlyWritten()) {
                traceWriter.printf("%d %h ", 1, this.registers.getNZP());
            }
            else {
                traceWriter.printf("%d %h ", 0, 0);
            }
            if (this.memory.isRecentlyStored()) {
                traceWriter.printf("%d %s %s", 1, Word.toHex(this.memory.getRecentAddress(), false), Word.toHex(this.memory.getRecentValue(), false));
            }
            else if (this.memory.isRecentlyLoaded()) {
                traceWriter.printf("%d %s %s", 0, Word.toHex(this.memory.getRecentAddress(), false), Word.toHex(this.memory.getRecentValue(), false));
            }
            else {
                traceWriter.printf("%d %s %s", 0, Word.toHex(0, false), Word.toHex(0, false));
            }
            traceWriter.println("");
            traceWriter.flush();
        }
    }
    
    @Override
    public void run() {
        try {
            this.executePumpedContinues();
        }
        catch (ExceptionException ex) {
            if (this.gui != null) {
                ex.showMessageDialog(null);
            }
            Console.println(ex.getMessage());
        }
    }
    
    public void updateSourceWindow() {
        final int pc = this.registers.getPC();
        final int lookupLoc = this.srcTable.lookupLoc(pc);
        if (lookupLoc == -1) {
            return;
        }
        final String lookupFn = this.srcTable.lookupFn(pc);
        if (!lookupFn.equalsIgnoreCase(this.src_filename)) {
            this.gui.loadSourceFile(lookupFn);
            this.src_filename = lookupFn;
            this.src_line_number = -1;
        }
        if (lookupLoc != this.src_line_number) {
            this.gui.gotoSourceLine(this.src_line_number, lookupLoc);
            this.src_line_number = lookupLoc;
        }
    }
    
    public void updateStatusLabel() {
        if (this.gui != null) {
            if (!this.registers.getClockMCR()) {
                this.gui.setStatusLabelHalted();
            }
            else if (this.isContinueMode()) {
                this.gui.setStatusLabelRunning();
            }
            else {
                this.gui.setStatusLabelSuspended();
            }
        }
    }
}
