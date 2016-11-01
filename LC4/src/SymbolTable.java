import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;

// 
// Decompiled by Procyon v0.5.30
// 

class SymbolTable
{
    private final Hashtable<String, Integer> stoaTable;
    private final Hashtable<Integer, String> atosTable;
    
    SymbolTable() {
        this.stoaTable = new Hashtable<String, Integer>();
        this.atosTable = new Hashtable<Integer, String>();
    }
    
    void clear() {
        this.stoaTable.clear();
        this.atosTable.clear();
    }
    
    void insert(final int n, final String s) {
        this.stoaTable.put(s, n);
        this.atosTable.put(n, s);
    }
    
    void insert(final String s, final int n) {
        this.stoaTable.put(s, n);
        this.atosTable.put(n, s);
    }
    
    void removeAddr(final int n) {
        if (this.atosTable.contains(n)) {
            this.stoaTable.remove(this.atosTable.get(n));
            this.atosTable.remove(n);
        }
    }
    
    void remove(final String s) {
        if (this.stoaTable.contains(s)) {
            final Integer n = this.stoaTable.get(s);
            this.stoaTable.remove(s);
            this.atosTable.remove(n);
        }
    }
    
    public String lookupAddr(final int n) {
        return this.atosTable.get(new Integer(n));
    }
    
    public int lookupSym(final String s) {
        final Integer value = this.stoaTable.get(s);
        return (value != null) ? value : Integer.MAX_VALUE;
    }
    
    public boolean existSym(final String s) {
        return this.stoaTable.get(s) != null;
    }
    
    Enumeration<String> getSyms() {
        return this.stoaTable.keys();
    }
    
    public int numSyms() {
        return this.stoaTable.size();
    }
    
    public String loadSymbolTable(final String s) {
        String s2 = "";
        final byte[] array = new byte[2];
        try {
            final FileInputStream fileInputStream = new FileInputStream(new File(s));
            while (fileInputStream.read(array) == 2) {
                final int convertByteArray = Word.convertByteArray(array[0], array[1]);
                final String hex = Word.toHex(convertByteArray);
                if (convertByteArray == 50103) {
                    if (fileInputStream.read(array) < 2) {
                        throw new IOException("Missing symbol addr");
                    }
                    final int convertByteArray2 = Word.convertByteArray(array[0], array[1]);
                    if (fileInputStream.read(array) < 2) {
                        throw new IOException("Missing symbol size");
                    }
                    final int convertByteArray3 = Word.convertByteArray(array[0], array[1]);
                    final byte[] array2 = new byte[convertByteArray3];
                    if (fileInputStream.read(array2, 0, convertByteArray3) < convertByteArray3) {
                        throw new IOException("Malformed string");
                    }
                    this.insert(convertByteArray2, new String(array2));
                }
                else if (convertByteArray == 51934 || convertByteArray == 56026) {
                    if (fileInputStream.read(array) < 2) {
                        throw new IOException("Missing " + hex + " section addr");
                    }
                    if (fileInputStream.read(array) < 2) {
                        throw new IOException("Missing " + hex + " section size");
                    }
                    final long n = Word.convertByteArray(array[0], array[1]) * 2;
                    if (fileInputStream.skip(n) < n) {
                        throw new IOException("Unexpectedly short " + hex + " section");
                    }
                }
                else if (convertByteArray == 61822) {
                    if (fileInputStream.read(array) < 2) {
                        throw new IOException("Missing " + hex + " section size");
                    }
                    final long n2 = Word.convertByteArray(array[0], array[1]);
                    if (fileInputStream.skip(n2) < n2) {
                        throw new IOException("Unexpectedly short " + hex + " section");
                    }
                }
                else {
                    if (convertByteArray != 29022) {
                        throw new IOException("Unknown section " + hex);
                    }
                    if (fileInputStream.skip(6L) < 6L) {
                        throw new IOException("Unexpectedly short " + hex + " section");
                    }
                }
                s2 = " symbols ... ";
            }
        }
        catch (IOException ex) {
            return "Error: Could not load symbols (" + ex.toString() + ")";
        }
        return s2;
    }
}
