import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

// 
// Decompiled by Procyon v0.5.30
// 

class SimSrcTable
{
    private final int CODE_SIZE = 65536;
    private final int[] addrToLoc;
    private final int[] addrToFn;
    private final ArrayList<String> fnlst;
    
    SimSrcTable() {
        this.addrToLoc = new int[65536];
        this.addrToFn = new int[65536];
        this.fnlst = new ArrayList<String>();
        this.reset();
    }
    
    public void reset() {
        this.fnlst.clear();
        for (int i = 0; i < 65536; ++i) {
            this.addrToLoc[i] = (this.addrToFn[i] = -1);
        }
    }
    
    public int lookupLoc(final int n) {
        return this.addrToLoc[n];
    }
    
    public String lookupFn(final int n) {
        return (this.addrToFn[n] == -1) ? null : this.fnlst.get(this.addrToFn[n]);
    }
    
    public String loadSrcTable(final String s) {
        final byte[] array = new byte[2];
        String s2;
        try {
            final FileInputStream fileInputStream = new FileInputStream(new File(s));
            while (fileInputStream.read(array) == 2) {
                final int convertByteArray = Word.convertByteArray(array[0], array[1]);
                final String hex = Word.toHex(convertByteArray);
                if (convertByteArray == 61822) {
                    if (fileInputStream.read(array) < 2) {
                        throw new IOException("Missing filename size");
                    }
                    final int convertByteArray2 = Word.convertByteArray(array[0], array[1]);
                    final byte[] array2 = new byte[convertByteArray2];
                    if (fileInputStream.read(array2, 0, convertByteArray2) < convertByteArray2) {
                        throw new IOException("Malformed filename");
                    }
                    this.fnlst.add(new String(array2));
                    System.out.println(array2);
                }
                else if (convertByteArray == 29022) {
                    if (fileInputStream.read(array) < 2) {
                        throw new IOException("Missing address");
                    }
                    final int convertByteArray3 = Word.convertByteArray(array[0], array[1]);
                    if (fileInputStream.read(array) < 2) {
                        throw new IOException("Missing line number");
                    }
                    this.addrToLoc[convertByteArray3] = Word.convertByteArray(array[0], array[1]);
                    if (fileInputStream.read(array) < 2) {
                        throw new IOException("Missing filename");
                    }
                    this.addrToFn[convertByteArray3] = Word.convertByteArray(array[0], array[1]);
                    if (this.addrToFn[convertByteArray3] >= this.fnlst.size()) {
                        throw new IOException("Unknown filename");
                    }
                    continue;
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
                    continue;
                }
                else {
                    if (convertByteArray != 50103) {
                        throw new IOException("Unknown section " + hex);
                    }
                    if (fileInputStream.read(array) < 2) {
                        throw new IOException("Missing " + hex + " section address");
                    }
                    if (fileInputStream.read(array) < 2) {
                        throw new IOException("Missing " + hex + " section size");
                    }
                    final long n2 = Word.convertByteArray(array[0], array[1]);
                    if (fileInputStream.skip(n2) < n2) {
                        throw new IOException("Unexpectedly short " + hex + " section");
                    }
                    continue;
                }
            }
            fileInputStream.close();
            s2 = " file and line numbers ... ";
        }
        catch (IOException ex) {
            return "Error: Could not load file and line numbers (" + ex.toString() + ")";
        }
        return s2;
    }
}
