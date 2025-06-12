package net.eli.elimod.quantum;

import java.util.Optional;

import net.eli.elimod.utils.Utils;

public class State {
    protected Complex[] vec;
    public Complex[] getVec() { return vec; }
    public void      setVec(Complex[] vec) { this.vec = vec; }
    public Complex get(int i) { return vec[i]; }
    public void    set(int i, Complex z) { vec[i] = z; }

    public State(Complex[] vec) {
        this.vec = vec;
    }
    public int size(){
        return vec.length;
    }
    public int numQbits(){
        return (int)Math.round(Math.log(size()) / Math.log(2));
    }
    public static State[] basis(int n){
        State[] out = new State[n];
        for (int i = 0; i < n; i++) {
            Complex[] state = new Complex[n];
            for (int j = 0; j < n; j++) {
                state[j] = (i==j) ? Complex.ONE : Complex.ZERO;
            }
            out[i] = new State(state);
        }
        return out;
    }
    public State[] basis() { return State.basis(size()); }

    public static Complex dot(State s1, State s2){
        Complex[] u = s1.getVec();
        Complex[] v = s2.getVec();
        Complex out = new Complex(0,0);
        for (int i = 0; i < u.length; i++) {
            // out += u[i].re * v[i].re + u[i].im * v[i].im;
            out.addmut(Complex.mul(Complex.con(u[i]), v[i]));
        }
        return out;
    }
    public static double aligment(State s1, State s2){
        return dot(s1, s2).magSqr();
    }
    public static Gate projection(State s1, State s2){
        int size = s1.size();
        Complex[][] out = new Complex[size][size];
        for (int i = 0; i < size; i++) { for (int j = 0; j < size; j++) {
                out[i][j] = s1.get(i).mul(s2.get(j));
        }}
        return new Gate(out);
    }
    public static Gate projection(State s1) { return projection(s1, s1); }
    private static Complex[] tensorVec(Complex[] a, Complex[] b) { //gen
        Complex[] result = new Complex[a.length * b.length];
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < b.length; j++) {
                result[i * b.length + j] = a[i].mul(b[j]);
            }
        }
        return result;
    }
    public static State tensor(State... states) { //gen
        if (states.length == 0) throw new IllegalArgumentException("At least one state required");
    
        Complex[] result = states[0].getVec();
        for (int i = 1; i < states.length; i++) {
            result = tensorVec(result, states[i].getVec());
        }
        return new State(result);
    }
    
    @Override public String toString() {
        String out = "|" + vec[0].toString();

        for (int i = 1; i < vec.length; i++) {
            out += ", " + vec[i].toString();
        }
        return out + ">";
    }

    public State mulmut(Complex z){
        for (int i = 0; i < vec.length; i++) {
            vec[i] = vec[i].mul(z);
        }
        return this;
    }
    public State mulmut(double x){
        for (int i = 0; i < vec.length; i++) {
            vec[i] = vec[i].mul(x);
        }
        return this;
    }

    public double normSqr(){
        Complex[] v = getVec();
        double out = 0;
        for (int i = 0; i < v.length; i++) {
            // out += u[i].re * v[i].re + u[i].im * v[i].im;
            out += v[i].magSqr();
        }
        return out;
    }
    public double norm(){
        return Math.sqrt(normSqr());
    }
    public State normalize(){
        this.mulmut(1 / this.norm());
        return this;
    }


    public static State productState(Qbit... qbits){
        int n = qbits.length;
        Complex[] vec = new Complex[Utils.powi(2, n)];
        for (int i = 0; i < vec.length; i++) {
            vec[i] = Complex.ONE;
        }
        for (int i = 0; i < n; i++) {
            Complex[] qbit = qbits[i].getVec();
            // int d = Utils.powi(2, i);
            int d = Utils.powi(2, n-i-1);
            for (int j = 0; j < vec.length; j++) {
                vec[j] = vec[j].mul(qbit[(j/d)%2]);
            }
        }
        return new State(vec);
    }
    public Optional<Qbit[]> decomposeState(){
        System.out.println("decomposing: " + toString());
        if(vec.length == 0){
            Qbit[] out = {};
            return Optional.of(out);
        }
        if(vec.length == 2){
            Qbit[] out = {new Qbit(vec)};
            return Optional.of(out);
        }
        if(vec.length == 4){
            Qbit[] out = new Qbit[2];

            System.out.println("check: " + vec[0].mul(vec[3]).toString() + " == " + vec[1].mul(vec[2]).toString() + "   " + ! vec[0].mul(vec[3])  .eq(   vec[1].mul(vec[2]) ));
            if(! vec[0].mul(vec[3])  .eq(   vec[1].mul(vec[2]) )){
                return Optional.empty();
            }
            // double c0 = Math.sqrt(vec[0].mag()); 
            double ph0 = vec[2].phase();
            double ph1 = vec[1].phase();
            double c0 = Math.sqrt(vec[0].magSqr() + vec[1].magSqr());
            double s0 = Math.sqrt(vec[2].magSqr() + vec[3].magSqr());
            double c1 = Math.sqrt(vec[0].magSqr() + vec[2].magSqr());
            double s1 = Math.sqrt(vec[1].magSqr() + vec[3].magSqr());

            out[0] = new Qbit(Complex.fromPolar(c0, 0), Complex.fromPolar(s0, ph0));
            out[1] = new Qbit(Complex.fromPolar(c1, 0), Complex.fromPolar(s1, ph1));

            return Optional.of(out);
        }
        return Optional.empty();
    }

    public State[] schmidtDecomposition(){ // elements-of-quantum-computation-and-quantum-communication [Anirban Aathak] page 96 
        if(size()==2){
            State[] out = {this};
            return out;
        }
        if(size() == 4){
            var out = new State[2];
            Complex a = vec[0].powi(2).add(vec[1].powi(2));
            Complex b = vec[0].con().mul(vec[2]). add( vec[1].con().mul(vec[3]));
            Complex delta = Complex.sum(Complex.ONE, Complex.fromRe(4 * b.mag()), a.powi(2).mul(4), a.mul(-4)).sqrt();
            // Complex coeff0 = a.sqrt();
            // Complex coeff1 = Complex.ONE.sub(a).sqrt();
            Complex eigv0, eigv1;
            Qbit v0,v1;
            if(b.eq(Complex.ZERO)){
                eigv0 = a;
                eigv1 = Complex.ONE.sub(a);
                v0 = new Qbit(Complex.ZERO, Complex.ONE);
                v1 = new Qbit(Complex.ONE,  Complex.ZERO);
            }
            else{
                eigv0 = Complex.ONE.add(delta).div(2);
                eigv1 = Complex.ONE.sub(delta).div(2);
                v0 = new Qbit(Complex.sum(Complex.ONE, a.mul(-2), delta      ).div( b.con().mul(2) ), Complex.ONE);
                v1 = new Qbit(Complex.sum(Complex.ONE, a.mul(-2), delta.neg()).div( b.con().mul(2) ), Complex.ONE);
            }
            v0.normalize();
            v1.normalize();
            var coeff0 = eigv0.sqrt();
            var coeff1 = eigv1.sqrt();
            // matrix for eqation M*(x0 x1) = (S0 S2) and M*(y0 y1) = (S1 S3) where S is initial 4-state and x,y form second qbit base
            var ma = coeff0.mul(v0.get(0));
            var mb = coeff1.mul(v1.get(0));
            var mc = coeff0.mul(v0.get(1));
            var md = coeff1.mul(v1.get(1));
            var amp = Complex.ONE.div( Complex.sub( ma.mul(md), mb.mul(mc)) );
            Complex[][] inverse = {{md.div(amp),       mb.neg().div(amp)},
                                   {mc.neg().div(amp), ma.div(amp)      }};
            var solveGate = new Gate(inverse);
            var xvec = solveGate.actOn(new Qbit(vec[0], vec[2]));
            var yvec = solveGate.actOn(new Qbit(vec[1], vec[3]));
            var u0 = new Qbit(xvec.get(0), yvec.get(0));
            var u1 = new Qbit(xvec.get(1), yvec.get(1));
            return out;
        }
        return new State[0];
    }
    
    public Optional<Qbit> asQbit(){ 
        if(size()==2){
            return Optional.of(new Qbit(vec));
        }
        return Optional.empty();
    }

    // public double measurementProbability(Qbit direction, int qbitPos){
    //     int n = numQbits();

    // }

}


