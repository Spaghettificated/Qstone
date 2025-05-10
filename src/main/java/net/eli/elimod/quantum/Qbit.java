package net.eli.elimod.quantum;

import java.util.LinkedHashMap;
import java.util.Map;

import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Qbit extends State{
    // public Complex[] getVec() { return vec; }
    // public void setVec(Complex[] vec) { this.vec = vec; }
  
    public static final Qbit ZERO    = new Qbit(Complex.ONE,     Complex.ZERO);
    public static final Qbit ONE     = new Qbit(Complex.ZERO,    Complex.ONE);
    public static final Qbit PLUS    = new Qbit(Complex.ONE,     Complex.ONE)       .mulmut( 1/Math.sqrt(2) );
    public static final Qbit MINUS   = new Qbit(Complex.ONE,     Complex.ONE.neg()) .mulmut( 1/Math.sqrt(2) );
    public static final Qbit PLUS_I  = new Qbit(Complex.ONE,     Complex.I)         .mulmut( 1/Math.sqrt(2) );
    public static final Qbit MINUS_I = new Qbit(Complex.ONE,     Complex.I.neg())   .mulmut( 1/Math.sqrt(2) );
    
    // public static final Gate[] SINGLE_GATES = {Gate.X, Gate.Y, Gate.Z, Gate.H, Gate.S, Gate.T};
    public static final Map<String, Gate> SINGLE_GATES = new LinkedHashMap<>();
    static {
        SINGLE_GATES.put("x", Gate.X);
        SINGLE_GATES.put("y", Gate.Y);
        SINGLE_GATES.put("z", Gate.Z);
        SINGLE_GATES.put("h", Gate.H);
        SINGLE_GATES.put("s", Gate.S);
        SINGLE_GATES.put("t", Gate.T);
    }

    
    static Complex[] prepareArray(Complex z, Complex w){
        Complex[] vec = new Complex[2];
        vec[0] = z; vec[1] = w;
        return vec;
    }
    public Qbit(Complex z, Complex w) {
        super(prepareArray(z, w));
    }
    public Qbit(Complex[] vec) {
        super(vec);
    }


    public Qbit mulmut(Complex z){
        for (int i = 0; i < vec.length; i++) {
            vec[i] = vec[i].mul(z);
        }
        return this;
    }
    public Qbit mulmut(double x){
        for (int i = 0; i < vec.length; i++) {
            vec[i] = vec[i].mul(x);
        }
        return this;
    }

    public static Qbit fromDirection(Direction dir){
        if(dir == Direction.UP) { return Qbit.ZERO; }
        if(dir == Direction.DOWN) { return Qbit.ONE; }
        if(dir == Direction.NORTH) { return Qbit.MINUS_I; }
        if(dir == Direction.SOUTH) { return Qbit.PLUS_I; }
        if(dir == Direction.EAST) { return Qbit.PLUS; }
        return Qbit.MINUS;
    }

    public boolean eq(Qbit other) {
        var u = getVec();
        var v = other.getVec();
        return u[0].eq(v[0]) && u[1].eq(v[1]);
    }

    public Vec3d blochVec(){
        return new Vec3d(State.dot(this, Gate.X.actOn(this)).re, 
                         State.dot(this, Gate.Z.actOn(this)).re,
                         State.dot(this, Gate.Y.actOn(this)).re);
    }
}
