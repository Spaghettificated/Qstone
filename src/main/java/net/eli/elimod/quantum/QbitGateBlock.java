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
        builder.add(IS_CONTROLLED);
        builder.add(CONTROL_DIRECTION);
        super.appendProperties(builder);
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
		var new_state = state;

        if(state.get(IS_CONTROLLED, false)){
            var dir = state.get(CONTROL_DIRECTION);
            var neighbour = world.getBlockState(pos.offset(dir));
            if(neighbour.getBlock() == ModBlocks.QBIT_CONTROL){
                // if(neighbour.get(FACING) == dir.getOpposite()){
                //     return state;
                // }
                // return state;
                super.getStateForNeighborUpdate(new_state, world, tickView, pos, direction, neighborPos, neighborState, random);
            }
        }

        for (Direction dir : DIRECTIONS) {
            if(dir != state.get(FACING)){
                var neighbour = world.getBlockState(pos.offset(dir));
                if(neighbour.getBlock() == ModBlocks.QBIT_CONTROL){
                    // if(neighbour.get(FACING) == dir.getOpposite()){
                    //     return state.with(IS_CONTROLLED, true).with(CONTROL_DIRECTION, dir);
                    // }
		            
                    // return state.with(IS_CONTROLLED, true).with(CONTROL_DIRECTION, dir);
                    new_state = state.with(IS_CONTROLLED, true).with(CONTROL_DIRECTION, dir);
                    return super.getStateForNeighborUpdate(new_state, world, tickView, pos, direction, neighborPos, neighborState, random);
                }
            }
        }
        // return state.with(IS_CONTROLLED, false);
        new_state = state.with(IS_CONTROLLED, false);
		return super.getStateForNeighborUpdate(new_state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public void reciveQbit(BlockState state, World world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof QbitEntity entity) {
            Gate actinGate = gate;
            var hasControl = state.get(IS_CONTROLLED, false);
            if (hasControl && world.getBlockEntity(pos.offset(state.get(CONTROL_DIRECTION))) instanceof QbitEntity controlEntity){ // jeśli ma bramkę kontrolną
                if (entity.isPresent() && controlEntity.isPresent()){
                    System.out.println("entangling: " + entity.getState().get().toString() + " " + Integer.toString(entity.getQbitNumber()) + " | " + Integer.toString(entity.getQbit_pos()));
                    System.out.println("with:       " + entity.getState().get().toString() + " " + Integer.toString(entity.getQbitNumber()) + " | " + Integer.toString(entity.getQbit_pos()));
                    entity.entangle(controlEntity);
                    System.out.println("entangled state " + entity.getState().get().toString() + " on " + Integer.toString(entity.getQbitNumber()) + " qbits");
                    System.out.println(Integer.toString(controlEntity.getQbit_pos()) + " | " + Integer.toString(entity.getQbit_pos()));
                    actinGate = gate.controled(entity.getQbitNumber(), controlEntity.getQbit_pos(), entity.getQbit_pos());
                    entity.actOnAll(actinGate, world);
                    System.out.println("output state " + entity.getState().get().toString() + " on " + Integer.toString(entity.getQbitNumber()) + " qbits");
                    System.out.println(entity.disentangle(world));
                }
            }
            else if (entity.isPresent() && !hasControl){
                entity.actOn(actinGate, world);
            }
            for (BlockPos epos : entity.getEntangled()){
              if (epos != pos){
                world.updateNeighbor(epos, null, null);
              }
            }
        }
        super.reciveQbit(state, world, pos);
    }
    @Override
    public boolean passQbit(BlockState state, World world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof QbitEntity entity) {
            var hasControl = state.get(IS_CONTROLLED, false);
            if (hasControl){ // jeśli ma bramkę kontrolną
                var controlPos = pos.offset(state.get(CONTROL_DIRECTION));
                if (world.getBlockEntity(controlPos) instanceof QbitEntity controlEntity) {
                    if (entity.isPresent() && controlEntity.isPresent()
                        && world.getBlockState(controlPos).getBlock() instanceof QbitBlock controlBlock ){
                            controlBlock.passQbit(world.getBlockState(controlPos), world, controlPos);
                            return super.passQbit(state, world, pos);
                    }
                }
            }
            else if (entity.isPresent()){
                return super.passQbit(state, world, pos);
            }
        }
        return false;
    }
}
