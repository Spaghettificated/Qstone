package net.eli.elimod;

import net.eli.elimod.quantum.Gate;

public class Main {
    public static void main(String[] args) {
        // System.err.println(Complex.ONE);
        // System.err.println(Complex.I);
        // System.err.println(new Qbit(Complex.ONE, Complex.I));
        // System.err.println(new Qbit(new Complex(1,0), new Complex(0,1)));
        // System.err.println(Qbit.MINUS);
        // System.err.println(Qbit.dot(Qbit.PLUS, Qbit.ZERO));
        // System.err.println(Gate.X);
        // System.err.println(Qbit.ZERO);
        // System.err.println(Gate.Z.actOn(Qbit.ZERO));
        // System.err.println("lol" );
        // System.err.println((5/4)%2);

        // State product = State.productState(Qbit.ONE, Qbit.ZERO);
        // System.err.println(product);
        // product = Gate.CNOT.actOn(product);
        // System.err.println(product);
        // Qbit[] qbits = product.decomposeState().get();
        // System.err.println("compost:");
        // for (Qbit qbit : qbits) {
        //     System.err.println(qbit);
        // }
        // State product1 = State.productState(Qbit.ONE, Qbit.ZERO, Qbit.ZERO);
        // System.err.println(product1);
        // State product2 = State.tensor(Qbit.ONE, Qbit.ZERO, Qbit.ZERO);
        // // State product2 = State.tensor(Qbit.ONE, State.tensor(Qbit.ZERO, Qbit.ZERO));
        // System.err.println(product2);

        // // System.err.println((int)(Math.log(8) / Math.log(2)));
        // // System.err.println(Math.round(7.9));
        // // State[] decomposed = State.productState(Qbit.ONE, Qbit.ZERO).schmidtDecomposition();
        // // for (State state : decomposed) {
        // //     System.err.println(state);
        // // }
        // // System.err.println(State.projection(Qbit.ZERO, Qbit.ONE).actOn(Qbit.ZERO));
        // System.err.println(Gate.controled_by1(Gate.X));
        // var m = new MultiQbit(0);
        // m.increment();
        // System.err.println(m.getI());

        System.err.println(Gate.X.controled_by0());

        System.err.println(Gate.X.controled(2, 0, 1));
        System.out.println(1 << 2 );
    }
  }
