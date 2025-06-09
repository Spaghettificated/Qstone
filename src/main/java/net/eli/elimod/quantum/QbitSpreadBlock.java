package net.eli.elimod.quantum;

import java.util.Optional;

import net.eli.elimod.utils.OptDirection;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

public abstract class QbitSpreadBlock extends QbitBlock{
	public static final EnumProperty<Direction> FACING = Properties.FACING;

    public QbitSpreadBlock(Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.NORTH));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        super.appendProperties(builder);
    }

    protected BlockPos sourcePos(BlockState state, BlockPos pos){
        Direction direction = state.get(FACING);
        return pos.offset(direction);
    }
    protected Optional<QbitEntity> sourceQbit(BlockState state, World world, BlockPos pos){
        if(world.getBlockEntity(sourcePos(state, pos)) instanceof QbitEntity otherQbitEntity){
            return Optional.of(otherQbitEntity);
        }
        return Optional.empty();
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
        var dir = ctx.getPlayerLookDirection().getOpposite().getOpposite();
		return this.getDefaultState().with(FACING, dir).with(SOURCE, OptDirection.from(dir)).with(TARGET, OptDirection.NONE);
	}
}
