package net.eli.elimod.quantum;

public class Gate {
    Complex[][] mat;
    static final double[][] X_ARR = {{0,1},
                                     {1,0}};
    static final Complex[][] Y_ARR = {{Complex.ZERO, Complex.I.neg()},
                                      {Complex.I,    Complex.ZERO   }};
    static final double[][] Z_ARR = {{1, 0},
                                     {0,-1}};
    static final double[][] H_ARR = {{1 / Math.sqrt(2),  1 / Math.sqrt(2)},
                                     {1 / Math.sqrt(2), -1 / Math.sqrt(2)}};
    static final Complex[][] S_ARR = {{Complex.ONE, Complex.ZERO},
                                      {Complex.ZERO, Complex.I}};
    static final Complex[][] T_ARR = {{Complex.ONE, Complex.ZERO},
                                      {Complex.ZERO, Complex.fromPhase(Math.PI/4)}};
    static final double[][] CNOT_ARR = {{1,0,0,0},
                                        {0,1,0,0},
                                        {0,0,0,1},
                                        {0,0,1,0}};
    static final double[][] TOFFOLI_ARR = {
        {1,0,0,0,0,0,0,0},
        {0,1,0,0,0,0,0,0},
        {0,0,1,0,0,0,0,0},
        {0,0,0,1,0,0,0,0},
        {0,0,0,0,1,0,0,0},
        {0,0,0,0,0,1,0,0},
        {0,0,0,0,0,0,0,1},
        {0,0,0,0,0,0,1,0} 
    };
    public static final Gate X = new Gate(X_ARR);
    public static final Gate Y = new Gate(Y_ARR);
    public static final Gate Z = new Gate(Z_ARR);
    public static final Gate H = new Gate(H_ARR);
    public static final Gate S = new Gate(S_ARR);
    public static final Gate T = new Gate(T_ARR);
    public static final Gate CNOT = new Gate(CNOT_ARR);
    public static final Gate TOFFOLI = new Gate(TOFFOLI_ARR);

    public Gate(double[][] A) {
        // for (int i = 0; i < A.length; i++) {
        //     if(A.length != A[i].length){
        //         throw new Exception("matrix x and y dimensions do not match");
        //     }
        // }
        Complex[][] reformed = new Complex[A.length][A.length];
        for (int i = 0; i < reformed.length; i++) {
            for (int j = 0; j < reformed.length; j++) {
                reformed[i][j] = Complex.fromRe(A[i][j]);
            }
        }
        this.mat = reformed;
    }
    public Gate(Complex[][] mat) {
        // for (int i = 0; i < mat.length; i++) {
        //     if(mat.length != mat[i].length){
        //         throw new Exception("matrix x and y dimensions do not match");
        //     }
        // }
        this.mat = mat;
    }
    public int size(){
        return mat.length;
    }

    public Qbit actOn(Qbit x){
        // if(size() != x.size()){
        //     throw new Exception("matrix and vector dimensions do not match");
        // }
        var v = x.getVec();
        Complex[] out = new Complex[v.length];
        for (int i = 0; i < out.length; i++) {
            out[i] = new Complex(0,0);
        }
        for (int i = 0; i < out.length; i++) { for (int j = 0; j < out.length; j++) {
                out[i] = out[i].add(Complex.mul(mat[i][j], v[j]));
        }}
        return new Qbit(out);
    }
    public State actOn(State x){
        // if(size() != x.size()){
        //     throw new Exception("matrix and vector dimensions do not match");
        // }
        var v = x.getVec();
        Complex[] out = new Complex[v.length];
        for (int i = 0; i < out.length; i++) {
            out[i] = new Complex(0,0);
        }
        for (int i = 0; i < out.length; i++) { for (int j = 0; j < out.length; j++) {
                out[i] = out[i].add(Complex.mul(mat[i][j], v[j]));
        }}
        return new State(out);
    }

    // public State actOn(State x){
    //     // if(size() != x.size()){
    //     //     throw new Exception("matrix and vector dimensions do not match");
    //     // }
    //     var v = x.getVec();
    //     Complex[] out = new Complex[v.length];
    //     for (int i = 0; i < out.length; i++) {
    //         out[i] = Complex.ZERO;
    //     }
    //     for (int i = 0; i < out.length; i++) {
    //         for (int j = 0; j < out.length; j++) {
    //             out[i].add(Complex.mul(mat[i][j], out[j]));
    //         }
            
    //     }
    //     return new State(out);
    // }


    
    @Override public String toString() {
        String out = "";

        for (int i = 0; i < mat.length; i++) { 
            out += "| " + mat[i][0].toString();
            for (int j = 1; j < mat.length; j++) {
                out += " " + mat[i][j].toString();
            }
            out += " |\n";
        }
        return out;
    }
}
