 package net.eli.elimod.quantum;

import java.util.Arrays;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.common.collect.Streams;
import com.llamalad7.mixinextras.lib.apache.commons.ArrayUtils;

import net.eli.elimod.setup.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtDouble;
import net.minecraft.nbt.NbtList;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.ActionResult;
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
    public BlockPos[] getEntangled() { return entangled; }
    public void setEntangled(BlockPos[] entangled) {  this.entangled = entangled; }
    public Optional<State> getState()                { return state; }
    public Vec3d[] getBlochVecs()                  { return blochVecs; }
    public void setState(Qbit qbit)                 { this.state = Optional.of(qbit); this.updateBlochVecs(); }
    public void clearState()                        { this.state = Optional.empty();  this.updateBlochVecs(); }
    public void setStateOption(Optional<State> qbit) { this.state = qbit;             this.updateBlochVecs(); }
    // public void set(QbitEntity other) {this.qbit = other.qbit; this.qbit_pos = other.qbit_pos;}
    
    public boolean isPresent() { return state.isPresent(); }
    public boolean isEmpty()   { return state.isEmpty(); }

    public void setQbit(Qbit qbit){
        state = Optional.of(qbit);
        qbit_pos = 0;
        entangled = new BlockPos[1];
        entangled[0] = getPos();
        updateBlochVecs();
        markDirty();
    }

    public boolean clone(QbitEntity other){
        for (BlockPos pos : other.entangled) {
            if (pos==getPos()) {return false; }
        }
        qbit_pos = other.qbit_pos;
        entangled = other.entangled.clone();
        entangled[qbit_pos] = getPos();
        state = other.getState();
        blochVecs = other.getBlochVecs();
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

    public void updateBlochVecs(){
        // on second thought this probably shouldn't be calculated serverside
        // probably better is to have boolean "renderUpdatePending" here
        // and save bloch sphere state in renderer

        int n_qbits = state.isPresent() ? 1 : 0;
        Vec3d[] out = new Vec3d[n_qbits];
        if(n_qbits == 1){
            out[0] = state.get().asQbit().get().blochVec();
        }
        // System.out.printf("%s %s %s %s\n", n_qbits, out.length, qbit, qbit.isPresent());
        // return out;
        blochVecs = out;
    }

    public QbitEntity(BlockPos pos, BlockState state, Optional<State> initialQbit) {
        super(ModBlockEntities.QBIT_ENTITY, pos, state);
        this.state = initialQbit;
        updateBlochVecs();
        // isVisible = true;
        qbit_pos = 0;
        entangled = new BlockPos[1];
        entangled[0] = pos;
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
    private Vec3d[] readBlochNBT(NbtCompound nbt) {
        int bloch_n = nbt.getInt("bloch_n").get();
        var blochComponents = nbt.getList("bloch_components").get();
        Vec3d[] newBlochVecs = new Vec3d[bloch_n];
        for (int i = 0; i < bloch_n; i++) {
            newBlochVecs[i] = new Vec3d(
                blochComponents.getDouble(i).get(),
                blochComponents.getDouble(i+1).get(),
                blochComponents.getDouble(i+2).get()
            );
        }
        return newBlochVecs;
    }

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(nbt, registryLookup);
        state = readQbitNBT(nbt, "qbit0");
        blochVecs = readBlochNBT(nbt);
	}

    public void entangle(QbitEntity other){
        if(! Arrays.asList(entangled).contains(other.getPos()) ){
            return;
        }
        int n = state.get().numQbits();
        entangled = ArrayUtils.addAll(entangled, other.getEntangled());
        state = Optional.of(State.tensor(state.get(), other.state.get()));
        other.get_entangled(n, state, entangled.clone());
    }
    public void get_entangled(int other_n, Optional<State> state, BlockPos[] ent){
        qbit_pos = other_n + qbit_pos;
        state = state;
        entangled = ent;
    }
    public boolean disentangle(World world){
        if( state.get().numQbits() == 2 ){
            var bits = state.get().decomposeState();
            if(!bits.isPresent()) { return false; }
            for (var p : entangled) {
                if(world.getBlockEntity(p) instanceof QbitEntity qbitEntity){
                    qbitEntity.getDisentangled(bits.get());
                }
            }
            return true;
        }
        return false;
    }
    public void getDisentangled(Qbit[] bits){
        state = Optional.of(bits[qbit_pos]);
        qbit_pos = 0;
        entangled = new BlockPos[1];
        entangled[0] = getPos();
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
