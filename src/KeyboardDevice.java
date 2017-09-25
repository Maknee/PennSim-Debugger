import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.InputStreamReader;
import java.io.BufferedReader;

// 
// Decompiled by Procyon v0.5.30
// 

public class KeyboardDevice
{
    private static final Word KB_AVAILABLE;
    private static final Word KB_UNAVAILABLE;
    private BufferedReader kbin;
    private BufferedReader defkbin;
    private static int CBUFSIZE;
    private static char TIMER_TICK;
    public static int SCRIPT_MODE;
    public static int INTERACTIVE_MODE;
    private int current;
    private int mode;
    private int defmode;
    
    public KeyboardDevice() {
        this.kbin = null;
        this.defkbin = null;
        this.current = 0;
        this.kbin = new BufferedReader(new InputStreamReader(System.in));
        this.mode = KeyboardDevice.INTERACTIVE_MODE;
        this.defkbin = this.kbin;
        this.defmode = this.mode;
    }
    
    public void setDefaultInputStream() {
        this.defkbin = this.kbin;
    }
    
    public void setDefaultInputMode() {
        this.defmode = this.mode;
    }
    
    public void setInputStream(final InputStream inputStream) {
        this.kbin = new BufferedReader(new InputStreamReader(inputStream));
    }
    
    public void setInputMode(final int mode) {
        this.mode = mode;
    }
    
    public void reset() {
        this.kbin = this.defkbin;
        this.mode = this.defmode;
        this.current = 0;
    }
    
    public Word status() {
        if (this.available()) {
            return KeyboardDevice.KB_AVAILABLE;
        }
        return KeyboardDevice.KB_UNAVAILABLE;
    }
    
    public boolean available() {
        try {
            if (this.kbin.ready()) {
                this.kbin.mark(1);
                if (this.kbin.read() == KeyboardDevice.TIMER_TICK) {
                    this.kbin.reset();
                    return false;
                }
                this.kbin.reset();
                return true;
            }
        }
        catch (IOException ex) {
            ErrorLog.logError(ex);
        }
        return false;
    }
    
    public Word read() {
        final char[] array = new char[KeyboardDevice.CBUFSIZE];
        try {
            if (this.available()) {
                if (this.mode == KeyboardDevice.INTERACTIVE_MODE) {
                    this.current = array[this.kbin.read(array, 0, KeyboardDevice.CBUFSIZE) - 1];
                }
                else {
                    this.current = this.kbin.read();
                }
            }
        }
        catch (IOException ex) {
            ErrorLog.logError(ex);
        }
        return new Word(this.current);
    }
    
    public boolean hasTimerTick() {
        try {
            this.kbin.mark(1);
            if (this.kbin.ready()) {
                if (this.kbin.read() == KeyboardDevice.TIMER_TICK) {
                    return true;
                }
                this.kbin.reset();
                return false;
            }
        }
        catch (IOException ex) {
            ErrorLog.logError(ex);
        }
        return false;
    }
    
    static {
        KB_AVAILABLE = new Word(32768);
        KB_UNAVAILABLE = new Word(0);
        KeyboardDevice.CBUFSIZE = 128;
        KeyboardDevice.TIMER_TICK = '.';
        KeyboardDevice.SCRIPT_MODE = 0;
        KeyboardDevice.INTERACTIVE_MODE = 1;
    }
}
