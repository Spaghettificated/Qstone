package net.eli.elimod.quantum;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import net.minecraft.block.BlockState;
import net.minecraft.block.BlockWithEntity;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class QbitSourceBlock extends QbitBlock {
	public QbitSourceBlock(Settings settings) {
		super(settings);
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
		setState(Optional.of(Qbit.fromDirection(dir)), state, world, pos, this);
        player.sendMessage(Text.literal( "Set the qbit to " + blockEntity.getState().get().toString() ), true);

        return ActionResult.SUCCESS;
    }

}
