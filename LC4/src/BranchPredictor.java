// 
// Decompiled by Procyon v0.5.30
// 

public class BranchPredictor
{
    private int[][] predictor;
    private int size;
    private Machine mac;
    private static final int TAG = 0;
    private static final int PREDICTION = 1;
    
    public BranchPredictor() {
        this.size = 0;
    }
    
    public BranchPredictor(final Machine mac, final int size) {
        this.size = 0;
        this.mac = mac;
        this.size = size;
        this.predictor = new int[size][2];
    }
    
    public int getPredictedPC(final int n) {
        int n2 = n + 1;
        if (this.size > 0) {
            final int n3 = n % this.size;
            if (this.predictor[n3][0] == n) {
                n2 = this.predictor[n3][1];
            }
        }
        return n2;
    }
    
    public void update(final int n, final int n2) {
        if (this.size > 0) {
            this.predictor[n % this.size][0] = n;
            this.predictor[n % this.size][1] = n2;
        }
    }
    
    @Override
    public String toString() {
        String string = new String("");
        for (int i = 0; i < this.predictor.length; ++i) {
            string = string + String.valueOf(i) + ":" + " tag: " + this.predictor[i][0] + " pred: " + this.predictor[i][1];
        }
        return string;
    }
    
    public void reset() {
        for (int i = 0; i < this.predictor.length; ++i) {
            this.predictor[i][0] = 0;
            this.predictor[i][1] = 0;
        }
    }
}
