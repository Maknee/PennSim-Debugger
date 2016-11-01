// 
// Decompiled by Procyon v0.5.30
// 

public class IllegalMemAccessException extends ExceptionException
{
    private int addr;
    private boolean priv;
    
    public IllegalMemAccessException(final int addr, final boolean priv) {
        this.addr = addr;
        this.priv = priv;
    }
    
    @Override
    public String getExceptionDescription() {
        return "IllegalMemAccessException accessing address " + Word.toHex(this.addr) + " with privelege " + this.priv + "\n";
    }
}
