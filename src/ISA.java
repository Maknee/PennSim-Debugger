import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;

// 
// Decompiled by Procyon v0.5.30
// 

public class ISA
{
    public static InstructionDef[] lookupTable;
    public static HashSet opcodeSet;
    public static Hashtable formatToDef;
    
    public static String disassemble(final Word word, final int n, final Machine machine) {
        final InstructionDef instructionDef = ISA.lookupTable[word.getValue()];
        if (instructionDef == null) {
            return ".FILL " + word.toHex();
        }
        return instructionDef.disassemble(word, n, machine);
    }
    
    public static boolean isOpcode(final String s) {
        return ISA.opcodeSet.contains(s.toUpperCase());
    }
    
    public static InstructionDef getInstruction(final Word word) {
    	return ISA.lookupTable[word.getValue()];
    }
    
    public static void checkFormat(final Instruction instruction, final int n) throws AsException {
        if (ISA.formatToDef.get(instruction.getFormat()) == null) {
            throw new AsException(instruction, "Unknown instruction format: '" + instruction.getFormat() + "'");
        }
    }
    
    public static void encode(final Instruction instruction, final List list) throws AsException {
        final String format = instruction.getFormat();
        if (ISA.formatToDef.get(format) == null) {
            instruction.error("Unknown instruction format: '" + format + "'");
        }
    }
    
    public static boolean isCall(final Word word) throws IllegalInstructionException {
        final InstructionDef instructionDef = ISA.lookupTable[word.getValue()];
        if (instructionDef != null) {
            return instructionDef.isCall();
        }
        throw new IllegalInstructionException("Undefined instruction:  " + word.toHex());
    }
    
    public static void createDef(final String opcode, final String encoding, final InstructionDef instructionDef) {
        instructionDef.setOpcode(opcode);
        if (encoding != null) {
            instructionDef.setEncoding(encoding);
            if (!instructionDef.isData() && !instructionDef.isPseudo() && !instructionDef.isDirective()) {
                int n = 0;
                int n2 = 0;
                for (int i = 0; i < 65535; ++i) {
                    if (instructionDef.match(new Word(i))) {
                        if (ISA.lookupTable[i] == null) {
                            ++n;
                            ISA.lookupTable[i] = instructionDef;
                        }
                        else {
                            ++n2;
                        }
                    }
                }
                check(n > 0 || n2 > 0, "Useless instruction defined, probably an error, opcode=" + opcode);
            }
        }
        ISA.formatToDef.put(instructionDef.getFormat(), instructionDef);
        ISA.opcodeSet.add(instructionDef.getOpcode().toUpperCase());
    }
    
    public static void check(final boolean b, final String s) {
        if (!b) {
            throw new InternalException(s);
        }
    }
    
    protected static void labelRefToPCOffset(final SymbolTable symbolTable, final Instruction instruction, final int n) throws AsException {
        final int n2 = instruction.getAddress() + 1;
        final int lookupSym = symbolTable.lookupSym(instruction.getLabelRef());
        if (lookupSym == Integer.MAX_VALUE) {
            throw new AsException(instruction, "Undeclared label '" + instruction.getLabelRef() + "'");
        }
        final int offsetImmediate = lookupSym - n2;
        if (offsetImmediate < -(1 << n - 1) || offsetImmediate > 1 << n - 1) {
            throw new AsException(instruction, "Jump offset longer than " + n + " bits");
        }
        instruction.setOffsetImmediate(offsetImmediate);
    }
    
    protected void init() {
        createDef(".OS", "xxxxxxxxxxxxxxxx", new DirectiveDef());
        createDef(".CODE", "xxxxxxxxxxxxxxxx", new DirectiveDef());
        createDef(".DATA", "xxxxxxxxxxxxxxxx", new DirectiveDef());
        createDef(".FALIGN", "xxxxxxxxxxxxxxxx", new DirectiveDef());
        createDef(".ADDR", "uuuuuuuuuuuuuuuu", new DirectiveDef());
        createDef(".FILE", "zzzzzzzzzzzzzzzz", new DirectiveDef());
        createDef(".LOC", "uuuuuuuuuuuuuuuu", new DirectiveDef());
        createDef(".CONST", "iiiiiiiiiiiiiiii", new DirectiveDef());
        createDef(".UCONST", "uuuuuuuuuuuuuuuu", new DirectiveDef());
        createDef(".FILL", "iiiiiiiiiiiiiiii", new DataDef() {
            @Override
            public void encode(final SymbolTable symbolTable, final Instruction instruction, final List list) throws AsException {
                list.add(new Word(instruction.getOffsetImmediate()));
            }
        });
        createDef(".FILL", "uuuuuuuuuuuuuuuu", new DataDef() {
            @Override
            public void encode(final SymbolTable symbolTable, final Instruction instruction, final List list) throws AsException {
                list.add(new Word(instruction.getOffsetImmediate()));
            }
        });
        createDef(".BLKW", "uuuuuuuuuuuuuuuu", new DataDef() {
            @Override
            public void encode(final SymbolTable symbolTable, final Instruction instruction, final List list) throws AsException {
                for (int offsetImmediate = instruction.getOffsetImmediate(), i = 0; i < offsetImmediate; ++i) {
                    list.add(new Word(0));
                }
            }
            
            @Override
            public int getNextAddress(final Instruction instruction) throws AsException {
                return instruction.getAddress() + instruction.getOffsetImmediate();
            }
        });
        createDef(".STRINGZ", "zzzzzzzzzzzzzzzz", new DataDef() {
            @Override
            public void encode(final SymbolTable symbolTable, final Instruction instruction, final List list) throws AsException {
                for (int i = 0; i < instruction.getStringz().length(); ++i) {
                    list.add(new Word(instruction.getStringz().charAt(i)));
                }
                list.add(new Word(0));
            }
            
            @Override
            public int getNextAddress(final Instruction instruction) throws AsException {
                return instruction.getAddress() + instruction.getStringz().length() + 1;
            }
        });
    }
    
    static {
        ISA.lookupTable = new InstructionDef[65536];
        ISA.opcodeSet = new HashSet();
        ISA.formatToDef = new Hashtable();
    }
}
