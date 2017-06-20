/*
 * Assembly exception
 */
class AsException extends Exception
{
    public Instruction insn;
    
    AsException(final Instruction insn, final String s) {
        super(s);
        this.insn = insn;
    }
    
    AsException(final String s) {
        super(s);
    }
    
    @Override
    public String getMessage() {
        String string = "Assembly error: ";
        if (this.insn != null) {
            string = string + "[line " + this.insn.getLineNumber() + ", '" + this.insn.getOriginalLine() + "']: ";
        }
        return string + super.getMessage();
    }
}
