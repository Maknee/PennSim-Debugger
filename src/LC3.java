// 
// Decompiled by Procyon v0.5.30
// 

public class LC3 extends ISA
{
    public void init() {
        super.init();
        ISA.createDef("BR", "0000 111 ppppppppp", new BranchDef());
        ISA.createDef("BRnzp", "0000 111 ppppppppp", new BranchDef());
        ISA.createDef("BRp", "0000 001 ppppppppp", new BranchDef());
        ISA.createDef("BRz", "0000 010 ppppppppp", new BranchDef());
        ISA.createDef("BRzp", "0000 011 ppppppppp", new BranchDef());
        ISA.createDef("BRn", "0000 100 ppppppppp", new BranchDef());
        ISA.createDef("BRnp", "0000 101 ppppppppp", new BranchDef());
        ISA.createDef("BRnz", "0000 110 ppppppppp", new BranchDef());
        ISA.createDef("NOP", "0000 000 000000000", new InstructionDef() {
           	
        	@Override
            public ControlSignals decodeSignals() {
           	 	ControlSignals signals = new ControlSignals();
           	 	signals.PCMuxCTL = 1;
           	 	signals.rsMuxCTL = 15;
           		signals.rtMuxCTL = 15;
                signals.rdMuxCTL = 15;
                signals.regFileWE = 0;
                signals.regInputMuxCTL = 15;
                signals.ArithCTL = 15;
                signals.ArithMuxCTL = 15;
                signals.LOGICCTL = 15;
                signals.LogicMuxCTL = 15;
                signals.SHIFTCTL = 15;
                signals.CONSTCTL = 15;
                signals.CMPCTL = 15;
                signals.ALUMuxCTL = 15;
                signals.NZPWE = 0;
                signals.DATAWE = 0;
                return signals;
            }
        	
        	@Override
        	public boolean isNop() {
        		return true;
        	}
        	
        	@Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                return n + 1;
            }
        });
        ISA.createDef("ADD", "0001 ddd sss 0 00 ttt", new InstructionDef() {
           	
        	@Override
            public ControlSignals decodeSignals() {
           	 	ControlSignals signals = new ControlSignals();
           	 	signals.PCMuxCTL = 1;
           	 	signals.rsMuxCTL = 0;
           		signals.rtMuxCTL = 0;
                signals.rdMuxCTL = 0;
                signals.regFileWE = 1;
                signals.regInputMuxCTL = 0;
                signals.ArithCTL = 0;
                signals.ArithMuxCTL = 0;
                signals.LOGICCTL = 15;
                signals.LogicMuxCTL = 15;
                signals.SHIFTCTL = 15;
                signals.CONSTCTL = 15;
                signals.CMPCTL = 15;
                signals.ALUMuxCTL = 0;
                signals.NZPWE = 1;
                signals.DATAWE = 0;
                return signals;
            }
        	
        	@Override
            public boolean isAdd() {
            	return true;
            }
        	
           	@Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                final int nzp = registerFile.getRegister(this.getSReg(word)) + registerFile.getRegister(this.getTReg(word));
                registerFile.setRegister(this.getDReg(word), nzp);
                registerFile.setNZP(nzp);
                return n + 1;
            }
        });
        ISA.createDef("MUL", "0001 ddd sss 0 01 ttt", new InstructionDef() {
           	
        	@Override
            public ControlSignals decodeSignals() {
           	 	ControlSignals signals = new ControlSignals();
           	 	signals.PCMuxCTL = 1;
           	 	signals.rsMuxCTL = 0;
           		signals.rtMuxCTL = 0;
                signals.rdMuxCTL = 0;
                signals.regFileWE = 1;
                signals.regInputMuxCTL = 0;
                signals.ArithCTL = 1;
                signals.ArithMuxCTL = 0;
                signals.LOGICCTL = 15;
                signals.LogicMuxCTL = 15;
                signals.SHIFTCTL = 15;
                signals.CONSTCTL = 15;
                signals.CMPCTL = 15;
                signals.ALUMuxCTL = 0;
                signals.NZPWE = 1;
                signals.DATAWE = 0;
                return signals;
            }
        	
        	@Override
            public boolean isMul() {
            	return true;
            }
        	
           	@Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                final int nzp = registerFile.getRegister(this.getSReg(word)) * registerFile.getRegister(this.getTReg(word));
                registerFile.setRegister(this.getDReg(word), nzp);
                registerFile.setNZP(nzp);
                return n + 1;
            }
        });
        ISA.createDef("SUB", "0001 ddd sss 0 10 ttt", new InstructionDef() {
           	
        	@Override
            public ControlSignals decodeSignals() {
           	 	ControlSignals signals = new ControlSignals();
           	 	signals.PCMuxCTL = 1;
           	 	signals.rsMuxCTL = 0;
           		signals.rtMuxCTL = 0;
                signals.rdMuxCTL = 0;
                signals.regFileWE = 1;
                signals.regInputMuxCTL = 0;
                signals.ArithCTL = 2;
                signals.ArithMuxCTL = 0;
                signals.LOGICCTL = 15;
                signals.LogicMuxCTL = 15;
                signals.SHIFTCTL = 15;
                signals.CONSTCTL = 15;
                signals.CMPCTL = 15;
                signals.ALUMuxCTL = 0;
                signals.NZPWE = 1;
                signals.DATAWE = 0;
                return signals;
            }
        	
        	@Override
            public boolean isSub() {
            	return true;
            }
        	
           	@Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                final int nzp = registerFile.getRegister(this.getSReg(word)) - registerFile.getRegister(this.getTReg(word));
                registerFile.setRegister(this.getDReg(word), nzp);
                registerFile.setNZP(nzp);
                return n + 1;
            }
        });
        ISA.createDef("DIV", "0001 ddd sss 0 11 ttt", new InstructionDef() {
           	
        	@Override
            public ControlSignals decodeSignals() {
           	 	ControlSignals signals = new ControlSignals();
           	 	signals.PCMuxCTL = 1;
           	 	signals.rsMuxCTL = 0;
           		signals.rtMuxCTL = 0;
                signals.rdMuxCTL = 0;
                signals.regFileWE = 1;
                signals.regInputMuxCTL = 0;
                signals.ArithCTL = 3;
                signals.ArithMuxCTL = 0;
                signals.LOGICCTL = 15;
                signals.LogicMuxCTL = 15;
                signals.SHIFTCTL = 15;
                signals.CONSTCTL = 15;
                signals.CMPCTL = 15;
                signals.ALUMuxCTL = 0;
                signals.NZPWE = 1;
                signals.DATAWE = 0;
                return signals;
            }
        	
        	@Override
            public boolean isDiv() {
            	return true;
            }
        	
           	@Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                final int register = registerFile.getRegister(this.getTReg(word));
                int nzp = 0;
                if (register != 0) {
                    nzp = registerFile.getRegister(this.getSReg(word)) / registerFile.getRegister(this.getTReg(word));
                }
            	//System.out.println("current: " + nzp + " after: " + (nzp & 0xFFFF) );
                registerFile.setRegister(this.getDReg(word), nzp);
                registerFile.setNZP(nzp);
                return n + 1;
            }
        });
        ISA.createDef("ADD", "0001 ddd sss 1 iiiii", new InstructionDef() {
           	
        	@Override
            public ControlSignals decodeSignals() {
           	 	ControlSignals signals = new ControlSignals();
           	 	signals.PCMuxCTL = 1;
           	 	signals.rsMuxCTL = 0;
           		signals.rtMuxCTL = 15;
                signals.rdMuxCTL = 0;
                signals.regFileWE = 1;
                signals.regInputMuxCTL = 0;
                signals.ArithCTL = 0;
                signals.ArithMuxCTL = 1;
                signals.LOGICCTL = 15;
                signals.LogicMuxCTL = 15;
                signals.SHIFTCTL = 15;
                signals.CONSTCTL = 15;
                signals.CMPCTL = 15;
                signals.ALUMuxCTL = 0;
                signals.NZPWE = 1;
                signals.DATAWE = 0;
                return signals;
            }
        	
        	@Override
            public boolean isAddIMM() {
            	return true;
            }
        	
           	@Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                final int nzp = registerFile.getRegister(this.getSReg(word)) + this.getSignedImmed(word);
                registerFile.setRegister(this.getDReg(word), nzp);
                registerFile.setNZP(nzp);
                return n + 1;
            }
        });
        ISA.createDef("CMP", "0010 sss 00 0000 ttt", new InstructionDef() {
           	
        	@Override
            public ControlSignals decodeSignals() {
           	 	ControlSignals signals = new ControlSignals();
           	 	signals.PCMuxCTL = 1;
           	 	signals.rsMuxCTL = 2;
           		signals.rtMuxCTL = 0;
                signals.rdMuxCTL = 15;
                signals.regFileWE = 0;
                signals.regInputMuxCTL = 0;
                signals.ArithCTL = 15;
                signals.ArithMuxCTL = 15;
                signals.LOGICCTL = 15;
                signals.LogicMuxCTL = 15;
                signals.SHIFTCTL = 15;
                signals.CONSTCTL = 15;
                signals.CMPCTL = 0;
                signals.ALUMuxCTL = 4;
                signals.NZPWE = 1;
                signals.DATAWE = 0;
                return signals;
            }
        	
        	@Override
            public boolean isCMP() {
            	return true;
            }
        	
           	@Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                final short n2 = (short)registerFile.getRegister(this.getSReg(word));
                final short n3 = (short)registerFile.getRegister(this.getTReg(word));
                if (n2 > n3) {
                    registerFile.setP();
                }
                else if (n2 == n3) {
                    registerFile.setZ();
                }
                else {
                    registerFile.setN();
                }
                return n + 1;
            }
        });
        ISA.createDef("CMPU", "0010 sss 01 0000 ttt", new InstructionDef() {
           	
        	@Override
            public ControlSignals decodeSignals() {
           	 	ControlSignals signals = new ControlSignals();
           	 	signals.PCMuxCTL = 1;
           	 	signals.rsMuxCTL = 2;
           		signals.rtMuxCTL = 0;
                signals.rdMuxCTL = 15;
                signals.regFileWE = 0;
                signals.regInputMuxCTL = 0;
                signals.ArithCTL = 15;
                signals.ArithMuxCTL = 15;
                signals.LOGICCTL = 15;
                signals.LogicMuxCTL = 15;
                signals.SHIFTCTL = 15;
                signals.CONSTCTL = 15;
                signals.CMPCTL = 1;
                signals.ALUMuxCTL = 4;
                signals.NZPWE = 1;
                signals.DATAWE = 0;
                return signals;
            }
        	
        	@Override
            public boolean isCMPU() {
            	return true;
            }
        	
           	@Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                final int register = registerFile.getRegister(this.getSReg(word));
                final int register2 = registerFile.getRegister(this.getTReg(word));
                if (register > register2) {
                    registerFile.setP();
                }
                else if (register == register2) {
                    registerFile.setZ();
                }
                else {
                    registerFile.setN();
                }
                return n + 1;
            }
        });
        ISA.createDef("CMPI", "0010 sss 10 iiiiiii", new InstructionDef() {
           	
        	@Override
            public ControlSignals decodeSignals() {
           	 	ControlSignals signals = new ControlSignals();
           	 	signals.PCMuxCTL = 1;
           	 	signals.rsMuxCTL = 2;
           		signals.rtMuxCTL = 0;
                signals.rdMuxCTL = 15;
                signals.regFileWE = 0;
                signals.regInputMuxCTL = 0;
                signals.ArithCTL = 15;
                signals.ArithMuxCTL = 15;
                signals.LOGICCTL = 15;
                signals.LogicMuxCTL = 15;
                signals.SHIFTCTL = 15;
                signals.CONSTCTL = 15;
                signals.CMPCTL = 2;
                signals.ALUMuxCTL = 4;
                signals.NZPWE = 1;
                signals.DATAWE = 0;
                return signals;
            }
        	
        	@Override
            public boolean isCMPI() {
            	return true;
            }
        	
           	@Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                final short n2 = (short)registerFile.getRegister(this.getSReg(word));
                final short n3 = (short)this.getSignedImmed(word);
                if (n2 > n3) {
                    registerFile.setP();
                }
                else if (n2 == n3) {
                    registerFile.setZ();
                }
                else {
                    registerFile.setN();
                }
                return n + 1;
            }
        });
        ISA.createDef("CMPIU", "0010 sss 11 uuuuuuu", new InstructionDef() {
           	
        	@Override
            public ControlSignals decodeSignals() {
           	 	ControlSignals signals = new ControlSignals();
           	 	signals.PCMuxCTL = 1;
           	 	signals.rsMuxCTL = 2;
           		signals.rtMuxCTL = 0;
                signals.rdMuxCTL = 15;
                signals.regFileWE = 0;
                signals.regInputMuxCTL = 0;
                signals.ArithCTL = 15;
                signals.ArithMuxCTL = 15;
                signals.LOGICCTL = 15;
                signals.LogicMuxCTL = 15;
                signals.SHIFTCTL = 15;
                signals.CONSTCTL = 15;
                signals.CMPCTL = 3;
                signals.ALUMuxCTL = 4;
                signals.NZPWE = 1;
                signals.DATAWE = 0;
                return signals;
            }
        	
        	@Override
            public boolean isCMPIU() {
            	return true;
            }
        	
           	@Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                final int register = registerFile.getRegister(this.getSReg(word));
                final int unsignedImmed = this.getUnsignedImmed(word);
                if (register > unsignedImmed) {
                    registerFile.setP();
                }
                else if (register == unsignedImmed) {
                    registerFile.setZ();
                }
                else {
                    registerFile.setN();
                }
                return n + 1;
            }
        });
        ISA.createDef("JSRR", "0100 000 ddd 000000", new InstructionDef() {
           	
        	@Override
            public ControlSignals decodeSignals() {
           	 	ControlSignals signals = new ControlSignals();
           	 	signals.PCMuxCTL = 3;
           	 	signals.rsMuxCTL = 1;
           		signals.rtMuxCTL = 15;
                signals.rdMuxCTL = 1;
                signals.regFileWE = 1;
                signals.regInputMuxCTL = 2;
                signals.ArithCTL = 15;
                signals.ArithMuxCTL = 15;
                signals.LOGICCTL = 15;
                signals.LogicMuxCTL = 15;
                signals.SHIFTCTL = 15;
                signals.CONSTCTL = 15;
                signals.CMPCTL = 15;
                signals.ALUMuxCTL = 15;
                signals.NZPWE = 1;
                signals.DATAWE = 0;
                return signals;
            }
        	
        	@Override
            public boolean isJSRR() {
                return true;
            }
        	
           	@Override
            public boolean isCall() {
                return true;
            }
            
            @Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                final int register = registerFile.getRegister(this.getDReg(word));
                registerFile.setRegister(7, n + 1);
                registerFile.setNZP(n + 1);
                return register;
            }
        });
        ISA.createDef("JSR", "0100 1 aaaaaaaaaaa", new InstructionDef() {
           	
        	@Override
            public ControlSignals decodeSignals() {
           	 	ControlSignals signals = new ControlSignals();
           	 	signals.PCMuxCTL = 5;
           	 	signals.rsMuxCTL = 15;
           		signals.rtMuxCTL = 15;
                signals.rdMuxCTL = 1;
                signals.regFileWE = 1;
                signals.regInputMuxCTL = 2;
                signals.ArithCTL = 15;
                signals.ArithMuxCTL = 15;
                signals.LOGICCTL = 15;
                signals.LogicMuxCTL = 15;
                signals.SHIFTCTL = 15;
                signals.CONSTCTL = 15;
                signals.CMPCTL = 15;
                signals.ALUMuxCTL = 15;
                signals.NZPWE = 1;
                signals.DATAWE = 0;
                return signals;
            }
        	
        	@Override
        	public boolean isJSR() {
        		return true;
        	}
        	
           	@Override
            public boolean isCall() {
                return true;
            }
            
            @Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                registerFile.setRegister(7, n + 1);
                registerFile.setNZP(n + 1);
                return (n & 0x8000) | this.getAbsAligned(word) << 4;
            }
        });
        ISA.createDef("AND", "0101 ddd sss 0 00 ttt", new InstructionDef() {
           	
        	@Override
            public ControlSignals decodeSignals() {
           	 	ControlSignals signals = new ControlSignals();
           	 	signals.PCMuxCTL = 1;
           	 	signals.rsMuxCTL = 0;
           		signals.rtMuxCTL = 0;
                signals.rdMuxCTL = 0;
                signals.regFileWE = 1;
                signals.regInputMuxCTL = 0;
                signals.ArithCTL = 15;
                signals.ArithMuxCTL = 15;
                signals.LOGICCTL = 0;
                signals.LogicMuxCTL = 0;
                signals.SHIFTCTL = 15;
                signals.CONSTCTL = 15;
                signals.CMPCTL = 15;
                signals.ALUMuxCTL = 1;
                signals.NZPWE = 1;
                signals.DATAWE = 0;
                return signals;
            }
        	
        	@Override
            public boolean isAnd() {
            	return true;
            }
        	
           	@Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                final int nzp = registerFile.getRegister(this.getSReg(word)) & registerFile.getRegister(this.getTReg(word));
                registerFile.setRegister(this.getDReg(word), nzp);
                registerFile.setNZP(nzp);
                return n + 1;
            }
        });
        ISA.createDef("NOT", "0101 ddd sss 0 01 xxx", new InstructionDef() {
           	
        	@Override
            public ControlSignals decodeSignals() {
           	 	ControlSignals signals = new ControlSignals();
           	 	signals.PCMuxCTL = 1;
           	 	signals.rsMuxCTL = 0;
           		signals.rtMuxCTL = 15;
                signals.rdMuxCTL = 0;
                signals.regFileWE = 1;
                signals.regInputMuxCTL = 0;
                signals.ArithCTL = 15;
                signals.ArithMuxCTL = 15;
                signals.LOGICCTL = 1;
                signals.LogicMuxCTL = 0;
                signals.SHIFTCTL = 15;
                signals.CONSTCTL = 15;
                signals.CMPCTL = 15;
                signals.ALUMuxCTL = 1;
                signals.NZPWE = 1;
                signals.DATAWE = 0;
                return signals;
            }
        	
        	@Override
            public boolean isNot() {
            	return true;
            }
        	
           	@Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                final int nzp = ~registerFile.getRegister(this.getSReg(word));
                registerFile.setRegister(this.getDReg(word), nzp);
                registerFile.setNZP(nzp);
                return n + 1;
            }
        });
        ISA.createDef("OR", "0101 ddd sss 0 10 ttt", new InstructionDef() {
           	
        	@Override
            public ControlSignals decodeSignals() {
           	 	ControlSignals signals = new ControlSignals();
           	 	signals.PCMuxCTL = 1;
           	 	signals.rsMuxCTL = 0;
           		signals.rtMuxCTL = 0;
                signals.rdMuxCTL = 0;
                signals.regFileWE = 1;
                signals.regInputMuxCTL = 0;
                signals.ArithCTL = 15;
                signals.ArithMuxCTL = 15;
                signals.LOGICCTL = 2;
                signals.LogicMuxCTL = 0;
                signals.SHIFTCTL = 15;
                signals.CONSTCTL = 15;
                signals.CMPCTL = 15;
                signals.ALUMuxCTL = 1;
                signals.NZPWE = 1;
                signals.DATAWE = 0;
                return signals;
            }
        	
        	@Override
            public boolean isOr() {
            	return true;
            }
        	
           	@Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                final int nzp = registerFile.getRegister(this.getSReg(word)) | registerFile.getRegister(this.getTReg(word));
                registerFile.setRegister(this.getDReg(word), nzp);
                registerFile.setNZP(nzp);
                return n + 1;
            }
        });
        ISA.createDef("XOR", "0101 ddd sss 0 11 ttt", new InstructionDef() {
           	
        	@Override
            public ControlSignals decodeSignals() {
           	 	ControlSignals signals = new ControlSignals();
           	 	signals.PCMuxCTL = 1;
           	 	signals.rsMuxCTL = 0;
           		signals.rtMuxCTL = 0;
                signals.rdMuxCTL = 0;
                signals.regFileWE = 1;
                signals.regInputMuxCTL = 0;
                signals.ArithCTL = 15;
                signals.ArithMuxCTL = 15;
                signals.LOGICCTL = 3;
                signals.LogicMuxCTL = 0;
                signals.SHIFTCTL = 15;
                signals.CONSTCTL = 15;
                signals.CMPCTL = 15;
                signals.ALUMuxCTL = 1;
                signals.NZPWE = 1;
                signals.DATAWE = 0;
                return signals;
            }
        	
        	@Override
            public boolean isXor() {
            	return true;
            }
        	
           	@Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                final int nzp = registerFile.getRegister(this.getSReg(word)) ^ registerFile.getRegister(this.getTReg(word));
                registerFile.setRegister(this.getDReg(word), nzp);
                registerFile.setNZP(nzp);
                return n + 1;
            }
        });
        ISA.createDef("AND", "0101 ddd sss 1 iiiii", new InstructionDef() {
           	
        	@Override
            public ControlSignals decodeSignals() {
           	 	ControlSignals signals = new ControlSignals();
           	 	signals.PCMuxCTL = 1;
           	 	signals.rsMuxCTL = 0;
           		signals.rtMuxCTL = 15;
                signals.rdMuxCTL = 0;
                signals.regFileWE = 1;
                signals.regInputMuxCTL = 0;
                signals.ArithCTL = 15;
                signals.ArithMuxCTL = 15;
                signals.LOGICCTL = 0;
                signals.LogicMuxCTL = 1;
                signals.SHIFTCTL = 15;
                signals.CONSTCTL = 15;
                signals.CMPCTL = 15;
                signals.ALUMuxCTL = 1;
                signals.NZPWE = 1;
                signals.DATAWE = 0;
                return signals;
            }
        	
        	@Override
            public boolean isAndIMM() {
            	return true;
            }
        	
           	@Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                final int nzp = registerFile.getRegister(this.getSReg(word)) & this.getSignedImmed(word);
                registerFile.setRegister(this.getDReg(word), nzp);
                registerFile.setNZP(nzp);
                return n + 1;
            }
        });
        ISA.createDef("LDR", "0110 ddd sss iiiiii", new InstructionDef() {
           	
        	@Override
            public ControlSignals decodeSignals() {
           	 	ControlSignals signals = new ControlSignals();
           	 	signals.PCMuxCTL = 1;
           	 	signals.rsMuxCTL = 0;
           		signals.rtMuxCTL = 15;
                signals.rdMuxCTL = 0;
                signals.regFileWE = 1;
                signals.regInputMuxCTL = 1;
                signals.ArithCTL = 0;
                signals.ArithMuxCTL = 2;
                signals.LOGICCTL = 15;
                signals.LogicMuxCTL = 15;
                signals.SHIFTCTL = 15;
                signals.CONSTCTL = 15;
                signals.CMPCTL = 15;
                signals.ALUMuxCTL = 0;
                signals.NZPWE = 1;
                signals.DATAWE = 0;
                return signals;
            }
        	
        	@Override
            public boolean isLdr() {
            	return true;
            }
        	
           	@Override
            public boolean isLoad() {
                return true;
            }
            
            @Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) throws IllegalMemAccessException {
                final int value = memory.checkAndRead(registerFile.getRegister(this.getSReg(word)) + this.getSignedImmed(word), registerFile.getPrivMode()).getValue();
                registerFile.setRegister(this.getDReg(word), value);
                registerFile.setNZP(value);
                return n + 1;
            }
        });
        ISA.createDef("STR", "0111 ddd sss iiiiii", new InstructionDef() {
           	
        	@Override
            public ControlSignals decodeSignals() {
           	 	ControlSignals signals = new ControlSignals();
           	 	signals.PCMuxCTL = 1;
           	 	signals.rsMuxCTL = 0;
           		signals.rtMuxCTL = 1;
                signals.rdMuxCTL = 15;
                signals.regFileWE = 0;
                signals.regInputMuxCTL = 15;
                signals.ArithCTL = 0;
                signals.ArithMuxCTL = 2;
                signals.LOGICCTL = 15;
                signals.LogicMuxCTL = 15;
                signals.SHIFTCTL = 15;
                signals.CONSTCTL = 15;
                signals.CMPCTL = 15;
                signals.ALUMuxCTL = 0;
                signals.NZPWE = 0;
                signals.DATAWE = 1;
                return signals;
            }
        	
           	@Override
            public boolean isStore() {
                return true;
            }
            
            @Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) throws IllegalMemAccessException {
                memory.checkAndWrite(registerFile.getRegister(this.getSReg(word)) + this.getSignedImmed(word), registerFile.getRegister(this.getDReg(word)), registerFile.getPrivMode());
                return n + 1;
            }
        });
        ISA.createDef("RTI", "1000 000000000000", new InstructionDef() {
           	
        	@Override
            public ControlSignals decodeSignals() {
           	 	ControlSignals signals = new ControlSignals();
           	 	signals.PCMuxCTL = 3;
           	 	signals.rsMuxCTL = 1;
           		signals.rtMuxCTL = 15;
                signals.rdMuxCTL = 15;
                signals.regFileWE = 0;
                signals.regInputMuxCTL = 15;
                signals.ArithCTL = 15;
                signals.ArithMuxCTL = 15;
                signals.LOGICCTL = 15;
                signals.LogicMuxCTL = 15;
                signals.SHIFTCTL = 15;
                signals.CONSTCTL = 15;
                signals.CMPCTL = 15;
                signals.ALUMuxCTL = 15;
                signals.NZPWE = 0;
                signals.DATAWE = 0;
                return signals;
            }
        	
        	@Override
        	public boolean isRet()
        	{
        		return true;
        	}
        	
           	@Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) throws IllegalInstructionException {
                if (registerFile.getPrivMode()) {
                    registerFile.setPrivMode(false);
                    return registerFile.getRegister(7);
                }
                throw new IllegalInstructionException("RTI can only be executed in privileged mode");
            }
        });
        ISA.createDef("CONST", "1001 ddd iiiiiiiii", new InstructionDef() {
           	
        	@Override
            public ControlSignals decodeSignals() {
           	 	ControlSignals signals = new ControlSignals();
           	 	signals.PCMuxCTL = 1;
           	 	signals.rsMuxCTL = 15;
           		signals.rtMuxCTL = 15;
                signals.rdMuxCTL = 0;
                signals.regFileWE = 1;
                signals.regInputMuxCTL = 0;
                signals.ArithCTL = 15;
                signals.ArithMuxCTL = 15;
                signals.LOGICCTL = 15;
                signals.LogicMuxCTL = 15;
                signals.SHIFTCTL = 15;
                signals.CONSTCTL = 0;
                signals.CMPCTL = 15;
                signals.ALUMuxCTL = 15;
                signals.NZPWE = 1;
                signals.DATAWE = 0;
                return signals;
            }
        	
        	@Override
            public boolean isConst() {
            	return true;
            }
        	
           	@Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                final int signedImmed = this.getSignedImmed(word);
                registerFile.setRegister(this.getDReg(word), signedImmed);
                registerFile.setNZP(signedImmed);
                return n + 1;
            }
        });
        ISA.createDef("CONST", "1001 ddd lllllllll", new InstructionDef() {
           	
        	@Override
            public ControlSignals decodeSignals() {
           	 	ControlSignals signals = new ControlSignals();
           	 	signals.PCMuxCTL = 1;
           	 	signals.rsMuxCTL = 15;
           		signals.rtMuxCTL = 15;
                signals.rdMuxCTL = 0;
                signals.regFileWE = 1;
                signals.regInputMuxCTL = 0;
                signals.ArithCTL = 15;
                signals.ArithMuxCTL = 15;
                signals.LOGICCTL = 15;
                signals.LogicMuxCTL = 15;
                signals.SHIFTCTL = 15;
                signals.CONSTCTL = 0;
                signals.CMPCTL = 15;
                signals.ALUMuxCTL = 15;
                signals.NZPWE = 1;
                signals.DATAWE = 0;
                return signals;
            }
        	
        	@Override
            public boolean isConstIMM() {
            	return true;
            }
        	
           	@Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                final int signedImmed = this.getSignedImmed(word);
                registerFile.setRegister(this.getDReg(word), signedImmed);
                registerFile.setNZP(signedImmed);
                return n + 1;
            }
        });
        ISA.createDef("SLL", "1010 ddd sss 00 uuuu", new InstructionDef() {
           	
        	@Override
            public ControlSignals decodeSignals() {
           	 	ControlSignals signals = new ControlSignals();
           	 	signals.PCMuxCTL = 1;
           	 	signals.rsMuxCTL = 0;
           		signals.rtMuxCTL = 15;
                signals.rdMuxCTL = 0;
                signals.regFileWE = 1;
                signals.regInputMuxCTL = 0;
                signals.ArithCTL = 15;
                signals.ArithMuxCTL = 15;
                signals.LOGICCTL = 15;
                signals.LogicMuxCTL = 15;
                signals.SHIFTCTL = 0;
                signals.CONSTCTL = 15;
                signals.CMPCTL = 15;
                signals.ALUMuxCTL = 15;
                signals.NZPWE = 1;
                signals.DATAWE = 0;
                return signals;
            }
        	
        	@Override
            public boolean isSLL() {
            	return true;
            }
        	
           	@Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                final int nzp = registerFile.getRegister(this.getSReg(word)) << this.getUnsignedImmed(word);
                registerFile.setRegister(this.getDReg(word), nzp);
                registerFile.setNZP(nzp);
                return n + 1;
            }
        });
        ISA.createDef("SRA", "1010 ddd sss 01 uuuu", new InstructionDef() {
           	
        	@Override
            public ControlSignals decodeSignals() {
           	 	ControlSignals signals = new ControlSignals();
           	 	signals.PCMuxCTL = 1;
           	 	signals.rsMuxCTL = 0;
           		signals.rtMuxCTL = 15;
                signals.rdMuxCTL = 0;
                signals.regFileWE = 1;
                signals.regInputMuxCTL = 0;
                signals.ArithCTL = 15;
                signals.ArithMuxCTL = 15;
                signals.LOGICCTL = 15;
                signals.LogicMuxCTL = 15;
                signals.SHIFTCTL = 1;
                signals.CONSTCTL = 15;
                signals.CMPCTL = 15;
                signals.ALUMuxCTL = 15;
                signals.NZPWE = 1;
                signals.DATAWE = 0;
                return signals;
            }
        	
        	@Override
            public boolean isSRA() {
            	return true;
            }
        	
           	@Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                final int nzp = (short)registerFile.getRegister(this.getSReg(word)) >> this.getUnsignedImmed(word);
                registerFile.setRegister(this.getDReg(word), nzp);
                registerFile.setNZP(nzp);
                return n + 1;
            }
        });
        ISA.createDef("SRL", "1010 ddd sss 10 uuuu", new InstructionDef() {
           	
        	@Override
            public ControlSignals decodeSignals() {
           	 	ControlSignals signals = new ControlSignals();
           	 	signals.PCMuxCTL = 1;
           	 	signals.rsMuxCTL = 0;
           		signals.rtMuxCTL = 15;
                signals.rdMuxCTL = 0;
                signals.regFileWE = 1;
                signals.regInputMuxCTL = 0;
                signals.ArithCTL = 15;
                signals.ArithMuxCTL = 15;
                signals.LOGICCTL = 15;
                signals.LogicMuxCTL = 15;
                signals.SHIFTCTL = 2;
                signals.CONSTCTL = 15;
                signals.CMPCTL = 15;
                signals.ALUMuxCTL = 15;
                signals.NZPWE = 1;
                signals.DATAWE = 0;
                return signals;
            }
        	
        	@Override
            public boolean isSRL() {
            	return true;
            }
        	
           	@Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                final int nzp = registerFile.getRegister(this.getSReg(word)) >>> this.getUnsignedImmed(word);
                registerFile.setRegister(this.getDReg(word), nzp);
                registerFile.setNZP(nzp);
                return n + 1;
            }
        });
        ISA.createDef("MOD", "1010 ddd sss 111 ttt", new InstructionDef() {
           	
        	@Override
            public ControlSignals decodeSignals() {
           	 	ControlSignals signals = new ControlSignals();
           	 	signals.PCMuxCTL = 1;
           	 	signals.rsMuxCTL = 0;
           		signals.rtMuxCTL = 0;
                signals.rdMuxCTL = 0;
                signals.regFileWE = 1;
                signals.regInputMuxCTL = 0;
                signals.ArithCTL = 4;
                signals.ArithMuxCTL = 0;
                signals.LOGICCTL = 15;
                signals.LogicMuxCTL = 15;
                signals.SHIFTCTL = 15;
                signals.CONSTCTL = 15;
                signals.CMPCTL = 15;
                signals.ALUMuxCTL = 0;
                signals.NZPWE = 1;
                signals.DATAWE = 0;
                return signals;
            }
        	
        	@Override
            public boolean isMod() {
            	return true;
            }
        	
           	@Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                final int register = registerFile.getRegister(this.getTReg(word));
                int nzp = 0;
                if (register != 0) {
                    nzp = registerFile.getRegister(this.getSReg(word)) % registerFile.getRegister(this.getTReg(word));
                }
                registerFile.setRegister(this.getDReg(word), nzp);
                registerFile.setNZP(nzp);
                return n + 1;
            }
        });
        ISA.createDef("JMPR", "1100 000 ddd 000000", new InstructionDef() {
           	
        	@Override
            public ControlSignals decodeSignals() {
           	 	ControlSignals signals = new ControlSignals();
           	 	signals.PCMuxCTL = 3;
           	 	signals.rsMuxCTL = 0;
           		signals.rtMuxCTL = 15;
                signals.rdMuxCTL = 15;
                signals.regFileWE = 0;
                signals.regInputMuxCTL = 15;
                signals.ArithCTL = 15;
                signals.ArithMuxCTL = 15;
                signals.LOGICCTL = 15;
                signals.LogicMuxCTL = 15;
                signals.SHIFTCTL = 15;
                signals.CONSTCTL = 15;
                signals.CMPCTL = 15;
                signals.ALUMuxCTL = 15;
                signals.NZPWE = 0;
                signals.DATAWE = 0;
                return signals;
            }
        	
        	@Override
        	public boolean isJumpR() {
        		return true;
        	}
        	
        	@Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                return registerFile.getRegister(this.getDReg(word));
            }
        });
        ISA.createDef("RET", "1100 000 111 000000", new InstructionDef() {
           	
        	@Override
            public ControlSignals decodeSignals() {
           	 	ControlSignals signals = new ControlSignals();
           	 	signals.PCMuxCTL = 3;
           	 	signals.rsMuxCTL = 1;
           		signals.rtMuxCTL = 15;
                signals.rdMuxCTL = 15;
                signals.regFileWE = 0;
                signals.regInputMuxCTL = 15;
                signals.ArithCTL = 15;
                signals.ArithMuxCTL = 15;
                signals.LOGICCTL = 15;
                signals.LogicMuxCTL = 15;
                signals.SHIFTCTL = 15;
                signals.CONSTCTL = 15;
                signals.CMPCTL = 15;
                signals.ALUMuxCTL = 15;
                signals.NZPWE = 0;
                signals.DATAWE = 0;
                return signals;
            }
        	
        	@Override
        	public boolean isRet() {
        		return true;
        	}
        	
        	@Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                return registerFile.getRegister(7);
            }
        });
        ISA.createDef("JMP", "1100 1 ppppppppppp", new InstructionDef() {
            
        	@Override
            public ControlSignals decodeSignals() {
           	 	ControlSignals signals = new ControlSignals();
           	 	signals.PCMuxCTL = 2;
           	 	signals.rsMuxCTL = 15;
           		signals.rtMuxCTL = 15;
                signals.rdMuxCTL = 15;
                signals.regFileWE = 0;
                signals.regInputMuxCTL = 15;
                signals.ArithCTL = 15;
                signals.ArithMuxCTL = 15;
                signals.LOGICCTL = 15;
                signals.LogicMuxCTL = 15;
                signals.SHIFTCTL = 15;
                signals.CONSTCTL = 15;
                signals.CMPCTL = 15;
                signals.ALUMuxCTL = 15;
                signals.NZPWE = 0;
                signals.DATAWE = 0;
                return signals;
            }
        	
        	@Override
        	public boolean isJump() {
        		return true;
        	}
        	
        	@Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                return n + 1 + this.getPCOffset(word);
            }
        });
        ISA.createDef("HICONST", "1101 ddd 1 uuuuuuuu", new InstructionDef() {
            
        	@Override
            public ControlSignals decodeSignals() {
           	 	ControlSignals signals = new ControlSignals();
           	 	signals.PCMuxCTL = 1;
           	 	signals.rsMuxCTL = 15;
           		signals.rtMuxCTL = 15;
                signals.rdMuxCTL = 0;
                signals.regFileWE = 1;
                signals.regInputMuxCTL = 0;
                signals.ArithCTL = 15;
                signals.ArithMuxCTL = 15;
                signals.LOGICCTL = 15;
                signals.LogicMuxCTL = 15;
                signals.SHIFTCTL = 15;
                signals.CONSTCTL = 1;
                signals.CMPCTL = 15;
                signals.ALUMuxCTL = 15;
                signals.NZPWE = 1;
                signals.DATAWE = 0;
                return signals;
            }
        	
        	@Override
            public boolean isHiConst() {
            	return true;
            }
        	
        	@Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                final int nzp = (registerFile.getRegister(this.getDReg(word)) & 0xFF) | this.getUnsignedImmed(word) << 8;
                registerFile.setRegister(this.getDReg(word), nzp);
                registerFile.setNZP(nzp);
                return n + 1;
            }
        });
        ISA.createDef("HICONST", "1101 ddd 1 hhhhhhhh", new InstructionDef() {
            
        	@Override
            public ControlSignals decodeSignals() {
           	 	ControlSignals signals = new ControlSignals();
           	 	signals.PCMuxCTL = 1;
           	 	signals.rsMuxCTL = 15;
           		signals.rtMuxCTL = 15;
                signals.rdMuxCTL = 0;
                signals.regFileWE = 1;
                signals.regInputMuxCTL = 0;
                signals.ArithCTL = 15;
                signals.ArithMuxCTL = 15;
                signals.LOGICCTL = 15;
                signals.LogicMuxCTL = 15;
                signals.SHIFTCTL = 15;
                signals.CONSTCTL = 1;
                signals.CMPCTL = 15;
                signals.ALUMuxCTL = 15;
                signals.NZPWE = 1;
                signals.DATAWE = 0;
                return signals;
            }
        	
        	@Override
            public boolean isHiConst() {
            	return true;
            }
        	
        	@Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                final int nzp = (registerFile.getRegister(this.getDReg(word)) & 0xFF) | this.getUnsignedImmed(word) << 8;
                registerFile.setRegister(this.getDReg(word), nzp);
                registerFile.setNZP(nzp);
                return n + 1;
            }
        });
        ISA.createDef("TRAP", "1111 0000 uuuuuuuu", new InstructionDef() {
        	
        	@Override
            public ControlSignals decodeSignals() {
           	 	ControlSignals signals = new ControlSignals();
           	 	signals.PCMuxCTL = 4;
           	 	signals.rsMuxCTL = 15;
           		signals.rtMuxCTL = 15;
                signals.rdMuxCTL = 1;
                signals.regFileWE = 1;
                signals.regInputMuxCTL = 2;
                signals.ArithCTL = 15;
                signals.ArithMuxCTL = 15;
                signals.LOGICCTL = 15;
                signals.LogicMuxCTL = 15;
                signals.SHIFTCTL = 15;
                signals.CONSTCTL = 15;
                signals.CMPCTL = 15;
                signals.ALUMuxCTL = 15;
                signals.NZPWE = 1;
                signals.DATAWE = 0;
                return signals;
            }
        	
        	@Override
        	public boolean isTRAP() {
        		return true;
        	}
        	
            @Override
            public boolean isCall() {
                return true;
            }
            
            @Override
            public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) {
                registerFile.setPrivMode(true);
                registerFile.setRegister(7, n + 1);
                registerFile.setNZP(n + 1);
                return 32768 + word.getZext(8, 0);
            }
        });
        ISA.createDef("LEA", "xxxx ddd ppppppppp", new PseudoDef());
        ISA.createDef("LC", "xxxx ddd ppppppppp", new PseudoDef());
    }
    
    private class BranchDef extends InstructionDef
    {
    	@Override
        public ControlSignals decodeSignals() {
       	 	ControlSignals signals = new ControlSignals();
       	 	signals.PCMuxCTL = 0;
       	 	signals.rsMuxCTL = 15;
       		signals.rtMuxCTL = 15;
            signals.rdMuxCTL = 15;
            signals.regFileWE = 0;
            signals.regInputMuxCTL = 15;
            signals.ArithCTL = 15;
            signals.ArithMuxCTL = 15;
            signals.LOGICCTL = 15;
            signals.LogicMuxCTL = 15;
            signals.SHIFTCTL = 15;
            signals.CONSTCTL = 15;
            signals.CMPCTL = 15;
            signals.ALUMuxCTL = 15;
            signals.NZPWE = 0;
            signals.DATAWE = 0;
            return signals;
        }
    	
        @Override
        public boolean isBranch() {
            return true;
        }
        
        @Override
        public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) throws IllegalMemAccessException, IllegalInstructionException {
            if ((word.getZext(11, 9) & registerFile.getNZP()) != 0x0) {
                return n + 1 + this.getPCOffset(word);
            }
            return n + 1;
        }
    }
}
