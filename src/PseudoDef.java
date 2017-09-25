// 
// Decompiled by Procyon v0.5.30
// 

public class PseudoDef extends InstructionDef
{
    @Override
    public boolean isPseudo() {
        return true;
    }
    
    @Override
    public String disassemble(final Word word, final int n, final Machine machine) {
        return "";
    }
    
    @Override
    public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) throws IllegalMemAccessException, IllegalInstructionException {
        throw new IllegalInstructionException("Cannot execute pseudo instructions");
    }
}
