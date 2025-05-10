package net.eli.elimod.quantum;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

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
    public boolean isVisible;
    private Optional<Qbit> qbit;
    private Vec3d[] blochVecs = new Vec3d[0];
    public Optional<Qbit> getQbit()                { return qbit; }
    public Vec3d[] getBlochVecs()                  { return blochVecs; }
    public void setQbit(Qbit qbit)                 { this.qbit = Optional.of(qbit);  this.updateBlochVecs(); }
    public void clearQbit()                        { this.qbit = Optional.empty();   this.updateBlochVecs(); }
    public void setQbitOption(Optional<Qbit> qbit) { this.qbit = qbit;               this.updateBlochVecs(); }

    public void updateBlochVecs(){
        // on second thought this probably shouldn't be calculated serverside
        // probably better is to have boolean "renderUpdatePending" here
        // and save bloch sphere state in renderer

        int n_qbits = qbit.isPresent() ? 1 : 0;
        Vec3d[] out = new Vec3d[n_qbits];
        if(n_qbits == 1){
            out[0] = qbit.get().blochVec();
        }
        // System.out.printf("%s %s %s %s\n", n_qbits, out.length, qbit, qbit.isPresent());
        // return out;
        blochVecs = out;
    }

    public QbitEntity(BlockPos pos, BlockState state, Optional<Qbit> initialQbit) {
        super(ModBlockEntities.QBIT_ENTITY, pos, state);
        this.qbit = initialQbit;
        updateBlochVecs();
        isVisible = true;
    }
    public QbitEntity(BlockPos pos, BlockState state, Qbit initialQbit) {
        this(pos, state, Optional.of(initialQbit));
    }
    public QbitEntity(BlockPos pos, BlockState state) {
        this(pos, state, Optional.empty());
    }


    private void writeQbitNBT(NbtCompound nbt, Qbit qbit, String name) {
        nbt.putDouble(name + "_x_re", qbit.get(0).re);
        nbt.putDouble(name + "_x_im", qbit.get(0).im);
        nbt.putDouble(name + "_y_re", qbit.get(1).re);
        nbt.putDouble(name + "_y_im", qbit.get(1).im);
    }
	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		if(qbit.isPresent()){
            writeQbitNBT(nbt, qbit.get(), "qbit0");
        }

        // int[] blochComponents = new int[blochVecs.length * 3];
        nbt.putInt("bloch_n", blochVecs.length);
        NbtList blochComponents = new NbtList();
        for (int i = 0; i < blochVecs.length * 3; i++) {
            // blochComponents[i] = blochVecs[i/3].getComponentAlongAxis(Axis.values()[i%3])
            // nbt.putDouble("bloch_component_" + (String)i, blochVecs[i/3].getComponentAlongAxis(Axis.values()[i%3]));
            blochComponents.add( NbtDouble.of(blochVecs[i/3].getComponentAlongAxis(Axis.values()[i%3])));
        }
        nbt.put("bloch_components", blochComponents);
		super.writeNbt(nbt, registryLookup);
	}

    private Optional<Qbit> readQbitNBT(NbtCompound nbt, String name) {
        var re0 = nbt.getDouble(name + "_x_re");
        var im0 = nbt.getDouble(name + "_x_im");
        var re1 = nbt.getDouble(name + "_y_re");
        var im1 = nbt.getDouble(name + "_y_im");
        if(re0.isPresent() && im0.isPresent() && re1.isPresent() && im1.isPresent()){
            return Optional.of(new Qbit(
                new Complex(re0.get(), im0.get()),
                new Complex(re1.get(), im1.get())
            ));
        }
        else{
            return Optional.empty();
        }
    }
	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(nbt, registryLookup);
        qbit = readQbitNBT(nbt, "qbit0");
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
        blochVecs = newBlochVecs;
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
