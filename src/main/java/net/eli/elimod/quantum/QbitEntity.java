 package net.eli.elimod.quantum;

import java.util.Arrays;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Streams;
import com.llamalad7.mixinextras.lib.apache.commons.ArrayUtils;

import net.eli.elimod.setup.ModBlockEntities;
import net.eli.elimod.utils.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtInt;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Util;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.world.World;

public class QbitEntity extends BlockEntity{
    public boolean updated = false;
    private Optional<State> state;
    private int qbit_pos;

    private BlockPos[] entangled;

    private Vec3d[] blochVecs = new Vec3d[0];

    public int getQbit_pos() { return qbit_pos; }
    public void setQbit_pos(int qbit_pos) { this.qbit_pos = qbit_pos; }
    public BlockPos[] getEntangled() { return entangled; }
    public void setEntangled(BlockPos[] entangled) {  this.entangled = entangled; }
    public Optional<State> getState()                { return state; }
    public Vec3d[] getBlochVecs()                  { return blochVecs; }
    public void setState(State qbit)                 { this.state = Optional.of(qbit); this.updateBlochVecs(); }
    public void clearState()                        { this.state = Optional.empty();  this.updateBlochVecs(); }
    public void setStateOption(Optional<State> qbit) { this.state = qbit;             this.updateBlochVecs(); }
    // public void set(QbitEntity other) {this.qbit = other.qbit; this.qbit_pos = other.qbit_pos;}
    
    public boolean isPresent() { return state.isPresent(); }
    public boolean isEmpty()   { return state.isEmpty(); }
    public int getQbitNumber() { return entangled.length; }

    public void setQbit(Qbit qbit){
        state = Optional.of(qbit);
        qbit_pos = 0;
        entangled = new BlockPos[1];
        entangled[0] = getPos();
        updateBlochVecs();
        markDirty();
    }

    public boolean clone(QbitEntity other){
        for (BlockPos pos : other.getEntangled()) {
            if (pos==getPos()) { return false; }
        }
        this.qbit_pos = other.qbit_pos;
        // System.out.println(String.format("target qbits: %d, source qubits: %d", entangled.length, other.entangled.length));
        this.entangled = other.getEntangled().clone();
        // System.out.println(String.format("target qbits: %d, source qubits: %d", entangled.length, other.entangled.length));
        if (entangled.length > 0)
            { entangled[qbit_pos] = getPos(); }
        this.state = other.getState();
        this.blochVecs = other.getBlochVecs();
        this.markDirty();
        return true;
    }
    public void clear(){
        state = Optional.empty();
        entangled = new BlockPos[0];
        blochVecs = new Vec3d[0];
        qbit_pos = 0;
        this.markDirty();
    }
    public boolean takeFrom(QbitEntity other, World world){
        if( !clone(other) ) { return false; }
        for (int i = 0; i < entangled.length; i++) {
            if(i != qbit_pos){
                if (world.getBlockEntity(entangled[i]) instanceof QbitEntity entangledEntity){
                    entangledEntity.setEntangled(entangled);
                    entangledEntity.markDirty();
                }
            }
        }
        other.clear();
        return true;
    }

    public void actOn(Gate gate, World world){
        System.out.println("acting on " + state.get().toString() + " with\n" + gate.toString());
        var actingGate = gate.extended(getQbitNumber(), getQbit_pos());

        var result = actingGate.actOn(state.get());
        for (var pos : getEntangled().clone()) {
            if (world.getBlockEntity(pos) instanceof QbitEntity entity){
                entity.setState(result);
                entity.markDirty();
            }
        }
    }

    public void collapse(Qbit into){
        int n = getQbitNumber() - 1;
        int j = getQbit_pos();
        int p = Utils.powi(2, n);
        Complex[] stateArr = new Complex[p];
        // for (int i = 0; i < stateArr.length; i++)   { stateArr[i] = Complex.ZERO; }

        BlockPos[] restEntangled = new BlockPos[n];

        for (int i = 0; i < j; i++) {
            restEntangled[i] = entangled[i];
        }
        for (int i = j+1; i < n+1; i++) {
            restEntangled[i-1] = entangled[i];
        }

        var oldVec = getState().get().vec;
        for (int i = 0; i < p; i++) {
            int x = Utils.powi(2, j);
            var y = p/x;
            var a = oldVec[x*i].mul(into.get(0));
            var b = oldVec[x*i + y].mul(into.get(1));
            stateArr[i] = a.add(b);
        }
        State restState = new State(stateArr);
        restState = restState.normalize();

        for (int i = 0; i < j; i++) {
            if (world.getBlockEntity(entangled[i]) instanceof QbitEntity ent){
                ent.setState(restState);
                ent.setEntangled(restEntangled);
                ent.updateBlochVecs();
                ent.markDirty();
            }
        }
        for (int i = j+1; i < n+1; i++) {
            if (world.getBlockEntity(entangled[i]) instanceof QbitEntity ent){
                ent.setState(restState);
                ent.setEntangled(restEntangled);
                ent.setQbit_pos(ent.getQbit_pos() - 1);
                ent.updateBlochVecs();
                ent.markDirty();
            }
        }

        setQbit(into);
        markDirty();
    }

    public void actOnAll(Gate gate, World world){
        System.out.println("acting on " + state.get().toString() + " with\n" + gate.toString());

        var result = gate.actOn(state.get());
        for (var pos : getEntangled().clone()) {
            if (world.getBlockEntity(pos) instanceof QbitEntity entity){
                entity.setState(result);
                entity.markDirty();
            }
        }
    }

    public void updateBlochVecs(){
        // on second thought this probably shouldn't be calculated serverside
        // probably better is to have boolean "renderUpdatePending" here
        // and save bloch sphere state in renderer

        int n_qbits = getQbitNumber();
        Vec3d[] out = new Vec3d[n_qbits];
        if(n_qbits == 1){
            out[0] = state.get().asQbit().get().blochVec();
            blochVecs = out;
        }
        else if(n_qbits==2){
            var i = getQbit_pos();
            var decomposed = state.get().decomposeState();
            blochVecs = new Vec3d[2];
            if(decomposed.isPresent()){
                blochVecs[0] = decomposed.get()[0].blochVec();
                blochVecs[1] = decomposed.get()[1].blochVec();
            }
            else {
                var qbits = state.get().schmidtDecomposition();
                var v0 = qbits[0].norm();
                var v1 = qbits[1].norm();
                var s0 = qbits[0].normalize().decomposeState().get();
                var s1 = qbits[1].normalize().decomposeState().get();
                blochVecs[0] = s0[i].mulmut(v0).blochVec();
                blochVecs[1] = s1[i].mulmut(v1).blochVec();
            }

        }
        else{
            blochVecs = new Vec3d[0];
        }
        // System.out.printf("%s %s %s %s\n", n_qbits, out.length, qbit, qbit.isPresent());
        // return out;
    }

    public QbitEntity(BlockPos pos, BlockState state, Optional<State> initialQbit) {
        super(ModBlockEntities.QBIT_ENTITY, pos, state);
        this.state = initialQbit;
        // isVisible = true;
        qbit_pos = 0;
        if (initialQbit.isPresent()){
            entangled = new BlockPos[1];
            entangled[0] = pos;
        }
        else{
            entangled = new BlockPos[0];
        }
        updateBlochVecs();
    }
    public QbitEntity(BlockPos pos, BlockState state, Qbit initialQbit) {
        this(pos, state, Optional.of(initialQbit));
    }
    public QbitEntity(BlockPos pos, BlockState state) {
        this(pos, state, Optional.empty());
    }

    private void writeStateNBT(NbtCompound nbt, State qbit, String name) {
        nbt.putInt("state_size", qbit.size());
        NbtList state_coefficients = new NbtList();
        for (Complex z : qbit.getVec()) {
            state_coefficients.add(NbtDouble.of(z.re));
            state_coefficients.add(NbtDouble.of(z.im));
        }
        nbt.put("state_coefficients", state_coefficients);
    }
    private void writeEntangledNBT(NbtCompound nbt) {
        nbt.putInt("entangled_n", entangled.length);
        NbtList components = new NbtList();
        for (int i = 0; i < entangled.length; i++) {
            components.add(3*i,   NbtInt.of(entangled[i].getX()));
            components.add(3*i+1, NbtInt.of(entangled[i].getY()));
            components.add(3*i+2, NbtInt.of(entangled[i].getZ()));
        //     // components.add( NbtInt.of(entangled[i/3].getComponentAlongAxis(Axis.values()[2-(i%3)])));
        }
        nbt.put("entangled_components", components);
    }
    private void writeBlochNBT(NbtCompound nbt) {
        nbt.putInt("bloch_n", blochVecs.length);
        NbtList blochComponents = new NbtList();
        for (int i = 0; i < blochVecs.length * 3; i++) {
            // blochComponents[i] = blochVecs[i/3].getComponentAlongAxis(Axis.values()[i%3])
            // nbt.putDouble("bloch_component_" + (String)i, blochVecs[i/3].getComponentAlongAxis(Axis.values()[i%3]));
            blochComponents.add( NbtDouble.of(blochVecs[i/3].getComponentAlongAxis(Axis.values()[i%3])));
        }
        nbt.put("bloch_components", blochComponents);
    }
	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		if(state.isPresent()){
            writeStateNBT(nbt, state.get(), "qbit0");
        }
        writeBlochNBT( nbt);
        writeEntangledNBT( nbt);
        nbt.putInt("qbit_pos", qbit_pos);

        // int[] blochComponents = new int[blochVecs.length * 3];
		super.writeNbt(nbt, registryLookup);
	}

    private Optional<State> readQbitNBT(NbtCompound nbt, String name) {
        var state_coefficients = nbt.getList("state_coefficients");
        if(state_coefficients.isPresent()){
            
            var s = nbt.getInt("state_size", 0);
            Complex[] v = new Complex[s];
            for (int i = 0; i < s; i++) {
                var re = state_coefficients.get().getDouble(2*i).get();
                var im = state_coefficients.get().getDouble(2*i+1).get();
                v[i] = new Complex(re, im);
            }
            return Optional.of(new State(v));
        }
        else{
            return Optional.empty();
        }
    }
    private BlockPos[] readEntangledNBT(NbtCompound nbt) {
        int bloch_n = nbt.getInt("entangled_n").get();
        var blochComponents = nbt.getList("entangled_components").get();
        BlockPos[] newBlochVecs = new BlockPos[bloch_n];
        for (int i = 0; i < bloch_n; i++) {
            newBlochVecs[i] = new BlockPos(
                blochComponents.getInt(3*i).get(),
                blochComponents.getInt(3*i+1).get(),
                blochComponents.getInt(3*i+2).get()
            );
        }
        return newBlochVecs;
    }
    private Vec3d[] readBlochNBT(NbtCompound nbt) {
        int bloch_n = nbt.getInt("bloch_n").get();
        var blochComponents = nbt.getList("bloch_components").get();
        Vec3d[] newBlochVecs = new Vec3d[bloch_n];
        for (int i = 0; i < bloch_n; i++) {
            newBlochVecs[i] = new Vec3d(
                blochComponents.getDouble(3*i).get(),
                blochComponents.getDouble(3*i+1).get(),
                blochComponents.getDouble(3*i+2).get()
            );
        }
        return newBlochVecs;
    }

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(nbt, registryLookup);
        state = readQbitNBT(nbt, "qbit0");
        blochVecs = readBlochNBT(nbt);
        this.entangled = readEntangledNBT(nbt);
        qbit_pos = nbt.getInt("qbit_pos", qbit_pos);
	}

    public void entangle(QbitEntity other){
        if(Arrays.asList(entangled).contains(other.getPos()) ){
            return;
        }
        // int n = state.get().numQbits();
        int n = getQbitNumber();
        int other_n = other.getQbitNumber();
        // entangled = ArrayUtils.addAll(entangled, other.getEntangled());
        var new_entangled = new BlockPos[n + other_n];
        for (int i = 0; i < n; i++) {
                new_entangled[i] = entangled[i];
        }
        for (int i = 0; i < other_n; i++) {
                new_entangled[n+i] = other.getEntangled()[i];
        }
        this.entangled = new_entangled;
        state = Optional.of(State.tensor(state.get(), other.state.get()));
        other.get_entangled(n, state, new_entangled.clone());
        this.markDirty();
    }
    public void get_entangled(int other_n, Optional<State> state, BlockPos[] ent){
        this.qbit_pos = other_n + this.qbit_pos;
        this.state = state;
        this.entangled = ent;
        this.markDirty();
    }
    public boolean disentangle(World world){
        if( state.get().numQbits() == 2 ){
            var bits = state.get().decomposeState();
            if(bits.isEmpty()) { return false; }
            for (var p : getEntangled().clone()) {
                if(world.getBlockEntity(p) instanceof QbitEntity qbitEntity){
                    qbitEntity.getDisentangled(bits.get());
                }
            }
            this.updateBlochVecs();
            this.markDirty();
            return true;
        }
        return false;
    }
    public void getDisentangled(Qbit[] bits){
        state = Optional.of(bits[qbit_pos]);
        qbit_pos = 0;
        entangled = new BlockPos[1];
        entangled[0] = getPos();
        this.updateBlochVecs();
        this.markDirty();
    }


    @Nullable
    @Override
    public Packet<ClientPlayPacketListener> toUpdatePacket() {
        return BlockEntityUpdateS2CPacket.create(this);
    }

    @Override
    public NbtCompound toInitialChunkDataNbt(RegistryWrapper.WrapperLookup registryLookup) {
        return createNbt(registryLookup);
    }

}
