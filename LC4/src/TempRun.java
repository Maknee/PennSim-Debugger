// 
// Decompiled by Procyon v0.5.30
// 

class TempRun implements Runnable
{
    GUI ms;
    
    public TempRun(final GUI ms) {
        this.ms = ms;
    }
    
    @Override
    public void run() {
        this.ms.setUpGUI();
    }
}
