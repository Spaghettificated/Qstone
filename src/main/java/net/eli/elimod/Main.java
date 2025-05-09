package net.eli.elimod;

import net.eli.elimod.quantum.Complex;
import net.eli.elimod.quantum.Gate;
import net.eli.elimod.quantum.Qbit;
import net.eli.elimod.quantum.State;

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
        System.err.println("lol" );
        System.err.println((5/4)%2);

        State product = State.productState(Qbit.ONE, Qbit.ZERO);
        System.err.println(product);
        product = Gate.CNOT.actOn(product);
        System.err.println(product);
        Qbit[] qbits = product.decomposeState().get();
        System.err.println("compost:");
        for (Qbit qbit : qbits) {
            System.err.println(qbit);
        }
        State product1 = State.productState(Qbit.ONE, Qbit.ZERO, Qbit.ZERO);
        System.err.println(product1);
        System.err.println((int)(Math.log(8) / Math.log(2)));
        System.err.println(Math.round(7.9));
    }
  }
