package net.eli.elimod.quantum;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.eli.elimod.utils.OptDirection;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.tick.ScheduledTickView;

public class QbitControlBlock extends QbitBlock {
    public static final EnumProperty<Direction> FACING = Properties.FACING;

	public QbitControlBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.SOUTH).with(SOURCE, OptDirection.NONE).with(TARGET, OptDirection.NONE));
    }
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        super.appendProperties(builder);
    }

    @Override
	protected MapCodec<? extends QbitBlock> getCodec() {
		return createCodec(QbitWireBlock::new);
	}

    @Override
	protected void onBlockAdded(BlockState state, World world, BlockPos pos, BlockState oldState, boolean notify) {
		if (!state.isOf(oldState.getBlock())) {
			if (!world.isClient() && !world.getBlockTickScheduler().isQueued(pos, this)) {
				BlockState blockState = state;
				world.setBlockState(pos, blockState, Block.NOTIFY_LISTENERS | Block.FORCE_STATE);
				// this.updateNeighbors(world, pos, blockState);
                neighborUpdate(state, world, pos, this, null, collidable);
			}
		}
	}


	@Override
	public BlockState getPlacementState(ItemPlacementContext ctx) {
		return this.getDefaultState().with(FACING, ctx.getSide().getOpposite());
	}
    

    public OptDirection findSource(BlockState state, WorldView world, BlockPos pos){
        for (Direction dir : Direction.values()) {
            if ( dir != state.get(FACING) ) {
                var neighbourState = world.getBlockState(pos.offset(dir));
                if( neighbourState.getBlock() instanceof QbitBlock neigbour
                    && neighbourState.get(SOURCE, OptDirection.NONE) != OptDirection.from(dir.getOpposite())){
                    if(neigbour.isSource()){
                        return OptDirection.from(dir);
                    }
                }
            }
		}
		return OptDirection.NONE;
	}

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView,
            BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        var new_state = state;
        if (state.get(SOURCE, OptDirection.NONE).isNone()){
            new_state = state.with(SOURCE, findSource(state, world, pos));
        }
        else{
            if (state.get(SOURCE, OptDirection.NONE).getDirection() == direction){
                new_state = state.with(SOURCE, findSource(state, world, pos));
            }
        }
        return super.getStateForNeighborUpdate(new_state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    // // @Override
    // protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
        
    //     // updateNeighbors(world, pos, state);

    //     Direction direction = state.get(FACING);
    //     // if (!(world.getBlockEntity(pos) instanceof QbitEntity thisQbitEntity)) {
    //     //     return;
    //     // }
    //     if ((world.getBlockEntity(pos.offset(direction)) instanceof QbitEntity otherQbitEntity)) {
    //         // thisQbitEntity.setQbitOption(otherQbitEntity.getQbit());
    //         setState(otherQbitEntity.getQbit().map(q -> gate.actOn(q)), state, world, pos, sourceBlock);
    //     }
    //     else{
    //         // thisQbitEntity.setQbitOption(Optional.empty());
    //         setState(Optional.empty(), state, world, pos, sourceBlock);
    //     }
        
    //     // if(pos == )
	// 	// world.updateNeighborsExcept(pos, this, direction, null );
    // }



    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView view, BlockPos pos, ShapeContext context) {
        // float diff = 1f/8f;
        float diff = 3f/8f;
        return VoxelShapes.cuboid(0.5f - diff, 0.5f - diff, 0.5f - diff, 0.5f + diff, 0.5f + diff, 0.5f + diff);
    }

    @Override
    public void reciveQbit(BlockState state, World world, BlockPos pos) {
        var gatePos = pos.offset(state.get(FACING));
        var gateBlockState = world.getBlockState(gatePos);
        if (gateBlockState.getBlock() instanceof QbitGateBlock gateBlock){
            gateBlock.reciveQbit(gateBlockState, world, gatePos);
        }
        super.reciveQbit(state, world, pos);
    }
}
