package net.eli.elimod.quantum;

import java.util.Arrays;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;

// public class QbitGateBlock {
public class QbitGateBlock extends QbitSpreadBlock {
    private Gate gate;
    private Optional<BlockPos> control;

	public QbitGateBlock(Settings settings, Gate gate) {
        super(settings);
        this.gate = gate;
        control = Optional.empty();
    }
    @Override
	protected MapCodec<? extends QbitBlock> getCodec() {
		return createCodec(QbitWireBlock::new);
	}

    


    // @Override
    // protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
    //     Direction direction = state.get(FACING);
    //     if ((world.getBlockEntity(pos.offset(direction)) instanceof QbitEntity otherQbitEntity)) {
    //         if( control.isPresent() ) {
                
    //         }
    //         else{
    //             setState(otherQbitEntity.getQbit().map(q -> gate.actOn(q)), state, world, pos, sourceBlock);
    //         }
    //     }
    //     else{
    //         setState(Optional.empty(), state, world, pos, sourceBlock);
    //     }

    // }

    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
        Optional<QbitEntity> source = sourceQbit(state, world, pos);
        if (source.isPresent()) {
            var sourceQbit = source.get().getQbit().get();
            if( control.isPresent()){
                var controlPos = control.get();
                QbitEntity controlSource = sourceQbit(world.getBlockState(controlPos), world, controlPos).get();
                var controlSourceQbit = source.get().getQbit().get();

                State entangled = State.tensor(sourceQbit, controlSourceQbit);
                Gate controlled_gate = gate.controled_by1();
                var disentangled = controlled_gate.actOn(entangled).decomposeState();
                if(disentangled.isPresent()){
                    setState(Optional.of(disentangled.get()[0]), state, world, pos, sourceBlock);
                    setState(Optional.of(disentangled.get()[1]), world.getBlockState(pos), world, controlPos, sourceBlock);
                }
                else{

                }

                
                // var controlSource = world.getBlockEntity(pos)
            }
            else{
                setState(Optional.of(gate.actOn(sourceQbit)), state, world, pos, sourceBlock);
            }
        }
        else{
            setState(Optional.empty(), state, world, pos, sourceBlock);
        }
    }
}
