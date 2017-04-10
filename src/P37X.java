// 
// Decompiled by Procyon v0.5.30
// 

public class P37X extends ISA
{
    public void init() {
        super.init();
        ISA.createDef("ADD", "0000 ddd sss ttt 100", new InstructionDef() {
            @Override
            public int getDestinationReg(final Word word) {
                return this.getDReg(word);
            }
            
            @Override
            public int getSourceReg1(final Word word) {
                return this.getSReg(word);
            }
            
            @Override
            public int getSourceReg2(final Word word) {
                return this.getTReg(word);
            }
            
            @Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                registerFile.setRegister(this.getDReg(word), registerFile.getRegister(this.getSReg(word)) + registerFile.getRegister(this.getTReg(word)));
                return n + 1;
            }
        });
        ISA.createDef("SUB", "0000 ddd sss ttt 101", new InstructionDef() {
            @Override
            public int getDestinationReg(final Word word) {
                return this.getDReg(word);
            }
            
            @Override
            public int getSourceReg1(final Word word) {
                return this.getSReg(word);
            }
            
            @Override
            public int getSourceReg2(final Word word) {
                return this.getTReg(word);
            }
            
            @Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                registerFile.setRegister(this.getDReg(word), registerFile.getRegister(this.getSReg(word)) - registerFile.getRegister(this.getTReg(word)));
                return n + 1;
            }
        });
        ISA.createDef("MUL", "0000 ddd sss ttt 110", new InstructionDef() {
            @Override
            public int getDestinationReg(final Word word) {
                return this.getDReg(word);
            }
            
            @Override
            public int getSourceReg1(final Word word) {
                return this.getSReg(word);
            }
            
            @Override
            public int getSourceReg2(final Word word) {
                return this.getTReg(word);
            }
            
            @Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                registerFile.setRegister(this.getDReg(word), registerFile.getRegister(this.getSReg(word)) * registerFile.getRegister(this.getTReg(word)));
                return n + 1;
            }
        });
        ISA.createDef("OR", "0001 ddd sss ttt 000", new InstructionDef() {
            @Override
            public int getDestinationReg(final Word word) {
                return this.getDReg(word);
            }
            
            @Override
            public int getSourceReg1(final Word word) {
                return this.getSReg(word);
            }
            
            @Override
            public int getSourceReg2(final Word word) {
                return this.getTReg(word);
            }
            
            @Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                registerFile.setRegister(this.getDReg(word), registerFile.getRegister(this.getSReg(word)) | registerFile.getRegister(this.getTReg(word)));
                return n + 1;
            }
        });
        ISA.createDef("NOT", "0001 ddd sss xxx 001", new InstructionDef() {
            @Override
            public int getDestinationReg(final Word word) {
                return this.getDReg(word);
            }
            
            @Override
            public int getSourceReg1(final Word word) {
                return this.getSReg(word);
            }
            
            @Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                registerFile.setRegister(this.getDReg(word), ~registerFile.getRegister(this.getSReg(word)));
                return n + 1;
            }
        });
        ISA.createDef("AND", "0001 ddd sss ttt 010", new InstructionDef() {
            @Override
            public int getDestinationReg(final Word word) {
                return this.getDReg(word);
            }
            
            @Override
            public int getSourceReg1(final Word word) {
                return this.getSReg(word);
            }
            
            @Override
            public int getSourceReg2(final Word word) {
                return this.getTReg(word);
            }
            
            @Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                registerFile.setRegister(this.getDReg(word), registerFile.getRegister(this.getSReg(word)) & registerFile.getRegister(this.getTReg(word)));
                return n + 1;
            }
        });
        ISA.createDef("XOR", "0001 ddd sss ttt 011", new InstructionDef() {
            @Override
            public int getDestinationReg(final Word word) {
                return this.getDReg(word);
            }
            
            @Override
            public int getSourceReg1(final Word word) {
                return this.getSReg(word);
            }
            
            @Override
            public int getSourceReg2(final Word word) {
                return this.getTReg(word);
            }
            
            @Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                registerFile.setRegister(this.getDReg(word), registerFile.getRegister(this.getSReg(word)) ^ registerFile.getRegister(this.getTReg(word)));
                return n + 1;
            }
        });
        ISA.createDef("SLL", "0001 ddd sss ttt 100", new InstructionDef() {
            @Override
            public int getDestinationReg(final Word word) {
                return this.getDReg(word);
            }
            
            @Override
            public int getSourceReg1(final Word word) {
                return this.getSReg(word);
            }
            
            @Override
            public int getSourceReg2(final Word word) {
                return this.getTReg(word);
            }
            
            @Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                registerFile.setRegister(this.getDReg(word), registerFile.getRegister(this.getSReg(word)) << (registerFile.getRegister(this.getTReg(word)) & 0xF));
                return n + 1;
            }
        });
        ISA.createDef("SRL", "0001 ddd sss ttt 101", new InstructionDef() {
            @Override
            public int getDestinationReg(final Word word) {
                return this.getDReg(word);
            }
            
            @Override
            public int getSourceReg1(final Word word) {
                return this.getSReg(word);
            }
            
            @Override
            public int getSourceReg2(final Word word) {
                return this.getTReg(word);
            }
            
            @Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                registerFile.setRegister(this.getDReg(word), registerFile.getRegister(this.getSReg(word)) >>> (registerFile.getRegister(this.getTReg(word)) & 0xF));
                return n + 1;
            }
        });
        ISA.createDef("SRA", "0001 ddd sss ttt 110", new InstructionDef() {
            @Override
            public int getDestinationReg(final Word word) {
                return this.getDReg(word);
            }
            
            @Override
            public int getSourceReg1(final Word word) {
                return this.getSReg(word);
            }
            
            @Override
            public int getSourceReg2(final Word word) {
                return this.getTReg(word);
            }
            
            @Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                registerFile.setRegister(this.getDReg(word), (short)registerFile.getRegister(this.getSReg(word)) >> (registerFile.getRegister(this.getTReg(word)) & 0xF));
                return n + 1;
            }
        });
        ISA.createDef("GETC", "0010 0000 00100000", new TrapDef());
        ISA.createDef("PUTC", "0010 0000 00100001", new TrapDef());
        ISA.createDef("PUTS", "0010 0000 00100010", new TrapDef());
        ISA.createDef("EGETC", "0010 0000 00100011", new TrapDef());
        ISA.createDef("HALT", "0010 0000 00100101", new TrapDef());
        ISA.createDef("TRAP", "0010 0000 uuuuuuuu", new TrapDef());
        ISA.createDef("RTT", "0011 ddd xxxxxxxxx", new InstructionDef() {
            @Override
            public int getSourceReg1(final Word word) {
                return this.getDReg(word);
            }
            
            @Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                registerFile.setPrivMode(false);
                return registerFile.getRegister(this.getDReg(word));
            }
        });
        ISA.createDef("JUMP", "0100 pppppppppppp", new InstructionDef() {
            @Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                return n + 1 + this.getPCOffset(word);
            }
        });
        ISA.createDef("JUMPR", "0101 ddd xxxxxxxxx", new InstructionDef() {
            @Override
            public int getSourceReg1(final Word word) {
                return this.getDReg(word);
            }
            
            @Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                return registerFile.getRegister(this.getDReg(word));
            }
        });
        ISA.createDef("JSR", "0110 pppppppppppp", new InstructionDef() {
            @Override
            public int getDestinationReg(final Word word) {
                return 7;
            }
            
            @Override
            public boolean isCall() {
                return true;
            }
            
            @Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                registerFile.setRegister(7, n + 1);
                return n + 1 + this.getPCOffset(word);
            }
        });
        ISA.createDef("JSRR", "0111 ddd xxxxxxxxx", new InstructionDef() {
            @Override
            public int getDestinationReg(final Word word) {
                return 7;
            }
            
            @Override
            public int getSourceReg1(final Word word) {
                return this.getDReg(word);
            }
            
            @Override
            public boolean isCall() {
                return true;
            }
            
            @Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                final int register = registerFile.getRegister(this.getDReg(word));
                registerFile.setRegister(7, n + 1);
                return register;
            }
        });
        ISA.createDef("NOOP", "1000 xxx 000 xxxxxx", new InstructionDef() {
            @Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                return n + 1;
            }
        });
        ISA.createDef("BRP", "1000 ddd 001 pppppp", new InstructionDef() {
            @Override
            public boolean isBranch() {
                return true;
            }
            
            @Override
            public int getSourceReg1(final Word word) {
                return this.getDReg(word);
            }
            
            @Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                final int n2 = registerFile.getRegister(this.getDReg(word)) & 0xFFFF;
                if (n2 != 0 && (n2 & 0x8000) == 0x0) {
                    return n + 1 + this.getPCOffset(word);
                }
                return n + 1;
            }
        });
        ISA.createDef("BRZ", "1000 ddd 010 pppppp", new InstructionDef() {
            @Override
            public boolean isBranch() {
                return true;
            }
            
            @Override
            public int getSourceReg1(final Word word) {
                return this.getDReg(word);
            }
            
            @Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                if ((registerFile.getRegister(this.getDReg(word)) & 0xFFFF) == 0x0) {
                    return n + 1 + this.getPCOffset(word);
                }
                return n + 1;
            }
        });
        ISA.createDef("BRZP", "1000 ddd 011 pppppp", new InstructionDef() {
            @Override
            public boolean isBranch() {
                return true;
            }
            
            @Override
            public int getSourceReg1(final Word word) {
                return this.getDReg(word);
            }
            
            @Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                final int n2 = registerFile.getRegister(this.getDReg(word)) & 0xFFFF;
                if (n2 == 0 || (n2 & 0x8000) == 0x0) {
                    return n + 1 + this.getPCOffset(word);
                }
                return n + 1;
            }
        });
        ISA.createDef("BRN", "1000 ddd 100 pppppp", new InstructionDef() {
            @Override
            public boolean isBranch() {
                return true;
            }
            
            @Override
            public int getSourceReg1(final Word word) {
                return this.getDReg(word);
            }
            
            @Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                if ((registerFile.getRegister(this.getDReg(word)) & 0xFFFF & 0x8000) != 0x0) {
                    return n + 1 + this.getPCOffset(word);
                }
                return n + 1;
            }
        });
        ISA.createDef("BRNP", "1000 ddd 101 pppppp", new InstructionDef() {
            @Override
            public boolean isBranch() {
                return true;
            }
            
            @Override
            public int getSourceReg1(final Word word) {
                return this.getDReg(word);
            }
            
            @Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                if ((registerFile.getRegister(this.getDReg(word)) & 0xFFFF) != 0x0) {
                    return n + 1 + this.getPCOffset(word);
                }
                return n + 1;
            }
        });
        ISA.createDef("BRNZ", "1000 ddd 110 pppppp", new InstructionDef() {
            @Override
            public boolean isBranch() {
                return true;
            }
            
            @Override
            public int getSourceReg1(final Word word) {
                return this.getDReg(word);
            }
            
            @Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                final int n2 = registerFile.getRegister(this.getDReg(word)) & 0xFFFF;
                if (n2 == 0 || (n2 & 0x8000) != 0x0) {
                    return n + 1 + this.getPCOffset(word);
                }
                return n + 1;
            }
        });
        ISA.createDef("BRNZP", "1000 ddd 111 pppppp", new InstructionDef() {
            @Override
            public boolean isBranch() {
                return true;
            }
            
            @Override
            public int getSourceReg1(final Word word) {
                return this.getDReg(word);
            }
            
            @Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                return n + 1 + this.getPCOffset(word);
            }
        });
        ISA.createDef("CONST", "1001 ddd iiiiiiiii", new InstructionDef() {
            @Override
            public int getDestinationReg(final Word word) {
                return this.getDReg(word);
            }
            
            @Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                registerFile.setRegister(this.getDReg(word), this.getSignedImmed(word));
                return n + 1;
            }
        });
        ISA.createDef("INC", "1010 ddd iiiiiiiii", new InstructionDef() {
            @Override
            public int getDestinationReg(final Word word) {
                return this.getDReg(word);
            }
            
            @Override
            public int getSourceReg1(final Word word) {
                return this.getDReg(word);
            }
            
            @Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                registerFile.setRegister(this.getDReg(word), registerFile.getRegister(this.getDReg(word)) + this.getSignedImmed(word));
                return n + 1;
            }
        });
        ISA.createDef("LEA", "1011 ddd ppppppppp", new InstructionDef() {
            @Override
            public int getDestinationReg(final Word word) {
                return this.getDReg(word);
            }
            
            @Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                registerFile.setRegister(this.getDReg(word), n + 1 + this.getPCOffset(word));
                return n + 1;
            }
        });
        ISA.createDef("LDR", "1100 ddd sss iiiiii", new InstructionDef() {
            @Override
            public boolean isLoad() {
                return true;
            }
            
            @Override
            public int getDestinationReg(final Word word) {
                return this.getDReg(word);
            }
            
            @Override
            public int getSourceReg1(final Word word) {
                return this.getSReg(word);
            }
            
            @Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) throws IllegalMemAccessException {
                registerFile.setRegister(this.getDReg(word), memory.checkAndRead(registerFile.getRegister(this.getSReg(word)) + this.getSignedImmed(word), registerFile.getPrivMode()).getValue());
                return n + 1;
            }
        });
        ISA.createDef("STR", "1101 ddd sss iiiiii", new InstructionDef() {
            @Override
            public boolean isStore() {
                return true;
            }
            
            @Override
            public int getSourceReg1(final Word word) {
                return this.getDReg(word);
            }
            
            @Override
            public int getSourceReg2(final Word word) {
                return this.getSReg(word);
            }
            
            @Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) throws IllegalMemAccessException {
                memory.checkAndWrite(registerFile.getRegister(this.getSReg(word)) + this.getSignedImmed(word), registerFile.getRegister(this.getDReg(word)), registerFile.getPrivMode());
                return n + 1;
            }
        });
        ISA.createDef("LD", "1110 ddd ppppppppp", new InstructionDef() {
            @Override
            public boolean isLoad() {
                return true;
            }
            
            @Override
            public int getDestinationReg(final Word word) {
                return this.getDReg(word);
            }
            
            @Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) throws IllegalMemAccessException {
                registerFile.setRegister(this.getDReg(word), memory.checkAndRead(n + 1 + this.getPCOffset(word), registerFile.getPrivMode()).getValue());
                return n + 1;
            }
        });
        ISA.createDef("ST", "1111 ddd ppppppppp", new InstructionDef() {
            @Override
            public boolean isStore() {
                return true;
            }
            
            @Override
            public int getSourceReg1(final Word word) {
                return this.getDReg(word);
            }
            
            @Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) throws IllegalMemAccessException {
                memory.checkAndWrite(n + 1 + this.getPCOffset(word), registerFile.getRegister(this.getDReg(word)), registerFile.getPrivMode());
                return n + 1;
            }
        });
    }
    
    private class TrapDef extends InstructionDef
    {
        @Override
        public boolean isCall() {
            return true;
        }
        
        @Override
        public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
            registerFile.setPrivMode(true);
            registerFile.setRegister(7, n + 1);
            return word.getZext(8, 0);
        }
    }
}
