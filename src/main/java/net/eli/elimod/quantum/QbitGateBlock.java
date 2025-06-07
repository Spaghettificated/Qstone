package net.eli.elimod.quantum;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.eli.elimod.setup.ModBlocks;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.tick.ScheduledTickView;

// public class QbitGateBlock {
public class QbitGateBlock extends QbitSpreadBlock {
    public static final BooleanProperty IS_CONTROLLED = BooleanProperty.of("is_controlled");
    public static final EnumProperty<Direction> CONTROL_DIRECTION = EnumProperty.of("control_direction", Direction.class);
    private Gate gate;
    // private Optional<BlockPos> control;

	public QbitGateBlock(Settings settings, Gate gate) {
        super(settings);
        this.gate = gate;
        // control = Optional.empty();
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(IS_CONTROLLED, false)
                .with(CONTROL_DIRECTION, Direction.NORTH));
    }
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        super.appendProperties(builder);
        builder.add(IS_CONTROLLED);
        builder.add(CONTROL_DIRECTION);
    }
    @Override
	protected MapCodec<? extends QbitBlock> getCodec() {
		return createCodec(QbitWireBlock::new);
	}
    public BlockPos[] controlBits(BlockState state, World world, BlockPos pos){
        Direction facing = state.get(FACING);
        // var x = new ArrayList(Direction.values()).removeIf(x -> x==state.get("FACING").get();)
        var v = Arrays.stream(Direction.values())
                     .filter(x -> x!=facing)
                     .map(x -> pos.offset(x))
                     .toArray(BlockPos[]::new);
        return v;
    }

    @Override
    protected BlockState getStateForNeighborUpdate(
		BlockState state,
		WorldView world,
		ScheduledTickView tickView,
		BlockPos pos,
		Direction direction,
		BlockPos neighborPos,
		BlockState neighborState,
		Random random
	) {
        if(state.get(IS_CONTROLLED, false)){
            var dir = state.get(CONTROL_DIRECTION);
            var neighbour = world.getBlockState(pos.offset(dir));
            if(neighbour.getBlock() == ModBlocks.QBIT_CONTROL){
                // if(neighbour.get(FACING) == dir.getOpposite()){
                //     return state;
                // }
                return state;
            }
        }

        for (Direction dir : DIRECTIONS) {
            if(dir != state.get(FACING)){
                var neighbour = world.getBlockState(pos.offset(dir));
                if(neighbour.getBlock() == ModBlocks.QBIT_CONTROL){
                    // if(neighbour.get(FACING) == dir.getOpposite()){
                    //     return state.with(IS_CONTROLLED, true).with(CONTROL_DIRECTION, dir);
                    // }
                    return state.with(IS_CONTROLLED, true).with(CONTROL_DIRECTION, dir);
                }
            }
        }
        return state.with(IS_CONTROLLED, false);
    }

    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
        
        
        Optional<QbitEntity> source = sourceQbit(state, world, pos);
        if (source.isPresent()) {
            var sourceQbit = source.get().getState().get();
            // if( control.isPresent()){
            if( state.get(IS_CONTROLLED, false)){
                var controlPos = pos.offset(state.get(CONTROL_DIRECTION));
                // var controlPos = control.get();
                QbitEntity controlSource = sourceQbit(world.getBlockState(controlPos), world, controlPos).get();
                var controlSourceQbit = controlSource.getState().get();

                State entangled = State.tensor(sourceQbit, controlSourceQbit);
                Gate controlled_gate = gate.controled_by1();
                var disentangled = controlled_gate.actOn(entangled).decomposeState();
                if(disentangled.isPresent()){
                    setState(Optional.of(disentangled.get()[0]), state, world, pos, sourceBlock);
                    setState(Optional.of(disentangled.get()[1]), world.getBlockState(controlPos), world, controlPos, sourceBlock);
                }
                else{
                    return;
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
