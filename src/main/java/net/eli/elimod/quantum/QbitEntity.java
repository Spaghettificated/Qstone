package net.eli.elimod.quantum;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.eli.elimod.setup.ModBlockEntities;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class QbitEntity extends BlockEntity{
    private Optional<Qbit> qbit;
    public Optional<Qbit> getQbit() { return qbit; }
    public void setQbit(Qbit qbit) { this.qbit = Optional.of(qbit); }
    public void clearQbit() { this.qbit = Optional.empty(); }
    public void setQbitOption(Optional<Qbit> qbit) { this.qbit = qbit; }
    
    public QbitEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.QBIT_ENTITY, pos, state);
        this.qbit = Optional.empty();
    }
    public QbitEntity(BlockPos pos, BlockState state, Optional<Qbit> initialQbit) {
        super(ModBlockEntities.QBIT_ENTITY, pos, state);
        this.qbit = initialQbit;
    }
    public QbitEntity(BlockPos pos, BlockState state, Qbit initialQbit) {
        super(ModBlockEntities.QBIT_ENTITY, pos, state);
        this.qbit = Optional.of(initialQbit);
    }


	@Override
	protected void writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		if(qbit.isPresent()){
            var qbitVec = qbit.get().getVec();
            nbt.putBoolean("has_state", true);
            nbt.putDouble("re0", qbitVec[0].re);
            nbt.putDouble("im0", qbitVec[0].im);
            nbt.putDouble("re1", qbitVec[1].re);
            nbt.putDouble("im1", qbitVec[1].im);
        }
        else{
            nbt.putBoolean("has_state", false);
        }

		super.writeNbt(nbt, registryLookup);
	}

	@Override
	protected void readNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup registryLookup) {
		super.readNbt(nbt, registryLookup);


        var re0 = nbt.getDouble("re0");
        var im0 = nbt.getDouble("im0");
        var re1 = nbt.getDouble("re1");
        var im1 = nbt.getDouble("im1");

        // if(nbt.getBoolean("has_state", false)){
        if(re0.isPresent() && im0.isPresent() && re1.isPresent() && im1.isPresent()){
            qbit = Optional.of(new Qbit(
                new Complex(re0.get(), im0.get()),
                new Complex(re1.get(), im1.get())
            ));
        }
        else{
            qbit = Optional.empty();
        }


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
