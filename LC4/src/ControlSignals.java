// 
// Decompiled by Procyon v0.5.30
// 

public class ControlSignals
{   
    public byte PCMuxCTL;
    public byte rsMuxCTL;
    public byte rtMuxCTL;
    public byte rdMuxCTL;
    public byte regFileWE;
    public byte regInputMuxCTL;
    public byte ArithCTL;
    public byte ArithMuxCTL;
    public byte LOGICCTL;
    public byte LogicMuxCTL;
    public byte SHIFTCTL;
    public byte CONSTCTL;
    public byte CMPCTL;
    public byte ALUMuxCTL;
    public byte NZPWE;
    public byte DATAWE;
    
    public ControlSignals() {
    	
    };
    
    public ControlSignals(
    byte PCMuxCTL,
    byte rsMuxCTL,
    byte rtMuxCTL,
    byte rdMuxCTL,
    byte regFileWE,
    byte regInputMuxCTL,
    byte ArithCTL,
    byte ArithMuxCTL,
    byte LOGICCTL,
    byte LogicMuxCTL,
    byte SHIFTCTL,
    byte CONSTCTL,
    byte CMPCTL,
    byte ALUMuxCTL,
    byte NZPWE,
    byte DATAWE) {
        this.PCMuxCTL = PCMuxCTL;
        this.rsMuxCTL = rsMuxCTL;
        this.rtMuxCTL = rtMuxCTL;
        this.rdMuxCTL = rdMuxCTL;
        this.regFileWE = regFileWE;
        this.regInputMuxCTL = regInputMuxCTL;
        this.ArithCTL = ArithCTL;
        this.ArithMuxCTL = ArithMuxCTL;
        this.LOGICCTL = LOGICCTL;
        this.LogicMuxCTL = LogicMuxCTL;
        this.SHIFTCTL = SHIFTCTL;
        this.CONSTCTL = CONSTCTL;
        this.CMPCTL = CMPCTL;
        this.ALUMuxCTL = ALUMuxCTL;
        this.NZPWE = NZPWE;
        this.DATAWE = DATAWE;
    }
}
