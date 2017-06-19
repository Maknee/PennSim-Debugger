import javax.swing.table.AbstractTableModel;

// 
// Decompiled by Procyon v0.5.30
// 

public class Memory extends AbstractTableModel
{
    public static final int MEM_SIZE = 65536;
    public static final int BEGIN_DEVICE_REGISTERS = 65024;
    public static final int KBSR = 65024;
    public static final int KBDR = 65026;
    public static final int ADSR = 65028;
    public static final int ADDR = 65030;
    public static final int TSR = 65032;
    public static final int TIR = 65034;
    public static final int VDCR = 65036;
    public static final int DISABLE_TIMER = 0;
    public static final int MANUAL_TIMER_MODE = 1;
    public static final int MPR = 65535;
    public static final int MCR = 65534;
    public static final int CCR = 65280;
    public static final int ICR = 65281;
    public static final int LSCR = 65282;
    public static final int BSCR = 65283;
    public static final int USER_MIN = 0;
    public static final int USER_MAX = 32767;
    public static final int USER_CODE_MIN = 0;
    public static final int USER_CODE_MAX = 8191;
    public static final int OS_MIN = 32768;
    public static final int OS_MAX = 65535;
    public static final int OS_CODE_MIN = 32768;
    public static final int OS_CODE_MAX = 40959;
    private final int addrMin = 0;
    private final int addrMax = 65535;
    private KeyboardDevice kbDevice;
    private MonitorDevice monitorDevice;
    private TimerDevice timerDevice;
    private Word[] memArr;
    private String[] colNames;
    private boolean[] nextBreakPoints;
    private boolean[] breakPoints;
    protected final Machine machine;
    public static final int BREAKPOINT_COLUMN = 0;
    public static final int ADDRESS_COLUMN = 1;
    public static final int VALUE_COLUMN = 2;
    private boolean recentlyLoaded;
    private boolean recentlyStored;
    private int recentValue;
    private int recentAddress;
    
    public boolean isRecentlyLoaded() {
        return this.recentlyLoaded;
    }
    
    public boolean isRecentlyStored() {
        return this.recentlyStored;
    }
    
    public int getRecentValue() {
        assert this.recentlyLoaded || this.recentlyStored;
        return this.recentValue;
    }
    
    public int getRecentAddress() {
        assert this.recentlyLoaded || this.recentlyStored;
        return this.recentAddress;
    }
    
    public Word getWord(int address) {
    	return memArr[address];
    }
    
    public void clearRecent() {
        this.recentlyLoaded = false;
        this.recentlyStored = false;
        this.recentValue = 0;
        this.recentAddress = 0;
    }
    
    public Memory(final Machine machine) {
        this.kbDevice = new KeyboardDevice();
        this.monitorDevice = new MonitorDevice();
        this.timerDevice = new TimerDevice();
        this.memArr = new Word[65536];
        this.colNames = null;
        this.nextBreakPoints = new boolean[65536];
        this.breakPoints = new boolean[65536];
        this.machine = machine;
        for (int i = 0; i < 65536; ++i) {
            this.memArr[i] = new Word();
            this.breakPoints[i] = false;
        }
        this.colNames = new String[] { "BP", "Address", "Value" };
        this.timerDevice.setTimer();
        this.clearRecent();
    }
    
    public void reset() {
        for (int i = 0; i < 65536; ++i) {
            this.memArr[i].reset();
        }
        this.clearAllBreakPoints();
        this.fireTableDataChanged();
        this.kbDevice.reset();
        this.monitorDevice.reset();
        this.timerDevice.reset();
        this.clearRecent();
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
    public int getRowCount() {
        return this.memArr.length;
    }
    
    public boolean checkCodeAddrRange(final int n) {
        return (n >= 0 && n <= 8191) || (n >= 32768 && n <= 40959);
    }
    
    public boolean checkAddrRange(final int n) {
        return n >= 0 && n <= 65535;
    }
    
    public boolean checkAddrPriv(final int n, final boolean b) {
        return b || (n & 0x8000) == 0x0;
    }
    
    public boolean isBreakPointSet(final int n) {
        return this.breakPoints[n];
    }
    
    public String setBreakPoint(final String s) {
        final int lookupSym = this.machine.getSymTable().lookupSym(s);
        if (lookupSym == Integer.MAX_VALUE) {
            return "Error: Invalid label ('" + s + "')";
        }
        return this.setBreakPoint(lookupSym);
    }
    
    public String setBreakPoint(final int n) {
        if (!this.checkAddrRange(n)) {
            return "Error: Invalid address or label";
        }
        String userFeedBack = new String();
        if(this.breakPoints[n] == true) {
        	this.breakPoints[n] = false;
        	userFeedBack = "Breakpoint removed at " + Word.toHex(n);
        } else {
        	this.breakPoints[n] = true;
        	userFeedBack = "Breakpoint set at " + Word.toHex(n);
        }
        this.fireTableCellUpdated(n, -1);
        return userFeedBack;
    }
    
    public String clearBreakPoint(final String s) {
        final int lookupSym = this.machine.getSymTable().lookupSym(s);
        if (lookupSym == Integer.MAX_VALUE) {
            return "Error: Invalid label ('" + s + "')";
        }
        return this.clearBreakPoint(lookupSym);
    }
    
    public String clearBreakPoint(final int n) {
        if (!this.checkAddrRange(n)) {
            return "Error: Invalid address or label";
        }
        this.breakPoints[n] = false;
        this.fireTableCellUpdated(n, -1);
        return "Breakpoint cleared at " + Word.toHex(n);
    }
    
    public void clearAllBreakPoints() {
        for (int i = 0; i < 65536; ++i) {
            this.breakPoints[i] = false;
            this.nextBreakPoints[i] = false;
        }
    }
    
    public void setNextBreakPoint(final int n) {
        assert this.checkAddrRange(n);
        this.nextBreakPoints[n] = true;
    }
    
    public boolean isNextBreakPointSet(final int n) {
        assert this.checkAddrRange(n);
        return this.nextBreakPoints[n];
    }
    
    public void clearNextBreakPoint(final int n) {
        assert this.checkAddrRange(n);
        this.nextBreakPoints[n] = false;
    }
    
    public Word checkAndRead(final int recentAddress, final boolean b) throws IllegalMemAccessException {
        if (!this.checkAddrPriv(recentAddress, b)) {
            throw new IllegalMemAccessException(recentAddress, b);
        }
        if (this.checkCodeAddrRange(recentAddress)) {
            throw new IllegalMemAccessException(recentAddress, b);
        }
        final Word read = this.read(recentAddress);
        assert !this.recentlyStored;
        assert !this.recentlyLoaded;
        this.recentlyLoaded = true;
        this.recentValue = read.getValue();
        this.recentAddress = recentAddress;
        return read;
    }
    
    //This returns no exception when reading memory
    public Word checkAndReadNoException(final int n)
    {
        return this.read(n);
    }
    
    public Word checkAndFetch(final int n, final boolean b) throws IllegalMemAccessException {
        if (!this.checkAddrPriv(n, b)) {
            throw new IllegalMemAccessException(n, b);
        }
        if (!this.checkCodeAddrRange(n)) {
            throw new IllegalMemAccessException(n, b);
        }
        return this.read(n);
    }
    
    public void checkAndWrite(final int recentAddress, final int recentValue, final boolean b) throws IllegalMemAccessException {
        if (!this.checkAddrPriv(recentAddress, b)) {
            throw new IllegalMemAccessException(recentAddress, b);
        }
        if (this.checkCodeAddrRange(recentAddress)) {
            throw new IllegalMemAccessException(recentAddress, b);
        }
        this.write(recentAddress, recentValue);
        assert !this.recentlyStored;
        assert !this.recentlyLoaded;
        this.recentlyStored = true;
        this.recentValue = recentValue;
        this.recentAddress = recentAddress;
    }
    
    private void resetVideoBuffer() {
        for (int i = 49152; i < 65024; ++i) {
            this.memArr[i].setValue(0);
        }
    }
    
    public KeyboardDevice getKeyBoardDevice() {
        return this.kbDevice;
    }
    
    public MonitorDevice getMonitorDevice() {
        return this.monitorDevice;
    }
    
    @Override
    public boolean isCellEditable(final int n, final int n2) {
        return (n2 == 2 || n2 == 0) && n < 65024;
    }
    
    public Word read(final int n) {
        if (!this.checkAddrRange(n)) {
            return null;
        }
        switch (n) {
            case 65024: {
                return this.kbDevice.status();
            }
            case 65026: {
                return this.kbDevice.read();
            }
            case 65028: {
                return this.monitorDevice.status();
            }
            case 65032: {
                return this.timerDevice.status();
            }
            case 65034: {
                return new Word((int)this.timerDevice.getInterval());
            }
            case 65534: {
                return new Word(this.machine.getRegisterFile().getMCR());
            }
            case 65280: {
                return new Word(this.machine.CYCLE_COUNT);
            }
            case 65281: {
                return new Word(this.machine.INSTRUCTION_COUNT);
            }
            case 65282: {
                return new Word(this.machine.LOAD_STALL_COUNT);
            }
            case 65283: {
                return new Word(this.machine.BRANCH_STALL_COUNT);
            }
            default: {
                return this.memArr[n];
            }
        }
    }
    
    @Override
    public void setValueAt(final Object o, final int breakPoint, final int n) {
        if (n == 2) {
            this.write(breakPoint, Word.parseNum((String)o));
            this.fireTableCellUpdated(breakPoint, n);
        }
        if (n == 0) {
            if (o != null) {
                Console.println(this.setBreakPoint(breakPoint));
            }
            else {
                Console.println(this.clearBreakPoint(breakPoint));
            }
        }
    }
    
    public void write(final int n, final int n2) {
        switch (n) {
            case 65036: {
                if (n2 == 1) {
                    this.resetVideoBuffer();
                    break;
                }
                if (n2 == 2 && PennSim.isGraphical()) {
                    this.machine.getGUI().getVideoConsole().bltMemBuffer();
                    break;
                }
                break;
            }
            case 65030: {
                this.monitorDevice.write((char)n2);
                break;
            }
            case 65034: {
                this.timerDevice.setTimer(n2);
                if (n2 == 0) {
                    this.timerDevice.setEnabled(false);
                    break;
                }
                this.timerDevice.setEnabled(true);
                if (n2 == 1) {
                    this.timerDevice.setTimer(this.kbDevice);
                    break;
                }
                break;
            }
            case 65534: {
                this.machine.getRegisterFile().setMCR(n2);
                if ((n2 & 0x8000) == 0x0) {
                    this.machine.stopExecution(1, true);
                    break;
                }
                this.machine.updateStatusLabel();
                break;
            }
        }
        this.memArr[n].setValue(n2);
        this.fireTableCellUpdated(n, -1);
    }
    
    @Override
    public Object getValueAt(final int n, final int n2) {
        Object o = null;
        switch (n2) {
            case 0: {
                o = new Boolean(this.isBreakPointSet(n));
                break;
            }
            case 1: {
                final String lookupAddr = this.machine.getSymTable().lookupAddr(n);
                o = ((lookupAddr != null) ? lookupAddr : Word.toHex(n));
                break;
            }
            case 2: {
                if ((n >= 0 && n <= 8191) || (n >= 32768 && n <= 40959)) {
                    o = ISA.disassemble(this.memArr[n], n, this.machine);
                    break;
                }
                if (n < 65024) {
                    o = this.memArr[n].toHex();
                    break;
                }
                o = "???";
                break;
            }
        }
        return o;
    }
}
