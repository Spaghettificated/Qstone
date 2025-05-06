package net.eli.elimod.quantum;

public class State {
    protected Complex[] vec;
    public Complex[] getVec() { return vec; }
    public void setVec(Complex[] vec) { this.vec = vec; }

    public State(Complex[] vec) {
        this.vec = vec;
    }
    public int size(){
        return vec.length;
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
}
