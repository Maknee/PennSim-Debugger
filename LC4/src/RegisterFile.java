import javax.swing.table.AbstractTableModel;

// 
// Decompiled by Procyon v0.5.30
// 

public class RegisterFile extends AbstractTableModel
{
    public static final int NUM_REGISTERS = 8;
    private static final int NUM_ROWS = 12;
    private static final int PC_ROW = 8;
    private static final int PSR_ROW = 10;
    private static final int CC_ROW = 11;
    private final String[] colNames;
    private final Machine machine;
    private Word PC;
    private Word PSR;
    private Word MCR;
    private Word[] regArr;
    private static String[] indNames;
    private static int[] indRow;
    private static int[] indCol;
    private boolean recentlyWritten;
    private int recentlyWrittenValue;
    private int recentlyWrittenRegister;
    private boolean NZPRecentlyWritten;
    
    private Word savedPC;
    private Word savedPSR;
    private Word savedMCR;
    private Word[] savedregArr;
    private int savedrecentlyWrittenValue;
    private int savedrecentlyWrittenRegister;
    private boolean savedNZPRecentlyWritten;
    
    public void pushad() {
    	this.savedPC = PC.clone();
    	this.savedPSR = PSR.clone();
    	this.savedMCR = MCR.clone();
    	this.savedregArr = new Word[regArr.length];
    	for(int i = 0; i < regArr.length; i++) {
    		savedregArr[i] = regArr[i].clone();
    	}
    	this.savedrecentlyWrittenValue = recentlyWrittenValue;
    	this.savedrecentlyWrittenRegister = recentlyWrittenRegister;
    	this.savedNZPRecentlyWritten = NZPRecentlyWritten;
    }
    
    public void popad() {
    	this.PC = savedPC;
    	this.PSR = savedPSR;
    	this.MCR = savedMCR;
    	this.regArr = savedregArr;
    	this.recentlyWrittenValue = savedrecentlyWrittenValue;
    	this.recentlyWrittenRegister = savedrecentlyWrittenRegister;
    	this.NZPRecentlyWritten = savedNZPRecentlyWritten;
    	this.fireTableDataChanged();
    }
    
    public RegisterFile(final Machine machine) {
        this.colNames = new String[] { "Register", "Value", "Register", "Value" };
        this.regArr = new Word[8];
        this.machine = machine;
        if (!PennSim.isLC3()) {
            RegisterFile.indNames[11] = "";
        }
        for (int i = 0; i < 8; ++i) {
            this.regArr[i] = new Word();
        }
        this.PC = new Word();
        this.MCR = new Word();
        this.PSR = new Word();
        this.reset();
    }
    
    public void reset() {
        for (int i = 0; i < 8; ++i) {
            this.regArr[i].setValue(0);
        }
        this.PC.setValue(33280);
        this.MCR.setValue(32768);
        this.PSR.setValue(2);
        this.setPrivMode(true);
        this.fireTableDataChanged();
        this.clearRecent();
    }
    
    @Override
    public int getRowCount() {
        return 6;
    }
    
    @Override
    public int getColumnCount() {
        return this.colNames.length;
    }
    
    @Override
    public String getColumnName(final int n) {
        return this.colNames[n];
    }
    
    @Override
    public boolean isCellEditable(final int n, final int n2) {
        return n2 == 1 || n2 == 3;
    }
    
    @Override
    public Object getValueAt(final int n, final int n2) {
        if (n2 == 0) {
            return RegisterFile.indNames[n];
        }
        if (n2 == 1) {
            return this.regArr[n].toHex();
        }
        if (n2 == 2) {
            return RegisterFile.indNames[n + 6];
        }
        if (n2 == 3) {
            if (n < 2) {
                return this.regArr[n + 6].toHex();
            }
            if (n == 2) {
                return this.PC.toHex();
            }
            if (n == 3) {
                return "";
            }
            if (n == 4) {
                return this.PSR.toHex();
            }
            if (n == 5) {
                if (PennSim.isLC3()) {
                    return this.printCC();
                }
                return "";
            }
        }
        return null;
    }
    
    @Override
    public void setValueAt(final Object o, final int n, final int n2) {
        if (n2 == 1) {
            this.regArr[n].setValue(Word.parseNum((String)o));
        }
        else if (n2 == 3) {
            if (n < 2) {
                this.regArr[n + 6].setValue(Word.parseNum((String)o));
            }
            else {
                if (n == 5) {
                    this.setNZP((String)o);
                    return;
                }
                final int num = Word.parseNum((String)o);
                if (n == 2) {
                    this.setPC(num);
                    if (this.machine.getGUI() != null) {
                        this.machine.getGUI().scrollToPC();
                    }
                }
                else if (n == 4) {
                    this.setPSR(num);
                }
            }
        }
        this.fireTableCellUpdated(n, n2);
    }
    
    public boolean isRecentlyWritten() {
        return this.recentlyWritten;
    }
    
    public int getRecentlyWrittenValue() {
        assert this.recentlyWritten;
        return this.recentlyWrittenValue;
    }
    
    public int getRecentlyWrittenRegister() {
        assert this.recentlyWritten;
        return this.recentlyWrittenRegister;
    }
    
    public boolean isNZPRecentlyWritten() {
        return this.NZPRecentlyWritten;
    }
    
    public void clearRecent() {
        this.recentlyWritten = false;
        this.recentlyWrittenValue = 0;
        this.recentlyWrittenRegister = 0;
        this.NZPRecentlyWritten = false;
    }
    
    public int getPC() {
        return this.PC.getValue();
    }
    
    public void setPC(final int value) {
        final int value2 = this.PC.getValue();
        this.PC.setValue(value);
        this.fireTableCellUpdated(RegisterFile.indRow[8], RegisterFile.indCol[8]);
        this.machine.getMemory().fireTableRowsUpdated(value2, value2);
        this.machine.getMemory().fireTableRowsUpdated(value, value);
    }
    
    public void incPC(final int n) {
        this.setPC(this.PC.getValue() + n);
    }
    
    public String printRegister(final int n) throws IndexOutOfBoundsException {
        if (n < 0 || n >= 8) {
            throw new IndexOutOfBoundsException("Register index must be from 0 to 7");
        }
        return this.regArr[n].toHex();
    }
    
    public int getRegister(final int n) throws IndexOutOfBoundsException {
        if (n < 0 || n >= 8) {
            throw new IndexOutOfBoundsException("Register index must be from 0 to 7");
        }
        return this.regArr[n].getValue();
    }
    
    public void setRegister(final int recentlyWrittenRegister, final int n) {
        if (recentlyWrittenRegister < 0 || recentlyWrittenRegister >= 8) {
            throw new IndexOutOfBoundsException("Register index must be from 0 to 7");
        }
        this.recentlyWritten = true;
        this.recentlyWrittenValue = n;
        this.recentlyWrittenRegister = recentlyWrittenRegister;
        this.regArr[recentlyWrittenRegister].setValue(n);
        this.fireTableCellUpdated(RegisterFile.indRow[recentlyWrittenRegister], RegisterFile.indCol[recentlyWrittenRegister]);
    }
    
    public boolean getN() {
        return this.PSR.getBit(2) == 1;
    }
    
    public boolean getZ() {
        return this.PSR.getBit(1) == 1;
    }
    
    public boolean getP() {
        return this.PSR.getBit(0) == 1;
    }
    
    public int getNZP() {
        return this.PSR.getZext(2, 0);
    }
    
    public boolean getPrivMode() {
        return this.PSR.getBit(15) == 1;
    }
    
    public String printCC() {
        if (!(this.getN() ^ this.getZ() ^ this.getP()) || (this.getN() && this.getZ() && this.getP())) {
            return "invalid";
        }
        if (this.getN()) {
            return "N";
        }
        if (this.getZ()) {
            return "Z";
        }
        if (this.getP()) {
            return "P";
        }
        return "unset";
    }
    
    public int getPSR() {
        return this.PSR.getValue();
    }
    
    public void setNZP(int n) {
        final int n2 = this.PSR.getValue() & 0xFFFFFFF8;
        n &= 0xFFFF;
        int psr;
        if ((n & 0x8000) != 0x0) {
            psr = (n2 | 0x4);
        }
        else if (n == 0) {
            psr = (n2 | 0x2);
        }
        else {
            psr = (n2 | 0x1);
        }
        this.setPSR(psr);
        this.NZPRecentlyWritten = true;
    }
    
    public void setNZP(String trim) {
        trim = trim.toLowerCase().trim();
        if (!trim.equals("n") && !trim.equals("z") && !trim.equals("p")) {
            Console.println("Condition codes must be set as one of `n', `z' or `p'");
            return;
        }
        if (trim.equals("n")) {
            this.setN();
        }
        else if (trim.equals("z")) {
            this.setZ();
        }
        else {
            this.setP();
        }
    }
    
    public void setN() {
        this.setNZP(32768);
    }
    
    public void setZ() {
        this.setNZP(0);
    }
    
    public void setP() {
        this.setNZP(1);
    }
    
    public void setPrivMode(final boolean b) {
        final int value = this.PSR.getValue();
        int psr;
        if (!b) {
            psr = (value & 0x7FFF);
        }
        else {
            psr = (value | 0x8000);
        }
        this.setPSR(psr);
    }
    
    public void setClockMCR(final boolean b) {
        if (b) {
            this.setMCR(this.MCR.getValue() | 0x8000);
        }
        else {
            this.setMCR(this.MCR.getValue() & 0x7FFF);
        }
    }
    
    public boolean getClockMCR() {
        return (this.getMCR() & 0x8000) != 0x0;
    }
    
    public void setMCR(final int value) {
        this.MCR.setValue(value);
    }
    
    public int getMCR() {
        return this.MCR.getValue();
    }
    
    public void setPSR(final int value) {
        this.PSR.setValue(value);
        this.fireTableCellUpdated(RegisterFile.indRow[10], RegisterFile.indCol[10]);
        this.fireTableCellUpdated(RegisterFile.indRow[11], RegisterFile.indCol[11]);
    }
    
    @Override
    public String toString() {
        String string = "[";
        for (int i = 0; i < 8; ++i) {
            string = string + "R" + i + ": " + this.regArr[i].toHex() + ((i != 7) ? "," : "");
        }
        return string + "]" + "\nPC = " + this.PC.toHex() + "\nPSR = " + this.PSR.toHex() + "\nCC = " + this.printCC();
    }
    
    public static boolean isLegalRegister(final int n) {
        return n >= 0 && n <= 8;
    }
    
    static {
        RegisterFile.indNames = new String[] { "R0", "R1", "R2", "R3", "R4", "R5", "R6", "R7", "PC", "", "PSR", "CC" };
        RegisterFile.indRow = new int[] { 0, 1, 2, 3, 4, 5, 0, 1, 2, 3, 4, 5 };
        RegisterFile.indCol = new int[] { 1, 1, 1, 1, 1, 1, 3, 3, 3, 3, 3, 3 };
    }
}
