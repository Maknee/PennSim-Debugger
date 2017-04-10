// 
// Decompiled by Procyon v0.5.30
// 

public class TimerDevice
{
    private static final Word TIMER_SET;
    private static final Word TIMER_UNSET;
    private static int MANUAL_TIMER;
    private static int AUTOMATIC_TIMER;
    private static long TIMER_INTERVAL;
    private int mode;
    private boolean enabled;
    private long lastTime;
    private long interval;
    private KeyboardDevice kb;
    
    public TimerDevice() {
        this.enabled = false;
        this.kb = null;
        this.mode = TimerDevice.AUTOMATIC_TIMER;
        this.enabled = true;
    }
    
    public boolean isEnabled() {
        return this.enabled;
    }
    
    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }
    
    public long getInterval() {
        return this.interval;
    }
    
    public void setTimer() {
        this.mode = TimerDevice.AUTOMATIC_TIMER;
        this.interval = TimerDevice.TIMER_INTERVAL;
        this.lastTime = System.currentTimeMillis();
    }
    
    public void setTimer(final long interval) {
        this.mode = TimerDevice.AUTOMATIC_TIMER;
        this.interval = interval;
        this.lastTime = System.currentTimeMillis();
    }
    
    public void setTimer(final KeyboardDevice kb) {
        this.mode = TimerDevice.MANUAL_TIMER;
        this.interval = 1L;
        this.kb = kb;
    }
    
    public void reset() {
        this.mode = TimerDevice.AUTOMATIC_TIMER;
        this.setTimer(TimerDevice.TIMER_INTERVAL);
    }
    
    public Word status() {
        if (this.hasGoneOff()) {
            return TimerDevice.TIMER_SET;
        }
        return TimerDevice.TIMER_UNSET;
    }
    
    public boolean hasGoneOff() {
        if (!this.enabled) {
            return false;
        }
        if (this.mode != TimerDevice.AUTOMATIC_TIMER) {
            return this.kb.hasTimerTick();
        }
        final long currentTimeMillis = System.currentTimeMillis();
        if (currentTimeMillis - this.lastTime > this.interval) {
            this.lastTime = currentTimeMillis;
            return true;
        }
        return false;
    }
    
    static {
        TIMER_SET = new Word(32768);
        TIMER_UNSET = new Word(0);
        TimerDevice.MANUAL_TIMER = 0;
        TimerDevice.AUTOMATIC_TIMER = 1;
        TimerDevice.TIMER_INTERVAL = 500L;
    }
}
