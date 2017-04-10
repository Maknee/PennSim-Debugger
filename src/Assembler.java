import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;

// 
// Decompiled by Procyon v0.5.30
// 

class Assembler
{
    private static final int SECTION_SIZE = 32768;
    private static final int INSN_SECTION_SIZE = 8192;
    private static final int DATA_SECTION_SIZE = 24576;
    private static final int USER_MIN = 0;
    private static final int USER_MAX = 32767;
    private static final int USER_CODE_MIN = 0;
    private static final int USER_CODE_MAX = 8191;
    private static final int USER_DATA_MIN = 8192;
    private static final int USER_DATA_MAX = 32767;
    private static final int OS_MIN = 32768;
    private static final int OS_MAX = 65535;
    private static final int OS_CODE_MIN = 32768;
    private static final int OS_CODE_MAX = 40959;
    private static final int OS_DATA_MIN = 40960;
    private static final int OS_DATA_MAX = 65535;
    boolean[] bitmap;
    String basefn;
    ArrayList<?> asmlst;
    
    Assembler() {
        this.bitmap = new boolean[32768];
        this.basefn = null;
        this.asmlst = new ArrayList<Object>();
    }
    
    String as(final String s, final String[] array) throws AsException {
        final SymbolTable symbolTable = new SymbolTable();
        final SymbolTable symbolTable2 = new SymbolTable();
        for (int i = 0; i < 32768; ++i) {
            this.bitmap[i] = false;
        }
        if (s == null || s.length() == 0) {
            throw new AsException("No output file specified");
        }
        if (array.length == 0) {
            throw new AsException("No input files specified");
        }
        this.formatObjectFile(symbolTable, this.layoutAndSymbols(symbolTable2, symbolTable, this.expandPseudoInsns(symbolTable2, this.extractConstants(symbolTable2, this.splitLabels(this.parse(array))))), s);
        return "";
    }
    
    void checkAddr(final SymbolTable symbolTable, final boolean b, final boolean b2, final int n) throws AsException {
        final String lookupAddr = symbolTable.lookupAddr(n);
        if (b && !b2 && (n < 40960 || n > 65535)) {
            throw new AsException("OS DATA address must be 40960-65535 : " + n + " (" + lookupAddr + ")");
        }
        if (b && b2 && (n < 32768 || n > 40959)) {
            throw new AsException("OS CODE address must be 32768-40959 : " + n + " (" + lookupAddr + ")");
        }
        if (!b && !b2 && (n < 8192 || n > 32767)) {
            throw new AsException("USER DATA address must be 8192-32767 : " + n + " (" + lookupAddr + ")");
        }
        if (!b && b2 && (n < 0 || n > 8191)) {
            throw new AsException("USER CODE address must be 0-8191 : " + n + " (" + lookupAddr + ")");
        }
    }
    
    List<Instruction> parse(final String[] array) throws AsException {
        final ArrayList<Instruction> list = new ArrayList<Instruction>();
        String string = null;
        try {
            for (int i = 0; i < array.length; ++i) {
                string = array[i] + ".asm";
                int n = 1;
                final BufferedReader bufferedReader = new BufferedReader(new FileReader(string));
                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    final Instruction instruction = new Instruction(line, string, n++);
                    if (instruction.getOpcode() != null || instruction.getLabel() != null) {
                        list.add(instruction);
                    }
                }
                bufferedReader.close();
            }
        }
        catch (IOException ex) {
            throw new AsException("Couldn't read file (" + string + ")");
        }
        return list;
    }
    
    private List<Instruction> splitLabels(final List<Instruction> list) throws AsException {
        final ArrayList<Instruction> list2 = new ArrayList<Instruction>();
        final Iterator<Instruction> iterator = list.iterator();
        while (iterator.hasNext()) {
            iterator.next().splitLabels(list2);
        }
        return list2;
    }
    
    private List<Instruction> extractConstants(final SymbolTable symbolTable, final List<Instruction> list) throws AsException {
        final ArrayList<Instruction> list2 = new ArrayList<Instruction>();
        final Iterator<Instruction> iterator = list.iterator();
        Instruction instruction = null;
        Instruction instruction2 = null;
        while (iterator.hasNext()) {
            final Instruction instruction3 = instruction2;
            instruction2 = instruction;
            instruction = iterator.next();
            if (instruction.getOpcode() != null && (instruction.getOpcode().equalsIgnoreCase(".CONST") || instruction.getOpcode().equalsIgnoreCase(".UCONST"))) {
                if (instruction2 == null || instruction2.getLabel() == null) {
                    throw new AsException(instruction, "Un-labeled constant");
                }
                if (symbolTable.existSym(instruction2.getLabel())) {
                    instruction.error("Duplicate label ('" + instruction2.getLabel() + "')");
                }
                symbolTable.insert(instruction2.getLabel(), instruction.getOffsetImmediate());
                instruction = (instruction2 = null);
            }
            if (instruction3 != null) {
                list2.add(instruction3);
            }
        }
        if (instruction2 != null) {
            list2.add(instruction2);
        }
        if (instruction != null) {
            list2.add(instruction);
        }
        return list2;
    }
    
    private List<Instruction> expandPseudoInsns(final SymbolTable symbolTable, final List<Instruction> list) throws AsException {
        final ArrayList<Instruction> list2 = new ArrayList<Instruction>();
        for (final Instruction instruction : list) {
            if (instruction.getOpcode() != null && instruction.getOpcode().equalsIgnoreCase("LEA")) {
                list2.add(new Instruction("CONST R" + instruction.getRegs(0) + ", " + instruction.getLabelRef(), instruction.getFileName(), instruction.getLineNumber()));
                list2.add(new Instruction("HICONST R" + instruction.getRegs(0) + ", " + instruction.getLabelRef(), instruction.getFileName(), instruction.getLineNumber()));
            }
            else if (instruction.getOpcode() != null && instruction.getOpcode().equalsIgnoreCase("LC")) {
                final int lookupSym = symbolTable.lookupSym(instruction.getLabelRef());
                if (lookupSym == Integer.MAX_VALUE) {
                    throw new AsException(instruction, "Undefined constant label + `" + instruction.getLabelRef() + "'");
                }
                if (lookupSym >= -256 && lookupSym <= 255) {
                    list2.add(new Instruction("CONST R" + instruction.getRegs(0) + ", #" + lookupSym, instruction.getFileName(), instruction.getLineNumber()));
                }
                else {
                    list2.add(new Instruction("CONST R" + instruction.getRegs(0) + ", #" + (lookupSym & 0xFF), instruction.getFileName(), instruction.getLineNumber()));
                    list2.add(new Instruction("HICONST R" + instruction.getRegs(0) + ", #" + (lookupSym >>> 8), instruction.getFileName(), instruction.getLineNumber()));
                }
            }
            else {
                list2.add(instruction);
            }
        }
        return list2;
    }
    
    List<Instruction> layoutAndSymbols(final SymbolTable symbolTable, final SymbolTable symbolTable2, final List<Instruction> list) throws AsException {
        final ArrayList<Instruction> list2 = new ArrayList<Instruction>();
        int address = 0;
        int offsetImmediate = 0;
        int offsetImmediate2 = -1;
        String stringz = null;
        boolean b = false;
        boolean b2 = true;
        for (final Instruction instruction : list) {
            if (instruction.getLabel() != null) {
                if (instruction.getLabel().length() > 64) {
                    instruction.error("Labels can be no longer than 64 characters ('" + instruction.getLabel() + "').");
                }
                if (symbolTable.lookupSym(instruction.getLabel()) != Integer.MAX_VALUE || symbolTable2.lookupSym(instruction.getLabel()) != Integer.MAX_VALUE) {
                    instruction.error("Duplicate label ('" + instruction.getLabel() + "')");
                }
                symbolTable2.insert(instruction.getLabel(), b2 ? address : offsetImmediate);
            }
            else if (instruction.getOpcode().equalsIgnoreCase(".OS")) {
                b = true;
                address = 32768;
                offsetImmediate = 40960;
            }
            else if (instruction.getOpcode().equalsIgnoreCase(".USER")) {
                b = false;
                address = 0;
                offsetImmediate = 8192;
            }
            else if (instruction.getOpcode().equalsIgnoreCase(".CODE")) {
                b2 = true;
            }
            else if (instruction.getOpcode().equalsIgnoreCase(".DATA")) {
                b2 = false;
            }
            else if (instruction.getOpcode().equalsIgnoreCase(".FILE")) {
                stringz = instruction.getStringz();
            }
            else if (instruction.getOpcode().equalsIgnoreCase(".LOC")) {
                offsetImmediate2 = instruction.getOffsetImmediate();
            }
            else if (instruction.getOpcode().equalsIgnoreCase(".ADDR")) {
                this.checkAddr(symbolTable2, b, b2, instruction.getOffsetImmediate());
                if (b2) {
                    address = instruction.getOffsetImmediate();
                }
                else {
                    offsetImmediate = instruction.getOffsetImmediate();
                }
            }
            else if (instruction.getOpcode().equalsIgnoreCase(".FALIGN")) {
                if (address % 16 <= 0) {
                    continue;
                }
                address = address + 16 - address % 16;
            }
            else {
                final InstructionDef instructionDef = (InstructionDef) ISA.formatToDef.get(instruction.getFormat());
                if (instructionDef == null) {
                    throw new AsException(instruction, "Undefined opcode '" + instruction.getOpcode() + "'");
                }
                if (instructionDef.isData()) {
                    this.checkAddr(symbolTable2, b, false, offsetImmediate);
                    instruction.setAddress(offsetImmediate);
                    final int nextAddress = instructionDef.getNextAddress(instruction);
                    this.checkAddr(symbolTable2, b, false, nextAddress - 1);
                    for (int i = offsetImmediate; i < nextAddress; ++i) {
                        final int n = b ? (i - 32768) : (i - 0);
                        if (this.bitmap[n]) {
                            throw new AsException("Data already at this address: " + offsetImmediate + " (" + symbolTable2.lookupAddr(n) + ")");
                        }
                        this.bitmap[n] = true;
                    }
                    offsetImmediate = nextAddress;
                }
                else {
                    final int n2 = b ? (address - 32768) : (address - 0);
                    this.checkAddr(symbolTable2, b, true, address);
                    if (this.bitmap[n2]) {
                        throw new AsException("Instruction already at address " + address + ", new : " + instruction.getOriginalLine());
                    }
                    this.bitmap[n2] = true;
                    instruction.setAddress(address);
                    address = instructionDef.getNextAddress(instruction);
                    instruction.setSrcFileName(stringz);
                    instruction.setSrcLineNumber(offsetImmediate2);
                }
                list2.add(instruction);
            }
        }
        return list2;
    }
    
    void formatObjectFile(final SymbolTable symbolTable, final List<Instruction> list, final String s) throws AsException {
        final String string = s + ".obj";
        try {
            final ArrayList<Word> list2 = new ArrayList<Word>();
            final BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(string));
            int n = -1;
            int address = -1;
            int n2 = 0;
            for (final Instruction instruction : list) {
                if (instruction.getOpcode() == null) {
                    Console.println(instruction.getOriginalLine());
                }
                final InstructionDef instructionDef = (InstructionDef) ISA.formatToDef.get(instruction.getFormat());
                if (instructionDef != null) {
                    if (instructionDef.isData()) {
                        continue;
                    }
                    if (address != -1 && instruction.getAddress() != address + 1) {
                        new Word(51934).writeWordToFile(bufferedOutputStream);
                        new Word(n).writeWordToFile(bufferedOutputStream);
                        new Word(n2).writeWordToFile(bufferedOutputStream);
                        final Iterator<Word> iterator2 = list2.iterator();
                        while (iterator2.hasNext()) {
                            iterator2.next().writeWordToFile(bufferedOutputStream);
                        }
                        list2.clear();
                        n2 = 0;
                        n = -1;
                    }
                    instructionDef.encode(symbolTable, instruction, list2);
                    if (n == -1) {
                        n = instruction.getAddress();
                    }
                    address = instruction.getAddress();
                    ++n2;
                }
            }
            if (address != -1) {
                new Word(51934).writeWordToFile(bufferedOutputStream);
                new Word(n).writeWordToFile(bufferedOutputStream);
                new Word(n2).writeWordToFile(bufferedOutputStream);
                final Iterator<Word> iterator3 = list2.iterator();
                while (iterator3.hasNext()) {
                    iterator3.next().writeWordToFile(bufferedOutputStream);
                }
                list2.clear();
                n2 = 0;
                address = (n = -1);
            }
            for (final Instruction instruction2 : list) {
                if (instruction2.getOpcode() == null) {
                    Console.println(instruction2.getOriginalLine());
                }
                final InstructionDef instructionDef2 = (InstructionDef) ISA.formatToDef.get(instruction2.getFormat());
                if (instructionDef2 != null) {
                    if (!instructionDef2.isData()) {
                        continue;
                    }
                    if (address != -1 && instruction2.getAddress() != address + 1) {
                        new Word(56026).writeWordToFile(bufferedOutputStream);
                        new Word(n).writeWordToFile(bufferedOutputStream);
                        new Word(n2).writeWordToFile(bufferedOutputStream);
                        final Iterator<Word> iterator5 = list2.iterator();
                        while (iterator5.hasNext()) {
                            iterator5.next().writeWordToFile(bufferedOutputStream);
                        }
                        list2.clear();
                        n2 = 0;
                        n = -1;
                    }
                    instructionDef2.encode(null, instruction2, list2);
                    if (n == -1) {
                        n = instruction2.getAddress();
                    }
                    address = instructionDef2.getNextAddress(instruction2) - 1;
                    n2 += instructionDef2.getNextAddress(instruction2) - instruction2.getAddress();
                }
            }
            if (address != -1) {
                new Word(56026).writeWordToFile(bufferedOutputStream);
                new Word(n).writeWordToFile(bufferedOutputStream);
                new Word(n2).writeWordToFile(bufferedOutputStream);
                final Iterator<Word> iterator6 = list2.iterator();
                while (iterator6.hasNext()) {
                    iterator6.next().writeWordToFile(bufferedOutputStream);
                }
                list2.clear();
            }
            final Enumeration<?> syms = symbolTable.getSyms();
            while (syms.hasMoreElements()) {
                final String s2 = (String) syms.nextElement();
                new Word(50103).writeWordToFile(bufferedOutputStream);
                new Word(symbolTable.lookupSym(s2)).writeWordToFile(bufferedOutputStream);
                new Word(s2.length()).writeWordToFile(bufferedOutputStream);
                bufferedOutputStream.write(s2.getBytes(), 0, s2.length());
            }
            final ArrayList<String> list3 = new ArrayList<String>();
            final Iterator<Instruction> iterator7 = list.iterator();
            while (iterator7.hasNext()) {
                final String srcFileName = iterator7.next().getSrcFileName();
                if (srcFileName != null && !list3.contains(srcFileName)) {
                    list3.add(srcFileName);
                }
            }
            for (int i = 0; i < list3.size(); ++i) {
                final String s3 = list3.get(i);
                new Word(61822).writeWordToFile(bufferedOutputStream);
                new Word(s3.length()).writeWordToFile(bufferedOutputStream);
                bufferedOutputStream.write(s3.getBytes(), 0, s3.length());
            }
            for (final Instruction instruction3 : list) {
                if (instruction3.getSrcLineNumber() == -1) {
                    continue;
                }
                new Word(29022).writeWordToFile(bufferedOutputStream);
                new Word(instruction3.getAddress()).writeWordToFile(bufferedOutputStream);
                new Word(instruction3.getSrcLineNumber()).writeWordToFile(bufferedOutputStream);
                new Word(list3.indexOf(instruction3.getSrcFileName())).writeWordToFile(bufferedOutputStream);
            }
            bufferedOutputStream.close();
        }
        catch (IOException ex) {
            throw new AsException("Couldn't write file (" + string + ")");
        }
    }
    
    private String formatAddress(final int n) {
        final String string = "0000" + Integer.toHexString(n).toUpperCase();
        return string.substring(string.length() - 4);
    }
}
