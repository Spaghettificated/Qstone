package net.eli.elimod.quantum;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.block.WireOrientation;

public class QbitSourceBlock extends QbitBlock {
    public static final BooleanProperty POWERED = Properties.POWERED;

	public QbitSourceBlock(Settings settings) {
		super(settings);
		isTarget = false;
        this.setDefaultState(this.getDefaultState().with(POWERED, false));
	}
	@Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
        super.appendProperties(builder);
    }

	@Override
	protected MapCodec<? extends QbitBlock> getCodec() {
		return createCodec(QbitSourceBlock::new);
	}

	@Nullable
	@Override
	public BlockEntity createBlockEntity(BlockPos pos, BlockState state) {
		return new QbitEntity(pos, state, Qbit.ZERO);
	}

	@Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        if (!(world.getBlockEntity(pos) instanceof QbitEntity blockEntity)) {
            return super.onUse(state, world, pos, player, hit);
        }

		var dir = hit.getSide();
		if(player.isSneaking()){
			dir = dir.getOpposite();
		}

		// blockEntity.setQbit(Qbit.fromDirection(dir));
		// setState(Optional.of(Qbit.fromDirection(dir)), state, world, pos, this);
		blockEntity.setQbit(Qbit.fromDirection(dir));
		world.updateListeners(pos, state, state, 0);
        player.sendMessage(Text.literal( "Set the qbit to " + blockEntity.getState().get().toString() ), true);

        return ActionResult.SUCCESS;
    }

	protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
		if (world instanceof ServerWorld serverWorld) {
			this.update(state, serverWorld, pos);
		}
	}

	protected void update(BlockState state, World world, BlockPos pos) {
		boolean bl = world.isReceivingRedstonePower(pos);
		boolean powered = state.get(POWERED);
		// System.out.println("source update");
		if (bl != powered) {
			if (bl) {
				// System.out.println("sending qbit");
				if (passQbit(state, world, pos)){
					world.playSound((Entity)null, pos, SoundEvents.BLOCK_COPPER_BULB_TURN_ON , SoundCategory.BLOCKS);
					// System.out.println("qbit sent");
				}
				// else System.out.println("qbit not sent");
			}

			world.setBlockState(pos, state.with(POWERED, bl), 3);
		}
		// System.out.println("source update ended\n");
    }
}
