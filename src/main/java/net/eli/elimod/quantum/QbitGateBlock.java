package net.eli.elimod.quantum;

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
public class QbitGateBlock extends QbitBlock {
    public static final EnumProperty<Direction> FACING = Properties.FACING;
    private Gate gate;

	public QbitGateBlock(Settings settings, Gate gate) {
        super(settings);
        this.gate = gate;
        this.setDefaultState(this.stateManager.getDefaultState().with(FACING, Direction.SOUTH));
    }
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
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
		return this.getDefaultState().with(FACING, ctx.getPlayerLookDirection().getOpposite().getOpposite());
	}
    

	@Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!(world.getBlockEntity(pos) instanceof QbitEntity blockEntity)) {
            return super.onUse(state, world, pos, player, hit);
        }

        // updateNeighbors(world, pos, state);

        return ActionResult.SUCCESS;
    }


    // @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
        
        // updateNeighbors(world, pos, state);

        Direction direction = state.get(FACING);
        // if (!(world.getBlockEntity(pos) instanceof QbitEntity thisQbitEntity)) {
        //     return;
        // }
        if ((world.getBlockEntity(pos.offset(direction)) instanceof QbitEntity otherQbitEntity)) {
            // thisQbitEntity.setQbitOption(otherQbitEntity.getQbit());
            setState(otherQbitEntity.getQbit().map(q -> gate.actOn(q)), state, world, pos, sourceBlock);
        }
        else{
            // thisQbitEntity.setQbitOption(Optional.empty());
            setState(Optional.empty(), state, world, pos, sourceBlock);
        }
        
        // if(pos == )
		// world.updateNeighborsExcept(pos, this, direction, null );
    }

    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.INVISIBLE;
    }
}
