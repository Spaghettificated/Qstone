package net.eli.elimod.quantum;

import java.util.Optional;

import net.eli.Utils;

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
    public long numQbits(){
        return Math.round(Math.log(size()) / Math.log(2));
    }
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
    
    public Optional<Qbit> asQbit(){
        if(size()==2){
            return Optional.of(new Qbit(vec));
        }
        return Optional.empty();
    }

}
