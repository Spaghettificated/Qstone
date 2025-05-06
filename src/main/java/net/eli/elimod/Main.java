package net.eli.elimod;

import net.eli.elimod.quantum.Complex;
import net.eli.elimod.quantum.Gate;
import net.eli.elimod.quantum.Qbit;

public class Main {
    public static void main(String[] args) {
        System.err.println(Complex.ONE);
        System.err.println(Complex.I);
        System.err.println(new Qbit(Complex.ONE, Complex.I));
        System.err.println(new Qbit(new Complex(1,0), new Complex(0,1)));
        System.err.println(Qbit.MINUS);
        System.err.println(Qbit.dot(Qbit.PLUS, Qbit.ZERO));
        System.err.println(Gate.X);
        System.err.println(Qbit.ZERO);
        System.err.println(Gate.Z.actOn(Qbit.ZERO));
    }
  }
