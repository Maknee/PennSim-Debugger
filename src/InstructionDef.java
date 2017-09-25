import java.util.List;

// 
// Decompiled by Procyon v0.5.30
// 

public abstract class InstructionDef
{
    private String opcode;
    private String format;
    Location dReg;
    Location sReg;
    Location tReg;
    Location signedImmed;
    Location pcOffset;
    Location unsignedImmed;
    Location absHi;
    Location absLo;
    Location absAligned;
    Location abs;
    private int mask;
    private int match;
    
    public InstructionDef() {
        this.opcode = null;
        this.format = new String();
        this.dReg = new Location();
        this.sReg = new Location();
        this.tReg = new Location();
        this.signedImmed = new Location();
        this.pcOffset = new Location();
        this.unsignedImmed = new Location();
        this.absHi = new Location();
        this.absLo = new Location();
        this.absAligned = new Location();
        this.abs = new Location();
        this.mask = 0;
        this.match = 0;
    }
    
    //base
    public ControlSignals decodeSignals() {
    	return null;
    }
    
    public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) throws IllegalMemAccessException, IllegalInstructionException {
        throw new IllegalInstructionException("Abstract instruction (or pseudo-instruction)");
    }
    
    public void expand(final Instruction instruction, final List list) throws AsException {
        throw new AsException("Expanding a non-pseudo instruction");
    }
    
    public boolean isCMP() {
    	return false;
    }
    
    public boolean isCMPU() {
    	return false;
    }
    
    public boolean isCMPI() {
    	return false;
    }
    
    public boolean isCMPIU() {
    	return false;
    }
    
    public boolean isNop() {
    	return false;
    }
    
    public boolean isPseudo() {
        return false;
    }
    
    public boolean isData() {
        return false;
    }
    
    public boolean isDirective() {
        return false;
    }
    
    public boolean isCall() {
        return false;
    }
    
    public boolean isBranch() {
        return false;
    }
    
    public boolean isLoad() {
        return false;
    }
    
    public boolean isStore() {
        return false;
    }
    
    public boolean isJump() {
    	return false;
    }
    
    public boolean isJSR () {
    	return false;
    }
    
    public boolean isJSRR() {
    	return false;
    }
    
    public boolean isJumpR() {
    	return false;
    }
    
    public boolean isTRAP () {
    	return false;
    }
    
    public boolean isRet() {
    	return false;
    }
    
    public boolean isAdd() {
    	return false;
    }
    
    public boolean isSub() {
    	return false;
    }
    
    public boolean isMul() {
    	return false;
    }
    
    public boolean isDiv() {
    	return false;
    }
    
    public boolean isAddIMM() {
    	return false;
    }
    
    public boolean isAnd() {
    	return false;
    }
    
    public boolean isNot() {
    	return false;
    }
    
    public boolean isOr() {
    	return false;
    }
    
    public boolean isXor() {
    	return false;
    }
    
    public boolean isAndIMM() {
    	return false;
    }
    
    public boolean isLdr() {
    	return false;
    }
    
    public boolean isConst() {
    	return false;
    }
    
    public boolean isConstIMM() {
    	return false;
    }
    
    public boolean isSLL() {
    	return false;
    }
    
    public boolean isSRA() {
    	return false;
    }
    
    public boolean isSRL() {
    	return false;
    }
    
    public boolean isMod() {
    	return false;
    }
    
    public boolean isHiConst() {
    	return false;
    }
    
    public final String getOpcode() {
        return this.opcode;
    }
    
    public final void setOpcode(final String opcode) {
        this.opcode = opcode;
    }
    
    public String getFormat() {
        return (this.opcode.toUpperCase() + " " + this.format).trim();
    }
    
    public int getNextAddress(final Instruction instruction) throws AsException {
        return instruction.getAddress() + 1;
    }
    
    public String getSrcFileName(final Instruction instruction) throws AsException {
        return instruction.getSrcFileName();
    }
    
    public int getSrcLineNumber(final Instruction instruction) throws AsException {
        return instruction.getSrcLineNumber();
    }
    
    public int getDestinationReg(final Word word) {
        return -1;
    }
    
    public int getSourceReg1(final Word word) {
        return -1;
    }
    
    public int getSourceReg2(final Word word) {
        return -1;
    }
    
    public final int getDReg(final Word word) {
        ISA.check(this.dReg.valid, "Invalid register" + word.toHex());
        return word.getZext(this.dReg.start, this.dReg.end);
    }
    
    public final int getSReg(final Word word) {
        ISA.check(this.sReg.valid, "Invalid register");
        return word.getZext(this.sReg.start, this.sReg.end);
    }
    
    public final int getTReg(final Word word) {
        ISA.check(this.tReg.valid, "Invalid register");
        return word.getZext(this.tReg.start, this.tReg.end);
    }
    
    public final int getSignedImmed(final Word word) {
        return word.getSext(this.signedImmed.start, this.signedImmed.end);
    }
    
    public final int getPCOffset(final Word word) {
        return word.getSext(this.pcOffset.start, this.pcOffset.end);
    }
    
    public final int getAbs(final Word word) {
        return word.getZext(this.abs.start, this.abs.end);
    }
    
    public final int getAbsAligned(final Word word) {
        return word.getZext(this.absAligned.start, this.absAligned.end);
    }
    
    public final int getAbsHi(final Word word) {
        return word.getZext(this.absHi.start, this.absHi.end);
    }
    
    public final int getAbsLo(final Word word) {
        return word.getZext(this.absLo.start, this.absLo.end);
    }
    
    public final int getUnsignedImmed(final Word word) {
        return word.getZext(this.unsignedImmed.start, this.unsignedImmed.end);
    }
    
    public String disassemble(final Word word, final int n, final Machine machine) {
        int n2 = 1;
        String s = this.getOpcode();
        if (this.dReg.valid) {
            String s2;
            if (n2 != 0) {
                s2 = s + " ";
                n2 = 0;
            }
            else {
                s2 = s + ", ";
            }
            s = s2 + "R" + this.getDReg(word);
        }
        if (this.sReg.valid) {
            String s3;
            if (n2 != 0) {
                s3 = s + " ";
                n2 = 0;
            }
            else {
                s3 = s + ", ";
            }
            s = s3 + "R" + this.getSReg(word);
        }
        if (this.tReg.valid) {
            String s4;
            if (n2 != 0) {
                s4 = s + " ";
                n2 = 0;
            }
            else {
                s4 = s + ", ";
            }
            s = s4 + "R" + this.getTReg(word);
        }
        if (this.signedImmed.valid) {
            String s5;
            if (n2 != 0) {
                s5 = s + " ";
                n2 = 0;
            }
            else {
                s5 = s + ", ";
            }
            s = s5 + "#" + this.getSignedImmed(word);
        }
        if (this.unsignedImmed.valid) {
            String s6;
            if (n2 != 0) {
                s6 = s + " ";
                n2 = 0;
            }
            else {
                s6 = s + ", ";
            }
            s = s6 + "x" + Integer.toHexString(this.getUnsignedImmed(word)).toUpperCase();
        }
        if (this.pcOffset.valid) {
            String s7;
            if (n2 != 0) {
                s7 = s + " ";
                n2 = 0;
            }
            else {
                s7 = s + ", ";
            }
            final int n3 = n + this.getPCOffset(word) + 1;
            String lookupAddr = null;
            if (machine != null) {
                lookupAddr = machine.getSymTable().lookupAddr(n3);
            }
            if (lookupAddr != null) {
                s = s7 + lookupAddr;
            }
            else {
                s = s7 + Word.toHex(n3);
            }
        }
        if (this.absLo.valid) {
            String s8;
            if (n2 != 0) {
                s8 = s + " ";
                n2 = 0;
            }
            else {
                s8 = s + ", ";
            }
            s = s8 + Word.toHex(this.getAbsLo(word));
        }
        if (this.absHi.valid) {
            String s9;
            if (n2 != 0) {
                s9 = s + " ";
                n2 = 0;
            }
            else {
                s9 = s + ", ";
            }
            s = s9 + Word.toHex(this.getAbsHi(word));
        }
        if (this.abs.valid) {
            String s10;
            if (n2 != 0) {
                s10 = s + " ";
                n2 = 0;
            }
            else {
                s10 = s + ", ";
            }
            final int abs = this.getAbs(word);
            String lookupAddr2 = null;
            if (machine != null) {
                lookupAddr2 = machine.getSymTable().lookupAddr(abs);
            }
            if (lookupAddr2 != null) {
                s = s10 + lookupAddr2;
            }
            else {
                s = s10 + Word.toHex(abs);
            }
        }
        if (this.absAligned.valid) {
            String s11;
            if (n2 != 0) {
                s11 = s + " ";
            }
            else {
                s11 = s + ", ";
            }
            final int n4 = (n & 0x8000) | this.getAbsAligned(word) << 4;
            String lookupAddr3 = null;
            if (machine != null) {
                lookupAddr3 = machine.getSymTable().lookupAddr(n4);
            }
            if (lookupAddr3 != null) {
                s = s11 + lookupAddr3;
            }
            else {
                s = s11 + Word.toHex(n4);
            }
        }
        return s;
    }
    
    public void encode(final SymbolTable symbolTable, final Instruction instruction, final List list) throws AsException {
        final Word word = new Word();
        word.setValue(this.match);
        try {
            int n = 0;
            if (this.dReg.valid) {
                word.setUnsignedField(instruction.getRegs(n), this.dReg.start, this.dReg.end);
                ++n;
            }
            if (this.sReg.valid) {
                word.setUnsignedField(instruction.getRegs(n), this.sReg.start, this.sReg.end);
                ++n;
            }
            if (this.tReg.valid) {
                word.setUnsignedField(instruction.getRegs(n), this.tReg.start, this.tReg.end);
                ++n;
            }
        }
        catch (AsException ex) {
            throw new AsException(instruction, "Register number out of range");
        }
        try {
            if (this.signedImmed.valid) {
                word.setSignedField(instruction.getOffsetImmediate(), this.signedImmed.start, this.signedImmed.end);
            }
            if (this.unsignedImmed.valid) {
                word.setUnsignedField(instruction.getOffsetImmediate(), this.unsignedImmed.start, this.unsignedImmed.end);
            }
        }
        catch (AsException ex2) {
            throw new AsException(instruction, "Immediate out of range");
        }
        if (this.pcOffset.valid) {
            final int lookupSym = symbolTable.lookupSym(instruction.getLabelRef());
            if (lookupSym == Integer.MAX_VALUE) {
                throw new AsException(instruction, "Undeclared instruction label: " + instruction.getLabelRef());
            }
            instruction.setOffsetImmediate(lookupSym - (instruction.getAddress() + 1));
            try {
                word.setSignedField(instruction.getOffsetImmediate(), this.pcOffset.start, this.pcOffset.end);
            }
            catch (AsException ex3) {
                throw new AsException(instruction, "PC-relative offset out of range");
            }
        }
        if (this.absHi.valid) {
            final int lookupSym2 = symbolTable.lookupSym(instruction.getLabelRef());
            if (lookupSym2 == Integer.MAX_VALUE) {
                throw new AsException(instruction, "Undeclared instruction or data label: " + instruction.getLabelRef());
            }
            instruction.setAbsHi(lookupSym2);
            try {
                word.setUnsignedField(instruction.getAbsHi(), this.absHi.start, this.absHi.end);
            }
            catch (AsException ex4) {
                throw new AsException(instruction, "AbsoluteHi out of range");
            }
        }
        if (this.absLo.valid) {
            final int lookupSym3 = symbolTable.lookupSym(instruction.getLabelRef());
            if (lookupSym3 == Integer.MAX_VALUE) {
                throw new AsException(instruction, "Undeclared instruction or data label: " + instruction.getLabelRef());
            }
            instruction.setAbsLo(lookupSym3);
            try {
                word.setSignedField(instruction.getAbsLo(), this.absLo.start, this.absLo.end);
            }
            catch (AsException ex5) {
                throw new AsException(instruction, "AbsoluteLo out of range");
            }
        }
        if (this.absAligned.valid) {
            final int lookupSym4 = symbolTable.lookupSym(instruction.getLabelRef());
            if (lookupSym4 == Integer.MAX_VALUE) {
                throw new AsException(instruction, "Undeclared instruction or data label: " + instruction.getLabelRef());
            }
            instruction.setAbsAligned(lookupSym4);
            try {
                word.setUnsignedField(instruction.getAbsAligned(), this.absAligned.start, this.absAligned.end);
            }
            catch (AsException ex6) {
                throw new AsException(instruction, "AbsoluteAligned out of range");
            }
        }
        list.add(word);
    }
    
    private String encodeField(final String s, final char c, final String s2, final Location location) {
        final int index = s.indexOf(c);
        final int lastIndex = s.lastIndexOf(c);
        if (index != -1 && lastIndex != -1) {
            ISA.check(s.substring(index, lastIndex).matches("[" + c + "]*"), "Strange encoding of '" + c + "': " + s);
            location.valid = true;
            location.start = 15 - index;
            location.end = 15 - lastIndex;
            this.format = this.format + s2 + " ";
            return s.replaceAll("" + c, "x");
        }
        return s;
    }
    
    public final boolean match(final Word word) {
        return (word.getValue() & this.mask) == this.match;
    }
    
    public final void setEncoding(String s) {
        final String s2 = s;
        s = s.toLowerCase();
        s = s.replaceAll("\\s", "");
        s = s.replaceAll("[^x10iudstpzlhab]", "");
        ISA.check(s.length() == 16, "Strange encoding: " + s2);
        s = this.encodeField(s, 'd', "Reg", this.dReg);
        s = this.encodeField(s, 's', "Reg", this.sReg);
        s = this.encodeField(s, 't', "Reg", this.tReg);
        s = this.encodeField(s, 'i', "Num", this.signedImmed);
        s = this.encodeField(s, 'p', "Label", this.pcOffset);
        s = this.encodeField(s, 'b', "Label", this.abs);
        s = this.encodeField(s, 'l', "Label", this.absLo);
        s = this.encodeField(s, 'h', "Label", this.absHi);
        s = this.encodeField(s, 'a', "Label", this.absAligned);
        s = this.encodeField(s, 'u', "Num", this.unsignedImmed);
        s = this.encodeField(s, 'z', "String", this.unsignedImmed);
        s = s.replaceAll("[^x10]", "");
        ISA.check(s.length() == 16, "Strange encoding: " + s2);
        this.mask = Integer.parseInt(s.replaceAll("0", "1").replaceAll("x", "0"), 2);
        this.match = Integer.parseInt(s.replaceAll("x", "0"), 2);
    }
    
    class Location
    {
        public boolean valid;
        public int start;
        public int end;
        
        Location() {
            this.valid = false;
            this.start = -1;
            this.end = -1;
        }
    }
}
