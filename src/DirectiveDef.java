// 
// Decompiled by Procyon v0.5.30
// 

public class DirectiveDef extends InstructionDef
{
    @Override
    public boolean isDirective() {
        return true;
    }
    
    @Override
    public String disassemble(final Word word, final int n, final Machine machine) {
        return "";
    }
    
    @Override
    public int execute(final Word word, final int n, final RegisterFile registerFile, final Memory memory, final Machine machine) throws IllegalMemAccessException, IllegalInstructionException {
        throw new IllegalInstructionException("Cannot execute directives");
    }
}
