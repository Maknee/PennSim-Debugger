import java.util.List;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// 
// Decompiled by Procyon v0.5.30
// 

class Instruction
{
    private String originalLine;
    private String format;
    private int address;
    private String opcode;
    private String label;
    private String label_ref;
    private Vector<Integer> regs;
    private String stringz;
    private Integer offset_immediate;
    private int abs_hi;
    private int abs_lo;
    private int abs_aligned;
    private int line_number;
    private String filename;
    private int src_line_number;
    private String src_file_name;
    
    public String getFormat() {
        return this.format;
    }
    
    public void setAddress(final int address) {
        this.address = address;
    }
    
    public int getAddress() {
        return this.address;
    }
    
    public String getOriginalLine() {
        return this.originalLine;
    }
    
    public int getLineNumber() {
        return this.line_number;
    }
    
    public String getFileName() {
        return this.filename;
    }
    
    public String getOpcode() {
        return this.opcode;
    }
    
    public String getLabel() {
        return this.label;
    }
    
    public String getLabelRef() {
        return this.label_ref;
    }
    
    public int getRegs(final int n) {
        return this.regs.get(n);
    }
    
    public String getStringz() {
        return this.stringz;
    }
    
    public int getOffsetImmediate() throws AsException {
        if (this.offset_immediate == null) {
            throw new AsException(this, "Internal error: no offset/immediate when expected");
        }
        return this.offset_immediate;
    }
    
    public void setSrcFileName(final String src_file_name) {
        this.src_file_name = src_file_name;
    }
    
    public void setSrcLineNumber(final int src_line_number) {
        this.src_line_number = src_line_number;
    }
    
    public String getSrcFileName() {
        return this.src_file_name;
    }
    
    public int getSrcLineNumber() {
        return this.src_line_number;
    }
    
    public void setOffsetImmediate(final int n) {
        this.offset_immediate = new Integer(n);
    }
    
    public int getAbsHi() {
        return this.abs_hi;
    }
    
    public int getAbsLo() {
        return this.abs_lo;
    }
    
    public int getAbsAligned() {
        return this.abs_aligned;
    }
    
    public void setAbsHi(final int n) {
        this.abs_hi = n >>> 8;
    }
    
    public void setAbsLo(final int n) {
        this.abs_lo = (n & 0xFF);
    }
    
    public void setAbsAligned(final int n) {
        this.abs_aligned = (n >>> 4 & 0x7FF);
    }
    
    public void error(final String s) throws AsException {
        throw new AsException(this, s);
    }
    
    Instruction(String originalLine, final String filename, final int line_number) throws AsException {
        this.format = new String();
        this.regs = new Vector<Integer>();
        this.line_number = 0;
        this.filename = null;
        this.src_line_number = -1;
        this.src_file_name = null;
        this.line_number = line_number;
        this.filename = filename;
        this.originalLine = originalLine;
        final int index = originalLine.indexOf(59);
        if (index != -1) {
            originalLine = originalLine.substring(0, index);
        }
        originalLine = originalLine.replace("\\\"", "\u0000");
        final Matcher matcher = Pattern.compile("([^\"]*)[\"]([^\"]*)[\"](.*)").matcher(originalLine);
        if (matcher.matches()) {
            this.stringz = matcher.group(2);
            this.stringz = this.stringz.replace("\u0000", "\"");
            this.stringz = this.stringz.replace("\\n", "\n");
            this.stringz = this.stringz.replace("\\t", "\t");
            this.stringz = this.stringz.replace("\\0", "\u0000");
            originalLine = matcher.group(1) + " " + matcher.group(3);
        }
        originalLine = originalLine.toUpperCase();
        originalLine = originalLine.replace(",", " ");
        originalLine = originalLine.trim();
        if (originalLine.length() == 0) {
            return;
        }
        final String[] split = originalLine.split("[\\s]+");
        for (int i = 0; i < split.length; ++i) {
            final String s = split[i];
            if (ISA.isOpcode(s)) {
                this.opcode = s;
                this.format = this.format + s + " ";
            }
            else if (s.matches("[#]?[-]?[\\d]+")) {
                this.offset_immediate = Integer.parseInt(s.replace("#", ""), 10);
                this.format += "Num ";
            }
            else if (s.matches("[B][01]+")) {
                this.offset_immediate = Integer.parseInt(s.replace("B", ""), 2);
                this.format += "Num ";
            }
            else if (s.matches("[0]?[X][ABCDEF\\d]+")) {
                this.offset_immediate = Integer.parseInt(s.replace("0X", "").replace("X", ""), 16);
                this.format += "Num ";
            }
            else if (s.matches("R[\\d]+")) {
                this.regs.add(new Integer(Integer.parseInt(s.replace("R", ""), 10)));
                this.format += "Reg ";
            }
            else if (i == 0 && s.matches("[\\w_][\\w_\\d]*[:]?")) {
                this.label = s.replace(":", "");
            }
            else {
                if (i == 0 || !s.matches("[\\w_][\\w_\\d]*")) {
                    throw new AsException(this, "Unrecognizable token: `" + s + "` on line  " + line_number + "(" + i + " " + this.originalLine + ")\n");
                }
                this.label_ref = s;
                this.format += "Label ";
            }
        }
        if (this.stringz != null) {
            this.format += "String";
        }
        this.format = this.format.trim();
        if (this.opcode == null) {
            if (this.format.length() != 0) {
                throw new AsException(this, "Unexpected instruction format");
            }
        }
        else {
            ISA.checkFormat(this, this.line_number);
        }
    }
    
    public void splitLabels(final List<Instruction> list) throws AsException {
        if (this.opcode != null || this.label != null) {
            if (this.opcode != null && this.label != null) {
                list.add(new Instruction(this.label, this.filename, this.line_number));
                this.label = null;
                list.add(this);
            }
            else {
                list.add(this);
            }
        }
    }
}
