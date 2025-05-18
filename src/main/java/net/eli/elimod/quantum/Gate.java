package net.eli.elimod.quantum;

import net.eli.Utils;

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
    static final double[][] SWAP_ARR = {{1,0,0,0},
                                        {0,0,1,0},
                                        {0,1,0,0},
                                        {0,0,0,1}};
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
    public static final Gate I = Gate.identity(2);
    public static final Gate X = new Gate(X_ARR);
    public static final Gate Y = new Gate(Y_ARR);
    public static final Gate Z = new Gate(Z_ARR);
    public static final Gate H = new Gate(H_ARR);
    public static final Gate S = new Gate(S_ARR);
    public static final Gate T = new Gate(T_ARR);
    public static final Gate CNOT = new Gate(CNOT_ARR);
    public static final Gate SWAP = new Gate(SWAP_ARR);
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


    public static Gate controled_by0(Gate gate){
        Gate no_change = Gate.tensor(State.projection(Qbit.ZERO), Gate.I);
        Gate change = Gate.tensor(State.projection(Qbit.ONE), gate);
        return no_change.add(change);
    }
    public static Gate controled_by1(Gate gate){
        Gate no_change = Gate.tensor(Gate.I, State.projection(Qbit.ZERO));
        Gate change = Gate.tensor(gate, State.projection(Qbit.ONE));
        return no_change.add(change);
    }
    public Gate controled_by0() {return controled_by0(this); }
    public Gate controled_by1() {return controled_by1(this); }

    public Gate controled(int n_qbits, int control_qbit, int acting_qbit){
        // Gate no_change, change;
        // if(control_qbit < acting_qbit){
        //     Gate[] no_change
        // }
        // else{

        // }
        Gate[] no_change_factors = new Gate[n_qbits];
        Gate[] change_factors = new Gate[n_qbits];
        for (int i = 0; i < n_qbits; i++) {
            if(i==control_qbit){
                no_change_factors[i] = State.projection(Qbit.ZERO);
                change_factors[i]    = State.projection(Qbit.ONE);
            }
            else if(i==control_qbit){
                no_change_factors[i] = Gate.I;
                change_factors[i]    = this;
            }
            else{
                no_change_factors[i] = Gate.I;
                change_factors[i]    = Gate.I;
            }
        }
        Gate no_change = Gate.tensor(no_change_factors);
        Gate change = Gate.tensor(change_factors);
        return no_change.add(change);
    }

    public Gate mul(Complex scalar) { //gen
        int n = size();
        Complex[][] result = new Complex[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                result[i][j] = mat[i][j].mul(scalar);
            }
        }
        return new Gate(result);
    }
    public Gate add(Gate other) { //gen
        int n = size();
        Complex[][] result = new Complex[n][n];
        for (int i = 0; i < n; i++) { for (int j = 0; j < n; j++) {
                result[i][j] = mat[i][j].add(other.mat[i][j]);
        }}
        return new Gate(result);
    }
    public Gate sub(Gate other) { //gen
        int n = size();
        Complex[][] result = new Complex[n][n];
        for (int i = 0; i < n; i++) { for (int j = 0; j < n; j++) {
                result[i][j] = mat[i][j].sub(other.mat[i][j]);
        }}
        return new Gate(result);
    }
    public Gate dagger() { //gen
        int n = size();
        Complex[][] result = new Complex[n][n];
        for (int i = 0; i < n; i++) { for (int j = 0; j < n; j++) {
                result[j][i] = mat[i][j].con();
        }}
        return new Gate(result);
    }
    public Gate tensor(Gate other) { //gen
        int n = this.size();
        int m = other.size();
        Complex[][] result = new Complex[n * m][n * m];
    
        for (int i = 0; i < n; i++) { for (int j = 0; j < n; j++) {
            for (int k = 0; k < m; k++) { for (int l = 0; l < m; l++) {
                result[i * m + k][j * m + l] = this.mat[i][j].mul(other.mat[k][l]);
            }}
        }}
    
        return new Gate(result);
    }
    public static Gate tensor(Gate... gates) { //gen
        // if (gates.length == 0) throw new IllegalArgumentException("At least one gate required");
    
        Gate result = gates[0];
        for (int i = 1; i < gates.length; i++) {
            result = result.tensor(gates[i]);
        }
        return result;
    }
    public static Gate identity(int dim) { //gen
        Complex[][] id = new Complex[dim][dim];
        for (int i = 0; i < dim; i++) { for (int j = 0; j < dim; j++) {
                id[i][j] = (i == j) ? Complex.ONE : Complex.ZERO;
        }}
        return new Gate(id);
    }
    public static Gate identityByPow(int n) { return identity(Utils.powi(2, n)); }
    public static Gate zero(int dim) { //gen
        Complex[][] id = new Complex[dim][dim];
        for (int i = 0; i < dim; i++) { for (int j = 0; j < dim; j++) {
                id[i][j] = Complex.ZERO;
        }}
        return new Gate(id);
    }
}
