import java.io.IOException;
import java.io.BufferedOutputStream;

// 
// Decompiled by Procyon v0.5.30
// 

public class Word
{
    private int value;
    
    public Word(final int value) {
        this.setValue(value);
    }
    
    public Word() {
        this.value = 0;
    }
    
    public void reset() {
        this.value = 0;
    }
    
    public String toHex() {
        return toHex(this.value, true);
    }
    
    public String toHex(final boolean b) {
        return toHex(this.value, b);
    }
    
    public String toBinary() {
        return toBinary(this.value, true);
    }
    
    public String toBinary(final boolean b) {
        return toBinary(this.value, b);
    }
    
    public static String toHex(final int n, final boolean b) {
        String s = Integer.toHexString(n & 0xFFFF).toUpperCase();
        if (s.length() > 4) {
            Console.println("Converting oversized value " + s + " to hex.");
        }
        while (s.length() < 4) {
            s = "0" + s;
        }
        return b ? ("x" + s) : s;
    }
    
    public static String toHex(final int n) {
        return toHex(n, true);
    }
    
    public static String toBinary(final int n, final boolean b) {
        String s = Integer.toBinaryString(n & 0xFFFF).toUpperCase();
        if (s.length() > 16) {
            Console.println("Converting oversized value " + s + " to binary.");
        }
        while (s.length() < 16) {
            s = "0" + s;
        }
        return b ? ("b" + s) : s;
    }
    
    public static String toBinary(final int n) {
        return toBinary(n, true);
    }
    
    @Override
    public String toString() {
        return Integer.toString(this.value);
    }
    
    public void setValue(final int n) {
        this.value = (n & 0xFFFF);
    }
    
    public int getValue() {
        return this.value;
    }
    
    void writeWordToFile(final BufferedOutputStream bufferedOutputStream) throws IOException {
        final byte b = (byte)(this.value >> 8 & 0xFF);
        final byte b2 = (byte)(this.value & 0xFF);
        bufferedOutputStream.write(b);
        bufferedOutputStream.write(b2);
    }
    
    public static int parseNum(final String s) {
        int n;
        try {
            if (s.indexOf(120) == 0) {
                n = Integer.parseInt(s.replace('x', '0'), 16);
            }
            else {
                n = Integer.parseInt(s);
            }
        }
        catch (NumberFormatException ex) {
            n = Integer.MAX_VALUE;
        }
        catch (NullPointerException ex2) {
            n = Integer.MAX_VALUE;
        }
        return n;
    }
    
    public int getZext(final int n, final int n2) {
        final int value = this.value;
        if (n2 > n) {
            return this.getZext(n2, n);
        }
        if (n > 15 || n < 0 || n2 > 15 || n2 < 0) {
            throw new InternalException("Bits out of range: " + n + " " + n2);
        }
        return (~(-1 << n + 1) & value) >> n2;
    }
    
    public int getSext(final int n, final int n2) {
        final int value = this.value;
        if (n2 > n) {
            return this.getSext(n2, n);
        }
        if (n > 15 || n < 0 || n2 > 15 || n2 < 0) {
            throw new InternalException("Bits out of range: " + n + " " + n2);
        }
        int n3;
        if ((value & 1 << n) != 0x0) {
            n3 = (-1 << n | value);
        }
        else {
            n3 = (~(-1 << n + 1) & value);
        }
        return n3 >> n2;
    }
    
    public int getBit(final int n) {
        return this.getZext(n, n);
    }
    
    private void setField(final int n, final int n2, final int n3) throws AsException {
        if (n3 > n2) {
            throw new AsException("Hi and lo bit operands reversed.");
        }
        if (n2 > 15 || n2 < 0 || n3 > 15 || n3 < 0) {
            throw new AsException("Bits out of range: " + n2 + " " + n3);
        }
        final int n4 = ~(-1 << n2 - n3 + 1) << n3;
        this.value = ((n4 & n << n3) | (~n4 & this.value));
    }
    
    public void setSignedField(final int n, final int n2, final int n3) throws AsException {
        if (n3 > n2) {
            throw new AsException("Hi and lo bit operands reversed.");
        }
        if (n2 > 15 || n2 < 0 || n3 > 15 || n3 < 0) {
            throw new InternalException("Bits out of range: " + n2 + " " + n3);
        }
        final int n4 = n >> n2 - n3;
        if (n4 == 0 || n4 == -1) {
            this.setField(n, n2, n3);
            this.setField(n, n2, n3);
            return;
        }
        throw new AsException("Immediate out of range: " + n);
    }
    
    public void setUnsignedField(final int n, final int n2, final int n3) throws AsException {
        if (n3 > n2) {
            throw new AsException("Hi and lo bit operands reversed.");
        }
        if (n2 > 15 || n2 < 0 || n3 > 15 || n3 < 0) {
            throw new InternalException("Bits out of range: " + n2 + " " + n3);
        }
        if (n >> n2 - n3 + 1 == 0) {
            this.setField(n, n2, n3);
            return;
        }
        throw new AsException("Immediate out of range: " + n);
    }
    
    public static int convertByteArray(final byte b, final byte b2) {
        final boolean b3 = false;
        final int n = 255;
        return ((b3 ? 1 : 0) | (n & b)) << 8 | (n & b2);
    }
}
