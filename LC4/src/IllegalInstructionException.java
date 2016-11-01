// 
// Decompiled by Procyon v0.5.30
// 

public class IllegalInstructionException extends ExceptionException
{
    public IllegalInstructionException(final String s) {
        super(s);
    }
    
    @Override
    public String getExceptionDescription() {
        return "IllegalInstructionException: " + this.getMessage();
    }
}
