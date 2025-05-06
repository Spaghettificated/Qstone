package net.eli.elimod.quantum;

public class Complex implements Cloneable{
    public double re;
    public double im;
    public static final Complex ZERO = new Complex(0,0);
    public static final Complex ONE = new Complex(1,0);
    public static final Complex I = new Complex(0,1);

    public Complex(double re, double im){
        this.re = re;
        this.im = im;
    }

    public Complex clone(){
        return new Complex(re, im);
    }
    public void set(Complex z){
        re = z.re;
        im = z.im;
    }


    public double magSqr(){
        return Math.pow(re, 2) + Math.pow(im, 2);
    }
    public double mag(){
        return Math.sqrt(this.magSqr());
    }
    public double phase(){
        return Math.atan2(im, re);
    }

    public boolean eq(Complex other) {
        // return (re==other.re) && (im == other.im);
        return (re - other.re < 0.001) && (im - other.im < 0.01);
    }

    public Complex neg() { return new Complex(-re, -im); }
    public Complex con() { return new Complex(re, -im); }

    public Complex negmut() { set(this.neg()); return this; }
    public Complex conmut() { set(this.con()); return this; }

    public Complex add(double x) { return new Complex(this.re + x, this.im); }
    public Complex sub(double x) { return new Complex(this.re - x, this.im); }
    public Complex mul(double x) { return new Complex(this.re * x, this.im * x); }
    public Complex div(double x) { return new Complex(this.re / x, this.im / x); }

    public Complex addmut(double x) { set(this.add(x)); return this; }
    public Complex submut(double x) { set(this.sub(x)); return this; }
    public Complex mulmut(double x) { set(this.mul(x)); return this; }
    public Complex divmut(double x) { set(this.div(x)); return this; }

    public Complex add(Complex z) { return new Complex(this.re + z.re, this.im + z.im); }
    public Complex sub(Complex z) { return new Complex(this.re - z.re, this.im - z.im); }
    public Complex mul(Complex z) { return new Complex(this.re * z.re - this.im * z.im, this.re * z.im + this.im * z.re); }
    public Complex div(Complex z) {
        // double denominator = z.re * z.re + z.im * z.im;
        // double real = (this.re * z.re + this.im * z.im) / denominator;
        // double imag = (this.im * z.re - this.re * z.im) / denominator;
        // return new Complex(real, imag);
        return this.mul(z.con()).div(z.magSqr());
    }
    public Complex addmut(Complex z) { set(this.add(z)); return this; }
    public Complex submut(Complex z) { set(this.sub(z)); return this; }
    public Complex mulmut(Complex z) { set(this.mul(z)); return this; }
    public Complex divmut(Complex z) { set(this.div(z)); return this; }

    public static Complex neg(Complex z){ return z.neg(); }
    public static Complex con(Complex z){ return z.con(); }
    public static Complex add(Complex z, Complex w){ return z.add(w); }
    public static Complex sub(Complex z, Complex w){ return z.sub(w); }
    public static Complex mul(Complex z, Complex w){ return z.mul(w); }
    public static Complex div(Complex z, Complex w){ return z.div(w); }

    public static Complex add(Complex z, double x) { return z.add(x); }
    public static Complex add(double x, Complex z) { return z.add(x); }
    public static Complex sub(Complex z, double x) { return z.sub(x); }
    public static Complex sub(double x, Complex z) { return new Complex(x, 0).sub(z); }
    public static Complex mul(Complex z, double x) { return z.mul(x); }
    public static Complex mul(double x, Complex z) { return z.mul(x); }
    public static Complex div(Complex z, double x) { return z.div(x); }
    public static Complex div(double x, Complex z) { return new Complex(x, 0).div(z); }

    // public Complex neg(){
    //     re = -re;
    //     im = -im;
    //     return this;
    // }
    // public Complex con(){
    //     im = -im;
    //     return this;
    // }
    // public Complex add(Complex z){
    //     re += z.re;
    //     im += z.im;
    //     return this;
    // }
    // public Complex sub(Complex z){
    //     re -= z.re;
    //     im -= z.im;
    //     return this;
    // }
    // public Complex mul(double x){
    //     re *= x;
    //     im *= x;
    //     return this;
    // }
    // public Complex mul(Complex z){
    //     double new_re = re * z.re - im * z.im;
    //     im *= re * z.im + im * z.re;
    //     re = new_re;
    //     return this;
    // }
    // public Complex div(double x){
    //     re /= x;
    //     im /= x;
    //     return this;
    // }
    // public Complex div(Complex z){
    //     this.mul(con(z));
    //     this.div(z.magSqr());
    //     return this;
    // }

    // public static Complex neg(Complex z){
    //     return z.clone().neg();
    // }
    // public static Complex con(Complex z){
    //     return z.clone().con();
    // }
    // public static Complex add(Complex z, Complex w){
    //     return z.clone().add(w);
    // }
    // public static Complex sub(Complex z, Complex w){
    //     return z.clone().sub(w);
    // }
    // public static Complex mul(Complex z, Complex w){
    //     return z.clone().mul(w);
    // }
    // public static Complex div(Complex z, Complex w){
    //     return z.clone().div(w);
    // }

    public static Complex fromPhase(double theta){
        return new Complex(Math.cos(theta), Math.sin(theta));
    }
    public static Complex fromRe(double re){
        return new Complex(re, 0);
    }
    public static Complex fromIm(double im){
        return new Complex(0, im);
    }

    // public static Complex add(Complex a, Complex b){
    //     return new Complex(a.re + b.re, a.im + b.im);
    // }
    // public static Complex sub(Complex a, Complex b){
    //     return new Complex(a.re - b.re, a.im - b.im);
    // }
    // public static Complex mul(Complex a, Complex b){
    //     return new Complex(a.re * b.re, a.im * b.im);
    // }
    // public static Complex div(Complex a, Complex b){
    //     return new Complex(a.re / b.re, a.im / b.im);
    // }

    @Override public String toString() {
        if(re==0. && im==0.) { return "0.00"; }
        if(re==0.) { return String.format("%.2fi", im); }
        if(im==0.) { return String.format("%.2f", re); }
        return (im>0) ? String.format("%.2f + %.2fi", re, im) : String.format("%.2f - %.2fi", re, im);
    }
    
}